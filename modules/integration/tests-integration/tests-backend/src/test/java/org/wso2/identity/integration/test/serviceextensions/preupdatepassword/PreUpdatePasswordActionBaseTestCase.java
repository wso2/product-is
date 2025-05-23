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

import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PasswordSharing;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdatepassword.model.PreUpdatePasswordActionModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.*;

public class PreUpdatePasswordActionBaseTestCase extends ActionsBaseTestCase {

    private static final String SCIM2_USERS_API = "/scim2/Users";
    private static final String INTERNAL_USER_MANAGEMENT_UPDATE = "internal_user_mgt_update";
    private static final String APP_CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_OFFLINE = "Recovery.AdminPasswordReset.Offline";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_OTP = "Recovery.AdminPasswordReset.OTP";
    private static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_LINK = "Recovery.AdminPasswordReset.RecoveryLink";
    public static final String ENABLE_ADMIN_PASSWORD_RESET_WITH_SMS_OTP = "Recovery.AdminPasswordReset.SMSOTP";
    private static final String TRUE_STRING = "true";
    private static final String FALSE_STRING = "false";

    protected static final String TEST_USER1_USERNAME = "testUsername";
    protected static final String TEST_USER2_USERNAME = "testUsername2";
    protected static final String TEST_USER_PASSWORD = "TestPassword@123";
    protected static final String TEST_USER_UPDATED_PASSWORD = "UpdatedTestPassword@123";
    protected static final String RESET_PASSWORD = "ResetTestPassword@123";
    protected static final String TEST_USER_GIVEN_NAME = "test_user_given_name";
    protected static final String TEST_USER_LASTNAME = "test_user_last_name";
    protected static final String TEST_USER_EMAIL = "test.user@gmail.com";
    protected static final String PRIMARY_USER_STORE_ID = "UFJJTUFSWQ==";
    protected static final String PRIMARY_USER_STORE_NAME = "PRIMARY";
    protected static final String USER_SYSTEM_SCHEMA_ATTRIBUTE ="urn:scim:wso2:schema";
    protected static final String FORCE_PASSWORD_RESET_ATTRIBUTE = "forcePasswordReset";
    protected static final String PRE_UPDATE_PASSWORD_API_PATH = "preUpdatePassword";
    protected static final String ACTION_NAME = "Pre Update Password Action";
    protected static final String ACTION_DESCRIPTION = "This is a test for pre update password action type";
    protected static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    protected static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";


    protected CloseableHttpClient client;
    protected IdentityGovernanceRestClient identityGovernanceRestClient;

    private final CookieStore cookieStore = new BasicCookieStore();

    /**
     * Initialize the test case.
     *
     * @param userMode User Mode
     * @throws Exception If an error occurred while initializing the clients.
     */
    protected void init(TestUserMode userMode) throws Exception {

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
                .setDefaultCookieStore(cookieStore)
                .build();

        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
    }

    protected void updatePasswordRecoveryFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("Recovery.Notification.Password.emailLink.Enable");
        propertyReq.setValue(enable ? "true" : "false");
        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWNjb3VudC1yZWNvdmVyeQ", connectorsPatchReq);
    }

    protected void enableAdminPasswordResetRecoveryEmailLink() throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);

        PropertyReq emailLinkProperty = new PropertyReq();
        emailLinkProperty.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_LINK);
        emailLinkProperty.setValue(TRUE_STRING);
        connectorsPatchReq.addProperties(emailLinkProperty);

        PropertyReq emailOtpProperty = new PropertyReq();
        emailOtpProperty.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_EMAIL_OTP);
        emailOtpProperty.setValue(FALSE_STRING);
        connectorsPatchReq.addProperties(emailOtpProperty);

        PropertyReq offlineProperty = new PropertyReq();
        offlineProperty.setName(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE);
        offlineProperty.setValue(FALSE_STRING);
        connectorsPatchReq.addProperties(offlineProperty);

        PropertyReq smsOtpProperty = new PropertyReq();
        smsOtpProperty.setName(ENABLE_ADMIN_PASSWORD_RESET_WITH_SMS_OTP);
        smsOtpProperty.setValue(FALSE_STRING);
        connectorsPatchReq.addProperties(smsOtpProperty);

        identityGovernanceRestClient.updateConnectors("QWNjb3VudCBNYW5hZ2VtZW50",
                "YWRtaW4tZm9yY2VkLXBhc3N3b3JkLXJlc2V0", connectorsPatchReq);
    }

    protected void updateAdminInitiatedPasswordResetEmailFeatureStatus(boolean enable) throws IOException {

        ConnectorsPatchReq connectorsPatchReq = new ConnectorsPatchReq();
        connectorsPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq propertyReq = new PropertyReq();
        propertyReq.setName("EmailVerification.Enable");
        propertyReq.setValue(enable ? "true" : "false");

        connectorsPatchReq.addProperties(propertyReq);
        identityGovernanceRestClient.updateConnectors("VXNlciBPbmJvYXJkaW5n",
                "dXNlci1lbWFpbC12ZXJpZmljYXRpb24", connectorsPatchReq);
    }

    protected HttpResponse resetPassword(String recoveryLink, String newPassword) throws IOException {

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
                value = newPassword;
            }
            formParams.add(new BasicNameValuePair(name, value));
        }

        return sendPostRequestWithParameters(client, formParams, actionURL);
    }

    protected String getRecoveryURLFromEmail() {

        Assert.assertTrue(org.wso2.identity.integration.test.util.Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = org.wso2.identity.integration.test.util.Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");
        Document doc = Jsoup.parse(body);

        return doc.selectFirst("#bodyCell").selectFirst("a").attr("href");
    }

    protected String retrievePasswordResetURL(ApplicationResponseModel application) throws Exception {

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

    protected void submitPasswordRecoveryForm(String url, String username) throws Exception {

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

    protected String createPreUpdatePasswordAction(String actionName, String actionDescription) throws IOException {

        AuthenticationType authentication = new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BASIC)
                .putPropertiesItem(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME)
                .putPropertiesItem(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);

        Endpoint endpoint = new Endpoint()
                .uri(EXTERNAL_SERVICE_URI)
                .authentication(authentication);

        PreUpdatePasswordActionModel actionModel = new PreUpdatePasswordActionModel();
        actionModel.setName(actionName);
        actionModel.setDescription(actionDescription);
        actionModel.setEndpoint(endpoint);
        actionModel.setPasswordSharing(new PasswordSharing().format(PasswordSharing.FormatEnum.PLAIN_TEXT));

        return createAction(PRE_UPDATE_PASSWORD_API_PATH, actionModel);
    }

    protected String getTokenWithClientCredentialsGrant(String applicationId, String clientId, String clientSecret) throws Exception {

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
