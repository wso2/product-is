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

package org.wso2.identity.scenarios.sso.test.dcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.HTTPCommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.sso.test.dcr.util.Constants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil.getCommonHeaders;

public class DCRTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(DCRTestCase.class);

    private static final String REGISTER_REQUESTS_LOCATION = "flow.requests.location";

    private static final String UPDATE_REQUESTS_LOCATION = "update.requests.location";

    private static JSONParser parser = new JSONParser();

    private HTTPCommonClient httpCommonClient;

    private JSONObject registerRequestJSON;

    private JSONObject updateRequestJSON;

    private String username;

    private String password;

    private String tenantDomain;

    private String dcrEndpoint;

    private String clientId;

    @Factory(dataProvider = "dcrConfigProvider")
    public DCRTestCase(JSONObject registerRequestJSON, JSONObject updateRequestJSON, String username, String password,
            String tenantDomain) {

        this.registerRequestJSON = registerRequestJSON;
        this.updateRequestJSON = updateRequestJSON;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.dcrEndpoint = getDcrEndpoint();
    }

    @DataProvider(name = "dcrConfigProvider")
    private static Object[][] dcrConfigProvider() throws Exception {

        return new Object[][] {
                {
                        getRegisterRequestJSON("request1.json"), getUpdateRequestJSON("request1.json"), ADMIN_USERNAME,
                        ADMIN_PASSWORD, SUPER_TENANT_DOMAIN
                }, {
                        getRegisterRequestJSON("request2.json"), getUpdateRequestJSON("request2.json"), ADMIN_USERNAME,
                        ADMIN_PASSWORD, SUPER_TENANT_DOMAIN
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        httpCommonClient = new HTTPCommonClient();
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws IOException {

        httpCommonClient.closeHttpClient();
    }

    @Test(description = "4.1.4.1.1")
    public void registerOAuth2Application() throws Exception {

        HttpResponse response = httpCommonClient
                .sendPostRequestWithJSON(dcrEndpoint, registerRequestJSON, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Application creation failed. Request Object: " + registerRequestJSON.toJSONString());

        validateResponse(registerRequestJSON, response, true);
    }

    @Test(description = "4.1.4.1.2",
          dependsOnMethods = { "registerOAuth2Application" })
    public void getOAuth2ApplicationByName() throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(Constants.RegistrationRequestElements.CLIENT_NAME,
                registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_NAME).toString());

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint, queryParams, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Application retrieval failed for name: " + registerRequestJSON
                        .get(Constants.RegistrationRequestElements.CLIENT_NAME).toString() + ", Request Object: "
                        + registerRequestJSON.toJSONString());

        validateResponse(registerRequestJSON, response, false);
    }

    @Test(description = "4.1.4.1.3",
          dependsOnMethods = { "registerOAuth2Application" })
    public void getOAuth2ApplicationByClientId() throws Exception {

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint + "/" + this.clientId, null, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Application retrieval failed for client id: " + clientId + ", Request Object: " + registerRequestJSON
                        .toJSONString());

        validateResponse(registerRequestJSON, response, false);
    }

    @Test(description = "4.1.4.1.4",
          dependsOnMethods = { "getOAuth2ApplicationByName", "getOAuth2ApplicationByClientId" })
    public void updateOAuth2Application() throws Exception {

        if (updateRequestJSON == null) {
            return;
        }

        HttpResponse response = httpCommonClient
                .sendPutRequestWithJSON(dcrEndpoint + "/" + this.clientId, updateRequestJSON,
                        getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Application update failed. Request Object: " + updateRequestJSON.toJSONString());

        validateResponse(updateRequestJSON, response, false);
    }

    @Test(description = "4.1.4.1.5",
          dependsOnMethods = { "updateOAuth2Application" })
    public void getUpdatedOAuth2ApplicationByName() throws Exception {

        if (updateRequestJSON == null) {
            return;
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(Constants.RegistrationRequestElements.CLIENT_NAME,
                updateRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_NAME).toString());

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint, queryParams, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Update application retrieval failed for name: " + updateRequestJSON
                        .get(Constants.RegistrationRequestElements.CLIENT_NAME).toString() + ", Request Object: "
                        + updateRequestJSON.toJSONString());

        validateResponse(updateRequestJSON, response, false);
    }

    @Test(description = "4.1.4.1.6",
          dependsOnMethods = { "updateOAuth2Application" })
    public void getUpdatedOAuth2ApplicationByClientId() throws Exception {

        if (updateRequestJSON == null) {
            return;
        }

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint + "/" + this.clientId, null, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Updated application retrieval failed for client id: " + clientId + " Request Object: "
                        + updateRequestJSON.toJSONString());

        validateResponse(updateRequestJSON, response, false);
    }

    @Test(description = "4.1.4.1.7",
          dependsOnMethods = { "getUpdatedOAuth2ApplicationByName", "getUpdatedOAuth2ApplicationByClientId" })
    public void deleteOAuth2ApplicationByClientId() throws Exception {

        HttpResponse response = httpCommonClient
                .sendDeleteRequest(dcrEndpoint + "/" + this.clientId, getCommonHeaders(username, password));

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Delete application failed for client id: " + clientId + ", Initial request Object: "
                        + registerRequestJSON.toJSONString());
    }

    @Test(description = "4.1.4.1.8",
          dependsOnMethods = { "deleteOAuth2ApplicationByClientId" })
    public void getDeletedOAuth2ApplicationByName() throws Exception {

        String clientName = registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_NAME).toString();
        if (updateRequestJSON != null) {
            clientName = updateRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_NAME).toString();
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(Constants.RegistrationRequestElements.CLIENT_NAME, clientName);

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint, queryParams, getCommonHeaders(username, password));

        assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Failed to delete the application for name: " + registerRequestJSON
                        .get(Constants.RegistrationRequestElements.CLIENT_NAME).toString()
                        + ", Initial request Object: " + registerRequestJSON.toJSONString());
    }

    @Test(description = "4.1.4.1.9",
          dependsOnMethods = { "deleteOAuth2ApplicationByClientId" })
    public void getDeletedOAuth2ApplicationByClientId() throws Exception {

        HttpResponse response = httpCommonClient
                .sendGetRequest(dcrEndpoint + "/" + this.clientId, null, getCommonHeaders(username, password));

        assertNotEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Updated application retrieval failed for client id: " + clientId + ", Initial request Object: "
                        + registerRequestJSON.toJSONString());
    }

    /**
     * Get register request JSON object.
     *
     * @param fileName File name.
     * @return Register request JSON object.
     * @throws Exception Exception.
     */
    private static JSONObject getRegisterRequestJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(REGISTER_REQUESTS_LOCATION, fileName)));
    }

    /**
     * Get update request JSON object.
     *
     * @param fileName File name.
     * @return Update request JSON object.
     * @throws Exception Exception.
     */
    private static JSONObject getUpdateRequestJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(UPDATE_REQUESTS_LOCATION, fileName)));
    }

    /**
     * Get file path.
     *
     * @param folderPath Folder path.
     * @param fileName   File name.
     * @return File path.
     * @throws Exception Exception.
     */
    private static String getFilePath(String folderPath, String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(folderPath) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    private String getDcrEndpoint() {

        if (tenantDomain == null || SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
            return getDeploymentProperty(IS_HTTPS_URL) + "/api/identity/oauth2/dcr/v1.1/register";
        }
        return getDeploymentProperty(IS_HTTPS_URL) + "/t/" + tenantDomain + "/api/identity/oauth2/dcr/v1.1/register";
    }

    private void validateResponse(JSONObject requestJSON, HttpResponse response, boolean isSetClientId)
            throws IOException {

        JSONObject applicationJSON = getJSONFromResponse(response);

        assertEquals(applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_NAME).toString(),
                requestJSON.get(Constants.RegistrationRequestElements.CLIENT_NAME).toString(),
                "Received client_name value is invalid. Request Object: " + requestJSON.toJSONString()
                        + ", Response Object: " + applicationJSON.toJSONString());

        if (registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_ID) != null) {
            assertEquals(applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_ID).toString(),
                    registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_ID).toString(),
                    "Received client_id value is invalid. Request Object: " + registerRequestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());
        } else {
            assertNotNull(applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_ID),
                    "Received client_id value is null. Request Object: " + registerRequestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());
        }

        if (isSetClientId) {
            this.clientId = applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_ID).toString();
        }

        if (registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_SECRET) != null) {
            assertEquals(applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_SECRET).toString(),
                    registerRequestJSON.get(Constants.RegistrationRequestElements.CLIENT_SECRET).toString(),
                    "Received client_secret value is invalid. Request Object: " + registerRequestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());
        } else {
            assertNotNull(applicationJSON.get(Constants.ApplicationResponseElements.CLIENT_SECRET),
                    "Received client_secret value is null. Request Object: " + registerRequestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());
        }

        if (requestJSON.get(Constants.RegistrationRequestElements.REDIRECT_URIS) != null
                && ((JSONArray) requestJSON.get(Constants.ApplicationResponseElements.REDIRECT_URIS)).size() > 0) {

            JSONArray redirectUris = (JSONArray) requestJSON.get(Constants.RegistrationRequestElements.REDIRECT_URIS);

            assertNotNull(applicationJSON.get(Constants.ApplicationResponseElements.REDIRECT_URIS),
                    "Received redirect_uris value is null. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());

            JSONArray returnedRedirectUris = (JSONArray) applicationJSON
                    .get(Constants.ApplicationResponseElements.REDIRECT_URIS);
            assertEquals(returnedRedirectUris.size(), redirectUris.size(),
                    "Received redirect_uris size is invalid. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + applicationJSON.toJSONString());

            for (Object redirectUrl : redirectUris) {
                assertTrue(returnedRedirectUris.contains(redirectUrl),
                        "Received redirect_uris content is invalid. Request Object: " + requestJSON.toJSONString()
                                + ", Response Object: " + applicationJSON.toJSONString());
            }
        }
    }
}
