package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.enums.PlanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Program: hy-engine @ClassName: StopCacheHandle @Author: huyi @Date: 2020-08-30
 * 22:22 @Description: 暂停planId缓存控制类 @Version: V1.0
 */
@Component
public class StopCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.STOP_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, Set<Integer>> stopCache;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> stopLock;

  public Set<Integer> get(String key) {
    return stopCache.get(key);
  }

  public Map<String, Set<Integer>> getAll(Set<String> keys) {
    return stopCache.getAll(keys);
  }

  public void put(String key, Set<Integer> queue) {
    stopCache.put(key, queue);
  }

  public void putAll(Map<String, Set<Integer>> map) {
    stopCache.putAll(map);
  }

  public boolean remove(String key) {
    return stopCache.remove(key);
  }

  public void removeAll(Set<String> keys) {
    stopCache.removeAll(keys);
  }

  public AutoReleaseLock tryLock(String key, long expire, TimeUnit timeUtil) {
    return stopLock.tryLock(key, expire, timeUtil);
  }

  public boolean tryLockAndRun(String key, long expire, TimeUnit timeUnit, Runnable runnable) {
    return stopLock.tryLockAndRun(key, expire, timeUnit, runnable);
  }

  public boolean saveStop(PlanEntity planEntity) {
    if (!planEntity.getStatus().equals(PlanType.STOP.getCode())) {
      return false;
    }
    AtomicBoolean isOk = new AtomicBoolean(false);
    tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopSet = get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isEmpty(stopSet)) {
            Set<Integer> stopPlanId = new HashSet<>();
            stopPlanId.add(planEntity.getPlanId());
            put(RedisConstant.STOP_KEY, stopPlanId);
          } else {
            stopSet.add(planEntity.getPlanId());
            put(RedisConstant.STOP_KEY, stopSet);
          }
          isOk.set(true);
        });
    return isOk.get();
  }

  public boolean removeStop(PlanEntity planEntity) {
    AtomicBoolean isOk = new AtomicBoolean(false);
    tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopList = get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isNotEmpty(stopList)) {
            stopList.removeIf(s -> s.equals(planEntity.getPlanId()));
            put(RedisConstant.STOP_KEY, stopList);
          }
          isOk.set(true);
        });
    return isOk.get();
  }

  public boolean checkStop(PlanEntity planEntity) {
    AtomicBoolean isOk = new AtomicBoolean(false);
    tryLockAndRun(
        RedisConstant.STOP_KEY,
        3,
        TimeUnit.SECONDS,
        () -> {
          Set<Integer> stopPlanId = get(RedisConstant.STOP_KEY);
          if (EmptyUtil.isNotEmpty(stopPlanId)) {
            isOk.set(stopPlanId.contains(planEntity.getPlanId()));
          }
        });
    return isOk.get();
  }

  public String getReport() {
    if (stopCache.get(RedisConstant.STOP_KEY) == null) {
      return "";
    }
    return stopCache.get(RedisConstant.STOP_KEY).toString();
  }
}
