package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

public class OIDCFederatedIdpInitLogoutTest extends ISIntegrationTest {

    protected Log log = LogFactory.getLog(OIDCFederatedIdpInitLogoutTest.class);

    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    // Tenant names and indexes.
    private static final String PRIMARY_TENANT_NAME = "primary.com";
    private static final String FEDERATED_TENANT_NAME = "federated.com";
    private static final int PRIMARY_TENANT = 0;
    private static final int FEDERATED_TENANT = 1;
    // Name of the primary Is configured in federated Is as a service provider.
    private static final String FEDERATED_IS_PRIMARY_SP_NAME = "PrimaryIS";
    // Primary idp related urls.
    private static final String PRIMARY_IS_IDP_NAME = "PrimaryIS";
    private static final String PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC = "OpenIDConnectAuthenticator";
    private static final String PRIMARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9853/t/primary" +
            ".com/oauth2/authorize";
    private static final String PRIMARY_IS_IDP_CALLBACK_URL = "https://localhost:9853/t/primary.com/commonauth";
    //TODO: Enable endpoint in framework.
    private static final String PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT = "https://localhost:9853/t/primary" +
            ".com/identity/oidc/slo";
    // Federated idp related urls.
    private static final String FEDERATED_IS_AUTHORIZE_ENDPOINT = "https://localhost:9853/t/federated" +
            ".com/oauth2/authorize";
    private static final String FEDERATED_IS_IDP_CALLBACK_URL = "https://localhost:9853/t/federated.com/commonauth";
    private static final String FEDERATED_IS_TOKEN_ENDPOINT = "https://localhost:9853/t/federated.com/oauth2/token";
    private static final String FEDERATED_IS_LOGOUT_ENDPOINT = "https://localhost:9853/t/federated.com/oidc/logout";
    private static final String FEDERATED_IS_SERVICES_URI = "https://localhost:9853/t/federated.com/services/";

    // Urls related to service provider configured in Primary IS.
    private static final String PRIMARY_IS_SP_NAME = "playground2";
    private static final String PRIMARY_IS_SP_AUTHENTICATION_TYPE = "federated";
    private static final String PRIMARY_IS_SP_CALLBACK_URL =
            "http://localhost:8490/playground2/oauth2-authorize-user.jsp";
    private static final String PRIMARY_IS_SP_BACK_CHANNEL_LOGOUT_URL = "http://localhost:8490/playground2/bclogout";

    // Urls related to service provider configured in Federated IS.
    private static final String FEDERATED_IS_SP_NAME = "travelocity";
    private static final String FEDERATED_IS_SP_CALLBACK_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String FEDERATED_IS_SP_BACK_CHANNEL_LOGOUT_URL =
            "http://localhost:8490/travelocity.com/bclogout";

    private static final String OIDC_APP_CLIENT_ID = "ClientID";
    private static final String OIDC_APP_CLIENT_SECRET = "ClientSecret";

    // Username and password of the user in federated IS.
    private static final String FEDERATED_IS_TEST_USERNAME = "testFederatedUser";
    private static final String FEDERATED_IS_TEST_PASSWORD = "testFederatePassword";
    private static final String FEDERATED_IS_TEST_USER_ROLES = "admin";

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, OauthAdminClient> oAuthAdminClients;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private TenantManagementServiceClient tenantServiceClient;
    private ServerConfigurationManager serverConfigurationManager;
    private final AutomationContext context;
    // Client Id and Secret of primary Is service provider configured in federated idp.
    private String fedISClientID;
    private String fedISClientSecret;
    // Client Id and Secret of service provider configured in primary Is.
    private String primSPClientID;
    private String primSPClientSecret;
    // Client Id and Secret of service provider configured in federated Is.
    private String fedSP_ClientID;
    private String fedSP_ClientSecret;
    // Data related to the login flow.
    private String username;
    private String isk;
    private String userId;
    private CookieStore cookieStore;
    private HttpClient httpClientWithoutAutoRedirections;
    private CloseableHttpClient client;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OIDCFederatedIdpInitLogoutTest(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.init();
//        changeISConfiguration();
        applicationManagementServiceClients = new HashMap<>();
        oAuthAdminClients = new HashMap<>();
        tenantServiceClient = new TenantManagementServiceClient(isServer.getContextUrls().getBackEndUrl(),
                sessionCookie);
        createTenants();
        backendURL = context.getContextUrls().getBackEndUrl();
        AuthenticatorClient logManger = new AuthenticatorClient(backendURL);
        String primaryTenantCookie = logManger
                .login("primaryAdmin@primary.com", "password", isServer.getInstance().getHosts().get("default"));
        String federatedTenantCookie = logManger
                .login("federatedAdmin@federated.com", "password", isServer.getInstance().getHosts().get("default"));
        createServiceClients(primaryTenantCookie, federatedTenantCookie);
        createPrimaryServiceProviderInSecondaryTenant();
        createIdentityProviderInPrimaryTenant();
        createServiceProviderInPrimaryTenant();
        createServiceProviderInSecondaryTenant();
        cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        addUserToSecondaryIS();
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing federated idp login.")
    private void testFederatedLogin() throws Exception {

        String locationValue = authCodeGrantSendAuthRequestPost(FEDERATED_IS_SP_CALLBACK_URL, fedSP_ClientID,
                FEDERATED_IS_AUTHORIZE_ENDPOINT);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");
        String sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");

        String sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");

        AuthorizationCode authorizationCode = authCodeGrantSendApprovalPost(sessionDataKeyConsent);
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");

        String idToken = authCodeGrantSendGetTokensPost(authorizationCode);
        Assert.assertNotNull(idToken, "ID token is null");
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        username = jwtClaimsSet.getSubject();
        isk = (String) jwtClaimsSet.getClaim("isk");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing primary idp login.", dependsOnMethods = "testFederatedLogin")
    private void testPrimaryLogin() throws Exception {

        String authorizeCall = authCodeGrantSendAuthRequestPost(PRIMARY_IS_SP_CALLBACK_URL, primSPClientID,
                PRIMARY_IS_AUTHORIZE_ENDPOINT);
        Assert.assertNotNull(authorizeCall, "Location value is null.");
        HttpResponse authorizeResponse = sendGetRequest(httpClientWithoutAutoRedirections, authorizeCall);
        String commonAuthCall = getLocationHeaderValue(authorizeResponse);
        HttpResponse commonAuthResponse = sendGetRequest(client, commonAuthCall);
        String authorizeCall2 = getLocationHeaderValue(commonAuthResponse);
        HttpResponse authorizeCall2Response = sendGetRequest(client, authorizeCall2);
        String authCode = getLocationHeaderValue(authorizeCall2Response);
        AuthorizationCode authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(authCode,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        String idToken = authCodeGrantSendGetTokensPost(authorizationCode);
        Assert.assertNotNull(idToken, "ID token is null");
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
    }

    public String authCodeGrantSendAuthRequestPost(String callbackUrl, String spClientId,
                                                   String authorizeEndpoint)
            throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", callbackUrl));
        urlParameters.add(new BasicNameValuePair("client_id", spClientId));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, authorizeEndpoint);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        return locationValue;
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException
     * @throws URISyntaxException
     */
    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        HttpResponse response2 = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response2);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");
        EntityUtils.consume(response2.getEntity());
        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    public AuthorizationCode authCodeGrantSendApprovalPost(String sessionDataKeyConsent) throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");
        EntityUtils.consume(response.getEntity());
        // Extract authorization code from the location value.
        return new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
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
    public HttpResponse sendApprovalPost(HttpClient client, String sessionDataKeyConsent)
            throws ClientProtocolException,
            IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, FEDERATED_IS_AUTHORIZE_ENDPOINT);

        return response;
    }

    public String authCodeGrantSendGetTokensPost(AuthorizationCode authorizationCode) throws Exception {

        ClientID clientID = new ClientID(fedSP_ClientID);
        Secret clientSecret = new Secret(fedSP_ClientSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(FEDERATED_IS_SP_CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant =
                new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(FEDERATED_IS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        return oidcTokens.getIDTokenString();

    }

    /**
     * Creates two tenants, primary.com and federated.com.
     *
     * @throws Exception - Exception if failed to create tenants.
     */
    private void createTenants() throws Exception {

        tenantServiceClient.addTenant(PRIMARY_TENANT_NAME, "primaryAdmin", "password",
                "primary@primary.com", "Primary", "Admin");
        tenantServiceClient.addTenant(FEDERATED_TENANT_NAME, "federatedAdmin", "password", "federated" +
                "@federated.com", "Federated", "Admin");
        Assert.assertNotNull(tenantServiceClient.getTenant("primary.com").getTenantDomain(),
                "Failed to create Primary tenant.");
        Assert.assertNotNull(tenantServiceClient.getTenant("federated.com").getTenantDomain(),
                "Failed to create federated tenant.");
    }

    /**
     * Create service clients for the tenants.
     *
     * @throws XPathExpressionException - Exception if failed.
     * @throws RemoteException          - Exception if failed.
     */
    private void createServiceClients(String primaryTenantCookie, String federatedTenantCookie)
            throws XPathExpressionException, RemoteException {

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        applicationManagementServiceClients
                .put(PRIMARY_TENANT, new ApplicationManagementServiceClient(primaryTenantCookie,
                        isServer.getContextUrls().getBackEndUrl(), configContext));
        applicationManagementServiceClients
                .put(FEDERATED_TENANT, new ApplicationManagementServiceClient(federatedTenantCookie,
                        isServer.getContextUrls().getBackEndUrl(), configContext));
        identityProviderMgtServiceClient =
                new IdentityProviderMgtServiceClient(primaryTenantCookie, isServer.getContextUrls().getBackEndUrl());
        oAuthAdminClients.put(PRIMARY_TENANT, new OauthAdminClient(isServer.getContextUrls().getBackEndUrl(),
                primaryTenantCookie));
        oAuthAdminClients.put(FEDERATED_TENANT, new OauthAdminClient(isServer.getContextUrls().getBackEndUrl(),
                federatedTenantCookie));
    }

    /**
     * Creates a service provider for primary tenant IS in the federated tenant IS.
     *
     * @throws Exception - Exception if failed.
     */
    private void createPrimaryServiceProviderInSecondaryTenant()
            throws Exception {

        addServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);
        ServiceProvider serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);
        HashMap<String, String> credentials =
                updateServiceProviderWithOIDCConfigs(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME,
                        PRIMARY_IS_IDP_CALLBACK_URL, PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT, serviceProvider);
        fedISClientID = credentials.get(OIDC_APP_CLIENT_ID);
        fedISClientSecret = credentials.get(OIDC_APP_CLIENT_SECRET);
        updateServiceProvider(FEDERATED_TENANT, serviceProvider);
        serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_PRIMARY_SP_NAME);
        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().
                getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (fedISClientID.equals(config.getInboundAuthKey()) && OAuth2Constant.OAUTH_2.equals(
                        config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }
        Assert.assertTrue(success, "Failed to update PrimaryIS service provider with inbound OIDC configs in " +
                "secondary IS");

    }

    /**
     * Creates a identity provider in primary tenant IS.
     *
     * @throws Exception - Throw Exception if failed.
     */
    private void createIdentityProviderInPrimaryTenant() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(PRIMARY_IS_IDP_NAME);
        FederatedAuthenticatorConfig oidcAuthnConfig = new FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC);
        oidcAuthnConfig.setDisplayName("openidconnect");
        oidcAuthnConfig.setEnabled(true);
        oidcAuthnConfig.setProperties(getOIDCAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(oidcAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{oidcAuthnConfig});
        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        jitConfig.setProvisioningUserStore("PRIMARY");
        identityProvider.setJustInTimeProvisioningConfig(jitConfig);
        identityProviderMgtServiceClient.addIdP(identityProvider);
        Assert.assertNotNull(identityProviderMgtServiceClient.getIdPByName(PRIMARY_IS_IDP_NAME), "Failed to " +
                "create " +
                "Identity Provider 'trustedIdP' in primary IS");
    }

    /**
     * Creates 'playground2' service provider in the primary tenant.
     *
     * @throws Exception - Throw Exception if failed.
     */
    private void createServiceProviderInPrimaryTenant() throws Exception {

        addServiceProvider(PRIMARY_TENANT, PRIMARY_IS_SP_NAME);
        ServiceProvider serviceProvider = getServiceProvider(PRIMARY_TENANT, PRIMARY_IS_SP_NAME);
        HashMap<String, String> credentials = updateServiceProviderWithOIDCConfigs(PRIMARY_TENANT, PRIMARY_IS_SP_NAME,
                PRIMARY_IS_SP_CALLBACK_URL, PRIMARY_IS_SP_BACK_CHANNEL_LOGOUT_URL, serviceProvider);
        primSPClientID = credentials.get(OIDC_APP_CLIENT_ID);
        primSPClientSecret = credentials.get(OIDC_APP_CLIENT_SECRET);
        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.
                application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(PRIMARY_IS_IDP_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig oidcAuthnConfig = new
                org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName(PRIMARY_IS_IDP_AUTHENTICATOR_NAME_OIDC);
        oidcAuthnConfig.setDisplayName("openidconnect");
        idP.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.xsd.
                FederatedAuthenticatorConfig[]{oidcAuthnConfig});

        authStep.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd.
                IdentityProvider[]{idP});

        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(new AuthenticationStep[]{
                authStep});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setAuthenticationType(PRIMARY_IS_SP_AUTHENTICATION_TYPE);

        updateServiceProvider(PRIMARY_TENANT, serviceProvider);
        serviceProvider = getServiceProvider(PRIMARY_TENANT, PRIMARY_IS_SP_NAME);
        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().
                getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (credentials.get(OIDC_APP_CLIENT_ID).equals(config.getInboundAuthKey()) &&
                        OAuth2Constant.OAUTH_2.equals(
                                config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }
        Assert.assertTrue(success, "Failed to update Playground2 service provider in primaryIS with inbound OIDC " +
                "configs in ");
    }

    private void createServiceProviderInSecondaryTenant() throws Exception {

        addServiceProvider(FEDERATED_TENANT, FEDERATED_IS_SP_NAME);
        ServiceProvider serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_SP_NAME);
        HashMap<String, String> credentials =
                updateServiceProviderWithOIDCConfigs(FEDERATED_TENANT, FEDERATED_IS_SP_NAME,
                        FEDERATED_IS_SP_CALLBACK_URL, FEDERATED_IS_SP_BACK_CHANNEL_LOGOUT_URL, serviceProvider);
        fedSP_ClientID = credentials.get(OIDC_APP_CLIENT_ID);
        fedSP_ClientSecret = credentials.get(OIDC_APP_CLIENT_SECRET);
        updateServiceProvider(FEDERATED_TENANT, serviceProvider);
        serviceProvider = getServiceProvider(FEDERATED_TENANT, FEDERATED_IS_SP_NAME);
        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().
                getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (credentials.get(OIDC_APP_CLIENT_ID).equals(config.getInboundAuthKey()) &&
                        OAuth2Constant.OAUTH_2.equals(
                                config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }
        Assert.assertTrue(success, "Failed to update Playground2 service provider in primaryIS with inbound OIDC " +
                "configs in ");
    }

    private boolean addUserToSecondaryIS() throws Exception {

        UserManagementClient usrMgtClient = new UserManagementClient(FEDERATED_IS_SERVICES_URI, "federatedAdmin" +
                "@federated.com",
                "password");
        if (usrMgtClient == null) {
            return false;
        } else {
            String[] roles = {FEDERATED_IS_TEST_USER_ROLES};
            usrMgtClient.addUser(FEDERATED_IS_TEST_USERNAME, FEDERATED_IS_TEST_PASSWORD, roles, null);
            if (usrMgtClient.userNameExists(FEDERATED_IS_TEST_USER_ROLES, FEDERATED_IS_TEST_USERNAME)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Create a service provider in ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant to which the service provider needs to be added.
     * @param applicationName - Name of the application.
     * @throws Exception - Throw Exception if failed.
     */
    public void addServiceProvider(int tenant, String applicationName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClients.get(tenant).createApplication(serviceProvider);
    }

    /**
     * Retrieve the a service provider from ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant from where the service provider needs to be fetched.
     * @param applicationName - Name of the application.
     * @return - Return the service provider.
     * @throws Exception - Throw Exception if failed.
     */
    public ServiceProvider getServiceProvider(int tenant, String applicationName)
            throws Exception {

        return applicationManagementServiceClients.get(tenant).getApplication(applicationName);
    }

    /**
     * Update a service provider in ApplicationManagementServiceClient for the given tenant.
     *
     * @param tenant          - Tenant where the service provider needs to be updated.
     * @param serviceProvider - Service provider which needs to be updated.
     * @throws Exception - Throw Exception if failed.
     */
    public void updateServiceProvider(int tenant, ServiceProvider serviceProvider)
            throws Exception {

        applicationManagementServiceClients.get(tenant).updateApplicationData(serviceProvider);
    }

    /**
     * Update the given service provider with OIDC configurations.
     *
     * @param tenant               - Tenant where the service provider needs to be updated.
     * @param applicationName      - Name of the application.
     * @param callbackUrl          - Call back url.
     * @param backChannelLogoutUrl - Back channel logout url.
     * @param serviceProvider      - Service provider.
     * @return - Returns hashmap containing app credentials.
     * @throws Exception - Throw Exception if failed.
     */
    private HashMap<String, String> updateServiceProviderWithOIDCConfigs(int tenant,
                                                                         String applicationName,
                                                                         String callbackUrl,
                                                                         String backChannelLogoutUrl,
                                                                         ServiceProvider serviceProvider)
            throws Exception {

        String oidcAppClientId = null;
        String oidcAppClientSecret = null;
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(applicationName);
        appDTO.setCallbackUrl(callbackUrl);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        appDTO.setBackChannelLogoutUrl(backChannelLogoutUrl);

        oAuthAdminClients.get(tenant).registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO createdApp = oAuthAdminClients.get(tenant).getOAuthAppByName(applicationName);
        Assert.assertNotNull(createdApp, "Adding OIDC app failed.");

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig.setInboundAuthType(OAuth2Constant.OAUTH_2);

        if (StringUtils.isNotBlank(createdApp.getOauthConsumerKey())) {
            inboundAuthenticationRequestConfig.setInboundAuthKey(createdApp.getOauthConsumerKey());
            oidcAppClientId = createdApp.getOauthConsumerKey();
        }

        if (StringUtils.isNotBlank(createdApp.getOauthConsumerSecret())) {
            org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.
                    application.common.model.xsd.Property();
            property.setName(OAuth2Constant.OAUTH_CONSUMER_SECRET);
            property.setValue(createdApp.getOauthConsumerSecret());
            oidcAppClientSecret = createdApp.getOauthConsumerSecret();
            org.wso2.carbon.identity.application.common.model.xsd.Property[] properties = {property};
            inboundAuthenticationRequestConfig.setProperties(properties);
        }
        serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new
                InboundAuthenticationRequestConfig[]{inboundAuthenticationRequestConfig});

        HashMap<String, String> credentials = new HashMap<>();
        credentials.put(OIDC_APP_CLIENT_ID, oidcAppClientId);
        credentials.put(OIDC_APP_CLIENT_SECRET, oidcAppClientSecret);
        return credentials;
    }

    /**
     * Get the OIDC authentication configuration properties for the Idp.
     *
     * @return - Returns OIDC Auth Config Properties.
     */
    private Property[] getOIDCAuthnConfigProperties() {

        Property[] properties = new Property[8];
        Property property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.IDP_NAME);
        property.setValue("oidcFedIdP");
        properties[0] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CLIENT_ID);
        property.setValue(fedISClientID);
        properties[1] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET);
        property.setValue(fedISClientSecret);
        properties[2] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
        property.setValue(FEDERATED_IS_AUTHORIZE_ENDPOINT);
        properties[3] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
        property.setValue(FEDERATED_IS_TOKEN_ENDPOINT);
        properties[4] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.CALLBACK_URL);
        property.setValue(PRIMARY_IS_IDP_CALLBACK_URL);
        properties[5] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
        property.setValue(FEDERATED_IS_LOGOUT_ENDPOINT);
        properties[6] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");
        property.setValue("scope=" + OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN);
        properties[7] = property;
        return properties;
    }

    public String extractValueFromResponse(HttpResponse response, String key, int token)
            throws IOException {

        String value = null;
        String line;
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

    private HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", FEDERATED_IS_TEST_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", FEDERATED_IS_TEST_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters, FEDERATED_IS_IDP_CALLBACK_URL);
    }

    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.addHeader("User-Agent", OAuth2Constant.USER_AGENT);

        return client.execute(getRequest);
    }

    private HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing deployment.toml to enable tenant qualified url.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oidc" +
                File.separator + "tenant_qualified_paths_enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

}
