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
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
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
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared constants, fields and helpers for the VP Authenticator flow tests.
 * Holds no {@code @Test} methods and no shared {@code @BeforeClass}/{@code @AfterClass} —
 * each subclass is responsible for its own setup and teardown.
 */
public abstract class VPAuthenticatorFlowTestBase extends VPTestBase {

    protected static final String CALLBACK_URL          = "https://localhost/callback";
    protected static final String ISSUER_URL             = "https://test-issuer.example.com";
    protected static final String COMMONAUTH_PATH        = "commonauth";
    protected static final String WALLET_LOGIN_JSP       = "wallet_login.jsp";

    protected static final int    POLL_MAX_RETRIES       = 15;
    protected static final long   POLL_INTERVAL_MS       = 500L;

    protected ECKey issuerSigningKey;

    protected String presentationDefinitionId;
    protected String appId;
    protected String idpId;
    protected String vpIdpName;
    protected IdpMgtRestClient idpMgtRestClient;

    /**
     * Creates the presentation definition from the test resource JSON and returns its server-assigned ID.
     *
     * <p>If the server returns 409 (resource already exists from a previous aborted run), the stale
     * definition is deleted first and then the definition is created fresh.  This avoids leaking
     * state from a previous run (e.g. different trusted-issuer configs) into the current test.
     */
    protected String createPresentationDefinition() throws Exception {

        String json = "{\"identifier\":\"test-vp-auth-flow-definition\","
                + "\"displayName\":\"Test VP Auth Flow Definition\","
                + "\"description\":\"Presentation definition for VP authenticator end-to-end flow integration tests\","
                + "\"credentials\":[{\"id\":\"PersonCredential\",\"type\":\"PersonCredential\","
                + "\"format\":\"dc+sd-jwt\"}]}";
        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH;
        Response response = adminPost(url, json);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        if (status == HttpStatus.SC_CONFLICT) {
            String identifier = new JSONObject(json).getString("identifier");
            Response listResponse = adminGet(serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "?limit=100");
            String listBody = listResponse.getBody().asString();
            Assert.assertEquals(listResponse.getStatusCode(), HttpStatus.SC_OK,
                    "POST returned 409 but list-all also failed (status "
                            + listResponse.getStatusCode() + "): " + listBody);
            JSONObject listJson = new JSONObject(listBody);
            JSONArray definitions = listJson.optJSONArray("presentationDefinitions");
            String staleId = null;
            if (definitions != null) {
                for (int i = 0; i < definitions.length(); i++) {
                    JSONObject def = definitions.getJSONObject(i);
                    if (identifier.equals(def.optString("identifier"))) {
                        staleId = def.getString("id");
                        break;
                    }
                }
            }
            Assert.assertNotNull(staleId,
                    "No matching definition found for identifier='" + identifier + "' after 409.");
            log.info("Deleting stale presentation definition id='" + staleId
                    + "' (identifier='" + identifier + "') left by a previous run.");
            adminDelete(serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + staleId);
            response = adminPost(url, json);
            body = response.getBody().asString();
            status = response.getStatusCode();
        }
        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create presentation definition (status " + status + "): " + body);
        JSONObject jsonResponse = new JSONObject(body);
        return jsonResponse.getString("id");
    }

    protected String createPresentationDefinition(String apiBase, String user, String pass) throws Exception {

        String json = "{\"identifier\":\"test-vp-auth-flow-definition\","
                + "\"displayName\":\"Test VP Auth Flow Definition\","
                + "\"description\":\"Presentation definition for VP authenticator end-to-end flow integration tests\","
                + "\"credentials\":[{\"id\":\"PersonCredential\",\"type\":\"PersonCredential\","
                + "\"format\":\"dc+sd-jwt\"}]}";
        String url = apiBase + PRESENTATION_DEFINITIONS_PATH;
        Response response = adminPost(url, json, user, pass);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        if (status == HttpStatus.SC_CONFLICT) {
            String identifier = new JSONObject(json).getString("identifier");
            Response listResp = adminGet(apiBase + PRESENTATION_DEFINITIONS_PATH + "?limit=100", user, pass);
            String listBody = listResp.getBody().asString();
            Assert.assertEquals(listResp.getStatusCode(), HttpStatus.SC_OK,
                    "POST returned 409 but list-all also failed: " + listBody);
            JSONArray definitions = new JSONObject(listBody).optJSONArray("presentationDefinitions");
            String staleId = null;
            if (definitions != null) {
                for (int i = 0; i < definitions.length(); i++) {
                    JSONObject def = definitions.getJSONObject(i);
                    if (identifier.equals(def.optString("identifier"))) {
                        staleId = def.getString("id");
                        break;
                    }
                }
            }
            Assert.assertNotNull(staleId,
                    "No matching definition found for identifier='" + identifier + "' after 409.");
            adminDelete(apiBase + PRESENTATION_DEFINITIONS_PATH + "/" + staleId, user, pass);
            response = adminPost(url, json, user, pass);
            body = response.getBody().asString();
            status = response.getStatusCode();
        }
        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create presentation definition (status " + status + "): " + body);
        return new JSONObject(body).getString("id");
    }

    protected String createVPAuthenticatorIDP(String definitionId, String idpName,
                                               String apiBase, String user, String pass) throws Exception {

        JSONObject property = new JSONObject();
        property.put("key", "presentationDefinitionId");
        property.put("value", definitionId);
        JSONObject authenticator = new JSONObject();
        authenticator.put("authenticatorId", VPTestUtils.PRESENTATION_AUTHENTICATOR_ID);
        authenticator.put("isEnabled", true);
        authenticator.put("properties", new JSONArray().put(property));
        JSONObject federatedAuthenticators = new JSONObject();
        federatedAuthenticators.put("defaultAuthenticatorId", VPTestUtils.PRESENTATION_AUTHENTICATOR_ID);
        federatedAuthenticators.put("authenticators", new JSONArray().put(authenticator));
        JSONObject idpObj = new JSONObject();
        idpObj.put("name", idpName);
        idpObj.put("description", "VP Authenticator IDP for integration tests");
        idpObj.put("federatedAuthenticators", federatedAuthenticators);
        String url = apiBase + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpObj.toString(), user, pass);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
            throw new AssertionError("IDP creation failed (status " + status + "): " + body);
        }
        return new JSONObject(body).getString("id");
    }

    protected void configureIdpClaims(String idpId, String apiBase, String user, String pass) throws Exception {

        JSONObject claimConfig = new JSONObject();
        claimConfig.put("userIdClaim", new JSONObject().put("uri", "email"));
        claimConfig.put("mappings", new JSONArray()
                .put(new JSONObject().put("idpClaim", "given_name")
                        .put("localClaim", new JSONObject().put("uri", "http://wso2.org/claims/givenname")))
                .put(new JSONObject().put("idpClaim", "family_name")
                        .put("localClaim", new JSONObject().put("uri", "http://wso2.org/claims/lastname")))
                .put(new JSONObject().put("idpClaim", "email")
                        .put("localClaim", new JSONObject().put("uri", "http://wso2.org/claims/emailaddress"))));
        claimConfig.put("provisioningClaims", new JSONArray());
        String url = apiBase + "/api/server/v1/identity-providers/" + idpId + "/claims";
        Response response = adminPut(url, claimConfig.toString(), user, pass);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "IDP claim config update failed (status " + status + "): " + response.getBody().asString());
    }

    protected String[] createOAuthApp(String idpName, String apiBase, String user, String pass) throws Exception {

        String appName = "vp-auth-flow-app-" + UUID.randomUUID();
        JSONObject oidc = new JSONObject()
                .put("grantTypes", new JSONArray().put("authorization_code"))
                .put("callbackURLs", new JSONArray().put(CALLBACK_URL));
        JSONObject step = new JSONObject()
                .put("id", 1)
                .put("options", new JSONArray().put(
                        new JSONObject().put("idp", idpName).put("authenticator", "PresentationAuthenticator")));
        JSONObject appObj = new JSONObject()
                .put("name", appName)
                .put("inboundProtocolConfiguration", new JSONObject().put("oidc", oidc))
                .put("advancedConfigurations", new JSONObject().put("skipLoginConsent", true))
                .put("authenticationSequence", new JSONObject()
                        .put("type", "USER_DEFINED")
                        .put("steps", new JSONArray().put(step)));
        String url = apiBase + "/api/server/v1/applications";
        Response response = adminPost(url, appObj.toString(), user, pass);
        int status = response.getStatusCode();
        if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
            throw new AssertionError("App creation failed (status " + status + "): " + response.getBody().asString());
        }
        String appLocation = response.getHeader("Location");
        if (appLocation == null) {
            throw new AssertionError("App creation response had no Location header.");
        }
        String appId = appLocation.substring(appLocation.lastIndexOf("/") + 1);
        String oidcUrl = apiBase + "/api/server/v1/applications/" + appId + "/inbound-protocols/oidc";
        Response oidcResp = adminGet(oidcUrl, user, pass);
        int oidcStatus = oidcResp.getStatusCode();
        if (oidcStatus != HttpStatus.SC_OK) {
            throw new AssertionError("OIDC config retrieval failed (status " + oidcStatus + "): "
                    + oidcResp.getBody().asString());
        }
        String consumerKey = new JSONObject(oidcResp.getBody().asString()).optString("clientId", null);
        return new String[]{appId, consumerKey};
    }

    /** Deletes the presentation definition with the given ID. */
    protected void deletePresentationDefinition(String defId) {

        if (defId == null) {
            return;
        }
        adminDelete(serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + defId);
    }

    /**
     * Create a federated IDP configured with the PresentationExchangeAuthenticator.
     * Returns the server-assigned IDP ID.
     */
    protected String createVPAuthenticatorIDP() throws Exception {

        String generatedIdpName = "VPTestIDP-" + UUID.randomUUID();
        JSONObject property = new JSONObject();
        property.put("key", "presentationDefinitionId");
        property.put("value", presentationDefinitionId);
        JSONObject authenticator = new JSONObject();
        authenticator.put("authenticatorId", VPTestUtils.PRESENTATION_AUTHENTICATOR_ID);
        authenticator.put("isEnabled", true);
        authenticator.put("properties", new JSONArray().put(property));
        JSONObject federatedAuthenticators = new JSONObject();
        federatedAuthenticators.put("defaultAuthenticatorId", VPTestUtils.PRESENTATION_AUTHENTICATOR_ID);
        federatedAuthenticators.put("authenticators", new JSONArray().put(authenticator));
        JSONObject jit = new JSONObject();
        jit.put("isEnabled", true);
        jit.put("userstore", "PRIMARY");
        jit.put("scheme", "PROVISION_SILENTLY");
        JSONObject idpObj = new JSONObject();
        idpObj.put("name", generatedIdpName);
        idpObj.put("description", "VP Authenticator IDP for integration tests");
        idpObj.put("federatedAuthenticators", federatedAuthenticators);
        idpObj.put("provisioning", new JSONObject().put("jit", jit));
        String idpJson = idpObj.toString();

        String url = serverBaseUrl() + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpJson);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
            throw new AssertionError("IDP creation failed (status " + status + "): " + body);
        }
        JSONObject jsonResponse = new JSONObject(body);
        String createdIdpId = jsonResponse.getString("id");
        vpIdpName = generatedIdpName;
        Claims.ClaimMapping givenNameMapping = new Claims.ClaimMapping()
                .idpClaim("given_name")
                .localClaim(new Claims.Claim().uri("http://wso2.org/claims/givenname"));
        Claims.ClaimMapping familyNameMapping = new Claims.ClaimMapping()
                .idpClaim("family_name")
                .localClaim(new Claims.Claim().uri("http://wso2.org/claims/lastname"));
        Claims.ClaimMapping emailMapping = new Claims.ClaimMapping()
                .idpClaim("email")
                .localClaim(new Claims.Claim().uri("http://wso2.org/claims/emailaddress"));
        Claims claimConfig = new Claims()
                .userIdClaim(new Claims.Claim().uri("email"))
                .provisioningClaims(new ArrayList<>());
        claimConfig.setMappings(new ArrayList<>(
                java.util.Arrays.asList(givenNameMapping, familyNameMapping, emailMapping)));
        idpMgtRestClient.updateIdpClaimConfig(createdIdpId, claimConfig);
        return createdIdpId;
    }

    /**
     * Create an OAuth2 application with the VP IDP wired into step 1 of its auth sequence.
     * Uses the REST API v1 to create the application with the auth sequence included from the start.
     * Returns the server-assigned application ID.
     */
    protected String createOAuthApplicationWithVPIdp() throws Exception {

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
            throw new AssertionError("OAuth2 application creation returned null ID.");
        }
        OpenIDConnectConfiguration createdOidc = restClient.getOIDCInboundDetails(createdAppId);
        consumerKey = createdOidc.getClientId();
        consumerSecret = createdOidc.getClientSecret();
        return createdAppId;
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

        configureVPResponseMode("direct_post");
        AuthFlowContext ctx = startAuthFlowAndExtractContext();
        if (ctx == null || ctx.requestId == null) {
            log.warn("Could not extract requestId from auth flow — VP session tests will be limited.");
            return null;
        }

        try (CloseableHttpResponse jwtResponse = vpSessionRestClient.getPresentationRequestJwt(ctx.requestId)) {
            String jwtBody = EntityUtils.toString(jwtResponse.getEntity()).trim();
            Assert.assertEquals(jwtResponse.getStatusLine().getStatusCode(), org.apache.http.HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200. Body: " + jwtBody);
            SignedJWT requestJwt = SignedJWT.parse(jwtBody);
            ctx.nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            ctx.audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
        }
        Assert.assertNotNull(ctx.nonce, "Presentation request JWT must contain 'nonce'.");
        Assert.assertNotNull(ctx.audience, "Presentation request JWT must contain 'client_id'.");
        return ctx;
    }

    protected AuthFlowContext startAuthFlowAndExtractContext() {

        return startAuthFlowAndExtractContext(buildAuthorizationUrl());
    }

    protected AuthFlowContext startAuthFlowAndExtractContext(String authUrl) {

        try {
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
            throw new RuntimeException("startAuthFlowAndExtractContext failed: " + e.getMessage(), e);
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

    protected String triggerCommonAuthCallbackAndGetRedirect(String sessionDataKey, String requestId, String status,
            BasicCookieStore cookieStore) throws Exception {

        return triggerCommonAuthCallbackAndGetRedirect(
                serverURL + COMMONAUTH_PATH, sessionDataKey, requestId, status, cookieStore);
    }

    protected String triggerCommonAuthCallbackAndGetRedirect(String commonAuthUrl,
            String sessionDataKey, String requestId, String status,
            BasicCookieStore cookieStore) throws Exception {

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

    /** Deletes the test application if one was created. */
    protected void deleteApplicationIfExists() {

        if (appId != null) {
            try {
                deleteApp(appId);
            } catch (Exception e) {
                log.warn("Failed to delete app: " + e.getMessage());
            }
        }
    }

    /** Deletes the test IDP if one was created. */
    protected void deleteIdpIfExists() {

        if (idpId != null) {
            try {
                idpMgtRestClient.deleteIdp(idpId);
            } catch (Exception e) {
                log.warn("Failed to delete IDP: " + e.getMessage());
            }
        }
    }

    /** Deletes the test presentation definition. */
    protected void deletePresentationDefinitionQuietly() {

        try {
            deletePresentationDefinition(presentationDefinitionId);
        } catch (Exception e) {
            log.warn("Failed to delete presentation definition: " + e.getMessage());
        }
    }

    /** Closes the IDP management REST client. */
    protected void closeIdpMgtClient() {

        if (idpMgtRestClient != null) {
            try {
                idpMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close IDP management client: " + e.getMessage());
            }
        }
    }

    /** Closes the shared OAuth2 REST client. */
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
