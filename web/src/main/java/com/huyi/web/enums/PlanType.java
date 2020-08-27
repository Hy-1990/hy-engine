package com.huyi.web.enums;

/** @Author huyi @Date 2020/8/26 20:51 @Description: 计划状态 */
public enum PlanType {
  // 准备中
  READY(0),
  // 运行中
  RUNNING(1),
  // 暂停中
  STOP(2),
  // 已完成
  FINISHED(9);

  private final Integer code;

  PlanType(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
