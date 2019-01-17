package com.transing.mcss4dpm.biz.service.impl.local;

import com.jeeframework.logicframework.biz.exception.BizException;
import com.jeeframework.logicframework.biz.service.BaseService;
import com.jeeframework.logicframework.integration.dao.DAOException;
import com.transing.mcss4dpm.biz.service.TaskService;
import com.transing.mcss4dpm.integration.TaskDataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("taskService")
public class TaskServicePojo extends BaseService implements TaskService {
    @Resource
    private TaskDataService taskDataService;

    @Override
    public List<Task> getTaskById(String pid) {
        try {
            return taskDataService.getTaskById(pid);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<Task> getTaskByTaskId(String taskId) {
        try {
            return taskDataService.getTaskByTaskId(taskId);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<TTask> getTTaskByTaskId(String taskId) {
        try {
            return taskDataService.getTTaskByTaskId(taskId);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public Integer addTask(Task task) {
        try {
            return taskDataService.addTask(task);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public int updateTaskById(String taskId, String status,int completeNu) {
        try {
            return taskDataService.updateTaskById(taskId, status,completeNu);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public int addSubTaskList(List<SubTask> subTaskList) {
        try {
            return taskDataService.addSubTaskList(subTaskList);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public Integer addSubTask(SubTask subTask) {
        try {
            return taskDataService.addSubTask(subTask);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public int updateSubTaskStatus(String subTaskId,String status) {
        try {
            return taskDataService.updateSubTaskStatus(subTaskId,status);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public int updateTaskNu(String taskId, String taskNu) {
        try {
            return taskDataService.updateTaskNu(taskId,taskNu);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public SubTask getSubTaskBySubId(String subTaskId) {
        try {
            return taskDataService.getSubTaskBySubId(subTaskId);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<DealClass> getDealClasseList() {
        try {
            return taskDataService.getDealClasseList();
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<SubTask> getSubTask(String pid) {
        try {
            return taskDataService.getSubTask(pid);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<DevicesInf> getDevicesList(Map<String,String> param ) {
        try {
            return taskDataService.getDevicesList(param);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }

    @Override
    public List<DevicesInf> getDevicesByServerList(DevicesInf devicesInf) {
        try {
            return taskDataService.getDevicesByServerList(devicesInf);
        } catch (DAOException e) {
            throw new BizException(e);
        }
    }
}
