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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.ExpectedSharingMode;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserSharePatchRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBody;
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
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.*;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation.OpEnum.ADD;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation.OpEnum.REMOVE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments.ModeEnum.NONE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments.ModeEnum.SELECTED;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS;

/**
 * Integration tests for the V2 User Sharing REST API — failure/partial-success cases.
 *
 * <p>V2 handles invalid entities (roles, orgs) idempotently: valid parts of the request
 * are processed and invalid parts are silently ignored, returning 202 Accepted.
 */
public class UserSharingFailureTest extends UserSharingBaseTest {

    // -------------------------------------------------------------------------
    // Invalid Data Constants
    // -------------------------------------------------------------------------
    private static final String INVALID_ORG_1_NAME = "invalid-org-1-name";
    private static final String INVALID_ORG_1_ID = "invalid-org-1-id";
    private static final String INVALID_APP_1_NAME = "invalid-app-1";
    private static final String INVALID_APP_ROLE_1 = "invalid-app-role-1";
    private static final String INVALID_ORG_ROLE_1 = "invalid-org-role-1";
    private static final String INVALID_USER_1_ID = "invalid-user-id-1";

    // SharingMode Policy Strings for Assertions
    private static final String POLICY_ALL_EXISTING_AND_FUTURE_ORGS = "ALL_EXISTING_AND_FUTURE_ORGS";
    private static final String POLICY_WITH_CHILDREN = "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN";

    private static final String MODE_SELECTED = "SELECTED";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserSharingFailureTest(TestUserMode userMode) throws Exception {

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

    // =========================================================================
    // Phase 1 — Selective User Sharing with Invalid Details
    // =========================================================================

    @DataProvider(name = "selectiveUserSharingInvalidDataProvider")
    public Object[][] selectiveUserSharingInvalidDataProvider() {

        // TC1: Invalid Organizations — valid org (L1Org1) is processed, invalid org is ignored.
        List<String> userIdsForTC1 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> orgsForTC1 = buildOrgsForInvalidSelectiveShareTC1();
        Map<String, Object> expectedForTC1 = buildExpectedResultsForInvalidSelectiveShareTC1();

        // TC2: Invalid Roles — Valid role (APP_ROLE_1) is added, invalid role is skipped.
        List<String> userIdsForTC2 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> orgsForTC2 = buildOrgsForInvalidSelectiveShareTC2();
        Map<String, Object> expectedForTC2 = buildExpectedResultsForInvalidSelectiveShareTC2();

        // TC3: Invalid Users — System processes request idempotently, GET returns no shares.
        List<String> userIdsForTC3 = Collections.singletonList(INVALID_USER_1_ID);
        Map<String, Map<String, Object>> orgsForTC3 = buildOrgsForInvalidSelectiveShareTC1(); // Reuse payload
        Map<String, Object> expectedForTC3 = setExpectedResultsForEmptyShare();

        return new Object[][]{
                {userIdsForTC1, orgsForTC1, expectedForTC1},
                {userIdsForTC2, orgsForTC2, expectedForTC2},
                {userIdsForTC3, orgsForTC3, expectedForTC3}
        };
    }

    @Test(dataProvider = "selectiveUserSharingInvalidDataProvider")
    public void testSelectiveUserSharingWithInvalidDetails(List<String> userIds,
                                                           Map<String, Map<String, Object>> organizations,
                                                           Map<String, Object> expectedResults) throws Exception {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBody))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        // Note: We skip assertion for the invalid user ID because the GET call expects a valid SCIM user
        if (!userIds.contains(INVALID_USER_1_ID)) {
            validateUserSharingResults(userIds, expectedResults);
        }
    }

    // =========================================================================
    // Phase 2 — General User Sharing with Invalid Details
    // =========================================================================

    @DataProvider(name = "generalUserSharingInvalidDataProvider")
    public Object[][] generalUserSharingInvalidDataProvider() {

        // TC1: Invalid Roles — Valid ORG_ROLE_1 is processed, INVALID_ORG_ROLE_1 is ignored.
        List<String> userIdsForTC1 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTC1 = buildPolicyForInvalidGeneralShareTC1();
        Map<String, Object> expectedForTC1 = buildExpectedResultsForInvalidGeneralShareTC1();

        // TC2: Conflicting Users (Re-sharing rootUser2 generally replaces selective share)
        List<String> userIdsForTC2 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTC2 = buildPolicyForInvalidGeneralShareTC2();
        Map<String, Object> expectedForTC2 = buildExpectedResultsForInvalidGeneralShareTC2();

        return new Object[][]{
                {userIdsForTC1, policyWithRolesForTC1, expectedForTC1},
                {userIdsForTC2, policyWithRolesForTC2, expectedForTC2}
        };
    }

    @Test(dataProvider = "generalUserSharingInvalidDataProvider",
            dependsOnMethods = "testSelectiveUserSharingWithInvalidDetails")
    public void testGeneralUserSharingWithInvalidDetails(List<String> userIds,
                                                         Map<String, Object> policyWithRoleAssignments,
                                                         Map<String, Object> expectedResults) throws Exception {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignments))
                .roleAssignment(getRoleAssignmentsForGeneralUserSharing(policyWithRoleAssignments));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, toJSONString(requestBody))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // =========================================================================
    // Phase 3 — PATCH Failures (New in V2)
    // =========================================================================

    @Test(dependsOnMethods = "testGeneralUserSharingWithInvalidDetails")
    public void testPatchUserSharingWithInvalidDetails() throws Exception {

        // TC1: Patching a User in an Unshared Organization
        // rootUser1 is only in L1Org1. Trying to PATCH L2Org1.
        String rootUser1Id = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        UserSharePatchRequestBody patchTC1 = new UserSharePatchRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(Collections.singletonList(rootUser1Id)))
                .operations(Collections.singletonList(
                        buildPatchOperation(ADD, getOrgId(L2_ORG_1_NAME),
                                Collections.singletonList(
                                        createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)))));

        getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH, toJSONString(patchTC1))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        // Assert state remains exactly as it was after Phase 1 TC1
        validateUserSharingResults(Collections.singletonList(rootUser1Id), buildExpectedResultsForInvalidSelectiveShareTC1());

        // TC2: Patching with Invalid Roles
        // rootUser2 is generally shared (Phase 2 TC2). Attempting to add an invalid role.
        String rootUser2Id = getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        UserSharePatchRequestBody patchTC2 = new UserSharePatchRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(Collections.singletonList(rootUser2Id)))
                .operations(Collections.singletonList(
                        buildPatchOperation(ADD, getOrgId(L1_ORG_1_NAME),
                                Collections.singletonList(
                                        createRoleWithAudience(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE)))));

        getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH, toJSONString(patchTC2))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        // Assert state remains exactly as it was after Phase 2 TC2
        validateUserSharingResults(Collections.singletonList(rootUser2Id), buildExpectedResultsForInvalidGeneralShareTC2());

        // TC3: Removing a non-assigned Role (Idempotent)
        // rootUser3 is generally shared with ORG_ROLE_1. Attempt to remove APP_ROLE_2.
        String rootUser3Id = getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        UserSharePatchRequestBody patchTC3 = new UserSharePatchRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(Collections.singletonList(rootUser3Id)))
                .operations(Collections.singletonList(
                        buildPatchOperation(REMOVE, getOrgId(L1_ORG_1_NAME),
                                Collections.singletonList(
                                        createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)))));

        getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH, toJSONString(patchTC3))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        // Assert state remains exactly as it was after Phase 2 TC1
        validateUserSharingResults(Collections.singletonList(rootUser3Id), buildExpectedResultsForInvalidGeneralShareTC1());
    }

    // =========================================================================
    // Phase 4 — Selective Unsharing with Invalid Details
    // =========================================================================

    @Test(dependsOnMethods = "testPatchUserSharingWithInvalidDetails")
    public void testSelectiveUnsharingWithInvalidDetails() throws Exception {

        String rootUser1Id = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);

        // TC1: Unshare from an invalid organization
        UserUnshareRequestBody requestBody = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(Collections.singletonList(rootUser1Id)))
                .orgIds(getOrganizationsForSelectiveUserUnsharing(Collections.singletonList(INVALID_ORG_1_ID)));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH, toJSONString(requestBody))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        // rootUser1 should still have their L1Org1 share active.
        validateUserSharingResults(Collections.singletonList(rootUser1Id), buildExpectedResultsForInvalidSelectiveShareTC1());

        // TC2: Unshare Invalid User
        UserUnshareRequestBody requestBody2 = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(Collections.singletonList(INVALID_USER_1_ID)))
                .orgIds(getOrganizationsForSelectiveUserUnsharing(Collections.singletonList(getOrgId(L1_ORG_1_NAME))));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH, toJSONString(requestBody2))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);
    }

    // =========================================================================
    // Phase 5 — Re-Sharing via Sub-Org Context
    // =========================================================================

    @Test(dependsOnMethods = "testSelectiveUnsharingWithInvalidDetails")
    public void testUserSharingWithReSharingContext() throws Exception {

        String rootUser1Id = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        String subOrgToken = orgDetails.get(L1_ORG_1_NAME).get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN).toString();

        // Attempting to invoke the share endpoint using the sub-org's switch token instead of root.
        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(Collections.singletonList(rootUser1Id)))
                .organizations(getOrganizationsForSelectiveUserSharing(buildOrgsForInvalidSelectiveShareTC2()));

        HttpResponse response = getResponseOfPostToSubOrg(USER_SHARING_API_BASE_PATH + SHARE_PATH,
                toJSONString(requestBody), subOrgToken);

        // Endpoint accepts the request idempotently for both user modes — always returns 202.
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);

        // State remains unchanged for rootUser1 (Only shared to L1Org1 with no roles)
        validateUserSharingResults(Collections.singletonList(rootUser1Id), buildExpectedResultsForInvalidSelectiveShareTC1());
    }

    // =========================================================================
    // Builders for Map / Data Setup
    // =========================================================================

    private Map<String, Map<String, Object>> buildOrgsForInvalidSelectiveShareTC1() {
        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> validOrg = new HashMap<>();
        validOrg.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        validOrg.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        validOrg.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        validOrg.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS, createRoleAssignments(NONE, Collections.emptyList()));
        organizations.put(L1_ORG_1_NAME, validOrg);

        Map<String, Object> invalidOrg = new HashMap<>();
        invalidOrg.put(MAP_KEY_SELECTIVE_ORG_ID, INVALID_ORG_1_ID);
        invalidOrg.put(MAP_KEY_SELECTIVE_ORG_NAME, INVALID_ORG_1_NAME);
        invalidOrg.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        invalidOrg.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS, createRoleAssignments(NONE, Collections.emptyList()));
        organizations.put(INVALID_ORG_1_NAME, invalidOrg);

        return organizations;
    }

    private Map<String, Object> buildExpectedResultsForInvalidSelectiveShareTC1() {
        Map<String, Object> expectedResults = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleWithAudience>> expectedRoles = new HashMap<>();
        expectedRoles.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRoles);

        Map<String, ExpectedSharingMode> expectedPerOrgMode = new HashMap<>();
        expectedPerOrgMode.put(getOrgId(L1_ORG_1_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgMode);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);
        return expectedResults;
    }

    private Map<String, Map<String, Object>> buildOrgsForInvalidSelectiveShareTC2() {
        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> validOrgWithInvalidRole = new HashMap<>();
        validOrgWithInvalidRole.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        validOrgWithInvalidRole.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        validOrgWithInvalidRole.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);

        List<RoleWithAudience> mixedRoles = Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE)
        );
        validOrgWithInvalidRole.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS, createRoleAssignments(SELECTED, mixedRoles));

        organizations.put(L1_ORG_2_NAME, validOrgWithInvalidRole);
        return organizations;
    }

    private Map<String, Object> buildExpectedResultsForInvalidSelectiveShareTC2() {
        Map<String, Object> expectedResults = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME));

        RoleWithAudience validRole = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRoles = new HashMap<>();
        expectedRoles.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(validRole));
        expectedRoles.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(validRole));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRoles);

        Map<String, ExpectedSharingMode> expectedPerOrgMode = new HashMap<>();
        expectedPerOrgMode.put(getOrgId(L1_ORG_2_NAME), new ExpectedSharingMode(POLICY_WITH_CHILDREN, MODE_SELECTED, Collections.singletonList(validRole)));
        expectedPerOrgMode.put(getOrgId(L2_ORG_3_NAME), null); // child org doesn't hold policy

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgMode);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);
        return expectedResults;
    }

    private Map<String, Object> buildPolicyForInvalidGeneralShareTC1() {
        Map<String, Object> policy = new HashMap<>();
        policy.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);

        List<RoleWithAudience> mixedRoles = Arrays.asList(
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                createRoleWithAudience(INVALID_ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)
        );
        policy.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENTS, createRoleAssignments(SELECTED, mixedRoles));
        return policy;
    }

    private Map<String, Object> buildExpectedResultsForInvalidGeneralShareTC1() {
        Map<String, Object> expectedResults = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRoles = new HashMap<>();
        expectedRoles.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L3_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRoles);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED, Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        return expectedResults;
    }

    private Map<String, Object> buildPolicyForInvalidGeneralShareTC2() {
        Map<String, Object> policy = new HashMap<>();
        policy.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);
        policy.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENTS, createRoleAssignments(SELECTED, Collections.singletonList(
                createRoleWithAudience(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        return policy;
    }

    private Map<String, Object> buildExpectedResultsForInvalidGeneralShareTC2() {
        Map<String, Object> expectedResults = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRoles = new HashMap<>();
        expectedRoles.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L2_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L2_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L3_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRoles.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRoles);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED, Collections.singletonList(createRoleWithAudience(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));
        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForEmptyShare() {
        Map<String, Object> expectedResults = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, new HashMap<>());
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);
        return expectedResults;
    }

    // =========================================================================
    // Setup Methods
    // =========================================================================

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
        addOrganization(L1_ORG_1_NAME);
        addOrganization(L1_ORG_2_NAME);
        addOrganization(L1_ORG_3_NAME);
        addSubOrganization(L2_ORG_1_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_2_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_3_NAME, getOrgId(L1_ORG_2_NAME), 2);
        addSubOrganization(L3_ORG_1_NAME, getOrgId(L2_ORG_1_NAME), 3);
    }

    protected void setupApplicationsAndRoles() throws Exception {
        Map<String, String> rootOrgOrganizationRoles = setUpOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2));
        createApplication(APP_1_NAME, APPLICATION_AUDIENCE, Arrays.asList(APP_ROLE_1, APP_ROLE_2));
        createApplication(APP_2_NAME, ORGANIZATION_AUDIENCE, new ArrayList<>(rootOrgOrganizationRoles.keySet()));
    }

    private void setupUsers() throws Exception {
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME));
    }
}