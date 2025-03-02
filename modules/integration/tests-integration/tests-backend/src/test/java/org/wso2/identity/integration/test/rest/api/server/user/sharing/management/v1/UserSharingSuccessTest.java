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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    private Map<String, Map<String, Object>> userDetails = new HashMap<>();
    private Map<String, Map<String, Object>> orgDetails = new HashMap<>();
    private Map<String, Map<String, Object>> appDetails = new HashMap<>();
    private Map<String, Map<String, Object>> roleDetails = new HashMap<>();

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
        cleanUpRoles(APPLICATION_AUDIENCE, ORGANIZATION_AUDIENCE);
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

        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
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
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();

        // IMMEDIATE EXISTING
        List<String> userIdsForTestCase3 = Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Object> policyWithRolesForTestCase3 = setPolicyWithRolesForGeneralUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForGeneralUserSharingTestCase3();

        // ALL EXISTING AND FUTURE
        List<String> userIdsForTestCase4 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
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

        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY));
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
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
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();
        List<String> removingOrgIdsForTestCase1 = Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME));
        Map<String, Object> expectedUnsharedResultsForTestCase1 = setExpectedUnsharedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();
        List<String> removingOrgIdsForTestCase2 = Collections.singletonList(getOrgId(L1_ORG_1_NAME));
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
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME,
                APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME,
                        APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLES, Collections.emptyList());

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org3.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.emptyList());

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedUnsharedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

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
        addOrganization(L1_ORG_1_NAME);
        addOrganization(L1_ORG_2_NAME);
        addOrganization(L1_ORG_3_NAME);

        // Create Level 2 Organizations
        addSubOrganization(L2_ORG_1_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_2_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_3_NAME, getOrgId(L1_ORG_2_NAME), 2);

        // Create Level 3 Organization
        addSubOrganization(L3_ORG_1_NAME, getOrgId(L2_ORG_1_NAME), 3);
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

        Map<String, String> rootOrgOrganizationRoles = createOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2, ORG_ROLE_3));

        createApplication(APP_1_NAME, APPLICATION_AUDIENCE, Arrays.asList(APP_ROLE_1, APP_ROLE_2, APP_ROLE_3));
        createApplication(APP_2_NAME, ORGANIZATION_AUDIENCE, new ArrayList<>(rootOrgOrganizationRoles.keySet()));
    }

    private Map<String, Object> createApplication(String appName, String audience, List<String> roleNames) throws Exception{

        Map<String, Object> createdAppDetails = new HashMap<>();
        String rootOrgAppName = appName + "/" + ROOT_ORG_NAME;

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
            storeRoleDetails(APPLICATION_AUDIENCE, rootOrgAppName, roleIdsByName);
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

        appDetails.put(appName, createdAppDetails);
        return createdAppDetails;
    }


    private Map<String, Object> getAppDetailsOfSubOrg(String appName, String audience, List<String> roleNames, Map<String, Object> orgDetail) throws Exception {

        Map<String, Object> subOrgAppDetails = new HashMap<>();

        String subOrgName = (String) orgDetail.get("orgName");
        String subOrgId = (String) orgDetail.get("orgId");
        String subOrgSwitchToken = (String) orgDetail.get("orgSwitchToken");
        String subOrgAppName = appName + "/" + subOrgName;

        String subOrgAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgSwitchToken);

        Map<String, String> subOrgRoleIdsByName = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audience) ?
                getSubOrgRoleIdsByName(roleNames, APPLICATION_AUDIENCE, subOrgAppName, subOrgAppId, subOrgSwitchToken) :
                getSubOrgRoleIdsByName(roleNames,ORGANIZATION_AUDIENCE, subOrgName, subOrgId, subOrgSwitchToken);

        subOrgAppDetails.put("subOrgName", subOrgName);
        subOrgAppDetails.put("appName", appName);
        subOrgAppDetails.put("appId", subOrgAppId);
        subOrgAppDetails.put("roleNames", roleNames);
        subOrgAppDetails.put("roleIdsByName", subOrgRoleIdsByName);

        return subOrgAppDetails;
    }

    private Map<String, String> getSubOrgRoleIdsByName(List<String> roleNames, String audienceType, String audienceName, String audienceValue, String subOrgSwitchToken) throws Exception {

        Map<String, String> roleIdsByName = new HashMap<>();
        for (String roleName : roleNames) {
            String sharedAppRoleId =
                    scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(roleName, audienceValue, subOrgSwitchToken);
            roleIdsByName.put(roleName, sharedAppRoleId);
        }

        if (StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audienceType)) {
            storeRoleDetails(APPLICATION_AUDIENCE, audienceName, roleIdsByName);
        } else {
            storeRoleDetails(ORGANIZATION_AUDIENCE, audienceName, roleIdsByName);
        }

        return roleIdsByName;
    }

    private Map<String, String> createOrganizationRoles(String orgName, List<String> orgRoleNames) throws IOException {

        Map<String, String> orgRoleIdsByName = new HashMap<>();
        for (String orgRoleName : orgRoleNames) {
            RoleV2 orgRole = new RoleV2(null, orgRoleName, Collections.emptyList(), Collections.emptyList());
            String orgRoleId = scim2RestClient.addV2Role(orgRole);
            orgRoleIdsByName.put(orgRoleName, orgRoleId);
        }

        storeRoleDetails(ORGANIZATION_AUDIENCE, orgName, orgRoleIdsByName);

        return orgRoleIdsByName;
    }

    private void storeRoleDetails(String audienceType, String audienceName, Map<String, String> rolesOfAudience) {

        String key = StringUtils.equalsIgnoreCase(APPLICATION_AUDIENCE, audienceType)
                ? APPLICATION_AUDIENCE
                : ORGANIZATION_AUDIENCE;

        Map<String, Object> rolesMapOfAudienceType = new HashMap<>();
        rolesMapOfAudienceType.put(audienceName, rolesOfAudience);

        roleDetails.computeIfAbsent(key, k -> new HashMap<>()).putAll(rolesMapOfAudienceType);
    }

    private void setupUsers() throws Exception {

        createUser(createUserObject(ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME));

        createSuborgUser(createUserObject(L1_ORG_1_USER_1_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(L1_ORG_1_USER_2_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(L1_ORG_1_USER_3_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
    }

    private String createUser(UserObject user) throws Exception{

        String userId = scim2RestClient.createUser(user);

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", user.getUserName());
        userDetail.put("userId", userId);
        userDetail.put("isRootOrgUser", true);
        userDetail.put("orgName", ROOT_ORG_NAME);
        userDetail.put("orgId", ROOT_ORG_ID);
        userDetail.put("orgLevel", 1);

        userDetails.put(user.getUserName(), userDetail);
        return  userId;
    }

    private String createSuborgUser(UserObject user, String suborg) throws Exception{

        String userId = scim2RestClient.createSubOrgUser(user, (String) orgDetails.get(suborg).get("orgSwitchToken"));

        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", user.getUserName());
        userDetail.put("userId", userId);
        userDetail.put("isRootOrgUser", false);
        userDetail.put("orgName", suborg);
        userDetail.put("orgId", orgDetails.get(suborg).get("orgId"));
        userDetail.put("orgLevel", orgDetails.get(suborg).get("orgLevel"));

        userDetails.put(user.getUserName(), userDetail);
        return  userId;
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

    /**
     * Clean up users by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the users.
     */
    private void cleanUpUsers() throws Exception {

        deleteUserIfExists(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY));
        deleteUserIfExists(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY));
        deleteUserIfExists(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
        deleteSubOrgUserIfExists(getUserId(L1_ORG_1_USER_1_USERNAME, USER_DOMAIN_PRIMARY), (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        deleteSubOrgUserIfExists(getUserId(L1_ORG_1_USER_2_USERNAME, USER_DOMAIN_PRIMARY), (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
        deleteSubOrgUserIfExists(getUserId(L1_ORG_1_USER_3_USERNAME, USER_DOMAIN_PRIMARY), (String) orgDetails.get(L1_ORG_1_NAME).get("orgSwitchToken"));
    }

    /**
     * Cleans up roles for the specified audiences if exists.
     * Audiences will always be either ORGANIZATION_AUDIENCE or APPLICATION_AUDIENCE or both.
     *
     * <p>
     * The `@SuppressWarnings("unchecked")` annotation is used in this method because the values being cast are
     * predefined in the test data providers.
     * </p>
     * @param audiences The audiences for which roles need to be cleaned up.
     * @throws Exception If an error occurs during the cleanup process.
     */
    @SuppressWarnings("unchecked")
    private void cleanUpRoles(String... audiences) throws Exception {

        for(String audience : audiences) {
            Map<String, Object> orgWiseRolesOfAudience = roleDetails.get(audience);
            for (Map.Entry<String, Object> entry : orgWiseRolesOfAudience.entrySet()) {
                String audienceName = entry.getKey();
                Map<String, String> roles = (Map<String, String>) entry.getValue();
                for (Map.Entry<String, String> role : roles.entrySet()) {
                    String roleId = role.getValue();
                    if(audienceName.contains(ROOT_ORG_NAME)) {
                        deleteRoleIfExists(roleId);
                    }
                }
            }
        }
    }

    /**
     * Cleans up applications by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the applications.
     */
    private void cleanUpApplications() throws Exception {

        for (Map.Entry<String, Map<String, Object>> entry : appDetails.entrySet()) {
            Map<String, Object> details = entry.getValue();
            deleteApplicationIfExists(details.get("appId").toString());
        }
    }

    /**
     * Cleans up organizations by deleting them if they exist.
     *
     * @throws Exception If an error occurs while deleting the organizations.
     */
    private void cleanUpOrganizations() throws Exception {

        deleteSubOrganizationIfExists(getOrgId(L3_ORG_1_NAME), getOrgId(L2_ORG_1_NAME));
        deleteSubOrganizationIfExists(getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_2_NAME));
        deleteSubOrganizationIfExists(getOrgId(L2_ORG_2_NAME), getOrgId(L1_ORG_1_NAME));
        deleteSubOrganizationIfExists(getOrgId(L2_ORG_1_NAME), getOrgId(L1_ORG_1_NAME));
        deleteOrganizationIfExists(getOrgId(L1_ORG_3_NAME));
        deleteOrganizationIfExists(getOrgId(L1_ORG_2_NAME));
        deleteOrganizationIfExists(getOrgId(L1_ORG_1_NAME));
    }

    /**
     * Close the HTTP clients for OAuth2, SCIM2, and Organization Management.
     *
     * @throws IOException If an error occurred while closing the HTTP clients.
     */
    private void closeRestClients() throws IOException {

        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
    }
    
    private String getOrgId(String orgName) {
        
        return orgDetails.get(orgName).get("orgId").toString();
    }
    
    private String getUserId(String userName, String userDomain) {
        
        String domainQualifiedUserName = userDomain + "/" + userName;
        return userDetails.get(domainQualifiedUserName).get("userId").toString();
    }
}
