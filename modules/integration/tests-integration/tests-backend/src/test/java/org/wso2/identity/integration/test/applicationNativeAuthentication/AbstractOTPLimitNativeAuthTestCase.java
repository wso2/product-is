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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PhoneNumbers;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.DESCRIPTION;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGES;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.RESPONSE_MODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TRACE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.USERNAME_PARAM;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;

/**
 * Abstract base class for Email-OTP and SMS-OTP OTP limit (retry + resend) integration tests.
 */
public abstract class AbstractOTPLimitNativeAuthTestCase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String TEST_USER_NAME = "it_user_otp_limits";
    protected static final String TEST_USER_PASSWORD = "User@123Otp!";
    protected static final String TEST_USER_EMAIL = "it_user_otp_limits@example.com";
    protected static final String TEST_USER_MOBILE = "+94771234567";

    protected static final String WRONG_OTP = "000000";
    protected static final String ADAPTIVE_SCRIPT_TEMPLATE =
            "var onLoginRequest = function(context) {\n" +
            "  executeStep(1, {\n" +
            "    authenticatorParams: {\n" +
            "      local: {\n" +
            "        \"%s\": {\n" +
            "          \"enableRetryFromAuthenticator\": \"true\",\n" +
            "          \"maximumAllowedFailureAttempts\": \"2\",\n" +
            "          \"maximumAllowedResendAttempts\": \"1\",\n" +
            "          \"terminateOnResendLimitExceeded\": \"true\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }, {});\n" +
            "};\n";

    protected static final String ADAPTIVE_SCRIPT_SOFT_RESEND_TEMPLATE =
            "var onLoginRequest = function(context) {\n" +
            "  executeStep(1, {\n" +
            "    authenticatorParams: {\n" +
            "      local: {\n" +
            "        \"%s\": {\n" +
            "          \"enableRetryFromAuthenticator\": \"true\",\n" +
            "          \"maximumAllowedFailureAttempts\": \"2\",\n" +
            "          \"maximumAllowedResendAttempts\": \"1\",\n" +
            "          \"terminateOnResendLimitExceeded\": \"false\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }, {});\n" +
            "};\n";

    protected CloseableHttpClient client;
    protected SCIM2RestClient scim2RestClient;
    protected String userId;
    protected String soapBackendURL;
    protected String authorizeEndpointURL;
    protected String flowId;
    protected String authenticatorId;
    protected String href;

    /**
     * Initializes common resources for OTP limit tests.
     *
     * @param userMode the user mode to initialize for (SUPER_TENANT or TENANT).
     */
    protected void commonInit(TestUserMode userMode) throws Exception {

        soapBackendURL = backendURL;
        backendURL = backendURL.replace("services/", "");

        authorizeEndpointURL = getTenantQualifiedURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain());

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();

        setSystemproperties();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userId = scim2RestClient.createUser(buildTestUser());

        resetResidentIDPCache();
    }

    /**
     * Tears down the common resources created.
     */
    protected void commonTearDown() throws Exception {

        if (userId != null) {
            scim2RestClient.deleteUser(userId);
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (restClient != null) {
            restClient.closeHttpClient();
        }
        if (client != null) {
            client.close();
        }

        // Nullify framework-level state.
        consumerKey = null;
        consumerSecret = null;
        flowId = null;
        authenticatorId = null;
        href = null;
        userId = null;
    }

    /**
     * Initiates a fresh API-based auth flow then completes the mandatory IDF step.
     *
     * @param consumerKey OAuth2 client id of the target application.
     */
    protected void initiateAuthFlow(String consumerKey) throws Exception {

        // Step 1 – POST /oauth2/authorize → IDF challenge.
        HttpResponse response = sendPostRequestWithParameters(
                client,
                buildOAuth2Parameters(consumerKey),
                authorizeEndpointURL);
        Assert.assertNotNull(response, "Auth flow initiation returned null response.");

        String bodyStr = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject initJson = (JSONObject) parser.parse(bodyStr);
        Assert.assertNotNull(initJson, "Auth flow init response JSON is null.");

        extractFlowState(initJson);

        // Step 2 – submit username through IDF → OTP is triggered and sent.
        submitUsernameForIDF();
    }

    /**
     * Submits the test username through the IDF (Identifier-First) step.
     */
    private void submitUsernameForIDF() {

        String idfBody = buildIDFSubmitBody(flowId, authenticatorId, TEST_USER_NAME);
        ExtractableResponse<Response> resp = postAndAssertStatus(href, idfBody, HttpStatus.SC_OK);

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertNotEquals(status, FAIL_INCOMPLETE,
                "IDF step failed unexpectedly. Username '" + TEST_USER_NAME + "' may not exist.");

        refreshFlowState(resp);
    }

    /**
     * Submits an OTP code against the current flow, asserts the expected HTTP status,
     * and returns the response for further assertion.
     *
     * @param otpCode        OTP to submit.
     * @param expectedStatus expected HTTP status code.
     */
    protected ExtractableResponse<Response> submitOTPCode(String otpCode, int expectedStatus) {

        return postAndAssertStatus(href, buildOTPSubmitBody(flowId, authenticatorId, otpCode), expectedStatus);
    }

    /**
     * Triggers an OTP resend against the current flow, asserts the expected HTTP status,
     * and returns the response for further assertion.
     *
     * @param expectedStatus expected HTTP status code.
     */
    protected ExtractableResponse<Response> triggerResend(int expectedStatus) {

        return postAndAssertStatus(href, buildResendBody(flowId, authenticatorId), expectedStatus);
    }

    /**
     * POSTs a JSON body to {@code url}, asserts the HTTP status, and returns
     * the extractable response.
     */
    protected ExtractableResponse<Response> postAndAssertStatus(
            String url, String requestBody, int expectedStatus) {

        return given()
                .contentType(ContentType.JSON)
                .headers(new HashMap<>())
                .body(requestBody)
                .when()
                .post(url)
                .then()
                .assertThat()
                .statusCode(expectedStatus)
                .extract();
    }

    /**
     * Asserts that an HTTP 400 error response body exactly matches the expected error.
     *
     * @param resp     the 400 response to inspect.
     * @param expected the {@link Constants.OtpLimitExceededError} enum entry to assert against.
     */
    protected void assertOTPLimitExceededPayload(
            ExtractableResponse<Response> resp,
            Constants.OtpLimitExceededError expected) {

        String actualCode = resp.jsonPath().getString(CODE);
        String actualMessage = resp.jsonPath().getString(MESSAGE);
        String actualDescription = resp.jsonPath().getString(DESCRIPTION);
        String actualTraceId = resp.jsonPath().getString(TRACE_ID);

        Assert.assertEquals(actualCode, expected.getCode(),
                "Error code mismatch. Expected: " + expected.getCode() + ", got: " + actualCode);
        Assert.assertEquals(actualMessage, expected.getMessage(),
                "Error message mismatch. Expected: " + expected.getMessage() + ", got: " + actualMessage);
        Assert.assertEquals(actualDescription, expected.getDescription(),
                "Error description mismatch. Expected: " + expected.getDescription()
                        + ", got: " + actualDescription);

        Assert.assertNotNull(actualTraceId, "traceId must be present in the error response.");
        Assert.assertFalse(actualTraceId.trim().isEmpty(), "traceId must not be blank in the error response.");
    }

    /**
     * Parses the init-response JSON and stores flowId, and authenticatorId.
     * and href(URL that should be called on next request) into instance fields.
     */
    protected void extractFlowState(JSONObject json) {

        Assert.assertTrue(json.containsKey(FLOW_ID), "flowId missing from init response.");
        Assert.assertTrue(json.containsKey(FLOW_STATUS), "flowStatus missing from init response.");
        Assert.assertTrue(json.containsKey(FLOW_TYPE), "flowType missing from init response.");
        Assert.assertTrue(json.containsKey(NEXT_STEP), "nextStep missing from init response.");
        Assert.assertTrue(json.containsKey(LINKS), "links missing from init response.");

        flowId = (String) json.get(FLOW_ID);

        JSONObject nextStep = (JSONObject) json.get(NEXT_STEP);
        Assert.assertTrue(nextStep.containsKey(STEP_TYPE), "stepType missing from nextStep.");
        Assert.assertTrue(nextStep.containsKey(AUTHENTICATORS), "authenticators missing from nextStep.");

        JSONArray authenticatorsArray = (JSONArray) nextStep.get(AUTHENTICATORS);
        Assert.assertFalse(authenticatorsArray.isEmpty(), "authenticators list must not be empty.");

        JSONObject authenticator = (JSONObject) authenticatorsArray.get(0);
        Assert.assertTrue(authenticator.containsKey(AUTHENTICATOR_ID), "authenticatorId missing.");
        Assert.assertTrue(authenticator.containsKey(METADATA), "metadata missing.");
        Assert.assertTrue(authenticator.containsKey(REQUIRED_PARAMS), "requiredParams missing.");

        authenticatorId = (String) authenticator.get(AUTHENTICATOR_ID);

        JSONObject metadata = (JSONObject) authenticator.get(METADATA);
        Assert.assertTrue(metadata.containsKey(PROMPT_TYPE), "promptType missing from metadata.");
        Assert.assertTrue(metadata.containsKey(PARAMS), "params missing from metadata.");

        JSONArray linksArray = (JSONArray) json.get(LINKS);
        Assert.assertFalse(linksArray.isEmpty(), "links list must not be empty.");
        JSONObject link = (JSONObject) linksArray.get(0);
        Assert.assertTrue(link.containsKey(HREF), "href missing from links.");
        href = link.get(HREF).toString();
    }

    /**
     * Fully refreshes flowId, authenticatorId, and href instance fields from the given response.
     * from success response.  All three fields are asserted to
     * be present (used after the IDF step where the OTP challenge must supply them).
     */
    protected void refreshFlowState(ExtractableResponse<Response> resp) {

        String newFlowId = resp.jsonPath().getString(FLOW_ID);
        Assert.assertNotNull(newFlowId, "flowId must be present in the IDF step response.");
        flowId = newFlowId;

        List<Map<String, Object>> links = resp.jsonPath().getList(LINKS);
        Assert.assertNotNull(links, "links must be present in the IDF step response.");
        Assert.assertFalse(links.isEmpty(), "links must not be empty in the IDF step response.");
        Object hrefObj = links.get(0).get(HREF);
        Assert.assertNotNull(hrefObj, "href must be present in the IDF step response links.");
        href = hrefObj.toString();

        String nextStepPrefix = NEXT_STEP + "." + AUTHENTICATORS;
        List<Map<String, Object>> auths = resp.jsonPath().getList(nextStepPrefix);
        Assert.assertNotNull(auths, "authenticators must be present in the IDF step nextStep.");
        Assert.assertFalse(auths.isEmpty(), "authenticators must not be empty in the IDF step nextStep.");
        Object idObj = auths.get(0).get(AUTHENTICATOR_ID);
        Assert.assertNotNull(idObj, "authenticatorId must be present in the IDF step authenticator.");
        authenticatorId = idObj.toString();
    }

    /**
     * Refreshes flowId, authenticatorId, and href instance fields from the given response if present.
     * that keeps the flow alive (FAIL_INCOMPLETE / INCOMPLETE).
     */
    protected void refreshHrefAndAuthenticatorId(ExtractableResponse<Response> resp) {

        List<Map<String, Object>> links = resp.jsonPath().getList(LINKS);
        if (links != null && !links.isEmpty()) {
            Object hrefObj = links.get(0).get(HREF);
            if (hrefObj != null) {
                href = hrefObj.toString();
            }
        }

        String nextStepPrefix = NEXT_STEP + "." + AUTHENTICATORS;
        List<Map<String, Object>> auths = resp.jsonPath().getList(nextStepPrefix);
        if (auths != null && !auths.isEmpty()) {
            Object idObj = auths.get(0).get(AUTHENTICATOR_ID);
            if (idObj != null) {
                authenticatorId = idObj.toString();
            }
        }
    }

    private String buildOTPSubmitBody(String flowId, String authenticatorId, String otpCode) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"OTPcode\": \"" + otpCode + "\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    private String buildIDFSubmitBody(String flowId, String authenticatorId, String username) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"" + USERNAME_PARAM + "\": \"" + username + "\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    private String buildResendBody(String flowId, String authenticatorId) {

        return "{\n" +
               "  \"flowId\": \"" + flowId + "\",\n" +
               "  \"selectedAuthenticator\": {\n" +
               "    \"authenticatorId\": \"" + authenticatorId + "\",\n" +
               "    \"params\": {\n" +
               "      \"resendCode\": \"true\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, RESPONSE_MODE));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        params.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        return params;
    }

    /**
     * Creates an OIDC application with API-based authentication enabled, a single
     * authentication step for authenticatorName, and the adaptive-auth script
     * that enforces the OTP retry/resend limits.
     *
     * @param appName           display name for the application.
     * @param authenticatorName authenticator key.
     * @return the created {@link ApplicationResponseModel}.
     */
    protected ApplicationResponseModel createOTPApp(String appName, String authenticatorName)
            throws Exception {

        return createOTPAppWithAdaptiveScript(appName, authenticatorName,
                String.format(ADAPTIVE_SCRIPT_TEMPLATE, authenticatorName));
    }

    /**
     * Creates an OIDC application with soft resend limit enabled.
     *
     * @param appName           display name for the application.
     * @param authenticatorName authenticator key.
     * @return the created {@link ApplicationResponseModel}.
     */
    protected ApplicationResponseModel createOTPAppWithSoftResendLimit(String appName, String authenticatorName)
            throws Exception {

        return createOTPAppWithAdaptiveScript(appName, authenticatorName,
                String.format(ADAPTIVE_SCRIPT_SOFT_RESEND_TEMPLATE, authenticatorName));
    }

    /**
     * Creates an OIDC application with API-based authentication enabled, a single
     * authentication step for authenticatorName, and the adaptive-auth script
     * that enforces the OTP retry/resend limits with the option to terminate or not
     * on resend limit exceeded.
     *
     * @param appName           display name for the application.
     * @param authenticatorName authenticator key.
     * @param adaptiveScript    adaptive auth script content to set in the application.
     * @return the created {@link ApplicationResponseModel}.
     */
    private ApplicationResponseModel createOTPAppWithAdaptiveScript(String appName, String authenticatorName,
                                                                    String adaptiveScript)
            throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocols);
        application.setName(appName);

        application.advancedConfigurations(new AdvancedApplicationConfiguration());
        application.getAdvancedConfigurations().setEnableAPIBasedAuthentication(true);

        AuthenticationStep step = new AuthenticationStep();
        step.setId(1);
        step.addOptionsItem(new Authenticator().idp("LOCAL").authenticator(authenticatorName));

        AuthenticationSequence authSequence = new AuthenticationSequence();
        authSequence.setType(AuthenticationSequence.TypeEnum.USER_DEFINED);
        authSequence.addStepsItem(step);
        authSequence.setSubjectStepId(1);
        authSequence.setScript(adaptiveScript);

        application.setAuthenticationSequence(authSequence);

        String newAppId = addApplication(application);
        return getApplication(newAppId);
    }

    /**
     * Asserts the response when the resend limit is exceeded and
     *
     * @param resp the HTTP 200 response to assert.
     */
    protected void assertSoftResendLimitExceededPayload(ExtractableResponse<Response> resp) {

        String status = resp.jsonPath().getString(FLOW_STATUS);
        Assert.assertEquals(status, FAIL_INCOMPLETE,
                "Soft resend limit: expected FAIL_INCOMPLETE but got: " + status);

        Constants.OtpLimitExceededMessage expected = Constants.OtpLimitExceededMessage.RESEND_LIMIT_EXCEEDED;
        String messagesPrefix = NEXT_STEP + "." + MESSAGES + "[0]";
        String actualType = resp.jsonPath().getString(messagesPrefix + ".type");
        String actualMessageId = resp.jsonPath().getString(messagesPrefix + "." + MESSAGE_ID);
        String actualMessage = resp.jsonPath().getString(messagesPrefix + "." + MESSAGE);

        Assert.assertEquals(actualType, expected.getType(),
                "Soft resend limit: expected message type " + expected.getType() + " but got: " + actualType);
        Assert.assertEquals(actualMessageId, expected.getMessageId(),
                "Soft resend limit: expected messageId " + expected.getMessageId()
                        + " but got: " + actualMessageId);
        Assert.assertEquals(actualMessage, expected.getMessage(),
                "Soft resend limit: expected message '" + expected.getMessage()
                        + "' but got: " + actualMessage);
    }

    /**
     * Builds the test user with both an email address (for Email-OTP) and a mobile
     * number (for SMS-OTP).
     */
    protected UserObject buildTestUser() {

        UserObject user = new UserObject();
        user.setUserName(TEST_USER_NAME);
        user.setPassword(TEST_USER_PASSWORD);
        user.setName(new Name()
                .givenName(OIDCUtilTest.firstName)
                .familyName(OIDCUtilTest.lastName));
        // Email claim (http://wso2.org/claims/emailaddress) – required by email-otp-authenticator.
        user.addEmail(new Email().value(TEST_USER_EMAIL));
        // Mobile claim (http://wso2.org/claims/mobile) – required by sms-otp-authenticator.
        // SCIM2 maps phoneNumbers[type eq "mobile"].value → http://wso2.org/claims/mobile.
        user.addPhoneNumbers(new PhoneNumbers().type("mobile").value(TEST_USER_MOBILE));
        return user;
    }

    /**
     * Resets the resident IDP cache to ensure that any changes to the IDP (e.g. enabling
     * just the SAML SSO federated authenticator) are reflected in the test execution.
     */
    protected void resetResidentIDPCache() throws Exception {

        IdentityProviderMgtServiceClient idpMgtClient =
                new IdentityProviderMgtServiceClient(sessionCookie, soapBackendURL);
        IdentityProvider residentIdp = idpMgtClient.getResidentIdP();

        FederatedAuthenticatorConfig[] configs = residentIdp.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig cfg : configs) {
            if (!cfg.getName().equalsIgnoreCase("samlsso")) {
                configs = (FederatedAuthenticatorConfig[]) ArrayUtils.removeElement(configs, cfg);
            }
        }
        residentIdp.setFederatedAuthenticatorConfigs(configs);
        idpMgtClient.updateResidentIdP(residentIdp);
    }
}

