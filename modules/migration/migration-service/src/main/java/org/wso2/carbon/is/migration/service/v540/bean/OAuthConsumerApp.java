/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.is.migration.service.v540.bean;

/**
 * Bean class for Oauth2 Consumer App.
 */
public class OAuthConsumerApp {

    private String consumerKey;

    private int tenantId;

    private Long userAccessTokenExpiryTime;

    private Long refreshTokenExpiryTime;

    private Long applicationAccessTokenExpiryTime;

    public OAuthConsumerApp(String consumerKey, int tenantId) {

        this.consumerKey = consumerKey;
        this.tenantId = tenantId;
    }

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public Long getUserAccessTokenExpiryTime() {

        return userAccessTokenExpiryTime;
    }

    public void setUserAccessTokenExpiryTime(Long userAccessTokenExpiryTime) {

        this.userAccessTokenExpiryTime = userAccessTokenExpiryTime;
    }

    public Long getRefreshTokenExpiryTime() {

        return refreshTokenExpiryTime;
    }

    public void setRefreshTokenExpiryTime(Long refreshTokenExpiryTime) {

        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
    }

    public Long getApplicationAccessTokenExpiryTime() {

        return applicationAccessTokenExpiryTime;
    }

    public void setApplicationAccessTokenExpiryTime(Long applicationAccessTokenExpiryTime) {

        this.applicationAccessTokenExpiryTime = applicationAccessTokenExpiryTime;
    }
}
