package com.huyi.web.handle;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.google.common.base.Joiner;
import com.huyi.common.utils.RedisUtil;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/27 16:32 @Description: */
@Component
public class PlanCacheHandle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @CreateCache(name = RedisConstant.PLAN_PREFIX, cacheType = CacheType.REMOTE)
  public Cache<String, LinkedList<PlanEntity>> planCache;

  @Autowired private RedisUtil redisUtil;

  /** 分布式锁 */
  @CreateCache(cacheType = CacheType.REMOTE)
  public Cache<String, String> planLock;

  public LinkedList<PlanEntity> get(String key) {
    return planCache.get(key);
  }

  public Map<String, LinkedList<PlanEntity>> getAll(Set<String> keys) {
    return planCache.getAll(keys);
  }

  public void put(String key, LinkedList<PlanEntity> queue) {
    planCache.put(key, queue);
  }

  public void putAll(Map<String, LinkedList<PlanEntity>> map) {
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

  public String getCacheReport() {
    Set<String> keys = redisUtil.getAllKeyMatch(RedisConstant.PLAN_PREFIX);
    if (keys.size() == 0) {
      return "";
    } else {
      List<String> reports = new ArrayList<>();
      keys.forEach(
          (x) -> {
            reports.add("userId:" + x.split("-")[2] + ",plans:{" + get(x.split("-")[2]) + "}");
          });
      return Joiner.on(";").join(reports);
    }
  }
}
