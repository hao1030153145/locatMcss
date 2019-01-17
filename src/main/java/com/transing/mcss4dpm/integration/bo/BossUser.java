package com.transing.mcss4dpm.integration.bo;

/**
 * 用户对象
 *
 * @author lanceyan
 * @version 1.0
 * @see com.jeeframework.logicframework.integration.bo.AbstractBO
 */
public class BossUser extends UserBase {
    private String userName;
    private Integer type;
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}