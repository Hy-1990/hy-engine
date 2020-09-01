package com.huyi.web.workers;

import com.codahale.metrics.health.HealthCheck;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.handle.PlanHandle;
import com.huyi.web.handle.RunningCacheHandle;
import com.huyi.web.handle.StopCacheHandle;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @Author huyi @Date 2020/9/1 18:58 @Description: */
@NoArgsConstructor
public class DoctorWorker extends HealthCheck implements Runnable {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private PlanEntity planEntity;
  private PlanHandle planHandle;
  private RunningCacheHandle runningCacheHandle;
  private StopCacheHandle stopCacheHandle;

  public DoctorWorker(
      PlanEntity planEntity,
      PlanHandle planHandle,
      RunningCacheHandle runningCacheHandle,
      StopCacheHandle stopCacheHandle) {
    this.planEntity = planEntity;
    this.planHandle = planHandle;
    this.runningCacheHandle = runningCacheHandle;
    this.stopCacheHandle = stopCacheHandle;
  }

  @Override
  public void run() {}

  @Override
  protected Result check() throws Exception {
    return null;
  }
}
