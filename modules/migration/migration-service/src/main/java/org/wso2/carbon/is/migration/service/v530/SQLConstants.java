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

package org.wso2.carbon.is.migration.service.v530;

/**
 * Holds the SQL queries and related constants
 */
public class SQLConstants {

    private SQLConstants() {
    }

    public static final String ID_COLUMN = "ID";

    public static final String LOAD_CLAIM_DIALECTS = "SELECT * FROM UM_DIALECT";
    public static final String LOAD_MAPPED_ATTRIBUTE = "SELECT * FROM UM_CLAIM WHERE UM_DIALECT_ID = ?";

    // Claim Dialect SQLs
    public static final String ADD_CLAIM_DIALECT = "INSERT INTO IDN_CLAIM_DIALECT (DIALECT_URI, TENANT_ID) " +
                                                   "VALUES (?, ?)";
    public static final String GET_CLAIM_DIALECT = "SELECT ID FROM IDN_CLAIM_DIALECT WHERE DIALECT_URI= ? AND " +
                                                   "TENANT_ID = ? ";

    // Claim SQLs
    public static final String GET_CLAIM_ID = "SELECT ID FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT ID FROM " +
            "IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?) AND CLAIM_URI=? AND TENANT_ID=?";

    // Claim SQLs
    public static final String GET_CLAIM_URI = "SELECT CLAIM_URI FROM IDN_CLAIM WHERE DIALECT_ID=(SELECT ID FROM " +
            "IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?) AND ID=? AND TENANT_ID=?";

    public static final String ADD_CLAIM = "INSERT INTO IDN_CLAIM (DIALECT_ID, CLAIM_URI, TENANT_ID) VALUES ((SELECT " +
            "ID FROM IDN_CLAIM_DIALECT WHERE DIALECT_URI=? AND TENANT_ID=?), ?, ?)";

    // Local Claim Attributes SQLs
    public static final String ADD_CLAIM_MAPPED_ATTRIBUTE = "INSERT INTO IDN_CLAIM_MAPPED_ATTRIBUTE (LOCAL_CLAIM_ID, " +
            "USER_STORE_DOMAIN_NAME, ATTRIBUTE_NAME, TENANT_ID) VALUES (?, ?, ?, ?)";
    public static final String GET_CLAIM_FROM_MAPPED_ATTRIBUTE = "SELECT LOCAL_CLAIM_ID " +
            "FROM IDN_CLAIM_MAPPED_ATTRIBUTE WHERE USER_STORE_DOMAIN_NAME = ? AND ATTRIBUTE_NAME =? AND TENANT_ID=?";

    // Local Claim Properties SQLs
    public static final String ADD_CLAIM_PROPERTY = "INSERT INTO IDN_CLAIM_PROPERTY (LOCAL_CLAIM_ID, PROPERTY_NAME, " +
            "PROPERTY_VALUE, TENANT_ID) VALUES (?, ?, ?, ?)";

    // External Claim Mapping SQLs
    public static final String ADD_CLAIM_MAPPING = "INSERT INTO IDN_CLAIM_MAPPING (MAPPED_LOCAL_CLAIM_ID, " +
            "EXT_CLAIM_ID, TENANT_ID) VALUES (?, ?, ?)";

    //Select all entries for given permission string
    public static final String SELECT_PERMISSION = "SELECT UM_ID, UM_RESOURCE_ID, UM_ACTION , UM_TENANT_ID, " +
            "UM_MODULE_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID=?";

    //Select permission entry count
    public static final String SELECT_PERMISSION_COUNT = "SELECT COUNT(1) FROM UM_PERMISSION WHERE UM_RESOURCE_ID=? " +
            "AND UM_ACTION=? AND UM_TENANT_ID=? AND UM_MODULE_ID=?";

    //Select role count with permission
    public static final String SELECT_ROLE_PERMISSION_COUNT = "SELECT COUNT(1) FROM UM_ROLE_PERMISSION WHERE " +
            "UM_PERMISSION_ID=? AND UM_ROLE_NAME=? AND UM_IS_ALLOWED=? AND UM_TENANT_ID=? AND UM_DOMAIN_ID=?";

    //insert permission
    public static final String INSERT_PERMISSION = "INSERT INTO UM_PERMISSION (UM_RESOURCE_ID, UM_ACTION , " +
            "UM_TENANT_ID, UM_MODULE_ID) VALUES (?, ?, ?, ?)";

    //Select role entries with permission
    public static final String SELECT_ROLES_WITH_PERMISSION = "SELECT UM_PERMISSION_ID, UM_ROLE_NAME, UM_IS_ALLOWED, " +
            "UM_TENANT_ID, UM_DOMAIN_ID FROM UM_ROLE_PERMISSION WHERE UM_PERMISSION_ID=?";

    //Select permission entries with permission string in tenant
    public static final String SELECT_PERMISSION_IN_TENANT = "SELECT UM_ID, UM_RESOURCE_ID, UM_ACTION , " +
            "UM_TENANT_ID, UM_MODULE_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID=? AND UM_TENANT_ID=?";

    //Insert permission for role
    public static final String INSERT_ROLES_WITH_PERMISSION = "INSERT INTO UM_ROLE_PERMISSION (UM_PERMISSION_ID," +
            "UM_ROLE_NAME, UM_IS_ALLOWED, UM_TENANT_ID, UM_DOMAIN_ID) VALUES (?, ?, ?,?, ?)";

}
