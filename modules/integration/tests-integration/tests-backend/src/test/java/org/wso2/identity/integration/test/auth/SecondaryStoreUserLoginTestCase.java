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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This class contains test case for authentication of users in both primary and secondary user stores.
 */

public class SecondaryStoreUserLoginTestCase extends OIDCAbstractIntegrationTest {

    private OIDCApplication playgroundApp;
    protected HttpClient client;
    protected List<NameValuePair> consentParameters = new ArrayList<>();
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected static String primUsername = "primaryUsername";
    protected static String primPassword = "primaryPassword";
    protected static String secUsername = "secondaryUsername";
    protected static String secPassword = "secondaryPassword";
    private static final String primaryUserRole = "jdbcUserStoreRole";
    private static final String secondaryUserRole = "WSO2TEST.COM/jdbcUserStoreRole";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private static final UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private UserManagementClient userMgtClient;
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";
    private Tomcat tomcat;
    public static final int TOMCAT_PORT = 8490;
    public static final String playgroundAppName = "playground.app";
    public static final String playgroundAppContext = "/playground.app";
    public static final String playgroundAppCallBackUri = "http://localhost:" + TOMCAT_PORT + "/playground" + "" +
            ".app/oauth2client";
    public static final String targetApplicationUrl = "http://localhost:" + TOMCAT_PORT + "%s";
    public static final String appUserAuthorizePath = "/playground2/oauth2-authorize-user.jsp";
    private static final Log log = LogFactory.getLog(OIDCAbstractIntegrationTest.class);
    private static final Log LOG = LogFactory.getLog(TomcatInitializerTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        CookieStore cookieStore = new BasicCookieStore();

//      Register a secondary user store
        userStoreConfigAdminServiceClient =
                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        try {
            UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(JDBC_CLASS, DOMAIN_ID,
                    userStoreConfigUtils.getJDBCUserStoreProperties(USER_STORE_DB_NAME));
            userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
            Thread.sleep(5000);
        } catch (Exception e) {
            log.error(e);
        }
        boolean isSecondaryUserStoreDeployed = userStoreConfigUtils.waitForUserStoreDeployment(
                userStoreConfigAdminServiceClient, DOMAIN_ID);

        if (isSecondaryUserStoreDeployed) {

//          Creating users in the primary and secondary user stores
            try{
                addUserIntoJDBCUserStore(primUsername, primPassword);
                addUserIntoJDBCUserStore(secUsername, secPassword);
            } catch (Exception e) {
                log.error(e);
            }

//          Creating and starting application on tomcat
            initAndCreatePlaygroundApplication();
            startTomcat();
            client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

//          To generate sessionDataKey
            try {
                testSendAuthenticationRequest(playgroundApp, client);
            } catch (Exception e) {
                log.error(e);
            }
        }
        else {
            log.error("Secondary user store is not deployed");
        }
    }
    @DataProvider(name = "userCredentialProvider")
    public static Object[][] userCredentialProvider() {

        return new Object[][]{
                {primUsername, primPassword},
                {secUsername, secPassword},
        };
    }
    @Test(groups = "wso2.is", description = "Check the secondary user store user login flow",
            dataProvider = "userCredentialProvider")
    public void testUserLogin(String username, String password){

        try {
            testAuthentication(playgroundApp, username, password);
        } catch (Exception e) {
            log.error("Error: " + e);
        }
    }
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        stopTomcat();
        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
        userMgtClient.deleteRole(primaryUserRole);
        userMgtClient.deleteUser(primUsername);
        deleteApplication(playgroundApp);
        clear();
    }
    private void testAuthentication (OIDCApplication application, String username, String password)
            throws IOException {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, username, password);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName()
                + ". response " + "is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for "
                + application.getApplicationName());
    }
    public void testSendAuthenticationRequest(OIDCApplication application, HttpClient client)
            throws Exception {

        List<NameValuePair> urlParameters = OIDCUtilTest.getNameValuePairs(application);
        application.setApplicationContext("");

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (targetApplicationUrl, application.getApplicationContext() + appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for "
                + application.getApplicationName() + ". " + "Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization request failed for "
                + application.getApplicationName() + ". Authorized response header is null");

        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for "
                + application.getApplicationName() + ". " + "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null for "
                + application.getApplicationName());

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for "
                + application.getApplicationName());

        EntityUtils.consume(response.getEntity());
    }
    protected void initAndCreatePlaygroundApplication() throws Exception {

        playgroundApp = new OIDCApplication(playgroundAppName,
                playgroundAppContext,
                playgroundAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.lastNameClaimUri);
        ServiceProvider serviceProvider = new ServiceProvider();
        createApplication(serviceProvider, playgroundApp);
    }
    public void addUserIntoJDBCUserStore(String username, String password) throws Exception {

        if (Objects.equals(username, secUsername)) {
            userMgtClient.addRole(secondaryUserRole, null, new String[]{PERMISSION_LOGIN});
            Assert.assertTrue(userMgtClient.roleNameExists(secondaryUserRole),
                    "Role name doesn't exist");

            userMgtClient.addUser(DOMAIN_ID + "/" + username, password, new String[]{secondaryUserRole}, null);
            Assert.assertTrue(userMgtClient.userNameExists(secondaryUserRole, DOMAIN_ID + "/" + username),
                    "User name doesn't exist");
        } else {
            userMgtClient.addRole(primaryUserRole, null, new String[]{PERMISSION_LOGIN});
            Assert.assertTrue(userMgtClient.roleNameExists(primaryUserRole),
                    "Role name doesn't exist");

            userMgtClient.addUser(username, password, new String[]{primaryUserRole}, null);
            Assert.assertTrue(userMgtClient.userNameExists(primaryUserRole, username),
                    "User name doesn't exist");
        }
    }
    private void startTomcat() throws LifecycleException, NullPointerException {

        tomcat = Utils.getTomcat(getClass());
        URL resourceUrl = getClass().getResource("/samples/" + "playground2" + ".war");
        Assert.assertNotNull(resourceUrl, "resourceUrl is null");
        tomcat.addWebapp(tomcat.getHost(), "/" + "playground2", resourceUrl.getPath());
        LOG.info("Deployed tomcat application " + "playground2");
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            LOG.error("Error while starting tomcat server ", e);
            throw e;
        }
        LOG.info("Tomcat server started.");
    }
    private void stopTomcat() throws LifecycleException {

        tomcat.stop();
        tomcat.destroy();
        LOG.info("Tomcat server stopped.");
    }
}