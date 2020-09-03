package com.huyi.dao.es.repo;

import com.huyi.dao.es.model.ReportData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/** @Author huyi @Date 2020/7/2016:29 @Description: 表kbr_index_test操作类 */
public interface ReportDataRepo extends ElasticsearchRepository<ReportData, Integer> {}
