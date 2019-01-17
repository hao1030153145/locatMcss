package com.transing.mcss4dpm.integration.bo;

import java.util.Date;

/**
 * ${脚本信息模型}
 *
 * @author weiqiliu
 * @version 1.0 2018/1/22
 */
public class ScriptInfoBO {
    private int id;
    private int appId;
    private int deviceId;
    private int datasourceId;
    private int datatypeId;
    private String name;
    private int status;
    private Date createTime;
    private Date updatedDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(int datasourceId) {
        this.datasourceId = datasourceId;
    }

    public int getDatatypeId() {
        return datatypeId;
    }

    public void setDatatypeId(int datatypeId) {
        this.datatypeId = datatypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
