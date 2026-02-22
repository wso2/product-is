/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.UserClaimConfig;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.IdTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceCreationModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeCreationModel;
import org.wso2.identity.integration.test.restclients.APIResourceManagementClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.BASIC_AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.BASIC_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;

/**
 * Holds the integration tests for sub organization application authorizations.
 */
public class OAuth2AuthorizationForSubOrganizationAppsTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String API_SERVER_BASE_PATH = "api/server/v1";
    private static final String API_RESOURCE_MANAGEMENT_PATH = "/api-resources";
    private static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    private static final String AUTHORIZED_APIS_PATH = "/authorized-apis";
    private static final String TENANT_PATH = "t/";
    private static final String PATH_SEPARATOR = "/";
    private static final String API_RESOURCES = "apiResources";
    private static final String ID = "id";
    private static final String POLICY_IDENTIFIER = "policyIdentifier";
    private static final String RBAC_POLICY = "RBAC";
    private static final String SCOPES = "scopes";
    private static final String ORG_END_USER_USERNAME = "alex";
    private static final String ORG_END_USER_PASSWORD = "Alex@wso2";
    private static final String ORG_END_USER_EMAIL = "alex@wso2.com";
    private static final String TEST_ORG_END_USER_USERNAME = "testUser";
    private static final String TEST_ORG_END_USER_PASSWORD = "TestUser@wso2";
    private static final String TEST_ORG_END_USER_EMAIL = "testuser@wso2.com";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String USERS = "users";
    private static final String ORGANIZATION_MAIN_APP_NAME = "Sub Organization Application - Auth Code";
    private static final String ORGANIZATION_TEST_APP_NAME = "Token Test Org App";
    private static final String TEST_API_RESOURCE_NAME = "Test Organization API";
    private static final String TEST_API_RESOURCE_IDENTIFIER = "test_org_api";
    private static final String TEST_API_RESOURCE_SCOPE_NAME = "test_read";
    private static final String TEST_API_RESOURCE_SCOPE_DISPLAY_NAME = "Test Read";
    private static final String TEST_API_RESOURCE_SCOPE_DESCRIPTION = "Test read permission";
    private static final String SYSTEM_SCOPE = "SYSTEM";
    private static final String ORGANIZATION_APP_AUTHORIZED_APIS_JSON = "organization-app-authorized-apis.json";
    private static final String FILTER_IDENTIFIER_EQ = "?filter=identifier+eq+";
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static boolean isServerConfigured = false;
    private static ServerConfigurationManager staticServerConfigurationManager;

    // Instance variables per user mode)
    private final TestUserMode userMode;
    private final String ORGANIZATION_NAME;
    private final String ORGANIZATION_HANDLE;
    private String ORG_APP_CLIENT_ID;
    private String ORG_APP_CLIENT_SECRET;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private APIResourceManagementClient apiResourceManagementClient;
    private String accessToken;
    private String orgAppAccessToken;
    private String switchedM2MToken;
    private String sessionDataKey;
    private String orgEndUserId;
    private String authorizationCode;
    private String organizationId;
    private String testApiResourceId;
    private String organizationApplicationId;

    // Variables for JWT token flow
    private String jwtSessionDataKey;
    private String jwtAuthorizationCode;
    private String jwtOrgAppAccessToken;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
//                {TestUserMode.SUPER_TENANT_ADMIN, "sub001", "sub001"},
                {TestUserMode.TENANT_ADMIN, "sub002", "sub002"}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2AuthorizationForSubOrganizationAppsTestCase(TestUserMode userMode, String orgName, String orgHandle) {

        this.userMode = userMode;
        this.ORGANIZATION_NAME = orgName;
        this.ORGANIZATION_HANDLE = orgHandle;
    }


    @BeforeClass(alwaysRun = true)
    public void initTestClass() throws Exception {

        // Configure deployment.toml only once before all tests
        synchronized (OAuth2AuthorizationForSubOrganizationAppsTestCase.class) {
            if (!isServerConfigured) {
                // Initialize with super tenant admin to configure server
                super.init(TestUserMode.SUPER_TENANT_ADMIN);
                configureIntrospectionEndpoint();
                // Reinitialize after server restart to refresh context for both super tenant and tenant modes
                super.init(TestUserMode.SUPER_TENANT_ADMIN);
                log.info("Server configured with introspection endpoint settings");
                isServerConfigured = true;
            }
        }
    }

    @Test(priority = 1)
    public void testInit() throws Exception {

        // Initialize with the correct user mode
        super.init(userMode);

        CookieStore cookieStore = new BasicCookieStore();

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();

        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {
                        return false;
                    }
                })
                .build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        apiResourceManagementClient = new APIResourceManagementClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass())));

        // Creating and sharing the application which will manage the organizations and applications
        addAndShareApp();
        getM2MAccessToken();

        // Create an organization
        organizationId = orgMgtRestClient.addOrganizationWithToken(ORGANIZATION_NAME, ORGANIZATION_HANDLE, accessToken);

        // Get a token from CC grant and switch it to the created organization
        switchM2MAccessToken(organizationId);

        // Create an API resource in the tenant
        testApiResourceId = createTestAPIResource();

        /*
         Create an application from the switched token in the created application and enable authorization
         grant types. The newly created API resource will be added via updateOrganizationAppAuthorizedAPIs
        */
        organizationApplicationId = createOrganizationApplication(ORGANIZATION_MAIN_APP_NAME, switchedM2MToken);

        // Create a user in the organization to perform login
        createOrgUser();

        // Create an application role and assign the created user to the application role
        createOrgUserRoleAndAssign(organizationApplicationId);
    }

    @Test(priority = 2, dependsOnMethods = "testInit")
    public void testOrganizationApplicationClientDetails() {

        assertNotNull(ORG_APP_CLIENT_ID, "Organization Application Client ID should not be null");
        assertNotNull(ORG_APP_CLIENT_SECRET, "Organization Application Client Secret should not be null");
    }

    @Test(priority = 3, dependsOnMethods = "testOrganizationApplicationClientDetails")
    public void testSendAuthorizeRequestForOrganizationApp() throws Exception {

        sessionDataKey = performAuthorizationRequest("Session data key is null.");
    }

    @Test(priority = 4, dependsOnMethods = "testSendAuthorizeRequestForOrganizationApp")
    public void testSendLoginPostForOrganizationApp() throws Exception {

        authorizationCode = performLoginAndGetAuthCode(sessionDataKey);
    }

    @Test(priority = 5, dependsOnMethods = "testSendLoginPostForOrganizationApp")
    public void testGetAccessTokenWithAuthorizationCodeForOrganizationApp() throws Exception {

        orgAppAccessToken = getAccessTokenWithAuthCode(authorizationCode, "Access token is null.");
    }

    @Test(priority = 6, dependsOnMethods = "testGetAccessTokenWithAuthorizationCodeForOrganizationApp")
    public void testIntrospectTokenForOrganizationApp() throws Exception {

        validateTokenIntrospection(orgAppAccessToken, "Token is not active.", "Bearer");
    }

    @Test(priority = 7, dependsOnMethods = "testIntrospectTokenForOrganizationApp")
    public void testInvokeApplicationMgtAPIsFromOrganizationAppToken() throws Exception {

        OAuth2RestClient oAuth2RestClient = createOAuth2RestClientForOrganization();

        // Validate application creation
        ApplicationModel applicationModel = getOrganizationAppModel(ORGANIZATION_TEST_APP_NAME);
        String orgAppId = oAuth2RestClient.createOrganizationApplication(applicationModel, orgAppAccessToken);
        assertNotNull(orgAppId, "Application ID cannot be null");

        // Validate application retrieval
        ApplicationResponseModel applicationResponseModel = oAuth2RestClient.getOrganizationApplication(orgAppId,
                orgAppAccessToken);
        assertEquals(applicationResponseModel.getName(), ORGANIZATION_TEST_APP_NAME, "Application name is " +
                "not present in the response.");
    }

    @Test(priority = 8, dependsOnMethods = "testInvokeApplicationMgtAPIsFromOrganizationAppToken")
    public void testInvokeUserMgtAPIsFromOrganizationAppToken() throws Exception {

        SCIM2RestClient scim2RestClientLocal = createSCIM2RestClientForOrganization();

        // Create sub org end user.
        UserObject endUser = new UserObject();
        endUser.setUserName(TEST_ORG_END_USER_USERNAME);
        endUser.setPassword(TEST_ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(TEST_ORG_END_USER_EMAIL));
        // Validate User creation
        String subOrgUserId = scim2RestClientLocal.createSubOrgUser(endUser, orgAppAccessToken);
        assertNotNull(subOrgUserId, "User ID cannot be null");

        // Validate User retrieval
        org.json.simple.JSONObject subOrgUserObject = scim2RestClientLocal.getSubOrgUser(subOrgUserId, null,
                orgAppAccessToken);
        assertNotNull(subOrgUserObject, "Sub Org User is null");
        assertEquals(subOrgUserObject.get("userName").toString(), TEST_ORG_END_USER_USERNAME, "Expected " +
                "user name is not present in the response.");
        assertTrue(subOrgUserObject.get("emails").toString().contains(TEST_ORG_END_USER_EMAIL), "Expected " +
                "user emails are not present in the response.");
        scim2RestClientLocal.closeHttpClient();
    }

    @Test(priority = 9, dependsOnMethods = "testInvokeUserMgtAPIsFromOrganizationAppToken")
    public void testUpdateApplicationTokenTypeToJWT() throws Exception {

        // Update the organization application to use JWT token type
        updateOrganizationApplicationTokenType(organizationApplicationId, "JWT", switchedM2MToken);

        // Verify the update by retrieving the application configuration
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfOrganizationApplication(
                organizationApplicationId, switchedM2MToken);
        assertNotNull(oidcConfig, "OIDC configuration should not be null");

        // Get the access token configuration
        AccessTokenConfiguration accessTokenConfig = oidcConfig.getAccessToken();
        assertNotNull(accessTokenConfig, "Access token configuration should not be null");
        assertEquals(accessTokenConfig.getType(), "JWT", "Token type should be updated to JWT");

        // Clear all sessions and cookies before starting JWT token flow
        clearAllSessions();
        log.info("All sessions and cookies cleared. Ready for JWT token flow.");
    }

    // ========== JWT Token Cycle Tests (After Token Type Update) ==========

    @Test(priority = 10, dependsOnMethods = "testUpdateApplicationTokenTypeToJWT")
    public void testSendAuthorizeRequestForOrganizationAppWithJWT() throws Exception {

        jwtSessionDataKey = performAuthorizationRequest("Session data key is null for JWT flow.");
    }

    @Test(priority = 11, dependsOnMethods = "testSendAuthorizeRequestForOrganizationAppWithJWT")
    public void testSendLoginPostForOrganizationAppWithJWT() throws Exception {

        jwtAuthorizationCode = performLoginAndGetAuthCode(jwtSessionDataKey);
    }

    @Test(priority = 12, dependsOnMethods = "testSendLoginPostForOrganizationAppWithJWT")
    public void testGetJWTAccessTokenWithAuthorizationCodeForOrganizationApp() throws Exception {

        jwtOrgAppAccessToken = getAccessTokenWithAuthCode(jwtAuthorizationCode, "JWT access token is null.");

        // Validate token format - JWT tokens typically contain two dots (three parts)
        assertEquals(jwtOrgAppAccessToken.split("\\.").length, 3,
                "Token should be in JWT format (three parts separated by dots).");
    }

    @Test(priority = 13, dependsOnMethods = "testGetJWTAccessTokenWithAuthorizationCodeForOrganizationApp")
    public void testIntrospectJWTTokenForOrganizationApp() throws Exception {

        validateTokenIntrospection(jwtOrgAppAccessToken, "JWT token is not active.",
                ApplicationConfig.TokenType.JWT.getTokenTypeProperty());
    }

    @Test(priority = 14, dependsOnMethods = "testIntrospectJWTTokenForOrganizationApp")
    public void testInvokeApplicationMgtAPIsWithJWTToken() throws Exception {

        OAuth2RestClient oAuth2RestClient = createOAuth2RestClientForOrganization();

        // Validate application creation with JWT token
        ApplicationModel applicationModel = getOrganizationAppModel(ORGANIZATION_TEST_APP_NAME + "-JWT");
        String orgAppId = oAuth2RestClient.createOrganizationApplication(applicationModel, jwtOrgAppAccessToken);
        assertNotNull(orgAppId, "Application ID cannot be null when using JWT token");

        // Validate application retrieval
        ApplicationResponseModel applicationResponseModel = oAuth2RestClient.getOrganizationApplication(orgAppId,
                jwtOrgAppAccessToken);
        assertEquals(applicationResponseModel.getName(), ORGANIZATION_TEST_APP_NAME + "-JWT",
                "Application name is not present in the response.");
    }

    @Test(priority = 15, dependsOnMethods = "testInvokeApplicationMgtAPIsWithJWTToken")
    public void testInvokeUserMgtAPIsWithJWTToken() throws Exception {

        SCIM2RestClient scim2RestClientLocal = createSCIM2RestClientForOrganization();

        // Create sub org end user with JWT token
        UserObject endUser = new UserObject();
        endUser.setUserName(TEST_ORG_END_USER_USERNAME + "-jwt");
        endUser.setPassword(TEST_ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value("jwt-" + TEST_ORG_END_USER_EMAIL));

        // Validate User creation with JWT token
        String subOrgUserId = scim2RestClientLocal.createSubOrgUser(endUser, jwtOrgAppAccessToken);
        assertNotNull(subOrgUserId, "User ID cannot be null when using JWT token");

        // Validate User retrieval
        org.json.simple.JSONObject subOrgUserObject = scim2RestClientLocal.getSubOrgUser(subOrgUserId, null,
                jwtOrgAppAccessToken);
        assertNotNull(subOrgUserObject, "Sub Org User is null");
        assertEquals(subOrgUserObject.get("userName").toString(), TEST_ORG_END_USER_USERNAME + "-jwt",
                "Expected user name is not present in the response.");
        assertTrue(subOrgUserObject.get("emails").toString().contains("jwt-" + TEST_ORG_END_USER_EMAIL),
                "Expected user emails are not present in the response.");
        scim2RestClientLocal.closeHttpClient();
    }

    @Test(priority = 16, dependsOnMethods = "testInvokeUserMgtAPIsWithJWTToken")
    public void testValidateJWTTokenStructure() throws Exception {

        // Validate that the JWT token has the expected structure
        assertNotNull(jwtOrgAppAccessToken, "JWT token should not be null");

        // JWT tokens have three parts: header.payload.signature
        String[] jwtParts = jwtOrgAppAccessToken.split("\\.");
        assertEquals(jwtParts.length, 3, "JWT token should have exactly 3 parts (header.payload.signature)");

        // Decode and validate the payload (second part)
        String payload = new String(java.util.Base64.getUrlDecoder().decode(jwtParts[1]));
        JSONObject payloadJson = new JSONObject(payload);

        // Validate standard JWT claims
        assertTrue(payloadJson.has("sub"), "JWT should have 'sub' (subject) claim");
        assertTrue(payloadJson.has("iat"), "JWT should have 'iat' (issued at) claim");
        assertTrue(payloadJson.has("exp"), "JWT should have 'exp' (expiration) claim");
        assertTrue(payloadJson.has("client_id"), "JWT should have 'client_id' claim");

        // Validate the client_id matches
        assertEquals(payloadJson.getString("client_id"), ORG_APP_CLIENT_ID,
                "JWT client_id should match the application client ID");

        log.info("JWT token structure validated successfully. Token type: JWT");
    }

    @Test(priority = 17, dependsOnMethods = "testValidateJWTTokenStructure")
    public void testCompareOpaqueAndJWTTokens() throws Exception {

        // Compare the characteristics of OPAQUE and JWT tokens
        log.info("Original OPAQUE token length: " + orgAppAccessToken.length());
        log.info("JWT token length: " + jwtOrgAppAccessToken.length());

        /*
         OPAQUE tokens are typically UUIDs (36 chars) or random strings and JWT tokens are much longer due to
         header.payload.signature structure
        */
        assertTrue(jwtOrgAppAccessToken.length() > orgAppAccessToken.length(),
                "JWT token should typically be longer than OPAQUE token due to its structure");

        // JWT tokens contain dots (.) as separators
        assertTrue(jwtOrgAppAccessToken.contains("."), "JWT token should contain dots as part separators");

        // OPAQUE tokens typically don't have dots (unless they're a specific format)
        int opaqueDotsCount = orgAppAccessToken.length() - orgAppAccessToken.replace(".", "").length();
        int jwtDotsCount = jwtOrgAppAccessToken.length() - jwtOrgAppAccessToken.replace(".", "").length();

        assertTrue(jwtDotsCount >= 2, "JWT token should have at least 2 dots");

        log.info("Token comparison completed successfully:");
        log.info("  - OPAQUE token dots: " + opaqueDotsCount);
        log.info("  - JWT token dots: " + jwtDotsCount);
        log.info("Both token types (OPAQUE and JWT) have been tested successfully!");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() throws Exception {

        if (testApiResourceId != null && apiResourceManagementClient != null) {
            apiResourceManagementClient.deleteAPIResource(testApiResourceId);
        }
        if (organizationId != null && accessToken != null && orgMgtRestClient != null) {
            orgMgtRestClient.deleteOrganizationWithToken(organizationId, accessToken);
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
        if (restClient != null) {
            restClient.closeHttpClient();
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (apiResourceManagementClient != null) {
            apiResourceManagementClient.closeHttpClient();
        }
        if (client != null) {
            client.close();
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanupClass() throws Exception {

        // Restore original deployment.toml configuration after all tests complete
        synchronized (OAuth2AuthorizationForSubOrganizationAppsTestCase.class) {
            if (isServerConfigured && staticServerConfigurationManager != null) {
                log.info("Resetting deployment.toml to original configuration");
                staticServerConfigurationManager.restoreToLastConfiguration(true);
                isServerConfigured = false;
            }
        }
    }

    private void addAndShareApp() throws Exception {

        List<UserClaimConfig> userClaimConfigs = Collections.singletonList(
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/emailaddress")
                        .oidcClaimUri("email")
                        .build());

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .tokenType(ApplicationConfig.TokenType.OPAQUE)
                .grantTypes(Arrays.asList(OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                        OAuth2Constant.OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH))
                .expiryTime(300)
                .claimsList(userClaimConfigs)
                .refreshTokenExpiryTime(86400)
                .audienceList(Arrays.asList("audience1", "audience2", "audience3"))
                .build();

        ApplicationResponseModel application = addApplication(applicationConfig);
        String applicationId = application.getId();

        JSONObject managementAppAPIResources = new JSONObject(RESTTestBase.readResource(
                MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass()));

        updateAuthorizedAPIs(applicationId, managementAppAPIResources);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        CLIENT_ID = oidcConfig.getClientId();
        CLIENT_SECRET = oidcConfig.getClientSecret();
        shareApplication(applicationId);
    }

    private String createOrganizationApplication(String applicationName, String accessToken) throws Exception {

        List<UserClaimConfig> userClaimConfigs = Collections.singletonList(
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/emailaddress")
                        .oidcClaimUri("email")
                        .build());

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .tokenType(ApplicationConfig.TokenType.OPAQUE)
                .grantTypes(Arrays.asList(OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                        OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                        OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))
                .claimsList(userClaimConfigs)
                .expiryTime(300)
                .refreshTokenExpiryTime(86400)
                .audienceList(Arrays.asList("audience1", "audience2", "audience3"))
                .skipConsent(true)
                .build();

        ApplicationResponseModel applicationResponseModel = addOrganizationApplication(applicationName,
                applicationConfig, accessToken);
        String organizationApplicationId = applicationResponseModel.getId();

        JSONObject orgAppAPIResources = new JSONObject(RESTTestBase.readResource(
                ORGANIZATION_APP_AUTHORIZED_APIS_JSON, this.getClass()));

        // Add the newly created test API resource to the JSON
        orgAppAPIResources.put(TEST_API_RESOURCE_IDENTIFIER, new org.json.JSONArray().put(
                TEST_API_RESOURCE_SCOPE_NAME));

        updateOrganizationAppAuthorizedAPIs(organizationApplicationId, orgAppAPIResources);


        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfOrganizationApplication(
                organizationApplicationId, accessToken);
        ORG_APP_CLIENT_ID = oidcConfig.getClientId();
        ORG_APP_CLIENT_SECRET = oidcConfig.getClientSecret();
        return organizationApplicationId;
    }

    private void updateAuthorizedAPIs(String appId, JSONObject authorizedAPIs) throws JSONException, IOException {

        RestBaseClient restBaseClient = new RestBaseClient();
        String apiResourceManagementApiBasePath = buildPath(serverURL, tenantInfo.getDomain(), false,
                API_RESOURCE_MANAGEMENT_PATH);
        String applicationManagementApiBasePath = buildPath(serverURL, tenantInfo.getDomain(), false,
                APPLICATION_MANAGEMENT_PATH);

        for (Iterator<String> apiNameIterator = authorizedAPIs.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = authorizedAPIs.get(apiName);

            String apiUUID;
            try (CloseableHttpResponse apiResourceResponse = restBaseClient.getResponseOfHttpGet(
                    apiResourceManagementApiBasePath + FILTER_IDENTIFIER_EQ + apiName,
                    getHeaders())) {
                JSONObject apiResourceResponseBody =
                        new JSONObject(EntityUtils.toString(apiResourceResponse.getEntity()));
                apiUUID = apiResourceResponseBody.getJSONArray(API_RESOURCES).getJSONObject(0).getString(ID);
            }

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put(ID, apiUUID);
            authorizedAPIRequestBody.put(POLICY_IDENTIFIER, RBAC_POLICY);
            authorizedAPIRequestBody.put(SCOPES, requiredScopes);

            try (CloseableHttpResponse appAuthorizedAPIsResponse = restBaseClient.getResponseOfHttpPost(
                    applicationManagementApiBasePath + PATH_SEPARATOR + appId + AUTHORIZED_APIS_PATH,
                    authorizedAPIRequestBody.toString(), getHeaders())) {
                assertEquals(appAuthorizedAPIsResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                        String.format("Authorized APIs update failed for application with ID: %s for API: %s.",
                                appId, apiName));
            }
        }
    }

    private void updateOrganizationAppAuthorizedAPIs(String appId, JSONObject authorizedAPIs)
            throws JSONException, IOException {

        RestBaseClient restBaseClient = new RestBaseClient();
        String apiResourceManagementApiBasePath = buildPath(serverURL, tenantInfo.getDomain(), true,
                API_RESOURCE_MANAGEMENT_PATH);
        String applicationManagementApiBasePath = buildPath(serverURL, tenantInfo.getDomain(), true,
                APPLICATION_MANAGEMENT_PATH);

        for (Iterator<String> apiNameIterator = authorizedAPIs.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = authorizedAPIs.get(apiName);

            String apiUUID;
            try (CloseableHttpResponse apiResourceResponse = restBaseClient.getResponseOfHttpGet(
                    apiResourceManagementApiBasePath + FILTER_IDENTIFIER_EQ + apiName,
                    getHeadersWithToken())) {
                JSONObject apiResourceResponseBody =
                        new JSONObject(EntityUtils.toString(apiResourceResponse.getEntity()));
                apiUUID = apiResourceResponseBody.getJSONArray(API_RESOURCES).getJSONObject(0).getString(ID);
            }

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put(ID, apiUUID);
            authorizedAPIRequestBody.put(POLICY_IDENTIFIER, RBAC_POLICY);
            authorizedAPIRequestBody.put(SCOPES, requiredScopes);

            try (CloseableHttpResponse appAuthorizedAPIsResponse = restBaseClient.getResponseOfHttpPost(
                    applicationManagementApiBasePath + PATH_SEPARATOR + appId + AUTHORIZED_APIS_PATH,
                    authorizedAPIRequestBody.toString(), getHeadersWithToken())) {
                assertEquals(appAuthorizedAPIsResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                        String.format("Authorized APIs update failed for application with ID: %s for API: %s.",
                                appId, apiName));
            }
        }
    }

    private String buildPath(String serverUrl, String tenantDomain, boolean appendOrgPath, String endpointURL) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            if (!appendOrgPath) {
                return serverUrl + API_SERVER_BASE_PATH + endpointURL;
            }
            return serverUrl + "o/" + API_SERVER_BASE_PATH + endpointURL;
        }
        if (!appendOrgPath) {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_SERVER_BASE_PATH + endpointURL;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + "o/" + API_SERVER_BASE_PATH + endpointURL;
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((tenantInfo.getTenantAdmin().getUserName() + ":" +
                        tenantInfo.getTenantAdmin().getPassword()).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getHeadersWithToken() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE
                + switchedM2MToken);
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private void getM2MAccessToken() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(
                "grantType",
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        urlParameters.add(new BasicNameValuePair("consumerKey", CLIENT_ID));
        urlParameters.add(new BasicNameValuePair("consumerSecret", CLIENT_SECRET));
        urlParameters.add(new BasicNameValuePair("scope", SYSTEM_SCOPE));
        urlParameters.add(new BasicNameValuePair("accessEndpoint",
                getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        assertNotNull(response, "Authorization request failed. Authorized response is null");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        Map<String, Integer> keyPositionMap = new HashMap<>(2);
        keyPositionMap.put("name=\"accessToken\"", 1);

        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        assertNotNull(keyValues, "Access token Key value is null.");

        if (keyValues.isEmpty()) {
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            throw new Exception("Failed to extract access token from response. Response body: " + responseBody);
        }

        accessToken = keyValues.get(0).getValue();
        assertNotNull(accessToken, "Access token value is null.");
    }

    private void shareApplication(String appId) throws Exception {

        // Share the B2B app with all child organizations.
        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);

        OAuth2RestClient oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        oAuth2RestClient.shareApplication(appId, applicationSharePOSTRequest);
    }

    private void switchM2MAccessToken(String organizationId) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, "organization_switch"));
        urlParameters.add(new BasicNameValuePair("token", accessToken));
        urlParameters.add(new BasicNameValuePair("scope", SYSTEM_SCOPE));
        urlParameters.add(new BasicNameValuePair("switching_organization", organizationId));

        HttpPost httpPost = new HttpPost(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        httpPost.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes())));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        assertNotNull(json, "Access token response is null.");
        assertNotNull(json.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        switchedM2MToken = (String) json.get(OAuth2Constant.ACCESS_TOKEN);
    }

    private void createOrgUser() throws Exception {

        // Create sub org end user.
        UserObject endUser = new UserObject();
        endUser.setUserName(ORG_END_USER_USERNAME);
        endUser.setPassword(ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(ORG_END_USER_EMAIL));
        orgEndUserId = scim2RestClient.createSubOrgUser(endUser, switchedM2MToken);
    }

    /**
     * Creates an organization user role with necessary permissions and assigns the created user to the role.
     */
    private void createOrgUserRoleAndAssign(String appID) throws JSONException, IOException {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission("internal_org_user_mgt_view"));
        permissions.add(new Permission("internal_org_user_mgt_list"));
        permissions.add(new Permission("internal_org_user_mgt_create"));
        permissions.add(new Permission("internal_org_user_mgt_delete"));
        permissions.add(new Permission("internal_org_application_mgt_view"));
        permissions.add(new Permission("internal_org_application_mgt_create"));
        permissions.add(new Permission("internal_org_application_mgt_delete"));
        permissions.add(new Permission("internal_org_group_mgt_view"));
        permissions.add(new Permission("internal_org_group_mgt_create"));
        permissions.add(new Permission(TEST_API_RESOURCE_SCOPE_NAME));
        Audience roleAudience = new Audience("APPLICATION", appID);
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleAudience, "ORGANIZATION-USER-ROLE", permissions, schemas);

        String roleId = scim2RestClient.addOrganizationV2Roles(role, switchedM2MToken);
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(orgEndUserId));
        scim2RestClient.updateOrganizationUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject),
                roleId, switchedM2MToken);
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates an application model for organization applications.
     */
    private ApplicationModel getOrganizationAppModel(String applicationName) {

        ApplicationModel application = new ApplicationModel();
        application.setName(applicationName);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList(OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));

        AccessTokenConfiguration accessTokenConfiguration = new AccessTokenConfiguration();
        accessTokenConfiguration.type(ApplicationConfig.TokenType.OPAQUE.getTokenTypeProperty());
        accessTokenConfiguration.applicationAccessTokenExpiryInSeconds(86400L);
        accessTokenConfiguration.userAccessTokenExpiryInSeconds(300L);

        oidcConfig.idToken(new IdTokenConfiguration().audience(Arrays.asList("audience01", "audience02")));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);

        application.advancedConfigurations(new AdvancedApplicationConfiguration()
                .skipLoginConsent(true)
                .skipLogoutConsent(true));

        return application;
    }

    /**
     * Creates a test API resource that can be authorized in the organization application.
     */
    private String createTestAPIResource() throws Exception {

        APIResourceCreationModel apiResourceCreationModel = new APIResourceCreationModel();
        apiResourceCreationModel.setName(TEST_API_RESOURCE_NAME);
        apiResourceCreationModel.setIdentifier(TEST_API_RESOURCE_IDENTIFIER);
        apiResourceCreationModel.setRequiresAuthorization(true);

        ScopeCreationModel scope = new ScopeCreationModel();
        scope.setName(TEST_API_RESOURCE_SCOPE_NAME);
        scope.setDisplayName(TEST_API_RESOURCE_SCOPE_DISPLAY_NAME);
        scope.setDescription(TEST_API_RESOURCE_SCOPE_DESCRIPTION);
        apiResourceCreationModel.addScopesItem(scope);

        return apiResourceManagementClient.createAPIResource(apiResourceCreationModel);
    }

    /**
     * Updates the token type of the organization application.
     */
    private void updateOrganizationApplicationTokenType(String appId, String tokenType, String accessToken)
            throws Exception {

        // Get the current OIDC configuration
        OpenIDConnectConfiguration currentOidcConfig = getOIDCInboundDetailsOfOrganizationApplication(appId, accessToken);

        // Update the access token type
        AccessTokenConfiguration accessTokenConfig = currentOidcConfig.getAccessToken();
        if (accessTokenConfig == null) {
            accessTokenConfig = new AccessTokenConfiguration();
        }
        accessTokenConfig.setType(tokenType);
        currentOidcConfig.setAccessToken(accessTokenConfig);

        // Update the application inbound configuration
        RestBaseClient restBaseClient = new RestBaseClient();
        String applicationManagementApiBasePath = buildPath(serverURL, tenantInfo.getDomain(), true,
                APPLICATION_MANAGEMENT_PATH);
        String endPointUrl = applicationManagementApiBasePath + PATH_SEPARATOR + appId +
                "/inbound-protocols/oidc";

        String jsonRequest = restBaseClient.toJSONString(currentOidcConfig);

        try (CloseableHttpResponse response = restBaseClient.getResponseOfHttpPut(endPointUrl, jsonRequest,
                getHeadersWithToken())) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to update organization application token type");
        }
    }

    /**
     * Performs authorization request and returns the session data key.
     */
    private String performAuthorizationRequest(String errorMessage) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        String scopeString = "internal_org_user_mgt_view internal_org_user_mgt_list internal_org_user_mgt_create " +
                "internal_org_user_mgt_delete internal_org_application_mgt_view internal_org_application_mgt_create " +
                "internal_org_application_mgt_delete internal_org_group_mgt_view internal_org_group_mgt_create"
                + " " + TEST_API_RESOURCE_SCOPE_NAME;
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", ORG_APP_CLIENT_ID));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", scopeString));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, ORGANIZATION_HANDLE));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "SessionDataKey key value is null");

        String sessionKey = keyValues.get(0).getValue();
        assertNotNull(sessionKey, errorMessage);
        EntityUtils.consume(response.getEntity());

        return sessionKey;
    }

    /**
     * Performs login and returns the authorization code.
     */
    private String performLoginAndGetAuthCode(String sessionKey) throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionKey, ORG_END_USER_USERNAME,
                ORG_END_USER_PASSWORD, ORGANIZATION_HANDLE);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        String authCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authCode, "Authorization code is null");

        return authCode;
    }

    /**
     * Gets access token using authorization code and returns the token with response.
     */
    private String getAccessTokenWithAuthCode(String authCode, String errorMessage) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", ORG_APP_CLIENT_ID));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(ORG_APP_CLIENT_ID, ORG_APP_CLIENT_SECRET)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, ORGANIZATION_HANDLE));
        assertNotNull(response, "Failed to receive response for access token request.");

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token is not present in the response.");
        String token = jsonResponse.getString("access_token");
        assertNotNull(token, errorMessage);

        // Validate scopes in the same response
        String scope = jsonResponse.getString("scope");
        assertTrue(scope.contains("internal_org_user_mgt_list"), "User List scope is not present in the response.");
        assertTrue(scope.contains("internal_org_application_mgt_create"), "Application Create scope is not present in the response.");
        assertTrue(scope.contains(TEST_API_RESOURCE_SCOPE_NAME), TEST_API_RESOURCE_SCOPE_NAME + " scope is not present in the response.");

        return token;
    }

    /**
     * Validates token through introspection endpoint.
     */
    private void validateTokenIntrospection(String token, String activeErrorMessage, String tokenType)
            throws Exception {

        String introspectionUrl = getTenantQualifiedURL(OAuth2Constant.INTRO_SPEC_ENDPOINT, ORGANIZATION_HANDLE);
        log.info("Introspection endpoint URL: " + introspectionUrl);
        log.info("Introspecting token: " + token);

        org.json.simple.JSONObject introspectionResponse = introspectTokenWithTenant(client, token,
                introspectionUrl, ORG_APP_CLIENT_ID, ORG_APP_CLIENT_SECRET);
        log.info("Introspection response: " + introspectionResponse.toJSONString());

        // Validate the token is active
        assertNotNull(introspectionResponse, "Introspection response is null.");
        assertTrue(introspectionResponse.containsKey("active"),
                "Active status is not present in the introspection response.");
        assertTrue((Boolean) introspectionResponse.get("active"), activeErrorMessage);

        // Validate the scopes
        assertTrue(introspectionResponse.containsKey("scope"),
                "Scope is not present in the introspection response.");
        String scope = introspectionResponse.get("scope").toString();

        assertTrue(scope.contains("internal_org_user_mgt_list"), "User List scope is not present in the introspection response.");
        assertTrue(scope.contains("internal_org_application_mgt_create"), "Application Create scope is not present in the introspection response.");
        assertTrue(scope.contains(TEST_API_RESOURCE_SCOPE_NAME), TEST_API_RESOURCE_SCOPE_NAME + " scope is not present in the introspection response.");

        // Additional validations
        assertTrue(introspectionResponse.containsKey("client_id"),
                "Client ID is not present in the introspection response.");
        assertEquals(introspectionResponse.get("client_id").toString(), ORG_APP_CLIENT_ID,
                "Client ID does not match.");

        assertTrue(introspectionResponse.containsKey("token_type"),
                "Token type is not present in the introspection response.");
        assertEquals(introspectionResponse.get("token_type").toString(), tokenType,
                "Token type is not " + tokenType);
    }

    /**
     * Creates OAuth2RestClient for organization.
     */
    private OAuth2RestClient createOAuth2RestClientForOrganization() throws IOException {

        Tenant tenantInfo = new Tenant();
        tenantInfo.setDomain(ORGANIZATION_HANDLE);
        User user = new User();
        user.setUserName("testadmin@" + ORGANIZATION_HANDLE);
        tenantInfo.setContextUser(user);

        return new OAuth2RestClient(serverURL, tenantInfo);
    }

    /**
     * Creates SCIM2RestClient for organization.
     */
    private SCIM2RestClient createSCIM2RestClientForOrganization() throws IOException {

        Tenant tenantInfo = new Tenant();
        tenantInfo.setDomain(ORGANIZATION_HANDLE);
        User user = new User();
        user.setUserName("testadmin@" + ORGANIZATION_HANDLE);
        tenantInfo.setContextUser(user);

        return new SCIM2RestClient(serverURL, tenantInfo);
    }

    /**
     * Clears all sessions and cookies by recreating the HTTP client with a fresh cookie store.
     * This ensures a clean state for subsequent test flows.
     */
    private void clearAllSessions() {

        CookieStore cookieStore = new BasicCookieStore();

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();

        // Close the existing client if it exists
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error closing existing HTTP client", e);
            }
        }

        // Create a new HTTP client with a fresh cookie store (no sessions)
        client = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {
                        return false;
                    }
                })
                .build();

        log.info("HTTP client recreated with fresh session state");
    }

    /**
     * Add the configuration to the deployment.toml to enable introspection endpoint with BasicClientAuthentication.
     */
    private void configureIntrospectionEndpoint() throws Exception {

        log.info("Configuring deployment.toml for introspection endpoint with BasicClientAuthentication");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File introspectionConfigFile = new File(getISResourceLocation() + File.separator + "oauth" +
                File.separator + "introspection-config.toml");

        staticServerConfigurationManager = new ServerConfigurationManager(isServer);
        staticServerConfigurationManager.applyConfigurationWithoutRestart(introspectionConfigFile, defaultTomlFile, true);
        staticServerConfigurationManager.restartForcefully();
    }
}
