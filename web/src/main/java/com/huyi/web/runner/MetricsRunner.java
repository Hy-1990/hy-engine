package com.huyi.web.runner;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.huyi.web.handle.PlanCacheHandle;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import com.huyi.web.handle.StopCacheHandle;
import com.huyi.web.workers.InputWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/31 9:02 @Description: 指标表 */
@Component
@Order(2)
public class MetricsRunner implements ApplicationRunner {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private PlanCacheHandle planCacheHandle;
  @Autowired private PlanHandle planHandlel;
  @Autowired private StopCacheHandle stopCacheHandle;
  @Autowired private RunningCacheHandle runningCacheHandle;

  private static final MetricRegistry REGISTRY = new MetricRegistry();

  private static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

  private static final ConsoleReporter REPORTER =
      ConsoleReporter.forRegistry(REGISTRY)
          .convertDurationsTo(TimeUnit.SECONDS)
          .convertRatesTo(TimeUnit.SECONDS)
          .build();

  @Override
  public void run(ApplicationArguments args) throws Exception {
    REGISTRY.register(
        MetricRegistry.name(PlanHandle.class, "PlanQueue"),
        (Gauge<String>) planHandlel::getPlanQueue);
    REGISTRY.register(
        MetricRegistry.name(PlanHandle.class, "workQueue"),
        (Gauge<String>) planHandlel::getTaskQueue);
    REGISTRY.register(
        MetricRegistry.name(PlanCacheHandle.class, "PlanCache"),
        (Gauge<String>) planCacheHandle::getCacheReport);
    REGISTRY.register(
        MetricRegistry.name(StopCacheHandle.class, "StopCache"),
        (Gauge<String>) stopCacheHandle::getReport);
    REGISTRY.register(
        MetricRegistry.name(RunningCacheHandle.class, "RunningCache"),
        (Gauge<String>) runningCacheHandle::getReport);

    // 健康检查
    HEALTH_CHECK_REGISTRY.register("InputHealth", new InputWorker());
    HEALTH_CHECK_REGISTRY.register("InputDeadLock", new ThreadDeadlockHealthCheck());

    REGISTRY.gauge("HY-METRICS", () -> HEALTH_CHECK_REGISTRY::runHealthChecks);

    REPORTER.start(10, TimeUnit.SECONDS);
  }
}
