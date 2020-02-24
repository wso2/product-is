/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.user.store.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

public class UserStoreFailureTest extends UserStoreTestBase {

    private static final String INCORRECT_DOMAIN_ID = "SkRCQy0x";
    private static final String INCORRECT_TYPE_ID = "WSDFGHBCVNMLKI";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserStoreFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

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
    public void testAddUserStoreNegativeCase() throws Exception {

        String body = readResource("add-secondary-user-store-negative.json");
        Response response = getResponseOfPost(USER_STORE_PATH_COMPONENT, body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "SUS-65001");
    }

    @Test
    public void testGetUserStoreByDomainIdNegativeCase() {

        Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR +
                INCORRECT_DOMAIN_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SUS-60003");
    }

    @Test
    public void testUpdateUserStoreByIdNegativeCase() throws IOException {

        String body = readResource("user-store-add-secondary-user-store.json");
        Response response = getResponseOfPut(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR +
                INCORRECT_DOMAIN_ID, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SUS-60001");
    }

    @Test
    public void testGetUserStoreTypeByIdNegativeCase() {

        Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT + USER_STORE_META_COMPONENT
                + PATH_SEPARATOR + INCORRECT_TYPE_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "SUS-60003");
    }

    @Test
    public void testDeleteUserStoreByIdNegativeCase() {

        Response response = getResponseOfDelete(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR
                + INCORRECT_DOMAIN_ID);
        validateHttpStatusCode(response, HttpStatus.SC_NO_CONTENT);
    }
}
