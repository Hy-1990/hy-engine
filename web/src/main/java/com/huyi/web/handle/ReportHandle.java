package com.huyi.web.handle;

import com.huyi.web.entity.ReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/** @Author huyi @Date 2020/9/2 9:23 @Description: 结果控制类 */
@Component
public class ReportHandle {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public static ConcurrentHashMap<Integer, List<ReportEntity>> reportQueue;

  public static StampedLock reportLock;

  @PostConstruct
  public void init() {
    reportQueue = new ConcurrentHashMap<>();
    reportLock = new StampedLock();
  }

  public void saveReport(Integer planId, List<ReportEntity> reports) {
    long stamped = reportLock.writeLock();
    try {
      reportQueue.put(planId, reports);
    } finally {
      reportLock.unlockWrite(stamped);
    }
  }

  public List<ReportEntity> getReport(Integer planId) {
    long stamped = reportLock.readLock();
    try {
      return reportQueue.get(planId);
    } finally {
      reportLock.unlockRead(stamped);
    }
  }

  public String getCacheReport() {
    if (reportQueue.size() == 0) {
      return "";
    }
    return "完成结果计划id：" + reportQueue.keySet().toString();
  }
}
