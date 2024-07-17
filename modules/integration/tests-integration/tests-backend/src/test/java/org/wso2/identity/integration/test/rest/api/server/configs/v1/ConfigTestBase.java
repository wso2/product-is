/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base class for server configuration management tests.
 */
public class ConfigTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "configs.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.configs.v1";

    public static final String CONFIGS_API_BASE_PATH = "/configs";
    public static final String CONFIGS_AUTHENTICATOR_API_BASE_PATH = "/configs/authenticators";
    public static final String CONFIGS_INBOUND_SCIM_API_BASE_PATH = "/configs/provisioning/inbound/scim";
    public static final String CORS_CONFIGS_API_BASE_PATH = "/configs/cors";
    public static final String IMPERSONATION_CONFIGS_API_BASE_PATH = "/configs/impersonation";
    public static final String HOME_REALM_IDENTIFIERS_API_BASE_PATH = "/configs/home-realm-identifiers";
    public static final String SAML_INBOUND_AUTH_CONFIG_API_PATH = "/configs/authentication/inbound/saml2";
    public static final String PASSIVE_STS_INBOUND_AUTH_CONFIG_API_PATH = "/configs/authentication/inbound/passivests";
    public static final String SAML_METADATA_ENDPOINT_SUPER_TENANT = "https://localhost:9853/identity/metadata/saml2";
    public static final String SAML_METADATA_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/identity/metadata/saml2";
    public static final String SAML_SSO_URL_SUPER_TENANT = "https://localhost:9853/samlsso";
    public static final String SAML_SSO_URL_TENANT = "https://localhost:9853/t/wso2.com/samlsso";
    public static final String PASSIVE_STS_URL_SUPER_TENANT = "https://localhost:9853/passivests";
    public static final String PASSIVE_STS_URL_TENANT = "https://localhost:9853/t/wso2.com/passivests";

    public static final String PATH_SEPARATOR = "/";
    public static final String SAMPLE_AUTHENTICATOR_ID = "QmFzaWNBdXRoZW50aWNhdG9y";

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
}
