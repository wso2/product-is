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

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
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

import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.IMMEDIATE_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.TENANT_PATH;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;

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

    // Invalid Selective User Sharing.

    @DataProvider(name = "selectiveUserSharingWithInvalidDetailsDataProvider")
    public Object[][] selectiveUserSharingWithInvalidDetailsDataProvider() {

        // Test case 1: User sharing with invalid roles.
        List<String> userIdsForTestCase1 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase1();

        // Test case 2: User sharing with invalid organizations.
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 = setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase2();

        // Test case 3: User sharing with invalid users.
        List<String> userIdsForTestCase3 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Map<String, Object>> organizationsForTestCase3 = setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase3();

        // Test case 4: User sharing with conflicting users.
        List<String> userIdsForTestCase4 = Collections.singletonList(getUserId(ROOT_ORG_USER_DUPLICATED_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase4 = setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase4();
        Map<String, Object> expectedResultsForTestCase4 = setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase4();

        // Test case 5: User sharing with non-immediate child organizations.
        List<String> userIdsForTestCase5 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase5 = setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase5();
        Map<String, Object> expectedResultsForTestCase5 = setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase5();

        return new Object[][] {
                { userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1 },
                { userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2 },
                { userIdsForTestCase3, organizationsForTestCase3, expectedResultsForTestCase3 },
                { userIdsForTestCase4, organizationsForTestCase4, expectedResultsForTestCase4 }
        };
    }

    @Test(dataProvider = "selectiveUserSharingWithInvalidDetailsDataProvider")
    public void testSelectiveUserSharing(List<String> userIds,
                                         Map<String, Map<String, Object>> organizations,
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

    // Invalid General User Sharing.

    @DataProvider(name = "generalUserSharingWithInvalidDetailsDataProvider")
    public Object[][] generalUserSharingWithInvalidDetailsDataProvider() {

        // Test case 1: User sharing with invalid roles.
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase1();
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase1();

        // Test case 2: User sharing with invalid users.
        List<String> userIdsForTestCase2 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase2();
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase2();

        // Test case 3: User sharing with conflicting users.
        List<String> userIdsForTestCase3 = Collections.singletonList(getUserId(ROOT_ORG_USER_DUPLICATED_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase3 = setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase3();
        Map<String, Object> expectedResultsForTestCase3 = setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase3();

        return new Object[][] {
                { userIdsForTestCase1, policyWithRolesForTestCase1, expectedResultsForTestCase1 },
                { userIdsForTestCase2, policyWithRolesForTestCase2, expectedResultsForTestCase2 },
                { userIdsForTestCase3, policyWithRolesForTestCase3, expectedResultsForTestCase3 }
        };
    }

    @Test(dataProvider = "generalUserSharingWithInvalidDetailsDataProvider")
    public void testGeneralUserSharing(List<String> userIds,
                                                         Map<String, Object> policyWithRoles,
                                                         Map<String, Object> expectedResults) throws Exception {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoles))
                .roles(getRolesForGeneralUserSharing(policyWithRoles));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // Invalid General User Unsharing.

    @DataProvider(name = "generalUserUnsharingWithInvalidDetailsDataProvider")
    public Object[][] generalUserUnsharingWithInvalidDetailsDataProvider() {

        List<String> sharingUserIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase1();
        List<String> userIdsForTestCase1 = Collections.singletonList(INVALID_USER_1_ID);
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase1();

        List<String> sharingUserIdsForTestCase2 = Collections.singletonList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase2();
        List<String> userIdsForTestCase2 = Arrays.asList(INVALID_USER_1_ID, INVALID_USER_2_ID);
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase2();

        return new Object[][] {
                { sharingUserIdsForTestCase1, policyWithRolesForTestCase1, expectedSharedResultsForTestCase1, userIdsForTestCase1, expectedResultsForTestCase1},
                { sharingUserIdsForTestCase2, policyWithRolesForTestCase2, expectedSharedResultsForTestCase2, userIdsForTestCase2, expectedResultsForTestCase2},
        };
    }

    @Test(dataProvider = "generalUserUnsharingWithInvalidDetailsDataProvider")
    public void testGeneralUserUnsharing(List<String> userIds,
                                         Map<String, Object> policyWithRoles,
                                         Map<String, Object> expectedSharedResults,
                                         List<String> removingUserIds,
                                         Map<String, Object> expectedResults) throws Exception {

        // Sharing valid users.
        testGeneralUserSharing(userIds, policyWithRoles, expectedSharedResults);

        // Unsharing invalid users.
        UserUnshareWithAllRequestBody requestBody = new UserUnshareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(removingUserIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH, toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // Invalid Selective User Unsharing.

    @DataProvider(name = "selectiveUserUnsharingDataProvider")
    public Object[][] selectiveUserUnsharingDataProvider() {

        // ALL EXISTING
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase1();
        List<String> removingUserIdsForTestCase1 = Arrays.asList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), INVALID_USER_1_ID, INVALID_USER_2_ID);
        List<String> removingOrgIdsForTestCase1 =Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L1_ORG_2_NAME));
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserUnsharingWithInvalidDetailsTestCase1();

        // IMMEDIATE EXISTING AND FUTURE
        List<String> userIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase2 = setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase2();
        Map<String, Object> expectedSharedResultsForTestCase2 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase2();
        List<String> removingUserIdsForTestCase2 = Arrays.asList(getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME), INVALID_USER_1_ID);
        List<String> removingOrgIdsForTestCase2 = Arrays.asList(getOrgId(L1_ORG_1_NAME), INVALID_ORG_1_ID);
        Map<String, Object> expectedResultsForTestCase2 = setExpectedResultsForSelectiveUserUnsharingWithInvalidDetailsTestCase2();

        return new Object[][] {
                { userIdsForTestCase1, policyWithRolesForTestCase1, expectedSharedResultsForTestCase1, removingUserIdsForTestCase1, removingOrgIdsForTestCase1, expectedResultsForTestCase1},
                { userIdsForTestCase2, policyWithRolesForTestCase2, expectedSharedResultsForTestCase2, removingUserIdsForTestCase2, removingOrgIdsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingDataProvider")
    public void testSelectiveUserUnsharing(List<String> userIds,
                                           Map<String, Object> policyWithRoles,
                                           Map<String, Object> expectedSharedResults,
                                           List<String> removingUserIds,
                                           List<String> removingOrgIds,
                                           Map<String, Object> expectedResults) throws Exception {

        testGeneralUserSharing(userIds, policyWithRoles, expectedSharedResults);

        UserUnshareRequestBody requestBody = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(removingUserIds))
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

    // Invalid Selective User Sharing for re-sharing.

    @DataProvider(name = "selectiveUserSharingWithReSharingDataProvider")
    public Object[][] selectiveUserSharingWithReSharingDataProvider() {

        // Test case 1: User re-sharing.
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 = setOrganizationsForSelectiveUserSharingWithValidDetailsTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingWithValidDetailsTestCase1();
        Map<String, Map<String, Object>> organizationsForReSharingTestCase1 = setOrganizationsForSelectiveUserSharingWithReSharingTestCase1();
        Map<String, Object> reSharingSubOrgDetailsForTestCase1 = orgDetails.get(L1_ORG_1_NAME);
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForSelectiveUserSharingWithReSharingTestCase1();

        return new Object[][] {
                { userIdsForTestCase1, organizationsForTestCase1, expectedSharedResultsForTestCase1, organizationsForReSharingTestCase1, reSharingSubOrgDetailsForTestCase1, expectedResultsForTestCase1 }
        };
    }

    @Test(dataProvider = "selectiveUserSharingWithReSharingDataProvider")
    public void testSelectiveUserSharingWithReSharing(List<String> userIds,
                                                      Map<String, Map<String, Object>> organizations,
                                                      Map<String, Object> expectedSharedResults,
                                                      Map<String, Map<String, Object>> organizationsForReSharing,
                                                      Map<String, Object> reSharingSubOrgDetails,
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

        List<String> sharedUserIds = validateUserSharingResultsAndGetSharedUsersList(userIds, reSharingSubOrgDetails, expectedSharedResults);

        UserShareRequestBody requestBodyForReSharing = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(sharedUserIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizationsForReSharing));

        HttpResponse responseOfReSharing =  getResponseOfPostToSubOrg(USER_SHARING_API_BASE_PATH + SHARE_PATH, toJSONString(requestBodyForReSharing), reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN).toString());

        Assert.assertEquals(responseOfReSharing.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);

        validateUserSharingResults(sharedUserIds, expectedResults);
    }

    // Invalid General User Sharing for re-sharing.

    @DataProvider(name = "generalUserSharingWithReSharingDataProvider")
    public Object[][] generalUserSharingWithReSharingDataProvider() {

        // Test case 1: User re-sharing.
        List<String> userIdsForTestCase1 = Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRolesForTestCase1 = setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase1();
        Map<String, Object> expectedSharedResultsForTestCase1 = setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase1();
        Map<String, Object> reSharingSubOrgDetailsForTestCase1 = orgDetails.get(L1_ORG_1_NAME);
        Map<String, Object> expectedResultsForTestCase1 = setExpectedResultsForGenealUserSharingWithReSharingTestCase1();

        return new Object[][] {
                { userIdsForTestCase1, policyWithRolesForTestCase1, expectedSharedResultsForTestCase1, reSharingSubOrgDetailsForTestCase1, expectedResultsForTestCase1 }
        };
    }

    @Test(dataProvider = "generalUserSharingWithReSharingDataProvider")
    public void testGeneralUserSharingWithReSharing(List<String> userIds, Map<String, Object> policyWithRoles,
                                                    Map<String, Object> expectedSharedResults,
                                                    Map<String, Object> reSharingSubOrgDetails,
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

        List<String> sharedUserIds = validateUserSharingResultsAndGetSharedUsersList(userIds, reSharingSubOrgDetails, expectedSharedResults);

        UserShareWithAllRequestBody requestBodyForReSharing = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoles))
                .roles(getRolesForGeneralUserSharing(policyWithRoles));

        HttpResponse responseOfReSharing =  getResponseOfPostToSubOrg(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH,
                toJSONString(requestBodyForReSharing), reSharingSubOrgDetails.get(MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN).toString());

        Assert.assertEquals(responseOfReSharing.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);

        validateUserSharingResults(sharedUserIds, expectedResults);
    }

    // Test cases builders.

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase1() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase1() {

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

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase2() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase2() {

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

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase3() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase4() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L1_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase4() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 3);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithInvalidDetailsTestCase5() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L3_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L3_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithInvalidDetailsTestCase5() {

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

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase1() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Arrays.asList(
                createRoleWithAudience(INVALID_APP_ROLE_1, INVALID_APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(APP_ROLE_1, INVALID_APP_2_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(INVALID_APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(INVALID_ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE),
                createRoleWithAudience(INVALID_ORG_ROLE_2, INVALID_ORG_1_NAME, ORGANIZATION_AUDIENCE),
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase1() {

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

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase2() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingWithInvalidDetailsTestCase3() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithInvalidDetailsTestCase3() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Arrays.asList(L1_ORG_2_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithValidDetailsTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L1_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

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

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithValidDetailsTestCase1() {

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

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase1() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Collections.singletonList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase1() {

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

    private Map<String, Object> setPolicyWithRolesForGeneralUserSharingWithValidDetailsTestCase2() {

        Map<String, Object> policyWithRoles = new HashMap<>();

        policyWithRoles.put(MAP_KEY_GENERAL_POLICY, IMMEDIATE_EXISTING_ORGS_ONLY);
        policyWithRoles.put(MAP_KEY_GENERAL_ROLES, Arrays.asList(createRoleWithAudience(APP_ROLE_3, APP_1_NAME, APPLICATION_AUDIENCE), createRoleWithAudience(ORG_ROLE_3, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        return policyWithRoles;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingWithValidDetailsTestCase2() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingWithInvalidDetailsTestCase1() {

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

    private Map<String, Object> setExpectedResultsForSelectiveUserUnsharingWithInvalidDetailsTestCase2() {

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

    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingWithReSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        // Organization 1
        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L2_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L2_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLES, Collections.singletonList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));

        organizations.put(L2_ORG_1_NAME, org1);

        // Organization 2
        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L2_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L2_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLES, Arrays.asList(
                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE),
                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE)));

        organizations.put(L2_ORG_2_NAME, org2);

        return organizations;
    }

    private Map<String, Object> setExpectedResultsForSelectiveUserSharingWithReSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 0);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS, Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES, Collections.emptyList());

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        return expectedResults;
    }

    private Map<String, Object> setExpectedResultsForGenealUserSharingWithReSharingTestCase1() {

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

        Map<String, String> rootOrgOrganizationRoles = createOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2, ORG_ROLE_3));

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
        createSuborgUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_DUPLICATED_USERNAME, ROOT_ORG_NAME), L1_ORG_1_NAME);
    }
}
