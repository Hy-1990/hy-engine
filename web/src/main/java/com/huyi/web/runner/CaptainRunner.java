package com.huyi.web.runner;

import com.google.common.util.concurrent.Monitor;
import com.huyi.web.handle.PlanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Program: hy-engine @ClassName: CaptainRunner @Author: huyi @Date: 2020-08-31 23:53 @Description:
 * 核心引擎 @Version: V1.0
 */
@Component
@Order(3)
public class CaptainRunner implements ApplicationRunner {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private PlanHandle planHandle;
  private final Monitor planMonitor = new Monitor();

  @Override
  public void run(ApplicationArguments args) throws Exception {
    while (true){

    }
  }
}
