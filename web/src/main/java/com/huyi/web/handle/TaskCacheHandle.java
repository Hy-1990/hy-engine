package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.CacheHyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 17:22 @Description: */
public class TaskCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.REAL_TASK_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, List<TaskEntity>> taskCache;

  @CreateCache(name = RedisConstant.TMP_TASK_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, List<TaskEntity>> tempCache;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> taskLock;

  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> tempLock;

  public List<TaskEntity> get(CacheHyType cacheHyType, String key) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      return taskCache.get(key);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      return tempCache.get(key);
    } else {
      return null;
    }
  }

  public Map<String, List<TaskEntity>> getAll(CacheHyType cacheHyType, Set<String> keys) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      return taskCache.getAll(keys);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      return tempCache.getAll(keys);
    } else {
      return null;
    }
  }

  public void put(CacheHyType cacheHyType, String key, List<TaskEntity> queue) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      taskCache.put(key, queue);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      tempCache.put(key, queue);
    }
  }

  public void putAll(CacheHyType cacheHyType, Map<String, List<TaskEntity>> map) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      taskCache.putAll(map);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      tempCache.putAll(map);
    }
  }

  public boolean remove(CacheHyType cacheHyType, String key) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      return taskCache.remove(key);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      return tempCache.remove(key);
    } else {
      return false;
    }
  }

  public void removeAll(CacheHyType cacheHyType, Set<String> keys) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      taskCache.removeAll(keys);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      tempCache.removeAll(keys);
    }
  }

  public AutoReleaseLock tryLock(
      CacheHyType cacheHyType, String key, long expire, TimeUnit timeUtil) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      return taskCache.tryLock(key, expire, timeUtil);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      return tempCache.tryLock(key, expire, timeUtil);
    } else {
      return null;
    }
  }

  public boolean tryLockAndRun(
      CacheHyType cacheHyType, String key, long expire, TimeUnit timeUnit, Runnable runnable) {
    if (cacheHyType.equals(CacheHyType.REAL_TASK)) {
      return taskCache.tryLockAndRun(key, expire, timeUnit, runnable);
    } else if (cacheHyType.equals(CacheHyType.TEMP_TASK)) {
      return tempCache.tryLockAndRun(key, expire, timeUnit, runnable);
    } else {
      return false;
    }
  }
}
