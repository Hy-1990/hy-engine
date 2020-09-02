package com.huyi.web.runner;

import com.huyi.common.utils.RedisUtil;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import com.huyi.web.workers.InputWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 10:32 @Description: 监视器 */
@Component
@Order(2)
public class InitRunner implements ApplicationRunner {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private PlanCacheHandle planCacheHandle;
  @Autowired private RedisUtil redisUtil;
  @Autowired private PlanHandle planHandle;
  @Autowired private RunningCacheHandle runningCacheHandle;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    ThreadPoolConfig.inputPool.scheduleAtFixedRate(
        new InputWorker(planCacheHandle, redisUtil, planHandle, runningCacheHandle),
        0,
        15,
        TimeUnit.SECONDS);
    logger.info("HY-推送引擎启动！");
    TimeUnit.SECONDS.sleep(15);
  }
}
