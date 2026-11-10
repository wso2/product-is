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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.util.UUID;

/**
 * Integration tests covering failure and error-path scenarios for the VP verification session API.
 */
public class VPVerificationSessionFailureTestCase extends VPVerificationSessionTestBase {

    private static final int    POLL_MAX_RETRIES   = 15;
    private static final long   POLL_INTERVAL_MS   = 500L;
    private static final String TRUSTED_ISSUER_URL = "https://test-issuer.example.com";

    private static ECKey trustedIssuerKey;

    private String presentationDefinitionId;

    private String walletConnectionIdpId;

    private String savedClientIdScheme;
    private String savedResponseMode;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        trustedIssuerKey = SDJwtVcTestBuilder.generateSigningKey();

        Response configResponse = adminGet(serverBaseUrl() + "/api/server/v1/configs/openid4vp");
        if (configResponse.getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
            JSONObject existing = new JSONObject(configResponse.getBody().asString());
            savedClientIdScheme = existing.optString("clientIdScheme", "x509_san_dns");
            savedResponseMode = existing.optString("responseMode", "direct_post");
        } else {
            savedClientIdScheme = "x509_san_dns";
            savedResponseMode = "direct_post";
        }

        configureVPResponseMode("direct_post");
        walletConnectionIdpId = createDigitalWalletConnection();
        presentationDefinitionId = createPresentationDefinition();
        initVPSessionClient();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, TRUSTED_ISSUER_URL, trustedIssuerKey);
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
        if (savedClientIdScheme != null) {
            try {
                String restoreJson = "{\"clientIdScheme\":\"" + savedClientIdScheme
                        + "\",\"responseMode\":\"" + savedResponseMode + "\"}";
                adminPut(serverBaseUrl() + "/api/server/v1/configs/openid4vp", restoreJson);
            } catch (Exception e) {
                log.warn("Failed to restore VP config in tearDown: " + e.getMessage());
            }
        }
    }

    @Test(groups = "wso2.is",
            description = "Starting a session with an empty body returns 400.")
    public void testStartSessionEmptyBody() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.startVerificationSessionWithBody("")) {
            int status = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "Empty body must result in 400 Bad Request.");
        }
    }

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

    @Test(groups = "wso2.is",
            description = "A VP with a nonce that does not match the session nonce causes FAILED status.")
    public void testSubmitNonceMismatch() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String audience = fetchRequestParams(requestId)[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the nonce.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with nonce-mismatched VP must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP containing an already-expired SD-JWT-VC causes FAILED status.")
    public void testSubmitExpiredCredential() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .expiresInSeconds(-3600L)
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates expiry.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with an expired credential must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP signed by an issuer key not in the trusted-issuer config causes FAILED status.")
    public void testSubmitUntrustedIssuer() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        ECKey untrustedKey = SDJwtVcTestBuilder.generateSigningKey();

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(untrustedKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the issuer key.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with an untrusted issuer key must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP containing a credential with the wrong 'vct' causes FAILED status.")
    public void testSubmitWrongCredentialType() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("WrongCredentialType")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the credential type.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with wrong credential type must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "Submitting a structurally malformed VP token causes FAILED status.")
    public void testSubmitInvalidSdJwtFormat() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String malformedToken = "{\"" + CREDENTIAL_IDENTIFIER + "\": \"not.a.valid.sd.jwt.token\"}";

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, malformedToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the token format.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a malformed VP token must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP with a disclosure whose SHA-256 hash does not match the issuer JWT causes FAILED.")
    public void testSubmitInvalidDisclosureHash() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .corruptDisclosure(true)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the disclosure hashes.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a corrupted disclosure hash must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP that is an SD-JWT without a key-binding JWT (KB-JWT) causes FAILED status.")
    public void testSubmitInvalidKbJwt() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String sdJwtWithoutKb = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .signingKey(trustedIssuerKey)
                .omitKbJwt(true)
                .build();
        String vpToken = "{\"" + CREDENTIAL_IDENTIFIER + "\": \"" + sdJwtWithoutKb + "\"}";

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates KB-JWT presence.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "Session with a VP missing the KB-JWT must transition to FAILED.");
    }

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

    @Test(groups = "wso2.is",
            description = "A second VP submission to an already-VERIFIED session returns 400.")
    public void testSubmitToAlreadyVerifiedSession() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse firstResponse = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int firstStatus = firstResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(firstResponse.getEntity());
            Assert.assertEquals(firstStatus, HttpStatus.SC_OK,
                    "First VP submission must be accepted (200) before the session can reach VERIFIED.");
        }

        String terminalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(terminalStatus, STATUS_VERIFIED,
                "A valid VP signed by the registered trusted issuer must reach VERIFIED.");

        try (CloseableHttpResponse secondResponse = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int status = secondResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(secondResponse.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_BAD_REQUEST,
                    "A second VP submission to a terminal session must return 400.");
        }
    }

    @Test(groups = "wso2.is",
            description = "A VP submission to an already-FAILED session returns 400.")
    public void testSubmitToAlreadyFailedSession() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");

        try (CloseableHttpResponse errorResponse = vpSessionRestClient.submitWalletError(
                requestId, "access_denied", "User declined.")) {
            int errorStatus = errorResponse.getStatusLine().getStatusCode();
            EntityUtils.consume(errorResponse.getEntity());
            Assert.assertEquals(errorStatus, HttpStatus.SC_OK,
                    "Wallet error submission must be accepted (200) to transition the session to FAILED.");
        }

        String failedStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(failedStatus, STATUS_FAILED,
                "Session must be FAILED before testing that a second VP submission is rejected.");

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

    @Test(groups = "wso2.is",
            description = "A VP whose issuer URL is not in the trusted-issuer list causes FAILED status.")
    public void testVerificationFailsWithWrongIssuer() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");
        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        ECKey untrustedKey = SDJwtVcTestBuilder.generateSigningKey();
        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .issuer("https://untrusted-issuer.example.com")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(untrustedKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the issuer URL.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "VP whose issuer URL is not in the trusted list must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A VP with a KB-JWT that targets the wrong audience causes FAILED status.")
    public void testVerificationFailsWithWrongAudience() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");
        String[] params = fetchRequestParams(requestId);
        String nonce = params[0];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience("https://wrong-audience.example.com")
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS evaluates the audience.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "VP with wrong KB-JWT audience must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A KB-JWT signed by a key that does not match the holder key in cnf causes FAILED.")
    public void testVerificationFailsWithKbJwtSignedByWrongKey() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");
        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        ECKey wrongKey = SDJwtVcTestBuilder.generateSigningKey();

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .kbJwtSigningKey(wrongKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS verifies the KB-JWT signature.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "VP whose KB-JWT is signed by a key mismatching cnf must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "A KB-JWT containing a tampered sd_hash that does not cover the SD-JWT causes FAILED.")
    public void testVerificationFailsWithSdHashMismatch() throws Exception {

        JSONObject session = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestId = (String) session.get("requestId");
        String[] params = fetchRequestParams(requestId);
        String nonce    = params[0];
        String audience = params[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonce)
                .audience(audience)
                .signingKey(trustedIssuerKey)
                .tamperedSdHash(true)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse response = vpSessionRestClient.submitVPDirectPost(requestId, vpToken)) {
            int submitStatus = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "VP submission must be accepted (200) before IS verifies the sd_hash.");
        }

        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_FAILED,
                "VP with tampered sd_hash in the KB-JWT must transition to FAILED.");
    }

    @Test(groups = "wso2.is",
            description = "Re-using a VP token from one session in a different session causes FAILED (nonce mismatch).")
    public void testVerificationFailsWithReplayedVP() throws Exception {

        JSONObject sessionA = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestIdA = (String) sessionA.get("requestId");
        String[] paramsA = fetchRequestParams(requestIdA);
        String nonceA    = paramsA[0];
        String audienceA = paramsA[1];

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce(nonceA)
                .audience(audienceA)
                .signingKey(trustedIssuerKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse responseA = vpSessionRestClient.submitVPDirectPost(requestIdA, vpToken)) {
            int submitStatus = responseA.getStatusLine().getStatusCode();
            EntityUtils.consume(responseA.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "First VP submission to session A must return 200.");
        }
        String statusA = pollStatusUntilNotActive(requestIdA, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(statusA, STATUS_VERIFIED,
                "Session A must be VERIFIED before testing replay protection.");

        JSONObject sessionB = vpSessionRestClient.startVerificationSession(DEFINITION_IDENTIFIER);
        String requestIdB = (String) sessionB.get("requestId");

        try (CloseableHttpResponse responseB = vpSessionRestClient.submitVPDirectPost(requestIdB, vpToken)) {
            int submitStatus = responseB.getStatusLine().getStatusCode();
            EntityUtils.consume(responseB.getEntity());
            Assert.assertEquals(submitStatus, HttpStatus.SC_OK,
                    "Replayed VP submission to session B must be accepted (200) before IS evaluates the nonce.");
        }

        String statusB = pollStatusUntilNotActive(requestIdB, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(statusB, STATUS_FAILED,
                "Replaying a VP from session A into session B must transition to FAILED (nonce mismatch).");
    }


    private String[] fetchRequestParams(String requestId) throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(requestId)) {
            String body = EntityUtils.toString(response.getEntity()).trim();
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200. Body: " + body);
            SignedJWT requestJwt = SignedJWT.parse(body);
            String nonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            String audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            Assert.assertNotNull(nonce, "Presentation request JWT must contain 'nonce'.");
            Assert.assertNotNull(audience, "Presentation request JWT must contain 'client_id'.");
            return new String[]{nonce, audience};
        }
    }
}
