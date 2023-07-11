/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.identity.integration.test.oidc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.apache.commons.lang.StringUtils;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test case to test the consent management with OIDC SSO.
 */
public class OIDCSSOConsentTestCase extends OIDCAbstractIntegrationTest {

    protected OIDCUser user;
    protected String accessToken;
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected String authorizationCode;

    CookieStore cookieStore = new BasicCookieStore();

    protected Lookup<CookieSpecProvider> cookieSpecRegistry;
    protected RequestConfig requestConfig;
    protected HttpClient client;
    protected List<NameValuePair> consentParameters = new ArrayList<>();
    OIDCApplication playgroundApp;
    ServiceProvider serviceProvider;
    private String claimsToGetConsent;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        initUser();
        createUser(user);
        userInfo.setUserName(user.getUsername());
        userInfo.setPassword(user.getPassword());

        playgroundApp = initApplication();
        serviceProvider = createApplication(new ServiceProvider(), playgroundApp);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplication(playgroundApp);
        clear();
    }

    @Test(groups = "wso2.is", description = "Test consent management after updating " +
            "the application claim configurations")
    public void testConsentWithAppClaimConfigUpdate() throws Exception {

        testSendAuthenticationRequest(playgroundApp, client);
        testAuthentication(playgroundApp);
        Assert.assertEquals(claimsToGetConsent, "0_Email,1_First Name",
                "Requested claims were not prompted to ask the consent.");
        testConsentApproval(playgroundApp);
        testGetAccessToken(playgroundApp);

        performOIDCLogout();
        updateApplication(playgroundApp, serviceProvider);

        // Login again with updated claim configurations.
        testSendAuthenticationRequest(playgroundApp, client);
        testAuthentication(playgroundApp);
        Assert.assertEquals(claimsToGetConsent, "0_Last Name",
                "Requested claims which were updated after the first login, " +
                        "were not prompted to ask the consent.");
    }

    public void testSendAuthenticationRequest(OIDCApplication application, HttpClient client)
            throws Exception {

        List<NameValuePair> urlParameters = OIDCUtilTest.getNameValuePairs(application);
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (OIDCUtilTest.targetApplicationUrl, application.getApplicationContext() +
                        OAuth2Constant.PlaygroundAppPaths.appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() +
                ". Authorized response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        Assert.assertNotNull(locationHeader, "Authorization request failed for " +
                application.getApplicationName() + ". Authorized response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " +
                application.getApplicationName() + ". Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "The sessionDataKey value is null for " +
                application.getApplicationName());

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + application.getApplicationName());

        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() +
                ". response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " +
                application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        response = httpClientWithoutAutoRedirections.execute(getRequest);

        claimsToGetConsent = claimsToGetConsent(response, userInfo, tenantInfo);
        consentParameters.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        HttpClientBuilder.create().setDefaultCookieStore(cookieStore);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null for " + application
                .getApplicationName());

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid sessionDataKeyConsent for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    private void testConsentApproval(OIDCApplication application) throws Exception {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed for " + application.getApplicationName() + ". "
                + "response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for " + application.getApplicationName()
                + ". Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization code response is invalid for " +
                application.getApplicationName());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received for " +
                application.getApplicationName());

        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code not received for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    private void testGetAccessToken(OIDCApplication application) throws Exception {

        HttpResponse response = sendGetAccessTokenPost(client, application);
        Assert.assertNotNull(response, "Access token response is invalid for " +
                application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, String.format(OIDCUtilTest.targetApplicationUrl,
                application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths.appAuthorizePath));

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractInputValueFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token not received for " + application.getApplicationName());

        accessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(accessToken, "Access token not received for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, String.format(OIDCUtilTest.targetApplicationUrl,
                application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths.appAuthorizePath));

        keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("id=\"loggedUser\"", 1);
        keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "No user logged in for " + application.getApplicationName());

        String loggedUser = keyValues.get(0).getValue();
        Assert.assertNotNull(loggedUser, "Logged user is null for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    protected void initUser() throws Exception {

        user = new OIDCUser(OIDCUtilTest.username, OIDCUtilTest.password);
        user.setProfile(OIDCUtilTest.profile);
        user.addUserClaim(OIDCUtilTest.emailClaimUri, OIDCUtilTest.email);
        user.addUserClaim(OIDCUtilTest.firstNameClaimUri, OIDCUtilTest.firstName);
        user.addUserClaim(OIDCUtilTest.lastNameClaimUri, OIDCUtilTest.lastName);
        user.addRole(OIDCUtilTest.role);
    }

    protected OIDCApplication initApplication() {

        playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppContext,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        return playgroundApp;
    }

    private void updateApplication(OIDCApplication playgroundApp, ServiceProvider serviceProvider) throws Exception {

        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        ClaimConfig claimConfig = new ClaimConfig();
        Claim claim = new Claim();
        claim.setClaimUri(OIDCUtilTest.lastNameClaimUri);
        ClaimMapping claimMapping = new ClaimMapping();
        claimMapping.setRequested(true);
        claimMapping.setLocalClaim(claim);
        claimMapping.setRemoteClaim(claim);
        claimConfig.addClaimMappings(claimMapping);
        serviceProvider.setClaimConfig(claimConfig);
        updateApplication(serviceProvider);
    }

    protected HttpResponse sendGetAccessTokenPost(HttpClient client, OIDCApplication application) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("accessEndpoint", OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        urlParameters.add(new BasicNameValuePair("consumerSecret", application.getClientSecret()));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (OIDCUtilTest.targetApplicationUrl, application.getApplicationContext() +
                        OAuth2Constant.PlaygroundAppPaths.accessTokenRequestPath));

        return response;
    }

    private void performOIDCLogout() {

        try {
            String oidcLogoutUrl = identityContextUrls.getWebAppURLHttps() + "/oidc/logout";
            HttpResponse response = sendGetRequest(client, oidcLogoutUrl);
            EntityUtils.consume(response.getEntity());

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("consent", "approve"));
            response = sendPostRequestWithParameters(client, urlParameters, oidcLogoutUrl);
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(client, locationHeader.getValue());
            Assert.assertNotNull(response, "OIDC Logout failed.");
            String result = DataExtractUtil.getContentData(response);
            Assert.assertTrue(result.contains("You have successfully logged out"), "OIDC logout failed.");
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("OIDC Logout failed.", e);
        }
    }

    public static String claimsToGetConsent(HttpResponse response, User userInfo, Tenant tenantInfo)
            throws Exception {

        String redirectUrl = Utils.getRedirectUrl(response);
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        String mandatoryClaims = queryParams.get("requestedClaims");;
        String requestedClaims = queryParams.get("mandatoryClaims");

        //Get the claims from the data api if the claims are not in the redirect url.
        if (isBlank(requestedClaims) && isBlank(mandatoryClaims)) {
            String sessionDataKeyConsent = queryParams.get("sessionDataKeyConsent");
            HttpResponse dataAPIResponse = Utils.sendDataAPIGetRequest(sessionDataKeyConsent, userInfo,
                    tenantInfo);
            JSONObject jsonObject = new JSONObject(DataExtractUtil.getContentData(dataAPIResponse));

            if (jsonObject.has("mandatoryClaims")) {
                mandatoryClaims = jsonObject.getString("mandatoryClaims");
            }

            if (jsonObject.has("requestedClaims")) {
                requestedClaims = jsonObject.getString("requestedClaims");
            }

        }
        if (StringUtils.isNotEmpty(requestedClaims)) {
            return requestedClaims;
        }
        if (StringUtils.isNotEmpty(mandatoryClaims)) {
            return mandatoryClaims;
        }
        return StringUtils.EMPTY;
    }
}
