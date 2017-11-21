/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.openid;

public class OpenIDUtils {

    public enum User {
        ADMIN("openidadmin","openidadmin", "openidadmin@wso2.com", "admin"),
        USER1("openiduser1", "openiduser1", "openiduser1@wso2.com", "internal/everyone"),
        USER2("openiduser2", "openiduser2", "openiduser2@wso2.com", "internal/everyone");

        private String username;
        private String password;
        private String email;
        private String role;

        User(String username, String password, String email, String role) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    };

    public enum UserConsent{
        APPROVE,
        APPROVE_ALWAYS,
        SKIP
    };

    public enum AppType{
        SMART_WITH_CLAIMS("travelocity.com-openid-smartconsumerwithclaims"),
        SMART_WITHOUT_CLAIMS("travelocity.com-openid-smartconsumerwithoutclaims"),
        DUMB_WITH_CLAIMS("travelocity.com-openid-dumbconsumerwithclaims"),
        DUMB_WITHOUT_CLAIMS("travelocity.com-openid-dumbconsumerwithoutclaims");

        private String artifact;

        AppType(String artifact) {
            this.artifact = artifact;
        }

        public String getArtifact() {
            return artifact;
        }
    }

    public static class OpenIDConfig {

        private User user;
        private UserConsent userConsent;
        private AppType appType;


        public OpenIDConfig(User user, UserConsent userConsent, AppType appType) {
            this.user = user;
            this.userConsent = userConsent;
            this.appType = appType;
        }

        public User getUser() {
            return user;
        }

        public UserConsent getUserConsent() {
            return userConsent;
        }

        public AppType getAppType() {
            return appType;
        }

        @Override
        public String toString() {
            return "OpenIdConfig[" +
                    "user=" + user +
                    ", userConsent=" + userConsent +
                    ", appType=" + appType +
                    ']';
        }
    }

}
