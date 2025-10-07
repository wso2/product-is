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

package org.wso2.identity.integration.test.serviceextensions.preupdatepassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.restclients.*;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UsersRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Integration test class for testing the pre update password action execution.
 * This test case extends {@link ActionsBaseTestCase} and focuses on success scenarios related
 * to password update flows.
 */
public class PreUpdatePasswordActionSuccessTestCase extends PreUpdatePasswordActionBaseTestCase {

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private UsersRestClient usersRestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String userId;
    private ApplicationResponseModel application;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdatePasswordActionSuccessTestCase(TestUserMode testUserMode) {

        userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
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

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        usersRestClient = new UsersRestClient(serverURL, tenantInfo);

        application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        UserObject userInfo = new UserObject()
                .userName(TEST_USER1_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        userId = scim2RestClient.createUser(userInfo);

        updatePasswordRecoveryFeatureStatus(true);
        updateSelfRegistrationStatus(true);
        enableAdminPasswordResetRecoveryEmailLink();
        updateAdminInitiatedPasswordResetEmailFeatureStatus(true);

        actionId = createPreUpdatePasswordAction(ACTION_NAME, ACTION_DESCRIPTION);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-update-password-response.json"));
    }

    @BeforeMethod
    public void setUp() throws Exception {

        serviceExtensionMockServer.resetRequests();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        updatePasswordRecoveryFeatureStatus(false);
        updateSelfRegistrationStatus(false);
        updateAdminInitiatedPasswordResetEmailFeatureStatus(false);

        deleteAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        deleteApp(application.getId());
        scim2RestClient.deleteUser(userId);
        restClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        flowExecutionClient.closeHttpClient();
        client.close();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
    }

    @Test(description = "Verify the password update in self service portal with pre update password action")
    public void testUserUpdatePassword() throws Exception {

        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_UPDATED_PASSWORD);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(new UserItemAddGroupobj()
                        .op(UserItemAddGroupobj.OpEnum.REPLACE)
                        .value(passwordValue));
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(patchUserInfo,
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

        assertNotNull(response);
        assertActionRequestPayload(userId, TEST_USER_UPDATED_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testUserUpdatePassword" ,
            description = "Verify the admin update password with pre update password action")
    public void testAdminUpdatePassword() throws Exception {

        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(new UserItemAddGroupobj()
                        .op(UserItemAddGroupobj.OpEnum.REPLACE)
                        .value(passwordValue));
        scim2RestClient.updateUser(patchUserInfo, userId);

        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testAdminUpdatePassword",
            description = "Verify the admin force password reset with pre update password action")
    public void testAdminForcePasswordReset() throws Exception {

        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(new UserItemAddGroupobj()
                        .op(UserItemAddGroupobj.OpEnum.REPLACE)
                        .path(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + FORCE_PASSWORD_RESET_ATTRIBUTE)
                        .value(true));
        scim2RestClient.updateUser(patchUserInfo, userId);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));

        assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.RESET);
    }

    @Test(dependsOnMethods = "testAdminForcePasswordReset",
            description = "Verify the admin invite user to set password with pre update password action")
    public void testAdminInviteUserToSetPassword() throws Exception {

        UserObject adminInvitedUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL))
                .scimSchemaExtensionSystem(new ScimSchemaExtensionSystem().askPassword(true));
        String adminInvitedUserId = scim2RestClient.createUser(adminInvitedUserInfo);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Set Successfully"));

        assertActionRequestPayload(adminInvitedUserId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(adminInvitedUserId);
    }

    @Test(dependsOnMethods = "testAdminInviteUserToSetPassword",
            description = "Verify the password update by an authorized application with pre update password action")
    public void testApplicationUpdatePassword() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE).value(passwordValue));
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

//     @Test(dependsOnMethods = "testApplicationUpdatePassword",
//             description = "Verify the user password reset with pre update password action")
//     public void testUserResetPassword() throws Exception {

//         String passwordRecoveryFormURL = retrievePasswordResetURL(application);
//         submitPasswordRecoveryForm(passwordRecoveryFormURL, TEST_USER1_USERNAME);

//         String recoveryLink = getRecoveryURLFromEmail();
//         HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
//         Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
//         Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));

//         assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
//                 PreUpdatePasswordEvent.Action.RESET);
//     }

    @Test(dependsOnMethods = "testApplicationUpdatePassword",
            description = "Verify the user password recovery flow with pre update password action")
    public void testUserResetPasswordWithPasswordRecoveryFlow() throws Exception {

        updateFlowStatus(PASSWORD_RECOVERY_FLOW_TYPE, true);
        addPasswordRecoveryFlow();

        Object executionResponseObj = executePasswordRecoveryFlow();
        assertTrue(executionResponseObj instanceof FlowExecutionResponse,
                "Expected FlowExecutionResponse for success flow.");
        FlowExecutionResponse step3Response = (FlowExecutionResponse) executionResponseObj;
        assertNotNull(step3Response, "Flow execution response cannot be null.");

        assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.RESET);
        updateFlowStatus(PASSWORD_RECOVERY_FLOW_TYPE, false);
    }

    @Test(dependsOnMethods = "testUserResetPasswordWithPasswordRecoveryFlow",
            description = "Verify the user password set with pre update password action via offline invite link")
    public void testUserSetPasswordViaOfflineInviteLink() throws Exception {

        UserObject offlineInvitingUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        String offlineInvitingUserId = scim2RestClient.createUser(offlineInvitingUserInfo);
        // Password action is triggered in user creation in offline link flow due architectural gap.
        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.REGISTER);
        serviceExtensionMockServer.resetRequests();

        InvitationRequest invitationRequest = new InvitationRequest()
                .username(offlineInvitingUserInfo.getUserName())
                .userstore(PRIMARY_USER_STORE_NAME);
        String inviteLink = usersRestClient.generateOfflineInviteLink(invitationRequest);

        HttpResponse postResponse = resetPassword(inviteLink, RESET_PASSWORD);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));

        assertActionRequestPayload(offlineInvitingUserId, RESET_PASSWORD,
                PreUpdatePasswordEvent.FlowInitiatorType.ADMIN, PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(offlineInvitingUserId);
    }

    @Test(dependsOnMethods = "testUserSetPasswordViaOfflineInviteLink",
            description = "Verify admin invited user registration flow with pre update password action")
    public void testAdminInvitedUserRegistrationFlow() throws Exception {

        updateFlowStatus(INVITED_USER_REGISTRATION_FLOW_TYPE, true);
        addInvitedUserRegistrationFlow();

        UserObject adminInvitedUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL))
                .scimSchemaExtensionSystem(new ScimSchemaExtensionSystem().askPassword(true));
        String adminInvitedUserId = scim2RestClient.createUser(adminInvitedUserInfo);

        Object executionResponseObj = executeAdminInvitedUserRegistrationFlow();
        assertTrue(executionResponseObj instanceof FlowExecutionResponse,
                "Expected FlowExecutionResponse for success flow.");
        FlowExecutionResponse executionResponse = (FlowExecutionResponse) executionResponseObj;
        assertNotNull(executionResponse, "Flow execution response cannot be null.");

        assertActionRequestPayload(adminInvitedUserId, RESET_PASSWORD,
                PreUpdatePasswordEvent.FlowInitiatorType.ADMIN, PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(adminInvitedUserId);
        updateFlowStatus(INVITED_USER_REGISTRATION_FLOW_TYPE, false);
    }

    @Test(dependsOnMethods = "testAdminInvitedUserRegistrationFlow",
            description = "Verify the admin initiated user registration with pre update password action")
    public void testAdminInitiatedUserRegistration() throws Exception {

        UserObject adminRegisteredUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        String adminRegisteredUserId = scim2RestClient.createUser(adminRegisteredUserInfo);
        assertNotNull(adminRegisteredUserId);

        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.REGISTER);
        scim2RestClient.deleteUser(adminRegisteredUserId);
    }

    @Test(dependsOnMethods = "testAdminInitiatedUserRegistration",
            description = "Verify the application initiated user registration with pre update password action")
    public void testApplicationInitiatedUserRegistration() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret);
        UserObject appRegisteredUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        org.json.simple.JSONObject response = scim2RestClient.createUser(appRegisteredUserInfo, token);
        assertNotNull(response);

        int statusCode = Integer.parseInt(response.get("statusCode").toString());
        assertEquals(statusCode, HttpServletResponse.SC_CREATED,
                "Expected user creation status code 201 but got " + statusCode);

        assertActionRequestPayload(null, TEST_USER_PASSWORD,
                PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION, PreUpdatePasswordEvent.Action.REGISTER);
        org.json.simple.JSONObject responseBody = (org.json.simple.JSONObject) response.get("body");
        scim2RestClient.deleteUser(responseBody.get("id").toString());
    }

    @Test(dependsOnMethods = "testApplicationInitiatedUserRegistration",
            description = "Verify the user initiated self registration flow with pre update password action")
    public void testUserRegistrationWithSelfRegistrationFlow() throws Exception {

        updateFlowStatus(REGISTRATION_FLOW_TYPE, true);
        addRegistrationFlow();
        flowExecutionClient.initiateFlowExecution(REGISTRATION_FLOW_TYPE);
        FlowExecutionRequest flowExecutionRequest = buildUserRegistrationFlowRequest();
        Object executionResponseObj = flowExecutionClient.executeFlow(flowExecutionRequest);
        assertTrue(executionResponseObj instanceof FlowExecutionResponse,
                "Unexpected response type for flow execution.");

        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.REGISTER);
        org.json.simple.JSONObject response = scim2RestClient.filterUsers("userName+eq+" + TEST_USER2_USERNAME);
        JSONArray resources = (JSONArray) response.get("Resources");
        String userId = ((org.json.simple.JSONObject) resources.get(0)).get("id").toString();
        scim2RestClient.deleteUser(userId);
        updateFlowStatus(REGISTRATION_FLOW_TYPE, false);
    }

    @Test(dependsOnMethods = "testUserRegistrationWithSelfRegistrationFlow",
            description = "Verify the user initiated self registration with pre update password action")
    public void testUserInitiatedSelfRegistration() throws Exception {

        String userRegistrationFormURL = retrieveUserRegistrationURL(application);
        submitUserRegistrationForm(userRegistrationFormURL, TEST_USER2_USERNAME, TEST_USER_PASSWORD);

        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.REGISTER);
        org.json.simple.JSONObject response = scim2RestClient.filterUsers("userName+eq+" + TEST_USER2_USERNAME);
        JSONArray resources = (JSONArray) response.get("Resources");
        String userId = ((org.json.simple.JSONObject) resources.get(0)).get("id").toString();
        scim2RestClient.deleteUser(userId);
    }

    private void assertActionRequestPayload(String userId, String updatedPassword,
                                            PreUpdatePasswordEvent.FlowInitiatorType initiatorType,
                                            PreUpdatePasswordEvent.Action action) throws JsonProcessingException {

        String actualRequestPayload = serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actionRequest = new ObjectMapper()
                .readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);

        assertEquals(actionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertEquals(actionRequest.getEvent().getTenant().getName(), tenantInfo.getDomain());
        assertEquals(actionRequest.getEvent().getTenant().getId(), tenantId);
        assertEquals(actionRequest.getEvent().getUserStore().getName(), PRIMARY_USER_STORE_NAME);
        assertEquals(actionRequest.getEvent().getUserStore().getId(), PRIMARY_USER_STORE_ID);

        PasswordUpdatingUser user = actionRequest.getEvent().getPasswordUpdatingUser();

        assertEquals(user.getId(), userId);
        assertEquals(user.getUpdatingCredential().getType(), Credential.Type.PASSWORD);
        assertEquals(user.getUpdatingCredential().getFormat(), Credential.Format.PLAIN_TEXT);
        assertEquals(user.getUpdatingCredential().getValue(), updatedPassword.toCharArray());
        assertEquals(actionRequest.getEvent().getInitiatorType(), initiatorType);
        assertEquals(actionRequest.getEvent().getAction(), action);
    }

    private void assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType initiatorType,
                                                            PreUpdatePasswordEvent.Action action)
            throws JsonProcessingException {

        String actualRequestPayload = serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actionRequest = new ObjectMapper()
                .readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);

        assertEquals(actionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertEquals(actionRequest.getEvent().getTenant().getName(), tenantInfo.getDomain());
        assertEquals(actionRequest.getEvent().getTenant().getId(), tenantId);
        assertEquals(actionRequest.getEvent().getUserStore().getName(), PRIMARY_USER_STORE_NAME);
        assertEquals(actionRequest.getEvent().getUserStore().getId(), PRIMARY_USER_STORE_ID);

        PasswordUpdatingUser user = actionRequest.getEvent().getPasswordUpdatingUser();

        assertNull(user.getId());
        assertEquals(user.getUpdatingCredential().getType(), Credential.Type.PASSWORD);
        assertEquals(user.getUpdatingCredential().getFormat(), Credential.Format.PLAIN_TEXT);
        assertNotNull(user.getUpdatingCredential().getValue());
        assertEquals(actionRequest.getEvent().getInitiatorType(), initiatorType);
        assertEquals(actionRequest.getEvent().getAction(), action);
    }
}
