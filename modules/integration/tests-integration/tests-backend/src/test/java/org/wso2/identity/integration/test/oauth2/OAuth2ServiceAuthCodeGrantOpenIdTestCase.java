/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.oauth.Oauth2TokenValidationClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceAuthCodeGrantOpenIdTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private Oauth2TokenValidationClient oAuth2TokenValidationClient;
    private AuthenticatorClient logManger;

    private File identityXML;
    private String adminUsername;
    private String adminPassword;
    private String accessToken;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;

    private String consumerKey;
    private String consumerSecret;

    private DefaultHttpClient client;
    private Tomcat tomcat;

    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String USER_EMAIL = "abc@wso2.com";
    private static final String USERNAME = "authcodegrantuser";
    private static final String PASSWORD = "pass123";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_USER);

        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        String sessionIndex = logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        oAuth2TokenValidationClient = new Oauth2TokenValidationClient(backendURL, sessionIndex);
        client = new DefaultHttpClient();
        setSystemproperties();
        remoteUSMServiceClient.addUser(USERNAME, PASSWORD, new String[]{"admin"}, getUserClaims(), "default", true);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();
        stopTomcat(tomcat);

        logManger = null;
        consumerKey = null;
        accessToken = null;
        resetISConfiguration();
    }

    @Test(alwaysRun = true, description = "Deploy playground application")
    public void testDeployPlaygroundApp() {
        try {
            tomcat = getTomcat();
            URL resourceUrl =
                    getClass().getResource(File.separator + "samples" + File.separator +
                            "playground2.war");
            startTomcat(tomcat, OAuth2Constant.PLAYGROUND_APP_CONTEXT_ROOT, resourceUrl.getPath());
        } catch (Exception e) {
            Assert.fail("Playground application deployment failed.", e);
        }
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration", dependsOnMethods =
            "testDeployPlaygroundApp")
    public void testRegisterApplication() throws Exception {
        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDto.getOauthConsumerSecret();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorozedPost() throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " "
                + OAuth2Constant.OAUTH2_SCOPE_EMAIL));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response,
                "Authorization request failed. Authorized user response is null.");

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
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {
        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
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
        Assert.assertNotNull(response, "Approval response is invalid.");
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

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("id=\"loggedUser\"", 1);
        keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");

        String loggedUser = keyValues.get(0).getValue();
        Assert.assertNotNull(loggedUser, "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "null", "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "", "Logged user is null.");
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

    @Test(groups = "wso2.is", description = "Validate the user claim values", dependsOnMethods = "testGetAccessToken")
    public void testClaims() throws Exception {
        HttpGet request = new HttpGet(OAuth2Constant.USER_INFO_ENDPOINT);

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String email = ((org.json.simple.JSONObject) obj).get("email").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(USER_EMAIL, email, "Incorrect email claim value");
    }

    @Test(groups = "wso2.is", description = "Validate Token Expiration Time",
            dependsOnMethods = "testValidateAccessToken")
    public void testValidateTokenExpirationTime() throws Exception {
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessTokenDTO = new
                OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessTokenDTO.setIdentifier(accessToken);
        accessTokenDTO.setTokenType("bearer");
        requestDTO.setAccessToken(accessTokenDTO);

        OAuth2TokenValidationResponseDTO responseDTO = oAuth2TokenValidationClient.validateToken(requestDTO);
        Assert.assertNotNull(responseDTO != null && responseDTO.getAuthorizationContextToken() != null,
                "received authorization context token is null");

        if (responseDTO != null && responseDTO.getAuthorizationContextToken() != null) {
            String tokenString = responseDTO.getAuthorizationContextToken().getTokenString();
            Assert.assertNotNull(tokenString, "received token string is null");

            String[] tokenElements = tokenString.split("\\.");
            Assert.assertTrue(tokenElements.length > 1, "Invalid JWT token received");

            JSONObject jwtJsonObject = new JSONObject(new String(Base64.decodeBase64(tokenElements[1])));
            Assert.assertNotNull(jwtJsonObject.get("exp"), "'exp' value is not included");

            long expValue = Long.valueOf(jwtJsonObject.get("exp").toString());
            // ratio between these vales is normally 999, used 975 just to be in the safe side
            Assert.assertTrue(System.currentTimeMillis() / expValue > 975, "'exp time is not in milliseconds'");
        }
    }


    @Test(groups = "wso2.is", description = "Validate Authorization Context of jwt Token", dependsOnMethods =
            "testGetAccessToken")
    public void testAuthorizationContextValidateJwtToken() throws Exception {
        String claimURI[] = {OAuth2Constant.WSO2_CLAIM_DIALECT_ROLE};
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessTokenDTO = new
                OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessTokenDTO.setIdentifier(accessToken);
        accessTokenDTO.setTokenType("bearer");
        requestDTO.setAccessToken(accessTokenDTO);
        requestDTO.setRequiredClaimURIs(claimURI);

        OAuth2TokenValidationResponseDTO responseDTO = oAuth2TokenValidationClient.validateToken(requestDTO);
        if (responseDTO != null && responseDTO.getAuthorizationContextToken() != null) {
            String tokenString = responseDTO.getAuthorizationContextToken().getTokenString();

            String[] tokenElements = tokenString.split("\\.");
            JSONObject jwtJsonObject = new JSONObject(new String(Base64.decodeBase64(tokenElements[1])));
            String jwtClaimMappingRoleValues = jwtJsonObject.get(OAuth2Constant.WSO2_CLAIM_DIALECT_ROLE).toString();
            Assert.assertTrue(jwtClaimMappingRoleValues.contains(","), "Broken JWT Token from Authorization context");

            String[] jwtClaimMappingRoleElements = jwtClaimMappingRoleValues.replaceAll("[\\[\\]\"]", "").split(",");
            List<String> jwtClaimMappingRoleElementsList = Arrays.asList(jwtClaimMappingRoleElements);
            Assert.assertTrue(jwtClaimMappingRoleElementsList.contains("Internal/everyone"), "Invalid JWT Token Role " +
                    "Values");
        }
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing identity.xml changing the entity id of SSOService");

        String carbonHome = CarbonUtils.getCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File
                .separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation()
                + File.separator + "oauth" + File.separator
                + "jwt-token-gen-enabled-identity.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }

    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);

        return response;
    }


    protected ClaimValue[] getUserClaims() {
        ClaimValue[] claimValues = new ClaimValue[1];

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(USER_EMAIL);
        claimValues[0] = email;

        return claimValues;
    }

}