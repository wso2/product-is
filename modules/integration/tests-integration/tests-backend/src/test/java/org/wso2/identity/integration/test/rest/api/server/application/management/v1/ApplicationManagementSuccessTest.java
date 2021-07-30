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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.URLUtils;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of the Application Management REST API.
 */
public class ApplicationManagementSuccessTest extends ApplicationManagementBaseTest {

    private static final String MY_ACCOUNT = "My Account";
    private static final String CREATED_APP_NAME = "My SAMPLE APP";
    private String createdAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testGetAllApplications() throws IOException {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(response.asString(), ApplicationListResponse.class);

        assertNotNull(listResponse);
        Assert.assertFalse(listResponse.getApplications()
                        .stream()
                        .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(ApplicationConstants.LOCAL_SP)),
                "Default resident service provider '" + ApplicationConstants.LOCAL_SP + "' is listed by the API");

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            // Check whether the default "My Account" app exists.
            Assert.assertTrue(listResponse.getApplications()
                            .stream()
                            .anyMatch(appBasicInfo -> appBasicInfo.getName().equals(MY_ACCOUNT)),
                    "Default application 'My Account' is not listed by the API.");
        }
    }

    @Test
    public void testGetResidentApplication() throws IOException {

        Response response = getResponseOfGet(RESIDENT_APP_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("provisioningConfigurations", notNullValue());
    }

    @Test
    public void createApplication() throws Exception {

        String body = readResource("create-basic-application.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetApplicationById() throws Exception {

        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(CREATED_APP_NAME));
    }

    @Test(dependsOnMethods = {"createApplication"})
    public void testGetBasicApplicationDetailsWithFilter() throws Exception {

        // Create another application with name sample.
        String body = readResource("create-sample-basic-application.json");
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = extractApplicationIdFromLocationHeader(location);
        assertNotBlank(createdAppId);

        // Perform an eq with filter.
        Map<String, Object> params = new HashMap<>();
        params.put("filter", "name eq SAMPLE");
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH, params)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(1));
    }

    @Test(dependsOnMethods = {"testGetApplicationById"})
    public void testDeleteApplicationById() throws Exception {

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Verify that the application is not available.
        getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
