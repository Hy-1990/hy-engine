package com.huyi.web.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.huyi.common.dto.HYResult;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.TaskType;
import com.huyi.web.handle.CacheHandle;
import com.huyi.web.service.inf.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static java.util.concurrent.ThreadLocalRandom.current;

/** @Author huyi @Date 2020/8/25 13:50 @Description: 任务功能接口实现 */
@Service
public class TaskServiceImpl implements TaskService {
  private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

  @Autowired private CacheHandle cacheHandle;

  @Override
  public boolean saveTask(TaskEntity entity) {

    return false;
  }

  @Override
  public HYResult<ReportEntity> doTask(TaskEntity taskEntity, RateLimiter rateLimiter) {
    rateLimiter.acquire();
    HYResult<ReportEntity> hyResult = new HYResult<>();
    if (EmptyUtil.isEmpty(taskEntity.getPlanId()) || EmptyUtil.isEmpty(taskEntity.getTaskId())) {
      hyResult = hyResult.error("task异常");
      hyResult.setData(ReportEntity.builder().status(TaskType.EXCEPTION.getCode()).build());
      return hyResult;
    }
    if (taskEntity.getStatus().equals(TaskType.RUNNING.getCode())) {
      long amount =
          taskEntity.getAmount() == 0L ? current().nextInt(10) + 1 : taskEntity.getAmount();
      String param = EmptyUtil.isEmpty(taskEntity.getParam()) ? "hy" : taskEntity.getParam();
      ReportEntity reportEntity =
          ReportEntity.builder()
              .planId(taskEntity.getPlanId())
              .taskId(taskEntity.getTaskId())
              .status(TaskType.FINISHED.getCode())
              .result(param + "-" + amount)
              .build();
      hyResult = hyResult.success();
      hyResult.setData(reportEntity);
      return hyResult;
    } else {
      hyResult = hyResult.error("task异常");
      hyResult.setData(ReportEntity.builder().status(TaskType.EXCEPTION.getCode()).build());
      return hyResult;
    }
  }
}
