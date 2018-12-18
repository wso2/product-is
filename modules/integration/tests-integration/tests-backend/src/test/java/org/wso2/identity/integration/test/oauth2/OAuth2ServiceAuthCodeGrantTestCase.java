/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class OAuth2ServiceAuthCodeGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private AuthenticatorClient logManger;
    private String adminUsername;
    private String adminPassword;
    private String accessToken;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;
    private String consumerKey;
    private String consumerSecret;

    private static final String PLAYGROUND_RESET_PAGE = "http://localhost:" + CommonConstants.DEFAULT_TOMCAT_PORT +
            "/playground2/oauth2.jsp?reset=true";
    private DefaultHttpClient client;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String carbonHome = Utils.getResidentCarbonHome();
        String identityXMLFile = getISResourceLocation() + File.separator + "identity-original-530-cache-enabled.xml";
        File defaultIdentityXml = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator
                + "identity.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        File srcFile = new File(identityXMLFile);
        serverConfigurationManager.applyConfigurationWithoutRestart(srcFile, defaultIdentityXml, true);
        serverConfigurationManager.restartGracefully();
        super.init(TestUserMode.SUPER_TENANT_USER);
        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        client = new DefaultHttpClient();

        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = appDto.getOauthConsumerSecret();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request without response_type param", dependsOnMethods
            = "testRegisterApplication")
    public void testSendAuthorozedPostForError() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        AutomationContext automationContext = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        String authorizeEndpoint = automationContext.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/authorize");
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, authorizeEndpoint);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertTrue(locationHeader.getValue().contains("redirect_uri"),
                "Error response does not contain redirect_uri");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorozedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", ""));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorozedPost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT, Utils.getRedirectUrl
                    (response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");

        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractTableRowDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(response, "Authorization Code key value is invalid.");
        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());

    }

    @Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessToken() throws Exception {

        HttpResponse response = sendGetAccessTokenPost(client, consumerSecret);
        Assert.assertNotNull(response, "Error occured while getting access token.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        accessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(accessToken, "Access token is null.");

        EntityUtils.consume(response.getEntity());

    }

    @Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testGetAccessToken")
    public void testValidateAccessToken() throws Exception {

        HttpResponse response = sendValidateAccessTokenPost(client, accessToken);
        Assert.assertNotNull(response, "Validate access token response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"valid\"", 1);

        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        String valid = keyValues.get(0).getValue();
        Assert.assertEquals(valid, "true", "Token Validation failed");

        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Resending authorization code", dependsOnMethods =
            "testValidateAccessToken")
    public void testAuthzCodeResend() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, OAuth2Constant
                .OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " + Base64
                .encodeBase64String((consumerKey + ":" + consumerSecret).getBytes()).trim());
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(OAuth2Constant.INVALID_GRANT_ERROR, errorMessage, "Reusing the Authorization code has not" +
                " revoked the access token issued.");
    }

    @Test(groups = "wso2.is", description = "Retrying authorization code flow", dependsOnMethods =
            "testAuthzCodeResend")
    public void testAuthzCodeGrantRetry() throws Exception {

        String oldAccessToken = accessToken;

        HttpResponse response = sendGetRequest(client, PLAYGROUND_RESET_PAGE);
        EntityUtils.consume(response.getEntity());

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_PLAYGROUND_NAME, OAuth2Constant
                .OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CONSUMER_KEY_PLAYGROUND_NAME, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CALLBACKURL_PLAYGROUND_NAME, OAuth2Constant
                .CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_ENDPOINT_PLAYGROUND_NAME, OAuth2Constant
                .APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_PLAYGROUND_NAME, OAuth2Constant
                .AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.SCOPE_PLAYGROUND_NAME, ""));

        response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");

        testSendApprovalPost();
        testGetAccessToken();
        Assert.assertNotEquals(oldAccessToken, accessToken, "Access token not revoked from authorization code reusing");
        testAuthzCodeResend();
    }

    @Test(groups = "wso2.is",
            description = "Invalid authorization code",
            dependsOnMethods = "testRegisterApplication")
    public void testInvalidAuthzCode() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME,
                "authorizationinvalidcode12345678"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " + Base64
                .encodeBase64String((consumerKey + ":" + consumerSecret).getBytes()).trim());
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(OAuth2Constant.INVALID_GRANT_ERROR, errorMessage,
                "Invalid authorization code should have " + "produced error code : "
                        + OAuth2Constant.INVALID_GRANT_ERROR);
    }

}
