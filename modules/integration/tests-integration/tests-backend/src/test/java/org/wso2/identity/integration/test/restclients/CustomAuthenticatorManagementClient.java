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

package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorCreation;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util.UserDefinedLocalAuthenticatorPayload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class provides methods to manage custom authenticators via REST API.
 */
public class CustomAuthenticatorManagementClient extends RestBaseClient {

    private static final String AUTHENTICATOR_CUSTOM_API_BASE_PATH = "/authenticators/custom";
    private static final String PATH_SEPARATOR = "/";

    // Endpoint authentication type identifiers used on the wire. These mirror the framework's
    // AuthenticationType.TypeEnum string values. Strings are used directly so this client compiles
    // both before and after the api-server PR that adds CLIENT_CREDENTIAL/PASSWORD_CREDENTIAL to
    // the published enum.
    public static final String AUTH_TYPE_BASIC = "BASIC";
    public static final String AUTH_TYPE_BEARER = "BEARER";
    public static final String AUTH_TYPE_API_KEY = "API_KEY";
    public static final String AUTH_TYPE_CLIENT_CREDENTIAL = "CLIENT_CREDENTIAL";
    public static final String AUTH_TYPE_PASSWORD_CREDENTIAL = "PASSWORD_CREDENTIAL";

    // Endpoint authentication property keys (must match the framework's Authentication builders).
    public static final String USERNAME_PROPERTY = "username";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String ACCESS_TOKEN_PROPERTY = "accessToken";
    public static final String API_KEY_HEADER_PROPERTY = "apiKeyHeader";
    public static final String API_KEY_VALUE_PROPERTY = "apiKeyValue";
    public static final String CLIENT_ID_PROPERTY = "clientId";
    public static final String CLIENT_SECRET_PROPERTY = "clientSecret";
    public static final String TOKEN_ENDPOINT_PROPERTY = "tokenEndpoint";
    public static final String SCOPES_PROPERTY = "scopes";

    private final String username;
    private final String password;

    private final String customAuthenticatorAPIBasePath;

    public CustomAuthenticatorManagementClient(String serverUrl, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        customAuthenticatorAPIBasePath =
                getCustomAuthenticatorAPIBasePath(serverUrl, tenantInfo.getContextUser().getUserDomain());

    }

    /**
     * Create a user-defined local custom authenticator with BASIC endpoint authentication.
     */
    public String createCustomAuthWithBasicEndpointAuth(String authenticatorName, String displayName,
                                                        String endpointUri, String endpointAuthUsername,
                                                        String endpointAuthPassword) throws Exception {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(USERNAME_PROPERTY, endpointAuthUsername);
        properties.put(PASSWORD_PROPERTY, endpointAuthPassword);
        return createCustomAuthenticator(authenticatorName, displayName, endpointUri,
                AUTH_TYPE_BASIC, properties);
    }

    /**
     * Create a user-defined local custom authenticator with BEARER endpoint authentication.
     */
    public String createCustomAuthWithBearerEndpointAuth(String authenticatorName, String displayName,
                                                         String endpointUri, String accessToken) throws Exception {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(ACCESS_TOKEN_PROPERTY, accessToken);
        return createCustomAuthenticator(authenticatorName, displayName, endpointUri,
                AUTH_TYPE_BEARER, properties);
    }

    /**
     * Create a user-defined local custom authenticator with API_KEY endpoint authentication.
     */
    public String createCustomAuthWithApiKeyEndpointAuth(String authenticatorName, String displayName,
                                                         String endpointUri, String apiKeyHeader,
                                                         String apiKeyValue) throws Exception {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(API_KEY_HEADER_PROPERTY, apiKeyHeader);
        properties.put(API_KEY_VALUE_PROPERTY, apiKeyValue);
        return createCustomAuthenticator(authenticatorName, displayName, endpointUri,
                AUTH_TYPE_API_KEY, properties);
    }

    /**
     * Create a user-defined local custom authenticator with OAuth2 CLIENT_CREDENTIAL endpoint authentication.
     */
    public String createCustomAuthWithClientCredentialEndpointAuth(String authenticatorName, String displayName,
                                                                   String endpointUri, String clientId,
                                                                   String clientSecret, String tokenEndpoint,
                                                                   String scopes) throws Exception {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(CLIENT_ID_PROPERTY, clientId);
        properties.put(CLIENT_SECRET_PROPERTY, clientSecret);
        properties.put(TOKEN_ENDPOINT_PROPERTY, tokenEndpoint);
        properties.put(SCOPES_PROPERTY, scopes);
        return createCustomAuthenticator(authenticatorName, displayName, endpointUri,
                AUTH_TYPE_CLIENT_CREDENTIAL, properties);
    }

    /**
     * Create a user-defined local custom authenticator with OAuth2 PASSWORD_CREDENTIAL endpoint authentication.
     */
    public String createCustomAuthWithPasswordCredentialEndpointAuth(String authenticatorName, String displayName,
                                                                     String endpointUri, String clientId,
                                                                     String clientSecret, String tokenEndpoint,
                                                                     String endpointAuthUsername,
                                                                     String endpointAuthPassword,
                                                                     String scopes) throws Exception {

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(CLIENT_ID_PROPERTY, clientId);
        properties.put(CLIENT_SECRET_PROPERTY, clientSecret);
        properties.put(TOKEN_ENDPOINT_PROPERTY, tokenEndpoint);
        properties.put(USERNAME_PROPERTY, endpointAuthUsername);
        properties.put(PASSWORD_PROPERTY, endpointAuthPassword);
        properties.put(SCOPES_PROPERTY, scopes);
        return createCustomAuthenticator(authenticatorName, displayName, endpointUri,
                AUTH_TYPE_PASSWORD_CREDENTIAL, properties);
    }

    /**
     * Backwards-compatible wrapper that delegates to {@link #createCustomAuthWithBasicEndpointAuth}.
     */
    public String createCustomInternalUserAuthenticator(String authenticatorName, String displayName,
                                                        String endpointUri,
                                                        String endpointAuthUsername, String endpointAuthPassword)
            throws Exception {

        return createCustomAuthWithBasicEndpointAuth(authenticatorName, displayName, endpointUri,
                endpointAuthUsername, endpointAuthPassword);
    }

    /**
     * Retrieve the JSON representation of a custom authenticator.
     */
    public String getCustomAuthenticator(String authenticatorId) throws Exception {

        String apiEndpoint = customAuthenticatorAPIBasePath + PATH_SEPARATOR + authenticatorId;
        try (CloseableHttpResponse response = getResponseOfHttpGet(apiEndpoint, getHeaders())) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public boolean deleteCustomAuthenticator(String authenticatorId) throws Exception {

        String apiEndpoint = customAuthenticatorAPIBasePath + PATH_SEPARATOR + authenticatorId;

        int status;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(apiEndpoint, getHeaders())) {
            status = response.getStatusLine().getStatusCode();
        }

        return status == 204;
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private String createCustomAuthenticator(String name, String displayName, String endpointUri,
                                             String authType,
                                             Map<String, String> authProperties) throws Exception {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        config.setName(name);
        config.setDisplayName(displayName);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(endpointUri);
        endpointConfig.authenticationType(authType);
        endpointConfig.authenticationProperties(new HashMap<>(authProperties));
        endpointConfig.allowedParameters(new ArrayList<>(Collections.singletonList("testParam")));
        config.setEndpointConfig(endpointConfig.build());

        UserDefinedLocalAuthenticatorCreation authenticatorCreationPayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorCreation(config);

        try {
            String jsonRequestBody =
                    UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(authenticatorCreationPayload);

            try (CloseableHttpResponse response = getResponseOfHttpPost(customAuthenticatorAPIBasePath, jsonRequestBody,
                    getHeaders())) {
                String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
                return locationElements[locationElements.length - 1];
            }
        } catch (JsonProcessingException e) {
            throw new Exception("Error while creating custom authenticator request payload.", e);
        }
    }

    private String getCustomAuthenticatorAPIBasePath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_SERVER_PATH + AUTHENTICATOR_CUSTOM_API_BASE_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_PATH +
                    AUTHENTICATOR_CUSTOM_API_BASE_PATH;
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }
}
