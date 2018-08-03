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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import sun.security.provider.X509Factory;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH_APPLICATION_NAME;

/**
* OAuth2 test integration abstraction
*/
public class OAuth2ServiceAbstractIntegrationTest extends ISIntegrationTest {
	protected String consumerKey;
	protected String consumerSecret;

	protected final static String SERVICE_PROVIDER_NAME = "PlaygroundServiceProvider";
	protected final static String SERVICE_PROVIDER_DESC = "Playground Service Provider";
	protected static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
	private static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
	private static final String COUNTRY_CLAIM_URI = "http://wso2.org/claims/country";
	private static final String customClaimURI1 = "http://wso2.org/claims/challengeQuestion1";
	private static final String customClaimURI2 = "http://wso2.org/claims/challengeQuestion2";
	private static final String GRANT_TYPE_PASSWORD = "password";
	private static final String SCOPE_PRODUCTION = "PRODUCTION";
	private final static int TOMCAT_PORT = 8490;

	protected ApplicationManagementServiceClient appMgtclient;
	protected OauthAdminClient adminClient;
	protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;


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
		remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
	}

	/**
	 * Create Application with the given app configurations
	 *
	 * @return OAuthConsumerAppDTO
	 * @throws Exception
	 */
	public OAuthConsumerAppDTO createApplication() throws Exception {
		OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
		appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
		appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
		appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
		appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
				+ "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
		return createApplication(appDTO);
	}

    /**
     * To set ServiceProvider Provider Claim configuration.
     *
     * @param serviceProvider Specific Service Provider.
     * @return Relevant service provider with updated claim configurations.
     */
    ServiceProvider setServiceProviderClaimConfig(ServiceProvider serviceProvider) {

        ClaimConfig claimConfig = new ClaimConfig();
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);

        Claim countryClaim = new Claim();
        countryClaim.setClaimUri(COUNTRY_CLAIM_URI);
        ClaimMapping countryClaimMapping = new ClaimMapping();
        countryClaimMapping.setRequested(true);
        countryClaimMapping.setLocalClaim(countryClaim);
        countryClaimMapping.setRemoteClaim(countryClaim);

        claimConfig.setClaimMappings(
                new org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] { emailClaimMapping,
                        countryClaimMapping });

        serviceProvider.setClaimConfig(claimConfig);
        return serviceProvider;
    }

	/**
	 * Create Application with a given appDTO
	 *
	 * @return OAuthConsumerAppDTO
	 * @throws Exception
	 */
	public OAuthConsumerAppDTO createApplication(OAuthConsumerAppDTO appDTO) throws Exception {
		OAuthConsumerAppDTO appDtoResult = null;

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
		serviceProvider = setServiceProviderClaimConfig(serviceProvider);
		serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
		List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

		if (consumerKey != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
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
			InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
			opicAuthenticationRequest.setInboundAuthType("passivests");
			authRequestList.add(opicAuthenticationRequest);
		}

		String openidRealm = SERVICE_PROVIDER_NAME;
		if (openidRealm != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
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

	public void UpdateApplicationClaimConfig() throws Exception {
		ServiceProvider serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
		ClaimConfig claimConfig = getClaimConfig();
		serviceProvider.setClaimConfig(claimConfig);
		appMgtclient.updateApplicationData(serviceProvider);
	}

	private ClaimConfig getClaimConfig() {
		ClaimConfig claimConfig = new ClaimConfig();
		ClaimMapping emailClaimMapping = getClaimMapping(EMAIL_CLAIM_URI);
		ClaimMapping givenNameClaimMapping = getClaimMapping(GIVEN_NAME_CLAIM_URI);
		ClaimMapping countryClaimMapping = getClaimMapping(COUNTRY_CLAIM_URI);
		ClaimMapping customClaimMapping1 = getClaimMapping(customClaimURI1);
		ClaimMapping customClaimMapping2 = getClaimMapping(customClaimURI2);
		claimConfig.setClaimMappings(new org.wso2.carbon.identity.application.common.model.xsd
				.ClaimMapping[]{emailClaimMapping, givenNameClaimMapping, countryClaimMapping, customClaimMapping1,
				customClaimMapping2});
		return claimConfig;
	}

	private ClaimMapping getClaimMapping(String claimUri) {
		Claim claim = new Claim();
		claim.setClaimUri(claimUri);
		ClaimMapping claimMapping = new ClaimMapping();
		claimMapping.setRequested(true);
		claimMapping.setLocalClaim(claim);
		claimMapping.setRemoteClaim(claim);
		return claimMapping;
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

	public HttpResponse sendConsentGetRequest(DefaultHttpClient client, String locationURL, CookieStore cookieStore,
											  List<NameValuePair> consentRequiredClaimsFromResponse) throws Exception {

		HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
																		.setDefaultCookieStore(cookieStore).build();
		HttpGet getRequest = new HttpGet(locationURL);
		getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		HttpResponse response = httpClientWithoutAutoRedirections.execute(getRequest);

		consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
		Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
		client.setCookieStore(cookieStore);
		EntityUtils.consume(response.getEntity());
		return httpResponse;
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
		List<NameValuePair> urlParameters = new ArrayList<>();
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
	 * Send approval post request with consent
	 *
	 * @param client http client
	 * @param sessionDataKeyConsent session consent data
	 * @param consentClaims claims requiring user consent
	 * @return http response
	 * @throws java.io.IOException
	 */
	public HttpResponse sendApprovalPostWithConsent(HttpClient client, String sessionDataKeyConsent,
													List<NameValuePair> consentClaims) throws IOException {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("consent", "approve"));
		urlParameters.add(new BasicNameValuePair("scope-approval", "approve"));
		urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

		if (consentClaims != null) {
			urlParameters.addAll(consentClaims);
		}

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
	public void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath) throws LifecycleException {
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

    /**
     * Request access token from the given token generation endpoint
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @param backendUrl     token generation API endpoint
     * @return token
     * @throws Exception if something went wrong when requesting token
     */
    public String requestAccessToken(String consumerKey, String consumerSecret,
                                     String backendUrl, String username, String password) throws Exception {
        List<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(backendUrl);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", username));
        postParameters.add(new BasicNameValuePair("password", password));
        postParameters.add(new BasicNameValuePair("scope", SCOPE_PRODUCTION));
        postParameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE_PASSWORD));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        //Get access token from the response
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        Object accessToken = json.get("access_token");
        if (accessToken == null) {
            throw new Exception("Error occurred while requesting access token. Access token not found in json response");
        }
        return accessToken.toString();
    }

    /**
     * Get base64 encoded string of consumer key and secret
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @return base 64 encoded string
     */
    public String getBase64EncodedString(String consumerKey, String consumerSecret) {
        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

	/**
	 * Convert a x509 certificate to pem format.
	 *
	 * @param x509Certificate Certificate in x509 format.
	 * @return Certificate in pem format.
	 * @throws CertificateEncodingException
	 */
	public String convertToPem(X509Certificate x509Certificate) throws CertificateEncodingException {

		String certBegin = X509Factory.BEGIN_CERT;
		String endCert = X509Factory.END_CERT;
		String pemCert = new String(java.util.Base64.getEncoder().encode(x509Certificate.getEncoded()));
		return certBegin + pemCert + endCert;
	}

	/**
	 * Build and return a basic consumer application DTO with all OAuth2 grant types.
	 *
	 * @param callBackURL String callback URL.
	 * @return Basic OAuthConsumerAppDTO object.
	 */
	public OAuthConsumerAppDTO getBasicOAuthApp(String callBackURL) {

		OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
		appDTO.setApplicationName(OAUTH_APPLICATION_NAME);
		appDTO.setCallbackUrl(callBackURL);
		appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
		appDTO.setGrantTypes("authorization_code implicit password client_credentials");
		return appDTO;
	}

	/**
	 * Register a service provider and setup consumer key and secret when a OAuthConsumerAppDTO is given.
	 *
	 * @param appDTO OAuthConsumerAppDTO of the service provider.
	 * @return Registered service provider.
	 * @throws Exception
	 */
	public ServiceProvider registerServiceProviderWithOAuthInboundConfigs(OAuthConsumerAppDTO appDTO)
			throws Exception {

		ServiceProvider serviceProvider = generateServiceProvider(appDTO);
		return getServiceProvider(serviceProvider);
	}

	/**
	 * Register a SP with some local and outbound configs.
	 *
	 * @param appDTO OAuthConsumerAppDTO of the service provider.
	 * @return Registered service provider with some local and outbound configs
	 * @throws Exception
	 */
	public ServiceProvider registerServiceProviderWithLocalAndOutboundConfigs(OAuthConsumerAppDTO appDTO)
			throws Exception {

		ServiceProvider serviceProvider = generateServiceProvider(appDTO);
		serviceProvider.getLocalAndOutBoundAuthenticationConfig().setUseTenantDomainInLocalSubjectIdentifier(true);
		return getServiceProvider(serviceProvider);
	}

	private ServiceProvider generateServiceProvider(OAuthConsumerAppDTO appDTO) throws Exception {

		adminClient.registerOAuthApplicationData(appDTO);

		OAuthConsumerAppDTO oauthConsumerApp = adminClient.getOAuthAppByName(appDTO.getApplicationName());
		consumerKey = oauthConsumerApp.getOauthConsumerKey();
		consumerSecret = oauthConsumerApp.getOauthConsumerSecret();

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
		appMgtclient.createApplication(serviceProvider);

		serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);

		List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();
		setInboundOAuthConfig(authRequestList);

		if (authRequestList.size() > 0) {
			serviceProvider.getInboundAuthenticationConfig()
					.setInboundAuthenticationRequestConfigs(authRequestList.toArray(
							new InboundAuthenticationRequestConfig[authRequestList.size()]));
		}
		return serviceProvider;
	}

	private ServiceProvider getServiceProvider(ServiceProvider serviceProvider) throws Exception {

		appMgtclient.updateApplicationData(serviceProvider);
		return appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
	}

	/**
	 * Update app with inbound configurations.
	 *
	 * @param authRequestList Authentication Request Config list.
	 */
	private void setInboundOAuthConfig(List<InboundAuthenticationRequestConfig> authRequestList) {

		if (consumerKey != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest =
					new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(consumerKey);
			opicAuthenticationRequest.setInboundAuthType("oauth2");
			if (consumerSecret != null && !consumerSecret.isEmpty()) {
				Property property = new Property();
				property.setName("oauthConsumerSecret");
				property.setValue(consumerSecret);
				Property[] properties = {property};
				opicAuthenticationRequest.setProperties(properties);
			}
			authRequestList.add(opicAuthenticationRequest);
		}
	}
}
