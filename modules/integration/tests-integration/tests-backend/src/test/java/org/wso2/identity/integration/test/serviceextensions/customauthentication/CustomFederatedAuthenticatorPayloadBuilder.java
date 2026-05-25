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

import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.util.UserDefinedAuthenticatorPayload;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory for {@link UserDefinedAuthenticatorPayload} instances used by the federated custom
 * authenticator integration tests. Mirrors the factory methods on
 * {@link org.wso2.identity.integration.test.restclients.CustomAuthenticatorManagementClient},
 * but targets the IdP API's {@link UserDefinedAuthenticatorPayload} model.
 */
public final class CustomFederatedAuthenticatorPayloadBuilder {

    public static final String DEFAULT_FEDERATED_AUTHENTICATOR_ID = "Y3VzdG9tLUF1dGhlbnRpY2F0b3Ix";

    public static final String USERNAME_PROPERTY = "username";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String ACCESS_TOKEN_PROPERTY = "accessToken";
    public static final String API_KEY_HEADER_PROPERTY = "apiKeyHeader";
    public static final String API_KEY_VALUE_PROPERTY = "apiKeyValue";
    public static final String CLIENT_ID_PROPERTY = "clientId";
    public static final String CLIENT_SECRET_PROPERTY = "clientSecret";
    public static final String TOKEN_ENDPOINT_PROPERTY = "tokenEndpoint";
    public static final String SCOPES_PROPERTY = "scopes";

    private CustomFederatedAuthenticatorPayloadBuilder() {

    }

    public static UserDefinedAuthenticatorPayload buildBasic(String endpointUri, String username, String password) {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(USERNAME_PROPERTY, username);
        properties.put(PASSWORD_PROPERTY, password);
        return build(endpointUri, AuthenticationType.TypeEnum.BASIC, properties);
    }

    public static UserDefinedAuthenticatorPayload buildBearer(String endpointUri, String accessToken) {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(ACCESS_TOKEN_PROPERTY, accessToken);
        return build(endpointUri, AuthenticationType.TypeEnum.BEARER, properties);
    }

    public static UserDefinedAuthenticatorPayload buildApiKey(String endpointUri, String apiKeyHeader,
                                                              String apiKeyValue) {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(API_KEY_HEADER_PROPERTY, apiKeyHeader);
        properties.put(API_KEY_VALUE_PROPERTY, apiKeyValue);
        return build(endpointUri, AuthenticationType.TypeEnum.API_KEY, properties);
    }

    public static UserDefinedAuthenticatorPayload buildClientCredential(String endpointUri, String clientId,
                                                                        String clientSecret, String tokenEndpoint,
                                                                        String scopes) {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(CLIENT_ID_PROPERTY, clientId);
        properties.put(CLIENT_SECRET_PROPERTY, clientSecret);
        properties.put(TOKEN_ENDPOINT_PROPERTY, tokenEndpoint);
        properties.put(SCOPES_PROPERTY, scopes);
        return build(endpointUri, AuthenticationType.TypeEnum.CLIENT_CREDENTIAL, properties);
    }

    public static UserDefinedAuthenticatorPayload buildPasswordCredential(String endpointUri, String clientId,
                                                                          String clientSecret, String tokenEndpoint,
                                                                          String username, String password,
                                                                          String scopes) {

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(CLIENT_ID_PROPERTY, clientId);
        properties.put(CLIENT_SECRET_PROPERTY, clientSecret);
        properties.put(TOKEN_ENDPOINT_PROPERTY, tokenEndpoint);
        properties.put(USERNAME_PROPERTY, username);
        properties.put(PASSWORD_PROPERTY, password);
        properties.put(SCOPES_PROPERTY, scopes);
        return build(endpointUri, AuthenticationType.TypeEnum.PASSWORD_CREDENTIAL, properties);
    }

    private static UserDefinedAuthenticatorPayload build(String endpointUri, AuthenticationType.TypeEnum type,
                                                         Map<String, Object> properties) {

        UserDefinedAuthenticatorPayload payload = new UserDefinedAuthenticatorPayload();
        payload.setIsEnabled(true);
        payload.setAuthenticatorId(DEFAULT_FEDERATED_AUTHENTICATOR_ID);
        payload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(type);
        authenticationType.setProperties(properties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpointUri);
        endpoint.authentication(authenticationType);

        payload.setEndpoint(endpoint);
        return payload;
    }
}
