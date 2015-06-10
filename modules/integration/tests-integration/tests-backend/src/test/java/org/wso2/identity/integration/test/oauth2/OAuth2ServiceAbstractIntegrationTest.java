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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* OAuth2 test integration abstraction
*/
public class OAuth2ServiceAbstractIntegrationTest extends ISIntegrationTest {
	protected String consumerKey;
	protected String consumerSecret;

	private final static String SERVICE_PROVIDER_NAME = "PlaygroundServiceProver";
	private final static String SERVICE_PROVIDER_DESC = "Playground Service Prover";
	private final static int TOMCAT_PORT = 8090;

	protected ApplicationManagementServiceClient appMgtclient;
	protected OauthAdminClient adminClient;

	/**
	 * Initialize
	 *
	 * @param userMode
	 *            - User Id
	 * @throws Exception
	 */
	protected void init(TestUserMode userMode) throws Exception {
		super.init(userMode);
		appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
		adminClient = new OauthAdminClient(backendURL, sessionCookie);
	}

	/**
	 * Create Application
	 *
	 * @return OAuthConsumerAppDTO
	 * @throws Exception
	 */
	public OAuthConsumerAppDTO createApplication() throws Exception {
		OAuthConsumerAppDTO appDtoResult = null;

		OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
		appDTO.setApplicationName(org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH_APPLICATION_NAME);
		appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
		appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
		appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
		                     "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

		adminClient.registerOAuthApplicationData(appDTO);
		OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

		for (OAuthConsumerAppDTO appDto : appDtos) {
			if (appDto.getApplicationName().equals(OAuth2Constant.OAUTH_APPLICATION_NAME)) {
				appDtoResult = appDto;
				consumerKey = appDto.getOauthConsumerKey();
				consumerSecret = appDto.getOauthConsumerSecret();
			}
		}
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
		serviceProvider.setDescription(SERVICE_PROVIDER_DESC);
		appMgtclient.createApplication(serviceProvider);

		serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
		serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
		List<InboundAuthenticationRequestConfig> authRequestList =
		                                                           new ArrayList<InboundAuthenticationRequestConfig>();

		if (consumerKey != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest =
			                                                               new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(consumerKey);
			opicAuthenticationRequest.setInboundAuthType("oauth2");
			if (consumerSecret != null && !consumerSecret.isEmpty()) {
				Property property = new Property();
				property.setName("oauthConsumerSecret");
				property.setValue(consumerSecret);
				Property[] properties = { property };
				opicAuthenticationRequest.setProperties(properties);
			}
			authRequestList.add(opicAuthenticationRequest);
		}

		String passiveSTSRealm = SERVICE_PROVIDER_NAME;
		if (passiveSTSRealm != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest =
			                                                               new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
			opicAuthenticationRequest.setInboundAuthType("passivests");
			authRequestList.add(opicAuthenticationRequest);
		}

		String openidRealm = SERVICE_PROVIDER_NAME;
		if (openidRealm != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest =
			                                                               new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(openidRealm);
			opicAuthenticationRequest.setInboundAuthType("openid");
			authRequestList.add(opicAuthenticationRequest);
		}

		if (authRequestList.size() > 0) {
			serviceProvider.getInboundAuthenticationConfig()
			               .setInboundAuthenticationRequestConfigs(authRequestList.toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
		}
		appMgtclient.updateApplicationData(serviceProvider);
		return appDtoResult;
	}

	/**
	 * Send post request with parameters
	 * @param client
	 * @param urlParameters
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url) throws ClientProtocolException,
	                                                         IOException {
		HttpPost request = new HttpPost(url);
		request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		request.setEntity(new UrlEncodedFormEntity(urlParameters));

		HttpResponse response = client.execute(request);
		return response;
	}

	/**
	 * Send Get request
	 *
	 * @param client
	 *            - http Client
	 * @param locationURL
	 *            - Get url location
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendGetRequest(HttpClient client, String locationURL)
	                                                                         throws
	                                                                         ClientProtocolException,
	                                                                         IOException {
		HttpGet getRequest = new HttpGet(locationURL);
		getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		HttpResponse response = client.execute(getRequest);

		return response;
	}

	/**
	 * Send Post request
	 *
	 * @param client
	 *            - http Client
	 * @param locationURL
	 *            - Post url location
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendPostRequest(HttpClient client, String locationURL)
	                                                                          throws ClientProtocolException,
	                                                                          IOException {
		HttpPost postRequest = new HttpPost(locationURL);
		postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		HttpResponse response = client.execute(postRequest);

		return response;
	}

	/**
	 * Send login post request
	 *
	 * @param client
	 *            - Http client
	 * @param sessionDataKey
	 *            - Session data key
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey)
	                                                                           throws ClientProtocolException,
	                                                                           IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("username", userInfo.getUserName()));
		urlParameters.add(new BasicNameValuePair("password", userInfo.getPassword()));
		urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

		HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);

		return response;
	}

	/**
	 * Send approval post request
	 *
	 * @param client
	 *            - http client
	 * @param sessionDataKeyConsent
	 *            - session consent data
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendApprovalPost(HttpClient client, String sessionDataKeyConsent)
	                                                                                     throws ClientProtocolException,
	                                                                                     IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("consent", "approve"));
		urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

		HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.APPROVAL_URL);

		return response;
	}

	/**
	 * Send approval post request
	 *
	 * @param client
	 *            - http client
	 * @param consumerSecret
	 *            - consumer secret
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendGetAccessTokenPost(HttpClient client, String consumerSecret)
	                                                                                    throws ClientProtocolException,
	                                                                                    IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
		urlParameters.add(new BasicNameValuePair("accessEndpoint",
		                                         OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
		HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.GET_ACCESS_TOKEN_URL);

		return response;
	}

	/**
	 * Send validate access token post request
	 * @param client - http client
	 * @param accessToken - access token
	 * @return http response
	 * @throws ClientProtocolException
	 * @throws java.io.IOException
	 */
	public HttpResponse sendValidateAccessTokenPost(HttpClient client, String accessToken)
	                                                                                    throws ClientProtocolException,
	                                                                                    IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("accessToken", accessToken));

		HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.ACCESS_RESOURCES_URL);

		return response;
	}

	/**
	 * Delete Application
	 *
	 * @throws Exception
	 */
	public void deleteApplication() throws Exception {
		appMgtclient.deleteApplication(SERVICE_PROVIDER_NAME);
	}

	/**
	 * Remove OAuth Application
	 *
	 * @throws Exception
	 */
	public void removeOAuthApplicationData() throws Exception {
		adminClient.removeOAuthApplicationData(consumerKey);
	}

	/**
	 * Start Tomcat server instance
	 *
	 * @param tomcat
	 *            - Tomcat Instance
	 * @param webAppUrl
	 *            - Web Application URL
	 * @param webAppPath
	 *            - Application war file path
	 * @throws LifecycleException
	 */
	public void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
	                                                                           throws LifecycleException {
		tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
		tomcat.start();
	}

	/**
	 * Stop
	 *
	 * @param tomcat
	 * @throws LifecycleException
	 */
	public void stopTomcat(Tomcat tomcat) throws LifecycleException {
		tomcat.stop();
		tomcat.destroy();
	}

	/**
	 * Create Tomcat server instance
	 *
	 * @return tomcat instance
	 */
	public Tomcat getTomcat() {
		Tomcat tomcat = new Tomcat();
		tomcat.getService().setContainer(tomcat.getEngine());
		tomcat.setPort(TOMCAT_PORT);
		tomcat.setBaseDir("");

		StandardHost stdHost = (StandardHost) tomcat.getHost();

		stdHost.setAppBase("");
		stdHost.setAutoDeploy(true);
		stdHost.setDeployOnStartup(true);
		stdHost.setUnpackWARs(true);
		tomcat.setHost(stdHost);

		return tomcat;
	}
}
