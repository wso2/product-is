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
 * Bean class for OAuth2 Scope.
 */
public class OAuth2Scope {

    private int scopeId;

    private String scopeKey;

    private String name;

    private String roles;

    public OAuth2Scope(int scopeId, String scopeKey, String name, String roles) {

        this.scopeId = scopeId;
        this.scopeKey = scopeKey;
        this.name = name;
        this.roles = roles;
    }

    public int getScopeId() {

        return scopeId;
    }

    public void setScopeId(int scopeId) {

        this.scopeId = scopeId;
    }

    public String getScopeKey() {

        return scopeKey;
    }

    public void setScopeKey(String scopeKey) {

        this.scopeKey = scopeKey;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getRoles() {

        return roles;
    }

    public void setRoles(String roles) {

        this.roles = roles;
    }
}
