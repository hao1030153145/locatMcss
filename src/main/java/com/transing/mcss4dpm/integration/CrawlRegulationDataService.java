package com.transing.mcss4dpm.integration;

import com.jeeframework.logicframework.integration.DataServiceException;
import com.transing.mcss4dpm.integration.bo.CrawlRegulationTestBO;

import java.util.List;

/**
 * Created by Administrator on 2018/1/20 0020.
 */
public interface CrawlRegulationDataService {
    // 根据dataTypeId获取数据数目
    List<CrawlRegulationTestBO> selectRegulationTotalBydataTypeId(Integer dataTypeId) throws DataServiceException;
}
