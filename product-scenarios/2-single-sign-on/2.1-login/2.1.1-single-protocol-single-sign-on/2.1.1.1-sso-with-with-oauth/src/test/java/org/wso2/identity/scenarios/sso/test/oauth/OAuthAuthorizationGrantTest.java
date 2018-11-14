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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.ApplicationUtil;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil.KeyValue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.ApplicationUtil.deleteDCRApplication;
import static org.wso2.identity.scenarios.commons.util.Constants.COMMONAUTH_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.DCR_REGISTER_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_AUTHORIZE_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_TOKEN_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_ACCESS_TOKEN;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CLIENT_ID;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CLIENT_SECRET;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractDataFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.getSessionDataConsentKeyFromConsentPage;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendAuthorizeGet;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPost;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendOAuthConsentApprovalPost;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendTokenRequest;

public class OAuthAuthorizationGrantTest extends ScenarioTestBase {

    private static final String REDIRECT_URL = "http://testapp.org";
    private static final String APPLICATION_NAME = "TestApp1";

    private CloseableHttpClient client;
    private String clientId;
    private String clientSecret;
    private String dcrEndpoint;
    private String tokenEndpoint;
    private String authzEndpoint;
    private String commonauthEndpoint;
    private String sessionDataKey;
    private String oAuthConsentUrl;
    private String sessionDataKeyConsent;
    private String authzCode;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        String serverURL = getDeploymentProperties().getProperty(IS_HTTPS_URL);
        dcrEndpoint = serverURL + DCR_REGISTER_URI_CONTEXT;
        authzEndpoint = serverURL + OAUTH_AUTHORIZE_URI_CONTEXT;
        tokenEndpoint = serverURL + OAUTH_TOKEN_URI_CONTEXT;
        commonauthEndpoint = serverURL + COMMONAUTH_URI_CONTEXT;
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

    @Test(description = "2.1.1.1.1")
    public void intiAuthorizeRequest() throws Exception {

        HttpResponse response = sendAuthorizeGet(client, authzEndpoint, clientId, REDIRECT_URL, null, null);
        assertNotNull(response, "Response for authorize GET is null");

        sessionDataKey = getSessionDataKey(response);
        assertNotNull(sessionDataKey, "sessionDataKey is null");
        EntityUtils.consume(response.getEntity());
    }

    @Test(description = "2.1.1.1.1", dependsOnMethods = "intiAuthorizeRequest")
    public void authenticate() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey, commonauthEndpoint, ADMIN_USERNAME,
                                              ADMIN_PASSWORD);
        assertNotNull(response, "Response for login POST is null");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        oAuthConsentUrl = locationHeader.getValue();
        EntityUtils.consume(response.getEntity());
    }

    @Test(description = "2.1.1.1.1", dependsOnMethods = "authenticate")
    public void initOAuthConsent() throws Exception {

        HttpResponse response = sendGetRequest(client, oAuthConsentUrl, null);
        assertNotNull(response, "Response for oauth consent GET is null");

        sessionDataKeyConsent = getSessionDataConsentKeyFromConsentPage(response);
        Assert.assertNotNull(sessionDataKeyConsent, "SessionDataKeyConsent key value is null");

        EntityUtils.consume(response.getEntity());
    }

    @Test(description = "2.1.1.1.1", dependsOnMethods = "initOAuthConsent")
    public void submitOAuthConsent() throws Exception {

        HttpResponse response = sendOAuthConsentApprovalPost(client, sessionDataKeyConsent, authzEndpoint);
        Assert.assertNotNull(response, "OAuth consent POST is null.");
        authzCode = getAuthzCode(response);
    }

    @Test(description = "2.1.1.1.1", dependsOnMethods = "submitOAuthConsent")
    public void getOAuthToken() throws Exception {

        HttpResponse response = sendTokenRequest(client, authzCode, tokenEndpoint, clientId, clientSecret,
                                                 REDIRECT_URL, null, null);
        Assert.assertNotNull(response, "OAuth token POST is null.");

        JSONObject json = getJSONFromResponse(response);
        Assert.assertNotNull(json.get(PARAM_ACCESS_TOKEN), "Access token is not available in the token response.");
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
