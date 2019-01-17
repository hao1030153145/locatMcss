package com.transing.mcss4dpm.biz.service;

import com.jeeframework.logicframework.biz.service.BizService;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/15
 */
public interface CommonService extends BizService {
    Integer addDevices(String devicesName);

    Integer removeDevices(String devicesName);
}
