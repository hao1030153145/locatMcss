package com.transing.mcss4dpm.integration.impl.ibatis;

import com.jeeframework.logicframework.integration.dao.DAOException;
import com.jeeframework.logicframework.integration.dao.ibatis.BaseDaoiBATIS;
import com.transing.mcss4dpm.integration.CommonDataService;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2017/12/15
 */
@Scope("prototype")
@Repository("commonDataService")
public class CommonDAOibatis extends BaseDaoiBATIS implements CommonDataService {
    @Override
    public Integer addDevices(String devicesName) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("devicesName",devicesName);
            sqlSessionTemplate.insert("commonMapper.addDevices",map);
            return 1;
        } catch (DataAccessException e) {
            throw new DAOException("添加设备", e);
        }
    }

    @Override
    public Integer removeDevices(String devicesName) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("devicesName",devicesName);
            sqlSessionTemplate.delete("commonMapper.removeDevices",map);
            return 1;
        } catch (DataAccessException e) {
            throw new DAOException("删除设备", e);
        }
    }
}
