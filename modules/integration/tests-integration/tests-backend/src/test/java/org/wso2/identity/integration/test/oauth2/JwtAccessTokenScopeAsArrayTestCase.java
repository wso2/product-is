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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Integration tests for the enableJwtScopeAsArray configuration.
 * Validates Config API behavior, organization inheritance, and JWT scope formatting
 * during the OIDC Client Credentials flow.
 */
public class JwtAccessTokenScopeAsArrayTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String OAUTH2_INBOUND_AUTH_CONFIG_PATH = "/configs/authentication/inbound/oauth2";
    private static final String ENABLE_JWT_SCOPE_AS_ARRAY = "enableJwtScopeAsArray";
    private static final String SUB_ORG_NAME = "jwtScopeTestSubOrg";
    private static final String AUTHORIZED_APIS_JSON = "jwt-scope-array-authorized-apis.json";
    private static final String TEST_SCOPE_1 = "test_scope_01";
    private static final String TEST_SCOPE_2 = "test_scope_02";
    private static final String DOMAIN_API_NAME = "JwtScopeTestAPI";
    private static final String DOMAIN_API_IDENTIFIER = "/jwt-scope-test-api";

    private AutomationContext context;
    private String username;
    private String userPassword;
    private Tenant tenantInfo;
    private String applicationId;
    private String consumerKey;
    private String consumerSecret;
    private RestBaseClient configRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private String configApiBasePath;
    private String subOrgConfigApiBasePath;
    private String subOrgId;
    private String switchedM2MToken;
    private String domainAPIId;

    @DataProvider(name = "userModeConfigProvider")
    public static Object[][] userModeConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "userModeConfigProvider")
    public JwtAccessTokenScopeAsArrayTestCase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        tenantInfo = context.getContextTenant();

        restClient = new org.wso2.identity.integration.test.restclients.OAuth2RestClient(serverURL, tenantInfo);
        configRestClient = new RestBaseClient();

        String tenantDomain = tenantInfo.getDomain();

        // Build Config API paths.
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            configApiBasePath = serverURL + "api/server/v1" + OAUTH2_INBOUND_AUTH_CONFIG_PATH;
            subOrgConfigApiBasePath = serverURL + "o/api/server/v1" + OAUTH2_INBOUND_AUTH_CONFIG_PATH;
        } else {
            configApiBasePath = serverURL + "t/" + tenantDomain + "/api/server/v1" + OAUTH2_INBOUND_AUTH_CONFIG_PATH;
            subOrgConfigApiBasePath =
                    serverURL + "t/" + tenantDomain + "/o/api/server/v1" + OAUTH2_INBOUND_AUTH_CONFIG_PATH;
        }

        // Initialize OrgMgtRestClient for sub-org testing.
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON, this.getClass())));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (applicationId != null) {
            deleteApp(applicationId);
        }
        if (domainAPIId != null) {
            deleteDomainAPI(domainAPIId);
        }

        // Revert tenant config to default.
        patchOAuth2InboundConfig(false);

        if (subOrgId != null) {
            orgMgtRestClient.deleteOrganization(subOrgId);
        }

        if (restClient != null) {
            restClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
        if (configRestClient != null && configRestClient.client != null) {
            configRestClient.client.close();
        }
    }

    // ========== Organization-Level Configuration & Inheritance Tests ==========

    @Test(groups = "wso2.is", description = "Verify default value of enableJwtScopeAsArray in tenant Config API.")
    public void testDefaultOAuth2InboundConfig() throws Exception {

        JSONObject config = getOAuth2InboundConfig();
        Assert.assertFalse(config.getBoolean(ENABLE_JWT_SCOPE_AS_ARRAY),
                "Default value of enableJwtScopeAsArray should be false.");
    }

    @Test(groups = "wso2.is", description = "Update enableJwtScopeAsArray to true at tenant level.",
            dependsOnMethods = "testDefaultOAuth2InboundConfig")
    public void testUpdateTenantLevelConfig() throws Exception {

        patchOAuth2InboundConfig(true);

        JSONObject config = getOAuth2InboundConfig();
        Assert.assertTrue(config.getBoolean(ENABLE_JWT_SCOPE_AS_ARRAY),
                "enableJwtScopeAsArray should be true after update.");
    }

    @Test(groups = "wso2.is", description = "Verify sub-org inherits enableJwtScopeAsArray from parent tenant.",
            dependsOnMethods = "testUpdateTenantLevelConfig")
    public void testSubOrgInheritsConfig() throws Exception {

        subOrgId = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        switchedM2MToken = orgMgtRestClient.switchM2MToken(subOrgId);
        System.out.println("Switched m2m token " + switchedM2MToken.toString());

        JSONObject subOrgConfig = getSubOrgOAuth2InboundConfig(switchedM2MToken);
        Assert.assertTrue(subOrgConfig.getBoolean(ENABLE_JWT_SCOPE_AS_ARRAY),
                "Sub-org should inherit enableJwtScopeAsArray=true from parent tenant.");
    }

    @Test(groups = "wso2.is", description = "Update enableJwtScopeAsArray in sub-org to override inherited value.",
            dependsOnMethods = "testSubOrgInheritsConfig")
    public void testUpdateSubOrgConfig() throws Exception {

        patchSubOrgOAuth2InboundConfig(false, switchedM2MToken);

        JSONObject subOrgConfig = getSubOrgOAuth2InboundConfig(switchedM2MToken);
        Assert.assertFalse(subOrgConfig.getBoolean(ENABLE_JWT_SCOPE_AS_ARRAY),
                "Sub-org should be able to override enableJwtScopeAsArray to false.");
    }

    @Test(groups = "wso2.is", description = "Revert sub-org enableJwtScopeAsArray back to true.",
            dependsOnMethods = "testUpdateSubOrgConfig")
    public void testRevertSubOrgConfig() throws Exception {

        patchSubOrgOAuth2InboundConfig(true, switchedM2MToken);

        JSONObject subOrgConfig = getSubOrgOAuth2InboundConfig(switchedM2MToken);
        Assert.assertTrue(subOrgConfig.getBoolean(ENABLE_JWT_SCOPE_AS_ARRAY),
                "Sub-org enableJwtScopeAsArray should be true after revert.");
    }

    // ========== JWT Formatting (Tenant-Level Global Config) Tests ==========

    @Test(groups = "wso2.is", description = "Register application with JWT access token type.",
            dependsOnMethods = "testRevertSubOrgConfig")
    public void testRegisterApplicationWithJwtToken() throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName("JwtScopeArrayTestApp");

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "client_credentials");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);

        String appId = addApplication(application);
        ApplicationResponseModel createdApp = getApplication(appId);
        applicationId = createdApp.getId();

        // Create a domain API with test scopes and authorize it to the application.
        List<String> domainScopes = Arrays.asList(TEST_SCOPE_1, TEST_SCOPE_2);
        domainAPIId = createDomainAPI(DOMAIN_API_NAME, DOMAIN_API_IDENTIFIER, domainScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, domainScopes);

        OpenIDConnectConfiguration oidcInboundConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcInboundConfig.getClientId();
        consumerSecret = oidcInboundConfig.getClientSecret();

        Assert.assertNotNull(consumerKey, "Consumer key should not be null.");
        Assert.assertNotNull(consumerSecret, "Consumer secret should not be null.");
    }

    @Test(groups = "wso2.is",
            description = "Verify JWT scope is array when tenant config is enabled and no app-level override.",
            dependsOnMethods = "testRegisterApplicationWithJwtToken")
    public void testJwtScopeAsArrayFromTenantConfig() throws Exception {

        String jwtToken = performClientCredentialsGrantAndGetJwt(TEST_SCOPE_1, TEST_SCOPE_2);
        Object scopeClaim = parseScopeClaimFromJwt(jwtToken);
        assertScopeIsArray(scopeClaim);
    }

    @Test(groups = "wso2.is",
            description = "Verify Application OIDC properties do not include enableJwtScopeAsArray when not set.",
            dependsOnMethods = "testJwtScopeAsArrayFromTenantConfig")
    public void testAppOidcPropertiesExcludeJwtScopeAsArray() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        Assert.assertNull(oidcConfig.getAccessToken().getEnableJwtScopeAsArray(),
                "enableJwtScopeAsArray should not be present in app OIDC config when not set at app level.");
    }

    // ========== Application-Level Override Tests ==========

    @Test(groups = "wso2.is",
            description = "Set enableJwtScopeAsArray=true at app level and verify JWT scope is array.",
            dependsOnMethods = "testAppOidcPropertiesExcludeJwtScopeAsArray")
    public void testAppOverrideEnableTrue() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcConfig.getAccessToken().enableJwtScopeAsArray(true);
        updateApplicationInboundConfig(applicationId, oidcConfig, OIDC);

        // Verify the GET API now includes the property.
        OpenIDConnectConfiguration updatedConfig = getOIDCInboundDetailsOfApplication(applicationId);
        Assert.assertEquals(updatedConfig.getAccessToken().getEnableJwtScopeAsArray(), Boolean.TRUE,
                "enableJwtScopeAsArray should be true in app OIDC config.");

        // Verify JWT scope is array.
        String jwtToken = performClientCredentialsGrantAndGetJwt(TEST_SCOPE_1, TEST_SCOPE_2);
        Object scopeClaim = parseScopeClaimFromJwt(jwtToken);
        assertScopeIsArray(scopeClaim);
    }

    @Test(groups = "wso2.is",
            description = "Set enableJwtScopeAsArray=false at app level (overriding tenant=true), " +
                    "verify JWT scope is space-delimited string.",
            dependsOnMethods = "testAppOverrideEnableTrue")
    public void testAppOverrideDisableFalse() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcConfig.getAccessToken().enableJwtScopeAsArray(false);
        updateApplicationInboundConfig(applicationId, oidcConfig, OIDC);

        // Verify JWT scope is a string (app-level false overrides tenant-level true).
        String jwtToken = performClientCredentialsGrantAndGetJwt(TEST_SCOPE_1, TEST_SCOPE_2);
        Object scopeClaim = parseScopeClaimFromJwt(jwtToken);
        assertScopeIsString(scopeClaim);
    }

    @Test(groups = "wso2.is",
            description = "Remove app-level override (set to null), verify JWT falls back to tenant config (array).",
            dependsOnMethods = "testAppOverrideDisableFalse")
    public void testAppOverrideRemovedFallsBackToTenant() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcConfig.getAccessToken().enableJwtScopeAsArray(null);
        updateApplicationInboundConfig(applicationId, oidcConfig, OIDC);

        // Verify JWT scope falls back to array (tenant config = true).
        String jwtToken = performClientCredentialsGrantAndGetJwt(TEST_SCOPE_1, TEST_SCOPE_2);
        Object scopeClaim = parseScopeClaimFromJwt(jwtToken);
        assertScopeIsArray(scopeClaim);
    }

    // ========== Helper Methods ==========

    private JSONObject getOAuth2InboundConfig() throws Exception {

        try (CloseableHttpResponse response = configRestClient.getResponseOfHttpGet(
                configApiBasePath, getBasicAuthHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to get OAuth2 inbound config.");
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    private JSONObject getSubOrgOAuth2InboundConfig(String bearerToken) throws Exception {

        try (CloseableHttpResponse response = configRestClient.getResponseOfHttpGet(
                subOrgConfigApiBasePath, getBearerHeaders(bearerToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to get sub-org OAuth2 inbound config.");
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    private void patchOAuth2InboundConfig(boolean enableJwtScopeAsArray) throws Exception {

        String body = new JSONObject().put(ENABLE_JWT_SCOPE_AS_ARRAY, enableJwtScopeAsArray).toString();
        try (CloseableHttpResponse response = configRestClient.getResponseOfHttpPatch(
                configApiBasePath, body, getBasicAuthHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to patch OAuth2 inbound config.");
        }
    }

    private void patchSubOrgOAuth2InboundConfig(boolean enableJwtScopeAsArray, String bearerToken) throws Exception {

        String body = new JSONObject().put(ENABLE_JWT_SCOPE_AS_ARRAY, enableJwtScopeAsArray).toString();
        try (CloseableHttpResponse response = configRestClient.getResponseOfHttpPatch(
                subOrgConfigApiBasePath, body, getBearerHeaders(bearerToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to patch sub-org OAuth2 inbound config.");
        }
    }

    private String performClientCredentialsGrantAndGetJwt(String... scopes) throws Exception {

        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        ClientID clientID = new ClientID(consumerKey);
        Secret secret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, secret);
        Scope scope = new Scope(scopes);

        URI tokenEndpoint = new URI(getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        Assert.assertTrue(tokenResponse.indicatesSuccess(), "Token request failed.");

        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
        String accessToken = accessTokenResponse.getTokens().getAccessToken().getValue();
        Assert.assertNotNull(accessToken, "Access token is null.");

        return accessToken;
    }

    private Object parseScopeClaimFromJwt(String jwtToken) throws Exception {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        return claimsSet.getClaim("scope");
    }

    private void assertScopeIsArray(Object scopeClaim) {

        Assert.assertNotNull(scopeClaim, "Scope claim should not be null in JWT.");
        Assert.assertTrue(scopeClaim instanceof List,
                "Scope claim should be a JSON array (List) but was: " + scopeClaim.getClass().getName());
    }

    private void assertScopeIsString(Object scopeClaim) {

        Assert.assertNotNull(scopeClaim, "Scope claim should not be null in JWT.");
        Assert.assertTrue(scopeClaim instanceof String,
                "Scope claim should be a space-delimited String but was: " + scopeClaim.getClass().getName());
    }

    private Header[] getBasicAuthHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(RestBaseClient.USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(RestBaseClient.AUTHORIZATION_ATTRIBUTE,
                RestBaseClient.BASIC_AUTHORIZATION_ATTRIBUTE +
                        Base64.encodeBase64String((username + ":" + userPassword).getBytes()).trim());
        headerList[2] = new BasicHeader(RestBaseClient.CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getBearerHeaders(String token) {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(RestBaseClient.USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(RestBaseClient.AUTHORIZATION_ATTRIBUTE,
                RestBaseClient.BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE + token);
        headerList[2] = new BasicHeader(RestBaseClient.CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }
}
