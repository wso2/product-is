/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.idp.v1;

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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Identity Provider Management REST APIs failure paths.
 */
public class IdPFailureTest extends IdPTestBase {

    private String idPId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdPFailureTest(TestUserMode userMode) throws Exception {

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
    public void testGetIdPWithInvalidId() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60002", "random-id");
    }

    @Test(dependsOnMethods = {"testGetIdPWithInvalidId"})
    public void addIdPConflict() throws IOException {

        Response response = getResponseOfPost(IDP_API_BASE_PATH, readResource("add-idp2.json"));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        idPId = location.substring(location.lastIndexOf("/") + 1);

        response = getResponseOfPost(IDP_API_BASE_PATH, readResource("add-idp-conflict.json"));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "IDP-60001", "Google-2");
    }


    @Test(dependsOnMethods = {"addIdPConflict"})
    public void testGetIdPFederatedAuthenticatorWithInvalidAuthId() {

        Response response = getResponseOfGet(
                IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH +
                        PATH_SEPARATOR + "random-fed-auth-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60022", "random-fed-auth-id");
    }

    @Test(dependsOnMethods = {"testGetIdPFederatedAuthenticatorWithInvalidAuthId"})
    public void testGetIdPOutboundConnectorWithInvalidConnectorId() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_OUTBOUND_CONNECTORS_PATH + PATH_SEPARATOR +
                "random-connector-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60023", "random-connector-id");
    }

    @Test
    public void testGetIdPTemplateWithInvalidId() throws Exception {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH +
                PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "TMM_00021", "random-id");
    }

    @Test(dependsOnMethods = {"testGetIdPTemplateWithInvalidId"})
    public void testAddIdPTemplateConflict() throws IOException {

        String body = readResource("add-idp-template2.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        response = getResponseOfPost(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH,
                readResource("add-idp-template-conflict.json"));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "TMM_00014", "Google-2");
    }

    @Test(dependsOnMethods = {"testAddIdPTemplateConflict"})
    public void testFilterIdPTemplatesWithInvalidSearchKey() throws Exception {

        String url = IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH;
        Map<String, Object> filterParam = new HashMap<>();
        filterParam.put("filter", "test eq 'DEFAULT'");
        Response response = getResponseOfGetWithQueryParams(url, filterParam);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "IDP-65055", "Invalid search filter");
    }
}
