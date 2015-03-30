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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceAuthCodeGrantOpenIdTestCase extends OAuth2ServiceAbstractIntegrationTest {

	private AuthenticatorClient logManger;
	private String adminUsername;
	private String adminPassword;
	private String accessToken;
	private String sessionDataKeyConsent;
	private String sessionDataKey;
	private String authorizationCode;

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
		client = new DefaultHttpClient();
		setSystemproperties();
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
		urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
		urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
		urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
		urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
		urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
		urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));

		HttpResponse response =
		                        sendPostRequestWithParameters(client, urlParameters,
		                                                      OAuth2Constant.AUTHORIZED_USER_URL);
		Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader,
		                     "Authorization request failed. Authorized response header is null");
		EntityUtils.consume(response.getEntity());

		response = sendGetRequest(client, locationHeader.getValue());
		Assert.assertNotNull(response,
		                     "Authorization request failed. Authorized user response is null.");

		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"sessionDataKey\"", 1);
		List<KeyValue> keyValues =
		                           DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
		Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

		sessionDataKey = keyValues.get(0).getValue();
		Assert.assertNotNull(sessionDataKey, "Session data key is null.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorozedPost")
	public void testSendLoginPost() throws Exception {
		HttpResponse response = sendLoginPost(client, sessionDataKey);
		Assert.assertNotNull(response, "Login request failed. response is null.");

		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Login response header is null");
		EntityUtils.consume(response.getEntity());

		response = sendGetRequest(client, locationHeader.getValue());
		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
		List<KeyValue> keyValues =
		                           DataExtractUtil.extractSessionConsentDataFromResponse(response,
		                                                                                 keyPositionMap);
		Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");

		sessionDataKeyConsent = keyValues.get(0).getValue();
		Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
	public void testSendApprovalPost() throws Exception {
		HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
		Assert.assertNotNull(response, "Approval request failed. response is invalid.");

		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
		EntityUtils.consume(response.getEntity());

		response = sendPostRequest(client, locationHeader.getValue());
		Assert.assertNotNull(response, "Get Activation response is invalid.");

		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("Authorization Code", 1);
		List<KeyValue> keyValues =
		                           DataExtractUtil.extractTableRowDataFromResponse(response,
		                                                                           keyPositionMap);
		Assert.assertNotNull(response, "Authorization Code key value is invalid.");

		authorizationCode = keyValues.get(0).getValue();
		Assert.assertNotNull(authorizationCode, "Authorization code is null.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendApprovalPost")
	public void testGetAccessToken() throws Exception {
		HttpResponse response = sendGetAccessTokenPost(client, consumerSecret);
		Assert.assertNotNull(response, "Approval response is invalid.");
		EntityUtils.consume(response.getEntity());

		response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"accessToken\"", 1);
		List<KeyValue> keyValues =
		                           DataExtractUtil.extractInputValueFromResponse(response,
		                                                                         keyPositionMap);
		Assert.assertNotNull(keyValues, "Access token Key value is null.");

		accessToken = keyValues.get(0).getValue();
		Assert.assertNotNull(accessToken, "Access token is null.");
		EntityUtils.consume(response.getEntity());

		response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

		keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("id=\"loggedUser\"", 1);
		keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
		Assert.assertNotNull(keyValues, "Access token Key value is null.");

		String loggedUser = keyValues.get(0).getValue();
		Assert.assertNotNull(loggedUser, "Logged user is null.");
		Assert.assertNotEquals(loggedUser, "null", "Logged user is null.");
		Assert.assertNotEquals(loggedUser, "", "Logged user is null.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testGetAccessToken")
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
		Assert.assertEquals(valid, "true", "Token Validation failed");
		EntityUtils.consume(response.getEntity());
	}

}