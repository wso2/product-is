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
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
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
    private static final String TOKEN_API_ENDPOINT = "https://localhost:9853/oauth2/token";
    private static final String SCOPE_PRODUCTION = "PRODUCTION";
    private static final String GRANT_TYPE_PASSWORD = "password";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        setSystemproperties();
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    /**
     * This test is written to test for token revocation with invalid client id , secrets and access tokens.
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(description = "Revoke token with invalid credentials.")
    public void testRevokeToken() throws Exception {
        //Application utils
        OAuthConsumerAppDTO appDto = createApplication();
        consumerKey = appDto.getOauthConsumerKey();
        consumerSecret = appDto.getOauthConsumerSecret();
        String errorMessage = null;
        //request for token
        String token = requestAccessToken(consumerKey, consumerSecret, TOKEN_API_ENDPOINT);

        //Revoke access token with invalid format of client credentials
        errorMessage = revokeToken(token, getInvalidBase64EncodedString(consumerKey, consumerSecret));
        Assert.assertEquals(OAuth2Constant.INVALID_CLIENT, errorMessage,
                "Invalid format in sending client credentials, should have produced : " + OAuth2Constant.INVALID_CLIENT
                        + "error code");

        //client credentials been null
        errorMessage = revokeToken(token, null);
        Assert.assertEquals(OAuth2Constant.INVALID_CLIENT, errorMessage,
                "Invalid format in sending client credentials, should have produced : " + OAuth2Constant.INVALID_CLIENT
                        + "error code");

        //revoke with invalid client key
        errorMessage = revokeToken(token, "dummyConsumerKey" + consumerSecret);
        Assert.assertEquals(OAuth2Constant.INVALID_CLIENT, errorMessage,
                "Invalid format in sending client credentials, should have produced : " + OAuth2Constant.INVALID_CLIENT
                        + "error code");

        //token is required parameter in request
        errorMessage = revokeToken(null, getBase64EncodedString(consumerKey, consumerSecret));
        //The request is missing a required parameter as per spec.
        Assert.assertEquals(OAuth2Constant.INVALID_REQUEST, errorMessage,
                "Invalid format in sending client credentials, should have produced : " + OAuth2Constant.INVALID_CLIENT
                        + "error code");

    }

    public String revokeToken(String accessToken, String clientCredentials) throws Exception {

        //Revoke access token
        String errorMessage = null;
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpRevoke = new HttpPost(REVOKE_TOKEN_API_ENDPOINT);
        //Generate revoke token post request
        httpRevoke.setHeader("Authorization", "Basic " + clientCredentials);
        httpRevoke.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("token", accessToken));
        httpRevoke.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpRevoke);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);

        Assert.assertNotNull(obj, "Returned error response should have produced a valid JSON.");
        Assert.assertNotNull(((JSONObject) obj).get("error"), "Returned error response should have 'error' defined.");

        errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        return errorMessage;

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    /**
     * Request access token from the given token generation endpoint
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @param backendUrl     token generation API endpoint
     * @return token
     * @throws Exception if something went wrong when requesting token
     */
    private String requestAccessToken(String consumerKey, String consumerSecret, String backendUrl) throws Exception {
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(backendUrl);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", "admin"));
        postParameters.add(new BasicNameValuePair("password", "admin"));
        postParameters.add(new BasicNameValuePair("scope", SCOPE_PRODUCTION));
        postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        //Get access token from the response
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        return json.get("access_token").toString();
    }

    /**
     * Create Application with the given app configurations
     *
     * @return OAuthConsumerAppDTO
     * @throws Exception
     */
    public OAuthConsumerAppDTO createApplication() throws Exception {
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        return createApplication(appDTO);
    }

    /**
     * Get base64 encoded string of consumer key and secret
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @return base 64 encoded string
     */
    private static String getBase64EncodedString(String consumerKey, String consumerSecret) {
        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
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