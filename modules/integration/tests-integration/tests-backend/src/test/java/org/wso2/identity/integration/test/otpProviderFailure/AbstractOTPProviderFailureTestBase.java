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

package org.wso2.identity.integration.test.otpProviderFailure;

import com.icegreen.greenmail.util.GreenMailUtil;
import jakarta.mail.Message;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.wso2.identity.integration.test.base.MockSMSProvider;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Properties;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single abstract base for all OTP provider sending-failure notification integration tests.
 */
public abstract class AbstractOTPProviderFailureTestBase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String BASIC_AUTHENTICATOR = "BasicAuthenticator";
    private static final int MAX_REDIRECTS = 10;

    protected static final String CALLBACK_URL = "https://example.com/oidc-callback";
    protected static final String SMS_SENDER_REQUEST_FORMAT = "{\"content\": {{body}}, \"to\": {{mobile}} }";

    protected void enableEmailOTPSendingFailureNotification() throws IOException {

        updateOTPConnectorProperty(
                Constants.EMAIL_OTP_CONNECTOR_ID,
                Constants.EMAIL_OTP_NOTIFY_SENDING_FAILURE_PROPERTY,
                Constants.PROPERTY_ENABLED);
    }

    protected void disableEmailOTPSendingFailureNotification() throws IOException {

        updateOTPConnectorProperty(
                Constants.EMAIL_OTP_CONNECTOR_ID,
                Constants.EMAIL_OTP_NOTIFY_SENDING_FAILURE_PROPERTY,
                Constants.PROPERTY_DISABLED);
    }

    protected void enableSMSOTPSendingFailureNotification() throws IOException {

        updateOTPConnectorProperty(
                Constants.SMS_OTP_CONNECTOR_ID,
                Constants.SMS_OTP_NOTIFY_SENDING_FAILURE_PROPERTY,
                Constants.PROPERTY_ENABLED);
    }

    protected void disableSMSOTPSendingFailureNotification() throws IOException {

        updateOTPConnectorProperty(
                Constants.SMS_OTP_CONNECTOR_ID,
                Constants.SMS_OTP_NOTIFY_SENDING_FAILURE_PROPERTY,
                Constants.PROPERTY_DISABLED);
    }

    private void updateOTPConnectorProperty(String connectorId, String propertyName, String value)
            throws IOException {

        PropertyReq property = new PropertyReq();
        property.setName(propertyName);
        property.setValue(value);
        ConnectorsPatchReq patchReq = new ConnectorsPatchReq();
        patchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        patchReq.addProperties(property);

        IdentityGovernanceRestClient client = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        try {
            client.updateConnectors(Constants.MFA_GOVERNANCE_CATEGORY_ID, connectorId, patchReq);
        } finally {
            client.closeHttpClient();
        }
    }

    protected String addTwoStepOIDCApp(String appName, String otpAuthenticatorName) throws Exception {

        return addApplication(buildTwoStepAppModel(appName, otpAuthenticatorName, false));
    }

    protected String addTwoStepNativeApp(String appName, String otpAuthenticatorName) throws Exception {

        return addApplication(buildTwoStepAppModel(appName, otpAuthenticatorName, true));
    }

    private ApplicationModel buildTwoStepAppModel(String appName, String otpAuthenticatorName, boolean apiNative) {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(CALLBACK_URL));
        if (apiNative) {
            oidcConfig.setPublicClient(true);
        }

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);

        if (apiNative) {
            AdvancedApplicationConfiguration advancedConfig = new AdvancedApplicationConfiguration();
            advancedConfig.setEnableAPIBasedAuthentication(true);
            application.setAdvancedConfigurations(advancedConfig);
        }

        AuthenticationStep step1 = new AuthenticationStep();
        step1.setId(1);
        step1.addOptionsItem(new Authenticator().idp("LOCAL").authenticator(BASIC_AUTHENTICATOR));

        AuthenticationStep step2 = new AuthenticationStep();
        step2.setId(2);
        step2.addOptionsItem(new Authenticator().idp("LOCAL").authenticator(otpAuthenticatorName));

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step1);
        authSequence.addStepsItem(step2);
        authSequence.setSubjectStepId(1);

        application.setAuthenticationSequence(authSequence);
        return application;
    }

    protected String createTestUser(SCIM2RestClient scim2) throws Exception {

        return scim2.createUser(buildTestUserObject());
    }

    protected UserObject buildTestUserObject() {

        UserObject user = new UserObject();
        user.setUserName(Constants.TEST_USER_NAME);
        user.setPassword(Constants.TEST_USER_PASSWORD);
        user.setName(new Name()
                .givenName(Constants.TEST_USER_FIRST_NAME)
                .familyName(Constants.TEST_USER_LAST_NAME));
        user.addEmail(new Email().value(Constants.TEST_USER_EMAIL));
        user.addPhoneNumbers(new PhoneNumbers().type("mobile").value(Constants.TEST_USER_MOBILE));
        return user;
    }

    protected SMSSender buildSMSSender() {

        SMSSender smsSender = new SMSSender();
        smsSender.setProvider(MockSMSProvider.SMS_SENDER_PROVIDER_TYPE);
        smsSender.setProviderURL(MockSMSProvider.SMS_SENDER_URL);
        smsSender.contentType(SMSSender.ContentTypeEnum.JSON);
        List<Properties> properties = new ArrayList<>();
        properties.add(new Properties().key("body").value(SMS_SENDER_REQUEST_FORMAT));
        smsSender.setProperties(properties);
        return smsSender;
    }

    protected CloseableHttpClient createSessionHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        return HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }

    protected CloseableHttpClient createTrustAllHttpClient() throws Exception {

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
        return HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }

    protected String initiateRedirectFlow(CloseableHttpClient httpClient, String clientId) throws Exception {

        String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                + "?response_type=code&client_id=" + clientId
                + "&redirect_uri=" + CALLBACK_URL
                + "&scope=openid";
        HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
        response = followRedirectsUntilLoginPage(httpClient, response);

        Map<String, Integer> keyPositionMap = new HashMap<>();
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey not found in authorize response.");
        String sessionDataKey = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());
        return sessionDataKey;
    }

    protected HttpResponse sendCredentials(CloseableHttpClient httpClient, String sessionDataKey,
            String username, String password) throws IOException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(httpClient, params,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    protected HttpResponse sendOTPCode(CloseableHttpClient httpClient, String sessionDataKey,
            String otpCode) throws IOException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("OTPcode", otpCode));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(httpClient, params,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    protected String followRedirectsToCallback(CloseableHttpClient httpClient, HttpResponse response)
            throws Exception {

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            if (locationHeader == null) {
                break;
            }
            String location = locationHeader.getValue();
            EntityUtils.consume(response.getEntity());
            if (location.startsWith(CALLBACK_URL)) {
                return location;
            }
            response = sendGetRequest(httpClient, location);
        }
        EntityUtils.consume(response.getEntity());
        return null;
    }

    private HttpResponse followRedirectsUntilLoginPage(CloseableHttpClient httpClient, HttpResponse response)
            throws Exception {

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            int status = response.getStatusLine().getStatusCode();
            if (status < 300 || status >= 400) {
                return response;
            }
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            if (locationHeader == null) {
                return response;
            }
            EntityUtils.consume(response.getEntity());
            response = sendGetRequest(httpClient, locationHeader.getValue());
        }
        return response;
    }

    protected AuthnFlowState initiateNativeFlow(String clientId) throws Exception {

        String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain());

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(authorizeUrl);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("response_type", "code"));
            params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            params.add(new BasicNameValuePair("scope", "openid"));
            params.add(new BasicNameValuePair("response_mode", "direct"));
            post.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            JSONObject json = (JSONObject) new JSONParser().parse(body);

            String flowId = (String) json.get("flowId");
            JSONObject nextStep = (JSONObject) json.get("nextStep");
            Assert.assertNotNull(nextStep, "Expected 'nextStep' in authorize response: " + body);
            JSONArray authenticators = (JSONArray) nextStep.get("authenticators");
            String authenticatorId = (String) ((JSONObject) authenticators.get(0)).get("authenticatorId");

            return new AuthnFlowState(flowId, authenticatorId, json);
        }
    }

    protected AuthnFlowState completeBasicAuth(AuthnFlowState state, String username, String password)
            throws Exception {

        String body = "{\"flowId\":\"" + state.flowId + "\","
                + "\"selectedAuthenticator\":{"
                + "\"authenticatorId\":\"" + state.authenticatorId + "\","
                + "\"params\":{\"username\":\"" + username + "\","
                + "\"password\":\"" + password + "\"}}}";
        return postToAuthn(state.flowId, body);
    }

    protected AuthnFlowState submitOTPCode(AuthnFlowState state, String otpCode) throws Exception {

        String body = "{\"flowId\":\"" + state.flowId + "\","
                + "\"selectedAuthenticator\":{"
                + "\"authenticatorId\":\"" + state.authenticatorId + "\","
                + "\"params\":{\"OTPCode\":\"" + otpCode + "\"}}}";
        return postToAuthn(state.flowId, body);
    }

    private AuthnFlowState postToAuthn(String flowId, String jsonBody) throws Exception {

        String authnUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                .replaceAll("oauth2/authorize/?$", "oauth2/authn/");

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(authnUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            JSONObject json = (JSONObject) new JSONParser().parse(body);

            String nextAuthenticatorId = null;
            JSONObject nextStep = (JSONObject) json.get("nextStep");
            if (nextStep != null) {
                JSONArray authenticators = (JSONArray) nextStep.get("authenticators");
                if (authenticators != null && !authenticators.isEmpty()) {
                    nextAuthenticatorId = (String) ((JSONObject) authenticators.get(0)).get("authenticatorId");
                }
            }
            return new AuthnFlowState(flowId, nextAuthenticatorId, json);
        }
    }

    protected String followToOtpPage(CloseableHttpClient httpClient, HttpResponse response) throws Exception {

        for (int i = 0; i < MAX_REDIRECTS; i++) {
            Header loc = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            if (loc == null) {
                EntityUtils.consume(response.getEntity());
                return null;
            }
            String url = loc.getValue();
            EntityUtils.consume(response.getEntity());
            if (url.contains("emailOtp.jsp") || url.contains("smsOtp.jsp")) {
                return url;
            }
            response = sendGetRequest(httpClient, url);
        }
        return null;
    }

    protected String extractUrlParam(String url, String paramName) throws Exception {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if (paramName.equals(param.getName())) {
                return param.getValue();
            }
        }
        return null;
    }

    protected String getPageHtml(CloseableHttpClient httpClient, String url) throws IOException {

        HttpResponse response = sendGetRequest(httpClient, url);
        return EntityUtils.toString(response.getEntity());
    }

    protected String extractOtpFromEmail(int emailCount) throws Exception {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, emailCount),
                "Expected " + emailCount + " email(s) to arrive within 10 seconds.");
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[messages.length - 1]).replaceAll("=\r?\n", "");
        Matcher matcher = Pattern.compile("\\s*<b>(\\d+)</b>").matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    protected HttpResponse resendOTP(CloseableHttpClient httpClient, String sessionDataKeyForOtp) throws IOException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("resendCode", "true"));
        params.add(new BasicNameValuePair("sessionDataKey", sessionDataKeyForOtp));
        return sendPostRequestWithParameters(httpClient, params,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    protected boolean hasProviderFailureInMessages(JSONObject response) {

        JSONObject nextStep = (JSONObject) response.get("nextStep");
        if (nextStep == null) return false;
        JSONArray messages = (JSONArray) nextStep.get("messages");
        if (messages == null) return false;
        for (Object msg : messages) {
            JSONObject message = (JSONObject) msg;
            String type = (String) message.get("type");
            String messageId = (String) message.get("messageId");
            if ("ERROR".equals(type) && messageId != null
                    && (messageId.startsWith(Constants.EMAIL_PROVIDER_ERROR_CODE_PREFIX)
                        || messageId.startsWith(Constants.SMS_PROVIDER_ERROR_CODE_PREFIX))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Carries the mutable state across steps of an app-native authentication flow.
     * {@code response} is the full JSON response so tests can inspect it directly.
     */
    protected static class AuthnFlowState {

        public final String flowId;
        public final String authenticatorId;
        public final JSONObject response;

        AuthnFlowState(String flowId, String authenticatorId, JSONObject response) {

            this.flowId = flowId;
            this.authenticatorId = authenticatorId;
            this.response = response;
        }
    }
}
