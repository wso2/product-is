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
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils
        .extractApplicationIdFromLocationHeader;

/**
 * Test class for Application Management REST APIs success paths.
 */
public class ApplicationTemplateManagementSuccessTest extends ApplicationManagementBaseTest {

    private static final String CREATED_TEMPLATE_NAME = "Sample Application Template";
    private static final String UPDATED_TEMPLATE_NAME = "Updated Sample Application Template";
    private static final String TEMPLATE_IMAGE_URL = "https://example.com/logo/my-logo.png";
    private static final String UPDATED_TEMPLATE_IMAGE_URL = "https://example.com/logo/update-logo.png";
    private String createdTemplateId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationTemplateManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreateTemplate() throws Exception {

        String body = readResource("create-application-template.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdTemplateId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdTemplateId);
    }

    @Test(dependsOnMethods = {"testCreateTemplate"})
    public void testGetAllApplicationTemplates() throws Exception {

        String baseIdentifier = "templates.find{ it.id == '" + createdTemplateId + "' }.";
        Response response = getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("templates.size()", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetAllApplicationTemplates"})
    public void testGetApplicationTemplateById() throws Exception {

        getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + createdTemplateId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(CREATED_TEMPLATE_NAME))
                .body("image", equalTo(TEMPLATE_IMAGE_URL))
                .body("application", notNullValue())
                .body("application.claimConfiguration.requestedClaims", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetApplicationTemplateById"})
    public void testUpdateApplicationTemplateById() throws Exception {

        String body = readResource("update-application-template.json");
        getResponseOfPut(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + createdTemplateId, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + createdTemplateId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(UPDATED_TEMPLATE_NAME))
                .body("image", equalTo(UPDATED_TEMPLATE_IMAGE_URL))
                .body("application", notNullValue())
                .body("application.claimConfiguration.requestedClaims", nullValue());
    }

    @Test(dependsOnMethods = {"testUpdateApplicationTemplateById"})
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + createdTemplateId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application template is not available.
        getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH + "/" + createdTemplateId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testDeleteApplicationById"})
    public void testGetEmptyList() throws Exception {

        getResponseOfGet(APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("templates.size()", is(0));
    }
}
