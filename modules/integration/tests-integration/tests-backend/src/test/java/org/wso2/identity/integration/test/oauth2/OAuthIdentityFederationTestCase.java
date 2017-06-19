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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest.TOMCAT_PORT;
import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuthIdentityFederationTestCase extends ISIntegrationTest {

    private AuthenticatorClient logManger;
    private String adminUsername;
    private String adminPassword;
    private String primaryAccessToken;
    private String primarySessionDataKeyConsent;
    private String primarySessionDataKey;
    private String primaryAuthorizationCode;
    private String primaryConsumerKey;
    private String primaryConsumerSecret;
    private String secondaryAccessToken;
    private String secondarySessionDataKeyConsent;
    private String secondarySessionDataKey;
    private String secondaryAuthorizationCode;
    private String secondaryConsumerKey;
    private String secondaryConsumerSecret;

    private static final String PLAYGROUND_RESET_PAGE = "http://localhost:" + CommonConstants.DEFAULT_TOMCAT_PORT +
            "/playground2/oauth2.jsp?reset=true";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private String secondaryUserName = "federatedOAuthUser";
    private String secondaryUserPwd = "testFederatePassword";
    private String secondaryUserRole = "admin";
    private String SECONDARY_SP_NAME = "ISasServiceProvider";
    private String PRIMARY_SP_NAME = "Playground";
    private static final String OAUTH_AUTHENTICATOR = "OpenIDConnectAuthenticator";
    private static final String OAUTH_AUTHENTICATOR_DISPLAY_NAME = "openidconnect";
    private static final String IDENTITY_PROVIDER_NAME = "ISasIDP";
    private String INBOUND_AUTH_TYPE = "oauth2";
    private String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private String TOKEN_ENDPOINT = "https://localhost:%s/oauth2/token";
    private String AUTHORIZE_ENDPOINT = "https://localhost:%s/oauth2/authorize";
    private static final String AUTHENTICATION_TYPE = "federated";
    private DefaultHttpClient client;
    private MultipleServersManager manager;
    protected Map<Integer, AutomationContext> automationContextMap;
    private Tomcat tomcat;
    protected static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients = new HashMap<>();
    private Map<Integer, IdentityProviderMgtServiceClient> identityProviderMgtServiceClients = new HashMap<>();
    private Map<Integer, OauthAdminClient> oauthAdminClients = new HashMap<>();
    private Map<Integer, RemoteUserStoreManagerServiceClient> remoteUserStoreManagerServiceClients = new HashMap<>();

    public static final String AUTHORIZATION_EP = "AuthorizationEndPoint";
    public static final String TOKEN_EP = "TokenEndPoint";
    public static final String USER_INFO_EP = "UserInfoEndPoint";

    public static final String CLIENT_ID = "ClientId";
    public static final String CLIENT_SECRET = "ClientSecret";
    public static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";
    public static final String OAUTH2_AUTHZ_URL = "OAuth2AuthzEPUrl";
    public static final String OAUTH2_TOKEN_URL = "OAuth2TokenEPUrl";
    public static final String OAUTH2_REVOKE_URL = "OAuth2RevokeEPUrl";
    public static final String OAUTH2_INTROSPECT_URL = "OAuth2IntrospectEPUrl";
    public static final String OAUTH2_USER_INFO_EP_URL = "OAuth2UserInfoEPUrl";
    public static final String CALLBACK_URL = "callbackUrl";
    public static final String QUERY_PARAMS = "commonAuthQueryParams";
    public static final String IS_USER_ID_IN_CLAIM = "IsUserIdInClaims";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        applicationManagementServiceClients = new HashMap<Integer, ApplicationManagementServiceClient>();
        identityProviderMgtServiceClients = new HashMap<Integer, IdentityProviderMgtServiceClient>();
        automationContextMap = new HashMap<Integer, AutomationContext>();
        manager = new MultipleServersManager();

        automationContextMap.put(0, isServer);
        logManger = new AuthenticatorClient(backendURL);
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));
        client = new DefaultHttpClient();
        Map<String, String> startupParameters = new HashMap<String, String>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);

        startCarbonServer(PORT_OFFSET_1, context, startupParameters);

        //Service clients for primary IS
        createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants.ServiceClientType[] {
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT,
                IdentityConstants.ServiceClientType.OAUTH_CONFIG,
                IdentityConstants.ServiceClientType.REMOTE_USER_STORE_MANAGEMENT
        });

        //Service clients for secondary IS
        createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[] {
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.OAUTH_CONFIG
        });

        setSystemproperties();

        //add new test user to secondary IS
        boolean userCreated = addUserToSecondaryIS();
        Assert.assertTrue(userCreated, "User creation failed");

        try {
            tomcat = getTomcat();
            URL resourceUrl = getClass().getResource(File.separator + "samples" + File.separator +
                    "playground2.war");
            startTomcat(tomcat, OAuth2Constant.PLAYGROUND_APP_CONTEXT_ROOT, resourceUrl.getPath());

        } catch (Exception e) {
            Assert.fail("Playground application deployment failed.", e);
        }

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        stopTomcat(tomcat);

        //Delete the primary application
        deleteServiceProvider(PORT_OFFSET_0, PRIMARY_SP_NAME);
        oauthAdminClients.get(PORT_OFFSET_0).removeOAuthApplicationData(primaryConsumerKey);

        //Delete the secondary application
        deleteServiceProvider(PORT_OFFSET_1, SECONDARY_SP_NAME);
        oauthAdminClients.get(PORT_OFFSET_1).removeOAuthApplicationData(secondaryConsumerKey);
        deleteAddedUsers();
        stopCarbonServer(PORT_OFFSET_1);

        logManger = null;
    }

    @Test(priority = 1,
          groups = "wso2.is",
          description = "Check create service provider in secondary IS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {

        OAuthConsumerAppDTO appDTO = createOAuthApplication(SECONDARY_SP_NAME, SECONDARY_SP_NAME,
                String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0), PORT_OFFSET_1);

        Assert.assertNotNull(appDTO, "Creating application should have returned a valid response.");
        secondaryConsumerKey = appDTO.getOauthConsumerKey();
        secondaryConsumerSecret = appDTO.getOauthConsumerSecret();

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_SP_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (appDTO.getOauthConsumerKey().equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE
                        .equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in secondary IS");
    }

    @Test(groups = "wso2.is",
          description = "Check create identity provider in primary IS",
          dependsOnMethods = "testCreateServiceProviderInSecondaryIS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        identityProvider.setAlias(String.format(TOKEN_ENDPOINT,DEFAULT_PORT+PORT_OFFSET_1));
        identityProvider.setEnable(true);
        identityProvider.setIdentityProviderDescription("Test IDP for OIDC federation.");
        identityProvider.setHomeRealmId("FavoriteIDP");
        identityProvider.setCertificate("");

        FederatedAuthenticatorConfig OauthAuthnConfig = new FederatedAuthenticatorConfig();
        OauthAuthnConfig.setName(OAUTH_AUTHENTICATOR);
        OauthAuthnConfig.setDisplayName(OAUTH_AUTHENTICATOR_DISPLAY_NAME);
        OauthAuthnConfig.setEnabled(true);
        OauthAuthnConfig.setProperties(getOAuthAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(OauthAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] { OauthAuthnConfig });

        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        jitConfig.setProvisioningUserStore("PRIMARY");
        identityProvider.setJustInTimeProvisioningConfig(jitConfig);

        addIdentityProvider(PORT_OFFSET_0, identityProvider);

        Assert.assertNotNull(getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME),
                "Failed to create Identity Provider 'ISasIDP' in primary IS");

        Assert.assertNotNull(getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME), "Failed to create Identity Provider 'trustedIdP' in primary IS");
    }

    @Test(groups = "wso2.is",
          description = "Check create service provider in primary IS",
          dependsOnMethods = "testCreateIdentityProviderInPrimaryIS")
    public void testCreateServiceProviderInPrimaryIS() throws Exception {

        ServiceProvider serviceProvider;
        OAuthConsumerAppDTO appDTO = createOAuthApplication(PRIMARY_SP_NAME, PRIMARY_SP_NAME,
                OAuth2Constant.CALLBACK_URL, PORT_OFFSET_0);

        Assert.assertNotNull(appDTO, "Creating application should have returned a valid response.");
        primaryConsumerKey = appDTO.getOauthConsumerKey();
        primaryConsumerSecret = appDTO.getOauthConsumerSecret();

        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        authStep.setFederatedIdentityProviders(
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[] { idP });

        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_SP_NAME);
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setAuthenticationSteps(new AuthenticationStep[] { authStep });
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(AUTHENTICATION_TYPE);

        updateServiceProvider(PORT_OFFSET_0, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_SP_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (appDTO.getOauthConsumerKey().equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE
                        .equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in primary IS");
        Assert.assertTrue(AUTHENTICATION_TYPE
                        .equals(serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationType()),
                "Failed to update local and out bound configs in primary IS.");
    }



    @Test(groups = "wso2.is",
          description = "Send authorize user request without response_type param",
          dependsOnMethods = "testCreateServiceProviderInPrimaryIS")
    public void testSendAuthorizedPostForError() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("client_id", primaryConsumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        AutomationContext automationContext = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        String authorizeEndpoint = automationContext.getContextUrls().getBackEndUrl()
                .replace("services/", "oauth2/authorize");
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, authorizeEndpoint);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertTrue(locationHeader.getValue().contains("redirect_uri"),
                "Error response does not contain redirect_uri");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is",
          description = "Send authorize user request",
          dependsOnMethods = "testCreateServiceProviderInPrimaryIS")
    public void testSendAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", primaryConsumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", ""));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorized response is null");

        String sessionId = extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
        Assert.assertNotNull(sessionId, "Unable to acquire 'sessionDataKey' value");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendFederatedGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        primarySessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(primarySessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());


    }

    @Test(groups = "wso2.is",
          description = "Send login post request",
          dependsOnMethods = "testSendAuthorizedPost")
    public void testSendLoginPost() throws Exception {
        HttpResponse response = sendLoginPost(client, primarySessionDataKey);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        primarySessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(primarySessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is",
          description = "Send approval post request",
          dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {
        HttpResponse response = sendApprovalPost(client, primarySessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(response, "Authorization Code key value is invalid.");
        primaryAuthorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(primaryAuthorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());

    }

    @Test(groups = "wso2.is",
          description = "Get access token",
          dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessToken() throws Exception {
        HttpResponse response = sendGetAccessTokenPost(client, primaryConsumerSecret);
        Assert.assertNotNull(response, "Error occured while getting access token.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);
        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"primaryAccessToken\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractInputValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        primaryAccessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(primaryAccessToken, "Access token is null.");

        EntityUtils.consume(response.getEntity());

    }

    @Test(groups = "wso2.is",
          description = "Validate access token",
          dependsOnMethods = "testGetAccessToken")
    public void testValidateAccessToken() throws Exception {

        HttpResponse response = sendValidateAccessTokenPost(client, primaryAccessToken);
        Assert.assertNotNull(response, "Validate access token response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"valid\"", 1);

        List<KeyValue> keyValues = DataExtractUtil.extractInputValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        String valid = keyValues.get(0).getValue();
        Assert.assertEquals(valid, "true", "Token Validation failed");

        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is",
          description = "Resending authorization code",
          dependsOnMethods = "testValidateAccessToken")
    public void testAuthzCodeResend() throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, primaryAuthorizationCode));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        HttpPost request = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, OAuth2Constant.USER_AGENT);
        request.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " + Base64
                .encodeBase64String((primaryConsumerKey + ":" + primaryConsumerSecret).getBytes()).trim());
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String errorMessage = ((JSONObject) obj).get("error").toString();
        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(OAuth2Constant.INVALID_GRANT_ERROR, errorMessage,
                "Reusing the Authorization code has not" + " revoked the access token issued.");
    }

    @Test(groups = "wso2.is",
          description = "Retrying authorization code flow",
          dependsOnMethods = "testAuthzCodeResend")
    public void testAuthzCodeGrantRetry() throws Exception {
        String oldAccessToken = primaryAccessToken;

        HttpResponse response = sendGetRequest(client, PLAYGROUND_RESET_PAGE);
        EntityUtils.consume(response.getEntity());

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_PLAYGROUND_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CONSUMER_KEY_PLAYGROUND_NAME, primaryConsumerKey));
        urlParameters
                .add(new BasicNameValuePair(OAuth2Constant.CALLBACKURL_PLAYGROUND_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_ENDPOINT_PLAYGROUND_NAME,
                OAuth2Constant.APPROVAL_URL));
        urlParameters
                .add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_PLAYGROUND_NAME, OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.SCOPE_PLAYGROUND_NAME, ""));

        response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        primarySessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(primarySessionDataKeyConsent, "Invalid session key consent.");

        testSendApprovalPost();
        testGetAccessToken();
        Assert.assertNotEquals(oldAccessToken, primaryAccessToken,
                "Access token not revoked from authorization code reusing");
        testAuthzCodeResend();
    }

    /**
     * Start Tomcat server instance
     *
     * @param tomcat     - Tomcat Instance
     * @param webAppUrl  - Web Application URL
     * @param webAppPath - Application war file path
     * @throws LifecycleException
     */
    private void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath) throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    /**
     * Create Tomcat server instance
     *
     * @return tomcat instance
     */
    private Tomcat getTomcat() {
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
     * Stop
     *
     * @param tomcat
     * @throws LifecycleException
     */
    private void stopTomcat(Tomcat tomcat) throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * Send post request with parameters
     *
     * @param client
     * @param urlParameters
     * @param url
     * @return
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws ClientProtocolException, IOException {
        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        return response;
    }

    /**
     * Send Get request
     *
     * @param client      - http Client
     * @param locationURL - Get url location
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendFederatedGetRequest(HttpClient client, String locationURL)
            throws ClientProtocolException, IOException {
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        getRequest.setHeader("Referer", "http://localhost:8490/playground2/oauth2.jsp");
        HttpResponse response = client.execute(getRequest);

        return response;
    }

    /**
     * Send Get request
     *
     * @param client      - http Client
     * @param locationURL - Get url location
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendGetRequest(HttpClient client, String locationURL)
            throws ClientProtocolException, IOException {
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(getRequest);

        return response;
    }

    /**
     * Send Post request
     *
     * @param client      - http Client
     * @param locationURL - Post url location
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendPostRequest(HttpClient client, String locationURL)
            throws ClientProtocolException, IOException {
        HttpPost postRequest = new HttpPost(locationURL);
        postRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = client.execute(postRequest);

        return response;
    }

    /**
     * Send login post request
     *
     * @param client         - Http client
     * @param sessionDataKey - Session data key
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendLoginPost(HttpClient client, String sessionDataKey)
            throws ClientProtocolException, IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", secondaryUserName));
        urlParameters.add(new BasicNameValuePair("password", secondaryUserPwd));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);

        return response;
    }

    /**
     * Send approval post request
     *
     * @param client                - http client
     * @param sessionDataKeyConsent - session consent data
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendApprovalPost(HttpClient client, String sessionDataKeyConsent)
            throws ClientProtocolException, IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        urlParameters.add(new BasicNameValuePair("primarySessionDataKeyConsent", sessionDataKeyConsent));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.APPROVAL_URL);

        return response;
    }

    /**
     * Send approval post request
     *
     * @param client         - http client
     * @param consumerSecret - consumer secret
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendGetAccessTokenPost(HttpClient client, String consumerSecret)
            throws ClientProtocolException, IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("accessEndpoint", OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        urlParameters.add(new BasicNameValuePair("primaryConsumerSecret", consumerSecret));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.GET_ACCESS_TOKEN_URL);

        return response;
    }

    /**
     * Send validate access token post request
     *
     * @param client      - http client
     * @param accessToken - access token
     * @return http response
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    private HttpResponse sendValidateAccessTokenPost(HttpClient client, String accessToken)
            throws ClientProtocolException, IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("primaryAccessToken", accessToken));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.ACCESS_RESOURCES_URL);

        return response;
    }

    public void createServiceClients(int portOffset, String sessionCookie,
            IdentityConstants.ServiceClientType[] adminClients) throws Exception {

        if (adminClients == null) {
            return;
        }

        String serviceUrl = getSecureServiceUrl(portOffset,
                automationContextMap.get(portOffset).getContextUrls().getSecureServiceUrl());

        if (sessionCookie == null) {
            AuthenticatorClient authenticatorClient = new AuthenticatorClient(serviceUrl);

            sessionCookie = authenticatorClient
                    .login(automationContextMap.get(portOffset).getSuperTenant().getTenantAdmin().getUserName(),
                            automationContextMap.get(portOffset).getSuperTenant().getTenantAdmin().getPassword(),
                            automationContextMap.get(portOffset).getDefaultInstance().getHosts().get("default"));
        }

        if (sessionCookie != null) {
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null, null);
            for (IdentityConstants.ServiceClientType clientType : adminClients) {
                if (IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT.equals(clientType)) {
                    applicationManagementServiceClients.put(portOffset,
                            new ApplicationManagementServiceClient(sessionCookie, serviceUrl, configContext));
                } else if (IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT.equals(clientType)) {
                    identityProviderMgtServiceClients
                            .put(portOffset, new IdentityProviderMgtServiceClient(sessionCookie, serviceUrl));
                } else if (IdentityConstants.ServiceClientType.OAUTH_CONFIG.equals(clientType)) {
                    oauthAdminClients.put(portOffset, new OauthAdminClient(serviceUrl, sessionCookie));
                } else if (IdentityConstants.ServiceClientType.REMOTE_USER_STORE_MANAGEMENT.equals(clientType)) {
                    remoteUserStoreManagerServiceClients
                            .put(portOffset, new RemoteUserStoreManagerServiceClient(serviceUrl, sessionCookie));
                }
            }
        }
    }

    private String getSecureServiceUrl(int portOffset, String baseUrl) {
        return baseUrl.replace("9853", String.valueOf(DEFAULT_PORT + portOffset)) + "/";
    }

    private boolean addUserToSecondaryIS() throws Exception {
        UserManagementClient usrMgtClient = new UserManagementClient(getSecondaryISURI(), "admin", "admin");
        if (usrMgtClient == null) {
            return false;
        } else {
            String[] roles = { secondaryUserRole };
            usrMgtClient.addUser(secondaryUserName, secondaryUserPwd, roles, null);
            if (usrMgtClient.userNameExists(secondaryUserRole, secondaryUserName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Function to retrieve service URI of secondary IS
     *
     * @return service uri
     */
    protected String getSecondaryISURI() {
        return String.format("https://localhost:%s/services/", DEFAULT_PORT + PORT_OFFSET_1);
    }

    private org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] getOAuthAuthnConfigProperties() {

        org.wso2.carbon.identity.application.common.model.idp.xsd.Property[] properties = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property[7];
        org.wso2.carbon.identity.application.common.model.idp.xsd.Property property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(CLIENT_ID);
        property.setValue(secondaryConsumerKey);
        properties[0] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(CLIENT_SECRET);
        property.setValue(secondaryConsumerSecret);
        properties[1] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(OAUTH2_AUTHZ_URL);
        property.setValue(String.format(AUTHORIZE_ENDPOINT, DEFAULT_PORT + PORT_OFFSET_1));
        properties[2] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(OAUTH2_TOKEN_URL);
        property.setValue(String.format(TOKEN_ENDPOINT, DEFAULT_PORT + PORT_OFFSET_1));
        properties[3] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(CALLBACK_URL);
        property.setValue(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        properties[4] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(QUERY_PARAMS);
        property.setValue(null);
        properties[5] = property;

        property = new org.wso2.carbon.identity.application.common.model.idp.xsd.Property();
        property.setName(IS_USER_ID_IN_CLAIM);
        property.setValue("false");
        properties[6] = property;

        return properties;
    }

    /**
     * Create Application with the given app configurations
     *
     * @return OAuthConsumerAppDTO
     * @throws Exception
     */
    private OAuthConsumerAppDTO createOAuthApplication(String applicationName, String spName, String callBack, int port)
            throws Exception {
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        String consumerKey = null;
        String consumerSecret = null;
        appDTO.setApplicationName(applicationName);
        appDTO.setCallbackUrl(callBack);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        OauthAdminClient adminClient = oauthAdminClients.get(port);
        ApplicationManagementServiceClient appMgtClient = applicationManagementServiceClients.get(port);

        OAuthConsumerAppDTO appDtoResult = null;

        adminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(applicationName)) {
                appDtoResult = appDto;
                consumerKey = appDto.getOauthConsumerKey();
                consumerSecret = appDto.getOauthConsumerSecret();
            }
        }
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(spName);
        serviceProvider.setDescription("Test application for OAuth.");
        appMgtClient.createApplication(serviceProvider);

        serviceProvider = appMgtClient.getApplication(spName);
        ClaimConfig claimConfig = new ClaimConfig();
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimConfig.setClaimMappings(
                new org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] { emailClaimMapping });

        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();

        if (consumerKey != null) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(consumerKey);
            opicAuthenticationRequest.setInboundAuthType("oauth2");
            if (StringUtils.isNotBlank(consumerSecret)) {
                Property property = new Property();
                property.setName("oauthConsumerSecret");
                property.setValue(consumerSecret);
                Property[] properties = { property };
                opicAuthenticationRequest.setProperties(properties);
            }
            authRequestList.add(opicAuthenticationRequest);
        }

        InboundAuthenticationRequestConfig stsAuthenticationRequest = new InboundAuthenticationRequestConfig();
        stsAuthenticationRequest.setInboundAuthKey(spName);
        stsAuthenticationRequest.setInboundAuthType("passivests");
        authRequestList.add(stsAuthenticationRequest);

        InboundAuthenticationRequestConfig openIdAuthenticationRequest = new InboundAuthenticationRequestConfig();
        openIdAuthenticationRequest.setInboundAuthKey(spName);
        openIdAuthenticationRequest.setInboundAuthType("openid");
        authRequestList.add(openIdAuthenticationRequest);

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(
                    authRequestList.toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
        }
        appMgtClient.updateApplicationData(serviceProvider);
        return appDtoResult;
    }

    private void deleteAddedUsers() throws RemoteException, UserAdminUserAdminException {
        UserManagementClient usrMgtClient = new UserManagementClient(getSecondaryISURI(), "admin", "admin");
        usrMgtClient.deleteUser(secondaryUserName);
    }

    public ServiceProvider getServiceProvider(int portOffset, String applicationName) throws Exception {
        return applicationManagementServiceClients.get(portOffset).getApplication(applicationName);
    }

    public void updateServiceProvider(int portOffset, ServiceProvider serviceProvider) throws Exception {
        applicationManagementServiceClients.get(portOffset).updateApplicationData(serviceProvider);
    }

    public void deleteServiceProvider(int portOffset, String applicationName) throws Exception {
        applicationManagementServiceClients.get(portOffset).deleteApplication(applicationName);
    }

    public void addIdentityProvider(int portOffset, IdentityProvider identityProvider) throws Exception {
        identityProviderMgtServiceClients.get(portOffset).addIdP(identityProvider);
    }

    public IdentityProvider getIdentityProvider(int portOffset, String idPName) throws Exception {
        return identityProviderMgtServiceClients.get(portOffset).getIdPByName(idPName);
    }

    public void startCarbonServer(int portOffset, AutomationContext context, Map<String, String> startupParameters)
            throws Exception {

        automationContextMap.put(portOffset, context);
        CarbonTestServerManager server = new CarbonTestServerManager(context, System.getProperty("carbon.zip"),
                startupParameters);
        manager.startServers(server);
    }

    public void stopCarbonServer(int portOffset) throws Exception {
        manager.stopAllServers();
    }

    public String extractValueFromResponse(HttpResponse response, String key, int token)
            throws IOException {
        String value = null;
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
                break;
            }
        }
        bufferedReader.close();
        return value;
    }

}
