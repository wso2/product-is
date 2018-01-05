/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.is.migration.service.v540.bean;

/**
 * Bean class for Service Provider specific OAuth2 token expiry times.
 */
public class SpOAuth2ExpiryTimeConfiguration {

    private String consumerKey;

    private Long userAccessTokenExpiryTime;

    private Long refreshTokenExpiryTime;

    private Long applicationAccessTokenExpiryTime;

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
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

