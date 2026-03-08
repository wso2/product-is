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

/**
 * Integration tests for Compatibility Settings REST API failure paths.
 */
public class CompatibilitySettingsFailureTest extends ConfigTestBase {

    private static final String ERROR_CODE_INVALID_INPUT = "CNF-60003";
    private static final String ERROR_CODE_UNSUPPORTED_SETTING_GROUP = "ICS-60002";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CompatibilitySettingsFailureTest(TestUserMode userMode) throws Exception {

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

    @Test(description = "GET by group with invalid group name returns 400 CNF-60003")
    public void testGetCompatibilitySettingsByGroupInvalidGroupName() {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + "invalid@@group");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, ERROR_CODE_INVALID_INPUT);
    }

    @Test(description = "GET by group with non-existent group returns 400 ICS-60002")
    public void testGetCompatibilitySettingsByGroupNonexistentGroup() {

        Response response = getResponseOfGet(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + "nonexistentgroup123");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, ERROR_CODE_UNSUPPORTED_SETTING_GROUP);
    }

    @Test(description = "GET all compatibility settings without auth returns 401")
    public void testGetCompatibilitySettingsWithoutAuth() {

        Response response = getResponseOfGetWithoutAuthentication(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH, "application/json");
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(description = "GET by group without auth returns 401")
    public void testGetCompatibilitySettingsByGroupWithoutAuth() {

        Response response = getResponseOfGetWithoutAuthentication(
                CONFIGS_COMPATIBILITY_SETTINGS_API_BASE_PATH + PATH_SEPARATOR + "flowExecution",
                "application/json");
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }
}
