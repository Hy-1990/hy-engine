package com.huyi.web.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** @Author huyi @Date 2020/8/26 20:47 @Description: 计划实体类 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanEntity implements Serializable {
  private static final long serialVersionUID = -2848414088229747177L;
  private Integer planId;
  private Integer userId;
  private Integer level;
  private Integer robotSize;
  private Integer status;
  private String msg;
  private Long planTime;
  private Integer taskAmount;
}
