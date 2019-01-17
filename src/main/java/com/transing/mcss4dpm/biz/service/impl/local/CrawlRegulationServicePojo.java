package com.transing.mcss4dpm.biz.service.impl.local;

import com.jeeframework.logicframework.biz.exception.BizException;
import com.jeeframework.logicframework.biz.service.BaseService;
import com.jeeframework.logicframework.integration.DataServiceException;
import com.jeeframework.logicframework.integration.dao.DAOException;
import com.transing.mcss4dpm.biz.service.CrawlRegulationService;
import com.transing.mcss4dpm.integration.CrawlRegulationDataService;
import com.transing.mcss4dpm.integration.bo.CrawlRegulationTestBO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Administrator on 2018/1/23 0023.
 */
@Service("crawlRegulationService")
public class CrawlRegulationServicePojo extends BaseService implements CrawlRegulationService {

    @Resource
    private CrawlRegulationDataService crawlRegulationDataService;

    @Override
    public List<CrawlRegulationTestBO> selectRegulationTotalBydataTypeId(Integer dataTypeId) throws DataServiceException {
        try {
            return crawlRegulationDataService.selectRegulationTotalBydataTypeId(dataTypeId);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }
}
