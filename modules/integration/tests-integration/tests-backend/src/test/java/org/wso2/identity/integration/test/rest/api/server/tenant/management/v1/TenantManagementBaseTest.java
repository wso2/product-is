/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.rest.api.server.tenant.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base test class for tenant management REST APIs.
 */
public class TenantManagementBaseTest extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "tenant-management.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.tenant.management.v1";
    public static final String TENANT_API_BASE_PATH = "/tenants";
    public static final String TENANT_DOMAIN_BASE_PATH = "/domain";
    public static final String TENANT_API_OWNER_PATH = "/owners";
    public static final String TENANT_API_LIFECYCLE_PATH = "/lifecycle-status";

    public static final String PATH_SEPARATOR = "/";
    public static final String TENANT_DOMAIN_NAME = "abc1.com";

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
