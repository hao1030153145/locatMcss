package com.transing.mcss4dpm.biz.service;

import com.jeeframework.logicframework.biz.service.BizService;
import com.transing.mcss4dpm.integration.bo.*;

import java.util.List;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
public interface ScriptService extends BizService {
    ApplicationBO getApplicationById(String id);

    ScriptInfoBO getScriptInfoByDatatypeId(String id);

    ScriptInfoBO getScriptInfoById(String id);

    List<ScriptDetailBO> getScriptDetailByScriptid(String id);

    List<CrawlRegulationBO> getCrawlRegulationByTypeid(String id);

    CrawlRegulationListBO getCrawlRegulationListByTypeid(String id);

    void updateDevicesInfo(DevicesInf devicesInf);
}
