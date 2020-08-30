package com.huyi.web.workers;

import com.huyi.common.utils.EmptyUtil;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.enums.PlanType;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Program: hy-engine @ClassName: InputWorker @Author: huyi @Date: 2020-08-29 23:08 @Description:
 * 推送计划工作线程 @Version: V1.0
 */
public class InputWorker implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private PlanCacheHandle planCacheHandle;
  private RedisUtil redisUtil;

  public InputWorker(PlanCacheHandle planCacheHandle, RedisUtil redisUtil) {
    this.planCacheHandle = planCacheHandle;
    this.redisUtil = redisUtil;
  }

  @Override
  public void run() {

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
                      .findFirst();
              newPlan.ifPresent(
                  p -> {
                    if (!PlanHandle.planQueue.containsKey(p.getUserId())) {
                      PlanHandle.planQueue.put(
                          p.getUserId(),
                          new ConcurrentHashMap<>(
                              Arrays.asList(p).stream()
                                  .collect(Collectors.toMap(PlanEntity::getPlanId, y -> y))));
                      PlanHandle.workQueue.put(p.getPlanId(), new ArrayList<>());
                      plans.remove(p);
                      planCacheHandle.put(key.split("-")[2], plans);
                      hasPoll.set(true);
                    } else {
                      ConcurrentHashMap<Integer, PlanEntity> localPlans =
                          PlanHandle.planQueue.get(p.getUserId());
                      if (localPlans.containsKey(p.getPlanId())) {
                        hasPoll.set(false);
                      } else {
                        localPlans.put(p.getPlanId(), p);
                        PlanHandle.workQueue.put(p.getPlanId(), new ArrayList<>());
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
      logger.info(planCacheHandle.get("1").toString());
      logger.info(PlanHandle.planQueue.toString());
    } else {
      logger.info("无可推送的数据！");
    }
    System.out.println("+++++++++++++++++++++++++++++++++");
  }
}
