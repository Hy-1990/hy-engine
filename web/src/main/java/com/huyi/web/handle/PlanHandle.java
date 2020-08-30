package com.huyi.web.handle;

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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** @Author huyi @Date 2020/8/26 21:52 @Description: 计划控制类 */
@Component
public class PlanHandle {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static Monitor monitor = new Monitor();

  @Autowired private PlanCacheHandle planCacheHandle;
  @Autowired private StopCacheHandle stopCacheHandle;
  @Autowired private TaskCacheHandle taskCacheHandle;

  public static ConcurrentHashMap<Integer, List<TaskEntity>> workQueue;

  public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, PlanEntity>> planQueue;

  @PostConstruct
  public void init() {
    workQueue = new ConcurrentHashMap<>();
    planQueue = new ConcurrentHashMap<>();
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
                        result.set(queue.add(planEntity));
                        planCacheHandle.put(key, queue);
                        msg.append(PlanCode.CREATE_SUCCESS.getMsg());
                      } else {
                        result.set(false);
                        msg.append(PlanCode.CREATE_INVALID.getMsg());
                      }
                    } else {
                      if (planEntity.getStatus().equals(PlanType.READY.getCode())) {
                        if (!plans.contains(planEntity)) {
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
                      } else if (planEntity.getStatus().equals(PlanType.STOP.getCode())) {
                        boolean stop = saveStop(planEntity);
                        if (stop) {
                          result.set(true);
                          msg.append(PlanCode.STOP_SUCCESS.getMsg());
                        } else {
                          result.set(false);
                          msg.append(PlanCode.STOP_FAILED.getMsg());
                        }
                      } else if (planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
                        boolean remove = removeStop(planEntity);
                        if (remove) {
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
    Set<Integer> stopSet = stopCacheHandle.get(RedisConstant.STOP_KEY);
    if (EmptyUtil.isEmpty(stopSet)) {
      Set<Integer> stopPlanId = new HashSet<>();
      stopPlanId.add(planEntity.getPlanId());
      stopCacheHandle.put(RedisConstant.STOP_KEY, stopPlanId);
    } else {
      stopSet.add(planEntity.getPlanId());
    }
    return true;
  }

  public boolean removeStop(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.RUNNING.getCode())) {
      return false;
    }
    Set<Integer> stopList = stopCacheHandle.get(RedisConstant.STOP_KEY);
    if (EmptyUtil.isEmpty(stopList)) {
      return false;
    } else {
      return stopList.remove(planEntity.getPlanId());
    }
  }
}
