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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Secret Management REST APIs success paths.
 */
public class SecretManagementSuccessTest extends SecretManagementTestBase {

    private String secretTypeName = "sample-secret-type";
    private String secretName = "sample-secret";

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
    public void testAddSecretType() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            String body = readResource("add-secret-type.json");
            Response response =
                    getResponseOfPost(SECRET_TYPE_API_BASE_PATH, body);

            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header(HttpHeaders.LOCATION, notNullValue());
            String location = response.getHeader(HttpHeaders.LOCATION);
            assertNotNull(location);
            secretTypeName = location.substring(location.lastIndexOf("/") + 1);
            assertNotNull(secretTypeName);
        }
    }

    @Test(dependsOnMethods = {"testAddSecretType"})
    public void testGetSecretType() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response = getResponseOfGet(SECRET_TYPE_API_BASE_PATH + PATH_SEPARATOR + secretTypeName);
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("name", equalTo(secretTypeName))
                    .body("description", equalTo("sample secret type"));
        }
    }

    @Test(dependsOnMethods = {"testGetSecretType"})
    public void testUpdateSecretType() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            String body = readResource("update-secret-type.json");
            Response response = getResponseOfPut(SECRET_TYPE_API_BASE_PATH + PATH_SEPARATOR + secretTypeName, body);

            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("name", equalTo(secretTypeName))
                    .body("description", equalTo("updated secret type"));
        }
    }

    @Test(dependsOnMethods = {"testUpdateSecretType"})
    public void testDeleteSecretType() throws IOException {

        if (!StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            Response response =
                    getResponseOfDelete(SECRET_TYPE_API_BASE_PATH + PATH_SEPARATOR + secretTypeName);
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }
    }

    @Test
    public void testAddSecret() throws IOException {

        String body = readResource("add-secret.json");
        Response response =
                getResponseOfPost(SECRET_API_BASE_PATH + PATH_SEPARATOR + SECRET_TYPE, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        secretName = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(secretName);
    }

    @Test(dependsOnMethods = {"testAddSecret"})
    public void testGetSecret() throws IOException {

        Response response = getResponseOfGet(SECRET_API_BASE_PATH + PATH_SEPARATOR + SECRET_TYPE + PATH_SEPARATOR + secretName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretName", equalTo(secretName))
                .body("description", equalTo("This is a sample secret"));
    }

    @Test(dependsOnMethods = {"testGetSecret"})
    public void testUpdateSecret() throws IOException {

        String body = readResource("update-secret.json");
        Response response = getResponseOfPut(SECRET_API_BASE_PATH + PATH_SEPARATOR + SECRET_TYPE + PATH_SEPARATOR + secretName, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretName", equalTo(secretName))
                .body("description", equalTo("This is a updated sample secret"));
    }

    @Test(dependsOnMethods = {"testUpdateSecret"})
    public void testDeleteSecret() throws IOException {

        Response response =
                getResponseOfDelete(SECRET_API_BASE_PATH + PATH_SEPARATOR + SECRET_TYPE +
                        PATH_SEPARATOR + secretName);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
