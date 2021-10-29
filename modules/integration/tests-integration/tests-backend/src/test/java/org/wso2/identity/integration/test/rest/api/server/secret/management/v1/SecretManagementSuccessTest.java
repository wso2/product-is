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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.secret.management.v1.model.SecretListResponse;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Secret Management REST APIs success paths.
 */
public class SecretManagementSuccessTest extends SecretManagementTestBase {

    private final String secretName = "sample-secret";
    private String secretId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SecretManagementSuccessTest(TestUserMode userMode) throws Exception {

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
    public void testAddSecret() throws IOException {

        String body = readResource("add-secret.json");
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
    }

    @Test(dependsOnMethods = {"testAddSecret"})
    public void testGetAllSecrets() throws IOException {

        Response response = getResponseOfGet(SECRET_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        SecretListResponse listResponse = jsonWriter.readValue(response.asString(), SecretListResponse.class);

        assertNotNull(listResponse);
        Assert.assertTrue(listResponse.getSecrets()
                        .stream().
                        anyMatch(secret -> secret.getSecretId().equals(secretId)),
                "Created secret by the test suite is listed by the API");
    }

    @Test(dependsOnMethods = {"testGetAllSecrets"})
    public void testGetSecret() throws IOException {

        Response response = getResponseOfGet(SECRET_API_BASE_PATH + PATH_SEPARATOR + secretId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(secretId))
                .body("secretName", equalTo(secretName))
                .body("description", equalTo("This is a sample secret"));
    }

    @Test(dependsOnMethods = {"testGetSecret"})
    public void testUpdateSecret() throws IOException {

        String body = readResource("update-secret.json");
        Response response = getResponseOfPut(SECRET_API_BASE_PATH + PATH_SEPARATOR + secretId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(secretId))
                .body("secretName", equalTo(secretName))
                .body("description", equalTo("This is a updated sample secret"));
    }

    @Test(dependsOnMethods = {"testUpdateSecret"})
    public void testPatchSecretValue() throws IOException {

        String body = readResource("patch-secret-value.json");
        Response response = getResponseOfPatch(SECRET_API_BASE_PATH + PATH_SEPARATOR + secretId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(secretId))
                .body("secretName", equalTo(secretName));
    }

    @Test(dependsOnMethods = {"testPatchSecretValue"})
    public void testPatchSecretDescription() throws IOException {

        String body = readResource("patch-secret-description.json");
        Response response = getResponseOfPatch(SECRET_API_BASE_PATH + PATH_SEPARATOR + secretId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(secretId))
                .body("secretName", equalTo(secretName))
                .body("description", equalTo("This is a updated description through patch"));
    }

    @Test(dependsOnMethods = {"testPatchSecretDescription"})
    public void testDeleteSecret() throws IOException {

        Response response =
                getResponseOfDelete(SECRET_API_BASE_PATH + PATH_SEPARATOR + secretId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
