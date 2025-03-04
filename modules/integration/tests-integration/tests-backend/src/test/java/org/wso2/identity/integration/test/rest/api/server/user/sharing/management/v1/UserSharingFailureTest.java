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
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBody;
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
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN;

/**
 * Tests for failure cases of the User Sharing REST APIs.
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

    // Selective User Sharing.

    @DataProvider(name = "selectiveUserSharingWithInvalidRolesDataProvider")
    public Object[][] selectiveUserSharingWithInvalidRolesDataProvider() {

        // Test case 1: User sharing with invalid roles.
        List<String> userIdsForTestCase1 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingTestCase1();

        // Test case 2: User sharing with invalid organizations.
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY));
        Map<String, Map<String, Object>> organizationsForTestCase2 = setOrganizationsForSelectiveUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserSharingTestCase2();

        // Test case 3: User sharing with invalid users.
        List<String> userIdsForTestCase3 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Map<String, Object>> organizationsForTestCase3 = setOrganizationsForSelectiveUserSharingTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForSelectiveUserSharingTestCase3();



        return new Object[][] {
                { userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1 },
                { userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2 },
                { userIdsForTestCase3, organizationsForTestCase3, expectedResultsForTestCase3 }
        };
    }

    @Test(dataProvider = "selectiveUserSharingWithInvalidRolesDataProvider")
    public void testSelectiveUserSharingWithInvalidRoles(List<String> userIds,
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
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        Thread.sleep(5000); // Waiting until user sharing is completed.
        for (String userId : userIds) {
            validateUserHasBeenSharedToExpectedOrgsWithExpectedRoles(userId, expectedResults);
        }
    }

    // Test cases builders for selective user sharing.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, INVALID_APP_2_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, INVALID_ORG_1_NAME, ORGANIZATION_AUDIENCE),
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        // Organization 3
        Map<String, Object> org3 = new HashMap<>();
        org3.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_3_NAME));
        org3.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_3_NAME);
        org3.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org3.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(INVALID_APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(INVALID_ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                createRoleWithAudience(INVALID_ORG_ROLE_2, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

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

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, INVALID_ORG_1_ID);
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, INVALID_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(INVALID_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase3() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();

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
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL, new JSONObject(readResource(AUTHORIZED_APIS_JSON)));
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

        Map<String, String> rootOrgOrganizationRoles = createOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2, ORG_ROLE_3));

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
