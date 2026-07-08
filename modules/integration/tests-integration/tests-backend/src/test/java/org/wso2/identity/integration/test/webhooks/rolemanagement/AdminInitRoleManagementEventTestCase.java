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

package org.wso2.identity.integration.test.webhooks.rolemanagement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.webhooks.rolemanagement.eventpayloadbuilder.AdminInitRoleManagementEventTestExpectedEventPayloadBuilder;
import org.wso2.identity.integration.test.webhooks.util.WebhookEventTestManager;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * This class tests the role management events triggered by the admin user via the SCIM2 v2 Roles API.
 */
public class AdminInitRoleManagementEventTestCase extends ISIntegrationTest {

    private static final String ROLE_EVENT_CHANNEL = "https://schemas.identity.wso2.org/events/role";
    private static final String ROLE_CREATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleCreated";
    private static final String ROLE_PERMISSIONS_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/rolePermissionsUpdated";
    private static final String ROLE_GROUPS_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleGroupsUpdated";
    private static final String ROLE_IDP_GROUPS_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleIdpGroupsUpdated";
    private static final String ROLE_USERS_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleUsersUpdated";
    private static final String ROLE_META_UPDATED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleMetaUpdated";
    private static final String ROLE_DELETED_EVENT_URI =
            "https://schemas.identity.wso2.org/events/role/event-type/roleDeleted";

    private static final String APP_NAME = "webhook-test-role-app";
    private static final String SCIM2_USERS_API_IDENTIFIER = "/scim2/Users";
    private static final String RBAC_POLICY = "RBAC";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";
    private static final String ROLE_NAME = "webhook-test-role";
    private static final String UPDATED_ROLE_NAME = "webhook-test-role-updated";
    private static final String INITIAL_PERMISSION = "internal_user_mgt_view";
    private static final String ADDED_PERMISSION = "internal_user_mgt_create";
    private static final String USERNAME_1 = "webhookroleuser1";
    private static final String USERNAME_2 = "webhookroleuser2";
    private static final String USER_PASSWORD = "TestPassword@123";
    private static final String AGENT_DISPLAY_NAME = "webhook-test-agent";
    private static final String LOCAL_GROUP_1 = "webhookrolegroup1";
    private static final String LOCAL_GROUP_2 = "webhookrolegroup2";
    private static final String IDP_NAME = "webhook-test-idp";
    private static final String IDP_GROUP_NAME = "webhook-test-idp-group";
    private static final String OIDC_AUTHENTICATOR_ID = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    private static final String OIDC_AUTHENTICATOR_NAME = "OpenIDConnectAuthenticator";
    private static final String PATH_GROUPS = "groups";
    private static final String PATH_USERS = "users";
    private static final String PATH_PERMISSIONS = "permissions";
    private static final String PATH_DISPLAY_NAME = "displayName";

    private final AutomationContext automationContext;
    private final TestUserMode userMode;
    private String tenantDomain;

    private WebhookEventTestManager webhookEventTestManager;
    private SCIM2RestClient scim2RestClient;
    private OAuth2RestClient oAuth2RestClient;
    private IdpMgtRestClient idpMgtRestClient;

    private String appId;
    private String roleId;
    private String user1Id;
    private String user2Id;
    private String agentId;
    private String group1Id;
    private String group2Id;
    private String idpId;
    private String idpGroupId;

    @Factory(dataProvider = "testExecutionContextProvider")
    public AdminInitRoleManagementEventTestCase(TestUserMode userMode) throws Exception {

        this.userMode = userMode;
        automationContext = new AutomationContext("IDENTITY", userMode);
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        tenantDomain = automationContext.getContextTenant().getDomain();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);

        appId = createApplication();
        authorizeUserManagementAPI(appId);

        user1Id = createRoleMemberUser(USERNAME_1);
        user2Id = createRoleMemberUser(USERNAME_2);
        agentId = scim2RestClient.createAgent(AGENT_DISPLAY_NAME, user1Id);
        group1Id = scim2RestClient.createGroup(new GroupRequestObject().displayName(LOCAL_GROUP_1));
        group2Id = scim2RestClient.createGroup(new GroupRequestObject().displayName(LOCAL_GROUP_2));
        idpId = createStandardsBasedIdP();
        idpGroupId = addIdPGroup();

        webhookEventTestManager = new WebhookEventTestManager("/scim2/webhook", "WSO2",
                Collections.singletonList(ROLE_EVENT_CHANNEL),
                "AdminInitRoleManagementEventTestCase",
                automationContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (roleId != null) {
            scim2RestClient.attemptRoleV2Delete(roleId);
        }
        if (agentId != null) {
            scim2RestClient.deleteAgent(agentId);
        }
        if (user1Id != null) {
            scim2RestClient.deleteUser(user1Id);
        }
        if (user2Id != null) {
            scim2RestClient.deleteUser(user2Id);
        }
        if (group1Id != null) {
            scim2RestClient.deleteGroup(group1Id);
        }
        if (group2Id != null) {
            scim2RestClient.deleteGroup(group2Id);
        }
        if (idpId != null) {
            idpMgtRestClient.deleteIdp(idpId);
        }
        if (appId != null) {
            oAuth2RestClient.deleteApplication(appId);
        }

        scim2RestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        idpMgtRestClient.closeHttpClient();
        webhookEventTestManager.teardown();
    }

    @Test
    public void testCreateRole() throws Exception {

        Audience audience = new Audience(APPLICATION_AUDIENCE, appId);
        RoleV2 role = new RoleV2(audience, ROLE_NAME,
                Collections.singletonList(new Permission(INITIAL_PERMISSION)), Collections.emptyList());
        roleId = scim2RestClient.addV2Role(role);
        assertNotNull(roleId);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_CREATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleCreatedEventPayload(
                        tenantDomain, appId, APP_NAME, ROLE_NAME, INITIAL_PERMISSION));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testCreateRole")
    public void testUpdateRolePermissions() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_PERMISSIONS,
                        new ListObject().value(ADDED_PERMISSION)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_PERMISSIONS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedRolePermissionsUpdatedEventPayload(tenantDomain, appId, APP_NAME, ROLE_NAME,
                                ADDED_PERMISSION));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testUpdateRolePermissions")
    public void testAddGroupToRole() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_GROUPS,
                        new ListObject().value(group1Id)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_GROUPS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleGroupsAddedEventPayload(
                        tenantDomain, appId, APP_NAME, ROLE_NAME, group1Id, LOCAL_GROUP_1));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAddGroupToRole")
    public void testUpdateRoleGroups() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_GROUPS,
                        new ListObject().value(group2Id)))
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.REMOVE, PATH_GROUPS,
                        new ListObject().value(group1Id)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_GROUPS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleGroupsAddedEventPayload(
                        tenantDomain, appId, APP_NAME, ROLE_NAME, group2Id, LOCAL_GROUP_2));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testUpdateRoleGroups")
    public void testAddIdpGroupToRole() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_GROUPS,
                        new ListObject().value(idpGroupId)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_IDP_GROUPS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedRoleIdpGroupsUpdatedEventPayload(tenantDomain, appId, APP_NAME, ROLE_NAME,
                                idpGroupId, IDP_GROUP_NAME, idpId, IDP_NAME));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAddIdpGroupToRole")
    public void testAddUserToRole() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_USERS,
                        new ListObject().value(user1Id)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_USERS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleUsersAddedEventPayload(
                        tenantDomain, appId, APP_NAME, ROLE_NAME, user1Id, USERNAME_1));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAddUserToRole")
    public void testAddSecondUserAndAgentToRole() throws Exception {

        PatchOperationRequestObject patchRequest = new PatchOperationRequestObject()
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_USERS,
                        new ListObject().value(user2Id)))
                .addOperations(buildListValueOperation(RoleItemAddGroupobj.OpEnum.ADD, PATH_USERS,
                        new ListObject().value(agentId)));
        scim2RestClient.updateUsersOfRoleV2(roleId, patchRequest);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_USERS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder
                        .buildExpectedRoleUsersAndAgentAddedEventPayload(tenantDomain, appId, APP_NAME, ROLE_NAME,
                                user2Id, USERNAME_2, agentId, AGENT_DISPLAY_NAME));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testAddSecondUserAndAgentToRole")
    public void testRemoveUsersFromRole() throws Exception {

        // A SCIM PATCH remove on the "users" path removes all current members of the role, so every member
        // (both regular users and the agent) is reported under removedUsers.
        RoleItemAddGroupobj removeUsersOperation = new RoleItemAddGroupobj();
        removeUsersOperation.setOp(RoleItemAddGroupobj.OpEnum.REMOVE);
        removeUsersOperation.setPath(PATH_USERS);
        scim2RestClient.updateUsersOfRoleV2(roleId,
                new PatchOperationRequestObject().addOperations(removeUsersOperation));

        webhookEventTestManager.stackExpectedEventPayload(ROLE_USERS_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleAllUsersRemovedEventPayload(
                        tenantDomain, appId, APP_NAME, ROLE_NAME, user1Id, USERNAME_1, user2Id, USERNAME_2, agentId,
                        AGENT_DISPLAY_NAME));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testRemoveUsersFromRole")
    public void testUpdateRoleName() throws Exception {

        UserItemAddGroupobj renameOperation = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        renameOperation.setPath(PATH_DISPLAY_NAME);
        renameOperation.setValue(UPDATED_ROLE_NAME);
        scim2RestClient.updateUsersOfRoleV2(roleId, new PatchOperationRequestObject().addOperations(renameOperation));

        webhookEventTestManager.stackExpectedEventPayload(ROLE_META_UPDATED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleMetaUpdatedEventPayload(
                        tenantDomain, appId, APP_NAME, UPDATED_ROLE_NAME));
        webhookEventTestManager.validateStackedEventPayloads();
    }

    @Test(dependsOnMethods = "testUpdateRoleName")
    public void testDeleteRole() throws Exception {

        scim2RestClient.deleteV2Role(roleId);

        webhookEventTestManager.stackExpectedEventPayload(ROLE_DELETED_EVENT_URI,
                AdminInitRoleManagementEventTestExpectedEventPayloadBuilder.buildExpectedRoleDeletedEventPayload(
                        tenantDomain));
        webhookEventTestManager.validateStackedEventPayloads();

        roleId = null;
    }

    private RoleItemAddGroupobj buildListValueOperation(RoleItemAddGroupobj.OpEnum op, String path, ListObject value) {

        RoleItemAddGroupobj operation = new RoleItemAddGroupobj();
        operation.setOp(op);
        operation.setPath(path);
        operation.addValue(value);
        return operation;
    }

    private String createApplication() throws Exception {

        ApplicationModel application = new ApplicationModel()
                .name(APP_NAME)
                .description("Application used as the audience for the role management webhook tests.");
        return oAuth2RestClient.createApplication(application);
    }

    private void authorizeUserManagementAPI(String applicationId) throws Exception {

        List<APIResourceListItem> apiResources =
                oAuth2RestClient.getAPIResourcesWithFiltering("identifier+eq+" + SCIM2_USERS_API_IDENTIFIER);
        if (apiResources == null || apiResources.isEmpty()) {
            return;
        }

        String apiId = apiResources.get(0).getId();
        List<ScopeGetModel> apiResourceScopes = oAuth2RestClient.getAPIResourceScopes(apiId);

        AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(apiId);
        authorizedAPICreationModel.setPolicyIdentifier(RBAC_POLICY);
        apiResourceScopes.forEach(scope -> authorizedAPICreationModel.addScopesItem(scope.getName()));
        oAuth2RestClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
    }

    private String createRoleMemberUser(String username) throws Exception {

        UserObject userInfo = new UserObject()
                .userName(username)
                .password(USER_PASSWORD)
                .name(new Name().givenName(username).familyName(username))
                .addEmail(new Email().value(username + "@test.com"));
        return scim2RestClient.createUser(userInfo);
    }

    private String createStandardsBasedIdP() throws Exception {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(OIDC_AUTHENTICATOR_ID)
                .name(OIDC_AUTHENTICATOR_NAME)
                .isEnabled(true)
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.IDP_NAME).value(IDP_NAME))
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.CLIENT_ID).value("dummyClientId"))
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET)
                        .value("dummyClientSecret"))
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL)
                        .value("https://localhost:9853/oauth2/authorize"))
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL)
                        .value("https://localhost:9853/oauth2/token"))
                .addProperty(new Property().key(IdentityConstants.Authenticator.OIDC.CALLBACK_URL)
                        .value("https://localhost:9853/commonauth"));

        FederatedAuthenticatorRequest federatedAuthenticatorRequest = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(OIDC_AUTHENTICATOR_ID)
                .addAuthenticator(authenticator);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(IDP_NAME)
                .federatedAuthenticators(federatedAuthenticatorRequest);

        return idpMgtRestClient.createIdentityProvider(idpPostRequest);
    }

    private String addIdPGroup() throws Exception {

        JSONArray idpGroups = idpMgtRestClient.addIdPGroups(idpId, IDP_GROUP_NAME);
        JSONObject idpGroup = (JSONObject) idpGroups.get(0);
        return idpGroup.get("id").toString();
    }
}
