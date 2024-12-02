/*
 * Copyright (c) 2016, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.base.MockApplicationServer;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * This test class tests OIDC SSO functionality for two replying party applications.
 */
public class OIDCAuthCodeGrantSSOTestCase extends OIDCAbstractIntegrationTest {

    protected UserObject user;
    protected Map<String, OIDCApplication> applications = new HashMap<>(2);

    protected String accessToken;
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected String authorizationCode;

    CookieStore cookieStore = new BasicCookieStore();

    protected Lookup<CookieSpecProvider> cookieSpecRegistry;
    protected RequestConfig requestConfig;
    protected HttpClient client;
    protected List<NameValuePair> consentParameters = new ArrayList<>();
    private MockApplicationServer mockApplicationServer;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        initUser();
        createUser(user);

        initApplications();
        createApplications();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore)
                .build();

        mockApplicationServer = new MockApplicationServer();
        mockApplicationServer.start();

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplications();
        clear();
        mockApplicationServer.stop();
    }

    @Test(groups = "wso2.is", description = "Test authz endpoint before creating a valid session")
    public void testAuthzRequestWithoutValidSessionForIDENTITY5581() throws Exception {

        //When accessing the below endpoint from with invalid session it should provide a message with login_required
        OIDCApplication application = applications.get(OIDCUtilTest.playgroundAppOneAppName);
        URI uri = new URIBuilder(OAuth2Constant.APPROVAL_URL)
                .addParameter("client_id", application.getClientId())
                .addParameter("scope", "openid")
                .addParameter("response_type", "code")
                .addParameter("prompt", "none")
                .addParameter("redirect_uri", application.getCallBackURL()).build();
        HttpResponse httpResponse = sendGetRequest(client, uri.toString());

        EntityUtils.consume(httpResponse.getEntity());
        Assert.assertTrue(mockApplicationServer.getErrorCode(application.getApplicationName()).contains("login_required"));
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.appone",
            dependsOnMethods = "testAuthzRequestWithoutValidSessionForIDENTITY5581")
    public void testSendAuthenticationRequestFromRP1() throws Exception {

        testSendAuthenticationRequest(applications.get(OIDCUtilTest.playgroundAppOneAppName), true, client, cookieStore);
    }

    @Test(groups = "wso2.is", description = "Authenticate for playground.appone", dependsOnMethods =
            "testSendAuthenticationRequestFromRP1")
    public void testAuthenticationFromRP1() throws Exception {

        testAuthentication(applications.get(OIDCUtilTest.playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Approve consent for playground.appone", dependsOnMethods =
            "testAuthenticationFromRP1")
    public void testConsentApprovalFromRP1() throws Exception {

        testConsentApproval(applications.get(OIDCUtilTest.playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Get access token for playground.appone", dependsOnMethods =
            "testConsentApprovalFromRP1")
    public void testGetAccessTokenFromRP1() throws Exception {

        testGetAccessToken(applications.get(OIDCUtilTest.playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Get user claim values for playground.appone", dependsOnMethods =
            "testGetAccessTokenFromRP1")
    public void testUserClaimsFromRP1() throws Exception {

        testUserClaims();
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.apptwo")
    public void testSendAuthenticationRequestFromRP2() throws Exception {

        testSendAuthenticationRequest(applications.get(OIDCUtilTest.playgroundAppTwoAppName), false, client,
                cookieStore);
    }

    @Test(groups = "wso2.is", description = "Approve consent for playground.apptwo", dependsOnMethods =
            "testSendAuthenticationRequestFromRP2")
    public void testConsentApprovalFromRP2() throws Exception {

        testConsentApproval(applications.get(OIDCUtilTest.playgroundAppTwoAppName));
    }

    @Test(groups = "wso2.is", description = "Get access token for playground.apptwo", dependsOnMethods =
            "testConsentApprovalFromRP2")
    public void testGetAccessTokenFromRP2() throws Exception {

        testGetAccessToken(applications.get(OIDCUtilTest.playgroundAppTwoAppName));
    }

    @Test(groups = "wso2.is", description = "Get user claim values for playground.apptwo", dependsOnMethods =
            "testGetAccessTokenFromRP2")
    public void testUserClaimsFromRP2() throws Exception {

        testUserClaims();
    }

    public void testSendAuthenticationRequest(OIDCApplication application, boolean isFirstAuthenticationRequest,
                                              HttpClient client, CookieStore cookieStore) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", application.getCallBackURL()));

        urlParameters.add(new BasicNameValuePair("scope", "openid email profile"));

        HttpResponse response;
        if (isFirstAuthenticationRequest) {
            response = sendPostRequestWithParameters(client, urlParameters,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(client, locationHeader.getValue());
        } else {
            HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                    .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                    .setDefaultRequestConfig(requestConfig)
                    .disableRedirectHandling()
                    .setDefaultCookieStore(cookieStore).build();
            response = sendPostRequestWithParameters(httpClientWithoutAutoRedirections, urlParameters,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        }

        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName()
                + ". Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        if (isFirstAuthenticationRequest) {
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey key value is null for " + application.getApplicationName());

            sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + application.getApplicationName());

        } else {

            Header consentLocationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

            if (Utils.requestMissingClaims(response)) {

                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                Assert.assertNotNull(consentLocationHeader, "OAuth consent url is null for " +
                        consentLocationHeader.getValue());

                consentParameters.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
                response = sendGetRequest(client, consentLocationHeader.getValue());

                keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
                List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse
                        (response, keyPositionMap);
                Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null for " + application
                        .getApplicationName());

                sessionDataKeyConsent = keyValues.get(0).getValue();
                Assert.assertNotNull(sessionDataKeyConsent, "Invalid sessionDataKeyConsent for " + application
                        .getApplicationName());
            }
        }

        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, user.getUserName(),
                user.getPassword());
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() + ". response "
                + "is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

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
        Assert.assertNotNull(response, "Approval request failed for " + application.getApplicationName() + ". " +
                "response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for " + application.getApplicationName() + ". "
                + "Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization code response is invalid for "
                + application.getApplicationName());

        authorizationCode = mockApplicationServer.getAuthorizationCodeForApp(application.getApplicationName());
        Assert.assertNotNull(authorizationCode, "Authorization code not received for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    private void testGetAccessToken(OIDCApplication application) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientSecret()));

        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(application.getClientId(),
                        application.getClientSecret())));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        String responseString = EntityUtils.toString(response.getEntity());
        Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(responseString);
        accessToken = (String) responseMap.get("access_token");

        String idToken = (String) responseMap.get("id_token");
        String[] tokenParts = idToken.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
        Map<String, Object> parsedIdToken = (Map<String, Object>) JSONValue.parse(payload);
        Assert.assertNotNull(parsedIdToken.get("sub"), "No user logged in for " + application.getApplicationName());

        EntityUtils.consume(response.getEntity());
    }

    private void testUserClaims() throws Exception {

        HttpGet request = new HttpGet(OAuth2Constant.USER_INFO_ENDPOINT);

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String email = ((org.json.simple.JSONObject) obj).get("email").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(OIDCUtilTest.email, email, "Incorrect email claim value");
    }

    protected void initUser() throws Exception {

        user = new UserObject();
        user.setUserName(OIDCUtilTest.username);
        user.setPassword(OIDCUtilTest.password);
        user.setName(new Name().givenName(OIDCUtilTest.firstName).familyName(OIDCUtilTest.lastName));
        user.addEmail(new Email().value(OIDCUtilTest.email));
    }

    protected void initApplications() throws Exception {

        OIDCApplication playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppContext,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        applications.put(OIDCUtilTest.playgroundAppOneAppName, playgroundApp);

        playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppTwoAppName, OIDCUtilTest.playgroundAppTwoAppContext,
                OIDCUtilTest.playgroundAppTwoAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        applications.put(OIDCUtilTest.playgroundAppTwoAppName, playgroundApp);
    }

    protected void createApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            createApplication(entry.getValue());
        }
    }

    protected void deleteApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            deleteApplication(entry.getValue());
        }
    }
}
