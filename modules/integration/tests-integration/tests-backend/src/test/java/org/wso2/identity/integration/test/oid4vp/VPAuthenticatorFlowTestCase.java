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
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.apache.http.Header;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence.TypeEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Claims;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * End-to-end integration tests for the VP Authenticator flow.
 */
public class VPAuthenticatorFlowTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL             = "https://localhost/callback";
    private static final String CREDENTIAL_IDENTIFIER    = "PersonCredential";
    private static final String ISSUER_URL               = "https://test-issuer.example.com";
    private static final String PRESENTATION_DEFINITIONS_PATH = "/api/server/v1/presentation-definitions";
    private static final String COMMONAUTH_PATH          = "commonauth";
    private static final String WALLET_LOGIN_JSP         = "wallet_login.jsp";

    // Secondary-tenant constants for testAuthFlowTenanted.
    private static final String TENANT_AUTH_DOMAIN   = "vptest-tenant-auth.com";
    private static final String TENANT_AUTH_ADMIN    = "admin@vptest-tenant-auth.com";
    private static final String TENANT_AUTH_PASSWORD = "Admin@123";

    private static final int    POLL_MAX_RETRIES         = 15;
    private static final long   POLL_INTERVAL_MS         = 500L;

    private static ECKey issuerSigningKey;

    private String presentationDefinitionId;
    private String appId;
    private String idpId;
    private String vpIdpName;
    private VPSessionRestClient vpSessionRestClient;
    private IdpMgtRestClient idpMgtRestClient;
    private SCIM2RestClient scim2RestClient;
    private String jitProvisionedUserId;

    // Tenant-B infrastructure for testAuthFlowTenanted.
    private TenantMgtRestClient tenantMgtRestClient;
    private String tenantBDefinitionId;
    private String tenantBIdpId;
    private String tenantBIdpName;
    private String tenantBAppId;
    private String tenantBConsumerKey;
    private VPSessionRestClient vpSessionRestClientTenantB;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        issuerSigningKey = SDJwtVcTestBuilder.generateSigningKey();
        configureVPResponseMode("direct_post");
        presentationDefinitionId = createPresentationDefinition();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, isServer.getContextTenant());
        idpId = createVPAuthenticatorIDP();
        appId = createOAuthApplicationWithVPIdp();
        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());
        scim2RestClient = new SCIM2RestClient(serverURL, isServer.getContextTenant());
        provisionTenantB();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        if (appId != null) {
            try {
                deleteApp(appId);
            } catch (Exception e) {
                log.warn("Failed to delete app: " + e.getMessage());
            }
        }
        if (idpId != null) {
            try {
                idpMgtRestClient.deleteIdp(idpId);
            } catch (Exception e) {
                log.warn("Failed to delete IDP: " + e.getMessage());
            }
        }
        try {
            deletePresentationDefinition(presentationDefinitionId);
        } catch (Exception e) {
            log.warn("Failed to delete presentation definition: " + e.getMessage());
        }
        if (vpSessionRestClient != null) {
            try {
                vpSessionRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close VP session client: " + e.getMessage());
            }
        }
        if (idpMgtRestClient != null) {
            try {
                idpMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close IDP management client: " + e.getMessage());
            }
        }
        if (restClient != null) {
            try {
                restClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close REST client: " + e.getMessage());
            }
        }
        if (jitProvisionedUserId != null) {
            try {
                scim2RestClient.deleteUser(jitProvisionedUserId);
            } catch (Exception e) {
                log.warn("Failed to delete JIT-provisioned user: " + e.getMessage());
            }
        }
        if (scim2RestClient != null) {
            try {
                scim2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close SCIM2 client: " + e.getMessage());
            }
        }

        // Clean up tenant B.
        String tenantApiPrefix = serverURL + "/t/" + TENANT_AUTH_DOMAIN;
        if (tenantBAppId != null) {
            try (CloseableHttpResponse r = tenantBAdminDelete(
                    tenantApiPrefix + "/api/server/v1/applications/" + tenantBAppId)) {
                EntityUtils.consume(r.getEntity());
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B app: " + e.getMessage());
            }
        }
        if (tenantBIdpId != null) {
            try (CloseableHttpResponse r = tenantBAdminDelete(
                    tenantApiPrefix + "/api/server/v1/identity-providers/" + tenantBIdpId)) {
                EntityUtils.consume(r.getEntity());
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B IDP: " + e.getMessage());
            }
        }
        if (tenantBDefinitionId != null) {
            try (CloseableHttpResponse r = tenantBAdminDelete(
                    tenantApiPrefix + PRESENTATION_DEFINITIONS_PATH + "/" + tenantBDefinitionId)) {
                EntityUtils.consume(r.getEntity());
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B presentation definition: " + e.getMessage());
            }
        }
        if (vpSessionRestClientTenantB != null) {
            try {
                vpSessionRestClientTenantB.closeHttpClient();
            } catch (IOException e) {
                log.warn("Failed to close tenant-B VP session client: " + e.getMessage());
            }
        }
        if (tenantMgtRestClient != null) {
            try {
                tenantMgtRestClient.closeHttpClient();
            } catch (IOException e) {
                log.warn("Failed to close tenant management client: " + e.getMessage());
            }
        }
    }

    @Test(groups = "wso2.is",
            description = "Initiating OAuth2 auth code flow with VP authenticator redirects to wallet_login.jsp.")
    public void testInitiateAuthFlowRedirectsToWalletLogin() throws Exception {

        AuthFlowContext ctx = startAuthFlowAndExtractContext();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + ".");
        Assert.assertTrue(ctx.walletLoginUrl != null && ctx.walletLoginUrl.contains(WALLET_LOGIN_JSP),
                "Redirect must point to " + WALLET_LOGIN_JSP + " but was: " + ctx.walletLoginUrl);
        Assert.assertNotNull(ctx.requestId,
                "Wallet login redirect must carry a requestId parameter.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");
    }

    @Test(groups = "wso2.is",
            description = "The requestId embedded in the wallet login redirect maps to an ACTIVE session.",
            dependsOnMethods = "testInitiateAuthFlowRedirectsToWalletLogin")
    public void testWalletLoginPageContainsCorrectSession() throws Exception {

        AuthFlowContext ctx = startAuthFlowAndExtractContext();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + ".");
        Assert.assertTrue(ctx.walletLoginUrl != null && ctx.walletLoginUrl.contains(WALLET_LOGIN_JSP),
                "Redirect must point to " + WALLET_LOGIN_JSP + " but was: " + ctx.walletLoginUrl);
        Assert.assertNotNull(ctx.requestId,
                "Wallet login redirect must carry a requestId parameter.");

        JSONObject statusResponse = vpSessionRestClient.getSessionStatus(ctx.requestId);
        String status = (String) statusResponse.get("status");
        Assert.assertEquals(status, "ACTIVE",
                "Session referenced by wallet login page must be ACTIVE.");
    }

    @Test(groups = "wso2.is",
            description = "A valid SD-JWT-VC submission completes the auth flow and produces an access token.")
    public void testAuthFlowSuccessAfterValidVPSubmission() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("family_name", "Smith")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            int status = vpResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(vpResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "VP submission during auth flow must return 200.");
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "VP token from configured trusted issuer with correct nonce must result in a VERIFIED session.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code,
                "After successful VP verification the auth code must be returned in the redirect.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        Assert.assertNotNull(tokenResponse.get("access_token"),
                "Token endpoint must return an access_token after a successful VP auth flow.");
        Assert.assertNotNull(tokenResponse.get("id_token"),
                "Token endpoint must return an id_token after a successful VP auth flow.");
    }

    @Test(groups = "wso2.is",
            description = "After a successful VP flow, claim values from the credential appear in the id_token.")
    public void testAuthFlowClaimsAreMappedToIdToken() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("family_name", "Smith")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            EntityUtils.consume(vpResponse.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "VP token from configured trusted issuer with correct nonce must result in a VERIFIED session.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code,
                "After successful VP verification the auth code must be returned in the redirect.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        String idToken = (String) tokenResponse.get("id_token");
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String[] jwtParts = idToken.split("\\.");
        Assert.assertEquals(jwtParts.length, 3, "id_token must be a compact-serialized JWT with 3 parts.");

        String payloadJson = new String(
                java.util.Base64.getUrlDecoder().decode(jwtParts[1]),
                StandardCharsets.UTF_8);
        JSONObject claims = (JSONObject) new JSONParser().parse(payloadJson);

        Assert.assertNotNull(claims.get("sub"),
                "id_token must contain a 'sub' claim.");
        // The IDP maps VP credential claims to local claims → OIDC id_token claims:
        // given_name  → http://wso2.org/claims/givenname  → OIDC given_name
        // family_name → http://wso2.org/claims/lastname   → OIDC family_name
        Assert.assertEquals(claims.get("given_name"), "Alice",
                "id_token 'given_name' must reflect the VP credential's 'given_name' value via the IDP claim mapping.");
        Assert.assertEquals(claims.get("family_name"), "Smith",
                "id_token 'family_name' must reflect the VP credential's 'family_name' value via the IDP claim mapping.");
    }

    @Test(groups = "wso2.is",
            description = "Submitting a VP with a wrong nonce causes the auth flow to end with an error redirect.")
    public void testAuthFlowFailsWhenVPVerificationFails() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            EntityUtils.consume(vpResponse.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP with wrong nonce must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, ctx.requestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a failed VP callback — got no Location header.");
        // When retry is enabled IS may redirect to wallet_login.jsp or retry.do for another attempt.
        // If neither, the redirect must carry an error parameter to the client.
        if (!redirectUri.contains(WALLET_LOGIN_JSP) && !redirectUri.contains("retry.do")) {
            Assert.assertTrue(redirectUri.contains("error") || redirectUri.contains("error_description"),
                    "Auth flow error redirect must carry an 'error' parameter.  Got: " + redirectUri);
        }
    }

    @Test(groups = "wso2.is",
            description = "A wallet access_denied response causes the auth flow to end with an error redirect.")
    public void testAuthFlowFailsWhenWalletReportsError() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        try (CloseableHttpResponse errorResponse = vpSessionRestClient.submitWalletError(
                ctx.requestId, "access_denied", "User declined to share credentials.")) {
            EntityUtils.consume(errorResponse.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "Wallet access_denied must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, ctx.requestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a wallet error callback — got no Location header.");
        if (!redirectUri.contains(WALLET_LOGIN_JSP) && !redirectUri.contains("retry.do")) {
            Assert.assertTrue(redirectUri.contains("error"),
                    "Auth flow error redirect must carry an 'error' parameter.  Got: " + redirectUri);
        }
    }

    @Test(groups = "wso2.is",
            description = "When retry is enabled, a failed VP attempt causes IS to issue a new requestId.")
    public void testAuthFlowRetryAfterFailure() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String firstRequestId = ctx.requestId;

        String badVpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce")
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(firstRequestId, badVpToken)) {
            EntityUtils.consume(resp.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(firstRequestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP with wrong nonce must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, firstRequestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a failed VP callback — got no Location header.");

        // When retry is enabled IS redirects back to wallet_login.jsp with a fresh requestId.
        // Assert that the new requestId is different from the failed session's requestId.
        if (redirectUri.contains(WALLET_LOGIN_JSP)) {
            String newRequestId = extractQueryParam(redirectUri, "requestId");
            Assert.assertNotNull(newRequestId,
                    "Retry redirect to " + WALLET_LOGIN_JSP + " must carry a requestId.  Got: " + redirectUri);
            Assert.assertNotEquals(newRequestId, firstRequestId,
                    "Retry must issue a fresh requestId distinct from the failed session's requestId.");
        }
        // If retry is not enabled IS redirects with an error to the client — that path is
        // covered by testAuthFlowFailsWhenVPVerificationFails.
    }

    @Test(groups = "wso2.is",
            description = "JIT provisioning creates a local user account on first successful VP login.")
    public void testJITProvisioningCreatesUserOnFirstLogin() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        if (ctx == null) {
            log.warn("testJITProvisioningCreatesUserOnFirstLogin: could not initiate auth flow — skipping.");
            return;
        }

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "JIT")
                .claim("family_name", "TestUser")
                .claim("email", "jit.testuser@example.com")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            EntityUtils.consume(resp.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "VP from trusted issuer must reach VERIFIED to trigger JIT provisioning.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code, "Auth flow must produce an authorization code after VERIFIED VP.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        String idToken = (String) tokenResponse.get("id_token");
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String payload = new String(
                java.util.Base64.getUrlDecoder().decode(idToken.split("\\.")[1]),
                StandardCharsets.UTF_8);
        JSONObject idTokenClaims = (JSONObject) new JSONParser().parse(payload);
        String sub = (String) idTokenClaims.get("sub");
        Assert.assertNotNull(sub, "id_token must contain a 'sub' claim.");

        String filter = java.net.URLEncoder.encode("userName eq \"" + sub + "\"", StandardCharsets.UTF_8);
        JSONObject users = scim2RestClient.filterUsers(filter);
        long totalResults = (long) users.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "JIT provisioning must create exactly one local user for sub='" + sub + "'.");

        jitProvisionedUserId = (String) ((JSONObject) ((JSONArray) users.get("Resources")).get(0)).get("id");
    }

    @Test(groups = "wso2.is",
            description = "A second VP login by the same subject does not create a duplicate local user.",
            dependsOnMethods = "testJITProvisioningCreatesUserOnFirstLogin")
    public void testJITProvisioningDoesNotDuplicateUser() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        if (ctx == null) {
            return;
        }

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "JIT")
                .claim("family_name", "TestUser")
                .claim("email", "jit.testuser@example.com")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            int status = resp.getStatusLine().getStatusCode();
            EntityUtils.consume(resp.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "Second VP submission (same user) must return 200.");
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "Second VP from the same trusted issuer must also reach VERIFIED.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code, "Auth flow must produce an authorization code on second login.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        String idToken = (String) tokenResponse.get("id_token");
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token on second login.");

        String payload = new String(
                java.util.Base64.getUrlDecoder().decode(idToken.split("\\.")[1]),
                StandardCharsets.UTF_8);
        JSONObject idTokenClaims = (JSONObject) new JSONParser().parse(payload);
        String sub = (String) idTokenClaims.get("sub");
        Assert.assertNotNull(sub, "id_token must contain a 'sub' claim.");

        String filter = java.net.URLEncoder.encode("userName eq \"" + sub + "\"", StandardCharsets.UTF_8);
        JSONObject users = scim2RestClient.filterUsers(filter);
        long totalResults = (long) users.get("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "A second login by the same subject must not create a duplicate user — found " + totalResults + ".");
    }

    @Test(groups = "wso2.is",
            description = "A VP credential with only a subset of mapped claims still produces a VERIFIED flow "
                    + "and an id_token that contains sub and the claims present in the credential.")
    public void testAuthFlowWithPartialCredentialClaims() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        // VP credential with only given_name — family_name is intentionally omitted.
        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            EntityUtils.consume(resp.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "VP from trusted issuer with correct nonce must reach VERIFIED even with partial claims.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code,
                "Auth code must be returned after a VERIFIED VP submission.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        String idToken = (String) tokenResponse.get("id_token");
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String[] jwtParts = idToken.split("\\.");
        Assert.assertEquals(jwtParts.length, 3, "id_token must be a compact-serialized JWT with 3 parts.");

        JSONObject claims = (JSONObject) new JSONParser().parse(
                new String(java.util.Base64.getUrlDecoder().decode(jwtParts[1]), StandardCharsets.UTF_8));

        Assert.assertNotNull(claims.get("sub"),
                "id_token must contain a 'sub' claim.");
        Assert.assertEquals(claims.get("given_name"), "Alice",
                "id_token must contain 'given_name' mapped from the VP credential.");
    }

    @Test(groups = "wso2.is",
            description = "VP authenticator flow works correctly in a secondary tenant.")
    public void testAuthFlowTenanted() throws Exception {

        if (tenantBConsumerKey == null) {
            log.warn("testAuthFlowTenanted: tenant-B OAuth2 app not provisioned — skipping.");
            return;
        }

        AuthFlowContext ctx = initiateAuthFlowTenantBAndFetchNonce();
        Assert.assertNotNull(ctx, "Tenant-B auth flow context must not be null.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("family_name", "Smith")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClientTenantB.submitVPDirectPost(ctx.requestId, vpToken)) {
            int status = vpResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(vpResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "VP submission in tenant-B auth flow must return 200.");
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS,
                vpSessionRestClientTenantB);
        Assert.assertNotEquals(finalStatus, "ACTIVE",
                "Tenant-B VP session must have left ACTIVE state after submission.");

        if ("VERIFIED".equals(finalStatus) && ctx.sessionDataKey != null) {
            String code = triggerTenantBCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, "success",
                    ctx.cookieStore);
            if (code != null) {
                Assert.assertFalse(code.isEmpty(),
                        "Tenant-B auth flow must return a non-empty authorization code.");
            }
        }
    }

    private String createPresentationDefinition() throws Exception {

        String json = readResourceAsString(
                "org/wso2/identity/integration/test/oid4vp/create-vp-presentation-definition-flow.json");
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH;

        try (CloseableHttpResponse response = adminPost(url, json)) {
            String body = EntityUtils.toString(response.getEntity());
            int status = response.getStatusLine().getStatusCode();
            Assert.assertTrue(status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK,
                    "Failed to create presentation definition (status " + status + "): " + body);
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(body);
            return jsonResponse.get("id").toString();
        }
    }

    private void deletePresentationDefinition(String defId) throws IOException {

        if (defId == null) {
            return;
        }
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + defId;
        try (CloseableHttpResponse response = adminDelete(url)) {
            EntityUtils.consume(response.getEntity());
        }
    }

    /**
     * Create a federated IDP configured with the PresentationExchangeAuthenticator.
     * Returns the server-assigned IDP ID.
     */
    private String createVPAuthenticatorIDP() throws Exception {

        // Build the IDP JSON payload inline — avoids importing generated model classes
        // that may not yet exist for the VP authenticator type.
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
        try (CloseableHttpResponse response = adminPost(url, idpJson)) {
            String body = EntityUtils.toString(response.getEntity());
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK) {
                JSONObject jsonResponse = (JSONObject) new JSONParser().parse(body);
                String createdIdpId = jsonResponse.get("id").toString();
                vpIdpName = generatedIdpName;
                // Maps the IDP's "given_name" claim so the subject identifier resolves to it.
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
    }

    /**
     * Create an OAuth2 application with the VP IDP wired into step 1 of its auth sequence.
     * Uses the REST API v1 to create the application with the auth sequence included from the start.
     * Returns the server-assigned application ID.
     */
    private String createOAuthApplicationWithVPIdp() throws Exception {

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

    /**
     * Provision all infrastructure for the secondary-tenant auth flow test.
     * Best-effort: logs warnings if individual steps fail so the remaining tests can still run.
     */
    private void provisionTenantB() {

        try {
            tenantMgtRestClient = new TenantMgtRestClient(serverURL, isServer.getContextTenant());

            Owner owner = new Owner();
            owner.setUsername("admin");
            owner.setPassword(TENANT_AUTH_PASSWORD);
            owner.setEmail(TENANT_AUTH_ADMIN);
            owner.setFirstname("Admin");
            owner.setLastname("TenantAuth");
            owner.setProvisioningMethod("inline-password");

            TenantModel tenantModel = new TenantModel();
            tenantModel.setDomain(TENANT_AUTH_DOMAIN);
            tenantModel.addOwnersItem(owner);
            tenantMgtRestClient.addTenant(tenantModel);

            User tenantBUser = new User();
            tenantBUser.setUserName(TENANT_AUTH_ADMIN);
            tenantBUser.setPassword(TENANT_AUTH_PASSWORD);
            Tenant tenantB = new Tenant();
            tenantB.setDomain(TENANT_AUTH_DOMAIN);
            tenantB.setContextUser(tenantBUser);
            vpSessionRestClientTenantB = new VPSessionRestClient(serverURL, tenantB);

            String tenantApiPrefix = serverURL + "/t/" + TENANT_AUTH_DOMAIN;
            String responseModeBody = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"direct_post\"}";
            try (CloseableHttpResponse r = tenantBAdminPut(
                    tenantApiPrefix + "/api/server/v1/configs/openid4vp", responseModeBody)) {
                EntityUtils.consume(r.getEntity());
            }

            String defJson = readResourceAsString(
                    "org/wso2/identity/integration/test/oid4vp/create-vp-presentation-definition-flow.json");
            try (CloseableHttpResponse r = tenantBAdminPost(
                    tenantApiPrefix + PRESENTATION_DEFINITIONS_PATH, defJson)) {
                String body = EntityUtils.toString(r.getEntity());
                int status = r.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
                    log.warn("Tenant-B definition creation failed (status " + status + "): " + body);
                    return;
                }
                tenantBDefinitionId = ((JSONObject) new JSONParser().parse(body)).get("id").toString();
            }

            String pemCert = buildSelfSignedPemCertificate(issuerSigningKey);
            String base64Pem = java.util.Base64.getEncoder()
                    .encodeToString(pemCert.getBytes(StandardCharsets.UTF_8));
            String issuerBody = "{\"issuerConfigs\":[{"
                    + "\"issuerUrl\":\"" + ISSUER_URL + "\","
                    + "\"keySourceType\":\"pem\","
                    + "\"keySource\":\"" + base64Pem + "\""
                    + "}]}";
            try (CloseableHttpResponse r = tenantBAdminPut(
                    tenantApiPrefix + PRESENTATION_DEFINITIONS_PATH + "/" + tenantBDefinitionId
                            + "/credentials/" + CREDENTIAL_IDENTIFIER + "/issuer-configs", issuerBody)) {
                EntityUtils.consume(r.getEntity());
            }

            tenantBIdpName = "VPTestIDP-TenantB-" + UUID.randomUUID();
            String idpJson = "{"
                    + "\"name\": \"" + tenantBIdpName + "\","
                    + "\"description\": \"VP Authenticator IDP for tenant B integration tests\","
                    + "\"federatedAuthenticators\": {"
                    + "  \"defaultAuthenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                    + "  \"authenticators\": [{"
                    + "    \"authenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                    + "    \"isEnabled\": true,"
                    + "    \"properties\": [{"
                    + "      \"key\": \"presentationDefinitionId\","
                    + "      \"value\": \"" + tenantBDefinitionId + "\""
                    + "    }]"
                    + "  }]"
                    + "}"
                    + "}";
            try (CloseableHttpResponse r = tenantBAdminPost(
                    tenantApiPrefix + "/api/server/v1/identity-providers", idpJson)) {
                String body = EntityUtils.toString(r.getEntity());
                int status = r.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
                    log.warn("Tenant-B IDP creation failed (status " + status + "): " + body);
                    return;
                }
                tenantBIdpId = ((JSONObject) new JSONParser().parse(body)).get("id").toString();
            }

            String appName = "vp-auth-flow-tenant-b-app-" + UUID.randomUUID();
            String appJson = "{"
                    + "\"name\": \"" + appName + "\","
                    + "\"inboundProtocolConfiguration\": {"
                    + "  \"oidc\": {"
                    + "    \"grantTypes\": [\"authorization_code\"],"
                    + "    \"callbackURLs\": [\"" + CALLBACK_URL + "\"]"
                    + "  }"
                    + "},"
                    + "\"authenticationSequence\": {"
                    + "  \"type\": \"USER_DEFINED\","
                    + "  \"steps\": [{"
                    + "    \"id\": 1,"
                    + "    \"options\": [{"
                    + "      \"idp\": \"" + tenantBIdpName + "\","
                    + "      \"authenticator\": \"PresentationAuthenticator\""
                    + "    }]"
                    + "  }]"
                    + "}"
                    + "}";
            try (CloseableHttpResponse r = tenantBAdminPost(
                    tenantApiPrefix + "/api/server/v1/applications", appJson)) {
                String body = EntityUtils.toString(r.getEntity());
                int status = r.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_OK) {
                    log.warn("Tenant-B app creation failed (status " + status + "): " + body);
                    return;
                }
                tenantBAppId = ((JSONObject) new JSONParser().parse(body)).get("id").toString();
            }

            String oidcUrl = tenantApiPrefix + "/api/server/v1/applications/" + tenantBAppId
                    + "/inbound-protocols/oidc";
            try (CloseableHttpResponse r = tenantBAdminGet(oidcUrl)) {
                String body = EntityUtils.toString(r.getEntity());
                int status = r.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    log.warn("Tenant-B OIDC config retrieval failed (status " + status + "): " + body);
                    return;
                }
                JSONObject oidcConfig = (JSONObject) new JSONParser().parse(body);
                tenantBConsumerKey = (String) oidcConfig.get("clientId");
            }

        } catch (Exception e) {
            log.warn("provisionTenantB failed: " + e.getMessage()
                    + ".  testAuthFlowTenanted will be skipped.");
        }
    }

    private String buildAuthorizationUrl() {

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
    private static class AuthFlowContext {
        String requestId;
        String sessionDataKey;
        String walletLoginUrl;
        String nonce;
        String audience;
        BasicCookieStore cookieStore;
    }

    /**
     * Initiate the auth flow, follow redirects to extract the requestId and sessionDataKey,
     * then fetch the nonce + audience from the presentation request JWT.
     */
    private AuthFlowContext initiateAuthFlowAndFetchNonce() throws Exception {

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
                // KB-JWT aud must be the verifier's client_id, not the request JWT's aud.
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
    private AuthFlowContext startAuthFlowAndExtractContext() {

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

    private String buildTenantBAuthorizationUrl() {

        String base = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, TENANT_AUTH_DOMAIN);
        return base
                + "?response_type=code"
                + "&client_id=" + tenantBConsumerKey
                + "&redirect_uri=" + CALLBACK_URL
                + "&scope=openid+profile"
                + "&state=" + UUID.randomUUID();
    }

    /**
     * Initiate the tenant-B auth flow, follow redirects to extract requestId and sessionDataKey,
     * then fetch the nonce + audience from the presentation request JWT.
     */
    private AuthFlowContext initiateAuthFlowTenantBAndFetchNonce() throws Exception {

        String authUrl = buildTenantBAuthorizationUrl();
        BasicCookieStore cookieStore = new BasicCookieStore();
        AuthFlowContext ctx = null;

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
                        ctx = new AuthFlowContext();
                        ctx.requestId = extractQueryParam(location, "requestId");
                        ctx.sessionDataKey = extractQueryParam(location, "sessionDataKey");
                        ctx.walletLoginUrl = location;
                        ctx.cookieStore = cookieStore;
                        break;
                    }
                    currentUrl = location;
                }
            }
        } catch (Exception e) {
            log.warn("initiateAuthFlowTenantBAndFetchNonce: redirect follow failed: " + e.getMessage());
        }

        if (ctx == null || ctx.requestId == null) {
            log.warn("Could not extract requestId from tenant-B auth flow.");
            return null;
        }

        try (CloseableHttpResponse jwtResponse = vpSessionRestClientTenantB.getPresentationRequestJwt(ctx.requestId)) {
            String jwtBody = EntityUtils.toString(jwtResponse.getEntity()).trim();
            if (jwtBody.contains(".")) {
                SignedJWT requestJwt = SignedJWT.parse(jwtBody);
                ctx.nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
                ctx.audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            }
        } catch (Exception e) {
            log.warn("Could not fetch tenant-B presentation request JWT nonce: " + e.getMessage());
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
     * POST to IS tenant-B commonauth to complete the callback.
     * Returns the authorization code if the flow produces one, or {@code null}.
     */
    private String triggerTenantBCommonAuthCallback(String sessionDataKey, String requestId, String status,
            BasicCookieStore cookieStore) throws Exception {

        String commonAuthUrl = serverURL + "/t/" + TENANT_AUTH_DOMAIN + "/" + COMMONAUTH_PATH;

        try (CloseableHttpClient httpClient = buildNoRedirectHttpClient(cookieStore)) {
            HttpPost post = new HttpPost(commonAuthUrl);
            List<org.apache.http.NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            nvps.add(new BasicNameValuePair("vp_request_id", requestId));
            nvps.add(new BasicNameValuePair("status", status));
            post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(nvps));
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                EntityUtils.consume(response.getEntity());
                String location = getLocationHeader(response);
                if (location != null && location.contains("code=")) {
                    return extractQueryParam(location, "code");
                }
                return null;
            }
        } catch (Exception e) {
            log.warn("triggerTenantBCommonAuthCallback failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * POST to IS commonauth with the sessionDataKey and requestId to complete the callback.
     * Returns the authorization code if the flow succeeds, or {@code null}.
     *
     * IS auth flow produces multiple redirects:
     *   POST /commonauth → 302 /oauth2/authorize → 302 https://localhost/callback?code=...
     * We follow IS-internal hops until we reach the client callback URL that carries the code.
     */
    private String triggerCommonAuthCallback(String sessionDataKey, String requestId, String vpStatus,
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
    private String triggerCommonAuthCallbackAndGetRedirect(String sessionDataKey, String requestId, String status,
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

    private JSONObject exchangeCodeForTokens(String code) throws Exception {

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
                return (JSONObject) new JSONParser().parse(body);
            }
        }
    }

    private CloseableHttpClient buildNoRedirectHttpClient() {

        return buildNoRedirectHttpClient(null);
    }

    private CloseableHttpClient buildNoRedirectHttpClient(BasicCookieStore cookieStore) {

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

    private void configureTrustedIssuer(String definitionId, String credentialId,
                                        String issuerUrl, ECKey signingKey) throws Exception {

        String pemCert = buildSelfSignedPemCertificate(signingKey);
        String base64Pem = java.util.Base64.getEncoder()
                .encodeToString(pemCert.getBytes(StandardCharsets.UTF_8));
        String body = "{\"issuerConfigs\":[{"
                + "\"issuerUrl\":\"" + issuerUrl + "\","
                + "\"keySourceType\":\"pem\","
                + "\"keySource\":\"" + base64Pem + "\""
                + "}]}";
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId
                + "/credentials/" + credentialId + "/issuer-configs";
        try (CloseableHttpResponse response = adminPut(url, body)) {
            int status = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "Failed to configure trusted issuer (status " + status + "): " + responseBody);
        }
    }

    private static String buildSelfSignedPemCertificate(ECKey ecKey) throws Exception {

        X500Name dn = new X500Name("CN=test-issuer");
        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(ecKey.toECPublicKey().getEncoded());

        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 86400000L);
        ASN1EncodableVector validityVec = new ASN1EncodableVector();
        validityVec.add(new DERUTCTime(notBefore));
        validityVec.add(new DERUTCTime(notAfter));

        ASN1EncodableVector tbsVec = new ASN1EncodableVector();
        tbsVec.add(new DERTaggedObject(true, 0, new ASN1Integer(2)));
        tbsVec.add(new ASN1Integer(BigInteger.ONE));
        tbsVec.add(sigAlgId);
        tbsVec.add(dn);
        tbsVec.add(new DERSequence(validityVec));
        tbsVec.add(dn);
        tbsVec.add(spki);
        DERSequence tbsCert = new DERSequence(tbsVec);

        java.security.Signature sig = java.security.Signature.getInstance("SHA256withECDSA");
        sig.initSign(ecKey.toECPrivateKey());
        sig.update(tbsCert.getEncoded());
        byte[] sigBytes = sig.sign();

        ASN1EncodableVector certVec = new ASN1EncodableVector();
        certVec.add(tbsCert);
        certVec.add(sigAlgId);
        certVec.add(new DERBitString(sigBytes));
        byte[] certBytes = new DERSequence(certVec).getEncoded();

        String base64 = java.util.Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(certBytes);
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----";
    }

    private void configureVPResponseMode(String responseMode) throws IOException {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"" + responseMode + "\"}";
        String url = serverURL + "/api/server/v1/configs/openid4vp";
        try (CloseableHttpResponse response = adminPut(url, body)) {
            int status = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "Failed to configure VP response mode (status " + status + "): " + responseBody);
        }
    }

    private CloseableHttpResponse adminPost(String url, String json) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeaders(buildAdminHeaders());
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(post);
    }

    private CloseableHttpResponse adminPut(String url, String json) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpPut put = new org.apache.http.client.methods.HttpPut(url);
        put.setHeaders(buildAdminHeaders());
        put.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(put);
    }

    private CloseableHttpResponse adminDelete(String url) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpDelete delete = new org.apache.http.client.methods.HttpDelete(url);
        delete.setHeaders(buildAdminHeaders());
        return httpClient.execute(delete);
    }

    private Header[] buildAdminHeaders() {

        String credentials = userInfo.getUserName() + ":" + userInfo.getPassword();
        String encoded = Base64.encodeBase64String(credentials.getBytes(StandardCharsets.UTF_8)).trim();
        return new Header[]{
                new BasicHeader("Authorization", "Basic " + encoded),
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT)
        };
    }

    private static String getLocationHeader(CloseableHttpResponse response) {

        org.apache.http.Header locationHeader = response.getFirstHeader("Location");
        return locationHeader != null ? locationHeader.getValue() : null;
    }

    private static String extractQueryParam(String url, String paramName) {

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
            // Fragment-only or malformed URL — try raw split.
            int idx = url.indexOf(paramName + "=");
            if (idx >= 0) {
                String rest = url.substring(idx + paramName.length() + 1);
                int end = rest.indexOf('&');
                return end >= 0 ? rest.substring(0, end) : rest;
            }
        }
        return null;
    }

    private static String readResourceAsString(String resourcePath) throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = VPAuthenticatorFlowTestCase.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            Assert.assertNotNull(in, "Test resource not found on classpath: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs) throws Exception {

        return pollStatusUntilNotActive(requestId, maxRetries, intervalMs, vpSessionRestClient);
    }

    private String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs,
            VPSessionRestClient client) throws Exception {

        String status = "ACTIVE";
        for (int i = 0; i < maxRetries; i++) {
            JSONObject statusResponse = client.getSessionStatus(requestId);
            status = (String) statusResponse.get("status");
            if (!"ACTIVE".equals(status)) {
                break;
            }
            if (i < maxRetries - 1) {
                Thread.sleep(intervalMs);
            }
        }
        return status;
    }

    private CloseableHttpResponse tenantBAdminPost(String url, String json) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeaders(buildTenantBAdminHeaders());
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(post);
    }

    private CloseableHttpResponse tenantBAdminPut(String url, String json) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpPut put = new org.apache.http.client.methods.HttpPut(url);
        put.setHeaders(buildTenantBAdminHeaders());
        put.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(put);
    }

    private CloseableHttpResponse tenantBAdminDelete(String url) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpDelete delete =
                new org.apache.http.client.methods.HttpDelete(url);
        delete.setHeaders(buildTenantBAdminHeaders());
        return httpClient.execute(delete);
    }

    private CloseableHttpResponse tenantBAdminGet(String url) throws IOException {

        CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpGet get = new org.apache.http.client.methods.HttpGet(url);
        get.setHeaders(buildTenantBAdminHeaders());
        return httpClient.execute(get);
    }

    private Header[] buildTenantBAdminHeaders() {

        String credentials = TENANT_AUTH_ADMIN + ":" + TENANT_AUTH_PASSWORD;
        String encoded = Base64.encodeBase64String(credentials.getBytes(StandardCharsets.UTF_8)).trim();
        return new Header[]{
                new BasicHeader("Authorization", "Basic " + encoded),
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT)
        };
    }
}
