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

package org.wso2.identity.integration.test.rest.api.user.selfRegister;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SelfRegisterTestCase extends SelfRegisterTestBase {

    public static final String ENABLE_SELF_SIGN_UP = "SelfRegistration.Enable";
    public static final String SELF_REGISTRATION_ENDPOINT = "/me";
    protected static final String API_SELF_REGISTER_BASE_PATH = "/api/identity/user/%s";
    protected static final String API_SELF_REGISTER_BASE_PATH_IN_SWAGGER =
            "/t/\\{tenant-domain\\}" + API_SELF_REGISTER_BASE_PATH;
    protected static final String API_SELF_REGISTER_BASE_PATH_WITH_TENANT_CONTEXT =
            TENANT_CONTEXT_IN_URL + API_SELF_REGISTER_BASE_PATH;
    static final String API_VERSION_SELF_REGISTER = "v1.0";
    private String selfRegisterUserInfo;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION_SELF_REGISTER, swaggerDefinitionSelfRegister, tenant,
                API_SELF_REGISTER_BASE_PATH_IN_SWAGGER, API_SELF_REGISTER_BASE_PATH_WITH_TENANT_CONTEXT);
        selfRegisterUserInfo = readResource("self-register-request-body.json");
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() throws Exception {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void endTest() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "false", true);
        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Attempt self registration user before enabling the functionality"
    )
    public void testSelfRegisterBeforeEnable() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "false", true);
        Response responseOfPost = getResponseOfPost(SELF_REGISTRATION_ENDPOINT, selfRegisterUserInfo);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_BAD_REQUEST, "Self register user not enabled");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Create self registered user")
    public void testSelfRegister() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "true", true);
        Response responseOfPost = getResponseOfPost(SELF_REGISTRATION_ENDPOINT, selfRegisterUserInfo);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CREATED, "Self register user successful");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Create self registered user with existing username")
    public void testSelfRegisterWithExistingUsername() throws Exception {

        updateResidentIDPProperty(ENABLE_SELF_SIGN_UP, "true", true);
        Response responseOfPost = getResponseOfPost(SELF_REGISTRATION_ENDPOINT, selfRegisterUserInfo);
        Assert.assertEquals(responseOfPost.statusCode(), HttpStatus.SC_CONFLICT, "Self register user already exists");
    }
}
