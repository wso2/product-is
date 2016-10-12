/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oidc.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OIDCUser {

    private String username;
    private String password;
    private String profile;
    private Map<String, String> userClaims = null;
    private List<String> roles = null;

    public OIDCUser() {

    }

    public OIDCUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Map<String, String> getUserClaims() {
        if (userClaims == null) {
            userClaims = new HashMap<>();
        }
        return userClaims;
    }

    public void setUserClaims(Map<String, String> userClaims) {
        this.userClaims = userClaims;
    }

    public void addUserClaim(String claimUri, String claimValue) {
        Map<String, String> userClaims = getUserClaims();
        userClaims.put(claimUri, claimValue);
    }

    public List<String> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
        }
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        List<String> roles = getRoles();
        if (!roles.contains(role)){
            roles.add(role);
        }
    }
}
