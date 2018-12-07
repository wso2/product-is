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


public class TestConfig {

    public enum ClaimType {

        LOCAL, CUSTOM, NONE
    }

    public enum User {

        SUPER_TENANT_USER("user1name", "user1pass", "carbon.super", "user1name", "user1@test.com", "nickuser1",
                true),
        TENANT_USER("user2name", "user2pass", "wso2.com", "user2name", "user2@abc.com", "Nickuser2",
                true),
        SUPER_TENANT_USER_WITHOUT_MANDATORY_CLAIMS("user3", "user3", "carbon.super", "user3", "providedClaimValue",
                "providedClaimValue", false)
        ,
        TENANT_USER_WITHOUT_MANDATORY_CLAIMS("user4", "user4", "wso2.com", "user4", "providedClaimValue",
                "providedClaimValue", false);

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;
        private String nickname;
        private boolean setUserClaims;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email,
             String nickname, boolean setUserClaims) {

            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
            this.nickname = nickname;
            this.setUserClaims = setUserClaims;
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

        public String getEmail() {

            return email;
        }

        public String getNickname() {

            return nickname;
        }

        public boolean getSetUserClaims() {

            return setUserClaims;
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
