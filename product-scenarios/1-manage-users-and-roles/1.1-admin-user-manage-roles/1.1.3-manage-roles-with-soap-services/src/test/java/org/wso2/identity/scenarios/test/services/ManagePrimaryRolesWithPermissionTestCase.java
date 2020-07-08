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

public class ManagePrimaryRolesWithPermissionTestCase extends ScenarioTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ManagePrimaryRolesWithPermissionTestCase.class);

    private static final String PERMISSION_ADMIN_LOGIN = "/permission/admin/login";
    private static final String PERMISSION_ADMIN_MANAGE = "/permission/admin/manage";

    private String roleName;
    private String[] userList;
    private UserManagementClient userManagementClient;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManagePrimaryRolesWithPermissionTestCase(String roleName, String[] userList) {

        this.roleName = roleName;
        this.userList = userList;
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        "Manager", new String[] { "user1", "user2" }
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
    public void addPrimaryRole() {

        boolean status = addRole(roleName, new String[0], new String[0]);
        assertEquals(status, true, "Role has not been created successfully");
    }

    @Test(dependsOnMethods = "addPrimaryRole")
    public void deletePrimaryRole() {

        boolean status = deleteRole(roleName);
        assertEquals(status, true, "Role has not been deleted");
    }

    @Test(dependsOnMethods = "deletePrimaryRole")
    public void addPrimaryRoleWithPermissions() {

        boolean status = addRole(roleName, new String[0],
                new String[] { PERMISSION_ADMIN_LOGIN, PERMISSION_ADMIN_MANAGE });
        assertEquals(status, true, "Role has not been created successfully with permission");
    }

    @Test(dependsOnMethods = "addPrimaryRoleWithPermissions")
    public void deletePrimaryRoleWithPermissions() {

        boolean status = deleteRole(roleName);
        assertEquals(status, true, "Role has not been deleted");

    }

    @Test(dependsOnMethods = "deletePrimaryRoleWithPermissions")
    public void addUsers() {

        for (int i = 0; i < userList.length; i++) {
            boolean status = addUser(userList[i], "password", new String[0], null);
            assertEquals(status, true, "User has not been created successfully");
        }
    }

    @Test(dependsOnMethods = "addUsers")
    public void addRoleWithUsersAndPermissions() {

        boolean status = addRole(roleName, userList, new String[] { PERMISSION_ADMIN_MANAGE, PERMISSION_ADMIN_LOGIN });
        assertEquals(status, true, "Group has not been created successfully with few permission and users");
    }

    @Test(dependsOnMethods = "addRoleWithUsersAndPermissions")
    public void deleteUsers() {

        for (int i = 0; i < userList.length; i++) {
            boolean status = deleteUser(userList[i]);
            assertEquals(status, true, "User has not been deleted successfully");
        }
    }

    @Test(dependsOnMethods = "deleteUsers")
    public void deleteRole() {

        boolean status = deleteRole(roleName);
        assertEquals(status, true, "Group has not been deleted");
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

    private boolean addRole(String roleName, String[] userList, String[] permissions) {

        try {
            userManagementClient.addRole(roleName, userList, permissions);
        } catch (Exception e) {
            LOG.error("Error while adding the role. ", e);
            return false;
        }
        return true;
    }

    private boolean deleteRole(String roleName) {

        try {
            userManagementClient.deleteRole(roleName);
        } catch (Exception e) {
            LOG.error("Error while deleting the role. ", e);
            return false;
        }
        return true;
    }
}
