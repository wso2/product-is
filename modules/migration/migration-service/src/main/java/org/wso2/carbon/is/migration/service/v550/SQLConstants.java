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

package org.wso2.carbon.is.migration.service.v550;

public class SQLConstants {

    public static final String RETRIEVE_ACCESS_TOKEN_TABLE_MYSQL =
            "SELECT ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM " + "IDN_OAUTH2_ACCESS_TOKEN LIMIT 1";

    public static final String RETRIEVE_ACCESS_TOKEN_TABLE_DB2SQL =
            "SELECT ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM " + "IDN_OAUTH2_ACCESS_TOKEN FETCH FIRST 1 ROWS ONLY";

    public static final String RETRIEVE_ACCESS_TOKEN_TABLE_MSSQL =
            "SELECT TOP 1 ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM " + "IDN_OAUTH2_ACCESS_TOKEN";

    public static final String RETRIEVE_ACCESS_TOKEN_TABLE_INFORMIX =
            "SELECT FIRST 1 ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM " + "IDN_OAUTH2_ACCESS_TOKEN";

    public static final String RETRIEVE_ACCESS_TOKEN_TABLE_ORACLE =
            "SELECT ACCESS_TOKEN_HASH, REFRESH_TOKEN_HASH FROM " + "IDN_OAUTH2_ACCESS_TOKEN WHERE ROWNUM < 2";

    public static final String RETRIEVE_AUTHORIZATION_CODE_TABLE_MYSQL =
            "SELECT AUTHORIZATION_CODE_HASH FROM " + "IDN_OAUTH2_AUTHORIZATION_CODE LIMIT 1";

    public static final String RETRIEVE_AUTHORIZATION_CODE_TABLE_DB2SQL =
            "SELECT AUTHORIZATION_CODE_HASH FROM " + "IDN_OAUTH2_AUTHORIZATION_CODE FETCH FIRST 1 ROWS ONLY";

    public static final String RETRIEVE_AUTHORIZATION_CODE_TABLE_MSSQL =
            "SELECT TOP 1 AUTHORIZATION_CODE_HASH FROM " + "IDN_OAUTH2_AUTHORIZATION_CODE";

    public static final String RETRIEVE_AUTHORIZATION_CODE_TABLE_INFORMIX =
            "SELECT FIRST 1 AUTHORIZATION_CODE_HASH FROM " + "IDN_OAUTH2_AUTHORIZATION_CODE";

    public static final String RETRIEVE_AUTHORIZATION_CODE_TABLE_ORACLE =
            "SELECT AUTHORIZATION_CODE_HASH FROM " + "IDN_OAUTH2_AUTHORIZATION_CODE WHERE ROWNUM < 2";

    public static final String ADD_ACCESS_TOKEN_HASH_COLUMN =
            "ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD ACCESS_TOKEN_HASH VARCHAR(512)";


}
