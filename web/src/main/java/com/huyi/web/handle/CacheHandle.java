package com.huyi.web.handle;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.huyi.web.constant.RedisConstant;
import com.huyi.web.entity.PlanEntity;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.CacheHyType;
import com.huyi.web.enums.LockHyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/8/26 13:42 @Description: 缓存控制器 */
@Deprecated
@Component
public class CacheHandle {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

//  @CreateCache(name = RedisConstant.REAL_TASK_PREFIX, cacheType = CacheType.REMOTE)
//  private Cache<String, List<TaskEntity>> taskCache;
//
//  @CreateCache(
//      name = RedisConstant.TMP_TASK_PREFIX,
//      cacheType = CacheType.REMOTE,
//      expire = 30,
//      timeUnit = TimeUnit.MINUTES)
//  private Cache<String, List<TaskEntity>> tempCache;
//
//  @CreateCache(name = RedisConstant.PLAN_PREFIX, cacheType = CacheType.REMOTE)
//  private Cache<String, List<PlanEntity>> planCache;
//
//  /** 分布式锁 */
//  @CreateCache(cacheType = CacheType.REMOTE)
//  private Cache<String, String> planLock;
//
//  @CreateCache(cacheType = CacheType.REMOTE)
//  private Cache<String, String> tempLock;
//
//  @CreateCache(cacheType = CacheType.REMOTE)
//  private Cache<String, String> realLock;
//
//  /** 缓存选择器 */
//  public ConcurrentHashMap<CacheHyType, Cache> cacheSelector = new ConcurrentHashMap<>();
//
//  /** 分布式锁选择器 */
//  public ConcurrentHashMap<LockHyType, Cache> lockSelector = new ConcurrentHashMap<>();
//
//  @PostConstruct
//  void init() {
//    cacheSelector.put(CacheHyType.REAL_TASK, taskCache);
//    cacheSelector.put(CacheHyType.TEMP_TASK, tempCache);
//    cacheSelector.put(CacheHyType.PLAN, planCache);
//
//    lockSelector.put(LockHyType.PLAN_LOCK, planLock);
//    lockSelector.put(LockHyType.REAL_LOCK, realLock);
//    lockSelector.put(LockHyType.TEMP_LOCK, tempLock);
//  }
//
//  public <T> T get(CacheHyType cacheHyType, Object key, Class<T> tClass) {
//    CacheGetResult<T> result = cacheSelector.get(cacheHyType).GET(key);
//    if (result.isSuccess()) {
//      return result.getValue();
//    } else {
//      return null;
//    }
//  }
//
//  public <T> List<T> getlist(CacheHyType cacheHyType, Object key, Class<T> tClass) {
//    CacheGetResult<List<T>> result = cacheSelector.get(cacheHyType).GET(key);
//    if (result.isSuccess()) {
//      return result.getValue();
//    } else {
//      return null;
//    }
//  }
//
//  public <K, V> Map<K, V> getAll(
//      CacheHyType cacheHyType, Set<Object> keys, Class<K> kClass, Class<V> vClass) {
//    MultiGetResult result = cacheSelector.get(cacheHyType).GET_ALL(keys);
//    if (result != null) {
//      return result.getValues();
//    } else {
//      return null;
//    }
//  }
//
//  public boolean put(CacheHyType cacheHyType, Object key, Object value) {
//    CacheResult result = cacheSelector.get(cacheHyType).PUT(key, value);
//    if (result.isSuccess()) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public boolean putAll(CacheHyType cacheHyType, Map<Object, Object> map) {
//    CacheResult result = cacheSelector.get(cacheHyType).PUT_ALL(map);
//    if (result.isSuccess()) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public boolean computeIfAbsent(CacheHyType cacheHyType, Object key, Object value) {
//    Object result = cacheSelector.get(cacheHyType).computeIfAbsent(key, x -> value);
//    if (result != null) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public boolean remove(CacheHyType cacheHyType, Object key, Object value) {
//    CacheResult result = cacheSelector.get(cacheHyType).REMOVE(key);
//    if (result.isSuccess()) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public boolean removeAll(CacheHyType cacheHyType, Set<Object> keys) {
//    CacheResult result = cacheSelector.get(cacheHyType).REMOVE_ALL(keys);
//    if (result.isSuccess()) {
//      return true;
//    } else {
//      return false;
//    }
//  }
//
//  public AutoReleaseLock tryLock(
//      LockHyType lockHyType, Object key, long expire, TimeUnit timeUnit) {
//    return lockSelector.get(lockHyType).tryLock(key, expire, timeUnit);
//  }
//
//  public boolean tryLockAndRun(
//      LockHyType lockHyType, Object key, long expire, TimeUnit timeUnit, Runnable action) {
//    return lockSelector.get(lockHyType).tryLockAndRun(key, expire, timeUnit, action);
//  }
}
