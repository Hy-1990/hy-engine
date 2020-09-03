package com.huyi.dao.es.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/** @Author huyi @Date 2020/7/2016:22 @Description: es实体类 */
@Data
@Builder
@Document(indexName = "engine_report", type = "_doc", createIndex = true)
public class ReportData {
  /** 任务ID */
  @Id
  @Field(type = FieldType.Integer)
  @JsonProperty("task_id")
  private Integer taskId;

  /** 计划ID */
  @Field(type = FieldType.Integer)
  @JsonProperty("plan_id")
  private Integer planId;

  /** 状态code */
  @Field(type = FieldType.Integer)
  @JsonProperty("status")
  private Integer status;

  /** 任务结果 */
  @Field(type = FieldType.Keyword)
  @JsonProperty("result")
  private String result;

  /** 时间戳 */
  @Field(type = FieldType.Long)
  @JsonProperty("time_stamp")
  private Long timeStamp;
}
