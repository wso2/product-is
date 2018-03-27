/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This test case is for IDENTITY-5128, to check that when the oauth2 revoke endpoint is called with invalid client
 * credentials it should have proper error handling than having a server error.
 */
public class OAuth2TokenRevokeWithInvalidClientCredentialsTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String REVOKE_TOKEN_API_ENDPOINT = "https://localhost:9853/oauth2/revoke";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        setSystemproperties();
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    /**
     * This tests written for test for token revocation after cache timed out CARBON-15028
     * This test needed two APIM nodes with clustering enabled
     * During the test one node is use to generate the token and other node use to revoke the token
     * After cache timeout new token should issued after it revoked
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(description = "Revoke token with invalid sending of client credentials.")
    public void testRevokeTokenAfterCacheTimedOut() throws Exception {
        String clientKey = "dummyConsumerKey";
        String clientSecret = "dummyConsumerSecret";
        String accessToken = "dummyAccessToken";

        //Revoke access token
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpRevoke = new HttpPost(REVOKE_TOKEN_API_ENDPOINT);
        //Generate revoke token post request
        httpRevoke.setHeader("Authorization", "Basic " + getInvalidBase64EncodedString(clientKey, clientSecret));
        httpRevoke.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("token", accessToken));
        httpRevoke.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpRevoke);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);

        Assert.assertNotNull(obj, "Returned error response should have produced a valid JSON.");
        Assert.assertNotNull(((JSONObject) obj).get("error"), "Returned error response should have 'error' defined.");

        String errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        Assert.assertEquals("invalid_request", errorMessage,
                "Invalid format in sending client credentials, should have produced : " + OAuth2Constant.INVALID_CLIENT
                        + "error code");

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    /**
     * Get base64 encoded invalid with string of consumer key and secret and another dummy value
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @return base 64 encoded string
     */
    private static String getInvalidBase64EncodedString(String consumerKey, String consumerSecret) {
        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret + ":errorSuffix").getBytes()));
    }

}