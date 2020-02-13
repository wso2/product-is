/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
