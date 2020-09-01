package com.huyi.web.service.inf;

import com.google.common.util.concurrent.RateLimiter;
import com.huyi.common.dto.HYResult;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.entity.TaskEntity;

/** @Author huyi @Date 2020/8/25 13:50 @Description: 任务功能接口 */
public interface TaskService {
  boolean saveTask(TaskEntity entity);

  HYResult<ReportEntity> doTask(TaskEntity taskEntity, RateLimiter rateLimiter);
}
