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
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj.OpEnum;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This test class is used to check the behaviour of OAuth token revocation on multiple applications after
 * disabling the user account.
 */
public class OAuth2TokenRevocationAfterAccountDisablingTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private final String tokenType;
    private final String adminUsername;
    private final String adminPassword;
    private final String activeTenant;

    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Abcd@123";
    private static final String ENABLE_ACCOUNT_DISABLING_PROPERTY = "account.disable.handler.enable";
    private static final String CONNECTOR_ACCOUNT_DISABLE_HANDLER = "YWNjb3VudC5kaXNhYmxlLmhhbmRsZXI";
    private static final String CATEGORY_ACCOUNT_MANAGEMENT = "QWNjb3VudCBNYW5hZ2VtZW50";

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE ="urn:scim:wso2:schema";
    private static final String ACCOUNT_DISABLED_ATTRIBUTE ="accountDisabled";
    private static final String APP_CALLBACK_URL = "http://localhost:8490/playground2/oauth2client";

    private static final String SERVICE_PROVIDER_1_NAME = "PlaygroundServiceProvider1";
    private static final String SERVICE_PROVIDER_2_NAME = "PlaygroundServiceProvider2";
    private static final String TEST_NONCE = "test_nonce";
    private static final String INTROSPECT_SCOPE = "internal_application_mgt_view";
    private static final String INTROSPECT_SCOPE_IN_NEW_AUTHZ_RUNTIME = "internal_oauth2_introspect";
    private static boolean isLegacyRuntimeEnabled;

    private final Map<String, String> applications = new HashMap<>();
    private final Map<String, AccessToken> accessTokens = new HashMap<>();
    private final Map<String, AccessToken> privilegedAccessTokens = new HashMap<>();

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient client;
    private SCIM2RestClient scim2RestClient;
    private String userId;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private ConnectorsPatchReq connectorPatchRequest;

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
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.tokenType = "Default";
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        isLegacyRuntimeEnabled = CarbonUtils.isLegacyAuthzRuntimeEnabled();
        createServiceProviderApplication(SERVICE_PROVIDER_1_NAME);
        createServiceProviderApplication(SERVICE_PROVIDER_2_NAME);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

        userId = addNewTestUser();

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
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser();
        deleteApp(applications.get(SERVICE_PROVIDER_1_NAME));
        deleteApp(applications.get(SERVICE_PROVIDER_2_NAME));
        disableUserAccountDisablingFeature();
        scim2RestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
    }

    @Test(description = "Create access tokens")
    public void testCreateAccessTokens() throws Exception {

        Set<String> appKeys = applications.keySet();
        for (String appName : appKeys) {
            String appId = applications.get(appName);

            OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
            ClientID consumerKey = new ClientID(oidcConfig.getClientId());
            Secret consumerSecret = new Secret(oidcConfig.getClientSecret());

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

        PropertyReq property = new PropertyReq();
        property.setName(ENABLE_ACCOUNT_DISABLING_PROPERTY);
        property.setValue("true");

        connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        identityGovernanceRestClient.updateConnectors(CATEGORY_ACCOUNT_MANAGEMENT , CONNECTOR_ACCOUNT_DISABLE_HANDLER,
                connectorPatchRequest);
    }

    @Test(description = "Disabling the test user account", dependsOnMethods = "enableUserAccountDisablingFeature")
    private void testDisableUserAccount() throws Exception {

        UserItemAddGroupobj disableUserPatchOp = new UserItemAddGroupobj().op(OpEnum.REPLACE);
        disableUserPatchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + ACCOUNT_DISABLED_ATTRIBUTE);
        disableUserPatchOp.setValue(true);
        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(disableUserPatchOp), userId);

        Boolean accountActiveValue = (Boolean) ((JSONObject) scim2RestClient.getUser(userId, null)
                .get(USER_SYSTEM_SCHEMA_ATTRIBUTE)).get(ACCOUNT_DISABLED_ATTRIBUTE);
        Assert.assertTrue(accountActiveValue, "User account didn't disabled");
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

    private void createServiceProviderApplication(String serviceProviderName) throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "password");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type(tokenType);
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(serviceProviderName);

        String applicationId = addApplication(application);
        applications.put(serviceProviderName, applicationId);

        if (!isLegacyRuntimeEnabled) {
            // Authorize /oauth2/introspect API.
            authorizeSystemAPIs(applicationId, new ArrayList<>(Arrays.asList("/oauth2/introspect")));
            // Associate roles.
            ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
            AssociatedRolesConfig associatedRolesConfig =
                    new AssociatedRolesConfig().allowedAudience(AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
            applicationPatch = applicationPatch.associatedRoles(associatedRolesConfig);
            updateApplication(applicationId, applicationPatch);
        }
    }

    private String addNewTestUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_USERNAME);
        userInfo.setPassword(TEST_USER_PASSWORD);

        return scim2RestClient.createUser(userInfo);
    }

    private AccessToken requestAccessToken(ClientID key, Secret secret)
            throws URISyntaxException, IOException, ParseException {

        ClientAuthentication clientAuth = new ClientSecretBasic(key, secret);
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        AuthorizationGrant codeGrant = getAuthorizationCode(key);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AuthorizationGrant getAuthorizationCode(ClientID key) throws IOException, URISyntaxException {

        String sessionDataKey;
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
        return new AuthorizationCodeGrant(authorizationCode, callbackURI);
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
                getTenantQualifiedURL(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
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
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(adminUsername,
                new Secret(adminPassword));
        Scope scope;
        if (isLegacyRuntimeEnabled) {
            scope = new Scope(INTROSPECT_SCOPE);
        } else {
            scope = new Scope(INTROSPECT_SCOPE_IN_NEW_AUTHZ_RUNTIME);
        }
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

    private void disableUserAccountDisablingFeature() throws Exception {

        connectorPatchRequest.getProperties().get(0).setValue("false");
        identityGovernanceRestClient.updateConnectors(CATEGORY_ACCOUNT_MANAGEMENT , CONNECTOR_ACCOUNT_DISABLE_HANDLER,
                connectorPatchRequest);
    }

    private void deleteUser() {

        try {
            scim2RestClient.deleteUser(userId);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }
}
