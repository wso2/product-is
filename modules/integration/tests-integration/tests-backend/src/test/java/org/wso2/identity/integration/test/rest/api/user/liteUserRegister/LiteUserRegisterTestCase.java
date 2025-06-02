/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.user.liteUserRegister;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests for lite user register REST endpoint
 */
public class LiteUserRegisterTestCase extends LiteUserRegisterTestBase {

    protected static final String LITE_USER_REGISTRATION_ENDPOINT = "/lite";
    protected static final String ENABLE_LITE_SIGN_UP = "LiteRegistration.Enable";
    protected static final String API_LITE_USER_REGISTER_BASE_PATH = "/api/identity/user/%s";
    protected static final String API_LITE_USER_REGISTER_BASE_PATH_IN_SWAGGER =
            "/t/\\{tenant-domain\\}" + API_LITE_USER_REGISTER_BASE_PATH;
    protected static final String API_VERSION_LITE_USER = "v1.0";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION_LITE_USER, swaggerDefinitionLiteUserRegister, tenant,
                API_LITE_USER_REGISTER_BASE_PATH_IN_SWAGGER, API_LITE_USER_REGISTER_BASE_PATH);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() throws Exception {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void endTest() throws Exception {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @AfterClass(alwaysRun = true)
    public void endTestClass() throws Exception {

        updateResidentIDPProperty(ENABLE_LITE_SIGN_UP, "false", true);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Lite user flow with new and existing usernames"
    )
    public void testLiteUserRegistration() throws Exception {

        updateResidentIDPProperty(ENABLE_LITE_SIGN_UP, "true", true);
        String data = "{\"email\": \"testlitteuser@wso2.com\",\"realm\": \"PRIMARY\"," +
                "\"preferredChannel\":\"Email\",\"claims\":[], \"properties\": []}";
        Response responseOfPost = getResponseOfPost(LITE_USER_REGISTRATION_ENDPOINT, data);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CREATED, "Lite user flow unsuccessful");

        Response responseOfPostConflict = getResponseOfPost(LITE_USER_REGISTRATION_ENDPOINT, data);
        Assert.assertEquals(responseOfPostConflict.statusCode(), HttpStatus.SC_CONFLICT, "Username already exist");
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Lite user flow endpoint test before enabling the lite user register functionality"
    )
    public void testLiteUserRegistrationBeforeEnabling() throws Exception {

        updateResidentIDPProperty(ENABLE_LITE_SIGN_UP, "false", true);
        String data = "{\"email\": \"lanka@wso2.com\",\"realm\": \"PRIMARY\",\"preferredChannel\":\"Email\"," +
                "\"claims\":[], \"properties\": []}";
        Response responseOfPost = getResponseOfPost(LITE_USER_REGISTRATION_ENDPOINT, data);
        Assert.assertEquals(responseOfPost.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Error while testing request without authorization");
    }
}
