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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth.dcrm.bean.ServiceProvider;
import org.wso2.identity.integration.test.oauth.dcrm.util.OAuthDCRMConstants;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OAuthDCRMReadTestCase extends ISIntegrationTest {

    private HttpClient client;
    private ServiceProviderRegister serviceProviderRegister;
    private ServiceProvider serviceProvider;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        client = new DefaultHttpClient();
        serviceProviderRegister = new ServiceProviderRegister();

        JSONObject object = new JSONObject();
        object.put(OAuthDCRMConstants.CLIENT_NAME, "ReadApp");
        object.put(OAuthDCRMConstants.GRANT_TYPES, "implicit");
        object.put(OAuthDCRMConstants.REDIRECT_URIS, "http://ReadApp.com");

        serviceProvider = serviceProviderRegister.register(object.toJSONString());
    }

    @Test(alwaysRun = true, description = "Read service provider")
    public void testReadServiceProvider() throws IOException {

        HttpGet request = new HttpGet(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Service Provider read request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        Assert.assertNotNull(obj, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) obj;

        Assert.assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_NAME), serviceProvider.getClientName());
        Assert.assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_ID), serviceProvider.getClientID());
        Assert.assertEquals((String) jsonObject.get(OAuthDCRMConstants.CLIENT_SECRET),
                serviceProvider.getClientSecret());

        JSONArray grantTypes = (JSONArray) jsonObject.get(OAuthDCRMConstants.GRANT_TYPES);
        List<String> gt = new ArrayList<>();
        for (Object grantType:grantTypes) {
            gt.add((String) grantType);
        }
        Assert.assertEquals(gt, serviceProvider.getGrantTypes());

        JSONArray redirectURIs = (JSONArray) jsonObject.get(OAuthDCRMConstants.REDIRECT_URIS);
        List<String> ruri = new ArrayList<>();
        for (Object redirectURI:redirectURIs) {
            ruri.add((String) redirectURI);
        }
        Assert.assertEquals(ruri, serviceProvider.getRedirectUris());
    }

    @Test(alwaysRun = true, description = "Read request with an invalid client ID")
    public void testReadServiceProviderWithInvalidClientID() throws IOException {
        HttpGet request = new HttpGet(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Service Provider read request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        Assert.assertNotNull(obj, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) obj;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        Assert.assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED);
        Assert.assertEquals(errorDescription, "Error occurred while reading the existing service provider.");
    }
}
