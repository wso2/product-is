/*
 *
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.is.portal.user.client.api.bean;

import java.io.Serializable;

/**
 * Bean that encapsulates the configuration info.
 */
public class PasswordHistoryBean implements Serializable {

    private static final long serialVersionUID = -2913500114444797062L;
    private boolean isEnabled = true;
    private int minCountToAllowRepitition = 3;
    private int minAgeToAllowRepitition = 7;
    private String passwordHistoryDataStoreClass = null;
    private String hashingAlgorithm = "SHA-256";

    public String getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public void setHashingAlgorithm(String hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public String getPasswordHistoryDataStoreClass() {
        return passwordHistoryDataStoreClass;
    }

    public void setPasswordHistoryDataStoreClass(String passwordHistoryDataStoreClass) {
        this.passwordHistoryDataStoreClass = passwordHistoryDataStoreClass;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getMinCountToAllowRepitition() {
        return minCountToAllowRepitition;
    }

    public void setMinCountToAllowRepitition(int minCountToAllowRepitition) {
        this.minCountToAllowRepitition = minCountToAllowRepitition;
    }

    public int getMinAgeToAllowRepitition() {
        return minAgeToAllowRepitition;
    }

    public void setMinAgeToAllowRepitition(int minAgeToAllowRepitition) {
        this.minAgeToAllowRepitition = minAgeToAllowRepitition;
    }
}
