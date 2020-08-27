package com.huyi.web.runner;

import com.huyi.common.utils.EmptyUtil;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 10:32 @Description: 监视器 */
@Component
@Order(1)
public class MonitorRunner implements ApplicationRunner {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private PlanCacheHandle planCacheHandle;
  @Autowired private RedisUtil redisUtil;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    new Thread(
            () -> {
              while (true) {
                try {
                  TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                Set<String> keys = redisUtil.getAllKeyMatch(RedisConstant.PLAN_PREFIX);
                System.out.println(keys);
                keys.forEach(
                    (x) -> {
                      planCacheHandle
                          .get(x.split("-")[2])
                          .forEach((y) -> logger.info("监控缓存数据：{}", y));
                    });
                System.out.println("---------------------------------");
              }
            })
        .start();

    new Thread(
            () -> {
              while (true) {
                try {
                  TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                boolean hasRun =
                    planCacheHandle.tryLockAndRun(
                        "1",
                        10,
                        TimeUnit.SECONDS,
                        () -> {
                          LinkedBlockingQueue<PlanEntity> plans = planCacheHandle.get("1");
                          PlanEntity planEntity = plans.poll();
                          if (EmptyUtil.isNotEmpty(planEntity)) {
                            PlanHandle.workQueue.offer(planEntity);
                            planCacheHandle.put("1", plans);
                          }
                        });
                if (hasRun) {
                  PlanHandle.workQueue.forEach(
                      (x) -> {
                        logger.info("工作队列数据：{}", x);
                      });
                } else {
                  logger.info("工作队列数据拉取失败！");
                }
                System.out.println("+++++++++++++++++++++++++++++++++");
              }
            })
        .start();
    logger.info("缓存监视器启动！");
  }
}
