/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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


package org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.AuthenticationType;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorCreation;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.Endpoint;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorUpdate;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The util class to create a user defined local authenticator payload for APIs.
 */
public class UserDefinedLocalAuthenticatorPayload {

    /**
     * Convert the object to a JSON payload.
     *
     * @param ob Object to be converted to a JSON payload.
     * @return JSON payload.
     * @throws JsonProcessingException If any error occurred while converting the object to a JSON payload.
     */
    public static String convertToJasonPayload(Object ob) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsString(ob);
    }

    /**
     * Create a UserDefinedLocalAuthenticatorCreation object.
     *
     * @param config    UserDefinedLocalAuthenticatorConfig object.
     * @return UserDefinedLocalAuthenticatorCreation object.
     */
    public static UserDefinedLocalAuthenticatorCreation getBasedUserDefinedLocalAuthenticatorCreation(
            UserDefinedLocalAuthenticatorConfig config) {

        UserDefinedLocalAuthenticatorCreation configForCreation = new UserDefinedLocalAuthenticatorCreation();
        configForCreation.setName(config.getName());
        configForCreation.setDisplayName(config.getDisplayName());
        configForCreation.setIsEnabled(config.isEnabled());
        configForCreation.setAuthenticationType(UserDefinedLocalAuthenticatorCreation.AuthenticationTypeEnum.valueOf(
                config.getAuthenticationType().toString()));
        configForCreation.setEndpoint(convertToEndpoint(config.getEndpointConfig()));
        return configForCreation;
    }

    /**
     * Create a UserDefinedLocalAuthenticatorUpdate object.
     *
     * @param config    UserDefinedLocalAuthenticatorConfig object.
     * @return UserDefinedLocalAuthenticatorUpdate object.
     */
    public static UserDefinedLocalAuthenticatorUpdate getBasedUserDefinedLocalAuthenticatorUpdate(
            UserDefinedLocalAuthenticatorConfig config) {

        UserDefinedLocalAuthenticatorUpdate configForUpdate = new UserDefinedLocalAuthenticatorUpdate();
        configForUpdate.setDisplayName(config.getDisplayName());
        configForUpdate.setIsEnabled(config.isEnabled());
        configForUpdate.setEndpoint(convertToEndpoint(config.getEndpointConfig()));
        return configForUpdate;
    }

    private static Endpoint convertToEndpoint(UserDefinedAuthenticatorEndpointConfig endpointConfig) {

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpointConfig.getEndpointConfig().getUri());
        AuthenticationType authenticationConfig = new AuthenticationType();
        authenticationConfig.setType(AuthenticationType.TypeEnum.valueOf(endpointConfig.getEndpointConfig()
                .getAuthentication().getType().toString()));
        Map<String, Object> propertyMap = endpointConfig.getEndpointConfig().getAuthentication().getProperties().stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
        authenticationConfig.setProperties(propertyMap);
        authenticationConfig.setProperties(propertyMap);
        endpoint.setAuthentication(authenticationConfig);
        return endpoint;
    }
}
