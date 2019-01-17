package com.transing.mcss4dpm.integration.bo;

/**
 * 任务对象
 */
public class Task {
    private long taskId;

    private long pId;

    private String detailId;

    private String projectId;

    private String firstDetailId;

    private String flowId;

    private String taskName;

    private String dataSource;

    private String dataType;

    private String startTime;

    private String completeTime;

    private String completeNu;

    private String status; //0:待运行 1:完成 2:停止 9:出错

    private String inputPara;

    private String taskNu;

    private String batchNo;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getpId() {
        return pId;
    }

    public void setpId(long pId) {
        this.pId = pId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    public String getCompleteNu() {
        return completeNu;
    }

    public void setCompleteNu(String completeNu) {
        this.completeNu = completeNu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInputPara() {
        return inputPara;
    }

    public void setInputPara(String inputPara) {
        this.inputPara = inputPara;
    }

    public String getTaskNu() {
        return taskNu;
    }

    public void setTaskNu(String taskNu) {
        this.taskNu = taskNu;
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFirstDetailId() {
        return firstDetailId;
    }

    public void setFirstDetailId(String firstDetailId) {
        this.firstDetailId = firstDetailId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
}
