/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oidc;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * This test class tests OIDC SSO functionality for two replying party applications
 */
public class OIDCAuthCodeGrantSSOTestCase extends OIDCAbstractIntegrationTest {

    public static final String username = "oidcsessiontestuser";
    public static final String password = "oidcsessiontestuser";
    public static final String email = "oidcsessiontestuser@wso2.com";
    public static final String firstName = "oidcsessiontestuser-first";
    public static final String lastName = "oidcsessiontestuser-last";
    public static final String role = "internal/everyone";
    public static final String profile = "default";

    public static final String playgroundAppOneAppName = "playground.appone";
    public static final String playgroundAppOneAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".appone/oauth2client";
    public static final String playgroundAppOneAppContext = "/playground.appone";

    public static final String playgroundAppTwoAppName = "playground.apptwo";
    public static final String playgroundAppTwoAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".apptwo/oauth2client";
    public static final String playgroundAppTwoAppContext = "/playground.apptwo";

    public static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";

    public static final String emailClaimUri = "http://wso2.org/claims/emailaddress";
    public static final String firstNameClaimUri = "http://wso2.org/claims/givenname";
    public static final String lastNameClaimUri = "http://wso2.org/claims/lastname";

    protected OIDCUser user;
    protected Map<String, OIDCApplication> applications = new HashMap<>(2);

    protected String accessToken;
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected String authorizationCode;

    protected HttpClient client;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        initUser();
        createUser(user);

        initApplications();
        createApplications();

        startTomcat();
        deployApplications();

        client = HttpClientBuilder.create().build();

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplications();

        stopTomcat();

        appMgtclient = null;
        remoteUSMServiceClient = null;
        adminClient = null;

        client = null;

    }

    @Test(groups = "wso2.is", description = "Test authz endpoint before creating a valid session")
    public void testAuthzRequestWithoutValidSessionForIDENTITY5581() throws Exception {

        //When accessing the below endpoint from with invalid session it should provide a message with login_required
        OIDCApplication application = applications.get(playgroundAppOneAppName);
        URI uri = new URIBuilder(OAuth2Constant.APPROVAL_URL)
                .addParameter("client_id", application.getClientId())
                .addParameter("scope", "openid")
                .addParameter("response_type", "code")
                .addParameter("prompt", "none")
                .addParameter("redirect_uri", application.getCallBackURL()).build();
        HttpResponse httpResponse = sendGetRequest(client, uri.toString());
        String contentData = DataExtractUtil.getContentData(httpResponse);
        Assert.assertTrue(contentData.contains("login_required"));
        EntityUtils.consume(httpResponse.getEntity());
    }


    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.appone",dependsOnMethods = "testAuthzRequestWithoutValidSessionForIDENTITY5581")
    public void testSendAuthenticationRequestFromRP1() throws Exception {

        testSendAuthenticationRequest(applications.get(playgroundAppOneAppName), true);
    }

    @Test(groups = "wso2.is", description = "Authenticate for playground.appone", dependsOnMethods =
            "testSendAuthenticationRequestFromRP1")
    public void testAuthenticationFromRP1() throws Exception {

        testAuthentication(applications.get(playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Approve consent for playground.appone", dependsOnMethods =
            "testAuthenticationFromRP1")
    public void testConsentApprovalFromRP1() throws Exception {

        testConsentApproval(applications.get(playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Get access token for playground.appone", dependsOnMethods =
            "testConsentApprovalFromRP1")
    public void testGetAccessTokenFromRP1() throws Exception {

        testGetAccessToken(applications.get(playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Get user claim values for playground.appone", dependsOnMethods =
            "testGetAccessTokenFromRP1")
    public void testUserClaimsFromRP1() throws Exception {

        testUserClaims();
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.apptwo")
    public void testSendAuthenticationRequestFromRP2() throws Exception {

        testSendAuthenticationRequest(applications.get(playgroundAppTwoAppName), false);
    }

    @Test(groups = "wso2.is", description = "Approve consent for playground.apptwo", dependsOnMethods =
            "testSendAuthenticationRequestFromRP2")
    public void testConsentApprovalFromRP2() throws Exception {

        testConsentApproval(applications.get(playgroundAppTwoAppName));
    }

    @Test(groups = "wso2.is", description = "Get access token for playground.apptwo", dependsOnMethods =
            "testConsentApprovalFromRP2")
    public void testGetAccessTokenFromRP2() throws Exception {

        testGetAccessToken(applications.get(playgroundAppTwoAppName));
    }

    @Test(groups = "wso2.is", description = "Get user claim values for playground.apptwo", dependsOnMethods =
            "testGetAccessTokenFromRP2")
    public void testUserClaimsFromRP2() throws Exception {

        testUserClaims();
    }

    private void testSendAuthenticationRequest(OIDCApplication application, boolean isFirstAuthenticationRequest)
            throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant
                .OAUTH2_SCOPE_EMAIL));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (targetApplicationUrl, application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths
                        .appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() + ". "
                + "Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        Assert.assertNotNull(locationHeader, "Authorization request failed for " + application.getApplicationName() +
                ". Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() + ". "
                + "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        if (isFirstAuthenticationRequest) {
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            Assert.assertNotNull(keyValues, "sessionDataKey key value is null for " + application.getApplicationName());

            sessionDataKey = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + application.getApplicationName());
        } else {
            keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse
                    (response, keyPositionMap);
            Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null for " + application
                    .getApplicationName());

            sessionDataKeyConsent = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKeyConsent, "Invalid sessionDataKeyConsent for " + application
                    .getApplicationName());
        }

        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() + ". response "
                + "is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie =response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT , Utils.getRedirectUrl
                    (response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }
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

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed for " + application.getApplicationName() + ". " +
                "response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed for " + application.getApplicationName() + ". "
                + "Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization code response is invalid for " + application.getApplicationName
                ());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Authorization code not received for " + application.getApplicationName());

        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code not received for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    private void testGetAccessToken(OIDCApplication application) throws Exception {

        HttpResponse response = sendGetAccessTokenPost(client, application);
        Assert.assertNotNull(response, "Access token response is invalid for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, String.format(targetApplicationUrl, application.getApplicationContext() +
                OAuth2Constant.PlaygroundAppPaths.appAuthorizePath));

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractInputValueFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token not received for " + application.getApplicationName());

        accessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(accessToken, "Access token not received for " + application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, String.format(targetApplicationUrl, application.getApplicationContext() +
                OAuth2Constant.PlaygroundAppPaths.appAuthorizePath));

        keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("id=\"loggedUser\"", 1);
        keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "No user logged in for " + application.getApplicationName());

        String loggedUser = keyValues.get(0).getValue();
        Assert.assertNotNull(loggedUser, "Logged user is null for " + application.getApplicationName());
        Assert.assertNotEquals(loggedUser, "null", "Logged user is null for " + application.getApplicationName());
        Assert.assertNotEquals(loggedUser, "", "Logged user is null for " + application.getApplicationName());
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
        Assert.assertEquals(this.email, email, "Incorrect email claim value");
    }

    protected void initUser() throws Exception {

        user = new OIDCUser(username, password);
        user.setProfile(profile);
        user.addUserClaim(emailClaimUri, email);
        user.addUserClaim(firstNameClaimUri, firstName);
        user.addUserClaim(lastNameClaimUri, lastName);
        user.addRole(role);
    }

    protected void initApplications() throws Exception {

        OIDCApplication playgroundApp = new OIDCApplication(playgroundAppOneAppName, playgroundAppOneAppContext,
                playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playgroundAppOneAppName, playgroundApp);

        playgroundApp = new OIDCApplication(playgroundAppTwoAppName, playgroundAppTwoAppContext,
                playgroundAppTwoAppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playgroundAppTwoAppName, playgroundApp);
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

    protected void deployApplications() {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator + entry.getKey() +
                    "" + ".war");
            tomcat.addWebapp(tomcat.getHost(), entry.getValue().getApplicationContext(), resourceUrl.getPath());
        }
    }

    protected HttpResponse sendGetAccessTokenPost(HttpClient client, OIDCApplication application) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("accessEndpoint", OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        urlParameters.add(new BasicNameValuePair("consumerSecret", application.getClientSecret()));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (targetApplicationUrl, application.getApplicationContext() + OAuth2Constant.PlaygroundAppPaths
                        .accessTokenRequestPath));

        return response;
    }

    @Override
    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", user.getUsername()));
        urlParameters.add(new BasicNameValuePair("password", user.getPassword()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);

        return response;
    }
}
