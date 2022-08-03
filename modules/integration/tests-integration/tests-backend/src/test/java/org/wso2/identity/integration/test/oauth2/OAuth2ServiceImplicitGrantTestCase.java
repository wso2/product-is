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

import org.apache.http.Header;
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
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class OAuth2ServiceImplicitGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

	private AuthenticatorClient logManger;
	private String accessToken;
	private String scopes;
	private String sessionDataKeyConsent;
	private String sessionDataKey;

	private String consumerKey;
	private String consumerSecret;

	private CloseableHttpClient client;
	private final String username;
	private final String userPassword;
	private final AutomationContext context;
	private String backendURL;
	private String sessionCookie;
	private Tenant tenantInfo;
	private User userInfo;
	private LoginLogoutClient loginLogoutClient;
	private ContextUrls identityContextUrls;
	private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

	@DataProvider(name = "configProvider")
	public static Object[][] configProvider() {
		return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
	}

	@Factory(dataProvider = "configProvider")
	public OAuth2ServiceImplicitGrantTestCase(TestUserMode userMode) throws Exception {

		super.init(userMode);
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
		scopes = "abc";
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
	}

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testSendAuthorozedPost() throws Exception {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("grantType",
		                                         OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT));
		urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
		urlParameters.add(new BasicNameValuePair("scope", scopes));
		urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
		urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
		urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));

		HttpResponse response =
		                        sendPostRequestWithParameters(client, urlParameters,
		                                                      OAuth2Constant.AUTHORIZED_USER_URL);
		Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Authorized response header is null");
		EntityUtils.consume(response.getEntity());

		response = sendGetRequest(client, locationHeader.getValue());
		Assert.assertNotNull(response, "Authorized user response is null.");

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
		Assert.assertNotNull(response, "Login request failed. Login response is null.");

		if (Utils.requestMissingClaims(response)) {
			String pastrCookie = Utils.getPastreCookie(response);
			Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
			EntityUtils.consume(response.getEntity());

			response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT , Utils.getRedirectUrl
					(response), client, pastrCookie);
			EntityUtils.consume(response.getEntity());
		}
		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Login request failed. Login response header is null");
		EntityUtils.consume(response.getEntity());

		response = sendGetRequest(client, locationHeader.getValue());
		Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
		keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
		List<KeyValue> keyValues =
		                           DataExtractUtil.extractSessionConsentDataFromResponse(response,
		                                                                                 keyPositionMap);
		Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
		sessionDataKeyConsent = keyValues.get(0).getValue();
		EntityUtils.consume(response.getEntity());

		Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
	}

	@Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
	public void testSendApprovalPost() throws Exception {

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("consent", "approve"));
		urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

		HttpResponse response =
		                        sendPostRequestWithParameters(client, urlParameters,
		                                                      OAuth2Constant.APPROVAL_URL);
		Assert.assertNotNull(response, "Approval response is invalid.");

		Header locationHeader =
		                        response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Approval Location header is null.");

		accessToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
				OAuth2Constant.ACCESS_TOKEN);
		String urlScopes = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
				OAuth2Constant.OAUTH2_SCOPE);
		Assert.assertNotNull(accessToken, "Access token is null.");
		Assert.assertEquals(urlScopes, scopes, "Scopes are not equal.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testSendApprovalPost")
	public void testValidateAccessToken() throws Exception {

		String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
				OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
		org.json.simple.JSONObject responseObj = introspectTokenWithTenant(client, accessToken, introspectionUrl,
				username, userPassword);
		Assert.assertNotNull(responseObj, "Validate access token failed. response is invalid.");
		Assert.assertEquals(responseObj.get("active"), true, "Token Validation failed");
	}
}
