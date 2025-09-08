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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowConfig;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.restclients.*;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ExpectedPasswordUpdateResponse;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UsersRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test class for testing the pre update password action execution.
 * This test case extends {@link ActionsBaseTestCase} and focuses on failed and error scenarios related
 * to password update flows.
 */
public class PreUpdatePasswordActionFailureTestCase extends PreUpdatePasswordActionBaseTestCase {

    private static final String PASSWORD_RESET_ERROR = "The server encountered an internal error.";

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;
    private UsersRestClient usersRestClient;
    private FlowExecutionClient flowExecutionClient;
    private FlowManagementClient flowManagementClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String userId;
    private ApplicationResponseModel application;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedPasswordUpdateResponse expectedPasswordUpdateResponse;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdatePasswordActionFailureTestCase(TestUserMode testUserMode, ActionResponse actionResponse,
                                                  ExpectedPasswordUpdateResponse expectedPasswordUpdateResponse) {

        this.userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
        this.actionResponse = actionResponse;
        this.expectedPasswordUpdateResponse = expectedPasswordUpdateResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws IOException, URISyntaxException {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(HttpServletResponse.SC_OK,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_BAD_REQUEST,
                                "Some failure reason",
                                "Some description")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Compromised password",
                                "Error while updating attributes of user")},
                {TestUserMode.TENANT_USER, new ActionResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedPasswordUpdateResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Compromised password",
                                "Error while updating attributes of user")},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        usersRestClient = new UsersRestClient(serverURL, tenantInfo);
        flowExecutionClient = new FlowExecutionClient(serverURL, tenantInfo);
        flowManagementClient = new FlowManagementClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

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
        enableAdminPasswordResetRecoveryEmailLink();
        updateAdminInitiatedPasswordResetEmailFeatureStatus(true);

        actionId = createPreUpdatePasswordAction(ACTION_NAME, ACTION_DESCRIPTION);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
        enableFlow(flowManagementClient);
        addRegistrationFlow(flowManagementClient);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        serviceExtensionMockServer.resetRequests();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        updatePasswordRecoveryFeatureStatus(false);
        updateAdminInitiatedPasswordResetEmailFeatureStatus(false);

        deleteAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        deleteApp(application.getId());
        scim2RestClient.deleteUser(userId);
        restClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        serviceExtensionMockServer.stopServer();
        serviceExtensionMockServer = null;
        disableFlow(flowManagementClient);
    }
    protected void enableFlow(FlowManagementClient client) throws Exception {

        FlowConfig flowConfigDTO = new FlowConfig();
        flowConfigDTO.setIsEnabled(true);
        flowConfigDTO.setFlowType(PreUpdatePasswordActionSuccessTestCase.FlowTypes.REGISTRATION);
        client.updateFlowConfig(flowConfigDTO);
    }

    protected void disableFlow(FlowManagementClient client) throws Exception {

        FlowConfig flowConfigDTO = new FlowConfig();
        flowConfigDTO.setIsEnabled(false);
        flowConfigDTO.setFlowType(FlowTypes.REGISTRATION);
        client.updateFlowConfig(flowConfigDTO);
    }

    protected void addRegistrationFlow(FlowManagementClient client) throws Exception {
        String registrationFlowRequestJson = readResource(REGISTRATION_FLOW);
        FlowRequest flowRequest = new ObjectMapper()
                .readValue(registrationFlowRequestJson, FlowRequest.class);
        client.putFlow(flowRequest);
    }

    protected String readResource(String filename) throws IOException {

        return readResource(filename, this.getClass());
    }

    public static String readResource(String filename, Class cClass) throws IOException {

        try (InputStream resourceAsStream = cClass.getResourceAsStream(filename);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }

            return resourceFile.toString();
        }
    }

    protected static class FlowTypes {

        public static final String REGISTRATION = "REGISTRATION";
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
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedPasswordUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
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
        org.json.simple.JSONObject response = scim2RestClient.updateUserAndReturnResponse(patchUserInfo, userId);

        assertNotNull(response);
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedPasswordUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

//     @Test(dependsOnMethods = "testAdminUpdatePassword",
//             description = "Verify the user password reset with pre update password action")
//     public void testUserResetPassword() throws Exception {

//         String passwordRecoveryFormURL = retrievePasswordResetURL(application);
//         submitPasswordRecoveryForm(passwordRecoveryFormURL, TEST_USER1_USERNAME);

//         String recoveryLink = getRecoveryURLFromEmail();
//         HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
//         Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
//         assertErrors(postResponse);

//         assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
//                 PreUpdatePasswordEvent.Action.RESET);
//     }

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
        assertErrors(postResponse);

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
        assertErrors(postResponse);

        assertActionRequestPayload(adminInvitedUserId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(adminInvitedUserId);
    }

    @Test(dependsOnMethods = "testAdminInviteUserToSetPassword",
            description = "Verify the password update by an authorized application with pre update password action")
    public void testApplicationUpdatePassword() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret,
                "internal_user_mgt_update");
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        PatchOperationRequestObject patchUserInfo = new PatchOperationRequestObject()
                .addOperations(new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE).value(passwordValue));
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(patchUserInfo, userId, token);

        assertNotNull(response);
        String status = response.get("status").toString();
        assertEquals(status, String.valueOf(expectedPasswordUpdateResponse.getStatusCode()));
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testApplicationUpdatePassword",
            description = "Verify the user password set with pre update password action via offline invite link")
    public void testUserSetPasswordViaOfflineInviteLink() throws Exception {

        // Deactivating action to stop action execution at user creation, which is not required for this test.
        deactivateAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        UserObject offlineInvitingUserInfo = new UserObject()
                .userName(TEST_USER2_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        String offlineInvitingUserId = scim2RestClient.createUser(offlineInvitingUserInfo);

        activateAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        InvitationRequest invitationRequest = new InvitationRequest()
                .username(offlineInvitingUserInfo.getUserName())
                .userstore(PRIMARY_USER_STORE_NAME);
        String inviteLink = usersRestClient.generateOfflineInviteLink(invitationRequest);

        HttpResponse postResponse = resetPassword(inviteLink, RESET_PASSWORD);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
        assertErrors(postResponse);

        assertActionRequestPayload(offlineInvitingUserId, RESET_PASSWORD,
                PreUpdatePasswordEvent.FlowInitiatorType.ADMIN, PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(offlineInvitingUserId);
    }

    @Test(dependsOnMethods = "testUserSetPasswordViaOfflineInviteLink",
            description = "Verify the admin initiated user registration with pre update password action failure")
    public void testAdminInitiatedUserRegistration() throws Exception {

        UserObject adminRegisteredUserInfo = new UserObject()
                .userName(TEST_USER3_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        org.json.simple.JSONObject response = scim2RestClient.createUserAndReturnResponse(adminRegisteredUserInfo);

        assertNotNull(response, "User creation response is null");
        if (response.containsKey("error")) {
            Assert.fail("User creation failed: " + response.get("error"));
        }
        Object statusObj = response.get("status");
        assertNotNull(statusObj, "Status field is missing in response. Raw response: " + response.toJSONString());
        String status = statusObj.toString();
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }
        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.REGISTER);
    }

    @Test(dependsOnMethods = "testAdminInitiatedUserRegistration",
            description = "Verify the application initiated user registration with pre update password action failure")
    public void testApplicationInitiatedUserRegistration() throws Exception {

        String token = getTokenWithClientCredentialsGrant(application.getId(), clientId, clientSecret,
                "internal_user_mgt_create");

        UserObject appRegisteredUserInfo = new UserObject()
                .userName(TEST_USER4_USERNAME)
                .password(TEST_USER_PASSWORD)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LASTNAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        org.json.simple.JSONObject response = scim2RestClient.createUserWithBearerTokenAndReturnResponse(appRegisteredUserInfo, token);
        assertNotNull(response, "User creation response is null");
        if (response.containsKey("error")) {
            Assert.fail("User creation failed: " + response.get("error"));
        }
        Object statusObj = response.get("status");
        assertNotNull(statusObj, "Status field is missing in response. Raw response: " + response.toJSONString());
        String status = statusObj.toString();
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }

        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION,
                PreUpdatePasswordEvent.Action.REGISTER);
    }

    @Test(dependsOnMethods = "testApplicationInitiatedUserRegistration",
            description = "Verify the user initiated registration with pre update password action failure")
    public void testUserInitiatedUserRegistration() throws Exception {

        Object responseObj = flowExecutionClient.initiateFlowExecution("REGISTRATION");
        assertNotNull(responseObj, "Flow initiation response is null.");
        assertTrue(responseObj instanceof FlowExecutionResponse, "Unexpected response type for flow initiation.");

        FlowExecutionRequest flowExecutionRequest = new FlowExecutionRequest();
        flowExecutionRequest.setFlowType("REGISTRATION");
        flowExecutionRequest.setActionId("button_5zqc");
        Map<String, String> inputs = new HashMap<>();
        inputs.put("http://wso2.org/claims/username", TEST_USER5_USERNAME);
        inputs.put("password", TEST_USER_PASSWORD);
        inputs.put("http://wso2.org/claims/emailaddress", TEST_USER_EMAIL);
        inputs.put("http://wso2.org/claims/givenname", TEST_USER_GIVEN_NAME);
        inputs.put("http://wso2.org/claims/lastname", TEST_USER_LASTNAME);
        flowExecutionRequest.setInputs(inputs);

        org.json.simple.JSONObject response = flowExecutionClient.executeFlowAndReturnResponse(flowExecutionRequest);
        assertNotNull(response, "User creation response is null");
        if (response.containsKey("error")) {
            Assert.fail("User creation failed: " + response.get("error"));
        }
        Object statusObj = response.get("statusCode");
        assertNotNull(statusObj, "StatusCode field is missing in response. Raw response: " +
                response.toJSONString());
        String status = statusObj.toString();
        if (status.equals(String.valueOf(HttpServletResponse.SC_OK))) {
            assertEquals(response.get("scimType"), String.valueOf(expectedPasswordUpdateResponse.getErrorMessage()));
        }

        assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.REGISTER);
    }

    private void assertActionRequestPayloadWithUserCreation(PreUpdatePasswordEvent.FlowInitiatorType initiatorType,
                                                            PreUpdatePasswordEvent.Action action)
            throws JsonProcessingException {

        String actualRequestPayload = serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        PreUpdatePasswordActionRequest actionRequest = objectMapper
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

    private void assertErrors(HttpResponse postResponse) throws IOException {

        String htmlContent = EntityUtils.toString(postResponse.getEntity());
        switch (expectedPasswordUpdateResponse.getStatusCode()) {
            case HttpServletResponse.SC_BAD_REQUEST:
                assertHtmlErrorDescription(htmlContent);
                break;
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                assertTrue(htmlContent.contains(PASSWORD_RESET_ERROR));
                break;
            default:
                Assert.fail("Unexpected response status code: " + postResponse.getStatusLine().getStatusCode());
        }
        EntityUtils.consume(postResponse.getEntity());
    }

    private void assertHtmlErrorDescription(String htmlResponse) {

        Document doc = Jsoup.parse(htmlResponse);
        Elements header = doc.select("[data-testid=error-page-header]");
        String actualText = header.text().trim();

        assertEquals(actualText, expectedPasswordUpdateResponse.getErrorDetail(),
                "Error description text does not match");
    }
}
