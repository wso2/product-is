/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.oauth2;

import org.apache.axis2.AxisFault;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.common.clients.PropertiesAdminServiceClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case for oauth2 token time per service provider.
 */
public class OAuth2TokenTimePerSPTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String consumerKey;
    private String consumerSecret;
    private PropertiesAdminServiceClient propAdminServiceClient;
    private DefaultHttpClient client;
    private Tomcat tomcat;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        setSystemproperties();
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();
        stopTomcat(tomcat);
        consumerKey = null;
    }

    @Test(alwaysRun = true, description = "Deploy playground application")
    public void testDeployPlaygroundApp() {
        try {
            tomcat = getTomcat();
            URL resourceUrl =
                    getClass().getResource(File.separator + "samples" + File.separator +
                            "playground2.war");
            startTomcat(tomcat, OAuth2Constant.PLAYGROUND_APP_CONTEXT_ROOT, resourceUrl.getPath());

        } catch (LifecycleException e) {
            Assert.fail("Playground application deployment failed.", e);
        }
    }

    @Test(groups = "oauth2.sp", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO appDto = createApplication();
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = appDto.getOauthConsumerSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

    }

    @Test(groups = "oauth2.sp", description = "Add the property to the registry",
            dependsOnMethods = "testRegisterApplication")
    public void testAddPropertiesToRegistry() throws AxisFault,
            PropertiesAdminServiceRegistryExceptionException {

        String jsonStringOfTime = "{\n" +
                "\"userAccessTokenExpireTime\" : 500000,\n" +
                "\"applicationAccessTokenExpireTime\" : 500000,\n" +
                "\"refreshTokenExpireTime\" : 500000 \n" +
                "}\n";
        Boolean success = false;

        try {
            propAdminServiceClient = new PropertiesAdminServiceClient(backendURL, sessionCookie);
            propAdminServiceClient.setProperty("_system/config/identity/config/spTokenExpireTime",
                    consumerKey, jsonStringOfTime);
            success = true;
        } catch (RegistryException e) {
            Assert.fail("An issue occured when adding property to the resource", e);
        }
        Assert.assertTrue(success);
    }

    @Test(groups = "wso2.is", description = "Send authorize user request",
            dependsOnMethods = "testAddPropertiesToRegistry")
    public void testSendInvalidAuthenticationPost() throws IOException {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " +
                Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String tokenExpiryTime = ((JSONObject) obj).get("expires_in").toString();
        Assert.assertEquals(tokenExpiryTime, "500", "Expiry time is not eqaul to 500 miliseconds");
    }

}
