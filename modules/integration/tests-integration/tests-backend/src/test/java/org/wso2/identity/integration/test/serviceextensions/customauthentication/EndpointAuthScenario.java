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

package org.wso2.identity.integration.test.serviceextensions.customauthentication;

import org.testng.Assert;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.util.UserDefinedAuthenticatorPayload;
import org.wso2.identity.integration.test.restclients.CustomAuthenticatorManagementClient;
import org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encapsulates the configuration of a single custom authenticator endpoint authentication mode:
 * how to create the authenticator (local or federated), which inbound header the mock authenticator
 * endpoint should assert, and (for OAuth2-based modes) how to stub and verify the upstream token
 * endpoint.
 *
 * One enum constant per supported endpoint auth type so the success / failure test classes can
 * loop over them via {@link org.testng.annotations.Factory}.
 */
public enum EndpointAuthScenario {

    BASIC {
        @Override
        public String createLocalAuthenticator(CustomAuthenticatorManagementClient client, String authenticatorName,
                                               String displayName, String endpointUri) throws Exception {

            return client.createCustomAuthWithBasicEndpointAuth(authenticatorName, displayName, endpointUri,
                    BASIC_USERNAME, BASIC_PASSWORD);
        }

        @Override
        public UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri) {

            return CustomFederatedAuthenticatorPayloadBuilder.buildBasic(endpointUri, BASIC_USERNAME, BASIC_PASSWORD);
        }

        @Override
        public String expectedInboundHeaderName() {

            return MockCustomAuthenticatorService.AUTHORIZATION_HEADER;
        }

        @Override
        public String expectedInboundHeaderValue() {

            return "Basic " + Base64.getEncoder().encodeToString(
                    (BASIC_USERNAME + ":" + BASIC_PASSWORD).getBytes(StandardCharsets.UTF_8));
        }
    },

    BEARER {
        @Override
        public String createLocalAuthenticator(CustomAuthenticatorManagementClient client, String authenticatorName,
                                               String displayName, String endpointUri) throws Exception {

            return client.createCustomAuthWithBearerEndpointAuth(authenticatorName, displayName, endpointUri,
                    BEARER_ACCESS_TOKEN);
        }

        @Override
        public UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri) {

            return CustomFederatedAuthenticatorPayloadBuilder.buildBearer(endpointUri, BEARER_ACCESS_TOKEN);
        }

        @Override
        public String expectedInboundHeaderName() {

            return MockCustomAuthenticatorService.AUTHORIZATION_HEADER;
        }

        @Override
        public String expectedInboundHeaderValue() {

            return "Bearer " + BEARER_ACCESS_TOKEN;
        }
    },

    API_KEY {
        @Override
        public String createLocalAuthenticator(CustomAuthenticatorManagementClient client, String authenticatorName,
                                               String displayName, String endpointUri) throws Exception {

            return client.createCustomAuthWithApiKeyEndpointAuth(authenticatorName, displayName, endpointUri,
                    API_KEY_HEADER_NAME, API_KEY_VALUE);
        }

        @Override
        public UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri) {

            return CustomFederatedAuthenticatorPayloadBuilder.buildApiKey(endpointUri, API_KEY_HEADER_NAME,
                    API_KEY_VALUE);
        }

        @Override
        public String expectedInboundHeaderName() {

            return API_KEY_HEADER_NAME;
        }

        @Override
        public String expectedInboundHeaderValue() {

            return API_KEY_VALUE;
        }
    },

    CLIENT_CREDENTIAL {
        @Override
        public String createLocalAuthenticator(CustomAuthenticatorManagementClient client, String authenticatorName,
                                               String displayName, String endpointUri) throws Exception {

            return client.createCustomAuthWithClientCredentialEndpointAuth(authenticatorName, displayName, endpointUri,
                    MOCK_IDP_CLIENT_ID, MOCK_IDP_CLIENT_SECRET, MOCK_IDP_TOKEN_ENDPOINT_URI, MOCK_IDP_SCOPES);
        }

        @Override
        public UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri) {

            return CustomFederatedAuthenticatorPayloadBuilder.buildClientCredential(endpointUri, MOCK_IDP_CLIENT_ID,
                    MOCK_IDP_CLIENT_SECRET, MOCK_IDP_TOKEN_ENDPOINT_URI, MOCK_IDP_SCOPES);
        }

        @Override
        public String expectedInboundHeaderName() {

            return MockCustomAuthenticatorService.AUTHORIZATION_HEADER;
        }

        @Override
        public String expectedInboundHeaderValue() {

            return "Bearer " + MOCK_IDP_ACCESS_TOKEN;
        }

        @Override
        public boolean requiresTokenEndpoint() {

            return true;
        }

        @Override
        public void installSuccessTokenEndpointStub(ServiceExtensionMockServer server) {

            server.setupTokenEndpointStubForClientCredentialsGrant(MOCK_IDP_TOKEN_ENDPOINT_PATH,
                    "Basic " + base64(MOCK_IDP_CLIENT_ID, MOCK_IDP_CLIENT_SECRET), MOCK_IDP_ACCESS_TOKEN);
        }

        @Override
        public void verifyTokenEndpointReceivedGrantRequest(ServiceExtensionMockServer server) {

            int callCount = server.getReceivedRequestCount(MOCK_IDP_TOKEN_ENDPOINT_PATH);
            Assert.assertTrue(callCount >= 1, "Expected at least one call to the configured token endpoint.");

            String payload = server.getReceivedRequestPayload(MOCK_IDP_TOKEN_ENDPOINT_PATH);
            Assert.assertTrue(payload.contains("grant_type=client_credentials"),
                    "Expected grant_type=client_credentials in token endpoint request body. Body: " + payload);
            Assert.assertFalse(payload.contains("username="),
                    "Token endpoint request body must not contain a username field. Body: " + payload);
            Assert.assertFalse(payload.contains("password="),
                    "Token endpoint request body must not contain a password field. Body: " + payload);
        }
    },

    PASSWORD_CREDENTIAL {
        @Override
        public String createLocalAuthenticator(CustomAuthenticatorManagementClient client, String authenticatorName,
                                               String displayName, String endpointUri) throws Exception {

            return client.createCustomAuthWithPasswordCredentialEndpointAuth(authenticatorName, displayName,
                    endpointUri, MOCK_IDP_CLIENT_ID, MOCK_IDP_CLIENT_SECRET, MOCK_IDP_TOKEN_ENDPOINT_URI,
                    MOCK_IDP_USERNAME, MOCK_IDP_PASSWORD, MOCK_IDP_SCOPES);
        }

        @Override
        public UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri) {

            return CustomFederatedAuthenticatorPayloadBuilder.buildPasswordCredential(endpointUri, MOCK_IDP_CLIENT_ID,
                    MOCK_IDP_CLIENT_SECRET, MOCK_IDP_TOKEN_ENDPOINT_URI, MOCK_IDP_USERNAME, MOCK_IDP_PASSWORD,
                    MOCK_IDP_SCOPES);
        }

        @Override
        public String expectedInboundHeaderName() {

            return MockCustomAuthenticatorService.AUTHORIZATION_HEADER;
        }

        @Override
        public String expectedInboundHeaderValue() {

            return "Bearer " + MOCK_IDP_ACCESS_TOKEN;
        }

        @Override
        public boolean requiresTokenEndpoint() {

            return true;
        }

        @Override
        public void installSuccessTokenEndpointStub(ServiceExtensionMockServer server) {

            server.setupTokenEndpointStubForPasswordGrant(MOCK_IDP_TOKEN_ENDPOINT_PATH,
                    "Basic " + base64(MOCK_IDP_CLIENT_ID, MOCK_IDP_CLIENT_SECRET), MOCK_IDP_ACCESS_TOKEN,
                    MOCK_IDP_USERNAME, MOCK_IDP_PASSWORD);
        }

        @Override
        public void verifyTokenEndpointReceivedGrantRequest(ServiceExtensionMockServer server) {

            int callCount = server.getReceivedRequestCount(MOCK_IDP_TOKEN_ENDPOINT_PATH);
            Assert.assertTrue(callCount >= 1, "Expected at least one call to the configured token endpoint.");

            String payload = server.getReceivedRequestPayload(MOCK_IDP_TOKEN_ENDPOINT_PATH);
            Assert.assertTrue(payload.contains("grant_type=password"),
                    "Expected grant_type=password in token endpoint request body. Body: " + payload);
            Assert.assertTrue(payload.contains("username=" + MOCK_IDP_USERNAME),
                    "Expected configured username in token endpoint request body. Body: " + payload);
            Assert.assertTrue(payload.contains("password=" + MOCK_IDP_PASSWORD),
                    "Expected configured password in token endpoint request body. Body: " + payload);
        }
    };

    // -- Per-scenario fixed configuration values used across the test family. --------------------

    public static final String BASIC_USERNAME = "endpointUsername";
    public static final String BASIC_PASSWORD = "endpointPassword";
    public static final String BEARER_ACCESS_TOKEN = "static-bearer-token-value";
    public static final String API_KEY_HEADER_NAME = "X-API-Key";
    public static final String API_KEY_VALUE = "static-api-key-value";

    // Token endpoint stub configuration — mirrors the Actions framework tests
    // (ActionsBaseTestCase.java:37-55).
    public static final String MOCK_IDP_TOKEN_ENDPOINT_PATH = "/test/idp/token";
    public static final String MOCK_IDP_TOKEN_ENDPOINT_URI = "http://localhost:8587" + MOCK_IDP_TOKEN_ENDPOINT_PATH;
    public static final String MOCK_IDP_CLIENT_ID = "test-idp-client-id";
    public static final String MOCK_IDP_CLIENT_SECRET = "test-idp-client-secret";
    public static final String MOCK_IDP_USERNAME = "test-idp-user";
    public static final String MOCK_IDP_PASSWORD = "test-idp-password";
    public static final String MOCK_IDP_SCOPES = "send_scope";
    public static final String MOCK_IDP_ACCESS_TOKEN = "mock-idp-access-token-value";

    // -- Subclass-supplied behavior --------------------------------------------------------------

    public abstract String createLocalAuthenticator(CustomAuthenticatorManagementClient client,
                                                    String authenticatorName, String displayName,
                                                    String endpointUri) throws Exception;

    public abstract UserDefinedAuthenticatorPayload buildFederatedPayload(String endpointUri);

    public abstract String expectedInboundHeaderName();

    public abstract String expectedInboundHeaderValue();

    public boolean requiresTokenEndpoint() {

        return false;
    }

    public void installSuccessTokenEndpointStub(ServiceExtensionMockServer server) {

        // Default no-op for scenarios that don't use an upstream token endpoint.
    }

    /**
     * Failure variant: install a token endpoint stub that always returns HTTP 500. Only meaningful
     * for the OAuth2 scenarios.
     */
    public void installFailureTokenEndpointStub(ServiceExtensionMockServer server) {

        server.setupTokenEndpointStubWithError(MOCK_IDP_TOKEN_ENDPOINT_PATH, 500,
                "{\"error\":\"server_error\",\"error_description\":\"mock failure\"}");
    }

    public void verifyTokenEndpointReceivedGrantRequest(ServiceExtensionMockServer server) {

        // Default no-op for scenarios that don't use an upstream token endpoint.
    }

    private static String base64(String left, String right) {

        return Base64.getEncoder().encodeToString((left + ":" + right).getBytes(StandardCharsets.UTF_8));
    }
}
