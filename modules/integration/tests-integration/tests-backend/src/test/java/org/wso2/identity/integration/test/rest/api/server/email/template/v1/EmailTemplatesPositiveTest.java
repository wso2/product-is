/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.email.template.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.email.template.v1.model.EmailTemplateTypeWithID;
import org.wso2.identity.integration.test.rest.api.server.email.template.v1.model.EmailTemplateTypeWithoutTemplates;
import org.wso2.identity.integration.test.rest.api.server.email.template.v1.model.EmailTemplateWithID;
import org.wso2.identity.integration.test.rest.api.server.email.template.v1.model.SimpleEmailTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for the Email Templates REST API success path.
 */
public class EmailTemplatesPositiveTest extends EmailTemplatesTestBase {

    private List<EmailTemplateTypeWithoutTemplates> getAllEmailTemplateTypesResponse;
    private EmailTemplateTypeWithID getEmailTemplateTypeResponse;
    private List<SimpleEmailTemplate> getTemplatesListOfEmailTemplateTypeResponse;
    private EmailTemplateWithID getSpecificEmailTemplateResponse;

    private String templateTypeId = "";
    private String templateId = "";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public EmailTemplatesPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        // Init getAllEmailTemplateTypes method response
        String expectedResponse = readResource("get-all-email-template-types-response.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        getAllEmailTemplateTypesResponse =
                Arrays.asList(jsonWriter.readValue(expectedResponse, EmailTemplateTypeWithoutTemplates[].class));

        // Init getEmailTemplateType method response
        expectedResponse = readResource("get-email-template-type-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        getEmailTemplateTypeResponse = jsonWriter.readValue(expectedResponse, EmailTemplateTypeWithID.class);

        // Init getTemplatesListOfEmailTemplateType method response
        expectedResponse = readResource("get-templates-list-of-email-template-type-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        getTemplatesListOfEmailTemplateTypeResponse =
                Arrays.asList(jsonWriter.readValue(expectedResponse, SimpleEmailTemplate[].class));

        // Init getEmailTemplate method response
        expectedResponse = readResource("add-email-template-request.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        getSpecificEmailTemplateResponse = jsonWriter.readValue(expectedResponse, EmailTemplateWithID.class);
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

    // Get all default email template types from the API and match.
    @Test
    public void testGetAllEmailTemplateTypes() throws Exception {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH);
        String baseIdentifier = "find{ it.id == 'QWNjb3VudENvbmZpcm1hdGlvbg' }.";
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "displayName", equalTo("AccountConfirmation"))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/email/template-types/QWNjb3VudENvbmZpcm1hdGlvbg"));
    }

    // Get all email template types with required attributes.
    @Test(dependsOnMethods = {"testGetAllEmailTemplateTypes"})
    public void testGetAllEmailTemplateTypesWithRequiredAttribute() throws Exception {

        Map<String, Object> requiredAttributeParam = new HashMap<>();
        requiredAttributeParam.put("requiredAttributes",
                "templates.id,templates.contentType,templates.subject,templates.body,templates.footer");
        Response response = getResponseOfGetWithQueryParams(EMAIL_TEMPLATES_API_BASE_PATH +
                EMAIL_TEMPLATE_TYPES_PATH, requiredAttributeParam);
        String baseIdentifier = "find{ it.id == 'QWNjb3VudENvbmZpcm1hdGlvbg' }.";

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "displayName", equalTo("AccountConfirmation"))
                .body(baseIdentifier + "templates", notNullValue())
                .body(baseIdentifier + "templates.find{ it.id == 'en_US' }." + "subject",
                        equalTo("WSO2 - Account Confirmation"));
    }

    // Get the list of templates of the default AccountEnable email template type
    @Test(dependsOnMethods = {"testGetAllEmailTemplateTypesWithRequiredAttribute"})
    public void testGetTemplatesListOfEmailTemplateType() throws IOException {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + SAMPLE_TEMPLATE_TYPE_ID + EMAIL_TEMPLATES_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<SimpleEmailTemplate> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), SimpleEmailTemplate[].class));
        Assert.assertEquals(responseFound, getTemplatesListOfEmailTemplateTypeResponse,
                "Response of the get templates list of email template type doesn't match.");
    }

    // Add new email template type, save the returned template type id for next tests.
    @Test(dependsOnMethods = {"testGetTemplatesListOfEmailTemplateType"})
    public void testAddEmailTemplateType() throws IOException {

        String body = readResource("add-email-template-type-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        Response response = getResponseOfPost(path, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        templateTypeId = location.substring(location.lastIndexOf("/") + 1);
    }

    // Get the added email template type in the previous test and match.
    @Test(dependsOnMethods = {"testAddEmailTemplateType"})
    public void testGetEmailTemplateType() throws IOException {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + templateTypeId);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        EmailTemplateTypeWithID responseFound =
                jsonWriter.readValue(response.asString(), EmailTemplateTypeWithID.class);
        Assert.assertEquals(responseFound, getEmailTemplateTypeResponse,
                "Response of the get email template type doesn't match.");
    }

    // Add new email template to the previous added template type and save the template id.
    @Test(dependsOnMethods = {"testGetEmailTemplateType"})
    public void testAddEmailTemplate() throws IOException {

        String body = readResource("add-email-template-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                templateTypeId;
        Response response = getResponseOfPost(path, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        templateId = location.substring(location.lastIndexOf("/") + 1);
    }

    // Get the above added email template from the API and match.
    @Test(dependsOnMethods = {"testAddEmailTemplate"})
    public void testGetEmailTemplate() throws IOException {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + templateTypeId + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        EmailTemplateWithID responseFound = jsonWriter.readValue(response.asString(), EmailTemplateWithID.class);
        Assert.assertEquals(responseFound, getSpecificEmailTemplateResponse,
                "Response of the get specific email template doesn't match.");
    }

    // Update previously added email template, execute a get request and match if content updated.
    @Test(dependsOnMethods = {"testGetEmailTemplate"})
    public void testUpdateEmailTemplate() throws IOException {

        String body = readResource("update-email-template-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                templateTypeId + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId;
        getResponseOfPut(path, body).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + templateTypeId + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        EmailTemplateWithID responseFound = jsonWriter.readValue(response.asString(), EmailTemplateWithID.class);
        EmailTemplateWithID expectedResponse = jsonWriter.readValue(body, EmailTemplateWithID.class);

        Assert.assertEquals(responseFound, expectedResponse,"Expected response for the get email template, " +
                "after updating doesn't match.");
    }

    // Delete the same email template, execute a get and expects a NOT_FOUND
    @Test(dependsOnMethods = {"testUpdateEmailTemplate"})
    public void testDeleteEmailTemplate() {

        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                templateTypeId + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId;
        getResponseOfDelete(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    // Update previously added email template type, execute a get request and match if content updated.
    @Test(dependsOnMethods = {"testUpdateEmailTemplate"})
    public void testUpdateEmailTemplateType() throws IOException {

        String body = readResource("update-email-template-type-request.json");
        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                templateTypeId;
        getResponseOfPut(path, body).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + templateTypeId);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        EmailTemplateTypeWithID responseFound =
                jsonWriter.readValue(response.asString(), EmailTemplateTypeWithID.class);
        EmailTemplateTypeWithID expectedResponse =
                jsonWriter.readValue(readResource("updated-email-template-type-response.json"),
                        EmailTemplateTypeWithID.class);

        Assert.assertEquals(responseFound, expectedResponse,"Expected response for the get email " +
                "template type, after updating doesn't match.");
    }

    // Delete the same email template, execute a get and expects a NOT_FOUND
    @Test(dependsOnMethods = {"testUpdateEmailTemplateType"})
    public void testDeleteEmailTemplateType() {

        String path = EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR +
                templateTypeId;
        getResponseOfDelete(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
