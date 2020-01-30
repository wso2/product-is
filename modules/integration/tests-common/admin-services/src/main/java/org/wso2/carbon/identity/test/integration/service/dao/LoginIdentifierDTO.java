package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;

public class LoginIdentifierDTO implements Serializable {

    private String loginKey;
    private String loginValue;
    private String profileName;
    private String loginIdentifierType;

    public String getLoginKey() {

        return loginKey;
    }

    public void setLoginKey(String loginKey) {

        this.loginKey = loginKey;
    }

    public String getLoginValue() {

        return loginValue;
    }

    public void setLoginValue(String loginValue) {

        this.loginValue = loginValue;
    }

    public String getProfileName() {

        return profileName;
    }

    public void setProfileName(String profileName) {

        this.profileName = profileName;
    }

    public String getLoginIdentifierType() {

        return loginIdentifierType;
    }

    public void setLoginIdentifierType(String loginIdentifierType) {

        this.loginIdentifierType = loginIdentifierType;
    }
}
