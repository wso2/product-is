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
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for tenant management REST APIs failure paths.
 */
public class TenantFailureTest extends TenantManagementBaseTest {

    private String tenantId;

    public TenantFailureTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
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

    @Test
    public void testGetTenantWithInvalidId() {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "TM-60014", "random-id");
    }

    @Test
    public void addTenantConflict() throws IOException {

        Response response = getResponseOfPost(TENANT_API_BASE_PATH, readResource("add-tenant.json"));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        response = getResponseOfPost(TENANT_API_BASE_PATH, readResource("add-tenant-conflict.json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "TM-60009", "abc2.com");
    }

    @Test
    public void testGetOwnersWithInvalidId() {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + "random-id" +
                TENANT_API_OWNER_PATH);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "TM-60014", "random-id");
    }

}
