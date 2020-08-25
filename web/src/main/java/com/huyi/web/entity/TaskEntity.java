package com.huyi.web.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** @Author huyi @Date 2020/8/25 13:58 @Description: 任务实体类 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskEntity implements Serializable {
  private static final long serialVersionUID = -230406315701273955L;
  private String taskName;
  private String msg;
}
