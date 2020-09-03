package com.huyi.web.handle;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Monitor;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.PlanCode;
import com.huyi.web.enums.PlanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

/** @Author huyi @Date 2020/8/26 21:52 @Description: 计划控制类 */
@Component
public class PlanHandle {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static Monitor monitor = new Monitor();

  @Autowired private PlanCacheHandle planCacheHandle;
  @Autowired private StopCacheHandle stopCacheHandle;
  @Autowired private RunningCacheHandle runningCacheHandle;

  public static ConcurrentHashMap<Integer, List<TaskEntity>> workQueue;

  public static ConcurrentHashMap<Integer, LinkedList<PlanEntity>> planQueue;

  public static ConcurrentHashMap<Integer, List<ReportEntity>> reportQueue;

  public static StampedLock workLock;

  public static StampedLock planLock;

  public static StampedLock reportLock;

  @PostConstruct
  public void init() {
    workQueue = new ConcurrentHashMap<>();
    planQueue = new ConcurrentHashMap<>();
    reportQueue = new ConcurrentHashMap<>();
    workLock = new StampedLock();
    planLock = new StampedLock();
    reportLock = new StampedLock();
  }

  public void dispatch(PlanEntity planEntity) {
    if (EmptyUtil.isEmpty(planEntity)
        || EmptyUtil.isEmpty(planEntity.getPlanId())
        || EmptyUtil.isEmpty(planEntity.getUserId())) {
      return;
    }
    if (EmptyUtil.isEmpty(planEntity.getLevel())) {
      planEntity.setLevel(0);
    }
    if (EmptyUtil.isEmpty(planEntity.getPlanTime())) {
      planEntity.setPlanTime(System.currentTimeMillis());
    }
    AtomicBoolean result = new AtomicBoolean(false);
    AtomicInteger reTry = new AtomicInteger(0);
    StringBuffer msg = new StringBuffer();
    String key = planEntity.getUserId() + "";
    while (true) {
      if (monitor.enterIf(monitor.newGuard(() -> reTry.get() < 3))) {
        try {
          boolean hasRun =
              planCacheHandle.tryLockAndRun(
                  key,
                  10,
                  TimeUnit.SECONDS,
                  () -> {
                    LinkedList<PlanEntity> plans = planCacheHandle.get(key);
                    LinkedList<PlanEntity> queue = new LinkedList<>();
                    // 空任务，创建
                    if (EmptyUtil.isEmpty(plans)) {
                      if (planEntity.getStatus().equals(PlanType.READY.getCode())) {
                        if (!runningCacheHandle.checkRunning(planEntity)
                            && !stopCacheHandle.checkStop(planEntity)) {
                          result.set(queue.add(planEntity));
                          planCacheHandle.put(key, queue);
                          msg.append(PlanCode.CREATE_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.CREATE_EXIST.getMsg());
                        }
                      } else if (planEntity.getStatus().equals(PlanType.STOP.getCode())) {
                        boolean stop = stopCacheHandle.saveStop(planEntity);
                        boolean removeRun = runningCacheHandle.removeRunning(planEntity);
                        if (stop && removeRun) {
                          result.set(true);
                          msg.append(PlanCode.STOP_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.STOP_FAILED.getMsg());
                        }
                        // 恢复暂停任务，从暂停队列排除，添加进运行队列
                      } else if (planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
                        boolean remove = stopCacheHandle.removeStop(planEntity);
                        boolean reRun = runningCacheHandle.saveRunning(planEntity);
                        if (remove && reRun) {
                          result.set(true);
                          msg.append(PlanCode.RERUN_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.RERUN_FAILED.getMsg());
                        }
                      } else {
                        result.set(false);
                        msg.append(PlanCode.CREATE_INVALID.getMsg());
                      }
                    } else {
                      // 创建新任务
                      if (planEntity.getStatus().equals(PlanType.READY.getCode())) {
                        if (plans.stream()
                                .noneMatch(p -> p.getPlanId().equals(planEntity.getPlanId()))
                            && !runningCacheHandle.checkRunning(planEntity)
                            && !stopCacheHandle.checkStop(planEntity)) {
                          result.set(plans.add(planEntity));
                          queue =
                              plans.stream()
                                  .sorted(Comparator.comparing(PlanEntity::getPlanTime))
                                  .sorted(Comparator.comparing(PlanEntity::getLevel).reversed())
                                  .sorted(Comparator.comparing(PlanEntity::getStatus))
                                  .collect(Collectors.toCollection(LinkedList::new));
                          planCacheHandle.put(key, queue);
                          msg.append(PlanCode.CREATE_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.CREATE_EXIST.getMsg());
                        }
                        // 暂停任务保存进暂停队列，从运行队列排除
                      } else if (planEntity.getStatus().equals(PlanType.STOP.getCode())) {
                        boolean stop = stopCacheHandle.saveStop(planEntity);
                        boolean removeRun = runningCacheHandle.removeRunning(planEntity);
                        if (stop && removeRun) {
                          result.set(true);
                          msg.append(PlanCode.STOP_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.STOP_FAILED.getMsg());
                        }
                        // 恢复暂停任务，从暂停队列排除，添加进运行队列
                      } else if (planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
                        boolean remove = stopCacheHandle.removeStop(planEntity);
                        boolean reRun = runningCacheHandle.saveRunning(planEntity);
                        if (remove && reRun) {
                          result.set(true);
                          msg.append(PlanCode.RERUN_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.RERUN_FAILED.getMsg());
                        }
                      } else {
                        result.set(false);
                        msg.append(PlanCode.PARAM_INVALID.getMsg());
                      }
                    }
                  });
          if (hasRun) {
            break;
          }
        } finally {
          monitor.leave();
          reTry.incrementAndGet();
        }
      } else {
        break;
      }
    }

    if (result.get()) {
      logger.info(msg.toString());
    } else {
      logger.error(msg.toString());
    }
  }

  public void savePlan(Integer userId, LinkedList<PlanEntity> plans) {
    long stamped = planLock.writeLock();
    try {
      planQueue.put(userId, plans);
    } finally {
      planLock.unlockWrite(stamped);
    }
  }

  public LinkedList<PlanEntity> getPlan(Integer userId) {
    long stamped = planLock.readLock();
    try {
      return planQueue.get(userId);
    } finally {
      planLock.unlockRead(stamped);
    }
  }

  public void removePlan(Integer userId) {
    long stamped = planLock.writeLock();
    try {
      planQueue.remove(userId);
    } finally {
      planLock.unlockWrite(stamped);
    }
  }

  public void deletePlan(PlanEntity planEntity) {
    if (EmptyUtil.isEmpty(planEntity)
        || EmptyUtil.isEmpty(planEntity.getUserId())
        || EmptyUtil.isEmpty(planEntity.getPlanId())) {
      return;
    }
    long stamped = planLock.writeLock();
    try {
      LinkedList<PlanEntity> plans = planQueue.get(planEntity.getUserId());
      if (EmptyUtil.isEmpty(plans)) {
        return;
      }
      plans =
          plans.stream()
              .filter(p -> !p.getPlanId().equals(planEntity.getPlanId()))
              .collect(Collectors.toCollection(LinkedList::new));
      planQueue.putIfAbsent(planEntity.getUserId(), plans);
    } finally {
      planLock.unlockWrite(stamped);
    }
  }

  public boolean changePlanStatus(PlanEntity planEntity, PlanType planType) {
    if (EmptyUtil.isEmpty(planEntity)) {
      return false;
    }
    long stamped = planLock.writeLock();
    try {
      System.out.println(planQueue.toString());
      if (planQueue.containsKey(planEntity.getUserId())) {
        LinkedList<PlanEntity> linkedList = planQueue.get(planEntity.getUserId());
        linkedList =
            linkedList.stream()
                .peek(
                    x -> {
                      if (x.getPlanId().equals(planEntity.getPlanId())) {
                        x.setStatus(planType.getCode());
                      }
                    })
                .collect(Collectors.toCollection(LinkedList::new));
        planQueue.put(planEntity.getUserId(), linkedList);
        return true;
      } else {
        return false;
      }
    } finally {
      planLock.unlockWrite(stamped);
    }
  }

  public void saveReport(Integer planId, List<ReportEntity> reports) {
    long stamped = reportLock.writeLock();
    try {
      reportQueue.put(planId, reports);
    } finally {
      reportLock.unlockWrite(stamped);
    }
  }

  public void removeReport(Integer planId) {
    long stamped = reportLock.writeLock();
    try {
      reportQueue.remove(planId);
    } finally {
      reportLock.unlockWrite(stamped);
    }
  }

  public List<ReportEntity> getReport(Integer planId) {
    long stamped = reportLock.readLock();
    try {
      return reportQueue.get(planId);
    } finally {
      reportLock.unlockRead(stamped);
    }
  }

  public void removeWorkQueue(Integer planId) {
    long stamped = workLock.writeLock();
    try {
      workQueue.remove(planId);
    } finally {
      workLock.unlockWrite(stamped);
    }
  }

  public void saveWorkQueue(Integer planId, List<TaskEntity> tasks) {
    long stamped = workLock.writeLock();
    try {
      workQueue.put(planId, tasks);
    } finally {
      workLock.unlockWrite(stamped);
    }
  }

  public List<TaskEntity> getWorkQueue(Integer planId) {
    long stamped = workLock.readLock();
    try {
      return workQueue.get(planId);
    } finally {
      workLock.unlockRead(stamped);
    }
  }

  public String getPlanQueue() {
    if (planQueue.size() == 0) {
      return "";
    } else {
      List<String> plans = new ArrayList<>();
      planQueue.forEach(
          (k, v) -> {
            plans.add(
                "userId:"
                    + k
                    + ",plans:{size:"
                    + v.size()
                    + ","
                    + Joiner.on(",")
                        .join(
                            Collections.singleton(
                                v.stream()
                                    .collect(
                                        Collectors.toMap(
                                            PlanEntity::getPlanId, PlanEntity::getTaskAmount))
                                    .toString()))
                    + "}");
          });
      return "计划队列详情：" + Joiner.on(";").join(plans);
    }
  }

  public String getTaskQueue() {
    if (workQueue.size() == 0) {
      return "";
    } else {
      List<String> works = new ArrayList<>();
      workQueue.forEach(
          (k, v) -> {
            works.add(
                "planId:"
                    + k
                    + ",works:{size:"
                    + v.size()
                    + ","
                    + Joiner.on(",")
                        .join(v.stream().map(TaskEntity::getTaskId).collect(Collectors.toList()))
                    + "}");
          });
      return "任务队列详情：" + Joiner.on(";").join(works);
    }
  }

  public String getReportQueue() {
    if (reportQueue.size() == 0) {
      return "";
    } else {
      List<String> reports = new ArrayList<>();
      reportQueue.forEach(
          (k, v) -> {
            reports.add(
                "planId:"
                    + k
                    + ",reportId:{size:"
                    + v.size()
                    + ","
                    + Joiner.on(",")
                        .join(v.stream().map(ReportEntity::getTaskId).collect(Collectors.toList()))
                    + "}");
          });
      return "任务完成队列报表：" + Joiner.on(";").join(reports);
    }
  }

  public PlanEntity findPlan() {
    AtomicReference<PlanEntity> result = new AtomicReference<PlanEntity>();
    for (Map.Entry<Integer, LinkedList<PlanEntity>> entry : planQueue.entrySet()) {
      LinkedList<PlanEntity> plans = entry.getValue();
      Optional<PlanEntity> plan =
          plans.stream()
              .filter(
                  p ->
                      p.getStatus().equals(PlanType.READY.getCode())
                          || p.getStatus().equals(PlanType.STOP.getCode()))
              .filter(
                  y ->
                      (runningCacheHandle.get(RedisConstant.RUNNING_KEY) != null
                              && runningCacheHandle.get(RedisConstant.RUNNING_KEY).stream()
                                  .anyMatch(x -> x.getPlanId().equals(y.getPlanId())))
                          && (stopCacheHandle.get(RedisConstant.STOP_KEY) == null
                              || stopCacheHandle.get(RedisConstant.STOP_KEY).stream()
                                  .noneMatch(z -> y.getPlanId().equals(z))))
              .findFirst();
      if (plan.isPresent()) {
        result.set(plan.get());
        break;
      }
    }
    return result.get();
  }
}
