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

package org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.AuthenticationType;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class AuthenticatorTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "authenticators.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.authenticators.v1";

    protected static final String BASE_PATH = "/api/server/v1";
    protected static final String AUTHENTICATOR_API_BASE_PATH = "/authenticators";
    protected static final String AUTHENTICATOR_META_TAGS_PATH = "/authenticators/meta/tags";
    protected static final String AUTHENTICATOR_CUSTOM_API_BASE_PATH = "/authenticators/custom";
    protected static final String AUTHENTICATOR_CONFIG_API_BASE_PATH = "/configs/authenticators/";
    protected static final String PATH_SEPARATOR = "/";

    protected final String AUTHENTICATOR_NAME = "custom-Authenticator";
    protected final String AUTHENTICATOR_NAME_CC = "custom-AuthenticatorCC";
    protected final String AUTHENTICATOR_NAME_PWD = "custom-AuthenticatorPWD";
    protected final String AUTHENTICATOR_DISPLAY_NAME = "ABC custom authenticator";
    protected final String AUTHENTICATOR_DISPLAY_NAME_CC = "ABC custom authenticator CC";
    protected final String AUTHENTICATOR_DISPLAY_NAME_PWD = "ABC custom authenticator PWD";
    protected final String AUTHENTICATOR_IMAGE = "https://test.com/authenticator.png";
    protected final String AUTHENTICATOR_DESCRIPTION = "This is custom local authenticator.";
    protected final String AUTHENTICATOR_ENDPOINT_URI = "https://test.com/authenticate";
    protected final String AUTHENTICATOR_TOKEN_ENDPOINT_URI = "https://test.com/oauth2/token";
    protected final String AUTHENTICATOR_ENDPOINT_CLIENT_ID = "endpointClientId";
    protected final String AUTHENTICATOR_ENDPOINT_CLIENT_SECRET = "endpointClientSecret";
    protected final String AUTHENTICATOR_ENDPOINT_SCOPES = "internal_login";
    protected final String AUTHENTICATOR_ENDPOINT_USERNAME = "endpointUsername";
    protected final String AUTHENTICATOR_ENDPOINT_PASSWORD = "endpointPassword";
    protected final String customAuthenticatorId = Base64.getUrlEncoder().withoutPadding().encodeToString(
            AUTHENTICATOR_NAME.getBytes(StandardCharsets.UTF_8));
    protected final String customAuthenticatorIdCC = Base64.getUrlEncoder().withoutPadding().encodeToString(
            AUTHENTICATOR_NAME_CC.getBytes(StandardCharsets.UTF_8));
    protected final String customAuthenticatorIdPWD = Base64.getUrlEncoder().withoutPadding().encodeToString(
            AUTHENTICATOR_NAME_PWD.getBytes(StandardCharsets.UTF_8));
    protected final String UPDATE_VALUE_POSTFIX = "Updated";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    protected UserDefinedLocalAuthenticatorConfig createBaseUserDefinedLocalAuthenticator(
            AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(type);
        config.setName(AUTHENTICATOR_NAME);
        config.setDisplayName(AUTHENTICATOR_DISPLAY_NAME);
        config.setImageUrl(AUTHENTICATOR_IMAGE);
        config.setDescription(AUTHENTICATOR_DESCRIPTION);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(AUTHENTICATOR_ENDPOINT_URI);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.BASIC));
        endpointConfig.authenticationProperties(new HashMap<String, String>() {{
                put("username", "adminUsername");
                put("password", "adminPassword");
            }});
        config.setEndpointConfig(endpointConfig.build());

        return config;
    }

    protected UserDefinedLocalAuthenticatorConfig createClientCredentialUserDefinedLocalAuthenticator(
            AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(type);
        config.setName(AUTHENTICATOR_NAME_CC);
        config.setDisplayName(AUTHENTICATOR_DISPLAY_NAME_CC);
        config.setImageUrl(AUTHENTICATOR_IMAGE);
        config.setDescription(AUTHENTICATOR_DESCRIPTION);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(AUTHENTICATOR_ENDPOINT_URI);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.CLIENT_CREDENTIAL));
        endpointConfig.authenticationProperties(new HashMap<String, String>() {{
                put("clientId", AUTHENTICATOR_ENDPOINT_CLIENT_ID);
                put("clientSecret", AUTHENTICATOR_ENDPOINT_CLIENT_SECRET);
                put("tokenEndpoint", AUTHENTICATOR_TOKEN_ENDPOINT_URI);
                put("scopes", AUTHENTICATOR_ENDPOINT_SCOPES);
            }});
        config.setEndpointConfig(endpointConfig.build());

        return config;
    }

    protected UserDefinedLocalAuthenticatorConfig createPasswordCredentialUserDefinedLocalAuthenticator(
            AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(type);
        config.setName(AUTHENTICATOR_NAME_PWD);
        config.setDisplayName(AUTHENTICATOR_DISPLAY_NAME_PWD);
        config.setImageUrl(AUTHENTICATOR_IMAGE);
        config.setDescription(AUTHENTICATOR_DESCRIPTION);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(AUTHENTICATOR_ENDPOINT_URI);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.PASSWORD_CREDENTIAL));
        endpointConfig.authenticationProperties(new HashMap<String, String>() {{
                put("username", AUTHENTICATOR_ENDPOINT_USERNAME);
                put("password", AUTHENTICATOR_ENDPOINT_PASSWORD);
                put("tokenEndpoint", AUTHENTICATOR_TOKEN_ENDPOINT_URI);
                put("clientId", AUTHENTICATOR_ENDPOINT_CLIENT_ID);
                put("clientSecret", AUTHENTICATOR_ENDPOINT_CLIENT_SECRET);
                put("scopes", AUTHENTICATOR_ENDPOINT_SCOPES);
            }});
        config.setEndpointConfig(endpointConfig.build());

        return config;
    }
}
