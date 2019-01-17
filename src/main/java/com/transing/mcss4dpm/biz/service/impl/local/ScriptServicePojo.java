package com.transing.mcss4dpm.biz.service.impl.local;

import com.jeeframework.logicframework.biz.exception.BizException;
import com.jeeframework.logicframework.biz.service.BaseService;
import com.jeeframework.logicframework.integration.dao.DAOException;
import com.transing.mcss4dpm.biz.service.ScriptService;
import com.transing.mcss4dpm.integration.ScriptDataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
@Service("scriptService")
public class ScriptServicePojo extends BaseService implements ScriptService {
    @Resource
    private ScriptDataService scriptDataService;

    @Override
    public ApplicationBO getApplicationById(String id) {
        try {
            return scriptDataService.getApplicationById(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public ScriptInfoBO getScriptInfoByDatatypeId(String id) {
        try {
            return scriptDataService.getScriptInfoByDatatypeId(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public ScriptInfoBO getScriptInfoById(String id) {
        try {
            return scriptDataService.getScriptInfoById(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<ScriptDetailBO> getScriptDetailByScriptid(String id) {
        try {
            return scriptDataService.getScriptDetailByScriptid(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<CrawlRegulationBO> getCrawlRegulationByTypeid(String id) {
        try {
            return scriptDataService.getCrawlRegulationByTypeid(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public CrawlRegulationListBO getCrawlRegulationListByTypeid(String id) {
        try {
            return scriptDataService.getCrawlRegulationListByTypeid(id);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public void updateDevicesInfo(DevicesInf devicesInf) {
        try {
            scriptDataService.updateDevicesInfo(devicesInf);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }
}
