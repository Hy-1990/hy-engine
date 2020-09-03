package com.huyi.web.service.inf;

import com.huyi.dao.es.model.ReportData;

import java.util.List;

/** @Author huyi @Date 2020/9/3 19:37 @Description: 报告结果接口类 */
public interface ReportService {
  void saveListToEs(List<ReportData> list);
}
