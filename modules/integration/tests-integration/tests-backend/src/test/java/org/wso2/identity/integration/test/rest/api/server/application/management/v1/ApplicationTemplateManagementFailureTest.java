/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils
        .extractApplicationIdFromLocationHeader;

public class ApplicationTemplateManagementFailureTest extends ApplicationManagementBaseTest {

    private JSONObject applicationObject;
    private String invalidTemplateId = "some-wrong-id";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationTemplateManagementFailureTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @BeforeTest(alwaysRun = true)
    public void initTestClass() throws IOException, JSONException {

        applicationObject = createSampleApplicationObject();

        super.init();
    }

    @AfterTest(alwaysRun = true)
    public void testFinish() {

        super.testFinish();
    }

    @Test
    public void testGetApplicationTemplateByInvalidId() {

        Response responseOfGet = getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" +
                invalidTemplateId);

        validateHttpStatusCode(responseOfGet, HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteApplicationTemplateByInvalidId() {

        Response responseOfDelete = getResponseOfDelete(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" +
                invalidTemplateId);
        validateHttpStatusCode(responseOfDelete, HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testUpdateApplicationTemplateByInvalidId() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", "Sample Template");
        createRequest.put("description", "This is a sample Template");
        createRequest.put("application", applicationObject);
        String payload = createRequest.toString();
        Response responseOfDelete = getResponseOfPut(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" +
                invalidTemplateId, payload);
        validateHttpStatusCode(responseOfDelete, HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testCreateDuplicateTemplate() throws Exception {

        String body = readResource("create-application-template.json");
        String firstTemplateId = getTemplateId(createApplicationTemplate(body));
        Response responseOfRePost = createApplicationTemplate(body);
        validateErrorResponse(responseOfRePost, HttpStatus.SC_CONFLICT, "TMM_00014");
        removeCreatedApplicationTemplates(firstTemplateId);
    }

    @Test
    public void testUpdateApplicationTemplateWithDuplicateName() throws Exception {

        JSONObject firstTemplate = new JSONObject();
        firstTemplate.put("name", "First Template");
        firstTemplate.put("description", "Description of first template");
        firstTemplate.put("application", applicationObject);
        String payload1 = firstTemplate.toString();

        String firstTemplateId = getTemplateId(createApplicationTemplate(payload1));

        JSONObject secondTemplate = new JSONObject();
        secondTemplate.put("name", "Second Template");
        secondTemplate.put("description", "Description of second template");
        secondTemplate.put("application", applicationObject);
        String payload2 = secondTemplate.toString();

        String secondTemplateId = getTemplateId(createApplicationTemplate(payload2));

        JSONObject updateRequest = new JSONObject();
        updateRequest.put("name", "First Template");
        updateRequest.put("description", "Updated description of second template");
        updateRequest.put("application", applicationObject);
        String updatePayload = updateRequest.toString();

        Response responsePut = getResponseOfPut(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + secondTemplateId,
                updatePayload);
        validateErrorResponse(responsePut, HttpStatus.SC_CONFLICT, "TMM_00014");

        removeCreatedApplicationTemplates(firstTemplateId);
        removeCreatedApplicationTemplates(secondTemplateId);
    }

    @Test
    public void testFilterApplicationTemplatesWithInvalidSearchValue() throws Exception {

        JSONObject firstTemplate = new JSONObject();
        firstTemplate.put("name", "template1");
        firstTemplate.put("description", "Description of first template");
        firstTemplate.put("application", applicationObject);
        String payload1 = firstTemplate.toString();

        String firstTemplateId = getTemplateId(createApplicationTemplate(payload1));

        String url = APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH;
        Map<String, Object> filterParam = new HashMap<>();
        filterParam.put("filter", "name eq 'template2'");
        Response response = getResponseOfGetWithQueryParams(url, filterParam);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("templates.size()", notNullValue());
        removeCreatedApplicationTemplates(firstTemplateId);
    }

    @Test
    public void testFilterApplicationTemplatesWithInvalidSearchKey() throws Exception {

        String url = APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH;
        Map<String, Object> filterParam = new HashMap<>();
        filterParam.put("filter", "test eq 'template1'");
        Response response = getResponseOfGetWithQueryParams(url, filterParam);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-65502", "Invalid search filter");
    }

    @Test
    public void testNotImplementedLimit() {

        Response response = getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "?limit=30");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65004");
    }

    @Test
    public void testNotImplementedOffset() {

        Response response = getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "?offset=1");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65004");
    }

    private JSONObject createSampleApplicationObject() throws JSONException {

        JSONObject application = new JSONObject();
        application.put("name", "sample application");
        application.put("description", "This is the sample application.");
        application.put("imageUrl", "https://example.com/logo/my-logo.png");

        return application;
    }

    private void removeCreatedApplicationTemplates(String templateId) {

        getResponseOfDelete(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + templateId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private Response createApplicationTemplate(String requestBody) {

        return getResponseOfPost(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH, requestBody);
    }

    private String getTemplateId(Response responseOfPost) {

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        return extractApplicationIdFromLocationHeader(location);
    }
}
