package com.huyi.web.workers;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.RateLimiter;
import com.huyi.common.dto.HYResult;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import com.huyi.web.handle.StopCacheHandle;
import com.huyi.web.handle.TaskCacheHandle;
import com.huyi.web.service.impl.TaskServiceImpl;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** @Author huyi @Date 2020/9/1 18:23 @Description: 新任务船员类 */
@NoArgsConstructor
public class CrewWorker extends HealthCheck implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private PlanEntity planEntity;
  private PlanHandle planHandle;
  private RunningCacheHandle runningCacheHandle;
  private StopCacheHandle stopCacheHandle;
  private TaskCacheHandle taskCacheHandle;
  private TaskServiceImpl taskService;

  private static Monitor monitor = new Monitor();

  public CrewWorker(
      PlanEntity planEntity,
      PlanHandle planHandle,
      RunningCacheHandle runningCacheHandle,
      StopCacheHandle stopCacheHandle,
      TaskCacheHandle taskCacheHandle,
      TaskServiceImpl taskService) {
    this.planEntity = planEntity;
    this.planHandle = planHandle;
    this.runningCacheHandle = runningCacheHandle;
    this.stopCacheHandle = stopCacheHandle;
    this.taskCacheHandle = taskCacheHandle;
    this.taskService = taskService;
  }

  @Override
  public void run() {
    try {
      boolean ready =
          monitor.enterWhen(
              new Monitor.Guard(monitor) {
                @Override
                public boolean isSatisfied() {
                  return taskCacheHandle.get(planEntity.getPlanId() + "").size()
                      >= planEntity.getTaskAmount();
                }
              },
              30,
              TimeUnit.MINUTES);
      if (ready) {
        try {
          List<TaskEntity> tasks = new ArrayList<>();
          long workStamped = PlanHandle.workLock.writeLock();
          PlanHandle.workQueue.put(
              planEntity.getPlanId(),
              taskCacheHandle.get(planEntity.getPlanId() + "").stream()
                  .limit(planEntity.getTaskAmount())
                  .collect(Collectors.toList()));
          tasks = PlanHandle.workQueue.get(planEntity.getPlanId());
          PlanHandle.workLock.unlockWrite(workStamped);

          // 开始批量完成任务
          RateLimiter rateLimiter = RateLimiter.create(Double.valueOf(planEntity.getRobotSize()));
          List<CompletableFuture<HYResult<ReportEntity>>> result = new ArrayList<>();
          tasks.forEach(
              t -> {
                result.add(
                    CompletableFuture.supplyAsync(
                        () -> {
                          HYResult<ReportEntity> hyResult = taskService.doTask(t, rateLimiter);
                          return hyResult;
                        },
                        ThreadPoolConfig.crewPool));
              });

        } catch (Exception e) {
          logger.error("工作异常:{}", e.getMessage());
        } finally {
          monitor.leave();
        }
      } else {
        logger.error("计划：{}，因为任务30分钟内没有全部上送到缓存爆破！", planEntity.getPlanId());
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
