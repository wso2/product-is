/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.rmi.RemoteException;
import java.util.Arrays;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.mgt.UserIdentityManagementAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This test class is used to check the behaviour of OAuth token revocation on multiple applications after
 * disabling the user account.
 */
public class OAuth2TokenRevocationAfterAccountDisablingTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private UserManagementClient userMgtClient;
    private IdentityProviderMgtServiceClient idPMgtClient;
    private IdentityProvider residentIDP;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserIdentityManagementAdminServiceClient userIdentityManagementAdminServiceClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private RemoteUserStoreManagerServiceClient usmClient;

    private final String tokenType;
    private final String adminUsername;
    private final String adminPassword;
    private final String activeTenant;

    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String DEFAULT_STRING = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String ADMIN = "admin";

    private static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/identity/accountDisabled";
    private static final String ENABLE_ACCOUNT_DISABLING_PROPERTY = "account.disable.handler.enable";

    private static final String OAUTH_APPLICATION_NAME_1 = "oauthTestApplication1";
    private static final String OAUTH_APPLICATION_NAME_2 = "oauthTestApplication2";
    private static final String APP_CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";

    private static final String SERVICE_PROVIDER_1_NAME = "PlaygroundServiceProvider1";
    private static final String SERVICE_PROVIDER_2_NAME = "PlaygroundServiceProvider2";
    private static final String TEST_NONCE = "test_nonce";

    private Map<String, OAuthConsumerAppDTO> applications = new HashMap<>();
    private Map<String, AccessToken> accessTokens = new HashMap<>();
    private Map<String, AccessToken> privilegedAccessTokens = new HashMap<>();

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient client;

    @DataProvider
    public static Object[][] oAuthConsumerApplicationProvider() {

        return new Object[][] {
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "oAuthConsumerApplicationProvider")
    public OAuth2TokenRevocationAfterAccountDisablingTestCase(TestUserMode userMode)
            throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.tokenType = "Default";
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        createServiceProviderApplication(OAUTH_APPLICATION_NAME_1, SERVICE_PROVIDER_1_NAME);
        createServiceProviderApplication(OAUTH_APPLICATION_NAME_2, SERVICE_PROVIDER_2_NAME);
        addNewTestUser();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        idPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        residentIDP = idPMgtClient.getResidentIdP();
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        userIdentityManagementAdminServiceClient = new UserIdentityManagementAdminServiceClient(backendURL,
                sessionCookie);
        AuthenticatorClient logManager = new AuthenticatorClient(backendURL);
        String secondaryTenantDomain = isServer.getTenantList().get(1);
        String tenantCookie = logManager.login(ADMIN + "@" + secondaryTenantDomain,
                ADMIN, isServer.getInstance().getHosts().get(DEFAULT_STRING));
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser();
        deleteSpApplication(SERVICE_PROVIDER_1_NAME);
        deleteSpApplication(SERVICE_PROVIDER_2_NAME);
        IdentityProviderProperty[] idpProperties = residentIDP.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (ENABLE_ACCOUNT_DISABLING_PROPERTY.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue("false");
            }
        }
        updateResidentIDP(residentIDP, true);
    }

    @Test(description = "Create access tokens")
    public void testCreateAccessTokens() throws URISyntaxException, IOException, ParseException {

        Set<String> appKeys = applications.keySet();
        for (String appName : appKeys) {
            OAuthConsumerAppDTO appDTO = applications.get(appName);
            ClientID consumerKey = new ClientID(appDTO.getOauthConsumerKey());
            Secret consumerSecret = new Secret(appDTO.getOauthConsumerSecret());
            AccessToken accessToken = requestAccessToken(consumerKey, consumerSecret);
            accessTokens.put(appName, accessToken);
            AccessToken privilegedAccessToken = requestPrivilegedAccessToken(consumerKey, consumerSecret);
            privilegedAccessTokens.put(appName, privilegedAccessToken);

            // Introspect the returned access token to verify the validity.
            TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken,
                    privilegedAccessToken);
            Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(),
                    "Failed to receive a success response.");
            Assert.assertTrue(activeTokenIntrospectionResponse.toSuccessResponse().isActive(),
                    "Introspection response of an active access token is unsuccessful.");
        }
    }

    @Test(
            description = "Enabling the user account disabling feature for resident IDP",
            dependsOnMethods = "testCreateAccessTokens"
    )
    private void enableUserAccountDisablingFeature() throws Exception {

        IdentityProviderProperty[] idpProperties = residentIDP.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (ENABLE_ACCOUNT_DISABLING_PROPERTY.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue("true");
            }
        }
        updateResidentIDP(residentIDP, true);
    }

    @Test(description = "Disabling the test user account", dependsOnMethods = "enableUserAccountDisablingFeature")
    private void testDisableUserAccount()
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            UserProfileMgtServiceUserProfileExceptionException {

        setUserClaim(ACCOUNT_DISABLED_CLAIM_URI, "true");
        ClaimValue[] claimValues = usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]
                {ACCOUNT_DISABLED_CLAIM_URI}, "default");
        String accountDisabledClaimValue = null;
        if (ArrayUtils.isNotEmpty(claimValues)) {
            accountDisabledClaimValue = claimValues[0].getValue();
        }
        Assert.assertTrue(Boolean.parseBoolean(accountDisabledClaimValue), "User account didn't disabled");
    }

    @Test(
            description = "Check whether access token is revoked after disabling the account",
            dependsOnMethods = "testDisableUserAccount"
    )
    private void testIntrospectAccessTokenOfDisabledAccount() throws URISyntaxException, IOException, ParseException {

        Set<String> appKeys = applications.keySet();
        for (String appName : appKeys) {
            TokenIntrospectionResponse revokedTokenIntrospectionResponse =
                    introspectAccessToken(accessTokens.get(appName), privilegedAccessTokens.get(appName));
            Assert.assertTrue(revokedTokenIntrospectionResponse.indicatesSuccess(),
                    "Failed to receive a success response.");
            Assert.assertFalse(revokedTokenIntrospectionResponse.toSuccessResponse().isActive(),
                    "Introspection response of a revoked access token is successful.");
        }
    }

    private void createServiceProviderApplication(String oAuthAppName, String serviceProviderName) throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(oAuthAppName);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType(tokenType);
        appDTO.setGrantTypes("authorization_code password");
        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication(appDTO, serviceProviderName);
        applications.put(serviceProviderName, oAuthConsumerAppDTO);
    }

    private void addNewTestUser()
            throws UserStoreException, RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null, null,
                DEFAULT_STRING, false);
    }

    private void setUserClaim(String claimURI, String claimValue)
            throws RemoteException, UserProfileMgtServiceUserProfileExceptionException {

        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName(DEFAULT_STRING);
        UserFieldDTO disableAccountClaim = new UserFieldDTO();
        disableAccountClaim.setClaimUri(claimURI);
        disableAccountClaim.setFieldValue(claimValue);
        UserFieldDTO[] fields = new UserFieldDTO[1];
        fields[0] = disableAccountClaim;
        profile.setFieldValues(fields);
        userProfileMgtClient.setUserProfile(TEST_USER_USERNAME, profile);
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider, boolean isSuperTenant) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        federatedAuthenticatorConfigs = Arrays.stream(federatedAuthenticatorConfigs).filter(
                config -> config.getName().equalsIgnoreCase("samlsso")
        ).toArray(FederatedAuthenticatorConfig[]::new);

        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        if (isSuperTenant) {
            idPMgtClient.updateResidentIdP(residentIdentityProvider);
        } else {
            tenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        }
    }

    private AccessToken requestAccessToken(ClientID key, Secret secret)
            throws URISyntaxException, IOException, ParseException {

        ClientAuthentication clientAuth = new ClientSecretBasic(key, secret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant codeGrant = getAuthorizationCode(key);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AuthorizationGrant getAuthorizationCode(ClientID key) throws IOException, URISyntaxException {

        String sessionDataKey = "";
        String sessionDataKeyConsent = "";
        String sessionDataKeyResponse = getSessionDataKeyRequest(key.getValue());

        if (sessionDataKeyResponse.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT)) {
            sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(sessionDataKeyResponse,
                    OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        } else if (sessionDataKeyResponse.contains(OAuth2Constant.SESSION_DATA_KEY)) {
            sessionDataKey = DataExtractUtil.getParamFromURIString(sessionDataKeyResponse,
                    OAuth2Constant.SESSION_DATA_KEY);
            sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        }

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        AuthorizationCode authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        URI callbackURI = new URI(APP_CALLBACK_URL);
        AuthorizationGrant grant = new AuthorizationCodeGrant(authorizationCode, callbackURI);
        return grant;
    }

    private String getSessionDataKeyRequest(String consumerKey) throws IOException {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, APP_CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");
        return locationValue;
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    private String getSessionDataKeyConsent(HttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey,
                TEST_USER_USERNAME, TEST_USER_PASSWORD);
        Assert.assertNotNull(response, "Login request failed. response is null.");
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        // Request will return with a 302 to the authorized endpoint. Doing a GET will give the sessionDataKeyConsent.
        response = sendGetRequest(client, locationHeader.getValue());
        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");
        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    private AccessToken requestPrivilegedAccessToken(ClientID key, Secret secret)
            throws IOException, ParseException, URISyntaxException {

        ClientAuthentication clientAuth = new ClientSecretBasic(key, secret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(adminUsername,
                new Secret(adminPassword));
        Scope scope = new Scope("internal_application_mgt_view");
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private TokenIntrospectionResponse introspectAccessToken(AccessToken accessToken, AccessToken privilegedAccessToken)
            throws URISyntaxException, IOException, ParseException {

        URI introSpecEndpoint;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        }
        BearerAccessToken bearerAccessToken = new BearerAccessToken(privilegedAccessToken.getValue());
        TokenIntrospectionRequest TokenIntroRequest = new TokenIntrospectionRequest(introSpecEndpoint,
                bearerAccessToken, accessToken);
        HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection http response is null.");
        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }

    private void deleteSpApplication(String applicationName) throws Exception {

        applicationManagementServiceClient.deleteApplication(applicationName);
    }

    private void deleteUser() {

        try {
            remoteUSMServiceClient.deleteUser(TEST_USER_USERNAME);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }
}
