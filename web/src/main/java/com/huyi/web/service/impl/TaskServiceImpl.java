package com.huyi.web.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.huyi.common.log.HyLogger;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.service.inf.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** @Author huyi @Date 2020/8/25 13:50 @Description: 任务功能接口实现 */
@Service
public class TaskServiceImpl implements TaskService {
  private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

  @CreateCache(cacheType = CacheType.REMOTE)
  private volatile Cache<String, TaskEntity> taskCache;

  @Override
  public boolean saveTask(TaskEntity entity) {
    synchronized (taskCache) {
      String key = RedisConstant.TASK_PREFIX + entity.getTaskName();
      TaskEntity taskEntity = taskCache.get(key);
      if (EmptyUtil.isEmpty(taskEntity)) {
        taskCache.putIfAbsent(key, entity);
      } else {
        HyLogger.logger().warn("redis 中已存在：{}", entity.toString());
        logger.info("数据为：{}", entity.toString());
      }
      return true;
    }
  }
}
