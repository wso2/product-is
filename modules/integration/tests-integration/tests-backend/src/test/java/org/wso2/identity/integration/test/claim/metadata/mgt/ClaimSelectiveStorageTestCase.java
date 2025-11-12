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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.PropertyDTO;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ClaimDialectReqDTO;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test class for testing selective storage of local claims.
 * This test creates custom claims with SCIM mappings and validates claim property updates.
 */
public class ClaimSelectiveStorageTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(ClaimSelectiveStorageTestCase.class);

    private ClaimManagementRestClient claimManagementRestClient;
    private SCIM2RestClient scim2RestClient;
    private UserStoreMgtRestClient userStoreMgtRestClient;

    private static final String LOCAL_CLAIM_URI_PREFIX = "http://wso2.org/claims/";
    private static final String CUSTOM_CLAIM_1 = "custom1";
    private static final String CUSTOM_CLAIM_2 = "custom2";
    private static final String CUSTOM_CLAIM_3 = "custom3";

    private static final String EXISTING_IDENTITY_CLAIM_1 = "identity/userSourceId";
    private static final String USER_SOURCE_ID_ATTRIBUTE = "userSourceId";
    private static final String EXISTING_IDENTITY_CLAIM_2 = "identity/preferredChannel";
    private static final String PREFERRED_CHANNEL_ATTRIBUTE = "preferredChannel";
    private static final String COUNTRY_CLAIM = "country";

    // SCIM attribute names for existing WSO2 claims.
    private static final String SCIM_USER_SOURCE_ID = "userSourceId";
    private static final String SCIM_PREFERRED_CHANNEL = "preferredChannel";
    private static final String SCIM_COUNTRY = "country";

    // Custom SCIM schema dialect URI for testing.
    private static final String CUSTOM_SCHEMA_URI = "urn:scim:schemas:extension:custom:User";
    // WSO2 SCIM schema dialect URI for identity claims.
    private static final String WSO2_SCHEMA_URI = "urn:scim:wso2:schema";

    // User store domains.
    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final String SEC1_DOMAIN = "SEC1DOMAIN";
    private static final String SECONDARY_USER_STORE_TYPE_ID = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String SECONDARY_DB_NAME = "JDBC_SELECTIVE_STORAGE_DB";

    // Storage for claim IDs (only for custom claims we create).
    private String customClaim1Id;
    private String customClaim2Id;
    private String customClaim3Id;

    // Storage for external claim IDs (custom claims).
    private String scimCustomClaim1Id;
    private String scimCustomClaim2Id;
    private String scimCustomClaim3Id;

    // Custom schema dialect ID (will be created and deleted during test).
    private String customSchemaDialectId;

    // Test user constants.
    private static final String TEST_USER_1_USERNAME = "selectiveuser1";
    private static final String TEST_USER_2_USERNAME = "selectiveuser2";
    private static final String TEST_USER_3_USERNAME = "selectiveuser3";
    private static final String TEST_USER_PASSWORD = "Test@123";
    private static final String TEST_USER_EMAIL_DOMAIN = "@test.com";

    // Storage for user IDs (primary user store).
    private String user1Id;
    private String user2Id;
    private String user3Id;

    // Storage for secondary user store.
    private String sec1UserStoreDomainId;
    private String sec1User1Id;
    private String sec1User2Id;

    // Test user constants for secondary user store.
    private static final String SECONDARY_TEST_USER_1_USERNAME = "sec1user1";
    private static final String SECONDARY_TEST_USER_2_USERNAME = "sec1user2";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);

        // Get or create the custom schema dialect for testing.
        customSchemaDialectId = getDialectIdByURI(CUSTOM_SCHEMA_URI);
        Assert.assertNotNull(customSchemaDialectId, "Custom schema dialect should be created");
        log.info("Custom schema dialect ID: " + customSchemaDialectId);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        log.info("Starting cleanup...");

        // Delete secondary user store users first.
        try {
            if (sec1User2Id != null) {
                scim2RestClient.deleteUser(sec1User2Id);
                log.info("Deleted secondary test user 2");
            }
        } catch (Exception e) {
            log.error("Failed to delete secondary test user 2", e);
        }

        try {
            if (sec1User1Id != null) {
                scim2RestClient.deleteUser(sec1User1Id);
                log.info("Deleted secondary test user 1");
            }
        } catch (Exception e) {
            log.error("Failed to delete secondary test user 1", e);
        }

        // Delete primary user store users.
        try {
            if (user3Id != null) {
                scim2RestClient.deleteUser(user3Id);
                log.info("Deleted test user 3");
            }
        } catch (Exception e) {
            log.error("Failed to delete test user 3", e);
        }

        try {
            if (user2Id != null) {
                scim2RestClient.deleteUser(user2Id);
                log.info("Deleted test user 2");
            }
        } catch (Exception e) {
            log.error("Failed to delete test user 2", e);
        }

        try {
            if (user1Id != null) {
                scim2RestClient.deleteUser(user1Id);
                log.info("Deleted test user 1");
            }
        } catch (Exception e) {
            log.error("Failed to delete test user 1", e);
        }

        // Delete secondary user store.
        try {
            if (sec1UserStoreDomainId != null) {
                userStoreMgtRestClient.deleteUserStore(sec1UserStoreDomainId);
                userStoreMgtRestClient.waitForUserStoreUnDeployment(sec1UserStoreDomainId);
                log.info("Deleted secondary user store: " + SEC1_DOMAIN);

                // Clean up the H2 database files to prevent locks.
                try {
                    String dbPath = ServerConfigurationManager.getCarbonHome() + "/repository/database/"
                            + SECONDARY_DB_NAME;
                    File dbFile = new File(dbPath + ".mv.db");
                    if (dbFile.exists()) {
                        Thread.sleep(1000); // Give time for connections to close
                        if (dbFile.delete()) {
                            log.info("Deleted H2 database file: " + dbFile.getName());
                        }
                    }
                    File dbTraceFile = new File(dbPath + ".trace.db");
                    if (dbTraceFile.exists() && dbTraceFile.delete()) {
                        log.info("Deleted H2 trace file");
                    }
                } catch (Exception dbCleanupEx) {
                    log.warn("Could not clean up H2 database files: " + dbCleanupEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete secondary user store", e);
        }

        // Delete external SCIM claims from custom schema dialect (due to dependencies).
        try {
            if (scimCustomClaim3Id != null && customSchemaDialectId != null) {
                claimManagementRestClient.deleteExternalClaim(customSchemaDialectId, scimCustomClaim3Id);
                log.info("Deleted SCIM external claim: custom3");
            }
        } catch (Exception e) {
            log.error("Failed to delete SCIM external claim custom3", e);
        }

        try {
            if (scimCustomClaim2Id != null && customSchemaDialectId != null) {
                claimManagementRestClient.deleteExternalClaim(customSchemaDialectId, scimCustomClaim2Id);
                log.info("Deleted SCIM external claim: custom2");
            }
        } catch (Exception e) {
            log.error("Failed to delete SCIM external claim custom2", e);
        }

        try {
            if (scimCustomClaim1Id != null && customSchemaDialectId != null) {
                claimManagementRestClient.deleteExternalClaim(customSchemaDialectId, scimCustomClaim1Id);
                log.info("Deleted SCIM external claim: custom1");
            }
        } catch (Exception e) {
            log.error("Failed to delete SCIM external claim custom1", e);
        }

        // Delete the custom schema dialect.
        try {
            if (customSchemaDialectId != null) {
                claimManagementRestClient.deleteExternalDialect(customSchemaDialectId);
                log.info("Deleted custom schema dialect: " + CUSTOM_SCHEMA_URI);
            }
        } catch (Exception e) {
            log.error("Failed to delete custom schema dialect", e);
        }

        // Delete local claims.
        try {
            if (customClaim3Id != null) {
                claimManagementRestClient.deleteLocalClaim(customClaim3Id);
                log.info("Deleted local claim: custom3");
            }
        } catch (Exception e) {
            log.error("Failed to delete local claim custom3", e);
        }

        try {
            if (customClaim2Id != null) {
                claimManagementRestClient.deleteLocalClaim(customClaim2Id);
                log.info("Deleted local claim: custom2");
            }
        } catch (Exception e) {
            log.error("Failed to delete local claim custom2", e);
        }

        try {
            if (customClaim1Id != null) {
                claimManagementRestClient.deleteLocalClaim(customClaim1Id);
                log.info("Deleted local claim: custom1");
            }
        } catch (Exception e) {
            log.error("Failed to delete local claim custom1", e);
        }

        try {
            claimManagementRestClient.closeHttpClient();
            scim2RestClient.closeHttpClient();
            userStoreMgtRestClient.closeHttpClient();
            log.info("HTTP clients closed successfully");
        } catch (Exception e) {
            log.error("Failed to close HTTP clients", e);
        }
    }

    @Test(groups = "wso2.is", priority = 1, description = "Create custom claims")
    public void testCreateCustomClaims() throws Exception {

        log.info("Creating custom local claims");

        // Create custom claims (non-identity claims).
        customClaim1Id = createAndVerifyLocalClaim(CUSTOM_CLAIM_1, "Custom Claim 1",
                "This is custom claim 1 for testing selective storage", "string");

        customClaim2Id = createAndVerifyLocalClaim(CUSTOM_CLAIM_2, "Custom Claim 2",
                "This is custom claim 2 for testing selective storage", "string");

        customClaim3Id = createAndVerifyLocalClaim(CUSTOM_CLAIM_3, "Custom Claim 3",
                "This is custom claim 3 for testing selective storage", "string");
        
        log.info("Successfully created all custom local claims");

        log.info("Creating SCIM2 external claim mappings for custom claims in custom schema.");

        scimCustomClaim1Id = createExternalClaimMapping(CUSTOM_CLAIM_1, customSchemaDialectId, CUSTOM_SCHEMA_URI);
        scimCustomClaim2Id = createExternalClaimMapping(CUSTOM_CLAIM_2, customSchemaDialectId, CUSTOM_SCHEMA_URI);
        scimCustomClaim3Id = createExternalClaimMapping(CUSTOM_CLAIM_3, customSchemaDialectId, CUSTOM_SCHEMA_URI);

        log.info("Successfully created all SCIM2 external claim mappings for custom claims");
    }

    @Test(groups = "wso2.is", priority = 2, dependsOnMethods = {"testCreateCustomClaims"},
            description = "Verify default managedInUserStore is true for non-identity custom claims")
    public void testVerifyDefaultManagedInUserStore() throws Exception {

        log.info("Verifying default managedInUserStore value for custom claims");

        // Verify that custom claims (non-identity claims) have managedInUserStore = true by default.
        verifyManagedInUserStore(customClaim1Id, true);
        verifyManagedInUserStore(customClaim2Id, true);
        verifyManagedInUserStore(customClaim3Id, true);

        // Verify no ExcludedUserStores property is set by default.
        verifyExcludedUserStores(customClaim1Id, StringUtils.EMPTY);
        verifyExcludedUserStores(customClaim2Id, StringUtils.EMPTY);
        verifyExcludedUserStores(customClaim3Id, StringUtils.EMPTY);

        log.info("Successfully verified default managedInUserStore=true for all custom claims");
    }

    @Test(groups = "wso2.is", priority = 6, dependsOnMethods = {"testVerifyDefaultManagedInUserStore"},
            description = "Update claims to final configuration with managedInUserStore and ExcludedUserStores")
    public void testUpdateClaimsToFinalConfiguration() throws Exception {

        log.info("Updating claims to final selective storage configuration");

        // Get the country claim ID (existing claim).
        String countryClaimUri = LOCAL_CLAIM_URI_PREFIX + COUNTRY_CLAIM;
        String countryClaimId = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(countryClaimUri.getBytes());

        // Get existing identity claim IDs.
        String identityClaim1Uri = LOCAL_CLAIM_URI_PREFIX + EXISTING_IDENTITY_CLAIM_1;
        String identityClaim1Id = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(identityClaim1Uri.getBytes());

        String identityClaim2Uri = LOCAL_CLAIM_URI_PREFIX + EXISTING_IDENTITY_CLAIM_2;
        String identityClaim2Id = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(identityClaim2Uri.getBytes());

        // Final configuration updates.

        // COUNTRY: managedInUserStore = true (already true, but let's be explicit).
        updateClaimStorageProperties(countryClaimId, true, null);
        log.info("Updated COUNTRY claim with managedInUserStore=true");

        // CUSTOM_CLAIM_1: managedInUserStore = true, ExcludedUserStores = "PRIMARY".
        updateClaimStorageProperties(customClaim1Id, true, PRIMARY_DOMAIN);
        log.info("Updated CUSTOM_CLAIM_1 with managedInUserStore=true, ExcludedUserStores=PRIMARY");

        // CUSTOM_CLAIM_2: managedInUserStore = false.
        updateClaimStorageProperties(customClaim2Id, false, null);
        log.info("Updated CUSTOM_CLAIM_2 with managedInUserStore=false");

        // CUSTOM_CLAIM_3: managedInUserStore = true, ExcludedUserStores = "SEC1DOMAIN".
        updateClaimStorageProperties(customClaim3Id, true, SEC1_DOMAIN);
        log.info("Updated CUSTOM_CLAIM_3 with managedInUserStore=true, ExcludedUserStores=SEC1DOMAIN");

        // EXISTING_IDENTITY_CLAIM_1: managedInUserStore = false.
        updateClaimStorageProperties(identityClaim1Id, false, null);
        log.info("Updated EXISTING_IDENTITY_CLAIM_1 with managedInUserStore=false");

        // EXISTING_IDENTITY_CLAIM_2: managedInUserStore = true, ExcludedUserStores = "SEC1DOMAIN".
        updateClaimStorageProperties(identityClaim2Id, true, SEC1_DOMAIN);
        log.info("Updated EXISTING_IDENTITY_CLAIM_2 with managedInUserStore=true, ExcludedUserStores=SEC1DOMAIN");

        log.info("Successfully updated all claims to final configuration");

        // Verify all updates.
        log.info("Verifying final claim configurations");

        verifyManagedInUserStore(countryClaimId, true);
        verifyExcludedUserStores(countryClaimId, StringUtils.EMPTY);

        verifyManagedInUserStore(customClaim1Id, true);
        verifyExcludedUserStores(customClaim1Id, PRIMARY_DOMAIN);

        verifyManagedInUserStore(customClaim2Id, false);
        verifyExcludedUserStores(customClaim2Id, null);

        verifyManagedInUserStore(customClaim3Id, true);
        verifyExcludedUserStores(customClaim3Id, SEC1_DOMAIN);

        verifyManagedInUserStore(identityClaim1Id, false);
        verifyExcludedUserStores(identityClaim1Id, null);

        verifyManagedInUserStore(identityClaim2Id, true);
        verifyExcludedUserStores(identityClaim2Id, SEC1_DOMAIN);

        log.info("Successfully verified all final claim configurations");
    }

    @Test(groups = "wso2.is", priority = 9, dependsOnMethods = {"testUpdateClaimsToFinalConfiguration"},
            description = "Create secondary user store for selective storage testing")
    public void testCreateSecondaryUserStore() throws Exception {

        log.info("Creating secondary user store: " + SEC1_DOMAIN);

        // Create H2 database for secondary user store.
        log.info("Creating H2 database: " + SECONDARY_DB_NAME);
        H2DataBaseManager dbManager = new H2DataBaseManager(
                "jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/"
                        + SECONDARY_DB_NAME,
                "wso2automation",
                "wso2automation");
        dbManager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbManager.disconnect();
        log.info("H2 database created successfully");

        // Build user store request.
        UserStoreReq userStoreReq = buildSecondaryUserStoreReq();

        // Create the user store.
        sec1UserStoreDomainId = userStoreMgtRestClient.addUserStore(userStoreReq);
        Assert.assertNotNull(sec1UserStoreDomainId, "Secondary user store ID should not be null");
        log.info("User store creation initiated with ID: " + sec1UserStoreDomainId);

        // Wait for user store deployment.
        boolean isDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(SEC1_DOMAIN);
        Assert.assertTrue(isDeployed, "Secondary user store deployment failed");
        log.info("Successfully created and deployed secondary user store: " + SEC1_DOMAIN);
    }

    @Test(groups = "wso2.is", priority = 10, dependsOnMethods = {"testUpdateClaimsToFinalConfiguration"},
            description = "Create users in primary user store with custom claims and existing WSO2 claims")
    public void testCreateUsersWithCustomClaims() throws Exception {

        log.info("Creating users in primary user store with custom claims and existing WSO2 claims");

        // Create user1.
        Map<String, String> customClaims1 = new HashMap<>();
        customClaims1.put(CUSTOM_CLAIM_1, "custom1_value_user1");
        Map<String, Object> wso2Claims1 = new HashMap<>();
        wso2Claims1.put(SCIM_USER_SOURCE_ID, "user1_source");
        wso2Claims1.put(SCIM_PREFERRED_CHANNEL, "EMAIL");
        wso2Claims1.put(SCIM_COUNTRY, "United States");
        user1Id = createUserFromJsonTemplate(TEST_USER_1_USERNAME, TEST_USER_PASSWORD,
                TEST_USER_1_USERNAME + TEST_USER_EMAIL_DOMAIN, customClaims1, wso2Claims1);
        log.info("Created user1 with ID: " + user1Id);

        // Create user2.
        Map<String, String> customClaims2 = new HashMap<>();
        customClaims2.put(CUSTOM_CLAIM_2, "custom2_value_user2");
        Map<String, Object> wso2Claims2 = new HashMap<>();
        wso2Claims2.put(SCIM_PREFERRED_CHANNEL, "EMAIL");
        wso2Claims2.put(SCIM_COUNTRY, "Sri Lanka");
        user2Id = createUserFromJsonTemplate(TEST_USER_2_USERNAME, TEST_USER_PASSWORD,
                TEST_USER_2_USERNAME + TEST_USER_EMAIL_DOMAIN, customClaims2, wso2Claims2);
        log.info("Created user2 with ID: " + user2Id);

        // Create user3.
        Map<String, String> customClaims3 = new HashMap<>();
        customClaims3.put(CUSTOM_CLAIM_3, "custom3_value_user3");
        Map<String, Object> wso2Claims3 = new HashMap<>();
        wso2Claims3.put(SCIM_PREFERRED_CHANNEL, "SMS");
        wso2Claims3.put(SCIM_COUNTRY, "United States");
        user3Id = createUserFromJsonTemplate(TEST_USER_3_USERNAME, TEST_USER_PASSWORD,
                TEST_USER_3_USERNAME + TEST_USER_EMAIL_DOMAIN, customClaims3, wso2Claims3);
        log.info("Created user3 with ID: " + user3Id);

        log.info("Successfully created all users with custom claims and existing WSO2 claims");
    }

    @Test(groups = "wso2.is", priority = 13, dependsOnMethods = {"testCreateSecondaryUserStore"},
            description = "Create users in secondary user store with custom claims and existing WSO2 claims")
    public void testCreateSecondaryUsers() throws Exception {

        log.info("Creating users in secondary user store with custom claims and existing WSO2 claims");

        // Create secondary user1 with custom claims and EMAIL channel.
        String qualifiedUsername1 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_1_USERNAME;
        Map<String, String> customClaims1 = new HashMap<>();
        customClaims1.put(CUSTOM_CLAIM_1, "secondary_custom1_value");
        customClaims1.put(CUSTOM_CLAIM_2, "secondary_custom2_value");
        customClaims1.put(CUSTOM_CLAIM_3, "custom3_value_user3");
        Map<String, Object> wso2Claims1 = new HashMap<>();
        wso2Claims1.put(SCIM_USER_SOURCE_ID, "secondary_user1_source");
        wso2Claims1.put(SCIM_PREFERRED_CHANNEL, "EMAIL");
        wso2Claims1.put(SCIM_COUNTRY, "United States");
        sec1User1Id = createUserFromJsonTemplate(qualifiedUsername1, TEST_USER_PASSWORD,
                SECONDARY_TEST_USER_1_USERNAME + TEST_USER_EMAIL_DOMAIN, customClaims1, wso2Claims1);
        Assert.assertNotNull(sec1User1Id, "Secondary user1 ID should not be null");
        log.info("Created secondary user1 with ID: " + sec1User1Id);

        // Create secondary user2 with custom2 claim and SMS channel.
        String qualifiedUsername2 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_2_USERNAME;
        Map<String, String> customClaims2 = new HashMap<>();
        customClaims2.put(CUSTOM_CLAIM_2, "secondary_custom2_value");
        Map<String, Object> wso2Claims2 = new HashMap<>();
        wso2Claims2.put(SCIM_PREFERRED_CHANNEL, "SMS");
        wso2Claims2.put(SCIM_COUNTRY, "Sri Lanka");
        sec1User2Id = createUserFromJsonTemplate(qualifiedUsername2, TEST_USER_PASSWORD,
                SECONDARY_TEST_USER_2_USERNAME + TEST_USER_EMAIL_DOMAIN, customClaims2, wso2Claims2);
        Assert.assertNotNull(sec1User2Id, "Secondary user2 ID should not be null");
        log.info("Created secondary user2 with ID: " + sec1User2Id);

        log.info("Successfully created all users in secondary user store with custom claims and existing WSO2 claims");
    }

    @Test(groups = "wso2.is", priority = 15, dependsOnMethods = {"testCreateUsersWithCustomClaims"},
            description = "Test PATCH replace operation to add custom2 claim to user1")
    public void testPatchAddCustomClaimToUser() throws Exception {

        log.info("Testing PATCH replace operation to add custom2 to user1");

        // Build PATCH request to add custom2 claim using REPLACE.
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj replaceOperation = new UserItemAddGroupobj();
        replaceOperation.setOp(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceOperation.setPath(CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2);
        replaceOperation.setValue("custom2_added_to_user1");
        patchRequest.addOperations(replaceOperation);

        // Update user1 and get response.
        JSONObject updateResponse = scim2RestClient.updateUserAndReturnResponse(patchRequest, user1Id);
        log.info("Successfully executed PATCH replace operation to add custom2");

        // Verify the claim was added in the response.
        JSONObject customSchema = (JSONObject) updateResponse.get(CUSTOM_SCHEMA_URI);
        Assert.assertNotNull(customSchema, "Custom schema should be present in response");
        Assert.assertEquals(customSchema.get(CUSTOM_CLAIM_2), "custom2_added_to_user1");
        // Verify original claim is still there.
        Assert.assertEquals(customSchema.get(CUSTOM_CLAIM_1), "custom1_value_user1");
        log.info("Verified PATCH replace operation to add custom2");
    }

    @Test(groups = "wso2.is", priority = 16, dependsOnMethods = {"testCreateUsersWithCustomClaims"},
            description = "Test PATCH replace operation to update custom2 claim value")
    public void testPatchReplaceCustomClaim() throws Exception {

        log.info("Testing PATCH replace operation on user2 custom2 claim");

        // Build PATCH request to replace custom2 claim value.
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj replaceOperation = new UserItemAddGroupobj();
        replaceOperation.setOp(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceOperation.setPath(CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2);
        replaceOperation.setValue("custom2_value_replaced");
        patchRequest.addOperations(replaceOperation);

        // Update user2 and get response.
        JSONObject updateResponse = scim2RestClient.updateUserAndReturnResponse(patchRequest, user2Id);
        log.info("Successfully executed PATCH replace operation");

        // Verify the claim was updated in the response.
        JSONObject customSchema = (JSONObject) updateResponse.get(CUSTOM_SCHEMA_URI);
        Assert.assertNotNull(customSchema, "Custom schema should be present in response");
        Assert.assertEquals(customSchema.get(CUSTOM_CLAIM_2), "custom2_value_replaced");
        log.info("Verified PATCH replace operation");
    }

    @Test(groups = "wso2.is", priority = 17, dependsOnMethods = {"testCreateUsersWithCustomClaims"},
            description = "Test PATCH remove operation to remove custom3 claim")
    public void testPatchRemoveCustomClaim() throws Exception {

        log.info("Testing PATCH remove operation on user3 custom3 claim");

        // Build PATCH request to remove custom3 claim.
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj removeOperation = new UserItemAddGroupobj();
        removeOperation.setOp(UserItemAddGroupobj.OpEnum.REMOVE);
        removeOperation.setPath(CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_3);
        patchRequest.addOperations(removeOperation);

        // Update user3 and get response.
        JSONObject updateResponse = scim2RestClient.updateUserAndReturnResponse(patchRequest, user3Id);
        log.info("Successfully executed PATCH remove operation");

        // Verify the claim was removed in the response.
        JSONObject customSchema = (JSONObject) updateResponse.get(CUSTOM_SCHEMA_URI);
        if (customSchema != null) {
            // Claim should be removed or null.
            Object custom3Value = customSchema.get(CUSTOM_CLAIM_3);
            Assert.assertTrue(custom3Value == null || custom3Value.toString().isEmpty(),
                    "Custom3 claim should be removed in response");
        }
        log.info("Verified PATCH remove operation");
    }

    @Test(groups = "wso2.is", priority = 18, dependsOnMethods = {"testCreateUsersWithCustomClaims", "testCreateSecondaryUsers"},
            description = "Test filtering users by single custom claim and identity claims")
    public void testFilterUsersBySingleCustomClaim() throws Exception {

        log.info("Testing user filtering by single custom claim and identity claims");

        // Test 1: Filter by custom1 claim (should find user1).
        log.info("Test 1: Filtering by custom1 claim");
        String filter1 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_1 + " eq \"custom1_value_user1\"";
        filterAndVerifyUsers(filter1, 1, TEST_USER_1_USERNAME);
        log.info("Test 1 passed: Successfully filtered users by custom1 claim");

        // Test 2: Filter by custom2 claim (should find user2).
        log.info("Test 2: Filtering by custom2 claim");
        String filter2 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2 + " eq \"custom2_value_user2\"";
        filterAndVerifyUsers(filter2, 1, TEST_USER_2_USERNAME);
        log.info("Test 2 passed: Successfully filtered users by custom2 claim");

        // Test 3: Filter by custom3 claim - Cross-store test (should find user3 from primary and sec1User1 from secondary).
        log.info("Test 3: Filtering by custom3 claim across primary and secondary user stores");
        String filter3 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_3 + " eq \"custom3_value_user3\"";
        String qualifiedSecondaryUsername = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_1_USERNAME;
        filterAndVerifyUsers(filter3, 2, TEST_USER_3_USERNAME, qualifiedSecondaryUsername);
        log.info("Test 3 passed: Successfully filtered users by custom3 claim across stores");

        // Test 4: Filter by preferredChannel=EMAIL - Cross-store test (should find user1, user2, and sec1User1).
        log.info("Test 4: Filtering by preferredChannel=EMAIL across primary and secondary user stores");
        String filter4 = WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"EMAIL\"";
        filterAndVerifyUsers(filter4, 3, TEST_USER_1_USERNAME, TEST_USER_2_USERNAME, qualifiedSecondaryUsername);
        log.info("Test 4 passed: Successfully filtered users by preferredChannel=EMAIL across stores");

        // Test 5: Filter by preferredChannel=SMS - Cross-store test (should find user3 and sec1User2).
        log.info("Test 5: Filtering by preferredChannel=SMS across primary and secondary user stores");
        String filter5 = WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"SMS\"";
        String qualifiedSecondaryUsername2 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_2_USERNAME;
        filterAndVerifyUsers(filter5, 2, TEST_USER_3_USERNAME, qualifiedSecondaryUsername2);
        log.info("Test 5 passed: Successfully filtered users by preferredChannel=SMS across stores");

        log.info("All filtering tests passed successfully (including cross-store filtering)");
    }

    @Test(groups = "wso2.is", priority = 19, dependsOnMethods = {"testCreateUsersWithCustomClaims", "testPatchAddCustomClaimToUser", "testCreateSecondaryUsers"},
            description = "Test filtering users by multiple claims with AND operator (custom claims + identity claims)")
    public void testFilterUsersByMultipleClaims() throws Exception {

        log.info("Testing user filtering by multiple claims with AND operator");

        // Test 1: Filter by custom2 contains "custom2_added" AND preferredChannel eq "EMAIL" (should find user1 from primary).
        log.info("Test 1: Filtering by custom2 co 'custom2_added' AND preferredChannel eq 'EMAIL'");
        String filter1 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2 + " co \"custom2_added\" and " +
                WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"EMAIL\"";
        filterAndVerifyUsers(filter1, 1, TEST_USER_1_USERNAME);
        log.info("Test 1 passed: Successfully filtered by custom2 + preferredChannel");

        // Test 2: Filter by custom2 contains "secondary" AND preferredChannel eq "SMS".
        // (should find sec1User2 from secondary).
        log.info("Test 2: Filtering by custom2 co 'secondary' AND preferredChannel eq 'SMS'");
        String filter2 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2 + " co \"secondary\" and " +
                WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"SMS\"";
        String qualifiedSecondaryUsername2 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_2_USERNAME;
        filterAndVerifyUsers(filter2, 1, qualifiedSecondaryUsername2);
        log.info("Test 2 passed: Successfully filtered by custom2 + preferredChannel (secondary store)");

        // Test 3: Filter by custom3 eq "custom3_value_user3" AND preferredChannel eq "SMS" (should find user3 from primary).
        log.info("Test 3: Filtering by custom3 eq 'custom3_value_user3' AND preferredChannel eq 'SMS'");
        String filter3 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_3 + " eq \"custom3_value_user3\" and " +
                WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"SMS\"";
        filterAndVerifyUsers(filter3, 1, TEST_USER_3_USERNAME);
        log.info("Test 3 passed: Successfully filtered by custom3 + preferredChannel=SMS");

        // Test 4: Filter by custom3 eq "custom3_value_user3" AND preferredChannel eq "EMAIL" (should find sec1User1 from secondary).
        log.info("Test 4: Filtering by custom3 eq 'custom3_value_user3' AND preferredChannel eq 'EMAIL'");
        String filter4 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_3 + " eq \"custom3_value_user3\" and " +
                WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"EMAIL\"";
        String qualifiedSecondaryUsername1 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_1_USERNAME;
        filterAndVerifyUsers(filter4, 1, qualifiedSecondaryUsername1);
        log.info("Test 4 passed: Successfully filtered by custom3 + preferredChannel=EMAIL");

        // Test 5: Filter by custom2 contains "custom2" AND preferredChannel eq "EMAIL" (should find user1, user2, sec1User1).
        log.info("Test 5: Filtering by custom2 co 'custom2' AND preferredChannel eq 'EMAIL' (cross-store)");
        String filter5 = CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2 + " co \"custom2\" and " +
                WSO2_SCHEMA_URI + ":" + SCIM_PREFERRED_CHANNEL + " eq \"EMAIL\"";
        filterAndVerifyUsers(filter5, 3, TEST_USER_1_USERNAME, TEST_USER_2_USERNAME, qualifiedSecondaryUsername1);
        log.info("Test 5 passed: Successfully filtered by custom2 + preferredChannel=EMAIL across stores");

        log.info("All multiple claim filtering tests passed successfully");
    }

    @Test(groups = "wso2.is", priority = 20, dependsOnMethods = {"testCreateSecondaryUsers"},
            description = "Test PATCH replace operation to update custom claim on secondary user")
    public void testPatchAddToSecondaryUser() throws Exception {

        log.info("Testing PATCH replace operation on secondary user1 to update custom1 claim");

        // Build PATCH request to update custom1 claim using REPLACE.
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj replaceOperation = new UserItemAddGroupobj();
        replaceOperation.setOp(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceOperation.setPath(CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_1);
        replaceOperation.setValue("secondary_custom1_value_updated");
        patchRequest.addOperations(replaceOperation);

        // Update secondary user1 and get response.
        JSONObject updateResponse = scim2RestClient.updateUserAndReturnResponse(patchRequest, sec1User1Id);
        log.info("Successfully executed PATCH replace operation on secondary user1");

        // Verify the claim was updated in the response.
        JSONObject customSchema = (JSONObject) updateResponse.get(CUSTOM_SCHEMA_URI);
        Assert.assertNotNull(customSchema, "Custom schema should be present in response");
        Assert.assertEquals(customSchema.get(CUSTOM_CLAIM_1), "secondary_custom1_value_updated");
        log.info("Verified PATCH replace operation on secondary user1");
    }

    @Test(groups = "wso2.is", priority = 21, dependsOnMethods = {"testCreateSecondaryUsers"},
            description = "Test PATCH replace operation to update custom claim on secondary user")
    public void testPatchReplaceSecondaryUser() throws Exception {

        log.info("Testing PATCH replace operation on secondary user2 to update custom2 claim");

        // Update custom2 claim using REPLACE operation.
        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject();
        UserItemAddGroupobj replaceOperation = new UserItemAddGroupobj();
        replaceOperation.setOp(UserItemAddGroupobj.OpEnum.REPLACE);
        replaceOperation.setPath(CUSTOM_SCHEMA_URI + ":" + CUSTOM_CLAIM_2);
        replaceOperation.setValue("secondary_custom2_value_updated");
        patchRequest.addOperations(replaceOperation);

        JSONObject updateResponse = scim2RestClient.updateUserAndReturnResponse(patchRequest, sec1User2Id);
        log.info("Successfully executed PATCH replace operation on secondary user2");

        // Verify the claim was updated in the response.
        JSONObject customSchema = (JSONObject) updateResponse.get(CUSTOM_SCHEMA_URI);
        Assert.assertNotNull(customSchema, "Custom schema should be present in response");
        Assert.assertEquals(customSchema.get(CUSTOM_CLAIM_2), "secondary_custom2_value_updated");
        log.info("Verified PATCH replace operation on secondary user2");
    }

    @Test(groups = "wso2.is", priority = 22, dependsOnMethods = {"testCreateSecondaryUsers"},
            description = "Test filtering users from secondary user store")
    public void testFilterSecondaryUserStoreClaims() throws Exception {

        log.info("Testing user filtering for secondary user store");

        // Filter by username to get secondary users.
        String qualifiedUsername1 = SEC1_DOMAIN + "/" + SECONDARY_TEST_USER_1_USERNAME;
        String filter = "userName eq \"" + qualifiedUsername1 + "\"";

        filterAndVerifyUsers(filter, 1, qualifiedUsername1);

        log.info("Successfully filtered users from secondary user store");
    }

    /**
     * Create a user from JSON template with custom claims and existing WSO2 schema claims.
     *
     * @param username     Username for the user.
     * @param password     Password for the user.
     * @param email        Email address for the user.
     * @param customClaims Map of custom claim names to values.
     * @param wso2Claims   Map of existing WSO2 SCIM attribute names to values
     *                     (e.g., userSourceId, preferredChannel, country).
     * @return User ID of the created user.
     * @throws Exception If user creation fails.
     */
    private String createUserFromJsonTemplate(String username, String password, String email,
            Map<String, String> customClaims, Map<String, Object> wso2Claims) throws Exception {

        // Read and modify JSON template.
        JSONObject userJson = readAndModifyUserTemplate(username, password, email, customClaims, wso2Claims);

        // Convert JSON to string for SCIM2RestClient.
        String jsonRequest = userJson.toJSONString();
        log.info("Creating user with JSON: " + jsonRequest);

        // Create user using raw JSON.
        String userId = scim2RestClient.createUserWithRawJSON(jsonRequest);
        Assert.assertNotNull(userId, "User ID should not be null for " + username);

        // Verify user was created with custom claims.
        JSONObject createdUser = scim2RestClient.getUser(userId, null);
        Assert.assertEquals(createdUser.get("userName"), username);

        // Verify custom claims if provided.
        if (customClaims != null && !customClaims.isEmpty()) {
            JSONObject customSchema = (JSONObject) createdUser.get(CUSTOM_SCHEMA_URI);
            Assert.assertNotNull(customSchema, "Custom schema should be present in created user");
            for (Map.Entry<String, String> entry : customClaims.entrySet()) {
                Assert.assertEquals(customSchema.get(entry.getKey()), entry.getValue(),
                        "Custom claim " + entry.getKey() + " should match expected value");
            }
        }

        // Verify WSO2 schema claims if provided.
        if (wso2Claims != null && !wso2Claims.isEmpty()) {
            JSONObject wso2Schema = (JSONObject) createdUser.get(WSO2_SCHEMA_URI);
            Assert.assertNotNull(wso2Schema, "WSO2 schema should be present in created user");
            for (Map.Entry<String, Object> entry : wso2Claims.entrySet()) {
                Assert.assertEquals(wso2Schema.get(entry.getKey()), entry.getValue(),
                        "WSO2 claim " + entry.getKey() + " should match expected value");
            }
        }

        return userId;
    }

    /**
     * Read the user creation JSON template and modify it with provided values.
     *
     * @param username     Username for the user.
     * @param password     Password for the user.
     * @param email        Email address for the user.
     * @param customClaims Map of custom claim names to values.
     * @param wso2Claims   Map of existing WSO2 SCIM attribute names to values.
     * @return Modified JSON object.
     * @throws Exception If reading or parsing fails.
     */
    private JSONObject readAndModifyUserTemplate(String username, String password, String email,
            Map<String, String> customClaims, Map<String, Object> wso2Claims) throws Exception {

        // Read JSON template from resources.
        String templateContent = readResource("claim-selective-storage-test-user.json");
        JSONParser parser = new JSONParser();
        JSONObject userJson = (JSONObject) parser.parse(templateContent);

        // Update basic user info.
        userJson.put("userName", username);
        userJson.put("password", password);

        // Update email.
        JSONArray emails = (JSONArray) userJson.get("emails");
        if (emails != null && !emails.isEmpty()) {
            JSONObject emailObj = (JSONObject) emails.get(0);
            emailObj.put("value", email);
        }

        // Update custom claims.
        if (customClaims != null && !customClaims.isEmpty()) {
            JSONObject customSchemaObj = new JSONObject();
            for (Map.Entry<String, String> entry : customClaims.entrySet()) {
                customSchemaObj.put(entry.getKey(), entry.getValue());
            }
            userJson.put(CUSTOM_SCHEMA_URI, customSchemaObj);
        }

        // Update existing WSO2 schema claims.
        if (wso2Claims != null && !wso2Claims.isEmpty()) {
            JSONObject wso2SchemaObj = new JSONObject();
            for (Map.Entry<String, Object> entry : wso2Claims.entrySet()) {
                wso2SchemaObj.put(entry.getKey(), entry.getValue());
            }
            userJson.put(WSO2_SCHEMA_URI, wso2SchemaObj);
        }

        return userJson;
    }

    /**
     * Read a resource file from the classpath.
     *
     * @param fileName Name of the resource file (relative to the test resources directory).
     * @return File content as string.
     * @throws IOException If file reading fails.
     */
    private String readResource(String fileName) throws IOException {

        InputStream resourceAsStream = getClass().getResourceAsStream("/" + fileName);
        if (resourceAsStream == null) {
            throw new IOException("Resource file not found: " + fileName);
        }

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            byte[] buffer = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }

    /**
     * Build a LocalClaimReq object with the given parameters.
     *
     * @param claimName   Claim name (will be appended to the claim URI prefix).
     * @param displayName Display name for the claim.
     * @param description Description for the claim.
     * @param dataType    Data type of the claim (string, boolean, integer, etc.).
     * @return LocalClaimReq object.
     */
    private LocalClaimReq buildLocalClaimReq(String claimName, String displayName, String description,
            String dataType) {

        LocalClaimReq claimReq = new LocalClaimReq();
        claimReq.setClaimURI(LOCAL_CLAIM_URI_PREFIX + claimName);
        claimReq.setDisplayName(displayName);
        claimReq.setDescription(description);
        claimReq.setDataType(dataType);
        claimReq.setSupportedByDefault(true);
        claimReq.setRequired(false);
        claimReq.setReadOnly(false);

        // Create attribute mapping for PRIMARY userstore.
        // For identity claims (identity/*), use only the part after "identity/".
        String mappedAttributeName = claimName.contains("/") ?
                claimName.substring(claimName.lastIndexOf("/") + 1) : claimName;
        AttributeMappingDTO mapping = new AttributeMappingDTO();
        mapping.setMappedAttribute(mappedAttributeName);
        mapping.setUserstore(PRIMARY_DOMAIN);
        claimReq.setAttributeMapping(Collections.singletonList(mapping));

        return claimReq;
    }

    /**
     * Create a local claim and verify it was created successfully.
     *
     * @param claimName   Claim name (will be appended to URI prefix).
     * @param displayName Display name for the claim.
     * @param description Description for the claim.
     * @param dataType    Data type of the claim.
     * @return The created claim ID.
     * @throws Exception If claim creation fails.
     */
    private String createAndVerifyLocalClaim(String claimName, String displayName,
            String description, String dataType) throws Exception {

        log.info("Creating claim: " + claimName);

        LocalClaimReq claimReq = buildLocalClaimReq(claimName, displayName, description, dataType);
        String claimId = claimManagementRestClient.addLocalClaim(claimReq);
        Assert.assertNotNull(claimId, claimName + " ID should not be null");
        log.info("Successfully created " + claimName + " with ID: " + claimId);

        // Verify the claim was created.
        JSONObject retrievedClaim = claimManagementRestClient.getLocalClaim(claimId);
        Assert.assertEquals(retrievedClaim.get("claimURI"), LOCAL_CLAIM_URI_PREFIX + claimName);
        Assert.assertEquals(retrievedClaim.get("displayName"), displayName);
        // TODO: Add more property verifications - managedInUserStore and excluded user stores.

        return claimId;
    }

    /**
     * Create an external SCIM claim mapping to a local claim.
     *
     * @param claimName   The local claim name to map.
     * @param dialectId   The encoded dialect ID.
     * @param dialectURI  The dialect URI.
     * @return The created external claim ID.
     * @throws Exception If external claim creation fails.
     */
    private String createExternalClaimMapping(String claimName, String dialectId, String dialectURI) throws Exception {

        log.info("Creating SCIM external claim mapping for: " + claimName);
        log.info("Using dialect ID: " + dialectId);
        log.info("Using dialect URI: " + dialectURI);

        ExternalClaimReq externalClaim = new ExternalClaimReq();
        String externalClaimURI = dialectURI + ":" + claimName;
        externalClaim.setClaimURI(externalClaimURI);
        externalClaim.setMappedLocalClaimURI(LOCAL_CLAIM_URI_PREFIX + claimName);

        log.info("External claim URI: " + externalClaimURI);
        log.info("Mapped to local claim: " + LOCAL_CLAIM_URI_PREFIX + claimName);

        String externalClaimId = null;
        try {
            externalClaimId = claimManagementRestClient.addExternalClaim(dialectId, externalClaim);
        } catch (Exception e) {
            log.error("Failed to create external claim mapping for " + claimName, e);
            throw new Exception("External claim creation failed for " + claimName +
                    ". Dialect ID: " + dialectId + ". Error: " + e.getMessage(), e);
        }

        Assert.assertNotNull(externalClaimId,
                "SCIM external claim for " + claimName + " should not be null");
        log.info("Successfully created SCIM external claim mapping for " + claimName +
                " with ID: " + externalClaimId);

        return externalClaimId;
    }

    /**
     * Convert JSONArray of attribute mappings to List of AttributeMappingDTO.
     *
     * @param mappingsArray JSONArray containing attribute mapping objects.
     * @return List of AttributeMappingDTO objects.
     */
    private List<AttributeMappingDTO> convertJsonArrayToAttributeMappings(
            org.json.simple.JSONArray mappingsArray) {

        List<AttributeMappingDTO> mappings = new ArrayList<>();
        for (Object obj : mappingsArray) {
            JSONObject mappingObj = (JSONObject) obj;
            AttributeMappingDTO mapping = new AttributeMappingDTO();
            mapping.setMappedAttribute((String) mappingObj.get("mappedAttribute"));
            mapping.setUserstore((String) mappingObj.get("userstore"));
            mappings.add(mapping);
        }
        return mappings;
    }

    /**
     * Get the dialect ID by dialect URI.
     * This method checks if the dialect exists, creates it if it doesn't, and returns the encoded dialect ID.
     *
     * @param dialectURI The dialect URI to search for.
     * @return The encoded dialect ID.
     * @throws Exception If an error occurs during the search or creation.
     */
    private String getDialectIdByURI(String dialectURI) throws Exception {

        // The dialect ID is the Base64 URL-encoded version of the URI.
        String encodedDialectId = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(dialectURI.getBytes());

        log.info("Checking if dialect exists: " + dialectURI);
        log.info("Encoded dialect ID: " + encodedDialectId);

        // Try to get the dialect to see if it exists.
        JSONObject dialectResponse = null;
        try {
            dialectResponse = claimManagementRestClient.getExternalDialect(encodedDialectId);
        } catch (Exception e) {
            log.info("Dialect not found or error occurred, will attempt to create it");
        }

        boolean dialectExists = isExistingDialect(dialectResponse);

        if (!dialectExists) {
            log.info("Dialect does not exist, creating it: " + dialectURI);
            ClaimDialectReqDTO claimDialectReqDTO = new ClaimDialectReqDTO();
            claimDialectReqDTO.setDialectURI(dialectURI);
            try {
                claimManagementRestClient.addExternalDialect(claimDialectReqDTO);
                log.info("Successfully created dialect: " + dialectURI);
            } catch (Exception e) {
                // Dialect might have been created by another process, continue.
                log.warn("Failed to create dialect: " + e.getMessage());
            }
        } else {
            log.info("Dialect already exists: " + dialectURI);
        }

        return encodedDialectId;
    }

    /**
     * Check if a dialect exists based on the response from getExternalDialect.
     *
     * @param dialectResponse The JSON response from getExternalDialect.
     * @return true if the dialect exists, false otherwise.
     */
    private boolean isExistingDialect(JSONObject dialectResponse) {

        if (dialectResponse == null) {
            return false;
        }

        // Check for error code CMT-50016 (dialect not found).
        if (dialectResponse.get("code") != null && "CMT-50016".equals(dialectResponse.get("code"))) {
            return false;
        }

        // If it has an ID, the dialect exists.
        return dialectResponse.get("id") != null;
    }

    /**
     * Build a secondary user store request for JDBC user store.
     *
     * @return UserStoreReq object configured for JDBC secondary user store.
     */
    private UserStoreReq buildSecondaryUserStoreReq() {

        UserStoreReq userStoreReq = new UserStoreReq();
        userStoreReq.setTypeId(SECONDARY_USER_STORE_TYPE_ID);
        userStoreReq.setName(SEC1_DOMAIN);
        userStoreReq.setDescription("JDBC secondary user store for selective storage testing");

        // Add user store properties.
        List<UserStoreReq.Property> properties = new ArrayList<>();

        properties.add(new UserStoreReq.Property()
                .name("driverName")
                .value("org.h2.Driver"));

        properties.add(new UserStoreReq.Property()
                .name("url")
                .value("jdbc:h2:./repository/database/" + SECONDARY_DB_NAME));

        properties.add(new UserStoreReq.Property()
                .name("userName")
                .value("wso2automation"));

        properties.add(new UserStoreReq.Property()
                .name("password")
                .value("wso2automation"));

        properties.add(new UserStoreReq.Property()
                .name("disabled")
                .value("false"));

        properties.add(new UserStoreReq.Property()
                .name("PasswordDigest")
                .value("SHA-256"));

        properties.add(new UserStoreReq.Property()
                .name("StoreSaltedPassword")
                .value("true"));

        properties.add(new UserStoreReq.Property()
                .name("SCIMEnabled")
                .value("true"));

        properties.add(new UserStoreReq.Property()
                .name("UserIDEnabled")
                .value("true"));

        properties.add(new UserStoreReq.Property()
                .name("GroupIDEnabled")
                .value("true"));

        userStoreReq.setProperties(properties);

        // Add claim attribute mappings for custom claims in secondary user store.
        List<UserStoreReq.ClaimAttributeMapping> claimMappings = new ArrayList<>();

        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + CUSTOM_CLAIM_1)
                .mappedAttribute(CUSTOM_CLAIM_1));

        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + CUSTOM_CLAIM_2)
                .mappedAttribute(CUSTOM_CLAIM_2));

        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + CUSTOM_CLAIM_3)
                .mappedAttribute(CUSTOM_CLAIM_3));

        // Add mappings for existing WSO2 claims used in the test.
        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + EXISTING_IDENTITY_CLAIM_1)
                .mappedAttribute(USER_SOURCE_ID_ATTRIBUTE));

        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + EXISTING_IDENTITY_CLAIM_2)
                .mappedAttribute(PREFERRED_CHANNEL_ATTRIBUTE));

        claimMappings.add(new UserStoreReq.ClaimAttributeMapping()
                .claimURI(LOCAL_CLAIM_URI_PREFIX + COUNTRY_CLAIM)
                .mappedAttribute(COUNTRY_CLAIM));

        userStoreReq.setClaimAttributeMappings(claimMappings);

        return userStoreReq;
    }

    /**
     * Helper method to filter users and verify the results contain expected users.
     *
     * @param filter            The SCIM filter string (will be URL encoded).
     * @param expectedCount     Expected number of results (exact count).
     * @param expectedUsernames Usernames that should be present in the results.
     * @throws Exception If filtering fails or expected users are not found.
     */
    private void filterAndVerifyUsers(String filter, int expectedCount, String... expectedUsernames)
            throws Exception {

        String encodedFilter = URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
        JSONObject result = scim2RestClient.filterUsers(encodedFilter);

        Assert.assertNotNull(result, "Filter result should not be null");

        Long totalResults = (Long) result.get("totalResults");
        Assert.assertEquals(totalResults.intValue(), expectedCount,
                "Expected " + expectedCount + " users but found " + totalResults);

        JSONArray resources = (JSONArray) result.get("Resources");
        Assert.assertNotNull(resources, "Resources should not be null");

        // Verify all expected users are present in the results.
        for (String expectedUsername : expectedUsernames) {
            boolean found = false;
            for (Object resource : resources) {
                JSONObject user = (JSONObject) resource;
                if (expectedUsername.equals(user.get("userName"))) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "Expected user '" + expectedUsername + "' not found in filter results");
        }
    }

    /**
     * Helper method to verify managedInUserStore value for a claim.
     *
     * @param claimId                     The claim ID to check.
     * @param expectedManagedInUserStore  Expected value of managedInUserStore.
     * @throws Exception If verification fails.
     */
    private void verifyManagedInUserStore(String claimId, Boolean expectedManagedInUserStore) throws Exception {

        JSONObject claim = claimManagementRestClient.getLocalClaim(claimId);
        Assert.assertNotNull(claim, "Claim should not be null");

        Object managedInUserStoreValue = claim.get("managedInUserStore");
        if (expectedManagedInUserStore == null) {
            Assert.assertNull(managedInUserStoreValue,
                    "managedInUserStore should be null but was: " + managedInUserStoreValue);
        } else {
            Assert.assertNotNull(managedInUserStoreValue,
                    "managedInUserStore should not be null");
            Assert.assertEquals(managedInUserStoreValue, expectedManagedInUserStore,
                    "managedInUserStore value mismatch");
        }
        log.info("Verified managedInUserStore for claim " + claimId + ": " + expectedManagedInUserStore);
    }

    /**
     * Helper method to verify ExcludedUserStores property for a claim.
     *
     * @param claimId                  The claim ID to check.
     * @param expectedExcludedStores   Expected comma-separated list of excluded stores (null if not set).
     * @throws Exception If verification fails.
     */
    private void verifyExcludedUserStores(String claimId, String expectedExcludedStores) throws Exception {

        JSONObject claim = claimManagementRestClient.getLocalClaim(claimId);
        Assert.assertNotNull(claim, "Claim should not be null");

        JSONArray properties = (JSONArray) claim.get("properties");
        String actualExcludedStores = null;

        if (properties != null) {
            for (Object propObj : properties) {
                JSONObject property = (JSONObject) propObj;
                if ("ExcludedUserStores".equals(property.get("key"))) {
                    actualExcludedStores = (String) property.get("value");
                    break;
                }
            }
        }

        if (expectedExcludedStores == null) {
            Assert.assertNull(actualExcludedStores,
                    "ExcludedUserStores should be null but was: " + actualExcludedStores);
        } else {
            Assert.assertEquals(actualExcludedStores, expectedExcludedStores,
                    "ExcludedUserStores value mismatch");
        }
        log.info("Verified ExcludedUserStores for claim " + claimId + ": " + expectedExcludedStores);
    }

    /**
     * Helper method to update a claim with managedInUserStore and optionally ExcludedUserStores.
     *
     * @param claimId                   The claim ID to update.
     * @param managedInUserStore        Value for managedInUserStore.
     * @param excludedUserStores        Comma-separated list of excluded user stores (null to not set).
     * @throws Exception If update fails.
     */
    private void updateClaimStorageProperties(String claimId, Boolean managedInUserStore,
            String excludedUserStores) throws Exception {

        // Get the existing claim.
        JSONObject existingClaim = claimManagementRestClient.getLocalClaim(claimId);

        // Create update request preserving existing values.
        LocalClaimReq updateReq = new LocalClaimReq();
        updateReq.setClaimURI((String) existingClaim.get("claimURI"));
        updateReq.setDisplayName((String) existingClaim.get("displayName"));
        updateReq.setDescription((String) existingClaim.get("description"));

        // Set other properties.
        if (existingClaim.get("displayOrder") != null) {
            updateReq.setDisplayOrder(((Long) existingClaim.get("displayOrder")).intValue());
        }
        updateReq.setReadOnly((Boolean) existingClaim.get("readOnly"));
        updateReq.setRequired((Boolean) existingClaim.get("required"));
        updateReq.setSupportedByDefault((Boolean) existingClaim.get("supportedByDefault"));
        updateReq.setDataType((String) existingClaim.get("dataType"));

        // Set managedInUserStore.
        updateReq.setManagedInUserStore(managedInUserStore);

        // Convert JSONArray to List for attribute mapping.
        updateReq.setAttributeMapping(convertJsonArrayToAttributeMappings(
                (JSONArray) existingClaim.get("attributeMapping")));

        // Handle properties (including ExcludedUserStores).
        List<PropertyDTO> properties = new ArrayList<>();

        // Copy existing properties except ExcludedUserStores.
        JSONArray existingProperties = (JSONArray) existingClaim.get("properties");
        if (existingProperties != null) {
            for (Object propObj : existingProperties) {
                JSONObject property = (JSONObject) propObj;
                String key = (String) property.get("key");
                if (!"ExcludedUserStores".equals(key)) {
                    PropertyDTO prop = new PropertyDTO();
                    prop.setKey(key);
                    prop.setValue((String) property.get("value"));
                    properties.add(prop);
                }
            }
        }

        // Add ExcludedUserStores if provided.
        if (excludedUserStores != null) {
            PropertyDTO excludedProp = new PropertyDTO();
            excludedProp.setKey("ExcludedUserStores");
            excludedProp.setValue(excludedUserStores);
            properties.add(excludedProp);
        }

        updateReq.setProperties(properties);

        // Update the claim.
        claimManagementRestClient.updateLocalClaim(claimId, updateReq);
        log.info("Updated claim " + claimId + " with managedInUserStore=" + managedInUserStore +
                ", ExcludedUserStores=" + excludedUserStores);
    }
}
