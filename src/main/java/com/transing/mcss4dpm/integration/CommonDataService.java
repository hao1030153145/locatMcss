package com.transing.mcss4dpm.integration;

import com.jeeframework.logicframework.integration.DataService;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/15
 */
public interface CommonDataService extends DataService {
    Integer addDevices(String devicesName);
    Integer removeDevices(String devicesName);
}
