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

public class ManageInternalRolesWithPermissionTestCase extends ScenarioTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ManageInternalRolesWithPermissionTestCase.class);

    private static final String PERMISSION_ADMIN_LOGIN = "/permission/admin/login";
    private static final String PERMISSION_ADMIN_MANAGE = "/permission/admin/manage";

    private String internalRoleName;
    private String[] userList;
    private UserManagementClient userManagementClient;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManageInternalRolesWithPermissionTestCase(String internalRoleName, String[] userList) {

        this.internalRoleName = internalRoleName;
        this.userList = userList;
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        "engineer", new String[] { "user3", "user4" }
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
    public void addInternalRole() {

        boolean status = addInternalRole(internalRoleName, new String[0], new String[0]);
        assertEquals(status, true, "Internal role has not been created successfully");
    }

    @Test(dependsOnMethods = "addInternalRole")
    public void deleteInternalRole() {

        boolean status = deleteInternalRole("Internal/" + internalRoleName);
        assertEquals(status, true, "Internal role has not been deleted");
    }

    @Test(dependsOnMethods = "deleteInternalRole")
    public void addInternalRoleWithPermissions() {

        boolean status = addInternalRole(internalRoleName, new String[0], new String[] {
                PERMISSION_ADMIN_LOGIN, PERMISSION_ADMIN_MANAGE
        });
        assertEquals(status, true, "Internal role has not been created successfully with permission");
    }

    @Test(dependsOnMethods = "addInternalRoleWithPermissions")
    public void deleteInternalRoleWithPermissions() {

        boolean status = deleteInternalRole("Internal/" + internalRoleName);
        assertEquals(status, true, "Internal role has not been deleted");

    }

    @Test(dependsOnMethods = "deleteInternalRoleWithPermissions")
    public void addUsers() {

        for (int i = 0; i < userList.length; i++) {
            boolean status = addUser(userList[i], "password", new String[0], null);
            assertEquals(status, true, "User has not been created successfully");
        }
    }

    @Test(dependsOnMethods = "addUsers")
    public void addInternalRoleWithUsersAndPermissions() {

        boolean status = addInternalRole(internalRoleName, userList,
                new String[] { PERMISSION_ADMIN_MANAGE, PERMISSION_ADMIN_LOGIN });
        assertEquals(status, true, "internal role has not been created successfully with few permission and users");
    }

    @Test(dependsOnMethods = "addInternalRoleWithUsersAndPermissions")
    public void deleteUsers() {

        for (int i = 0; i < userList.length; i++) {
            boolean status = deleteUser(userList[i]);
            assertEquals(status, true, "User has not been deleted successfully");
        }
    }

    @Test(dependsOnMethods = "deleteUsers")
    public void deleteRole() {

        boolean status = deleteInternalRole("Internal/" + internalRoleName);
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

    private boolean addInternalRole(String roleName, String[] userList, String[] permissions) {

        try {
            userManagementClient.addInternalRole(roleName, userList, permissions);
        } catch (Exception e) {
            LOG.error("Error while adding an internal role. ", e);
            return false;
        }
        return true;
    }

    private boolean deleteInternalRole(String roleName) {

        try {
            userManagementClient.deleteRole(roleName);
        } catch (Exception e) {
            LOG.error("Error while deleting an internal role. ", e);
            return false;
        }
        return true;
    }
}
