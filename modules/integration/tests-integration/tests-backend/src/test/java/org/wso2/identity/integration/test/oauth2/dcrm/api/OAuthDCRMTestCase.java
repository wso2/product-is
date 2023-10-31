/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth2.dcrm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.DCRUtils;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * OAuth2 DCRM API Create process test case
 */
public class OAuthDCRMTestCase extends ISIntegrationTest {
    private static final String DUMMY_DCR_APP = "dummyDCRApp";
    private HttpClient client;

    private String client_id;
    private String username;
    private String password;
    private String tenant;

    @Factory(dataProvider = "dcrmConfigProvider")
    public OAuthDCRMTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

    }

    @DataProvider(name = "dcrmConfigProvider")
    public static Object[][] dcrmConfigProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        client = HttpClients.createDefault();
    }
    @Test(alwaysRun = true, groups = "wso2.is", priority = 1, description = "Create a service provider successfully")
    public void testCreateServiceProviderRequest() throws IOException {
        HttpPost request = new HttpPost(DCRUtils.getPath(tenant));
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONArray grantTypes = new JSONArray();
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_AUTHORIZATION_CODE);
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_IMPLICIT);

        JSONArray redirectURI = new JSONArray();
        redirectURI.add(OAuthDCRMConstants.REDIRECT_URI);

        JSONObject obj = new JSONObject();
        obj.put(OAuthDCRMConstants.CLIENT_NAME, OAuthDCRMConstants.APPLICATION_NAME);
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, redirectURI);

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Service Provider " +
                "has not been created successfully");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        client_id = ((JSONObject) responseObj).get("client_id").toString();

        assertNotNull(client_id, "client_id cannot be null");

    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 2, description =
            "Create a service provider with already registered client name")
    public void testCreateServiceProviderRequestWithExistingClientName() throws IOException {
        HttpPost request = new HttpPost(DCRUtils.getPath(tenant));
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONArray grantTypes = new JSONArray();
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_AUTHORIZATION_CODE);
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_IMPLICIT);

        JSONArray redirectURI = new JSONArray();
        redirectURI.add(OAuthDCRMConstants.REDIRECT_URI);

        JSONObject obj = new JSONObject();
        obj.put(OAuthDCRMConstants.CLIENT_NAME, OAuthDCRMConstants.APPLICATION_NAME);
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, redirectURI);

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 400, "Service Provider " +
                "creation request with already registered client name should have returned an bad request");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        String errorMsg = ((JSONObject) responseObj).get("error").toString();

        assertEquals(errorMsg, "invalid_client_metadata", "Invalid error message");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 3, description = "Read service provider")
    public void testReadServiceProvider() throws IOException {

        HttpGet request = new HttpGet(DCRUtils.getPath(tenant) + client_id);
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Service provider request has " +
                "not returned with successful response");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        String client_id = ((JSONObject) responseObj).get(OAuthDCRMConstants.CLIENT_ID).toString();
        assertEquals(client_id, this.client_id, "Client is should be equal.");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 4, description = "Read request with an invalid client ID")
    public void testReadServiceProviderWithInvalidClientID() throws IOException {
        HttpGet request = new HttpGet(DCRUtils.getPath(tenant) + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider read request " +
                "with invalid client ID should have returned an unauthorized");
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 5, description = "Delete Service Provider")
    public void testDeleteServiceProvider() throws IOException {

        HttpDelete request = new HttpDelete(DCRUtils.getPath(tenant) + client_id);
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "Service provider has not " +
                "been deleted successfully");

        EntityUtils.consume(response.getEntity());

        HttpGet getRequest = new HttpGet(DCRUtils.getPath(tenant) + client_id);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider read request " +
                "with invalid client ID should have returned an unauthorized");

        EntityUtils.consume(response.getEntity());

    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 6, description = "Delete service provider request with " +
            "invalid client id")
    public void testDeleteRequestWithInvalidClientID() throws IOException {
        HttpDelete request = new HttpDelete(DCRUtils.getPath(tenant) + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider delete request " +
                "with invalid client ID should have returned an unauthorized");

        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Try to register an OAuth app with authorization_code " +
            "grant without any redirect uris.", priority = 7)
    public void testRegisterAppWithAuthzCodeGrantAndNoRedirectUris() throws IOException {
        HttpPost request = new HttpPost(DCRUtils.getPath(tenant));
        DCRUtils.setRequestHeaders(request, username, password);

        JSONArray grantTypes = new JSONArray();
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_AUTHORIZATION_CODE);

        JSONObject dcrAppCreationRequest = new JSONObject();
        dcrAppCreationRequest.put(OAuthDCRMConstants.CLIENT_NAME, DUMMY_DCR_APP);
        dcrAppCreationRequest.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        dcrAppCreationRequest.put(OAuthDCRMConstants.REDIRECT_URIS, new JSONArray());

        StringEntity entity = new StringEntity(dcrAppCreationRequest.toJSONString());
        request.setEntity(entity);

        HttpResponse failedResponse = client.execute(request);
        assertEquals(failedResponse.getStatusLine().getStatusCode(), 400, "Since this was a BAD request should have " +
                "received an error response with 400 status code.");

        EntityUtils.consume(failedResponse.getEntity());
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 8, description = "Check whether created service providers " +
            "are cleaned up when OAuth app creation fails.")
    public void testRollbackOnInvalidRequest() throws IOException {
        // Basic Request
        JSONArray grantTypes = new JSONArray();
        grantTypes.add(OAuthDCRMConstants.GRANT_TYPE_AUTHORIZATION_CODE);

        JSONObject requestBody = new JSONObject();
        requestBody.put(OAuthDCRMConstants.CLIENT_NAME, DUMMY_DCR_APP);
        requestBody.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);

        //////////////////////// BAD REQUEST WITH EMPTY REDIRECT URI ///////////////////////////
        HttpPost badRequestWithoutRedirectUris = new HttpPost(DCRUtils.getPath(tenant));
        DCRUtils.setRequestHeaders(badRequestWithoutRedirectUris, username, password);
        // We keep the redirect uris empty to make this a bad request.
        JSONObject badRequestBody = (JSONObject) requestBody.clone();
        badRequestBody.put(OAuthDCRMConstants.REDIRECT_URIS, new JSONArray());
        badRequestWithoutRedirectUris.setEntity(new StringEntity(badRequestBody.toJSONString()));

        HttpResponse failedResponse = client.execute(badRequestWithoutRedirectUris);
        assertEquals(failedResponse.getStatusLine().getStatusCode(), 400, "Since this was a BAD request should have " +
                "received an error response with 400 status code.");

        EntityUtils.consume(failedResponse.getEntity());

        ///////////////// VALID REQUEST WITH THE SAME CLIENT_NAME ///////////////////////////
        HttpPost validRequest = new HttpPost(DCRUtils.getPath(tenant));
        DCRUtils.setRequestHeaders(validRequest, username, password);

        JSONArray redirectURIs = new JSONArray();
        redirectURIs.add(OAuthDCRMConstants.REDIRECT_URI);
        // Add a valid Redirect URI
        JSONObject validJSONBody = (JSONObject) requestBody.clone();
        validJSONBody.put(OAuthDCRMConstants.REDIRECT_URIS, redirectURIs);

        validRequest.setEntity(new StringEntity(validJSONBody.toJSONString()));
        HttpResponse successResponse = client.execute(validRequest);
        assertEquals(successResponse.getStatusLine().getStatusCode(), 201,
                "Service Provider should have been created with the same client name: " +  DUMMY_DCR_APP +
                        " attempted in the previous failed request.");

        BufferedReader rd = new BufferedReader(new InputStreamReader(successResponse.getEntity().getContent()));
        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(successResponse.getEntity());
        client_id = ((JSONObject) responseObj).get("client_id").toString();
        assertNotNull(client_id, "client_id cannot be null");

        // Deleting created application.
        testDeleteServiceProvider();
    }
    @Test(alwaysRun = true, groups = "wso2.is", priority = 9, description = "Create a service provider with " +
            "additional OIDC properties")
    public void testCreateServiceProviderRequestWithAdditionalParameters() throws Exception {

        HttpPost request = new HttpPost(DCRUtils.getPath(tenant));
        JSONObject registerRequestJSON = DCRUtils.getRegisterRequestJSON("request6.json");

        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        StringEntity entity = new StringEntity(registerRequestJSON.toJSONString());
        request.setEntity(entity);
        ObjectMapper mapper = new ObjectMapper();

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Service Provider " +
                "has not been created successfully");
        JSONObject createResponsePayload  = DCRUtils.getPayload(response);
        client_id = ((JSONObject) createResponsePayload).get("client_id").toString();
        assertNotNull(client_id, "client_id cannot be null");

        createResponsePayload.remove("client_id");
        createResponsePayload.remove("client_secret");
        createResponsePayload.remove("client_secret_expires_at");
        createResponsePayload.remove("software_statement");
        assertEquals(mapper.readTree(createResponsePayload.toJSONString()), mapper.readTree(
                registerRequestJSON.toJSONString()), "Response payload should be equal.");

        HttpGet getRequest = new HttpGet(DCRUtils.getPath(tenant) + client_id);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse getResponse = client.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Service provider request " +
                "has not returned with successful response");

        JSONObject getResponsePayload = DCRUtils.getPayload(getResponse);
        getResponsePayload.remove("client_id");
        getResponsePayload.remove("client_secret");
        getResponsePayload.remove("client_secret_expires_at");
        getResponsePayload.remove("software_statement");

        assertEquals(mapper.readTree(getResponsePayload.toJSONString()), mapper.readTree(
                registerRequestJSON.toJSONString()), "Response payload should be equal.");
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 10, description = "Create a service provider with " +
            "additional OIDC properties")
    public void testUpdateServiceProviderRequestWithAdditionalParameters() throws Exception {

        HttpPut request = new HttpPut(DCRUtils.getPath(tenant) + client_id);
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        JSONObject updateRequestPayload =  DCRUtils.getRegisterRequestJSON("request7.json");

        StringEntity entity = new StringEntity(updateRequestPayload.toJSONString());
        request.setEntity(entity);
        ObjectMapper mapper = new ObjectMapper();

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Service Provider " +
                "has not been created successfully");
        JSONObject updateResponsePayload  = DCRUtils.getPayload(response);
        client_id = ((JSONObject) updateResponsePayload).get("client_id").toString();
        assertNotNull(client_id, "client_id cannot be null");
        updateResponsePayload.remove("client_id");
        updateResponsePayload.remove("client_secret");
        updateResponsePayload.remove("client_secret_expires_at");
        updateResponsePayload.remove("software_statement");
        assertEquals(mapper.readTree(updateResponsePayload.toJSONString()),
                mapper.readTree(updateRequestPayload.toJSONString()), "Response payload should be equal.");

        // Verify that updated attribute is correctly returned by retrieving data.
        HttpGet getRequest = new HttpGet(DCRUtils.getPath(tenant) + client_id);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse getResponse = client.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Service provider request " +
                "has not returned with successful response");
        JSONObject getResponsePayload = DCRUtils.getPayload(getResponse);
        assertEquals(getResponsePayload.get("token_endpoint_auth_method"), "tls_client_auth");

        testDeleteServiceProvider();
    }
}
