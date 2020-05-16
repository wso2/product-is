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

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for Application PATCH operation.
 */
public class ApplicationPatchTest extends ApplicationManagementBaseTest {

    private static final String APP_NAME = "testPatchApplication";
    public static final String UPDATED_APP_NAME = "testUpdateNameApplication";
    private String appId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationPatchTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreateApplication() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", APP_NAME);
        String payload = createRequest.toString();

        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        appId = getApplicationId(responseOfPost);
        assertNotBlank(appId);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(APP_NAME));
    }

    @Test(dependsOnMethods = "testCreateApplication")
    public void testRenameApplication() throws Exception {

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("name", UPDATED_APP_NAME);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("name", equalTo(UPDATED_APP_NAME));
    }

    @Test(dependsOnMethods = "testRenameApplication")
    public void testUpdateBasicInformation() throws Exception {

        String description = "This is my application.";
        String imageUrl = "https://localhost/image.png";
        String accessUrl = "https://app.test.com/login";

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("description", description);
        patchRequest.put("imageUrl", imageUrl);
        patchRequest.put("accessUrl", accessUrl);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("description", equalTo(description))
                .body("imageUrl", equalTo(imageUrl))
                .body("accessUrl", equalTo(accessUrl));
    }

    @Test(dependsOnMethods = "testUpdateBasicInformation")
    public void testUpdateAdvancedConfiguration() throws Exception {

        // Let's assert the default values of the advanced configs first.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'saas' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'discoverableByEndUsers' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'skipLoginConsent' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'skipLogoutConsent' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'returnAuthenticatedIdpList' }.value", equalTo(false))
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(false));

        // Do the PATCH update request.
        String patchRequest = readResource("patch-application-advanced-configuration.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Assert that the PATCH update of advanced configs was successful.
        getApplication(appId).then()
                .body("advancedConfigurations.find{ it.key == 'saas' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'discoverableByEndUsers' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'skipLoginConsent' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'skipLogoutConsent' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'returnAuthenticatedIdpList' }.value", equalTo(true))
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(true));
    }

    @Test(dependsOnMethods = "testUpdateAdvancedConfiguration")
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application is not available.
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private String getApplicationId(Response createFirstAppResponse) {

        String location = createFirstAppResponse.getHeader(HttpHeaders.LOCATION);
        return extractApplicationIdFromLocationHeader(location);
    }

    private Response getApplication(String appId) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        return responseOfGet;
    }
}
