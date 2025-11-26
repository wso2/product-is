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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util;

import org.wso2.identity.integration.test.base.MockOAuth2TokenServer;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Authentication;

/**
 * Helper class to create Authentication objects for SMS senders
 */
public class AuthenticationBuilder {

    // Basic Auth constants
    public static final String BASIC_AUTH_USERNAME = "testuser";
    public static final String BASIC_AUTH_PASSWORD = "testpass";

    // Client Credential Auth constants
    public static final String CLIENT_CREDENTIAL_CLIENT_ID = "testClientId";
    public static final String CLIENT_CREDENTIAL_CLIENT_SECRET = "testClientSecret";
    public static final String ENCODED_CREDENTIAL = "dGVzdENsaWVudElkOnRlc3RDbGllbnRTZWNyZXQ=";
    public static final String CLIENT_CREDENTIAL_SCOPES = "read write";

    // API Key Auth constants
    public static final String API_KEY_HEADER = "test-api-header";
    public static final String API_KEY_VALUE = "test-api-key-12345";

    // Bearer Auth constants
    public static final String BEARER_TOKEN = "test-bearer-token-12345";

    /**
     * Creates a sample Authentication object based on the specified authentication type.
     *
     * @param authType the authentication type (BASIC, CLIENT_CREDENTIAL, API_KEY, BEARER, NONE)
     * @return Authentication object configured for the specified authentication type with sample values
     */
    public static Authentication createSampleAuth(Authentication.TypeEnum authType) {
        switch (authType) {
            case BASIC:
                return createBasicAuth(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
            case CLIENT_CREDENTIAL:
                return createClientCredentialAuth(CLIENT_CREDENTIAL_CLIENT_ID, CLIENT_CREDENTIAL_CLIENT_SECRET,
                        MockOAuth2TokenServer.TOKEN_ENDPOINT_URL, CLIENT_CREDENTIAL_SCOPES);
            case API_KEY:
                return createApiKeyAuth(API_KEY_HEADER, API_KEY_VALUE);
            case BEARER:
                return createBearerAuth(BEARER_TOKEN);
            case NONE:
                return createNoAuth();
            default:
                throw new IllegalArgumentException("Unsupported authentication type: " + authType);
        }
    }

    /**
     * Create a BASIC authentication object
     *
     * @param username Username for basic auth
     * @param password Password for basic auth
     * @return Authentication object configured for BASIC auth
     */
    public static Authentication createBasicAuth(String username, String password) {

        Authentication authentication = new Authentication();
        authentication.setType(Authentication.TypeEnum.BASIC);
        authentication.putPropertiesItem("username", username);
        authentication.putPropertiesItem("password", password);
        return authentication;
    }

    /**
     * Create a CLIENT_CREDENTIAL authentication object
     *
     * @param clientId Client ID
     * @param clientSecret Client secret
     * @param tokenEndpoint Token endpoint URL
     * @param scopes Scopes (optional)
     * @return Authentication object configured for CLIENT_CREDENTIAL auth
     */
    public static Authentication createClientCredentialAuth(String clientId, String clientSecret, 
                                                             String tokenEndpoint, String scopes) {

        Authentication authentication = new Authentication();
        authentication.setType(Authentication.TypeEnum.CLIENT_CREDENTIAL);
        authentication.putPropertiesItem("clientId", clientId);
        authentication.putPropertiesItem("clientSecret", clientSecret);
        authentication.putPropertiesItem("tokenEndpoint", tokenEndpoint);
        if (scopes != null && !scopes.isEmpty()) {
            authentication.putPropertiesItem("scopes", scopes);
        }
        return authentication;
    }

    /**
     * Create an API_KEY authentication object
     *
     * @param headerName Header name for API key
     * @param apiKey API key value
     * @return Authentication object configured for API_KEY auth
     */
    public static Authentication createApiKeyAuth(String headerName, String apiKey) {

        Authentication authentication = new Authentication();
        authentication.setType(Authentication.TypeEnum.API_KEY);
        authentication.putPropertiesItem("header", headerName);
        authentication.putPropertiesItem("value", apiKey);
        return authentication;
    }

    /**
     * Create a BEARER authentication object
     *
     * @param token Bearer token
     * @return Authentication object configured for BEARER auth
     */
    public static Authentication createBearerAuth(String token) {

        Authentication authentication = new Authentication();
        authentication.setType(Authentication.TypeEnum.BEARER);
        authentication.putPropertiesItem("accessToken", token);
        return authentication;
    }

    /**
     * Create a NONE authentication object (no authentication)
     *
     * @return Authentication object configured for no authentication
     */
    public static Authentication createNoAuth() {

        Authentication authentication = new Authentication();
        authentication.setType(Authentication.TypeEnum.NONE);
        return authentication;
    }
}
