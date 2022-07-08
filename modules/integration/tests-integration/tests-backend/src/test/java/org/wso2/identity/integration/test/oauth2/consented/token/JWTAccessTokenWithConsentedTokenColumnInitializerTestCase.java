/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.oauth2.consented.token;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.sql.SQLException;

/**
 * This class contains the initialization methods for the consented token column based tests.
 */
public class JWTAccessTokenWithConsentedTokenColumnInitializerTestCase extends
        OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeTest(alwaysRun = true)
    public void initConfiguration() throws Exception {

        super.init();
        changeISConfiguration();
    }

    @AfterTest(alwaysRun = true)
    public void restoreConfiguration() throws Exception {

        resetISConfiguration();
    }

    private void changeISConfiguration() throws Exception {

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        addConsentedTokenColumn();
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        deleteConsentedTokenColumn();
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    private void addConsentedTokenColumn() throws Exception {

        log.info("Trying to add the consented token column to the IDN_OAUTH2_ACCESS_TOKEN table.");
        int i = 0;
        while (true) {
            if (i == 10) {
                throw new Exception("Unable to connect to the H2 Database.");
            }
            i++;
            boolean connectedToH2DB = false;
            try {
                H2DataBaseManager dbManager =
                        new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository"
                                + "/database/WSO2IDENTITY_DB;" + "DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE",
                                "wso2carbon", "wso2carbon");
                log.info("Connected to the H2 Database.");
                connectedToH2DB = true;
                dbManager.executeUpdate("ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN ADD CONSENTED_TOKEN VARCHAR(6);");
                dbManager.disconnect();
                log.info("Added the consented token column to the IDN_OAUTH2_ACCESS_TOKEN table.");
                break;
            } catch (SQLException e) {
                if (connectedToH2DB) {
                    log.error("Error occurred while adding the consented token column to the " +
                            "IDN_OAUTH2_ACCESS_TOKEN table.", e);
                    break;
                }
                log.warn("Unable to connect to the H2 database. Reconnecting.");
                Thread.sleep(10000);
            }
        }
    }

    private void deleteConsentedTokenColumn() throws Exception {

        log.info("Trying to delete the consented token column from the IDN_OAUTH2_ACCESS_TOKEN table.");
        int i = 0;
        while (true) {
            if (i == 10) {
                throw new Exception("Unable to connect to the H2 Database.");
            }
            i++;
            boolean connectedToH2DB = false;
            try {
                H2DataBaseManager dbManager =
                        new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository"
                                + "/database/WSO2IDENTITY_DB;" + "DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE",
                                "wso2carbon", "wso2carbon");
                log.info("Connected to the H2 Database.");
                connectedToH2DB = true;
                dbManager.executeUpdate("ALTER TABLE IDN_OAUTH2_ACCESS_TOKEN DROP CONSENTED_TOKEN;");
                dbManager.disconnect();
                log.info("Deleted the consented token column from the IDN_OAUTH2_ACCESS_TOKEN table.");
                break;
            } catch (SQLException e) {
                if (connectedToH2DB) {
                    log.error("Error occurred while deleting the consented token column from " + "the "
                            + "IDN_OAUTH2_ACCESS_TOKEN table.", e);
                }
                log.warn("Unable to connect to the H2 database. Reconnecting.");
                Thread.sleep(10000);
            }
        }
    }
}
