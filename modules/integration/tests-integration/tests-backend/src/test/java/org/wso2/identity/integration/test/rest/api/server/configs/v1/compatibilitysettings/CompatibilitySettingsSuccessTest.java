/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.configs.v1.compatibilitysettings;

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
import org.wso2.identity.integration.test.rest.api.server.configs.v1.ConfigTestBase;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Integration tests for Compatibility Settings REST API success paths.
 */
public class CompatibilitySettingsSuccessTest extends ConfigTestBase {

    private static final String FLOW_EXECUTION_GROUP = "flowExecution";
    private static final String ENABLE_LEGACY_FLOWS = "enableLegacyFlows";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CompatibilitySettingsSuccessTest(TestUserMode userMode) throws Exception {

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

    @Test(description = "GET all compatibility settings returns 200 and valid structure")
    public void testGetCompatibilitySettings() throws Exception {

        Response response = getResponseOfGet(CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        response.then().body("$", notNullValue());
    }

    @Test(description = "GET compatibility settings by group flowExecution returns 200 and enableLegacyFlows")
    public void testGetCompatibilitySettingsByGroupFlowExecution() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_LEGACY_FLOWS, equalTo("false"));
    }

    @Test(description = "PATCH compatibility settings then GET reflects updated value",
            dependsOnMethods = {"testGetCompatibilitySettingsByGroupFlowExecution"})
    public void testPatchCompatibilitySettingsFlowExecution() throws Exception {

        String body = readResource("patch-compatibility-settings-flow-execution.json");
        Response patchResponse = getResponseOfPatch(CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH, body);
        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Response getResponse = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP);
        getResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_LEGACY_FLOWS, equalTo("true"));
    }

    @Test(description = "PATCH then GET by group returns consistent flowExecution settings",
            dependsOnMethods = {"testPatchCompatibilitySettingsFlowExecution"})
    public void testPatchThenGetByGroup() throws Exception {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + FLOW_EXECUTION_GROUP);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ENABLE_LEGACY_FLOWS, notNullValue());
    }
}
