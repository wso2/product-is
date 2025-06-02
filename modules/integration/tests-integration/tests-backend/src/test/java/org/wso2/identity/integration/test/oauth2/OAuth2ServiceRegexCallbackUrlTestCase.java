/*
 * Copyright (c) 2016, WSO2 LLC. (https://www.wso2.com).
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

public class OAuth2ServiceRegexCallbackUrlTestCase extends OAuth2ServiceAbstractIntegrationTest {

	private String accessToken;
	private String sessionDataKeyConsent;
	private String sessionDataKey;

	private String consumerKey;
	private String consumerSecret;
	private String applicationId;

	private Lookup<CookieSpecProvider> cookieSpecRegistry;
	private RequestConfig requestConfig;
	private CloseableHttpClient client;

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

		super.init(TestUserMode.SUPER_TENANT_USER);

		setSystemproperties();

		cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
				.register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
				.build();
		requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.DEFAULT)
				.build();
		client = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieSpecRegistry(cookieSpecRegistry)
				.build();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		deleteApp(applicationId);
		client.close();
		restClient.closeHttpClient();
		consumerKey = null;
		accessToken = null;
		applicationId = null;
	}

	@Test(groups = "wso2.is", description = "Check Oauth2 application flow")
	public void testRegisterApplication() throws Exception {

		ApplicationResponseModel application = createTestApplication();
		applicationId = application.getId();

		OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
		consumerKey = oidcConfig.getClientId();
		Assert.assertNotNull(consumerKey, "Application creation failed.");

		consumerSecret = oidcConfig.getClientSecret();
		Assert.assertNotNull(consumerSecret, "Application creation failed.");
	}

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testSendAuthorozedPost() throws Exception {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("grantType",
				OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT));
		urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
		urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_REQUEST_URL_WITH_PARAMS));
		urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
		urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
		urlParameters.add(new BasicNameValuePair("scope", "device_01"));

		HttpResponse response =
				sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZED_USER_URL);
		Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

		Header locationHeader =
				response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		Assert.assertNotNull(locationHeader, "Authorized response header is null");
		EntityUtils.consume(response.getEntity());

		response = sendGetRequest(client, locationHeader.getValue());
		Assert.assertNotNull(response, "Authorized user response is null.");

		Map<String, Integer> keyPositionMap = new HashMap<>(1);
		keyPositionMap.put("name=\"sessionDataKey\"", 1);
		List<DataExtractUtil.KeyValue> keyValues =
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
		Map<String, Integer> keyPositionMap = new HashMap<>(1);
		keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
		List<DataExtractUtil.KeyValue> keyValues =
				DataExtractUtil.extractSessionConsentDataFromResponse(response,
						keyPositionMap);
		Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
		sessionDataKeyConsent = keyValues.get(0).getValue();
		EntityUtils.consume(response.getEntity());

		Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
	}

	@Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
	public void testSendApprovalPost() throws Exception {

		List<NameValuePair> urlParameters = new ArrayList<>();
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
		Assert.assertNotNull(accessToken, "Access token is null.");
		EntityUtils.consume(response.getEntity());
	}

	@Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testSendApprovalPost")
	public void testValidateAccessToken() throws Exception {

		HttpResponse response = sendValidateAccessTokenPost(client, accessToken);
		Assert.assertNotNull(response, "Validate access token response is invalid.");

		Map<String, Integer> keyPositionMap = new HashMap<>(1);
		keyPositionMap.put("name=\"valid\"", 1);

		List<DataExtractUtil.KeyValue> keyValues =
				DataExtractUtil.extractInputValueFromResponse(response,
						keyPositionMap);
		Assert.assertNotNull(keyValues, "Access token Key value is null.");
		String valid = keyValues.get(0).getValue();
		EntityUtils.consume(response.getEntity());
		Assert.assertEquals(valid, "true", "Token Validation failed");
		EntityUtils.consume(response.getEntity());
	}

	private ApplicationResponseModel createTestApplication() throws Exception {

		ApplicationModel application = new ApplicationModel();

		List<String> grantTypes = new ArrayList<>();
		Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
				"refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

		List<String> callBackUrls = new ArrayList<>();
		Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL_REGEXP);

		OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
		oidcConfig.setGrantTypes(grantTypes);
		oidcConfig.setCallbackURLs(callBackUrls);

		InboundProtocols inboundProtocolsConfig = new InboundProtocols();
		inboundProtocolsConfig.setOidc(oidcConfig);

		application.setInboundProtocolConfiguration(inboundProtocolsConfig);
		application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);

		String appId = addApplication(application);

		return getApplication(appId);
	}
}
