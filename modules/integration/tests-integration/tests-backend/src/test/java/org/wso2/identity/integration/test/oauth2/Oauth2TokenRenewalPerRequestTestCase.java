/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This test class is used to test token renewal and old token revocation per token issue request.
 */
public class Oauth2TokenRenewalPerRequestTestCase extends OAuth2ServiceAbstractIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    private ServerConfigurationManager serverConfigurationManager;
    private CloseableHttpClient client;
    private static final String TEST_NONCE = "test_nonce";
    private String tempAccessToken;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        OAuthConsumerAppDTO appDto = createApplication();
        consumerKey = appDto.getOauthConsumerKey();
        consumerSecret = appDto.getOauthConsumerSecret();
        client = HttpClientBuilder.create().disableRedirectHandling().disableCookieManagement().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        log.info("Replacing deployment.toml with default configurations.");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(description = "Test token renewal per request and old token revocation using authzCode grant type.")
    public void authorizationCodeGrantTokenRenewalTest() throws Exception {

        JSONObject firstTokenRequest = getAuthzCodeAccessToken();
        String firstAccessToken = (String) firstTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);
        String firstRefreshToken = (String) firstTokenRequest.get(OAuth2Constant.REFRESH_TOKEN);

        JSONObject secondTokenRequest = getAuthzCodeAccessToken();
        String secondAccessToken = (String) secondTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);
        String secondRefreshToken = (String) secondTokenRequest.get(OAuth2Constant.REFRESH_TOKEN);

        Assert.assertNotEquals(firstAccessToken, secondAccessToken, "Old access token returned.");
        Assert.assertNotEquals(firstRefreshToken, secondRefreshToken, "Old refresh token returned.");
        checkOldTokenRevocation(firstAccessToken);
        tempAccessToken = secondAccessToken;
    }

    @Test(dependsOnMethods = {"authorizationCodeGrantTokenRenewalTest"}, description = "Test token renewal per " +
            "request and old token revocation using implicit grant type.")
    public void implicitGrantTokenRenewalTest() throws Exception {

        String firstAccessToken = getImplicitAccessToken();

        checkOldTokenRevocation(tempAccessToken);

        String secondAccessToken = getImplicitAccessToken();
        Assert.assertNotEquals(firstAccessToken, secondAccessToken, "Old access token returned.");
        checkOldTokenRevocation(firstAccessToken);

        tempAccessToken = secondAccessToken;
    }

    @Test(dependsOnMethods = {"implicitGrantTokenRenewalTest"}, description = "Test token renewal per request and old" +
            " token revocation using password grant type.")
    public void resourceOwnerTokenRenewalTest() throws Exception {

        JSONObject firstTokenRequest = getResourceOwnerAccessToken();
        String firstAccessToken = (String) firstTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);
        String firstRefreshToken = (String) firstTokenRequest.get(OAuth2Constant.REFRESH_TOKEN);

        checkOldTokenRevocation(tempAccessToken);

        JSONObject secondTokenRequest = getResourceOwnerAccessToken();
        String secondAccessToken = (String) secondTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);
        String secondRefreshToken = (String) secondTokenRequest.get(OAuth2Constant.REFRESH_TOKEN);

        Assert.assertNotEquals(firstAccessToken, secondAccessToken, "Old access token returned.");
        Assert.assertNotEquals(firstRefreshToken, secondRefreshToken, "Old refresh token returned.");
        checkOldTokenRevocation(firstAccessToken);
        tempAccessToken = secondAccessToken;
    }

    @Test(dependsOnMethods = {"resourceOwnerTokenRenewalTest"}, description = "Test token renewal per request and " +
            "old token revocation using client credentials grant type.")
    public void clientCredentialsTokenRenewalTest() throws Exception {

        JSONObject firstTokenRequest = getClientCredentialsAccessToken();
        String firstAccessToken = (String) firstTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);

        JSONObject secondTokenRequest = getClientCredentialsAccessToken();
        String secondAccessToken = (String) secondTokenRequest.get(OAuth2Constant.ACCESS_TOKEN);

        Assert.assertNotEquals(firstAccessToken, secondAccessToken, "Old access token returned.");
        checkOldTokenRevocation(firstAccessToken);
    }

    private JSONObject getAuthzCodeAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        String locationValue = sendAuthorizationRequest(client, urlParameters);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");
        String authorizationCode = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME);
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");

        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));

        JSONObject jsonResponse = responseObject(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters, consumerKey,
                consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");

        return jsonResponse;
    }

    private String getImplicitAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_RESPONSE_TYPE_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        String locationValue = sendAuthorizationRequest(client, urlParameters);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.ACCESS_TOKEN),
                "Access token not found in the response.");
        locationValue = locationValue.replace("#", "?");
        String token = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.ACCESS_TOKEN);
        Assert.assertNotNull(token, "Access token is null.");

        return token;
    }

    private JSONObject getResourceOwnerAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", userInfo.getUserName()));
        urlParameters.add(new BasicNameValuePair("password", userInfo.getPassword()));

        JSONObject jsonResponse = responseObject(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters, consumerKey,
                consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");

        return jsonResponse;
    }

    private JSONObject getClientCredentialsAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        JSONObject jsonResponse = responseObject(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters, consumerKey,
                consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");

        return jsonResponse;
    }

    private String sendAuthorizationRequest(HttpClient client, List<NameValuePair> urlParameters) throws Exception {

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String locationValue = getLocationHeaderValue(response);
        String sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());

        response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        return locationValue;
    }

    private void checkOldTokenRevocation(String token) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("token", token));

        JSONObject jsonResponse = responseObject(OAuth2Constant.INTRO_SPEC_ENDPOINT, urlParameters,
                userInfo.getUserName(), userInfo.getPassword());
        Assert.assertNotNull(jsonResponse, "Error in calling to introspection endpoint. Response is invalid.");
        Assert.assertEquals(jsonResponse.get("active"), false, "Old token still active.");
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    private JSONObject responseObject(String endpoint, List<NameValuePair> postParameters, String key, String secret)
            throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(key, secret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }

        return json;
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing deployment.toml to enable token renewal per request.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oauth" +
                File.separator + "token_renewal_per_request_enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }
}
