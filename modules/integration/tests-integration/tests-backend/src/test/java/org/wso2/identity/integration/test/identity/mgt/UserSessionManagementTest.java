/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.identity.integration.test.identity.mgt;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.mgt.UserIdentityManagementAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This test class is used to test user session termination functionality during password reset and user deletion.
 */
public class UserSessionManagementTest extends OIDCAbstractIntegrationTest {

    private UserManagementClient userMgtClient = null;
    private UserIdentityManagementAdminServiceClient userIdentityManagementAdminServiceClient = null;

    private static final String username = "sessiontestuser";
    private static final String password = "sessiontestuser";
    private static final String newPassword = "newsessiontestuser";
    private static final String role = "internal/everyone";
    private static final String profile = "default";

    private static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";

    private static final String playgroundAppName = "playground.usersession";
    private static final String playgroundAppCallBackUri = "http://localhost:" + TOMCAT_PORT +
            "/playground.usersession" + "" + "/oauth2client";
    private static final String playgroundAppContext = "/playground.usersession";

    private CookieStore cookieStore = new BasicCookieStore();
    private OIDCApplication playgroundApp;
    private String sessionDataKey;
    private HttpClient client;
    private String contentData;

    private String clientID;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        userIdentityManagementAdminServiceClient = new UserIdentityManagementAdminServiceClient(backendURL,
                sessionCookie);
        String[] roleList = {role};
        userMgtClient.addUser(username, password, roleList, profile);

        playgroundApp = new OIDCApplication(playgroundAppName, playgroundAppContext,
                playgroundAppCallBackUri);
        createApplication(playgroundApp);

        clientID = playgroundApp.getClientId();

        startTomcat();
        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" +
                ISIntegrationTest.URL_SEPARATOR + playgroundAppName + "" + ".war");
        tomcat.addWebapp(tomcat.getHost(), playgroundApp.getApplicationContext(), resourceUrl.getPath());

        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication(playgroundApp);
        stopTomcat();
        userMgtClient = null;
        userIdentityManagementAdminServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Test session termination while password reset.", priority = 1)
    public void testSessionTerminationWhenPasswordReset() throws Exception {

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertTrue(contentData.contains("login.do"), "Passive authentication request of an unauthorized " +
                "client did not redirected to the login page.");

        sendAuthenticationRequest();
        loginUser(username, password);

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertFalse(contentData.contains("login.do"), "Passive authentication request of an authorized " +
                "client redirected to the login page.");

        userIdentityManagementAdminServiceClient.resetUserPassword(username, newPassword);

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertTrue(contentData.contains("login.do"), "User has an active session after password reset.");
    }

    @Test(alwaysRun = true, description = "Test session termination while user deletion.", priority = 2)
    public void testSessionTerminationWhenUserDeletion() throws Exception {

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertTrue(contentData.contains("login.do"), "Passive authentication request of an unauthorized " +
                "client did not redirected to the login page.");

        sendAuthenticationRequest();
        loginUser(username, newPassword);

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertFalse(contentData.contains("login.do"), "Passive authentication request of an authorized " +
                "client redirected to the login page.");

        userMgtClient.deleteUser(username);

        contentData = sendPassiveAuthenticationRequest();
        Assert.assertTrue(contentData.contains("login.do"), "User has an active session after deletion.");
    }

    private String sendPassiveAuthenticationRequest() throws IOException, URISyntaxException {

        URI uri = new URIBuilder(OAuth2Constant.APPROVAL_URL)
                .addParameter("client_id", clientID)
                .addParameter("scope", "openid")
                .addParameter("response_type", "code")
                .addParameter("redirect_uri", playgroundAppCallBackUri).build();

        HttpResponse httpResponse = sendGetRequest(client, uri.toString());
        String responseContentData = DataExtractUtil.getContentData(httpResponse);
        EntityUtils.consume(httpResponse.getEntity());
        return responseContentData;
    }

    private void sendAuthenticationRequest() throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", clientID));
        urlParameters.add(new BasicNameValuePair("callbackurl", playgroundAppCallBackUri));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (targetApplicationUrl, playgroundAppContext + OAuth2Constant.PlaygroundAppPaths
                        .appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for " + playgroundAppName + ". "
                + "Authorized response is null");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        Assert.assertNotNull(locationHeader, "Authorization request failed for " + playgroundAppName +
                ". Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " + playgroundAppName + ". "
                + "Authorized user response is null.");
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for " + playgroundAppName);

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + playgroundAppName);
        EntityUtils.consume(response.getEntity());
    }


    private void loginUser(String username, String password) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);
        Assert.assertNotNull(response, "Login request failed for " + playgroundAppName + ". response "
                + "is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " + playgroundAppName);
        EntityUtils.consume(response.getEntity());
    }
}
