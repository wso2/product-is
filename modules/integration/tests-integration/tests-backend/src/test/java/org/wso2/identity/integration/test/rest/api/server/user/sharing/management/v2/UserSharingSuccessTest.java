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
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.ExpectedSharingMode;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserSharePatchRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareWithAllRequestBody;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.*;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.API_VERSION;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APPLICATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.APP_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.AUTHORIZED_APIS_JSON;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L1_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_2_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L2_ORG_3_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.L3_ORG_1_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_COUNT;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_IDS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ORG_NAMES;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_GENERAL_ROLE_ASSIGNMENTS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_POLICY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORGANIZATION_AUDIENCE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORG_ROLE_1;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ORG_ROLE_2;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.PATCH_SHARED_ORGANIZATIONS_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAILS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_PATCH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_SHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_DETAIL_VALUE_UNSHARING;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_STATUS;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.RESPONSE_STATUS_VALUE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_NAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_1_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_2_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.ROOT_ORG_USER_3_USERNAME;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.SHARE_WITH_ALL_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.UNSHARE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.UNSHARE_WITH_ALL_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.USER_DOMAIN_PRIMARY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant.UserSharingConstants.USER_SHARING_API_BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation.OpEnum.ADD;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.PatchOperation.OpEnum.REMOVE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments.ModeEnum.NONE;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments.ModeEnum.SELECTED;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS;

/**
 * Integration tests for the V2 User Sharing REST API — success cases.
 *
 * <p>The six test phases execute in a fixed dependency chain so each phase starts from a
 * well-defined DB state:
 *
 * <pre>
 * Phase 1 — testSelectiveUserSharing    (DB: clean)
 *   ↓ depends-on
 * Phase 2 — testSelectiveUserUnsharing  (DB: Phase-1 leftovers)
 *   ↓ depends-on
 * Phase 3 — testGeneralUserSharing      (DB: clean after Phase 2)
 *   ↓ depends-on
 * Phase 4 — testGeneralUserUnsharing    (DB: Phase-3 leftovers)
 *   ↓ depends-on
 * Phase 5 — testPatchGeneralSharedUser  (self-contained: own setup + cleanup)
 *   ↓ depends-on
 * Phase 6 — testPatchSelectiveSharedUser (self-contained: own setup + cleanup)
 * </pre>
 *
 * <p>Key V2 differences exercised here that are absent from V1:
 * <ul>
 *   <li>Role intent is expressed through {@link RoleAssignments} (mode + roles) instead of a flat
 *       role list. Mode {@code NONE} skips role assignment entirely and causes
 *       {@code sharingMode.roleAssignment.roles} to be absent (not empty) in the GET response.</li>
 *   <li>Selective share uses only two policies: {@code SELECTED_ORG_ONLY} (never stored in
 *       ResourceSharingPolicy) and
 *       {@code SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN}.</li>
 *   <li>General share supports only {@code ALL_EXISTING_AND_FUTURE_ORGS}.</li>
 *   <li>The GET endpoint returns roles and sharingMode inline. There is no separate
 *       {@code /shared-roles} endpoint. Assertions are therefore richer — both per-org inline
 *       roles and the top-level / per-org sharingMode are validated in every check.</li>
 *   <li>A new PATCH endpoint allows additive or selective role changes on an already-shared user.
 *       PATCH never modifies the ResourceSharingPolicy table, so
 *       {@code sharingMode.roleAssignment.roles} always reflects the original policy-time roles
 *       regardless of subsequent PATCH operations.</li>
 * </ul>
 */
public class UserSharingSuccessTest extends UserSharingBaseTest {

    // Sharingmode policy string constants — these are the exact JSON string values that the
    // GET response's sharingMode.policy field returns. They mirror the corresponding PolicyEnum
    // values from the V2 model classes.
    private static final String POLICY_ALL_EXISTING_AND_FUTURE_ORGS = "ALL_EXISTING_AND_FUTURE_ORGS";
    private static final String POLICY_WITH_CHILDREN =
            "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN";

    // Role-assignment mode strings used when building ExpectedSharingMode objects.
    private static final String MODE_SELECTED = "SELECTED";
    private static final String MODE_NONE = "NONE";

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

    // =========================================================================
    // Phase 1 — Selective User Sharing
    // =========================================================================

    /**
     * Data provider for {@link #testSelectiveUserSharing}.
     *
     * <p>TC1: rootUser1 shared to L1Org1 with {@code SELECTED_ORG_ONLY} and mode {@code NONE}.
     * {@code SELECTED_ORG_ONLY} is never written to the ResourceSharingPolicy table, so no
     * per-org sharingMode is present in the GET response.
     *
     * <p>TC2: rootUser2 shared to L1Org2 with {@code WITH_CHILDREN} and {@code APP_ROLE_1}.
     * L1Org2 becomes the policy-holding org; L2Org3 is its child. Only L1Org2 shows a per-org
     * sharingMode in the GET response.
     */
    @DataProvider(name = "selectiveUserSharingDataProvider")
    public Object[][] selectiveUserSharingDataProvider() {

        // TC1 — rootUser1, L1Org1, SELECTED_ORG_ONLY, NONE
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase1 =
                setOrganizationsForSelectiveUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForSelectiveUserSharingTestCase1();

        // TC2 — rootUser2, L1Org2, WITH_CHILDREN, SELECTED, [APP_ROLE_1]
        List<String> userIdsForTestCase2 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Map<String, Object>> organizationsForTestCase2 =
                setOrganizationsForSelectiveUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForSelectiveUserSharingTestCase2();

        return new Object[][]{
                {userIdsForTestCase1, organizationsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, organizationsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "selectiveUserSharingDataProvider")
    public void testSelectiveUserSharing(List<String> userIds,
                                         Map<String, Map<String, Object>> organizations,
                                         Map<String, Object> expectedResults) throws Exception {

        UserShareRequestBody requestBody = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(organizations));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // =========================================================================
    // Phase 2 — Selective User Unsharing
    // =========================================================================

    /**
     * Data provider for {@link #testSelectiveUserUnsharing}.
     *
     * <p>Entering state: TC1 row (rootUser1 → L1Org1) and TC2 rows
     * (rootUser2 → L1Org2, L2Org3) from Phase 1.
     *
     * <p>TC1: rootUser1 unshared from L1Org1. No cascade (L1Org1 has no children). No
     * ResourceSharingPolicy row existed (SELECTED_ORG_ONLY was never saved), so the policy
     * deletion is a no-op. DB becomes fully clean for rootUser1.
     *
     * <p>TC2: rootUser2 unshared from L1Org2. orgTreeInclusive = [L1Org2, L2Org3] — both rows
     * removed. The WITH_CHILDREN policy row for L1Org2 is deleted. DB fully clean for rootUser2.
     */
    @DataProvider(name = "selectiveUserUnsharingDataProvider")
    public Object[][] selectiveUserUnsharingDataProvider() {

        // TC1 — rootUser1, unshare from L1Org1
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        List<String> removingOrgIdsForTestCase1 = Collections.singletonList(getOrgId(L1_ORG_1_NAME));

        // TC2 — rootUser2, unshare from L1Org2 (cascades to L2Org3)
        List<String> userIdsForTestCase2 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        List<String> removingOrgIdsForTestCase2 = Collections.singletonList(getOrgId(L1_ORG_2_NAME));

        Map<String, Object> expectedResultsAfterUnshare = setExpectedResultsForEmptyShare();

        return new Object[][]{
                {userIdsForTestCase1, removingOrgIdsForTestCase1, expectedResultsAfterUnshare},
                {userIdsForTestCase2, removingOrgIdsForTestCase2, expectedResultsAfterUnshare}
        };
    }

    @Test(dataProvider = "selectiveUserUnsharingDataProvider",
            dependsOnMethods = {"testSelectiveUserSharing"})
    public void testSelectiveUserUnsharing(List<String> userIds, List<String> removingOrgIds,
                                           Map<String, Object> expectedResults) throws Exception {

        UserUnshareRequestBody requestBody = new UserUnshareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds))
                .orgIds(getOrganizationsForSelectiveUserUnsharing(removingOrgIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // =========================================================================
    // Phase 3 — General User Sharing
    // =========================================================================

    /**
     * Data provider for {@link #testGeneralUserSharing}.
     *
     * <p>Entering state: clean DB (Phase 2 fully unshared all selective shares).
     *
     * <p>TC1: rootUser1 shared with {@code ALL_EXISTING_AND_FUTURE_ORGS}, mode {@code SELECTED},
     * role {@code ORG_ROLE_1}. All 7 descendant orgs receive the share and the role. The policy
     * is stored at Root level, so the top-level sharingMode is populated and all per-org
     * sharingMode fields are null.
     *
     * <p>TC2: rootUser2 and rootUser3 shared with {@code ALL_EXISTING_AND_FUTURE_ORGS}, mode
     * {@code NONE}. All 7 orgs receive the share with no role assignments. The top-level
     * sharingMode.roleAssignment.roles field is absent (not an empty list) because mode is NONE.
     */
    @DataProvider(name = "generalUserSharingDataProvider")
    public Object[][] generalUserSharingDataProvider() {

        // TC1 — rootUser1, ALL_EXISTING_AND_FUTURE_ORGS, SELECTED, [ORG_ROLE_1]
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentsForTestCase1 =
                setPolicyWithRoleAssignmentsForGeneralUserSharingTestCase1();
        Map<String, Object> expectedResultsForTestCase1 =
                setExpectedResultsForGeneralUserSharingTestCase1();

        // TC2 — rootUser2 + rootUser3, ALL_EXISTING_AND_FUTURE_ORGS, NONE
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));
        Map<String, Object> policyWithRoleAssignmentsForTestCase2 =
                setPolicyWithRoleAssignmentsForGeneralUserSharingTestCase2();
        Map<String, Object> expectedResultsForTestCase2 =
                setExpectedResultsForGeneralUserSharingTestCase2();

        return new Object[][]{
                {userIdsForTestCase1, policyWithRoleAssignmentsForTestCase1, expectedResultsForTestCase1},
                {userIdsForTestCase2, policyWithRoleAssignmentsForTestCase2, expectedResultsForTestCase2}
        };
    }

    @Test(dataProvider = "generalUserSharingDataProvider",
            dependsOnMethods = {"testSelectiveUserUnsharing"})
    public void testGeneralUserSharing(List<String> userIds,
                                       Map<String, Object> policyWithRoleAssignments,
                                       Map<String, Object> expectedResults) throws Exception {

        UserShareWithAllRequestBody requestBody = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(getPolicyEnumForGeneralUserSharing(policyWithRoleAssignments))
                .roleAssignment(getRoleAssignmentsForGeneralUserSharing(policyWithRoleAssignments));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // =========================================================================
    // Phase 4 — General User Unsharing
    // =========================================================================

    /**
     * Data provider for {@link #testGeneralUserUnsharing}.
     *
     * <p>Entering state: rootUser1 (7 orgs + ORG_ROLE_1), rootUser2 (7 orgs, no roles),
     * rootUser3 (7 orgs, no roles) from Phase 3.
     *
     * <p>TC1: rootUser1 unshared with {@code unshare-with-all}. All 7 UserAssociation rows and
     * the Root-level ResourceSharingPolicy row are deleted.
     *
     * <p>TC2: rootUser2 and rootUser3 unshared together. Same outcome per user. DB fully clean.
     */
    @DataProvider(name = "generalUserUnsharingDataProvider")
    public Object[][] generalUserUnsharingDataProvider() {

        // TC1 — rootUser1
        List<String> userIdsForTestCase1 =
                Collections.singletonList(getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));

        // TC2 — rootUser2 + rootUser3
        List<String> userIdsForTestCase2 =
                Arrays.asList(getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME),
                        getUserId(ROOT_ORG_USER_3_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME));

        Map<String, Object> expectedResultsAfterUnshare = setExpectedResultsForEmptyShare();

        return new Object[][]{
                {userIdsForTestCase1, expectedResultsAfterUnshare},
                {userIdsForTestCase2, expectedResultsAfterUnshare}
        };
    }

    @Test(dataProvider = "generalUserUnsharingDataProvider",
            dependsOnMethods = {"testGeneralUserSharing"})
    public void testGeneralUserUnsharing(List<String> userIds,
                                         Map<String, Object> expectedResults) throws Exception {

        UserUnshareWithAllRequestBody requestBody = new UserUnshareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds));

        Response response = getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH,
                toJSONString(requestBody));

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_UNSHARING));

        validateUserSharingResults(userIds, expectedResults);
    }

    // =========================================================================
    // Phase 5 — PATCH on a General-Shared User (rootUser1, self-contained)
    // =========================================================================

    /**
     * Tests PATCH role operations on a user that has been shared via {@code share-with-all}.
     *
     * <p>This test is self-contained: it performs its own share setup, runs both PATCH test cases
     * sequentially (TC2 relies on the state left by TC1), then cleans up with
     * {@code unshare-with-all} regardless of assertion outcome.
     *
     * <p><b>Internal setup:</b> rootUser1 → {@code ALL_EXISTING_AND_FUTURE_ORGS}, mode
     * {@code SELECTED}, role {@code APP_ROLE_1}. Creates 7 UserAssociation rows, each with
     * {@code APP_ROLE_1}. Root-level policy stored: {@code SELECTED / [APP_ROLE_1]}.
     *
     * <p><b>TC1 — PATCH ADD APP_ROLE_2 to L1Org1:</b> {@code handleRoleAssignmentAddition} is
     * additive — APP_ROLE_1 is preserved. No cascade to L2Org1, L2Org2, L3Org1. Policy table
     * unchanged. After TC1: L1Org1 has [APP_ROLE_1, APP_ROLE_2]; all other orgs have [APP_ROLE_1].
     * Top-level sharingMode.roleAssignment.roles = [APP_ROLE_1] (original policy, not current
     * L1Org1 assignments).
     *
     * <p><b>TC2 — PATCH REMOVE APP_ROLE_1 from L1Org1:</b> APP_ROLE_1 removed from L1Org1; APP_ROLE_2
     * is preserved. No cascade. Policy table still unchanged. After TC2: L1Org1 has [APP_ROLE_2];
     * all other orgs have [APP_ROLE_1]. Top-level sharingMode.roleAssignment.roles still =
     * [APP_ROLE_1].
     */
    @Test(dependsOnMethods = {"testGeneralUserUnsharing"})
    public void testPatchGeneralSharedUser() throws Exception {

        String rootUser1Id = getUserId(ROOT_ORG_USER_1_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        List<String> userIds = Collections.singletonList(rootUser1Id);

        // ---- Internal setup ----
        UserShareWithAllRequestBody setupRequest = new UserShareWithAllRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .policy(ALL_EXISTING_AND_FUTURE_ORGS)
                .roleAssignment(createRoleAssignments(SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_WITH_ALL_PATH,
                toJSONString(setupRequest))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, setExpectedResultsAfterPatchGeneralShareSetup());

        try {
            // ---- TC1: PATCH ADD APP_ROLE_2 to L1Org1 ----
            UserSharePatchRequestBody patchRequestTC1 = new UserSharePatchRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                    .operations(Collections.singletonList(
                            buildPatchOperation(ADD, getOrgId(L1_ORG_1_NAME),
                                    Collections.singletonList(
                                            createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)))));

            getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH,
                    toJSONString(patchRequestTC1))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_ACCEPTED)
                    .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                    .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_PATCH));

            validateUserSharingResults(userIds, setExpectedResultsAfterPatchGeneralSharingTestCase1());

            // ---- TC2: PATCH REMOVE APP_ROLE_1 from L1Org1 ----
            UserSharePatchRequestBody patchRequestTC2 = new UserSharePatchRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                    .operations(Collections.singletonList(
                            buildPatchOperation(REMOVE, getOrgId(L1_ORG_1_NAME),
                                    Collections.singletonList(
                                            createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)))));

            getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH,
                    toJSONString(patchRequestTC2))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_ACCEPTED)
                    .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                    .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_PATCH));

            validateUserSharingResults(userIds, setExpectedResultsAfterPatchGeneralSharingTestCase2());

        } finally {
            // ---- Internal cleanup (always runs) ----
            UserUnshareWithAllRequestBody cleanupRequest = new UserUnshareWithAllRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds));
            getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH,
                    toJSONString(cleanupRequest));
            validateUserSharingResults(userIds, setExpectedResultsForEmptyShare());
        }
    }

    // =========================================================================
    // Phase 6 — PATCH on a Selective-Shared User (rootUser2, self-contained)
    // =========================================================================

    /**
     * Tests PATCH role operations on a user that has been shared via the selective
     * {@code share} endpoint with {@code SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN}.
     *
     * <p>This test is self-contained: own setup, sequential TCs, try-finally cleanup.
     *
     * <p><b>Internal setup:</b> rootUser2 → L1Org1, {@code WITH_CHILDREN}, mode {@code SELECTED},
     * role {@code APP_ROLE_1}. Creates UserAssociation rows for L1Org1 (policy holder), L2Org1,
     * L2Org2, and L3Org1, each with {@code APP_ROLE_1}. ResourceSharingPolicy row:
     * policyHoldingOrgId = L1Org1, policy = {@code WITH_CHILDREN}, roles = [APP_ROLE_1].
     *
     * <p><b>TC1 — PATCH ADD APP_ROLE_2 to L2Org1 (a child org, not the policy holder):</b>
     * PATCH is strictly per-org; no cascade. L1Org1 (the policy holder) is untouched.
     * L2Org1 per-org sharingMode remains null after this PATCH — sharingMode is determined by
     * the original share policy, not by PATCH targeting. After TC1: L1Org1 = [APP_ROLE_1] with
     * per-org sharingMode = WITH_CHILDREN/SELECTED/[APP_ROLE_1]; L2Org1 = [APP_ROLE_1, APP_ROLE_2]
     * with per-org sharingMode = null; L2Org2 = [APP_ROLE_1]; L3Org1 = [APP_ROLE_1].
     *
     * <p><b>TC2 — PATCH REMOVE APP_ROLE_1 from L1Org1 (the policy holder):</b> Proves that the
     * ResourceSharingPolicy table is never modified by PATCH — the per-org sharingMode for L1Org1
     * still shows roleAssignment.roles = [APP_ROLE_1] even after removal. L2Org1 retains
     * [APP_ROLE_1, APP_ROLE_2] from TC1; no cascade. After TC2: L1Org1 = [] with per-org
     * sharingMode still = WITH_CHILDREN/SELECTED/[APP_ROLE_1]; all other orgs unchanged.
     */
    @Test(dependsOnMethods = {"testPatchGeneralSharedUser"})
    public void testPatchSelectiveSharedUser() throws Exception {

        String rootUser2Id = getUserId(ROOT_ORG_USER_2_USERNAME, USER_DOMAIN_PRIMARY, ROOT_ORG_NAME);
        List<String> userIds = Collections.singletonList(rootUser2Id);

        // ---- Internal setup: selective share rootUser2 → L1Org1, WITH_CHILDREN, [APP_ROLE_1] ----
        Map<String, Map<String, Object>> setupOrganizations = new HashMap<>();
        Map<String, Object> setupOrgEntry = new HashMap<>();
        setupOrgEntry.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        setupOrgEntry.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        setupOrgEntry.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        setupOrgEntry.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS,
                createRoleAssignments(SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));
        setupOrganizations.put(L1_ORG_1_NAME, setupOrgEntry);

        UserShareRequestBody setupRequest = new UserShareRequestBody()
                .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                .organizations(getOrganizationsForSelectiveUserSharing(setupOrganizations));

        getResponseOfPost(USER_SHARING_API_BASE_PATH + SHARE_PATH,
                toJSONString(setupRequest))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_SHARING));

        validateUserSharingResults(userIds, setExpectedResultsAfterPatchSelectiveShareSetup());

        try {
            // ---- TC1: PATCH ADD APP_ROLE_2 to L2Org1 (child — not the policy holder) ----
            UserSharePatchRequestBody patchRequestTC1 = new UserSharePatchRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                    .operations(Collections.singletonList(
                            buildPatchOperation(ADD, getOrgId(L2_ORG_1_NAME),
                                    Collections.singletonList(
                                            createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE)))));

            getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH,
                    toJSONString(patchRequestTC1))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_ACCEPTED)
                    .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                    .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_PATCH));

            validateUserSharingResults(userIds, setExpectedResultsAfterPatchSelectiveSharingTestCase1());

            // ---- TC2: PATCH REMOVE APP_ROLE_1 from L1Org1 (the policy holder) ----
            UserSharePatchRequestBody patchRequestTC2 = new UserSharePatchRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserSharing(userIds))
                    .operations(Collections.singletonList(
                            buildPatchOperation(REMOVE, getOrgId(L1_ORG_1_NAME),
                                    Collections.singletonList(
                                            createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)))));

            getResponseOfPatch(USER_SHARING_API_BASE_PATH + PATCH_SHARED_ORGANIZATIONS_PATH,
                    toJSONString(patchRequestTC2))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_ACCEPTED)
                    .body(RESPONSE_STATUS, equalTo(RESPONSE_STATUS_VALUE))
                    .body(RESPONSE_DETAILS, equalTo(RESPONSE_DETAIL_VALUE_PATCH));

            validateUserSharingResults(userIds, setExpectedResultsAfterPatchSelectiveSharingTestCase2());

        } finally {
            // ---- Internal cleanup (always runs) ----
            UserUnshareWithAllRequestBody cleanupRequest = new UserUnshareWithAllRequestBody()
                    .userCriteria(getUserCriteriaForBaseUserUnsharing(userIds));
            getResponseOfPost(USER_SHARING_API_BASE_PATH + UNSHARE_WITH_ALL_PATH,
                    toJSONString(cleanupRequest));
            validateUserSharingResults(userIds, setExpectedResultsForEmptyShare());
        }
    }

    // =========================================================================
    // Phase 1 — Test Case Builders
    // =========================================================================

    /**
     * TC1: rootUser1 → L1Org1, {@code SELECTED_ORG_ONLY}, mode {@code NONE}, no roles.
     * {@code SELECTED_ORG_ONLY} is never saved to ResourceSharingPolicy.
     */
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase1() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org1 = new HashMap<>();
        org1.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_1_NAME));
        org1.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_1_NAME);
        org1.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_ONLY);
        org1.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS,
                createRoleAssignments(NONE, Collections.emptyList()));

        organizations.put(L1_ORG_1_NAME, org1);
        return organizations;
    }

    /**
     * Expected results for Phase 1 TC1.
     *
     * <p>orgCount = 1 (L1Org1 only). No roles. No per-org sharingMode (SELECTED_ORG_ONLY is
     * never saved to the policy table). Top-level sharingMode absent (not a general share).
     */
    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 1);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Collections.singletonList(getOrgId(L1_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Collections.singletonList(L1_ORG_1_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // SELECTED_ORG_ONLY → no per-org sharingMode (null means field must be absent).
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode = new HashMap<>();
        expectedPerOrgSharingMode.put(getOrgId(L1_ORG_1_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgSharingMode);

        // Not a general share → no top-level sharingMode.
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);

        return expectedResults;
    }

    /**
     * TC2: rootUser2 → L1Org2, {@code WITH_CHILDREN}, mode {@code SELECTED}, role {@code APP_ROLE_1}.
     * Policy saved with policyHoldingOrgId = L1Org2. L2Org3 is a child and is shared automatically.
     */
    private Map<String, Map<String, Object>> setOrganizationsForSelectiveUserSharingTestCase2() {

        Map<String, Map<String, Object>> organizations = new HashMap<>();

        Map<String, Object> org2 = new HashMap<>();
        org2.put(MAP_KEY_SELECTIVE_ORG_ID, getOrgId(L1_ORG_2_NAME));
        org2.put(MAP_KEY_SELECTIVE_ORG_NAME, L1_ORG_2_NAME);
        org2.put(MAP_KEY_SELECTIVE_POLICY, SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        org2.put(MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS,
                createRoleAssignments(SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));

        organizations.put(L1_ORG_2_NAME, org2);
        return organizations;
    }

    /**
     * Expected results for Phase 1 TC2.
     *
     * <p>orgCount = 2 (L1Org2 + L2Org3). Both orgs have APP_ROLE_1. Only L1Org2 (the policy
     * holder) has a per-org sharingMode; L2Org3 (child) has null.
     *
     * <p>Top-level sharingMode is absent — this is a selective, not general, share.
     */
    private Map<String, Object> setExpectedResultsForSelectiveUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 2);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_2_NAME, L2_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE)));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // L1Org2 is the policy holder → per-org sharingMode present.
        // L2Org3 is a child org → per-org sharingMode absent (null).
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode = new HashMap<>();
        expectedPerOrgSharingMode.put(getOrgId(L1_ORG_2_NAME),
                new ExpectedSharingMode(POLICY_WITH_CHILDREN, MODE_SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE))));
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_3_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgSharingMode);

        // Not a general share → no top-level sharingMode.
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);

        return expectedResults;
    }

    // =========================================================================
    // Phase 3 — Test Case Builders
    // =========================================================================

    /**
     * TC1: {@code ALL_EXISTING_AND_FUTURE_ORGS}, mode {@code SELECTED}, role {@code ORG_ROLE_1}.
     * Policy stored at Root. replaceExistingPolicies=true removes any prior policy for the user.
     */
    private Map<String, Object> setPolicyWithRoleAssignmentsForGeneralUserSharingTestCase1() {

        Map<String, Object> policyWithRoleAssignments = new HashMap<>();

        policyWithRoleAssignments.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);
        policyWithRoleAssignments.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENTS,
                createRoleAssignments(SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return policyWithRoleAssignments;
    }

    /**
     * Expected results for Phase 3 TC1.
     *
     * <p>orgCount = 7. All 7 orgs have {@code ORG_ROLE_1}. Because ORG_ROLE_1 is an
     * organization-audience role, the audience display in the GET response is the specific
     * sub-org's name — not ROOT_ORG_NAME — for each org entry. The per-org sharingMode is null
     * for every org (policy is held at Root, not at any individual child org). The top-level
     * sharingMode reflects the original policy-time role (audience = ROOT_ORG_NAME) as stored in
     * the ResourceSharingPolicy table.
     */
    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME,
                        L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        // Org-audience role: audience display = the specific sub-org's own name (not ROOT_ORG_NAME).
        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L3_ORG_1_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_2_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L2_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME),
                Collections.singletonList(createRoleWithAudience(ORG_ROLE_1, L1_ORG_3_NAME, ORGANIZATION_AUDIENCE)));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // General share → per-org sharingMode is null for every org (policy held at Root, not
        // at any child org).
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);

        // Top-level sharingMode reflects the policy-time roles stored in ResourceSharingPolicy at
        // Root level. ORG_ROLE_1 was specified with audienceName = ROOT_ORG_NAME.
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE,
                new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED,
                        Collections.singletonList(
                                createRoleWithAudience(ORG_ROLE_1, ROOT_ORG_NAME, ORGANIZATION_AUDIENCE))));

        return expectedResults;
    }

    /**
     * TC2: {@code ALL_EXISTING_AND_FUTURE_ORGS}, mode {@code NONE}, no roles.
     * Mode NONE causes the role-assignment block to be skipped entirely in
     * {@code shareAndAssignRolesIfPresent}.
     */
    private Map<String, Object> setPolicyWithRoleAssignmentsForGeneralUserSharingTestCase2() {

        Map<String, Object> policyWithRoleAssignments = new HashMap<>();

        policyWithRoleAssignments.put(MAP_KEY_GENERAL_POLICY, ALL_EXISTING_AND_FUTURE_ORGS);
        policyWithRoleAssignments.put(MAP_KEY_GENERAL_ROLE_ASSIGNMENTS,
                createRoleAssignments(NONE, Collections.emptyList()));

        return policyWithRoleAssignments;
    }

    private Map<String, Object> setExpectedResultsForGeneralUserSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME,
                        L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.emptyList());
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.emptyList());
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);

        // NONE mode: ExpectedSharingMode(policy, mode, null) → roleAssignment.roles must be
        // absent in the GET response (asserted with nullValue(), not equalTo(emptyList())).
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE,
                new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_NONE, null));

        return expectedResults;
    }

    // =========================================================================
    // Shared Empty-Result Builder (Phases 2 and 4)
    // =========================================================================

    /**
     * Returns an expected-results map that represents zero shared organizations — used to
     * validate state after any unshare operation that fully removes a user's shares.
     */
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
    // Phase 5 — PATCH General: Expected Result Builders
    // =========================================================================

    /**
     * Expected state immediately after Phase 5 internal setup (share-with-all rootUser1,
     * ALL_EXISTING_AND_FUTURE_ORGS, SELECTED, [APP_ROLE_1]).
     *
     * <p>All 7 orgs have APP_ROLE_1. Top-level sharingMode =
     * ALL_EXISTING_AND_FUTURE_ORGS / SELECTED / [APP_ROLE_1]. Per-org sharingMode null everywhere.
     */
    private Map<String, Object> setExpectedResultsAfterPatchGeneralShareSetup() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME,
                        L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE,
                new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED,
                        Collections.singletonList(appRole1)));

        return expectedResults;
    }

    /**
     * Expected state after Phase 5 TC1 (PATCH ADD APP_ROLE_2 to L1Org1).
     *
     * <p>L1Org1: [APP_ROLE_1, APP_ROLE_2]. All other 6 orgs: [APP_ROLE_1]. Top-level
     * sharingMode.roleAssignment.roles = [APP_ROLE_1] — the original policy-time roles, unchanged
     * by PATCH.
     */
    private Map<String, Object> setExpectedResultsAfterPatchGeneralSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME,
                        L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);
        RoleWithAudience appRole2 = createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        // L1Org1 now has both roles after PATCH ADD.
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Arrays.asList(appRole1, appRole2));
        // Remaining orgs are unaffected by the PATCH targeting L1Org1 (no cascade).
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        // PATCH never modifies ResourceSharingPolicy → roles in sharingMode still = [APP_ROLE_1].
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE,
                new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED,
                        Collections.singletonList(appRole1)));

        return expectedResults;
    }

    /**
     * Expected state after Phase 5 TC2 (PATCH REMOVE APP_ROLE_1 from L1Org1, continuing from TC1).
     *
     * <p>L1Org1: [APP_ROLE_2] only. All other 6 orgs: [APP_ROLE_1] (no cascade). Top-level
     * sharingMode.roleAssignment.roles = [APP_ROLE_1] — STILL the original policy-time roles even
     * though L1Org1 no longer has APP_ROLE_1 assigned.
     */
    private Map<String, Object> setExpectedResultsAfterPatchGeneralSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 7);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME), getOrgId(L2_ORG_2_NAME),
                        getOrgId(L3_ORG_1_NAME), getOrgId(L1_ORG_2_NAME), getOrgId(L2_ORG_3_NAME),
                        getOrgId(L1_ORG_3_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME,
                        L1_ORG_2_NAME, L2_ORG_3_NAME, L1_ORG_3_NAME));

        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);
        RoleWithAudience appRole2 = createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        // L1Org1: APP_ROLE_1 was removed; APP_ROLE_2 (added in TC1) remains.
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(appRole2));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_3_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, null);
        // PATCH never modifies ResourceSharingPolicy → roles in sharingMode STILL = [APP_ROLE_1].
        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE,
                new ExpectedSharingMode(POLICY_ALL_EXISTING_AND_FUTURE_ORGS, MODE_SELECTED,
                        Collections.singletonList(appRole1)));

        return expectedResults;
    }

    // =========================================================================
    // Phase 6 — PATCH Selective: Expected Result Builders
    // =========================================================================

    /**
     * Expected state immediately after Phase 6 internal setup (selective share rootUser2 →
     * L1Org1, WITH_CHILDREN, SELECTED, [APP_ROLE_1]).
     *
     * <p>Four orgs shared: L1Org1 (policy holder), L2Org1, L2Org2, L3Org1 (all children of
     * L1Org1). All have APP_ROLE_1. Only L1Org1 has a per-org sharingMode. Top-level sharingMode
     * is absent (selective, not general, share).
     */
    private Map<String, Object> setExpectedResultsAfterPatchSelectiveShareSetup() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 4);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME),
                        getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME));

        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // L1Org1 is the policy holder for WITH_CHILDREN.
        // L2Org1, L2Org2, L3Org1 are children → per-org sharingMode = null.
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode = new HashMap<>();
        expectedPerOrgSharingMode.put(getOrgId(L1_ORG_1_NAME),
                new ExpectedSharingMode(POLICY_WITH_CHILDREN, MODE_SELECTED,
                        Collections.singletonList(appRole1)));
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_1_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_2_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L3_ORG_1_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgSharingMode);

        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);

        return expectedResults;
    }

    /**
     * Expected state after Phase 6 TC1 (PATCH ADD APP_ROLE_2 to L2Org1).
     *
     * <p>L2Org1: [APP_ROLE_1, APP_ROLE_2]. L1Org1, L2Org2, L3Org1 unchanged. PATCH is strictly
     * per-org — there is no cascade and L1Org1 (the policy holder) is untouched. L2Org1 per-org
     * sharingMode remains null even though it was the direct PATCH target; sharingMode is
     * determined by the original share policy, not by PATCH.
     */
    private Map<String, Object> setExpectedResultsAfterPatchSelectiveSharingTestCase1() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 4);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME),
                        getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME));

        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);
        RoleWithAudience appRole2 = createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.singletonList(appRole1));
        // L2Org1: APP_ROLE_2 added; APP_ROLE_1 preserved (PATCH ADD is additive).
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Arrays.asList(appRole1, appRole2));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // L1Org1 per-org sharingMode unchanged (still the policy holder with original roles).
        // L2Org1 per-org sharingMode remains null — PATCH does not make it a policy holder.
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode = new HashMap<>();
        expectedPerOrgSharingMode.put(getOrgId(L1_ORG_1_NAME),
                new ExpectedSharingMode(POLICY_WITH_CHILDREN, MODE_SELECTED,
                        Collections.singletonList(appRole1)));
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_1_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_2_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L3_ORG_1_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgSharingMode);

        expectedResults.put(MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE, null);

        return expectedResults;
    }

    /**
     * Expected state after Phase 6 TC2 (PATCH REMOVE APP_ROLE_1 from L1Org1, continuing from TC1).
     *
     * <p>L1Org1: [] (empty — APP_ROLE_1 removed). All other orgs unchanged. Critical assertion:
     * L1Org1 per-org sharingMode.roleAssignment.roles STILL = [APP_ROLE_1] because the
     * ResourceSharingPolicy table was not modified by the PATCH. L2Org1 retains [APP_ROLE_1,
     * APP_ROLE_2] from TC1 — no cascade from PATCH.
     */
    private Map<String, Object> setExpectedResultsAfterPatchSelectiveSharingTestCase2() {

        Map<String, Object> expectedResults = new HashMap<>();

        expectedResults.put(MAP_KEY_EXPECTED_ORG_COUNT, 4);
        expectedResults.put(MAP_KEY_EXPECTED_ORG_IDS,
                Arrays.asList(getOrgId(L1_ORG_1_NAME), getOrgId(L2_ORG_1_NAME),
                        getOrgId(L2_ORG_2_NAME), getOrgId(L3_ORG_1_NAME)));
        expectedResults.put(MAP_KEY_EXPECTED_ORG_NAMES,
                Arrays.asList(L1_ORG_1_NAME, L2_ORG_1_NAME, L2_ORG_2_NAME, L3_ORG_1_NAME));

        RoleWithAudience appRole1 = createRoleWithAudience(APP_ROLE_1, APP_1_NAME, APPLICATION_AUDIENCE);
        RoleWithAudience appRole2 = createRoleWithAudience(APP_ROLE_2, APP_1_NAME, APPLICATION_AUDIENCE);

        Map<String, List<RoleWithAudience>> expectedRolesPerExpectedOrg = new HashMap<>();
        // L1Org1: APP_ROLE_1 removed; no roles remain.
        expectedRolesPerExpectedOrg.put(getOrgId(L1_ORG_1_NAME), Collections.emptyList());
        // L2Org1: unchanged from TC1 (APP_ROLE_2 added, APP_ROLE_1 still present).
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_1_NAME), Arrays.asList(appRole1, appRole2));
        expectedRolesPerExpectedOrg.put(getOrgId(L2_ORG_2_NAME), Collections.singletonList(appRole1));
        expectedRolesPerExpectedOrg.put(getOrgId(L3_ORG_1_NAME), Collections.singletonList(appRole1));
        expectedResults.put(MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG, expectedRolesPerExpectedOrg);

        // L1Org1 per-org sharingMode.roleAssignment.roles STILL = [APP_ROLE_1] even though the
        // actual role was removed. PATCH never modifies the ResourceSharingPolicy table.
        Map<String, ExpectedSharingMode> expectedPerOrgSharingMode = new HashMap<>();
        expectedPerOrgSharingMode.put(getOrgId(L1_ORG_1_NAME),
                new ExpectedSharingMode(POLICY_WITH_CHILDREN, MODE_SELECTED,
                        Collections.singletonList(appRole1)));
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_1_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L2_ORG_2_NAME), null);
        expectedPerOrgSharingMode.put(getOrgId(L3_ORG_1_NAME), null);
        expectedResults.put(MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE, expectedPerOrgSharingMode);

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

        // Level 1
        addOrganization(L1_ORG_1_NAME);
        addOrganization(L1_ORG_2_NAME);
        addOrganization(L1_ORG_3_NAME);

        // Level 2
        addSubOrganization(L2_ORG_1_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_2_NAME, getOrgId(L1_ORG_1_NAME), 2);
        addSubOrganization(L2_ORG_3_NAME, getOrgId(L1_ORG_2_NAME), 2);

        // Level 3
        addSubOrganization(L3_ORG_1_NAME, getOrgId(L2_ORG_1_NAME), 3);
    }

    /**
     * Creates the org-scoped roles used by V2 tests (ORG_ROLE_1, ORG_ROLE_2) and the
     * application App1 with its application-scoped roles (APP_ROLE_1, APP_ROLE_2).
     * App1 is shared with all sub-orgs so that application roles are resolvable in every
     * descendant org during sharing.
     */
    protected void setupApplicationsAndRoles() throws Exception {

        Map<String, String> rootOrgOrganizationRoles = setUpOrganizationRoles(ROOT_ORG_NAME, Arrays.asList(ORG_ROLE_1, ORG_ROLE_2));

        createApplication(APP_1_NAME, APPLICATION_AUDIENCE, Arrays.asList(APP_ROLE_1, APP_ROLE_2));
        createApplication(APP_2_NAME, UserSharingConstants.ORGANIZATION_AUDIENCE, new ArrayList<>(rootOrgOrganizationRoles.keySet()));
    }

    private void setupUsers() throws Exception {

        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_1_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_2_USERNAME, ROOT_ORG_NAME));
        createUser(createUserObject(USER_DOMAIN_PRIMARY, ROOT_ORG_USER_3_USERNAME, ROOT_ORG_NAME));
    }
}
