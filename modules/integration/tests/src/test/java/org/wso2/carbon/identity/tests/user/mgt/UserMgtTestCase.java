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

package org.wso2.carbon.identity.tests.user.mgt;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.utils.LoginLogoutUtil;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.carbon.utils.CarbonUtils;


public class UserMgtTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserMgtTestCase.class);
    private UserManagementClient userMgtClient;
    private LoginLogoutUtil logManger;
    private String adminUsername;
    private String adminPassword;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(0);
        logManger = new LoginLogoutUtil(isServer.getBackEndUrl());
        
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        
        userMgtClient = new UserManagementClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        
		if (!nameExists(userMgtClient.listAllUsers("lanka", 100), "lanka")) {
			userMgtClient.addUser("lanka", "lanka123", new String[] { "admin" }, "default");
		}
        userMgtClient.addUser("user1", "passWord1@", null, "default");
        userMgtClient.addUser("user2", "passWord1@", null, "default");
        userMgtClient.addUser("user3", "passWord1@", new String[]{"admin"}, "default");
        
        userMgtClient.addRole("umRole1", null, new String[]{"login"}, false);
        userMgtClient.addRole("umRole2", new String[]{"user1"}, new String[]{"login"}, false);
        userMgtClient.addRole("umRole3", null, new String[]{"login"}, false);
        
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userMgtClient.deleteUser("lanka");
        userMgtClient.deleteRole("architect");    
        userMgtClient.deleteUser("user1");
        userMgtClient.deleteUser("user2");
        userMgtClient.deleteUser("user3");
        
        userMgtClient.deleteRole("umRole1");
        userMgtClient.deleteRole("umRole2");
        userMgtClient.deleteRole("umRole3");
//        userMgtClient.deleteRole("umRole5");
//        userMgtClient.deleteRole("umRole4");
//        userMgtClient.deleteRole("umRole4_1");

        
        logManger = null;

    }

    @Test(groups = "wso2.is", description = "Get the users by user name")
    public void testListUsersByUserName() throws Exception {
        FlaggedName[] names = userMgtClient.listAllUsers("lanka", -1);
        Assert.assertEquals(names[0].getItemName(), "lanka");
    }

    @Test(groups = "wso2.is", description = "List the roles")
    public void testListRoles() throws Exception {
        FlaggedName[] roles = userMgtClient.listRoles("admin", -1);
        Assert.assertEquals(roles[0].getItemName(), "admin", "listRoles not including 'admin' for filter [admin]");
        FlaggedName[] roles2 = userMgtClient.listRoles("testRole", -1);
        Assert.assertEquals(roles2[0].getItemName(), "testRole", "listRoles not including 'testRole' for filter [testRole]");
    }


    @Test(groups = "wso2.is", description = "Get the roles by user name")
    public void testGetRolesByUserName() throws Exception {
        FlaggedName[] names = userMgtClient.getRolesOfUser("lanka", "admin", -1);
        Assert.assertEquals(names[0].getItemName(), "admin", "Returned role is not 'admin' for user 'lanka'");

    }

    @Test(groups = "wso2.is", description = "Check user existence")
    public void testUserNameExist() throws Exception {
        Boolean isExist = userMgtClient.userNameExists("admin", "lanka");
        Assert.assertTrue(isExist, "Existing user 'lanka' is not detected");

    }

    @Test(groups = "wso2.is", description = "Check role existence")
    public void testRoleNameExist() throws Exception {
        Boolean isExist = userMgtClient.roleNameExists("admin");
        Assert.assertTrue(isExist, "Role 'admin' is not found");
        Boolean isExistEveryoneRole = userMgtClient.roleNameExists("Internal/everyone");
        Assert.assertTrue(isExistEveryoneRole, "Role 'Internal/Everyone' is not found");

    }

    @Test(groups = "wso2.is", description = "Check role addition")
    public void testAddRole() throws Exception {
        userMgtClient.addRole("architect", new String[]{"lanka"}, new String[]{"login"}, false);
        FlaggedName[] roles = userMgtClient.getRolesOfUser("lanka", "architect", -1);
        Boolean isIncluded = false;
        for (FlaggedName role : roles) {
            if (role.getItemName().equalsIgnoreCase("architect")) {
                isIncluded = true;
            }
        }
        Assert.assertTrue(isIncluded, "Added role with user 'lanka' could not be retrieved.");
    }
    
    @Test(groups = "wso2.is", description = "Check internal role operations")
    public void testGetAllRoleNames() throws Exception {
    	userMgtClient.addInternalRole("umRole6", null, new String[]{"login"});
    	Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("umRole6", 0), "Internal/umRole6"), "Getting user roles has failed");
    	userMgtClient.deleteRole("Internal/umRole6");
    }
    
    @Test(groups = "wso2.is", description = "Check internal role operations")
    public void testAddDeleteInternalRoleOperations() throws Exception {

    	FlaggedName[] allRoles = null;
    	String[] permissionList = new String[]{"/permission"};
    	
//    	Test add internal role without user
    	userMgtClient.addInternalRole("manager_", null, permissionList);    	
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	
    	Assert.assertTrue(nameExists(allRoles, "Internal/manager_"), "The internal role add has failed");
    	
    	userMgtClient.deleteRole("Internal/manager_");
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	Assert.assertFalse(nameExists(allRoles, "Internal/manager_"), "The internal role without user delete has failed");
    	
    	String[] userList = new String[]{"lanka1_"};
//    	Test add internal role with user
    	userMgtClient.addInternalRole("sales_", userList, permissionList);    	
    	allRoles = userMgtClient.getAllRolesNames("sales_", 0);
    	
    	Assert.assertTrue(nameExists(allRoles, "Internal/sales_"), "The internal role add has failed");
		  	  	
    	userMgtClient.deleteRole("Internal/sales_");
    	allRoles = userMgtClient.getAllRolesNames("manager_", 0);
    	Assert.assertFalse(nameExists(allRoles, "Internal/sales_"), "The internal role with user delete has failed");
    }

    @Test(groups = "wso2.is", description = "Check add remove users of role ")
    public void testAddRemoveUsersOfRole() throws Exception {
    	
    	String[] newUsers = new String[]{"user1"};
    	String[] deletedUsers = new String[]{"user2"};
    	
    	userMgtClient.addRemoveUsersOfRole("admin", newUsers, deletedUsers);
    	
    	Assert.assertTrue(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"), "Getting user added to admin role failed");
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user2", 0), "user2"), "User user2 still exists in the admin role");
    	
//    	Clean up the modified users of role and test it.
    	userMgtClient.addRemoveUsersOfRole("admin", null, newUsers);
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("admin", "user1", 0), "user1"), "User user1 still exists in the admin role");
    }
    
    @Test(groups = "wso2.is", description = "Check add remove roles of user")
    public void testAddRemoveRolesOfUser() throws Exception {
    	
    	String[] newRoles = new String[]{"umRole1"};
    	String[] deletedRoles = new String[]{"umRole2"};
    	
    	userMgtClient.addRemoveRolesOfUser("user1", newRoles, deletedRoles);
    	
    	Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0), "umRole1"), "Adding role to user1 has failed");
    	Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole2", 0), "umRole2"), "Role still exists in the user1 roles");
    	
//    	Clean up the modified roles of user and test it.
    	userMgtClient.addRemoveRolesOfUser("user1", null, newRoles);
    	Assert.assertFalse(nameExists(userMgtClient.getRolesOfUser("user1", "umRole1", 0), "umRole1"), "Role still exists in the user1 roles");
    }
    
    @Test(groups = "wso2.is", description = "Check update users of role")
    public void testUpdateUsersOfRole() throws Exception {
    	
    	String[] userList = new String[]{"user1", "user2"};
    	FlaggedName[] userFlagList = new FlaggedName[userList.length];
    	
    	for(int i=0; i < userFlagList.length; i++) {
    		FlaggedName flaggedName = new FlaggedName();
    		flaggedName.setItemName(userList[i]);
    		flaggedName.setSelected(true);
    		userFlagList[i] = flaggedName;
    	}
    	
    	userMgtClient.updateUsersOfRole("umRole1", userFlagList);    	
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
  
//    TODO - invalid data error
    @Test(groups = "wso2.is", description = "Check update roles of user")
    public void testUpdateRolesOfUser() throws Exception {
    	
    	String[] roleList = new String[]{"umRole1"};
//    	userMgtClient.addRemoveRolesOfUser("user3", null, new String[]{"testRole"});
    	
    	userMgtClient.updateRolesOfUser("user3", roleList);    	    	
    	Assert.assertTrue(nameExists(userMgtClient.getRolesOfUser("user3", "umRole1", 10), "umRole1"), "Adding umRole1 to user has failed");
    	
//    	Calling with same user list should delete the users.
    	userMgtClient.updateRolesOfUser("user3", roleList);
    	Assert.assertFalse(nameExists(userMgtClient.getUsersOfRole("user3", "umRole1", 10), "umRole1"), "Deleting umRole1 from user has failed");

    }
    
    
    @Test(groups = "wso2.is", description = "Check change user password")
    public void testChangePassword() throws Exception {
    	
    	this.logManger.login("user3", "passWord1@", isServer.getBackEndUrl());
    	this.logManger.logout();
    	
    	userMgtClient.changePassword("user3", "passwordS1@");
    	
    	String value = this.logManger.login("user3", "passwordS1@", isServer.getBackEndUrl());
    	
    	Assert.assertTrue((value.indexOf("JSESSIONID") != -1), "User password change failed.");
    	this.logManger.logout();

    }
    
//    TODO - 
    @Test(groups = "wso2.is", description = "Check change user password")
    public void testChangePasswordByUser() throws Exception {
    	
//    	userMgtClient.addUser("user6", "passWord1@", new String[]{"admin"}, "default");
//    	this.logManger.login("user6", "passWord1@", isServer.getBackEndUrl());
//    	
//    	userMgtClient.changePasswordByUser("passWord1@", "passwordS1@");
//    	
//    	String value = this.logManger.login("user6", "passwordS1@", isServer.getBackEndUrl());
//    	
//    	Assert.assertTrue((value.indexOf("JSESSIONID") != -1), "User password change failed.");
//    	this.logManger.logout();
//    	
//    	userMgtClient.deleteUser("user6");
    }
    
//    TODO - Why the role update get failed?
    @Test(groups = "wso2.is", description = "Check update role name")
    public void testUpdateRoleName() throws Exception {

    	this.logManger.login(adminUsername, adminPassword, isServer.getBackEndUrl());
    	
    	userMgtClient.addRole("umRole7", null, new String[]{"login"}, false);
    	userMgtClient.updateRoleName("umRole7", "umRole7_1");
    	
    	Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames("umRole7", 10), "umRole7"), "Role umRole7 update failed.");
    	Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("umRole7_1", 10), "umRole7_1"), "Updating role umRole7 to umRole7_1 has failed.");

    	userMgtClient.deleteRole("umRole7_1");
    	this.logManger.logout();
    }
    
    @Test(groups = "wso2.is", description = "Check get shared roles")
    public void testGetAllSharedRoleNames() throws Exception {
//    	TODO - Adding shared role throws Nullpointer when you enable <Property name="SharedGroupEnabled">true</Property>
//    	in user-mgt.xml
    	
/*    	this.logManger.login(adminUsername, adminPassword, isServer.getBackEndUrl());
    	
    	userMgtClient.addRole("umShRole1", new String[]{"user1"}, new String[]{"login"}, true);
    	Assert.assertTrue(nameExists(userMgtClient.getAllSharedRoleNames("umShRole1", 0), "umShRole1"), "Getting shared role name umShRole1 has failed.");
    	
    	userMgtClient.deleteRole("umShRole1");
    	this.logManger.logout();*/
    }
    
    @Test(groups = "wso2.is", description = "Check set role UI permissions to resource")
    public void testSetRoleUIPermission() throws Exception {
    	
    	String resourceName = "/permission/testlogin";
    	
    	userMgtClient.setRoleUIPermission("umRole1", new String[]{resourceName});
    	UIPermissionNode uiPermissions = userMgtClient.getAllUIPermissions();

    	Assert.assertNotNull(uiPermissions.getResourcePath(), "Setting ui permissions to resource has failed.");
    	
    }
    
    @Test(groups = "wso2.is", description = "Check get shared of current user")
    public void testGetRolesOfCurrentUser() throws Exception {
    	
    	this.logManger.login(adminUsername, adminPassword, isServer.getBackEndUrl());
    	
		if (nameExists(userMgtClient.getAllRolesNames("umRole5", 100), "umRole5")) {
			userMgtClient.deleteRole("umRole5");
		}
    	
    	userMgtClient.addRole("umRole5", new String[]{"user3"}, new String[]{"admin"}, false);

//    	this.logManger.login("user3", "passWord1@", isServer.getBackEndUrl());
    	
    	Assert.assertTrue(nameExists(userMgtClient.getRolesOfCurrentUser(), "umRole5"), "Getting current user roles has failed.");
    	
    	this.logManger.logout();
    	userMgtClient.deleteRole("umRole5");
    }
    
    @Test(groups = "wso2.is", description = "Check list users by claim value")
    public void testListUserByClaim() throws Exception {
    	
//    	ClaimValue claimValue = new ClaimValue();
//    	claimValue.setClaimURI("http://wso2.org/claims/lastname");
//    	claimValue.setValue("user3");
//
//    	this.logManger.login("admin", "admin", isServer.getBackEndUrl());
//    	
//    	TODO - following call returns null
//    	FlaggedName[] allNames = userMgtClient.listUserByClaim(claimValue, "user3", 10);
//		
//    	Assert.assertTrue(nameExists(allNames, "user3"), "List user with claim value has failed");
//    	
//    	this.logManger.logout();    	
    }
    
    @Test(groups = "wso2.is", description = "Check getting user realm info")
    public void testGetUserRealmInfo() throws Exception{
    	
    	this.logManger.login(adminUsername, adminPassword, isServer.getBackEndUrl());    	
    	UserRealmInfo realmInfo = userMgtClient.getUserRealmInfo();
    	
    	Assert.assertNotNull(realmInfo);
    
    	this.logManger.logout();
    }
    
    @Test(groups = "wso2.is", description = "Check shared role enable")
    public void testIsSharedRolesEnabled() throws Exception{
    	
    	Assert.assertFalse(userMgtClient.isSharedRolesEnabled());

    }
    
    @Test(groups = "wso2.is", description = "Check getting permissions for role")
    public void testGetRolePermissions() throws Exception{
    	
    	UIPermissionNode permission = userMgtClient.getRolePermissions("umRole2");
    	Assert.assertEquals(permission.getDisplayName(), "All Permissions");
    }
    
    @Test(groups = "wso2.is", description = "Check importing bulk users")
    public void testBulkImportUsers() throws Exception{
    	
		File bulkUserFile = new File(getISResourceLocation() + File.separator + "userMgt"
				+ File.separator + "bulkUserImport.csv");
		
		DataHandler handler = new DataHandler(new FileDataSource(bulkUserFile));
		userMgtClient.bulkImportUsers("bulkUserImport.csv", handler, "PassWord1@");
		
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