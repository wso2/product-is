/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * WSO2 IS Integration tests for adding,deleting users and roles and changing passwords
 */
public class UserAdminTestCase {
    private static Log logger = LogFactory.getLog(UserAdminTestCase.class);
    private UserAdminStub userAdminStub = null;
    private String sessionCookie = null;
    private LoginLogoutUtil util = new LoginLogoutUtil();

    // Parameters for test methods
    private String userName= "User1";
    private String credential= "abc123";
    private String roleName= "eng";
    private String userName2= "User1";
    private String userName3= "User2";
    private String credential2= "ef432";
    private String roleName2= "eng";
    private String userName4= "User3";
    private String credential3= "lmnqp123";
    private String roleName3= "architect";
    private String[] userList= {"User1","User2"};
    private String roleName4= "architect";
    private String[] addingUsers= {"User3"};
    private String[] deletingUsers= {"User1","User2"};
    private String roleName5= "sales";
    private String userName5= "User3";
    private String userName6= "User1";
    private String[] updatedRoleList= {"sales",UserAdminConstants.EVERY_ONE_ROLE};
    private String[] newRoleList= {"sales"};
    private String[] oldRoleList= {"eng"};
    private String roleName6= "sales";
    private String oldRoleName= "eng";
    private String newRoleName= "engineer";
    private String userName7= "User1";
    private String userName8= "User3";
    private String password= "xyz321";

    @BeforeClass(groups = {"wso2.is"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9763);
        sessionCookie = util.login();
        logger.debug("Running User Admin Tests...");
        //authenticate admin service
        AuthenticateStub authenticateStub = new AuthenticateStub();
        userAdminStub = new UserAdminStub(UserAdminConstants.SERVICE_URL);
        authenticateStub.authenticateAdminStub(userAdminStub, sessionCookie);
    }

    @AfterClass(groups = {"wso2.is"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9763);
        util.logout();
        logger.debug("User Admin Tests were successful.");
    }

    //TODO:add just the role without a user

    //TODO: add role with permission list

    @Test(groups = "wso2.is")
    public void addUser() throws Exception {
        //add only the user name and password
        userAdminStub.addUser(userName, credential, null, null, null);
        //check whether user was added successfully
        String[] userList = userAdminStub.listUsers(userName);
        assertNotEquals(userList.length, 0, "Error in adding user");
    }

    // Fixing CARBON-11896
    @Test(groups = "wso2.is", dependsOnMethods = "addUser")
    public void addUserWithSpecialCharacters() throws Exception {
        //add only the user name and password
        String user = "amila/jayasekara";
        userAdminStub.addUser(user, credential, null, null, null);
        //check whether user was added successfully
        String[] userList = userAdminStub.listUsers(user);
        assertNotEquals(userList.length, 0, "Error in adding user");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addUserWithSpecialCharacters")
    public void addRoleWithUser() throws Exception {
        userAdminStub.addRole(roleName, new String[]{userName2}, null);
        FlaggedName[] roles = userAdminStub.getAllRolesNames();
        for (FlaggedName role : roles) {
            if (roleName.equals(role.getItemName())) {
                return;
            }
        }
        assertFalse(true, "Role: " + roleName + " was not added properly.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addRoleWithUser")
    public void addUserWithRole()
            throws Exception {
        userAdminStub.addUser(userName3, credential2, new String[]{roleName2}, null, null);
        FlaggedName[] users = userAdminStub.getUsersOfRole(roleName2, userName3);
        assertNotEquals(users.length, 0, "Error in adding users with role");

    }

    @Test(groups = "wso2.is", dependsOnMethods = "addUserWithRole")
    public void addUserWithClaims() throws Exception {
        ClaimValue claimVal1 = new ClaimValue();
        claimVal1.setClaimURI("http://wso2.org/claims/givenname");
        claimVal1.setValue("givenUserName");
        ClaimValue claimVal2 = new ClaimValue();
        claimVal2.setClaimURI("http://wso2.org/claims/givenname");
        claimVal2.setValue("givenUserName");
        ClaimValue[] claimValues = new ClaimValue[]{claimVal1, claimVal2};
        userAdminStub.addUser(userName4, credential3, null, claimValues, null);
        //there is no method to check whether claim values added properly.
        // If an exception doesn't occur, we can assume test was passed.
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addUserWithClaims")
    public void addRoleWithUserList() throws Exception {
        userAdminStub.addRole(roleName3, userList, null);
        for (String addUser : userList) {
            FlaggedName[] returnedUserList = userAdminStub.getUsersOfRole(roleName3, addUser);
            assertTrue(returnedUserList.length == 1 && returnedUserList[0].getSelected());
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addRoleWithUserList")
    public void updateUserListOfRole() throws Exception {
        List<FlaggedName> updatedUserList = new ArrayList<FlaggedName>();
        for (String addUser : addingUsers) {
            FlaggedName fName = new FlaggedName();
            fName.setItemName(addUser);
            fName.setSelected(true);
            updatedUserList.add(fName);
        }
        //add deleted users to the list
        for (String deletedUser : deletingUsers) {
            FlaggedName fName = new FlaggedName();
            fName.setItemName(deletedUser);
            fName.setSelected(false);
            updatedUserList.add(fName);
        }
        //call userAdminStub to update user list of role
        userAdminStub.updateUsersOfRole(roleName4, updatedUserList.toArray(
                new FlaggedName[updatedUserList.size()]));
        //verify by:
        //if delete users in retrieved list, fail
        for (String deletedUser : deletingUsers) {
            FlaggedName[] verifyingList = userAdminStub.getUsersOfRole(roleName4, deletedUser);
            assertTrue(!verifyingList[0].getSelected());
        }
        //if all added users are not in list fail
        for (String addingUser : addingUsers) {
            FlaggedName[] verifyingList = userAdminStub.getUsersOfRole(roleName4, addingUser);
            assertTrue(verifyingList[0].getSelected());
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "updateUserListOfRole")
    public void addRoleWithUser2() throws Exception {
        userAdminStub.addRole(roleName5, new String[]{userName5}, null);
        FlaggedName[] roles = userAdminStub.getAllRolesNames();
        for (FlaggedName role : roles) {
            if (roleName5.equals(role.getItemName())) {
                return;
            }
        }
        assertFalse(true, "Role: " + roleName + " was not added properly.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addRoleWithUser2")
    public void updateRolesOfUser() throws Exception {
        userAdminStub.updateRolesOfUser(userName6, updatedRoleList);
        FlaggedName[] roles = userAdminStub.getRolesOfUser(userName6);
        //check whether newly added roles are properly updated
        int count = 0;
        for (String newRole : newRoleList) {
            for (FlaggedName role : roles) {
                if (newRole.equals(role.getItemName()) && role.getSelected()) {
                    count++;
                }
            }

        }
        assertEquals(count, newRoleList.length, "Newly added roles not properly updated");
        //check whether deleted roles are updated properly
        count = 0;
        for (String deletedRole : oldRoleList) {
            for (FlaggedName role : roles) {
                if (deletedRole.equals(role.getItemName()) && (!role.getSelected())) {
                    count++;
                }
            }

        }
        assertEquals(count, oldRoleList.length, "Deleted roles not properly updated");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "updateRolesOfUser")
    public void deleteRole() throws Exception {
        userAdminStub.deleteRole(roleName6);
        //verify
        FlaggedName[] existingRoles = userAdminStub.getAllRolesNames();
        for (FlaggedName existingRole : existingRoles) {
            if (roleName6.equals(existingRole.getItemName())) {
                assertFalse(true, "Deleted role still exists...hence delete role test fails...");
            }
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "deleteRole")
    public void updateRole() throws Exception {
        userAdminStub.updateRoleName(oldRoleName, newRoleName);
        FlaggedName[] roles = userAdminStub.getAllRolesNames();
        for (FlaggedName role : roles) {
            if (oldRoleName.equals(role.getItemName())) {
                assertFalse(true, "Update role name test fails...");
            }
        }
    }

    @Test(groups = "wso2.is", dependsOnMethods = "updateRole")
    public void deleteUser() throws Exception {
        userAdminStub.deleteUser(userName7);
        String[] userList = userAdminStub.listUsers(userName6);
        assertTrue(userList == null || userList.length == 0);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "deleteUser")
    public void changePassword() throws Exception {
        userAdminStub.changePassword(userName8, password);
        //there is no way to verify whether password was changed correctly unless it gives
        //an exception on functional failure.
    }

}
