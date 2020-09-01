package com.huyi.web.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Program: hy-engine @ClassName: ReportEntity @Author: huyi @Date: 2020-09-01 23:47 @Description:
 * 执行结果类 @Version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportEntity implements Serializable {
  private static final long serialVersionUID = 146032858815928770L;
  private Integer planId;
  private Integer status;
  private Integer taskId;

  private String result;
}
