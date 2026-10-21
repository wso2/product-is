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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
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
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Integration tests covering the happy-path scenarios for the VP verification session API.
 */
public class VPVerificationSessionSuccessTestCase extends VPVerificationSessionTestBase {

    private static final int    POLL_MAX_RETRIES   = 15;
    private static final long   POLL_INTERVAL_MS   = 500L;

    /** The signing key used for both issuer credential and trusted-issuer registration. */
    private static ECKey issuerSigningKey;

    /** Issuer URL registered as trusted for PersonCredential. */
    private static final String ISSUER_URL = "https://test-issuer.example.com";

    /** Secondary tenant used to verify cross-tenant isolation (test 9). */
    private static final String TENANT_B_DOMAIN   = "vptest-tenant-b.com";
    private static final String TENANT_B_ADMIN    = "admin@vptest-tenant-b.com";
    private static final String TENANT_B_PASSWORD = "Admin@123";

    /** Presentation definition server-assigned ID, captured in @BeforeClass. */
    private String presentationDefinitionId;

    /** IDP with PresentationAuthenticator — triggers ECDSA key provisioning via VPIdPMgtListener. */
    private String walletConnectionIdpId;

    /** VP session client scoped to the secondary tenant (test 9). */
    private VPSessionRestClient vpSessionRestClientTenantB;

    /** Tenant management client for creating the secondary tenant in setUp. */
    private TenantMgtRestClient tenantMgtRestClient;

    /**
     * The requestId of the session started in test 1 — shared by tests 2, 3, 4, 5.
     * After test 5 the session enters VERIFIED state.
     */
    private String sharedRequestId;

    /** Nonce returned when the presentation request JWT was fetched (test 4). */
    private String sharedNonce;

    /** Audience (verifier URI) returned with the presentation request JWT (test 4). */
    private String sharedAudience;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        issuerSigningKey = SDJwtVcTestBuilder.generateSigningKey();
        configureVPResponseMode("direct_post");
        walletConnectionIdpId = createDigitalWalletConnection();
        presentationDefinitionId = createPresentationDefinition();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        initVPSessionClient();
        provisionTenantB();
    }

    /** Creates the secondary tenant (tenant-B) and initialises the tenant-scoped VP session client used by the cross-tenant isolation test. */
    private void provisionTenantB() throws Exception {

        tenantMgtRestClient = new TenantMgtRestClient(serverURL, isServer.getContextTenant());

        Owner owner = new Owner();
        owner.setUsername("admin");
        owner.setPassword(TENANT_B_PASSWORD);
        owner.setEmail(TENANT_B_ADMIN);
        owner.setFirstname("Admin");
        owner.setLastname("TenantB");
        owner.setProvisioningMethod("inline-password");

        TenantModel tenantModel = new TenantModel();
        tenantModel.setDomain(TENANT_B_DOMAIN);
        tenantModel.addOwnersItem(owner);
        tenantMgtRestClient.addTenant(tenantModel);

        User tenantBUser = new User();
        tenantBUser.setUserName(TENANT_B_ADMIN);
        tenantBUser.setPassword(TENANT_B_PASSWORD);

        Tenant tenantB = new Tenant();
        tenantB.setDomain(TENANT_B_DOMAIN);
        tenantB.setContextUser(tenantBUser);

        vpSessionRestClientTenantB = new VPSessionRestClient(serverURL, tenantB);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        try {
            deletePresentationDefinition(presentationDefinitionId);
        } catch (Exception e) {
            log.warn("Failed to delete presentation definition: " + e.getMessage());
        }
        try {
            deleteDigitalWalletConnection(walletConnectionIdpId);
        } catch (Exception e) {
            log.warn("Failed to delete wallet connection IDP: " + e.getMessage());
        }
        try {
            closeVPSessionClient();
        } catch (Exception e) {
            log.warn("Failed to close VP session client: " + e.getMessage());
        }
        if (vpSessionRestClientTenantB != null) {
            try {
                vpSessionRestClientTenantB.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close tenant-B VP session client: " + e.getMessage());
            }
        }
        if (tenantMgtRestClient != null) {
            try {
                tenantMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close tenant management client: " + e.getMessage());
            }
        }
    }

    @Test(groups = "wso2.is",
            description = "Starting a VP verification session returns requestId, walletUrl and requestUri.")
    public void testStartVerificationSession() throws Exception {

        JSONObject sessionResponse = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);

        sharedRequestId = (String) sessionResponse.get("requestId");
        Assert.assertNotNull(sharedRequestId,
                "startVerificationSession response must contain 'requestId'.");
        Assert.assertFalse(sharedRequestId.trim().isEmpty(),
                "'requestId' must not be blank.");

        String walletUrl = (String) sessionResponse.get("walletUrl");
        Assert.assertNotNull(walletUrl,
                "startVerificationSession response must contain 'walletUrl'.");
        Assert.assertTrue(walletUrl.startsWith("openid4vp://"),
                "'walletUrl' must start with 'openid4vp://' but was: " + walletUrl);

        String requestUri = (String) sessionResponse.get("requestUri");
        Assert.assertNotNull(requestUri,
                "startVerificationSession response must contain 'requestUri'.");
        Assert.assertTrue(requestUri.startsWith("https://"),
                "'requestUri' must be an HTTPS URL but was: " + requestUri);
    }

    @Test(groups = "wso2.is",
            description = "Polling session status before any VP submission returns ACTIVE.",
            dependsOnMethods = "testStartVerificationSession")
    public void testPollStatusWhileActive() throws Exception {

        JSONObject statusResponse = vpSessionRestClient.getSessionStatus(sharedRequestId);

        String status = (String) statusResponse.get("status");
        Assert.assertEquals(status, STATUS_ACTIVE,
                "Session status must be ACTIVE before any VP is submitted.");
    }

    @Test(groups = "wso2.is",
            description = "Fetching the session result while the session is still ACTIVE returns 400.",
            dependsOnMethods = "testPollStatusWhileActive")
    public void testGetResultWhileActive() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getSessionResult(sharedRequestId)) {
            int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(statusCode, HttpStatus.SC_BAD_REQUEST,
                    "Fetching result of an ACTIVE session must return 400.");
        }
    }

    @Test(groups = "wso2.is",
            description = "GET /requests/{id} returns a compact JWT with the presentation request.",
            dependsOnMethods = "testGetResultWhileActive")
    public void testFetchPresentationRequestJwt() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(sharedRequestId)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200.  Body: " + body);

            // The response must be a compact JWT (exactly two dots → three parts).
            String compactJwt = body.trim();
            Assert.assertEquals(compactJwt.split("\\.").length, 3,
                    "Presentation request response must be a compact JWT with exactly 3 parts (header.payload.signature).");

            // Parse and extract nonce + audience for use in the VP submission test.
            SignedJWT requestJwt = SignedJWT.parse(compactJwt);
            sharedNonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            Assert.assertNotNull(sharedNonce, "Presentation request JWT must contain a 'nonce' claim.");
            Assert.assertFalse(sharedNonce.trim().isEmpty(), "'nonce' claim must not be blank.");

            // The KB-JWT aud must be the verifier's client_id, not the request JWT's aud
            // (which is the fixed protocol audience "https://self-issued.me/v2").
            sharedAudience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            Assert.assertNotNull(sharedAudience, "Presentation request JWT must contain a 'client_id' claim.");
            Assert.assertFalse(sharedAudience.trim().isEmpty(), "'client_id' claim must not be blank.");
        }
    }

    @Test(groups = "wso2.is",
            description = "Submitting a structurally valid SD-JWT-VC via direct_post returns 200.",
            dependsOnMethods = "testFetchPresentationRequestJwt")
    public void testSubmitValidVPDirectPost() throws Exception {

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .issuer("https://test-issuer.example.com")
                .claim("given_name", "Alice")
                .claim("family_name", "Smith")
                .nonce(sharedNonce)
                .audience(sharedAudience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(sharedRequestId, vpToken)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                    "Submitting a valid VP via direct_post must return 200.  Body: " + body);
        }
    }

    @Test(groups = "wso2.is",
            description = "After a valid VP is submitted the session status transitions to VERIFIED.",
            dependsOnMethods = "testSubmitValidVPDirectPost")
    public void testPollStatusAfterVerification() throws Exception {

        String finalStatus = pollStatusUntilNotActive(sharedRequestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_VERIFIED,
                "Session status must be VERIFIED after a valid VP is submitted.");
    }

    @Test(groups = "wso2.is",
            description = "GET /verification-sessions/{id} on a VERIFIED session returns 200 with status=VERIFIED.",
            dependsOnMethods = "testPollStatusAfterVerification")
    public void testGetVerifiedResult() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getSessionResult(sharedRequestId)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                    "GET result of a VERIFIED session must return 200.  Body: " + body);

            JSONObject resultJson = new JSONObject(body);
            String resultStatus = (String) resultJson.get("status");
            Assert.assertEquals(resultStatus, STATUS_VERIFIED,
                    "Session result 'status' field must be VERIFIED.");
        }
    }

    @Test(groups = "wso2.is",
            description = "Two simultaneously started sessions receive distinct requestId values.")
    public void testMultipleConcurrentSessions() throws Exception {

        JSONObject session1 = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        JSONObject session2 = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);

        String requestId1 = (String) session1.get("requestId");
        String requestId2 = (String) session2.get("requestId");

        Assert.assertNotNull(requestId1, "Session 1 must return a requestId.");
        Assert.assertNotNull(requestId2, "Session 2 must return a requestId.");
        Assert.assertNotEquals(requestId1, requestId2,
                "Two distinct sessions must not share the same requestId.");

        // Both sessions should be ACTIVE immediately.
        String status1 = (String) vpSessionRestClient.getSessionStatus(requestId1).get("status");
        String status2 = (String) vpSessionRestClient.getSessionStatus(requestId2).get("status");
        Assert.assertEquals(status1, STATUS_ACTIVE,
                "Session 1 (requestId=" + requestId1 + ") must be ACTIVE immediately after creation.");
        Assert.assertEquals(status2, STATUS_ACTIVE,
                "Session 2 (requestId=" + requestId2 + ") must be ACTIVE immediately after creation.");
    }

    @Test(groups = "wso2.is",
            description = "A session started in the super tenant is not visible from a secondary tenant (returns 404).")
    public void testSessionForTenantA_NotVisibleFromTenantB() throws Exception {

        // Start a session in the super tenant.
        JSONObject sessionResponse = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) sessionResponse.get("requestId");
        Assert.assertNotNull(requestId, "Super-tenant session must return a requestId.");
        Assert.assertFalse(requestId.trim().isEmpty(), "Super-tenant session requestId must not be blank.");

        // Attempt to poll status through tenant-B credentials — session must be invisible.
        try (CloseableHttpResponse response = vpSessionRestClientTenantB.getSessionStatusRaw(requestId)) {
            int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(statusCode, HttpStatus.SC_NOT_FOUND,
                    "A super-tenant session must not be visible from tenant B (expected 404).");
        }
    }

    @Test(groups = "wso2.is",
            description = "Submitting a VP via the direct_post.jwt response mode returns 200 and session reaches VERIFIED.")
    public void testSubmitValidVPDirectPostJwt() throws Exception {

        configureVPResponseMode("direct_post.jwt");
        try {
            // Start a fresh session under direct_post.jwt mode.
            JSONObject sessionResponse = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
            String requestId = (String) sessionResponse.get("requestId");
            Assert.assertNotNull(requestId, "requestId must be present for direct_post.jwt session.");
            Assert.assertFalse(requestId.trim().isEmpty(), "requestId must not be blank.");

            // Fetch the request JWT.
            String compactJwt;
            try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(requestId)) {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                        "GET /requests/{id} must return 200 for direct_post.jwt session.");
                compactJwt = EntityUtils.toString(response.getEntity()).trim();
            }
            SignedJWT requestJwt = SignedJWT.parse(compactJwt);
            JWTClaimsSet requestClaims = requestJwt.getJWTClaimsSet();
            String nonce = requestClaims.getStringClaim("nonce");
            Assert.assertNotNull(nonce, "Presentation request JWT must contain a 'nonce' claim.");
            Assert.assertFalse(nonce.trim().isEmpty(), "'nonce' claim must not be blank.");
            String audience = requestClaims.getStringClaim("client_id");
            Assert.assertNotNull(audience, "Presentation request JWT must contain a 'client_id' claim.");
            Assert.assertFalse(audience.trim().isEmpty(), "'client_id' claim must not be blank.");

            // Extract the ephemeral encryption public key from client_metadata.jwks.keys[0].
            Map<String, Object> clientMetadata = requestClaims.getJSONObjectClaim("client_metadata");
            ECKey ephemeralPublicKey = extractEphemeralPublicKey(clientMetadata);

            // Build the SD-JWT-VC (raw token, nonce + audience bound via KB-JWT).
            String sdJwtVc = SDJwtVcTestBuilder.builder()
                    .vct("PersonCredential")
                    .issuer(ISSUER_URL)
                    .claim("given_name", "Alice")
                    .claim("family_name", "Smith")
                    .nonce(nonce)
                    .audience(audience)
                    .signingKey(issuerSigningKey)
                    .build();

            // Build the inner Authorization Response JWT.
            // vp_token must be a JSON object claim (not a string) — server calls getJSONObjectClaim("vp_token").
            Map<String, Object> vpTokenMap = new LinkedHashMap<>();
            vpTokenMap.put(CREDENTIAL_IDENTIFIER, sdJwtVc);
            JWTClaimsSet innerClaims = new JWTClaimsSet.Builder()
                    .claim("vp_token", vpTokenMap)
                    .issueTime(new Date())
                    .build();
            ECKey holderKey = SDJwtVcTestBuilder.generateSigningKey();
            SignedJWT innerJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), innerClaims);
            innerJwt.sign(new ECDSASigner(holderKey));

            // Encrypt with ECDH-ES + A256GCM; kid=requestId lets the server look up the session.
            JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES, EncryptionMethod.A256GCM)
                    .keyID(requestId)
                    .build();
            JWEObject jweObject = new JWEObject(jweHeader, new Payload(innerJwt));
            jweObject.encrypt(new ECDHEncrypter(ephemeralPublicKey));

            // Submit the JWE and assert 200.
            try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPostJwt(jweObject.serialize())) {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                        "Submitting a valid VP via direct_post.jwt must return 200. Body: " + body);
            }

            // Poll until the session reaches VERIFIED.
            String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
            Assert.assertEquals(finalStatus, STATUS_VERIFIED,
                    "Session must reach VERIFIED after a valid direct_post.jwt VP submission.");
        } finally {
            // Always restore so subsequent tests (or re-runs) see direct_post.
            configureVPResponseMode("direct_post");
        }
    }

    /**
     * Extracts the ephemeral EC public key embedded in the request JWT's
     * {@code client_metadata.jwks.keys[0]} claim.
     */
    @SuppressWarnings("unchecked")
    private ECKey extractEphemeralPublicKey(Map<String, Object> clientMetadata) throws Exception {

        Assert.assertNotNull(clientMetadata, "Request JWT must contain 'client_metadata' claim.");
        Map<String, Object> jwks = (Map<String, Object>) clientMetadata.get("jwks");
        Assert.assertNotNull(jwks, "client_metadata must contain 'jwks'.");
        List<?> keys = (List<?>) jwks.get("keys");
        Assert.assertNotNull(keys, "'jwks' must contain 'keys'.");
        Assert.assertFalse(keys.isEmpty(), "'keys' array must not be empty.");
        Map<String, Object> keyMap = (Map<String, Object>) keys.get(0);
        return ECKey.parse(new net.minidev.json.JSONObject(keyMap).toJSONString());
    }
}
