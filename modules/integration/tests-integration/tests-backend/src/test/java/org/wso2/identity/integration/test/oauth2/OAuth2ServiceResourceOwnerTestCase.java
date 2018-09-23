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
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
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

	private static final String lockedUser = "test_locked_user";
	private static final String lockedUserPassword = "test_locked_user_pass";
	private static final String ACCOUNT_LOCK_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";
	protected IdentityGovernanceServiceClient identityGovernanceServiceClient;

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

		identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);
		setAccountLocking("true");
		createLockedUser(lockedUser, lockedUserPassword);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		setAccountLocking("false");
		deleteUser(lockedUser);

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
			                  getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
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

    @Test(groups = "wso2.is", description = "Send authorize user request without having colan separated client it and" +
            " secret values", dependsOnMethods = "testRegisterApplication")
    public void testSendInvalidAuthorizedPost() throws Exception {

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
        Assert.assertEquals(errormsg, "invalid_request", "Invalid error message");
    }

	@Test(groups = "wso2.is", description = "Send token request with invalid credentials", dependsOnMethods =
            "testRegisterApplication")
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

    @Test(groups = "wso2.is", description = "Send token request with invalid consumer secret in Authorization header",
            dependsOnMethods = "testRegisterApplication")
    public void testSendInvalidConsumerSecretPost() throws Exception {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":someRandomString")
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errormsg = ((JSONObject) obj).get("error").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(errormsg, "invalid_client", "Invalid error message");
    }

    @Test(groups = "wso2.is", description = "Send token request with invalid consumer secret in Authorization header",
            dependsOnMethods = "testRegisterApplication")
    public void testSendInvalidConsumerKeyPost() throws Exception {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String(("someRandomString:" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errormsg = ((JSONObject) obj).get("error").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(errormsg, "invalid_client", "Invalid error message");
    }

    @Test(groups = "wso2.is", description = "Send token request with repeating parameter",
            dependsOnMethods = "testRegisterApplication")
    public void testSendInvalidRequestPost() throws Exception {

        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type",
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
                .getBytes()).trim());
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errormsg = ((JSONObject) obj).get("error").toString();

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(errormsg, "invalid_request", "Invalid error message");
    }

	@Test(groups = "wso2.is", description = "Send authorize request for locked user", dependsOnMethods =
			"testSendInvalidAuthenticationPost")
	public void testSendLockedAuthenticationPost() throws Exception {

		HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("grant_type",
				OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
		urlParameters.add(new BasicNameValuePair("username", lockedUser));
		urlParameters.add(new BasicNameValuePair("password", lockedUserPassword));

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
		Assert.assertTrue(errormsg.contains("17003 Account is locked for user " + lockedUser));
	}

	private void setAccountLocking(String value) {

		log.info("Set account locking: " + value);

		Property[] newProperties = new Property[1];
		Property prop = new Property();
		prop.setName("account.lock.handler.enable");
		prop.setValue(value);
		newProperties[0] = prop;

		try {
			identityGovernanceServiceClient.updateConfigurations(newProperties);
		} catch (Exception e) {
			Assert.fail("Error while updating resident idp", e);
		}
	}

	private void createLockedUser(String username, String password) {

		log.info("Creating User " + username);

		ClaimValue[] claimValues = new ClaimValue[1];
		// Need to add this claim and have the value true in order to test the fix
		ClaimValue accountLockClaim = new ClaimValue();
		accountLockClaim.setClaimURI(ACCOUNT_LOCK_CLAIM_URI);
		accountLockClaim.setValue(Boolean.TRUE.toString());
		claimValues[0] = accountLockClaim;

		try {
			remoteUSMServiceClient.addUser(username, password, null, claimValues, null, false);
		} catch (Exception e) {
			Assert.fail("Error while creating the user", e);
		}
	}

	private void deleteUser(String username) {
		log.info("Deleting User " + username);
		try {
			remoteUSMServiceClient.deleteUser(username);
		} catch (Exception e) {
			Assert.fail("Error while deleting the user", e);
		}
	}
}
