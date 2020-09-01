package com.huyi.web.handle;

import com.google.common.util.concurrent.Monitor;
import com.huyi.common.utils.EmptyUtil;
import com.huyi.web.entity.TaskEntity;
import com.huyi.web.enums.TaskCode;
import com.huyi.web.enums.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** @Author huyi @Date 2020/8/26 21:52 @Description: 任务控制类 */
@Component
public class TaskHandle {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private TaskCacheHandle taskCacheHandle;

  private static Monitor monitor = new Monitor();

  public void dispatch(TaskEntity taskEntity) {
    if (EmptyUtil.isEmpty(taskEntity)
        || EmptyUtil.isEmpty(taskEntity.getPlanId())
        || EmptyUtil.isEmpty(taskEntity.getTaskId())) {
      return;
    }
    if (EmptyUtil.isEmpty(taskEntity.getAmount())) {
      taskEntity.setAmount(0L);
    }
    taskEntity.setStatus(TaskType.RUNNING.getCode());
    AtomicBoolean result = new AtomicBoolean(false);
    AtomicInteger reTry = new AtomicInteger(0);
    StringBuffer msg = new StringBuffer();
    String key = taskEntity.getPlanId() + "";
    while (true) {
      if (monitor.enterIf(monitor.newGuard(() -> reTry.get() < 3))) {
        try {
          boolean hasRun =
              taskCacheHandle.tryLockAndRun(
                  key,
                  10,
                  TimeUnit.SECONDS,
                  () -> {
                    List<TaskEntity> tasks = taskCacheHandle.get(key);
                    List<TaskEntity> queue = new ArrayList<>();
                    if (EmptyUtil.isEmpty(tasks)) {
                      result.set(queue.add(taskEntity));
                      taskCacheHandle.put(key, queue);
                      msg.append(TaskCode.INPUT_SUCCESS.getMsg());
                    } else {
                      if (tasks.stream()
                          .noneMatch(t -> t.getTaskId().equals(taskEntity.getTaskId()))) {
                        result.set(tasks.add(taskEntity));
                        taskCacheHandle.put(key, tasks);
                        msg.append(TaskCode.INPUT_SUCCESS.getMsg());
                      } else {
                        result.set(false);
                        msg.append(TaskCode.INPUT_EXIST.getMsg());
                      }
                    }
                  });
          if (hasRun) {
            break;
          }
        } finally {
          monitor.leave();
          reTry.incrementAndGet();
        }
      } else {
        break;
      }
    }
    if (result.get()) {
      logger.info(msg.toString());
    } else {
      logger.error(msg.toString());
    }
  }
}
