package com.transing.mcss4dpm.integration.impl.ibatis;

import com.jeeframework.logicframework.integration.DataServiceException;
import com.jeeframework.logicframework.integration.dao.DAOException;
import com.jeeframework.logicframework.integration.dao.ibatis.BaseDaoiBATIS;
import com.transing.mcss4dpm.integration.CrawlRegulationDataService;
import com.transing.mcss4dpm.integration.bo.CrawlRegulationTestBO;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2018/1/20 0020.
 */

@Scope("prototype")
@Repository("crawlRegulationDataService")
public class CrawlRegulationDAOibatis extends BaseDaoiBATIS implements CrawlRegulationDataService {

    @Override
    public List<CrawlRegulationTestBO> selectRegulationTotalBydataTypeId(Integer dataTypeId) throws DataServiceException {
        try {
            return sqlSessionTemplate.selectList("crawlRegulationMapper.selectRegulationTotalBydataTypeId", dataTypeId);
        } catch (DataAccessException e) {
            throw new DAOException("根据dataTypeId获取抓取规则信息数目失败", e);
        }
    }
}
