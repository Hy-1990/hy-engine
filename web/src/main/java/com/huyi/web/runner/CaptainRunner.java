package com.huyi.web.runner;

import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.enums.PlanType;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import com.huyi.web.handle.StopCacheHandle;
import com.huyi.web.handle.TaskCacheHandle;
import com.huyi.web.service.impl.TaskServiceImpl;
import com.huyi.web.service.inf.TaskService;
import com.huyi.web.workers.CrewWorker;
import com.huyi.web.workers.DoctorWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Program: hy-engine @ClassName: CaptainRunner @Author: huyi @Date: 2020-08-31 23:53 @Description:
 * 核心引擎 @Version: V1.0
 */
@Component
@Order(3)
public class CaptainRunner implements ApplicationRunner {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private PlanHandle planHandle;
  @Autowired private RunningCacheHandle runningCacheHandle;
  @Autowired private StopCacheHandle stopCacheHandle;
  @Autowired private TaskCacheHandle taskCacheHandle;
  @Autowired private TaskServiceImpl taskService;
  private static boolean powerSwitch = false;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("核心引擎启动!");
    while (true) {
      PlanEntity planEntity = planHandle.findPlan();
      if (EmptyUtil.isEmpty(planEntity)) {
        TimeUnit.SECONDS.sleep(10);
        logger.info("计划队列暂时无可工作计划，引擎空闲10秒钟。");
        continue;
      }

      if (planEntity.getStatus().equals(PlanType.READY.getCode())) {
        ThreadPoolConfig.captainPool.execute(
            new CrewWorker(
                planEntity, planHandle, runningCacheHandle, stopCacheHandle, taskCacheHandle,taskService));
        logger.info("分配[planId:{}]计划进入工作池!", planEntity.getPlanId());
      }

      if (planEntity.getStatus().equals(PlanType.STOP.getCode())) {
        ThreadPoolConfig.captainPool.execute(
            new DoctorWorker(planEntity, planHandle, runningCacheHandle, stopCacheHandle));
        logger.info("恢复[planId:{}]计划进入工作池!", planEntity.getPlanId());
      }
      if (powerSwitch) {
        break;
      }
    }
  }

  public void close() {
    powerSwitch = true;
    logger.info("核心引擎关闭!");
  }
}
