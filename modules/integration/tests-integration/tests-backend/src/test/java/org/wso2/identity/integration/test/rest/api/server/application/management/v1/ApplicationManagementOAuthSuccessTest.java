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
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of the managing OAuth2/OIDC applications using Application Management REST API.
 */
public class ApplicationManagementOAuthSuccessTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private String createdAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementOAuthSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testCreateOAuthApp() throws Exception {

        String body = readResource("create-oauth-app.json");
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

    @Test(dependsOnMethods = "testCreateOAuthApp")
    public void testGetOAuthInboundDetails() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientId", notNullValue())
                .body("clientSecret", notNullValue());
    }

    @Test(dependsOnMethods = "testGetOAuthInboundDetails")
    public void testDeleteOAuthInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the OAuth inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeleteOAuthInbound")
    public void testDeleteFirstApp() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        createdAppId = null;
    }

    @Test(dependsOnMethods = "testDeleteFirstApp")
    public void testCreateOAuthAppWithPredefinedClientId() throws Exception {

        final String OAUTH_APP_NAME = "OAuth Application With ClientId";

        String body = readResource("create-oauth-app-with-predefined-clientid.json");
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

    @Test(dependsOnMethods = "testCreateOAuthAppWithPredefinedClientId")
    public void testGetOAuthInboundDetailsOfSecondApp() throws Exception {

        final String CLIENT_ID = "my_custom_client_id";
        final String CLIENT_SECRET = "my_custom_client_secret";

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientId", equalTo(CLIENT_ID))
                .body("clientSecret", equalTo(CLIENT_SECRET));
    }

    @Test(dependsOnMethods = "testGetOAuthInboundDetailsOfSecondApp")
    public void testUpdateOAuthInboundDetailsOfSecondApp() throws Exception {

        String body = readResource("update-oauth-app-with-predefined-clientid.json");
        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        getResponseOfPut(path, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("accessToken.bindingType", equalTo("cookie"))
                .body("grantTypes", containsInAnyOrder("refresh_token", "authorization_code", "account_switch",
                        "password"))
                .body("callbackURLs" , hasItem("http://localhost:8080/playground2/oauth2client"))
                .body("pkce.mandatory", equalTo(true))
                .body("pkce.supportPlainTransformAlgorithm", equalTo(true))
                .body("publicClient", equalTo(false));
    }

    @Test(dependsOnMethods = "testUpdateOAuthInboundDetailsOfSecondApp")
    public void testDeleteOAuthInboundOfSecondApp() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the OAuth inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeleteOAuthInboundOfSecondApp")
    public void testDeleteSecondApp() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        createdAppId = null;
    }
}
