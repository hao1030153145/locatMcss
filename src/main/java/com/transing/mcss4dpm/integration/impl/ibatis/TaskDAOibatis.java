package com.transing.mcss4dpm.integration.impl.ibatis;

import com.jeeframework.logicframework.integration.dao.DAOException;
import com.jeeframework.logicframework.integration.dao.ibatis.BaseDaoiBATIS;
import com.transing.mcss4dpm.integration.TaskDataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Scope("prototype")
@Repository("taskDataService")
public class TaskDAOibatis extends BaseDaoiBATIS implements TaskDataService {

    @Override
    public List<Task> getTaskById(String pid) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("pid",pid);
            return sqlSessionTemplate.selectList("projectMapper.getTaskById",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据pid查询", e);
        }
    }

    @Override
    public List<Task> getTaskByTaskId(String taskId) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("taskId",taskId);
            return sqlSessionTemplate.selectList("projectMapper.getTaskByTaskId",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据taskid查询", e);
        }
    }

    @Override
    public List<TTask> getTTaskByTaskId(String taskId) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("taskId",taskId);
            return sqlSessionTemplate.selectList("projectMapper.getTTaskByTaskId",map);
        } catch (DataAccessException e) {
            throw new DAOException("根据taskid查询", e);
        }
    }

    @Override
    public Integer addTask(Task task) {
        try {
            System.out.println("XXXX: remote_mcss : 添加任务");
            return sqlSessionTemplate.insert("projectMapper.addTask",task);
        } catch (DataAccessException e) {
            System.out.println("XXXX: remote_mcss : 添加任务失败:"+e.toString());
            throw new DAOException("添加任务", e);
        }
    }

    @Override
    public int updateTaskById(String taskId, String status,int completeNu) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("taskId",taskId);
            map.put("status",status);
            map.put("completeNu",completeNu+"");
            return sqlSessionTemplate.update("projectMapper.updateTaskById",map);
        } catch (DataAccessException e) {
            throw new DAOException("添加任务", e);
        }
    }

    @Override
    public int addSubTaskList(List<SubTask> subTaskList) {
        try {
            return sqlSessionTemplate.insert("projectMapper.addSubTaskList",subTaskList);
        } catch (DataAccessException e) {
            throw new DAOException("添加任务", e);
        }
    }

    @Override
    public Integer addSubTask(SubTask subTask) {
        try {
            return sqlSessionTemplate.insert("projectMapper.addSubTask",subTask);
        } catch (DataAccessException e) {
            throw new DAOException("添加子任务", e);
        }
    }

    @Override
    public int updateSubTaskStatus(String subTaskId,String status) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("subTaskId",subTaskId);
            map.put("status",status);
            return sqlSessionTemplate.update("projectMapper.updateSubTaskStatus",map);
        } catch (DataAccessException e) {
            throw new DAOException("更新子任务状态", e);
        }
    }

    @Override
    public int updateTaskNu(String taskId, String taskNu) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("taskId",taskId);
            map.put("taskNu",taskNu);
            return sqlSessionTemplate.update("projectMapper.updateTaskNu",map);
        } catch (DataAccessException e) {
            throw new DAOException("更新任务数量", e);
        }
    }

    @Override
    public SubTask getSubTaskBySubId(String subTaskId) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("subTaskId",subTaskId);
            return sqlSessionTemplate.selectOne("projectMapper.getSubTaskBySubId",map);
        } catch (DataAccessException e) {
            throw new DAOException("添加任务", e);
        }
    }

    @Override
    public List<DealClass> getDealClasseList() {
        try {
            return sqlSessionTemplate.selectList("projectMapper.getDealClasseList");
        } catch (DataAccessException e) {
            throw new DAOException("查询处理类", e);
        }
    }

    @Override
    public List<SubTask> getSubTask(String pid) {
        try {
            HashMap<String,String> map=new HashMap<>();
            map.put("pid",pid);
            return sqlSessionTemplate.selectList("projectMapper.getSubTask",map);
        } catch (DataAccessException e) {
            throw new DAOException("添加任务", e);
        }
    }

    @Override
    public List<DevicesInf> getDevicesList(Map<String,String> param) {
        try {
            return sqlSessionTemplate.selectList("projectMapper.getDevicesList",param);
        } catch (DataAccessException e) {
            throw new DAOException("获取设备", e);
        }
    }

    @Override
    public List<DevicesInf> getDevicesByServerList(DevicesInf devicesInf) {
        try {
            return sqlSessionTemplate.selectList("projectMapper.getDevicesByServerList",devicesInf);
        } catch (DataAccessException e) {
            throw new DAOException("获取设备", e);
        }
    }
}
