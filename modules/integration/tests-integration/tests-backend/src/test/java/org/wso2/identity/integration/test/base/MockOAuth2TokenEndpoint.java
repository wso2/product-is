/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Mock OAuth2 Token Endpoint for testing OAuth2 flows.
 * Supports client_credentials and refresh_token grant types.
 */
public class MockOAuth2TokenEndpoint {

    public static final String TOKEN_ENDPOINT_URL = "https://localhost:8093/oauth2/token";
    public static final String TOKEN_ENDPOINT_PATH = "/oauth2/token";
    private static final int TOKEN_ENDPOINT_PORT = 8093;
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final int DEFAULT_EXPIRES_IN = 3600;
    private static final int DEFAULT_REFRESH_TOKEN_EXPIRES_IN = 86400;
    
    // OAuth2 parameter names
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    
    // OAuth2 response field names
    private static final String RESPONSE_ACCESS_TOKEN = "access_token";
    private static final String RESPONSE_EXPIRES_IN = "expires_in";
    private static final String RESPONSE_REFRESH_TOKEN = "refresh_token";
    private static final String RESPONSE_REFRESH_TOKEN_EXPIRES_IN = "refresh_token_expires_in";
    private static final String RESPONSE_SCOPE = "scope";
    private static final String RESPONSE_ERROR = "error";
    private static final String RESPONSE_ERROR_DESCRIPTION = "error_description";
    
    // Other constants
    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTH_SCHEME_BASIC = "Basic ";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ERROR_INVALID_REQUEST = "invalid_request";
    private static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    private static final String TRANSFORMER_NAME = "oauth2-token-transformer";
    private static final String TOKEN_PREFIX_ACCESS = "access_token_";
    private static final String TOKEN_PREFIX_REFRESH = "refresh_token_";

    private WireMockServer wireMockServer;
    private final AtomicReference<String> lastAccessToken = new AtomicReference<>();
    private final AtomicReference<String> lastRefreshToken = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> lastRequestHeaders = new AtomicReference<>(new HashMap<>());
    private final AtomicReference<Map<String, String>> lastRequestBodyContent = new AtomicReference<>(new HashMap<>());
    private final Map<String, String> tokenStore = new HashMap<>();

    public void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpsPort(TOKEN_ENDPOINT_PORT)
                .httpDisabled(true)
                .keystorePath(Paths.get(Utils.getResidentCarbonHome(), "repository", "resources", "security",
                        ISIntegrationTest.KEYSTORE_NAME).toAbsolutePath().toString())
                .keystorePassword("wso2carbon")
                .keyManagerPassword("wso2carbon")
                .extensions(
                        new ResponseTemplateTransformer(null, true, null, null),
                        new OAuth2TokenResponseTransformer()));

        wireMockServer.start();
        configureMockEndpoints();
    }

    public void stop() {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void configureMockEndpoints() {

        try {
            wireMockServer.stubFor(post(urlEqualTo(TOKEN_ENDPOINT_PATH))
                    .willReturn(aResponse()
                            .withTransformers("response-template", TRANSFORMER_NAME)
                            .withStatus(200)
                            .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure mock OAuth2 token endpoint", e);
        }
    }

    /**
     * Get the last generated access token.
     *
     * @return The last access token
     */
    public String getLastAccessToken() {

        return lastAccessToken.get();
    }

    /**
     * Get the last generated refresh token.
     *
     * @return The last refresh token
     */
    public String getLastRefreshToken() {

        return lastRefreshToken.get();
    }

    /**
     * Get all headers received from the last token request.
     *
     * @return Map of header names to header values
     */
    public Map<String, String> getLastRequestHeaders() {

        return lastRequestHeaders.get();
    }

    /**
     * Get a specific header value from the last token request.
     *
     * @param headerName The name of the header to retrieve
     * @return The header value, or null if not found
     */
    public String getLastRequestHeader(String headerName) {

        return lastRequestHeaders.get().get(headerName);
    }

    /**
     * Get all parameters from the last token request.
     *
     * @return Map of parameter names to parameter values
     */
    public Map<String, String> getLastRequestBodyContent() {

        return lastRequestBodyContent.get();
    }

    /**
     * Get a specific parameter value from the last token request.
     *
     * @param paramName The name of the parameter to retrieve
     * @return The parameter value, or null if not found
     */
    public String getLastRequestParam(String paramName) {

        return lastRequestBodyContent.get().get(paramName);
    }

    /**
     * Clear stored tokens and request data.
     */
    public void clearData() {

        lastAccessToken.set(null);
        lastRefreshToken.set(null);
        lastRequestHeaders.set(new HashMap<>());
        lastRequestBodyContent.set(new HashMap<>());
    }

    /**
     * Custom ResponseTransformer for OAuth2 token endpoint.
     */
    private class OAuth2TokenResponseTransformer implements ResponseTransformerV2 {

        @Override
        public Response transform(Response response, ServeEvent serveEvent) {

            Request request = serveEvent.getRequest();

            Map<String, String> requestHeaders = new HashMap<>();
            request.getHeaders().all().forEach(header -> {
                requestHeaders.put(header.key(), header.firstValue());
            });
            lastRequestHeaders.set(requestHeaders);

            String requestBody = request.getBodyAsString();
            Map<String, String> params = parseJsonBody(requestBody);
            lastRequestBodyContent.set(params);

            String grantType = params.get(PARAM_GRANT_TYPE);
            if (grantType == null) {
                return createErrorResponse(400, ERROR_INVALID_REQUEST, "grant_type is required");
            }

            JSONObject tokenResponse;
            try {
                if (GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
                    tokenResponse = handleClientCredentialsGrant(params, requestHeaders);
                } else if (GRANT_TYPE_REFRESH_TOKEN.equals(grantType)) {
                    tokenResponse = handleRefreshTokenGrant(params, requestHeaders);
                } else {
                    return createErrorResponse(400, ERROR_UNSUPPORTED_GRANT_TYPE,
                            "Grant type " + grantType + " is not supported");
                }
            } catch (Exception e) {
                return createErrorResponse(400, ERROR_INVALID_REQUEST, e.getMessage());
            }

            return Response.Builder.like(response)
                    .but()
                    .body(tokenResponse.toString())
                    .build();
        }

        private JSONObject handleClientCredentialsGrant(Map<String, String> params,
                                                        Map<String, String> headers) throws Exception {

            // Validate client authentication
            validateClientAuthentication(params, headers);

            // Generate and store new tokens
            TokenPair tokens = generateAndStoreTokens();

            return buildTokenResponse(tokens.accessToken, tokens.refreshToken, params.get(PARAM_SCOPE));
        }

        private JSONObject handleRefreshTokenGrant(Map<String, String> params,
                                                   Map<String, String> headers) throws Exception {

            validateClientAuthentication(params, headers);

            String refreshToken = params.get(PARAM_REFRESH_TOKEN);
            if (refreshToken == null || refreshToken.isEmpty()) {
                throw new Exception("refresh_token is required");
            }

            if (!tokenStore.containsKey(refreshToken)) {
                throw new Exception("Invalid refresh token");
            }

            tokenStore.remove(refreshToken);
            TokenPair tokens = generateAndStoreTokens();

            return buildTokenResponse(tokens.accessToken, tokens.refreshToken, params.get(PARAM_SCOPE));
        }

        private TokenPair generateAndStoreTokens() {

            String accessToken = generateAccessToken();
            String refreshToken = generateRefreshToken();

            tokenStore.put(refreshToken, accessToken);

            lastAccessToken.set(accessToken);
            lastRefreshToken.set(refreshToken);

            return new TokenPair(accessToken, refreshToken);
        }

        private class TokenPair {
            final String accessToken;
            final String refreshToken;

            TokenPair(String accessToken, String refreshToken) {
                this.accessToken = accessToken;
                this.refreshToken = refreshToken;
            }
        }

        private JSONObject buildTokenResponse(String accessToken, String refreshToken, String scope)
                throws JSONException {

            // Build response according to OAuth 2.0 specification (RFC 6749)
            JSONObject response = new JSONObject();
            response.put(RESPONSE_ACCESS_TOKEN, accessToken);
            response.put(RESPONSE_EXPIRES_IN, DEFAULT_EXPIRES_IN);
            response.put(RESPONSE_REFRESH_TOKEN, refreshToken);
            response.put(RESPONSE_REFRESH_TOKEN_EXPIRES_IN, DEFAULT_REFRESH_TOKEN_EXPIRES_IN);

            // Add scope if requested
            if (scope != null && !scope.isEmpty()) {
                response.put(RESPONSE_SCOPE, scope);
            }

            return response;
        }

        private void validateClientAuthentication(Map<String, String> params,
                                                   Map<String, String> headers) throws Exception {

            // Check for client credentials in Authorization header (Basic Auth)
            String authHeader = headers.get(HEADER_AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith(AUTH_SCHEME_BASIC)) {
                String encodedCredentials = authHeader.substring(AUTH_SCHEME_BASIC.length());
                String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
                String[] credentials = decodedCredentials.split(":", 2);
                if (credentials.length != 2 || credentials[0].isEmpty() || credentials[1].isEmpty()) {
                    throw new Exception("Invalid client credentials");
                }
                return;
            }

            // Check for client credentials in request body
            String clientId = params.get(PARAM_CLIENT_ID);
            String clientSecret = params.get(PARAM_CLIENT_SECRET);
            if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
                return;
            }

            throw new Exception("Client authentication required");
        }

        private String generateAccessToken() {

            return TOKEN_PREFIX_ACCESS + UUID.randomUUID().toString().replace("-", "");
        }

        private String generateRefreshToken() {

            return TOKEN_PREFIX_REFRESH + UUID.randomUUID().toString().replace("-", "");
        }

        private Map<String, String> parseJsonBody(String body) {

            Map<String, String> params = new HashMap<>();
            if (body == null || body.isEmpty()) {
                return params;
            }

            try {
                JSONObject jsonObject = new JSONObject(body);
                @SuppressWarnings("unchecked")
                java.util.Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObject.get(key);
                    params.put(key, value != null ? value.toString() : null);
                }
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse JSON body: " + body, e);
            }
            return params;
        }

        private Response createErrorResponse(int statusCode, String error, String errorDescription) {

            JSONObject errorResponse = new JSONObject();
            try {
                errorResponse.put(RESPONSE_ERROR, error);
                errorResponse.put(RESPONSE_ERROR_DESCRIPTION, errorDescription);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return Response.Builder.like(Response.notConfigured())
                    .status(statusCode)
                    .body(errorResponse.toString())
                    .headers(new com.github.tomakehurst.wiremock.http.HttpHeaders(
                            new com.github.tomakehurst.wiremock.http.HttpHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)))
                    .build();
        }

        @Override
        public boolean applyGlobally() {

            return false;
        }

        @Override
        public String getName() {

            return TRANSFORMER_NAME;
        }
    }
}
