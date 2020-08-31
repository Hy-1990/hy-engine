package com.huyi.web.listener;

import com.huyi.common.utils.JsonUtils;
import com.huyi.web.config.RabbitMqConfig;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.handle.TaskHandle;
import com.huyi.web.service.inf.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @Author huyi @Date 2020/8/25 13:54 @Description: 任务mq监听器 */
@Component
public class TaskListener {
  private static final Logger logger = LoggerFactory.getLogger(TaskListener.class);
  @Autowired private TaskHandle taskHandle;

  private static final ExecutorService EXECUTOR_SERVICE =
      Executors.newCachedThreadPool(new CustomizableThreadFactory("TASK-LISTENER:"));

  @RabbitListener(queues = RabbitMqConfig.TASK_QUEUE, containerFactory = "taskMqRabbitFactory")
  @RabbitHandler
  public void process(String message) {
    TaskEntity taskEntity = JsonUtils.json2Bean(message, TaskEntity.class);
    EXECUTOR_SERVICE.execute(
        () -> {
          taskHandle.dispatch(taskEntity);
        });
  }
}
