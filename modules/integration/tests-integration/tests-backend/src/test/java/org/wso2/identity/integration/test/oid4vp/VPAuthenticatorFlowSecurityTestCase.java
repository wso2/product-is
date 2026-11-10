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
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Security-path integration tests for the VP Authenticator flow.
 */
public class VPAuthenticatorFlowSecurityTestCase extends VPAuthenticatorFlowTestBase {

    private static final String FLOW_TYPE_REGISTRATION = "REGISTRATION";
    private static final String FLOW_STATUS_COMPLETE   = "COMPLETE";

    private String savedClientIdScheme;
    private String savedResponseMode;

    private FlowResponse      originalFlowSteps;
    private FlowConfig        originalFlowConfig;
    private boolean           registrationFlowEnabled;

    private FlowManagementClient flowManagementClient;
    private FlowExecutionClient  flowExecutionClient;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        issuerSigningKey = SDJwtVcTestBuilder.generateSigningKey();

        Response configResponse = adminGet(serverBaseUrl() + "/api/server/v1/configs/openid4vp");
        if (configResponse.getStatusCode() == HttpStatus.SC_OK) {
            JSONObject existing = new JSONObject(configResponse.getBody().asString());
            savedClientIdScheme = existing.optString("clientIdScheme", "x509_san_dns");
            savedResponseMode   = existing.optString("responseMode", "direct_post");
        } else {
            savedClientIdScheme = "x509_san_dns";
            savedResponseMode   = "direct_post";
        }

        configureVPResponseMode("direct_post");
        presentationDefinitionId = createPresentationDefinition();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, isServer.getContextTenant());
        idpId            = createVPAuthenticatorIDP();
        appId            = createOAuthApplicationWithVPIdp();
        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());

        flowManagementClient = new FlowManagementClient(serverURL, isServer.getContextTenant());
        snapshotRegistrationFlow();
        configureRegistrationFlow(vpIdpName);
        enableRegistrationFlow();
        registrationFlowEnabled = true;

        flowExecutionClient = new FlowExecutionClient(serverURL, isServer.getContextTenant());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        if (registrationFlowEnabled) {
            try {
                disableRegistrationFlow();
            } catch (Exception e) {
                log.warn("Failed to disable registration flow in tearDown: " + e.getMessage());
            }
            try {
                restoreOriginalRegistrationFlow();
            } catch (Exception e) {
                log.warn("Failed to restore original registration flow in tearDown: " + e.getMessage());
            }
        }

        deleteApplicationIfExists();
        deleteIdpIfExists();
        deletePresentationDefinitionQuietly();
        closeVPSessionClient();
        closeIdpMgtClient();
        closeOAuthRestClient();

        if (savedClientIdScheme != null) {
            try {
                String restoreJson = "{\"clientIdScheme\":\"" + savedClientIdScheme
                        + "\",\"responseMode\":\"" + savedResponseMode + "\"}";
                adminPut(serverBaseUrl() + "/api/server/v1/configs/openid4vp", restoreJson);
            } catch (Exception e) {
                log.warn("Failed to restore VP config in tearDown: " + e.getMessage());
            }
        }
        if (flowManagementClient != null) {
            try { flowManagementClient.closeHttpClient(); }
            catch (Exception e) { log.warn("Failed to close flowManagementClient: " + e.getMessage()); }
        }
        if (flowExecutionClient != null) {
            try { flowExecutionClient.closeHttpClient(); }
            catch (Exception e) { log.warn("Failed to close flowExecutionClient: " + e.getMessage()); }
        }
    }

    @Test(groups = "wso2.is",
            description = "Sending status=success to commonauth for a FAILED VP session must not issue an auth code.")
    public void testFailedSessionWithForgedSuccessStatusCannotAuthenticate() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization flow must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String badVpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, badVpToken)) {
            EntityUtils.consume(vpResponse.getEntity());
        }
        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP with wrong nonce must result in a FAILED session before testing forged callback.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(
                ctx.sessionDataKey, ctx.requestId, "success", ctx.cookieStore);

        Assert.assertNotNull(redirectUri,
                "IS must redirect after the callback — got no Location header.");
        Assert.assertTrue(redirectUri.contains(WALLET_LOGIN_JSP),
                "FAILED session with forged success must redirect back to " + WALLET_LOGIN_JSP
                        + " — not to the app. Redirect: " + redirectUri);
        Assert.assertFalse(redirectUri.contains("code="),
                "FAILED session with forged success must NOT yield an auth code. Redirect: " + redirectUri);
    }

    @Test(groups = "wso2.is",
            description = "Sending status=success to commonauth for an ACTIVE VP session must not issue an auth code.")
    public void testActiveSessionWithForgedSuccessStatusCannotAuthenticate() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization flow must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        JSONObject statusResponse = vpSessionRestClient.getSessionStatus(ctx.requestId);
        Assert.assertEquals(statusResponse.get("status"), "ACTIVE",
                "Session must be ACTIVE before forging the callback.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(
                ctx.sessionDataKey, ctx.requestId, "success", ctx.cookieStore);

        Assert.assertNotNull(redirectUri,
                "IS must redirect after the callback — got no Location header.");
        Assert.assertTrue(redirectUri.contains(WALLET_LOGIN_JSP),
                "ACTIVE session with forged success must redirect back to " + WALLET_LOGIN_JSP
                        + " — not to the app. Redirect: " + redirectUri);
        Assert.assertFalse(redirectUri.contains("code="),
                "ACTIVE session with forged success must NOT yield an auth code. Redirect: " + redirectUri);
    }

    @Test(groups = "wso2.is",
            description = "Using sessionDataKey from flow A with requestId from flow B must not issue an auth code.")
    public void testMismatchedSessionDataKeyAndRequestIdCannotAuthenticate() throws Exception {

        AuthFlowContext ctxA = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctxA, "Flow A must produce a valid auth-flow context.");
        Assert.assertNotNull(ctxA.sessionDataKey, "Flow A must carry a sessionDataKey.");

        AuthFlowContext ctxB = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctxB, "Flow B must produce a valid auth-flow context.");
        Assert.assertNotNull(ctxB.sessionDataKey, "Flow B must carry a sessionDataKey.");

        Assert.assertNotEquals(ctxA.requestId, ctxB.requestId,
                "Two distinct auth flows must have different requestIds.");

        String vpTokenB = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .claim("email", "alice@example.com")
                .nonce(ctxB.nonce)
                .audience(ctxB.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctxB.requestId, vpTokenB)) {
            EntityUtils.consume(vpResponse.getEntity());
        }
        String statusB = pollStatusUntilNotActive(ctxB.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(statusB, "VERIFIED",
                "Flow B's session must be VERIFIED before testing mismatched callback.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(
                ctxA.sessionDataKey, ctxB.requestId, "success", ctxA.cookieStore);

        Assert.assertNotNull(redirectUri,
                "IS must redirect after the callback — got no Location header.");
        Assert.assertTrue(redirectUri.contains(WALLET_LOGIN_JSP),
                "Mismatched callback must redirect back to " + WALLET_LOGIN_JSP
                        + " — not to the app. Redirect: " + redirectUri);
        Assert.assertFalse(redirectUri.contains("code="),
                "Mismatched sessionDataKey+requestId must NOT yield an auth code. Redirect: " + redirectUri);
    }

    @Test(groups = "wso2.is",
            description = "Attempting to complete a registration flow using a FAILED VP session's requestId "
                    + "must not provision a user.")
    public void testSelfRegistrationWithFailedSessionRequestIdProvisionesNoUser() throws Exception {

        Object initiateResult = flowExecutionClient.initiateFlowExecution(FLOW_TYPE_REGISTRATION);
        Assert.assertNotNull(initiateResult,
                "Registration flow initiation must return a non-null response.");
        Assert.assertTrue(initiateResult instanceof FlowExecutionResponse,
                "Expected FlowExecutionResponse on initiation but got: "
                        + initiateResult.getClass().getSimpleName());

        FlowExecutionResponse initiateResponse = (FlowExecutionResponse) initiateResult;
        String flowId = initiateResponse.getFlowId();
        Assert.assertNotNull(flowId, "flowId must not be null.");
        Assert.assertFalse(flowId.trim().isEmpty(), "flowId must not be blank.");

        Map<String, Object> additionalData = initiateResponse.getData().getAdditionalData();
        Assert.assertNotNull(additionalData, "additionalData must be present in REDIRECTION response.");
        Object requestIdObj = additionalData.get("vp_request_id");
        Assert.assertNotNull(requestIdObj, "additionalData must contain 'vp_request_id'.");
        String requestId = requestIdObj.toString();

        String audience = fetchRegistrationRequestParams(requestId)[1];

        String badVpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience(audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(requestId, badVpToken)) {
            EntityUtils.consume(vpResponse.getEntity());
        }
        String finalStatus = pollStatusUntilNotActive(requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP session must be FAILED before testing registration with that failed requestId.");

        FlowExecutionRequest resumeRequest = new FlowExecutionRequest();
        resumeRequest.setFlowId(flowId);
        resumeRequest.setFlowType(FLOW_TYPE_REGISTRATION);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("vp_request_id", requestId);
        resumeRequest.setInputs(inputs);

        Object resumeResult = flowExecutionClient.executeFlow(resumeRequest);
        Assert.assertNotNull(resumeResult,
                "Registration flow resume must return a non-null response.");

        boolean isComplete = resumeResult instanceof FlowExecutionResponse
                && FLOW_STATUS_COMPLETE.equals(((FlowExecutionResponse) resumeResult).getFlowStatus());
        Assert.assertFalse(isComplete,
                "Registration flow must NOT complete when using a FAILED VP session's requestId.");
    }

    @Test(groups = "wso2.is",
            description = "Using a VERIFIED VP session from registration flow A to complete registration flow B "
                    + "must not provision an additional user.")
    public void testSelfRegistrationWithAnotherFlowRequestIdProvisionesNoUser() throws Exception {

        Object initiateA = flowExecutionClient.initiateFlowExecution(FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(initiateA instanceof FlowExecutionResponse,
                "Flow A initiation must return FlowExecutionResponse.");
        FlowExecutionResponse responseA = (FlowExecutionResponse) initiateA;
        String flowIdA = responseA.getFlowId();
        Assert.assertNotNull(flowIdA, "Flow A flowId must not be null.");

        Map<String, Object> additionalDataA = responseA.getData().getAdditionalData();
        String requestIdA = additionalDataA.get("vp_request_id").toString();

        String[] paramsA  = fetchRegistrationRequestParams(requestIdA);
        String nonceA     = paramsA[0];
        String audienceA  = paramsA[1];

        String vpTokenA = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice-" + UUID.randomUUID())
                .nonce(nonceA)
                .audience(audienceA)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(requestIdA, vpTokenA)) {
            EntityUtils.consume(resp.getEntity());
        }
        String statusA = pollStatusUntilNotActive(requestIdA, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(statusA, "VERIFIED",
                "Session A must be VERIFIED before testing requestId reuse in flow B.");

        Object initiateB = flowExecutionClient.initiateFlowExecution(FLOW_TYPE_REGISTRATION);
        Assert.assertTrue(initiateB instanceof FlowExecutionResponse,
                "Flow B initiation must return FlowExecutionResponse.");
        FlowExecutionResponse responseB = (FlowExecutionResponse) initiateB;
        String flowIdB = responseB.getFlowId();
        Assert.assertNotNull(flowIdB, "Flow B flowId must not be null.");

        FlowExecutionRequest resumeRequest = new FlowExecutionRequest();
        resumeRequest.setFlowId(flowIdB);
        resumeRequest.setFlowType(FLOW_TYPE_REGISTRATION);
        Map<String, String> inputs = new HashMap<>();
        inputs.put("vp_request_id", requestIdA);
        resumeRequest.setInputs(inputs);

        Object resumeResult = flowExecutionClient.executeFlow(resumeRequest);
        Assert.assertNotNull(resumeResult,
                "Registration flow B resume must return a non-null response.");

        boolean isComplete = resumeResult instanceof FlowExecutionResponse
                && FLOW_STATUS_COMPLETE.equals(((FlowExecutionResponse) resumeResult).getFlowStatus());
        Assert.assertFalse(isComplete,
                "Registration flow B must NOT complete when using session A's requestId (from a different flow).");
    }

    private String[] fetchRegistrationRequestParams(String requestId) throws Exception {

        try (CloseableHttpResponse response = vpSessionRestClient.getPresentationRequestJwt(requestId)) {
            String body = EntityUtils.toString(response.getEntity()).trim();
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "GET /requests/{id} must return 200. Body: " + body);
            SignedJWT requestJwt = SignedJWT.parse(body);
            String nonce    = requestJwt.getJWTClaimsSet().getStringClaim("nonce");
            String audience = requestJwt.getJWTClaimsSet().getStringClaim("client_id");
            Assert.assertNotNull(nonce,    "Presentation request JWT must contain 'nonce'.");
            Assert.assertNotNull(audience, "Presentation request JWT must contain 'client_id'.");
            return new String[]{nonce, audience};
        }
    }

    private void snapshotRegistrationFlow() throws Exception {

        Response flowResp = adminGet(serverBaseUrl() + "/api/server/v1/flow?flowType=" + FLOW_TYPE_REGISTRATION);
        int flowStatus = flowResp.getStatusCode();
        if (flowStatus == HttpStatus.SC_OK) {
            originalFlowSteps = flowManagementClient.getFlow(FLOW_TYPE_REGISTRATION);
        } else if (flowStatus == HttpStatus.SC_NOT_FOUND) {
            log.info("No existing REGISTRATION flow found (404); will not restore flow steps.");
        } else {
            throw new AssertionError("Unexpected status " + flowStatus
                    + " when retrieving REGISTRATION flow for snapshot: " + flowResp.getBody().asString());
        }

        Response cfgResp = adminGet(serverBaseUrl() + "/api/server/v1/flow/config?flowType=" + FLOW_TYPE_REGISTRATION);
        int cfgStatus = cfgResp.getStatusCode();
        if (cfgStatus == HttpStatus.SC_OK) {
            originalFlowConfig = flowManagementClient.getFlowConfig(FLOW_TYPE_REGISTRATION);
        } else if (cfgStatus == HttpStatus.SC_NOT_FOUND) {
            log.info("No existing REGISTRATION flow config found (404); will not restore flow config.");
        } else {
            throw new AssertionError("Unexpected status " + cfgStatus
                    + " when retrieving REGISTRATION flow config for snapshot: " + cfgResp.getBody().asString());
        }
    }

    private void configureRegistrationFlow(String idpName) {

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
        Response response = adminPut(serverBaseUrl() + "/api/server/v1/flow", flowJson);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Registration flow configuration failed with status: " + status);
    }

    private void enableRegistrationFlow() {

        Response response = adminPatch(serverBaseUrl() + "/api/server/v1/flow/config",
                "{\"flowType\":\"REGISTRATION\",\"isEnabled\":true}");
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Enabling registration flow failed with status: " + status);
    }

    private void disableRegistrationFlow() {

        Response response = adminPatch(serverBaseUrl() + "/api/server/v1/flow/config",
                "{\"flowType\":\"REGISTRATION\",\"isEnabled\":false}");
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_OK || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status disabling registration flow: " + status);
    }

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
}
