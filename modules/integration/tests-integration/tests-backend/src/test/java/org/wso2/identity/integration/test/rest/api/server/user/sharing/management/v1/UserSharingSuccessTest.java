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
import org.apache.commons.lang.StringUtils;
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
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareWithAllRequestBody;
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
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_ORGS_ONLY;

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

    private Map<String, Map<String, Object>> orgDetails = new HashMap<>();
    private String l1Org1Id;
    private String l1Org2Id;
    private String l1Org3Id;
    private String l2Org1Id;
    private String l2Org2Id;
    private String l2Org3Id;
    private String l3Org1Id;

    Map<String, Object> app1Details;
    Map<String, Object> app2Details;

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

    @DataProvider(name = "selectiveUserSharingDataProvider")
    public Object[][] selectiveUserSharingDataProvider() {

        List<String> userIdsForTestCase1 = Collections.singletonList(rootOrgUser1Id);
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        List<String> userIdsForTestCase2 = Arrays.asList(rootOrgUser1Id, rootOrgUser2Id, rootOrgUser3Id);
        Map<String, Map<String, Object>> organizationsForTestCase2 = setOrganizationsForSelectiveUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserSharingTestCase2();

        return new Object[][] {
                { userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1 },
                { userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2 }
        };
    }

    @Test(dataProvider = "selectiveUserSharingDataProvider")
    public void testSelectiveUserSharing(List<String> userIds,
                                                  Map<String, Map<String, Object>> organizations,
                                                  Map<String, Object> expectedResults) throws InterruptedException {

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

        Thread.sleep(5000);
        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
        }
    }

    // General User Sharing.

    @DataProvider(name = "generalUserSharingDataProvider")
    public Object[][] generalUserSharingDataProvider() {

        // ALL EXISTING
        List<String> userIdsForTestCase1 = Collections.singletonList(rootOrgUser1Id);
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 = Arrays.asList(rootOrgUser3Id, rootOrgUser2Id);
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();

        // IMMEDIATE EXISTING
        List<String> userIdsForTestCase3 = Collections.singletonList(rootOrgUser2Id);
        Map<String, Object> policyWithRolesForTestCase3 = setPolicyWithRolesForGeneralUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForGeneralUserSharingTestCase3();

        // ALL EXISTING AND FUTURE
        List<String> userIdsForTestCase4 = Arrays.asList(rootOrgUser1Id, rootOrgUser2Id, rootOrgUser3Id);
        Map<String, Object> policyWithRolesForTestCase4 = setPolicyWithRolesForGeneralUserSharingTestCase4();
        Map<String, Object> expectedResultsForTestCase4 = setExpectedResultsForGeneralUserSharingTestCase4();

        return new Object[][] {
                { userIdsForTestCase1, policyWithRolesForTestCase1, expectedResultsForTestCase1 },
                { userIdsForTestCase2, policyWithRolesForTestCase2, expectedResultsForTestCase2 },
                { userIdsForTestCase3, policyWithRolesForTestCase3, expectedResultsForTestCase3 },
                { userIdsForTestCase4, policyWithRolesForTestCase4, expectedResultsForTestCase4 }
        };
    }

    @Test(dataProvider = "generalUserSharingDataProvider")
    public void testGeneralUserSharing(List<String> userIds,
                                                  Map<String, Object> policyWithRoles,
                                                  Map<String, Object> expectedResults) throws InterruptedException {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoles))
                .roles(getRolesForGeneralUserSharing(policyWithRoles));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User sharing process triggered successfully."));

        Thread.sleep(5000);
        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
        }
    }

    // General User Unsharing.

    @DataProvider(name = "generalUserUnsharingDataProvider")
    public Object[][] generalUserUnsharingDataProvider() {

        List<String> userIdsForTestCase1 = Collections.singletonList(rootOrgUser1Id);
        List<String> userIdsForTestCase2 = Arrays.asList(rootOrgUser1Id, rootOrgUser2Id, rootOrgUser3Id);
        List<String> userIdsForTestCase3 = Collections.emptyList();
        Map<String, Object> expectedResultsForTestCase = setExpectedResultsForGeneralUserUnsharingTestCase1();

        return new Object[][] {
                { userIdsForTestCase1, expectedResultsForTestCase},
                { userIdsForTestCase2, expectedResultsForTestCase},
                { userIdsForTestCase3, expectedResultsForTestCase}
        };
    }

    @Test(dataProvider = "generalUserUnsharingDataProvider")
    public void testGeneralUserUnsharing(List<String> userIds,
                                       Map<String, Object> expectedResults) throws InterruptedException {

        UserUnshareWithAllRequestBody requestBody = new UserUnshareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User unsharing process triggered successfully."));

        Thread.sleep(5000);
        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
        }
    }

    // Selective User Unsharing.

    @DataProvider(name = "selectiveUserUnsharingDataProvider")
    public Object[][] selectiveUserUnsharingDataProvider() {

        // ALL EXISTING
        List<String> userIdsForTestCase1 = Collections.singletonList(rootOrgUser1Id);
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();
        List<String> removingOrgIdsForTestCase1 = Arrays.asList(l1Org1Id, l1Org2Id);
        Map<String, Object> expectedUnsharedResultsForTestCase1 = setExpectedUnsharedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 = Arrays.asList(rootOrgUser3Id, rootOrgUser2Id);
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();
        List<String> removingOrgIdsForTestCase2 = Collections.singletonList(l1Org1Id);
        Map<String, Object> expectedUnsharedResultsForTestCase2 = setExpectedUnsharedResultsForGeneralUserSharingTestCase2();

        return new Object[][] {
                { userIdsForTestCase1, policyWithRolesForTestCase1, expectedSharedResultsForTestCase1, removingOrgIdsForTestCase1, expectedUnsharedResultsForTestCase1},
                { userIdsForTestCase2, policyWithRolesForTestCase2, expectedSharedResultsForTestCase2, removingOrgIdsForTestCase2, expectedUnsharedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingDataProvider")
    public void testSelectiveUserUnsharing(List<String> userIds,
                                       Map<String, Object> policyWithRoles,
                                       Map<String, Object> expectedSharedResults,
                                       List<String> removingOrgIds,
                                       Map<String, Object> expectedUnsharedResults) throws InterruptedException {

        testGeneralUserSharing(userIds, policyWithRoles, expectedSharedResults);

        UserUnshareRequestBody requestBody = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds))
                .organizations(removingOrgIds);

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("status", equalTo("Processing"))
                .body("details", equalTo("User unsharing process triggered successfully."));

        Thread.sleep(5000);
        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedUnsharedResults);
        }
    }


    /**
     * Validate that the user has been shared to the expected organizations with the expected roles.
     *
     * @param userId          The ID of the user to validate.
     * @param expectedResults A map containing the expected results, including the expected organization count,
     *                        expected organization IDs, expected organization names, and expected roles per
     *                        organization.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private void validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(String userId, Map<String, Object> expectedResults) {

        testGetSharedOrganizations(
                userId,
                (int) expectedResults.get(MAP_KEY_EXPECTED_ORG_COUNT),
                (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_IDS),
                (List<String>) expectedResults.get(MAP_KEY_EXPECTED_ORG_NAMES)
                                  );

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = (Map<String, List<RoleWithAudience>>) expectedResults.get(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG);
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
     * Creates a `UserUnshareRequestBodyUserCriteria` object with the given user IDs.
     *
     * @param userIds The list of user IDs to be included in the criteria.
     * @return A `UserUnshareRequestBodyUserCriteria` object containing the specified user IDs.
     */
    private UserUnshareRequestBodyUserCriteria getUserCriteriaForBaseUserUnsharing(List<String> userIds) {

        UserUnshareRequestBodyUserCriteria criteria = new UserUnshareRequestBodyUserCriteria();
        criteria.setUserIds(userIds);
        return criteria;
    }

    /**
     * Converts a map of organization details into a list of `UserShareRequestBodyOrganizations` objects.
     *
     * @param organizations A map where the key is the organization name and the value is a map of organization details.
     * @return A list of `UserShareRequestBodyOrganizations` objects.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private List<UserShareRequestBodyOrganizations> getOrganizationsForSelectiveUserSharing(Map<String, Map<String, Object>> organizations) {

        List<UserShareRequestBodyOrganizations> orgs = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : organizations.entrySet()) {

            Map<String, Object> orgDetails = entry.getValue();

            UserShareRequestBodyOrganizations org = new UserShareRequestBodyOrganizations();
            org.setOrgId((String) orgDetails.get(MAP_KEY_SELECTIVE_ORG_ID));
            org.setPolicy((UserShareRequestBodyOrganizations.PolicyEnum) orgDetails.get(MAP_KEY_SELECTIVE_POLICY));
            org.setRoles((List<RoleWithAudience>) orgDetails.get(MAP_KEY_SELECTIVE_ROLES));

            orgs.add(org);
        }
        return orgs;
    }

    /**
     * Retrieves the policy enum for general user sharing from the provided map.
     *
     * @param policyWithRoles A map containing the policy and roles for general user sharing.
     * @return The policy enum for general user sharing.
     */
    private UserShareWithAllRequestBody.PolicyEnum getPolicyEnumForGeneralUserSharing(Map<String, Object> policyWithRoles) {

        return (UserShareWithAllRequestBody.PolicyEnum)policyWithRoles.get(MAP_KEY_GENERAL_POLICY) ;
    }

    /**
     * Retrieves the roles for general user sharing from the provided map.
     *
     * @param policyWithRoles A map containing the policy and roles for general user sharing.
     * @return A list of `RoleWithAudience` objects representing the roles for general user sharing.
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     */
    @SuppressWarnings("unchecked")
    private List<RoleWithAudience> getRolesForGeneralUserSharing(Map<String, Object> policyWithRoles) {

        return (List<RoleWithAudience>) policyWithRoles.get(MAP_KEY_GENERAL_ROLES);
    }

    // Test cases builders for selective user sharing.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, l1Org1Id);
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME,
                APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, l1Org2Id);
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME,
                        APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, l1Org3Id);
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLES, Collections.emptyList());

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l2Org1Id, l2Org2Id, l3Org1Id, l1Org2Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org2Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l3Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org2Id, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org3Id, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, l1Org2Id);
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, l1Org3Id);
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org3.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org2Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org2Id, Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org3Id, Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase1() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l2Org1Id, l2Org2Id, l3Org1Id, l1Org2Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org2Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l3Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org2Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org3Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase2() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l1Org2Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org2Id, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase3() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l1Org2Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org2Id, Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase4() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Collections.emptyList());

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase4() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org1Id, l2Org1Id, l2Org2Id, l3Org1Id, l1Org2Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org1Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l2Org1Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l2Org2Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l3Org1Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l1Org2Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l2Org3Id, Collections.emptyList());
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserUnsharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedUnsharedResultsForGeneralUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 5);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l2Org1Id, l2Org2Id, l3Org1Id, l2Org3Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l2Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org2Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l3Org1Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l2Org3Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedUnsharedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(l1Org2Id, l1Org3Id));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(l1Org2Id, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(l1Org3Id, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

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

        // Create Level 1 Organizations
        l1Org1Id = addOrganization(L1_ORG_1_NAME);
        l1Org2Id = addOrganization(L1_ORG_2_NAME);
        l1Org3Id = addOrganization(L1_ORG_3_NAME);

        // Create Level 2 Organizations
        l2Org1Id = addSubOrganization(L2_ORG_1_NAME, l1Org1Id, 2);
        l2Org2Id = addSubOrganization(L2_ORG_2_NAME, l1Org1Id, 2);
        l2Org3Id = addSubOrganization(L2_ORG_3_NAME, l1Org2Id, 2);

        // Create Level 3 Organization
        l3Org1Id = addSubOrganization(L3_ORG_1_NAME, l2Org1Id, 3);
    }

    private String addOrganization(String orgName) throws Exception {
        String orgId = orgMgtRestClient.addOrganization(orgName);
        setOrgDetails(orgName, orgId, ROOT_ORG_ID, 1);
        return orgId;
    }

    private String addSubOrganization(String orgName, String parentId, int orgLevel) throws Exception {
        String orgId = orgMgtRestClient.addSubOrganization(orgName, parentId);
        setOrgDetails(orgName, orgId, parentId, orgLevel);
        return orgId;
    }

    private void setOrgDetails(String orgName, String orgId, String parentId, int orgLevel) throws Exception {

        Map<String, Object> orgDetail = new HashMap<>();
        orgDetail.put("orgName", orgName);
        orgDetail.put("orgId", orgId);
        orgDetail.put("parentOrgId", parentId);
        orgDetail.put("orgSwitchToken", orgMgtRestClient.switchM2MToken(orgId));
        orgDetail.put("orgLevel", orgLevel);
        orgDetails.put(orgName, orgDetail);

    }

    protected void setupApplicationsAndRoles() throws Exception {

        Map<String, String> organizationRoles = createOrganizationRoles();
        app1Details = createApplication(APP_1_NAME, APPLICATION_AUDIENCE, Arrays.asList(APP_ROLE_1, APP_ROLE_2, APP_ROLE_3));
        app2Details = createApplication(APP_2_NAME, ORGANIZATION_AUDIENCE, new ArrayList<>(organizationRoles.keySet()));
    }

    private Map<String, Object> createApplication(String appName, String audience, List<String> roleNames) throws Exception{

        Map<String, Object> createdAppDetails = new HashMap<>();

        ApplicationResponseModel application = addApplication(appName);
        String appId = application.getId();
        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(appId);
        String clientId = oidcConfig.getClientId();
        String clientSecret = oidcConfig.getClientSecret();
        Map<String, String> roleIdsByName = new HashMap<>();

        if (StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience)){

            Audience appRoleAudience = new Audience(APPLICATION_AUDIENCE, appId);
            for (String roleName : roleNames) {
                RoleV2 appRole = new RoleV2(appRoleAudience, roleName, Collections.emptyList(), Collections.emptyList());
                String roleId = scim2RestClient.addV2Role(appRole);
                roleIdsByName.put(roleName, roleId);
            }
            createdAppDetails.put("appAudience", APPLICATION_AUDIENCE);

        } else {

            switchApplicationAudience(appId, AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);

            for (String roleName: roleNames){
                String roleId = scim2RestClient.getRoleIdByName(roleName);
                roleIdsByName.put(roleName, roleId);
            }
            createdAppDetails.put("appAudience", ORGANIZATION_AUDIENCE);
        }

        // Mark roles and groups as requested claims for the app 2.
        updateRequestedClaimsOfApp(appId, getClaimConfigurationsWithRolesAndGroups());
        shareApplication(appId);

        Map<String, Object> appDetailsOfSubOrgs = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : orgDetails.entrySet()) {
            String orgName = entry.getKey();
            Map<String, Object> orgDetail = entry.getValue();

            Map<String, Object> appDetailsOfSubOrg = getAppDetailsOfSubOrg(appName, audience, roleNames, orgDetail);
            appDetailsOfSubOrgs.put(orgName, appDetailsOfSubOrg);
        }

        createdAppDetails.put("appName", appName);
        createdAppDetails.put("appId", appId);
        createdAppDetails.put("clientId", clientId);
        createdAppDetails.put("clientSecret", clientSecret);
        createdAppDetails.put("roleNames", roleNames);
        createdAppDetails.put("roleIdsByName", roleIdsByName);
        createdAppDetails.put("appDetailsOfSubOrgs", appDetailsOfSubOrgs);

        return createdAppDetails;
    }


    private Map<String, Object> getAppDetailsOfSubOrg(String appName, String audience, List<String> roleNames,
                                                      Map<String, Object> orgDetail) throws Exception {

        Map<String, Object> subOrgAppDetails = new HashMap<>();

        String subOrgName = (String) orgDetail.get("orgName");
        String subOrgId = (String) orgDetail.get("orgId");
        String subOrgSwitchToken = (String) orgDetail.get("orgSwitchToken");

        String subOrgAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgSwitchToken);

        Map<String, String> subOrgRoleIdsByName = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience) ?
                getSubOrgRoleIdsByName(roleNames, subOrgAppId, subOrgSwitchToken) :
                getSubOrgRoleIdsByName(roleNames, subOrgId, subOrgSwitchToken);

        subOrgAppDetails.put("subOrgName", subOrgName);
        subOrgAppDetails.put("appName", appName);
        subOrgAppDetails.put("appId", subOrgAppId);
        subOrgAppDetails.put("roleNames", roleNames);
        subOrgAppDetails.put("roleIdsByName", subOrgRoleIdsByName);

        return subOrgAppDetails;
    }

    private Map<String, String> getSubOrgRoleIdsByName (List<String> roleNames, String audienceValue, String subOrgSwitchToken) throws Exception {

        Map<String, String> roleIdsByName = new HashMap<>();
        for (String roleName : roleNames) {
            String sharedAppRoleId =
                    scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(roleName, audienceValue, subOrgSwitchToken);
            roleIdsByName.put(roleName, sharedAppRoleId);
        }
        return roleIdsByName;
    }

    private Map<String, String> createOrganizationRoles() throws IOException {

        Map<String, String> orgRoleIdsByName = new HashMap<>();

        RoleV2 orgRole1 = new RoleV2(null, ORG_ROLE_1, Collections.emptyList(), Collections.emptyList());
        orgRole1Id = scim2RestClient.addV2Role(orgRole1);
        RoleV2 orgRole2 = new RoleV2(null, ORG_ROLE_2, Collections.emptyList(), Collections.emptyList());
        orgRole2Id = scim2RestClient.addV2Role(orgRole2);
        RoleV2 orgRole3 = new RoleV2(null, ORG_ROLE_3, Collections.emptyList(), Collections.emptyList());
        orgRole3Id = scim2RestClient.addV2Role(orgRole3);

        orgRoleIdsByName.put(ORG_ROLE_1, orgRole1Id);
        orgRoleIdsByName.put(ORG_ROLE_2, orgRole2Id);
        orgRoleIdsByName.put(ORG_ROLE_3, orgRole3Id);

        return orgRoleIdsByName;
    }

    private void setupUsers() throws Exception {

        UserObject rootOrgUser1 = createUserObject(ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME);
        rootOrgUser1Id = scim2RestClient.createUser(rootOrgUser1);
        UserObject rootOrgUser2 = createUserObject(ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME);
        rootOrgUser2Id = scim2RestClient.createUser(rootOrgUser2);
        UserObject rootOrgUser3 = createUserObject(ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME);
        rootOrgUser3Id = scim2RestClient.createUser(rootOrgUser3);

        UserObject l1Org1User1 = createUserObject(L1_ORG_1_USER_1_USERNAME, L1_ORG_1_NAME);
        l1Org1User1Id = scim2RestClient.createSubOrgUser(l1Org1User1, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        UserObject l1Org1User2 = createUserObject(L1_ORG_1_USER_2_USERNAME, L1_ORG_1_NAME);
        l1Org1User2Id = scim2RestClient.createSubOrgUser(l1Org1User2, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        UserObject l1Org1User3 = createUserObject(L1_ORG_1_USER_3_USERNAME, L1_ORG_1_NAME);
        l1Org1User3Id = scim2RestClient.createSubOrgUser(l1Org1User3, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
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
        deleteSubOrgUserIfExists(l1Org1User1Id, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        deleteSubOrgUserIfExists(l1Org1User2Id, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        deleteSubOrgUserIfExists(l1Org1User3Id, (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
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

        deleteApplicationIfExists(app1Details.get("appId").toString());
        deleteApplicationIfExists(app2Details.get("appId").toString());
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
    
    private String getOrgId(String orgName) {
        
        return orgDetails.get(orgName).get("orgId").toString();
    }
}
