/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.secret.management.v1;

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

import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Secret Management REST APIs failure paths.
 */
public class SecretManagementFailureTest extends SecretManagementTestBase {

    public static final String INCORRECT_SECRET_TYPE = "INCORRECT_TYPE";
    public static final String SECRET_API_INCORRECT_BASE_PATH = "/secret-mgt/types/"
            + INCORRECT_SECRET_TYPE + "/secrets";
    private String secretId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SecretManagementFailureTest(TestUserMode userMode) throws Exception {

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
    public void testGetSecretsByInvalidTypeName() {

        Response response = getResponseOfGet(SECRET_API_INCORRECT_BASE_PATH);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testAddSecretConflict() throws IOException {

        String body = readResource("add-secret-2.json");
        Response response = getResponseOfPost(SECRET_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        secretId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(secretId);

        response = getResponseOfPost(SECRET_API_BASE_PATH, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "SECRETM_60006");
    }

    @Test
    public void testAddSecretWithNonExistingSecretTypeName() throws IOException {

        String body = readResource("add-secret.json");
        Response response = getResponseOfPost(SECRET_API_INCORRECT_BASE_PATH, body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test(dependsOnMethods = {"testAddSecretConflict"})
    public void testGetSecretByNonExistingSecretTypeName() {

        Response response = getResponseOfGet(
                SECRET_API_INCORRECT_BASE_PATH + PATH_SEPARATOR + secretId);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testGetSecretByNonExistingSecretId() {

        Response response = getResponseOfGet(SECRET_API_BASE_PATH
                + PATH_SEPARATOR + "0273-2933-2132-3321");

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00013");
    }

    @Test(dependsOnMethods = {"testAddSecretConflict"})
    public void testUpdateSecretByNonExistingSecretTypeName() throws IOException {

        String body = readResource("update-secret.json");
        Response response = getResponseOfPut(
                SECRET_API_INCORRECT_BASE_PATH + PATH_SEPARATOR + secretId, body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testUpdateSecretByNonExistingSecretId() throws IOException {

        String body = readResource("update-secret.json");
        Response response = getResponseOfPut(
                SECRET_API_BASE_PATH + PATH_SEPARATOR + "0273-2933-2132-3321", body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_60003");
    }

    @Test(dependsOnMethods = {"testAddSecretConflict"})
    public void testPatchSecretValueByNonExistingSecretTypeName() throws IOException {

        String body = readResource("patch-secret-value.json");
        Response response = getResponseOfPatch(
                SECRET_API_INCORRECT_BASE_PATH + PATH_SEPARATOR + secretId, body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testPatchSecretValueByNonExistingSecretId() throws IOException {

        String body = readResource("patch-secret-value.json");
        Response response = getResponseOfPatch(
                SECRET_API_BASE_PATH + PATH_SEPARATOR + "0273-2933-2132-3321", body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_60003");
    }

    @Test(dependsOnMethods = {"testAddSecretConflict"})
    public void testPatchSecretDescriptionByNonExistingSecretTypeName() throws IOException {

        String body = readResource("patch-secret-description.json");
        Response response = getResponseOfPatch(
                SECRET_API_INCORRECT_BASE_PATH + PATH_SEPARATOR + secretId, body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testPatchSecretDescriptionByNonExistingSecretId() throws IOException {

        String body = readResource("patch-secret-description.json");
        Response response = getResponseOfPatch(
                SECRET_API_BASE_PATH + PATH_SEPARATOR + "0273-2933-2132-3321", body);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_60003");
    }

    @Test(dependsOnMethods = {"testAddSecretConflict"})
    public void testDeleteSecretByNonExistingSecretTypeName() {

        Response response = getResponseOfDelete(
                SECRET_API_INCORRECT_BASE_PATH + PATH_SEPARATOR + secretId);

        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SECRETM_00019");
    }

    @Test
    public void testDeleteSecretByNonExistingSecretId() {

        Response response = getResponseOfDelete(
                SECRET_API_BASE_PATH + PATH_SEPARATOR + "0273-2933-2132-3321");

        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }
}
