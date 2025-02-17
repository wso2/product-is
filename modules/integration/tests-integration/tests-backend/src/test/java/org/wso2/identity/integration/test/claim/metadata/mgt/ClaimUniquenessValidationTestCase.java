/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.claim.metadata.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimRes;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient.CreateUserResponse;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;

import java.io.File;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

/**
 * This class contains test cases for validating claim uniqueness across primary and secondary user stores.
 */
public class ClaimUniquenessValidationTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(ClaimUniquenessValidationTestCase.class);

    private SCIM2RestClient scim2RestClient;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private ClaimManagementRestClient claimManagementRestClient;

    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String SECONDARY_DOMAIN_ID = "SECONDARY";
    private static final String PRIMARY_DOMAIN_ID = "PRIMARY";
    private static final String USER_STORE_DB_NAME = "UNIQUENESS_VALIDATION_SECONDARY_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private String userStoreId;

    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String TEST_USER_PASSWORD = "Sample1$";
    private static final String TEST_USER_FIRST_NAME = "uvFirstName";
    private static final String TEST_USER_LAST_NAME = "uvLastName";
    private static final String TEST_USER1_USERNAME = "uvuser1";
    private static final String TEST_USER2_USERNAME = "uvuser2";
    private static final String TEST_USER3_USERNAME = "uvuser3";
    private static final String TEST_USER_EMAIL = "uvuser@wso2.com";
    private static final String TEST_USER1_EMAIL = "uvuser1@wso2.com";
    private static final String TEST_USER2_EMAIL = "uvuser2@wso2.com";
    private static final String TEST_USER3_EMAIL = "uvuser3@wso2.com";

    private LocalClaimReq emailClaimReq;
    private String emailClaimId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);

        initializeEmailClaim();
        createSecondaryUserStore();
    }

    private void initializeEmailClaim() throws Exception {

        LocalClaimRes existingClaim = claimManagementRestClient.getLocalClaimByUri(EMAIL_CLAIM_URI);
        emailClaimId = existingClaim.getId();
        emailClaimReq = new LocalClaimReq();
        emailClaimReq.setClaimURI(existingClaim.getClaimURI());
        emailClaimReq.setDisplayName(existingClaim.getDisplayName());
        emailClaimReq.setDescription(existingClaim.getDescription());
        emailClaimReq.setDisplayOrder(existingClaim.getDisplayOrder());
        emailClaimReq.setReadOnly(existingClaim.getReadOnly());
        emailClaimReq.setRequired(existingClaim.getRequired());
        emailClaimReq.setSupportedByDefault(existingClaim.getSupportedByDefault());
        emailClaimReq.setAttributeMapping(existingClaim.getAttributeMapping());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.NONE);
        userStoreMgtRestClient.deleteUserStore(userStoreId);
        scim2RestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
        userStoreMgtRestClient.closeHttpClient();
    }

    @Test(description = "Test user creation when email uniqueness validation is disabled (NONE).")
    public void testUserCreationWithoutEmailUniquenessValidation() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        try {
            // First set the claim uniqueness scope to NONE.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.NONE);

            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            user1Id = scim2RestClient.createUser(user1);

            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER1_EMAIL);
            user2Id = scim2RestClient.createUser(user2);

            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER1_EMAIL);
            user3Id = scim2RestClient.createUser(user3);

            // Verify users exist.
            Assert.assertNotNull(scim2RestClient.getUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME));
            Assert.assertNotNull(scim2RestClient.getUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME));
            Assert.assertNotNull(scim2RestClient.getUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME));
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
        }
    }

    @Test(description = "Test user creation with email uniqueness validation within userstore (WITHIN_USERSTORE).")
    public void testUserCreationWithEmailUniquenessWithinUserStore() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        try {
            // Set claim uniqueness scope to WITHIN_USERSTORE.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.WITHIN_USERSTORE);

            // Create first user in PRIMARY userstore - should succeed.
            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response1 = scim2RestClient.attemptUserCreation(user1);
            Assert.assertEquals(response1.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to create user1 in PRIMARY userstore with unique email.");
            user1Id = response1.getUserId();
            Assert.assertNotNull(scim2RestClient.getUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME));

            // Try to create second user in PRIMARY userstore with same email - should fail.
            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response2 = scim2RestClient.attemptUserCreation(user2);
            if (response2.getUserId() != null) {
                user2Id = response2.getUserId();
            }
            Assert.assertNotEquals(response2.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "User2 creation succeeded when it should have failed due to duplicate " +
                            "email within PRIMARY userstore.");

            // Create user in SECONDARY userstore with same email - should succeed.
            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response3 = scim2RestClient.attemptUserCreation(user3);
            Assert.assertEquals(response3.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to create user3 in SECONDARY userstore with duplicate email " +
                            "from PRIMARY userstore.");
            user3Id = response3.getUserId();
            Assert.assertNotNull(scim2RestClient.getUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME));
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
        }
    }

    @Test(description = "Test user creation with email uniqueness validation across userstores (ACROSS_USERSTORES).")
    public void testUserCreationWithEmailUniquenessAcrossUserStores() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        String user4Id = null;
        try {
            // Set claim uniqueness scope to ACROSS_USERSTORES.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.ACROSS_USERSTORES);

            // Create first user in PRIMARY userstore - should succeed.
            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response1 = scim2RestClient.attemptUserCreation(user1);
            Assert.assertEquals(response1.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to create user1 in PRIMARY userstore with unique email.");
            user1Id = response1.getUserId();
            Assert.assertNotNull(scim2RestClient.getUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME));

            // Try to create second user in PRIMARY userstore with same email - should fail.
            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response2 = scim2RestClient.attemptUserCreation(user2);
            if (response2.getUserId() != null) {
                user2Id = response2.getUserId();
            }
            Assert.assertNotEquals(response2.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "User2 creation succeeded when it should have failed due to duplicate " +
                            "email in PRIMARY userstore.");

            // Try to create user in SECONDARY userstore with same email - should fail.
            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response3 = scim2RestClient.attemptUserCreation(user3);
            if (response3.getUserId() != null) {
                user3Id = response3.getUserId();
            }
            Assert.assertNotEquals(response3.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "User3 creation succeeded when it should have failed due to duplicate " +
                            "email in PRIMARY userstore.");

            // Verify user creation with different email in SECONDARY userstore succeeds.
            UserObject user4 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER3_EMAIL);
            CreateUserResponse response4 = scim2RestClient.attemptUserCreation(user4);
            Assert.assertEquals(response4.getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Failed to create user4 in SECONDARY userstore with unique email.");
            user4Id = response4.getUserId();
            Assert.assertNotNull(scim2RestClient.getUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME));
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
            if (user4Id != null) {
                scim2RestClient.deleteUser(user4Id);
            }
        }
    }

    @Test(description = "Test email update when uniqueness validation is disabled (NONE).")
    public void testEmailUpdateWithNoUniquenessValidation() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        try {
            // Set claim uniqueness scope to NONE.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.NONE);

            // Create users with initial emails.
            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response1 = scim2RestClient.attemptUserCreation(user1);
            Assert.assertEquals(response1.getStatusCode(), HttpServletResponse.SC_CREATED);
            user1Id = response1.getUserId();

            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER2_EMAIL);
            CreateUserResponse response2 = scim2RestClient.attemptUserCreation(user2);
            Assert.assertEquals(response2.getStatusCode(), HttpServletResponse.SC_CREATED);
            user2Id = response2.getUserId();

            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER3_EMAIL);
            CreateUserResponse response3 = scim2RestClient.attemptUserCreation(user3);
            Assert.assertEquals(response3.getStatusCode(), HttpServletResponse.SC_CREATED);
            user3Id = response3.getUserId();

            PatchOperationRequestObject patchOp = createEmailUpdatePatchRequest(TEST_USER_EMAIL);

            // All users' email update should succeed.
            int updateStatus1 = scim2RestClient.attemptUserUpdate(patchOp, user1Id);
            Assert.assertEquals(updateStatus1, HttpServletResponse.SC_OK, 
                    "Failed to update email for user1 when uniqueness validation is disabled.");

            int updateStatus2 = scim2RestClient.attemptUserUpdate(patchOp, user2Id);
            Assert.assertEquals(updateStatus2, HttpServletResponse.SC_OK, 
                    "Failed to update email for user2 when uniqueness validation is disabled.");

            int updateStatus3 = scim2RestClient.attemptUserUpdate(patchOp, user3Id);
            Assert.assertEquals(updateStatus3, HttpServletResponse.SC_OK, 
                    "Failed to update email for user3 when uniqueness validation is disabled.");
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
        }
    }

    @Test(description = "Test email update with uniqueness validation within userstore (WITHIN_USERSTORE).")
    public void testEmailUpdateWithinUserStoreValidation() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        try {
            // Set claim uniqueness scope to WITHIN_USERSTORE.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.WITHIN_USERSTORE);

            // Create users with initial emails.
            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response1 = scim2RestClient.attemptUserCreation(user1);
            Assert.assertEquals(response1.getStatusCode(), HttpServletResponse.SC_CREATED);
            user1Id = response1.getUserId();

            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER2_EMAIL);
            CreateUserResponse response2 = scim2RestClient.attemptUserCreation(user2);
            Assert.assertEquals(response2.getStatusCode(), HttpServletResponse.SC_CREATED);
            user2Id = response2.getUserId();

            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER3_EMAIL);
            CreateUserResponse response3 = scim2RestClient.attemptUserCreation(user3);
            Assert.assertEquals(response3.getStatusCode(), HttpServletResponse.SC_CREATED);
            user3Id = response3.getUserId();

            PatchOperationRequestObject patchOp = createEmailUpdatePatchRequest(TEST_USER_EMAIL);

            // User1 email update should succeed.
            int updateStatus1 = scim2RestClient.attemptUserUpdate(patchOp, user1Id);
            Assert.assertEquals(updateStatus1, HttpServletResponse.SC_OK, 
                    "Failed to update email for user1 in PRIMARY userstore.");

            // User2 email update should fail.
            int updateStatus2 = scim2RestClient.attemptUserUpdate(patchOp, user2Id);
            Assert.assertNotEquals(updateStatus2, HttpServletResponse.SC_OK,
                    "Email update succeeded for user2 in PRIMARY userstore when it should have failed due " +
                            "to duplicate email within PRIMARY userstore.");

            // User3 email update should succeed.
            int updateStatus3 = scim2RestClient.attemptUserUpdate(patchOp, user3Id);
            Assert.assertEquals(updateStatus3, HttpServletResponse.SC_OK, 
                    "Failed to update email for user3 in SECONDARY userstore with duplicate " +
                            "email from PRIMARY userstore.");
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
        }
    }

    @Test(description = "Test email update with uniqueness validation across userstores (ACROSS_USERSTORES).")
    public void testEmailUpdateAcrossUserStoresValidation() throws Exception {

        String user1Id = null;
        String user2Id = null;
        String user3Id = null;
        try {
            // Set claim uniqueness scope to ACROSS_USERSTORES.
            setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum.ACROSS_USERSTORES);

            // Create users with initial emails.
            UserObject user1 = initUser(PRIMARY_DOMAIN_ID, TEST_USER1_USERNAME, TEST_USER1_EMAIL);
            CreateUserResponse response1 = scim2RestClient.attemptUserCreation(user1);
            Assert.assertEquals(response1.getStatusCode(), HttpServletResponse.SC_CREATED);
            user1Id = response1.getUserId();

            UserObject user2 = initUser(PRIMARY_DOMAIN_ID, TEST_USER2_USERNAME, TEST_USER2_EMAIL);
            CreateUserResponse response2 = scim2RestClient.attemptUserCreation(user2);
            Assert.assertEquals(response2.getStatusCode(), HttpServletResponse.SC_CREATED);
            user2Id = response2.getUserId();

            UserObject user3 = initUser(SECONDARY_DOMAIN_ID, TEST_USER3_USERNAME, TEST_USER3_EMAIL);
            CreateUserResponse response3 = scim2RestClient.attemptUserCreation(user3);
            Assert.assertEquals(response3.getStatusCode(), HttpServletResponse.SC_CREATED);
            user3Id = response3.getUserId();

            PatchOperationRequestObject patchOp = createEmailUpdatePatchRequest(TEST_USER_EMAIL);

            // User1 email update should succeed.
            int updateStatus1 = scim2RestClient.attemptUserUpdate(patchOp, user1Id);
            Assert.assertEquals(updateStatus1, HttpServletResponse.SC_OK, 
                    "Failed to update email for user1 in PRIMARY userstore.");

            // User2 email update should fail.
            int updateStatus2 = scim2RestClient.attemptUserUpdate(patchOp, user2Id);
            Assert.assertNotEquals(updateStatus2, HttpServletResponse.SC_OK,
                    "Email update succeeded for user2 in PRIMARY userstore when it should have failed " +
                            "due to duplicate email in PRIMARY userstore.");

            // User3 email update should fail.
            int updateStatus3 = scim2RestClient.attemptUserUpdate(patchOp, user3Id);
            Assert.assertNotEquals(updateStatus3, HttpServletResponse.SC_OK,
                    "Email update succeeded for user3 in SECONDARY userstore when it should have failed due " +
                            "to duplicate email in PRIMARY userstore.");
        } finally {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
            }
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
            }
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
            }
        }
    }

    private UserObject initUser(String domain, String username, String email) {

        UserObject user = new UserObject();
        user.setUserName(domain + "/" + username);
        user.setPassword(TEST_USER_PASSWORD);
        user.setName(new Name().givenName(TEST_USER_FIRST_NAME).familyName(TEST_USER_LAST_NAME));
        user.addEmail(new Email().value(email));
        return user;
    }

    private void setClaimUniquenessScope(LocalClaimReq.UniquenessScopeEnum scope) throws Exception {

        emailClaimReq.setUniquenessScope(scope);
        claimManagementRestClient.updateLocalClaim(emailClaimId, emailClaimReq);
        log.info("Updated claim uniqueness scope for email to: " + scope);
    }

    private void createSecondaryUserStore() throws Exception {

        // Creating database.
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        // Register a secondary user store.
        UserStoreReq userStore = new UserStoreReq()
                .typeId(USER_STORE_TYPE)
                .name(SECONDARY_DOMAIN_ID)
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("driverName")
                        .value("org.h2.Driver"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("url")
                        .value("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("userName")
                        .value(DB_USER_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("password")
                        .value(DB_USER_PASSWORD))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UsernameJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("Disabled")
                        .value("false"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordDigest")
                        .value("SHA-256"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("StoreSaltedPassword")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("SCIMEnabled")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("CountRetrieverClass")
                        .value("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UserIDEnabled")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("GroupIDEnabled")
                        .value("true"));

        userStoreId = userStoreMgtRestClient.addUserStore(userStore);
        Thread.sleep(5000);
        boolean isSecondaryUserStoreDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(SECONDARY_DOMAIN_ID);
        Assert.assertTrue(isSecondaryUserStoreDeployed, "Secondary user store deployment failed.");
    }

    private PatchOperationRequestObject createEmailUpdatePatchRequest(String newEmail) {

        UserItemAddGroupobj updateEmailPatchOp = new UserItemAddGroupobj()
                .op(UserItemAddGroupobj.OpEnum.REPLACE)
                .path("emails")
                .value(Collections.singletonList(newEmail));
        return new PatchOperationRequestObject().addOperations(updateEmailPatchOp);
    }
}
