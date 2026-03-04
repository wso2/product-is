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
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.API_VERSION;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APPLICATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_ROLE_3;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.AUTHORIZED_APIS_JSON;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_1_USER_1_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_1_USER_2_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_1_USER_3_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L3_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_COUNT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_IDS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_NAMES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_ROLE_ASSIGNMENT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORGANIZATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORG_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORG_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORG_ROLE_3;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAILS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_SHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_UNSHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_STATUS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_STATUS_VALUE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_1_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_2_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_3_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_DUPLICATED_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SELECTIVE_SHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GENERAL_SHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SELECTIVE_UNSHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.GENERAL_UNSHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARE_PATCH_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.USER_DOMAIN_PRIMARY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.USER_SHARING_API_BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_VALUE_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_AMPERSAND;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_LIMIT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_AFTER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_BEFORE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.QUERY_PARAM_FILTER;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_ORGANIZATIONS_SIZE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.PATCH_OP_ADD;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_ORG_DETAILS_KEY_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignment.ModeEnum;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserOrgShareConfig.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_ORGS_ONLY;

/**
 * Tests for failure cases of the User Sharing V2 REST APIs.
 */
public class UserSharingFailureTest extends UserSharingBaseTest {

    private static final String INVALID_ORG_1_NAME = "invalid-org-1-name";
    private static final String INVALID_ORG_1_ID = "invalid-org-1-id";

    private static final String INVALID_APP_1_NAME = "invalid-app-1";
    private static final String INVALID_APP_2_NAME = "invalid-app-2";

    private static final String INVALID_APP_ROLE_1 = "invalid-app-role-1";
    private static final String INVALID_APP_ROLE_2 = "invalid-app-role-2";
    private static final String INVALID_ORG_ROLE_1 = "invalid-org-role-1";
    private static final String INVALID_ORG_ROLE_2 = "invalid-org-role-2";

    private static final String INVALID_USER_1_ID = "invalid-user-id-1";
    private static final String INVALID_USER_2_ID = "invalid-user-id-2";

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

    // ========================================
    // Invalid Selective User Sharing Tests
    // ========================================

    @DataProvider(name = "selectiveUserSharingWithInvalidDetailsDataProvider")
    public Object[][] selectiveUserSharingWithInvalidDetailsDataProvider() {

        // Test case 1: User sharing with invalid roles.
        List<String> userIdsForTestCase1 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 =
                setOrganizationsForSelectiveUserSharingWithInvalidRoles();
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForSelectiveUserSharingWithInvalidRoles();

        // Test case 2: User sharing with invalid organizations.
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 =
                setOrganizationsForSelectiveUserSharingWithInvalidOrgs();
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForSelectiveUserSharingWithInvalidOrgs();

        // Test case 3: User sharing with invalid users.
        List<String> userIdsForTestCase3 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Map<String, Object>> organizationsForTestCase3 =
                setOrganizationsForSelectiveUserSharingWithInvalidUsers();
        Map<String, Object> expectedResultsForTestCase3 =
                setExpectedResultsForSelectiveUserSharingWithInvalidUsers();

        // Test case 4: User sharing with conflicting users.
        List<String> userIdsForTestCase4 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_DUPLICATED_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase4 =
                setOrganizationsForSelectiveUserSharingWithConflictingUsers();
        Map<String, Object> expectedResultsForTestCase4 =
                setExpectedResultsForSelectiveUserSharingWithConflictingUsers();

        // Test case 5: User sharing with non-immediate child organizations.
        List<String> userIdsForTestCase5 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase5 =
                setOrganizationsForSelectiveUserSharingWithNonImmediateChildren();
        Map<String, Object> expectedResultsForTestCase5 =
                setExpectedResultsForSelectiveUserSharingWithNonImmediateChildren();

        // Test case 6: User sharing with NONE role mode but providing roles (V2 specific).
        List<String> userIdsForTestCase6 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase6 =
                setOrganizationsForSelectiveUserSharingWithNoneModeMismatch();
        Map<String, Object> expectedResultsForTestCase6 =
                setExpectedResultsForSelectiveUserSharingWithNoneModeMismatch();

        return new Object[][]{
                {userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2},
                {userIdsForTestCase3, organizationsForTestCase3, expectedResultsForTestCase3},
                {userIdsForTestCase4, organizationsForTestCase4, expectedResultsForTestCase4},
                {userIdsForTestCase5, organizationsForTestCase5, expectedResultsForTestCase5},
                {userIdsForTestCase6, organizationsForTestCase6, expectedResultsForTestCase6}
        };
    }

    @Test(dataProvider = "selectiveUserSharingWithInvalidDetailsDataProvider")
    public void testSelectiveUserSharingWithInvalidDetails(List<String> userIds,
                                                           Map<String, Map<String, Object>> organizations,
                                                           Map<String, Object> expectedResults) throws Exception {

        UserShareSelectedRequestBody requestBody = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SELECTIVE_SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // ========================================
    // Invalid General User Sharing Tests
    // ========================================

    @DataProvider(name = "generalUserSharingWithInvalidDetailsDataProvider")
    public Object[][] generalUserSharingWithInvalidDetailsDataProvider() {

        // Test case 1: User sharing with invalid roles.
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 =
                setPolicyWithRoleAssignmentForGeneralUserSharingWithInvalidRoles();
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingWithInvalidRoles();

        // Test case 2: User sharing with invalid users.
        List<String> userIdsForTestCase2 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Object> policyWithRoleAssignmentForTestCase2 =
                setPolicyWithRoleAssignmentForGeneralUserSharingWithInvalidUsers();
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForGeneralUserSharingWithInvalidUsers();

        // Test case 3: User sharing with conflicting users.
        List<String> userIdsForTestCase3 = Collections.singletonList(
                getUserId(ROOT_ORG_USER_DUPLICATED_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase3 =
                setPolicyWithRoleAssignmentForGeneralUserSharingWithConflictingUsers();
        Map<String, Object> expectedResultsForTestCase3 =
                setExpectedResultsForGeneralUserSharingWithConflictingUsers();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRoleAssignmentForTestCase2, expectedResultsForTestCase2},
                {userIdsForTestCase3, policyWithRoleAssignmentForTestCase3, expectedResultsForTestCase3}
        };
    }

    @Test(dataProvider = "generalUserSharingWithInvalidDetailsDataProvider")
    public void testGeneralUserSharingWithInvalidDetails(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                                         Map<String, Object> expectedResults) throws Exception {

        UserShareAllRequestBody requestBody = new UserShareAllRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignment))
                .roleAssignment(getRoleAssignmentForGeneralUserSharing(policyWithRoleAssignment));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + GENERAL_SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // ========================================
    // Invalid General User Unsharing Tests
    // ========================================

    @DataProvider(name = "generalUserUnsharingWithInvalidDetailsDataProvider")
    public Object[][] generalUserUnsharingWithInvalidDetailsDataProvider() {

        List<String> sharingUserIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 =
                setPolicyWithRoleAssignmentForGeneralUserSharingValid1();
        Map<String, Object> expectedSharedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingValid1();
        List<String> userIdsForTestCase1 = Collections.singletonList(INVALID_USER_1_ID);
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingValid1();

        List<String> sharingUserIdsForTestCase2 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase2 =
                setPolicyWithRoleAssignmentForGeneralUserSharingValid2();
        Map<String, Object> expectedSharedResultsForTestCase2 =
                setExpectedResultsForGeneralUserSharingValid2();
        List<String> userIdsForTestCase2 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForGeneralUserSharingValid2();

        return new Object[][]{
                {sharingUserIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedSharedResultsForTestCase1,
                        userIdsForTestCase1, expectedResultsForTestCase1},
                {sharingUserIdsForTestCase2, policyWithRoleAssignmentForTestCase2, expectedSharedResultsForTestCase2,
                        userIdsForTestCase2, expectedResultsForTestCase2},
        };
    }

    @Test(dataProvider = "generalUserUnsharingWithInvalidDetailsDataProvider")
    public void testGeneralUserUnsharingWithInvalidDetails(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                                           Map<String, Object> expectedSharedResults, List<String> removingUserIds,
                                                           Map<String, Object> expectedResults) throws Exception {

        // First share valid users.
        testGeneralUserSharingWithValidDetails(userIds, policyWithRoleAssignment, expectedSharedResults);

        // Then try to unshare with invalid users.
        UserUnshareAllRequestBody requestBody = new UserUnshareAllRequestBody()
                .userCriteria(getUserCriteria(removingUserIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + GENERAL_UNSHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // Helper method for valid general sharing used by unsharing tests.
    private void testGeneralUserSharingWithValidDetails(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                                        Map<String, Object> expectedResults) throws Exception {

        UserShareAllRequestBody requestBody = new UserShareAllRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignment))
                .roleAssignment(getRoleAssignmentForGeneralUserSharing(policyWithRoleAssignment));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + GENERAL_SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // ========================================
    // Invalid Selective User Unsharing Tests
    // ========================================

    @DataProvider(name = "selectiveUserUnsharingWithInvalidDetailsDataProvider")
    public Object[][] selectiveUserUnsharingWithInvalidDetailsDataProvider() {

        // ALL EXISTING with invalid users and orgs
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 =
                setPolicyWithRoleAssignmentForGeneralUserSharingValid1();
        Map<String, Object> expectedSharedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingValid1();
        List<String> removingUserIdsForTestCase1 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        INVALID_USER_1_ID, INVALID_USER_2_ID);
        List<String> removingOrgIdsForTestCase1 = Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME));
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForSelectiveUnsharingWithInvalidDetails1();

        // IMMEDIATE EXISTING AND FUTURE with invalid org
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase2 =
                setPolicyWithRoleAssignmentForGeneralUserSharingValid2();
        Map<String, Object> expectedSharedResultsForTestCase2 =
                setExpectedResultsForGeneralUserSharingValid2();
        List<String> removingUserIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), INVALID_USER_1_ID);
        List<String> removingOrgIdsForTestCase2 = Arrays.asList(getOrgId(L1_ORG_1_NAME), INVALID_ORG_1_ID);
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForSelectiveUnsharingWithInvalidDetails2();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedSharedResultsForTestCase1,
                        removingUserIdsForTestCase1, removingOrgIdsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRoleAssignmentForTestCase2, expectedSharedResultsForTestCase2,
                        removingUserIdsForTestCase2, removingOrgIdsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingWithInvalidDetailsDataProvider")
    public void testSelectiveUserUnsharingWithInvalidDetails(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                                              Map<String, Object> expectedSharedResults, List<String> removingUserIds,
                                                              List<String> removingOrgIds, Map<String, Object> expectedResults)
            throws Exception {

        testGeneralUserSharingWithValidDetails(userIds, policyWithRoleAssignment, expectedSharedResults);

        UserUnshareSelectedRequestBody requestBody = new UserUnshareSelectedRequestBody()
                .userCriteria(getUserCriteria(removingUserIds))
                .orgIds(removingOrgIds);

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SELECTIVE_UNSHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // ========================================
    // Invalid PATCH User Sharing Tests (V2 specific)
    // ========================================

    @DataProvider(name = "patchUserSharingWithInvalidDetailsDataProvider")
    public Object[][] patchUserSharingWithInvalidDetailsDataProvider() {

        // Test case 1: PATCH with invalid user ID
        List<String> sharingUserIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 =
                setOrganizationsForSelectiveUserSharingValid1();
        Map<String, Object> expectedSharedResultsForTestCase1 =
                setExpectedResultsForSelectiveUserSharingValid1();
        String invalidUserIdForTestCase1 = INVALID_USER_1_ID;
        String orgIdForTestCase1 = null; // Will be set at runtime
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForSelectiveUserSharingValid1();

        // Test case 2: PATCH with invalid org ID
        List<String> sharingUserIdsForTestCase2 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 =
                setOrganizationsForSelectiveUserSharingValid2();
        Map<String, Object> expectedSharedResultsForTestCase2 =
                setExpectedResultsForSelectiveUserSharingValid2();
        String userIdForTestCase2 = null; // Will be set at runtime
        String invalidOrgIdForTestCase2 = INVALID_ORG_1_ID;
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForSelectiveUserSharingValid2();

        return new Object[][]{
                {sharingUserIdsForTestCase1, organizationsForTestCase1, expectedSharedResultsForTestCase1,
                        invalidUserIdForTestCase1, L1_ORG_1_NAME, expectedResultsForTestCase1, true},
                {sharingUserIdsForTestCase2, organizationsForTestCase2, expectedSharedResultsForTestCase2,
                        ROOT_ORG_USER_2_USERNAME, invalidOrgIdForTestCase2, expectedResultsForTestCase2, false}
        };
    }

    @Test(dataProvider = "patchUserSharingWithInvalidDetailsDataProvider")
    public void testPatchUserSharingWithInvalidDetails(List<String> userIds,
                                                        Map<String, Map<String, Object>> organizations,
                                                        Map<String, Object> expectedSharedResults,
                                                        String userIdOrUsername, String orgIdOrName,
                                                        Map<String, Object> expectedResults,
                                                        boolean isInvalidUser) throws Exception {

        // First share users with selective sharing.
        UserShareSelectedRequestBody shareRequestBody = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response shareResponse = getResponseOfPost(USER_SHARING_API_BASE_PATH + SELECTIVE_SHARE_PATH,
                toJSONString(shareRequestBody));

        shareResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        validateUserSharingResults(userIds, expectedSharedResults);

        // Now try PATCH with invalid details.
        String userId = isInvalidUser ? userIdOrUsername :
                getUserId(userIdOrUsername, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        String orgId = isInvalidUser ? getOrgId(orgIdOrName) : orgIdOrName;

        List<RoleShareConfig> newRoles = Arrays.asList(
                createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)
        );

        // Create PATCH operation using the correct V2 API pattern
        UserSharingPatchOperation patchOp = createPatchOperation(PATCH_OP_ADD, orgId, newRoles);

        UserSharingPatchRequest patchRequest = new UserSharingPatchRequest()
                .operations(Collections.singletonList(patchOp));

        // The PATCH request should fail or be ignored for invalid user/org.
        String patchEndpoint = USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATCH_PATH;
        Response patchResponse = getResponseOfPatch(patchEndpoint, toJSONString(patchRequest));

        // The API accepts the request but skips invalid operations.
        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED);

        // Verify original sharing results are unchanged.
        validateUserSharingResults(userIds, expectedResults);
    }

    // ========================================
    // Re-sharing Test (Invalid Scenario)
    // ========================================

    @DataProvider(name = "selectiveUserSharingWithReSharingDataProvider")
    public Object[][] selectiveUserSharingWithReSharingDataProvider() {

        // Test case 1: User re-sharing from sub-org (should be rejected).
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 =
                setOrganizationsForSelectiveUserSharingValid1();
        Map<String, Object> expectedSharedResultsForTestCase1 =
                setExpectedResultsForSelectiveUserSharingValid1();
        Map<String, Map<String, Object>> organizationsForReSharingTestCase1 =
                setOrganizationsForReSharingFromSubOrg();
        Map<String, Object> reSharingSubOrgDetailsForTestCase1 = orgDetails.get(L1_ORG_1_NAME);
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForEmptySharedResult();

        return new Object[][]{
                {userIdsForTestCase1, organizationsForTestCase1, expectedSharedResultsForTestCase1,
                        organizationsForReSharingTestCase1, reSharingSubOrgDetailsForTestCase1,
                        expectedResultsForTestCase1}
        };
    }

    @Test(dataProvider = "selectiveUserSharingWithReSharingDataProvider")
    public void testSelectiveUserSharingWithReSharing(List<String> userIds,
                                                       Map<String, Map<String, Object>> organizations,
                                                       Map<String, Object> expectedSharedResults,
                                                       Map<String, Map<String, Object>> organizationsForReSharing,
                                                       Map<String, Object> reSharingSubOrgDetails,
                                                       Map<String, Object> expectedResults) throws Exception {

        UserShareSelectedRequestBody requestBody = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SELECTIVE_SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        // Validate and get shared user IDs
        validateUserSharingResults(userIds, expectedSharedResults);
        List<String> sharedUserIds = getSharedUserIds(userIds, reSharingSubOrgDetails);

        UserShareSelectedRequestBody requestBodyForReSharing = new UserShareSelectedRequestBody()
                .userCriteria(getUserCriteria(sharedUserIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizationsForReSharing));

        HttpResponse responseOfReSharing = getResponseOfPostToSubOrg(USER_SHARING_API_BASE_PATH + SELECTIVE_SHARE_PATH,
                toJSONString(requestBodyForReSharing),
                reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN).toString());

        Assert.assertEquals(responseOfReSharing.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);

        validateUserSharingResults(sharedUserIds, expectedResults);
    }

    @DataProvider(name = "generalUserSharingWithReSharingDataProvider")
    public Object[][] generalUserSharingWithReSharingDataProvider() {

        // Test case 1: User re-sharing from sub-org (should be rejected).
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentForTestCase1 =
                setPolicyWithRoleAssignmentForGeneralUserSharingValid1();
        Map<String, Object> expectedSharedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingValid1();
        Map<String, Object> reSharingSubOrgDetailsForTestCase1 = orgDetails.get(L1_ORG_1_NAME);
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForEmptySharedResult();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentForTestCase1, expectedSharedResultsForTestCase1,
                        reSharingSubOrgDetailsForTestCase1, expectedResultsForTestCase1}
        };
    }

    @Test(dataProvider = "generalUserSharingWithReSharingDataProvider")
    public void testGeneralUserSharingWithReSharing(List<String> userIds, Map<String, Object> policyWithRoleAssignment,
                                                     Map<String, Object> expectedSharedResults,
                                                     Map<String, Object> reSharingSubOrgDetails,
                                                     Map<String, Object> expectedResults) throws Exception {

        UserShareAllRequestBody requestBody = new UserShareAllRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignment))
                .roleAssignment(getRoleAssignmentForGeneralUserSharing(policyWithRoleAssignment));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + GENERAL_SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        // Validate and get shared user IDs
        validateUserSharingResults(userIds, expectedSharedResults);
        List<String> sharedUserIds = getSharedUserIds(userIds, reSharingSubOrgDetails);

        UserShareAllRequestBody requestBodyForReSharing = new UserShareAllRequestBody()
                .userCriteria(getUserCriteria(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignment))
                .roleAssignment(getRoleAssignmentForGeneralUserSharing(policyWithRoleAssignment));

        HttpResponse responseOfReSharing = getResponseOfPostToSubOrg(USER_SHARING_API_BASE_PATH + GENERAL_SHARE_PATH,
                toJSONString(requestBodyForReSharing),
                reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN).toString());

        Assert.assertEquals(responseOfReSharing.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);

        validateUserSharingResults(sharedUserIds, expectedResults);
    }

    // ========================================
    // Invalid GET Shared Organizations Tests
    // ========================================

    @DataProvider(name = "getSharedOrganizationsWithInvalidDetailsDataProvider")
    public Object[][] getSharedOrganizationsWithInvalidDetailsDataProvider() {

        // Test case 1: GET with invalid UUID format for user ID (returns BAD_REQUEST)
        String invalidUserId = INVALID_USER_1_ID;
        Map<String, String> queryParamsForTestCase1 = Collections.emptyMap();
        int expectedStatusCodeForTestCase1 = HttpStatus.SC_BAD_REQUEST;

        // Test case 2: GET with invalid limit (negative value)
        String validUserId = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        Map<String, String> queryParamsForTestCase2 = new HashMap<>();
        queryParamsForTestCase2.put(QUERY_PARAM_LIMIT, "-1");
        int expectedStatusCodeForTestCase2 = HttpStatus.SC_BAD_REQUEST;

        // Test case 3: GET with invalid after cursor
        Map<String, String> queryParamsForTestCase3 = new HashMap<>();
        queryParamsForTestCase3.put(QUERY_PARAM_AFTER, "invalid-cursor-value");
        int expectedStatusCodeForTestCase3 = HttpStatus.SC_BAD_REQUEST;

        // Test case 4: GET with invalid before cursor
        Map<String, String> queryParamsForTestCase4 = new HashMap<>();
        queryParamsForTestCase4.put(QUERY_PARAM_BEFORE, "invalid-cursor-value");
        int expectedStatusCodeForTestCase4 = HttpStatus.SC_BAD_REQUEST;

        return new Object[][]{
                {invalidUserId, queryParamsForTestCase1, expectedStatusCodeForTestCase1},
                {validUserId, queryParamsForTestCase2, expectedStatusCodeForTestCase2},
                {validUserId, queryParamsForTestCase3, expectedStatusCodeForTestCase3},
                {validUserId, queryParamsForTestCase4, expectedStatusCodeForTestCase4}
        };
    }

    @Test(dataProvider = "getSharedOrganizationsWithInvalidDetailsDataProvider")
    public void testGetSharedOrganizationsWithInvalidDetails(String userId, Map<String, String> queryParams,
                                                              int expectedStatusCode) throws Exception {

        // Build the GET endpoint with query parameters
        StringBuilder getEndpoint = new StringBuilder(
                USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATCH_PATH);
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
                .statusCode(expectedStatusCode);
    }

    @Test
    public void testGetSharedOrganizationsForUnsharedUser() throws Exception {

        // Use a user that hasn't been shared with any organization
        String userId = getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);

        String getEndpoint = USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATCH_PATH;

        Response response = getResponseOfGet(getEndpoint);

        // Should return OK with empty organizations array
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(RESPONSE_ORGANIZATIONS_SIZE, equalTo(0));
    }

    @Test
    public void testGetSharedOrganizationsWithInvalidFilterSyntax() throws Exception {

        String userId = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);

        // Invalid filter syntax
        String getEndpoint = USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATCH_PATH +
                QUERY_PARAM_SEPARATOR + QUERY_PARAM_FILTER + QUERY_PARAM_VALUE_SEPARATOR + "invalid filter syntax";

        Response response = getResponseOfGet(getEndpoint);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    // Helper method to get shared user IDs after sharing.
    private List<String> getSharedUserIds(List<String> userIds, Map<String, Object> reSharingSubOrgDetails) 
            throws Exception {

        List<String> sharedUserIds = new ArrayList<>();
        for (String userId : userIds) {
            Response sharedOrgsResponse =
                    getResponseOfGet(USER_SHARING_API_BASE_PATH + PATH_SEPARATOR + userId + SHARE_PATH);
            String sharedUserId = extractSharedUserId(sharedOrgsResponse,
                    reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_NAME).toString());
            sharedUserIds.add(sharedUserId);
        }
        return sharedUserIds;
    }

    // ========================================
    // Test Case Data Builders
    // ========================================

    // Invalid Selective Sharing - Invalid Roles
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidRoles() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1 with invalid app role
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2 with invalid app name
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, INVALID_APP_2_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3 with all invalid roles
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(INVALID_APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(INVALID_ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                createRoleShareConfig(INVALID_ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidRoles() {

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

    // Invalid Selective Sharing - Invalid Organizations
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidOrgs() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Invalid Organization
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, INVALID_ORG_1_ID);
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, INVALID_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Collections.singletonList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(INVALID_ORG_1_NAME, org1);

        // Valid Organization
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidOrgs() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Invalid Selective Sharing - Invalid Users
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidUsers() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Collections.singletonList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.NONE, Collections.emptyList()));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidUsers() {

        return setExpectedResultsForEmptySharedResult();
    }

    // Invalid Selective Sharing - Conflicting Users
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithConflictingUsers() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Collections.singletonList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithConflictingUsers() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Invalid Selective Sharing - Non-Immediate Children
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithNonImmediateChildren() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Trying to share to non-immediate child (L3_ORG_1 is not immediate child of root)
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L3_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L3_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Collections.singletonList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(INVALID_ORG_1_NAME, org1);

        // Valid org
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithNonImmediateChildren() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // V2 Specific - NONE mode with roles (mismatch scenario)
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithNoneModeMismatch() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Using NONE mode but providing roles (should be ignored)
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        // NONE mode means no roles should be assigned, but we're providing roles anyway
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.NONE, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithNoneModeMismatch() {

        Map<String, Object> expectedResults = new HashMap<>();

        // User should be shared but without any roles (NONE mode)
        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Invalid General Sharing - Invalid Roles
    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingWithInvalidRoles() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(
                        createRoleShareConfig(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(APP_ROLE_1, INVALID_APP_2_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(INVALID_APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(INVALID_ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                        createRoleShareConfig(INVALID_ORG_ROLE_2, INVALID_ORG_1_NAME, ORGANIZATION_AUDIENCE),
                        createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithInvalidRoles() {

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
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Invalid General Sharing - Invalid Users
    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingWithInvalidUsers() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(
                        createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithInvalidUsers() {

        return setExpectedResultsForEmptySharedResult();
    }

    // Invalid General Sharing - Conflicting Users
    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingWithConflictingUsers() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithConflictingUsers() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // Valid Sharing configurations (used by unsharing tests)
    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingValid1() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingValid1() {

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
        for (String orgId : Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME))) {
            expectedRolesPerExpectedOrg.put(orgId,
                    Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        }

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRoleAssignmentForGeneralUserSharingValid2() {

        Map<String, Object> policyWithRoleAssignment = new HashMap<>();

        policyWithRoleAssignment.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoleAssignment.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Arrays.asList(
                        createRoleShareConfig(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleShareConfig(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return policyWithRoleAssignment;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingValid2() {

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

    // Selective Unsharing expected results
    private Map<String, Object> setExpectedResultsForSelectiveUnsharingWithInvalidDetails1() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUnsharingWithInvalidDetails2() {

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

    // Valid Selective Sharing configurations (used by PATCH tests)
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingValid1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);

        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingValid1() {

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
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleShareConfig(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingValid2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_1_NAME, org1);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingValid2() {

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

    // Re-sharing from sub-org configurations
    private Map<String, Map<String, Object>> setOrganizationsForReSharingFromSubOrg() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L2_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L2_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED,
                Collections.singletonList(createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L2_ORG_1_NAME, org1);

        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L2_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L2_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT, createRoleAssignment(ModeEnum.SELECTED, Arrays.asList(
                createRoleShareConfig(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleShareConfig(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        organizations.put(L2_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForEmptySharedResult() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleShareConfig>> expectedRolesPerExpectedOrg = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    // ========================================
    // Setup Methods
    // ========================================

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
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_DUPLICATED_USERNAME, ROOT_ORG_NAME));

        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_1_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_2_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, L1_ORG_1_USER_3_USERNAME, L1_ORG_1_NAME), L1_ORG_1_NAME);
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_DUPLICATED_USERNAME, ROOT_ORG_NAME),
                L1_ORG_1_NAME);
    }
}
