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

package org.wso2.carbon.is.migration.service.v540.bean;

public class RolePermission {

    private int id;

    private int permissionId;

    private String roleName;

    private int tenantId;

    private int domainId;

    public RolePermission(int id, int permissionId, String roleName, int tenantId, int domainId) {

        this.id = id;
        this.permissionId = permissionId;
        this.roleName = roleName;
        this.tenantId = tenantId;
        this.domainId = domainId;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getPermissionId() {

        return permissionId;
    }

    public void setPermissionId(int permissionId) {

        this.permissionId = permissionId;
    }

    public String getRoleName() {

        return roleName;
    }

    public void setRoleName(String roleName) {

        this.roleName = roleName;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public int getDomainId() {

        return domainId;
    }

    public void setDomainId(int domainId) {

        this.domainId = domainId;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof RolePermission)) {
            return false;
        }

        RolePermission rolePermission = (RolePermission) object;

        return this.permissionId == rolePermission.permissionId && this.roleName.equals(rolePermission.roleName)
                && this.tenantId == rolePermission.tenantId && this.domainId == rolePermission.domainId;
    }
}
