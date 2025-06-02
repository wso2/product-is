/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.util.ArrayList;

public class IDENTITY6777OAuth2TokenExpiryTestCase extends OAuth2ServiceAbstractIntegrationTest {
    private static final String SCOPE_PRODUCTION = "PRODUCTION";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String TOKEN_API_ENDPOINT = "https://localhost:9853/oauth2/token";

    private ServerConfigurationManager serverConfigurationManager;

    private File identityXML;
    private String adminUsername;
    private String adminPassword;
    private String accessToken;

    private String consumerKey;
    private String consumerSecret;

    private DefaultHttpClient client;

    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String USER_EMAIL = "abc@wso2.com";
    private static final String USERNAME = "authcodegrantuser";
    private static final String PASSWORD = "Pass@123";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_USER);

        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        client = new DefaultHttpClient();
        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        accessToken = null;
        resetISConfiguration();
    }

    @Test(description = "Check Oauth2 application flow")
    public void testRegisterApplication() throws Exception {
        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDto.getOauthConsumerSecret();
    }


    /**
     * This tests written for test for token expiration for resource owner grant.
     *
     * @throws Exception
     */
    @Test(description = "Test access token expiry for resource owner grant", dependsOnMethods =
            "testRegisterApplication")
    public void testTokenExpiryForResourceOwnerGrant() throws Exception {

        JSONObject responseT0 = requestAccessTokenUsingResourceOwnerGrant();
        String accessTokenT0 = responseT0.get("access_token").toString();
        String refreshTokenT0 = responseT0.get("refresh_token").toString();
        log.info("Initial access token: " + accessTokenT0 + ", refresh token: " + refreshTokenT0 + " at t=0s");

        // Access, refresh tokens must be unchanged
        Thread.sleep(30 * 1000);
        JSONObject responseT30  = requestAccessTokenUsingResourceOwnerGrant();
        String accessTokenT30 = responseT30.get("access_token").toString();
        String refreshTokenT30 = responseT30.get("refresh_token").toString();
        log.info("Access token: " + accessTokenT30 + ", refresh token: " + refreshTokenT30 + " at t=30s");
        Assert.assertEquals(accessTokenT30, accessTokenT0, "Unexpected access token for active access token.");
        Assert.assertEquals(refreshTokenT30, refreshTokenT0,
                "Unexpected refresh token for active access & refresh token.");

        // Access token should be changed, refresh token must be unchanged.
        Thread.sleep(31 * 1000);
        JSONObject responseT61  = requestAccessTokenUsingResourceOwnerGrant();
        String accessTokenT61 = responseT61.get("access_token").toString();
        String refreshTokenT61 = responseT61.get("refresh_token").toString();
        log.info("Access token: " + accessTokenT61 + ", refresh token: " + refreshTokenT61 + " at t=61s");
        Assert.assertNotEquals(accessTokenT61, accessTokenT0, "Unexpected access token for expired access token.");
        Assert.assertEquals(refreshTokenT61, refreshTokenT0,
                "Unexpected access token for expired access token with active refresh token.");

        // At this step refresh token should be regenerated, even though its active at this point.
        // Access token should be changed, refresh token must be also changed tih the fix of IDENTITY-67777.
        Thread.sleep(61 * 1000);
        JSONObject responseT122  = requestAccessTokenUsingResourceOwnerGrant();
        String accessTokenT122 = responseT122.get("access_token").toString();
        String refreshTokenT122 = responseT122.get("refresh_token").toString();
        log.info("Access token: " + accessTokenT122 + ", refresh token: " + refreshTokenT122 + " at t=122s");
        Assert.assertNotEquals(accessTokenT122, accessTokenT61, "Unexpected access token for expired access token.");
        Assert.assertNotEquals(refreshTokenT122, refreshTokenT61,
                "Unexpected access token for expired access token with refresh token thats going to expired.");

        // If the issue IDENTITY-6777 present, it should failed with below assert as well.
        Thread.sleep(31 * 1000);
        JSONObject responseT153  = requestAccessTokenUsingResourceOwnerGrant();
        String accessTokenT153 = responseT153.get("access_token").toString();
        String refreshTokenT153 = responseT153.get("refresh_token").toString();
        log.info("Access token: " + accessTokenT153 + ", refresh token: " + refreshTokenT153 + " at t=153s");
        Assert.assertEquals(accessTokenT153, accessTokenT122, "Unexpected access token for active access token.");
        Assert.assertEquals(refreshTokenT153, refreshTokenT153, "Unexpected access token for active access token.");
    }

    /**
     * This tests written for test for token expiration for client credentials grant.
     *
     * @throws Exception
     */
    @Test(description = "Test access token expiry for client credentials grant",
            dependsOnMethods = "testRegisterApplication")
    public void testTokenExpiryForClientCredentialGrant() throws Exception {
        String accessTokenT0 = requestAccessTokenUsingClientCredentialsGrant();
        log.info("Initial access token at t=0s: " + accessTokenT0);

        Thread.sleep(30 * 1000);
        String accessTokenT30  = requestAccessTokenUsingClientCredentialsGrant();
        log.info("Access token at t=30s: " + accessTokenT30);
        Assert.assertEquals(accessTokenT30, accessTokenT0, "Unexpected access token for active access token.");

        Thread.sleep(31 * 1000);
        String accessTokenT61  = requestAccessTokenUsingClientCredentialsGrant();
        log.info("Access token at t=61s: " + accessTokenT61);
        Assert.assertNotEquals(accessTokenT61, accessTokenT0, "Unexpected access token for expired access token.");

        // At this step refresh token should be regenerated, even though its active at this point. But client credential
        // grant does not have a significance of asserting at this point.
        Thread.sleep(61 * 1000);
        String accessTokenT122  = requestAccessTokenUsingClientCredentialsGrant();
        log.info("Access token at t=122s: " + accessTokenT122);
        Assert.assertNotEquals(accessTokenT122, accessTokenT61, "Unexpected access token for expired access token.");

        // If the issue IDENTITY-6777 present, it should failed with below assert.
        Thread.sleep(31 * 1000);
        String accessTokenT153  = requestAccessTokenUsingClientCredentialsGrant();
        log.info("Access token at t=153s: " + accessTokenT153);
        Assert.assertEquals(accessTokenT153, accessTokenT122, "Unexpected access token for active access token.");
    }

    /**
     * Request access token using resource owner grant.
     *
     * @return
     * @throws Exception
     */
    public JSONObject requestAccessTokenUsingResourceOwnerGrant() throws Exception {
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(TOKEN_API_ENDPOINT);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", adminUsername));
        postParameters.add(new BasicNameValuePair("password", adminPassword));
        postParameters.add(new BasicNameValuePair("scope", "RO"));
        postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        //Get access token from the response
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(responseString);
    }

    /**
     * Request access token using client credentials grant.
     *
     * @return
     * @throws Exception
     */
    public String requestAccessTokenUsingClientCredentialsGrant() throws Exception {
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(TOKEN_API_ENDPOINT);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("scope", "CC"));
        postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        //Get access token from the response
        JSONParser parser = new JSONParser();
        JSONObject token = (JSONObject) parser.parse(responseString);
        return token.get("access_token").toString();
    }

    /**
     * Change the server configuration to reduce token expiry time.
     *
     * @throws Exception
     */
    private void changeISConfiguration() throws Exception {
        log.info("Replacing repository/conf/identity/identity.xml to configure expiry time");

        String carbonHome = Utils.getResidentCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File
                .separator + "identity.xml");
        File configuredIdentityXml = new File(getISResourceLocation()
                + File.separator + "oauth" + File.separator + "IDENTITY6777-identity.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXml, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Restore  the server configuration.
     *
     * @throws Exception
     */
    private void resetISConfiguration() throws Exception {
        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }
}
