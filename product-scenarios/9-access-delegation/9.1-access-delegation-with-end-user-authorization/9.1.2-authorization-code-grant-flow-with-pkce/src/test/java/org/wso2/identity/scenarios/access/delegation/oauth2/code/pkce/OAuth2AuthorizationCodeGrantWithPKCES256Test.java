/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.access.delegation.oauth2.code.pkce;

import org.apache.commons.codec.binary.Base64;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.CLIENT_ID;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.CLIENT_SECRET;
import static org.wso2.identity.scenarios.commons.util.OAuth2Constants.DCRResponseElements.REDIRECT_URIS;

/**
 * This test class tests the access token retrieval using authorization code grant flow with PKCE and validate the
 * access token.
 */
public class OAuth2AuthorizationCodeGrantWithPKCES256Test extends ScenarioTestBase {

    private String dcrRequestFile;

    private String appCreatorUsername;

    private String appCreatorPassword;

    private String username;

    private String password;

    private String tenantDomain;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String sessionDataKey;

    private String sessionDataKeyConsent;

    private String consentUrl;

    private String authorizeCode;

    private String accessToken;

    private String pkceVerifier;

    private HTTPCommonClient httpCommonClient;

    private OAuth2CommonClient oAuth2CommonClient;

    private SSOCommonClient ssoCommonClient;

    @Factory(dataProvider = "oAuth2AuthorizationCodeGrantWithPKCES256ConfigProvider")
    public OAuth2AuthorizationCodeGrantWithPKCES256Test(String dcrRequestFile, String appCreatorUsername,
            String appCreatorPassword, String username, String password, String tenantDomain) {

        this.appCreatorUsername = appCreatorUsername;
        this.appCreatorPassword = appCreatorPassword;
        this.dcrRequestFile = dcrRequestFile;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
    }

    @DataProvider(name = "oAuth2AuthorizationCodeGrantWithPKCES256ConfigProvider")
    private static Object[][] oAuth2AuthorizationCodeGrantWithPKCES256ConfigProvider() throws Exception {

        return new Object[][] {
                {
                        "dcr-request-1.json", ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_USERNAME, ADMIN_PASSWORD,
                        SUPER_TENANT_DOMAIN
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        httpCommonClient = new HTTPCommonClient();
        oAuth2CommonClient = new OAuth2CommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL),
                tenantDomain);
        ssoCommonClient = new SSOCommonClient(httpCommonClient, getDeploymentProperty(IS_HTTPS_URL), tenantDomain);

        // Register OAuth2 application.
        HttpResponse response = oAuth2CommonClient
                .createOAuth2Application(dcrRequestFile, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "OAuth2 Application creation failed. Request File: " + dcrRequestFile);

        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        // Validate application creation.
        oAuth2CommonClient.validateApplicationCreationResponse(dcrRequestFile, responseJSON);

        clientId = responseJSON.get(CLIENT_ID).toString();
        clientSecret = responseJSON.get(CLIENT_SECRET).toString();
        redirectUri = ((JSONArray) responseJSON.get(REDIRECT_URIS)).get(0).toString();

        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .deleteOAuth2Application(clientId, appCreatorUsername, appCreatorPassword);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Delete application failed for client id: " + clientId + ", Request File: " + dcrRequestFile);

        httpCommonClient.closeHttpClient();
    }

    @Test(description = "9.1.2.1")
    public void intiAuthorizeRequest() throws Exception {

        pkceVerifier = getPKCECodeVerifier();
        Map<String, String> params = new HashMap<>();
        params.put(OAuth2Constants.PKCERequestElements.CODE_CHALLENGE, getPKCECodeChallenge(pkceVerifier));
        params.put(OAuth2Constants.PKCERequestElements.CODE_CHALLENGE_METHOD, "S256");

        HttpResponse response = oAuth2CommonClient
                .sendAuthorizeGet(clientId, null, redirectUri, OAuth2Constants.ResponseTypes.CODE, params);
        sessionDataKey = ssoCommonClient.getSessionDataKey(response);
        assertNotNull(sessionDataKey, "sessionDataKey parameter value is null.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.2.2",
          dependsOnMethods = "intiAuthorizeRequest")
    public void authenticate() throws Exception {

        HttpResponse response = ssoCommonClient.sendLoginPost(sessionDataKey, username, password);
        consentUrl = ssoCommonClient.getLocationHeader(response);
        assertNotNull(consentUrl, "Location header is null. Invalid consent page url.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.2.3",
          dependsOnMethods = "authenticate")
    public void initOAuthConsent() throws Exception {

        HttpResponse response = httpCommonClient.sendGetRequest(consentUrl, null, null);
        sessionDataKeyConsent = ssoCommonClient.getSessionDataKeyConsent(response);
        assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent parameter value is null");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.2.4",
          dependsOnMethods = "initOAuthConsent")
    public void submitOAuthConsent() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendOAuthConsentApprovePost(sessionDataKeyConsent, SSOConstants.ApprovalType.APPROVE_ONCE);
        authorizeCode = oAuth2CommonClient.getAuthorizeCode(response);
        assertNotNull(authorizeCode, "code parameter value is null. Invalid authorization code.");

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.2.5",
          dependsOnMethods = "submitOAuthConsent")
    public void getOAccessToken() throws Exception {

        HttpResponse response = oAuth2CommonClient
                .sendCodeGrantTokenRequest(authorizeCode, redirectUri, clientId, clientSecret, pkceVerifier);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateAccessToken(responseJSON, true);
        accessToken = responseJSON.get(OAuth2Constants.TokenResponseElements.ACCESS_TOKEN).toString();

        httpCommonClient.consume(response);
    }

    @Test(description = "9.1.2.6",
          dependsOnMethods = "getOAccessToken")
    public void introspectAccessToken() throws Exception {

        HttpResponse response = oAuth2CommonClient.sendIntrospectRequest(accessToken, username, password);
        JSONObject responseJSON = httpCommonClient.getJSONFromResponse(response);
        oAuth2CommonClient.validateIntrospectResponse(responseJSON);

        httpCommonClient.consume(response);
    }

    /**
     * Generates PKCE code verifier.
     *
     * @return PKCE code verifier.
     */
    private String getPKCECodeVerifier() {

        return (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replaceAll("-", "");
    }

    /**
     * Generates PKCE code challenge.
     *
     * @param codeVerifier PKCE code verifier.
     * @return Code challenge.
     * @throws NoSuchAlgorithmException No Such Algorithm Exception.
     */
    private String getPKCECodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        //Base64 encoded string is trimmed to remove trailing CR LF
        return new String(Base64.encodeBase64URLSafe(hash), StandardCharsets.UTF_8).trim();
    }
}
