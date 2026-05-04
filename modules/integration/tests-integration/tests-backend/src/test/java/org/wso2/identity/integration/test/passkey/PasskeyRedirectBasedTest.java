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

package org.wso2.identity.integration.test.passkey;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redirect-based integration tests for the passkey authentication flow.
 */
public class PasskeyRedirectBasedTest extends PasskeyTestBase {

    private static final String APP_NAME = "PasskeyRedirectTestApp";
    private static final String CALLBACK_URL = "https://example.com/oidc-callback";
    private static final int MAX_REDIRECTS = 10;

    private static final String TEST_USERNAME = "passkey_test_user";
    private static final String TEST_PASSWORD = "Passkey@Test123";
    private static final String TEST_EMAIL = "passkey_test_user@wso2.com";

    private static final String SECOND_TEST_USERNAME = "passkey_test_user2";
    private static final String SECOND_TEST_PASSWORD = "Passkey@Test456";
    private static final String SECOND_TEST_EMAIL = "passkey_test_user2@wso2.com";

    private String appId;
    private String testUserId;
    private String secondTestUserId;
    private SCIM2RestClient scim2RestClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PasskeyRedirectBasedTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        restClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        testUserId = createTestUser(scim2RestClient, TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        secondTestUserId = createTestUser(scim2RestClient, SECOND_TEST_USERNAME, SECOND_TEST_PASSWORD,
                SECOND_TEST_EMAIL);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        if (appId != null) {
            deleteApp(appId);
        }
        if (testUserId != null) {
            deleteTestUser(scim2RestClient, testUserId);
        }
        if (secondTestUserId != null) {
            deleteTestUser(scim2RestClient, secondTestUserId);
        }
        setPasskeyProgressiveEnrollmentEnabled(false);
        setUsernameLessAuthenticationEnabled(true);
        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        clearAllCredentials();
    }

    @Test(description = "Verify app creation with passkey as first factor and progressive enrollment enabled.")
    public void testCreateAppWithPasskeyProgressiveEnrollment() throws Exception {

        setPasskeyProgressiveEnrollmentEnabled(true);
        Assert.assertTrue(isPasskeyProgressiveEnrollmentEnabled(),
                "Passkey progressive enrollment should be enabled.");

        setUsernameLessAuthenticationEnabled(true);
        Assert.assertTrue(isUsernameLessAuthenticationEnabled(),
                "Passkey username-less authentication should be enabled.");

        appId = addOIDCAppWithPasskeyProgressiveEnrollment(APP_NAME, CALLBACK_URL);
        Assert.assertNotNull(appId, "Application ID should not be null after creation.");
    }

    @Test(description = "Verify passkey registration via the created app with progressive enrollment.",
            dependsOnMethods = "testCreateAppWithPasskeyProgressiveEnrollment")
    public void testPasskeyRegistrationWithProgressiveEnrollment() throws Exception {

        String clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        String commonAuthUrl = getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain());
        String origin = serverURL.replaceAll("/$", "");
        String userName = TEST_USERNAME;

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            // Step 1: Initiate the authorization code flow and follow redirects to the login page.
            String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                    + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + CALLBACK_URL
                    + "&scope=openid";
            HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
            response = followRedirectsUntilLoginPage(httpClient, response);

            // Step 2: Extract sessionDataKey from the login page.
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null.");
            EntityUtils.consume(response.getEntity());

            // Step 3: Select the FIDOAuthenticator from the multi-option login page so that the
            // server routes subsequent requests to it rather than to BasicAuthenticator.
            List<NameValuePair> selectPasskeyParams = new ArrayList<>();
            selectPasskeyParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            selectPasskeyParams.add(new BasicNameValuePair("authenticator", "FIDOAuthenticator"));
            selectPasskeyParams.add(new BasicNameValuePair("idp", "LOCAL"));
            response = sendPostRequestWithParameters(httpClient, selectPasskeyParams, commonAuthUrl);
            Header fidoPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (fidoPageHeader != null) {
                response = sendGetRequest(httpClient, fidoPageHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 4: POST INIT_FIDO_ENROLL to trigger the passkey progressive enrollment flow.
            List<NameValuePair> initEnrollParams = new ArrayList<>();
            initEnrollParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            initEnrollParams.add(new BasicNameValuePair("tokenResponse", "tmp val"));
            initEnrollParams.add(new BasicNameValuePair("scenario", "INIT_FIDO_ENROLL"));
            response = sendPostRequestWithParameters(httpClient, initEnrollParams, commonAuthUrl);

            // Follow the redirect back to the basic auth page to maintain the session context.
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (locationHeader != null) {
                response = sendGetRequest(httpClient, locationHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 5: POST user credentials to identify the user for enrollment.
            List<NameValuePair> credentialParams = new ArrayList<>();
            credentialParams.add(new BasicNameValuePair("usernameUserInput", userName));
            credentialParams.add(new BasicNameValuePair("username", userName));
            credentialParams.add(new BasicNameValuePair("password", TEST_PASSWORD));
            credentialParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            response = sendPostRequestWithParameters(httpClient, credentialParams, commonAuthUrl);

            // Step 6: Extract the WebAuthn creation options from the redirect to fido2-enroll.jsp.
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader,
                    "Expected a redirect to fido2-enroll.jsp from the authorize endpoint.");
            String enrollJspUrl = locationHeader.getValue();
            EntityUtils.consume(response.getEntity());

            JSONObject creationData = extractCreationOptionsFromUrl(enrollJspUrl);
            String requestId = (String) creationData.get("requestId");
            JSONObject pkOptions = (JSONObject) creationData.get("publicKeyCredentialCreationOptions");
            String challenge = (String) pkOptions.get("challenge");
            String rpId = (String) ((JSONObject) pkOptions.get("rp")).get("id");
            byte[] userHandle = base64UrlDecode((String) ((JSONObject) pkOptions.get("user")).get("id"));

            // Step 7: Perform the WebAuthn registration ceremony using the virtual authenticator.
            String challengeResponse = registerPasskey(requestId, challenge, rpId, origin, userName, userHandle);

            // Step 8: POST FINISH_FIDO_ENROLL with the registration credential to complete enrollment.
            List<NameValuePair> finishEnrollParams = new ArrayList<>();
            finishEnrollParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            finishEnrollParams.add(new BasicNameValuePair("challengeResponse", challengeResponse));
            finishEnrollParams.add(new BasicNameValuePair("scenario", "FINISH_FIDO_ENROLL"));
            finishEnrollParams.add(new BasicNameValuePair("displayName", userName));
            response = sendPostRequestWithParameters(httpClient, finishEnrollParams, commonAuthUrl);

            // Step 9: Follow the redirect chain until the callback URL with an authorization code appears.
            String callbackWithCode = followRedirectsToCallback(httpClient, response);
            Assert.assertNotNull(callbackWithCode,
                    "Expected a redirect to the callback URL after successful passkey registration.");
            Assert.assertTrue(callbackWithCode.startsWith(CALLBACK_URL),
                    "Final redirect should target the configured callback URL.");
            Assert.assertTrue(callbackWithCode.contains("code="),
                    "Authorization code should be present in the callback URL.");
        }
    }

    @Test(description = "Verify passkey login with a previously registered passkey credential.",
            dependsOnMethods = "testPasskeyRegistrationWithProgressiveEnrollment")
    public void testPasskeyLoginWithRegisteredCredential() throws Exception {

        String clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        String commonAuthUrl = getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain());
        String origin = serverURL.replaceAll("/$", "");

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            // Step 1: Initiate the authorization code flow and follow redirects to the login page.
            String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                    + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + CALLBACK_URL
                    + "&scope=openid";
            HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
            response = followRedirectsUntilLoginPage(httpClient, response);

            // Step 2: Extract sessionDataKey from the login page.
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null.");
            EntityUtils.consume(response.getEntity());

            // Step 3: Select the FIDOAuthenticator to start the passkey assertion flow.
            List<NameValuePair> selectPasskeyParams = new ArrayList<>();
            selectPasskeyParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            selectPasskeyParams.add(new BasicNameValuePair("authenticator", "FIDOAuthenticator"));
            selectPasskeyParams.add(new BasicNameValuePair("idp", "LOCAL"));
            response = sendPostRequestWithParameters(httpClient, selectPasskeyParams, commonAuthUrl);
            Header fidoAuthPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertNotNull(fidoAuthPageHeader,
                    "Expected a redirect to fido2-auth.jsp after selecting the FIDOAuthenticator.");
            String fidoAuthJspUrl = fidoAuthPageHeader.getValue();

            // Step 4: Parse the WebAuthn request options from the fido2-auth.jsp redirect URL.
            JSONObject requestData = extractRequestOptionsFromUrl(fidoAuthJspUrl);
            String requestId = (String) requestData.get("requestId");
            JSONObject pkOptions = (JSONObject) requestData.get("publicKeyCredentialRequestOptions");
            String challenge = (String) pkOptions.get("challenge");
            String rpId = pkOptions.containsKey("rpId") ? (String) pkOptions.get("rpId") : new URI(serverURL).getHost();

            // Step 5: Perform the WebAuthn authentication ceremony using the stored credential.
            String tokenResponse = authenticatePasskey(requestId, challenge, rpId, origin, TEST_USERNAME);

            // Step 6: POST the assertion response to complete the passkey login.
            List<NameValuePair> finishLoginParams = new ArrayList<>();
            finishLoginParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            finishLoginParams.add(new BasicNameValuePair("tokenResponse", tokenResponse));
            response = sendPostRequestWithParameters(httpClient, finishLoginParams, commonAuthUrl);

            // Step 7: Follow the redirect chain until the callback URL with an authorization code appears.
            String callbackWithCode = followRedirectsToCallback(httpClient, response);
            Assert.assertNotNull(callbackWithCode,
                    "Expected a redirect to the callback URL after successful passkey login.");
            Assert.assertTrue(callbackWithCode.startsWith(CALLBACK_URL),
                    "Final redirect should target the configured callback URL.");
            Assert.assertTrue(callbackWithCode.contains("code="),
                    "Authorization code should be present in the callback URL.");
        }
    }

    @Test(description = "Disable username-less authentication to verify the IDF username step is enforced.",
            dependsOnMethods = "testPasskeyLoginWithRegisteredCredential")
    public void testDisableUsernameLessAuthentication() throws Exception {

        setUsernameLessAuthenticationEnabled(false);
        Assert.assertFalse(isUsernameLessAuthenticationEnabled(),
                "Passkey username-less authentication should be disabled.");
    }

    @Test(description = "Verify passkey login when username-less authentication is disabled: " +
            "an IDF username step must be completed before the WebAuthn assertion.",
            dependsOnMethods = "testDisableUsernameLessAuthentication")
    public void testPasskeyLoginWithUsernameLessDisabled() throws Exception {

        String clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        String commonAuthUrl = getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain());
        String origin = serverURL.replaceAll("/$", "");

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            // Step 1: Initiate the authorization code flow and follow redirects to the login page.
            String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                    + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + CALLBACK_URL
                    + "&scope=openid";
            HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
            response = followRedirectsUntilLoginPage(httpClient, response);

            // Step 2: Extract sessionDataKey from the login page.
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null.");
            EntityUtils.consume(response.getEntity());

            // Step 3: Select the FIDOAuthenticator to start the passkey authentication flow.
            List<NameValuePair> selectPasskeyParams = new ArrayList<>();
            selectPasskeyParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            selectPasskeyParams.add(new BasicNameValuePair("authenticator", "FIDOAuthenticator"));
            selectPasskeyParams.add(new BasicNameValuePair("idp", "LOCAL"));
            response = sendPostRequestWithParameters(httpClient, selectPasskeyParams, commonAuthUrl);
            Header fidoPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (fidoPageHeader != null) {
                response = sendGetRequest(httpClient, fidoPageHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 4: Submit the username via the IDF step. When username-less authentication is disabled
            // the server requires the username before it can issue a WebAuthn challenge.
            List<NameValuePair> idfParams = new ArrayList<>();
            idfParams.add(new BasicNameValuePair("username", TEST_USERNAME));
            idfParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            idfParams.add(new BasicNameValuePair("scenario", "INIT_FIDO_AUTH"));
            idfParams.add(new BasicNameValuePair("authType", "idf"));
            response = sendPostRequestWithParameters(httpClient, idfParams, commonAuthUrl);

            // Step 5: Parse the WebAuthn request options from the fido2-auth.jsp redirect URL.
            Header fidoAuthPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertNotNull(fidoAuthPageHeader,
                    "Expected a redirect to fido2-auth.jsp after submitting the username in IDF step.");
            String fidoAuthJspUrl = fidoAuthPageHeader.getValue();

            JSONObject requestData = extractRequestOptionsFromUrl(fidoAuthJspUrl);
            String requestId = (String) requestData.get("requestId");
            JSONObject pkOptions = (JSONObject) requestData.get("publicKeyCredentialRequestOptions");
            String challenge = (String) pkOptions.get("challenge");
            String rpId = pkOptions.containsKey("rpId")
                    ? (String) pkOptions.get("rpId")
                    : new URI(serverURL).getHost();

            // Step 6: Perform the WebAuthn authentication ceremony using the stored credential.
            String tokenResponse = authenticatePasskey(requestId, challenge, rpId, origin, TEST_USERNAME);

            // Step 7: POST the assertion response to complete the passkey login.
            List<NameValuePair> finishLoginParams = new ArrayList<>();
            finishLoginParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            finishLoginParams.add(new BasicNameValuePair("tokenResponse", tokenResponse));
            response = sendPostRequestWithParameters(httpClient, finishLoginParams, commonAuthUrl);

            // Step 8: Follow the redirect chain until the callback URL with an authorization code appears.
            String callbackWithCode = followRedirectsToCallback(httpClient, response);
            Assert.assertNotNull(callbackWithCode,
                    "Expected a redirect to the callback URL after successful passkey login.");
            Assert.assertTrue(callbackWithCode.startsWith(CALLBACK_URL),
                    "Final redirect should target the configured callback URL.");
            Assert.assertTrue(callbackWithCode.contains("code="),
                    "Authorization code should be present in the callback URL.");
        }
    }

    @Test(description = "Register a passkey for the second test user via progressive enrollment.",
            dependsOnMethods = "testPasskeyLoginWithUsernameLessDisabled")
    public void testPasskeyRegistrationForSecondUser() throws Exception {

        String clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        String commonAuthUrl = getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain());
        String origin = serverURL.replaceAll("/$", "");

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            // Step 1: Initiate the authorization code flow and follow redirects to the login page.
            String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                    + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + CALLBACK_URL
                    + "&scope=openid";
            HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
            response = followRedirectsUntilLoginPage(httpClient, response);

            // Step 2: Extract sessionDataKey from the login page.
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null.");
            EntityUtils.consume(response.getEntity());

            // Step 3: Select the FIDOAuthenticator from the multi-option login page.
            List<NameValuePair> selectPasskeyParams = new ArrayList<>();
            selectPasskeyParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            selectPasskeyParams.add(new BasicNameValuePair("authenticator", "FIDOAuthenticator"));
            selectPasskeyParams.add(new BasicNameValuePair("idp", "LOCAL"));
            response = sendPostRequestWithParameters(httpClient, selectPasskeyParams, commonAuthUrl);
            Header fidoPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (fidoPageHeader != null) {
                response = sendGetRequest(httpClient, fidoPageHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 4: POST INIT_FIDO_ENROLL to trigger the passkey progressive enrollment flow.
            // Since username-less authentication is disabled, the username and authType=idf are required
            // so the server can identify the user before initiating the enrollment ceremony.
            List<NameValuePair> initEnrollParams = new ArrayList<>();
            initEnrollParams.add(new BasicNameValuePair("username", SECOND_TEST_USERNAME));
            initEnrollParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            initEnrollParams.add(new BasicNameValuePair("scenario", "INIT_FIDO_ENROLL"));
            initEnrollParams.add(new BasicNameValuePair("authType", "idf"));
            response = sendPostRequestWithParameters(httpClient, initEnrollParams, commonAuthUrl);
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (locationHeader != null) {
                response = sendGetRequest(httpClient, locationHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 5: POST the second user's credentials to identify them for enrollment.
            List<NameValuePair> credentialParams = new ArrayList<>();
            credentialParams.add(new BasicNameValuePair("username", SECOND_TEST_USERNAME));
            credentialParams.add(new BasicNameValuePair("password", SECOND_TEST_PASSWORD));
            credentialParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            response = sendPostRequestWithParameters(httpClient, credentialParams, commonAuthUrl);

            // Step 6: Extract the WebAuthn creation options from the fido2-enroll.jsp redirect URL.
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader,
                    "Expected a redirect to fido2-enroll.jsp from the authorize endpoint.");
            String enrollJspUrl = locationHeader.getValue();
            EntityUtils.consume(response.getEntity());

            JSONObject creationData = extractCreationOptionsFromUrl(enrollJspUrl);
            String requestId = (String) creationData.get("requestId");
            JSONObject pkOptions = (JSONObject) creationData.get("publicKeyCredentialCreationOptions");
            String challenge = (String) pkOptions.get("challenge");
            String rpId = (String) ((JSONObject) pkOptions.get("rp")).get("id");
            byte[] userHandle = base64UrlDecode((String) ((JSONObject) pkOptions.get("user")).get("id"));

            // Step 7: Perform the WebAuthn registration ceremony for the second user.
            String challengeResponse = registerPasskey(requestId, challenge, rpId, origin,
                    SECOND_TEST_USERNAME, userHandle);

            // Step 8: POST FINISH_FIDO_ENROLL to complete enrollment.
            List<NameValuePair> finishEnrollParams = new ArrayList<>();
            finishEnrollParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            finishEnrollParams.add(new BasicNameValuePair("challengeResponse", challengeResponse));
            finishEnrollParams.add(new BasicNameValuePair("scenario", "FINISH_FIDO_ENROLL"));
            finishEnrollParams.add(new BasicNameValuePair("displayName", SECOND_TEST_USERNAME));
            response = sendPostRequestWithParameters(httpClient, finishEnrollParams, commonAuthUrl);

            // Step 9: Follow the redirect chain to the callback URL and verify enrollment succeeded.
            String callbackWithCode = followRedirectsToCallback(httpClient, response);
            Assert.assertNotNull(callbackWithCode,
                    "Expected a redirect to the callback URL after successful passkey registration.");
            Assert.assertTrue(callbackWithCode.startsWith(CALLBACK_URL),
                    "Final redirect should target the configured callback URL.");
            Assert.assertTrue(callbackWithCode.contains("code="),
                    "Authorization code should be present in the callback URL.");
        }
    }

    @Test(description = "Verify that passkey login fails when the credential presented belongs to a different " +
            "user than the one submitted in the IDF username step.",
            dependsOnMethods = "testPasskeyRegistrationForSecondUser")
    public void testPasskeyLoginWithMismatchedCredential() throws Exception {

        String clientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
        String commonAuthUrl = getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain());
        String origin = serverURL.replaceAll("/$", "");

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            // Step 1: Initiate the authorization code flow and follow redirects to the login page.
            String authorizeUrl = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                    + "?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + CALLBACK_URL
                    + "&scope=openid";
            HttpResponse response = sendGetRequest(httpClient, authorizeUrl);
            response = followRedirectsUntilLoginPage(httpClient, response);

            // Step 2: Extract sessionDataKey from the login page.
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null.");
            EntityUtils.consume(response.getEntity());

            // Step 3: Select the FIDOAuthenticator to start the passkey authentication flow.
            List<NameValuePair> selectPasskeyParams = new ArrayList<>();
            selectPasskeyParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            selectPasskeyParams.add(new BasicNameValuePair("authenticator", "FIDOAuthenticator"));
            selectPasskeyParams.add(new BasicNameValuePair("idp", "LOCAL"));
            response = sendPostRequestWithParameters(httpClient, selectPasskeyParams, commonAuthUrl);
            Header fidoPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            if (fidoPageHeader != null) {
                response = sendGetRequest(httpClient, fidoPageHeader.getValue());
                EntityUtils.consume(response.getEntity());
            }

            // Step 4: Submit the second user's username in the IDF step. The server will issue a WebAuthn
            // challenge scoped to the second user's registered credentials.
            List<NameValuePair> idfParams = new ArrayList<>();
            idfParams.add(new BasicNameValuePair("username", SECOND_TEST_USERNAME));
            idfParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            idfParams.add(new BasicNameValuePair("scenario", "INIT_FIDO_AUTH"));
            idfParams.add(new BasicNameValuePair("authType", "idf"));
            response = sendPostRequestWithParameters(httpClient, idfParams, commonAuthUrl);

            // Step 5: Parse the WebAuthn request options from the fido2-auth.jsp redirect URL.
            Header fidoAuthPageHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertNotNull(fidoAuthPageHeader,
                    "Expected a redirect to fido2-auth.jsp after submitting the username in IDF step.");
            String fidoAuthJspUrl = fidoAuthPageHeader.getValue();

            JSONObject requestData = extractRequestOptionsFromUrl(fidoAuthJspUrl);
            String requestId = (String) requestData.get("requestId");
            JSONObject pkOptions = (JSONObject) requestData.get("publicKeyCredentialRequestOptions");
            String challenge = (String) pkOptions.get("challenge");
            String rpId = pkOptions.containsKey("rpId")
                    ? (String) pkOptions.get("rpId")
                    : new URI(serverURL).getHost();

            // Step 6: Intentionally sign the challenge using the first user's credential. The credential ID
            // is not in the second user's allowCredentials list, so the server must reject the assertion.
            String tokenResponse = authenticatePasskey(requestId, challenge, rpId, origin, TEST_USERNAME);

            // Step 7: POST the mismatched assertion response.
            List<NameValuePair> finishLoginParams = new ArrayList<>();
            finishLoginParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            finishLoginParams.add(new BasicNameValuePair("tokenResponse", tokenResponse));
            response = sendPostRequestWithParameters(httpClient, finishLoginParams, commonAuthUrl);

            // Step 8: Verify that no authorization code is issued — the server must reject the assertion.
            String callbackWithCode = followRedirectsToCallback(httpClient, response);
            Assert.assertNotNull(callbackWithCode,
                    "Expected a redirect to the callback URL with an error response.");
            Assert.assertTrue(callbackWithCode.startsWith(CALLBACK_URL),
                    "Error redirect should target the configured callback URL.");
            Assert.assertTrue(callbackWithCode.contains("error="),
                    "Callback URL should contain an error parameter when login fails.");
            Assert.assertFalse(callbackWithCode.contains("code="),
                    "No authorization code should be issued when the asserted credential belongs " +
                            "to a different user than the one identified in the IDF step.");
        }
    }

    /**
     * Follows HTTP redirects until a non-redirect (2xx) response is received — the login page HTML.
     */
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

    /**
     * Follows GET redirects until a URL starting with the callback URL is encountered, then returns it.
     * Returns null if the callback URL is not reached within the redirect limit.
     */
    private String followRedirectsToCallback(CloseableHttpClient httpClient, HttpResponse response)
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

    /**
     * Parses the WebAuthn creation options JSON from the {@code data} query parameter
     * of the fido2-enroll.jsp redirect URL.
     */
    private JSONObject extractCreationOptionsFromUrl(String url) throws Exception {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if ("data".equals(param.getName())) {
                return (JSONObject) new JSONParser().parse(param.getValue());
            }
        }
        throw new IllegalStateException("'data' parameter not found in fido2-enroll.jsp URL: " + url);
    }

    /**
     * Parses the WebAuthn request options JSON from the {@code data} query parameter
     * of the fido2-auth.jsp redirect URL.
     */
    private JSONObject extractRequestOptionsFromUrl(String url) throws Exception {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if ("data".equals(param.getName())) {
                return (JSONObject) new JSONParser().parse(param.getValue());
            }
        }
        throw new IllegalStateException("'data' parameter not found in fido2-auth.jsp URL: " + url);
    }
}
