package com.huyi.web.workers;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.RateLimiter;
import com.huyi.common.dto.HYResult;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.PlanType;
import com.huyi.web.handle.*;
import com.huyi.web.service.impl.TaskServiceImpl;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** @Author huyi @Date 2020/9/1 18:58 @Description: */
@NoArgsConstructor
public class DoctorWorker extends HealthCheck implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private PlanEntity planEntity;
  private PlanHandle planHandle;
  private RunningCacheHandle runningCacheHandle;
  private StopCacheHandle stopCacheHandle;
  private TaskCacheHandle taskCacheHandle;
  private TaskServiceImpl taskService;
  private ReportHandle reportHandle;

  private static Monitor monitor = new Monitor();
  // 暂停缓存监视器
  private static Monitor stopMonitor = new Monitor();
  // 是否被暂停
  private static boolean ifStop = false;

  public DoctorWorker(
      PlanEntity planEntity,
      PlanHandle planHandle,
      RunningCacheHandle runningCacheHandle,
      StopCacheHandle stopCacheHandle,
      TaskCacheHandle taskCacheHandle,
      TaskServiceImpl taskService,
      ReportHandle reportHandle) {
    this.planEntity = planEntity;
    this.planHandle = planHandle;
    this.runningCacheHandle = runningCacheHandle;
    this.stopCacheHandle = stopCacheHandle;
    this.taskCacheHandle = taskCacheHandle;
    this.taskService = taskService;
    this.reportHandle = reportHandle;
  }

  @Override
  public void run() {
    try {
      logger.info("计划ID:{},开始恢复工作！", planEntity.getPlanId());
      boolean ready =
          monitor.enterWhen(
              new Monitor.Guard(monitor) {
                @Override
                public boolean isSatisfied() {
                  return taskCacheHandle.get(planEntity.getPlanId() + "") != null
                      && taskCacheHandle.get(planEntity.getPlanId() + "").size()
                          >= planEntity.getTaskAmount();
                }
              },
              30,
              TimeUnit.MINUTES);
      if (ready) {
        try {
          List<TaskEntity> tasks = planHandle.getWorkQueue(planEntity.getPlanId());
          List<ReportEntity> reports = planHandle.getReport(planEntity.getPlanId());
          // 如果本地内存中task任务为空，则重新从缓存中取出task数据，防止断电重启数据丢失情况。
          if (EmptyUtil.isEmpty(tasks)) {
            planHandle.saveWorkQueue(
                planEntity.getPlanId(),
                taskCacheHandle.get(planEntity.getPlanId() + "").stream()
                    .limit(planEntity.getTaskAmount())
                    .collect(Collectors.toList()));
            tasks = planHandle.getWorkQueue(planEntity.getPlanId());
          }
          // 如果本地内存中暂停任务之前做过,过滤已经做过的任务。
          if (EmptyUtil.isNotEmpty(reports)) {
            tasks =
                tasks.stream()
                    .filter(
                        t -> reports.stream().noneMatch(r -> r.getTaskId().equals(t.getTaskId())))
                    .collect(Collectors.toList());
          }

          // 开始批量完成任务
          RateLimiter rateLimiter = RateLimiter.create(Double.valueOf(planEntity.getRobotSize()));
          List<CompletableFuture<HYResult<ReportEntity>>> result = new ArrayList<>();
          for (TaskEntity task : tasks) {
            if (stopMonitor.enterIf(
                stopMonitor.newGuard(
                    () ->
                        (stopCacheHandle.get(RedisConstant.STOP_KEY) == null
                            || stopCacheHandle.get(RedisConstant.STOP_KEY).stream()
                                .noneMatch(t -> task.getPlanId().equals(t)))))) {
              try {
                result.add(
                    CompletableFuture.supplyAsync(
                        () -> taskService.doTask(task, rateLimiter), ThreadPoolConfig.crewPool));
              } finally {
                stopMonitor.leave();
              }
            } else {
              ifStop = true;
              break;
            }
          }
          // 处理任务完成结果
          List<ReportEntity> supplyReports = new ArrayList<>();
          result.forEach(
              x -> {
                try {
                  supplyReports.add(x.get().getData());
                } catch (InterruptedException | ExecutionException e) {
                  logger.error("工作异常:{}", e.getMessage());
                }
              });

          if (EmptyUtil.isNotEmpty(reports)) {
            supplyReports.addAll(reports);
          }
          List<ReportEntity> finalLastReports = supplyReports;

          if (ifStop && supplyReports.size() < tasks.size()) {
            // 如果被暂停，则存回结果队列，修改计划为暂停状态
            planHandle.saveReport(planEntity.getPlanId(), finalLastReports);
            planHandle.changePlanStatus(planEntity, PlanType.STOP);
            // 过滤没有跑完的task
            tasks =
                tasks.stream()
                    .filter(
                        t ->
                            finalLastReports.stream()
                                .noneMatch(r -> r.getTaskId().equals(t.getTaskId())))
                    .collect(Collectors.toList());
            planHandle.saveWorkQueue(planEntity.getPlanId(), tasks);
            logger.info("计划ID:{},已暂停并保存!", planEntity.getPlanId());
          } else {
            // 如果成功，则存到结果报告队列，修改计划为完成状态，删除运行内存中的计划
            reportHandle.saveReport(planEntity.getPlanId(), finalLastReports);
            runningCacheHandle.removeRunning(planEntity);
            planHandle.changePlanStatus(planEntity, PlanType.FINISHED);
            planHandle.removeWorkQueue(planEntity.getPlanId());
            taskCacheHandle.remove(planEntity.getPlanId() + "");
            stopCacheHandle.removeStop(planEntity);
            planHandle.removeReport(planEntity.getPlanId());
            logger.info("计划ID:{},已完成!", planEntity.getPlanId());
          }
        } catch (Exception e) {
          e.printStackTrace();
          logger.error("计划ID:{},工作异常:{}", planEntity.getPlanId(), e.getMessage());
        } finally {
          monitor.leave();
        }
      } else {
        logger.error("计划：{}，因为任务30分钟内没有全部上送到缓存爆破！", planEntity.getPlanId());
        planHandle.deletePlan(planEntity);
        planHandle.removeWorkQueue(planEntity.getPlanId());
        taskCacheHandle.remove(planEntity.getPlanId() + "");
        monitor.leave();
      }
    } catch (InterruptedException e) {
      logger.error("计划：{}，在工作池内异常，异常内容：{}", planEntity.getPlanId(), e.getMessage());
    }
  }

  @Override
  protected Result check() throws Exception {
    return null;
  }
}
