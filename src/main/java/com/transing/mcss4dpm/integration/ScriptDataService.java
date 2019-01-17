package com.transing.mcss4dpm.integration;

import com.jeeframework.logicframework.integration.DataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
@Scope("prototype")
@Repository("scriptDataService")
public interface ScriptDataService extends DataService {
    ApplicationBO getApplicationById(String id);

    ScriptInfoBO getScriptInfoByDatatypeId(String id);

    ScriptInfoBO getScriptInfoById(String id);

    List<ScriptDetailBO> getScriptDetailByScriptid(String id);

    List<CrawlRegulationBO> getCrawlRegulationByTypeid(String id);

    CrawlRegulationListBO getCrawlRegulationListByTypeid(String id);

    void updateDevicesInfo(DevicesInf devicesInf);
}
