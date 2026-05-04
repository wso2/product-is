/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj.OpEnum;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This test class is used to verify the OAuth2 password grant flow behavior when a user account is locked.
 *
 */
public class OAuth2PasswordGrantWithAccountLockingTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private final String tokenType;
    private final String adminUsername;
    private final String adminPassword;
    private final String activeTenant;

    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String TEST_USER_USERNAME = "testPasswordGrantUser";
    private static final String TEST_USER_PASSWORD = "Abcd@123";
    private static final String APP_NAME = "PasswordGrantTestApp";

    private static final String USER_SYSTEM_SCHEMA_ATTRIBUTE = "urn:scim:wso2:schema";
    private static final String ACCOUNT_LOCKED_ATTRIBUTE = "accountLocked";
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private HttpClient client;
    private SCIM2RestClient scim2RestClient;
    private String applicationId;
    private String userId;
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    @DataProvider
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2PasswordGrantWithAccountLockingTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.tokenType = "Default";
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        createPasswordGrantApplication();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userId = createTestUser();

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
    public void testCleanup() throws Exception {

        deleteUser();
        deleteApp(applicationId);
        scim2RestClient.closeHttpClient();
    }

    @Test(description = "Test password grant before account lock")
    public void testPasswordGrantBeforeLock() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        ClientID consumerKey = new ClientID(oidcConfig.getClientId());
        Secret consumerSecret = new Secret(oidcConfig.getClientSecret());

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        // Request access token using password grant
        ResourceOwnerPasswordCredentialsGrant passwordGrant =
                new ResourceOwnerPasswordCredentialsGrant(TEST_USER_USERNAME, new Secret(TEST_USER_PASSWORD));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        Assert.assertNotNull(tokenHTTPResp, "Token response is null");
        Assert.assertEquals(tokenHTTPResp.getStatusCode(), 200,
                "Password grant failed. Expected 200 OK, got " + tokenHTTPResp.getStatusCode());

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        Assert.assertTrue(accessTokenResponse.indicatesSuccess(),
                "Token response does not indicate success");

        accessToken = accessTokenResponse.getTokens().getAccessToken();
        refreshToken = accessTokenResponse.getTokens().getRefreshToken();

        Assert.assertNotNull(accessToken, "Access token is null");
        Assert.assertNotNull(refreshToken, "Refresh token is null");
    }

    @Test(description = "Test access token introspection before account lock", 
            dependsOnMethods = "testPasswordGrantBeforeLock")
    public void testIntrospectAccessTokenBeforeLock() throws Exception {

        // Introspect the access token
        TokenIntrospectionResponse introspectionResponse = introspectAccessToken(accessToken);

        Assert.assertTrue(introspectionResponse.indicatesSuccess());
        Assert.assertTrue(introspectionResponse.toSuccessResponse().isActive());
    }

    @Test(description = "Test locking user account",
            dependsOnMethods = "testIntrospectAccessTokenBeforeLock")
    public void testLockUserAccount() throws Exception {

        UserItemAddGroupobj lockUserPatchOp = new UserItemAddGroupobj().op(OpEnum.REPLACE);
        lockUserPatchOp.setPath(USER_SYSTEM_SCHEMA_ATTRIBUTE + ":" + ACCOUNT_LOCKED_ATTRIBUTE);
        lockUserPatchOp.setValue(true);

        scim2RestClient.updateUser(new PatchOperationRequestObject().addOperations(lockUserPatchOp), userId);

        // Verify user is locked
        Boolean isAccountLocked = (Boolean) ((JSONObject) scim2RestClient.getUser(userId, null)
                .get(USER_SYSTEM_SCHEMA_ATTRIBUTE)).get(ACCOUNT_LOCKED_ATTRIBUTE);
        Assert.assertTrue(isAccountLocked, "User account was not locked successfully");
    }

    @Test(description = "Test access token introspection with locked account",
            dependsOnMethods = "testLockUserAccount")
    public void testIntrospectAccessTokenAfterAccountLocking() throws Exception {

        TokenIntrospectionResponse introspectionResponse = introspectAccessToken(accessToken);

        Assert.assertFalse(introspectionResponse.toSuccessResponse().isActive(),
                "Access token should be inactive after account lock");

    }

    @Test(description = "Test refresh token grant with locked account fails",
            dependsOnMethods = "testLockUserAccount")
    public void testRefreshTokenGrantWithLockedAccount() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        ClientID consumerKey = new ClientID(oidcConfig.getClientId());
        Secret consumerSecret = new Secret(oidcConfig.getClientSecret());

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        // Attempt refresh token grant with locked user
        com.nimbusds.oauth2.sdk.RefreshTokenGrant refreshTokenGrant =
                new com.nimbusds.oauth2.sdk.RefreshTokenGrant(refreshToken);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, refreshTokenGrant);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        Assert.assertNotNull(tokenHTTPResp, "Token response is null");
        Assert.assertEquals(tokenHTTPResp.getStatusCode(), 400,
                "Refresh token grant should fail with 400, got " + tokenHTTPResp.getStatusCode());
        Assert.assertTrue(tokenHTTPResp.getContent().contains("invalid_grant"),
                "Error response should contain 'invalid_grant' error");

    }

    /**
     * Create OAuth2 application with password grant enabled.
     */
    private void createPasswordGrantApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "password", "refresh_token");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, "http://localhost:8490/playground2/oauth2client");

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
        application.setName(APP_NAME);

        applicationId = addApplication(application);
    }

    /**
     * Create test user with specified credentials.
     */
    private String createTestUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_USERNAME);
        userInfo.setPassword(TEST_USER_PASSWORD);

        return scim2RestClient.createUser(userInfo);
    }

    /**
     * Introspect an access token to check its status using admin credentials.
     */
    private TokenIntrospectionResponse introspectAccessToken(AccessToken accessToken)
            throws URISyntaxException, IOException, ParseException {

        URI introSpecEndpoint;
        String authUsername;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
            authUsername = adminUsername + "@" + TENANT_DOMAIN;
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
            authUsername = adminUsername;
        }

        ClientAuthentication clientAuth = new ClientSecretBasic(
                new ClientID(authUsername), new Secret(adminPassword));
        HTTPRequest httpRequest =
                new TokenIntrospectionRequest(introSpecEndpoint, clientAuth, accessToken).toHTTPRequest();
        httpRequest.setAuthorization("Basic " + getBase64EncodedString(authUsername, adminPassword));

        HTTPResponse introspectionHTTPResp = httpRequest.send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection HTTP response is null");
        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }

    /**
     * Delete test user.
     */
    private void deleteUser() {

        try {
            scim2RestClient.deleteUser(userId);
        } catch (Exception e) {
            Assert.fail("Error while deleting the user: " + e.getMessage(), e);
        }
    }
}
