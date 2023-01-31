/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.auth;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.base.TomcatInitializerTestCase;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTH_CODE_BODY_ELEMENT;

/**
 * This class contains test case for authentication of users in both primary and secondary user stores.
 */

public class SecondaryStoreUserLoginTestCase extends OIDCAbstractIntegrationTest {

    public static final int TOMCAT_PORT = 8490;
    public static final String PLAYGROUND_APP_NAME = "playground.app";
    public static final String PLAYGROUND_APP_CONTEXT = "/playground.app";
    public static final String PLAYGROUND_APP_CALLBACK_URI = "http://localhost:" + TOMCAT_PORT +
            "/playground.app/oauth2client";
    private static final String PRIMARY_USERNAME = "primaryUsername";
    private static final String PRIMARY_PASSWORD = "primaryPassword";
    private static final String SECONDARY_USERNAME = "secondaryUsername";
    private static final String SECONDARY_PASSWORD = "secondaryPassword";
    private static final UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String PRIMARY_USER_ROLE = "jdbcUserStoreRole";
    private static final String SECONDARY_USER_ROLE = DOMAIN_ID + "/" + "jdbcUserStoreRole";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";
    private static final Log log = LogFactory.getLog(TomcatInitializerTestCase.class);
    private OIDCApplication playgroundApp;
    private HttpClient client;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserManagementClient userMgtClient;
    private Tomcat tomcat;
    private String clientID;

    @DataProvider(name = "userCredentialProvider")
    public static Object[][] userCredentialProvider() {

        return new Object[][]{{PRIMARY_USERNAME, PRIMARY_PASSWORD}, {SECONDARY_USERNAME, SECONDARY_PASSWORD}};
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        // Register a secondary user store
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(JDBC_CLASS, DOMAIN_ID,
                userStoreConfigUtils.getJDBCUserStoreProperties(USER_STORE_DB_NAME));
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        boolean isSecondaryUserStoreDeployed = userStoreConfigUtils.waitForUserStoreDeployment(
                userStoreConfigAdminServiceClient, DOMAIN_ID);
        Assert.assertTrue(isSecondaryUserStoreDeployed);
        // Creating users in the primary and secondary user stores
        addUserIntoJDBCUserStore(PRIMARY_USERNAME, PRIMARY_PASSWORD, false);
        addUserIntoJDBCUserStore(SECONDARY_USERNAME, SECONDARY_PASSWORD, true);
        // Creating, registering and starting application on tomcat
        createAndRegisterPlaygroundApplication();
        startTomcat();
    }

    @Test(groups = "wso2.is", description = "Check the secondary user store user login flow",
            dataProvider = "userCredentialProvider")
    public void testUserLogin(String username, String password) throws Exception {

        CookieStore cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        // Sending authorization request to IS
        sendAuthorizedPost();
        // User (resource owner) authentication
        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, username, password);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(2);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        keyPositionMap.put(AUTH_CODE_BODY_ELEMENT, 2);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        if (!AUTH_CODE_BODY_ELEMENT.equals(keyValues.get(0).getKey())) {
            sessionDataKeyConsent = keyValues.get(0).getValue();
            EntityUtils.consume(response.getEntity());
            // Authorization
            testSendApprovalPost();
        } else {
            String authorizationCode = keyValues.get(0).getValue();
            Assert.assertNotNull(authorizationCode, "Authorization code is null.");
            EntityUtils.consume(response.getEntity());
        }
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        stopTomcat();
        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
        userMgtClient.deleteUser(PRIMARY_USERNAME);
        userMgtClient.deleteUser(DOMAIN_ID + "/" + SECONDARY_USERNAME);
        deleteApplication(playgroundApp);
        clear();
    }

    private void sendAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", clientID));
        urlParameters.add(new BasicNameValuePair("callbackurl", PLAYGROUND_APP_CALLBACK_URI));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", ""));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorized response is null");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    private void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        String locationHeaderValue = locationHeader.getValue();
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");
        String authorizationCodeString = StringUtils.substringAfterLast(locationHeaderValue, "?code=");
        // Assuring that the authorization code is received, which confirms that the login is successful
        Assert.assertNotNull(authorizationCodeString, "Authorization code not present, hence login unsuccessful.");
        EntityUtils.consume(response.getEntity());
    }

    private void createAndRegisterPlaygroundApplication() throws Exception {

        playgroundApp = new OIDCApplication(PLAYGROUND_APP_NAME, PLAYGROUND_APP_CONTEXT, PLAYGROUND_APP_CALLBACK_URI);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        ServiceProvider serviceProvider = new ServiceProvider();
        createApplication(serviceProvider, playgroundApp);
        clientID = playgroundApp.getClientId();
    }

    private void addUserIntoJDBCUserStore(String username, String password, boolean isSecondaryStoreUser)
            throws Exception {

        if (isSecondaryStoreUser) {
            userMgtClient.addRole(SECONDARY_USER_ROLE, null, new String[]{PERMISSION_LOGIN});
            Assert.assertTrue(userMgtClient.roleNameExists(SECONDARY_USER_ROLE), "Role name doesn't exist");
            userMgtClient.addUser(DOMAIN_ID + "/" + username, password, new String[]{SECONDARY_USER_ROLE}, null);
            Assert.assertTrue(userMgtClient.userNameExists(SECONDARY_USER_ROLE, DOMAIN_ID + "/" + username),
                    "User is not created.");
        } else {
            userMgtClient.addRole(PRIMARY_USER_ROLE, null, new String[]{PERMISSION_LOGIN});
            Assert.assertTrue(userMgtClient.roleNameExists(PRIMARY_USER_ROLE), "Role name doesn't exist");
            userMgtClient.addUser(username, password, new String[]{PRIMARY_USER_ROLE}, null);
            Assert.assertTrue(userMgtClient.userNameExists(PRIMARY_USER_ROLE, username), "User is not created.");
        }
    }

    private void startTomcat() throws LifecycleException, NullPointerException {

        tomcat = Utils.getTomcat(getClass());
        URL resourceUrl = getClass().getResource("/samples/playground2.war");
        Assert.assertNotNull(resourceUrl, "resourceUrl is null");
        tomcat.addWebapp(tomcat.getHost(), "/" + "playground2", resourceUrl.getPath());
        log.info("Deployed tomcat application " + "playground2");
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            log.error("Error while starting tomcat server ", e);
            throw e;
        }
        log.info("Tomcat server started.");
    }

    private void stopTomcat() throws LifecycleException {

        tomcat.stop();
        tomcat.destroy();
        log.info("Tomcat server stopped.");
    }
}
