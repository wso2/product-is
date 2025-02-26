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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudienceAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for successful cases of the User Sharing REST APIs.
 */
public class UserSharingSuccessTest extends UserSharingBaseTest {

    private String rootOrgUserId;
    private String l1Org1UserId;

    private String l1Org1Id;
    private String l1Org2Id;
    private String l2Org1Id;
    private String l2Org2Id;
    private String l2Org3Id;
    private String l3Org1Id;

    private String l1Org1SwitchToken;
    private String l2Org1SwitchToken;

    private String appId1;
    private String appId2;
    private String sharedApp1IdInLevel1Org;
    private String sharedApp2IdInLevel1Org;

    private ApplicationResponseModel application1WithAppAudienceRoles;
    private ApplicationResponseModel application2WithOrgAudienceRoles;
    private String clientIdApp1;
    private String clientSecretApp1;
    private String clientIdApp2;
    private String clientSecretApp2;

    private String appRole1Id;
    private String appRole2Id;
    private String appRole3Id;
    private String orgRole1Id;
    private String orgRole2Id;
    private String orgRole3Id;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSharingSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON)));

        setupOrganizations();
        setupTokens();
        setupApplicationsAndRoles();
        setupUsers();
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        // Cleanup users
        deleteUserIfExists(rootOrgUserId);
        deleteSubOrgUserIfExists(l1Org1UserId, l1Org1SwitchToken);

        // Cleanup roles
        deleteRoleIfExists(appRole1Id);
        deleteRoleIfExists(appRole2Id);
        deleteRoleIfExists(appRole3Id);
        deleteRoleIfExists(orgRole1Id);
        deleteRoleIfExists(orgRole2Id);
        deleteRoleIfExists(orgRole3Id);

        // Cleanup applications
        deleteApplicationIfExists(application1WithAppAudienceRoles.getId());
        deleteApplicationIfExists(application2WithOrgAudienceRoles.getId());

        // Cleanup organizations
        deleteSubOrganizationIfExists(l3Org1Id, l2Org1Id);
        deleteSubOrganizationIfExists(l2Org3Id, l1Org2Id);
        deleteSubOrganizationIfExists(l2Org2Id, l1Org1Id);
        deleteSubOrganizationIfExists(l2Org1Id, l1Org1Id);
        deleteOrganizationIfExists(l1Org2Id);
        deleteOrganizationIfExists(l1Org1Id);

        // Close REST clients
        closeRestClients();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "generalSharingPolicies")
    public Object[][] generalSharingPolicies() {

        String[] allOrgIds = {l1Org1Id, l1Org2Id, l2Org1Id, l2Org2Id, l2Org3Id, l3Org1Id};
        String[] allOrgNames =
                {L1_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L2_ORG_3_NAME, L3_ORG_1_NAME};
        String[] immediateOrgIds = {l1Org1Id, l1Org2Id};
        String[] immediateOrgNames = {L1_ORG_1_NAME, L1_ORG_2_NAME};

        return new Object[][]{
                {UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY, 6, allOrgIds, allOrgNames},
                {UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS, 6, allOrgIds, allOrgNames},
                {UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_ORGS_ONLY, 2, immediateOrgIds,
                        immediateOrgNames},
                {UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS, 2, immediateOrgIds,
                        immediateOrgNames}
        };
    }

    @Test(dataProvider = "generalSharingPolicies")
    public void testShareUsersWithAllOrganizations(UserShareWithAllRequestBody.PolicyEnum policy, int expectedOrgCount, String[] expectedOrgIds, String[] expectedOrgNames)
            throws Exception {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteria())
                .policy(policy)
                .roles(Arrays.asList(
                        createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_1, SUPER_ORG, ORGANIZATION_AUDIENCE)));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User sharing process triggered successfully."));

        Thread.sleep(5000); // Wait for the sharing process to complete.

        // Validate shared organizations
        testGetSharedOrganizationsWithAllWithoutPagination(expectedOrgCount, expectedOrgIds, expectedOrgNames);

        // Validate shared roles for each shared organization
        for (int i = 0; i < expectedOrgCount; i++) {
            testGetSharedRolesForOrgWithRolesWithoutPagination(expectedOrgIds[i], expectedOrgNames[i]);
        }
    }

    public void testGetSharedOrganizationsWithAllWithoutPagination(int expectedOrgCount, String[] expectedOrgIds, String[] expectedOrgNames) throws Exception {

        Response response =
                getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + rootOrgUserId + SHARED_ORGANIZATIONS_PATH);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("links.size()", equalTo(1))
                .body("links[0].isEmpty()", equalTo(true))
                .body("sharedOrganizations", notNullValue())
                .body("sharedOrganizations.size()", equalTo(expectedOrgCount))
                .body("sharedOrganizations.orgId", hasItems(expectedOrgIds))
                .body("sharedOrganizations.orgName", hasItems(expectedOrgNames))
                .body("sharedOrganizations.sharedType", everyItem(equalTo("SHARED")))
                .body("sharedOrganizations.rolesRef", hasItems(
                        Arrays.stream(expectedOrgIds)
                                .map(orgId -> getSharedOrgsRolesRef(rootOrgUserId, orgId))
                                .toArray(String[]::new)));
    }

    public void testGetSharedRolesForOrgWithRolesWithoutPagination(String orgId, String orgName) {

        Response response = getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + rootOrgUserId + SHARED_ROLES_PATH,
                Collections.singletonMap("orgId", orgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("links.size()", equalTo(1))  // Ensure one empty object inside the array
                .body("links[0].isEmpty()", equalTo(true))  // Ensure the object inside is empty
                .body("roles", notNullValue())
                .body("roles.size()", equalTo(2))  // Expecting 2 roles per shared organization
                .body("roles.displayName", hasItems(APP_ROLE_1, ORG_ROLE_1))  // Ensure both roles exist
                .body("roles.audience.display", hasItems(APP_1_NAME, orgName))  // Ensure correct audience
                .body("roles.audience.type", hasItems(APPLICATION_AUDIENCE, ORGANIZATION_AUDIENCE));  // Ensure correct types
    }

    private UserShareRequestBodyUserCriteria getUserCriteria() {

        UserShareRequestBodyUserCriteria criteria = new UserShareRequestBodyUserCriteria();
        criteria.setUserIds(Collections.singletonList(rootOrgUserId));
        return criteria;
    }

    private RoleWithAudience createRoleWithAudience(String roleName, String display, String type) {

        RoleWithAudienceAudience audience = new RoleWithAudienceAudience();
        audience.setDisplay(display);
        audience.setType(type);

        RoleWithAudience roleWithAudience = new RoleWithAudience();
        roleWithAudience.setDisplayName(roleName);
        roleWithAudience.setAudience(audience);

        return roleWithAudience;
    }

    private void setupOrganizations() throws Exception {

        l1Org1Id = orgMgtRestClient.addOrganization(L1_ORG_1_NAME);
        l1Org2Id = orgMgtRestClient.addOrganization(L1_ORG_2_NAME);
        l2Org1Id = orgMgtRestClient.addSubOrganization(L2_ORG_1_NAME, l1Org1Id);
        l2Org2Id = orgMgtRestClient.addSubOrganization(L2_ORG_2_NAME, l1Org1Id);
        l2Org3Id = orgMgtRestClient.addSubOrganization(L2_ORG_3_NAME, l1Org2Id);
        l3Org1Id = orgMgtRestClient.addSubOrganization(L3_ORG_1_NAME, l2Org1Id);
    }

    private void setupTokens() throws Exception {

        l1Org1SwitchToken = orgMgtRestClient.switchM2MToken(l1Org1Id);
        l2Org1SwitchToken = orgMgtRestClient.switchM2MToken(l2Org1Id);
    }

    protected void setupApplicationsAndRoles() throws Exception {

        // Create a new application which consume application audience roles and share with all children.
        application1WithAppAudienceRoles = addApplication(APP_1_NAME);
        String app1Id = application1WithAppAudienceRoles.getId();
        OpenIDConnectConfiguration oidcConfigOfApp1 = oAuth2RestClient.getOIDCInboundDetails(app1Id);
        clientIdApp1 = oidcConfigOfApp1.getClientId();
        clientSecretApp1 = oidcConfigOfApp1.getClientSecret();
        createApp1RolesWithAppAudience(app1Id);
        // Mark roles and groups as requested claims for the app 1.
        updateRequestedClaimsOfApp(app1Id, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(app1Id);
        sharedApp1IdInLevel1Org =
                oAuth2RestClient.getAppIdUsingAppNameInOrganization(APP_1_NAME, l1Org1SwitchToken);

        // Create a new application which consume organization audience roles and share with all children.
        application2WithOrgAudienceRoles = addApplication(APP_2_NAME);
        String app2Id = application2WithOrgAudienceRoles.getId();
        OpenIDConnectConfiguration oidcConfigOfApp2 = oAuth2RestClient.getOIDCInboundDetails(app2Id);
        clientIdApp2 = oidcConfigOfApp2.getClientId();
        clientSecretApp2 = oidcConfigOfApp2.getClientSecret();
        createOrganizationRoles();
        switchApplicationAudience(app2Id, AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
        // Mark roles and groups as requested claims for the app 2.
        updateRequestedClaimsOfApp(app2Id, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(app2Id);
        sharedApp2IdInLevel1Org =
                oAuth2RestClient.getAppIdUsingAppNameInOrganization(APP_2_NAME, l1Org1SwitchToken);
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

    private void setupUsers() throws Exception {

        UserObject rootOrgUser = createUserObject(ROOT_ORG_USERNAME, ROOT_ORG_NAME);
        rootOrgUserId = scim2RestClient.createUser(rootOrgUser);

        UserObject l1Org1User = createUserObject(L1_ORG_1_USERNAME, L1_ORG_1_NAME);
        l1Org1UserId = scim2RestClient.createSubOrgUser(l1Org1User, l1Org1SwitchToken);
    }

    private String getSharedOrgsRolesRef(String userId, String orgId) {

        return "/api/server/v1" + USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ROLES_PATH + "?orgId=" + orgId;
    }

    private void deleteUserIfExists(String userId) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
    }

    private void deleteSubOrgUserIfExists(String userId, String organizationSwitchToken) throws Exception {

        if (userId != null) {
            scim2RestClient.deleteSubOrgUser(userId, organizationSwitchToken);
        }
    }

    private void deleteRoleIfExists(String roleId) throws Exception {

        if (roleId != null) {
            scim2RestClient.deleteV2Role(roleId);
        }
    }

    private void deleteApplicationIfExists(String appId) throws Exception {

        if (appId != null) {
            oAuth2RestClient.deleteApplication(appId);
        }
    }

    private void deleteSubOrganizationIfExists(String orgId, String parentId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteSubOrganization(orgId, parentId);
        }
    }

    private void deleteOrganizationIfExists(String orgId) throws Exception {

        if (orgId != null) {
            orgMgtRestClient.deleteOrganization(orgId);
        }
    }

    private void closeRestClients() throws IOException {

        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }



    @DataProvider(name = "selectiveSharingPoliciesWithRoles")
    public Object[][] selectiveSharingPoliciesWithRoles() {
        return new Object[][]{
                createTestCase(UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY,
                        new String[]{l1Org1Id},
                        new String[]{L1_ORG_1_NAME},
                        new RoleWithAudience[]{createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)},
                        createExpectedRoles(
                                L1_ORG_1_NAME, createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)
                                           ),
                        new String[]{l1Org2Id},
                        new String[]{L1_ORG_2_NAME},
                        new RoleWithAudience[]{createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)},
                        createExpectedRoles(
                                L1_ORG_2_NAME, createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)
                                           )
                              ),
                createTestCase(UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY,
                        new String[]{l1Org1Id, l2Org1Id, l2Org2Id, l3Org1Id},
                        new String[]{L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME},
                        new RoleWithAudience[]{
                                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                                createRoleWithAudience(ORG_ROLE_1, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)
                        },
                        createExpectedRoles(
                                L1_ORG_1_NAME, createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                                L2_ORG_1_NAME, createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                                L2_ORG_2_NAME, createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                                L3_ORG_1_NAME, createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)
                                           ),
                        new String[]{l1Org2Id, l2Org3Id},
                        new String[]{L1_ORG_2_NAME, L2_ORG_3_NAME},
                        new RoleWithAudience[]{
                                createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE),
                                createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)
                        },
                        createExpectedRoles(
                                L1_ORG_2_NAME, createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE),
                                L2_ORG_3_NAME, createRoleWithAudience(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)
                                           )
                              ),
                createTestCase(UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN,
                        new String[]{l1Org1Id, l2Org1Id, l2Org2Id},
                        new String[]{L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME},
                        new RoleWithAudience[]{createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)},
                        createExpectedRoles(
                                L1_ORG_1_NAME, createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                                L2_ORG_1_NAME, createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                                L2_ORG_2_NAME, createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)
                                           ),
                        new String[]{l1Org2Id, l2Org3Id},
                        new String[]{L1_ORG_2_NAME, L2_ORG_3_NAME},
                        new RoleWithAudience[]{},
                        createExpectedRoles(
                                L1_ORG_2_NAME, new RoleWithAudience[]{},
                                L2_ORG_3_NAME, new RoleWithAudience[]{}
                                           )
                              )
        };
    }


    @Test(dataProvider = "selectiveSharingPoliciesWithRoles")
    public void testSelectiveUserSharingWithRoles(
            UserShareRequestBodyOrganizations.PolicyEnum policy,
            String[] expectedOrgIdsForL1Org1, String[] expectedOrgNamesForL1Org1, RoleWithAudience[] rolesForL1Org1,
            Map<String, RoleWithAudience[]> expectedRolesForL1Org1,
            String[] expectedOrgIdsForL1Org2, String[] expectedOrgNamesForL1Org2, RoleWithAudience[] rolesForL1Org2,
            Map<String, RoleWithAudience[]> expectedRolesForL1Org2) throws Exception {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteria())
                .organizations(Arrays.asList(
                        new UserShareRequestBodyOrganizations()
                                .orgId(l1Org1Id)
                                .policy(policy)
                                .roles(Arrays.asList(rolesForL1Org1)),
                        new UserShareRequestBodyOrganizations()
                                .orgId(l1Org2Id)
                                .policy(policy)
                                .roles(Arrays.asList(rolesForL1Org2))
                                            ));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User sharing process triggered successfully."));

        // Validate shared organizations
        validateSharedOrganizations(expectedOrgIdsForL1Org1, expectedOrgNamesForL1Org1, expectedOrgIdsForL1Org2, expectedOrgNamesForL1Org2);

        // Validate roles in shared organizations
        validateSharedRoles(expectedOrgIdsForL1Org1, expectedRolesForL1Org1);
        validateSharedRoles(expectedOrgIdsForL1Org2, expectedRolesForL1Org2);
    }

    private void validateSharedOrganizations(String[] expectedOrgIds1, String[] expectedOrgNames1, String[] expectedOrgIds2, String[] expectedOrgNames2)
            throws Exception {
        testGetSharedOrganizationsWithAllWithoutPagination(expectedOrgIds1.length + expectedOrgIds2.length,
                Stream.concat(Arrays.stream(expectedOrgIds1), Arrays.stream(expectedOrgIds2)).toArray(String[]::new),
                Stream.concat(Arrays.stream(expectedOrgNames1), Arrays.stream(expectedOrgNames2)).toArray(String[]::new));
    }

    private void validateSharedRoles(String[] expectedOrgIds, Map<String, RoleWithAudience[]> expectedRoles) {
        for (String orgId : expectedOrgIds) {
            String orgName = getOrgNameById(orgId);
            testGetSharedRolesForOrgWithRolesWithoutPagination(orgId, orgName, expectedRoles.get(orgName));
        }
    }

    private Object[] createTestCase(UserShareRequestBodyOrganizations.PolicyEnum policy,
                                    String[] orgIds1, String[] orgNames1, RoleWithAudience[] roles1, Map<String, RoleWithAudience[]> expectedRoles1,
                                    String[] orgIds2, String[] orgNames2, RoleWithAudience[] roles2, Map<String, RoleWithAudience[]> expectedRoles2) {
        return new Object[]{policy, orgIds1, orgNames1, roles1, expectedRoles1, orgIds2, orgNames2, roles2, expectedRoles2};
    }

    private Map<String, RoleWithAudience[]> createExpectedRoles(Object... data) {
        Map<String, RoleWithAudience[]> roleMap = new HashMap<>();
        for (int i = 0; i < data.length; i += 2) {
            roleMap.put((String) data[i], new RoleWithAudience[]{(RoleWithAudience) data[i + 1]});
        }
        return roleMap;
    }

//    @Test(dataProvider = "selectiveSharingPoliciesWithRoles")
//    public void testSelectiveUserSharingWithRoles(
//            UserShareRequestBodyOrganizations.PolicyEnum policy,
//            String[] expectedOrgIdsForL1Org1, String[] expectedOrgNamesForL1Org1, RoleWithAudience[] rolesForL1Org1,
//            Map<String, RoleWithAudience[]> expectedRolesForL1Org1,
//            String[] expectedOrgIdsForL1Org2, String[] expectedOrgNamesForL1Org2, RoleWithAudience[] rolesForL1Org2,
//            Map<String, RoleWithAudience[]> expectedRolesForL1Org2)
//            throws Exception {
//
//        UserShareRequestBody requestBody = new UserShareRequestBody()
//                .userCriteria(getUserCriteria())
//                .organizations(Arrays.asList(
//                        new UserShareRequestBodyOrganizations()
//                                .orgId(l1Org1Id)
//                                .policy(policy)
//                                .roles(Arrays.asList(rolesForL1Org1)),
//                        new UserShareRequestBodyOrganizations()
//                                .orgId(l1Org2Id)
//                                .policy(policy)
//                                .roles(Arrays.asList(rolesForL1Org2))
//                                            ));
//
//        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody));
//
//        response.then()
//                .log().ifValidationFails()
//                .assertThat()
//                .statusCode(HttpStatus.SC_ACCEPTED)
//                .body("status", equalTo("Processing"))
//                .body("details", equalTo("User sharing process triggered successfully."));
//
//        Thread.sleep(5000); // Wait for sharing process to complete.
//
//        // Validate shared organizations
//        testGetSharedOrganizationsWithAllWithoutPagination(expectedOrgIdsForL1Org1.length+expectedOrgIdsForL1Org2.length,
//                Stream.concat(Arrays.stream(expectedOrgIdsForL1Org1),
//                                Arrays.stream(expectedOrgIdsForL1Org2))
//                        .toArray(String[]::new),
//                Stream.concat(Arrays.stream(expectedOrgNamesForL1Org1),
//                                Arrays.stream(expectedOrgNamesForL1Org2))
//                        .toArray(String[]::new));
//
//        // Validate roles in shared organizations
//        for (String orgId : expectedOrgIdsForL1Org1) {
//            String orgName = getOrgNameById(orgId);
//            testGetSharedRolesForOrgWithRolesWithoutPagination(orgId, orgName,
//                    expectedRolesForL1Org1.get(orgName));
//        }
//        for (String orgId : expectedOrgIdsForL1Org2) {
//            String orgName = getOrgNameById(orgId);
//            testGetSharedRolesForOrgWithRolesWithoutPagination(orgId, orgName,
//                    expectedRolesForL1Org2.get(orgName));
//        }
//    }

    public void testGetSharedRolesForOrgWithRolesWithoutPagination(String orgId, String orgName, RoleWithAudience[] expectedRoles) {

        Response response = getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + rootOrgUserId + SHARED_ROLES_PATH,
                Collections.singletonMap("orgId", orgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("links.size()", equalTo(1))
                .body("links[0].isEmpty()", equalTo(true))
                .body("roles", notNullValue())
                .body("roles.size()", equalTo(expectedRoles.length));

        if (expectedRoles.length > 0) {
            response.then()
                    .body("roles.displayName", hasItems(
                            Arrays.stream(expectedRoles)
                                    .map(RoleWithAudience::getDisplayName)
                                    .toArray(String[]::new)))
                    .body("roles.audience.display", hasItems(
                            Arrays.stream(expectedRoles)
                                    .map(role -> role.getAudience().getDisplay())
                                    .toArray(String[]::new)))// Now directly matching expected audiences
                    .body("roles.audience.type", hasItems(
                            Arrays.stream(expectedRoles)
                                    .map(role -> role.getAudience().getType())
                                    .toArray(String[]::new)));
        }
    }

    private String getOrgNameById(String orgId) {
        if (orgId.equals(l1Org1Id)) {
            return L1_ORG_1_NAME;
        } else if (orgId.equals(l1Org2Id)) {
            return L1_ORG_2_NAME;
        } else if (orgId.equals(l2Org1Id)) {
            return L2_ORG_1_NAME;
        } else if (orgId.equals(l2Org2Id)) {
            return L2_ORG_2_NAME;
        } else if (orgId.equals(l2Org3Id)) {
            return L2_ORG_3_NAME;
        } else if (orgId.equals(l3Org1Id)) {
            return L3_ORG_1_NAME;
        }
        return null;
    }


}
