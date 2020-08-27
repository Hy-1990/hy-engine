package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 16:32 @Description: */
@Component
public class PlanCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.PLAN_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, LinkedBlockingQueue<PlanEntity>> planCache;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> planLock;

  public LinkedBlockingQueue<PlanEntity> get(String key) {
    return planCache.get(key);
  }

  public Map<String, LinkedBlockingQueue<PlanEntity>> getAll(Set<String> keys) {
    return planCache.getAll(keys);
  }

  public void put(String key, LinkedBlockingQueue<PlanEntity> queue) {
    planCache.put(key, queue);
  }

  public void putAll(Map<String, LinkedBlockingQueue<PlanEntity>> map) {
    planCache.putAll(map);
  }

  public boolean remove(String key) {
    return planCache.remove(key);
  }

  public void removeAll(Set<String> keys) {
    planCache.removeAll(keys);
  }

  public AutoReleaseLock tryLock(String key, long expire, TimeUnit timeUtil) {
    return planLock.tryLock(key, expire, timeUtil);
  }

  public boolean tryLockAndRun(String key, long expire, TimeUnit timeUnit, Runnable runnable) {
    return planLock.tryLockAndRun(key, expire, timeUnit, runnable);
  }
}
