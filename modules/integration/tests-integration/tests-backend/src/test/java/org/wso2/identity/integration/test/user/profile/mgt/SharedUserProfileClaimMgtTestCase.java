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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ClaimDialectReqDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;

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
    private static final String ROLES_CLAIM_URI = "http://wso2.org/claims/roles";
    private static final String GROUPS_CLAIM_URI = "http://wso2.org/claims/groups";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";
    private static final String ORGANIZATION_AUDIENCE = "ORGANIZATION";
    private static final String APP_1_NAME = "APP_1";
    private static final String APP_2_NAME = "APP_2";
    private static final String APP_ROLE_1 = "App-Role-1";
    private static final String APP_ROLE_2 = "App-Role-2";
    private static final String APP_ROLE_3 = "App-Role-3";
    private static final String ORG_ROLE_1 = "Org-Role-1";
    private static final String ORG_ROLE_2 = "Org-Role-2";
    private static final String ORG_ROLE_3 = "Org-Role-3";
    private static final String GROUP_1 = "Group1";
    private static final String GROUP_2 = "Group2";
    private static final String SUB_GROUP_1 = "SubGroup1";
    private static final String SUB_GROUP_2 = "SubGroup2";
    private static final String SUPER_ORG_NAME = "Super";
    private static final String SUB_CLAIM = "sub";
    private static final String ISS_CLAIM = "iss";
    private static final String ORG_ID_CLAIM = "org_id";
    private static final String ORG_NAME_CLAIM = "org_name";
    private static final String ORG_VERSION_V0 = "v0.0.0";
    private final TestUserMode userMode;
    private ClaimManagementRestClient claimManagementRestClient;
    private SCIM2RestClient scim2RestClient;
    private UserSharingRestClient userSharingRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private CloseableHttpClient httpClient;
    private String level1OrgId;
    private String level2OrgId;
    private String switchedM2MTokenForLevel1Org;
    private String switchedM2MTokenForLevel2Org;
    private String rootOrgUserId;
    private String sharedUserIdInLevel1Org;
    private String sharedUserIdInLevel2Org;
    private ApplicationResponseModel application1WithAppAudienceRoles;
    private String sharedApp1IdInLevel1Org;
    private ApplicationResponseModel application2WithOrgAudienceRoles;
    private String sharedApp2IdInLevel1Org;
    private String clientIdApp1;
    private String clientSecretApp1;
    private String clientIdApp2;
    private String clientSecretApp2;
    private String customClaimId;
    private String scimClaimIdOfCustomClaim;
    private String appRole1Id;
    private String appRole2Id;
    private String appRole3Id;
    private String orgRole1Id;
    private String orgRole2Id;
    private String orgRole3Id;
    private String group1Id;
    private String group2Id;
    private String subGroup1Id;
    private String subGroup2Id;

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
        httpClient = HttpClientBuilder.create().build();
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

        if (TestUserMode.TENANT_ADMIN.equals(userMode)) {
            orgMgtRestClient.updateOrganizationVersion(ORG_VERSION_V0);
        }

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

        // Create a new application which consume application audience roles and share with all children.
        application1WithAppAudienceRoles = addApplication(APP_1_NAME);
        String app1Id = application1WithAppAudienceRoles.getId();
        OpenIDConnectConfiguration oidcConfigOfApp1 = getOIDCInboundDetailsOfApplication(app1Id);
        clientIdApp1 = oidcConfigOfApp1.getClientId();
        clientSecretApp1 = oidcConfigOfApp1.getClientSecret();
        createApp1RolesWithAppAudience(app1Id);
        // Mark roles and groups as requested claims for the app 1.
        updateRequestedClaimsOfApp(app1Id, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(app1Id);
        sharedApp1IdInLevel1Org =
                oAuth2RestClient.getAppIdUsingAppNameInOrganization(APP_1_NAME, switchedM2MTokenForLevel1Org);

        // Create a new application which consume organization audience roles and share with all children.
        application2WithOrgAudienceRoles = addApplication(APP_2_NAME);
        String app2Id = application2WithOrgAudienceRoles.getId();
        OpenIDConnectConfiguration oidcConfigOfApp2 = getOIDCInboundDetailsOfApplication(app2Id);
        clientIdApp2 = oidcConfigOfApp2.getClientId();
        clientSecretApp2 = oidcConfigOfApp2.getClientSecret();
        createOrganizationRoles();
        switchApplicationAudience(app2Id, AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
        // Mark roles and groups as requested claims for the app 2.
        updateRequestedClaimsOfApp(app2Id, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(app2Id);
        sharedApp2IdInLevel1Org =
                oAuth2RestClient.getAppIdUsingAppNameInOrganization(APP_2_NAME, switchedM2MTokenForLevel1Org);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Clean up roles.
        scim2RestClient.deleteV2Role(appRole1Id);
        scim2RestClient.deleteV2Role(appRole2Id);
        scim2RestClient.deleteV2Role(appRole3Id);
        scim2RestClient.deleteV2Role(orgRole1Id);
        scim2RestClient.deleteV2Role(orgRole2Id);
        scim2RestClient.deleteV2Role(orgRole3Id);
        // Delete user.
        scim2RestClient.deleteUser(rootOrgUserId);
        // Delete groups.
        scim2RestClient.deleteGroup(group1Id);
        scim2RestClient.deleteGroup(group2Id);
        scim2RestClient.deleteSubOrgGroup(subGroup1Id, switchedM2MTokenForLevel1Org);
        scim2RestClient.deleteSubOrgGroup(subGroup2Id, switchedM2MTokenForLevel1Org);
        // Delete applications.
        oAuth2RestClient.deleteApplication(application1WithAppAudienceRoles.getId());
        oAuth2RestClient.deleteApplication(application2WithOrgAudienceRoles.getId());
        // Delete sub organizations.
        orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
        orgMgtRestClient.deleteOrganization(level1OrgId);
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
        httpClient.close();
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
            description = "Verify roles can be added to root users and shared users.")
    public void testAddRolesToRootUserAndSharedUsers() throws Exception {

        // Add appRole1, appRole2, orgRole1 and orgRole2 roles to root org user.
        RoleItemAddGroupobj patchOperationToAddRootOrgUser = new RoleItemAddGroupobj();
        patchOperationToAddRootOrgUser.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchOperationToAddRootOrgUser.setPath("users");
        patchOperationToAddRootOrgUser.addValue(new ListObject().value(rootOrgUserId));

        scim2RestClient.updateUsersOfRoleV2(appRole1Id,
                new PatchOperationRequestObject().addOperations(patchOperationToAddRootOrgUser));
        scim2RestClient.updateUsersOfRoleV2(appRole2Id,
                new PatchOperationRequestObject().addOperations(patchOperationToAddRootOrgUser));
        scim2RestClient.updateUsersOfRoleV2(orgRole1Id,
                new PatchOperationRequestObject().addOperations(patchOperationToAddRootOrgUser));
        scim2RestClient.updateUsersOfRoleV2(orgRole2Id,
                new PatchOperationRequestObject().addOperations(patchOperationToAddRootOrgUser));

        org.json.simple.JSONObject assignedRolesOfRootUser = scim2RestClient.getUser(rootOrgUserId, "roles");
        Assert.assertNotNull(assignedRolesOfRootUser, "Failed to get the root organization user.");
        // Get the manually assigned 4 roles and everyone role.
        Assert.assertEquals(((org.json.simple.JSONArray) assignedRolesOfRootUser.get("roles")).size(), 5,
                "Unexpected number of roles.");

        // Add shared appRole2, appRole3, orgRole2 and orgRole3 roles to shared user in level 1 org.
        RoleItemAddGroupobj patchOperationToAddSharedUserInLevel1Org = new RoleItemAddGroupobj();
        patchOperationToAddSharedUserInLevel1Org.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchOperationToAddSharedUserInLevel1Org.setPath("users");
        patchOperationToAddSharedUserInLevel1Org.addValue(new ListObject().value(sharedUserIdInLevel1Org));

        String sharedAppRole2InLevel1Org =
                scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(APP_ROLE_2, sharedApp1IdInLevel1Org,
                        switchedM2MTokenForLevel1Org);
        String sharedAppRole3InLevel1Org =
                scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(APP_ROLE_3, sharedApp1IdInLevel1Org,
                        switchedM2MTokenForLevel1Org);
        String sharedOrgRole2InLevel1Org = scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(ORG_ROLE_2, level1OrgId,
                switchedM2MTokenForLevel1Org);
        String sharedOrgRole3InLevel1Org = scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(ORG_ROLE_3, level1OrgId,
                switchedM2MTokenForLevel1Org);

        scim2RestClient.updateUsersOfRoleV2InSubOrg(sharedAppRole2InLevel1Org,
                new PatchOperationRequestObject().addOperations(patchOperationToAddSharedUserInLevel1Org),
                switchedM2MTokenForLevel1Org);
        scim2RestClient.updateUsersOfRoleV2InSubOrg(sharedAppRole3InLevel1Org,
                new PatchOperationRequestObject().addOperations(patchOperationToAddSharedUserInLevel1Org),
                switchedM2MTokenForLevel1Org);
        scim2RestClient.updateUsersOfRoleV2InSubOrg(sharedOrgRole2InLevel1Org,
                new PatchOperationRequestObject().addOperations(patchOperationToAddSharedUserInLevel1Org),
                switchedM2MTokenForLevel1Org);
        scim2RestClient.updateUsersOfRoleV2InSubOrg(sharedOrgRole3InLevel1Org,
                new PatchOperationRequestObject().addOperations(patchOperationToAddSharedUserInLevel1Org),
                switchedM2MTokenForLevel1Org);

        org.json.simple.JSONObject assignedRolesOfSharedUserInLevel1Org =
                scim2RestClient.getSubOrgUser(sharedUserIdInLevel1Org, "roles", switchedM2MTokenForLevel1Org);
        Assert.assertNotNull(assignedRolesOfSharedUserInLevel1Org, "Failed to get the shared user in level 1 org.");
        Assert.assertEquals(((org.json.simple.JSONArray) assignedRolesOfSharedUserInLevel1Org.get("roles")).size(), 4,
                "Unexpected number of roles.");
    }

    @Test(dependsOnMethods = {"testAddRolesToRootUserAndSharedUsers"},
            description = "Verify groups can be added to root users and shared users.")
    public void testAddGroupsToRootUserAndSharedUsers() throws Exception {

        // Create group 1 and group 2 in root org, and assign to root user.
        group1Id = scim2RestClient.createGroup(new GroupRequestObject().displayName(GROUP_1)
                .addMember(new GroupRequestObject.MemberItem().value(rootOrgUserId)));
        group2Id = scim2RestClient.createGroup(new GroupRequestObject().displayName(GROUP_2)
                .addMember(new GroupRequestObject.MemberItem().value(rootOrgUserId)));

        Assert.assertNotNull(group1Id, "Failed to create group 1 in root org.");
        Assert.assertNotNull(group2Id, "Failed to create group 2 in root org.");
        org.json.simple.JSONObject rootOrgUserGroups = scim2RestClient.getUser(rootOrgUserId, "groups");
        Assert.assertNotNull(rootOrgUserGroups, "Failed to get the root organization user with groups.");
        Assert.assertEquals(((org.json.simple.JSONArray) rootOrgUserGroups.get("groups")).size(), 2,
                "Unexpected number of groups.");

        // Add subgroup 1 and subgroup 2 to shared user in level1 org.
        subGroup1Id = scim2RestClient.createSubOrgGroup(new GroupRequestObject().displayName(SUB_GROUP_1)
                        .addMember(new GroupRequestObject.MemberItem().value(sharedUserIdInLevel1Org)),
                switchedM2MTokenForLevel1Org);
        subGroup2Id = scim2RestClient.createSubOrgGroup(new GroupRequestObject().displayName(SUB_GROUP_2)
                        .addMember(new GroupRequestObject.MemberItem().value(sharedUserIdInLevel1Org)),
                switchedM2MTokenForLevel1Org);

        Assert.assertNotNull(subGroup1Id, "Failed to create sub group 1 in level 1 org.");
        Assert.assertNotNull(subGroup2Id, "Failed to create sub group 2 in level 1 org.");
        org.json.simple.JSONObject sharedUserInLevel1OrgGroups =
                scim2RestClient.getSubOrgUser(sharedUserIdInLevel1Org, "groups", switchedM2MTokenForLevel1Org);
        Assert.assertNotNull(sharedUserInLevel1OrgGroups, "Failed to get the shared user in level 1 org with groups.");
        Assert.assertEquals(((org.json.simple.JSONArray) sharedUserInLevel1OrgGroups.get("groups")).size(), 2,
                "Unexpected number of groups.");
    }

    @Test(dependsOnMethods = {"testAddGroupsToRootUserAndSharedUsers"},
            description = "Verify the groups and roles in organization switched token in app role consuming app.")
    public void testGroupsAndRolesInOrganizationSwitchedTokenFromAppRoleUsingApp() throws Exception {

        String[] requestedScopes = {"openid", "roles", "groups"};
        HttpResponse httpResponseOfPasswordGrantRequest =
                sendTokenRequestForPasswordGrant(ROOT_ORG_USERNAME, ROOT_ORG_USER_PASSWORD,
                        Arrays.asList(requestedScopes), clientIdApp1, clientSecretApp1);
        String passwordGrantResponseBody =
                EntityUtils.toString(httpResponseOfPasswordGrantRequest.getEntity(), "UTF-8");
        EntityUtils.consume(httpResponseOfPasswordGrantRequest.getEntity());
        Assert.assertNotNull(httpResponseOfPasswordGrantRequest, "Failed password grant request.");
        String accessTokenFromPasswordGrant = extractAccessToken(passwordGrantResponseBody);
        String idTokenFromPasswordGrant = extractIdToken(passwordGrantResponseBody);
        Assert.assertNotNull(accessTokenFromPasswordGrant, "Failed to get the access token from password grant.");
        Assert.assertNotNull(idTokenFromPasswordGrant, "Failed to get the id token from password grant.");
        JWTClaimsSet jwtClaimsSet = parseJWTToken(idTokenFromPasswordGrant);
        Assert.assertNotNull(jwtClaimsSet, "Failed to parse the id token from password grant.");
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(SUB_CLAIM), rootOrgUserId, "Incorrect sub in token.");
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantInfo.getDomain())) {
            Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ORG_NAME_CLAIM), SUPER_ORG_NAME,
                    "Incorrect org_name in token.");
        } else {
            Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ORG_NAME_CLAIM), tenantInfo.getDomain(),
                    "Incorrect org_name in token.");
        }
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ISS_CLAIM),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()), "Incorrect iss in token.");
        assertRolesInToken(jwtClaimsSet, 2, new String[]{APP_ROLE_1, APP_ROLE_2});
        assertGroupsInToken(jwtClaimsSet, 2, new String[]{GROUP_1, GROUP_2});

        // Switch the token.
        HttpResponse httpResponseOfOrgSwitchGrantRequest =
                getOrganizationSwitchToken(clientIdApp1, clientSecretApp1, accessTokenFromPasswordGrant, level1OrgId,
                        Arrays.asList(requestedScopes));
        String organizationSwitchTokenResponseBody =
                EntityUtils.toString(httpResponseOfOrgSwitchGrantRequest.getEntity(), "UTF-8");
        EntityUtils.consume(httpResponseOfOrgSwitchGrantRequest.getEntity());
        Assert.assertNotNull(httpResponseOfOrgSwitchGrantRequest, "Failed organization switch grant request.");
        String accessTokenFromOrgSwitchGrant = extractAccessToken(organizationSwitchTokenResponseBody);
        String idTokenFromOrgSwitchGrant = extractIdToken(organizationSwitchTokenResponseBody);
        Assert.assertNotNull(accessTokenFromOrgSwitchGrant,
                "Failed to get the access token from organization switch grant.");
        Assert.assertNotNull(idTokenFromOrgSwitchGrant, "Failed to get the id token from organization switch grant.");
        JWTClaimsSet jwtClaimsSetOfSwitchedToken = parseJWTToken(idTokenFromOrgSwitchGrant);
        Assert.assertNotNull(jwtClaimsSetOfSwitchedToken, "Failed to parse the id token got from org switch grant.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(SUB_CLAIM), rootOrgUserId,
                "Incorrect sub in token.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(ORG_NAME_CLAIM), L1_SUB_ORG_NAME,
                "Incorrect org_name in token.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(ORG_ID_CLAIM), level1OrgId,
                "Incorrect org_id in token.");
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ISS_CLAIM),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()), "Incorrect iss in token.");
        assertRolesInToken(jwtClaimsSetOfSwitchedToken, 2, new String[]{APP_ROLE_2, APP_ROLE_3});
        assertGroupsInToken(jwtClaimsSetOfSwitchedToken, 2, new String[]{SUB_GROUP_1, SUB_GROUP_2});
    }

    @Test(dependsOnMethods = {"testGroupsAndRolesInOrganizationSwitchedTokenFromAppRoleUsingApp"},
            description = "Verify the groups and roles in organization switched token in org role consuming app.")
    public void testGroupsAndRolesInOrganizationSwitchedTokenFromOrgRoleUsingApp() throws Exception {

        String[] requestedScopes = {"openid", "roles", "groups"};
        HttpResponse httpResponseOfPasswordGrantRequest =
                sendTokenRequestForPasswordGrant(ROOT_ORG_USERNAME, ROOT_ORG_USER_PASSWORD,
                        Arrays.asList(requestedScopes), clientIdApp2, clientSecretApp2);
        String passwordGrantResponseBody =
                EntityUtils.toString(httpResponseOfPasswordGrantRequest.getEntity(), "UTF-8");
        EntityUtils.consume(httpResponseOfPasswordGrantRequest.getEntity());
        Assert.assertNotNull(httpResponseOfPasswordGrantRequest, "Failed password grant request.");
        String accessTokenFromPasswordGrant = extractAccessToken(passwordGrantResponseBody);
        String idTokenFromPasswordGrant = extractIdToken(passwordGrantResponseBody);
        Assert.assertNotNull(accessTokenFromPasswordGrant, "Failed to get the access token from password grant.");
        Assert.assertNotNull(idTokenFromPasswordGrant, "Failed to get the id token from password grant.");
        JWTClaimsSet jwtClaimsSet = parseJWTToken(idTokenFromPasswordGrant);
        Assert.assertNotNull(jwtClaimsSet, "Failed to parse the id token from password grant.");
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(SUB_CLAIM), rootOrgUserId, "Incorrect sub in token.");
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantInfo.getDomain())) {
            Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ORG_NAME_CLAIM), SUPER_ORG_NAME,
                    "Incorrect org_name in token.");
        } else {
            Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ORG_NAME_CLAIM), tenantInfo.getDomain(),
                    "Incorrect org_name in token.");
        }
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ISS_CLAIM),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()), "Incorrect iss in token.");
        assertRolesInToken(jwtClaimsSet, 3, new String[]{ORG_ROLE_1, ORG_ROLE_2, "everyone"});
        assertGroupsInToken(jwtClaimsSet, 2, new String[]{GROUP_1, GROUP_2});

        // Switch the token.
        HttpResponse httpResponseOfOrgSwitchGrantRequest =
                getOrganizationSwitchToken(clientIdApp2, clientSecretApp2, accessTokenFromPasswordGrant, level1OrgId,
                        Arrays.asList(requestedScopes));
        String organizationSwitchTokenResponseBody =
                EntityUtils.toString(httpResponseOfOrgSwitchGrantRequest.getEntity(), "UTF-8");
        EntityUtils.consume(httpResponseOfOrgSwitchGrantRequest.getEntity());
        Assert.assertNotNull(httpResponseOfOrgSwitchGrantRequest, "Failed organization switch grant request.");
        String accessTokenFromOrgSwitchGrant = extractAccessToken(organizationSwitchTokenResponseBody);
        String idTokenFromOrgSwitchGrant = extractIdToken(organizationSwitchTokenResponseBody);
        Assert.assertNotNull(accessTokenFromOrgSwitchGrant,
                "Failed to get the access token from organization switch grant.");
        Assert.assertNotNull(idTokenFromOrgSwitchGrant, "Failed to get the id token from organization switch grant.");
        JWTClaimsSet jwtClaimsSetOfSwitchedToken = parseJWTToken(idTokenFromOrgSwitchGrant);
        Assert.assertNotNull(jwtClaimsSetOfSwitchedToken, "Failed to parse the id token got from org switch grant.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(SUB_CLAIM), rootOrgUserId,
                "Incorrect sub in token.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(ORG_NAME_CLAIM), L1_SUB_ORG_NAME,
                "Incorrect org_name in token.");
        Assert.assertEquals((String) jwtClaimsSetOfSwitchedToken.getClaims().get(ORG_ID_CLAIM), level1OrgId,
                "Incorrect org_id in token.");
        Assert.assertEquals((String) jwtClaimsSet.getClaims().get(ISS_CLAIM),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()), "Incorrect iss in token.");
        assertRolesInToken(jwtClaimsSetOfSwitchedToken, 2, new String[]{ORG_ROLE_2, ORG_ROLE_3});
        assertGroupsInToken(jwtClaimsSetOfSwitchedToken, 2, new String[]{SUB_GROUP_1, SUB_GROUP_2});
    }

    private void assertRolesInToken(JWTClaimsSet jwtClaimsSet, int expectedRoleCount, String[] expectedRoles) {

        java.util.List<?> rolesInToken = (java.util.List<?>) jwtClaimsSet.getClaims().get("roles");
        Assert.assertEquals(rolesInToken.size(), expectedRoleCount, "Incorrect roles count in token.");

        for (String expectedRole : expectedRoles) {
            boolean roleFound = false;
            for (int i = 0; i < rolesInToken.size(); i++) {
                if (expectedRole.equals(rolesInToken.get(i))) {
                    roleFound = true;
                    break;
                }
            }
            Assert.assertTrue(roleFound, expectedRole + " is not found in token.");
        }
    }

    private void assertGroupsInToken(JWTClaimsSet jwtClaimsSet, int expectedGroupCount, String[] expectedGroups) {

        java.util.List<?> groupsInToken = (java.util.List<?>) jwtClaimsSet.getClaims().get("groups");
        Assert.assertEquals(groupsInToken.size(), expectedGroupCount, "Incorrect groups count in token.");

        for (String expectedGroup : expectedGroups) {
            boolean groupFound = false;
            for (int i = 0; i < groupsInToken.size(); i++) {
                if (expectedGroup.equals(groupsInToken.get(i))) {
                    groupFound = true;
                    break;
                }
            }
            Assert.assertTrue(groupFound, expectedGroup + " is not found in token.");
        }
    }

    @Test(dependsOnMethods = {"testGroupsAndRolesInOrganizationSwitchedTokenFromOrgRoleUsingApp"},
            description = "Verify the shared users profiles have resolved given name and email from origin.")
    public void testGivenNameAndEmailResolvedFromOrigin() throws Exception {

        // Validate shared user in level 1 org.
        validateClaimsResolvedFromOriginInSharedUser(sharedUserIdInLevel1Org, switchedM2MTokenForLevel1Org);
        // Validate shared user in level 2 org.
        validateClaimsResolvedFromOriginInSharedUser(sharedUserIdInLevel2Org, switchedM2MTokenForLevel2Org);
    }

    private void validateClaimsResolvedFromOriginInSharedUser(String sharedUserId,
                                                              String switchedM2MToken) throws Exception {

        org.json.simple.JSONObject sharedUser = scim2RestClient.getSubOrgUser(sharedUserId, null, switchedM2MToken);
        String givenNameOfSharedUser = (String) ((org.json.simple.JSONObject) sharedUser.get("name")).get("givenName");
        org.json.simple.JSONArray emails = (org.json.simple.JSONArray) sharedUser.get("emails");
        String emailOfSharedUser = "";
        for (org.json.simple.JSONObject email : (Iterable<org.json.simple.JSONObject>) emails) {
            if ((Boolean) email.get("primary")) {
                emailOfSharedUser = (String) email.get("value");
                break;
            }
        }
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

        if (TestUserMode.TENANT_ADMIN.equals(userMode)) {
            // V0 sub-orgs should not have custom claims before a shared app marks them as requested claims.
            verifyCustomClaimIsNotShared(customClaimId, switchedM2MTokenForLevel1Org);
            verifyCustomClaimIsNotShared(customClaimId, switchedM2MTokenForLevel2Org);
        }

        updateRequestedClaimsOfApp(application1WithAppAudienceRoles.getId(), getClaimConfigurationsWithCustomClaim());

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

        org.json.simple.JSONObject sharedUser = scim2RestClient.getSubOrgUser(sharedUserId, null, switchedM2MToken);
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
                scim2RestClient.getSubOrgUser(sharedUserIdInLevel2Org, null, switchedM2MTokenForLevel2Org);
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
        String managedOrgUpdateFailureDetail = "Can not replace a immutable attribute or a read-only attribute";
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

    private ClaimConfiguration getClaimConfigurationsWithCustomClaim() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addClaimMappingsItem(getClaimMapping(CUSTOM_CLAIM_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(CUSTOM_CLAIM_URI));

        return claimConfiguration;
    }

    private ClaimConfiguration getClaimConfigurationsWithRolesAndGroups() {

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(ROLES_CLAIM_URI));
        claimConfiguration.addRequestedClaimsItem(getRequestedClaim(GROUPS_CLAIM_URI));
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

    private void shareApplication(String applicationId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(applicationId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    private boolean isExistingDialect(org.json.simple.JSONObject externalDialectGetResponse) {

        if (externalDialectGetResponse.get("code") != null &&
                externalDialectGetResponse.get("code").equals("CMT-50016")) {
            return false;
        }
        return externalDialectGetResponse.get("id") != null;
    }

    private void switchApplicationAudience(String appId, AssociatedRolesConfig.AllowedAudienceEnum newAudience)
            throws Exception {

        AssociatedRolesConfig associatedRolesConfigApp = new AssociatedRolesConfig();
        associatedRolesConfigApp.setAllowedAudience(newAudience);
        ApplicationPatchModel applicationPatchModel = new ApplicationPatchModel();
        applicationPatchModel.setAssociatedRoles(associatedRolesConfigApp);
        oAuth2RestClient.updateApplication(appId, applicationPatchModel);
    }

    private void createOrganizationRoles() throws IOException {

        RoleV2 orgRole1 = new RoleV2(null, ORG_ROLE_1, Collections.emptyList(), Collections.emptyList());
        orgRole1Id = scim2RestClient.addV2Role(orgRole1);
        RoleV2 orgRole2 = new RoleV2(null, ORG_ROLE_2, Collections.emptyList(), Collections.emptyList());
        orgRole2Id = scim2RestClient.addV2Role(orgRole2);
        RoleV2 orgRole3 = new RoleV2(null, ORG_ROLE_3, Collections.emptyList(), Collections.emptyList());
        orgRole3Id = scim2RestClient.addV2Role(orgRole3);
    }

    private void createApp1RolesWithAppAudience(String app1Id) throws IOException {

        Audience app1RoleAudience = new Audience(APPLICATION_AUDIENCE, app1Id);
        RoleV2 appRole1 = new RoleV2(app1RoleAudience, APP_ROLE_1, Collections.emptyList(), Collections.emptyList());
        appRole1Id = scim2RestClient.addV2Role(appRole1);
        RoleV2 appRole2 = new RoleV2(app1RoleAudience, APP_ROLE_2, Collections.emptyList(), Collections.emptyList());
        appRole2Id = scim2RestClient.addV2Role(appRole2);
        RoleV2 appRole3 = new RoleV2(app1RoleAudience, APP_ROLE_3, Collections.emptyList(), Collections.emptyList());
        appRole3Id = scim2RestClient.addV2Role(appRole3);
    }

    private void updateRequestedClaimsOfApp(String applicationId, ClaimConfiguration claimConfigurationsForApp)
            throws IOException {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(claimConfigurationsForApp);
        oAuth2RestClient.updateApplication(applicationId, applicationPatch);
    }

    private ApplicationResponseModel addApplication(String appName) throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "organization_switch");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(appName);
        application.setIsManagementApp(true);

        application.setClaimConfiguration(setApplicationClaimConfig());
        String appId = addApplication(application);

        return getApplication(appId);
    }

    private HttpResponse sendTokenRequestForPasswordGrant(String username, String password,
                                                          List<String> requestedScopes, String clientId,
                                                          String clientSecret) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));

        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(httpClient, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private HttpResponse getOrganizationSwitchToken(String clientId, String clientSecret, String currentToken,
                                                   String switchingOrganizationId, List<String> requestedScopes)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, "organization_switch"));
        parameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE_TOKEN, currentToken));
        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, scopes));
        parameters.add(new BasicNameValuePair("switching_organization", switchingOrganizationId));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(httpClient, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private String extractAccessToken(String tokenResponseBody) throws Exception {

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject responseJSONBody = (org.json.simple.JSONObject) parser.parse(tokenResponseBody);
        Assert.assertNotNull(responseJSONBody, "Access token response is null.");
        Assert.assertNotNull(responseJSONBody.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        return (String) responseJSONBody.get(OAuth2Constant.ACCESS_TOKEN);
    }

    private String extractIdToken(String tokenResponseBody) throws Exception {

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject responseJSONBody = (org.json.simple.JSONObject) parser.parse(tokenResponseBody);
        Assert.assertNotNull(responseJSONBody, "ID token response is null.");
        Assert.assertNotNull(responseJSONBody.get(OAuth2Constant.ID_TOKEN), "ID token is null.");
        return (String) responseJSONBody.get(OAuth2Constant.ID_TOKEN);
    }

    private JWTClaimsSet parseJWTToken(String token) throws Exception {

        SignedJWT jwt = SignedJWT.parse(token);
        return jwt.getJWTClaimsSet();
    }
}
