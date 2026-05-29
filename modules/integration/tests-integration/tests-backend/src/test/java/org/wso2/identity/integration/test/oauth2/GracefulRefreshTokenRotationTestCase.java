/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RefreshTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;

/**
 * Integration tests for the graceful refresh token rotation feature.
 *
 * Exercises four runtime behaviours:
 *   A — happy path: rotate once, then reuse the old RT inside the grace window
 *   B — sibling revocation (child branch): replaying the old RT revokes the previously issued child
 *   C — sibling revocation (parent branch): using the new RT revokes the gracefully-rotated parent
 *   D — reuse-limit enforcement: exceeding the limit returns 400 invalid_grant
 *   E — grace-window expiry: replaying an old RT after the grace window returns 400 invalid_grant
 */
public class GracefulRefreshTokenRotationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String TEST_USER_USERNAME = "gracefulRTUser";
    private static final String TEST_USER_PASSWORD = "Abcd@123";
    private static final String APP_NAME_PREFIX = "GracefulRTApp_";

    private static final String TENANT_DOMAIN = "wso2.com";

    private final String adminUsername;
    private final String adminPassword;
    private final String activeTenant;

    private SCIM2RestClient scim2RestClient;
    private String userId;

    // Scenario A state
    private String appIdA;
    private RefreshToken rt0A;
    private RefreshToken rt1A;
    private AccessToken at1A;

    // Scenario B state
    private String appIdB;
    private RefreshToken rt0B;
    private AccessToken at1B;

    // Scenario C state
    private String appIdC;
    private RefreshToken rt0C;
    private RefreshToken rt1C;
    private AccessToken at2C;

    // Scenario D state
    private String appIdD;
    private RefreshToken rt0D;

    // Scenario E state
    private String appIdE;
    private RefreshToken rt0E;

    @DataProvider
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public GracefulRefreshTokenRotationTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_USERNAME);
        userInfo.setPassword(TEST_USER_PASSWORD);
        userId = scim2RestClient.createUser(userInfo);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        try {
            scim2RestClient.deleteUser(userId);
        } catch (Exception e) {
            Assert.fail("Error while deleting test user: " + e.getMessage(), e);
        }
        scim2RestClient.closeHttpClient();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario A — Happy path: rotate once, reuse old RT inside the grace window
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = "scenarioA",
          description = "Create app for happy-path graceful rotation scenario")
    public void testA1_createApp() throws Exception {

        appIdA = createGracefulApp(APP_NAME_PREFIX + "A", 30, 5);
        Assert.assertNotNull(appIdA, "Application ID must not be null");
    }

    @Test(groups = "scenarioA", dependsOnMethods = "testA1_createApp",
          description = "Obtain initial access and refresh token via password grant")
    public void testA2_initialPasswordGrant() throws Exception {

        AccessTokenResponse response = passwordGrant(appIdA);
        rt0A = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt0A, "Initial refresh token must not be null");
    }

    @Test(groups = "scenarioA", dependsOnMethods = "testA2_initialPasswordGrant",
          description = "First rotation with RT0 issues new AT1/RT1")
    public void testA3_firstRotationIssuesNewTokens() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdA, rt0A);
        rt1A = response.getTokens().getRefreshToken();
        at1A = response.getTokens().getAccessToken();
        Assert.assertNotNull(rt1A, "Rotated refresh token must not be null");
        Assert.assertNotEquals(rt1A.getValue(), rt0A.getValue(),
                "Rotated RT must differ from the original");
        Assert.assertNotNull(at1A, "New access token must not be null");
    }

    @Test(groups = "scenarioA", dependsOnMethods = "testA3_firstRotationIssuesNewTokens",
          description = "Reusing RT0 inside the grace window succeeds and issues RT2/AT2")
    public void testA4_reuseOldRTInsideGraceWindow() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdA, rt0A);
        AccessToken at2A = response.getTokens().getAccessToken();
        RefreshToken rt2A = response.getTokens().getRefreshToken();
        Assert.assertNotNull(at2A, "AT2 must not be null");
        Assert.assertNotNull(rt2A, "RT2 must not be null");
        Assert.assertTrue(introspectActive(at2A), "AT2 must be active");
    }

    @Test(groups = "scenarioA", dependsOnMethods = "testA4_reuseOldRTInsideGraceWindow",
          description = "Clean up scenario A app")
    public void testA5_cleanup() throws Exception {

        deleteApp(appIdA);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario B — Sibling revocation: replaying old RT revokes the child token
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = "scenarioB",
          description = "Create app for child-revocation scenario")
    public void testB1_createApp() throws Exception {

        appIdB = createGracefulApp(APP_NAME_PREFIX + "B", 30, 5);
        Assert.assertNotNull(appIdB);
    }

    @Test(groups = "scenarioB", dependsOnMethods = "testB1_createApp",
          description = "Obtain initial tokens for scenario B via authorization code grant")
    public void testB2_initialAuthCodeGrant() throws Exception {

        AccessTokenResponse response = authCodeGrant(appIdB);
        rt0B = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt0B);
    }

    @Test(groups = "scenarioB", dependsOnMethods = "testB2_initialAuthCodeGrant",
          description = "First rotation: RT0 → RT1/AT1; AT1 is initially active")
    public void testB3_firstRotation() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdB, rt0B);
        at1B = response.getTokens().getAccessToken();
        Assert.assertNotNull(at1B);
        Assert.assertTrue(introspectActive(at1B), "AT1 must be active before sibling revocation");
    }

    @Test(groups = "scenarioB", dependsOnMethods = "testB3_firstRotation",
          description = "Replaying RT0 revokes the previously issued child (AT1 becomes inactive)")
    public void testB4_replayOldRTRevokesChild() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdB, rt0B);
        AccessToken at2B = response.getTokens().getAccessToken();
        Assert.assertNotNull(at2B, "AT2 must not be null");
        Assert.assertFalse(introspectActive(at1B),
                "AT1 must be revoked after sibling revocation (child branch)");
        Assert.assertTrue(introspectActive(at2B), "AT2 must be active");
    }

    @Test(groups = "scenarioB", dependsOnMethods = "testB4_replayOldRTRevokesChild",
          description = "Clean up scenario B app")
    public void testB5_cleanup() throws Exception {

        deleteApp(appIdB);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario C — Sibling revocation: using the new RT makes old RT invalid
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = "scenarioC",
          description = "Create app for parent-revocation scenario")
    public void testC1_createApp() throws Exception {

        appIdC = createGracefulApp(APP_NAME_PREFIX + "C", 30, 5);
        Assert.assertNotNull(appIdC);
    }

    @Test(groups = "scenarioC", dependsOnMethods = "testC1_createApp",
          description = "Obtain initial tokens for scenario C via authorization code grant")
    public void testC2_initialAuthCodeGrant() throws Exception {

        AccessTokenResponse response = authCodeGrant(appIdC);
        rt0C = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt0C);
    }

    @Test(groups = "scenarioC", dependsOnMethods = "testC2_initialAuthCodeGrant",
          description = "First rotation: RT0 → RT1/AT1")
    public void testC3_firstRotation() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdC, rt0C);
        rt1C = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt1C, "RT1 must not be null");
    }

    @Test(groups = "scenarioC", dependsOnMethods = "testC3_firstRotation",
          description = "Using the new RT1 revokes the GRACEFULLY_ROTATED parent (RT0 no longer works)")
    public void testC4_useNewRTRevokesParent() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdC, rt1C);
        at2C = response.getTokens().getAccessToken();
        Assert.assertNotNull(at2C, "AT2 must not be null");
        Assert.assertTrue(introspectActive(at2C), "AT2 must be active");

        // RT0 should now be revoked — refresh attempt must return 400 invalid_grant.
        HTTPResponse raw = refreshGrantRaw(appIdC, rt0C);
        Assert.assertEquals(raw.getStatusCode(), 400,
                "Replaying RT0 after parent revocation must return 400");
        Assert.assertTrue(raw.getContent().contains("invalid_grant"),
                "Error must be invalid_grant");
    }

    @Test(groups = "scenarioC", dependsOnMethods = "testC4_useNewRTRevokesParent",
          description = "Clean up scenario C app")
    public void testC5_cleanup() throws Exception {

        deleteApp(appIdC);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario D — Reuse-limit enforcement
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = "scenarioD",
          description = "Create app with reuseLimit=2 for reuse-limit scenario")
    public void testD1_createApp() throws Exception {

        appIdD = createGracefulApp(APP_NAME_PREFIX + "D", 60, 2);
        Assert.assertNotNull(appIdD);
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD1_createApp",
          description = "Obtain initial tokens for scenario D via authorization code grant")
    public void testD2_initialAuthCodeGrant() throws Exception {

        AccessTokenResponse response = authCodeGrant(appIdD);
        rt0D = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt0D);
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD2_initialAuthCodeGrant",
          description = "First reuse of RT0 succeeds (counter → 0)")
    public void testD3_replay1Succeeds() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdD, rt0D);
        Assert.assertNotNull(response.getTokens().getRefreshToken(),
                "Refresh grant  must return a new refresh token");
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD3_replay1Succeeds",
          description = "First reuse of RT0 succeeds (counter → 1)")
    public void testD4_replay2Succeeds() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdD, rt0D);
        Assert.assertNotNull(response.getTokens().getRefreshToken(),
                "Replay 1 must return a new refresh token");
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD4_replay2Succeeds",
            description = "Second reuse of RT0 succeeds (counter → 2)")
    public void testD4_replay3Succeeds() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdD, rt0D);
        Assert.assertNotNull(response.getTokens().getRefreshToken(),
                "Replay 2 must return a new refresh token");
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD4_replay3Succeeds",
          description = "Third reuse of RT0 exceeds reuseLimit=2 and returns 400 invalid_grant")
    public void testD5_replay2ExceedsLimit() throws Exception {

        HTTPResponse raw = refreshGrantRaw(appIdD, rt0D);
        Assert.assertEquals(raw.getStatusCode(), 400,
                "Third replay must return 400 when reuse limit is exceeded");
        String body = raw.getContent();
        Assert.assertTrue(body.contains("invalid_grant"),
                "Error must be invalid_grant. Actual body: " + body);
        Assert.assertTrue(body.toLowerCase().contains("graceful reuse limit"),
                "Error description must mention graceful reuse limit. Actual body: " + body);
    }

    @Test(groups = "scenarioD", dependsOnMethods = "testD5_replay2ExceedsLimit",
          description = "Clean up scenario D app")
    public void testD6_cleanup() throws Exception {

        deleteApp(appIdD);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario E — Grace window expiry
    // ─────────────────────────────────────────────────────────────────────────

    @Test(groups = "scenarioE",
          description = "Create app with validity=3s for grace-expiry scenario")
    public void testE1_createApp() throws Exception {

        appIdE = createGracefulApp(APP_NAME_PREFIX + "E", 3, 5);
        Assert.assertNotNull(appIdE);
    }

    @Test(groups = "scenarioE", dependsOnMethods = "testE1_createApp",
          description = "Obtain initial tokens for scenario E")
    public void testE2_initialPasswordGrant() throws Exception {

        AccessTokenResponse response = passwordGrant(appIdE);
        rt0E = response.getTokens().getRefreshToken();
        Assert.assertNotNull(rt0E);
    }

    @Test(groups = "scenarioE", dependsOnMethods = "testE2_initialPasswordGrant",
          description = "Rotate once with RT0 → RT1 to start the grace clock")
    public void testE3_firstRotation() throws Exception {

        AccessTokenResponse response = refreshGrant(appIdE, rt0E);
        Assert.assertNotNull(response.getTokens().getRefreshToken(),
                "First rotation must succeed");
    }

    @Test(groups = "scenarioE", dependsOnMethods = "testE3_firstRotation",
          description = "Replaying RT0 after the 3s grace window has expired returns 400 invalid_grant")
    public void testE4_replayAfterGraceExpiry() throws Exception {

        // Sleep past the 3 s grace window configured for this app.
        Thread.sleep(5000);

        HTTPResponse raw = refreshGrantRaw(appIdE, rt0E);
        Assert.assertEquals(raw.getStatusCode(), 400,
                "Replay after grace expiry must return 400");
        String body = raw.getContent();
        Assert.assertTrue(body.contains("invalid_grant"),
                "Error must be invalid_grant. Actual body: " + body);
        Assert.assertTrue(body.toLowerCase().contains("grace period has expired"),
                "Error description must mention grace period expiry. Actual body: " + body);
    }

    @Test(groups = "scenarioE", dependsOnMethods = "testE4_replayAfterGraceExpiry",
          description = "Clean up scenario E app")
    public void testE5_cleanup() throws Exception {

        deleteApp(appIdE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String createGracefulApp(String appName, int validitySec, int reuseLimit) throws Exception {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "password", "refresh_token");

        RefreshTokenConfiguration refreshConfig = new RefreshTokenConfiguration()
                .expiryInSeconds(3600L)
                .renewRefreshToken(true)
                .gracefulRefreshTokenRotationEnabled(true)
                .gracefulRefreshTokenRotationValidityPeriod(validitySec)
                .gracefulRefreshTokenReuseLimit(reuseLimit);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("Default");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));
        oidcConfig.setAccessToken(accessTokenConfig);
        oidcConfig.setRefreshToken(refreshConfig);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);
        application.setInboundProtocolConfiguration(inboundProtocols);

        return addApplication(application);
    }

    private AccessTokenResponse authCodeGrant(String appId) throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        String clientId = oidcConfig.getClientId();
        String clientSecret = oidcConfig.getClientSecret();
        String tokenEndpoint = getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
        String authorizeEndpoint = getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain());

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        CookieStore cookieStore = new BasicCookieStore();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            // Step 1: POST to /authorize to get the login page.
            List<NameValuePair> authzParams = new ArrayList<>();
            authzParams.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
            authzParams.add(new BasicNameValuePair("client_id", clientId));
            authzParams.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
            authzParams.add(new BasicNameValuePair("scope", "openid"));

            HttpResponse authzResponse = sendPostRequestWithParameters(httpClient, authzParams, authorizeEndpoint);
            Header locationHeader = authzResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Authorization request did not return a Location header");
            EntityUtils.consume(authzResponse.getEntity());

            // Step 2: Follow the redirect to the login page, extract sessionDataKey.
            HttpResponse loginPageResponse = sendGetRequest(httpClient, locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>();
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(loginPageResponse, keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey not found in login page");
            String sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "sessionDataKey must not be null");
            EntityUtils.consume(loginPageResponse.getEntity());

            // Step 3: Submit login credentials.
            HttpResponse loginResponse = sendLoginPostForCustomUsers(
                    httpClient, sessionDataKey, TEST_USER_USERNAME, TEST_USER_PASSWORD);
            locationHeader = loginResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Login did not return a Location header");
            EntityUtils.consume(loginResponse.getEntity());

            // Step 4: Follow the post-login redirect; handle consent if prompted.
            HttpResponse postLoginResponse = sendGetRequest(httpClient, locationHeader.getValue());
            locationHeader = postLoginResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

            if (locationHeader == null) {
                // Consent page rendered as HTML — extract sessionDataKeyConsent from the body.
                Map<String, Integer> consentKeyMap = new HashMap<>();
                consentKeyMap.put("name=\"sessionDataKeyConsent\"", 1);
                List<KeyValue> consentKeyValues =
                        DataExtractUtil.extractDataFromResponse(postLoginResponse, consentKeyMap);
                EntityUtils.consume(postLoginResponse.getEntity());
                String sessionDataKeyConsent = consentKeyValues.get(0).getValue();

                HttpResponse consentResponse = sendApprovalPost(httpClient, sessionDataKeyConsent);
                locationHeader = consentResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
                Assert.assertNotNull(locationHeader, "Consent approval did not return a Location header");
                EntityUtils.consume(consentResponse.getEntity());

                HttpResponse afterConsentResponse = sendGetRequest(httpClient, locationHeader.getValue());
                locationHeader = afterConsentResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
                Assert.assertNotNull(locationHeader, "Post-consent redirect did not contain a Location header");
                EntityUtils.consume(afterConsentResponse.getEntity());
            } else {
                EntityUtils.consume(postLoginResponse.getEntity());
            }

            // Step 5: Extract the authorization code from the callback redirect URL.
            String callbackLocation = locationHeader.getValue();
            String authorizationCode = URLEncodedUtils
                    .parse(URI.create(callbackLocation), StandardCharsets.UTF_8)
                    .stream()
                    .filter(p -> "code".equals(p.getName()))
                    .map(NameValuePair::getValue)
                    .findFirst()
                    .orElse(null);
            Assert.assertNotNull(authorizationCode, "Authorization code not found in callback URL: " + callbackLocation);

            // Step 6: Exchange the code for tokens.
            List<NameValuePair> tokenParams = new ArrayList<>();
            tokenParams.add(new BasicNameValuePair("code", authorizationCode));
            tokenParams.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
            tokenParams.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
            tokenParams.add(new BasicNameValuePair("client_id", clientId));
            tokenParams.add(new BasicNameValuePair("scope", "openid"));

            List<org.apache.http.Header> headers = new ArrayList<>();
            headers.add(new org.apache.http.message.BasicHeader(OAuth2Constant.AUTHORIZATION_HEADER,
                    OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
            headers.add(new org.apache.http.message.BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
            headers.add(new org.apache.http.message.BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

            HttpResponse tokenResponse = sendPostRequest(httpClient, headers, tokenParams, tokenEndpoint);
            Assert.assertEquals(tokenResponse.getStatusLine().getStatusCode(), 200,
                    "Token exchange must return 200");
            String responseBody = EntityUtils.toString(tokenResponse.getEntity(), "UTF-8");

            return AccessTokenResponse.parse(JSONObjectUtils.parse(responseBody));
        }
    }

    private AccessTokenResponse passwordGrant(String appId)
            throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        ClientAuthentication clientAuth = new ClientSecretBasic(
                new ClientID(oidcConfig.getClientId()), new Secret(oidcConfig.getClientSecret()));
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        ResourceOwnerPasswordCredentialsGrant grant =
                new ResourceOwnerPasswordCredentialsGrant(TEST_USER_USERNAME, new Secret(TEST_USER_PASSWORD));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, grant, new Scope("openid"));
        HTTPResponse httpResponse = request.toHTTPRequest().send();

        Assert.assertEquals(httpResponse.getStatusCode(), 200,
                "Password grant must return 200. Body: " + httpResponse.getContent());
        return AccessTokenResponse.parse(httpResponse);
    }

    private HTTPResponse refreshGrantRaw(String appId, RefreshToken refreshToken)
            throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        ClientAuthentication clientAuth = new ClientSecretBasic(
                new ClientID(oidcConfig.getClientId()), new Secret(oidcConfig.getClientSecret()));
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth,
                new RefreshTokenGrant(refreshToken), new Scope("openid"));
        return request.toHTTPRequest().send();
    }

    private AccessTokenResponse refreshGrant(String appId, RefreshToken refreshToken)
            throws Exception {

        HTTPResponse httpResponse = refreshGrantRaw(appId, refreshToken);
        Assert.assertEquals(httpResponse.getStatusCode(), 200,
                "Refresh grant must return 200. Body: " + httpResponse.getContent());
        return AccessTokenResponse.parse(httpResponse);
    }

    private boolean introspectActive(AccessToken accessToken)
            throws URISyntaxException, IOException, ParseException {

        URI introSpecEndpoint;
        String authUsername;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
            authUsername = adminUsername + "@" + TENANT_DOMAIN;
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
            authUsername = adminUsername;
        }

        HTTPRequest httpRequest = new TokenIntrospectionRequest(
                introSpecEndpoint,
                new ClientSecretBasic(new ClientID(authUsername), new Secret(adminPassword)),
                accessToken).toHTTPRequest();
        httpRequest.setAuthorization("Basic " + getBase64EncodedString(authUsername, adminPassword));

        HTTPResponse introspectionHTTPResp = httpRequest.send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection HTTP response is null");
        TokenIntrospectionResponse response = TokenIntrospectionResponse.parse(introspectionHTTPResp);
        Assert.assertTrue(response.indicatesSuccess(),
                "Introspection request must succeed. Body: " + introspectionHTTPResp.getContent());
        return response.toSuccessResponse().isActive();
    }
}
