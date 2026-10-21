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
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowConfig;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowExecutionResponse;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowResponse;
import org.wso2.identity.integration.test.restclients.FlowExecutionClient;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for the VP-based self-registration flow using {@code PresentationExecutor}.
 */
public class VPSelfRegistrationFlowTestCase extends VPVerificationSessionTestBase {

    // Definition resource used by this test.
    private static final String FLOW_DEFINITION_RESOURCE =
            "org/wso2/identity/integration/test/oid4vp/create-vp-presentation-definition-registration.json";

    private static final String ISSUER_URL             = "https://test-issuer.example.com";

    private static final String WALLET_IDP_NAME = "VP-SelfReg-Test-IDP";

    private static final int    POLL_MAX_RETRIES = 15;
    private static final long   POLL_INTERVAL_MS = 500L;

    private static final String FLOW_TYPE_REGISTRATION     = "REGISTRATION";
    private static final String FLOW_STATUS_INCOMPLETE     = "INCOMPLETE";
    private static final String FLOW_STATUS_COMPLETE       = "COMPLETE";
    private static final String FLOW_RESP_TYPE_REDIRECTION = "REDIRECTION";

    private ECKey issuerSigningKey;
    private String presentationDefinitionId;
    private String walletConnectionIdpId;

    private FlowExecutionClient flowExecutionClient;
    private FlowManagementClient flowManagementClient;
    private SCIM2RestClient scim2RestClient;

    private static final String REGISTERED_USERNAME = "Alice";
    private String registeredUserId;

    private FlowResponse originalFlowSteps;
    private FlowConfig originalFlowConfig;

    private String sharedFlowId;
    private String sharedRequestId;
    private String sharedNonce;
    private String sharedAudience;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        issuerSigningKey = SDJwtVcTestBuilder.generateSigningKey();

        flowManagementClient = new FlowManagementClient(serverURL, isServer.getContextTenant());

        try {
            originalFlowSteps = flowManagementClient.getFlow(FLOW_TYPE_REGISTRATION);
        } catch (Exception e) {
            log.warn("No existing REGISTRATION flow to save: " + e.getMessage());
        }
        try {
            originalFlowConfig = flowManagementClient.getFlowConfig(FLOW_TYPE_REGISTRATION);
        } catch (Exception e) {
            log.warn("No existing REGISTRATION flow config to save: " + e.getMessage());
        }

        configureVPResponseMode("direct_post");
        presentationDefinitionId = createPresentationDefinitionFromResource(FLOW_DEFINITION_RESOURCE);
        walletConnectionIdpId = createWalletConnectionIdpWithDefinitionId(WALLET_IDP_NAME, presentationDefinitionId);
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        configureIdpSubjectClaim(walletConnectionIdpId);
        configureRegistrationFlow(WALLET_IDP_NAME);
        enableRegistrationFlow();

        flowExecutionClient = new FlowExecutionClient(serverURL, isServer.getContextTenant());
        scim2RestClient = new SCIM2RestClient(serverURL, isServer.getContextTenant());
        // Delete any user left by a previous test run so self-registration starts clean.
        deleteUserIfExists(REGISTERED_USERNAME);
        initVPSessionClient();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        if (registeredUserId != null) {
            try {
                scim2RestClient.deleteUser(registeredUserId);
            } catch (Exception e) {
                log.warn("Failed to delete registered user during teardown: " + e.getMessage());
            }
        }
        if (scim2RestClient != null) {
            try {
                scim2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close SCIM2 client during teardown: " + e.getMessage());
            }
        }
        try {
            disableRegistrationFlow();
        } catch (Exception e) {
            log.warn("Failed to disable registration flow during teardown: " + e.getMessage());
        }
        try {
            restoreOriginalRegistrationFlow();
        } catch (Exception e) {
            log.warn("Failed to restore original registration flow during teardown: " + e.getMessage());
        }
        try {
            deleteDigitalWalletConnection(walletConnectionIdpId);
        } catch (Exception e) {
            log.warn("Failed to delete wallet connection IDP during teardown: " + e.getMessage());
        }
        try {
            deletePresentationDefinition(presentationDefinitionId);
        } catch (Exception e) {
            log.warn("Failed to delete presentation definition during teardown: " + e.getMessage());
        }
        try {
            closeVPSessionClient();
        } catch (Exception e) {
            log.warn("Failed to close VP session client during teardown: " + e.getMessage());
        }
        if (flowManagementClient != null) {
            try {
                flowManagementClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close flow management client during teardown: " + e.getMessage());
            }
        }
        if (flowExecutionClient != null) {
            try {
                flowExecutionClient.closeHttpClient();
            } catch (Exception e) {
                log.warn("Failed to close flow execution client during teardown: " + e.getMessage());
            }
        }
    }

    @Test(groups = "wso2.is",
            description = "Initiating a VP registration flow returns REDIRECTION type with wallet URL and requestId.")
    public void testInitiateSelfRegistrationViaVP() throws Exception {

        Object responseObj = flowExecutionClient.initiateFlowExecution(FLOW_TYPE_REGISTRATION);
        Assert.assertNotNull(responseObj, "Flow execution must return a non-null response.");
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse,
                "Expected FlowExecutionResponse but got: " + responseObj.getClass().getSimpleName());

        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        sharedFlowId = response.getFlowId();
        Assert.assertNotNull(sharedFlowId, "flowId must not be null.");
        Assert.assertFalse(sharedFlowId.trim().isEmpty(), "flowId must not be blank.");
        Assert.assertEquals(response.getFlowStatus(), FLOW_STATUS_INCOMPLETE,
                "Flow status must be INCOMPLETE after initiation.");
        Assert.assertEquals(response.getType().toString(), FLOW_RESP_TYPE_REDIRECTION,
                "Flow type must be REDIRECTION for VP-based registration.");

        Assert.assertNotNull(response.getData(), "Response data must not be null.");
        Map<String, Object> additionalData = response.getData().getAdditionalData();
        Assert.assertNotNull(additionalData, "additionalData must be present in REDIRECTION response.");

        Object requestIdObj = additionalData.get("vp_request_id");
        Assert.assertNotNull(requestIdObj, "additionalData must contain 'vp_request_id'.");
        sharedRequestId = requestIdObj.toString();
        Assert.assertFalse(sharedRequestId.trim().isEmpty(), "'vp_request_id' must not be blank.");

        String redirectURL = response.getData().getRedirectURL();
        Assert.assertNotNull(redirectURL,
                "REDIRECTION response must contain a redirect URL in data.redirectURL.");
        Assert.assertFalse(redirectURL.isEmpty(),
                "Redirect URL in data.redirectURL must not be blank.");
        Assert.assertTrue(redirectURL.startsWith("openid4vp://"),
                "Redirect URL must be a wallet deep-link starting with 'openid4vp://', but was: " + redirectURL);
    }

    @Test(groups = "wso2.is",
            description = "GET /requests/{id} returns a compact JWT with nonce and client_id.",
            dependsOnMethods = "testInitiateSelfRegistrationViaVP")
    public void testFetchPresentationRequestJwt() throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(sharedRequestId)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity()).trim();

            Assert.assertEquals(statusCode, HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200.  Body: " + body);
            Assert.assertEquals(body.split("\\.").length, 3,
                    "Presentation request response must be a compact-serialized JWT with exactly 3 parts (header.payload.signature).");

            SignedJWT requestJwt = SignedJWT.parse(body);
            sharedNonce = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            Assert.assertNotNull(sharedNonce, "Presentation request JWT must contain a 'nonce' claim.");
            Assert.assertFalse(sharedNonce.trim().isEmpty(), "'nonce' claim must not be blank.");
            sharedAudience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            Assert.assertNotNull(sharedAudience, "Presentation request JWT must contain a 'client_id' claim.");
            Assert.assertFalse(sharedAudience.trim().isEmpty(), "'client_id' claim must not be blank.");
        }
    }

    @Test(groups = "wso2.is",
            description = "Submitting a valid SD-JWT-VC VP token via direct_post returns 200.",
            dependsOnMethods = "testFetchPresentationRequestJwt")
    public void testSubmitValidVPForSelfRegistration() throws Exception {

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .issuer(ISSUER_URL)
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
            description = "After VP submission the verification session transitions to VERIFIED.",
            dependsOnMethods = "testSubmitValidVPForSelfRegistration")
    public void testVPSessionTransitionsToVerified() throws Exception {

        String finalStatus = pollStatusUntilNotActive(sharedRequestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, STATUS_VERIFIED,
                "VP session must be VERIFIED before the registration flow can complete.");
    }

    @Test(groups = "wso2.is",
            description = "Resuming the registration flow after VP verification returns flowStatus=COMPLETE.",
            dependsOnMethods = "testVPSessionTransitionsToVerified")
    public void testRegistrationFlowCompletesAfterVPVerified() throws Exception {

        FlowExecutionRequest request = new FlowExecutionRequest();
        request.setFlowId(sharedFlowId);
        request.setFlowType(FLOW_TYPE_REGISTRATION);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("vp_request_id", sharedRequestId);
        request.setInputs(inputs);

        Object responseObj = flowExecutionClient.executeFlow(request);
        Assert.assertNotNull(responseObj, "Flow resume must return a non-null response.");
        Assert.assertTrue(responseObj instanceof FlowExecutionResponse,
                "Expected FlowExecutionResponse on flow resume but got: "
                        + responseObj.getClass().getSimpleName());

        FlowExecutionResponse response = (FlowExecutionResponse) responseObj;
        Assert.assertEquals(response.getFlowStatus(), FLOW_STATUS_COMPLETE,
                "Registration flow must be COMPLETE after successful VP verification.");

        String filter = java.net.URLEncoder.encode(
                "userName eq \"" + REGISTERED_USERNAME + "\"", StandardCharsets.UTF_8);
        JSONObject users = new JSONObject(scim2RestClient.filterUsers(filter).toJSONString());
        Assert.assertEquals(users.getLong("totalResults"), 1L,
                "Registration flow must provision exactly one user with userName='" + REGISTERED_USERNAME + "'.");
        registeredUserId = users.getJSONArray("Resources").getJSONObject(0).getString("id");
    }

    /**
     * Restores the REGISTRATION flow steps and enabled state to the values captured before setUp ran.
     * No-ops if no snapshot was saved (fresh IS install with no pre-existing flow).
     */
    private void restoreOriginalRegistrationFlow() throws Exception {

        if (originalFlowSteps != null) {
            FlowRequest restoreRequest = new FlowRequest();
            restoreRequest.setFlowType(FLOW_TYPE_REGISTRATION);
            restoreRequest.setSteps(originalFlowSteps.getSteps());
            flowManagementClient.putFlow(restoreRequest);
        }
        if (originalFlowConfig != null) {
            flowManagementClient.updateFlowConfig(originalFlowConfig);
        }
    }

    /**
     * Creates a digital wallet connection IDP with the given fixed name.
     */
    private String createWalletConnectionIdpWithDefinitionId(String idpName, String definitionId) throws Exception {

        String idpJson = "{"
                + "\"name\": \"" + idpName + "\","
                + "\"description\": \"VP self-registration test IDP\","
                + "\"federatedAuthenticators\": {"
                + "  \"defaultAuthenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "  \"authenticators\": [{"
                + "    \"authenticatorId\": \"UHJlc2VudGF0aW9uQXV0aGVudGljYXRvcg\","
                + "    \"isEnabled\": true,"
                + "    \"properties\": [{"
                + "      \"key\": \"presentationDefinitionId\","
                + "      \"value\": \"" + definitionId + "\""
                + "    }]"
                + "  }]"
                + "}"
                + "}";

        String url = serverURL + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpJson);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create wallet connection IDP (status " + status + "): " + body);
        JSONObject json = new JSONObject(body);
        Object idObj = json.opt("id");
        Assert.assertNotNull(idObj, "IDP creation response must contain 'id'.");
        return idObj.toString();
    }

    /**
     * Sets the IDP's subject claim to {@code "given_name"} so that {@code PresentationExecutor}
     * can resolve a non-blank subject identifier from the VP credential claim {@code given_name}.
     */
    private void configureIdpSubjectClaim(String idpId) throws IOException {

        String claimsJson = "{"
                + "\"userIdClaim\": {\"uri\": \"given_name\"},"
                + "\"mappings\": ["
                + "  {\"idpClaim\": \"given_name\","
                + "   \"localClaim\": {\"uri\": \"http://wso2.org/claims/givenname\"}},"
                + "  {\"idpClaim\": \"family_name\","
                + "   \"localClaim\": {\"uri\": \"http://wso2.org/claims/lastname\"}}"
                + "]"
                + "}";
        String url = serverURL + "/api/server/v1/identity-providers/" + idpId + "/claims";
        Response response = adminPut(url, claimsJson);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "IDP claim configuration update failed with status: " + status);
    }

    /**
     * Registers a registration flow that uses {@code PresentationExecutor} referencing the
     * given IDP connection name, followed by {@code UserProvisioningExecutor} at END.
     */
    private void configureRegistrationFlow(String idpName) throws IOException {

        String flowJson = "{"
                + "\"flowType\": \"REGISTRATION\","
                + "\"steps\": ["
                + "  {"
                + "    \"id\": \"vp_exec_step\","
                + "    \"type\": \"EXECUTION\","
                + "    \"data\": {"
                + "      \"action\": {"
                + "        \"type\": \"EXECUTOR\","
                + "        \"executor\": {"
                + "          \"name\": \"PresentationExecutor\","
                + "          \"meta\": {\"idpName\": \"" + idpName + "\"}"
                + "        },"
                + "        \"next\": \"END\""
                + "      }"
                + "    },"
                + "    \"size\": {\"width\": 206, \"height\": 70},"
                + "    \"position\": {\"x\": 200, \"y\": 200}"
                + "  },"
                + "  {"
                + "    \"id\": \"END\","
                + "    \"type\": \"END\","
                + "    \"data\": {"
                + "      \"action\": {"
                + "        \"type\": \"EXECUTOR\","
                + "        \"executor\": {\"name\": \"UserProvisioningExecutor\"}"
                + "      }"
                + "    },"
                + "    \"size\": {\"width\": 350, \"height\": 277},"
                + "    \"position\": {\"x\": 700, \"y\": 200}"
                + "  }"
                + "]"
                + "}";

        String url = serverURL + "/api/server/v1/flow";
        Response response = adminPut(url, flowJson);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Registration flow configuration failed with status: " + status);
    }

    /**
     * Enables the REGISTRATION flow orchestration via the flow config API.
     */
    private void enableRegistrationFlow() throws IOException {

        String requestBody = "{\"flowType\":\"REGISTRATION\",\"isEnabled\":true}";
        String url = serverURL + "/api/server/v1/flow/config";
        Response response = adminPatch(url, requestBody);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Enabling registration flow failed with status: " + status);
    }

    /**
     * Disables the REGISTRATION flow orchestration. Called from @AfterClass.
     */
    private void disableRegistrationFlow() throws IOException {

        String requestBody = "{\"flowType\":\"REGISTRATION\",\"isEnabled\":false}";
        String url = serverURL + "/api/server/v1/flow/config";
        Response response = adminPatch(url, requestBody);
        int status = response.getStatusCode();
        // Tolerate 200 or 404 (server may already have cleared state).
        Assert.assertTrue(status == HttpStatus.SC_OK || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status disabling registration flow: " + status);
    }

    /** Deletes all users matching the given username via SCIM2; no-op if none exist. Used to isolate test runs. */
    private void deleteUserIfExists(String userName) {

        VPTestUtils.deleteUserIfExists(userName, scim2RestClient);
    }
}
