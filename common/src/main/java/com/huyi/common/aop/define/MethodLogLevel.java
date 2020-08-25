package com.huyi.common.aop.define;

/** @Author huyi @Date 2020/7/17 11:15 @Description */
public enum MethodLogLevel {
  // info
  INFO("INFO"),
  // debug
  DEBUG("DEBUG"),
  // error
  ERROR("ERROR");

  private final String levelName;

  MethodLogLevel(String levelName) {
    this.levelName = levelName;
  }

  public String getLevelName() {
    return levelName;
  }
}
