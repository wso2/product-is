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

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.*;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import sun.security.provider.X509Factory;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
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
	protected static final String COUNTRY_CLAIM_URI = "http://wso2.org/claims/country";
	private static final String customClaimURI1 = "http://wso2.org/claims/challengeQuestion1";
	private static final String customClaimURI2 = "http://wso2.org/claims/challengeQuestion2";
	private static final String GRANT_TYPE_PASSWORD = "password";
	private static final String SCOPE_PRODUCTION = "PRODUCTION";
	public static final String OIDC = "oidc";
	public static final String SAML = "saml";
	private final static int TOMCAT_PORT = 8490;

	protected ApplicationManagementServiceClient appMgtclient;
	protected OauthAdminClient adminClient;
	protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
	protected OAuth2RestClient restClient;


	/**
	 * Initialize
	 *
	 * @param userMode - User Id
	 * @throws Exception Exception
	 */
	protected void init(TestUserMode userMode) throws Exception {
		super.init(userMode);
		appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
		adminClient = new OauthAdminClient(backendURL, sessionCookie);
		remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
		restClient = new OAuth2RestClient(serverURL, tenantInfo);
	}

	/**
	 * Create Application with the given app configurations
	 *
	 * @return OAuthConsumerAppDTO
	 * @throws Exception Exception
	 */
	public OAuthConsumerAppDTO createApplication() throws Exception {
		OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
		appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
		appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
		appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
		appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
				+ "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
		return createApplication(appDTO, SERVICE_PROVIDER_NAME);
	}

    public ApplicationResponseModel addApplication() throws Exception {

		ApplicationModel application = new ApplicationModel();

		List<String> grantTypes = new ArrayList<>();
		Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
				"refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

		List<String> callBackUrls = new ArrayList<>();
		Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

		OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
		oidcConfig.setGrantTypes(grantTypes);
		oidcConfig.setCallbackURLs(callBackUrls);

		InboundProtocols inboundProtocolsConfig = new InboundProtocols();
		inboundProtocolsConfig.setOidc(oidcConfig);

		application.setInboundProtocolConfiguration(inboundProtocolsConfig);
		application.setName(SERVICE_PROVIDER_NAME);
		application.setIsManagementApp(true);

		application.setClaimConfiguration(setApplicationClaimConfig()); ;

		String appId = addApplication(application);

		return getApplication(appId);
	}

	protected ClaimConfiguration setApplicationClaimConfig() {

		ClaimMappings emailClaim = new ClaimMappings().applicationClaim(EMAIL_CLAIM_URI);
		emailClaim.setLocalClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(EMAIL_CLAIM_URI));
		ClaimMappings countryClaim = new ClaimMappings().applicationClaim(COUNTRY_CLAIM_URI);
		countryClaim.setLocalClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(COUNTRY_CLAIM_URI));

		RequestedClaimConfiguration emailRequestedClaim = new RequestedClaimConfiguration();
		emailRequestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(EMAIL_CLAIM_URI));
		RequestedClaimConfiguration countryRequestedClaim = new RequestedClaimConfiguration();
		countryRequestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(COUNTRY_CLAIM_URI));

		ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(DialectEnum.CUSTOM);
		claimConfiguration.addClaimMappingsItem(emailClaim);
		claimConfiguration.addClaimMappingsItem(countryClaim);
		claimConfiguration.addRequestedClaimsItem(emailRequestedClaim);
		claimConfiguration.addRequestedClaimsItem(countryRequestedClaim);

		return claimConfiguration;
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
	 * Create Application with a given ApplicationModel
	 *
	 * @param application application creation object
	 * @return application id
	 * @throws Exception Exception
	 */
	public String addApplication(ApplicationModel application) throws Exception {
		return restClient.createApplication(application);
	}

	/**
	 * Get Application details with a given id
	 *
	 * @param appId application Id
	 * @return ApplicationResponseModel
	 * @throws Exception Exception
	 */
	public ApplicationResponseModel getApplication(String appId) throws Exception {
		return restClient.getApplication(appId);
	}

	/**
	 * Get Application details with a given id
	 *
	 * @param appId application Id
	 * @param application application update patch object
	 * @throws Exception Exception
	 */
	public void updateApplication(String appId, ApplicationPatchModel application) throws Exception {
		restClient.updateApplication(appId, application);
	}

	/**
	 * Get Application oidc inbound configuration details with a given id
	 *
	 * @param appId application Id
	 * @return OpenIDConnectConfiguration
	 * @throws Exception Exception
	 */
	public OpenIDConnectConfiguration getOIDCInboundDetailsOfApplication(String appId) throws Exception {
		return restClient.getOIDCInboundDetails(appId);
	}

	/**
	 * Get Application saml inbound configuration details with a given id
	 *
	 * @param appId application Id
	 * @return SAML2ServiceProvider
	 * @throws Exception Exception
	 */
	public SAML2ServiceProvider getSAMLInboundDetailsOfApplication(String appId) throws Exception {
		return restClient.getSAMLInboundDetails(appId);
	}

	/**
	 * Update Application inbound configuration details with a given id and the inbound Type
	 *
	 * @param appId application Id
	 * @param InboundConfig InboundConfig object
	 * @param inboundType inbound configuration type
	 */
	public void updateApplicationInboundConfig(String appId, Object InboundConfig, String inboundType)
			throws IOException {
		restClient.updateInboundDetailsOfApplication(appId, InboundConfig, inboundType);
	}

	public OAuthConsumerAppDTO createApplication(OAuthConsumerAppDTO appDTO, String serviceProviderName)
			throws Exception {
		OAuthConsumerAppDTO appDtoResult = null;

		adminClient.registerOAuthApplicationData(appDTO);
		OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

		for (OAuthConsumerAppDTO appDto : appDtos) {
			if (appDto.getApplicationName().equals(appDTO.getApplicationName())) {
				appDtoResult = appDto;
				consumerKey = appDto.getOauthConsumerKey();
				consumerSecret = appDto.getOauthConsumerSecret();
			}
		}
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setApplicationName(serviceProviderName);
		serviceProvider.setDescription(SERVICE_PROVIDER_DESC);
		serviceProvider.setManagementApp(true);
		appMgtclient.createApplication(serviceProvider);

		serviceProvider = appMgtclient.getApplication(serviceProviderName);
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

		String passiveSTSRealm = serviceProviderName;
		if (passiveSTSRealm != null) {
			InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
			opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
			opicAuthenticationRequest.setInboundAuthType("passivests");
			authRequestList.add(opicAuthenticationRequest);
		}

		String openidRealm = serviceProviderName;
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

	public void UpdateApplicationClaimConfig(String appId) throws Exception {

		ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
		applicationPatch.setClaimConfiguration(getClaimConfigurations());
		restClient.updateApplication(appId, applicationPatch);
	}

	private ClaimConfiguration getClaimConfigurations() {

		ClaimConfiguration claimConfiguration = new ClaimConfiguration().dialect(DialectEnum.CUSTOM);
		claimConfiguration.addClaimMappingsItem(getClaimMapping(EMAIL_CLAIM_URI));
		claimConfiguration.addRequestedClaimsItem(getRequestedClaim(EMAIL_CLAIM_URI));

		claimConfiguration.addClaimMappingsItem(getClaimMapping(GIVEN_NAME_CLAIM_URI));
		claimConfiguration.addRequestedClaimsItem(getRequestedClaim(GIVEN_NAME_CLAIM_URI));

		claimConfiguration.addClaimMappingsItem(getClaimMapping(COUNTRY_CLAIM_URI));
		claimConfiguration.addRequestedClaimsItem(getRequestedClaim(COUNTRY_CLAIM_URI));

		claimConfiguration.addClaimMappingsItem(getClaimMapping(customClaimURI1));
		claimConfiguration.addRequestedClaimsItem(getRequestedClaim(customClaimURI1));

		claimConfiguration.addClaimMappingsItem(getClaimMapping(customClaimURI2));
		claimConfiguration.addRequestedClaimsItem(getRequestedClaim(customClaimURI2));

		return claimConfiguration;
	}

	private ClaimMappings getClaimMapping(String claimUri) {
		ClaimMappings claim = new ClaimMappings().applicationClaim(claimUri);
		claim.setLocalClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(claimUri));
		return claim;
	}

	private RequestedClaimConfiguration getRequestedClaim(String claimUri) {
		RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
		requestedClaim.setClaim(new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim().uri(claimUri));
		return requestedClaim;
	}

	/**
	 * Send post request with parameters
	 * @param client HttpClient
	 * @param urlParameters url parameters
	 * @param url endpoint
	 * @return HttpResponse
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
			throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);
		request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		request.setEntity(new UrlEncodedFormEntity(urlParameters));

		return client.execute(request);
	}

	/**
	 * Send Get request
	 *
	 * @param client - http Client
	 * @param locationURL - Get url location
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendGetRequest(HttpClient client, String locationURL) throws ClientProtocolException, IOException {
		HttpGet getRequest = new HttpGet(locationURL);
		getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		return client.execute(getRequest);
	}

	public HttpResponse sendConsentGetRequest(CloseableHttpClient client, String locationURL, CookieStore cookieStore,
											  List<NameValuePair> consentRequiredClaimsFromResponse) throws Exception {

		Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
				.register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
				.build();
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.DEFAULT)
				.build();
		HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieSpecRegistry(cookieSpecRegistry)
				.disableRedirectHandling()
				.setDefaultCookieStore(cookieStore).build();
		HttpGet getRequest = new HttpGet(locationURL);
		getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		HttpResponse response = httpClientWithoutAutoRedirections.execute(getRequest);

		consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
		Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
		HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
		EntityUtils.consume(response.getEntity());
		return httpResponse;
	}

	/**
	 * Send Post request
	 *
	 * @param client - http Client
	 * @param locationURL - Post url location
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendPostRequest(HttpClient client, String locationURL) throws ClientProtocolException,
			IOException {
		HttpPost postRequest = new HttpPost(locationURL);
		postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
		return client.execute(postRequest);
	}

	/**
	 * Send login post request
	 *
	 * @param client - Http client
	 * @param sessionDataKey - Session data key
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws ClientProtocolException,
			IOException {
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("username", userInfo.getUserName()));
		urlParameters.add(new BasicNameValuePair("password", userInfo.getPassword()));
		urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
		log.info(">>> sendLoginPost:sessionDataKey: " + sessionDataKey);
		return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
	}

	/**
	 * Send login post request with given username and password credentials.
	 *
	 * @param client         Http client.
	 * @param sessionDataKey Session data key.
	 * @param username       Username.
	 * @param password       Password.
	 * @return Http response.
	 * @throws ClientProtocolException 	ClientProtocolException
	 * @throws IOException				IOException
	 */
	public HttpResponse sendLoginPostForCustomUsers(HttpClient client, String sessionDataKey, String username,
													String password) throws ClientProtocolException, IOException {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("username", username));
		urlParameters.add(new BasicNameValuePair("password", password));
		urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
		log.info(">>> sendLoginPost:sessionDataKey: " + sessionDataKey);
		return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
	}

	/**
	 * Send login post request for a tenant with given username and password credentials.
	 *
	 * @param client         Http client.
	 * @param sessionDataKey Session data key.
	 * @param username       Username.
	 * @param password       Password.
	 * @param tenantDomain	 Tenant domain.
	 * @return Http response.
	 * @throws ClientProtocolException 	ClientProtocolException
	 * @throws IOException				IOException
	 */
	public HttpResponse sendLoginPostForCustomUsers(HttpClient client, String sessionDataKey, String username,
													String password, String tenantDomain)
			throws ClientProtocolException, IOException {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("username", username));
		urlParameters.add(new BasicNameValuePair("password", password));
		urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
		log.info(">>> sendLoginPost:sessionDataKey: " + sessionDataKey);
		String url = OAuth2Constant.TENANT_COMMON_AUTH_URL.replace(OAuth2Constant.TENANT_PLACEHOLDER, tenantDomain);
		return sendPostRequestWithParameters(client, urlParameters, url);
	}

	/**
	 * Send approval post request
	 *
	 * @param client - http client
	 * @param sessionDataKeyConsent - session consent data
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendApprovalPost(HttpClient client, String sessionDataKeyConsent) throws ClientProtocolException,
			IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("consent", "approve"));
		urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

		return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.APPROVAL_URL, tenantInfo.getDomain()));
	}

	/**
	 * Send approval post request with consent
	 *
	 * @param client http client
	 * @param sessionDataKeyConsent session consent data
	 * @param consentClaims claims requiring user consent
	 * @return http response
	 * @throws java.io.IOException java.io.IOException
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

		return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.APPROVAL_URL, tenantInfo.getDomain()));
	}

	/**
	 * Send approval post request for tenant with consent.
	 *
	 * @param client 				http client.
	 * @param sessionDataKeyConsent session consent data.
	 * @param consentClaims 		claims requiring user consent.
	 * @param tenantDomain 			tenant domain.
	 * @return http response.
	 * @throws java.io.IOException IOException.
	 */
	public HttpResponse sendApprovalPostWithConsent(HttpClient client, String sessionDataKeyConsent,
													List<NameValuePair> consentClaims, String tenantDomain)
			throws IOException {

		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("consent", "approve"));
		urlParameters.add(new BasicNameValuePair("scope-approval", "approve"));
		urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

		if (consentClaims != null) {
			urlParameters.addAll(consentClaims);
		}
		String url = OAuth2Constant.TENANT_APPROVAL_URL.replace(OAuth2Constant.TENANT_PLACEHOLDER, tenantDomain);

		return sendPostRequestWithParameters(client, urlParameters, url);
	}

	/**
	 * Send approval post request
	 *
	 * @param client - http client
	 * @param consumerSecret - consumer secret
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendGetAccessTokenPost(HttpClient client, String consumerSecret) throws ClientProtocolException,
	                                                                                    IOException {
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
		urlParameters.add(new BasicNameValuePair("accessEndpoint",
                getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
		urlParameters.add(new BasicNameValuePair("consumerSecret", consumerSecret));
		return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.GET_ACCESS_TOKEN_URL);
	}

	/**
	 * Send validate access token post request
	 * @param client - http client
	 * @param accessToken - access token
	 * @return http response
	 * @throws ClientProtocolException ClientProtocolException
	 * @throws java.io.IOException java.io.IOException
	 */
	public HttpResponse sendValidateAccessTokenPost(HttpClient client, String accessToken)
	                                                                                    throws ClientProtocolException,
	                                                                                    IOException {
		List<NameValuePair> urlParameters = new ArrayList<>();
		urlParameters.add(new BasicNameValuePair("accessToken", accessToken));

		return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.ACCESS_RESOURCES_URL);
	}

	/**
	 * Send token introspection post request according to the tenant domain.
	 * @param client - http client
	 * @param accessToken - access token
	 * @param endpoint - Introspection URL of the tenant domain.
	 * @return JSON object of the response.
	 * @throws Exception Exception
	 */
	public JSONObject introspectTokenWithTenant(HttpClient client, String accessToken, String endpoint, String key,
												String secret) throws Exception {

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("token", accessToken));
		return responseObject(client, endpoint, urlParameters, "Basic " + getBase64EncodedString(key, secret));
	}

	/**
	 * Delete Application
	 *
	 * @throws Exception Exception
	 */
	public void deleteApplication() throws Exception {
		appMgtclient.deleteApplication(SERVICE_PROVIDER_NAME);
	}

	public void deleteApp(String appId) throws Exception {
		restClient.deleteApplication(appId);
	}

	/**
	 * Remove OAuth Application
	 *
	 * @throws Exception Exception
	 */
	public void removeOAuthApplicationData() throws Exception {
		adminClient.removeOAuthApplicationData(consumerKey);
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

	public void updateApplicationCertificate(String appId, X509Certificate sp1X509PublicCert) throws Exception {

		Certificate certificate = new Certificate();
		certificate.setType(Certificate.TypeEnum.PEM);
		certificate.setValue(convertToPem(sp1X509PublicCert));

		ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
		applicationPatch = applicationPatch.advancedConfigurations(new AdvancedApplicationConfiguration());
		applicationPatch.getAdvancedConfigurations().setCertificate(certificate);

		updateApplication(appId, applicationPatch);
	}

	/**
	 * Convert a x509 certificate to pem format.
	 *
	 * @param x509Certificate Certificate in x509 format.
	 * @return Certificate in pem format.
	 * @throws CertificateEncodingException CertificateEncodingException
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
		appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token");
		return appDTO;
	}

	/**
	 * Create and return a basic consumer application with all OAuth2 grant types.
	 *
	 * @param callBackURL String callback URL.
	 * @return ApplicationResponseModel object.
	 */
	public ApplicationResponseModel getBasicOAuthApplication(String callBackURL) throws Exception {

		ApplicationModel application = new ApplicationModel();

		List<String> grantTypes = new ArrayList<>();
		Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
				"refresh_token");

		List<String> callBackUrls = new ArrayList<>();
		Collections.addAll(callBackUrls, callBackURL);

		OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
		oidcConfig.setGrantTypes(grantTypes);
		oidcConfig.setCallbackURLs(callBackUrls);

		InboundProtocols inboundProtocolsConfig = new InboundProtocols();
		inboundProtocolsConfig.setOidc(oidcConfig);

		application.setInboundProtocolConfiguration(inboundProtocolsConfig);
		application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);
		application.isManagementApp(true);

		String appId = addApplication(application);

		return getApplication(appId);
	}

	/**
	 * Register a service provider and setup consumer key and secret when a OAuthConsumerAppDTO is given.
	 *
	 * @param appDTO OAuthConsumerAppDTO of the service provider.
	 * @return Registered service provider.
	 * @throws Exception Exception
	 */
	public ServiceProvider registerServiceProviderWithOAuthInboundConfigs(OAuthConsumerAppDTO appDTO)
			throws Exception {

		ServiceProvider serviceProvider = generateServiceProvider(appDTO);
		return getServiceProvider(serviceProvider);
	}

	protected ServiceProvider generateServiceProvider(OAuthConsumerAppDTO appDTO) throws Exception {

		adminClient.registerOAuthApplicationData(appDTO);

		OAuthConsumerAppDTO oauthConsumerApp = adminClient.getOAuthAppByName(appDTO.getApplicationName());
		consumerKey = oauthConsumerApp.getOauthConsumerKey();
		consumerSecret = oauthConsumerApp.getOauthConsumerSecret();

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
		serviceProvider.setManagementApp(true);
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

	protected ServiceProvider getServiceProvider(ServiceProvider serviceProvider) throws Exception {

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

	/**
	 * Build post request and return json response object.
	 *
	 * @param endpoint      		Endpoint.
	 * @param postParameters 		postParameters.
	 * @param client            	httpclient.
	 * @param authorizationHeader  	Authentication header.
	 * @return JSON object of the response.
	 * @throws Exception Exception
	 */
	private JSONObject responseObject(HttpClient client, String endpoint, List<NameValuePair> postParameters,
									  String authorizationHeader) throws Exception {

		HttpPost httpPost = new HttpPost(endpoint);
		httpPost.setHeader("Authorization", authorizationHeader);
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
		HttpResponse response = client.execute(httpPost);
		String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
		EntityUtils.consume(response.getEntity());
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(responseString);
		if (json == null) {
			throw new Exception("Error occurred while getting the response.");
		}
		return json;
	}

	/**
	 * Get public certificate from jwks endpoint.
	 *
	 * @param client HttpClient.
	 * @param endPoint jwks endpoint.
	 * @return String object of the certificate.
	 * @throws Exception Exception
	 */
	public String getPublicCertificate(CloseableHttpClient client, String endPoint) throws Exception {
		HttpGet request = new HttpGet(endPoint);
		CloseableHttpResponse response = client.execute(request);

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
		return ((JSONArray) ((JSONObject)((JSONArray) json.get("keys")).get(0)).get("x5c")).get(0).toString();
	}

	/**
	 * Authorize list of SYSTEM APIs to an application.
	 *
	 * @param applicationId Application id.
	 * @param apiIdentifiers API identifiers to authorize.
	 * @throws Exception Error occured while authorizing APIs.
	 */
	public void authorizeSystemAPIs(String applicationId, List<String> apiIdentifiers) throws Exception {

		apiIdentifiers.stream().forEach(apiIdentifier -> {
			try {
				List<APIResourceListItem> filteredAPIResource =
						restClient.getAPIResourcesWithFiltering("type+eq+SYSTEM+and+identifier+eq+" + apiIdentifier);
				if (filteredAPIResource == null || filteredAPIResource.isEmpty()) {
					return;
				}
				String apiId = filteredAPIResource.get(0).getId();
				// Get API scopes.
				List<ScopeGetModel> apiResourceScopes = restClient.getAPIResourceScopes(apiId);
				AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
				authorizedAPICreationModel.setId(apiId);
				authorizedAPICreationModel.setPolicyIdentifier("RBAC");
				apiResourceScopes.forEach(scope -> {
					authorizedAPICreationModel.addScopesItem(scope.getName());
				});
				restClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
			} catch (Exception e) {
				throw new RuntimeException("Error while authorizing system API " + apiIdentifier + " to application "
						+ applicationId, e);
			}
		});
	}

	public String getRoleV2ResourceId(String roleName, String audienceType, String OrganizationId) throws Exception {

		List<String> roles = restClient.getRoles(roleName, audienceType, OrganizationId);
		if (roles.size() == 1) {
			return roles.get(0);
		}
		return null;
	}
}
