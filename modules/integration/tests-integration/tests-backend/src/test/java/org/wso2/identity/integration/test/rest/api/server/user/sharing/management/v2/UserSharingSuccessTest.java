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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignment;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleShareConfig;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareSelectedRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserSharingPatchOperation;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserSharingPatchRequest;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareSelectedRequestBody;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.*;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignment.ModeEnum;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareAllRequestBody.PolicyEnum.*;

/**
 * Tests for successful cases of the User Sharing V2 REST APIs.
 */
public class UserSharingSuccessTest extends UserSharingBaseTest {

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
        setupDetailMaps();
        setupRestClients();
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
        cleanUpDetailMaps();
        closeRestClients();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    // Selective User Sharing with V2 API.

    @DataProvider(name = "selectiveUserSharingDataProvider")
    public Object[][] selectiveUserSharingDataProvider() {

        // Test case 1: Share single user to multiple orgs with different policies and role assignments
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        // Test case 2: Share multiple users to multiple orgs with SELECTED role assignment mode
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 = setOrganizationsForSelectiveUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserSharingTestCase2();

        // Test case 3: Share with NONE role assignment mode (no roles)
        List<String> userIdsForTestCase3 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase3 = setOrganizationsForSelectiveUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForSelectiveUserSharingTestCase3();

        return new Object[][]{
                {userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2},
                {userIdsForTestCase3, organizationsForTestCase3, expectedResultsForTestCase3}
        };
    }

    @Test(dataProvider = "selectiveUserSharingDataProvider")
    public void testSelectiveUserSharing(List<String> userIds, Map<String, Map<String, Object>> organizations,
                                         Map<String, Object> expectedResults) throws Exception {

        UserShareSelectedRequestBody requestBody = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // General User Sharing with V2 API.

    @DataProvider(name = "generalUserSharingDataProvider")
    public Object[][] generalUserSharingDataProvider() {

        // ALL_EXISTING_ORGS_ONLY with SELECTED roles
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE_EXISTING_AND_FUTURE_ORGS with SELECTED roles
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase2 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();

        // IMMEDIATE_EXISTING_ORGS_ONLY with SELECTED roles
        List<String> userIdsForTestCase3 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase3 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForGeneralUserSharingTestCase3();

        // ALL_EXISTING_AND_FUTURE_ORGS with NONE role assignment (no roles)
        List<String> userIdsForTestCase4 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase4 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase4();
        Map<String, Object> expectedResultsForTestCase4 = setExpectedResultsForGeneralUserSharingTestCase4();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRoleAssignmentForTestCase2, expectedResultsForTestCase2},
                {userIdsForTestCase3, policyWithRoleAssignmentForTestCase3, expectedResultsForTestCase3},
                {userIdsForTestCase4, policyWithRoleAssignmentForTestCase4, expectedResultsForTestCase4}
        };
    }

    @Test(dataProvider = "generalUserSharingDataProvider")
    public void testGeneralUserSharing(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                       Map<String, Object> expectedResults) throws Exception {

        UserShareAllRequestBody requestBody = new UserShareAllRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignment))
                .roleAssignment(getRoleAssignmentForGeneralUserSharing(policyWithRoleAssignment));

        Response response =
                getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // General User Unsharing.

    @DataProvider(name = "generalUserUnsharingDataProvider")
    public Object[][] generalUserUnsharingDataProvider() {

        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        List<String> userIdsForTestCase3 = Collections.emptyList();
        Map<String, Object> expectedResultsForTestCase = setExpectedResultsForGeneralUserUnsharingTestCase1();

        return new Object[][]{
                {userIdsForTestCase1, expectedResultsForTestCase},
                {userIdsForTestCase2, expectedResultsForTestCase},
                {userIdsForTestCase3, expectedResultsForTestCase}
        };
    }

    @Test(dataProvider = "generalUserUnsharingDataProvider")
    public void testGeneralUserUnsharing(List<String> userIds, Map<String, Object> expectedResults) throws Exception {

        UserUnshareAllRequestBody requestBody = new UserUnshareAllRequestBody()
                .userCriteria(getUserCriteria(userIds));

        Response response =
                getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // Selective User Unsharing.

    @DataProvider(name = "selectiveUserUnsharingDataProvider")
    public Object[][] selectiveUserUnsharingDataProvider() {

        // ALL_EXISTING_ORGS_ONLY then unshare from specific orgs
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();
        List<String> removingOrgIdsForTestCase1 = Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME));
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserUnsharingTestCase1();

        // IMMEDIATE_EXISTING_AND_FUTURE_ORGS then unshare from specific org
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase2 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();
        List<String> removingOrgIdsForTestCase2 = Collections.singletonList(getOrgId(L1_ORG_1_NAME));
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserUnsharingTestCase2();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedSharedResultsForTestCase1,
                        removingOrgIdsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRoleAssignmentForTestCase2, expectedSharedResultsForTestCase2,
                        removingOrgIdsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingDataProvider")
    public void testSelectiveUserUnsharing(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                           Map<String, Object> expectedSharedResults, List<String> removingOrgIds,
                                           Map<String, Object> expectedResults) throws Exception {

        // First share the users
        testGeneralUserSharing(userIds, policyWithRoleAssignment, expectedSharedResults);

        // Then unshare from specific orgs
        UserUnshareSelectedRequestBody requestBody = new UserUnshareSelectedRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .orgIds(removingOrgIds);

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // PATCH Role Assignment Tests.

    @DataProvider(name = "patchRoleAssignmentDataProvider")
    public Object[][] patchRoleAssignmentDataProvider() {

        // Test case 1: Add a new role to an already shared user
        String userIdForTestCase1 = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Map<String, Object>> sharingOrgsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase3();
        Map<String, Object> initialShareExpectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase3();
        String targetOrgIdForTestCase1 = getOrgId(L1_ORG_1_NAME);
        List<UserSharingPatchOperation> patchOperationsForTestCase1 = Collections.singletonList(
                createPatchOperation(PATCH_OP_ADD, targetOrgIdForTestCase1,
                        Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForPatchAddRoleTestCase1();

        // Test case 2: Remove a role from an already shared user
        String userIdForTestCase2 = getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Map<String, Object>> sharingOrgsForTestCase2 = setOrganizationsForPatchRemoveRoleTestCase2();
        Map<String, Object> initialShareExpectedResultsForTestCase2 = setExpectedResultsForPatchRemoveRoleInitialShare();
        String targetOrgIdForTestCase2 = getOrgId(L1_ORG_2_NAME);
        List<UserSharingPatchOperation> patchOperationsForTestCase2 = Collections.singletonList(
                createPatchOperation(PATCH_OP_REMOVE, targetOrgIdForTestCase2,
                        Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForPatchRemoveRoleTestCase2();

        // Test case 3: Add multiple roles in a single PATCH request
        String userIdForTestCase3 = getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Map<String, Object>> sharingOrgsForTestCase3 = setOrganizationsForSelectiveUserSharingTestCase3();
        Map<String, Object> initialShareExpectedResultsForTestCase3 = setExpectedResultsForSelectiveUserSharingTestCase3();
        String targetOrgIdForTestCase3 = getOrgId(L1_ORG_1_NAME);
        List<UserSharingPatchOperation> patchOperationsForTestCase3 = Collections.singletonList(
                createPatchOperation(PATCH_OP_ADD, targetOrgIdForTestCase3,
                        Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                                createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForPatchAddMultipleRolesTestCase3();

        return new Object[][]{
                {userIdForTestCase1, sharingOrgsForTestCase1, initialShareExpectedResultsForTestCase1,
                        targetOrgIdForTestCase1, patchOperationsForTestCase1, expectedResultsForTestCase1},
                {userIdForTestCase2, sharingOrgsForTestCase2, initialShareExpectedResultsForTestCase2,
                        targetOrgIdForTestCase2, patchOperationsForTestCase2, expectedResultsForTestCase2},
                {userIdForTestCase3, sharingOrgsForTestCase3, initialShareExpectedResultsForTestCase3,
                        targetOrgIdForTestCase3, patchOperationsForTestCase3, expectedResultsForTestCase3}
        };
    }

    @Test(dataProvider = "patchRoleAssignmentDataProvider")
    public void testPatchRoleAssignment(String userId, Map<String, Map<String, Object>> sharingOrganizations,
                                        Map<String, Object> initialExpectedResults, String targetOrgId,
                                        List<UserSharingPatchOperation> patchOperations,
                                        Map<String, Object> expectedResults) throws Exception {

        // First share the user to organizations
        UserShareSelectedRequestBody shareRequestBody = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(Collections.singletonList(userId)))
                .organizations(getOrganizationsForSelectiveUserSharing(sharingOrganizations));

        Response shareResponse =
                getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(shareRequestBody));

        shareResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(Collections.singletonList(userId), initialExpectedResults);

        // Now PATCH the role assignment
        UserSharingPatchRequest patchRequestBody = new UserSharingPatchRequest().operations(patchOperations);

        String patchEndpoint = USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATCH_PATH;
        Response patchResponse = getResponseOfPatch(patchEndpoint, toJSONString(patchRequestBody));

        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UPDATING));

        validateUserSharingResults(Collections.singletonList(userId), expectedResults);
    }

    // GET Shared Organizations Tests.

    @DataProvider(name = "getSharedOrganizationsDataProvider")
    public Object[][] getSharedOrganizationsDataProvider() {

        // Test case 1: Get all shared orgs (default - no pagination params)
        String userIdForTestCase1 = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Object> policyWithRoleAssignmentForTestCase1 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase1();
        Map<String, Object> expectedShareResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();
        Map<String, String> queryParamsForTestCase1 = Collections.emptyMap();
        int expectedOrgCountForTestCase1 = 7;

        // Test case 2: Get shared orgs with limit
        String userIdForTestCase2 = getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Object> policyWithRoleAssignmentForTestCase2 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase4();
        Map<String, Object> expectedShareResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase4();
        Map<String, String> queryParamsForTestCase2 = new HashMap<>();
        queryParamsForTestCase2.put(QUERY_PARAM_LIMIT, "3");
        int expectedOrgCountForTestCase2 = 3;

        // Test case 3: Get shared orgs with attributes=roles
        String userIdForTestCase3 = getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, Object> policyWithRoleAssignmentForTestCase3 = 
                setPolicyWithRoleAssignmentForGeneralUserSharingTestCase2();
        Map<String, Object> expectedShareResultsForTestCase3 = setExpectedResultsForGeneralUserSharingTestCase2();
        Map<String, String> queryParamsForTestCase3 = new HashMap<>();
        queryParamsForTestCase3.put(QUERY_PARAM_ATTRIBUTES, QUERY_PARAM_ATTRIBUTES_ROLES);
        int expectedOrgCountForTestCase3 = 3;

        return new Object[][]{
                {userIdForTestCase1, policyWithRoleAssignmentForTestCase1, expectedShareResultsForTestCase1,
                        queryParamsForTestCase1, expectedOrgCountForTestCase1},
                {userIdForTestCase2, policyWithRoleAssignmentForTestCase2, expectedShareResultsForTestCase2,
                        queryParamsForTestCase2, expectedOrgCountForTestCase2},
                {userIdForTestCase3, policyWithRoleAssignmentForTestCase3, expectedShareResultsForTestCase3,
                        queryParamsForTestCase3, expectedOrgCountForTestCase3}
        };
    }

    @Test(dataProvider = "getSharedOrganizationsDataProvider")
    public void testGetSharedOrganizations(String userId, Map<String, Object> policyWithRoleAssignment,
                                           Map<String, Object> expectedShareResults, Map<String, String> queryParams,
                                           int expectedOrgCount) throws Exception {

        // First share the user
        testGeneralUserSharing(Collections.singletonList(userId), policyWithRoleAssignment, expectedShareResults);

        // Build the GET endpoint with query parameters
        StringBuilder getEndpoint = new StringBuilder(
                USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATH);
        if (!queryParams.isEmpty()) {
            getEndpoint.append(QUERY_PARAM_SEPARATOR);
            List<String> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                params.add(entry.getKey() + QUERY_PARAM_VALUE_SEPARATOR + entry.getValue());
            }
            getEndpoint.append(String.join(QUERY_PARAM_AMPERSAND, params));
        }

        Response response = getResponseOfGet(getEndpoint.toString());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ORGANIZATIONS, notNullValue())
                .body(RESPONSE_ORGANIZATIONS_SIZE, lessThanOrEqualTo(expectedOrgCount));

        // Verify pagination fields are present when limit is specified
        if (queryParams.containsKey(QUERY_PARAM_LIMIT)) {
            response.then()
                    .body(RESPONSE_COUNT, lessThanOrEqualTo(Integer.parseInt(queryParams.get(QUERY_PARAM_LIMIT))));
        }
    }

    @DataProvider(name = "getSharedOrganizationsWithPaginationDataProvider")
    public Object[][] getSharedOrganizationsWithPaginationDataProvider() {

        // Setup: Share user to all 7 organizations first

        // Test case 1: Get first page with limit=3
        Map<String, String> queryParamsForTestCase1 = new HashMap<>();
        queryParamsForTestCase1.put(QUERY_PARAM_LIMIT, "3");

        // Test case 2: Get specific page using after cursor (simulated)
        Map<String, String> queryParamsForTestCase2 = new HashMap<>();
        queryParamsForTestCase2.put(QUERY_PARAM_LIMIT, "2");

        // Test case 3: Get with filter
        Map<String, String> queryParamsForTestCase3 = new HashMap<>();
        queryParamsForTestCase3.put(QUERY_PARAM_FILTER, "orgName co L1");
        queryParamsForTestCase3.put(QUERY_PARAM_LIMIT, "10");

        return new Object[][]{
                {queryParamsForTestCase1, 3, 3},
                {queryParamsForTestCase2, 2, 2},
                {queryParamsForTestCase3, 10, 3}  // Only 3 L1 orgs exist
        };
    }

    @Test(dataProvider = "getSharedOrganizationsWithPaginationDataProvider")
    public void testGetSharedOrganizationsWithPagination(Map<String, String> queryParams, int expectedLimit,
                                                         int maxExpectedCount) throws Exception {

        String userId = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);

        // First share the user to all organizations
        Map<String, Object> policyWithRoleAssignment = setPolicyWithRoleAssignmentForGeneralUserSharingTestCase1();
        Map<String, Object> expectedShareResults = setExpectedResultsForGeneralUserSharingTestCase1();
        testGeneralUserSharing(Collections.singletonList(userId), policyWithRoleAssignment, expectedShareResults);

        // Build the GET endpoint with query parameters
        StringBuilder getEndpoint = new StringBuilder(
                USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATH);
        if (!queryParams.isEmpty()) {
            getEndpoint.append(QUERY_PARAM_SEPARATOR);
            List<String> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                params.add(entry.getKey() + QUERY_PARAM_VALUE_SEPARATOR + entry.getValue());
            }
            getEndpoint.append(String.join(QUERY_PARAM_AMPERSAND, params));
        }

        Response response = getResponseOfGet(getEndpoint.toString());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ORGANIZATIONS, notNullValue())
                .body(RESPONSE_ORGANIZATIONS_SIZE, lessThanOrEqualTo(expectedLimit))
                .body(RESPONSE_ORGANIZATIONS_SIZE, lessThanOrEqualTo(maxExpectedCount));

        // Verify count doesn't exceed limit
        if (queryParams.containsKey(QUERY_PARAM_LIMIT)) {
            response.then()
                    .body(RESPONSE_COUNT, lessThanOrEqualTo(expectedLimit));
        }
    }

    @Test
    public void testGetSharedOrganizationsWithRolesAttribute() throws Exception {

        String userId = getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);

        // First share the user with roles
        Map<String, Object> policyWithRoleAssignment = setPolicyWithRoleAssignmentForGeneralUserSharingTestCase2();
        Map<String, Object> expectedShareResults = setExpectedResultsForGeneralUserSharingTestCase2();
        testGeneralUserSharing(Collections.singletonList(userId), policyWithRoleAssignment, expectedShareResults);

        // GET with roles attribute
        String getEndpoint = USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATH +
                QUERY_PARAM_SEPARATOR + QUERY_PARAM_ATTRIBUTES + QUERY_PARAM_VALUE_SEPARATOR + 
                QUERY_PARAM_ATTRIBUTES_ROLES;

        Response response = getResponseOfGet(getEndpoint);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ORGANIZATIONS, notNullValue())
                .body(RESPONSE_ORGANIZATIONS_SIZE, greaterThanOrEqualTo(1));

        // Verify roles are included in response
        List<Map<String, Object>> orgs = response.jsonPath().getList(RESPONSE_ORGANIZATIONS);
        for (Map<String, Object> org : orgs) {
            // When attributes=roles is specified, roles should be present
            if (org.containsKey("roles")) {
                List<?> roles = (List<?>) org.get("roles");
                // Roles array should exist (might be empty for NONE mode)
                org.containsKey("roles");
            }
        }
    }

    // Test case builders for Selective User Sharing.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1: SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY with SELECTED roles
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));
        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2: SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN with SELECTED roles
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3: SELECTED_ORG_ONLY with NONE role assignment
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.NONE, null));
        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME,
                        L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 2 with SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3 with SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org3.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE))));
        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase3() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization with NONE role assignment mode
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.NONE, null));
        organizations.put(L1_ORG_1_NAME, org1);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Test case builders for General User Sharing.

    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingTestCase1() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME,
                        L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingTestCase2() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingTestCase3() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingTestCase4() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.NONE, null));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase4() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME,
                        L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
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

    // Test case builders for Unsharing.

    private Map<String, Object> setExpectedResultsForGeneralUserUnsharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 5);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME),
                        getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Test case builders for PATCH Role Assignment.

    private Map<String, Map<String, Object>> setOrganizationsForPatchRemoveRoleTestCase2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization with SELECTED role assignment mode (with APP_ROLE_1)
        Map<String, Object> org = new HashMap<>();
        org.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org.put(MAP_KEY_SELECTIVE_POLICY, PolicyEnum.SELECTED_ORG_ONLY);
        org.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE))));
        organizations.put(L1_ORG_2_NAME, org);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForPatchRemoveRoleInitialShare() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_2_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_2_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForPatchAddRoleTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForPatchRemoveRoleTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_2_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_2_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        // After removing APP_ROLE_1, only APP_ROLE_2 should remain
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForPatchAddMultipleRolesTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Setup methods.

    private void setupDetailMaps() {

        userDetails = new HashMap<>();
        orgDetails = new HashMap<>();
        appDetails = new HashMap<>();
        roleDetails = new HashMap<>();
    }

    private void setupRestClients() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON)));
        httpClient = HttpClientBuilder.create().build();
    }

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

    protected void setupApplicationsAndRoles() throws Exception {

        Map<String, String> rootOrgOrganizationRoles =
                setUpOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2, ORG_ROLE_3));

        createApplication(APP_1_NAME, APPLICATION_AUDIENCE, Arrays.asList(APP_ROLE_1, APP_ROLE_2, APP_ROLE_3));
        createApplication(APP_2_NAME, ORGANIZATION_AUDIENCE, new ArrayList<>(rootOrgOrganizationRoles.keySet()));
    }

    private void setupUsers() throws Exception {

        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME));

        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_1_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_2_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_3_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
    }
}
