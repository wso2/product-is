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

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Server Configuration Management REST APIs success paths.
 */
public class ConfigSuccessTest extends ConfigTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConfigSuccessTest(TestUserMode userMode) throws Exception {

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
    public void testGetAuthenticator() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_AUTHENTICATOR_API_BASE_PATH + PATH_SEPARATOR + SAMPLE_AUTHENTICATOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(SAMPLE_AUTHENTICATOR_ID))
                .body("name", equalTo("BasicAuthenticator"))
                .body("displayName", equalTo("basic"))
                .body("isEnabled", equalTo(true))
                .body("properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetAuthenticator"})
    public void testGetAuthenticators() throws Exception {

        String baseIdentifier = "find{ it.id == '" + SAMPLE_AUTHENTICATOR_ID + "' }.";
        Response response = getResponseOfGet(CONFIGS_AUTHENTICATOR_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("BasicAuthenticator"))
                .body(baseIdentifier + "displayName", equalTo("basic"))
                .body(baseIdentifier + "isEnabled", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetAuthenticators"})
    public void testGetConfigs() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("idleSessionTimeoutPeriod", notNullValue())
                .body("rememberMePeriod", notNullValue())
                .body("homeRealmIdentifiers", notNullValue())
                .body("provisioning", notNullValue())
                .body("authenticators", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetConfigs"})
    public void testPatchConfigs() throws Exception {

        String body = readResource("patch-replace-configs.json");
        Response response = getResponseOfPatch(
                CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("idleSessionTimeoutPeriod", equalTo("20"));

        body = readResource("patch-add-configs.json");
        response = getResponseOfPatch(
                CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("homeRealmIdentifiers.contains(\"test-realm\")", equalTo(true));

        body = readResource("patch-remove-configs.json");
        response = getResponseOfPatch(
                CONFIGS_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("homeRealmIdentifiers.contains(\"test-realm\")", equalTo(false));
    }

    @Test(dependsOnMethods = {"testPatchConfigs"})
    public void testUpdateScimConfigs() throws Exception {

        String body = readResource("update-scim-configs.json");

        Response response = getResponseOfPut(CONFIGS_INBOUND_SCIM_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(CONFIGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("provisioning", notNullValue())
                .body("provisioning.inbound.scim.provisioningUserstore", equalTo("PRIMARY"))
                .body("provisioning.inbound.scim.enableProxyMode", equalTo(false));
    }
}
