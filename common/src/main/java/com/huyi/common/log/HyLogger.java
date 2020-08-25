package com.huyi.common.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HyLogger {
  protected static Logger logger = LogManager.getLogger();

  public static Logger logger() {
    return logger;
  }
}
