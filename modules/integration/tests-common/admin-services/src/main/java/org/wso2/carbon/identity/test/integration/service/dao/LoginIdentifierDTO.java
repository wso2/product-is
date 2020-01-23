package org.wso2.carbon.identity.test.integration.service.dao;

import java.io.Serializable;

public class LoginIdentifierDTO implements Serializable {

    private String loginKey;
    private String loginValue;
    private String profileName;
    private LoginIdentifierDTO.LoginIdentifierType loginIdentifierType;

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

    public LoginIdentifierType getLoginIdentifierType() {

        return loginIdentifierType;
    }

    public void setLoginIdentifierType(LoginIdentifierType loginIdentifierType) {

        this.loginIdentifierType = loginIdentifierType;
    }

    public static enum LoginIdentifierType {
        CLAIM_URI,
        ATTRIBUTE;

        private LoginIdentifierType() {}
    }
}
