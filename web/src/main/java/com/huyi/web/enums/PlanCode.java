package com.huyi.web.enums;

/**
 * @Program: hy-engine @ClassName: PlanErrorCode @Author: huyi @Date: 2020-08-30 17:20 @Description:
 * 计划错误码 @Version: V1.0
 */
public enum PlanCode {
  // 无效计划参数
  PARAM_INVALID(9999, "计划请求参数异常!"),

  // 创建计划无效请求
  CREATE_INVALID(1001, "无效创建计划请求!"),
  // 成功请求
  CREATE_SUCCESS(1100, "成功创建计划请求!"),
  // 创建计划已存在
  CREATE_EXIST(1002, "创建计划已存在!"),

  // 暂停计划失败
  STOP_FAILED(2001, "暂停计划失败!"),
  // 暂停请求成功
  STOP_SUCCESS(2200, "暂停计划成功!"),

  // 重新运行失败
  RERUN_FAILED(3001, "重新运行失败!"),
  // 重新运行计划成功
  RERUN_SUCCESS(3300, "重新运行计划成功!");

  private final Integer code;
  private final String msg;

  PlanCode(Integer code, String msg) {
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
