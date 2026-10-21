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

package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST client for OID4VP verification session API endpoints.
 * Covers both authenticated admin calls and unauthenticated wallet-facing endpoints.
 */
public class VPSessionRestClient extends RestBaseClient {

    private static final String OID4VP_PATH = "oid4vp";
    private static final String VERIFICATION_SESSIONS_PATH = "verification-sessions";
    private static final String STATUS_PATH = "status";
    private static final String REQUESTS_PATH = "requests";
    private static final String RESPONSES_PATH = "responses";

    private final String baseUrl;
    private final String username;
    private final String password;

    /**
     * Construct a VPSessionRestClient.
     *
     * @param serverUrl  Server URL (e.g. {@code https://localhost:9443/}).
     * @param tenantInfo Tenant context from the test automation framework.
     */
    public VPSessionRestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.baseUrl = buildBaseUrl(serverUrl, tenantDomain);
    }

    // ------------------------------------------------------------------
    // Authenticated management calls
    // ------------------------------------------------------------------

    /**
     * Start a new verification session for the given presentation definition identifier.
     *
     * @param definitionIdentifier Presentation definition identifier configured on the server.
     * @return JSON response containing {@code requestId}, {@code walletUrl}, and {@code requestUri}.
     * @throws Exception If the request fails or the server returns a non-201 status.
     */
    public JSONObject startVerificationSession(String definitionIdentifier) throws Exception {

        JSONObject body = new JSONObject();
        body.put("presentationDefinitionIdentifier", definitionIdentifier);

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH;
        try (CloseableHttpResponse response = getResponseOfHttpPost(url, body.toString(), getAdminHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                    "Start verification session failed: " + responseBody);
            return new JSONObject(responseBody);
        }
    }

    /**
     * Start a verification session and return the raw HTTP response (caller owns close).
     *
     * @param definitionIdentifier Presentation definition identifier.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP request fails.
     * @throws JSONException If the request body cannot be built.
     */
    public CloseableHttpResponse startVerificationSessionRaw(String definitionIdentifier)
            throws IOException, JSONException {

        JSONObject body = new JSONObject();
        body.put("presentationDefinitionIdentifier", definitionIdentifier);

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH;
        return getResponseOfHttpPost(url, body.toString(), getAdminHeaders());
    }

    /**
     * Start a verification session with an arbitrary raw JSON body (for negative tests).
     *
     * @param rawBody JSON body string.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP request fails.
     */
    public CloseableHttpResponse startVerificationSessionWithBody(String rawBody) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH;
        return getResponseOfHttpPost(url, rawBody, getAdminHeaders());
    }

    /**
     * Poll the status of a verification session.
     *
     * @param requestId The session request ID.
     * @return JSON response containing the {@code status} field.
     * @throws Exception If the HTTP call fails.
     */
    public JSONObject getSessionStatus(String requestId) throws Exception {

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH
                + PATH_SEPARATOR + requestId + PATH_SEPARATOR + STATUS_PATH;
        try (CloseableHttpResponse response = getResponseOfHttpGet(url, getAdminHeaders())) {
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Get the raw HTTP response for a session status poll (useful for asserting 4xx codes).
     *
     * @param requestId The session request ID.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse getSessionStatusRaw(String requestId) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH
                + PATH_SEPARATOR + requestId + PATH_SEPARATOR + STATUS_PATH;
        return getResponseOfHttpGet(url, getAdminHeaders());
    }

    /**
     * Retrieve the full verification session result (available once the session is VERIFIED).
     *
     * @param requestId The session request ID.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse getSessionResult(String requestId) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + VERIFICATION_SESSIONS_PATH + PATH_SEPARATOR + requestId;
        return getResponseOfHttpGet(url, getAdminHeaders());
    }

    // ------------------------------------------------------------------
    // Wallet-facing (unauthenticated) endpoints
    // ------------------------------------------------------------------

    /**
     * Fetch the presentation request JWT from the public {@code /requests/{id}} endpoint.
     * No authentication is required — this is the wallet-facing endpoint.
     *
     * @param requestId The session request ID.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse getPresentationRequestJwt(String requestId) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + REQUESTS_PATH + PATH_SEPARATOR + requestId;
        return getResponseOfHttpGet(url, new Header[]{});
    }

    /**
     * Submit a verifiable presentation via {@code direct_post} (plain VP token).
     * Form parameters: {@code vp_token} and {@code state}.
     *
     * @param requestId The session request ID used as the {@code state} parameter.
     * @param vpToken   The VP token string.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse submitVPDirectPost(String requestId, String vpToken) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + RESPONSES_PATH;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vp_token", vpToken);
        params.put("state", requestId);
        return getResponseOfHttpPostWithParameters(url, new Header[]{}, params);
    }

    /**
     * Submit a verifiable presentation via {@code direct_post.jwt} (JWE-encrypted response).
     * Form parameter: {@code response}.
     *
     * @param jweCompact The compact JWE token.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse submitVPDirectPostJwt(String jweCompact) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + RESPONSES_PATH;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("response", jweCompact);
        return getResponseOfHttpPostWithParameters(url, new Header[]{}, params);
    }

    /**
     * Submit a wallet error response (e.g. {@code access_denied}).
     * Form parameters: {@code error}, {@code error_description}, and {@code state}.
     *
     * @param requestId        The session request ID.
     * @param error            OAuth2 error code.
     * @param errorDescription Human-readable error description.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse submitWalletError(String requestId, String error, String errorDescription)
            throws IOException {

        String url = baseUrl + PATH_SEPARATOR + RESPONSES_PATH;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("error", error);
        params.put("error_description", errorDescription);
        params.put("state", requestId);
        return getResponseOfHttpPostWithParameters(url, new Header[]{}, params);
    }

    /**
     * Submit a response to a non-existent or wrong state (for negative testing).
     * Form parameters: {@code vp_token} and {@code state}.
     *
     * @param state   The state value (may be invalid/random).
     * @param vpToken The VP token string.
     * @return Raw {@link CloseableHttpResponse} — caller must close.
     * @throws IOException If the HTTP call fails.
     */
    public CloseableHttpResponse submitVPDirectPostWithState(String state, String vpToken) throws IOException {

        String url = baseUrl + PATH_SEPARATOR + RESPONSES_PATH;
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vp_token", vpToken);
        params.put("state", state);
        return getResponseOfHttpPostWithParameters(url, new Header[]{}, params);
    }

    /**
     * Close the underlying HTTP client.
     *
     * @throws IOException If the client cannot be closed.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private String buildBaseUrl(String serverUrl, String tenantDomain) {

        // serverUrl already ends with "/" (e.g. https://localhost:9443/)
        String normalised = serverUrl.endsWith(PATH_SEPARATOR) ? serverUrl : serverUrl + PATH_SEPARATOR;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            return normalised + OID4VP_PATH;
        }
        return normalised + TENANT_PATH + tenantDomain + PATH_SEPARATOR + OID4VP_PATH;
    }

    private Header[] getAdminHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE
                + Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}
