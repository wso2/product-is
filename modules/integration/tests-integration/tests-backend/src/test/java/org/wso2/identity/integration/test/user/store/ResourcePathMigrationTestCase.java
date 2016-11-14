/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.user.store;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.UserStoreExceptionException;
import org.wso2.identity.integration.common.clients.authorization.mgt.RemoteAuthorizationManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.Arrays;

public class ResourcePathMigrationTestCase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    private String originalH2DbPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator
            + "database" + File.separator + "WSO2CARBON_DB.h2.db";
    private String copyH2DbPath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS"
            + File.separator + "userMgt" + File.separator + "WSO2CARBON_DB.h2.db";
    private String backupH2DbPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator
            + "database" + File.separator + "WSO2CARBON_DB.h2.db.bak";

    private File originalH2Db = new File(originalH2DbPath);
    private File copyH2Db = new File(copyH2DbPath);
    private File backupH2Db = new File(backupH2DbPath);

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);

        try {
            // Backup the existing database.
            Files.copy(originalH2Db.toPath(), backupH2Db.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(copyH2Db.toPath(), originalH2Db.toPath(), StandardCopyOption.REPLACE_EXISTING);
            serverConfigurationManager.restartGracefully();
        } catch (IOException ex) {
            log.error("Error while copying database", ex);
        } catch (AutomationUtilException ex) {
            log.error("Error while restarting server", ex);
        }

        super.init();
    }

    @Test(groups = "wso2.is", description = "Test for the resource path validity after a migration")
    public void testResourcePathMigration() throws Exception {

        String resourceId = "/_system/governance/repository/Madu";
        String action = "http://www.wso2.org/projects/registry/actions/get";
        String [] allowedRoles = {"INTERNAL/everyone","role2", "admin"};

        RemoteAuthorizationManagerServiceClient serviceClient =
                new RemoteAuthorizationManagerServiceClient(backendURL, sessionCookie);

        try {
            String[] roles = serviceClient.getAllowedRolesForResource(resourceId, action);
            Arrays.sort(allowedRoles);
            Arrays.sort(roles);
            Assert.assertTrue(Arrays.equals(allowedRoles, roles));
        } catch (RemoteException | UserStoreExceptionException ex) {
            log.error("Error occurred while retrieving roles", ex);
            Assert.fail();
        }
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

        try {
            // Restore the backed up database.
            Files.copy(backupH2Db.toPath(), originalH2Db.toPath(), StandardCopyOption.REPLACE_EXISTING);
            serverConfigurationManager.restartGracefully();
        } catch (IOException ex) {
            log.error("Failed to restore the database", ex);
        } catch (AutomationUtilException ex) {
            log.error("Failed to restart the server", ex);
        }
    }
}
