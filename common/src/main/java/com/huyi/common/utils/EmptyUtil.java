package com.huyi.common.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmptyUtil {

  public static boolean isEmpty(List<?> list) {
    return (list == null || list.size() == 0);
  }

  public static boolean isNotEmpty(List<?> list) {
    return (list != null && list.size() > 0);
  }

  public static boolean isEmpty(String str) {
    return (str == null || "".equals(str) || " ".equals(str));
  }

  public static boolean isNotEmpty(String str) {
    return (str != null && !str.equals("") && !str.equals(" "));
  }

  public static boolean isNotEmpty(Object obj) {
    return obj != null;
  }

  public static boolean isEmpty(Object obj) {
    return obj == null;
  }

  public static boolean isEmpty(Object[] strings) {
    return (strings == null || strings.length == 0);
  }

  public static boolean isNotEmpty(Integer num) {
    return num != null;
  }

  public static boolean isEmpty(Integer num) {
    return num == null;
  }

  public static boolean isNotEmpty(Object[] strings) {
    return (strings != null && strings.length > 0);
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return (map == null || map.size() == 0);
  }

  public static boolean isNotEmpty(Map<?, ?> map) {
    return (map != null && map.size() > 0);
  }

  public static boolean isEmpty(Set<?> set) {
    return (set == null || set.size() == 0);
  }

  public static boolean isNotEmpty(Set<?> set) {
    return (set != null && set.size() > 0);
  }
}
