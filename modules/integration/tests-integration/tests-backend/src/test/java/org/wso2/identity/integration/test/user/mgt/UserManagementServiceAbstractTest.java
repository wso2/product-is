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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;

public abstract class UserManagementServiceAbstractTest extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(UserManagementServiceAbstractTest.class);
    protected static final String EVERYONE_ROLE = "Internal/everyone";
    protected UserManagementClient userMgtClient;
    protected AuthenticatorClient authenticatorClient;
    protected String newUserName;
    protected String newUserRole;
    protected String newUserPassword;
    private String userRoleTmp;

    public void doInit() throws Exception {
        //creating context as super admin
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);
        setUserName();
        setUserPassword();
        setUserRole();
        Assert.assertNotNull(newUserName, "Please set a value to userName");
        Assert.assertNotNull(newUserRole, "Please set a value to userRole");

    }

    public void clean() throws Exception {
        if (nameExists(userMgtClient.listAllUsers(newUserName, 10), newUserName)) {
            userMgtClient.deleteUser(newUserName);
        }
        if (userMgtClient.roleNameExists(newUserRole)) {
            userMgtClient.deleteRole(newUserRole);
        }
        if (userMgtClient.roleNameExists(newUserRole + "tmpupdated")) {
            userMgtClient.deleteRole(newUserRole + "tmpupdated");
        }
        if (userMgtClient.roleNameExists(newUserRole + "tmp")) {
            userMgtClient.deleteRole(newUserRole + "tmp");
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Get all the role names")
    public void testGetAllRoleNames() throws Exception {
        Assert.assertTrue(userMgtClient.getAllRolesNames("*", 100).length > 1
                , "Getting all user role names return empty list");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check role addition", dependsOnMethods = "testGetAllRoleNames")
    public void testAddRole() throws Exception {
        Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames(newUserRole, 100), newUserRole), "User Role already exist");
        userMgtClient.addRole(newUserRole, null, new String[]{"login"}, false);
        Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames(newUserRole, 100), newUserRole), "Added user role name not found");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "testAddRole")
    public void testDeleteRole() throws Exception {
        userMgtClient.deleteRole(newUserRole);
        Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames(newUserRole, 100), newUserRole), "User Role still exist");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", dependsOnMethods = "testDeleteRole")
    public void addNewUser() throws Exception {
        userMgtClient.addRole(newUserRole, null, new String[]{"/permission/admin/login"});
        userMgtClient.addUser(newUserName, newUserPassword, new String[]{newUserRole}, null);
        Assert.assertTrue(userMgtClient.roleNameExists(newUserRole), "Role name doesn't exists");
        Assert.assertTrue(userMgtClient.userNameExists(newUserRole, newUserName), "User name doesn't exists");

        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, isServer.getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "addNewUser")
    public void testListAllUsers() throws Exception {
        Assert.assertTrue(userMgtClient.listAllUsers("*", 100).length > 0, "List all users return empty list");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "testListAllUsers")
    public void testListUsers() throws Exception {
        String[] usersList = userMgtClient.listUsers("*", 100);
        Assert.assertNotNull(usersList, "UserList null");
        Assert.assertTrue(usersList.length > 0, "List users return empty list");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check internal role operations", dependsOnMethods = "testListUsers")
    public void testAddDeleteInternalRoleOperations() throws Exception {

        FlaggedName[] allRoles = null;
        String[] permissionList = new String[]{"/permission"};
        String internalRoleName1 = "manager";
        String internalRoleName2 = "sales";
//    	Test add internal role without user
        userMgtClient.addInternalRole(internalRoleName1, null, permissionList);
        allRoles = userMgtClient.getAllRolesNames(internalRoleName1, 0);

        Assert.assertTrue(nameExists(allRoles, "Internal/manager"), "The internal role add has failed");

        userMgtClient.deleteRole("Internal/" + internalRoleName1);
        allRoles = userMgtClient.getAllRolesNames(internalRoleName1, 0);
        Assert.assertFalse(nameExists(allRoles, "Internal/manager"), "The internal role without user deletion has failed. Role name still exist");

        String[] userList = new String[]{newUserName};
//    	Test add internal role with user
        userMgtClient.addInternalRole(internalRoleName2, userList, permissionList);
        allRoles = userMgtClient.getAllRolesNames(internalRoleName2, 0);

        Assert.assertTrue(nameExists(allRoles, "Internal/" + internalRoleName2), "The internal role addition has failed");

        userMgtClient.deleteRole("Internal/sales");
        allRoles = userMgtClient.getAllRolesNames(internalRoleName2, 0);
        Assert.assertFalse(nameExists(allRoles, "Internal/sales"), "The internal role with user delete has failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check add remove users of role ", dependsOnMethods = "testAddDeleteInternalRoleOperations")
    public void testAddRemoveUsersOfRole() throws Exception {
        String newUserTmp = newUserName + "tmp123";
        userRoleTmp = newUserRole + "tmp";

        String[] newUsers = new String[]{newUserName};
        String[] deletedUsers = new String[]{newUserTmp};

        userMgtClient.addRole(userRoleTmp, null, new String[]{"login"}, false);
        userMgtClient.addUser(newUserTmp, newUserPassword, new String[]{userRoleTmp}, null);
        Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser(newUserTmp, userRoleTmp, 0), userRoleTmp), "Adding user has failed");

        userMgtClient.addRemoveUsersOfRole(userRoleTmp, newUsers, deletedUsers);

        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(userRoleTmp, newUserName, 0), newUserName), "user does not assigned to user role");
        Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole(userRoleTmp, newUserTmp, 0), newUserTmp), "User does not delete from user role");

//    	Clean up the modified users of role and test it.
        userMgtClient.addRemoveUsersOfRole(userRoleTmp, null, newUsers);
        Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole(userRoleTmp, newUserName, 0), newUserName), "User still exists in the assigned role");
        userMgtClient.deleteUser(newUserTmp);
        Assert.assertFalse(nameExists(userMgtClient.listAllUsers(newUserTmp, 10), newUserTmp), "User Deletion failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check add remove roles of user", dependsOnMethods = "testAddRemoveUsersOfRole")
    public void testAddRemoveRolesOfUser() throws Exception {

        String[] newRoles = new String[]{userRoleTmp};
        String[] deletedRoles = new String[]{newUserRole};

        userMgtClient.addRemoveRolesOfUser(newUserName, newRoles, deletedRoles);

        Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser(newUserName, newRoles[0], 0), newRoles[0]), "Adding role to user has failed");
        Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser(newUserName, deletedRoles[0], 0), deletedRoles[0]), "Role still exists in the user roles");

//    	Clean up the modified roles of user and test it.
        userMgtClient.addRemoveRolesOfUser(newUserName, null, newRoles);
        Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser(newUserName, newUserRole, 0), newUserRole), "Role still exists in the user roles");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check update users of role", dependsOnMethods = "testAddRemoveRolesOfUser")
    public void testUpdateUsersOfRole() throws Exception {

        String[] userList = new String[]{newUserName};
        FlaggedName[] userFlagList = new FlaggedName[userList.length];

        for (int i = 0; i < userFlagList.length; i++) {
            FlaggedName flaggedName = new FlaggedName();
            flaggedName.setItemName(userList[i]);
            flaggedName.setSelected(true);
            userFlagList[i] = flaggedName;
        }

        userMgtClient.updateUsersOfRole(newUserRole, userFlagList);
        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(newUserRole, newUserName, 0), newUserName), "Adding user to role has failed");

//    	Calling with same user list should delete the users.
        for (int i = 0; i < userFlagList.length; i++) {
            userFlagList[i].setSelected(false);
        }

        userMgtClient.updateUsersOfRole(userRoleTmp, userFlagList);
        Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole(userRoleTmp, newUserName, 0), newUserName), "Deleting user from role has failed");
//        Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("umRole1", "user2", 0), "user2"), "Deleting user2 from role has failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check update role name", dependsOnMethods = "testUpdateUsersOfRole")
    public void testUpdateRoleName() throws Exception {

        userMgtClient.updateRoleName(userRoleTmp, userRoleTmp + "updated");

        Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames(userRoleTmp, 10), userRoleTmp), "Role update failed.");
        Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames(userRoleTmp + "updated", 10), userRoleTmp + "updated"), "Updating role has failed. Role not updated");
        userRoleTmp = userRoleTmp + "updated";

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check update roles of user", dependsOnMethods = "testUpdateRoleName")
    public void testUpdateRolesOfUser() throws Exception {

        String[] roleList = new String[]{userRoleTmp, newUserRole, EVERYONE_ROLE};
        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(newUserRole, newUserName, 0), newUserName)
                , "User Does not belongs to " + newUserRole);

        userMgtClient.updateRolesOfUser(newUserName, roleList);

        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(userRoleTmp, newUserName, 0), newUserName)
                , "UserRole updating failed. User Does not belongs to " + userRoleTmp);
        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(newUserRole, newUserName, 0), newUserName), "User Role updating failed. Previous role deleted");


    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check change user password", dependsOnMethods = "testUpdateRolesOfUser")
    public void testChangePasswordOfUser() throws Exception {
        Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole(newUserRole, newUserName, 0), newUserName)
                , "user Does not belongs to user role " + newUserRole);
        Assert.assertTrue(authenticatorClient.login(newUserName, newUserPassword, isServer
                .getInstance().getHosts().get("default")).contains("JSESSIONID"), "Session Cookie not found");
        authenticatorClient.logOut();

        userMgtClient.changePassword(newUserName, "passwordS1@");
        newUserPassword = "passwordS1@";

        String value = authenticatorClient.login(newUserName, newUserPassword, isServer.getInstance().getHosts().get("default"));

        Assert.assertTrue((value.indexOf("JSESSIONID") != -1), "User password change failed. login not return session cookie");
        authenticatorClient.logOut();

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check get shared of current user", dependsOnMethods = "testChangePasswordOfUser")
    public void testGetRolesOfCurrentUser() throws Exception {

        String session = authenticatorClient.login(newUserName, newUserPassword, isServer.getInstance().getHosts().get("default"));
        UserManagementClient client = new UserManagementClient(backendURL, session);
        Assert.assertTrue(nameExists(client.getRolesOfCurrentUser(), newUserRole), "Getting current user roles has failed.");

        authenticatorClient.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check list users by claim value", dependsOnMethods = "testGetRolesOfCurrentUser")
    public void testListUserByClaim() throws Exception {
        UserProfileMgtServiceClient userProfileMgtServiceClient
                = new UserProfileMgtServiceClient(backendURL, getSessionCookie());
        UserProfileDTO profile
                = userProfileMgtServiceClient.getUserProfile(newUserName, "default");
        UserFieldDTO[] fields = userProfileMgtServiceClient.getProfileFieldsForInternalStore().getFieldValues();
        String profileConfigs = profile.getProfileName();
        for (UserFieldDTO field : fields) {
            if (field.getDisplayName().equalsIgnoreCase("Last Name")) {
                field.setFieldValue(newUserName + "LastName");
                continue;
            }

            if (field.getRequired()) {
                if (field.getDisplayName().equalsIgnoreCase("Email")) {
                    field.setFieldValue(newUserName + "@wso2.com");
                } else {
                    field.setFieldValue(newUserName);
                }
                continue;
            }
            if (field.getFieldValue() == null) {
                field.setFieldValue("");
            }

        }
        //creating a new profile with updated values
        UserProfileDTO newProfile = new UserProfileDTO();
        newProfile.setProfileName(profile.getProfileName());
        newProfile.setFieldValues(fields);
        newProfile.setProfileConifuration(profileConfigs);
        userProfileMgtServiceClient.setUserProfile(newUserName, newProfile);


        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI("http://wso2.org/claims/lastname");
        claimValue.setValue(newUserName + "LastName");


        FlaggedName[] allNames = userMgtClient.listUserByClaim(claimValue, newUserName, 10);

        Assert.assertTrue(nameExists(allNames, newUserName), "List user with claim value has failed");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check set role UI permissions to resource", dependsOnMethods = "testListUserByClaim")
    public void testSetRoleUIPermission() throws Exception {

        String resourceName = "/permission/testlogin";

        userMgtClient.setRoleUIPermission(newUserRole, new String[]{resourceName});
        UIPermissionNode uiPermissions = userMgtClient.getAllUIPermissions();

        Assert.assertNotNull(uiPermissions.getResourcePath(), "Setting ui permissions to resource has failed.");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check getting user realm info", dependsOnMethods = "testSetRoleUIPermission")
    public void testGetUserRealmInfo() throws Exception {

        UserRealmInfo realmInfo = userMgtClient.getUserRealmInfo();

        Assert.assertNotNull(realmInfo);

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check shared role enable", dependsOnMethods = "testGetUserRealmInfo")
    public void testIsSharedRolesEnabled() throws Exception {

        Assert.assertFalse(userMgtClient.isSharedRolesEnabled());

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check getting permissions for role", dependsOnMethods = "testIsSharedRolesEnabled")
    public void testGetRolePermissions() throws Exception {

        UIPermissionNode permission = userMgtClient.getRolePermissions(newUserRole);
        Assert.assertEquals(permission.getDisplayName(), "All Permissions");
    }

    //todo need to fix properly
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
//    @Test(groups = "wso2.is", description = "Check importing bulk users", dependsOnMethods = "testGetRolePermissions")
    public void testBulkImportUsers() throws Exception {

        //ToDo:get userStoreDomain properly
        String userStoreDomain = "PRIMARY";
        File bulkUserFile = new File(getISResourceLocation() + File.separator + "userMgt"
                                     + File.separator + "bulkUserImport.csv");

        DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
        userMgtClient.bulkImportUsers(userStoreDomain, "bulkUserImport.csv", handler, "PassWord1@");

        String[] userList = userMgtClient.listUsers("*", 100);

        Assert.assertNotNull(userList);
        Assert.assertEquals(userMgtClient.listUsers("bulkUser1", 10), new String[]{"bulkUser1"});
        Assert.assertEquals(userMgtClient.listUsers("bulkUser2", 10), new String[]{"bulkUser2"});
        Assert.assertEquals(userMgtClient.listUsers("bulkUser3", 10), new String[]{"bulkUser3"});

        userMgtClient.deleteUser("bulkUser1");
        userMgtClient.deleteUser("bulkUser2");
        userMgtClient.deleteUser("bulkUser3");
    }

    /**
     * Checks whether the passed Name exists in the FlaggedName array.
     *
     * @param allNames
     * @param inputName
     * @return
     */
    protected boolean nameExists(FlaggedName[] allNames, String inputName) {
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

    protected abstract void setUserName();

    protected abstract void setUserPassword();

    protected abstract void setUserRole();

}
