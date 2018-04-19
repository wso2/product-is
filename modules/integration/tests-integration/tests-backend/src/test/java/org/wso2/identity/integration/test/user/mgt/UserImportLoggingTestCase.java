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
package org.wso2.identity.integration.test.user.mgt;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.AUDIT_LOG_FILE_NAME;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.BULK_USER_IMPORT_ERROR_FILE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.BULK_USER_IMPORT_OP;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.BULK_USER_IMPORT_SUCCESS_FILE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.CAUSE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.COUNT;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.DUPLICATE_USERNAME;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.DUPLICATE_USERS;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.DUPLICATE_USER_COUNT;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.ENCODING;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.ERROR_MESSAGE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.FAILED_CAUSE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.FAILED_USERNAME;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.FAILED_USERS;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.FAILED_USER_COUNT;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.LOG_FILE_NAME;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.PIPE_REGEX;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.RESULT_REGEX;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.SUCCESS_COUNT;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.SUCCESS_USER_COUNT;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.SUCCESS_USER_COUNT_IN_MIXED_MODE;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.USERS;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.USER_NAME;
import static org.wso2.identity.integration.test.utils.BulkUserImportConstants.USER_STORE_DOMAIN;

/**
 * Test cases for the bulkUserImport log appender.
 * This will check the logs are directed to the new file.
 */
public class UserImportLoggingTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserImportLoggingTestCase.class);

    private final String BULK_USER_FILE = getISResourceLocation() + File.separator + "userMgt"
            + File.separator;
    private final String LOG_FILE_LOCATION = Utils.getResidentCarbonHome() + File.separatorChar + "repository" +
            File.separatorChar + "logs";
    private boolean isFilePresent = false;
    private UserManagementClient userMgtClient;

    @BeforeClass
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        PropertyConfigurator.configure(createLog4jConfiguration());
    }

    @Test(description = "Perform bulk user import and check the bulkuserimport log file is created.")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testBulkUserImportLogFileIsPresent() throws RemoteException, UserAdminUserAdminException {

        File bulkUserFile = new File(BULK_USER_FILE + BULK_USER_IMPORT_SUCCESS_FILE);

        DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
        userMgtClient.bulkImportUsers(USER_STORE_DOMAIN, BULK_USER_IMPORT_SUCCESS_FILE, handler, null);

        File logDir = new File(LOG_FILE_LOCATION);
        String[] logFiles = logDir.list();
        if (ArrayUtils.isNotEmpty(logFiles)) {
            for (String fileName : logFiles) {
                if (LOG_FILE_NAME.equals(fileName)) {
                    isFilePresent = true;
                }
            }
        }
        assertTrue(isFilePresent);
    }

    @Test(description = "Method to test the bulk user import error scenario. The test checks for the RemoteException " +
            "which is thrown by the bulkImportUser method",
            dependsOnMethods = {"testBulkUserImportLogFileIsPresent"})
    public void testBulkImportWithUserAdminException() throws UserAdminUserAdminException {

        String errorMessage = "";
        File bulkUserFile = new File(BULK_USER_FILE + BULK_USER_IMPORT_ERROR_FILE);

        DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
        try {
            userMgtClient.bulkImportUsers(USER_STORE_DOMAIN, BULK_USER_IMPORT_ERROR_FILE, handler, null);
        } catch (RemoteException e) {
            errorMessage = e.getMessage();
            assertEquals(errorMessage, ERROR_MESSAGE);
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            errorMessage = errorMessage.replaceAll(" ", "").replaceAll("\\.", "");
            String[] segments = errorMessage.split(",");

            String errorSegment = segments[1];
            String duplicateUserSegment = segments[2];

            assertEquals(Integer.valueOf(errorSegment.split(":")[1]).intValue(), FAILED_USER_COUNT);
            assertEquals(Integer.valueOf(duplicateUserSegment.split(":")[1]).intValue(), DUPLICATE_USER_COUNT);
        }
    }

    @Test(description = "Test the audit log content for the bulk user import operation for success users.",
            dependsOnMethods = {"testBulkImportWithUserAdminException"})
    public void testBulkUserImportAuditLogForSuccessUsers() throws IOException, JSONException {

        String[] auditLogSegments = null;
        JSONObject resultJson = null;
        String result = null;
        List<String> bulkUserImportAuditLogs = readAuditLogFile();

        /*
        * There are two log entries for bulk user import.
        *
        * 1. Success scenario
        * 2. Importing users with failures and duplicates.
        * */
        for (String line : bulkUserImportAuditLogs) {

            log.info("Audit log line: " + line);
            auditLogSegments = line.split(PIPE_REGEX);
            result = auditLogSegments[4];

            if (StringUtils.isNotEmpty(result)) {

                // Get the result as a json object
                resultJson = new JSONObject(result.trim().split(RESULT_REGEX)[1]);

                if (!resultJson.has(DUPLICATE_USERS) && !resultJson.has(FAILED_USERS)) {
                    // Assert the success count when all the users are imported successfully.
                    assertEquals(resultJson.getInt(SUCCESS_COUNT), SUCCESS_USER_COUNT_IN_MIXED_MODE);
                } else {
                    // Assert the success count
                    assertEquals(resultJson.getInt(SUCCESS_COUNT), SUCCESS_USER_COUNT);
                }
            }
        }
    }

    @Test(description = "Test the audit log content for the bulk user import operation for failed users.",
            dependsOnMethods = {"testBulkImportWithUserAdminException"})
    public void testBulkUserImportAuditLogForFailedUsers() throws IOException, JSONException {

        String[] auditLogSegments = null;
        JSONObject resultJson = null;
        String result = null;
        List<String> bulkUserImportAuditLogs = readAuditLogFile();

        for (String line : bulkUserImportAuditLogs) {

            log.info("Audit log line: " + line);
            auditLogSegments = line.split(PIPE_REGEX);
            result = auditLogSegments[4];

            if (StringUtils.isNotEmpty(result)) {

                // Get the result as a json object
                resultJson = new JSONObject(result.trim().split(RESULT_REGEX)[1]);

                if (resultJson.has(FAILED_USERS)) {

                    // Get the failed user information as json object
                    JSONObject failedUsers = resultJson.getJSONObject(FAILED_USERS);
                    // Get the failed users as Json array
                    JSONArray failedUsersList = failedUsers.getJSONArray(USERS);

                    // Check the failed user count matches the number of users in the list.
                    assertEquals(failedUsersList.length(), FAILED_USER_COUNT);

                    // Assert the failed user count, name and the cause.
                    assertEquals(failedUsers.getInt(COUNT), FAILED_USER_COUNT);

                    // Get the failed user json object
                    JSONObject failedUser = failedUsersList.getJSONObject(0);

                    // Assert the failed user name and the cause.
                    assertEquals(failedUser.getString(USER_NAME), FAILED_USERNAME);
                    assertEquals(failedUser.getString(CAUSE), FAILED_CAUSE);
                }
            }
        }
    }

    @Test(description = "Test the audit log content for the bulk user import operation for duplicate users.",
            dependsOnMethods = {"testBulkImportWithUserAdminException"})
    public void testBulkUserImportAuditLogForDuplicateUsers() throws IOException, JSONException {

        String[] auditLogSegments = null;
        JSONObject resultJson = null;
        String result = null;
        List<String> bulkUserImportAuditLogs = readAuditLogFile();

        for (String line : bulkUserImportAuditLogs) {

            log.info("Audit log line: " + line);
            auditLogSegments = line.split(PIPE_REGEX);
            result = auditLogSegments[4];

            if (StringUtils.isNotEmpty(result)) {

                // Get the result as a json object
                resultJson = new JSONObject(result.trim().split(RESULT_REGEX)[1]);

                if (resultJson.has(DUPLICATE_USERS)) {

                    // Get the duplicate users as a json object
                    JSONObject duplicateUsers = resultJson.getJSONObject(DUPLICATE_USERS);
                    // Get the duplicate user names
                    JSONArray duplicateUsersList = duplicateUsers.getJSONArray(USERS);

                    // Assert the duplicate count and the duplicate user names
                    assertEquals(duplicateUsers.getInt(COUNT), DUPLICATE_USER_COUNT);
                    // Check the duplicate users count is matches the number of users in the list
                    assertEquals(duplicateUsersList.length(), DUPLICATE_USER_COUNT);
                    assertEquals(duplicateUsersList.get(0), DUPLICATE_USERNAME);

                }
            }
        }
    }

    /**
     * Read the audit log file and extract the log entries as lines.
     *
     * @return : An Array List which contains audit log lines.
     * @throws IOException : If any error occurred while reading the file.
     */
    private List<String> readAuditLogFile() throws IOException {

        List<String> bulkUserImportAuditLogs = new ArrayList<>();
        String auditLogFile = LOG_FILE_LOCATION + File.separatorChar + AUDIT_LOG_FILE_NAME;
        File auditFile = new File(auditLogFile);

        // Iterate through the file and read lines.
        LineIterator iterator = FileUtils.lineIterator(auditFile, ENCODING);

        while (iterator.hasNext()) {
            String auditLine = iterator.nextLine();

            if (StringUtils.contains(auditLine, BULK_USER_IMPORT_OP)) {
                bulkUserImportAuditLogs.add(auditLine);
            }
        }
        return bulkUserImportAuditLogs;
    }

    /**
     * Generate the log4j properties related to the log appender.
     *
     * @return : Properties object with the necessary properties.
     */
    private Properties createLog4jConfiguration() {

        Properties log4jProperties = new Properties();
        log4jProperties.setProperty("log4j.logger.org.wso2.carbon.user.mgt.bulkimport", "DEBUG, " +
                "CARBON_BULK_USER_IMPORT");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT", "org.wso2.carbon.user.mgt.bulkimport" +
                ".BulkUserImportLogAppender");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.File", "${carbon" +
                ".home}/repository/logs/${instance.log}/bulkuserimport${instance.log}.log");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.Append", "false");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.layout", "org.wso2.carbon.utils.logging" +
                ".TenantAwarePatternLayout");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.layout.ConversionPattern", "[%T][%d] " +
                "%P%5p {%c} - %x %m%n");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.layout.TenantPattern", "%U%@%D [%T] [%S]");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.threshold", "DEBUG");
        log4jProperties.setProperty("log4j.appender.CARBON_BULK_USER_IMPORT.MaxFileSize", "10000kb");
        return log4jProperties;
    }
}
