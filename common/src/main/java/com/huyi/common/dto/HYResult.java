package com.huyi.common.dto;

public class HYResult<T> {
  private T data;
  private int code;
  private String message;

  public HYResult() {}

  public HYResult(T data) {
    this.data = data;
  }

  private enum CodeType {
    // error
    ERROR(-1, "失败"),
    // success
    SUCCESS(0, "成功");

    private final int value;
    private final String decrypt;

    CodeType(int value, String descript) {
      this.value = value;
      this.decrypt = descript;
    }

    @Override
    public String toString() {
      return this.decrypt;
    }
  }

  public HYResult success() {
    code = CodeType.SUCCESS.value;
    message = CodeType.SUCCESS.toString();
    return this;
  }

  public HYResult error() {
    code = CodeType.ERROR.value;
    message = CodeType.ERROR.toString();
    return this;
  }

  public HYResult success(String message) {
    code = CodeType.SUCCESS.value;
    this.message = message;
    return this;
  }

  public HYResult error(String message) {
    code = CodeType.ERROR.value;
    this.message = message;
    return this;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "HYResult{" + "data=" + data + ", code=" + code + ", message='" + message + '\'' + '}';
  }
}
