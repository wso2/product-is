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
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;

/**
 * Shared admin-REST and VP-session helpers used by both the VP verification session tests
 * and the VP authenticator flow tests.
 */
public abstract class VPTestBase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String CREDENTIAL_IDENTIFIER     = "PersonCredential";
    protected static final String PRESENTATION_DEFINITIONS_PATH = "/api/server/v1/presentation-definitions";

    protected VPSessionRestClient vpSessionRestClient;

    protected void initVPSessionClient() throws Exception {

        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());
    }

    protected void closeVPSessionClient() {

        if (vpSessionRestClient != null) {
            try {
                vpSessionRestClient.closeHttpClient();
            } catch (IOException e) {
                log.warn("Failed to close VPSessionRestClient: " + e.getMessage());
            }
        }
    }

    protected String serverBaseUrl() {

        return serverURL.endsWith("/") ? serverURL.substring(0, serverURL.length() - 1) : serverURL;
    }

    protected void configureVPResponseMode(String responseMode) throws Exception {

        configureVPResponseMode(responseMode, serverBaseUrl(), userInfo.getUserName(), userInfo.getPassword());
    }

    protected void configureVPResponseMode(String responseMode, String apiBase, String user, String pass)
            throws Exception {

        String url = apiBase + "/api/server/v1/configs/openid4vp";
        Response response = adminPut(url, VPTestUtils.buildVPResponseModeJson(responseMode), user, pass);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure VP response mode (status " + status + "): " + response.getBody().asString());
    }

    protected void configureTrustedIssuer(String definitionId, String credentialId,
                                          String issuerUrl, ECKey signingKey) throws Exception {

        configureTrustedIssuer(definitionId, credentialId, issuerUrl, signingKey,
                serverBaseUrl(), userInfo.getUserName(), userInfo.getPassword());
    }

    protected void configureTrustedIssuer(String definitionId, String credentialId,
                                          String issuerUrl, ECKey signingKey,
                                          String apiBase, String user, String pass) throws Exception {

        String body = VPTestUtils.buildTrustedIssuerRequestJson(issuerUrl, signingKey);
        String url = apiBase + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId
                + "/credentials/" + credentialId + "/issuer-configs";
        Response response = adminPut(url, body, user, pass);
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_OK,
                "Failed to configure trusted issuer (status " + status + "): " + response.getBody().asString());
    }

    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs) throws Exception {

        return pollStatusUntilNotActive(requestId, maxRetries, intervalMs, vpSessionRestClient);
    }

    protected String pollStatusUntilNotActive(String requestId, int maxRetries, long intervalMs,
            VPSessionRestClient client) throws Exception {

        AtomicReference<String> lastStatus = new AtomicReference<>("ACTIVE");
        await().atMost((long) maxRetries * intervalMs, TimeUnit.MILLISECONDS)
                .pollInterval(intervalMs, TimeUnit.MILLISECONDS)
                .until(() -> {
                    JSONObject statusResponse = client.getSessionStatus(requestId);
                    String status = (String) statusResponse.get("status");
                    lastStatus.set(status);
                    return !"ACTIVE".equals(status);
                });
        return lastStatus.get();
    }

    protected Response adminGet(String url) {

        return adminGet(url, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminGet(String url, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .get(normalizeUrl(url));
    }

    protected Response adminPost(String url, String json) {

        return adminPost(url, json, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminPost(String url, String json, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .post(normalizeUrl(url));
    }

    protected Response adminPut(String url, String json) {

        return adminPut(url, json, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminPut(String url, String json, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .put(normalizeUrl(url));
    }

    protected Response adminPatch(String url, String json) {

        return adminPatch(url, json, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminPatch(String url, String json, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .contentType(ContentType.JSON)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .body(json)
                .patch(normalizeUrl(url));
    }

    protected Response adminDelete(String url) {

        return adminDelete(url, userInfo.getUserName(), userInfo.getPassword());
    }

    protected Response adminDelete(String url, String user, String pass) {

        return RestAssured.given()
                .auth().preemptive().basic(user, pass)
                .header("User-Agent", OAuth2Constant.USER_AGENT)
                .delete(normalizeUrl(url));
    }

    private static String normalizeUrl(String url) {

        return url.replaceAll("([^:])//+", "$1/");
    }
}
