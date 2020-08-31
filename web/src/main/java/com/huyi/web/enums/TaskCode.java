package com.huyi.web.enums;

/**
 * @Program: hy-engine @ClassName: TaskCode @Author: huyi @Date: 2020-08-31 23:36 @Description:
 * 任务code @Version: V1.0
 */
public enum TaskCode {
  // 无效任务参数
  PARAM_INVALID(9999, "计划请求参数异常!"),
  // 插入内存成功
  INPUT_SUCCESS(11000, "插入内存成功!"),
  // 插入任务已存在
  INPUT_EXIST(10001, "插入任务已存在!");

  private final Integer code;
  private final String msg;

  TaskCode(Integer code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Integer getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
}
