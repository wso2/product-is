/*
 * Copyright (c) 2020-2025, WSO2 LLC. (http://www.wso2.com).
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
    private String tenantWithNameId;
    private String userId;
    private static final String TELEPHONE_CLAIM = "http://wso2.org/claims/telephone";

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

    @Test(dependsOnMethods = "testAddTenant", description = "Test adding a tenants with a tenant name.")
    public void testAddTenantWithTenantName() throws IOException {

        String body = readResource(ADD_TENANT_WITH_NAME);
        Response response = getResponseOfPost(TENANT_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        tenantWithNameId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(tenantWithNameId);
    }

    @Test(dependsOnMethods = "testAddTenantWithTenantName")
    public void testGetTenantByDomainName() {

        Response response =
                getResponseOfGet(TENANT_API_BASE_PATH + TENANT_DOMAIN_BASE_PATH + PATH_SEPARATOR + TENANT_DOMAIN_NAME);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ID, equalTo(tenantId))
                .body(NAME, equalTo(TENANT_DOMAIN_NAME))
                .body(DOMAIN, equalTo(TENANT_DOMAIN_NAME));
    }

    @Test(dependsOnMethods = "testGetTenantByDomainName")
    public void testGetATenantWithName() throws IOException {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantWithNameId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ID, equalTo(tenantWithNameId))
                .body(NAME, equalTo(TENANT_NAME))
                .body(DOMAIN, equalTo(TENANT_DOMAIN_NAME_04));
    }

    @Test(dependsOnMethods = "testGetATenantWithName")
    public void testGetTenant() throws IOException {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantId);
        String ownerName = "kim";
        String baseIdentifier = "owners.find{ it.username == '" + ownerName + "' }.";
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ID, equalTo(tenantId))
                .body(NAME, equalTo(TENANT_DOMAIN_NAME))
                .body(DOMAIN, equalTo(TENANT_DOMAIN_NAME))
                .body(baseIdentifier, notNullValue());
        TenantResponseModel tenantResponseModel = response.getBody().as(TenantResponseModel.class);
        userId = tenantResponseModel.getOwners().get(0).getId();
    }

    @Test(dependsOnMethods = "testGetTenant")
    public void testGetTenants() throws Exception {

        String baseIdentifier = "tenants.find{ it.id == '" + tenantId + "' }.";
        String activeStatusIdentifier = "owners.find{ it.id == '" + userId + "' }.";
        Response response = getResponseOfGet(TENANT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + DOMAIN, equalTo(TENANT_DOMAIN_NAME))
                .body(baseIdentifier + activeStatusIdentifier + "username", equalTo("kim"));
    }

    @Test(dependsOnMethods = "testGetTenants")
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

    @Test(dependsOnMethods = "testGetOwners")
    public void testUpdateOwner() throws Exception {

        String body = readResource("update-owner.json");
        Response response = getResponseOfPut(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantId +
                TENANT_API_OWNER_PATH + PATH_SEPARATOR + userId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "testUpdateOwner")
    public void testGetOwner() {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + PATH_SEPARATOR + tenantId +
                TENANT_API_OWNER_PATH + PATH_SEPARATOR + userId + "?additionalClaims=" + TELEPHONE_CLAIM);

        String claimsIdentifier = "additionalClaims.find{ it.claim == '" + TELEPHONE_CLAIM + "' }";

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("username", equalTo("kim"))
                .body( "lastname", equalTo("lee"))
                .body(claimsIdentifier + ".value", equalTo("+94 77 123 4568"));
    }

    @Test(dependsOnMethods = "testGetOwner")
    public void testGetFilteredTenantsEqual() {

        String baseIdentifier = "tenants.find{ it.id == '" + tenantId + "' }.";
        String activeStatusIdentifier = "owners.find{ it.id == '" + userId + "' }.";
        Response response = getResponseOfGet(TENANT_API_BASE_PATH + "?filter=domainName eq "
                + TENANT_DOMAIN_NAME);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1))
                .body(baseIdentifier + DOMAIN, equalTo(TENANT_DOMAIN_NAME))
                .body(baseIdentifier + activeStatusIdentifier + "username", equalTo("kim"));
    }

    @Test(dependsOnMethods = "testGetFilteredTenantsEqual")
    public void testGetFilteredTenantsContains() {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + "?filter=domainName co abc1");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));
    }

    @Test(dependsOnMethods = "testGetFilteredTenantsContains")
    public void testGetFilteredTenantsNotAvailable() {

        Response response = getResponseOfGet(TENANT_API_BASE_PATH + "?filter=domainName eq abc99.com");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(0));
    }
}
