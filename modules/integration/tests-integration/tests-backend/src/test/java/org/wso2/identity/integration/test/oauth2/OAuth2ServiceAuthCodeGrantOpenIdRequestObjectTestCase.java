/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
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
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.Oauth2TokenValidationClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class OAuth2ServiceAuthCodeGrantOpenIdRequestObjectTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String OIDC_CLAIM_DIALECT = "http://wso2.org/oidc/claim";
    private ServerConfigurationManager serverConfigurationManager;
    private Oauth2TokenValidationClient oAuth2TokenValidationClient;
    private AuthenticatorClient logManger;
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient;

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
    private List<NameValuePair> consentParameters = new ArrayList<>();
    private CookieStore cookieStore = new BasicCookieStore();
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";
    private static final String givenNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String countryClaimURI = "http://wso2.org/claims/country";
    private static final String customClaimURI1 = "http://wso2.org/claims/challengeQuestion1";
    private static final String customClaimURI2 = "http://wso2.org/claims/challengeQuestion2";
    private static final String externalClaimURI1 = "externalClaim1";
    private static final String externalClaimURI2 = "externalClaim2";

    private static final String USER_EMAIL = "abcrqo@wso2.com";
    private static final String USERNAME = "authcodegrantreqobjuser";
    private static final String PASSWORD = "pass123";
    private static final String GIVEN_NAME = "TestName";
    private static final String COUNTRY = "Country";
    private static final String CUSTOM_CLAIM1 = "customVal1";
    private static final String CUSTOM_CLAIM2 = "customVal2";

    private static final String REQUEST = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwiY" +
            "XVkIjpbImh0dHBzOi8vbG9jYWxob3N0Ojk0NDMvb2F1dGgyL3Rva2VuIl0sImNsYWltcyI6eyJ1c2VyaW5mbyI6eyJnaXZlbl9uYW1l" +
            "Ijp7ImVzc2VudGlhbCI6dHJ1ZX0sIm5pY2tuYW1lIjpudWxsLCJlbWFpbCI6eyJlc3NlbnRpYWwiOnRydWV9LCJleHRlcm5hbENsYWl" +
            "tMSI6eyJlc3NlbnRpYWwiOnRydWV9LCJwaWN0dXJlIjpudWxsfSwiaWRfdG9rZW4iOnsiZ2VuZGVyIjpudWxsLCJiaXJ0aGRhdGUiOn" +
            "siZXNzZW50aWFsIjp0cnVlfSwiY3VzdG9tQ2xhaW0xIjp7ImVzc2VudGlhbCI6dHJ1ZX0sImFjciI6eyJ2YWx1ZXMiOlsidXJuOm1hY" +
            "2U6aW5jb21tb246aWFwOnNpbHZlciJdfX19LCJpc3MiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwiZXhwIjoxNTE2Nzg2" +
            "ODc4LCJpYXQiOjE1MTY3ODMyNzgsImp0aSI6IjEwMDMifQ.";

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
        client.setCookieStore(cookieStore);
        setSystemproperties();
        remoteUSMServiceClient.addUser(USERNAME, PASSWORD, new String[]{"admin"}, getUserClaims(), "default", true);
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendURL,
                sessionIndex);
        addOIDCClaims(externalClaimURI1, customClaimURI1);
        addOIDCClaims(externalClaimURI2, customClaimURI2);

    }

    private void addOIDCClaims(String externalClaim, String localClaim) throws RemoteException, ClaimMetadataManagementServiceClaimMetadataException {
        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO.setExternalClaimDialectURI(OIDC_CLAIM_DIALECT);
        externalClaimDTO.setMappedLocalClaimURI(localClaim);
        externalClaimDTO.setExternalClaimURI(externalClaim);
        claimMetadataManagementServiceClient.addExternalClaim(externalClaimDTO);
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
                    getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
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
        UpdateApplicationClaimConfig();
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
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL
                + "?request=" + REQUEST));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));

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

        EntityUtils.consume(response.getEntity());
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendConsentGetRequest(client, locationHeader.getValue(), cookieStore, consentParameters);
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

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
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
        String givenName = ((org.json.simple.JSONObject) obj).get("given_name").toString();
        Object externalClaim1 = ((org.json.simple.JSONObject) obj).get("externalClaim1");
        Object externalClaim2 = ((org.json.simple.JSONObject) obj).get("externalClaim2");

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(USER_EMAIL, email, "Incorrect email claim value");
        Assert.assertEquals(GIVEN_NAME, givenName, "Incorrect given_name claim value");
        Assert.assertEquals(CUSTOM_CLAIM1, externalClaim1, "Incorrect externalClaim1 claim value");
        Assert.assertNull(externalClaim2, "A value for externalClaim2 claim is present in the response.");
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
        File defaultIdentityXml = new File(getISResourceLocation() + File.separator + "default-identity.xml");
        serverConfigurationManager.applyConfigurationWithoutRestart(defaultIdentityXml,
                identityXML, true);
        serverConfigurationManager.restartForcefully();
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
        ClaimValue[] claimValues = new ClaimValue[5];
        claimValues[0] = getClaimValue(emailClaimURI, USER_EMAIL);
        claimValues[1] = getClaimValue(givenNameClaimURI, GIVEN_NAME);
        claimValues[2] = getClaimValue(countryClaimURI, COUNTRY);
        claimValues[3] = getClaimValue(customClaimURI1, CUSTOM_CLAIM1);
        claimValues[4] = getClaimValue(customClaimURI2, CUSTOM_CLAIM2);

        return claimValues;
    }

    private ClaimValue getClaimValue(String claimURL, String claimVal) {
        ClaimValue claim = new ClaimValue();
        claim.setClaimURI(claimURL);
        claim.setValue(claimVal);
        return claim;
    }

}
