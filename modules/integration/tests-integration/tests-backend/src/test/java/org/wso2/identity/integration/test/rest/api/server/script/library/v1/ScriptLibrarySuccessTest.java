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

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for script library management REST APIs success paths.
 */
public class ScriptLibrarySuccessTest extends ScriptLibraryTestBase {

    public static final String SCRIPT_LIBRARY_NAME = "sample.js";
    public static final String SCRIPT_LIBRARY_DESCRIPTION = "sample description";
    public static final String SCRIPT_LIBRARY_UPDATED_DESCRIPTION = "sample description updated";
    public static final String SCRIPT_LIBRARY_CONTENT_PATH = "/content";
    public static final String SCRIPT_LIBRARY_CONTENT_TYPE_OF_GET_CONTENT = "application/octet-stream";
    public static String SCRIPT_LIBRARY_CONTENT_REF = null;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ScriptLibrarySuccessTest(TestUserMode userMode) throws Exception {

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
    public void testAddScriptLibrary() throws IOException {

        String content = readResource("sample-script-library.js");
        Response responseOfUpload =
                getResponseOfMultipartPost(SCRIPT_LIBRARY_API_BASE_PATH, content, SCRIPT_LIBRARY_NAME,
                        SCRIPT_LIBRARY_DESCRIPTION);
        responseOfUpload.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = responseOfUpload.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
    }

    @Test(dependsOnMethods = {"testAddScriptLibrary"})
    public void testGetScriptLibrary() throws XPathExpressionException {

        SCRIPT_LIBRARY_CONTENT_REF = "/t/" + context.getContextTenant()
                .getDomain() + "/api/server/v1/script-libraries/" + SCRIPT_LIBRARY_NAME + SCRIPT_LIBRARY_CONTENT_PATH;
        Response response = getResponseOfGet(SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + SCRIPT_LIBRARY_NAME);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(SCRIPT_LIBRARY_NAME))
                .body("description", equalTo(SCRIPT_LIBRARY_DESCRIPTION))
                .body("content-ref", equalTo(SCRIPT_LIBRARY_CONTENT_REF));
    }

    @Test(dependsOnMethods = {"testAddScriptLibrary"})
    public void testGetScriptLibraries() {

        Response response = getResponseOfGet(SCRIPT_LIBRARY_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testGetScriptLibrary"})
    public void testUpdateScriptLibrary() throws IOException, XPathExpressionException {

        SCRIPT_LIBRARY_CONTENT_REF = "/t/" + context.getContextTenant()
                .getDomain() + "/api/server/v1/script-libraries/" + SCRIPT_LIBRARY_NAME + SCRIPT_LIBRARY_CONTENT_PATH;
        String content = readResource("sample-script-library-updated.js");
        String endpoint = SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + SCRIPT_LIBRARY_NAME;
        Response response = getResponseOfMultipartPut(endpoint, content, SCRIPT_LIBRARY_UPDATED_DESCRIPTION);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateScriptLibrary"})
    public void testGetContentScriptLibrary() throws IOException {

        String content = readResource("sample-script-library-updated.js");
        String endpoint =
                SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + SCRIPT_LIBRARY_NAME + SCRIPT_LIBRARY_CONTENT_PATH;
        Response response = getResponseOfGet(endpoint, SCRIPT_LIBRARY_CONTENT_TYPE_OF_GET_CONTENT);
        String responseContent = response.asString();
        assertEquals(content, responseContent);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testGetContentScriptLibrary"})
    public void testDeleteScriptLibrary() {

        Response response = getResponseOfDelete(SCRIPT_LIBRARY_API_BASE_PATH + PATH_SEPARATOR + SCRIPT_LIBRARY_NAME);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

}
