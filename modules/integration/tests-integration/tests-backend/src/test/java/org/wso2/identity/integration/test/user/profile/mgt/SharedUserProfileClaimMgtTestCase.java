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

package org.wso2.identity.integration.test.user.profile.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ClaimDialectReqDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;

import java.util.Collections;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;

/**
 * Test class to verify shared user profile claim management.
 */
public class SharedUserProfileClaimMgtTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis.json";
    private static final String CUSTOM_CLAIM_UPDATE_TO_RESOLVE_FROM_ORIGIN_JSON =
            "custom-claim-update-to-resolve-from-origin.json";
    private static final String CUSTOM_CLAIM_UPDATE_TO_RESOLVE_FROM_SHARED_PROFILE_JSON =
            "custom-claim-update-to-resolve-from-shared-profile.json";
    private static final String L1_SUB_ORG_NAME = "L1_Sub_Org";
    private static final String L2_SUB_ORG_NAME = "L2_Sub_Org";
    private static final String ROOT_ORG_USERNAME = "alex";
    private static final String ROOT_ORG_USER_PASSWORD = "Wso2@123";
    private static final String ROOT_ORG_USER_EMAIL = "alex@gmail.com";
    private static final String ROOT_ORG_USER_GIVEN_NAME = "Alex";
    private static final String SCIM2_CUSTOM_SCHEMA_DIALECT_URI = "urn:scim:schemas:extension:custom:User";
    private static final String SCIM2_SYSTEM_SCHEMA_DIALECT_URI = "urn:scim:wso2:schema";
    private static final String LOCAL_CLAIM_DIALECT = "local";
    private static final String ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI =
            "dXJuOnNjaW06c2NoZW1hczpleHRlbnNpb246Y3VzdG9tOlVzZXI";
    private static final String CUSTOM_CLAIM_URI = "http://wso2.org/claims/customAttribute1";
    private static final String CUSTOM_CLAIM_NAME = "customAttribute1";
    private final TestUserMode userMode;
    private ClaimManagementRestClient claimManagementRestClient;
    private SCIM2RestClient scim2RestClient;
    private UserSharingRestClient userSharingRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private String level1OrgId;
    private String level2OrgId;
    private String switchedM2MTokenForLevel1Org;
    private String switchedM2MTokenForLevel2Org;
    private String rootOrgUserId;
    private String sharedUserIdInLevel1Org;
    private String sharedUserIdInLevel2Org;
    private ApplicationResponseModel application;
    private String clientId;
    private String clientSecret;
    private String customClaimId;
    private String scimClaimIdOfCustomClaim;

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][] {
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public SharedUserProfileClaimMgtTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userSharingRestClient = new UserSharingRestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON, this.getClass())));
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        level1OrgId = orgMgtRestClient.addOrganization(L1_SUB_ORG_NAME);
        level2OrgId = orgMgtRestClient.addSubOrganization(L2_SUB_ORG_NAME, level1OrgId);

        switchedM2MTokenForLevel1Org = orgMgtRestClient.switchM2MToken(level1OrgId);
        switchedM2MTokenForLevel2Org = orgMgtRestClient.switchM2MToken(level2OrgId);

        // Create a custom claim setting FromFirstFoundInHierarchy as SharedProfileValueResolvingMethod.
        LocalClaimReq localClaimReq = buildLocalClaimReq(CUSTOM_CLAIM_URI, CUSTOM_CLAIM_NAME, CUSTOM_CLAIM_NAME,
                LocalClaimReq.SharedProfileValueResolvingMethodEnum.FromFirstFoundInHierarchy);
        customClaimId = claimManagementRestClient.addLocalClaim(localClaimReq);
        // Add external scim2 claim.
        ClaimDialectReqDTO claimDialectReqDTO = new ClaimDialectReqDTO();
        claimDialectReqDTO.setDialectURI(SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        org.json.simple.JSONObject externalDialect =
                claimManagementRestClient.getExternalDialect(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        boolean isExistingDialect = isExistingDialect(externalDialect);
        if (!isExistingDialect) {
            claimManagementRestClient.addExternalDialect(claimDialectReqDTO);
        }
        String externalClaimURI = SCIM2_CUSTOM_SCHEMA_DIALECT_URI + ":" + CUSTOM_CLAIM_NAME;
        ExternalClaimReq externalClaimReq = new ExternalClaimReq();
        externalClaimReq.setClaimURI(externalClaimURI);
        externalClaimReq.setMappedLocalClaimURI(CUSTOM_CLAIM_URI);
        scimClaimIdOfCustomClaim =
                claimManagementRestClient.addExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI, externalClaimReq);

        // Create a new application and share with all children.
        application = addApplication();
        String applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();
        shareApplication();
    }

    private boolean isExistingDialect(org.json.simple.JSONObject externalDialectGetResponse) {

        if (externalDialectGetResponse.get("code") != null &&
                externalDialectGetResponse.get("code").equals("CMT-50016")) {
            return false;
        }
        return externalDialectGetResponse.get("id") != null;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2RestClient.deleteUser(rootOrgUserId);
        orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
        orgMgtRestClient.deleteOrganization(level1OrgId);
        oAuth2RestClient.deleteApplication(application.getId());
        orgMgtRestClient.closeHttpClient();
        idpMgtServiceClient.deleteIdP("SSO");
        scim2RestClient.closeHttpClient();
        userSharingRestClient.closeHttpClient();
        claimManagementRestClient.deleteExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI,
                scimClaimIdOfCustomClaim);
        claimManagementRestClient.deleteExternalDialect(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        claimManagementRestClient.deleteLocalClaim(customClaimId);
        claimManagementRestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
    }

    @Test(description = "Add a user in root organization and share with level 1 org and level 2 org.")
    public void testAddUserInRootOrgAndShareWithLevel1AndLevel2Org() throws Exception {

        // Create a user in root organization.
        UserObject rootUserInfo = new UserObject();
        rootUserInfo.setUserName(ROOT_ORG_USERNAME);
        rootUserInfo.setPassword(ROOT_ORG_USER_PASSWORD);
        rootUserInfo.setName(new Name().givenName(ROOT_ORG_USER_GIVEN_NAME));
        rootUserInfo.addEmail(new Email().value(ROOT_ORG_USER_EMAIL));
        rootOrgUserId = scim2RestClient.createUser(rootUserInfo);

        // Share the user with existing sub orgs.
        UserShareWithAllRequestBody userShareWithAllRequestBody = new UserShareWithAllRequestBody();
        userShareWithAllRequestBody.setUserCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(rootOrgUserId));
        userShareWithAllRequestBody.setPolicy(ALL_EXISTING_ORGS_ONLY);
        userSharingRestClient.shareUsersWithAll(userShareWithAllRequestBody);

        String userSearchReq = new JSONObject()
                .put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:SearchRequest"))
                .put("attributes", new JSONArray().put("id"))
                .put("filter", "userName eq " + ROOT_ORG_USERNAME )
                .toString();
        boolean isUserSharedToL1Org =
                scim2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MTokenForLevel1Org);
        if (!isUserSharedToL1Org) {
            Assert.fail("Failed sharing user to level 1 organization.");
        }
        boolean isUserSharedToL2Org =
                scim2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MTokenForLevel2Org);
        if (!isUserSharedToL2Org) {
            Assert.fail("Failed sharing user to level 2 organization.");
        }
        org.json.simple.JSONObject sharedUserInLevel1Org =
                scim2RestClient.searchSubOrgUser(userSearchReq, switchedM2MTokenForLevel1Org);
        sharedUserIdInLevel1Org =
                ((org.json.simple.JSONObject) ((org.json.simple.JSONArray) sharedUserInLevel1Org.get("Resources")).get(
                        0)).get("id").toString();
        Assert.assertNotNull(sharedUserIdInLevel1Org, "Failed sharing user to level 1 organization.");

        org.json.simple.JSONObject sharedUserInLevel2Org =
                scim2RestClient.searchSubOrgUser(userSearchReq, switchedM2MTokenForLevel2Org);
        sharedUserIdInLevel2Org =
                ((org.json.simple.JSONObject) ((org.json.simple.JSONArray) sharedUserInLevel2Org.get("Resources")).get(
                        0)).get("id").toString();
        Assert.assertNotNull(sharedUserIdInLevel2Org, "Failed sharing user to level 2 organization.");
    }

    @Test(dependsOnMethods = {"testAddUserInRootOrgAndShareWithLevel1AndLevel2Org"},
            description = "Verify the shared users profiles have resolved given name and email from origin.")
    public void testGivenNameAndEmailResolvedFromOrigin() throws Exception {

        // Validate shared user in level 1 org.
        validateClaimsResolvedFromOriginInSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org);
        // Validate shared user in level 2 org.
        validateClaimsResolvedFromOriginInSharedUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org);
    }

    private void validateClaimsResolvedFromOriginInSharedUser(String sharedUserId,
                                                              String switchedM2MToken) throws Exception {

        org.json.simple.JSONObject sharedUser = scim2RestClient.getSubOrgUser(sharedUserId, switchedM2MToken);
        String givenNameOfSharedUser = (String) ((org.json.simple.JSONObject) sharedUser.get("name")).get("givenName");
        String emailOfSharedUser = (String) ((org.json.simple.JSONArray) sharedUser.get("emails")).get(0);
        Assert.assertEquals(givenNameOfSharedUser, ROOT_ORG_USER_GIVEN_NAME, "Unexpected given name.");
        Assert.assertEquals(emailOfSharedUser, ROOT_ORG_USER_EMAIL, "Unexpected email.");
    }

    @Test(dependsOnMethods = {"testGivenNameAndEmailResolvedFromOrigin"},
            description = "Verify the claims resolved from origin can't be updated in shared profiles.")
    public void testClaimsResolvedFromOriginCanNotBeUpdatedInSharedProfiles() throws Exception {

        UserItemAddGroupobj givenNameUpdatePatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        givenNameUpdatePatchOp.setPath("name.givenName");
        givenNameUpdatePatchOp.setValue("UpdatedGivenName");
        String givenNameUpdateFailureDetail =
                "Claim: http://wso2.org/claims/givenname is not allowed to be updated for shared users.";
        // Try to update given name of shared user in level 1 org.
        tryToUpdateUnmodifiableAttributes(givenNameUpdatePatchOp, sharedUserIdInLevel1Org,
                switchedM2MTokenForLevel1Org, givenNameUpdateFailureDetail);
        // Try to update given name of shared user in level 2 org.
        tryToUpdateUnmodifiableAttributes(givenNameUpdatePatchOp, sharedUserIdInLevel2Org,
                switchedM2MTokenForLevel2Org, givenNameUpdateFailureDetail);
    }

    @Test(dependsOnMethods = {"testClaimsResolvedFromOriginCanNotBeUpdatedInSharedProfiles"},
            description = "Verify the claims resolved from first found in hierarchy.")
    public void testClaimsResolvedFromFirstFoundInHierarchy() throws Exception {

        // Sub orgs should not have custom claim before shared app mark it as requested claim.
        verifyCustomClaimIsNotShared(customClaimId, switchedM2MTokenForLevel1Org);
        verifyCustomClaimIsNotShared(customClaimId, switchedM2MTokenForLevel2Org);

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(getClaimConfigurations());
        oAuth2RestClient.updateApplication(application.getId(), applicationPatch);

        // Sub orgs should have custom claim after marking that claim as requested claim in shared app.
        verifyCustomClaimIsShared(customClaimId, switchedM2MTokenForLevel1Org);
        verifyCustomClaimIsShared(customClaimId, switchedM2MTokenForLevel2Org);

        // Update the custom claim in root org user.
        String customClaimValueSetInRootOrgUser = "ValueInRootOrgUser";
        UserItemAddGroupobj customClaimUpdatePatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        customClaimUpdatePatchOp.setPath(SCIM2_CUSTOM_SCHEMA_DIALECT_URI + ":" + CUSTOM_CLAIM_NAME);
        customClaimUpdatePatchOp.setValue(customClaimValueSetInRootOrgUser);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(customClaimUpdatePatchOp),
                rootOrgUserId);

        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org,
                customClaimValueSetInRootOrgUser);
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org,
                customClaimValueSetInRootOrgUser);

        // Update the custom claim in level 1 org's shared user.
        String customClaimValueSetInLevel1OrgUser = "ValueInLevel1OrgUser";
        customClaimUpdatePatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        customClaimUpdatePatchOp.setPath(SCIM2_CUSTOM_SCHEMA_DIALECT_URI + ":" + CUSTOM_CLAIM_NAME);
        customClaimUpdatePatchOp.setValue(customClaimValueSetInLevel1OrgUser);
        org.json.simple.JSONObject userUpdateResponse = scim2RestClient.updateSubOrgUser(
                new PatchOperationRequestObject().addOperations(customClaimUpdatePatchOp), sharedUserIdInLevel1Org,
                switchedM2MTokenForLevel1Org);
        Assert.assertNotNull(userUpdateResponse);
        Assert.assertEquals(userUpdateResponse.get("id"), sharedUserIdInLevel1Org,
                "Failed to update custom claim in level 1 org's shared user.");
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org,
                customClaimValueSetInLevel1OrgUser);
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org,
                customClaimValueSetInLevel1OrgUser);
    }

    private void validateCustomClaimValueOfSharedUser(String sharedUserId, String switchedM2MToken,
                                                      String expectedValue) throws Exception {

        org.json.simple.JSONObject sharedUser = scim2RestClient.getSubOrgUser(sharedUserId, switchedM2MToken);
        String customClaimValueOfSharedUser =
                (String) ((org.json.simple.JSONObject) sharedUser.get(SCIM2_CUSTOM_SCHEMA_DIALECT_URI)).get(
                        CUSTOM_CLAIM_NAME);
        Assert.assertEquals(customClaimValueOfSharedUser, expectedValue, "Unexpected custom claim value.");
    }

    private void verifyCustomClaimIsShared(String customClaimId, String switchedM2MToken) throws Exception {

        boolean isClaimSharingCompleted =
                claimManagementRestClient.isClaimSharingCompleted(customClaimId, switchedM2MToken);
        if (!isClaimSharingCompleted) {
            Assert.fail("Failed to share custom claim to sub org.");
        }
        org.json.simple.JSONObject customClaimInSubOrg =
                claimManagementRestClient.getSubOrgLocalClaim(customClaimId, switchedM2MToken);
        Assert.assertNotNull(customClaimInSubOrg, "Failed to get custom claim in sub org level.");
        Assert.assertEquals(customClaimInSubOrg.get("claimURI"), CUSTOM_CLAIM_URI);
        Assert.assertEquals(customClaimInSubOrg.get("id"), customClaimId);
        Assert.assertEquals(customClaimInSubOrg.get("sharedProfileValueResolvingMethod"),
                LocalClaimReq.SharedProfileValueResolvingMethodEnum.FromFirstFoundInHierarchy.toString());
    }

    private void verifyCustomClaimIsNotShared(String customClaimId, String switchedM2MToken) throws Exception {

        org.json.simple.JSONObject customClaimInSubOrg =
                claimManagementRestClient.getSubOrgLocalClaim(customClaimId, switchedM2MToken);
        Assert.assertEquals(customClaimInSubOrg.get("message"), "Resource not found.");
        Assert.assertEquals(customClaimInSubOrg.get("code"), "CMT-50019");
    }

    @Test(dependsOnMethods = {"testClaimsResolvedFromFirstFoundInHierarchy"},
            description = "Verify the claim behavior for shared user when change it FromFirstFoundInHierarchy to FromOrigin.")
    public void testChangeSharedClaimBehaviorFromFirstFoundInHierarchyToFromOrigin() throws Exception {

        String customClaimUpdateRequest =
                readResource(CUSTOM_CLAIM_UPDATE_TO_RESOLVE_FROM_ORIGIN_JSON, this.getClass());
        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, customClaimId, customClaimUpdateRequest);
        String customClaimValueSetInRootOrgUser = "ValueInRootOrgUser";
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org,
                customClaimValueSetInRootOrgUser);
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org,
                customClaimValueSetInRootOrgUser);
    }

    @Test(dependsOnMethods = {"testChangeSharedClaimBehaviorFromFirstFoundInHierarchyToFromOrigin"},
            description = "Verify the claim behavior for shared user when change it FromOrigin to FromSharedProfile.")
    public void testChangeSharedUserProfileBehaviorFromOriginToFromSharedProfile() throws Exception {

        String customClaimUpdateRequest =
                readResource(CUSTOM_CLAIM_UPDATE_TO_RESOLVE_FROM_SHARED_PROFILE_JSON, this.getClass());
        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, customClaimId, customClaimUpdateRequest);
        validateCustomClaimValueOfSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org,
                "ValueInLevel1OrgUser");
        /*
        No value available for the custom claim in shared user in level 2 org.
        Therefore, custom schema should not be returned.
         */
        org.json.simple.JSONObject sharedUser =
                scim2RestClient.getSubOrgUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org);
        Assert.assertNull(sharedUser.get(SCIM2_CUSTOM_SCHEMA_DIALECT_URI),
                "Unexpected custom schema in shared user in level 2 org.");
    }

    @Test(dependsOnMethods = {"testChangeSharedUserProfileBehaviorFromOriginToFromSharedProfile"},
            description = "Verify the managedOrg claim is non modifiable from shared users even though " +
                    "the value is resolved from shared profile.")
    public void testManagedOrgClaimIsNonModifiableFromSharedUsers() throws Exception {

        UserItemAddGroupobj managedOrgUpdatePatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        managedOrgUpdatePatchOp.setPath(SCIM2_SYSTEM_SCHEMA_DIALECT_URI + ":managedOrg");
        managedOrgUpdatePatchOp.setValue("UpdatedManagedOrg");
        String managedOrgUpdateFailureDetail = "The managed organization is a read only property.";
        // Try to update managed org of shared user in level 1 org.
        tryToUpdateUnmodifiableAttributes(managedOrgUpdatePatchOp, sharedUserIdInLevel1Org,
                switchedM2MTokenForLevel1Org, managedOrgUpdateFailureDetail);
        // Try to update managed org of shared user in level 2 org.
        tryToUpdateUnmodifiableAttributes(managedOrgUpdatePatchOp, sharedUserIdInLevel2Org,
                switchedM2MTokenForLevel2Org, managedOrgUpdateFailureDetail);
    }

    private void tryToUpdateUnmodifiableAttributes(UserItemAddGroupobj givenNameUpdatePatchOp, String sharedUserId,
                                                   String switchedM2MTokenForOrg, String errorDetail) throws Exception {

        org.json.simple.JSONObject userUpdateResponse = scim2RestClient.updateSubOrgUser(
                new PatchOperationRequestObject().addOperations(givenNameUpdatePatchOp), sharedUserId,
                switchedM2MTokenForOrg);
        Assert.assertEquals(userUpdateResponse.get("status"), "400");
        Assert.assertEquals(userUpdateResponse.get("detail"), errorDetail);
    }

    private ClaimConfiguration getClaimConfigurations() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addClaimMappingsItem(getClaimMapping(CUSTOM_CLAIM_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(CUSTOM_CLAIM_URI));

        return claimConfiguration;
    }

    private ClaimMappings getClaimMapping(String claimUri) {

        ClaimMappings claim = new ClaimMappings().applicationClaim(claimUri);
        claim.setLocalClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        claimUri));
        return claim;
    }

    private RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(
                        claimUri));
        return requestedClaim;
    }

    private LocalClaimReq buildLocalClaimReq(String claimURI, String displayName, String description,
                                             LocalClaimReq.SharedProfileValueResolvingMethodEnum sharedProfileValueResolvingMethod) {

        LocalClaimReq localClaimReq = new LocalClaimReq();
        localClaimReq.setClaimURI(claimURI);
        localClaimReq.setDisplayName(displayName);
        localClaimReq.setDescription(description);
        AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
        attributeMappingDTO.setMappedAttribute(displayName);
        attributeMappingDTO.setUserstore("PRIMARY");
        localClaimReq.setAttributeMapping(Collections.singletonList(attributeMappingDTO));
        localClaimReq.setSharedProfileValueResolvingMethod(sharedProfileValueResolvingMethod);
        return localClaimReq;
    }

    private void shareApplication() throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(application.getId(), applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }
}
