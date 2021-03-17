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
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
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

import java.io.File;
import java.io.IOException;
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
    private static final String PRIMARY_IS_NAME = "PrimaryIS";
    private static final String PRIMARY_IS_AUTHENTICATOR_NAME_OIDC = "OpenIDConnectAuthenticator";
    private static final String PRIMARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9853/t/primary" +
            ".com/oauth2/authorize";
    private static final String PRIMARY_IS_TOKEN_ENDPOINT = "https://localhost:9853/t/primary.com/oauth2/token";
    private static final String PRIMARY_IS_CALLBACK_URL = "https://localhost:9853/t/primary.com/commonauth";
    //TODO: Enable endpoint in framework.
    private static final String PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT = "https://localhost:9853/t/primary" +
            ".com/identity/oidc/slo";
    private static final String PRIMARY_IS_JWKS_URI = "https://localhost:9853/t/primary.com/oauth2/jwks";
    private static final String PRIMARY_IS_SESSIONS_EXTENSION_ENDPOINT =
            "https://localhost:9853/t/primary.com/identity/extend-session";
    // Federated idp related urls.
    private static final String FEDERATED_IS_AUTHORIZE_ENDPOINT = "https://localhost:9853/t/federated" +
            ".com/oauth2/authorize";
    private static final String FEDERATED_IS_CALLBACK_URL = "https://localhost:9853/t/federated.com/commonauth";
    private static final String FEDERATED_IS_TOKEN_ENDPOINT = "https://localhost:9853/t/federated.com/oauth2/token";
    private static final String FEDERATED_IS_LOGOUT_ENDPOINT = "https://localhost:9853/t/federated.com/oidc/logout";
    private static final String FEDERATED_IS_SERVICES_URI = "https://localhost:9853/t/federated.com/services/";
    private static final String FEDERATED_ME_SESSIONS_ENDPOINT =
            "https://localhost:9853/t/federated.com/api/users/v1/me/sessions";

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

    private static final int SUCCESS_STATUS_CODE = 200;
    private static final int FAILURE_STATUS_CODE = 400;

    private Map<Integer, ApplicationManagementServiceClient> applicationManagementServiceClients;
    private Map<Integer, OauthAdminClient> oAuthAdminClients;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private TenantManagementServiceClient tenantServiceClient;
    private UserManagementClient usrMgtClient;
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
    private String primaryIdToken;
    private String federatedIdToken;
    private String username;
    private String primaryIsk;
    private String federatedIsk;
    private String primaryUserId;
    private String federatedUserId;
    private String federatedSpSessionState;
    private CookieStore cookieStore;
    private CloseableHttpClient client;
    private HttpClient httpClientWithoutAutoRedirections;
    private JSONParser jsonParser;

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

//        super.init();
//        changeISConfiguration();
        super.init();
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
        createServiceProviderInPrimaryIdp();
        createServiceProviderInSecondaryIdp();
        cookieStore = new BasicCookieStore();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        Assert.assertTrue(addUserToSecondaryIS(), "Adding user to federated idp failed.");
        jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing federated idp login.")
    private void testFederatedLogin() throws Exception {

        // Make the authorize call to federated idp.
        String authCall = authorizeCallToIdp(client, FEDERATED_IS_SP_CALLBACK_URL, fedSP_ClientID,
                FEDERATED_IS_AUTHORIZE_ENDPOINT);
        Assert.assertTrue(authCall.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");
        String sessionDataKey = DataExtractUtil.getParamFromURIString(authCall, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        // Get session data key consent from federated idp.
        String sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        // Get authorization code from federated idp.
        AuthorizationCode authorizationCode =
                getAuthCodeFromIdp(sessionDataKeyConsent, FEDERATED_IS_AUTHORIZE_ENDPOINT);
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        // Get the id token from the federated idp.
        OIDCTokens tokens = getIdTokenFromIdp(authorizationCode, fedSP_ClientID, fedSP_ClientSecret,
                FEDERATED_IS_SP_CALLBACK_URL, FEDERATED_IS_TOKEN_ENDPOINT);
        federatedIdToken = tokens.getIDTokenString();
        String federatedAccessToken = tokens.getAccessToken().getValue();
        Assert.assertNotNull(federatedIdToken, "ID token is null");
        // Extract the claims from id token.
        SignedJWT signedJWT = SignedJWT.parse(federatedIdToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        username = jwtClaimsSet.getSubject();
        federatedIsk = (String) jwtClaimsSet.getClaim("isk");
        HttpResponse response = sendGetRequest(client, FEDERATED_ME_SESSIONS_ENDPOINT, federatedAccessToken);
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getEntity().getContent());
        federatedUserId = (String) jsonObject.get("userId");
        Assert.assertNotNull(jsonObject.get("sessions"), "No sessions found in the federated idp for the user.");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing primary idp login.", dependsOnMethods = "testFederatedLogin")
    private void testPrimaryLogin() throws Exception {

        // Make the authorize call to primary idp.
        String authorizeCall = authorizeCallToIdp(httpClientWithoutAutoRedirections,
                PRIMARY_IS_SP_CALLBACK_URL, primSPClientID, PRIMARY_IS_AUTHORIZE_ENDPOINT);
        Assert.assertNotNull(authorizeCall, "Location value is null.");
        String state = DataExtractUtil.getParamFromURIString(authorizeCall, "state");
        // Send authorize call from primary idp to federated idp.
        String commonAuthCall = authenticatePrimaryIsFromFed(state);
        String authCode = DataExtractUtil.getParamFromURIString(commonAuthCall, OAuth2Constant.AUTHORIZATION_CODE_NAME);
        String sessionState = DataExtractUtil.getParamFromURIString(commonAuthCall, "session_state");
        // Get the session data key consent from primary idp.
        String sessionDataKeyConsent = getSessionDataKeyConsentFromPrimaryIs(authCode, state, sessionState);
        // Get authorization code from idp.
        AuthorizationCode authorizationCode =
                getAuthCodeFromIdp(sessionDataKeyConsent, PRIMARY_IS_AUTHORIZE_ENDPOINT);
        // Get the id token from the primary is.
        OIDCTokens tokens = getIdTokenFromIdp(authorizationCode, primSPClientID, primSPClientSecret,
                PRIMARY_IS_SP_CALLBACK_URL, PRIMARY_IS_TOKEN_ENDPOINT);
        primaryIdToken = tokens.getIDTokenString();
        String primaryAccessToken = tokens.getAccessToken().getValue();
        Assert.assertNotNull(primaryIdToken, "ID token is null");
        // Extract claims from id token.
        SignedJWT signedJWT = SignedJWT.parse(primaryIdToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        primaryIsk = (String) jwtClaimsSet.getClaim("isk");
        // Get the userId of the user.
        List<NameValuePair> sessionExtensionParams = new ArrayList<>();
        sessionExtensionParams.add(new BasicNameValuePair("idpSessionKey", primaryIsk));
        HttpResponse response = sendGetRequestWithParameters(client,
                sessionExtensionParams, PRIMARY_IS_SESSIONS_EXTENSION_ENDPOINT);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), SUCCESS_STATUS_CODE, "Session doesn't exists " +
                "for the federated user in the primary idp.");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing federated idp init logout.", dependsOnMethods =
            "testPrimaryLogin")
    private void testFederatedIdpInitLogout() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("post_logout_redirect_uri", FEDERATED_IS_SP_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("id_token_hint", federatedIdToken));
        urlParameters.add(new BasicNameValuePair("session_state", federatedSpSessionState));

        HttpResponse response = sendGetRequestWithParameters(httpClientWithoutAutoRedirections, urlParameters,
                FEDERATED_IS_LOGOUT_ENDPOINT);
        List<NameValuePair> logoutUrlParameters = new ArrayList<>();
        logoutUrlParameters.add(new BasicNameValuePair("consent", "approve"));
        response = sendGetRequestWithParameters(httpClientWithoutAutoRedirections, logoutUrlParameters,
                FEDERATED_IS_LOGOUT_ENDPOINT);
        String redirectUrl = "http://localhost:8490/travelocity.com/home.jsp?sp=travelocity";
        Assert.assertEquals(getLocationHeaderValue(response), redirectUrl, "Logout failure in federated idp.");
        List<NameValuePair> sessionExtensionParams = new ArrayList<>();
        sessionExtensionParams.add(new BasicNameValuePair("idpSessionKey", primaryIsk));
        response = sendGetRequestWithParameters(client,
                sessionExtensionParams, PRIMARY_IS_SESSIONS_EXTENSION_ENDPOINT);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), FAILURE_STATUS_CODE, "OIDC federated idp " +
                "back-channel logout failed for the federated user in primary idp.");
    }

    /**
     * First authorization call to the idp.
     *
     * @param client            - http client.
     * @param callbackUrl       - callback url of the service provider.
     * @param spClientId        - client id of the service provider.
     * @param authorizeEndpoint - authorization endpoint of the idp.
     * @return - location header value in the response.
     * @throws Exception - Exception.
     */
    public String authorizeCallToIdp(HttpClient client, String callbackUrl, String spClientId,
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
     * Send authentication request to federated idp from primary is.
     *
     * @param state - state received during the authorization call.
     * @return - location value of the response containing authCode and sessionState.
     * @throws Exception - Exception.
     */
    public String authenticatePrimaryIsFromFed(String state)
            throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", PRIMARY_IS_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", fedISClientID));
        urlParameters.add(new BasicNameValuePair("state", state));
        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        FEDERATED_IS_AUTHORIZE_ENDPOINT);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        return locationValue;
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         - CloseableHttpClient object to send the login post.
     * @param sessionDataKey - String sessionDataKey obtained.
     * @return - Extracted sessionDataKeyConsent.
     * @throws IOException        - IOException.
     * @throws URISyntaxException - URISyntaxException.
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

    /**
     * Get the authorization code from the idp.
     *
     * @param sessionDataKeyConsent - sessionDataKeyConsent received after consent approval.
     * @param authorizeEndpoint     - authorization endpoint of the idp.
     * @return - AuthorizationCode object containing authCode.
     * @throws Exception - Exception.
     */
    public AuthorizationCode getAuthCodeFromIdp(String sessionDataKeyConsent, String authorizeEndpoint)
            throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent, authorizeEndpoint);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");
        EntityUtils.consume(response.getEntity());
        federatedSpSessionState = DataExtractUtil.getParamFromURIString(locationValue,
                "session_state");

        // Extract authorization code from the location value.
        return new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
    }

    /**
     * Send approval post request.
     *
     * @param client                - http client.
     * @param sessionDataKeyConsent - session consent data.
     * @return - http response.
     * @throws ClientProtocolException - ClientProtocolException
     * @throws IOException             - IOException.
     */
    public HttpResponse sendApprovalPost(HttpClient client, String sessionDataKeyConsent, String authorizeEndpoint)
            throws ClientProtocolException,
            IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        urlParameters.add(new BasicNameValuePair("sessionDataKeyConsent", sessionDataKeyConsent));
        return sendPostRequestWithParameters(client, urlParameters, authorizeEndpoint);
    }

    /**
     * Get the sessionDataKeyConsent value from the primary idp.
     *
     * @param authCode     - authorization code received from federated idp.
     * @param state        - state received during authorization call.
     * @param sessionState - session state received from federated idp.
     * @return - sessionDataKeyConsent.
     * @throws URISyntaxException - URISyntaxException.
     * @throws IOException        - IOException.
     */
    private String getSessionDataKeyConsentFromPrimaryIs(String authCode, String state, String sessionState)
            throws URISyntaxException,
            IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authCode));
        urlParameters.add(new BasicNameValuePair("state", state));
        urlParameters.add(new BasicNameValuePair("session_state", sessionState));
        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        PRIMARY_IS_CALLBACK_URL);
        Assert.assertNotNull(response, "CommonAuth request to primary idp failed. CommonAuth response is null");
        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(httpClientWithoutAutoRedirections, locationValue);
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());
        Assert.assertTrue(locationHeader.getValue().contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "SessionDataKeyConsent not found in response of the primary idp.");
        return DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    /**
     * Get the id token from the idp.
     *
     * @param authorizationCode - authorizationCode.
     * @param clientId          - clientId of the service provider.
     * @param clientSec         - clientSecret of the service provider.
     * @param spCallbackUrl     - callback url of the service provider.
     * @param tokenEndpoint     - token endpoint of the idp.
     * @return -  id token string.
     * @throws Exception - Exception.
     */
    public OIDCTokens getIdTokenFromIdp(AuthorizationCode authorizationCode, String clientId, String clientSec,
                                        String spCallbackUrl, String tokenEndpoint)
            throws Exception {

        ClientID clientID = new ClientID(clientId);
        Secret clientSecret = new Secret(clientSec);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(spCallbackUrl);
        AuthorizationCodeGrant authorizationCodeGrant =
                new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(tokenEndpoint), clientSecretBasic,
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
        return oidcTokens;

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
        usrMgtClient = new UserManagementClient(FEDERATED_IS_SERVICES_URI, "federatedAdmin" +
                "@federated.com",
                "password");
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
                        PRIMARY_IS_CALLBACK_URL, PRIMARY_IS_BACK_CHANNEL_LOGOUT_ENDPOINT, serviceProvider);
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
        identityProvider.setIdentityProviderName(PRIMARY_IS_NAME);
        // Set JWKS Uri to identity provider.
        IdentityProviderProperty property = new IdentityProviderProperty();
        property.setName("jwksUri");
        property.setValue(PRIMARY_IS_JWKS_URI);
        IdentityProviderProperty[] properties = {property};
        identityProvider.setIdpProperties(properties);
        // Set federated auth configs.
        FederatedAuthenticatorConfig oidcAuthnConfig = new FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName(PRIMARY_IS_AUTHENTICATOR_NAME_OIDC);
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
        Assert.assertNotNull(identityProviderMgtServiceClient.getIdPByName(PRIMARY_IS_NAME), "Failed to " +
                "create Identity Provider 'trustedIdP' in primary IS");
    }

    /**
     * Creates 'playground2' service provider in the primary idp.
     *
     * @throws Exception - Throw Exception if failed.
     */
    private void createServiceProviderInPrimaryIdp() throws Exception {

        addServiceProvider(PRIMARY_TENANT, PRIMARY_IS_SP_NAME);
        ServiceProvider serviceProvider = getServiceProvider(PRIMARY_TENANT, PRIMARY_IS_SP_NAME);
        HashMap<String, String> credentials = updateServiceProviderWithOIDCConfigs(PRIMARY_TENANT, PRIMARY_IS_SP_NAME,
                PRIMARY_IS_SP_CALLBACK_URL, PRIMARY_IS_SP_BACK_CHANNEL_LOGOUT_URL, serviceProvider);
        primSPClientID = credentials.get(OIDC_APP_CLIENT_ID);
        primSPClientSecret = credentials.get(OIDC_APP_CLIENT_SECRET);
        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.
                application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(PRIMARY_IS_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig oidcAuthnConfig = new
                org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName(PRIMARY_IS_AUTHENTICATOR_NAME_OIDC);
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

    /**
     * Create service provider, "travelocity" in the secondary idp.
     *
     * @throws Exception - Exception.
     */
    private void createServiceProviderInSecondaryIdp() throws Exception {

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

    /**
     * Add a user to federated idp.
     *
     * @return - boolean value indicating success.
     * @throws Exception - Exception.
     */
    private boolean addUserToSecondaryIS() throws Exception {

        if (usrMgtClient == null) {
            return false;
        } else {
            String[] roles = {FEDERATED_IS_TEST_USER_ROLES};
            usrMgtClient.addUser(FEDERATED_IS_TEST_USERNAME, FEDERATED_IS_TEST_PASSWORD, roles, null);
            return usrMgtClient.userNameExists(FEDERATED_IS_TEST_USER_ROLES, FEDERATED_IS_TEST_USERNAME);
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
        property.setValue(PRIMARY_IS_CALLBACK_URL);
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

    /**
     * Send login credentials to federated idp.
     *
     * @param client         - http client.
     * @param sessionDataKey - sessionDataKey.
     * @return - http response of the login request.
     * @throws IOException - IOException.
     */
    private HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", FEDERATED_IS_TEST_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", FEDERATED_IS_TEST_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters, FEDERATED_IS_CALLBACK_URL);
    }

    /**
     * Make a get request to the given url.
     *
     * @param client      - http client.
     * @param locationURL - url to make the request.
     * @return - http response of the request.
     * @throws IOException - IOException.
     */
    private HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.addHeader("User-Agent", OAuth2Constant.USER_AGENT);

        return client.execute(getRequest);
    }

    /**
     * Make a get request to the given url.
     *
     * @param client      - http client.
     * @param locationURL - url to make the request.
     * @return - http response of the request.
     * @throws IOException - IOException.
     */
    private HttpResponse sendGetRequest(HttpClient client, String locationURL, String token) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.addHeader("User-Agent", OAuth2Constant.USER_AGENT);
        getRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return client.execute(getRequest);
    }

    /**
     * Send a GET request with url parameters.
     *
     * @param client        - http client.
     * @param urlParameters - url parameters list.
     * @param url           - url to send the POST request.
     * @return - http response of the request.
     * @throws IOException - IOException.
     */
    private HttpResponse sendGetRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws IOException, URISyntaxException {

        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        URI uri = new URIBuilder(request.getURI())
                .addParameters(urlParameters)
                .build();
        request.setURI(uri);
        return client.execute(request);
    }

    /**
     * Send a POST request with url parameters.
     *
     * @param client        - http client.
     * @param urlParameters - url parameters list.
     * @param url           - url to send the POST request.
     * @return - http response of the request.
     * @throws IOException - IOException.
     */
    private HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    /**
     * Replace existing deployment.toml file with a file containing the configs to enable the tenant qualified urls
     * session.
     *
     * @throws Exception - Exception.
     */
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
