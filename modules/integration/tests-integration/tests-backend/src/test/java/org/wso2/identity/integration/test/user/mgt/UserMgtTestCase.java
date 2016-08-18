/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.user.mgt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class UserMgtTestCase extends ISIntegrationTest {

	private UserManagementClient userMgtClient;
	private AuthenticatorClient logManger;


	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

		super.init();

		logManger = new AuthenticatorClient(backendURL);
		userMgtClient = new UserManagementClient(backendURL, sessionCookie);

		if (!nameExists(userMgtClient.listAllUsers("testAdminUser", 100), "testAdminUser")) {
			userMgtClient.addUser("testAdminUser", "testAdminUser@123", new String[] { "admin" },
			                      "default");
		}

		userMgtClient.addUser("user1", "passWord1@", null, "default");
		userMgtClient.addUser("user2", "passWord1@", null, "default");
		userMgtClient.addUser("user3", "passWord1@", new String[] { "admin" }, "default");

		userMgtClient.addRole("umRole1", null, new String[] { "login" }, false);
		userMgtClient.addRole("umRole2", new String[] { "user1" }, new String[] { "login" }, false);
		userMgtClient.addRole("umRole3", null, new String[] { "login" }, false);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		userMgtClient.deleteUser("testAdminUser");
		userMgtClient.deleteRole("loginRole");
		userMgtClient.deleteUser("user1");
		userMgtClient.deleteUser("user2");
		userMgtClient.deleteUser("user3");
		userMgtClient.deleteRole("umRole1");
		userMgtClient.deleteRole("umRole2");
		userMgtClient.deleteRole("umRole3");
		logManger = null;
	}

	@Test(groups = "wso2.is", description = "Check role addition")
	public void testAddRole() throws Exception {
		userMgtClient.addRole("loginRole", new String[] { "testAdminUser" },
		                      new String[] { "/permission/admin/login" }, false);
		FlaggedName[] roles = userMgtClient.getRolesOfUser("testAdminUser", "loginRole", -1);
		Boolean isIncluded = false;
		for (FlaggedName role : roles) {
			if (role.getItemName().equalsIgnoreCase("loginRole")) {
				isIncluded = true;
			}
		}

		Assert.assertTrue(isIncluded,
		                  "Added role with user 'testAdminUser' could not be retrieved.");
	}

	@Test(groups = "wso2.is", description = "Get the users by user name")
	public void testListUsersByUserName() throws Exception {
		FlaggedName[] names = userMgtClient.listAllUsers("testAdminUser", -1);
		Assert.assertEquals(names[0].getItemName(), "testAdminUser");
	}

	@Test(groups = "wso2.is", description = "List the roles")
	public void testListRoles() throws Exception {
		FlaggedName[] roles = userMgtClient.listRoles("admin", -1);
		Assert.assertEquals(roles[0].getItemName(), "admin",
		                    "listRoles not including 'admin' for filter [admin]");
		FlaggedName[] roles2 = userMgtClient.listRoles("umRole1", -1);
		Assert.assertEquals(roles2[0].getItemName(), "umRole1",
		                    "listRoles not including 'umRole1' for filter [umRole1]");
	}

	@Test(groups = "wso2.is", description = "Get the roles by user name")
	public void testGetRolesByUserName() throws Exception {
		FlaggedName[] names = userMgtClient.getRolesOfUser("testAdminUser", "admin", -1);
		Assert.assertEquals(names[0].getItemName(), "admin",
		                    "Returned role is not 'admin' for user 'testAdminUser'");
	}

	@Test(groups = "wso2.is", description = "Check user existence")
	public void testUserNameExist() throws Exception {
		Boolean isExist = userMgtClient.userNameExists("admin", "testAdminUser");
		Assert.assertTrue(isExist, "Existing user 'testAdminUser' is not detected");
	}

	@Test(groups = "wso2.is", description = "Check role existence")
	public void testRoleNameExist() throws Exception {
		Boolean isExist = userMgtClient.roleNameExists("admin");
		Assert.assertTrue(isExist, "Role 'admin' is not found");
		Boolean isExistEveryoneRole = userMgtClient.roleNameExists("Internal/everyone");
		Assert.assertTrue(isExistEveryoneRole, "Role 'Internal/Everyone' is not found");
	}

	@Test(groups = "wso2.is", description = "Check internal role operations")
	public void testGetAllRoleNames() throws Exception {
		userMgtClient.addInternalRole("umRole6", null, new String[] { "login" });
		Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("umRole6", 0),
		                             "Internal/umRole6"), "Getting user roles has failed");
		userMgtClient.deleteRole("Internal/umRole6");
	}

	@Test(groups = "wso2.is", description = "Check internal role operations")
	public void testAddDeleteInternalRoleOperations() throws Exception {

		FlaggedName[] allRoles = null;
		String[] permissionList = new String[] { "/permission" };

		// Test add internal role without user
		userMgtClient.addInternalRole("manager_", null, permissionList);
		allRoles = userMgtClient.getAllRolesNames("manager_", 0);
		Assert.assertTrue(nameExists(allRoles, "Internal/manager_"),
		                  "The internal role add has failed");

		userMgtClient.deleteRole("Internal/manager_");
		allRoles = userMgtClient.getAllRolesNames("manager_", 0);
		Assert.assertFalse(nameExists(allRoles, "Internal/manager_"),
		                   "The internal role without user delete has failed");

		String[] userList = new String[] { "adminUser1_" };
		// Test add internal role with user
		userMgtClient.addInternalRole("sales_", userList, permissionList);
		allRoles = userMgtClient.getAllRolesNames("sales_", 0);
		Assert.assertTrue(nameExists(allRoles, "Internal/sales_"),
		                  "The internal role add has failed");

		userMgtClient.deleteRole("Internal/sales_");
		allRoles = userMgtClient.getAllRolesNames("sales_", 0);
		Assert.assertFalse(nameExists(allRoles, "Internal/sales_"),
		                   "The internal role with user delete has failed");
	}

	@Test(groups = "wso2.is", description = "Check add remove users of role ")
	public void testAddRemoveUsersOfRole() throws Exception {

		String[] newUsers = new String[] { "user1" };
		String[] deletedUsers = new String[] { "user2" };

		userMgtClient.addRemoveUsersOfRole("admin", newUsers, deletedUsers);

		Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"),
		                  "Getting user added to admin role failed");
		Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user2", 0), "user2"),
		                   "User user2 still exists in the admin role");

		// Clean up the modified users of role and test it.
		userMgtClient.addRemoveUsersOfRole("admin", null, newUsers);
		Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"),
		                   "User user1 still exists in the admin role");
	}

	@Test(groups = "wso2.is", description = "Check add remove roles of user")
	public void testAddRemoveRolesOfUser() throws Exception {

		String[] newRoles = new String[] { "umRole1" };
		String[] deletedRoles = new String[] { "umRole2" };

		userMgtClient.addRemoveRolesOfUser("user1", newRoles, deletedRoles);

		Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0), "umRole1"),
		                  "Adding role to user1 has failed");
		Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole2", 0),
		                              "umRole2"), "Role still exists in the user1 roles");

		// Clean up the modified roles of user and test it.
		userMgtClient.addRemoveRolesOfUser("user1", null, newRoles);
		Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0),
		                              "umRole1"), "Role still exists in the user1 roles");
	}

	@Test(groups = "wso2.is", description = "Check update users of role")
	public void testUpdateUsersOfRole() throws Exception {

		String[] userList = new String[] { "user1", "user2" };
		FlaggedName[] userFlagList = new FlaggedName[userList.length];

		for (int i = 0; i < userFlagList.length; i++) {
			FlaggedName flaggedName = new FlaggedName();
			flaggedName.setItemName(userList[i]);
			flaggedName.setSelected(true);
			userFlagList[i] = flaggedName;
		}

		userMgtClient.updateUsersOfRole("umRole1", userFlagList);
		Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("umRole1", "user1", 0), "user1"),
		                  "Adding user1 to role has failed");
		Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("umRole1", "user2", 0), "user2"),
		                  "Adding user2 to role has failed");

		// Calling with same user list should delete the users.
		for (int i = 0; i < userFlagList.length; i++) {
			userFlagList[i].setSelected(false);
		}

		userMgtClient.updateUsersOfRole("umRole1", userFlagList);
		Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("umRole1", "user1", 0), "user1"),
		                   "Deleting user1 from role has failed");
		Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("umRole1", "user2", 0), "user2"),
		                   "Deleting user2 from role has failed");
	}

	@Test(groups = "wso2.is", description = "Check update roles of user")
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

	@Test(groups = "wso2.is", description = "Check change user password")
	public void testChangePassword() throws Exception {

		String value1 = this.logManger.login("user3", "passWord1@", isServer.getInstance().getHosts().get("default"));
		Assert.assertTrue((value1.indexOf("JSESSIONID") != -1),
		                  "Login check for before change the password failed.");
		this.logManger.logOut();

		userMgtClient.changePassword("user3", "passwordS1@");

		String value2 = this.logManger.login("user3", "passwordS1@", isServer.getInstance().getHosts().get("default"));
		Assert.assertTrue((value2.indexOf("JSESSIONID") != -1), "User password change failed.");

		this.logManger.logOut();
		userMgtClient.changePassword("user3", "passWord1@");
	}

	@Test(groups = "wso2.is", description = "Check change logged user password")
	public void testChangePasswordByUser() throws Exception {

		userMgtClient.addUser("user6", "passWord1@", new String[] { "admin" }, "default");
		Assert.assertTrue(nameExists(userMgtClient.listAllUsers("user6", 10), "user6"),
		                  "Adding user 'user6' is failed.");

		UserManagementClient userMgtClient1 = new UserManagementClient(isServer.getContextUrls().getBackEndUrl(),
		                                                               "user6", "passWord1@");
		userMgtClient.changePasswordByUser("user6@carbon.super", "passWord1@", "passwordS1@");
		String value = this.logManger.login("user6", "passwordS1@", isServer.getInstance().getHosts().get("default"));
		Assert.assertTrue((value.indexOf("JSESSIONID") != -1), "User password change failed.");

		userMgtClient.deleteUser("user6");
	}

	@Test(groups = "wso2.is", description = "Check update role name")
	public void testUpdateRoleName() throws Exception {

		userMgtClient.addRole("umRole7", null, new String[] { "/permission/admin/login" }, false);
		userMgtClient.updateRoleName("umRole7", "umRole7_1");

		Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames("umRole7", 10), "umRole7"),
		                   "Role umRole7 update failed.");
		Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("umRole7_1", 10), "umRole7_1"),
		                  "Updating role umRole7 to umRole7_1 has failed.");

		userMgtClient.deleteRole("umRole7_1");
	}

	@Test(groups = "wso2.is", description = "Check get shared roles")
	public void testGetAllSharedRoleNames() throws Exception {
		// TODO - Adding shared role won't work as
		// <Property name="SharedGroupEnabled">true</Property> is not enabled
		// in user-mgt.xml

		// this.logManger.login(adminUsername, adminPassword,
		// isServer.getBackEndUrl());

		// userMgtClient.addRole("umShRole1", new String[]{"user1"}, new
		// String[]{"/permission/admin/login"}, true);
		// Assert.assertTrue(nameExists(userMgtClient.getAllSharedRoleNames("umShRole1",
		// 0), "umShRole1"), "Getting shared role name umShRole1 has failed.");

		// userMgtClient.deleteRole("umShRole1");
		// this.logManger.logout();
	}

	@Test(groups = "wso2.is", description = "Check set role UI permissions to resource")
	public void testSetRoleUIPermission() throws Exception {

		if (nameExists(userMgtClient.getAllRolesNames("umRole4", 100), "umRole4")) {
			userMgtClient.deleteRole("umRole4");
		}

		userMgtClient.addRole("umRole4", new String[] { "admin" }, null, false);
		userMgtClient.setRoleUIPermission("umRole4", new String[] { "/permission/admin/login" });
		List<String> selectedPermissions = getSelectedPermissions(userMgtClient.getRolePermissions("umRole4"),
		                                                          new ArrayList<String>());
		Assert.assertTrue((selectedPermissions != null && selectedPermissions.contains("/permission/admin/login")),
		                  "Setting ui permissions to resource has failed.");

		userMgtClient.deleteRole("umRole4");
	}

	@Test(groups = "wso2.is", description = "Check get roles of current user")
	public void testGetRolesOfCurrentUser() throws Exception {

		if (nameExists(userMgtClient.getAllRolesNames("umRole5", 100), "umRole5")) {
			userMgtClient.deleteRole("umRole5");
		}

		userMgtClient.addRole("umRole5", new String[] { "user3" }, new String[] { "admin" }, false);
		UserManagementClient userMgtClient1 = new UserManagementClient(isServer.getContextUrls().getBackEndUrl(),
		                                                               "user3", "passWord1@");
		Assert.assertTrue(nameExists(userMgtClient1.getRolesOfCurrentUser(), "umRole5"),
		                  "Getting current user roles has failed.");

		userMgtClient.deleteRole("umRole5");
	}

	@Test(groups = "wso2.is", description = "Check list users by claim value")
	public void testListUserByClaim() throws Exception {

		ClaimValue claimValue = new ClaimValue();
		claimValue.setClaimURI("http://wso2.org/claims/lastname");
		claimValue.setValue("user3");

		FlaggedName[] allNames = userMgtClient.listUserByClaim(claimValue, "user*", 10);
		Assert.assertTrue(nameExists(allNames, "user3"), "List user with claim value has failed");
	}

	@Test(groups = "wso2.is", description = "Check getting user realm info")
	public void testGetUserRealmInfo() throws Exception {

		UserRealmInfo realmInfo = userMgtClient.getUserRealmInfo();
		Assert.assertNotNull(realmInfo);
	}

	@Test(groups = "wso2.is", description = "Check shared role enable")
	public void testIsSharedRolesEnabled() throws Exception {

		Assert.assertFalse(userMgtClient.isSharedRolesEnabled());
	}

	@Test(groups = "wso2.is", description = "Check getting permissions for role")
	public void testGetRolePermissions() throws Exception {

		if (nameExists(userMgtClient.getAllRolesNames("umRole8", 100), "umRole8")) {
			userMgtClient.deleteRole("umRole8");
		}

		userMgtClient.addRole("umRole8", null, new String[] { "/permission" }, false);
		UIPermissionNode permission = userMgtClient.getRolePermissions("umRole8");
		Assert.assertTrue((permission != null &&
		                   permission.getResourcePath().equals("/permission/") && permission.getSelected()),
		                  "Unable to get the role permission");
	}

	//todo need to fix properly
//	@Test(groups = "wso2.is", description = "Check importing bulk users")
	public void testBulkImportUsers() throws Exception {

		//ToDo:get userStoreDomain properly
		String userStoreDomain = "PRIMARY";
		File bulkUserFile = new File(getISResourceLocation() + File.separator + "userMgt" +
		                             File.separator + "bulkUserImport.csv");

		DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
		userMgtClient.bulkImportUsers(userStoreDomain, "bulkUserImport.csv", handler, "PassWord1@");
		String[] userList = userMgtClient.listUsers("*", 100);

		Assert.assertNotNull(userList);
		Assert.assertEquals(userMgtClient.listUsers("bulkUser1", 10), new String[] { "bulkUser1" });
		Assert.assertEquals(userMgtClient.listUsers("bulkUser2", 10), new String[] { "bulkUser2" });
		Assert.assertEquals(userMgtClient.listUsers("bulkUser3", 10), new String[] { "bulkUser3" });

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

	/**
	 * Get selected permission list from the UIPermission node
	 *
	 * @param node
	 * @param selectedList
	 * @return
	 */
	private List<String> getSelectedPermissions(UIPermissionNode node, List<String> selectedList) {
		if (node != null) {
			if (node.getSelected()) {
				selectedList.add(node.getResourcePath());
			}

			UIPermissionNode[] nodeList = node.getNodeList();
			if (nodeList != null) {
				for (UIPermissionNode childNode : nodeList) {
					getSelectedPermissions(childNode, selectedList);
				}
			}
		}
		return selectedList;
	}
}