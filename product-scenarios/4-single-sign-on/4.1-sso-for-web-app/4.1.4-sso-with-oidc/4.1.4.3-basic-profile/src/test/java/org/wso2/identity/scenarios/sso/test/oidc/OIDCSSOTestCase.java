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

package org.wso2.identity.scenarios.sso.test.oidc;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.HTTPCommonClient;
import org.wso2.identity.scenarios.commons.OAuth2CommonClient;
import org.wso2.identity.scenarios.commons.SSOCommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.OAuth2Constants;
import org.wso2.identity.scenarios.commons.util.SSOConstants;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.CLIENT_ID;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.CLIENT_SECRET;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.REDIRECT_URIS;

/**
 * This test class tests the single sign-on for web applications using OIDC.
 */
public class OIDCSSOTestCase extends ScenarioTestBase {

    private String dcrRequestFile1;

    private String dcrRequestFile2;

    private String appCreatorUsername;

    private String appCreatorPassword;

    private String username;

    private String password;

    private String tenantDomain;

    private String clientId1;

    private String clientSecret1;

    private String redirectUri1;

    private String clientId2;

    private String clientSecret2;

    private String redirectUri2;

    private String sessionDataKey;

    private String sessionDataKeyConsent;

    private String consentUrl;

    private String authorizeCode;

    private String accessToken;

    private HTTPCommonClient httpCommonClient;

    private OAuth2CommonClient oAuth2CommonClient;

    private SSOCommonClient ssoCommonClient;

    @Factory(dataProvider = "oidcSSOConfigProvider")
    public OIDCSSOTestCase(String dcrRequestFile1, String dcrRequestFile2, String appCreatorUsername,
            String appCreatorPassword, String username, String password, String tenantDomain) {

        this.appCreatorUsername = appCreatorUsername;
        this.appCreatorPassword = appCreatorPassword;
        this.dcrRequestFile1 = dcrRequestFile1;
        this.dcrRequestFile2 = dcrRequestFile2;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
    }

    @DataProvider(name = "oidcSSOConfigProvider")
    private static Object[][] oidcSSOConfigProvider() throws Exception {

        return new Object[][] {
                {
                        "dcr-request-1.json", "dcr-request-2.json", ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_USERNAME,
                        ADMIN_PASSWORD, SUPER_TENANT_DOMAIN
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        httpCommonClient = new HTTPCommonClient();
        oAuth2CommonClient = new OAuth2CommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL),
                tenantDomain);
        ssoCommonClient = new SSOCommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL), tenantDomain);

        // Register OAuth2 application 1.
        HttpResponse response = oAuth2CommonClient
                .createOAuth2Application(dcrRequestFile1, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "OAuth2 Application creation failed. Request File: " + dcrRequestFile1);

        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        // Validate application creation.
        oAuth2CommonClient.validateApplicationCreationResponse(dcrRequestFile1, responseJSON);
        clientId1 = responseJSON.get(CLIENT_ID).toString();
        clientSecret1 = responseJSON.get(CLIENT_SECRET).toString();
        redirectUri1 = ((JSONArray) responseJSON.get(REDIRECT_URIS)).get(0).toString();

        httpCommonClient.consume(response);

        // Register OAuth2 application 2.
        response = oAuth2CommonClient.createOAuth2Application(dcrRequestFile2, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "OAuth2 Application creation failed. Request File: " + dcrRequestFile2);

        responseJSON = httpCommonClient.getJSONFromResponse(response);
        // Validate application creation.
        oAuth2CommonClient.validateApplicationCreationResponse(dcrRequestFile2, responseJSON);
        clientId2 = responseJSON.get(CLIENT_ID).toString();
        clientSecret2 = responseJSON.get(CLIENT_SECRET).toString();
        redirectUri2 = ((JSONArray) responseJSON.get(REDIRECT_URIS)).get(0).toString();

        httpCommonClient.consume(response);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .deleteOAuth2Application(clientId1, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Delete application failed for client id: " + clientId1 + ", Request File: " + dcrRequestFile1);
        httpCommonClient.consume(response);

        response = oAuth2CommonClient.deleteOAuth2Application(clientId2, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Delete application failed for client id: " + clientId2 + ", Request File: " + dcrRequestFile2);
        httpCommonClient.consume(response);

        httpCommonClient.closeHttpClient();
    }

    @Test(description = "4.1.4.3.1")
    public void intiAuthorizeRequestForApp1() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendAuthorizeGet(clientId1, "openid", redirectUri1, OAuth2Constants.ResponseTypes.CODE, null);
        sessionDataKey = ssoCommonClient.getSessionDataKey(response);
        assertNotNull(sessionDataKey, "sessionDataKey parameter value is null.");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.2",
          dependsOnMethods = { "intiAuthorizeRequestForApp1" })
    public void authenticateForApp1() throws Exception {

        HttpResponse response = ssoCommonClient.sendLoginPost(sessionDataKey, username, password);
        consentUrl = ssoCommonClient.getLocationHeader(response);
        assertNotNull(consentUrl, "Location header is null. Invalid consent page url.");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.3",
          dependsOnMethods = { "authenticateForApp1" })
    public void initOAuthConsentForApp1() throws Exception {

        HttpResponse response = httpCommonClient.sendGetRequest(consentUrl, null, null);
        sessionDataKeyConsent = ssoCommonClient.getSessionDataKeyConsent(response);
        assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent parameter value is null");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.4",
          dependsOnMethods = { "initOAuthConsentForApp1" })
    public void submitOAuthConsentForApp1() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendOAuthConsentApprovePost(sessionDataKeyConsent, SSOConstants.ApprovalType.APPROVE_ONCE);
        authorizeCode = oAuth2CommonClient.getAuthorizeCode(response);
        assertNotNull(authorizeCode, "code parameter value is null. Invalid authorization code.");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.5",
          dependsOnMethods = { "submitOAuthConsentForApp1" })
    public void getOAccessTokenForApp1() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendCodeGrantTokenRequest(authorizeCode, redirectUri1, clientId1, clientSecret1, null);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateAccessToken(responseJSON, false);
        accessToken = responseJSON.get(OAuth2Constants.TokenResponseElements.ACCESS_TOKEN).toString();

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.6",
          dependsOnMethods = { "getOAccessTokenForApp1" })
    public void introspectAccessTokenForApp1() throws Exception {

        HttpResponse response = oAuth2CommonClient.sendIntrospectRequest(accessToken, username, password);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateIntrospectResponse(responseJSON);

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.7",
          dependsOnMethods = { "introspectAccessTokenForApp1" })
    public void intiAuthorizeRequestForApp2() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendAuthorizeGet(clientId2, "openid", redirectUri2, OAuth2Constants.ResponseTypes.CODE, null);
        sessionDataKeyConsent = ssoCommonClient.getSessionDataKeyConsent(response);
        assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent parameter value is null");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.8",
          dependsOnMethods = { "intiAuthorizeRequestForApp2" })
    public void submitOAuthConsentForApp2() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendOAuthConsentApprovePost(sessionDataKeyConsent, SSOConstants.ApprovalType.APPROVE_ONCE);
        authorizeCode = oAuth2CommonClient.getAuthorizeCode(response);
        assertNotNull(authorizeCode, "code parameter value is null. Invalid authorization code.");

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.9",
          dependsOnMethods = "submitOAuthConsentForApp2")
    public void getOAccessTokenForApp2() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendCodeGrantTokenRequest(authorizeCode, redirectUri2, clientId2, clientSecret2, null);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateAccessToken(responseJSON, false);
        accessToken = responseJSON.get(OAuth2Constants.TokenResponseElements.ACCESS_TOKEN).toString();

        httpCommonClient.consume(response);
    }

    @Test(description = "4.1.4.3.10",
          dependsOnMethods = "getOAccessTokenForApp2")
    public void introspectAccessTokenForApp2() throws Exception {

        HttpResponse response = oAuth2CommonClient.sendIntrospectRequest(accessToken, username, password);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateIntrospectResponse(responseJSON);

        httpCommonClient.consume(response);
    }
}
