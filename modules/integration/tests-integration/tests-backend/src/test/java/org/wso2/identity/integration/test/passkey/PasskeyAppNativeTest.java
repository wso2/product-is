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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * App-native authentication integration tests for passkey flows.
 */
public class PasskeyAppNativeTest extends PasskeyTestBase {

    private static final String APP_NAME = "PasskeyAppNativeTestApp";
    private static final String MFA_APP_NAME = "PasskeyMFAAppNativeTestApp";
    private static final String ENROLL_APP_NAME = "PasskeyEnrollmentApp";
    private static final String CALLBACK_URL = "https://example.com/oidc-callback";

    private static final String FLOW_STATUS_SUCCESS = "SUCCESS_COMPLETED";

    private static final String TEST_USERNAME = "passkey_appnative_user";
    private static final String TEST_PASSWORD = "Passkey@AppNative123";
    private static final String TEST_EMAIL = "passkey_appnative_user@wso2.com";

    private static final String MFA_USERNAME = "passkey_mfa_appnative_user";
    private static final String MFA_PASSWORD = "Passkey@MFAAppNative123";
    private static final String MFA_EMAIL = "passkey_mfa_appnative_user@wso2.com";

    private String appId;
    private String appClientId;
    private String mfaAppId;
    private String mfaClientId;
    private String enrollAppId;
    private String enrollClientId;
    private String enrollClientSecret;
    private String testUserId;
    private String mfaUserId;
    private SCIM2RestClient scim2RestClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PasskeyAppNativeTest(TestUserMode userMode) throws Exception {

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
        mfaUserId = createTestUser(scim2RestClient, MFA_USERNAME, MFA_PASSWORD, MFA_EMAIL);

        // A dedicated app with password grant is used solely to obtain user tokens.
        enrollAppId = addPasswordGrantApp(ENROLL_APP_NAME, CALLBACK_URL);
        OpenIDConnectConfiguration enrollConfig = getOIDCInboundDetailsOfApplication(enrollAppId);
        enrollClientId = enrollConfig.getClientId();
        enrollClientSecret = enrollConfig.getClientSecret();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        if (appId != null) {
            deleteApp(appId);
        }
        if (mfaAppId != null) {
            deleteApp(mfaAppId);
        }
        if (enrollAppId != null) {
            deleteApp(enrollAppId);
        }
        if (testUserId != null) {
            deleteTestUser(scim2RestClient, testUserId);
        }
        if (mfaUserId != null) {
            deleteTestUser(scim2RestClient, mfaUserId);
        }
        setUsernameLessAuthenticationEnabled(true);
        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        clearAllCredentials();
    }

    @Test(description = "Create an OIDC app with passkey as the sole first-factor authenticator.")
    public void testCreatePasskeyFirstFactorApp() throws Exception {

        setUsernameLessAuthenticationEnabled(true);
        Assert.assertTrue(isUsernameLessAuthenticationEnabled(),
                "Username-less authentication should be enabled.");

        appId = addOIDCAppWithPasskeyForAppNative(APP_NAME, CALLBACK_URL);
        Assert.assertNotNull(appId, "Application ID should not be null after creation.");
        appClientId = getOIDCInboundDetailsOfApplication(appId).getClientId();
    }

    @Test(description = "Pre-enroll a passkey for the test user via the My Account WebAuthn API.",
            dependsOnMethods = "testCreatePasskeyFirstFactorApp")
    public void testEnrollPasskeyViaMyAccount() throws Exception {

        enrollPasskeyForUser(TEST_USERNAME, TEST_PASSWORD, enrollClientId, enrollClientSecret);
    }

    @Test(description = "Verify passkey login (usernameless) via app-native authentication.",
            dependsOnMethods = "testEnrollPasskeyViaMyAccount")
    public void testPasskeyLoginViaAppNative() throws Exception {

        String origin = serverURL.replaceAll("/$", "");
        String rpId = new URI(serverURL).getHost();

        // Step 1: Initiate app-native authorization — get flowId and the passkey authenticator info.
        AuthnState state = initiateAppNativeAuthorization(appClientId);

        // Step 2: Run the WebAuthn assertion ceremony and exchange the result for an auth code.
        String code = performPasskeyAssertion(state, rpId, origin, TEST_USERNAME);
        Assert.assertNotNull(code,
                "Authorization code should be present after successful passkey login.");
    }

    @Test(description = "Create an OIDC app with passkey configured as the second authentication " +
            "factor (BasicAuthenticator → FIDOAuthenticator).",
            dependsOnMethods = "testPasskeyLoginViaAppNative")
    public void testCreateMFAPasskeyApp() throws Exception {

        mfaAppId = addOIDCAppWithMFAPasskeyForAppNative(MFA_APP_NAME, CALLBACK_URL);
        Assert.assertNotNull(mfaAppId, "MFA application ID should not be null after creation.");
        mfaClientId = getOIDCInboundDetailsOfApplication(mfaAppId).getClientId();
    }

    @Test(description = "Pre-enroll a passkey for the MFA test user via the My Account WebAuthn API.",
            dependsOnMethods = "testCreateMFAPasskeyApp")
    public void testEnrollMFAPasskeyViaMyAccount() throws Exception {

        enrollPasskeyForUser(MFA_USERNAME, MFA_PASSWORD, enrollClientId, enrollClientSecret);
    }

    @Test(description = "Verify MFA passkey login via app-native authentication: the user first " +
            "completes BasicAuthenticator and then authenticates with a passkey.",
            dependsOnMethods = "testEnrollMFAPasskeyViaMyAccount")
    public void testMFAPasskeyLoginViaAppNative() throws Exception {

        String origin = serverURL.replaceAll("/$", "");
        String rpId = new URI(serverURL).getHost();

        // Step 1: Initiate app-native authorization — first step is BasicAuthenticator.
        AuthnState state = initiateAppNativeAuthorization(mfaClientId);

        // Step 2: Complete the basic authentication step with username and password.
        // The response contains the next step's authenticator (FIDOAuthenticator).
        state = completeBasicAuth(state.flowId, state.authenticatorId, MFA_USERNAME, MFA_PASSWORD);

        // Step 3: Run the WebAuthn assertion ceremony and exchange the result for an auth code.
        String code = performPasskeyAssertion(state, rpId, origin, MFA_USERNAME);
        Assert.assertNotNull(code,
                "Authorization code should be present after successful MFA passkey login.");
    }

    @Test(description = "Verify that MFA passkey login fails when the passkey credential presented " +
            "belongs to a different user than the one who completed the basic authentication step.",
            dependsOnMethods = "testMFAPasskeyLoginViaAppNative")
    public void testMFAPasskeyLoginWithWrongCredentialViaAppNative() throws Exception {

        String origin = serverURL.replaceAll("/$", "");
        String rpId = new URI(serverURL).getHost();

        // Step 1: Initiate app-native authorization — first step is BasicAuthenticator.
        AuthnState state = initiateAppNativeAuthorization(mfaClientId);

        // Step 2: Complete the basic authentication step with MFA_USERNAME credentials.
        state = completeBasicAuth(state.flowId, state.authenticatorId, MFA_USERNAME, MFA_PASSWORD);

        // Step 3: Decode the challenge and extract request options.
        byte[] challengeBytes = base64UrlDecode(state.challengeData);
        JSONObject challengeJson = (JSONObject) new JSONParser()
                .parse(new String(challengeBytes, StandardCharsets.UTF_8));

        String requestId = (String) challengeJson.get("requestId");
        JSONObject pkOptions = (JSONObject) challengeJson.get("publicKeyCredentialRequestOptions");
        String challenge = (String) pkOptions.get("challenge");
        String resolvedRpId = pkOptions.containsKey("rpId") ? (String) pkOptions.get("rpId") : rpId;

        // Step 4: Intentionally sign with TEST_USERNAME's credential. The credential ID is not in
        // MFA_USERNAME's allowCredentials list, so the server must reject the assertion.
        String tokenResponseJson = authenticatePasskey(requestId, challenge, resolvedRpId, origin, TEST_USERNAME);
        String encodedTokenResponse = Base64.getEncoder()
                .encodeToString(tokenResponseJson.getBytes(StandardCharsets.UTF_8));

        String body = "{\"flowId\":\"" + state.flowId + "\","
                + "\"selectedAuthenticator\":{"
                + "\"authenticatorId\":\"" + state.authenticatorId + "\","
                + "\"params\":{\"tokenResponse\":\"" + encodedTokenResponse + "\"}}}";

        // Step 5: POST the mismatched assertion and verify the server rejects it.
        JSONObject response = postToAuthn(body);
        String flowStatus = (String) response.get("flowStatus");
        Assert.assertNotEquals(flowStatus, FLOW_STATUS_SUCCESS,
                "Flow should not succeed when the passkey credential belongs to a different user " +
                        "than the one who completed the basic authentication step.");
    }

    /**
     * POSTs to {@code /oauth2/authorize} with {@code response_mode=direct} to start an app-native
     * flow. Returns the initial {@link AuthnState} containing the flowId, authenticatorId of the
     * first authenticator in the response, and optionally the passkey {@code challengeData} if the
     * server already included it (single-option {@code AUTHENTICATOR_PROMPT} step).
     */
    private AuthnState initiateAppNativeAuthorization(String clientId) throws Exception {

        String authorizeUrl = getTenantQualifiedURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain());

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
            Assert.assertNotNull(nextStep,
                    "Expected 'nextStep' in authorize response but got: " + body);
            JSONArray authenticators = (JSONArray) nextStep.get("authenticators");
            JSONObject authenticator = (JSONObject) authenticators.get(0);
            String authenticatorId = (String) authenticator.get("authenticatorId");
            String challengeData = extractChallengeData(authenticator);

            return new AuthnState(flowId, authenticatorId, challengeData);
        }
    }

    /**
     * POSTs username and password to {@code /oauth2/authn} to complete the BasicAuthenticator step
     * in an MFA flow. Returns an {@link AuthnState} representing the next step (FIDOAuthenticator),
     * including the {@code challengeData} if the server already provided it.
     */
    private AuthnState completeBasicAuth(String flowId, String authenticatorId,
            String username, String password) throws Exception {

        String body = "{\"flowId\":\"" + flowId + "\","
                + "\"selectedAuthenticator\":{"
                + "\"authenticatorId\":\"" + authenticatorId + "\","
                + "\"params\":{\"username\":\"" + username + "\","
                + "\"password\":\"" + password + "\"}}}";
        JSONObject response = postToAuthn(body);

        return extractAuthnState(flowId, response);
    }

    /**
     * Decodes the {@code challengeData} from the server, runs the virtual WebAuthn assertion
     * ceremony via {@link #authenticatePasskey}, base64-encodes the result, and POSTs it to
     * {@code /oauth2/authn} as the {@code tokenResponse}. Returns the authorization code from the
     * {@code SUCCESS_COMPLETED} response.
     */
    private String performPasskeyAssertion(AuthnState state, String rpId, String origin,
            String username) throws Exception {

        byte[] challengeBytes = base64UrlDecode(state.challengeData);
        JSONObject challengeJson = (JSONObject) new JSONParser()
                .parse(new String(challengeBytes, StandardCharsets.UTF_8));

        String requestId = (String) challengeJson.get("requestId");
        JSONObject pkOptions = (JSONObject) challengeJson.get("publicKeyCredentialRequestOptions");
        String challenge = (String) pkOptions.get("challenge");
        String resolvedRpId = pkOptions.containsKey("rpId") ? (String) pkOptions.get("rpId") : rpId;

        String tokenResponseJson = authenticatePasskey(requestId, challenge, resolvedRpId, origin, username);
        String encodedTokenResponse = Base64.getEncoder()
                .encodeToString(tokenResponseJson.getBytes(StandardCharsets.UTF_8));

        String body = "{\"flowId\":\"" + state.flowId + "\","
                + "\"selectedAuthenticator\":{"
                + "\"authenticatorId\":\"" + state.authenticatorId + "\","
                + "\"params\":{\"tokenResponse\":\"" + encodedTokenResponse + "\"}}}";

        JSONObject response = postToAuthn(body);

        String flowStatus = (String) response.get("flowStatus");
        Assert.assertEquals(flowStatus, FLOW_STATUS_SUCCESS,
                "Flow status should be SUCCESS_COMPLETED after passkey assertion.");

        JSONObject authData = (JSONObject) response.get("authData");
        Assert.assertNotNull(authData, "authData must be present in the success response.");
        return (String) authData.get("code");
    }

    /**
     * Makes a JSON POST to the tenant-qualified {@code /oauth2/authn/} endpoint.
     */
    private JSONObject postToAuthn(String jsonBody) throws Exception {

        String authnUrl = getTenantQualifiedURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain())
                .replaceAll("oauth2/authorize/?$", "oauth2/authn/");

        try (CloseableHttpClient client = createTrustAllHttpClient()) {
            HttpPost post = new HttpPost(authnUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            HttpResponse response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity());
            return (JSONObject) new JSONParser().parse(body);
        }
    }

    /**
     * Extracts the authenticatorId and challengeData from the {@code nextStep.authenticators[0]}
     * of an {@code /authn} response.
     */
    private AuthnState extractAuthnState(String flowId, JSONObject authnResponse) {

        JSONObject nextStep = (JSONObject) authnResponse.get("nextStep");
        Assert.assertNotNull(nextStep,
                "Expected 'nextStep' in authn response but got: " + authnResponse.toJSONString());
        JSONArray authenticators = (JSONArray) nextStep.get("authenticators");
        JSONObject authenticator = (JSONObject) authenticators.get(0);
        String authenticatorId = (String) authenticator.get("authenticatorId");
        String challengeData = extractChallengeData(authenticator);
        return new AuthnState(flowId, authenticatorId, challengeData);
    }

    /**
     * Returns the {@code challengeData} string from {@code authenticator.metadata.additionalData},
     * or {@code null} if the metadata or the field is absent.
     */
    private String extractChallengeData(JSONObject authenticator) {

        JSONObject metadata = (JSONObject) authenticator.get("metadata");
        if (metadata == null) {
            return null;
        }
        JSONObject additionalData = (JSONObject) metadata.get("additionalData");
        if (additionalData == null) {
            return null;
        }
        return (String) additionalData.get("challengeData");
    }

    /**
     * Holds the mutable state carried across steps of an app-native authentication flow.
     */
    private static class AuthnState {

        final String flowId;
        final String authenticatorId;
        final String challengeData;

        AuthnState(String flowId, String authenticatorId, String challengeData) {

            this.flowId = flowId;
            this.authenticatorId = authenticatorId;
            this.challengeData = challengeData;
        }
    }
}
