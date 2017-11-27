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
import org.apache.http.client.methods.HttpGet;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * OAuth2 DCRM Read process test case
 */
public class OAuthDCRMReadTestCase extends ISIntegrationTest {

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

    @Test(alwaysRun = true, description = "Read service provider")
    public void testReadServiceProvider() throws IOException {

        HttpGet request = new HttpGet(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider read request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        rd.close();
        assertNotNull(obj, "Returned response should have produced a valid JSON");

        JSONObject jsonObject = (JSONObject) obj;

        assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_NAME), serviceProvider.getClientName(),
                "Response contains an invalid client Name");
        assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_ID), serviceProvider.getClientID(),
                "Response contains an invalid client ID");
        assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_SECRET),
                serviceProvider.getClientSecret(), "Response contains an invalid client secret");

        JSONArray grantTypes = (JSONArray) jsonObject.get(OAuthDCRMConstants.GRANT_TYPES);
        List<String> gt = new ArrayList<>();
        for (Object grantType:grantTypes) {
            gt.add((String) grantType);
        }
        assertEquals(gt, serviceProvider.getGrantTypes(), "Returned grant types differ from the " +
                "registered grant types.");

        JSONArray redirectURIs = (JSONArray) jsonObject.get(OAuthDCRMConstants.REDIRECT_URIS);
        List<String> ruri = new ArrayList<>();
        for (Object redirectURI:redirectURIs) {
            ruri.add((String) redirectURI);
        }
        assertEquals(ruri, serviceProvider.getRedirectUris(), "Returned redirect URIs differ from the " +
                "registered redirect URIs.");
    }

    @Test(alwaysRun = true, description = "Read request with an invalid client ID")
    public void testReadServiceProviderWithInvalidClientID() throws IOException {
        HttpGet request = new HttpGet(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider read request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        rd.close();
        assertNotNull(obj, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) obj;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been " +
                "returned in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "Error occurred while reading the existing service provider.",
                "Error response contains and invalid format of error description");
    }
}
