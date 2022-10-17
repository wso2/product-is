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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceClientCredentialTestCase extends OAuth2ServiceAbstractIntegrationTest {
	private AuthenticatorClient logManger;
	private String accessToken;
	private String consumerKey;
	private String consumerSecret;
	private String username;
	private String userPassword;
	private final AutomationContext context;
	private String backendURL;
	private String sessionCookie;
	private Tenant tenantInfo;
	private User userInfo;
	private LoginLogoutClient loginLogoutClient;
	private ContextUrls identityContextUrls;
	private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

	private CloseableHttpClient client;

	@DataProvider(name = "configProvider")
	public static Object[][] configProvider() {
		return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
	}

	@Factory(dataProvider = "configProvider")
	public OAuth2ServiceClientCredentialTestCase(TestUserMode userMode) throws Exception {

		context = new AutomationContext("IDENTITY", userMode);
		this.username = context.getContextTenant().getTenantAdmin().getUserName();
		this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
	}

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

		backendURL = context.getContextUrls().getBackEndUrl();
		loginLogoutClient = new LoginLogoutClient(context);
		logManger = new AuthenticatorClient(backendURL);
		sessionCookie = logManger.login(username, userPassword, context.getInstance().getHosts().get("default"));
		identityContextUrls = context.getContextUrls();
		tenantInfo = context.getContextTenant();
		userInfo = tenantInfo.getContextUser();
		appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
		adminClient = new OauthAdminClient(backendURL, sessionCookie);
		remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

		setSystemproperties();
		client = HttpClientBuilder.create().build();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		appMgtclient.deleteApplication(SERVICE_PROVIDER_NAME);
		adminClient.removeOAuthApplicationData(consumerKey);
		client.close();
		logManger = null;
		consumerKey = null;
		accessToken = null;
	}

	@Test(groups = "wso2.is", description = "Check Oauth2 application registration")
	public void testRegisterApplication() throws Exception {

		OAuthConsumerAppDTO appDto = createApplication();
		Assert.assertNotNull(appDto, "Application creation failed.");

		consumerKey = appDto.getOauthConsumerKey();
		Assert.assertNotNull(consumerKey, "Application creation failed.");

		consumerSecret = appDto.getOauthConsumerSecret();
		Assert.assertNotNull(consumerSecret, "Application creation failed.");

	}

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testSendAuthorozedPost() throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair(
		                                         "grantType",
		                                         OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
		urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
		urlParameters.add(new BasicNameValuePair("accessEndpoint",
		                                         OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
		urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
		HttpResponse response =
		                        sendPostRequestWithParameters(client, urlParameters,
		                                                      OAuth2Constant.AUTHORIZED_USER_URL);
		Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
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

		String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
				OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
		org.json.simple.JSONObject responseObj = introspectTokenWithTenant(client, accessToken, introspectionUrl,
				username, userPassword);
		Assert.assertNotNull(responseObj, "Validate access token failed. response is invalid.");
		Assert.assertEquals(responseObj.get("active"), true, "Token Validation failed");
	}
}
