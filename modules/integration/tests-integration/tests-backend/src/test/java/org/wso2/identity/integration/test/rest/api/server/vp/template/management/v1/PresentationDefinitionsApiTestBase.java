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

package org.wso2.identity.integration.test.rest.api.server.vp.template.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base test class for Presentation Definition REST API integration tests.
 */
public class PresentationDefinitionsApiTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "presentation-definition.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.vp.template.management.v1";
    public static final String PRESENTATION_DEFINITIONS_API_BASE_PATH = "/presentation-definitions";
    public static final String PATH_SEPARATOR = "/";
    public static final String CONNECTED_IDPS_PATH = "connected-idps";
    public static final String CREDENTIALS_PATH = "credentials";
    public static final String ISSUER_CONFIGS_PATH = "issuer-configs";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s",
                    API_DEFINITION_NAME, API_PACKAGE_NAME), e);
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
