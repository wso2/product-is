/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.CommonConstants.DEFAULT_TOMCAT_PORT;

/**
 * Integration test for cross protocol logout.
 *
 * This test case will create an OIDC app and a SAML app.
 * The two tests will SSO the two apps and then logout from one app
 * and check the other app has also logged out by trying to login from
 * the other app and confirm that the login form is prompted.
 */
public class CrossProtocolLogoutTestCase extends ISIntegrationTest {

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient client;
    private final String OIDC_APP_NAME = "playground2";
    private final String SAML_ISSUER = "travelocity.com";
    private String oidcAppClientId = "";
    private String oidcAppClientSecret = "";
    private OauthAdminClient adminClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private final String SAML_SSO_LOGIN_URL =
            "http://localhost:" + DEFAULT_TOMCAT_PORT + "/travelocity.com/samlsso?SAML2.HTTPBinding=HTTP-POST";
    private final String ACS_URL = "http://localhost:" + DEFAULT_TOMCAT_PORT + "/travelocity.com/home.jsp";
    private final String ACS_URL_PARAMETERIZED = "http://localhost:" + DEFAULT_TOMCAT_PORT + "/%s/home.jsp";
    private String SAML_SSO_URL;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    private final String CONSENT = "consent";
    private final String APPROVE = "approve";
    private final String SCOPE_APPROVAL = "scope-approval";
    private final String USER_AGENT = "User-Agent";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        init();
        createOIDCApplication();
        createServiceProvider();
        createSAMLApplication();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        SAML_SSO_URL = identityContextUrls.getWebAppURLHttps() + "/samlsso";
    }

    @AfterClass(alwaysRun = true)
    public void testCleanUp() throws Exception {

        removeApplications();
    }

    protected void init() throws Exception {

        super.init();
        adminClient = new OauthAdminClient(backendURL, sessionCookie);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
    }

    @Test(groups = "wso2.is", description = "Test cross protocol logout for SAML.")
    public void testSAMLLogout() {

        // Login.
        initiateOIDCRequest(false);
        performOIDCLogin();
        performOIDCConsentApproval();
        initiateSAMLRequest();

        // OIDC app logout.
        performOIDCLogout();

        // Confirm SAML logout.
        confirmLoginPromptForSAML();
    }

    @Test(groups = "wso2.is", description = "Test cross protocol logout for OIDC.")
    public void testOIDCLogout() {

        // Login.
        initiateOIDCRequest(false);
        performOIDCLogin();
        performOIDCConsentApproval();
        initiateSAMLRequest();

        // SAML app logout.
        performSAMLLogout();

        // Confirm OIDC logout.
        initiateOIDCRequest(true);
    }

    private void createOIDCApplication() {

        try {
            OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
            appDTO.setApplicationName(OIDC_APP_NAME);
            appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
            appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
            appDTO.setGrantTypes(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
            appDTO.setBackChannelLogoutUrl("http://localhost:" + DEFAULT_TOMCAT_PORT + "/playground2/bclogout");

            adminClient.registerOAuthApplicationData(appDTO);
            OAuthConsumerAppDTO createdApp = adminClient.getOAuthAppByName(OIDC_APP_NAME);
            Assert.assertNotNull(createdApp, "Adding OIDC app failed.");
            oidcAppClientId = createdApp.getOauthConsumerKey();
            oidcAppClientSecret = createdApp.getOauthConsumerSecret();
        } catch (Exception e) {
            Assert.fail("Adding OIDC app failed.", e);
        }
    }

    private void createSAMLApplication() {

        try {
            SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
            samlssoServiceProviderDTO.setIssuer(SAML_ISSUER);
            samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{ACS_URL});
            samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(ACS_URL);
            samlssoServiceProviderDTO.setAttributeConsumingServiceIndex("1239245949");
            samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
            samlssoServiceProviderDTO.setDoSignAssertions(true);
            samlssoServiceProviderDTO.setDoSignResponse(true);
            samlssoServiceProviderDTO.setDoSingleLogout(true);
            samlssoServiceProviderDTO.setLoginPageURL("/carbon/admin/login.jsp");

            boolean isAddSuccess = ssoConfigServiceClient.addServiceProvider(samlssoServiceProviderDTO);
            Assert.assertTrue(isAddSuccess, "Adding SAML app failed.");
        } catch (Exception e) {
            Assert.fail("Adding SAML app failed.", e);
        }
    }

    private void removeApplications() throws Exception {

        adminClient.removeOAuthApplicationData(oidcAppClientId);
        applicationManagementServiceClient.deleteApplication(OIDC_APP_NAME);
        ssoConfigServiceClient.removeServiceProvider(SAML_ISSUER);
    }

    private void initiateOIDCRequest(boolean isCheckLogoutConfirmation) {

        try {
            List<NameValuePair> urlParameters = getOIDCInitiationRequestParams();
            HttpResponse response = sendPostRequestWithParameters(urlParameters,
                    "http://localhost:" + DEFAULT_TOMCAT_PORT + "/playground2/oauth2-authorize-user.jsp");
            Assert.assertNotNull(response, "Authorization response is null");
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Authorization response header is null.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(locationHeader.getValue());
            sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            if (isCheckLogoutConfirmation) {
                Assert.assertNotNull(sessionDataKey, "Cross protocol logout failed for OIDC.");
            } else {
                Assert.assertNotNull(sessionDataKey, "sessionDataKey is null for ." + OIDC_APP_NAME);
            }
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("OIDC initiation request failed.", e);
        }
    }

    private void performOIDCLogin() {

        try {
            HttpResponse response = sendLoginPost(sessionDataKey);
            Assert.assertNotNull(response, "OIDC login request response is null.");
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "OIDC login response header is null.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(
                    response, keyPositionMap);
            Assert.assertNotNull(keyValues, "SessionDataKeyConsent keyValues map is null.");
            sessionDataKeyConsent = keyValues.get(0).getValue();
            Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("OIDC login failed.", e);
        }
    }

    private void performOIDCConsentApproval() {

        try {
            HttpResponse response = sendApprovalPostWithConsent(sessionDataKeyConsent);
            Assert.assertNotNull(response, "OIDC consent approval request response is null.");
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "OIDC consent approval request location header is null.");
            EntityUtils.consume(response.getEntity());

            response = sendPostRequest(locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("Authorization Code", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractTableRowDataFromResponse(response, keyPositionMap);
            Assert.assertNotNull(keyValues, "Authorization code not received.");
            String authorizationCode = keyValues.get(0).getValue();
            Assert.assertNotNull(authorizationCode, "Authorization code not received.");
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("OIDC consent approval request failed.", e);
        }
    }

    private void performOIDCLogout() {

        try {
            String oidcLogoutUrl = identityContextUrls.getWebAppURLHttps() + "/oidc/logout";
            HttpResponse response = sendGetRequest(oidcLogoutUrl);
            EntityUtils.consume(response.getEntity());

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(CONSENT, APPROVE));
            response = sendPostRequestWithParameters(urlParameters, oidcLogoutUrl);
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(locationHeader.getValue());
            Assert.assertNotNull(response, "OIDC Logout failed.");
            String result = DataExtractUtil.getContentData(response);
            Assert.assertTrue(result.contains("You have successfully logged out"), "OIDC logout failed.");
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            Assert.fail("OIDC Logout failed.", e);
        }
    }

    private void initiateSAMLRequest() {

        try {
            HttpResponse response = sendGetRequest(SAML_SSO_LOGIN_URL);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
            if (Utils.requestMissingClaims(response)) {
                EntityUtils.consume(response.getEntity());
                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());
                response = Utils.sendPOSTConsentMessage(response, OAuth2Constant.COMMON_AUTH_URL,
                        OAuth2Constant.USER_AGENT, ACS_URL, client, pastrCookie);
                EntityUtils.consume(response.getEntity());
            }

            String redirectUrl = Utils.getRedirectUrl(response);
            if (StringUtils.isNotBlank(redirectUrl)) {
                response = Utils.sendRedirectRequest(response, OAuth2Constant.USER_AGENT, ACS_URL_PARAMETERIZED,
                        SAML_ISSUER, client);
            }
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(ACS_URL, CommonConstants.SAML_RESPONSE_PARAM, samlResponse);
            String resultPage = DataExtractUtil.getContentData(response);
            Assert.assertTrue(resultPage.contains("You are logged in as admin"), "SAML login failed.");
        } catch (Exception e) {
            Assert.fail("SAML initiation request failed.", e);
        }
    }

    private void performSAMLLogout() {

        try {
            HttpResponse response = sendGetRequest("http://localhost:" + DEFAULT_TOMCAT_PORT +
                    "/travelocity.com/logout?SAML2.HTTPBinding=HTTP-POST");
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            response = sendSAMLMessage(ACS_URL, CommonConstants.SAML_RESPONSE_PARAM, samlResponse);
            String resultPage = DataExtractUtil.getContentData(response);
            EntityUtils.consume(response.getEntity());
            Assert.assertTrue(resultPage.contains("index.jsp") && !resultPage.contains("error"),
                    "SAML Logout failed.");
        } catch (Exception e) {
            Assert.fail("SAML Logout failed.", e);
        }
    }

    private void confirmLoginPromptForSAML() {

        try {
            HttpResponse response = sendGetRequest(SAML_SSO_LOGIN_URL);
            String samlRequest = Utils.extractDataFromResponse(response, CommonConstants.SAML_REQUEST_PARAM, 5);
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest);
            EntityUtils.consume(response.getEntity());

            response = Utils.sendRedirectRequest(response, OAuth2Constant.USER_AGENT, ACS_URL_PARAMETERIZED,
                    SAML_ISSUER, client);
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            Assert.assertNotNull(sessionKey, "Cross protocol logout failed for SAML.");
        } catch (Exception e) {
            Assert.fail("SAML confirm login prompt failed.", e);
        }
    }

    public HttpResponse sendGetRequest(String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        return client.execute(getRequest);
    }

    public HttpResponse sendPostRequest(String locationURL) throws IOException {

        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        return client.execute(postRequest);
    }

    private HttpResponse sendApprovalPostWithConsent(String sessionDataKeyConsent) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CONSENT, APPROVE));
        urlParameters.add(new BasicNameValuePair(SCOPE_APPROVAL, APPROVE));
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));
        return sendPostRequestWithParameters(urlParameters, OAuth2Constant.APPROVAL_URL);
    }

    public HttpResponse sendLoginPost(String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(urlParameters, OAuth2Constant.COMMON_AUTH_URL);
    }

    public HttpResponse sendPostRequestWithParameters(List<NameValuePair> urlParameters, String url) throws
            IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(request);
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost post = new HttpPost(url);
        post.setHeader(USER_AGENT, OAuth2Constant.USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(post);
    }

    private List<NameValuePair> getOIDCInitiationRequestParams() {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", oidcAppClientId));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        return urlParameters;
    }

    private ServiceProvider createServiceProvider() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(OIDC_APP_NAME);
        serviceProvider.setManagementApp(true);
        applicationManagementServiceClient.createApplication(serviceProvider);
        serviceProvider = applicationManagementServiceClient.getApplication(OIDC_APP_NAME);
        serviceProvider.getApplicationID();

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(oidcAppClientId);
        requestConfig.setInboundAuthType("oauth2");
        if (StringUtils.isNotBlank(oidcAppClientSecret)) {
            Property property = new Property();
            property.setName("oauthConsumerSecret");
            property.setValue(oidcAppClientSecret);
            Property[] properties = {property};
            requestConfig.setProperties(properties);
        }

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{requestConfig});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
        return serviceProvider;
    }
}
