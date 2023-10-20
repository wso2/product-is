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

package org.wso2.identity.integration.test.rest.api.user.authorized.apps.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserAuthorizedAppsBaseTest extends RESTAPIUserTestBase {

    static final String API_DEFINITION_NAME = "authorizedApps.yaml";
    static final String API_VERSION = "v1";
    static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.authorized.apps.v1";

    public static final String AUTHORIZED_APPS_ENDPOINT_URI = "/%s/authorized-apps/";
    public static final String DCR_ENDPOINT_PATH_URI = "/t/%s/api/identity/oauth2/dcr/v1.1/register/";

    protected String userAuthorizedAppsEndpointUri;
    protected String dcrEndpointUri;
    protected String tokenEndpointUri;

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                                      API_PACKAGE_NAME), e);
        }
    }

    void initUrls(String pathParam) {

        this.userAuthorizedAppsEndpointUri = String.format(AUTHORIZED_APPS_ENDPOINT_URI, pathParam);
        this.dcrEndpointUri = String.format(DCR_ENDPOINT_PATH_URI, tenant);
        this.tokenEndpointUri = "/oauth2/token";
    }

    public void registerApplication(String appName, String clientId, String clientSecret) {

        String body = "{\n" +
                      "    \"client_name\": \"" + appName + "\",\n" +
                      "    \"grant_types\": [\n" +
                      "        \"password\"\n" +
                      "    ],\n" +
                      "    \"ext_param_client_id\": \"" + clientId + "\",\n" +
                      "    \"ext_param_client_secret\": \"" + clientSecret + "\"\n" +
                      "}";
        Response response = getResponseOfJSONPost(dcrEndpointUri, body, new HashMap<>());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("client_name", equalTo(appName))
                .body("client_id", equalTo(clientId))
                .body("client_secret", equalTo(clientSecret));
    }

    public void getTokenFromPasswordGrant(String clientId, String clientSecret) {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("username", authenticatingUserName);
        params.put("password", authenticatingCredential);

        Response response = getResponseOfFormPostWithAuth(tokenEndpointUri, params, new HashMap<>(), clientId,
                                                          clientSecret);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue());
    }

    public void deleteApplication(String clientId) {

        Response response = getResponseOfDelete(dcrEndpointUri + clientId, new HashMap<>());
        response.then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }


    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }
}
