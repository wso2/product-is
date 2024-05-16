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

package org.wso2.identity.integration.test.rest.api.server.roles.v2.model;

import java.util.List;

/**
 * Represents a role in the REST API version 2.
 * This class encapsulates the properties of a role, including its audience type,
 * display name, associated permissions, and schemas.
 */

public class RoleV2 {

    private Audience audience;
    private String displayName;
    private List<Permission> permissions;
    private List<String> schemas;

    public RoleV2(Audience audience, String displayName, List<Permission> permissions, List<String> schemas) {

        this.audience = audience;
        this.displayName = displayName;
        this.permissions = permissions;
        this.schemas = schemas;
    }

    public RoleV2(String displayName, List<Permission> permissions, List<String> schemas) {

        this.displayName = displayName;
        this.permissions = permissions;
        this.schemas = schemas;
    }

    public Audience getAudience() {
        return audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        return "Role{" +
                "audience=" + audience +
                ", displayName='" + displayName + '\'' +
                ", permissions=" + permissions +
                ", schemas=" + schemas +
                '}';
    }
}

