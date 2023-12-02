/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.identity.integration.test.rest.api.server.organization.management.v2;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
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
import java.util.HashMap;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for failure cases of the OIDC Scope Management REST APIs.
 */
public class OrganizationManagementFailureTest extends OrganizationManagementBaseTest {

    String scopeId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementFailureTest(TestUserMode userMode) throws Exception {

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
    public void testAddOIDCScope() throws IOException {

        Response response = getResponseOfJSONPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                readResource("add-scope-request-body1.json"), new HashMap<>());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        scopeId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(scopeId);
    }

    @Test(dependsOnMethods = "testAddOIDCScope")
    public void testAddExistingOIDCScope() throws IOException {

        Response response = getResponseOfJSONPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                readResource("add-scope-request-body1.json"), new HashMap<>());
        validateHttpStatusCode(response, HttpStatus.SC_CONFLICT);
    }

    @Test(dependsOnMethods = "testAddExistingOIDCScope")
    public void testAddInvalidOIDCScope() throws IOException {

        Response response = getResponseOfJSONPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                readResource("add-invalid-scope-request-body1.json"), new HashMap<>());
        validateHttpStatusCode(response, HttpStatus.SC_BAD_REQUEST);
    }

    @Test(dependsOnMethods = "testAddInvalidOIDCScope")
    public void testDeleteOIDCScope() {

        // Delete an existing scope by id.
        Response response = getResponseOfDelete(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + scopeId);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);

        // Try to delete the same scope again. DELETE should be idempotent.
        Response responseOfDeleteFailure =
                getResponseOfDelete(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + scopeId);
        validateHttpStatusCode(responseOfDeleteFailure, HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = "testDeleteOIDCScope")
    public void testGetDeletedOIDCScope() {

        Response responseAfterDelete = getResponseOfGet(
                ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + scopeId);
        validateHttpStatusCode(responseAfterDelete, HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testGetDeletedOIDCScope")
    public void testUpdateNotExistingOIDCScope() throws IOException {

        Response response = getResponseOfPut(ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + scopeId,
                readResource("update-scope-request-body1.json"));
        validateHttpStatusCode(response, HttpStatus.SC_NOT_FOUND);
    }

}
