package com.huyi.web.handle;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Monitor;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
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

  public static StampedLock workLock;

  public static StampedLock planLock;

  @PostConstruct
  public void init() {
    workQueue = new ConcurrentHashMap<>();
    planQueue = new ConcurrentHashMap<>();
    workLock = new StampedLock();
    planLock = new StampedLock();
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
                        if (!checkRunning(planEntity) && !checkStop(planEntity)) {
                          result.set(queue.add(planEntity));
                          planCacheHandle.put(key, queue);
                          msg.append(PlanCode.CREATE_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.CREATE_EXIST.getMsg());
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
                            && !checkRunning(planEntity)
                            && !checkStop(planEntity)) {
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
                        boolean stop = saveStop(planEntity);
                        boolean removeRun = removeRunning(planEntity);
                        if (stop && removeRun) {
                          result.set(true);
                          msg.append(PlanCode.STOP_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.STOP_FAILED.getMsg());
                        }
                        // 恢复暂停任务，从暂停队列排除，添加进运行队列
                      } else if (planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
                        boolean remove = removeStop(planEntity);
                        boolean reRun = saveRunning(planEntity);
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

  public boolean saveStop(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.STOP.getCode())) {
      return false;
    }
    AtomicBoolean isOk = new AtomicBoolean(false);
    stopCacheHandle.tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopSet = stopCacheHandle.get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isEmpty(stopSet)) {
            Set<Integer> stopPlanId = new HashSet<>();
            stopPlanId.add(planEntity.getPlanId());
            stopCacheHandle.put(RedisConstant.STOP_KEY, stopPlanId);
          } else {
            stopSet.add(planEntity.getPlanId());
            stopCacheHandle.put(RedisConstant.STOP_KEY, stopSet);
          }
          isOk.set(true);
        });
    return isOk.get();
  }

  public boolean removeStop(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
      return false;
    }
    AtomicBoolean isOk = new AtomicBoolean(false);
    stopCacheHandle.tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopList = stopCacheHandle.get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isNotEmpty(stopList)) {
            stopList.remove(planEntity.getPlanId());
          }
          isOk.set(true);
        });
    return isOk.get();
  }

  public boolean checkStop(PlanEntity planEntity) {
    AtomicBoolean isOk = new AtomicBoolean(false);
    stopCacheHandle.tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopPlanId = stopCacheHandle.get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isNotEmpty(stopPlanId)) {
            isOk.set(stopPlanId.contains(planEntity.getPlanId()));
          }
        });
    return isOk.get();
  }

  public boolean saveRunning(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.READY.getCode())) {
      return false;
    }
    AtomicBoolean isOk = new AtomicBoolean(false);
    runningCacheHandle.tryLockAndRun(
        RedisConstant.RUNNING_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<PlanEntity> runningSet = runningCacheHandle.get(RedisConstant.RUNNING_KEY);
          if (EmptyUtil.isEmpty(runningSet)) {
            Set<PlanEntity> runningPlanId = new HashSet<>();
            isOk.set(runningPlanId.add(planEntity));
            runningCacheHandle.put(RedisConstant.RUNNING_KEY, runningPlanId);
          } else {
            if (runningSet.stream().noneMatch(p -> p.getPlanId().equals(planEntity.getPlanId()))) {
              runningSet.add(planEntity);
              runningCacheHandle.put(RedisConstant.RUNNING_KEY, runningSet);
            }
            isOk.set(true);
          }
        });
    return isOk.get();
  }

  public boolean removeRunning(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.READY.getCode())) {
      return false;
    }
    AtomicBoolean isOk = new AtomicBoolean(false);
    runningCacheHandle.tryLockAndRun(
        RedisConstant.RUNNING_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<PlanEntity> runningList = runningCacheHandle.get(RedisConstant.RUNNING_KEY);
          if (EmptyUtil.isNotEmpty(runningList)) {
            boolean remove = false;
            runningList.removeIf(p -> p.getPlanId().equals(planEntity.getPlanId()));
          }
          isOk.set(true);
        });
    return isOk.get();
  }

  public boolean checkRunning(PlanEntity planEntity) {
    AtomicBoolean isOk = new AtomicBoolean(false);
    runningCacheHandle.tryLockAndRun(
        RedisConstant.RUNNING_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<PlanEntity> runningList = runningCacheHandle.get(RedisConstant.RUNNING_KEY);
          if (EmptyUtil.isNotEmpty(runningList)) {
            isOk.set(
                runningList.stream().anyMatch(x -> x.getPlanId().equals(planEntity.getPlanId())));
          }
        });
    return isOk.get();
  }

  public String getPlanQueue() {
    if (planQueue.size() == 0) {
      return "";
    } else {
      List<String> plans = new ArrayList<>();
      planQueue.forEach(
          (k, v) -> {
            plans.add("userId:" + k + ",plans:{" + v.toString() + "}");
          });
      return Joiner.on(";").join(plans);
    }
  }

  public String getTaskQueue() {
    if (workQueue.size() == 0) {
      return "";
    } else {
      List<String> works = new ArrayList<>();
      workQueue.forEach(
          (k, v) -> {
            works.add("planId:" + k + ",works:{" + v.toString() + "}");
          });
      return Joiner.on(";").join(works);
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
                      runningCacheHandle.get(RedisConstant.RUNNING_KEY).stream()
                              .anyMatch(x -> x.getPlanId().equals(y.getPlanId()))
                          && stopCacheHandle.get(RedisConstant.STOP_KEY).stream()
                              .noneMatch(z -> y.getPlanId().equals(z)))
              .findFirst();
      if (plan.isPresent()) {
        result.set(plan.get());
        break;
      }
    }
    return result.get();
  }
}
