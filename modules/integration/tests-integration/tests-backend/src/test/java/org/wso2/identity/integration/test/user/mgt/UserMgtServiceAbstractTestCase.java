/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.mgt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;

public abstract class UserMgtServiceAbstractTestCase extends ISIntegrationTest{

	private static final Log log = LogFactory.getLog(UserMgtServiceAbstractTestCase.class);
	protected UserManagementClient userMgtClient;
	protected AuthenticatorClient loginManger;
    private String adminUsername;
    private String adminPassword;
   
    protected void testInit() throws Exception {
        super.init();
        
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        loginManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
    }
	
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
	@Test(groups = "wso2.is", description = "Get all the role names")
    public void testGetAllRoleNames() throws Exception {	
		Assert.assertTrue(nameExists(userMgtClient.listRoles("umRole1", 100), "umRole1"), "Getting all user role names has " +
				"failed.");
    }
	
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check role addition", dependsOnMethods="testGetAllRoleNames")
    public void testAddRole() throws Exception{
    	userMgtClient.addRole("umRole2", null, new String[]{"login"}, false);
    	Assert.assertTrue(nameExists(userMgtClient.listRoles("umRole2", 100), "umRole2"), "Adding a user role has failed");
    }
	
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "testAddRole")
	public void testDeleteRole() throws Exception {
    	userMgtClient.deleteRole("umRole2");
    	Assert.assertFalse(nameExists(userMgtClient.listRoles("umRole2", 100), "umRole2"), "Deleting the added user role " +
				"has failed");

	}
    
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "testDeleteRole")
	public void testListAllUsers() throws Exception {
    	Assert.assertTrue(nameExists(userMgtClient.listAllUsers("user1", 100), "user1"), "List all users has failed");
	}
    
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods = "testListAllUsers")
	public void testListUsers() throws Exception {
    	boolean exists = false;
    	FlaggedName[] usersList = userMgtClient.listAllUsers("user1", 100);
    	for (FlaggedName user : usersList) {
			if("user1".equals(user.getItemName())){
				exists = true;
				break;
			}
		}
    	Assert.assertTrue(exists, "List users has failed");
	}
    
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check internal role operations", dependsOnMethods = "testListUsers")
    public void testAddDeleteInternalRoleOperations() throws Exception {

    	FlaggedName[] allRoles = null;
    	String[] permissionList = new String[]{"/permission"};

//    	Test add internal role without user
    	userMgtClient.addInternalRole("manager", null, permissionList);
    	allRoles = userMgtClient.listRoles("manager", 0);

    	Assert.assertTrue(nameExists(allRoles, "Internal/manager"), "The internal role add has failed");

    	userMgtClient.deleteRole("Internal/manager");
    	allRoles = userMgtClient.listRoles("manager", 0);
    	Assert.assertFalse(nameExists(allRoles, "Internal/manager"), "The internal role without user delete has failed");

    	String[] userList = new String[]{"user1"};
//    	Test add internal role with user
    	userMgtClient.addInternalRole("sales", userList, permissionList);
    	allRoles = userMgtClient.listRoles("sales", 0);

    	Assert.assertTrue(nameExists(allRoles, "Internal/sales"), "The internal role add has failed");

    	userMgtClient.deleteRole("Internal/sales");
    	allRoles = userMgtClient.listRoles("manager", 0);
    	Assert.assertFalse(nameExists(allRoles, "Internal/sales"), "The internal role with user delete has failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check add remove users of role ", dependsOnMethods = "testAddDeleteInternalRoleOperations")
    public void testAddRemoveUsersOfRole() throws Exception {

    	String[] newUsers = new String[]{"user1"};
    	String[] deletedUsers = new String[]{"user2"};

    	userMgtClient.updateUserListOfRole("admin", newUsers, deletedUsers);

    	Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"), "Getting user added to admin role failed");
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user2", 0), "user2"), "User user2 still exists in the admin role");

//    	Clean up the modified users of role and test it.
    	userMgtClient.updateUserListOfRole("admin", null, newUsers);
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"), "User user1 still exists in the admin role");
    }
    
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check add remove roles of user", dependsOnMethods = "testAddRemoveUsersOfRole")
    public void testAddRemoveRolesOfUser() throws Exception {

    	String[] newRoles = new String[]{"umRole1"};
    	String[] deletedRoles = new String[]{"umRole3"};

    	userMgtClient.addRemoveRolesOfUser("user1", newRoles, deletedRoles);

    	Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0), "umRole1"), "Adding role to user1 has failed");
    	Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole3", 0), "umRole3"), "Role still exists in the user1 roles");

//    	Clean up the modified roles of user and test it.
    	userMgtClient.addRemoveRolesOfUser("user1", null, newRoles);
    	Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0), "umRole1"), "Role still exists in the user1 roles");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check update users of role", dependsOnMethods = "testAddRemoveRolesOfUser")
    public void testUpdateUsersOfRole() throws Exception {

    	String[] userList = new String[]{"user1", "user2"};
    	FlaggedName[] userFlagList = new FlaggedName[userList.length];

    	for(int i=0; i < userFlagList.length; i++) {
    		FlaggedName flaggedName = new FlaggedName();
    		flaggedName.setItemName(userList[i]);
    		flaggedName.setSelected(true);
    		userFlagList[i] = flaggedName;
    	}

		userMgtClient.updateUserListOfRole("umRole1",userList,null);
    	Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("umRole1", "user1", 0), "user1"), "Adding user1 to role has failed");
    	Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("umRole1", "user2", 0), "user2"), "Adding user2 to role has failed");

//    	Calling with same user list should delete the users.
    	for(int i=0; i < userFlagList.length; i++) {
    		userFlagList[i].setSelected(false);
    	}

    	userMgtClient.updateUsersOfRole("umRole1", userFlagList);
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("umRole1", "user1", 0), "user1"), "Deleting user1 from role has failed");
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("umRole1", "user2", 0), "user2"), "Deleting user2 from role has failed");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check update role name", dependsOnMethods = "testUpdateUsersOfRole")
    public void testUpdateRoleName() throws Exception {

    	loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
				isServer.getSuperTenant().getTenantAdmin().getPassword(),
				isServer.getInstance().getHosts().get("default"));

    	userMgtClient.addRole("umRole7", null, new String[]{"login"}, false);
    	userMgtClient.updateRoleName("umRole7", "umRole7_1");

    	Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames("umRole7", 10), "umRole7"), "Role umRole7 update failed.");
    	Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("umRole7_1", 10), "umRole7_1"), "Updating role umRole7 to umRole7_1 has failed.");

    	userMgtClient.deleteRole("umRole7_1");
    	loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check update roles of user", dependsOnMethods = "testUpdateRoleName")
    public void testUpdateRolesOfUser() throws Exception {

        List<String> newRoleList = new ArrayList<String>();
        FlaggedName[] currentRoleList = userMgtClient.getRolesOfUser("user3", null, 0);
        if (currentRoleList != null) {
            for (FlaggedName role : currentRoleList) {
                if (role.getSelected()) {
                    newRoleList.add(role.getItemName());
                }
            }
        }

        if (!newRoleList.contains("umRole1")) {
            newRoleList.add("umRole1");
        }

        userMgtClient.updateRolesOfUser("user3",
                newRoleList.toArray(new String[newRoleList.size()]));
        Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser("user3", "umRole1", 10),
                "umRole1"), "Adding umRole1 to user has failed");

        newRoleList.remove("umRole1");
        userMgtClient.updateRolesOfUser("user3",
                newRoleList.toArray(new String[newRoleList.size()]));
        Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("user3", "umRole1", 10),
                "umRole1"), "Deleting umRole1 from user has failed");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check change user password", dependsOnMethods = "testUpdateRolesOfUser")
    public void testChangePassword() throws Exception {

    	loginManger.login("user4", "passWord1@", isServer.getInstance().getHosts().get("default"));
    	loginManger.logOut();

    	userMgtClient.changePassword("user4", "passWord2@");
		loginManger.logOut();

    	String value = loginManger.login("user4", "passWord2@", isServer.getInstance().getHosts().get("default"));

    	Assert.assertTrue((value.indexOf("JSESSIONID") != -1), "User password change failed.");
		//Before logout try to revert the password change
		userMgtClient.changePassword("user4", "passWord1@");
    	loginManger.logOut();

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check get shared of current user", dependsOnMethods = "testChangePassword")
    public void testGetRolesOfCurrentUser() throws Exception {

    	userMgtClient.addRole("umRole5", new String[]{"user4"}, new String[]{"admin"}, false);

    	loginManger.login("user4", "passWord1@", isServer.getInstance().getHosts().get("default"));

    	Assert.assertTrue(nameExists(userMgtClient.getRolesOfCurrentUser(), "umRole5"), "Getting current user roles has failed.");

    	loginManger.logOut();
    	userMgtClient.deleteRole("umRole5");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check list users by claim value", dependsOnMethods = "testGetRolesOfCurrentUser")
    public void testListUserByClaim() throws Exception {

    	ClaimValue claimValue = new ClaimValue();
    	claimValue.setClaimURI("http://wso2.org/claims/lastname");
    	claimValue.setValue("user3");

    	loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
				isServer.getSuperTenant().getTenantAdmin().getPassword(),
				isServer.getInstance().getHosts().get("default"));

    	FlaggedName[] allNames = userMgtClient.listUserByClaim(claimValue, "user3", 10);

    	Assert.assertTrue(nameExists(allNames, "user3"), "List user with claim value has failed");

    	loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check set role UI permissions to resource", dependsOnMethods = "testListUserByClaim")
    public void testSetRoleUIPermission() throws Exception {

    	String resourceName = "/permission/testlogin";

    	userMgtClient.setRoleUIPermission("umRole1", new String[]{resourceName});
    	UIPermissionNode uiPermissions = userMgtClient.getAllUIPermissions();

    	Assert.assertNotNull(uiPermissions.getResourcePath(), "Setting ui permissions to resource has failed.");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check getting user realm info", dependsOnMethods = "testSetRoleUIPermission")
    public void testGetUserRealmInfo() throws Exception{

    	loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
				isServer.getSuperTenant().getTenantAdmin().getPassword(),
				isServer.getInstance().getHosts().get("default"));
    	UserRealmInfo realmInfo = userMgtClient.getUserRealmInfo();

    	Assert.assertNotNull(realmInfo);

    	loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check shared role enable", dependsOnMethods = "testGetUserRealmInfo")
    public void testIsSharedRolesEnabled() throws Exception{

    	Assert.assertFalse(userMgtClient.isSharedRolesEnabled());

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check getting permissions for role", dependsOnMethods = "testIsSharedRolesEnabled")
    public void testGetRolePermissions() throws Exception{

    	UIPermissionNode permission = userMgtClient.getRolePermissions("umRole2");
    	Assert.assertEquals(permission.getDisplayName(), "All Permissions");
    }

	//todo need to fix properly
	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
//    @Test(groups = "wso2.is", description = "Check importing bulk users", dependsOnMethods = "testGetRolePermissions")
    public void testBulkImportUsers() throws Exception{

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

    protected boolean userNameExists(String[] allNames, String inputName) {

        boolean exists = false;

        for (String name : allNames) {
            if (inputName.equals(name)) {
                return true;
            } else {
                exists = false;
            }
        }

        return exists;
    }


	
}
