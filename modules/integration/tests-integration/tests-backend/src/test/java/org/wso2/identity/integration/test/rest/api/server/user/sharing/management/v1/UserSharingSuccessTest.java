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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserUnshareWithAllRequestBody;
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
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.API_VERSION;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APPLICATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APP_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APP_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APP_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APP_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.APP_ROLE_3;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.AUTHORIZED_APIS_JSON;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_1_USER_1_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_1_USER_2_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_1_USER_3_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L1_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L2_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L2_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L2_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.L3_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_COUNT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_IDS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_NAMES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_GENERAL_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_GENERAL_ROLES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_SELECTIVE_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ROLES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ORGANIZATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ORG_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ORG_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ORG_ROLE_3;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.RESPONSE_DETAILS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_SHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_UNSHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.RESPONSE_STATUS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.RESPONSE_STATUS_VALUE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ROOT_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ROOT_ORG_USER_1_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ROOT_ORG_USER_2_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.ROOT_ORG_USER_3_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.SHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.SHARE_WITH_ALL_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.UNSHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.UNSHARE_WITH_ALL_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.USER_DOMAIN_PRIMARY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.USER_SHARING_API_BASE_PATH;
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
        cleanUpSSOIdP();
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

    // Selective User Sharing.

    @DataProvider(name = "selectiveUserSharingDataProvider")
    public Object[][] selectiveUserSharingDataProvider() {

        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 = setOrganizationsForSelectiveUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserSharingTestCase2();

        return new Object[][]{
                {userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserSharingDataProvider")
    public void testSelectiveUserSharing(List<String> userIds, Map<String, Map<String, Object>> organizations,
                                         Map<String, Object> expectedResults) throws Exception {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
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

    // General User Sharing.

    @DataProvider(name = "generalUserSharingDataProvider")
    public Object[][] generalUserSharingDataProvider() {

        // ALL EXISTING
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();

        // IMMEDIATE EXISTING
        List<String> userIdsForTestCase3 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase3 = setPolicyWithRolesForGeneralUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForGeneralUserSharingTestCase3();

        // ALL EXISTING AND FUTURE
        List<String> userIdsForTestCase4 =
                Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase4 = setPolicyWithRolesForGeneralUserSharingTestCase4();
        Map<String, Object> expectedResultsForTestCase4 = setExpectedResultsForGeneralUserSharingTestCase4();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRolesForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRolesForTestCase2, expectedResultsForTestCase2},
                {userIdsForTestCase3, policyWithRolesForTestCase3, expectedResultsForTestCase3},
                {userIdsForTestCase4, policyWithRolesForTestCase4, expectedResultsForTestCase4}
        };
    }

    @Test(dataProvider = "generalUserSharingDataProvider")
    public void testGeneralUserSharing(List<String> userIds, Map<String, Object> policyWithRoles,
                                       Map<String, Object> expectedResults) throws Exception {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoles))
                .roles(getRolesForGeneralUserSharing(policyWithRoles));

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

        UserUnshareWithAllRequestBody requestBody = new UserUnshareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds));

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

        // ALL EXISTING
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingTestCase1();
        List<String> removingOrgIdsForTestCase1 = Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME));
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserUnsharingTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingTestCase2();
        List<String> removingOrgIdsForTestCase2 = Collections.singletonList(getOrgId(L1_ORG_1_NAME));
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserUnsharingTestCase2();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRolesForTestCase1, expectedSharedResultsForTestCase1,
                        removingOrgIdsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRolesForTestCase2, expectedSharedResultsForTestCase2,
                        removingOrgIdsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingDataProvider")
    public void testSelectiveUserUnsharing(List<String> userIds, Map<String, Object> policyWithRoles,
                                           Map<String, Object> expectedSharedResults, List<String> removingOrgIds,
                                           Map<String, Object> expectedResults) throws Exception {

        testGeneralUserSharing(userIds, policyWithRoles, expectedSharedResults);

        UserUnshareRequestBody requestBody = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds))
                .organizations(getOrganizationsForSelectiveUserUnsharing(removingOrgIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // Test cases builders.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES,
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES,
                Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME,
                        L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
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
        org2.put(MAP_KEY_SELECTIVE_ROLES,
                Arrays.asList(createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org3.put(MAP_KEY_SELECTIVE_ROLES,
                Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_3_NAME, org3);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_2, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Arrays.asList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_2, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase1() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES,
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        return policyWithRoles;
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

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase2() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES,
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingTestCase3() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES,
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_1_NAME, L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

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
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L1_ORG_2_NAME, L2_ORG_3_NAME,
                        L1_ORG_3_NAME));

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

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 5);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME),
                        getOrgId(L2_ORG_3_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE),
                        createRoleWithAudience(ORG_ROLE_3, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

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

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
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
