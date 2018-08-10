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

package org.wso2.carbon.is.migration.service.v540;

/**
 * Holds the SQL queries and related constants
 */
public class SQLConstants {

    public static final String GET_ALL_OAUTH2_SCOPES = "SELECT SCOPE_ID, SCOPE_KEY, NAME, ROLES FROM IDN_OAUTH2_SCOPE";

    public static final String ADD_SCOPE_BINDINGS = "INSERT INTO IDN_OAUTH2_SCOPE_BINDING (SCOPE_ID, SCOPE_BINDING) " +
            "VALUES (?, ?)";

    public static final String UPDATE_OAUTH2_SCOPES = "UPDATE IDN_OAUTH2_SCOPE SET NAME = ? WHERE SCOPE_ID = ? ";

    public static final String RETRIEVE_ALL_CONSUMER_APPS = "SELECT CONSUMER_KEY, TENANT_ID FROM " +
            "IDN_OAUTH_CONSUMER_APPS";

    public static final String UPDATE_EXPIRY_TIMES_IN_CONSUMER_APPS = "UPDATE IDN_OAUTH_CONSUMER_APPS SET " +
            "USER_ACCESS_TOKEN_EXPIRE_TIME=?, APP_ACCESS_TOKEN_EXPIRE_TIME=?, REFRESH_TOKEN_EXPIRE_TIME=? WHERE " +
            "CONSUMER_KEY=?";

    public static final String SELECT_ALL_PERMISSIONS = "SELECT UM_ID, UM_RESOURCE_ID, UM_ACTION, UM_TENANT_ID FROM "
            + "UM_PERMISSION";

    public static final String UPDATE_UM_ROLE_PERMISSION = "UPDATE UM_ROLE_PERMISSION SET UM_PERMISSION_ID = ? WHERE "
            + "UM_PERMISSION_ID = ? AND UM_TENANT_ID = ?";

    public static final String UPDATE_UM_USER_PERMISSION = "UPDATE UM_USER_PERMISSION SET UM_PERMISSION_ID = ? WHERE "
            + "UM_PERMISSION_ID = ? AND UM_TENANT_ID = ?";

    public static final String DELETE_DUPLICATED_PERMISSIONS = "DELETE FROM UM_PERMISSION WHERE UM_ID = ?";

    public static final String SELECT_ALL_ROLE_PERMISSIONS = "SELECT UM_ID, UM_PERMISSION_ID, UM_ROLE_NAME, "
            + "UM_TENANT_ID, UM_DOMAIN_ID FROM UM_ROLE_PERMISSION";

    public static final String DELETE_DUPLICATED_ROLE_PERMISSIONS = "DELETE FROM UM_ROLE_PERMISSION WHERE UM_ID = ?";

    public static final String SELECT_ALL_USER_PERMISSIONS = "SELECT UM_ID, UM_PERMISSION_ID, UM_USER_NAME, "
            + "UM_TENANT_ID FROM UM_USER_PERMISSION";

    public static final String DELETE_DUPLICATED_USER_PERMISSIONS = "DELETE FROM UM_USER_PERMISSION WHERE UM_ID = ?";
}
