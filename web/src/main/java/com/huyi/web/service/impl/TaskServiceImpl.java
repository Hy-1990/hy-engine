package com.huyi.web.service.impl;

import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.CacheHyType;
import com.huyi.web.handle.CacheHandle;
import com.huyi.web.service.inf.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** @Author huyi @Date 2020/8/25 13:50 @Description: 任务功能接口实现 */
@Service
public class TaskServiceImpl implements TaskService {
  private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

  @Autowired private CacheHandle cacheHandle;

  @Override
  public boolean saveTask(TaskEntity entity) {
//    String key = RedisConstant.REAL_TASK_PREFIX + entity.getPlanId();
//    return cacheHandle.computeIfAbsent(CacheHyType.REAL_TASK, key, entity);
    return false;
  }
}
