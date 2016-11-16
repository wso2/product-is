/*
*  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.test.requestPathAuthenticator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IDENTITY4604RequestPathAuthWithSAMLSSO extends RequestPathAuthenticatorBaseTestCase {

    private static final String ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    @Test(alwaysRun = true, description = "SAML login success", dependsOnMethods = { "testLoginSuccess" })
    public void testSMALRedirectBinding() throws Exception {
        HttpGet request = new HttpGet(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-Redirect");
        request.addHeader("http.protocol.handle-redirects", "false");

        HttpResponse response = client.execute(request);
        String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);

        response = sendSAMLMessage(ACS_URL, "SAMLResponse", samlResponse);
        String resultPage = extractDataFromResponse(response);

        Assert.assertTrue(resultPage.contains("You are logged in as " + super.adminUsername), "SAML SSO Login failed");

    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private String extractDataFromResponse(HttpResponse response, String key, int token) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String value = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
            }
        }
        rd.close();
        return value;
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(post);
    }

}

