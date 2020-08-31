package com.huyi.web.enums;

/** @Author huyi @Date 2020/8/26 20:59 @Description: 任务状态 */
public enum TaskType {
  // 运行中
  RUNNING(0),
  // 暂停中
  STOP(1),
  // 异常
  EXCEPTION(9999),
  // 已完成
  FINISHED(2);

  private final Integer code;

  TaskType(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }
}
