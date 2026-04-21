/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.identity.integration.test.oauth2.dataprovider.model;

import java.util.List;

public class ApplicationConfig {

    private TokenType tokenType;
    private long expiryTime;
    private List<String> audienceList;
    private List<UserClaimConfig> requestedClaimList;
    private List<String> grantTypes;
    private boolean skipConsent;
    private long refreshTokenExpiryTime;
    private boolean enableHybridFlow;
    private List<String> responseTypes;

    private ApplicationConfig(Builder builder) {

        this.tokenType = builder.tokenType;
        this.expiryTime = builder.expiryTime;
        this.audienceList = builder.audienceList;
        this.requestedClaimList = builder.claimsList;
        this.grantTypes = builder.grantTypes;
        this.skipConsent = builder.skipConsent;
        this.refreshTokenExpiryTime = builder.refreshTokenExpiryTime;
        this.enableHybridFlow = builder.enableHybridFlow;
        this.responseTypes = builder.responseTypes;
    }

    public TokenType getTokenType() {

        return tokenType;
    }

    public long getExpiryTime() {

        return expiryTime;
    }

    public List<String> getAudienceList() {

        return audienceList;
    }

    public List<UserClaimConfig> getRequestedClaimList() {

        return requestedClaimList;
    }

    public List<String> getGrantTypes() {

        return grantTypes;
    }

    public boolean isSkipConsent() {

        return skipConsent;
    }

    public long getRefreshTokenExpiryTime() {

        return refreshTokenExpiryTime;
    }

    public boolean isEnableHybridFlow() {

        return enableHybridFlow;
    }

    public List<String> getResponseTypes() {

        return responseTypes;
    }

    public enum TokenType {
        JWT("JWT"), OPAQUE("Default");

        String tokenTypeProperty;

        TokenType(String tokenTypeProperty) {

            this.tokenTypeProperty = tokenTypeProperty;
        }

        public String getTokenTypeProperty() {

            return tokenTypeProperty;
        }
    }

    public static class Builder {

        private TokenType tokenType;
        private int expiryTime;
        private List<String> audienceList;
        private List<UserClaimConfig> claimsList;
        private List<String> grantTypes;
        private boolean skipConsent;
        private long refreshTokenExpiryTime;
        private boolean enableHybridFlow;
        private List<String> responseTypes;

        public Builder tokenType(TokenType tokenType) {

            this.tokenType = tokenType;
            return this;
        }

        public Builder expiryTime(int expiryTime) {

            this.expiryTime = expiryTime;
            return this;
        }

        public Builder audienceList(List<String> audienceList) {

            this.audienceList = audienceList;
            return this;
        }

        public Builder claimsList(List<UserClaimConfig> claimsList) {

            this.claimsList = claimsList;
            return this;
        }

        public Builder grantTypes(List<String> grantTypes) {

            this.grantTypes = grantTypes;
            return this;
        }

        public Builder skipConsent(boolean skipConsent) {

            this.skipConsent = skipConsent;
            return this;
        }

        public Builder refreshTokenExpiryTime(long refreshTokenExpiryTime) {

            this.refreshTokenExpiryTime = refreshTokenExpiryTime;
            return this;
        }

        public ApplicationConfig build() {

            return new ApplicationConfig(this);
        }

        public Builder enableHybridFlow(boolean enableHybridFlow) {

            this.enableHybridFlow = enableHybridFlow;
            return this;
        }

        public Builder responseTypes(List<String> responseTypes) {

            this.responseTypes = responseTypes;
            return this;
        }
    }
}
