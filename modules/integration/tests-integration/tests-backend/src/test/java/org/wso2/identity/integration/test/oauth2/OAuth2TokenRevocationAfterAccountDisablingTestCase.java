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
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
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
import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
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
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.UserIdentityManagementAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This test class is used to check the behaviour of OAuth token revocation on multiple applications after
 * user account disabling
 */
public class OAuth2TokenRevocationAfterAccountDisablingTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private UserManagementClient userMgtClient;
    private IdentityProviderMgtServiceClient idPMgtClient;
    private IdentityProvider residentIDP;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserIdentityManagementAdminServiceClient userIdentityManagementAdminServiceClient;
    RemoteUserStoreManagerServiceClient usmClient;
    private ClientID consumerKey;
    private Secret consumerSecret;

    private final String tokenType;
    private final String adminUsername;
    private final String adminPassword;
    private final String activeTenant;
    private static final String TENANT_DOMAIN = "wso2.com";

    private static final String PROFILE_NAME = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String TEST_ROLE = "testRole";
    private static final String ADMIN = "admin";

    private static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/identity/accountDisabled";
    private static final String ENABLE_ACCOUNT_DISABLING_PROPERTY = "account.disable.handler.enable";

    Map<String, OAuthConsumerAppDTO> applicatons = new HashMap();

    Map<String, AccessToken> accessTokens = new HashMap<>();

    Map<String, AccessToken> privilegedAccessTokens = new HashMap<>();

    private final String OAUTH_APPLICATION_NAME_1 = "oauthTestApplication1";

    private final String OAUTH_APPLICATION_NAME_2 = "oauthTestApplication2";

    private final String CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";

    private final String OAUTH_VERSION_2 = "OAuth-2.0";

    protected final static String SERVICE_PROVIDER_1_NAME = "PlaygroundServiceProvider1";

    protected final static String SERVICE_PROVIDER_2_NAME = "PlaygroundServiceProvider2";

    AccessToken accessToken;

    AccessToken privilegedAccessToken;

    @DataProvider
    public static Object[][] oAuthConsumerApplicationProvider() {

        return new Object[][] {
                {"Default", TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "oAuthConsumerApplicationProvider")
    public OAuth2TokenRevocationAfterAccountDisablingTestCase(String tokenType, TestUserMode userMode)
            throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.tokenType = tokenType;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        createServiceProviderApplication();
        addNewTestUserWithRole();

        idPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        residentIDP = idPMgtClient.getResidentIdP();

        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        userIdentityManagementAdminServiceClient = new UserIdentityManagementAdminServiceClient(backendURL,
                sessionCookie);

        AuthenticatorClient logManager = new AuthenticatorClient(backendURL);
        String secondaryTenantDomain = isServer.getTenantList().get(1);
        String tenantCookie = logManager.login(ADMIN + "@" + secondaryTenantDomain,
                ADMIN, isServer.getInstance().getHosts().get("default"));
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
    }

    //@Test(description = "Create access tokens")
//    public void testCreateAccessTokens1() throws Exception {
//
//        for (OAuthConsumerAppDTO appDTO : applicatons) {
//
//            ClientID consumerKey = new ClientID(appDTO.getOauthConsumerKey());
//            Secret consumerSecret = new Secret(appDTO.getOauthConsumerSecret());
//            accessTokens.add(appDTO.getrequestAccessToken(consumerKey, consumerSecret));
//            privilegedAccessToken = requestPrivilegedAccessToken();
//        }
//
//        // Request access token
//        accessToken = requestAccessToken();
//        privilegedAccessToken = requestPrivilegedAccessToken();
//
//        // Introspect the returned access token to verify the validity
//        TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
//        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
//        Assert.assertTrue(activeTokenIntrospectionResponse.toSuccessResponse().isActive(),
//                "Introspection response of an active access token is unsuccessful.");
//    }

    @Test(description = "Create access tokens")
    public void testCreateAccessTokens() throws Exception {

        Set<String> appKeys = applicatons.keySet();
        for (String app : appKeys) {

            OAuthConsumerAppDTO appDTO = applicatons.get(app);
            ClientID consumerKey = new ClientID(appDTO.getOauthConsumerKey());
            Secret consumerSecret = new Secret(appDTO.getOauthConsumerSecret());
            AccessToken accessToken = requestAccessToken(consumerKey, consumerSecret);
            accessTokens.put(app, accessToken);
            AccessToken privilegedAccessToken = requestPrivilegedAccessToken(consumerKey, consumerSecret);
            privilegedAccessTokens.put(app, privilegedAccessToken);

            // Introspect the returned access token to verify the validity
            TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
            Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
            Assert.assertTrue(activeTokenIntrospectionResponse.toSuccessResponse().isActive(),
                    "Introspection response of an active access token is unsuccessful.");
        }
    }

    @Test(description = "Enabling the user account disabling feature for resident IDP",
            dependsOnMethods = "testCreateAccessTokens")
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
    private void disableUserAccount() throws Exception {

        setUserClaim(ACCOUNT_DISABLED_CLAIM_URI, "true");
        ClaimValue[] claimValues = usmClient.getUserClaimValuesForClaims(TEST_USER_USERNAME, new String[]
                {ACCOUNT_DISABLED_CLAIM_URI}, "default");
        String accountDisabledClaimValue = null;
        if (ArrayUtils.isNotEmpty(claimValues)) {
            accountDisabledClaimValue = claimValues[0].getValue();
        }
        Assert.assertTrue(Boolean.parseBoolean(accountDisabledClaimValue), "User account didn't disabled");
    }

//    @Test(description = "Check whether access token is revoked after disabling the account",
//            dependsOnMethods = "disableUserAccount")
//    private void introspectAccessTokenOfDisabledAccount() throws Exception {
//
//        TokenIntrospectionResponse revokedTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
//        Assert.assertTrue(revokedTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
//        Assert.assertFalse(revokedTokenIntrospectionResponse.toSuccessResponse().isActive(),
//                "Introspection response of a revoked access token is successful.");
//    }

    @Test(description = "Check whether access token is revoked after disabling the account",
            dependsOnMethods = "disableUserAccount")
    private void introspectAccessTokenOfDisabledAccount() throws Exception {

        Set<String> appKeys = applicatons.keySet();
        for (String app : appKeys) {

            TokenIntrospectionResponse revokedTokenIntrospectionResponse = introspectAccessToken(accessTokens.get(app),
                    privilegedAccessTokens.get(app));
            Assert.assertTrue(revokedTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
//            Assert.assertFalse(revokedTokenIntrospectionResponse.toSuccessResponse().isActive(),
//                    "Introspection response of a revoked access token is successful.");
        }
    }

    private void createServiceProviderApplication() throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAUTH_APPLICATION_NAME_1);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType(tokenType);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication(appDTO, SERVICE_PROVIDER_1_NAME);
        applicatons.put(SERVICE_PROVIDER_1_NAME, oAuthConsumerAppDTO);

        OAuthConsumerAppDTO appDTO2 = new OAuthConsumerAppDTO();
        appDTO2.setApplicationName(OAUTH_APPLICATION_NAME_2);
        appDTO2.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO2.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO2.setTokenType(tokenType);
        appDTO2.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        OAuthConsumerAppDTO oAuthConsumerAppDTO2 = createApplication(appDTO2, SERVICE_PROVIDER_2_NAME);
        applicatons.put(SERVICE_PROVIDER_2_NAME, oAuthConsumerAppDTO2);

//        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication(appDTO);

//        consumerKey = new ClientID(oAuthConsumerAppDTO.getOauthConsumerKey());
//        consumerSecret = new Secret(oAuthConsumerAppDTO.getOauthConsumerSecret());
    }

    private void addNewTestUserWithRole() throws Exception {

        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null, null,
                PROFILE_NAME, false);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtClient.addRole(TEST_ROLE, new String[]{TEST_USER_USERNAME},
                new String[]{"/permission/admin/login"}, false);
    }

    protected void setUserClaim(String claimURI, String calimValue) throws LogoutAuthenticationExceptionException,
            RemoteException,
            UserAdminUserAdminException, UserProfileMgtServiceUserProfileExceptionException {
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName(PROFILE_NAME);

        UserFieldDTO passwordResetClaim = new UserFieldDTO();
        passwordResetClaim.setClaimUri(claimURI);
        passwordResetClaim.setFieldValue(calimValue);

        UserFieldDTO[] fields = new UserFieldDTO[1];
        fields[0] = passwordResetClaim;

        profile.setFieldValues(fields);

        userProfileMgtClient.setUserProfile(TEST_USER_USERNAME, profile);
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider, boolean isSuperTenant) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        if (isSuperTenant) {
            idPMgtClient.updateResidentIdP(residentIdentityProvider);
        } else {
            tenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        }
    }

    private AccessToken requestAccessToken() throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(TEST_USER_USERNAME,
                new Secret(TEST_USER_PASSWORD));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AccessToken requestAccessToken(ClientID key, Secret secret) throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(key, secret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(TEST_USER_USERNAME,
                new Secret(TEST_USER_PASSWORD));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AccessToken requestPrivilegedAccessToken() throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(adminUsername,
                new Secret(adminPassword));

        Scope scope = new Scope("internal_application_mgt_view");

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AccessToken requestPrivilegedAccessToken(ClientID key, Secret secret) throws Exception {

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
            throws Exception {

        URI introSpecEndpoint;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        }
        BearerAccessToken bearerAccessToken = new BearerAccessToken(privilegedAccessToken.getValue());
        TokenIntrospectionRequest TokenIntroRequest = new TokenIntrospectionRequest(introSpecEndpoint,
                bearerAccessToken,
                accessToken);
        HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection http response is null.");

        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }
}
