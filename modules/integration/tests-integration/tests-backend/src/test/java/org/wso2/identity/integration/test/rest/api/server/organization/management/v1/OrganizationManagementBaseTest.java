/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.IOException;

/**
 * Base test class for the Organization Management REST APIs.
 */
public class OrganizationManagementBaseTest extends RESTAPIServerTestBase {

    public static final String SUPER_ORGANIZATION_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String API_DEFINITION_NAME = "org.wso2.carbon.identity.organization.management.yaml";
    static final String API_VERSION = "v1";

    static final String ORGANIZATION_MANAGEMENT_API_BASE_PATH = "/organizations";
    static final String ORGANIZATION_CONFIGS_API_BASE_PATH = "/organization-configs";
    static final String PATH_SEPARATOR = "/";

    protected static String swaggerDefinition;
    protected OAuth2RestClient oAuth2RestClient;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.organization.management.v1";

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
