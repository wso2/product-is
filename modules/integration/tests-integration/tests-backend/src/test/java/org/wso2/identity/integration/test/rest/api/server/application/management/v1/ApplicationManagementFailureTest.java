/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

/**
 * Tests for negative paths of the Application Management REST API.
 */
public class ApplicationManagementFailureTest extends ApplicationManagementBaseTest {

    private static final String INVALID_APPLICATION_ID = "xxx";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetApplicationWithInvalidId() {

        Response response =
                getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + INVALID_APPLICATION_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "APP-50005");
    }

    @Test
    public void testGetApplicationsWithSortByQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortBy=xxx");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-55001");
    }

    @Test
    public void testGetApplicationsWithSortOrderQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortOrder=ASC");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-55001");
    }

    @Test
    public void testGetApplicationsWithAttributesQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?attributes=name,imageUrl");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-55002");
    }
}
