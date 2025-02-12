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

package org.wso2.identity.integration.test.actions.preupdatepassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.actions.ActionsBaseTestCase;
import org.wso2.identity.integration.test.actions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.actions.dataprovider.model.ExpectedPasswordUpdateResponse;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.actions.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test class for testing the pre update password action execution.
 * This test case extends {@link ActionsBaseTestCase} and focuses on failed and error scenarios related
 * to password update flows.
 */
public class PreUpdatePasswordActionFailureTestCase extends PreUpdatePasswordActionBaseTestCase {

    private static final String TEST_USER1_USERNAME = "testUsername";
    private static final String TEST_USER2_USERNAME = "testUsername2";
    private static final String TEST_USER_PASSWORD = "TestPassword@123";
    private static final String TEST_USER_UPDATED_PASSWORD = "UpdatedTestPassword@123";
    private static final String RESET_PASSWORD = "ResetTestPassword@123";
    private static final String TEST_USER_GIVEN_NAME = "test_user_given_name";
    private static final String TEST_USER_LASTNAME = "test_user_last_name";
    private static final String TEST_USER_EMAIL = "test.user@gmail.com";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String PRIMARY_USER_STORE_ID = "UFJJTUFSWQ==";
    private static final String PRIMARY_USER_STORE_NAME = "PRIMARY";
    private static final String ACTION_NAME = "Pre Update Password Action";
    private static final String ACTION_DESCRIPTION = "This is a test for pre update password action type";
    private static final String PRE_UPDATE_PASSWORD_API_PATH = "preUpdatePassword";
    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String EMAIL_INVITE_PASSWORD_RESET_ERROR = "The server encountered an internal error.";

    private final String tenantId;
    private final TestUserMode userMode;

    private SCIM2RestClient scim2RestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String userId;
    private ApplicationResponseModel application;
    private ActionsMockServer actionsMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedPasswordUpdateResponse expectedPasswordUpdateResponse;

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE = "urn:scim:wso2:schema";
    private static final String FORCE_PASSWORD_RESET_ATTRIBUTE = "forcePasswordReset";

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
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse("400", "Some failure reason. " +
                                "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedPasswordUpdateResponse("400", "Some failure reason. " +
                                "Some description")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedPasswordUpdateResponse("500", "Error while updating " +
                                "attributes of user")},
                {TestUserMode.TENANT_USER, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedPasswordUpdateResponse("500", "Error while updating " +
                                "attributes of user")},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(userMode);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER1_USERNAME);
        userInfo.setPassword(TEST_USER_PASSWORD);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN_NAME));
        userInfo.getName().setFamilyName(TEST_USER_LASTNAME);
        userInfo.addEmail(new Email().value(TEST_USER_EMAIL));
        userId = scim2RestClient.createUser(userInfo);

        updatePasswordRecoveryFeatureStatus(true);
        updateAdminPasswordResetRecoveryEmailFeatureStatus(true);
        updateAdminInitiatedPasswordResetEmailFeatureStatus(true);

        actionId = createPreUpdatePasswordAction(ACTION_NAME, ACTION_DESCRIPTION);

        actionsMockServer = new ActionsMockServer();
        actionsMockServer.startServer();
        actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        actionsMockServer.resetRequests();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        updatePasswordRecoveryFeatureStatus(false);
        updateAdminPasswordResetRecoveryEmailFeatureStatus(false);
        updateAdminInitiatedPasswordResetEmailFeatureStatus(false);

        deleteAction(PRE_UPDATE_PASSWORD_API_PATH, actionId);
        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);
        restClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
        actionsMockServer.stopServer();
        actionsMockServer = null;
    }

    @Test(description = "Verify the password update in self service portal with pre update password action")
    public void testUserUpdatePassword() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_UPDATED_PASSWORD);
        updateUserPatchOp.setValue(passwordValue);
        org.json.simple.JSONObject response = scim2RestClient.updateUserMe(new PatchOperationRequestObject()
                        .addOperations(updateUserPatchOp), TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(),
                TEST_USER_PASSWORD);

        assertNotNull(response);
        assertEquals(response.get("status"), expectedPasswordUpdateResponse.getStatusCode());
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_UPDATED_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testUserUpdatePassword" ,
            description = "Verify the admin update password with pre update password action")
    public void testAdminUpdatePassword() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        updateUserPatchOp.setValue(passwordValue);
        org.json.simple.JSONObject response = scim2RestClient.updateUserAndReturnResponse(
                new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);

        assertNotNull(response);
        assertEquals(response.get("status"), expectedPasswordUpdateResponse.getStatusCode());
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testAdminUpdatePassword",
            description = "Verify the user password reset with pre update password action")
    public void testUserResetPassword() throws Exception {

        String passwordRecoveryFormURL = retrievePasswordResetURL(application);
        submitPasswordRecoveryForm(passwordRecoveryFormURL, TEST_USER1_USERNAME);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
        String htmlContent = EntityUtils.toString(postResponse.getEntity());
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);

        if (expectedPasswordUpdateResponse.getStatusCode().equals("400")) {
            assertTrue(htmlContent.contains(expectedPasswordUpdateResponse.getErrorDetail()));
        } else {
            assertTrue(htmlContent.contains(EMAIL_INVITE_PASSWORD_RESET_ERROR));
        }

        assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.RESET);
    }

    @Test(dependsOnMethods = "testUserResetPassword",
            description = "Verify the admin force password reset with pre update password action")
    public void testAdminForcePasswordReset() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        updateUserPatchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + FORCE_PASSWORD_RESET_ATTRIBUTE);
        updateUserPatchOp.setValue(true);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
        String htmlContent = EntityUtils.toString(postResponse.getEntity());
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);

        if (expectedPasswordUpdateResponse.getStatusCode().equals("400")) {
            assertTrue(htmlContent.contains(expectedPasswordUpdateResponse.getErrorDetail()));
        } else {
            assertTrue(htmlContent.contains(EMAIL_INVITE_PASSWORD_RESET_ERROR));
        }

        assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.RESET);
    }

    @Test(dependsOnMethods = "testAdminForcePasswordReset",
            description = "Verify the admin invite user to set password with pre update password action")
    public void testAdminInviteUserToSetPassword() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER2_USERNAME);
        userInfo.setPassword(TEST_USER_PASSWORD);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN_NAME));
        userInfo.getName().setFamilyName(TEST_USER_LASTNAME);
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().askPassword(true));
        userInfo.addEmail(new Email().value(TEST_USER_EMAIL));
        String tempUserId = scim2RestClient.createUser(userInfo);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink, RESET_PASSWORD);
        String htmlContent = EntityUtils.toString(postResponse.getEntity());
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);

        if (expectedPasswordUpdateResponse.getStatusCode().equals("400")) {
            assertTrue(htmlContent.contains(expectedPasswordUpdateResponse.getErrorDetail()));
        } else {
            assertTrue(htmlContent.contains(EMAIL_INVITE_PASSWORD_RESET_ERROR));
        }

        assertActionRequestPayload(tempUserId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(tempUserId);
    }

    @Test(dependsOnMethods = "testAdminInviteUserToSetPassword",
            description = "Verify the password update by an authorized application with pre update password action")
    public void testApplicationUpdatePassword() throws Exception {

        String token = getTokenWithClientCredentialsGrant(applicationId, clientId, clientSecret);

        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        updateUserPatchOp.setValue(passwordValue);
        org.json.simple.JSONObject response = scim2RestClient.updateUserWithBearerToken(
                new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId, token);

        assertNotNull(response);
        assertEquals(response.get("status"), expectedPasswordUpdateResponse.getStatusCode());
        assertTrue(response.get("detail").toString().contains(expectedPasswordUpdateResponse.getErrorDetail()));
        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    private void assertActionRequestPayload(String userId, String updatedPassword,
                                            PreUpdatePasswordEvent.FlowInitiatorType initiatorType,
                                            PreUpdatePasswordEvent.Action action) throws JsonProcessingException {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
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
}
