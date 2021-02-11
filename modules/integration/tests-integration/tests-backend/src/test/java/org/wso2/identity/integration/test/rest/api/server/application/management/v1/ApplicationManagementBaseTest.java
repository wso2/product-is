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
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.util.Set;

/**
 * Base test class for Application Management REST APIs.
 */
public class ApplicationManagementBaseTest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "applications.yaml";
    static final String API_VERSION = "v1";

    static final String APPLICATION_MANAGEMENT_API_BASE_PATH = "/applications";
    static final String METADATA_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH + "/meta";
    static final String RESIDENT_APP_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH + "/resident";
    static final String APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH +
            "/templates";
    static final String PATH_SEPARATOR = "/";

    protected static String swaggerDefinition;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.application.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    public ApplicationManagementBaseTest(TestUserMode userMode) throws Exception {

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
    public void testConclude() throws Exception {

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

    protected void cleanUpApplications(Set<String> appsToCleanUp) {

        appsToCleanUp.forEach(appId -> {
            String applicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
            Response responseOfDelete = getResponseOfDelete(applicationPath);
            responseOfDelete.then()
                    .log()
                    .ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);

            // Make sure we don't have deleted application details.
            getResponseOfGet(applicationPath).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        });
    }
}
