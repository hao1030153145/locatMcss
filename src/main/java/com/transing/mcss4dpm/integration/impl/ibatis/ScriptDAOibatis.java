package com.transing.mcss4dpm.integration.impl.ibatis;

import com.jeeframework.logicframework.integration.dao.DAOException;
import com.jeeframework.logicframework.integration.dao.ibatis.BaseDaoiBATIS;
import com.transing.mcss4dpm.integration.ScriptDataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * ${description}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
@Scope("prototype")
@Repository("scriptDataService")
public class ScriptDAOibatis extends BaseDaoiBATIS implements ScriptDataService {
    @Override
    public ApplicationBO getApplicationById(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("id",id+"");
            return sqlSessionTemplate.selectOne("scriptMapper.getApplicationById",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }

    @Override
    public ScriptInfoBO getScriptInfoByDatatypeId(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("datatypeid",id+"");
            return sqlSessionTemplate.selectOne("scriptMapper.getScriptInfoByDatatypeId",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }

    @Override
    public ScriptInfoBO getScriptInfoById(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("id",id+"");
            return sqlSessionTemplate.selectOne("scriptMapper.getScriptInfoById",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }

    @Override
    public List<ScriptDetailBO> getScriptDetailByScriptid(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("scriptid",id+"");
            return sqlSessionTemplate.selectList("scriptMapper.getScriptDetailByScriptid",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }

    @Override
    public List<CrawlRegulationBO> getCrawlRegulationByTypeid(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("scriptid",id+"");
            return sqlSessionTemplate.selectList("scriptMapper.getCrawlRegulationByTypeid",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }

    @Override
    public CrawlRegulationListBO getCrawlRegulationListByTypeid(String id) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("scriptid",id+"");
            return sqlSessionTemplate.selectOne("scriptMapper.getCrawlRegulationListByTypeid",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询抓取规则列表", e);
        }
    }

    @Override
    public void updateDevicesInfo(DevicesInf devicesInf) {
        try {
            sqlSessionTemplate.update("scriptMapper.updateDevicesInfo",devicesInf);
        } catch (DataAccessException e) {
            throw new DAOException("根据id查询应用失败", e);
        }
    }
}
