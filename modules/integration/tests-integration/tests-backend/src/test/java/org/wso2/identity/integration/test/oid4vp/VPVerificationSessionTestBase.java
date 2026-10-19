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
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Base class for VP verification session integration tests.
 *
 * <p>Provides shared constants, helpers for reading JSON resource files, and a factory
 * method for the {@link VPSessionRestClient}.  Subclasses call {@link #initVPSessionClient()}
 * after {@code super.init(TestUserMode.SUPER_TENANT_ADMIN)}.
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
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH;

        try (CloseableHttpResponse response = postJson(url, json)) {
            String body = EntityUtils.toString(response.getEntity());
            int status = response.getStatusLine().getStatusCode();
            Assert.assertTrue(status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK,
                    "Failed to create presentation definition (status " + status + "): " + body);
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(body);
            Object idObj = jsonResponse.get("id");
            Assert.assertNotNull(idObj, "Presentation definition response does not contain 'id'.");
            return idObj.toString();
        }
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
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId;
        try (CloseableHttpResponse response = deleteResource(url)) {
            int status = response.getStatusLine().getStatusCode();
            Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                    "Unexpected status when deleting presentation definition: " + status);
        }
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
        String url = serverURL + "/api/server/v1/identity-providers";
        try (CloseableHttpResponse response = postJson(url, idpJson)) {
            String body = EntityUtils.toString(response.getEntity());
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, HttpStatus.SC_CREATED,
                    "Failed to create digital wallet connection IDP (status " + status + "): " + body);
            JSONObject json = (JSONObject) new JSONParser().parse(body);
            Object idObj = json.get("id");
            Assert.assertNotNull(idObj, "IDP creation response does not contain 'id'.");
            return idObj.toString();
        } catch (Exception e) {
            throw new IOException("Failed to create digital wallet connection IDP: " + e.getMessage(), e);
        }
    }

    /**
     * Configure the VP tenant response mode.
     * Calls PUT /api/server/v1/configs/openid4vp so the server accepts plain direct_post VP submissions.
     *
     * @param responseMode One of {@code "direct_post"} or {@code "direct_post.jwt"}.
     */
    protected void configureVPResponseMode(String responseMode) throws IOException {

        String body = "{\"clientIdScheme\":\"x509_san_dns\",\"responseMode\":\"" + responseMode + "\"}";
        String url = serverURL + "/api/server/v1/configs/openid4vp";
        try (CloseableHttpResponse response = putJson(url, body)) {
            int status = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(status, HttpStatus.SC_OK,
                    "Failed to configure VP response mode (status " + status + "): " + responseBody);
        }
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
        String url = serverURL + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId
                + "/credentials/" + credentialId + "/issuer-configs";
        try (CloseableHttpResponse response = putJson(url, body)) {
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

        // TBSCertificate (v3, serial=1, ecdsa-with-SHA256, same issuer/subject, validity, spki)
        ASN1EncodableVector tbsVec = new ASN1EncodableVector();
        tbsVec.add(new DERTaggedObject(true, 0, new ASN1Integer(2)));
        tbsVec.add(new ASN1Integer(BigInteger.ONE));
        tbsVec.add(sigAlgId);
        tbsVec.add(dn);
        tbsVec.add(new DERSequence(validityVec));
        tbsVec.add(dn);
        tbsVec.add(spki);
        DERSequence tbsCert = new DERSequence(tbsVec);

        // Sign with JCA — no bcpkix involved
        java.security.Signature sig = java.security.Signature.getInstance("SHA256withECDSA");
        sig.initSign(ecKey.toECPrivateKey());
        sig.update(tbsCert.getEncoded());
        byte[] sigBytes = sig.sign();

        // Certificate ::= SEQUENCE { tbsCertificate, signatureAlgorithm, signatureValue }
        ASN1EncodableVector certVec = new ASN1EncodableVector();
        certVec.add(tbsCert);
        certVec.add(sigAlgId);
        certVec.add(new DERBitString(sigBytes));
        byte[] certBytes = new DERSequence(certVec).getEncoded();

        String base64 = java.util.Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(certBytes);
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----";
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
        String url = serverURL + "/api/server/v1/identity-providers/" + idpId;
        try (CloseableHttpResponse response = deleteResource(url)) {
            int status = response.getStatusLine().getStatusCode();
            Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                    "Unexpected status when deleting digital wallet connection IDP: " + status);
        }
    }

    /**
     * Poll the session status until it leaves the {@code ACTIVE} state or the retry
     * budget is exhausted.
     *
     * @param requestId     The session ID to poll.
     * @param maxRetries    Maximum number of polling attempts.
     * @param intervalMs    Wait in milliseconds between attempts.
     * @return The last observed status string.
     * @throws Exception If any HTTP call fails.
     */
    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs) throws Exception {

        String status = STATUS_ACTIVE;
        for (int i = 0; i < maxRetries; i++) {
            JSONObject statusResponse = vpSessionRestClient.getSessionStatus(requestId);
            status = (String) statusResponse.get("status");
            if (!STATUS_ACTIVE.equals(status)) {
                break;
            }
            if (i < maxRetries - 1) {
                Thread.sleep(intervalMs);
            }
        }
        return status;
    }

    private CloseableHttpResponse postJson(String url, String json) throws IOException {

        org.apache.http.impl.client.CloseableHttpClient httpClient =
                org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpPost post = new org.apache.http.client.methods.HttpPost(url);
        post.setHeaders(buildAdminHeaders());
        post.setEntity(new org.apache.http.entity.StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(post);
    }

    private CloseableHttpResponse putJson(String url, String json) throws IOException {

        org.apache.http.impl.client.CloseableHttpClient httpClient =
                org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpPut put = new org.apache.http.client.methods.HttpPut(url);
        put.setHeaders(buildAdminHeaders());
        put.setEntity(new org.apache.http.entity.StringEntity(json, StandardCharsets.UTF_8));
        return httpClient.execute(put);
    }

    private CloseableHttpResponse deleteResource(String url) throws IOException {

        org.apache.http.impl.client.CloseableHttpClient httpClient =
                org.apache.http.impl.client.HttpClients.createDefault();
        org.apache.http.client.methods.HttpDelete delete = new org.apache.http.client.methods.HttpDelete(url);
        delete.setHeaders(buildAdminHeaders());
        return httpClient.execute(delete);
    }

    private Header[] buildAdminHeaders() {

        String credentials = userInfo.getUserName() + ":" + userInfo.getPassword();
        String encoded = Base64.encodeBase64String(credentials.getBytes(StandardCharsets.UTF_8)).trim();
        return new Header[]{
                new BasicHeader("Authorization", "Basic " + encoded),
                new BasicHeader("Content-Type", String.valueOf(ContentType.JSON)),
                new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT)
        };
    }

    /**
     * Read a classpath resource into a String (UTF-8).
     *
     * @param resourcePath Classpath-relative path.
     * @return Content as a String.
     * @throws Exception If the resource cannot be found or read.
     */
    protected static String readResourceAsString(String resourcePath) throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = VPVerificationSessionTestBase.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            Assert.assertNotNull(in, "Test resource not found on classpath: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
