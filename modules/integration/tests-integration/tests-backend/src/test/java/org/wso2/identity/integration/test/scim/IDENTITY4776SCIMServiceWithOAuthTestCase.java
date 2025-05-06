/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.scim;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.wink.client.Resource;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.scim.utils.SCIMUtils;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Testcase for https://wso2.org/jira/browse/IDENTITY-4776
 * <p>
 * Steps to recreate the scenario.
 * 1. Add a secondary user store (with SCIMEnabled).
 * 2. Add a user/role to above secondary user store.
 * 3. Register an OAuth application, and generate a token (with primary store user).
 * 4. Login to carbon using secondary store user (to invoke BasicAuthHandler).
 * 5. Try to create new user using SCIM Service (using auth token generated on step 3).
 */
public class IDENTITY4776SCIMServiceWithOAuthTestCase extends OAuth2ServiceAbstractIntegrationTest {
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String USER_STORE_DB_NAME = "JDBC_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private static final String SCIM_USER_NAME = "scimUser";
    private static final String SECONDARY_STORE_USER_NAME = DOMAIN_ID + "/userStoreUser";
    private static final String SECONDARY_STORE_USER_ROLE = DOMAIN_ID + "/jdsbUserStoreRole";
    private static final String SECONDARY_STORE_USER_PASSWORD = "Password@123";
    private static final String SCOPE_DEFAULT = "default";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private ServerConfigurationManager serverConfigurationManager;
    private AuthenticatorClient authenticatorClient;
    private UserManagementClient userMgtClient;
    private String accessToken;
    private String scimUrl;
    private String oauth2Endpoint;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File scimConfiguredTomlFile = new File(getISResourceLocation() + File.separator + "scim"
                + File.separator + "IDENTITY4776" + File.separator + "catalina_server_config.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(scimConfiguredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(isServer);
        sessionCookie = loginLogoutClient.login();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        scimUrl = backendURL.substring(0, 22) + "/wso2/scim/";
        oauth2Endpoint = backendURL.substring(0, 22) + "/oauth2/token";
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        try {
            deleteApplication();
            removeOAuthApplicationData();
            userMgtClient.deleteUser(SCIM_USER_NAME);
            userMgtClient.deleteUser(SECONDARY_STORE_USER_NAME);
            userMgtClient.deleteRole(SECONDARY_STORE_USER_ROLE);
            userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
            serverConfigurationManager.restoreToLastConfiguration(false);
        } catch (Exception e) {
            log.error("Error occured while executing the test case: " + e.getMessage(), e);
            throw e;
        }
    }

    @Test(groups = "wso2.is", description = "loginUsingSecondaryUserStoreUser")
    public void addUsersUsingOAuthandSCIM() throws Exception {

        try {
            addSecondaryUserStore();
            addUserIntoSecondaryUserStore();
            registerOAuthApplication();
            loginUsingSecondaryUserStoreUser();

            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
            String postData = SCIMUtils.getEncodedSCIMUser(scimClient, SCIM_USER_NAME, "test",
                    new String[]{"scimUser@gmail.com", "scimUser@wso2.com"}, "SCIMUser", "Wso2@test",
                    "Sinhala", "0711234567");

            //create a apache wink ClientHandler to intercept and identify response messages
            Resource userResource = SCIMUtils.getUserResource(scimClient, scimUrl);
            String response = userResource.
                    header(SCIMConstants.AUTHORIZATION_HEADER, "Bearer " + accessToken).
                    contentType(SCIMConstants.APPLICATION_JSON).accept(SCIMConstants.APPLICATION_JSON).
                    post(String.class, postData);

            Object obj = JSONValue.parse(response);
            String scimUserId = ((JSONObject) obj).get("id").toString();
            Assert.assertTrue(isUserExists());
            Assert.assertNotNull(scimUserId);
        } catch (Exception e) {
            log.error("Error occured while executing the test case: " + e.getMessage(), e);
            throw e;
        }
    }

    private void addSecondaryUserStore() throws Exception {

        String jdbcClass = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + Utils.getResidentCarbonHome()
                + "/repository/database/" + USER_STORE_DB_NAME,
                DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(Utils.getResidentCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        PropertyDTO[] propertyDTOs = new PropertyDTO[12];
        for (int i = 0; i < 12; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(DB_USER_NAME);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(DB_USER_PASSWORD);

        propertyDTOs[4].setName("PasswordJavaRegEx");
        propertyDTOs[4].setValue("^[\\S]{5,30}$");

        propertyDTOs[5].setName("UsernameJavaRegEx");
        propertyDTOs[5].setValue("^[\\S]{5,30}$");

        propertyDTOs[6].setName("Disabled");
        propertyDTOs[6].setValue("false");

        propertyDTOs[7].setName("PasswordDigest");
        propertyDTOs[7].setValue("SHA-256");

        propertyDTOs[8].setName("StoreSaltedPassword");
        propertyDTOs[8].setValue("true");

        propertyDTOs[9].setName("SCIMEnabled");
        propertyDTOs[9].setValue("true");

        propertyDTOs[10].setName("UserIDEnabled");
        propertyDTOs[10].setValue("true");

        propertyDTOs[11].setName("GroupIDEnabled");
        propertyDTOs[11].setValue("true");

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient
                .createUserStoreDTO(jdbcClass, DOMAIN_ID, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);
    }

    private void addUserIntoSecondaryUserStore() throws Exception {
        authenticatorClient = new AuthenticatorClient(backendURL);
        userMgtClient.addRole(SECONDARY_STORE_USER_ROLE, null, new String[]{PERMISSION_LOGIN});
        userMgtClient.addUser(SECONDARY_STORE_USER_NAME, SECONDARY_STORE_USER_PASSWORD,
                new String[]{SECONDARY_STORE_USER_ROLE}, null);
    }

    private void registerOAuthApplication() throws Exception {
        OAuthConsumerAppDTO appDto = createApplication();
        consumerKey = appDto.getOauthConsumerKey();
        consumerSecret = appDto.getOauthConsumerSecret();
        accessToken = requestAccessToken(consumerKey, consumerSecret, oauth2Endpoint);
    }

    private String requestAccessToken(String consumerKey, String consumerSecret, String oauth2Endpoint)
            throws Exception {
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(oauth2Endpoint);
        //generate post request
        httpPost.setHeader("Authorization", "Basic "
                + new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes())));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", "admin"));
        postParameters.add(new BasicNameValuePair("password", "admin"));
        postParameters.add(new BasicNameValuePair("scope", SCOPE_DEFAULT));
        postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        //get access token from the response
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        return json.get("access_token").toString();
    }

    private void loginUsingSecondaryUserStoreUser() throws Exception {
        String sessionCookie = authenticatorClient.login(
                SECONDARY_STORE_USER_NAME, SECONDARY_STORE_USER_PASSWORD,
                isServer.getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();
    }

    private boolean isUserExists() throws Exception {
        boolean userExists = false;
        FlaggedName[] nameList = userMgtClient.listAllUsers(SCIM_USER_NAME, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(SCIM_USER_NAME)) {
                userExists = true;
            }
        }
        return userExists;
    }
}