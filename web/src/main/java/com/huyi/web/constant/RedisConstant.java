package com.huyi.web.constant;

/** @Author huyi @Date 2020/8/25 13:47 @Description: redis常量 */
public class RedisConstant {
  /** 任务前缀 */
  public static final String TASK_PREFIX = "task-planId-";
  /** 计划前缀 */
  public static final String PLAN_PREFIX = "plan-userId-";

  /** 暂停任务前缀 */
  public static final String STOP_PREFIX = "stop-planId-";
  /** 暂停任务关键字 */
  public static final String STOP_KEY = "stopList";
  /** 运行或者准备中任务前缀 */
  public static final String RUNNING_PREFIX = "running-planId-";
  /** 运行或者准备中任务关键字 */
  public static final String RUNNING_KEY = "runningList";
}
