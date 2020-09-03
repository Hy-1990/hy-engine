package com.huyi.web.service.impl;

import com.huyi.dao.es.model.ReportData;
import com.huyi.dao.es.repo.ReportDataRepo;
import com.huyi.web.service.inf.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** @Author huyi @Date 2020/9/3 19:38 @Description: 报告接口实现类 */
@Service
public class ReportDataServiceImpl implements ReportService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired private ReportDataRepo reportDataRepo;

  @Override
  public void saveListToEs(List<ReportData> list) {
    reportDataRepo.saveAll(list);
  }
}
