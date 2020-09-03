package com.huyi.web.runner;

import com.huyi.dao.es.model.ReportData;
import com.huyi.web.config.ThreadPoolConfig;
import com.huyi.web.entity.ReportEntity;
import com.huyi.web.handle.ReportHandle;
import com.huyi.web.service.inf.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** @Author huyi @Date 2020/9/3 19:40 @Description: 推送数据上es */
@Component
@Order(4)
public class DataPushRunner implements ApplicationRunner {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  @Autowired private ReportHandle reportHandle;
  @Autowired private ReportService reportService;

  private static ConcurrentHashMap<Integer, List<ReportEntity>> reports = new ConcurrentHashMap<>();

  @Override
  public void run(ApplicationArguments args) throws Exception {
    logger.info("**************HY-记录引擎启动!**************");
    ThreadPoolConfig.datapushSinglePool.execute(
        () -> {
          while (true) {
            ConcurrentHashMap<Integer, List<ReportEntity>> newReports =
                reportHandle.findUpdate(reports.keySet());
            if (newReports == null || newReports.size() == 0) {
              logger.info("推送数据队列暂时无可推送数据，引擎空闲10秒钟。");
              try {
                TimeUnit.SECONDS.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              continue;
            }
            long timeStamp = System.currentTimeMillis();

            for (Map.Entry<Integer, List<ReportEntity>> entry : newReports.entrySet()) {
              List<ReportData> list = new ArrayList<>();
              for (ReportEntity reportEntity : entry.getValue()) {
                ReportData reportData =
                    ReportData.builder()
                        .planId(reportEntity.getPlanId())
                        .result(reportEntity.getResult())
                        .status(reportEntity.getStatus())
                        .taskId(reportEntity.getTaskId())
                        .timeStamp(timeStamp)
                        .build();
                list.add(reportData);
              }
              reportService.saveListToEs(list);
            }
            newReports.forEach(
                (k, v) -> {
                  reports.put(k, v);
                });

            logger.info("推送数据引擎完成计划ID:{},数据推送！", newReports.keySet());
          }
        });
  }
}
