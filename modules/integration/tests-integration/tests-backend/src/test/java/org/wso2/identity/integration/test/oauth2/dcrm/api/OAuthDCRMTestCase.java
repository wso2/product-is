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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * OAuth2 DCRM API Create process test case
 */
public class OAuthDCRMTestCase extends ISIntegrationTest {
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
        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
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
        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
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

        HttpGet request = new HttpGet(getPath() + client_id);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
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
        HttpGet request = new HttpGet(getPath() + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider read request " +
                "with invalid client ID should have returned an unauthorized");
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 5, description = "Delete Service Provider")
    public void testDeleteServiceProvider() throws IOException {

        HttpDelete request = new HttpDelete(getPath() + client_id);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "Service provider has not " +
                "been deleted successfully");

        EntityUtils.consume(response.getEntity());

        HttpGet getRequest = new HttpGet(getPath() + client_id);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider read request " +
                "with invalid client ID should have returned an unauthorized");

        EntityUtils.consume(response.getEntity());

    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 6, description = "Delete service provider request with " +
            "invalid client id")
    public void testDeleteRequestWithInvalidClientID() throws IOException {
        HttpDelete request = new HttpDelete(getPath() + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 401, "Service Provider delete request " +
                "with invalid client ID should have returned an unauthorized");

        EntityUtils.consume(response.getEntity());
    }

    private String getPath() {
        if (tenant.equals("carbon.super")) {
            return OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART + OAuthDCRMConstants.DCR_ENDPOINT_PATH_PART;
        } else {
            return OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART + "/t/" + tenant + OAuthDCRMConstants
                    .DCR_ENDPOINT_PATH_PART;
        }
    }

    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
    }

}
