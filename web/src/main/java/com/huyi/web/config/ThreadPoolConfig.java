package com.huyi.web.config;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Program: hy-engine @ClassName: ThreadPoolConfig @Author: huyi @Date: 2020-08-31
 * 00:52 @Description: @Version: V1.0
 */
public class ThreadPoolConfig {
  /** 推送数据池 */
  public static final ScheduledExecutorService inputPool =
      Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("InputThread-"));

  /** 核心计划池 */
  public static final ExecutorService captainPool =
      Executors.newFixedThreadPool(500, new CustomizableThreadFactory("CaptainThread-"));
  /** 常规工作池 */
  public static final ExecutorService crewPool =
      Executors.newFixedThreadPool(2000, new CustomizableThreadFactory("CrewThread-"));

  /** runner captain单例池 */
  public static final ExecutorService captainSinglePool =
      Executors.newSingleThreadExecutor(new CustomizableThreadFactory("CaptainSingleThread-"));

  /** runner report单例池 */
  public static final ExecutorService datapushSinglePool =
      Executors.newSingleThreadExecutor(new CustomizableThreadFactory("DataPushSingleThread-"));
}
