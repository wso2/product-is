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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantResponseModel;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for tenant management REST APIs success paths.
 */
public class TenantSuccessTest extends TenantManagementBaseTest {

    private String tenantId;
    private String userId;

    public TenantSuccessTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInitWithoutTenantQualifiedPath(API_VERSION, swaggerDefinition);
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

    @Test(description = "Test adding tenants with different provisioning method.")
    public void testAddTenant() throws IOException {

        String body = readResource("add-tenant-inline-password.json");
        Response response = getResponseOfPost(TENANT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        tenantId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(tenantId);
    }

    @Test(dependsOnMethods = {"testAddTenant"})
    public void testGetTenantByDomainName() {

        Response response =
                getResponseOfGet(TENANT_API_BASE_PATH + TENANT_DOMAIN_BASE_PATH + PATH_SEPARATOR + TENANT_DOMAIN_NAME);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(tenantId))
                .body("domain", equalTo(TENANT_DOMAIN_NAME));
    }

    @Test(dependsOnMethods = {"testGetTenantByDomainName"})
    public void testGetTenant() throws IOException {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantId);
        String ownerName = "kim";
        String baseIdentifier = "owners.find{ it.username == '" + ownerName + "' }.";
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(tenantId))
                .body("domain", equalTo("abc1.com"))
                .body(baseIdentifier, notNullValue());
        TenantResponseModel tenantResponseModel = response.getBody().as(TenantResponseModel.class);
        userId = tenantResponseModel.getOwners().get(0).getId();
    }

    @Test(dependsOnMethods = {"testGetTenant"})
    public void testGetTenants() throws Exception {

        String baseIdentifier = "tenants.find{ it.id == '" + tenantId + "' }.";
        String activeStatusIdentifier = "owners.find{ it.id == '" + userId + "' }.";
        Response response = getResponseOfGet(TENANT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "domain", equalTo("abc1.com"))
                .body(baseIdentifier + activeStatusIdentifier + "username", equalTo("kim"));
    }

    @Test(dependsOnMethods = {"testGetTenant"})
    public void testGetOwners() throws Exception {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantId +
                TENANT_API_OWNER_PATH);

        String activeStatusIdentifier = "find{ it.id == '" + userId + "'}";
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(activeStatusIdentifier, notNullValue())
                .body(activeStatusIdentifier + ".username", equalTo("kim"));
    }
}
