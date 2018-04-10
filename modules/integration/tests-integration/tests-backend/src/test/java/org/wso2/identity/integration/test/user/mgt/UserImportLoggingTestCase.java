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

import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
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
import java.rmi.RemoteException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

/**
 * Test cases for the bulkUserImport log appender.
 * This will check the logs are directed to the new file.
 */
public class UserImportLoggingTestCase extends ISIntegrationTest {

    private UserManagementClient userMgtClient;
    private boolean isFilePresent = false;
    private final String LOG_FILE_NAME = "bulkuserimport.log";

    @BeforeClass
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        PropertyConfigurator.configure(createLog4jConfiguration());
    }

    @Test (description = "Perform bulk user import and check the bulkuserimport log file is created.")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testBulkUserImportLog() throws RemoteException, UserAdminUserAdminException {

        String userStoreDomain = "PRIMARY";
        File bulkUserFile = new File(getISResourceLocation() + File.separator + "userMgt"
                + File.separator + "bulkUserImport.csv");

        DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
        userMgtClient.bulkImportUsers(userStoreDomain, "bulkUserImport.csv", handler, "PassWord1@");
        String logsHome = Utils.getResidentCarbonHome() + File.separatorChar + "repository" + File.separatorChar +
                "logs";
        File logDir = new File(logsHome);
        String[] logFiles = logDir.list();

        if (logFiles != null) {
            for (String fileName : logFiles) {
                if (LOG_FILE_NAME.equals(fileName)) {
                    isFilePresent = true;
                }
            }
        }
        Assert.assertTrue(isFilePresent);
    }

    /**
     * Generate the log4j properties related to the log appender.
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
