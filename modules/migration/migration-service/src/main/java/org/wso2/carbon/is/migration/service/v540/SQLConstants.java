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
}
