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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * OAuth2 DCRM Delete process test case
 */
public class OAuthDCRMDeleteTestCase extends ISIntegrationTest{

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

    @Test(alwaysRun = true, description = "Delete Service Provider")
    public void testDeleteServiceProvider() throws IOException {

        HttpDelete request = new HttpDelete(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider Delete request failed");
        assertEquals(response.getStatusLine().getStatusCode(), 204, "Service provider has not " +
                "been deleted successfully");
    }

    @Test(alwaysRun = true, description = "Delete service provider request with invalid client id")
    public void testDeleteRequestWithInvalidClientID() throws IOException {

        HttpDelete request = new HttpDelete(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        assertNotNull(response, "Service Provider Delete request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        rd.close();
        assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED, "Invalid error code has been returned " +
                "in the response. Should have produced: " + OAuthDCRMConstants.BACKEND_FAILED);
        assertEquals(errorDescription, "Error occurred while reading the existing service provider.",
                "Error response contains and invalid format of error description");
    }
}
