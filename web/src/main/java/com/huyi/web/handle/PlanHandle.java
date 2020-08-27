package com.huyi.web.handle;

import com.google.common.util.concurrent.Monitor;
import com.huyi.common.log.HyLogger;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.entity.PlanEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
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

  public static LinkedBlockingQueue<PlanEntity> workQueue;

  @PostConstruct
  public void init() {
    workQueue = new LinkedBlockingQueue<>();
  }

  public void savePlan(PlanEntity planEntity) {
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
                    LinkedBlockingQueue<PlanEntity> plans = planCacheHandle.get(key);
                    LinkedBlockingQueue<PlanEntity> queue = new LinkedBlockingQueue<>();
                    if (EmptyUtil.isEmpty(plans)) {
                      result.set(queue.offer(planEntity));
                      planCacheHandle.put(key, queue);
                    } else {
                      result.set(plans.offer(planEntity));
                      HyLogger.logger().warn("huyi测试断电1：{}", plans.toString());
                      queue =
                          plans.stream()
                              .sorted(Comparator.comparing(PlanEntity::getPlanTime))
                              .sorted(Comparator.comparing(PlanEntity::getLevel).reversed())
                              .collect(Collectors.toCollection(LinkedBlockingQueue::new));
                      HyLogger.logger().warn("huyi测试断电2：{}", queue.toString());
                      planCacheHandle.put(key, queue);
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
      logger.info("保存计划进缓存成功！");
    } else {
      logger.error("保存九华进缓存失败!");
    }
  }
}
