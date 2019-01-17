package com.transing.mcss4dpm.biz.service.impl.local;

import com.jeeframework.logicframework.biz.exception.BizException;
import com.jeeframework.logicframework.biz.service.BaseService;
import com.jeeframework.logicframework.integration.dao.DAOException;
import com.transing.mcss4dpm.biz.service.CommonService;
import com.transing.mcss4dpm.integration.CommonDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/15
 */
@Service("commonService")
public class CommonServicePojo extends BaseService implements CommonService {
    @Resource
    private CommonDataService commonDataService;


    @Override
    public Integer addDevices(String devicesName) {
        try {
            return commonDataService.addDevices(devicesName);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public Integer removeDevices(String devicesName) {
        try {
            return commonDataService.removeDevices(devicesName);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }
}
