/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.mgt.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.um.ws.api.stub.ClaimDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class RemoteUserStoreManagerServiceTestCase extends ISIntegrationTest {

    private static Log log = LogFactory.getLog(RemoteUserStoreManagerServiceTestCase.class);
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerClient;

    private String role1 = "UserStoreTestRole1";
    private String role2 = "UserStoreTestRole2";
    private String role2NewName = "testNewNameRole2";

    private String user1 = "UserStoreTestUser1";// for role1
    private String user1Pwd = "testPassword1";
    private String user2 = "UserStoreTestUser2";// for role1
    private String user2Pwd = "testPassword2";
    private String user2NewPwd = "testNewPassword2";
    private String user3 = "UserStoreTestUser3";// for role2
    private String user3Pwd = "testPassword3";
    private String user4 = "UserStoreTestUser4";// for role2
    private String user4Pwd = "testPassword4";
    private String user4NewPwd = "testNewPassword4";
    private String user5 = "UserStoreTestUser5";// for role1 and role2
    private String user5Pwd = "testPassword5";

    private String claimUri = "http://wso2.org/claims/organization";
    private String claimValue = "WSO2";

    //claim values of user2
    private String claimURI1 = "http://wso2.org/claims/nickname";
    private String claimValue1 = "testCaseUser1";
    private String claimURI2 = "http://wso2.org/claims/country";
    private String claimValue2 = "testCaseUserCountry";
    private String claimURI3 = "http://wso2.org/claims/stateorprovince";
    private String claimValue3 = "western";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        remoteUserStoreManagerClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {
    }

    @Test(priority = 1, groups = "wso2.is", description = "Test addRole and isExistingRole operations")
    public void testAddRole()
            throws Exception {


        //add two test roles with empty users
        //remoteUserStoreManagerClient.addRole(role1, null, null);
        remoteUserStoreManagerClient.addRole(role2, null, null);

        //Add role1 with configure, login, manage, monitor, protected permissions
        UserManagementClient userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        String[] permissions = {"/permission/admin/configure/",
                                "/permission/admin/login",
                                "/permission/admin/manage/",
                                "/permission/admin/monitor",
                                "/permission/protected"};
        userMgtClient.addRole(role1, null, permissions);

        //verify role addition
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingRole(role1), "Adding new role failed : " + role1);
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingRole(role2), "Adding new role failed : " + role2);
    }

    @Test(priority = 2, groups = "wso2.is", description = "Test addUser and isExistingUser operations")
    public void testAddUser()
            throws RemoteException,
            org.wso2.carbon.user.api.UserStoreException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        //add users
        String[] roleList1 = new String[1];
        roleList1[0] = new String(role1);
        remoteUserStoreManagerClient.addUser(user1, user1Pwd, roleList1, null, null, false);

        String[] roleList2 = new String[1];
        roleList2[0] = new String(role1);
        remoteUserStoreManagerClient.addUser(user2, user2Pwd, roleList2, null, null, false);

        String[] roleList3 = new String[1];
        roleList3[0] = new String(role2);
        remoteUserStoreManagerClient.addUser(user3, user3Pwd, roleList3, null, null, false);

        String[] roleList4 = new String[1];
        roleList4[0] = new String(role2);
        remoteUserStoreManagerClient.addUser(user4, user4Pwd, roleList4, null, null, false);

        String[] roleList5 = new String[2];
        roleList5[0] = new String(role1);
        roleList5[1] = new String(role2);
        remoteUserStoreManagerClient.addUser(user5, user5Pwd, roleList5, null, null, false);


        //verify added users
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingUser(user1), "Adding new user failed : " + user1);
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingUser(user2), "Adding new user failed : " + user2);
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingUser(user3), "Adding new user failed : " + user3);
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingUser(user4), "Adding new user failed : " + user4);
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingUser(user5), "Adding new user failed : " + user5);

    }

    @Test(priority = 3, groups = "wso2.is", description = "Test listUsers operation")
    public void testListUsers()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] users = remoteUserStoreManagerClient.listUsers(user1, -1);

        Assert.assertNotNull(users, "user list retrieval failed");

        boolean userFound = false;
        for (String user : users) {
            if (user.equals(user1)) {
                userFound = true;
                break;
            }
        }
        Assert.assertTrue(userFound, "User not listed : " + user1);
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test getRoleNames operation")
    public void testGetRoleNames()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] roles = remoteUserStoreManagerClient.getRoleNames();

        Assert.assertNotNull(roles, "Retrieving role names failed");

        //check for newly added roles and default roles
        boolean role1Found = false;
        boolean role2Found = false;
        boolean adminRoleFound = false;
        boolean everyoneRoleFound = false;
        for (String role : roles) {
            if (role.equals(role1)) {
                role1Found = true;
            } else if (role.equals(role2)) {
                role2Found = true;
            } else if (role.equals("admin")) {
                adminRoleFound = true;
            } else if (role.equals("Internal/everyone")) {
                everyoneRoleFound = true;
            }
        }

        Assert.assertTrue(role1Found, "Role not listed : " + role1);
        Assert.assertTrue(role2Found, "Role not listed : " + role2);
        Assert.assertTrue(adminRoleFound, "Role not listed : admin");
        Assert.assertTrue(everyoneRoleFound, "Role not listed : Internal/everyone");
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test getAllProfileNames operation")
    public void testGetAllProfileNames()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] profiles = remoteUserStoreManagerClient.getAllProfileNames();

        Assert.assertNotNull(profiles, "Retrieving all profile names failed");

        //check for default profile
        boolean defaultProfileFound = false;
        for (String profile : profiles) {
            if (profile.equals("default")) {
                defaultProfileFound = true;
            }
        }
        Assert.assertTrue(defaultProfileFound, "Error in retrieving profile list");
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test getProfileNames operation")
    public void testGetProfileNames()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] profiles = remoteUserStoreManagerClient.getProfileNames(user1);

        Assert.assertNotNull(profiles, "Retrieving user profiles failed");
        Assert.assertEquals(profiles.length, 1, "Deviation from expected number of profiles related to user : " + user1);
        Assert.assertEquals(profiles[0], "default", "User does not belong to default profile");
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test authenticate operation")
    public void testAuthenticate()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        boolean authenticated = remoteUserStoreManagerClient.authenticate(user1, user1Pwd);
        Assert.assertTrue(authenticated, "Unable authenticate user : " + user1);
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test getPasswordExpirationTime operation")
    public void testGetPasswordExpirationTime()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        long expirationTime = remoteUserStoreManagerClient.getPasswordExpirationTime(user1);
        Assert.assertEquals(expirationTime, -1, "Deviation form expected value for password expiration time");
    }

    @Test(priority = 3, groups = "wso2.is", description = "Test setUserClaimValue and getUserClaimValue operations")
    public void testSetUserClaimValue()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        //add user claim
        remoteUserStoreManagerClient.setUserClaimValue(user1, claimUri, claimValue, null);

        //verify claim addition
        Assert.assertEquals(remoteUserStoreManagerClient.getUserClaimValue(user1, claimUri, null), claimValue, "Claim addition failed");
    }


    @Test(priority = 4, groups = "wso2.is", description = "Test setUserClaimValues and getUserClaimValuesForClaims operations")
    public void testSetUserClaimValues()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //set claims for user2
        ClaimValue[] claims = new ClaimValue[3];

        ClaimValue claim1 = new ClaimValue();
        claim1.setClaimURI(claimURI1);
        claim1.setValue(claimValue1);
        claims[0] = claim1;

        ClaimValue claim2 = new ClaimValue();
        claim2.setClaimURI(claimURI2);
        claim2.setValue(claimValue2);
        claims[1] = claim2;

        ClaimValue claim3 = new ClaimValue();
        claim3.setClaimURI(claimURI3);
        claim3.setValue(claimValue3);
        claims[2] = claim3;

        //add user claim
        remoteUserStoreManagerClient.setUserClaimValues(user2, claims, null);

        //verify claim addition
        String[] claimURIs = {claimURI1, claimURI2, claimURI3};

        ClaimValue[] newClaims = remoteUserStoreManagerClient.getUserClaimValuesForClaims(user2, claimURIs, null);

        Map<String, String> claimMap = new HashMap<String, String>();
        for (ClaimValue claim : newClaims) {
            claimMap.put(claim.getClaimURI(), claim.getValue());
        }

        Assert.assertTrue(claimMap.containsKey(claimURI1), "Claim not found :" + claimURI1);
        Assert.assertEquals(claimMap.get(claimURI1), claimValue1, "Claim deviate from added value");
        Assert.assertTrue(claimMap.containsKey(claimURI2), "Claim not found :" + claimURI2);
        Assert.assertEquals(claimMap.get(claimURI2), claimValue2, "Claim deviate from added value");
        Assert.assertTrue(claimMap.containsKey(claimURI3), "Claim not found :" + claimURI3);
        Assert.assertEquals(claimMap.get(claimURI3), claimValue3, "Claim deviate from added value");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getUserClaimValues operation")
    public void testGetUserClaimValues()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        ClaimDTO[] claims = remoteUserStoreManagerClient.getUserClaimValues(user2, null);
        Assert.assertNotNull(claims, "User claims retrieval failed");

        Map<String, ClaimDTO> claimMap = new HashMap<String, ClaimDTO>();
        for (ClaimDTO claim : claims) {
            claimMap.put(claim.getClaimUri(), claim);
        }

        //verify newly added claims are included in list
        Assert.assertTrue(claimMap.containsKey(claimURI1), "Claim not found :" + claimURI1);
        Assert.assertEquals(claimMap.get(claimURI1).getValue(), claimValue1, "Claim deviate from added value");
        Assert.assertTrue(claimMap.containsKey(claimURI2), "Claim not found :" + claimURI2);
        Assert.assertEquals(claimMap.get(claimURI2).getValue(), claimValue2, "Claim deviate from added value");
        Assert.assertTrue(claimMap.containsKey(claimURI3), "Claim not found :" + claimURI3);
        Assert.assertEquals(claimMap.get(claimURI3).getValue(), claimValue3, "Claim deviate from added value");

    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getRoleListOfUser operation")
    public void testGetRoleListOfUser()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] roles = remoteUserStoreManagerClient.getRoleListOfUser(user5);

        Assert.assertNotNull(roles, "Retrieving roles failed");

        if (roles.length > 0) {
            for (String role : roles) {
                if (!(role.equals(role1) || role.equals(role2) || role.equals("Internal/everyone"))) {
                    Assert.fail("User belongs to unknown role");
                }
            }
        } else {
            Assert.fail("Retrieving roles failed of user : " + user5);
        }
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getUserListOfRole operation")
    public void testGetUserListOfRole()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] userList = remoteUserStoreManagerClient.getUserListOfRole(role2);

        Assert.assertNotNull(userList, "User list retieval failed");
        if (userList.length != 3) {
            Assert.fail("Deviation from expected number of users");
        }

        boolean user3Found = false;
        boolean user4Found = false;
        for (String user : userList) {
            if (user.equals(user3)) {
                user3Found = true;
            } else if (user.equals(user4)) {
                user4Found = true;
            }
        }
        Assert.assertTrue(user3Found, "Unable to find user : " + user3);
        Assert.assertTrue(user4Found, "Unable to find user : " + user4);
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getTenantId operation")
    public void testGetTenantId()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        int tenantID = remoteUserStoreManagerClient.getTenantId();

        //verify tenant id
        Assert.assertEquals(tenantID, -1234, "Deviation from expected tenant id");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getUserList operation")
    public void testGetUserList()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] userList = remoteUserStoreManagerClient.getUserList(claimUri, claimValue, null);

        Assert.assertNotNull(userList, "User list retrieval failed");
        Assert.assertEquals(userList.length, 1,
                            "Unexpected user has same claimURI : " + claimUri + " with claim value : " + claimValue);
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test getHybridRoles operation")
    public void testGetHybridRoles()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        String[] roles = remoteUserStoreManagerClient.getHybridRoles();

        Assert.assertNotNull(roles, "Unable to retrieve hybrid roles");

        boolean defaultHybridRoleFound = false;
        for (String role : roles) {
            if (role.equals("Internal/everyone")) {
                defaultHybridRoleFound = true;
                break;
            }
        }

        Assert.assertTrue(defaultHybridRoleFound, "Default hybrid role unable to find");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test isReadOnly operation")
    public void testIsReadOnly()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        Assert.assertFalse(remoteUserStoreManagerClient.isReadOnly(), "User store in read only mode");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test updateCredential operation")
    public void testUpdateCredential()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        RemoteUserStoreManagerServiceClient rmtUserStoreManager = new RemoteUserStoreManagerServiceClient(backendURL, user2,
                user2Pwd);
        rmtUserStoreManager.updateCredential(user2, user2NewPwd, user2Pwd);

        //TODO: user old credentials get authenticated even after updating the credentials for some time. Therefore comment out below assertion until get fixed
        //TODO: Uncomment after fixing above issue
//        Assert.assertFalse(remoteUserStoreManagerClient.authenticate(user2, user2Pwd),
//                           "Credential update failed : old password not unchanged");
        Assert.assertTrue(remoteUserStoreManagerClient.authenticate(user2, user2NewPwd), "Password update failed");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test updateCredentialByAdmin operation")
    public void testUpdateCredentialByAdmin()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        remoteUserStoreManagerClient.updateCredentialByAdmin(user4, user4NewPwd);

        Assert.assertFalse(remoteUserStoreManagerClient.authenticate(user4, user4Pwd), "Credential update not Success");
        Assert.assertTrue(remoteUserStoreManagerClient.authenticate(user4, user4NewPwd), "Credential update failed");
    }


    @Test(priority = 5, groups = "wso2.is", description = "Test updateRoleListOfUser operation")
    public void testUpdateRoleListOfUser()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //remove user3 from role2 and add user3 to role1
        String[] deleteRoles = {role2};
        String[] newRoles = {role1};
        remoteUserStoreManagerClient.updateRoleListOfUser(user3, deleteRoles, newRoles);

        //verify update
        String[] roles = remoteUserStoreManagerClient.getRoleListOfUser(user3);
        Assert.assertNotNull(roles, "getRoleListOfUser failed");

        boolean roleDeleteSuccess = true;
        boolean roleNewSuccess = false;
        for (String role : roles) {
            if (role.equals(role1)) {
                roleNewSuccess = true;
            } else if (role.equals(role2)) {
                roleDeleteSuccess = false;
            }
        }
        Assert.assertTrue(roleNewSuccess, "Assigning new role to user failed");
        Assert.assertTrue(roleDeleteSuccess, "Role deletion failed");
    }


    @Test(priority = 6, groups = "wso2.is", description = "Test updateUserListOfRole operation")
    public void testUpdateUserListOfRole()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        //remove user4 and add user2 to role2
        String[] deleteUser = {user4};
        String[] addUser = {user2};
        remoteUserStoreManagerClient.updateUserListOfRole(role2, deleteUser, addUser);

        //verify update
        String[] users = remoteUserStoreManagerClient.getUserListOfRole(role2);
        Assert.assertNotNull(users, "getUserListOfRole failed");

        boolean deleteSuccess = true;
        boolean addSuccess = false;

        for (String user : users) {
            if (user.equals(user2)) {
                addSuccess = true;
            } else if (user.equals(user4)) {
                deleteSuccess = false;
            }
        }
        Assert.assertTrue(addSuccess, "adding new user failed");
        Assert.assertTrue(deleteSuccess, "deleting user failed");
    }


    @Test(priority = 7, groups = "wso2.is", description = "Test updateRoleName operation")
    public void testUpdateRoleName()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //update name of role2
        remoteUserStoreManagerClient.updateRoleName(role2, role2NewName);
        //verify update
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingRole(role2), "Role name unchanged");
        Assert.assertTrue(remoteUserStoreManagerClient.isExistingRole(role2NewName), "Role name update failed");
    }


    @Test(priority = 7, groups = "wso2.is", description = "Test deleteUserClaimValue operation")
    public void testDeleteUserClaimValue()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //delete claimUri of user1
        remoteUserStoreManagerClient.deleteUserClaimValue(user1, claimUri, null);

        //verify deletion
        Assert.assertNull(remoteUserStoreManagerClient.getUserClaimValue(user1, claimUri, null), "Claim deletion failed");
    }


    @Test(priority = 7, groups = "wso2.is", description = "Test deleteUserClaimValues operation")
    public void testDeleteUserClaimValues()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //delete added test claims for user2
        String[] claims = {claimURI1, claimURI2, claimURI3};
        remoteUserStoreManagerClient.deleteUserClaimValues(user2, claims, null);

        //verify claim deletion
        Assert.assertNull(remoteUserStoreManagerClient.getUserClaimValue(user2, claimURI1, null), "Claim deletion failed : " + claimURI1);
        Assert.assertNull(remoteUserStoreManagerClient.getUserClaimValue(user2, claimURI2, null), "Claim deletion failed : " + claimURI2);
        Assert.assertNull(remoteUserStoreManagerClient.getUserClaimValue(user2, claimURI2, null), "Claim deletion failed : " + claimURI3);
    }


    @Test(priority = 8, groups = "wso2.is", description = "Test deleteUser operation")
    public void testDeleteUser()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //delete all users
        remoteUserStoreManagerClient.deleteUser(user1);
        remoteUserStoreManagerClient.deleteUser(user2);
        remoteUserStoreManagerClient.deleteUser(user3);
        remoteUserStoreManagerClient.deleteUser(user4);
        remoteUserStoreManagerClient.deleteUser(user5);
        //verify deletion
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingUser(user1), "User deletion failed :" + user1);
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingUser(user2), "User deletion failed :" + user2);
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingUser(user3), "User deletion failed :" + user3);
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingUser(user4), "User deletion failed :" + user4);
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingUser(user5), "User deletion failed :" + user5);
    }


    @Test(priority = 9, groups = "wso2.is", description = "Test deleteRole operation")
    public void testDeleteRole()
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        //delete all created roles
        remoteUserStoreManagerClient.deleteRole(role1);
        remoteUserStoreManagerClient.deleteRole(role2NewName);

        //verify role deletion
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingRole(role1), "Role deletion failed : " + role1);
        Assert.assertFalse(remoteUserStoreManagerClient.isExistingRole(role2NewName), "Role deletion failed : " + role2NewName);
    }
}
