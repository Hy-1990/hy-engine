package com.huyi.web.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** @Author huyi @Date 2020/8/25 13:30 @Description: rabbitmq 配置类 */
@Configuration
public class RabbitMqConfig {
  public static final String TASK_QUEUE = "HY-TASK";
  public static final String PLAN_QUEUE = "HY-PLAN";

  @Bean("TASK_QUEUE")
  Queue taskQueue() {
    return new Queue(TASK_QUEUE, true);
  }

  @Bean("PLAN_QUEUE")
  Queue planQueue() {
    return new Queue(PLAN_QUEUE, true);
  }

  @Bean("taskMqRabbitFactory")
  public SimpleRabbitListenerContainerFactory taskMqRabbitFactory(
      SimpleRabbitListenerContainerFactoryConfigurer configure,
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    // 设置线程数
    factory.setConcurrentConsumers(15);
    // 最大线程数
    factory.setMaxConcurrentConsumers(60);

    configure.configure(factory, connectionFactory);
    return factory;
  }

  @Bean("planMqRabbitFactory")
  public SimpleRabbitListenerContainerFactory planMqRabbitFactory(
      SimpleRabbitListenerContainerFactoryConfigurer configure,
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    // 设置线程数
    factory.setConcurrentConsumers(15);
    // 最大线程数
    factory.setMaxConcurrentConsumers(60);

    configure.configure(factory, connectionFactory);
    return factory;
  }
}
