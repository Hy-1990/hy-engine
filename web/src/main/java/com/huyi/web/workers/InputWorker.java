package com.huyi.web.workers;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Joiner;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.enums.PlanType;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Program: hy-engine @ClassName: InputWorker @Author: huyi @Date: 2020-08-29 23:08 @Description:
 * 推送计划工作线程 @Version: V1.0
 */
@NoArgsConstructor
public class InputWorker extends HealthCheck implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private PlanCacheHandle planCacheHandle;
  private RedisUtil redisUtil;
  private PlanHandle planHandle;
  private RunningCacheHandle runningCacheHandle;
  private static final AtomicInteger FAIL_COUNT = new AtomicInteger(0);
  private static final ConcurrentSkipListSet<String> FAIL_REPORT = new ConcurrentSkipListSet<>();

  public InputWorker(
      PlanCacheHandle planCacheHandle,
      RedisUtil redisUtil,
      PlanHandle planHandle,
      RunningCacheHandle runningCacheHandle) {
    this.planCacheHandle = planCacheHandle;
    this.redisUtil = redisUtil;
    this.planHandle = planHandle;
    this.runningCacheHandle = runningCacheHandle;
  }

  @Override
  public void run() {
    try {
      AtomicBoolean hasPoll = new AtomicBoolean(false);
      Set<String> keys = redisUtil.getAllKeyMatch(RedisConstant.PLAN_PREFIX);
      for (String key : keys) {
        planCacheHandle.tryLockAndRun(
            key.split("-")[2],
            10,
            TimeUnit.SECONDS,
            () -> {
              LinkedList<PlanEntity> plans = planCacheHandle.get(key.split("-")[2]);
              if (EmptyUtil.isNotEmpty(plans)) {
                Optional<PlanEntity> newPlan =
                    plans.stream()
                        .filter(x -> x.getStatus().equals(PlanType.READY.getCode()))
                        .filter(y -> !runningCacheHandle.checkRunning(y))
                        .findFirst();
                newPlan.ifPresent(
                    p -> {
                      if (!PlanHandle.planQueue.containsKey(p.getUserId())) {
                        planHandle.savePlan(
                            p.getUserId(), new LinkedList<>(Collections.singletonList(p)));
                        planHandle.saveWorkQueue(p.getPlanId(), new ArrayList<>());
                        runningCacheHandle.saveRunning(p);
                        plans.remove(p);
                        planCacheHandle.put(key.split("-")[2], plans);
                        hasPoll.set(true);
                      } else {
                        LinkedList<PlanEntity> localPlans = planHandle.getPlan(p.getUserId());
                        if (localPlans.contains(p)) {
                          hasPoll.set(false);
                        } else {
                          localPlans.add(p);
                          localPlans =
                              localPlans.stream()
                                  .sorted(Comparator.comparing(PlanEntity::getPlanTime))
                                  .sorted(Comparator.comparing(PlanEntity::getLevel).reversed())
                                  .sorted(Comparator.comparing(PlanEntity::getStatus))
                                  .collect(Collectors.toCollection(LinkedList::new));
                          planHandle.savePlan(p.getUserId(), localPlans);
                          planHandle.saveWorkQueue(p.getPlanId(), new ArrayList<>());
                          runningCacheHandle.saveRunning(p);
                          plans.remove(p);
                          planCacheHandle.put(key.split("-")[2], plans);
                          hasPoll.set(true);
                        }
                      }
                    });
              }
            });
        if (hasPoll.get()) {
          break;
        }
      }
      if (hasPoll.get()) {
        logger.info("{}:推送dispatch计划完成！", LocalDateTime.now().toString());
        logger.info(PlanHandle.planQueue.toString());
      } else {
        logger.info("无可推送的数据！");
      }
    } catch (Exception e) {
      FAIL_REPORT.add(e.getMessage());
    }
  }

  @Override
  protected Result check() throws Exception {
    if (FAIL_COUNT.get() > 0) {
      return Result.unhealthy(
          "失败次数：" + FAIL_COUNT.get() + " 失败消息：" + Joiner.on(",").join(FAIL_REPORT));
    } else {
      return Result.healthy();
    }
  }
}
