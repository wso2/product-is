/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.utils;

/**
 * Constants for Bulk User import logging test cases.
 */
public class BulkUserImportConstants {

    public static final String AUDIT_LOG_FILE_NAME = "audit.log";
    public static final String BULK_USER_IMPORT_ERROR_FILE = "bulkImportUsersMixed.csv";
    public static final String BULK_USER_IMPORT_SUCCESS_FILE = "bulkUserImport.csv";
    public static final String LOG_FILE_NAME = "bulkuserimport.log";

    public static final String BULK_USER_IMPORT_OP = "bulk_user_import";
    public static final String ENCODING = "UTF-8";
    public static final String USER_STORE_DOMAIN = "PRIMARY";

    public static final String PIPE_REGEX = "\\|";
    public static final String RESULT_REGEX = "Result : ";

    // Json Parameters
    public static final String CAUSE = "cause";
    public static final String COUNT = "count";
    public static final String DUPLICATE_USERS = "duplicateUsers";
    public static final String FAILED_USERS = "failedUsers";
    public static final String SUCCESS_COUNT = "successCount";
    public static final String USERS = "users";
    public static final String USER_NAME = "name";

    // Asserts
    public static final String DUPLICATE_USERNAME = "name3";
    public static final String FAILED_CAUSE = "Claims and values are not in correct format";
    public static final String FAILED_USERNAME = "name522";
    public static final int DUPLICATE_USER_COUNT = 1;
    public static final int FAILED_USER_COUNT = 1;
    public static final int SUCCESS_USER_COUNT = 4;
    public static final int SUCCESS_USER_COUNT_IN_MIXED_MODE = 3;
}
