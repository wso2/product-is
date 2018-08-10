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

public class UserPermission {

    private int id;

    private int permissionId;

    private String userName;

    private int tenantId;

    public UserPermission(int id, int permissionId, String userName, int tenantId) {

        this.id = id;
        this.permissionId = permissionId;
        this.userName = userName;
        this.tenantId = tenantId;
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

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof UserPermission)) {
            return false;
        }

        UserPermission userPermission = (UserPermission) object;

        return this.permissionId == userPermission.permissionId && this.userName.equals(userPermission.userName)
                && this.tenantId == userPermission.tenantId;
    }
}
