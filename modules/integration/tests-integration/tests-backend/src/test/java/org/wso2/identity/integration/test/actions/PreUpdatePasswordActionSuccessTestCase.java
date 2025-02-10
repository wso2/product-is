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

package org.wso2.identity.integration.test.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.actions.model.*;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PasswordSharing;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PreUpdatePasswordActionModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.*;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.*;

/**
 * Integration test class for testing the pre update password action execution.
 * This test case extends {@link ActionsBaseTestCase} and focuses on success scenarios related
 * to password update flows.
 */
public class PreUpdatePasswordActionSuccessTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER1_USERNAME = "testUsername";
    private static final String TEST_USER2_USERNAME = "testUsername2";
    private static final String TEST_USER_PASSWORD = "TestPassword@123";
    private static final String TEST_USER_UPDATED_PASSWORD = "UpdatedTestPassword@123";
    private static final String RESET_PASSWORD = "ResetTestPassword@123";
    private static final String TEST_USER_GIVEN_NAME = "test_user_given_name";
    private static final String TEST_USER_LASTNAME = "test_user_last_name";
    private static final String TEST_USER_EMAIL = "test.user@gmail.com";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String PRIMARY_USER_STORE_ID = "UFJJTUFSWQ==";
    private static final String PRIMARY_USER_STORE_NAME = "PRIMARY";
    private static final String EXTERNAL_SERVICE_URI = "http://localhost:8587/test/action";
    private static final String APP_CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";
    private static final String ACTION_NAME = "Pre Update Password Action";
    private static final String ACTION_DESCRIPTION = "This is a test for pre update password action type";
    private static final String PRE_UPDATE_PASSWORD_API_PATH = "preUpdatePassword";
    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String INTERNAL_USER_MANAGEMENT_UPDATE = "internal_user_mgt_update";
    private static final String SCIM2_USERS_API = "/scim2/Users";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String MOCK_SERVER_AUTH_BASIC_USERNAME = "test";
    private static final String MOCK_SERVER_AUTH_BASIC_PASSWORD = "test";

    private final String tenantId;
    private final TestUserMode userMode;

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;

    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String userId;
    private ApplicationResponseModel application;
    private ActionsMockServer actionsMockServer;

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE ="urn:scim:wso2:schema";
    private static final String FORCE_PASSWORD_RESET_ATTRIBUTE = "forcePasswordReset";

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

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(userMode);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();


        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

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

        actionId = createPreUpdatePasswordAction();

        actionsMockServer = new ActionsMockServer();
        actionsMockServer.startServer();
        actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-update-password-response.json"));
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
        scim2RestClient.updateUserMe(new PatchOperationRequestObject().addOperations(updateUserPatchOp),
                TEST_USER1_USERNAME + "@" + tenantInfo.getDomain(), TEST_USER_PASSWORD);

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
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);

        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testAdminUpdatePassword",
            description = "Verify the admin force password reset with pre update password action")
    public void testAdminForcePasswordReset() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        updateUserPatchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + FORCE_PASSWORD_RESET_ATTRIBUTE);
        updateUserPatchOp.setValue(true);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));

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
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Set Successfully"));

        assertActionRequestPayload(tempUserId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN,
                PreUpdatePasswordEvent.Action.INVITE);
        scim2RestClient.deleteUser(tempUserId);
    }

    @Test(dependsOnMethods = "testAdminInviteUserToSetPassword",
            description = "Verify the password update by an authorized application with pre update password action")
    public void testApplicationUpdatePassword() throws Exception {

        String token = getTokenWithClientCredentialsGrant();

        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, TEST_USER_PASSWORD);
        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        updateUserPatchOp.setValue(passwordValue);
        scim2RestClient.updateUserWithBearerToken(new PatchOperationRequestObject().addOperations(updateUserPatchOp),
                userId, token);

        assertActionRequestPayload(userId, TEST_USER_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION,
                PreUpdatePasswordEvent.Action.UPDATE);
    }

    @Test(dependsOnMethods = "testApplicationUpdatePassword",
            description = "Verify the user password reset with pre update password action")
    public void testUserResetPassword() throws Exception {

        String passwordRecoveryFormURL = retrievePasswordResetURL(application, client);
        submitPasswordRecoveryForm(passwordRecoveryFormURL, TEST_USER1_USERNAME, client);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));

        assertActionRequestPayload(userId, RESET_PASSWORD, PreUpdatePasswordEvent.FlowInitiatorType.USER,
                PreUpdatePasswordEvent.Action.RESET);
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

    private void updatePasswordRecoveryFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("Recovery.Notification.Password.emailLink.Enable");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWNjb3VudC1yZWNvdmVyeQ", connectorsPatchReq);
    }

    private void updateAdminPasswordResetRecoveryEmailFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("Recovery.AdminPasswordReset.RecoveryLink");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWRtaW4tZm9yY2VkLXBhc3N3b3JkLXJlc2V0", connectorsPatchReq);
    }

    private void updateAdminInitiatedPasswordResetEmailFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("EmailVerification.Enable");
        propertyReq.setValue(enable ? "true" : "false");

        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("VXNlciBPbmJvYXJkaW5n",
                "dXNlci1lbWFpbC12ZXJpZmljYXRpb24", connectorsPatchReq);
    }

    private HttpResponse resetPassword(String recoveryLink) throws IOException {

        HttpResponse response = sendGetRequest(client, recoveryLink);

        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        Element form = doc.selectFirst("form");
        String baseURL = recoveryLink.substring(0, recoveryLink.lastIndexOf('/') + 1);
        Assert.assertNotNull(form, "Password reset form not found in the response.");
        String actionURL = new URL(new URL(baseURL), form.attr("action")).toString();

        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");
            if ("reset-password".equals(name) || "reset-password2".equals(name)) {
                value = RESET_PASSWORD;
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        return sendPostRequestWithParameters(client, formParams, actionURL);
    }

    private String getRecoveryURLFromEmail() {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);

        return doc.selectFirst("#bodyCell").selectFirst("a").attr("href");
    }

    private String retrievePasswordResetURL(ApplicationResponseModel application, HttpClient client) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", "openid email profile"));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header authorizeRequestURL = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, authorizeRequestURL.getValue());
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);
        Element link = doc.selectFirst("#passwordRecoverLink");
        Assert.assertNotNull(link, "Password recovery link not found in the response.");
        return link.attr("href");
    }

    private void submitPasswordRecoveryForm(String url, String username, HttpClient client) throws Exception {

        HttpResponse response = sendGetRequest(client, url);
        String htmlContent = EntityUtils.toString(response.getEntity());
        Document doc = Jsoup.parse(htmlContent);

        Element form = doc.selectFirst("form");
        String baseURL = url.substring(0, url.lastIndexOf('/') + 1);
        Assert.assertNotNull(form, "Password recovery form not found in the response.");
        String actionURL = new URL(new URL(baseURL), form.attr("action")).toString();

        List<NameValuePair> formParams = new ArrayList<>();
        for (Element input : form.select("input")) {
            String name = input.attr("name");
            String value = input.attr("value");
            if ("username".equals(name)) {
                value = username;
            }
            if ("usernameUserInput".equals(name)) {
                value = username;
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        HttpResponse postResponse = sendPostRequestWithParameters(client, formParams, actionURL);
        if (postResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Error occurred while submitting the password reset form.");
        }
        EntityUtils.consume(postResponse.getEntity());
    }

    private String createPreUpdatePasswordAction() throws IOException {

        AuthenticationType authentication = new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BASIC)
                .putPropertiesItem(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME)
                .putPropertiesItem(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);

        Endpoint endpoint = new Endpoint()
                .uri(EXTERNAL_SERVICE_URI)
                .authentication(authentication);

        PreUpdatePasswordActionModel actionModel = new PreUpdatePasswordActionModel();
        actionModel.setName(ACTION_NAME);
        actionModel.setDescription(ACTION_DESCRIPTION);
        actionModel.setEndpoint(endpoint);
        actionModel.setPasswordSharing(new PasswordSharing().format(PasswordSharing.FormatEnum.PLAIN_TEXT));

        return createAction(PRE_UPDATE_PASSWORD_API_PATH, actionModel);
    }

    private String getTokenWithClientCredentialsGrant() throws Exception {

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
        }

        List<String> requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,INTERNAL_USER_MANAGEMENT_UPDATE);
        String scopes = String.join(" ", requestedScopes);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"));
        return jsonResponse.getString("access_token");
    }
}
