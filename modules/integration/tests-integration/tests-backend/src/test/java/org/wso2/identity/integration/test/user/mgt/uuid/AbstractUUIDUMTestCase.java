/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.user.mgt.uuid;

import org.testng.Assert;
import org.wso2.carbon.identity.test.integration.service.stub.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimValue;
import org.wso2.carbon.identity.test.integration.service.stub.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.LoginIdentifierDTO;
import org.wso2.carbon.identity.test.integration.service.stub.PermissionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UniqueIDUserClaimSearchEntryDAO;
import org.wso2.carbon.identity.test.integration.service.stub.UserDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UserRoleListDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.core.model.OperationalOperation;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.usermgt.uuid.UUIDUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUUIDUMTestCase extends ISIntegrationTest {

    protected UUIDUserStoreManagerServiceClient userMgtClient;
    protected UserManagementClient oldUserMgtClient;
    protected AuthenticatorClient loginManger;

    protected String adminUsername;
    protected String adminPassword;

    private String user1Username = "uuid-user1";
    private String user2Username = "uuid-user2";
    private String user3Username = "uuid-user3";

    private String role1name = "uuid-role1";
    private String role2name = "uuid-role2";
    private String role3name = "uuid-role3";
    private String role4name = "uuid-role4";
    private String role5name = "uuid-role5";
    private String role6name = "uuid-role6";
    private String role7name = "uuid-role7";

    private String defaultProfile = "default";

    private String credential1 = "credential1";
    private String credential2 = "credential2";

    private String userId;

    protected void init() throws Exception {

        super.init();

        userMgtClient = new UUIDUserStoreManagerServiceClient(backendURL, sessionCookie);
        oldUserMgtClient = new UserManagementClient(backendURL, sessionCookie);
        loginManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
    }

    public void testAddUser() throws Exception {

        UserDTO userDTO = userMgtClient.addUserWithID(user1Username, credential1, new String[0], new ClaimValue[0],
                defaultProfile);
        this.userId = userDTO.getUserID();
        Assert.assertNotNull(userDTO);
    }

    public void testGetUser() throws Exception {

        UserDTO user = userMgtClient.getUserWithID(this.userId, new String[0], defaultProfile);

        Assert.assertNotNull(user);
        Assert.assertEquals(this.userId, user.getUserID());
    }

    public void testDeleteUser() throws Exception {

        userMgtClient.deleteUserWithID(this.userId);
        boolean value = userMgtClient.isExistingUserWithID(userId);
        Assert.assertFalse(value);

        // Restore to previous state.
        testAddUser();
    }

    public void testAuthenticateWithIDLoginIdentifier() throws Exception {

        LoginIdentifierDTO loginIdentifierDTO = new LoginIdentifierDTO();
        loginIdentifierDTO.setLoginIdentifierType("CLAIM_URI");
        loginIdentifierDTO.setLoginKey("http://wso2.org/claims/username");
        loginIdentifierDTO.setLoginValue(user1Username);
        loginIdentifierDTO.setProfileName(defaultProfile);

        AuthenticationResultDTO authenticationResultDTO = userMgtClient
                .authenticateWithIDLoginIdentifier(new LoginIdentifierDTO[]{loginIdentifierDTO}, "PRIMARY",
                        credential1);
        Assert.assertNotNull(authenticationResultDTO);
    }

    public void testAuthenticateWithIDUserId() throws Exception {

        AuthenticationResultDTO authenticationResultDTO = userMgtClient
                .authenticateWithIDUserId(userId, "PRIMARY", credential1);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");
    }

    public void testGetProfileNamesWithID() throws Exception {

        String[] profiles = userMgtClient.getProfileNamesWithID(userId);
        Assert.assertNotNull(profiles);
        Assert.assertTrue(profiles.length > 0);
    }

    public void testAuthenticateWithIDUsernameClaim() throws Exception {

        String usernameClaim = "http://wso2.org/claims/username";

        AuthenticationResultDTO authenticationResultDTO = userMgtClient
                .authenticateWithIDUsernameClaim(usernameClaim, user1Username, credential1, defaultProfile);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");
    }

    public void testGetPasswordExpirationTimeWithID() throws Exception {

        userMgtClient.getPasswordExpirationTimeWithID(userId);
        Assert.assertTrue(true);
    }

    public void testListAllUsers() throws Exception {

        Assert.assertTrue(userNameExists(userMgtClient.listUsersWithID(user1Username, 100), user1Username),
                "List all users has failed");
    }

    public void testIsExistingUserWithID() throws Exception {

        Assert.assertTrue(userMgtClient.isExistingUserWithID(userId));
    }

    public void testGetUserClaimValueWithID() throws Exception {

        String usernameClaim = "http://wso2.org/claims/username";

        String claimValue = userMgtClient.getUserClaimValueWithID(userId, usernameClaim, defaultProfile);
        Assert.assertNotNull(claimValue);
    }

    public void testGetUserClaimValuesWithID() throws Exception {

        String usernameClaim = "http://wso2.org/claims/username";

        ClaimValue[] claimValues = userMgtClient.getUserClaimValuesWithID(userId, new String[]{usernameClaim},
                defaultProfile);
        Assert.assertNotNull(claimValues);
        Assert.assertTrue(claimValues.length > 0);
    }

    public void testGetUserListOfRoleWithID() throws Exception {

        String roleName = "admin";
        UserDTO[] userDTOs = userMgtClient.getUserListOfRoleWithID(roleName);
        Assert.assertNotNull(userDTOs);
        Assert.assertTrue(userDTOs.length > 0);
    }

    public void testAddRoleWithId() throws Exception {

        String roleName = role6name;
        String[] userIdList = new String[]{userId};
        PermissionDTO[] permissionDTOS = new PermissionDTO[0];

        userMgtClient.addRoleWithID(roleName, userIdList, permissionDTOS, false);
        Assert.assertTrue(userMgtClient.isUserInRoleWithID(userId, roleName));
    }

    public void testIsUserInRoleWithID() throws Exception {

        String roleName = role7name;
        String[] userIdList = new String[]{userId};
        PermissionDTO[] permissionDTOS = new PermissionDTO[0];

        userMgtClient.addRoleWithID(roleName, userIdList, permissionDTOS, false);
        Assert.assertTrue(userMgtClient.isUserInRoleWithID(userId, role7name));
    }

    public void listUsersWithID() throws Exception {

        String filter = user1Username;
        UserDTO[] userDTO = userMgtClient.listUsersWithID(filter, 100, 0);
        Assert.assertNotNull(userDTO);
        Assert.assertTrue(userDTO.length > 0);
    }

    public void testGetUserListWithID() throws Exception {

        String claimUri = "http://wso2.org/claims/username";
        String claimValue = user1Username;
        UserDTO[] userDTOS = userMgtClient.getUserListWithID(claimUri, claimValue, defaultProfile, 100, 0);
        Assert.assertNotNull(userDTOS);
        Assert.assertTrue(userDTOS.length > 0);
    }

    public void testGetRoleListOfUsersWithID() throws Exception {

        String[] userIds = new String[]{userId};
        UserRoleListDTO[] userRoleListDTOS = userMgtClient.getRoleListOfUsersWithID(userIds);
        Assert.assertNotNull(userRoleListDTOS);
        Assert.assertTrue(userRoleListDTOS.length > 0);
    }

    public void testListUsers() throws Exception {

        boolean exists = false;
        UserDTO[] usersList = userMgtClient.listUsersWithID(user1Username, 100);
        for (UserDTO user : usersList) {
            if (this.userId.equals(user.getUserID())) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists, "List users has failed");
    }

    public void testAddRemoveUsersOfRole() throws Exception {

        UserDTO userDTO = userMgtClient.addUserWithID(user2Username, credential2, new String[0], new ClaimValue[0],
                defaultProfile);

        userMgtClient.addRoleWithID(role3name, new String[]{userDTO.getUserID()}, new PermissionDTO[]{}, false);

        String[] newUsers = new String[]{userDTO.getUserID()};
        String[] deletedUsers = new String[]{userId};

        userMgtClient.updateUserListOfRoleWithID(role3name, newUsers, deletedUsers);

        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID(role3name), user1Username),
                "Getting user added to umRole3 role failed");
        Assert.assertFalse(userNameExists(userMgtClient.getUserListOfRoleWithID(role3name), user2Username),
                "User user2 still exists in the umRole3 role");

        // Clean up the modified users of role and test it.
        userMgtClient.updateUserListOfRoleWithID(role3name, deletedUsers, newUsers);
        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID(role3name), user2Username),
                "Getting user added to umRole3 role failed");
        Assert.assertFalse(userNameExists(userMgtClient.getUserListOfRoleWithID(role3name), user1Username),
                "User user1 still exists in the umRole3 role");
    }

    public void testAddRemoveRolesOfUser() throws Exception {

        userMgtClient.addRoleWithID(role1name, new String[]{userId}, new PermissionDTO[]{}, false);
        userMgtClient.addRoleWithID(role2name, new String[0], new PermissionDTO[]{}, false);

        String[] newRoles = new String[]{role1name};
        String[] deletedRoles = new String[]{role2name};

        userMgtClient.updateRoleListOfUserWithID(userId, newRoles, deletedRoles);

        Assert.assertTrue(nameExists(userMgtClient.getRoleListOfUserWithID(userId), role2name),
                "Adding role to user1 has failed");
        Assert.assertFalse(nameExists(userMgtClient.getRoleListOfUserWithID(userId), role1name),
                "Role still exists in the user1 roles");

        // Clean up the modified roles of user and test it.
        userMgtClient.updateRoleListOfUserWithID(userId, deletedRoles, null);
        Assert.assertFalse(nameExists(userMgtClient.getRoleListOfUserWithID(userId), role2name),
                "Role still exists in the user1 roles");
    }

    public void testUpdateUsersOfRole() throws Exception {

        userMgtClient.addRoleWithID(role4name, new String[]{userId}, new PermissionDTO[]{}, false);

        UserDTO userDTO = userMgtClient.addUserWithID(user3Username, credential2, new String[0], new ClaimValue[0],
                defaultProfile);

        String[] userList = new String[]{userDTO.getUserID()};

        userMgtClient.updateUserListOfRoleWithID(role4name, null, userList);
        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID(role4name, user1Username, 1),
                user1Username), "Adding user1 to role has failed");
        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID(role4name, user3Username, 1),
                user3Username), "Adding user3 to role has failed");

        userMgtClient.updateUserListOfRoleWithID(role4name, new String[] {userDTO.getUserID()}, null);
        Assert.assertFalse(userNameExists(userMgtClient.getUserListOfRoleWithID(role4name, user3Username, 1),
                user3Username), "Removing user3 from role has failed");
    }

    public void testGetRolesOfCurrentUser() throws Exception {

        userMgtClient.addRoleWithID(role5name, new String[]{userId}, new PermissionDTO[]{}, false);
        String[] roles = userMgtClient.getRoleListOfUserWithID(userId);

        for (String role : roles) {
            if (role5name.equals(role)) {
                Assert.assertTrue(true);
                break;
            }
        }
    }

    public void testSetUserClaimValues() throws Exception {

        String claimValueStr = "last name";
        String claimURIStr = "http://wso2.org/claims/lastname";

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimUri(claimURIStr);
        claimValue.setClaimValue(claimValueStr);

        userMgtClient.setUserClaimValuesWithID(userId, new ClaimValue[]{claimValue}, defaultProfile);

        ClaimDTO[] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, defaultProfile);

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimURIStr.equals(claimDTO.getClaimUri())) {
                Assert.assertEquals(claimDTO.getValue(), claimValueStr);
                break;
            }
        }
    }

    public void testListUserByClaim() throws Exception {

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimUri("http://wso2.org/claims/lastname");
        claimValue.setClaimValue(user3Username);

        userMgtClient.setUserClaimValuesWithID(userId, new ClaimValue[]{claimValue}, defaultProfile);
        UserDTO[] userDTOS = userMgtClient.getUserListWithID(claimValue.getClaimUri(), user3Username, defaultProfile);

        Assert.assertNotNull(userDTOS);
        Assert.assertTrue(userDTOS.length > 0);
    }

    public void testDeleteUserClaimValuesWithID() throws Exception {

        String claimValueStr = "Sri Lanka";
        String claimURIStr = "http://wso2.org/claims/country";

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimUri(claimURIStr);
        claimValue.setClaimValue(claimValueStr);

        userMgtClient.setUserClaimValuesWithID(userId, new ClaimValue[]{claimValue}, defaultProfile);
        userMgtClient.deleteUserClaimValuesWithID(userId, new String[] {claimURIStr}, defaultProfile);

        ClaimDTO [] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, defaultProfile);

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimURIStr.equals(claimDTO.getClaimUri())) {
                Assert.assertNotEquals(claimValue, claimDTO.getValue());
                break;
            }
        }
    }

    public void testGetUserListConditionWithID() throws Exception {

        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setOperation(OperationalOperation.AND.toString());

        UserDTO [] userDTOS = userMgtClient.getUserListWithID(conditionDTO, "PRIMARY", defaultProfile, 100, 0, null,
                null);

        Assert.assertNotNull(userDTOS);
        Assert.assertTrue(userDTOS.length > 0);
    }

    public void testGetUsersClaimValuesWithID() throws Exception {

        List<String> userIds = new ArrayList<>();
        userIds.add(userId);

        List<String> claims = new ArrayList<>();
        claims.add("http://wso2.org/claims/username");

        UniqueIDUserClaimSearchEntryDAO[] uniqueIDUserClaimSearchEntries = userMgtClient
                .getUsersClaimValuesWithID(userIds, claims, defaultProfile);
        Assert.assertNotNull(uniqueIDUserClaimSearchEntries);
        Assert.assertTrue(uniqueIDUserClaimSearchEntries.length > 0);
    }

    public void testDeleteUserClaimValueWithID() throws Exception {

        String claimUri = "http://wso2.org/claims/lastname";
        String claimValue = "lastname";

        userMgtClient.setUserClaimValueWithID(userId, claimUri, claimValue, defaultProfile);
        userMgtClient.deleteUserClaimValueWithID(userId, claimUri, defaultProfile);

        ClaimDTO [] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, defaultProfile);

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimUri.equals(claimDTO.getClaimUri())) {
                Assert.assertNotEquals(claimValue, claimDTO.getValue());
                break;
            }
        }
    }

    public void testUpdateCredentialWithID() throws Exception {

        AuthenticationResultDTO authenticationResultDTO = userMgtClient.authenticateWithIDUserId(userId, "PRIMARY",
                credential1);

        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");

        String newCredential = credential2;
        userMgtClient.updateCredentialWithID(userId, newCredential, credential1);

        authenticationResultDTO = userMgtClient.authenticateWithIDUserId(userId, "PRIMARY",
                newCredential);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");

        // Revert again.
        userMgtClient.updateCredentialWithID(userId, credential1, newCredential);
    }

    public void testUpdateCredentialByAdminWithID() throws Exception {

        String newCredential = credential2;
        userMgtClient.updateCredentialByAdminWithID(userId, newCredential);

        AuthenticationResultDTO authenticationResultDTO = userMgtClient.authenticateWithIDUserId(userId, "PRIMARY",
                newCredential);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");

        // Revert again.
        userMgtClient.updateCredentialByAdminWithID(userId, credential1);
    }

    /**
     * Checks whether the passed Name exists in the FlaggedName array.
     *
     * @param userDTOS
     * @param inputName
     * @return
     */
    protected boolean userNameExists(UserDTO[] userDTOS, String inputName) {

        if (userDTOS == null) {
            return false;
        }

        boolean exists = false;

        for (UserDTO userDTO : userDTOS) {
            String name = userDTO.getUsername();

            if (name.equals(inputName)) {
                exists = true;
                break;
            } else {
                exists = false;
            }
        }

        return exists;
    }

    protected boolean nameExists(String[] allNames, String inputName) {

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

    protected void setUser1Username(String user1Username) {

        this.user1Username = user1Username;
    }

    protected void setUser2Username(String user2Username) {

        this.user2Username = user2Username;
    }

    protected void setUser3Username(String user3Username) {

        this.user3Username = user3Username;
    }
}
