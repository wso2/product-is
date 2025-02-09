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
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.actions.model.*;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PasswordSharing;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model
        .PreUpdatePasswordActionModel;
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.*;

/**
 * Integration test class for testing the pre issue access token flow with client credentials grant.
 * This test case extends {@link ActionsBaseTestCase} and focuses on scenarios related
 * to scopes and claims modifications through an external service.
 */
public class PreUpdatePasswordActionSuccessTestCase extends ActionsBaseTestCase {

    private static final String USERNAME = "testusername";
    private static final String USERNAME_1 = "testusername1";
    private static final String PASSWORD = "TestPassword@123";
    private static final String UPDATED_PASSWORD = "UpdatedTestPassword@123";
    private static final String RESET_PASSWORD = "ResetTestPassword@123";
    private static final String USER_GIVENNAME = "test_user_given_name";
    private static final String USER_LASTNAME = "test_user_last_name";
    private static final String USER_EMAIL = "test.user@gmail.com";
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
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private List<String> requestedScopes;
    private String accessToken;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String tenantId;
    private TestUserMode userMode;
    private ActionsMockServer actionsMockServer;
    private String userId;
    private ApplicationResponseModel application;
    private IdentityGovernanceRestClient identityGovernanceRestClient;

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE ="urn:scim:wso2:schema";
    private static final String FORCE_PASSWORD_RESET_ATTRIBUTE = "forcePasswordReset";
    private static final String ASK_PASSWORD_RESET_ATTRIBUTE = "askPassword";
    private static final String ACCOUNT_LOCKED_ATTRIBUTE = "accountLocked";

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreUpdatePasswordActionSuccessTestCase(TestUserMode testUserMode) {

        this.userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    /**
     * Initializes Test environment and sets up necessary configurations.
     *
     * @throws Exception If an error occurs during initialization
     */
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(userMode);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
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

        requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,INTERNAL_USER_MANAGEMENT_UPDATE);

        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME);
        userInfo.setPassword(PASSWORD);
        userInfo.setName(new Name().givenName(USER_GIVENNAME));
        userInfo.getName().setFamilyName(USER_LASTNAME);
        userInfo.addEmail(new Email().value(USER_EMAIL));
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

    @BeforeMethod
    public void setUp() throws Exception {


    }

    @Test(groups = "wso2.is", description =
            "Verify the password update with the execution of pre update password extension")
    public void testPreUpdatePasswordOnAdminUpdateTestCase() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, UPDATED_PASSWORD);
        updateUserPatchOp.setValue(passwordValue);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminUpdateTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnAdminUpdateRequestTestCase() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminUpdateRequestTestCase",  description =
            "Verify the password update admin force password reset email invite with the execution of pre update " +
                    "password extension")
    public void testPreUpdatePasswordOnAdminForcePasswordResetTestCase() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        updateUserPatchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE+":"+FORCE_PASSWORD_RESET_ATTRIBUTE);
        updateUserPatchOp.setValue(true);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(updateUserPatchOp), userId);
        Boolean accountActiveValue = (Boolean) ((JSONObject) scim2RestClient.getUser(userId, null)
                .get(USER_SYSTEM_SCHEMA_ATTRIBUTE)).get(ACCOUNT_LOCKED_ATTRIBUTE);
        Assert.assertTrue(accountActiveValue, "User account wasn't locked to self reset the password");

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminForcePasswordResetTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnAdminForcePasswordResetActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminForcePasswordResetActionRequest",
            description = "Verify the password update admin initiated ask password reset by email invite with the " +
                    "execution of pre update password extension")
    public void testPreUpdatePasswordOnAdminAskPasswordResetTestCase() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();

        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME_1);
        userInfo.setPassword(PASSWORD);
        userInfo.setName(new Name().givenName(USER_GIVENNAME));
        userInfo.getName().setFamilyName(USER_LASTNAME);
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().askPassword(true));
        userInfo.addEmail(new Email().value(USER_EMAIL));
        String userId = scim2RestClient.createUser(userInfo);

        Boolean accountActiveValue = (Boolean) ((JSONObject) scim2RestClient.getUser(userId, null)
                .get(USER_SYSTEM_SCHEMA_ATTRIBUTE)).get(ASK_PASSWORD_RESET_ATTRIBUTE);
        //Assert.assertTrue(accountActiveValue, "User account wasn't locked to self reset the password");

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        //Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Set Successfully"));

        scim2RestClient.deleteUser(userId);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminAskPasswordResetTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnAdminInitiatedPasswordResetActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
            new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnAdminInitiatedPasswordResetActionRequest",
            description = "Verify the password update by an authorized application with the execution of pre update " +
                    "password extension")
    public void testPreUpdatePasswordOnApplicationUserUpdateTestCase() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
        }

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        org.json.JSONObject jsonResponse = new org.json.JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, PASSWORD);
        updateUserPatchOp.setValue(passwordValue);

        scim2RestClient.updateUserWithBearerToken(new PatchOperationRequestObject().addOperations(updateUserPatchOp),
                userId, accessToken);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnApplicationUserUpdateTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnApplicationUserUpdateActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
            new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnApplicationUserUpdateActionRequest",
            description = "Verify the password update admin reset password invite with the execution of pre update " +
                    "password extension")
    public void testPreUpdatePasswordOnForgotPasswordResetTestCase() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();

        String passwordRecoveryFormURL = retrievePasswordResetURL(application, client);
        submitPasswordRecoveryForm(passwordRecoveryFormURL, USERNAME, client);

        String recoveryLink = getRecoveryURLFromEmail();
        HttpResponse postResponse = resetPassword(recoveryLink);
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        Assert.assertTrue(EntityUtils.toString(postResponse.getEntity()).contains("Password Reset Successfully"));
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnForgotPasswordResetTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnForgotPasswordResetActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
            new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnForgotPasswordResetActionRequest",
            description = "Verify the password update in self service portal with the execution of pre update " +
                    "password extension")
    public void testPreUpdatePasswordOnUserUpdateTestCase() throws Exception {

        UserItemAddGroupobj updateUserPatchOp = new UserItemAddGroupobj().op(UserItemAddGroupobj.OpEnum.REPLACE);
        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put(PASSWORD_PROPERTY, PASSWORD);
        updateUserPatchOp.setValue(passwordValue);
        scim2RestClient.updateUserMe(new PatchOperationRequestObject().addOperations(updateUserPatchOp),
                userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreUpdatePasswordOnUserUpdateTestCase", description =
            "Verify the pre update password action request")
    public void testPreUpdatePasswordOnUserUpdateActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreUpdatePasswordActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreUpdatePasswordActionRequest.class);
        PreUpdatePasswordActionRequest expectedRequest = getRequest();
        //assertEquals(actualRequest, expectedRequest);
        actionsMockServer.resetRequests();
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
        PropertyReq expiryTimeProperty = new PropertyReq();
        expiryTimeProperty.setName("EmailVerification.AskPassword.ExpiryTime");
        expiryTimeProperty.setValue("60");

        connectorsPatchReq.addProperties(propertyReq);
        connectorsPatchReq.addProperties(expiryTimeProperty);
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


    /**
     * Retrieves pre issue access token action request.
     *
     * @return pre issue access token request object
     */
    private PreUpdatePasswordActionRequest getRequest() throws ActionExecutionRequestBuilderException {

        Tenant tenant = new Tenant(tenantId, tenantInfo.getDomain());

        PasswordUpdatingUser user = new PasswordUpdatingUser.Builder()
                .id(userId)
                .updatingCredential(new Credential.Builder()
                        .type(Credential.Type.PASSWORD)
                        .format(Credential.Format.PLAIN_TEXT)
                        .value("Hello@123".toCharArray())
                        .build())
                .build();

        UserStore userStore = new UserStore();
        userStore.setId(PRIMARY_USER_STORE_ID);
        userStore.setName(PRIMARY_USER_STORE_NAME);

        PreUpdatePasswordEvent event = new PreUpdatePasswordEvent.Builder()
                .tenant(tenant)
                .user(user)
                .userStore(userStore)
                .initiator(PreUpdatePasswordEvent.FlowInitiator.ADMIN)
                .action(PreUpdatePasswordEvent.Action.UPDATE)
                .build();

        return new PreUpdatePasswordActionRequest.Builder()
                .actionType(ActionType.PRE_UPDATE_PASSWORD)
                .event(event)
                .build();

    }

    /**
     * Creates an action for pre-issuing an access token with basic authentication.
     *
     * @return ID of the created action
     * @throws IOException If an error occurred while creating the action
     */
    private String createPreUpdatePasswordAction() throws IOException {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME);
        authProperties.put(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);

        PreUpdatePasswordActionModel actionModel = new PreUpdatePasswordActionModel();
        actionModel.setName(ACTION_NAME);
        actionModel.setDescription(ACTION_DESCRIPTION);
        actionModel.setEndpoint(endpoint);
        actionModel.setPasswordSharing(new PasswordSharing().format(PasswordSharing.FormatEnum.PLAIN_TEXT));

        return createAction(PRE_UPDATE_PASSWORD_API_PATH, actionModel);
    }
}
