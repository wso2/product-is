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
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.base.TomcatInitializerTestCase;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq.Property;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject.MemberItem;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
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
    private static final String PRIMARY_PASSWORD = "Wso2@testPrimary123";
    private static final String SECONDARY_USERNAME = "secondaryUsername";
    private static final String SECONDARY_PASSWORD = "Wso2@testSecondary123";
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String PRIMARY_USER_GROUP = "jdbcUserStoreGroup";
    private static final String PRIMARY_USER_ROLE = "jdbcUserStoreRole";
    private static final String SECONDARY_USER_GROUP = DOMAIN_ID + "/" + "jdbcSecondaryUserStoreGroup";
    private static final String SECONDARY_USER_ROLE = "jdbcSecondaryUserStoreRole";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final Log LOG = LogFactory.getLog(TomcatInitializerTestCase.class);
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private OIDCApplication playgroundApp;
    private HttpClient client;
    private String sessionDataKey;
    private Tomcat tomcat;
    private String clientID;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private String userStoreId;
    private String secondaryUserStoreRoleId;
    private String secondaryUserStoreUserId;
    private String primaryUserStoreUserId;
    private String primaryUserStoreRoleId;
    private String secondaryUserStoreGroupId;
    private String primaryUserStoreGroupId;

    @DataProvider(name = "userCredentialProvider")
    public static Object[][] userCredentialProvider() {

        return new Object[][]{{PRIMARY_USERNAME, PRIMARY_PASSWORD}, {SECONDARY_USERNAME, SECONDARY_PASSWORD}};
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        addSecondaryJDBCUserStore();

        addUserIntoJDBCUserStore(PRIMARY_USERNAME, PRIMARY_PASSWORD, false);
        addUserIntoJDBCUserStore(SECONDARY_USERNAME, SECONDARY_PASSWORD, true);

        // Creating, registering and starting application on tomcat.
        createAndRegisterPlaygroundApplication();
        startTomcat();
    }

    @Test(groups = "wso2.is", description = "Check the secondary user store user login flow",
            dataProvider = "userCredentialProvider")
    public void testUserLogin(String username, String password) throws Exception {

        CookieStore cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        // Sending authorization request to IS.
        sendAuthorizedPost();
        // User (resource owner) authentication.
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
        String sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());
        // Authorization.
        checkAuthorizationCode(sessionDataKeyConsent);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        stopTomcat();
        scim2RestClient.deleteUser(primaryUserStoreUserId);
        scim2RestClient.deleteUser(secondaryUserStoreUserId);
        scim2RestClient.deleteGroup(primaryUserStoreGroupId);
        scim2RestClient.deleteGroup(secondaryUserStoreGroupId);
        scim2RestClient.deleteRole(primaryUserStoreRoleId);
        scim2RestClient.deleteRole(secondaryUserStoreRoleId);
        userStoreMgtRestClient.deleteUserStore(userStoreId);
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
        urlParameters.add(new BasicNameValuePair("scope", "device_01"));
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

    private void checkAuthorizationCode(String sessionDataKeyConsent) throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        String locationHeaderValue = locationHeader.getValue();
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");
        String authorizationCodeString = StringUtils.substringAfterLast(locationHeaderValue, "?code=");
        // Assuring that the authorization code is received, which confirms that the login is successful.
        Assert.assertNotNull(authorizationCodeString, "Authorization code not present, hence login unsuccessful.");
        EntityUtils.consume(response.getEntity());
    }

    private void createAndRegisterPlaygroundApplication() throws Exception {

        playgroundApp = new OIDCApplication(PLAYGROUND_APP_NAME, PLAYGROUND_APP_CALLBACK_URI);
        playgroundApp.addRequiredClaim(OIDCUtilTest.EMAIL_CLAIM_URI);
        playgroundApp.addRequiredClaim(OIDCUtilTest.FIRST_NAME_CLAIM_URI);
        playgroundApp.addRequiredClaim(OIDCUtilTest.LAST_NAME_CLAIM_URI);

        createApplication(playgroundApp);
        clientID = playgroundApp.getClientId();
    }

    private void addUserIntoJDBCUserStore(String username, String password, boolean isSecondaryStoreUser)
            throws Exception {

        if (isSecondaryStoreUser) {
            secondaryUserStoreUserId = scim2RestClient.createUser(new UserObject()
                    .userName(DOMAIN_ID + "/" + username)
                    .password(password));

            secondaryUserStoreGroupId = scim2RestClient.createGroup(new GroupRequestObject()
                    .displayName(SECONDARY_USER_GROUP)
                    .addMember(new MemberItem().value(secondaryUserStoreUserId)));

            secondaryUserStoreRoleId = scim2RestClient.addRole(new RoleRequestObject()
                    .displayName(SECONDARY_USER_ROLE)
                    .addPermissions(PERMISSION_LOGIN)
                    .addUsers(new ListObject().value(secondaryUserStoreUserId))
                    .addGroups(new ListObject().value(secondaryUserStoreGroupId)));
        } else {
            primaryUserStoreUserId = scim2RestClient.createUser(new UserObject()
                    .userName(username)
                    .password(password));

            primaryUserStoreGroupId = scim2RestClient.createGroup(new GroupRequestObject()
                    .displayName(PRIMARY_USER_GROUP)
                    .addMember(new MemberItem().value(primaryUserStoreUserId)));

            primaryUserStoreRoleId = scim2RestClient.addRole(new RoleRequestObject()
                    .displayName(PRIMARY_USER_ROLE)
                    .addPermissions(PERMISSION_LOGIN)
                    .addUsers(new ListObject().value(primaryUserStoreUserId))
                    .addGroups(new ListObject().value(primaryUserStoreGroupId)));
        }
    }

    private void startTomcat() throws LifecycleException, NullPointerException {

        tomcat = Utils.getTomcat(getClass());
        URL resourceUrl = getClass().getResource("/samples/playground2.war");
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

    private void addSecondaryJDBCUserStore() throws Exception {

        // Creating database.
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        // Register a secondary user store.
        UserStoreReq userStore = new UserStoreReq()
                .typeId(USER_STORE_TYPE)
                .name(DOMAIN_ID)
                .addPropertiesItem(new Property()
                        .name("driverName")
                        .value("org.h2.Driver"))
                .addPropertiesItem(new Property()
                        .name("url")
                        .value("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME))
                .addPropertiesItem(new Property()
                        .name("userName")
                        .value(DB_USER_NAME))
                .addPropertiesItem(new Property()
                        .name("password")
                        .value(DB_USER_PASSWORD))
                .addPropertiesItem(new Property()
                        .name("PasswordJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new Property()
                        .name("UsernameJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new Property()
                        .name("Disabled")
                        .value("false"))
                .addPropertiesItem(new Property()
                        .name("PasswordDigest")
                        .value("SHA-256"))
                .addPropertiesItem(new Property()
                        .name("StoreSaltedPassword")
                        .value("true"))
                .addPropertiesItem(new Property()
                        .name("SCIMEnabled")
                        .value("true"))
                .addPropertiesItem(new Property()
                        .name("CountRetrieverClass")
                        .value("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever"))
                .addPropertiesItem(new Property()
                        .name("UserIDEnabled")
                        .value("true"))
                .addPropertiesItem(new Property()
                        .name("GroupIDEnabled")
                        .value("true"));

        userStoreId = userStoreMgtRestClient.addUserStore(userStore);
        Thread.sleep(5000);
        boolean isSecondaryUserStoreDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(DOMAIN_ID);
        Assert.assertTrue(isSecondaryUserStoreDeployed);
    }
}
