/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.service.v550.bean;

public class OauthTokenInfo {

    private String accessToken;
    private String accessTokenHash;
    private String refreshTokenhash;


    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public String getRefreshTokenhash() {
        return refreshTokenhash;
    }

    public void setRefreshTokenhash(String refreshTokenhash) {
        this.refreshTokenhash = refreshTokenhash;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    private String refreshToken;

    private String tokenId;

    public OauthTokenInfo(String accessToken, String refreshToken, String tokenId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenId = tokenId;
    }

    public OauthTokenInfo(String tokenId) {
        this.tokenId = tokenId;
    }
}
