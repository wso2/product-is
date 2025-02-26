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
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN;

/**
 * Tests for successful cases of the User Sharing REST APIs.
 */
public class UserSharingSuccessTest extends UserSharingBaseTest {

    private String rootOrgUser1Id;
    private String rootOrgUser2Id;
    private String rootOrgUser3Id;
    private String l1Org1User1Id;
    private String l1Org1User2Id;
    private String l1Org1User3Id;


    private String l1Org1Id;
    private String l1Org2Id;
    private String l1Org3Id;
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

        cleanUpUsers();
        cleanUpRoles();
        cleanUpApplications();
        cleanUpOrganizations();
        closeRestClients();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    // Selective User Sharing.

    @DataProvider(name = "selectiveSharingPoliciesWithRoles")
    public Object[][] selectiveSharingPoliciesWithRoles() {

        List<String> userIdsForTestCase1 = Collections.singletonList(rootOrgUser1Id);
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        return new Object[][] {
                { userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1 }
        };
    }

    @Test(dataProvider = "selectiveSharingPoliciesWithRoles")
    public void testSelectiveUserSharingWithRoles(List<String> userIds,
                                                  Map<String, Map<String, Object>> organizations,
                                                  Map<String, Object> expectedResults) {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User sharing process triggered successfully."));

        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
        }
    }

    // General User Sharing.


    /**
     * Validate that the user has been shared to the expected organizations with the expected roles.
     *
     * @param userId          The ID of the user to validate.
     * @param expectedResults A map containing the expected results, including the expected organization count,
     *                        expected organization IDs, expected organization names, and expected roles per organization.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are predefined
     * in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private void validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(String userId, Map<String, Object> expectedResults) {

        testGetSharedOrganizations(
                userId,
                (int) expectedResults.get(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_COUNT),
                (List<String>) expectedResults.get(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_IDS),
                (List<String>) expectedResults.get(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_NAMES)
                                  );

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = (Map<String, List<RoleWithAudience>>) expectedResults.get(MAP_KEY_ORG_DETAILS_EXPECTED_ROLES_PER_EXPECTED_ORG);
        for (Map.Entry<String, List<RoleWithAudience>> entry : expectedRolesPerExpectedOrg.entrySet()) {
            testGetSharedRolesForOrg(userId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Test method for GET /user-sharing/{userId}/shared-organizations.
     *
     * @param userId           The ID of the user to get shared organizations for.
     * @param expectedOrgCount The expected number of shared organizations.
     * @param expectedOrgIds   The expected IDs of the shared organizations.
     * @param expectedOrgNames The expected names of the shared organizations.
     */
    public void testGetSharedOrganizations(String userId, int expectedOrgCount, List<String> expectedOrgIds, List<String> expectedOrgNames) {

        Response response =
                getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ORGANIZATIONS_PATH);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("links.size()", equalTo(1))
                .body("links[0].isEmpty()", equalTo(true))
                .body("sharedOrganizations", notNullValue())
                .body("sharedOrganizations.size()", equalTo(expectedOrgCount))
                .body("sharedOrganizations.orgId", hasItems(expectedOrgIds.toArray(new String[0])))
                .body("sharedOrganizations.orgName", hasItems(expectedOrgNames.toArray(new String[0])))
                .body("sharedOrganizations.sharedType", everyItem(equalTo("SHARED")))
                .body("sharedOrganizations.rolesRef", hasItems(
                        expectedOrgIds.stream()
                                .map(orgId -> getSharedOrgsRolesRef(userId, orgId))
                                .toArray(String[]::new)));
    }

    /**
     * Test method for GET /user-sharing/{userId}/shared-roles?orgId={orgId}.
     *
     * @param userId        The ID of the user to get shared roles for.
     * @param orgId         The ID of the organization to get shared roles for.
     * @param expectedRoles The expected roles for the user in the specified organization.
     */
    public void testGetSharedRolesForOrg(String userId, String orgId, List<RoleWithAudience> expectedRoles) {

        Response response = getResponseOfGet(USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ROLES_PATH,
                Collections.singletonMap(QUERY_PARAM_ORG_ID, orgId));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("links.size()", equalTo(1))
                .body("links[0].isEmpty()", equalTo(true))
                .body("roles", notNullValue())
                .body("roles.size()", equalTo(expectedRoles.size()));

        if (!expectedRoles.isEmpty()) {
            response.then()
                    .body("roles.displayName", hasItems(
                            expectedRoles.stream()
                                    .map(RoleWithAudience::getDisplayName)
                                    .toArray(String[]::new)))
                    .body("roles.audience.display", hasItems(
                            expectedRoles.stream()
                                    .map(role -> role.getAudience().getDisplay())
                                    .toArray(String[]::new)))
                    .body("roles.audience.type", hasItems(
                            expectedRoles.stream()
                                    .map(role -> role.getAudience().getType())
                                    .toArray(String[]::new)));
        }
    }

    /**
     * Creates a `UserShareRequestBodyUserCriteria` object with the given user IDs.
     *
     * @param userIds The list of user IDs to be included in the criteria.
     * @return A `UserShareRequestBodyUserCriteria` object containing the specified user IDs.
     */
    private UserShareRequestBodyUserCriteria getUserCriteriaForBaseUserSharing(List<String> userIds) {

        UserShareRequestBodyUserCriteria criteria = new UserShareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Converts a map of organization details into a list of `UserShareRequestBodyOrganizations` objects.
     *
     * @param organizations A map where the key is the organization name and the value is a map of organization details.
     * @return A list of `UserShareRequestBodyOrganizations` objects.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are predefined
     * in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private List<UserShareRequestBodyOrganizations> getOrganizationsForSelectiveUserSharing(Map<String, Map<String, Object>> organizations) {

        List<UserShareRequestBodyOrganizations> orgs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : organizations.entrySet()) {

            Map<String, Object> orgDetails = entry.getValue();

            UserShareRequestBodyOrganizations org = new UserShareRequestBodyOrganizations();
            org.setOrgId((String) orgDetails.get(MAP_KEY_ORG_DETAILS_ORG_ID));
            org.setPolicy((UserShareRequestBodyOrganizations.PolicyEnum) orgDetails.get(MAP_KEY_ORG_DETAILS_POLICY));
            org.setRoles((List<RoleWithAudience>) orgDetails.get(MAP_KEY_ORG_DETAILS_ROLES));

            orgs.add(org);
        }
        return orgs;
    }

    // Test cases builders for selective user sharing.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_ORG_DETAILS_ORG_ID, l1Org1Id);
        org1.put(MAP_KEY_ORG_DETAILS_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_ORG_DETAILS_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_ORG_DETAILS_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_ORG_DETAILS_ORG_ID, l1Org2Id);
        org2.put(MAP_KEY_ORG_DETAILS_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_ORG_DETAILS_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_ORG_DETAILS_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, SUPER_ORG, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_ORG_DETAILS_ORG_ID, l1Org3Id);
        org3.put(MAP_KEY_ORG_DETAILS_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_ORG_DETAILS_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_ORG_DETAILS_ROLES, Collections.emptyList());

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l2Org1Id, l2Org2Id, l3Org1Id, l1Org2Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_ORG_DETAILS_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org2Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l3Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org2Id, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org3Id, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.emptyList());

        expectedResults.put(MAP_KEY_ORG_DETAILS_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Helper Methods.

    private RoleWithAudience createRoleWithAudience(String roleName, String display, String type) {

        RoleWithAudienceAudience audience = new RoleWithAudienceAudience();
        audience.setDisplay(display);
        audience.setType(type);

        RoleWithAudience roleWithAudience = new RoleWithAudience();
        roleWithAudience.setDisplayName(roleName);
        roleWithAudience.setAudience(audience);

        return roleWithAudience;
    }

    private String getSharedOrgsRolesRef(String userId, String orgId) {

        return "/api/server/v1" + USER_SHARING_API_BASE_PATH + "/" + userId + SHARED_ROLES_PATH + "?orgId=" + orgId;
    }

    // Setup and cleanup methods.

    private void setupOrganizations() throws Exception {

        l1Org1Id = orgMgtRestClient.addOrganization(L1_ORG_1_NAME);
        l1Org2Id = orgMgtRestClient.addOrganization(L1_ORG_2_NAME);
        l1Org3Id = orgMgtRestClient.addOrganization(L1_ORG_3_NAME);
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

        UserObject rootOrgUser1 = createUserObject(ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME);
        rootOrgUser1Id = scim2RestClient.createUser(rootOrgUser1);
        UserObject rootOrgUser2 = createUserObject(ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME);
        rootOrgUser2Id = scim2RestClient.createUser(rootOrgUser2);
        UserObject rootOrgUser3 = createUserObject(ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME);
        rootOrgUser3Id = scim2RestClient.createUser(rootOrgUser3);

        UserObject l1Org1User1 = createUserObject(L1_ORG_1_USER_1_USERNAME, L1_ORG_1_NAME);
        l1Org1User1Id = scim2RestClient.createSubOrgUser(l1Org1User1, l1Org1SwitchToken);
        UserObject l1Org1User2 = createUserObject(L1_ORG_1_USER_2_USERNAME, L1_ORG_1_NAME);
        l1Org1User2Id = scim2RestClient.createSubOrgUser(l1Org1User2, l1Org1SwitchToken);
        UserObject l1Org1User3 = createUserObject(L1_ORG_1_USER_3_USERNAME, L1_ORG_1_NAME);
        l1Org1User3Id = scim2RestClient.createSubOrgUser(l1Org1User3, l1Org1SwitchToken);
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

    private void cleanUpUsers() throws Exception {

        deleteUserIfExists(rootOrgUser1Id);
        deleteUserIfExists(rootOrgUser2Id);
        deleteUserIfExists(rootOrgUser3Id);
        deleteSubOrgUserIfExists(l1Org1User1Id, l1Org1SwitchToken);
        deleteSubOrgUserIfExists(l1Org1User2Id, l1Org1SwitchToken);
        deleteSubOrgUserIfExists(l1Org1User3Id, l1Org1SwitchToken);
    }

    private void cleanUpRoles() throws Exception {

        deleteRoleIfExists(appRole1Id);
        deleteRoleIfExists(appRole2Id);
        deleteRoleIfExists(appRole3Id);
        deleteRoleIfExists(orgRole1Id);
        deleteRoleIfExists(orgRole2Id);
        deleteRoleIfExists(orgRole3Id);
    }

    private void cleanUpApplications() throws Exception {

        deleteApplicationIfExists(application1WithAppAudienceRoles.getId());
        deleteApplicationIfExists(application2WithOrgAudienceRoles.getId());
    }

    private void cleanUpOrganizations() throws Exception {

        deleteSubOrganizationIfExists(l3Org1Id, l2Org1Id);
        deleteSubOrganizationIfExists(l2Org3Id, l1Org2Id);
        deleteSubOrganizationIfExists(l2Org2Id, l1Org1Id);
        deleteSubOrganizationIfExists(l2Org1Id, l1Org1Id);
        deleteOrganizationIfExists(l1Org3Id);
        deleteOrganizationIfExists(l1Org2Id);
        deleteOrganizationIfExists(l1Org1Id);
    }

    private void closeRestClients() throws IOException {

        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }
}
