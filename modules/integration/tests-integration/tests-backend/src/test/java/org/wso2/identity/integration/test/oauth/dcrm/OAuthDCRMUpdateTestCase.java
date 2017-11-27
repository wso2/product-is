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
package org.wso2.identity.integration.test.oauth.dcrm;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth.dcrm.bean.ServiceProviderDataHolder;
import org.wso2.identity.integration.test.oauth.dcrm.util.OAuthDCRMConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * OAuth2 DCRM Update process test case
 */
public class OAuthDCRMUpdateTestCase extends ISIntegrationTest{

    private HttpClient client;
    private ServiceProviderRegister serviceProviderRegister;
    private ServiceProviderDataHolder serviceProvider;
    private OauthAdminClient adminClient;
    private ApplicationManagementServiceClient appMgtService;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        appMgtService = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        client = new DefaultHttpClient();
        serviceProviderRegister = new ServiceProviderRegister();
        serviceProvider = serviceProviderRegister.register(appMgtService, adminClient);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        adminClient.removeOAuthApplicationData(serviceProvider.getClientID());
        appMgtService.deleteApplication(serviceProvider.getClientName());
    }

    @Test(alwaysRun = true, description = "Update Service Provider")
    public void testUpdateServiceProvide() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        JSONArray redirecturi = new JSONArray();
        redirecturi.add("http://UpdatedApp.com");

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, serviceProvider.getClientID());
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, serviceProvider.getClientSecret());
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, redirecturi);

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;

        JSONArray grantTypesResponse = (JSONArray) jsonObject.get(OAuthDCRMConstants.GRANT_TYPES);
        List<String> gt = new ArrayList<>();
        for (Object gtObj:grantTypesResponse) {
            gt.add((String) gtObj);
        }

        assertEquals(gt, grantTypes, "Grant types are not updated.");

        JSONArray redirectURIResponse = (JSONArray) jsonObject.get(OAuthDCRMConstants.REDIRECT_URIS);
        List<String> redirectURIs = new ArrayList<>();
        for (Object uri:redirectURIResponse) {
            redirectURIs.add((String) uri);
        }

        assertEquals(redirectURIs, redirecturi, "Redirect URIs are not updated.");
    }

    @Test(alwaysRun = true, description = "Update request with invalid endpoint URL")
    public void testUpdateRequestWithInvalidEndpointURL() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, serviceProvider.getClientID());
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, serviceProvider.getClientSecret());
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, "http://UpdatedApp.com");

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been " +
                "returned in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "Error occurred while reading the existing service provider.",
                "Error response contains and invalid format of error description");

    }

    @Test(alwaysRun = true, description = "Update request with invalid client id")
    public void testUpdateRequestWithInvalidClientID() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, OAuthDCRMConstants.INVALID_CLIENT_ID);
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, serviceProvider.getClientSecret());
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, "http://UpdatedApp.com");

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been returned " +
                "in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "The included client ID is not a valid value",
                "Error response contains and invalid format of error description");
    }

    @Test(alwaysRun = true, description = "Update request with invalid client secret")
    public void testUpdateRequestWithInvalidClientSecret() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, serviceProvider.getClientID());
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, OAuthDCRMConstants.INVALID_CLIENT_SECRET);
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, "http://UpdatedApp.com");

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been returned " +
                "in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "The included client secret is not a valid value",
                "Error response contains and invalid format of error description");
    }

    @Test(alwaysRun = true, description = "Update request without grant type")
    public void testUpdateRequestWithoutGrantType() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, serviceProvider.getClientID());
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, serviceProvider.getClientSecret());
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, "http://UpdatedApp.com");

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;

        JSONArray grantTypes = (JSONArray) jsonObject.get(OAuthDCRMConstants.GRANT_TYPES);
        List<String> gt = new ArrayList<>();
        for (Object grantType:grantTypes) {
            gt.add((String) grantType);
        }

        assertEquals(gt.get(0), "authorization_code", "Grant type is not updated with " +
                "authorization_code");
    }

    @Test(alwaysRun = true, description = "Update request with invalid grant type")
    public void testUpdateRequestWithInvalidGrantType() throws IOException {

        HttpPut request = new HttpPut(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONObject obj = new JSONObject();

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("code");

        obj.put(OAuthDCRMConstants.CLIENT_NAME, serviceProvider.getClientName());
        obj.put(OAuthDCRMConstants.CLIENT_ID, serviceProvider.getClientID());
        obj.put(OAuthDCRMConstants.CLIENT_SECRET, serviceProvider.getClientSecret());
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, "http://UpdatedApp.com");

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider update request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been returned " +
                "in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "Error occurred while reading the updated service provider",
                "Error response contains and invalid format of error description");
    }
}
