/*
 * Copyright (c) 2019-2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for Application PATCH operation.
 */
public class ApplicationPatchTest extends ApplicationManagementBaseTest {

    private static final String APP_NAME = "testPatchApplication";
    public static final String UPDATED_APP_NAME = "testUpdateNameApplication";
    private static final String APP_TEMPLATE_ID = "Test_template_1";
    private static final String APP_TEMPLATE_VERSION = "v1.0.0";
    public static final String SUBJECT_CLAIM_URI = "http://wso2.org/claims/username";
    private static final int GROUPS_COUNT = 2;
    private static final String GROUP_NAME_PREFIX = "Group_2_";
    private String appId;
    private String[] groupIDs;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationPatchTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.init();
        groupIDs = super.createGroups(GROUPS_COUNT, GROUP_NAME_PREFIX);
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        super.testConclude();
        super.deleteGroups(groupIDs);
    }

    @Test
    public void testCreateApplication() throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("name", APP_NAME);
        createRequest.put("templateId", APP_TEMPLATE_ID);
        createRequest.put("templateVersion", APP_TEMPLATE_VERSION);
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
                .body("name", equalTo(APP_NAME))
                .body("templateId", equalTo(APP_TEMPLATE_ID))
                .body("templateVersion", equalTo(APP_TEMPLATE_VERSION));
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
        String templateId = "Test_template_2";
        String templateVersion = "v1.0.1";

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("description", description);
        patchRequest.put("imageUrl", imageUrl);
        patchRequest.put("accessUrl", accessUrl);
        patchRequest.put("templateId", templateId);
        patchRequest.put("templateVersion", templateVersion);

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
        getResponseOfPatch(path, patchRequest.toString()).then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("description", equalTo(description))
                .body("imageUrl", equalTo(imageUrl))
                .body("accessUrl", equalTo(accessUrl))
                .body("templateId", equalTo(templateId))
                .body("templateVersion", equalTo(templateVersion));
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
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(false))
                .body("advancedConfigurations.trustedAppConfiguration", nullValue());

        // Do the PATCH update request.
        String patchRequest = readResource("patch-application-advanced-configuration.json");
        patchRequest =
                super.addDiscoverableGroupsToApplicationPayload(new JSONObject(patchRequest), "PRIMARY", groupIDs)
                        .toString();
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
                .body("advancedConfigurations.find{ it.key == 'enableAuthorization' }.value", equalTo(true))
                .body("advancedConfigurations.trustedAppConfiguration.find{ it.key == 'isFIDOTrustedApp' }.value",
                        equalTo(true))
                .body("advancedConfigurations.trustedAppConfiguration.find{ it.key == 'isConsentGranted' }.value",
                        equalTo(true))
                .body("advancedConfigurations.trustedAppConfiguration.find{ it.key == 'androidPackageName' }.value",
                        equalTo("sample.package.name"))
                .body("advancedConfigurations.trustedAppConfiguration.find{ it.key == 'androidThumbprints' }.value",
                        Matchers.hasItem("sampleThumbprint"))
                .body("advancedConfigurations.trustedAppConfiguration.find{ it.key == 'appleAppId' }.value",
                        equalTo("sample.app.id"))
                .body("advancedConfigurations.discoverableGroups", hasSize(1))
                .body("advancedConfigurations.discoverableGroups[0].userStore", equalTo("PRIMARY"))
                .body("advancedConfigurations.discoverableGroups[0].groups[0].id", Matchers.oneOf(groupIDs))
                .body("advancedConfigurations.discoverableGroups[0].groups[1].id", Matchers.oneOf(groupIDs));
    }

    @Test(description = "Test updating the claim configuration of an application",
            dependsOnMethods = "testUpdateAdvancedConfiguration")
    public void testUpdateClaimConfiguration() throws Exception {

        String requestBody = readResource("update-claim-configuration.json");
        getResponseOfPatch(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId, requestBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getApplication(appId).then()
                .body("claimConfiguration.subject.claim.uri", equalTo(SUBJECT_CLAIM_URI))
                .body("claimConfiguration.subject.useMappedLocalSubject", equalTo(true))
                .body("claimConfiguration.subject.mappedLocalSubjectMandatory", equalTo(true))
                .body("claimConfiguration.subject.includeUserDomain", equalTo(false))
                .body("claimConfiguration.subject.includeTenantDomain", equalTo(false));
    }

    @Test(description = "Test updating claim configuration by disabling useMappedLocalSubject and enabling " +
            "mappedLocalSubjectMandatory",
            dependsOnMethods = "testUpdateClaimConfiguration")
    public void testUpdateInvalidClaimConfiguration() throws Exception {

        String requestBody = readResource("invalid-claim-configuration.json");
        getResponseOfPatch(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId, requestBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(dependsOnMethods = "testUpdateInvalidClaimConfiguration", alwaysRun = true)
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
