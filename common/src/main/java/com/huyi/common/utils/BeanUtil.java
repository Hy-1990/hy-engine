package com.huyi.common.utils;

/** @Author huyi @Date 2020/7/16 15:21 @Description */
public class BeanUtil {
  public static <T> T copyProperties(Class<T> clazz, Object source) throws RuntimeException {
    try {
      Object result = clazz.getDeclaredConstructor().newInstance();
      org.springframework.beans.BeanUtils.copyProperties(source, result);
      return (T) result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> void copyProperties(Object source, T target) throws RuntimeException {
    try {
      org.springframework.beans.BeanUtils.copyProperties(source, target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
