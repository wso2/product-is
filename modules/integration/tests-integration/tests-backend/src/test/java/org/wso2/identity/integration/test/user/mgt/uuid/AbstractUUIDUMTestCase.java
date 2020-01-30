package org.wso2.identity.integration.test.user.mgt.uuid;

import org.testng.Assert;
import org.wso2.carbon.identity.test.integration.service.stub.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.LoginIdentifierDTO;
import org.wso2.carbon.identity.test.integration.service.stub.PermissionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UserDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UserRoleListDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.core.model.xsd.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.mgt.common.xsd.ClaimValue;
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

    private String username = "user1";
    private String credential = "credential1";
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

        UserDTO userDTO = userMgtClient.addUserWithID(username, credential, new String[0],
                new org.wso2.carbon.user.mgt.common.xsd.ClaimValue[0], "default");
        this.userId = userDTO.getUserID();
        Assert.assertNotNull(userDTO);
    }

    public void testGetUser() throws Exception {

        UserDTO user = userMgtClient.getUserWithID(this.userId, new String[0], "default");

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
        loginIdentifierDTO.setLoginValue(username);
        loginIdentifierDTO.setProfileName("default");

        AuthenticationResultDTO authenticationResultDTO = userMgtClient
                .authenticateWithIDLoginIdentifier(new LoginIdentifierDTO[]{loginIdentifierDTO}, "PRIMARY",
                        credential);
        Assert.assertNotNull(authenticationResultDTO);
    }

    public void testAuthenticateWithIDUserId() throws Exception {

        AuthenticationResultDTO authenticationResultDTO = userMgtClient
                .authenticateWithIDUserId(userId, "PRIMARY", credential);
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
                .authenticateWithIDUsernameClaim(usernameClaim, username, credential, "default");
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");
    }

    public void testGetPasswordExpirationTimeWithID() throws Exception {

        userMgtClient.getPasswordExpirationTimeWithID(userId);
        Assert.assertTrue(true);
    }

    public void testListAllUsers() throws Exception {

        Assert.assertTrue(userNameExists(userMgtClient.listUsersWithID("user1", 100), "user1"),
                "List all users has failed");
    }

    public void testIsExistingUserWithID() throws Exception {

        Assert.assertTrue(userMgtClient.isExistingUserWithID(userId));
    }

    public void testGetUserClaimValueWithID() throws Exception {

        String usernameClaim = "http://wso2.org/claims/username";

        String claimValue = userMgtClient.getUserClaimValueWithID(userId, usernameClaim, "default");
        Assert.assertNotNull(claimValue);
    }

    public void testGetUserClaimValuesWithID() throws Exception {

        String usernameClaim = "http://wso2.org/claims/username";

        ClaimValue[] claimValues = userMgtClient.getUserClaimValuesWithID(userId, new String[]{usernameClaim},
                "default");
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

        String roleName = "role1";
        String[] userIdList = new String[]{userId};
        PermissionDTO[] permissionDTOS = new PermissionDTO[0];

        userMgtClient.addRoleWithID(roleName, userIdList, permissionDTOS, false);
        Assert.assertTrue(userMgtClient.isUserInRoleWithID(userId, roleName));
    }

    public void testIsUserInRoleWithID() throws Exception {

        Assert.assertTrue(userMgtClient.isUserInRoleWithID(userId, "role1"));
    }

    public void listUsersWithID() throws Exception {

        String filter = "user1";
        UserDTO[] userDTO = userMgtClient.listUsersWithID(filter, 100, 0);
        Assert.assertNotNull(userDTO);
        Assert.assertTrue(userDTO.length > 0);
    }

    public void testGetUserListWithID() throws Exception {

        String claimUri = "http://wso2.org/claims/username";
        String claimValue = "user1";
        UserDTO[] userDTOS = userMgtClient.getUserListWithID(claimUri, claimValue, "default", 100, 0);
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
        UserDTO[] usersList = userMgtClient.listUsersWithID("user1", 100);
        for (UserDTO user : usersList) {
            if (this.userId.equals(user.getUserID())) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists, "List users has failed");
    }

    public void testAddRemoveUsersOfRole() throws Exception {

        UserDTO userDTO = userMgtClient.addUserWithID("user2", "credential1", new String[0],
                new org.wso2.carbon.user.mgt.common.xsd.ClaimValue[0], "default");

        userMgtClient.addRoleWithID("umRole3", new String[]{userDTO.getUserID()}, new PermissionDTO[]{}, false);

        String[] newUsers = new String[]{userDTO.getUserID()};
        String[] deletedUsers = new String[]{userId};

        userMgtClient.updateUserListOfRoleWithID("umRole3", newUsers, deletedUsers);

        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID("umRole3"), "user1"),
                "Getting user added to admin role failed");
        Assert.assertFalse(userNameExists(userMgtClient.getUserListOfRoleWithID("umRole3", "user2", 0), "user2"),
                "User user2 still exists in the admin role");

        // Clean up the modified users of role and test it.
        userMgtClient.updateUserListOfRoleWithID("umRole3", null, newUsers);
        Assert.assertFalse(userNameExists(userMgtClient.getUserListOfRoleWithID("umRole3", "user1", 0), "user1"),
                "User user1 still exists in the admin role");
    }

    public void testAddRemoveRolesOfUser() throws Exception {

        userMgtClient.addRoleWithID("umRole1", new String[]{userId}, new PermissionDTO[]{}, false);
        userMgtClient.addRoleWithID("umRole2", new String[0], new PermissionDTO[]{}, false);

        String[] newRoles = new String[]{"umRole1"};
        String[] deletedRoles = new String[]{"umRole2"};

        userMgtClient.updateRoleListOfUserWithID(userId, newRoles, deletedRoles);

        Assert.assertTrue(nameExists(userMgtClient.getRoleListOfUserWithID(userId), "umRole2"),
                "Adding role to user1 has failed");
        Assert.assertFalse(nameExists(userMgtClient.getRoleListOfUserWithID(userId), "umRole1"),
                "Role still exists in the user1 roles");

        // Clean up the modified roles of user and test it.
        userMgtClient.updateRoleListOfUserWithID(userId, deletedRoles, null);
        Assert.assertFalse(nameExists(userMgtClient.getRoleListOfUserWithID(userId), "umRole2"),
                "Role still exists in the user1 roles");
    }

    public void testUpdateUsersOfRole() throws Exception {

        userMgtClient.addRoleWithID("umRole4", new String[]{userId}, new PermissionDTO[]{}, false);

        UserDTO userDTO = userMgtClient.addUserWithID("user3", "credential1", new String[0],
                new org.wso2.carbon.user.mgt.common.xsd.ClaimValue[0], "default");

        String[] userList = new String[]{userDTO.getUserID()};

        userMgtClient.updateUserListOfRoleWithID("umRole4", null, userList);
        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID("umRole4", "user1", 0), "user1"),
                "Adding user1 to role has failed");

        userMgtClient.updateUserListOfRoleWithID("umRole4", new String[] {userDTO.getUserID()}, null);
        Assert.assertTrue(userNameExists(userMgtClient.getUserListOfRoleWithID("umRole4", "user3", 0), "user3"),
                "Adding user3 to role has failed");
    }

    public void testGetRolesOfCurrentUser() throws Exception {

        userMgtClient.addRoleWithID("umRole5", new String[]{userId}, new PermissionDTO[]{}, false);
        String[] roles = userMgtClient.getRoleListOfUserWithID(userId);

        for (String role : roles) {
            if ("umRole5".equals(role)) {
                Assert.assertTrue(true);
                break;
            }
        }
    }

    public void testSetUserClaimValues() throws Exception {

        String claimValueStr = "last name";
        String claimURIStr = "http://wso2.org/claims/lastname";

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI(claimURIStr);
        claimValue.setValue(claimValueStr);

        userMgtClient.setUserClaimValuesWithID(userId, new ClaimValue[]{claimValue}, "default");

        ClaimDTO[] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, "default");

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimURIStr.equals(claimDTO.getClaimUri())) {
                Assert.assertEquals(claimDTO.getValue(), claimValueStr);
                break;
            }
        }
    }

    public void testListUserByClaim() throws Exception {

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI("http://wso2.org/claims/lastname");
        claimValue.setValue("user3");

        userMgtClient.setUserClaimValuesWithID(userId, new ClaimValue[]{claimValue}, "default");
        UserDTO[] userDTOS = userMgtClient.getUserListWithID(claimValue.getClaimURI(), "user3", "default");

        Assert.assertNotNull(userDTOS);
        Assert.assertTrue(userDTOS.length > 0);
    }

    public void testDeleteUserClaimValuesWithID() throws Exception {

        String claimUri = "http://wso2.org/claims/lastname";
        String claimValue = "lastname";

        userMgtClient.setUserClaimValueWithID(userId, claimUri, claimValue, "default");
        userMgtClient.deleteUserClaimValuesWithID(userId, new String[] {claimUri}, "default");

        ClaimDTO [] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, "default");

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimUri.equals(claimDTO.getClaimUri())) {
                Assert.assertNotEquals(claimValue, claimDTO.getValue());
                break;
            }
        }
    }

    public void testGetUserListConditionWithID() throws Exception {

        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setOperation("");

        UserDTO [] userDTOS = userMgtClient.getUserListWithID(conditionDTO, "PRIMARY", "default", 100, 0, null, null);
        Assert.assertNotNull(userDTOS);
        Assert.assertTrue(userDTOS.length > 0);
    }

    public void testGetUsersClaimValuesWithID() throws Exception {

        List<String> userIds = new ArrayList<>();
        userIds.add(userId);

        List<String> claims = new ArrayList<>();
        claims.add("http://wso2.org/claims/username");

        UniqueIDUserClaimSearchEntry [] uniqueIDUserClaimSearchEntries = userMgtClient
                .getUsersClaimValuesWithID(userIds, claims, "default");
        Assert.assertNotNull(uniqueIDUserClaimSearchEntries);
        Assert.assertTrue(uniqueIDUserClaimSearchEntries.length > 0);
    }

    public void testDeleteUserClaimValueWithID() throws Exception {

        String claimUri = "http://wso2.org/claims/lastname";
        String claimValue = "lastname";

        userMgtClient.setUserClaimValueWithID(userId, claimUri, claimValue, "default");
        userMgtClient.deleteUserClaimValueWithID(userId, claimUri, "default");

        ClaimDTO [] claimDTOs = userMgtClient.getUserClaimValuesWithID(userId, "default");

        for (ClaimDTO claimDTO : claimDTOs) {
            if (claimUri.equals(claimDTO.getClaimUri())) {
                Assert.assertNotEquals(claimValue, claimDTO.getValue());
                break;
            }
        }
    }

    public void testUpdateCredentialWithID() throws Exception {

        String newCredential = "credentia2";
        userMgtClient.updateCredentialWithID(userId, newCredential, credential);

        AuthenticationResultDTO authenticationResultDTO = userMgtClient.authenticateWithIDUserId(userId, "PRIMARY",
                newCredential);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");
    }

    public void testUpdateCredentialByAdminWithID() throws Exception {

        String newCredential = "credentia2";
        userMgtClient.updateCredentialByAdminWithID(userId, newCredential);

        AuthenticationResultDTO authenticationResultDTO = userMgtClient.authenticateWithIDUserId(userId, "PRIMARY",
                newCredential);
        Assert.assertNotNull(authenticationResultDTO);
        Assert.assertEquals(authenticationResultDTO.getAuthenticationStatus(), "SUCCESS");
    }

    /**
     * Checks whether the passed Name exists in the FlaggedName array.
     *
     * @param userDTOS
     * @param inputName
     * @return
     */
    protected boolean userNameExists(UserDTO[] userDTOS, String inputName) {

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

}
