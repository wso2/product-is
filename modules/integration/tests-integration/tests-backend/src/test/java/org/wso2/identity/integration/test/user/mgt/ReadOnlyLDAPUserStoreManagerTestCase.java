/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.test.user.mgt;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

public class ReadOnlyLDAPUserStoreManagerTestCase extends ISIntegrationTest {
    private ServerConfigurationManager scm;
    private File userMgtServerFile;

    private UserManagementClient userMgtClient;
    private AuthenticatorClient authenticatorClient;
    private String newUserName = "ReadOnlyLDAPUserName";
    private String newUserRole = "ReadOnlyLDAPUserRole";
    private String newUserPassword = "ReadOnlyLDAPUserPass";


    @BeforeClass(alwaysRun = true)
    public void configureServer() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);

        userMgtClient.addRole(newUserRole, null, new String[]{"/permission/admin/login"});
        userMgtClient.addUser(newUserName, newUserPassword, new String[]{newUserRole}, null);
        Assert.assertTrue(userMgtClient.roleNameExists(newUserRole), "Role name doesn't exists");
        Assert.assertTrue(userMgtClient.userNameExists(newUserRole, newUserName), "User name doesn't exists");

        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, isServer.getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();

        String carbonHome = ServerConfigurationManager.getCarbonHome();
        userMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator
                                     + "conf" + File.separator + "user-mgt.xml");
        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                                          + File.separator + "readOnlyLdapUserMgtConfig.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfiguration(userMgtConfigFile, userMgtServerFile, true, true);
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
    }

    @Test(groups = "wso2.is", description = "Test user login already exist in the ldap")
    public void userLoginTest() throws Exception {
        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, isServer.getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();
    }
    @Test(groups = "wso2.is", description = "Test user login already exist in the ldap")
    public void getUsersOfRole() throws Exception {
        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(newUserRole, newUserName, 10), newUserName), "List does not contains the user");
    }
    @Test(groups = "wso2.is", description = "Test user login already exist in the ldap")
    public void getRolesOfUser() throws Exception {
        Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser(newUserName, newUserRole, 10), newUserRole), "List does not contains the role");
    }

    @Test(groups = "wso2.is", description = "Test user login already exist in the ldap")
    public void getAllRolesNames() throws Exception {
        Assert.assertTrue(userMgtClient.getAllRolesNames("*", 10).length >=1, "No role listed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check role addition")
    public void testAddRole() throws RemoteException, UserAdminUserAdminException {
        Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames(newUserRole + "1", 100), newUserRole + "1"), "User Role already exist");
        try {
            userMgtClient.addRole(newUserRole + "1", null, new String[]{"login"}, false);
        } catch (AxisFault axisFault) {
            Assert.assertEquals(axisFault.getMessage(), "Read only user store or Role creation is disabled"
                    , "Error message mismatched");
        }

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is")
    public void addNewUser() throws RemoteException {
        try {
            userMgtClient.addUser(newUserName + "1", newUserPassword, new String[]{newUserRole}, null);
        } catch (UserAdminUserAdminException e) {
            Assert.assertEquals(e.getMessage(), "UserAdminUserAdminException", "Error Message mismatched");
        }

    }

    @Test(groups = "wso2.is", description = "Check update role name")
    public void testUpdateRoleName() {
        try {
            userMgtClient.updateRoleName(newUserRole, newUserRole + "updated");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Read-only UserStoreManager. Roles cannot be added or modified.")
                    , "Error message mismatched, expected 'Read-only UserStoreManager. Roles cannot be added or " +
                    "modified.', but was '" + e.getMessage() + " '");
        }

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check update users of role")
    public void testUpdateUsersOfRole() {

        String[] userList = new String[]{newUserName};
        FlaggedName[] userFlagList = new FlaggedName[userList.length];

        for (int i = 0; i < userFlagList.length; i++) {
            FlaggedName flaggedName = new FlaggedName();
            flaggedName.setItemName(userList[i]);
            flaggedName.setSelected(true);
            userFlagList[i] = flaggedName;
        }
        try {
            userMgtClient.updateUsersOfRole("admin", userFlagList);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "UserAdminUserAdminException",
                                "Error Message mismatched");
        }

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check add remove roles of user")
    public void testAddRemoveRolesOfUser() {

        String[] newRoles = new String[]{"admin"};
        String[] deletedRoles = new String[]{newUserRole};
        try {
            userMgtClient.addRemoveRolesOfUser(newUserName, newRoles, deletedRoles);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Error occurred while updating hybrid role list of user")
                    , "Error Message mismatched, expected 'Error occurred while updating hybrid role list of user', " +
                    "but was '" + e.getMessage() + " ,");
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check add remove users of role ")
    public void testAddRemoveUsersOfRole() {

        String[] newUsers = new String[]{"admin"};
        String[] deletedUsers = new String[]{newUserName};
        try {
            userMgtClient.addRemoveUsersOfRole(newUserRole, newUsers, deletedUsers);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Read-only user store.Roles cannot be added or modified"),
                    "Error message mismatched, expected 'Read-only user store.Roles cannot be added or modified', " +
                            "but was '" + e.getMessage() + " '");
        }

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role")
    public void testListAllUsers() throws Exception {
        FlaggedName[] userList = userMgtClient.listAllUsers("*", 100);
        Assert.assertTrue(userList.length > 0, "List all users return empty list");
        Assert.assertTrue(nameExists(userList, newUserName), "User Not Exist in the user list");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role")
    public void testListUsers() throws Exception {
        String[] usersList = userMgtClient.listUsers("*", 100);
        Assert.assertNotNull(usersList, "UserList null");
        Assert.assertTrue(usersList.length > 0, "List users return empty list");
    }

    @Test(groups = "wso2.is", description = "Check delete role")
    public void testListByClaimUsers() throws Exception {

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI("http://wso2.org/claims/lastname");
        claimValue.setValue("*");
        FlaggedName[] flaggedNames = userMgtClient.listUserByClaim(claimValue, "*", 1000);

        for (FlaggedName name : flaggedNames){
            if(name.getItemName().equals("krbtgt") || name.getItemName().equals("ldap")){
                Assert.fail("invalid user retrieved with claim : " + name.getItemName());

            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void restoreServer() throws Exception {

        scm.restoreToLastConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        if (nameExists(userMgtClient.listAllUsers(newUserName, 10), newUserName)) {
            userMgtClient.deleteUser(newUserName);
        }
        if (userMgtClient.roleNameExists(newUserRole)) {
            userMgtClient.deleteRole(newUserRole);
        }
    }

    private boolean nameExists(FlaggedName[] allNames, String inputName) {
        boolean exists = false;

        for (FlaggedName flaggedName : allNames) {
            String name = flaggedName.getItemName();

            if (name.equals(inputName)) {
                exists = true;
                break;
            } else {
                exists = false;
            }
        }

        return exists;
    }


}
