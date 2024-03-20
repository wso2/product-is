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

package org.wso2.identity.integration.test.rest.api.server.email.template.v2;

import io.restassured.RestAssured;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

/**
 * Base test class for the Email Templates REST APIs.
 *
 * Note: Generated model classes of the REST API were used in tests, with modified equals methods.
 */
public class EmailTemplatesTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "email-template.yml";
    static final String API_VERSION = "v2";
    private static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.server.email.template.v2";

    public static final String EMAIL_TEMPLATES_API_BASE_PATH = "/email";
    public static final String EMAIL_TEMPLATE_TYPES_PATH = "/template-types";
    public static final String ORG_EMAIL_TEMPLATES_PATH = "/org-templates";
    public static final String APP_EMAIL_TEMPLATES_PATH = "/app-templates";
    public static final String PATH_SEPARATOR = "/";

    public static final String SAMPLE_TEMPLATE_TYPE_ID = "QWNjb3VudEVuYWJsZQ";
    public static final String SAMPLE_APPLICATION_UUID = "159341d6-bc1e-4445-982d-43d4df707b9a";

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
