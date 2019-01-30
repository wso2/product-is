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

package org.wso2.identity.scenarios.commons;


import org.json.simple.JSONObject;
import org.wso2.identity.scenarios.commons.util.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.MULTI_ATTRIBUTE_SEPARATOR;

public class TestConfig {

    public enum ClaimType {

        LOCAL, CUSTOM, NONE
    }

    public static class User {

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private Map<String, String> userClaims;
        private List<String> roles;

        public User(String username, String password, String tenantDomain, String tenantAwareUsername, Map<String,
                String> userClaims, List<String> roles) {

            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.userClaims = userClaims;
            this.roles = roles;
        }

        public User(JSONObject userInfo, String tenantDomain) {
            this.tenantDomain = tenantDomain;
            populateUserInfo(userInfo);
        }

        protected void populateUserInfo(JSONObject userInfo) {

            JSONObject basicUserClaims = (JSONObject) userInfo.get("basic");
            this.userClaims = new HashMap<>();
            for (Object key : basicUserClaims.keySet()) {
                String claimUri = (String)key;
                String claimValue = basicUserClaims.get(claimUri).toString();
                if (Constants.ClaimURIs.USER_NAME_CLAIM_URI.equals(claimUri)) {
                    this.username = claimValue;
                    this.tenantAwareUsername = claimValue;
                } else if (Constants.ClaimURIs.ROLE_CLAIM_URI.equals(claimUri)) {
                    this.roles = Arrays.asList(claimValue.split(MULTI_ATTRIBUTE_SEPARATOR));
                } else if (Constants.TENANT_DOMAIN.equals(claimUri)) {
                    this.tenantDomain = claimValue;
                } else if (Constants.PASSWORD.equals(claimUri)) {
                    this.password = claimValue;
                } else {
                    this.userClaims.put(claimUri, claimValue);
                }

                if (!SUPER_TENANT_DOMAIN_NAME.equals(this.tenantDomain)) {
                    this.username = this.tenantAwareUsername + "@" + this.tenantDomain;
                }
            }
        }

        public String getUsername() {

            return username;
        }

        public String getPassword() {

            return password;
        }

        public String getTenantDomain() {

            return tenantDomain;
        }

        public String getTenantAwareUsername() {

            return tenantAwareUsername;
        }

        public Map<String, String> getUserClaims() {
            return userClaims;
        }

        public void setUserClaims(Map<String, String> userClaims) {
            this.userClaims = userClaims;
        }

        public String getUserClaim(String claimURi) {
            return userClaims.get(claimURi);
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        @Override
        public String toString() {

            return "User [" +
                    "  userName=" + this.tenantAwareUsername +
                    ", tenantDomain=" + this.tenantDomain +
                    ']';
        }
    }

    private TestUserMode userMode;
    private User user;
    private ClaimType claimType;

    public TestConfig(TestUserMode userMode, User user, ClaimType claimType) {

        this.userMode = userMode;
        this.user = user;
        this.claimType = claimType;
    }

    public TestUserMode getUserMode() {

        return userMode;
    }

    public User getUser() {

        return user;
    }

    public ClaimType getClaimType() {

        return claimType;
    }

    @Override
    public String toString() {

        return "TestConfig[" +
                "  userMode=" + userMode.name() +
                ", user=" + user.getUsername() +
                ", claimType=" + claimType +
                ']';
    }

}
