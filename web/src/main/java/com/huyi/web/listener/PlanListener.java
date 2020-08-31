package com.huyi.web.listener;

import com.huyi.common.utils.JsonUtils;
import com.huyi.web.config.RabbitMqConfig;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.handle.PlanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @Author huyi @Date 2020/8/26 21:43 @Description: 计划监听器 */
@Component
public class PlanListener {
  private static final Logger logger = LoggerFactory.getLogger(PlanListener.class);

  private static final ExecutorService EXECUTOR_SERVICE =
      Executors.newCachedThreadPool(new CustomizableThreadFactory("PLAN-LISTENER:"));
  @Autowired private PlanHandle planHandle;

  @RabbitListener(queues = RabbitMqConfig.PLAN_QUEUE, containerFactory = "planMqRabbitFactory")
  @RabbitHandler
  public void process(String message) {
    PlanEntity planEntity = JsonUtils.json2Bean(message, PlanEntity.class);
    EXECUTOR_SERVICE.execute(
        () -> {
          planHandle.dispatch(planEntity);
        });
  }
}
