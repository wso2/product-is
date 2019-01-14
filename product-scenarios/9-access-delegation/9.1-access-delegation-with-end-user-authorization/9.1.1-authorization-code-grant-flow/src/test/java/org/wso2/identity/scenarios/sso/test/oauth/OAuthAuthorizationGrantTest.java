/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.sso.test.oauth;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.ApplicationUtil;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil.KeyValue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.scenarios.commons.util.ApplicationUtil.deleteDCRApplication;
import static org.wso2.identity.scenarios.commons.util.Constants.COMMONAUTH_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.DCR_REGISTER_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.INTROSPECTION_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_AUTHORIZE_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_TOKEN_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_ACCESS_TOKEN;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CLIENT_ID;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CLIENT_SECRET;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.Constants.SCOPE_OPENID;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractDataFromResponse;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractFullContentFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.getSessionDataConsentKeyFromConsentPage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendAuthorizeGet;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPost;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendOAuthConsentApproveOncePost;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendTokenRequest;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendTokenValidateRequest;

public class OAuthAuthorizationGrantTest extends ScenarioTestBase {

    private static final String REDIRECT_URL = "http://testapp.org";
    private static final String APPLICATION_NAME = "TestApp1";
    private static final String INVALID_CLIENTID_ERROR = "A valid OAuth client could not be found for client_id";
    private static final String NULL_CLIENID_ERROR = "Client Id is not present in the authorization request";
    private static final String NULL_CALLBACKURI_ERROR = "Redirect URI is not present in the authorization request";
    private static final String INVALID_CALLBACKURI_ERROR = "invalid_callback";
    private static final String INVALID_CLIENT_SECRET_ERROR="Client Authentication failed";
    private static final String INVALID_GRANT_TYPE_ERROR="Unsupported grant_type value";

    private List<String> sessionDataKey = new ArrayList<>();
    private List<String> validOAuthConsentUrl = new ArrayList<>();
    private List<String> validSessionDataKeyConsent = new ArrayList<>();
    private List<String> validAuthzCode = new ArrayList<>();

    private CloseableHttpClient client;
    private String clientId;
    private String clientSecret;
    private String dcrEndpoint;
    private String tokenEndpoint;
    private String authzEndpoint;
    private String commonauthEndpoint;
    private String token;
    private String introspectionEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        String serverURL = getDeploymentProperty(IS_HTTPS_URL);
        dcrEndpoint = serverURL + DCR_REGISTER_URI_CONTEXT;
        authzEndpoint = serverURL + OAUTH_AUTHORIZE_URI_CONTEXT;
        tokenEndpoint = serverURL + OAUTH_TOKEN_URI_CONTEXT;
        commonauthEndpoint = serverURL + COMMONAUTH_URI_CONTEXT;
        introspectionEndpoint = serverURL + INTROSPECTION_URI;
        client = HttpClients.createDefault();

        registerApplication();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

         deleteApplication();
         client.close();
    }

    private void registerApplication() throws IOException {

        HttpResponse response = ApplicationUtil.registerDCRApplication(dcrEndpoint, APPLICATION_NAME, REDIRECT_URL,
                                                                       new String[] {GRANT_TYPE_AUTHORIZATION_CODE},
                                                                       ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "Application registration " +
                                                                                      "failed.");
        JSONObject responseObj = getJSONFromResponse(response);
        clientId = responseObj.get(PARAM_CLIENT_ID).toString();
        assertNotNull(clientId, "Client ID is null.");
        clientSecret = responseObj.get(PARAM_CLIENT_SECRET).toString();
        assertNotNull(clientSecret, "Client secret is null.");
    }

    private void deleteApplication() throws IOException {

        HttpResponse response = deleteDCRApplication(dcrEndpoint, clientId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                     "Application deletion failed.");
    }

    @DataProvider(name = "provideAuthorizeRequestParams") public Object[][] provideAuthorizeRequestParams() {
        return new Object[][] {

                //test with mandatory valid request parameters.
                { client, authzEndpoint, clientId, REDIRECT_URL, null, null, true, null, null },
                //test with the scope as "openid".
                { client, authzEndpoint, clientId, REDIRECT_URL, SCOPE_OPENID, null, true, null, null },
                //test with the value for scope other than null and openid.
                { client, authzEndpoint, clientId, REDIRECT_URL, "readOnly", null, true, null, null },
                //test with the invalid client Id.
                { client, authzEndpoint, "foo", REDIRECT_URL, SCOPE_OPENID, null, false, INVALID_CLIENTID_ERROR,
                        "Invalid client Id" },
                //test with client id as null value.
                { client, authzEndpoint, null, REDIRECT_URL, SCOPE_OPENID, null, false, NULL_CLIENID_ERROR,
                        "Client ID is null" },
                //test with redirect uri as null value.
                { client, authzEndpoint, clientId, null, SCOPE_OPENID, null, false, NULL_CALLBACKURI_ERROR,
                        "Call back url is null" },
                //test with invalid redirect uri
                { client, authzEndpoint, clientId, "http://foo.org", SCOPE_OPENID, null, false,
                        INVALID_CALLBACKURI_ERROR, "Invalid redirect uri" },

        };
    }

    @Test(description = "9.1.1.1", dataProvider = "provideAuthorizeRequestParams") public void intiAuthorizeRequest(
            CloseableHttpClient client, String authzEndpoint, String clientId, String redirectURI, String scope,
            Map<String, String> params, boolean isValidAuthReqParamList, String expected, String message)
            throws Exception {

        HttpResponse response = sendAuthorizeGet(client, authzEndpoint, clientId, redirectURI, scope, params);
        assertNotNull(response, "Response for authorize GET is null");

        if (isValidAuthReqParamList) {
            sessionDataKey.add(getSessionDataKey(response));
            assertNotNull(sessionDataKey, "sessionDataKey is null" + " : " + message);
            EntityUtils.consume(response.getEntity());
        }

        if (!isValidAuthReqParamList) {
            assertTrue(extractFullContentFromResponse(response).contains(expected), message);
        }
    }

    @DataProvider(name = "provideSessionDataKey") public Object[][] provideSessionDataKey() {
        return new Object[][] {

                // Authorize request with valid sessionDataKey and valid credentials.
                { sessionDataKey.get(0), ADMIN_USERNAME, ADMIN_PASSWORD, true, false, null },
                // Authorize request with valid sessionDataKey and invalid credentials.
                { sessionDataKey.get(1), ADMIN_USERNAME, "foo", false, true, "Invalid credentials" },
                // Authorize request with valid sessionDataKey and valid credentials.
                { sessionDataKey.get(2), ADMIN_USERNAME, ADMIN_PASSWORD, true, false, null } };
    }

    @Test(description = "9.1.1.1", dataProvider = "provideSessionDataKey", dependsOnMethods = "intiAuthorizeRequest")
    public void authenticate(String sessionDataKey, String username, String password, boolean isAuthenticated,
            boolean expected, String message) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey, commonauthEndpoint, username, password);
        assertNotNull(response, "Response for login POST is null");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (isAuthenticated) {
            Assert.assertEquals(Boolean.parseBoolean(
                    DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "authFailure")), expected,
                    message);
            validOAuthConsentUrl.add(locationHeader.getValue());
        }

        if (!isAuthenticated) {
            Assert.assertEquals(Boolean.parseBoolean(
                    DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "authFailure")), expected,
                    message);
        }
        EntityUtils.consume(response.getEntity());
    }

    @DataProvider(name = "provideOAuthConsentUrl") public Object[][] provideOAuthConsentUrl() {

        return new Object[][] { { validOAuthConsentUrl.get(0) }, { validOAuthConsentUrl.get(1) } };
    }

    @Test(description = "9.1.1.1", dataProvider = "provideOAuthConsentUrl", dependsOnMethods = "authenticate")
    public void initOAuthConsent(String oAuthConsentUrl) throws Exception {

        String sessionDataKeyConsent;
        HttpResponse response = sendGetRequest(client, oAuthConsentUrl, null);
        assertNotNull(response, "Response for oauth consent GET is null");

        sessionDataKeyConsent = getSessionDataConsentKeyFromConsentPage(response);
        Assert.assertNotNull(sessionDataKeyConsent, "SessionDataKeyConsent key value is null");
        validSessionDataKeyConsent.add(sessionDataKeyConsent);
        EntityUtils.consume(response.getEntity());
    }

    @DataProvider(name = "provideSessionDataKeyConsent") public Object[][] provideSessionDataKeyConsent() {

        return new Object[][] { { validSessionDataKeyConsent.get(0) }, { validSessionDataKeyConsent.get(1) } };
    }

    @Test(description = "9.1.1.1",dataProvider = "provideSessionDataKeyConsent", dependsOnMethods = "initOAuthConsent")
    public void submitOAuthConsent(String sessionDataKeyConsent) throws Exception {

        HttpResponse response = sendOAuthConsentApproveOncePost(client, sessionDataKeyConsent, authzEndpoint);
        Assert.assertNotNull(response, "OAuth consent POST is null.");
        validAuthzCode.add(getAuthzCode(response));
    }

    @DataProvider(name = "provideAuthCodeRequestParams") public Object[][] provideAuthCodeRequestParams() {
        return new Object[][]{

                //Invalid Client ID
                {validAuthzCode.get(0),tokenEndpoint,"rkldxC4eleo7fvPwtUHsvp9SFIMa",clientSecret,
                        REDIRECT_URL,GRANT_TYPE_AUTHORIZATION_CODE,false,INVALID_CLIENTID_ERROR,"Invalid Client ID"},
                //Invalid Client Secret
                {validAuthzCode.get(0),tokenEndpoint,clientId,"3Eedx5J6f9NfE7WEPpNbujRGOEa",REDIRECT_URL,
                        GRANT_TYPE_AUTHORIZATION_CODE,false,INVALID_CLIENT_SECRET_ERROR,"Client Authentication failed"},
                //Invalid Redirect URL
                {validAuthzCode.get(0),tokenEndpoint,clientId,clientSecret,"http://foo.org",
                        GRANT_TYPE_AUTHORIZATION_CODE,false,"Callback url mismatch","Invalid Redirect URL"},
                //Invalid grant type
                {validAuthzCode.get(0), tokenEndpoint,clientId,clientSecret,REDIRECT_URL,"foo",false,
                        INVALID_GRANT_TYPE_ERROR,"Unsupported grand type" },
                //Valid values
                {validAuthzCode.get(1), tokenEndpoint,clientId,clientSecret,REDIRECT_URL,
                        GRANT_TYPE_AUTHORIZATION_CODE,true,null,null }
        };
    }

    @Test(description = "9.1.1.1", dataProvider = "provideAuthCodeRequestParams",
            dependsOnMethods = "submitOAuthConsent") public void getOAuthToken(
            String authzCode, String tokenEndpoint, String clientId, String clientSecret, String redirectUrl,
            String grantType, boolean isValidRequest, String expected, String message) throws Exception {

        HttpResponse response = sendTokenRequest(client, authzCode, tokenEndpoint, clientId, clientSecret, redirectUrl,
                grantType, null, null);
        String responseString = extractFullContentFromResponse(response);
        Assert.assertNotNull(response, "OAuth token POST is null.");

        if (!isValidRequest) {
            assertTrue(responseString.contains(expected), message);
        }

        if (isValidRequest) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(responseString);
            token = json.get(PARAM_ACCESS_TOKEN).toString();
            Assert.assertNotNull(json.get(PARAM_ACCESS_TOKEN),
                    "Access token is not available in the token response : ");
            token = json.get(PARAM_ACCESS_TOKEN).toString();
        }
    }

    @Test(description = "9.1.1", dependsOnMethods = "getOAuthToken") public void validateOAuthToken()
            throws Exception {

        HttpResponse response = sendTokenValidateRequest(client, ADMIN_USERNAME, ADMIN_PASSWORD, token,
                introspectionEndpoint);
        JSONObject json = getJSONFromResponse(response);
        Assert.assertEquals(json.get("active").toString(), "true",
                "Access token is not a valid access " + "token");
    }

    private String getSessionDataKey(HttpResponse response) throws IOException {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY + "\"", 1);
        List<KeyValue> keyValues = extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        String sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
        return sessionDataKey;
    }

    private String getAuthzCode(HttpResponse response) throws IOException, URISyntaxException {

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");

        EntityUtils.consume(response.getEntity());

        String authzCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), PARAM_CODE);
        Assert.assertNotNull(authzCode, "Authorization code is null");

        return authzCode;
    }
}
