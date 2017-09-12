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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * OAuth2 DCRM API Create process test case
 */
public class OAuthDCRMCreateTestCase extends ISIntegrationTest{
    private HttpClient client;
    private ApplicationManagementServiceClient appMgtService;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        appMgtService = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        appMgtService.deleteApplication("testApp");
    }

    @Test(alwaysRun = true, description = "Create a service provider successfully")
    public void testCreateServiceProviderRequest() throws IOException {
        HttpPost request = new HttpPost(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        JSONArray redirectURI = new JSONArray();
        redirectURI.add("http://TestApp.com");

        JSONObject obj = new JSONObject();
        obj.put(OAuthDCRMConstants.CLIENT_NAME, "testApp");
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, redirectURI);

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Service Provider " +
                "has not been created successfully");
    }


    @Test(alwaysRun = true, description = "Create a service provider with already registered client name")
    public void testCreateServiceProviderRequestWithExistingClientName() throws IOException {
        HttpPost request = new HttpPost(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        JSONArray grantTypes = new JSONArray();
        grantTypes.add("authorization_code");
        grantTypes.add("implicit");

        JSONArray redirectURI = new JSONArray();
        redirectURI.add("http://TestApp.com");

        JSONObject obj = new JSONObject();
        obj.put(OAuthDCRMConstants.CLIENT_NAME, "testApp");
        obj.put(OAuthDCRMConstants.GRANT_TYPES, grantTypes);
        obj.put(OAuthDCRMConstants.REDIRECT_URIS, redirectURI);

        StringEntity entity = new StringEntity(obj.toJSONString());

        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 500, "Service Provider " +
                "creation request with already registered client name should have returned an internal server error");
    }

}
