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

package org.wso2.identity.integration.test.oid4vp;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Claims;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

/**
 * Shared constants, fields and helpers for the VP Authenticator flow tests.
 * Holds no {@code @Test} methods and no shared {@code @BeforeClass}/{@code @AfterClass} —
 * each subclass is responsible for its own setup and teardown.
 */
public abstract class VPAuthenticatorFlowTestBase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String CALLBACK_URL             = "https://localhost/callback";
    protected static final String CREDENTIAL_IDENTIFIER    = "PersonCredential";
    protected static final String ISSUER_URL               = "https://test-issuer.example.com";
    protected static final String PRESENTATION_DEFINITIONS_PATH = "/api/server/v1/presentation-definitions";
    protected static final String COMMONAUTH_PATH          = "commonauth";
    protected static final String WALLET_LOGIN_JSP         = "wallet_login.jsp";

    protected static final int    POLL_MAX_RETRIES         = 15;
    protected static final long   POLL_INTERVAL_MS         = 500L;

    protected ECKey issuerSigningKey;

    protected String presentationDefinitionId;
    protected String appId;
    protected String idpId;
    protected String vpIdpName;
    protected VPSessionRestClient vpSessionRestClient;
    protected IdpMgtRestClient idpMgtRestClient;

    /** Creates the presentation definition from the test resource JSON and returns its server-assigned ID. */
    protected String createPresentationDefinition() throws Exception {

        String json = VPTestUtils.readResourceAsString(
                "org/wso2/identity/integration/test/oid4vp/create-vp-presentation-definition-flow.json");
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH;
        Response response = adminPost(url, json);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK,
                "Failed to create presentation definition (status " + status + "): " + body);
        JSONObject jsonResponse = new JSONObject(body);
        return jsonResponse.getString("id");
    }

    /** Deletes the presentation definition with the given ID; no-op if {@code defId} is null. */
    protected void deletePresentationDefinition(String defId) {

        if (defId == null) {
            return;
        }
        adminDelete(serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + defId);
    }

    /**
     * Create a federated IDP configured with the PresentationExchangeAuthenticator.
     * Returns the server-assigned IDP ID.
     */
    protected String createVPAuthenticatorIDP() throws Exception {

        // Build the IDP JSON payload.
        String generatedIdpName = "VPTestIDP-" + UUID.randomUUID();
        String idpJson = "{"
                + "\"name\": \"" + generatedIdpName + "\","
                + "\"description\": \"VP Authenticator IDP for integration tests\","
                + "\"federatedAuthenticators\": {"
                + "  \"defaultAuthenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "  \"authenticators\": [{"
                + "    \"authenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "    \"isEnabled\": true,"
                + "    \"properties\": [{"
                + "      \"key\": \"presentationDefinitionId\","
                + "      \"value\": \"" + presentationDefinitionId + "\""
                + "    }]"
                + "  }]"
                + "},"
                + "\"provisioning\": {"
                + "  \"jit\": {"
                + "    \"isEnabled\": true,"
                + "    \"userstore\": \"PRIMARY\","
                + "    \"scheme\": \"PROVISION_SILENTLY\""
                + "  }"
                + "}"
                + "}";

        String url = serverURL + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpJson);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        if (status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK) {
            JSONObject jsonResponse = new JSONObject(body);
            String createdIdpId = jsonResponse.getString("id");
            vpIdpName = generatedIdpName;
            Claims.ClaimMapping givenNameMapping = new Claims.ClaimMapping()
                    .idpClaim("given_name")
                    .localClaim(new Claims.Claim().uri("http://wso2.org/claims/givenname"));
            Claims.ClaimMapping familyNameMapping = new Claims.ClaimMapping()
                    .idpClaim("family_name")
                    .localClaim(new Claims.Claim().uri("http://wso2.org/claims/lastname"));
            Claims claimConfig = new Claims()
                    .userIdClaim(new Claims.Claim().uri("given_name"))
                    .provisioningClaims(new ArrayList<>());
            claimConfig.setMappings(new ArrayList<>(java.util.Arrays.asList(givenNameMapping, familyNameMapping)));
            idpMgtRestClient.updateIdpClaimConfig(createdIdpId, claimConfig);
            return createdIdpId;
        }
        log.warn("IDP creation returned " + status + ": " + body
                + ".  Auth flow tests may not fully exercise the authenticator.");
        return null;
    }

    /**
     * Create an OAuth2 application with the VP IDP wired into step 1 of its auth sequence.
     * Uses the REST API v1 to create the application with the auth sequence included from the start.
     * Returns the server-assigned application ID.
     */
    protected String createOAuthApplicationWithVPIdp() throws Exception {

        if (vpIdpName == null) {
            log.warn("VP IDP name not set — skipping OAuth2 app creation.");
            return null;
        }
        try {
            OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration()
                    .addGrantTypesItem("authorization_code")
                    .addCallbackURLsItem(CALLBACK_URL);

            ApplicationModel app = new ApplicationModel()
                    .name("vp-auth-flow-app-" + UUID.randomUUID())
                    .inboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig))
                    .advancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true))
                    .claimConfiguration(buildClaimConfiguration())
                    .authenticationSequence(new AuthenticationSequence()
                            .type(TypeEnum.USER_DEFINED)
                            .addStepsItem(new AuthenticationStep()
                                    .id(1)
                                    .addOptionsItem(new Authenticator()
                                            .idp(vpIdpName)
                                            .authenticator("PresentationAuthenticator"))));

            String createdAppId = restClient.createApplication(app);
            if (createdAppId == null) {
                log.warn("Application creation returned null ID.");
                return null;
            }
            OpenIDConnectConfiguration createdOidc = restClient.getOIDCInboundDetails(createdAppId);
            consumerKey = createdOidc.getClientId();
            consumerSecret = createdOidc.getClientSecret();
            return createdAppId;
        } catch (Exception e) {
            log.warn("OAuth2 app creation failed: " + e.getMessage()
                    + ".  Auth flow tests will run with degraded coverage.");
        }
        return null;
    }

    /** Configures givenname and lastname as requested claims so that mapped VP credential claims appear in the id_token. */
    private ClaimConfiguration buildClaimConfiguration() {

        ClaimConfiguration claimConfig = new ClaimConfiguration();
        claimConfig.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/givenname"));
        claimConfig.addRequestedClaimsItem(getRequestedClaim("http://wso2.org/claims/lastname"));
        return claimConfig;
    }

    /** Builds a RequestedClaimConfiguration for the given claim URI. */
    private RequestedClaimConfiguration getRequestedClaim(String claimUri) {

        RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
        requestedClaim.setClaim(
                new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim()
                        .uri(claimUri));
        return requestedClaim;
    }

    /** Builds the OAuth2 authorization URL for the test application. */
    protected String buildAuthorizationUrl() {

        String base = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain());
        return base
                + "?response_type=code"
                + "&client_id=" + consumerKey
                + "&redirect_uri=" + CALLBACK_URL
                + "&scope=openid+profile"
                + "&state=" + UUID.randomUUID();
    }

    /**
     * Container for context extracted from an initiated auth flow.
     */
    protected static class AuthFlowContext {
        String requestId;
        String sessionDataKey;
        String walletLoginUrl;
        String nonce;
        String audience;
        BasicCookieStore cookieStore;
    }

    /**
     * Initiate the auth flow, follow redirects to extract the requestId and sessionDataKey,
     * then fetch the nonce and audience from the presentation request JWT.
     */
    protected AuthFlowContext initiateAuthFlowAndFetchNonce() throws Exception {

        AuthFlowContext ctx = startAuthFlowAndExtractContext();
        if (ctx == null || ctx.requestId == null) {
            log.warn("Could not extract requestId from auth flow — VP session tests will be limited.");
            return null;
        }

        try (CloseableHttpResponse jwtResponse = vpSessionRestClient.getPresentationRequestJwt(ctx.requestId)) {
            String jwtBody = EntityUtils.toString(jwtResponse.getEntity()).trim();
            if (jwtBody.contains(".")) {
                SignedJWT requestJwt = SignedJWT.parse(jwtBody);
                ctx.nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
                // KB-JWT aud must be the verifier's client_id.
                ctx.audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            }
        } catch (Exception e) {
            log.warn("Could not fetch presentation request JWT nonce: " + e.getMessage());
        }

        if (ctx.nonce == null) {
            ctx.nonce = "fallback-nonce-" + UUID.randomUUID();
        }
        if (ctx.audience == null) {
            ctx.audience = "https://verifier.example.com";
        }
        return ctx;
    }

    /**
     * Follow the auth redirect chain and extract both {@code requestId} and {@code sessionDataKey}
     * from the wallet_login.jsp URL.  Returns {@code null} if the requestId cannot be found.
     */
    protected AuthFlowContext startAuthFlowAndExtractContext() {

        try {
            String authUrl = buildAuthorizationUrl();
            BasicCookieStore cookieStore = new BasicCookieStore();
            try (CloseableHttpClient httpClient = buildNoRedirectHttpClient(cookieStore)) {
                String currentUrl = authUrl;
                for (int hop = 0; hop < 10; hop++) {
                    HttpGet get = new HttpGet(currentUrl);
                    try (CloseableHttpResponse response = httpClient.execute(get)) {
                        String location = getLocationHeader(response);
                        EntityUtils.consume(response.getEntity());
                        if (location == null) {
                            break;
                        }
                        if (location.contains("requestId") || location.contains(WALLET_LOGIN_JSP)) {
                            AuthFlowContext ctx = new AuthFlowContext();
                            ctx.requestId = extractQueryParam(location, "requestId");
                            ctx.sessionDataKey = extractQueryParam(location, "sessionDataKey");
                            ctx.walletLoginUrl = location;
                            ctx.cookieStore = cookieStore;
                            return ctx;
                        }
                        currentUrl = location;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("startAuthFlowAndExtractContext failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * POST to IS commonauth with the sessionDataKey and requestId to complete the callback.
     * Returns the authorization code if the flow succeeds, or {@code null}.
     */
    protected String triggerCommonAuthCallback(String sessionDataKey, String requestId, String vpStatus,
            BasicCookieStore cookieStore) throws Exception {

        String callbackStatus = "VERIFIED".equals(vpStatus) ? "success" : "failed";
        String location = triggerCommonAuthCallbackAndGetRedirect(sessionDataKey, requestId, callbackStatus,
                cookieStore);

        if (location == null) {
            return null;
        }
        if (location.contains("code=")) {
            return extractQueryParam(location, "code");
        }

        // Follow IS-internal redirects until the code appears or we hit the external callback.
        try (CloseableHttpClient httpClient = buildNoRedirectHttpClient(cookieStore)) {
            for (int hop = 0; hop < 5 && location != null; hop++) {
                if (location.contains("code=") || !location.startsWith(serverURL)) {
                    return extractQueryParam(location, "code");
                }
                HttpGet get = new HttpGet(location);
                try (CloseableHttpResponse response = httpClient.execute(get)) {
                    EntityUtils.consume(response.getEntity());
                    location = getLocationHeader(response);
                }
            }
        } catch (Exception e) {
            log.warn("triggerCommonAuthCallback redirect follow failed: " + e.getMessage());
        }

        return location != null ? extractQueryParam(location, "code") : null;
    }

    /**
     * POST to IS commonauth and return the Location redirect URI.
     * Mirrors the form wallet_login.jsp submits: sessionDataKey, vp_request_id, status.
     * The cookieStore must contain the nonce cookie captured during the auth flow initiation so that
     * IS can validate the session nonce and complete the flow rather than restarting it.
     */
    protected String triggerCommonAuthCallbackAndGetRedirect(String sessionDataKey, String requestId, String status,
            BasicCookieStore cookieStore) throws Exception {

        String commonAuthUrl = serverURL + COMMONAUTH_PATH;

        try (CloseableHttpClient httpClient = buildNoRedirectHttpClient(cookieStore)) {
            HttpPost post = new HttpPost(commonAuthUrl);
            List<org.apache.http.NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            nvps.add(new BasicNameValuePair("vp_request_id", requestId));
            nvps.add(new BasicNameValuePair("status", status));
            post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(nvps));
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                EntityUtils.consume(response.getEntity());
                return getLocationHeader(response);
            }
        } catch (Exception e) {
            log.warn("triggerCommonAuthCallbackAndGetRedirect failed: " + e.getMessage());
            return null;
        }
    }

    /** Exchanges an authorization code for tokens at the token endpoint and returns the full token response as JSON. */
    protected JSONObject exchangeCodeForTokens(String code) throws Exception {

        String tokenEndpoint = getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", CALLBACK_URL);

        String credentials = Base64.encodeBase64String((consumerKey + ":" + consumerSecret).getBytes()).trim();
        try (CloseableHttpClient httpClient = buildNoRedirectHttpClient()) {
            HttpPost post = new HttpPost(tokenEndpoint);
            post.setHeader("Authorization", "Basic " + credentials);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nvps = new ArrayList<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
            }
            post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(nvps));
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String body = EntityUtils.toString(response.getEntity());
                return new JSONObject(body);
            }
        }
    }

    /** Returns an HTTP client configured to not follow redirects, with no shared cookie store. */
    protected CloseableHttpClient buildNoRedirectHttpClient() {

        return buildNoRedirectHttpClient(null);
    }

    /** Returns an HTTP client configured to not follow redirects, sharing the provided cookie store. */
    protected CloseableHttpClient buildNoRedirectHttpClient(BasicCookieStore cookieStore) {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setRedirectsEnabled(false)
                .build();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig);
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        return builder.build();
    }

    /** Registers the issuer URL and its signing key as a trusted issuer for the given credential type. */
    protected void configureTrustedIssuer(String definitionId, String credentialId,
                                        String issuerUrl, ECKey signingKey) throws Exception {

        String pemCert = VPTestUtils.buildSelfSignedPemCertificate(signingKey);
        String base64Pem = java.util.Base64.getEncoder()
                .encodeToString(pemCert.getBytes(StandardCharsets.UTF_8));
        String body = "{\"issuerConfigs\":[{"
                + "\"issuerUrl\":\"" + issuerUrl + "\","
                + "\"keySourceType\":\"pem\","
                + "\"keySource\":\"" + base64Pem + "\""
                + "}]}";
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId
                + "/credentials/" + credentialId + "/issuer-configs";
        Response response = adminPut(url, body);
        int status = response.getStatusCode();
        String responseBody = response.getBody().asString();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure trusted issuer (status " + status + "): " + responseBody);
    }

    /** Sets the global VP response mode (e.g. {@code direct_post} or {@code direct_post.jwt}) via the configs API. */
    protected void configureVPResponseMode(String responseMode) {

        String json = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"" + responseMode + "\"}";
        String url = serverURL + "/api/server/v1/configs/openid4vp";
        Response response = adminPut(url, json);
        int status = response.getStatusCode();
        String responseBody = response.getBody().asString();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure VP response mode (status " + status + "): " + responseBody);
    }

    protected Response adminPost(String url, String json) {

        return adminPost(url, json, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminPost(String url, String json, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .post(url);
    }

    protected Response adminPut(String url, String json) {

        return adminPut(url, json, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminPut(String url, String json, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .put(url);
    }

    protected Response adminDelete(String url) {

        return adminDelete(url, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminDelete(String url, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .delete(url);
    }

    protected Response adminGet(String url, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .get(url);
    }

    /** Returns the value of the Location header from the response, or null if absent. */
    protected static String getLocationHeader(CloseableHttpResponse response) {

        org.apache.http.Header locationHeader = response.getFirstHeader("Location");
        return locationHeader != null ? locationHeader.getValue() : null;
    }

    /** Extracts the value of a named query parameter from a URL string, or null if not present. */
    protected static String extractQueryParam(String url, String paramName) {

        if (url == null) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query == null) {
                return null;
            }
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && paramName.equals(kv[0])) {
                    return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        } catch (URISyntaxException e) {
            int idx = url.indexOf(paramName + "=");
            if (idx >= 0) {
                String rest = url.substring(idx + paramName.length() + 1);
                int end = rest.indexOf('&');
                return end >= 0 ? rest.substring(0, end) : rest;
            }
        }
        return null;
    }

    /** Polls using the default VP session client; delegates to {@link #pollStatusUntilNotActive(String, int, long, VPSessionRestClient)}. */
    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs) throws Exception {

        return pollStatusUntilNotActive(requestId, maxRetries, intervalMs, vpSessionRestClient);
    }

    /**
     * Polls the VP session status at {@code intervalMs} intervals until it is no longer ACTIVE, then returns
     * the final status.
     *
     * @throws Exception If any HTTP call fails, or if the status is still ACTIVE once the
     *                    {@code maxRetries * intervalMs} timeout elapses.
     */
    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs,
            VPSessionRestClient client) throws Exception {

        AtomicReference<String> lastStatus = new AtomicReference<>("ACTIVE");
        await().atMost((long) maxRetries * intervalMs, TimeUnit.MILLISECONDS)
                .pollInterval(intervalMs, TimeUnit.MILLISECONDS)
                .until(() -> {
                    JSONObject statusResponse = client.getSessionStatus(requestId);
                    String status = (String) statusResponse.get("status");
                    lastStatus.set(status);
                    return !"ACTIVE".equals(status);
                });
        return lastStatus.get();
    }

    /** Deletes the test application if one was created, logging rather than throwing on failure. */
    protected void deleteApplicationIfExists() {

        if (appId != null) {
            try {
                deleteApp(appId);
            } catch (Exception e) {
                log.warn("Failed to delete app: " + e.getMessage());
            }
        }
    }

    /** Deletes the test IDP if one was created, logging rather than throwing on failure. */
    protected void deleteIdpIfExists() {

        if (idpId != null) {
            try {
                idpMgtRestClient.deleteIdp(idpId);
            } catch (Exception e) {
                log.warn("Failed to delete IDP: " + e.getMessage());
            }
        }
    }

    /** Deletes the test presentation definition, logging rather than throwing on failure. */
    protected void deletePresentationDefinitionQuietly() {

        try {
            deletePresentationDefinition(presentationDefinitionId);
        } catch (Exception e) {
            log.warn("Failed to delete presentation definition: " + e.getMessage());
        }
    }

    /** Closes the VP session REST client, logging rather than throwing on failure. */
    protected void closeVpSessionClient() {

        if (vpSessionRestClient != null) {
            try {
                vpSessionRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close VP session client: " + e.getMessage());
            }
        }
    }

    /** Closes the IDP management REST client, logging rather than throwing on failure. */
    protected void closeIdpMgtClient() {

        if (idpMgtRestClient != null) {
            try {
                idpMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close IDP management client: " + e.getMessage());
            }
        }
    }

    /** Closes the shared OAuth2 REST client, logging rather than throwing on failure. */
    protected void closeOAuthRestClient() {

        if (restClient != null) {
            try {
                restClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close REST client: " + e.getMessage());
            }
        }
    }
}
