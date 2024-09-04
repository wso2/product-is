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

import java.util.Map;

public class AuthorizingUser {

    private String username;
    private String password;

    private String userId;

    private Map<UserClaimConfig, Object> userClaims;

    private AuthorizingUser(Builder builder) {

        this.username = builder.username;
        this.password = builder.password;
        this.userClaims = builder.userClaims;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public Map<UserClaimConfig, Object> getUserClaims() {

        return userClaims;
    }

    public static class Builder {

        private String username;
        private String password;

        private Map<UserClaimConfig, Object> userClaims;

        public Builder username(String username) {

            this.username = username;
            return this;
        }

        public Builder password(String password) {

            this.password = password;
            return this;
        }

        public Builder userClaims(Map<UserClaimConfig, Object> userClaims) {

            this.userClaims = userClaims;
            return this;
        }

        public AuthorizingUser build() {

            return new AuthorizingUser(this);
        }
    }
}
