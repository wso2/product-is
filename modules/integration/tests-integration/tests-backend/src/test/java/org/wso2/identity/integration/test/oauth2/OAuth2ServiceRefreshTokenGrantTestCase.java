/*
 * Copyright (c) 2017, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.List;

public class OAuth2ServiceRefreshTokenGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String adminUsername;
    private String adminPassword;
    private String refreshToken;
    private String consumerKey;
    private String consumerSecret;
    private String applicationId;

    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        adminUsername = userInfo.getUserNameWithoutDomain();
        adminPassword = userInfo.getPassword();

        setSystemproperties();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);

        consumerKey = null;
        refreshToken = null;
        applicationId = null;
        client.close();
        restClient.closeHttpClient();
    }

    @Test(alwaysRun = true, description = "Check Oauth2 application flow")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

        applicationId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Validate refresh token", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorizedPost() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("username", adminUsername));
        postParameters.add(new BasicNameValuePair("password", adminPassword));
        postParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        JSONObject responseObject = responseObject(postParameters);
        refreshToken = responseObject.get("refresh_token").toString();

        Assert.assertNotNull(refreshToken, "Refresh token is null.");
    }

    @Test(groups = "wso2.is", description = "Validate refresh token", dependsOnMethods = "testSendAuthorizedPost")
    public void testRefreshTokenGrant() throws Exception {

        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        postParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));
        JSONObject responseObject = responseObject(postParameters);
        String accessToken = responseObject.get("access_token").toString();

        Assert.assertNotNull(accessToken, "Access token is null.");
    }

    private JSONObject responseObject(List<NameValuePair> postParameters) throws Exception {

        HttpPost httpPost = new HttpPost(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception(
                    "Error occurred while getting the response");
        }

        return json;
    }

}
