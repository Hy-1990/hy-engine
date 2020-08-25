package com.huyi.common.aop.define.anno;

import com.huyi.common.aop.define.MethodLogLevel;

import java.lang.annotation.*;

/** @Author huyi @Date 2020/7/17 11:15 @Description */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface MethodLog {

  /**
   * 操作类型
   *
   * @return 代码
   * @author M.simple
   * @version 1.0
   */
  MethodLogLevel methodLogLevel();
}
