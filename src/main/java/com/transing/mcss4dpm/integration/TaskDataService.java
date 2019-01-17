package com.transing.mcss4dpm.integration;

import com.jeeframework.logicframework.integration.DataService;
import com.transing.mcss4dpm.integration.bo.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Scope("prototype")
@Repository("taskDataService")
public interface TaskDataService extends DataService {

    List<Task> getTaskById(String pid);

    List<Task> getTaskByTaskId(String taskId);

    List<TTask> getTTaskByTaskId(String taskId);

    Integer addTask(Task task);

    int updateTaskById(String taskId, String status,int completeNu);

    int addSubTaskList(List<SubTask> subTaskList);

    Integer addSubTask(SubTask subTask);

    int updateSubTaskStatus(String subTaskId,String status);

    int updateTaskNu(String taskId, String taskNu);

    SubTask getSubTaskBySubId(String subTaskId);
    /**
     * 简单描述：查询处理类数据源类型对应列表
     * <p/>
     *
     * @
     */
    List<DealClass> getDealClasseList();

    List<SubTask> getSubTask(String pid);

    List<DevicesInf> getDevicesList(Map<String,String> param);

    List<DevicesInf> getDevicesByServerList(DevicesInf devicesInf);
}
