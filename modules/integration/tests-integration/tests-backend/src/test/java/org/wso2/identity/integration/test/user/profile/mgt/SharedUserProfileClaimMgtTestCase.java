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
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;

import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;

/**
 * Test class to verify shared user profile claim management.
 */
public class SharedUserProfileClaimMgtTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis.json";
    private static final String L1_SUB_ORG_NAME = "L1_Sub_Org";
    private static final String L2_SUB_ORG_NAME = "L2_Sub_Org";
    private static final String ROOT_ORG_USERNAME = "alex";
    private static final String ROOT_ORG_USER_PASSWORD = "Wso2@123";
    private static final String ROOT_ORG_USER_EMAIL = "alex@gmail.com";
    private static final String ROOT_ORG_USER_GIVEN_NAME = "Alex";
    private final TestUserMode userMode;
    private ClaimManagementRestClient claimManagementRestClient;
    private SCIM2RestClient scim2RestClient;
    private UserSharingRestClient userSharingRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private String level1OrgId;
    private String level2OrgId;
    private String switchedM2MTokenForLevel1Org;
    private String switchedM2MTokenForLevel2Org;
    private String rootOrgUserId;
    private String sharedUserIdInLevel1Org;
    private String sharedUserIdInLevel2Org;

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
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON, this.getClass())));
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        level1OrgId = orgMgtRestClient.addOrganization(L1_SUB_ORG_NAME);
        level2OrgId = orgMgtRestClient.addSubOrganization(L2_SUB_ORG_NAME, level1OrgId);

        switchedM2MTokenForLevel1Org = orgMgtRestClient.switchM2MToken(level1OrgId);
        switchedM2MTokenForLevel2Org = orgMgtRestClient.switchM2MToken(level2OrgId);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2RestClient.deleteUser(rootOrgUserId);
        orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
        orgMgtRestClient.deleteOrganization(level1OrgId);
        orgMgtRestClient.closeHttpClient();
        idpMgtServiceClient.deleteIdP("SSO");
        scim2RestClient.closeHttpClient();
        userSharingRestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
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
        // Try to update given name of shared user in level 1 org.
        updateGivenNameInSharedUserProfile(givenNameUpdatePatchOp, sharedUserIdInLevel1Org,
                switchedM2MTokenForLevel1Org);
        // Try to update given name of shared user in level 2 org.
        updateGivenNameInSharedUserProfile(givenNameUpdatePatchOp, sharedUserIdInLevel2Org,
                switchedM2MTokenForLevel2Org);

    }

    private void updateGivenNameInSharedUserProfile(UserItemAddGroupobj givenNameUpdatePatchOp, String sharedUserId,
                                                    String switchedM2MTokenForOrg) throws Exception {

        org.json.simple.JSONObject userUpdateResponse = scim2RestClient.updateSubOrgUser(
                new PatchOperationRequestObject().addOperations(givenNameUpdatePatchOp), sharedUserId,
                switchedM2MTokenForOrg);
        Assert.assertEquals(userUpdateResponse.get("status"), "400");
        Assert.assertEquals(userUpdateResponse.get("detail"),
                "Claim: http://wso2.org/claims/givenname is not allowed to be updated for shared users.");
    }
}
