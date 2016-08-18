/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceResourceOwnerTestCase extends OAuth2ServiceAbstractIntegrationTest {
	private AuthenticatorClient logManger;
	private String adminUsername;
	private String adminPassword;
	private String accessToken;
	private String consumerKey;
	private String consumerSecret;

	private DefaultHttpClient client;
	private Tomcat tomcat;

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {
		super.init(TestUserMode.SUPER_TENANT_USER);
		logManger = new AuthenticatorClient(backendURL);
		adminUsername = userInfo.getUserName();
		adminPassword = userInfo.getPassword();
		logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
				isServer.getSuperTenant().getTenantAdmin().getPassword(),
				isServer.getInstance().getHosts().get("default"));

		setSystemproperties();
		client = new DefaultHttpClient();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		deleteApplication();
		removeOAuthApplicationData();
		stopTomcat(tomcat);

		logManger = null;
		consumerKey = null;
		accessToken = null;
	}

	@Test(alwaysRun = true, description = "Deploy playground application")
	public void testDeployPlaygroundApp() {
		try {
			tomcat = getTomcat();
			URL resourceUrl =
			                  getClass().getResource(File.separator + "samples" + File.separator +
			                                                 "playground2.war");
			startTomcat(tomcat, OAuth2Constant.PLAYGROUND_APP_CONTEXT_ROOT, resourceUrl.getPath());

		} catch (Exception e) {
			Assert.fail("Playground application deployment failed.", e);
		}
	}

	@Test(groups = "wso2.is", description = "Check Oauth2 application registration", dependsOnMethods = "testDeployPlaygroundApp")
	public void testRegisterApplication() throws Exception {
		OAuthConsumerAppDTO appDto = createApplication();
		Assert.assertNotNull(appDto, "Application creation failed.");

		consumerKey = appDto.getOauthConsumerKey();
		Assert.assertNotNull(consumerKey, "Application creation failed.");

		consumerSecret = appDto.getOauthConsumerSecret();
	}

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testSendAuthorozedPost() throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("grantType",
		                                         OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
		urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
		urlParameters.add(new BasicNameValuePair("accessEndpoint",
		                                         OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
		urlParameters.add(new BasicNameValuePair("recowner", "admin"));
		urlParameters.add(new BasicNameValuePair("recpassword", "admin"));
		urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));

		HttpResponse response =
		                        sendPostRequestWithParameters(client, urlParameters,
		                                                      OAuth2Constant.AUTHORIZED_USER_URL);
		Assert.assertNotNull(response, "Authorized response is null");
		EntityUtils.consume(response.getEntity());

		response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"accessToken\"", 1);

		List<KeyValue> keyValues =
		                           DataExtractUtil.extractInputValueFromResponse(response,
		                                                                         keyPositionMap);
		Assert.assertNotNull(keyValues, "Access token Key value is null.");
		accessToken = keyValues.get(0).getValue();

		EntityUtils.consume(response.getEntity());
		Assert.assertNotNull(accessToken, "Access token is null.");
	}

	@Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testSendAuthorozedPost")
	public void testValidateAccessToken() throws Exception {
		HttpResponse response = sendValidateAccessTokenPost(client, accessToken);
		Assert.assertNotNull(response, "Validate access token response is invalid.");

		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"valid\"", 1);

		List<KeyValue> keyValues =
		                           DataExtractUtil.extractInputValueFromResponse(response,
		                                                                         keyPositionMap);
		Assert.assertNotNull(keyValues, "Access token Key value is null.");
		String valid = keyValues.get(0).getValue();
		EntityUtils.consume(response.getEntity());
		Assert.assertEquals(valid, "true", "Token Validation failed");
	}

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendInvalidAuthorozedPost() throws Exception {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errormsg = ((JSONObject) obj).get("error").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals("invalid_client", errormsg, "Invalid error message");
    }

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testSendInvalidAuthenticationPost() throws Exception {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin1"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errormsg = ((JSONObject) obj).get("error_description").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertTrue(errormsg.contains("Authentication failed for admin"));
	}

}
