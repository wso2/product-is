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

import com.nimbusds.jwt.SignedJWT;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Success-path integration tests for the VP Authenticator flow.
 */
public class VPAuthenticatorFlowSuccessTestCase extends VPAuthenticatorFlowTestBase {

    /** Snapshot of the global VP config taken in setUp so tearDown can restore it. */
    private String savedClientIdScheme;
    private String savedResponseMode;

    private static final String TENANT_AUTH_DOMAIN   = "vptest-tenant-auth.com";
    private static final String TENANT_AUTH_ADMIN    = "admin@vptest-tenant-auth.com";
    private static final String TENANT_AUTH_PASSWORD = "Admin@123";

    private SCIM2RestClient scim2RestClient;
    private String jitProvisionedUserId;

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

        Response configResponse = adminGet(serverBaseUrl() + "/api/server/v1/configs/openid4vp");
        if (configResponse.getStatusCode() == HttpStatus.SC_OK) {
            JSONObject existing = new JSONObject(configResponse.getBody().asString());
            savedClientIdScheme = existing.optString("clientIdScheme", "x509_san_dns");
            savedResponseMode = existing.optString("responseMode", "direct_post");
        } else {
            savedClientIdScheme = "x509_san_dns";
            savedResponseMode = "direct_post";
        }

        configureVPResponseMode("direct_post");
        presentationDefinitionId = createPresentationDefinition();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, isServer.getContextTenant());
        idpId = createVPAuthenticatorIDP();
        appId = createOAuthApplicationWithVPIdp();
        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());
        scim2RestClient = new SCIM2RestClient(serverURL, isServer.getContextTenant());
        deleteUserIfExists("alice@example.com");
        deleteUserIfExists("jit.testuser@example.com");
        provisionTenantB();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        deleteApplicationIfExists();
        deleteIdpIfExists();
        deletePresentationDefinitionQuietly();
        closeVPSessionClient();
        closeIdpMgtClient();
        closeOAuthRestClient();
        if (jitProvisionedUserId != null) {
            try {
                scim2RestClient.deleteUser(jitProvisionedUserId);
            } catch (Exception e) {
                log.warn("Failed to delete JIT-provisioned user: " + e.getMessage());
            }
        }
        deleteUserIfExists("alice@example.com");
        if (savedClientIdScheme != null) {
            try {
                String restoreJson = "{\"clientIdScheme\":\"" + savedClientIdScheme
                        + "\",\"responseMode\":\"" + savedResponseMode + "\"}";
                adminPut(serverBaseUrl() + "/api/server/v1/configs/openid4vp", restoreJson);
            } catch (Exception e) {
                log.warn("Failed to restore VP config in tearDown: " + e.getMessage());
            }
        }
        if (scim2RestClient != null) {
            try {
                scim2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close SCIM2 client: " + e.getMessage());
            }
        }

        String tenantApiPrefix = serverBaseUrl() + "/t/" + TENANT_AUTH_DOMAIN;
        if (tenantBAppId != null) {
            try {
                adminDelete(tenantApiPrefix + "/api/server/v1/applications/" + tenantBAppId,
                        TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B app: " + e.getMessage());
            }
        }
        if (tenantBIdpId != null) {
            try {
                adminDelete(tenantApiPrefix + "/api/server/v1/identity-providers/" + tenantBIdpId,
                        TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B IDP: " + e.getMessage());
            }
        }
        if (tenantBDefinitionId != null) {
            try {
                adminDelete(tenantApiPrefix + PRESENTATION_DEFINITIONS_PATH + "/" + tenantBDefinitionId,
                        TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);
            } catch (Exception e) {
                log.warn("Failed to delete tenant-B presentation definition: " + e.getMessage());
            }
        }
        try {
            new TenantManagementServiceClient(backendURL, sessionCookie).deleteTenant(TENANT_AUTH_DOMAIN);
        } catch (Exception e) {
            log.warn("Failed to delete tenant-B domain '" + TENANT_AUTH_DOMAIN + "': " + e.getMessage());
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
                .claim("email", "alice@example.com")
                .nonce(ctx.nonce)
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            int status = vpResponse.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(vpResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "VP submission during auth flow must return 200. Body: " + body);
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "VERIFIED",
                "VP token from configured trusted issuer with correct nonce must result in a VERIFIED session.");

        String code = triggerCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, finalStatus, ctx.cookieStore);
        Assert.assertNotNull(code,
                "After successful VP verification the auth code must be returned in the redirect.");

        JSONObject tokenResponse = exchangeCodeForTokens(code);
        Assert.assertNotNull(tokenResponse.opt("access_token"),
                "Token endpoint must return an access_token after a successful VP auth flow.");
        Assert.assertNotNull(tokenResponse.opt("id_token"),
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
                .claim("email", "alice@example.com")
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
        String idToken = tokenResponse.optString("id_token", null);
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String[] jwtParts = idToken.split("\\.");
        Assert.assertEquals(jwtParts.length, 3, "id_token must be a compact-serialized JWT with 3 parts.");

        String payloadJson = new String(
                java.util.Base64.getUrlDecoder().decode(jwtParts[1]),
                StandardCharsets.UTF_8);
        JSONObject claims = new JSONObject(payloadJson);

        Assert.assertNotNull(claims.opt("sub"),
                "id_token must contain a 'sub' claim.");
        Assert.assertEquals(claims.opt("given_name"), "Alice",
                "id_token 'given_name' must reflect the VP credential's 'given_name' value via the IDP claim mapping.");
        Assert.assertEquals(claims.opt("family_name"), "Smith",
                "id_token 'family_name' must reflect the VP credential's 'family_name' value via the IDP claim mapping.");
    }

    @Test(groups = "wso2.is",
            description = "JIT provisioning creates a local user account on first successful VP login.")
    public void testJITProvisioningCreatesUserOnFirstLogin() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

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
        String idToken = tokenResponse.optString("id_token", null);
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String payload = new String(
                java.util.Base64.getUrlDecoder().decode(idToken.split("\\.")[1]),
                StandardCharsets.UTF_8);
        JSONObject idTokenClaims = new JSONObject(payload);
        String sub = idTokenClaims.optString("sub", null);
        Assert.assertNotNull(sub, "id_token must contain a 'sub' claim.");

        String jitUsername = "jit.testuser@example.com";
        String filter = java.net.URLEncoder.encode("userName eq \"" + jitUsername + "\"", StandardCharsets.UTF_8);
        JSONObject users = new JSONObject(scim2RestClient.filterUsers(filter).toJSONString());
        long totalResults = users.getLong("totalResults");
        Assert.assertEquals(totalResults, 1L,
                "JIT provisioning must create exactly one local user with userName='" + jitUsername + "'.");

        jitProvisionedUserId = users.getJSONArray("Resources").getJSONObject(0).getString("id");
    }

    @Test(groups = "wso2.is",
            description = "A second VP login by the same subject does not create a duplicate local user.",
            dependsOnMethods = "testJITProvisioningCreatesUserOnFirstLogin")
    public void testJITProvisioningDoesNotDuplicateUser() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

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
        String idToken = tokenResponse.optString("id_token", null);
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token on second login.");

        String payload = new String(
                java.util.Base64.getUrlDecoder().decode(idToken.split("\\.")[1]),
                StandardCharsets.UTF_8);
        JSONObject idTokenClaims = new JSONObject(payload);
        String sub = idTokenClaims.optString("sub", null);
        Assert.assertNotNull(sub, "id_token must contain a 'sub' claim.");

        String jitUsername = "jit.testuser@example.com";
        String filter = java.net.URLEncoder.encode("userName eq \"" + jitUsername + "\"", StandardCharsets.UTF_8);
        JSONObject users = new JSONObject(scim2RestClient.filterUsers(filter).toJSONString());
        long totalResults = users.getLong("totalResults");
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

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("email", "alice@example.com")
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
        String idToken = tokenResponse.optString("id_token", null);
        Assert.assertNotNull(idToken, "Token endpoint must return an id_token.");

        String[] jwtParts = idToken.split("\\.");
        Assert.assertEquals(jwtParts.length, 3, "id_token must be a compact-serialized JWT with 3 parts.");

        JSONObject claims = new JSONObject(
                new String(java.util.Base64.getUrlDecoder().decode(jwtParts[1]), StandardCharsets.UTF_8));

        Assert.assertNotNull(claims.opt("sub"),
                "id_token must contain a 'sub' claim.");
        Assert.assertEquals(claims.opt("given_name"), "Alice",
                "id_token must contain 'given_name' mapped from the VP credential.");
    }

    @Test(groups = "wso2.is",
            description = "VP authenticator flow works correctly in a secondary tenant.")
    public void testAuthFlowTenanted() throws Exception {

        if (tenantBConsumerKey == null) {
            throw new SkipException("Tenant-B OAuth2 app not provisioned — skipping testAuthFlowTenanted.");
        }

        AuthFlowContext ctx = initiateAuthFlowTenantBAndFetchNonce();
        Assert.assertNotNull(ctx, "Tenant-B auth flow context must not be null.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("family_name", "Smith")
                .claim("email", "alice@example.com")
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
        Assert.assertEquals(finalStatus, "VERIFIED",
                "Tenant-B VP token from trusted issuer with correct nonce must result in a VERIFIED session.");

        Assert.assertNotNull(ctx.sessionDataKey,
                "Tenant-B wallet login redirect must carry a sessionDataKey parameter.");
        String code = triggerTenantBCommonAuthCallback(ctx.sessionDataKey, ctx.requestId, "success",
                ctx.cookieStore);
        Assert.assertNotNull(code, "Tenant-B auth flow must return an authorization code.");
        Assert.assertFalse(code.isEmpty(),
                "Tenant-B auth flow must return a non-empty authorization code.");
    }

    /** Deletes all users matching the given username via SCIM2. */
    private void deleteUserIfExists(String userName) {

        VPTestUtils.deleteUserIfExists(userName, scim2RestClient);
    }

    /**
     * Provision all infrastructure for the secondary-tenant auth flow test.
     */
    private void provisionTenantB() throws Exception {

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

        String tenantApiPrefix = serverBaseUrl() + "/t/" + TENANT_AUTH_DOMAIN;

        configureVPResponseMode("direct_post", tenantApiPrefix, TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);

        tenantBDefinitionId = createPresentationDefinition(tenantApiPrefix,
                TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);

        configureTrustedIssuer(tenantBDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey,
                tenantApiPrefix, TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);

        tenantBIdpName = "VPTestIDP-TenantB-" + UUID.randomUUID();
        tenantBIdpId = createVPAuthenticatorIDP(tenantBDefinitionId, tenantBIdpName,
                tenantApiPrefix, TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);

        configureIdpClaims(tenantBIdpId, tenantApiPrefix, TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);

        String[] tenantBApp = createOAuthApp(tenantBIdpName, tenantApiPrefix,
                TENANT_AUTH_ADMIN, TENANT_AUTH_PASSWORD);
        tenantBAppId = tenantBApp[0];
        tenantBConsumerKey = tenantBApp[1];
    }

    /** Builds the OAuth2 authorization URL for the tenant-B test application. */
    private String buildTenantBAuthorizationUrl() {

        String base = getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, TENANT_AUTH_DOMAIN);
        return base
                + "?response_type=code"
                + "&client_id=" + tenantBConsumerKey
                + "&redirect_uri=" + CALLBACK_URL
                + "&scope=openid+profile"
                + "&state=" + UUID.randomUUID();
    }

    private AuthFlowContext initiateAuthFlowTenantBAndFetchNonce() throws Exception {

        AuthFlowContext ctx = startAuthFlowAndExtractContext(buildTenantBAuthorizationUrl());
        if (ctx == null || ctx.requestId == null) {
            log.warn("Could not extract requestId from tenant-B auth flow.");
            return null;
        }

        try (CloseableHttpResponse jwtResponse = vpSessionRestClientTenantB.getPresentationRequestJwt(ctx.requestId)) {
            String jwtBody = EntityUtils.toString(jwtResponse.getEntity()).trim();
            Assert.assertEquals(jwtResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200 for tenant-B. Body: " + jwtBody);
            SignedJWT requestJwt = SignedJWT.parse(jwtBody);
            ctx.nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            ctx.audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
        }
        Assert.assertNotNull(ctx.nonce, "Tenant-B presentation request JWT must contain 'nonce'.");
        Assert.assertNotNull(ctx.audience, "Tenant-B presentation request JWT must contain 'client_id'.");
        return ctx;
    }

    private String triggerTenantBCommonAuthCallback(String sessionDataKey, String requestId, String status,
            BasicCookieStore cookieStore) throws Exception {

        String commonAuthUrl = serverBaseUrl() + "/t/" + TENANT_AUTH_DOMAIN + "/" + COMMONAUTH_PATH;
        String location = triggerCommonAuthCallbackAndGetRedirect(commonAuthUrl, sessionDataKey, requestId,
                status, cookieStore);

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
            log.warn("triggerTenantBCommonAuthCallback redirect follow failed: " + e.getMessage());
        }

        return location != null ? extractQueryParam(location, "code") : null;
    }
}
