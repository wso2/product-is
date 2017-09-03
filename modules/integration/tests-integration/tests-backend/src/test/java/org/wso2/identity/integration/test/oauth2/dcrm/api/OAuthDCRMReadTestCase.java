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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dcrm.api.bean.ServiceProviderDataHolder;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;

import java.io.*;

import static org.testng.Assert.assertEquals;

/**
 * OAuth2 DCRM API Read process test case
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
        client = HttpClients.createDefault();
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
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Service provider request has " +
                "not returned with successful response");
    }

    @Test(alwaysRun = true, description = "Read request with an invalid client ID")
    public void testReadServiceProviderWithInvalidClientID() throws IOException {
        HttpGet request = new HttpGet(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        request.addHeader(HttpHeaders.ACCEPT, OAuthDCRMConstants.ACCEPT);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 500, "Service Provider read request " +
                "with invalid client ID should returned an internal server error");
    }
}
