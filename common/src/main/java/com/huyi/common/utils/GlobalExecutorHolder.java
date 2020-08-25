package com.huyi.common.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 全局线程执行器持有者
 *
 * @author huyi
 * @create 2019-11-19 10:10
 */
public class GlobalExecutorHolder {

  private static Integer maxCpuCount = Runtime.getRuntime().availableProcessors() * 2;

  private static int COMMON_THREAD_POOL_CORE_SIZE = 16;

  private static int COMMON_THREAD_POOL_SIZE = 16;

  private static ExecutorService executor =
      new ThreadPoolExecutor(
          1,
          maxCpuCount,
          60,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(16),
          new NamedThreadFactory("knowledge-factory"));

  public static ExecutorService getGlobalExecutors() {
    return executor;
  }
}
