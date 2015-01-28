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
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.util.ArrayList;

public class OAuth2TokenRevokeAfterCacheTimeOutTestCase extends OAuth2ServiceAbstractIntegrationTest{
	private AuthenticatorClient logManger;
	private String adminUsername;
	private String adminPassword;
	private String accessToken;
	private String consumerKey;
	private String consumerSecret;
	public static final String SCOPE_PRODUCTION = "PRODUCTION";
	public static final String GRANT_TYPE_PASSWORD = "password";
	//TODO Remove hardcoded backend urls of cluster and the application key and secret once platform tests are configured
	private String tokenAPIEndpoint = "http://localhost:8280/token";
	private String revokeTokenAPIEndpoint = "http://localhost:8290/revoke";
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

	/**
	 * This tests written for test for token revocation after cache timed out CARBON-15028
	 * This test needed two APIM nodes with clustering enabled
	 * During the test one node is use to generate the token and other node use to revoke the token
	 * After cache timeout new token should issued after it revoked
	 * @throws Exception
	 */
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
	@Test(groups = {"wso2.am"}, description = "Revoke token after cache timed out")
	public void testRevokeTokenAfterCacheTimedOut() throws Exception {
		//Application utils
		OAuthConsumerAppDTO appDto = createApplication();
		consumerKey = appDto.getOauthConsumerKey();
		consumerSecret = appDto.getOauthConsumerSecret();
		//request for token
		String token = requestAccessToken(consumerKey, consumerSecret, tokenAPIEndpoint);
		//Sleep for 15m for cache timeouth
		Thread.sleep(15*60*1000);
		//Revoke access token
		revokeAccessToken(consumerKey, consumerSecret, token, revokeTokenAPIEndpoint);
		//Generate new token
		String newToken = requestAccessToken(consumerKey, consumerSecret, tokenAPIEndpoint);
		Assert.assertNotEquals(token, newToken, "Token revocation failed");
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		removeOAuthApplicationData();
		logManger = null;
		consumerKey = null;
		accessToken = null;
	}

	/**
	 * Request access token from the given token generation endpoint
	 * @param consumerKey consumer key of the application
	 * @param consumerSecret consumer secret of the application
	 * @param backendUrl token generation API endpoint
	 * @return token
	 * @throws Exception if something went wrong when requesting token
	 */
	public static String requestAccessToken(String consumerKey, String consumerSecret,
	                                        String backendUrl) throws Exception {
		ArrayList<NameValuePair> postParameters;
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost    = new HttpPost(backendUrl);
		//generate post request
		httpPost.setHeader("Authorization" , "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
		httpPost.setHeader("Content-Type" , "application/x-www-form-urlencoded");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("username", "admin"));
		postParameters.add(new BasicNameValuePair("password", "admin"));
		postParameters.add(new BasicNameValuePair("scope", SCOPE_PRODUCTION));
		postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
		httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
		HttpResponse response = client.execute(httpPost);
		String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
		//Get access token from the response
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(responseString);
		return json.get("access_token").toString();
	}

	/**
	 * Request access token from the given token generation endpoint
	 * @param consumerKey consumer key of the application
	 * @param consumerSecret consumer secret of the application
	 * @param backendUrl token generation API endpoint
	 * @param accessToken access token to be revoked
	 * @throws Exception if something went wrong when requesting token
	 */
	public static void revokeAccessToken(String consumerKey, String consumerSecret,
	                                     String accessToken, String backendUrl) throws Exception {
		ArrayList<NameValuePair> postParameters;
		HttpClient client = new DefaultHttpClient();
		HttpPost httpRevoke    = new HttpPost(backendUrl);
		//Generate revoke token post request
		httpRevoke.setHeader("Authorization" , "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
		httpRevoke.setHeader("Content-Type" , "application/x-www-form-urlencoded");
		postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("token", accessToken));
		httpRevoke.setEntity(new UrlEncodedFormEntity(postParameters));
		client.execute(httpRevoke);
	}

	/**
	 * Get base64 encoded string of consumer key and secret
	 * @param consumerKey consumer key of the application
	 * @param consumerSecret consumer secret of the application
	 * @return base 64 encoded string
	 */
	private static String getBase64EncodedString(String consumerKey, String consumerSecret) {
		return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
	}
}
