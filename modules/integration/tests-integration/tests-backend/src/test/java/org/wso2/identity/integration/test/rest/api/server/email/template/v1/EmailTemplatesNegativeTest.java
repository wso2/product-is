/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.email.template.v1;

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

/**
 * Test class for Email Templates REST API negative paths.
 */
public class EmailTemplatesNegativeTest extends EmailTemplatesTestBase {

    private static final String INCORRECT_TEMPLATE_TYPE_ID = "QWNjb3VudEVuYWJsZQqwSa";
    private static final String UNDECODABLE_TEMPLATE_TYPE_ID = "QWNjb3VudEVuYWJsZQ111";
    private static final String INCORRECT_TEMPLATE_ID = "en_FR";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public EmailTemplatesNegativeTest(TestUserMode userMode) throws Exception {

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
    public void testGetEmailTemplateTypeWithUndecodableTemplateTypeId() {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + UNDECODABLE_TEMPLATE_TYPE_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50002");
    }

    @Test
    public void testGetEmailTemplateTypeWithIncorrectTemplateTypeId() {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + INCORRECT_TEMPLATE_TYPE_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50002");
    }

    @Test
    public void testGetTemplatesListOfEmailTemplateTypeWithIncorrectTemplateTypeId() {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + INCORRECT_TEMPLATE_TYPE_ID + EMAIL_TEMPLATES_PATH);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50002");
    }

    @Test
    public void testGetEmailTemplateWithIncorrectTemplateId() {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + SAMPLE_TEMPLATE_TYPE_ID + EMAIL_TEMPLATES_PATH +
                PATH_SEPARATOR + INCORRECT_TEMPLATE_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50003");
    }

    @Test
    public void testAddConflictingEmailTemplateType() throws IOException {

        String body = readResource("add-email-template-type-conflict-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        Response response = getResponseOfPost(path, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "ETM-50005");
    }

    @Test
    public void testAddConflictingEmailTemplate() throws IOException {

        String body = readResource("add-email-template-conflict-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + SAMPLE_TEMPLATE_TYPE_ID;
        Response response = getResponseOfPost(path, body);
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "ETM-50004");
    }

    @Test
    public void testUpdateIncorrectEmailTemplateType() throws IOException {

        String body = readResource("update-email-template-type-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                INCORRECT_TEMPLATE_TYPE_ID;

        Response response = getResponseOfPut(path, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50002");
    }

    @Test
    public void testUpdateIncorrectEmailTemplate() throws IOException {

        String body = readResource("update-email-template-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                SAMPLE_TEMPLATE_TYPE_ID + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + INCORRECT_TEMPLATE_ID;

        Response response = getResponseOfPut(path, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "ETM-50003");
    }
}
