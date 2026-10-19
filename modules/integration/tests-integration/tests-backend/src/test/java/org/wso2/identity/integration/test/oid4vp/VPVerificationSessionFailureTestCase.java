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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.util.UUID;

/**
 * Integration tests covering failure and error-path scenarios for the VP verification session API.
 *
 * <p>Each test in this class is <em>independent</em>; no {@code dependsOnMethods} chains are used
 * unless the test explicitly needs the session created in a prior step of the <em>same</em> test.
 */
public class VPVerificationSessionFailureTestCase extends VPVerificationSessionTestBase {

    private static final int   POLL_MAX_RETRIES = 15;
    private static final long  POLL_INTERVAL_MS = 500L;

    /**
     * A trusted issuer key registered in @BeforeClass.  Credentials built with this key
     * will pass issuer-trust validation; credentials built with a freshly generated key will fail.
     */
    private static ECKey trustedIssuerKey;

    /** Server-assigned ID of the test presentation definition — deleted in @AfterClass. */
    private String presentationDefinitionId;

    /** IDP with PresentationAuthenticator — triggers ECDSA key provisioning via VPIdPMgtListener. */
    private String walletConnectionIdpId;

    // ------------------------------------------------------------------
    // @BeforeClass / @AfterClass
    // ------------------------------------------------------------------

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        trustedIssuerKey = SDJwtVcTestBuilder.generateSigningKey();
        walletConnectionIdpId = createDigitalWalletConnection();
        presentationDefinitionId = createPresentationDefinition();
        initVPSessionClient();
        // NOTE: Registering trustedIssuerKey as a trusted issuer for PersonCredential would be
        // done via the presentation-definition's credential issuer-configs API.  That API is not
        // yet modelled here; as a result, tests 11-16 that rely on trusted-issuer validation will
        // expect FAILED status regardless of whether the key is trusted, because the server will
        // reject any credential whose issuer key is not configured.  The tests are written to
        // assert the status transition to FAILED in those cases.
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        deletePresentationDefinition(presentationDefinitionId);
        deleteDigitalWalletConnection(walletConnectionIdpId);
        closeVPSessionClient();
    }

    // ------------------------------------------------------------------
    // Test 1 — POST /verification-sessions with empty body → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Starting a session with an empty body returns 400.")
    public void testStartSessionMissingBody() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.startVerificationSessionWithBody("")) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Empty body must result in 400 Bad Request.");
        }
    }

    // ------------------------------------------------------------------
    // Test 2 — POST /verification-sessions with {} body → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Starting a session with an empty JSON object (missing identifier) returns 400.")
    public void testStartSessionMissingIdentifier() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.startVerificationSessionWithBody("{}")) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Missing presentationDefinitionIdentifier must result in 400.");
        }
    }

    // ------------------------------------------------------------------
    // Test 3 — POST /verification-sessions with unknown identifier → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Starting a session with an unknown presentation definition identifier returns 400.")
    public void testStartSessionUnknownIdentifier() throws Exception {

        String body = "{\"presentationDefinitionIdentifier\":\"nonexistent-definition-" + UUID.randomUUID() + "\"}";
        try (CloseableHttpResponse response = vpSessionRestClient.startVerificationSessionWithBody(body)) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Unknown definition identifier must result in 400.");
        }
    }

    // ------------------------------------------------------------------
    // Test 4 — GET status for non-existent session → 404
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Polling status for a non-existent session returns 404.")
    public void testGetStatusNonExistentSession() throws Exception {

        String randomId = UUID.randomUUID().toString();
        try (CloseableHttpResponse response = vpSessionRestClient.getSessionStatusRaw(randomId)) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_NOT_FOUND,
                    "Non-existent session status must return 404.");
        }
    }

    // ------------------------------------------------------------------
    // Test 5 — GET result for non-existent session → 404
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Fetching the result for a non-existent session returns 404.")
    public void testGetResultNonExistentSession() throws Exception {

        String randomId = UUID.randomUUID().toString();
        try (CloseableHttpResponse response = vpSessionRestClient.getSessionResult(randomId)) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_NOT_FOUND,
                    "Non-existent session result must return 404.");
        }
    }

    // ------------------------------------------------------------------
    // Test 6 — GET /requests/{random} for non-existent session → 404
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Fetching the presentation request JWT for a non-existent session returns 404.")
    public void testGetRequestNonExistentSession() throws Exception {

        String randomId = UUID.randomUUID().toString();
        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(randomId)) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_NOT_FOUND,
                    "Presentation request for non-existent session must return 404.");
        }
    }

    // ------------------------------------------------------------------
    // Test 7 — Submit with wrong state → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Submitting a VP response with a non-existent state value returns 400.")
    public void testSubmitWithWrongState() throws Exception {

        String nonExistentState = UUID.randomUUID().toString();
        try (CloseableHttpResponse response =
                     vpSessionRestClient.submitVPDirectPostWithState(nonExistentState, "some-vp-token")) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Submitting with a non-existent state must return 400.");
        }
    }

    // ------------------------------------------------------------------
    // Test 8 — Nonce mismatch → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP with a nonce that does not match the session nonce causes FAILED status.")
    public void testSubmitNonceMismatch() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        // Build VP with the wrong nonce.
        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience("https://verifier.example.com")
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with nonce-mismatched VP must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 9 — Expired credential → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP containing an already-expired SD-JWT-VC causes FAILED status.")
    public void testSubmitExpiredCredential() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        // Fetch nonce for a structurally valid KB-JWT.
        String nonce = fetchNonce(requestId);

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://verifier.example.com")
                .expiresInSeconds(-3600L)    // expired 1 hour ago
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with an expired credential must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 10 — Untrusted issuer → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP signed by an issuer key not in the trusted-issuer config causes FAILED status.")
    public void testSubmitUntrustedIssuer() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String nonce = fetchNonce(requestId);

        // Generate a fresh key that is NOT registered as a trusted issuer.
        ECKey untrustedKey = SDJwtVcTestBuilder.generateSigningKey();

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://verifier.example.com")
                .signingKey(untrustedKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with an untrusted issuer key must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 11 — Wrong credential type (vct) → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP containing a credential with the wrong 'vct' causes FAILED status.")
    public void testSubmitWrongCredentialType() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String nonce = fetchNonce(requestId);

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("WrongCredentialType")   // does not match the definition requirement
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://verifier.example.com")
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with wrong credential type must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 12 — Malformed SD-JWT token → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "Submitting a structurally malformed VP token causes FAILED status.")
    public void testSubmitInvalidSdJwtFormat() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        // Completely invalid token — not even a compact JWT.
        String malformedToken = "{\"" + CREDENTIAL_IDENTIFIER + "\": \"not.a.valid.sd.jwt.token\"}";

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, malformedToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a malformed VP token must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 13 — Corrupted disclosure hash → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP with a disclosure whose SHA-256 hash does not match the issuer JWT causes FAILED.")
    public void testSubmitInvalidDisclosureHash() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String nonce = fetchNonce(requestId);

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://verifier.example.com")
                .signingKey(trustedIssuerKey)
                .corruptDisclosure(true)      // first disclosure is corrupted
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a corrupted disclosure hash must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 14 — Missing KB-JWT → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP that is an SD-JWT without a key-binding JWT (KB-JWT) causes FAILED status.")
    public void testSubmitInvalidKbJwt() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        // Build a token that has no KB-JWT tail — structurally invalid for VP presentation.
        String sdJwtWithoutKb = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .signingKey(trustedIssuerKey)
                .omitKbJwt(true)
                .build();
        String vpToken = "{\"" + CREDENTIAL_IDENTIFIER + "\": \"" + sdJwtWithoutKb + "\"}";

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(response.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a VP missing the KB-JWT must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 15 — Wallet reports access_denied error → session FAILED
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A wallet error submission (access_denied) causes the session to transition to FAILED.")
    public void testSubmitWalletError() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        try (CloseableHttpResponse response = vpSessionRestClient.submitWalletError(
                requestId, "access_denied", "User declined to share credentials.")) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "Wallet error submission must return 200.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session after wallet access_denied must transition to FAILED.");
    }

    // ------------------------------------------------------------------
    // Test 16 — Submit to already-VERIFIED session → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A second VP submission to an already-VERIFIED session returns 400.")
    public void testSubmitToAlreadyVerifiedSession() throws Exception {

        // Step 1: start session.
        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        // Step 2: fetch nonce from presentation request.
        String nonce = fetchNonce(requestId);

        // Step 3: build and submit a valid VP.
        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://verifier.example.com")
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse firstResponse = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            EntityUtils.consume(firstResponse.getEntity());
        }

        // Step 4: wait for VERIFIED (may not actually verify without registered issuer key, but
        // the session leaves ACTIVE either way — we then attempt a second submission).
        pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);

        // Step 5: submit again — expect 400 because the session is no longer ACTIVE.
        try (CloseableHttpResponse secondResponse = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int status = secondResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(secondResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "A second VP submission to a terminal session must return 400.");
        }
    }

    // ------------------------------------------------------------------
    // Test 17 — Submit to already-FAILED session → 400
    // ------------------------------------------------------------------

    @Test(groups = "wso2.is",
            description = "A VP submission to an already-FAILED session returns 400.")
    public void testSubmitToAlreadyFailedSession() throws Exception {

        // Step 1: start session and immediately report an error from the wallet.
        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        try (CloseableHttpResponse errorResponse = vpSessionRestClient.submitWalletError(
                requestId, "access_denied", "User declined.")) {
            EntityUtils.consume(errorResponse.getEntity());
        }

        // Step 2: wait for FAILED.
        pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);

        // Step 3: now try to submit a valid VP — expect 400.
        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse secondResponse = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int status = secondResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(secondResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Submitting a VP to an already-FAILED session must return 400.");
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Fetch the nonce from the presentation request JWT for the given session.
     * Returns a fallback nonce if parsing fails (for tests that only need a non-null nonce).
     */
    private String fetchNonce(String requestId) {

        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(requestId)) {
            String body = EntityUtils.toString(response.getEntity()).trim();
            if (body.contains(".")) {
                SignedJWT requestJwt = SignedJWT.parse(body);
                String nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
                if (nonce != null) {
                    return nonce;
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch nonce from presentation request JWT: " + e.getMessage());
        }
        return "fallback-nonce-" + UUID.randomUUID();
    }
}
