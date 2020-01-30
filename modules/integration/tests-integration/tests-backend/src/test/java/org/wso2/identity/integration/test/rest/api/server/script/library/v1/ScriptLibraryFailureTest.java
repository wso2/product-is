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

package org.wso2.identity.integration.test.rest.api.server.script.library.v1;

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

/**
 * Test class for script library management REST APIs failure paths.
 */
public class ScriptLibraryFailureTest extends ScriptLibraryTestBase {

    public static final String INVALID_SCRIPT_LIBRARY_NAME = "invalid-script-name";
    public static final String VALID_SCRIPT_LIBRARY_NAME = "valid-script-name.js";
    public static final String NOT_EXISTING_SCRIPT_LIBRARY_NAME = "invalid-script-name.js";
    public static final String SCRIPT_LIBRARY_DESCRIPTION = "sample description";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ScriptLibraryFailureTest(TestUserMode userMode) throws Exception {

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
    public void testGetScriptLibraryWithInvalidName() {

        Response response =
                getResponseOfGet(SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + NOT_EXISTING_SCRIPT_LIBRARY_NAME);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SCL-60006", NOT_EXISTING_SCRIPT_LIBRARY_NAME);
    }

    @Test
    public void testAddScriptLibraryWithInvalidName() throws IOException {

        String content = readResource("sample-script-library.js");
        Response response =
                getResponseOfMultipartPost(SCRIPT_LIBRARY_API_BASE_PATH, content, INVALID_SCRIPT_LIBRARY_NAME,
                        SCRIPT_LIBRARY_DESCRIPTION);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "SCL-60008", INVALID_SCRIPT_LIBRARY_NAME);
    }

    @Test
    public void testAddScriptLibraryWithInvalidScript() throws IOException {

        String content = readResource("sample-script-library-bad-content.js");
        Response response =
                getResponseOfMultipartPost(SCRIPT_LIBRARY_API_BASE_PATH, content, VALID_SCRIPT_LIBRARY_NAME,
                        SCRIPT_LIBRARY_DESCRIPTION);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "SCL-60002", VALID_SCRIPT_LIBRARY_NAME);
    }

    @Test(dependsOnMethods = {"testAddScriptLibraryWithInvalidScript"})
    public void testAddScriptLibraryConflict() throws IOException {

        String content = readResource("sample-script-library.js");
        Response responseOfUpload =
                getResponseOfMultipartPost(SCRIPT_LIBRARY_API_BASE_PATH, content, VALID_SCRIPT_LIBRARY_NAME,
                        SCRIPT_LIBRARY_DESCRIPTION);
        responseOfUpload.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        Response response =
                getResponseOfMultipartPost(SCRIPT_LIBRARY_API_BASE_PATH, content, VALID_SCRIPT_LIBRARY_NAME,
                        SCRIPT_LIBRARY_DESCRIPTION);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "SCL-60007", VALID_SCRIPT_LIBRARY_NAME);
    }

    @Test(dependsOnMethods = {"testAddScriptLibraryConflict"})
    public void testUpdateScriptLibraryInvalidScript() throws IOException {

        String content = readResource("sample-script-library-bad-content.js");
        String endpoint = SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + VALID_SCRIPT_LIBRARY_NAME;
        Response response =
                getResponseOfMultipartPut(endpoint, content, SCRIPT_LIBRARY_DESCRIPTION);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "SCL-60002", VALID_SCRIPT_LIBRARY_NAME);
    }

}
