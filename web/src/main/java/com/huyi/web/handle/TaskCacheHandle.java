package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.google.common.base.Joiner;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.CacheHyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 17:22 @Description: */
@Component
public class TaskCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.TASK_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, List<TaskEntity>> taskCache;

  @Autowired private RedisUtil redisUtil;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> taskLock;

  public List<TaskEntity> get(String key) {
    return taskCache.get(key);
  }

  public Map<String, List<TaskEntity>> getAll(Set<String> keys) {
    return taskCache.getAll(keys);
  }

  public void put(String key, List<TaskEntity> queue) {
    taskCache.put(key, queue);
  }

  public void putAll(Map<String, List<TaskEntity>> map) {
    taskCache.putAll(map);
  }

  public boolean remove(String key) {
    return taskCache.remove(key);
  }

  public void removeAll(Set<String> keys) {
    taskCache.removeAll(keys);
  }

  public AutoReleaseLock tryLock(String key, long expire, TimeUnit timeUtil) {
    return taskCache.tryLock(key, expire, timeUtil);
  }

  public boolean tryLockAndRun(String key, long expire, TimeUnit timeUnit, Runnable runnable) {
    return taskCache.tryLockAndRun(key, expire, timeUnit, runnable);
  }

  public String getCacheReport() {
    Set<String> keys = redisUtil.getAllKeyMatch(RedisConstant.TASK_PREFIX);
    if (keys.size() == 0) {
      return "";
    } else {
      List<String> reports = new ArrayList<>();
      keys.forEach(
          (x) -> {
            reports.add("planId:" + x.split("-")[2] + ",tasks:{" + get(x.split("-")[2]) + "}");
          });
      return Joiner.on(";").join(reports);
    }
  }
}
