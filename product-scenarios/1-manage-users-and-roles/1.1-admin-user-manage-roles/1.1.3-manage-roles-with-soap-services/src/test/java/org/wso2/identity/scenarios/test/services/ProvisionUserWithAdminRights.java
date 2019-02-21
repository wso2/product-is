/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.scenarios.test.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.clients.UserManagementClient;

import static org.testng.Assert.assertEquals;

public class ProvisionUserWithAdminRights extends ScenarioTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionUserWithAdminRights.class);

    private String userName;
    private String password;
    private String[] roles;
    private UserManagementClient userManagementClient;

    @Factory(dataProvider = "manageUserConfigProvider")
    public ProvisionUserWithAdminRights(String userName, String password, String[] roles) {

        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    @DataProvider(name = "manageUserConfigProvider")
    private static Object[][] manageUserConfigProvider() {

        return new Object[][] {
                {
                        "testUser", "password", new String[] { "admin" }
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        loginAndObtainSessionCookie();
        userManagementClient = new UserManagementClient(backendServiceURL, sessionCookie);
    }

    @Test
    public void addUserWithAdminRights() {

        boolean status = addUser(userName, password, roles, null);
        assertEquals(status, true, "User has not been created successfully with admin rights");
    }

    @Test(dependsOnMethods = "addUserWithAdminRights")
    public void deleteUser() {

        boolean status = deleteUser(userName);
        assertEquals(status, true, "User has not been deleted successfully");

    }

    private boolean addUser(String userName, String password, String[] roles, String profileName) {

        try {
            userManagementClient.addUser(userName, password, roles, profileName);
        } catch (Exception e) {
            LOG.error("Error while adding the user. ", e);
            return false;
        }
        return true;
    }

    private boolean deleteUser(String userName) {

        try {
            userManagementClient.deleteUser(userName);
        } catch (Exception e) {
            LOG.error("Error while deleting the user. ", e);
            return false;
        }
        return true;
    }
}
