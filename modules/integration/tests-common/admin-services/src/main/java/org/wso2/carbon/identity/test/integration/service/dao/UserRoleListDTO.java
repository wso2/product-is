package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;

public class UserRoleListDTO implements Serializable {

    private String userId;
    private String[] roleNames;

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String[] getRoleNames() {

        return roleNames;
    }

    public void setRoleNames(String[] roleNames) {

        this.roleNames = roleNames;
    }
}
