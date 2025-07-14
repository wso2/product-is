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
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.AuthenticationType;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorCreation;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util.UserDefinedLocalAuthenticatorPayload;

import java.io.IOException;
import java.util.HashMap;

/**
 * This class provides methods to manage custom authenticators via REST API.
 */
public class CustomAuthenticatorManagementClient extends RestBaseClient {

    private static final String AUTHENTICATOR_CUSTOM_API_BASE_PATH = "/authenticators/custom";
    private static final String PATH_SEPARATOR = "/";
    private final String username;
    private final String password;

    private final String customAuthenticatorAPIBasePath;

    public CustomAuthenticatorManagementClient(String serverUrl, Tenant tenantInfo) {

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        customAuthenticatorAPIBasePath =
                getCustomAuthenticatorAPIBasePath(serverUrl, tenantInfo.getContextUser().getUserDomain());

    }

    public String createCustomInternalUserAuthenticator(String authenticatorName, String displayName,
                                                        String endpointUri,
                                                        String endpointAuthUsername, String endpointAuthPassword)
            throws Exception {

        UserDefinedLocalAuthenticatorConfig testAuthenticatorConfig =
                createUserDefinedInternalUserAuthenticator(authenticatorName, displayName, endpointUri,
                        endpointAuthUsername, endpointAuthPassword);
        UserDefinedLocalAuthenticatorCreation authenticatorCreationPayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorCreation(testAuthenticatorConfig);

        try {
            String jsonRequestBody =
                    UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(authenticatorCreationPayload);

            try (CloseableHttpResponse response = getResponseOfHttpPost(customAuthenticatorAPIBasePath, jsonRequestBody,
                    getHeaders())) {
                String[] locationElements = response.getHeaders(LOCATION_HEADER)[0].toString().split(PATH_SEPARATOR);
                return locationElements[locationElements.length - 1];
            }
        } catch (JsonProcessingException e) {
            throw new Exception("Error while creating custom internal user authenticator request payload.", e);
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

    private UserDefinedLocalAuthenticatorConfig createUserDefinedInternalUserAuthenticator(String name,
                                                                                           String displayName,
                                                                                           String endpointUri,
                                                                                           String username,
                                                                                           String password) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        config.setName(name);
        config.setDisplayName(displayName);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(endpointUri);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.BASIC));
        endpointConfig.authenticationProperties(new HashMap<String, String>() {{
            put("username", username);
            put("password", password);
        }});
        config.setEndpointConfig(endpointConfig.build());

        return config;
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
