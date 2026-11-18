/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.webhooks.usersharing;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleWithAudienceAudience;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.SelectiveShareOrgDetails;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBody;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.UserUnshareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingV2RestClient;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.webhooks.usersharing.eventpayloadbuilder.UserSharingEventTestExpectedEventPayloadBuilder.buildExpectedUserSharedEventPayload;
import static org.wso2.identity.integration.test.webhooks.usersharing.eventpayloadbuilder.UserSharingEventTestExpectedEventPayloadBuilder.buildExpectedUserUnsharedEventPayload;
import static org.wso2.identity.integration.test.webhooks.usersharing.eventpayloadbuilder.UserSharingEventTestExpectedEventPayloadBuilder.createOrganizationObject;
import static org.wso2.identity.integration.test.webhooks.usersharing.eventpayloadbuilder.UserSharingEventTestExpectedEventPayloadBuilder.createRoleObject;

/**
 * Tests the user sharing events received by the organization the user belongs to, when an administrator shares the
 * user with sub organizations and unshares it from them through the user sharing v2 API.
 */
public class UserSharingEventTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String TEST_NAME = "UserSharingEventTestCase";
    private static final String USER_SHARING_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/user-sharing";
    private static final String USER_SHARED_EVENT_URI = USER_SHARING_EVENT_CHANNEL + "/event-type/userShared";
    private static final String USER_UNSHARED_EVENT_URI = USER_SHARING_EVENT_CHANNEL + "/event-type/userUnshared";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String PASSWORD = "TestPassword@123";
    private static final String USERNAME = TEST_NAME + "-user";
    private static final String EMAIL = USERNAME + "@test.com";
    private static final String ROLE_NAME = TEST_NAME + "-org-role";
    private static final String ORGANIZATION_AUDIENCE = "organization";
    private static final String ACTION_USER_SHARE = "USER_SHARE";
    private static final String ACTION_USER_UNSHARE = "USER_UNSHARE";
    private static final String ACTION_ORGANIZATION_CREATE = "ORGANIZATION_CREATE";
    private static final String ACTION_USER_ACCOUNT_DELETE = "USER_ACCOUNT_DELETE";
    private static final String INITIATOR_TYPE_ADMIN = "ADMIN";
    private static final String INITIATOR_TYPE_APPLICATION = "APPLICATION";
    private static final long SHARING_TIMEOUT_IN_SECONDS = 60;
    private static final long SHARING_POLL_INTERVAL_IN_SECONDS = 2;

    private final TestUserMode userMode;
    private final String subOrg1Name;
    private final String subOrg2Name;
    private final String childOrgName;

    private WebhookEventTestManager webhookEventTestManager;
    private OrgMgtRestClient orgMgtRestClient;
    private SCIM2RestClient scim2RestClient;
    private UserSharingV2RestClient userSharingRestClient;
    private String rootTenantDomain;
    private String rootOrgName;
    private String userId;
    private String roleId;
    private Org subOrg1;
    private Org subOrg2;
    private Org childOrg;

    @Factory(dataProvider = "testExecutionContextProvider")
    public UserSharingEventTestCase(TestUserMode userMode, String orgNamePrefix) {

        this.userMode = userMode;
        this.subOrg1Name = orgNamePrefix + "_1";
        this.subOrg2Name = orgNamePrefix + "_2";
        this.childOrgName = orgNamePrefix + "_2_child";
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "webhook_user_sharing_org"},
                {TestUserMode.TENANT_ADMIN, "webhook_user_sharing_t_org"}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);
        AutomationContext automationContext = new AutomationContext("IDENTITY", userMode);
        rootTenantDomain = tenantInfo.getDomain();
        rootOrgName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(rootTenantDomain) ? "Super" :
                rootTenantDomain;

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userSharingRestClient = new UserSharingV2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES,
                        OAuth2ServiceAbstractIntegrationTest.class)));

        subOrg1 = createSubOrg(subOrg1Name);
        subOrg2 = createSubOrg(subOrg2Name);
        userId = scim2RestClient.createUser(new UserObject()
                .userName(USERNAME)
                .password(PASSWORD)
                .addEmail(new Email().value(EMAIL)));
        assertNotNull(userId, "Failed to create user: " + USERNAME);
        roleId = scim2RestClient.addV2Role(new RoleV2(null, ROLE_NAME, Collections.emptyList(),
                Collections.emptyList()));
        assertNotNull(roleId, "Failed to create role: " + ROLE_NAME);

        // The webhook is created through the v1 API, so it receives only the events of the root organization.
        webhookEventTestManager = new WebhookEventTestManager("/" + TEST_NAME + "/webhook", "WSO2",
                Collections.singletonList(USER_SHARING_EVENT_CHANNEL), TEST_NAME, automationContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (webhookEventTestManager != null) {
            webhookEventTestManager.teardown();
        }
        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
        if (roleId != null) {
            scim2RestClient.deleteV2Role(roleId);
        }
        if (childOrg != null) {
            orgMgtRestClient.deleteSubOrganization(childOrg.id, subOrg2.id);
        }
        for (Org org : new Org[]{subOrg1, subOrg2}) {
            if (org != null) {
                orgMgtRestClient.deleteOrganization(org.id);
            }
        }
        orgMgtRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        userSharingRestClient.closeHttpClient();
        restClient.closeHttpClient();
    }

    @Test(description = "Administrator shares the user with a sub organization, assigning a role.")
    public void testShareUserWithRoles() throws Exception {

        RoleWithAudience role = new RoleWithAudience()
                .displayName(ROLE_NAME)
                .audience(new RoleWithAudienceAudience().display(rootOrgName).type(ORGANIZATION_AUDIENCE));
        userSharingRestClient.shareUsers(new UserShareRequestBody()
                .userCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(userId))
                .addOrganizationsItem(new SelectiveShareOrgDetails()
                        .orgId(subOrg1.id)
                        .policy(SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY)
                        .roleAssignment(new RoleAssignments()
                                .mode(RoleAssignments.ModeEnum.SELECTED)
                                .addRolesItem(role))));
        subOrg1.sharedUserId = waitForSharedUser(subOrg1);

        webhookEventTestManager.stackExpectedEventPayload(USER_SHARED_EVENT_URI,
                buildExpectedUserSharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, subOrg1.sharedUserId,
                        subOrg1.toOrganizationObject(), new JSONArray().put(createRoleObject(roleId, ROLE_NAME)),
                        ACTION_USER_SHARE, INITIATOR_TYPE_ADMIN));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testShareUserWithRoles",
            description = "Sharing the user again with the same sub organization does not publish an event.")
    public void testReshareUserPublishesNoEvent() throws Exception {

        shareUser(subOrg1, SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY);

        webhookEventTestManager.validateNoEventsReceived();
    }

    @Test(dependsOnMethods = "testReshareUserPublishesNoEvent",
            description = "Administrator unshares the user from a sub organization.")
    public void testUnshareUser() throws Exception {

        userSharingRestClient.unshareUsers(new UserUnshareRequestBody()
                .userCriteria(new UserUnshareRequestBodyUserCriteria().addUserIdsItem(userId))
                .addOrgIdsItem(subOrg1.id));
        waitForUnsharedUser(subOrg1);

        webhookEventTestManager.stackExpectedEventPayload(USER_UNSHARED_EVENT_URI,
                buildExpectedUserUnsharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, subOrg1.sharedUserId,
                        subOrg1.toOrganizationObject(), ACTION_USER_UNSHARE));
        webhookEventTestManager.validateStackedEventPayloads();
        subOrg1.sharedUserId = null;
    }

    @Test(dependsOnMethods = "testUnshareUser",
            description = "Administrator shares the user with two sub organizations in one request.")
    public void testShareUserWithMultipleOrganizations() throws Exception {

        userSharingRestClient.shareUsers(new UserShareRequestBody()
                .userCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(userId))
                .addOrganizationsItem(selectiveShareOrgDetails(subOrg1,
                        SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY))
                .addOrganizationsItem(selectiveShareOrgDetails(subOrg2,
                        SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN)));
        subOrg1.sharedUserId = waitForSharedUser(subOrg1);
        subOrg2.sharedUserId = waitForSharedUser(subOrg2);

        for (Org org : new Org[]{subOrg1, subOrg2}) {
            webhookEventTestManager.stackExpectedEventPayload(USER_SHARED_EVENT_URI,
                    buildExpectedUserSharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, org.sharedUserId,
                            org.toOrganizationObject(), null, ACTION_USER_SHARE, INITIATOR_TYPE_ADMIN));
        }
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testShareUserWithMultipleOrganizations",
            description = "Creating a child organization auto shares the user shared with its future children.")
    public void testAutoShareUserOnOrganizationCreation() throws Exception {

        childOrg = new Org();
        childOrg.name = childOrgName;
        // The handle of a child organization is generated by the server.
        childOrg.handle = "dummy-org-handle";
        childOrg.depth = 2;
        childOrg.id = orgMgtRestClient.addSubOrganization(childOrgName, subOrg2.id);
        assertNotNull(childOrg.id, "Failed to create child organization: " + childOrgName);
        childOrg.sharedUserId = waitForSharedUser(childOrg);

        // The organization is created with an M2M token, hence the initiator is an application.
        webhookEventTestManager.stackExpectedEventPayload(USER_SHARED_EVENT_URI,
                buildExpectedUserSharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, childOrg.sharedUserId,
                        childOrg.toOrganizationObject(), null, ACTION_ORGANIZATION_CREATE,
                        INITIATOR_TYPE_APPLICATION));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAutoShareUserOnOrganizationCreation",
            description = "Administrator unshares the user from all the organizations it is shared with.")
    public void testUnshareUserFromAllOrganizations() throws Exception {

        userSharingRestClient.unshareUsersWithAll(new UserUnshareWithAllRequestBody()
                .userCriteria(new UserUnshareRequestBodyUserCriteria().addUserIdsItem(userId)));
        for (Org org : new Org[]{subOrg1, subOrg2, childOrg}) {
            waitForUnsharedUser(org);
        }

        for (Org org : new Org[]{subOrg1, subOrg2, childOrg}) {
            webhookEventTestManager.stackExpectedEventPayload(USER_UNSHARED_EVENT_URI,
                    buildExpectedUserUnsharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, org.sharedUserId,
                            org.toOrganizationObject(), ACTION_USER_UNSHARE));
            org.sharedUserId = null;
        }
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testUnshareUserFromAllOrganizations",
            description = "Deleting a shared user unshares it from the organizations it is shared with.")
    public void testUnshareUserOnUserDeletion() throws Exception {

        shareUser(subOrg1, SelectiveShareOrgDetails.PolicyEnum.SELECTED_ORG_ONLY);
        subOrg1.sharedUserId = waitForSharedUser(subOrg1);
        webhookEventTestManager.stackExpectedEventPayload(USER_SHARED_EVENT_URI,
                buildExpectedUserSharedEventPayload(userId, USERNAME, EMAIL, rootTenantDomain, subOrg1.sharedUserId,
                        subOrg1.toOrganizationObject(), null, ACTION_USER_SHARE, INITIATOR_TYPE_ADMIN));
        webhookEventTestManager.validateStackedEventPayloads();

        scim2RestClient.deleteUser(userId);
        String deletedUserId = userId;
        // The user is deleted, hence it is not deleted again when cleaning up the test data.
        userId = null;

        webhookEventTestManager.stackExpectedEventPayload(USER_UNSHARED_EVENT_URI,
                buildExpectedUserUnsharedEventPayload(deletedUserId, USERNAME, EMAIL, rootTenantDomain,
                        subOrg1.sharedUserId, subOrg1.toOrganizationObject(), ACTION_USER_ACCOUNT_DELETE));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    private Org createSubOrg(String orgName) throws Exception {

        Org org = new Org();
        org.name = orgName;
        org.handle = orgName;
        org.depth = 1;
        org.id = orgMgtRestClient.addOrganization(orgName, orgName);
        assertNotNull(org.id, "Failed to create sub organization: " + orgName);
        return org;
    }

    private void shareUser(Org org, SelectiveShareOrgDetails.PolicyEnum policy) throws Exception {

        userSharingRestClient.shareUsers(new UserShareRequestBody()
                .userCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(userId))
                .addOrganizationsItem(selectiveShareOrgDetails(org, policy)));
    }

    private SelectiveShareOrgDetails selectiveShareOrgDetails(Org org, SelectiveShareOrgDetails.PolicyEnum policy) {

        return new SelectiveShareOrgDetails()
                .orgId(org.id)
                .policy(policy)
                .roleAssignment(new RoleAssignments().mode(RoleAssignments.ModeEnum.NONE).roles(new ArrayList<>()));
    }

    /**
     * Waits until the user is shared with the organization, since user sharing is asynchronous.
     *
     * @return ID of the shared user in the organization.
     */
    private String waitForSharedUser(Org org) {

        String[] sharedUserId = new String[1];
        await().atMost(SHARING_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollInterval(SHARING_POLL_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> (sharedUserId[0] = getSharedUserId(org.id)) != null);
        return sharedUserId[0];
    }

    private void waitForUnsharedUser(Org org) {

        await().atMost(SHARING_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .pollInterval(SHARING_POLL_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> getSharedUserId(org.id) == null);
    }

    private String getSharedUserId(String orgId) throws Exception {

        JSONArray organizations = userSharingRestClient.getSharedOrganizations(userId)
                .optJSONArray("organizations");
        if (organizations == null) {
            return null;
        }
        for (int i = 0; i < organizations.length(); i++) {
            JSONObject organization = organizations.getJSONObject(i);
            if (orgId.equals(organization.optString("orgId"))) {
                return organization.optString("sharedUserId", null);
            }
        }
        return null;
    }

    /**
     * An organization the user is shared with, and the ID of the shared user in it.
     */
    private static class Org {

        String id;
        String name;
        String handle;
        int depth;
        String sharedUserId;

        JSONObject toOrganizationObject() throws Exception {

            return createOrganizationObject(id, name, handle, depth);
        }
    }
}
