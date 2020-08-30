package com.huyi.web.runner;

import com.huyi.common.utils.EmptyUtil;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.workers.InputWorker;
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
    ThreadPoolConfig.inputPool.scheduleAtFixedRate(
        new InputWorker(planCacheHandle, redisUtil), 0, 15, TimeUnit.SECONDS);
  }
}
