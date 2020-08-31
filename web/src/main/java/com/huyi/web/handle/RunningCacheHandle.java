package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.google.common.base.Joiner;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/31 10:51 @Description: 运行队列缓存 */
@Component
public class RunningCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.RUNNING_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, Set<PlanEntity>> runningCache;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> runningLock;

  public Set<PlanEntity> get(String key) {
    return runningCache.get(key);
  }

  public Map<String, Set<PlanEntity>> getAll(Set<String> keys) {
    return runningCache.getAll(keys);
  }

  public void put(String key, Set<PlanEntity> queue) {
    runningCache.put(key, queue);
  }

  public void putAll(Map<String, Set<PlanEntity>> map) {
    runningCache.putAll(map);
  }

  public boolean remove(String key) {
    return runningCache.remove(key);
  }

  public void removeAll(Set<String> keys) {
    runningCache.removeAll(keys);
  }

  public AutoReleaseLock tryLock(String key, long expire, TimeUnit timeUtil) {
    return runningLock.tryLock(key, expire, timeUtil);
  }

  public boolean tryLockAndRun(String key, long expire, TimeUnit timeUnit, Runnable runnable) {
    return runningLock.tryLockAndRun(key, expire, timeUnit, runnable);
  }

  public String getReport() {
    if (runningCache.get(RedisConstant.RUNNING_KEY) == null){
      return "";
    }
    return runningCache.get(RedisConstant.RUNNING_KEY).toString();
  }
}
