package com.huyi.web.service.inf;

import com.huyi.web.entity.TaskEntity;

/** @Author huyi @Date 2020/8/25 13:50 @Description: 任务功能接口 */
public interface TaskService {
  boolean saveTask(TaskEntity entity);
}
