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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

/**
 * Base class for VP verification session integration tests.
 */
public abstract class VPVerificationSessionTestBase extends ISIntegrationTest {

    /** Identifier of the test presentation definition used across session tests. */
    protected static final String DEFINITION_IDENTIFIER = "test-vp-session-definition";

    /** Credential query ID used inside the presentation definition. */
    protected static final String CREDENTIAL_IDENTIFIER = "PersonCredential";

    protected static final String STATUS_ACTIVE   = "ACTIVE";
    protected static final String STATUS_VERIFIED = "VERIFIED";
    protected static final String STATUS_FAILED   = "FAILED";

    private static final String PRESENTATION_DEFINITIONS_PATH = "/api/server/v1/presentation-definitions";

    /** Client for the OID4VP verification-session API. */
    protected VPSessionRestClient vpSessionRestClient;

    /** Resource path that locates the JSON file used to create the presentation definition. */
    protected static final String CREATE_DEFINITION_RESOURCE =
            "org/wso2/identity/integration/test/oid4vp/create-vp-presentation-definition.json";

    /**
     * Initialise the {@link VPSessionRestClient}.
     * Call this from {@code @BeforeClass} after {@code super.init(...)}.
     */
    protected void initVPSessionClient() throws Exception {

        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());
    }

    /**
     * Create a presentation definition via the admin REST API.
     *
     * @return The {@code id} of the newly created definition.
     * @throws Exception If the HTTP call fails or the server does not return 201.
     */
    protected String createPresentationDefinition() throws Exception {

        return createPresentationDefinitionFromResource(CREATE_DEFINITION_RESOURCE);
    }

    /**
     * Create a presentation definition from a JSON file on the classpath.
     *
     * @param resourcePath Classpath-relative path to the JSON file.
     * @return The {@code id} field of the created definition.
     * @throws Exception If reading the resource or the HTTP call fails.
     */
    protected String createPresentationDefinitionFromResource(String resourcePath) throws Exception {

        String json = readResourceAsString(resourcePath);
        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH;
        Response response = adminPost(url, json);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK,
                "Failed to create presentation definition (status " + status + "): " + body);
        JSONObject jsonResponse = new JSONObject(body);
        Object idObj = jsonResponse.opt("id");
        Assert.assertNotNull(idObj, "Presentation definition response does not contain 'id'.");
        return idObj.toString();
    }

    /**
     * Delete a presentation definition by its server-assigned ID.
     *
     * @param definitionId The ID returned at creation time.
     * @throws IOException If the HTTP call fails.
     */
    protected void deletePresentationDefinition(String definitionId) throws IOException {

        if (definitionId == null) {
            return;
        }
        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId;
        Response response = adminDelete(url);
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status when deleting presentation definition: " + status);
    }

    /**
     * Close the VP session REST client.  Call from {@code @AfterClass}.
     */
    protected void closeVPSessionClient() {

        if (vpSessionRestClient != null) {
            try {
                vpSessionRestClient.closeHttpClient();
            } catch (IOException e) {
                log.warn("Failed to close VPSessionRestClient: " + e.getMessage());
            }
        }
    }

    /**
     * Create a "digital wallet connection" IDP that has the PresentationAuthenticator as its
     * federated authenticator.  Creating this IDP fires {@code VPIdPMgtListener.doPostAddIdP},
     * which provisions the ECDSA P-256 keypair needed to sign VP request JWTs.
     *
     * @return The server-assigned IDP {@code id}.
     */
    protected String createDigitalWalletConnection() throws IOException {

        String idpJson = "{"
                + "\"name\": \"VP-Session-Test-IDP-" + UUID.randomUUID() + "\","
                + "\"description\": \"VP session test IDP for ECDSA key provisioning\","
                + "\"federatedAuthenticators\": {"
                + "  \"defaultAuthenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "  \"authenticators\": [{"
                + "    \"authenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "    \"isEnabled\": true,"
                + "    \"properties\": [{"
                + "      \"key\": \"presentationDefinitionId\","
                + "      \"value\": \"" + DEFINITION_IDENTIFIER + "\""
                + "    }]"
                + "  }]"
                + "}"
                + "}";
        String url = serverBaseUrl() + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpJson);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create digital wallet connection IDP (status " + status + "): " + body);
        try {
            JSONObject json = new JSONObject(body);
            Object idObj = json.opt("id");
            Assert.assertNotNull(idObj, "IDP creation response does not contain 'id'.");
            return idObj.toString();
        } catch (Exception e) {
            throw new IOException("Failed to parse IDP creation response: " + e.getMessage(), e);
        }
    }

    /**
     * Configure the VP tenant response mode.
     * Calls PUT /api/server/v1/configs/openid4vp so the server accepts plain direct_post VP submissions.
     *
     * @param responseMode One of {@code "direct_post"} or {@code "direct_post.jwt"}.
     */
    protected void configureVPResponseMode(String responseMode) throws IOException {

        String json = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"" + responseMode + "\"}";
        String url = serverBaseUrl() + "/api/server/v1/configs/openid4vp";
        Response response = adminPut(url, json);
        int status = response.getStatusCode();
        String responseBody = response.getBody().asString();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure VP response mode (status " + status + "): " + responseBody);
    }

    /**
     * Register an in-memory EC signing key as a trusted issuer for the given credential.
     * Calls PUT /presentation-definitions/{defId}/credentials/{credId}/issuer-configs.
     *
     * @param definitionId  Server-assigned presentation definition ID.
     * @param credentialId  Credential identifier within the definition.
     * @param issuerUrl     The issuer URL to register.
     * @param signingKey    The ECKey whose public part to register (PEM / SubjectPublicKeyInfo).
     */
    protected void configureTrustedIssuer(String definitionId, String credentialId,
                                          String issuerUrl, ECKey signingKey) throws Exception {

        String pemCert = buildSelfSignedPemCertificate(signingKey);
        // Server expects keySource to be the full PEM string Base64-encoded (decodeBase64Pem on the server side).
        String base64Pem = java.util.Base64.getEncoder()
                .encodeToString(pemCert.getBytes(StandardCharsets.UTF_8));
        String body = "{\"issuerConfigs\":[{"
                + "\"issuerUrl\":\"" + issuerUrl + "\","
                + "\"keySourceType\":\"pem\","
                + "\"keySource\":\"" + base64Pem + "\""
                + "}]}";
        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId
                + "/credentials/" + credentialId + "/issuer-configs";
        Response response = adminPut(url, body);
        int status = response.getStatusCode();
        String responseBody = response.getBody().asString();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure trusted issuer (status " + status + "): " + responseBody);
    }

    private static String buildSelfSignedPemCertificate(ECKey ecKey) throws Exception {

        return VPTestUtils.buildSelfSignedPemCertificate(ecKey);
    }

    /**
     * Delete a digital wallet connection IDP by its server-assigned ID.
     *
     * @param idpId The ID returned at creation time.
     */
    protected void deleteDigitalWalletConnection(String idpId) throws IOException {

        if (idpId == null) {
            return;
        }
        String url = serverBaseUrl() + "/api/server/v1/identity-providers/" + idpId;
        Response response = adminDelete(url);
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status when deleting digital wallet connection IDP: " + status);
    }

    /**
     * Poll the session status until it leaves the {@code ACTIVE} state or the overall
     * timeout ({@code maxRetries * intervalMs}) is exhausted.
     *
     * @param requestId     The session ID to poll.
     * @param maxRetries    Number of polling attempts the timeout budget is sized for.
     * @param intervalMs    Wait in milliseconds between attempts.
     * @return The last observed status string.
     * @throws Exception If any HTTP call fails, or if the status is still {@code ACTIVE} once the timeout elapses.
     */
    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs) throws Exception {

        AtomicReference<String> lastStatus = new AtomicReference<>(STATUS_ACTIVE);
        await().atMost((long) maxRetries * intervalMs, TimeUnit.MILLISECONDS)
                .pollInterval(intervalMs, TimeUnit.MILLISECONDS)
                .until(() -> {
                    JSONObject statusResponse = vpSessionRestClient.getSessionStatus(requestId);
                    String status = (String) statusResponse.get("status");
                    lastStatus.set(status);
                    return !STATUS_ACTIVE.equals(status);
                });
        return lastStatus.get();
    }

    protected Response adminPost(String url, String json) {

        return RestAssured.given()
                .auth().preemptive().basic(userInfo.getUserName(), userInfo.getPassword())
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .post(url);
    }

    protected Response adminPut(String url, String json) {

        return RestAssured.given()
                .auth().preemptive().basic(userInfo.getUserName(), userInfo.getPassword())
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .put(url);
    }

    protected Response adminPatch(String url, String json) {

        return RestAssured.given()
                .auth().preemptive().basic(userInfo.getUserName(), userInfo.getPassword())
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .patch(url);
    }

    protected Response adminDelete(String url) {

        return RestAssured.given()
                .auth().preemptive().basic(userInfo.getUserName(), userInfo.getPassword())
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .delete(url);
    }

    /**
     * Returns the server base URL without a trailing slash.
     * ISIntegrationTest sets {@code serverURL} by stripping {@code "services/"} from the backend
     * URL, which leaves a trailing slash (e.g. {@code https://localhost:9853/}).  Path constants
     * in this class already start with {@code /}, so naively concatenating them yields a
     * double-slash that Apache HttpClient normalises silently but RestAssured sends verbatim —
     * causing Tomcat to reject the request with HTTP 400.
     */
    protected String serverBaseUrl() {
        return serverURL.endsWith("/") ? serverURL.substring(0, serverURL.length() - 1) : serverURL;
    }

    /**
     * Reads a classpath resource into a UTF-8 string.
     *
     * @param resourcePath Classpath-relative path.
     * @return Content as a String.
     * @throws Exception If the resource cannot be found or read.
     */
    protected static String readResourceAsString(String resourcePath) throws Exception {

        return VPTestUtils.readResourceAsString(resourcePath);
    }
}
