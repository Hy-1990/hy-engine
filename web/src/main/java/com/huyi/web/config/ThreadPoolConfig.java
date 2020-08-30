package com.huyi.web.config;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

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
}
