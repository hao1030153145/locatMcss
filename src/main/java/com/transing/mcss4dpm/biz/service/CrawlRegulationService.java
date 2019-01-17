package com.transing.mcss4dpm.biz.service;

import com.jeeframework.logicframework.integration.DataServiceException;
import com.transing.mcss4dpm.integration.bo.CrawlRegulationTestBO;

import java.util.List;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/4/9
 */
public interface CrawlRegulationService {
    // 根据dataTypeId获取数据数目
    List<CrawlRegulationTestBO> selectRegulationTotalBydataTypeId(Integer dataTypeId) throws DataServiceException;
}
