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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
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
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration tests for the enhanced org authentication (for B2B SaaS apps) covering the direct authentication path.
 * Direct access path: `/t/<root-tenant-domain>/o/<org-id>`
 */
public class EnhancedOrgAuthenticationDirectPathTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APP_NAME = "EnhancedOrgAuthenticationApp";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String ORG_END_USER_USERNAME = "testUser";
    private static final String ORG_END_USER_PASSWORD = "TestUser@wso2";
    private static final String ORG_END_USER_EMAIL = "testuser@wso2.com";

    private final TestUserMode userMode;
    private final String organizationName;
    private final String organizationHandle;

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;

    private String organizationId;
    private String orgUserId;
    private String rootApplicationId;
    private String clientId;
    private String clientSecret;
    private String switchedM2MToken;
    private String sessionDataKey;
    private String authorizationCode;
    private String accessToken;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "eoa_direct_sub_org", "eoa_direct_sub_org"},
                {TestUserMode.TENANT_ADMIN, "eoa_direct_t_sub_org", "eoa_direct_t_sub_org"}};
    }

    @Factory(dataProvider = "configProvider")
    public EnhancedOrgAuthenticationDirectPathTestCase(TestUserMode userMode, String orgName, String orgHandle) {

        this.userMode = userMode;
        this.organizationName = orgName;
        this.organizationHandle = orgHandle;
    }

    @Test(priority = 1)
    public void testInit() throws Exception {

        super.init(userMode);
        client = createHttpClient();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new org.json.JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass())));
    }

    @Test(priority = 2, dependsOnMethods = "testInit")
    public void testCreateEnhancedOrgAuthenticationApplication() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel app = new ApplicationModel()
                .name(APP_NAME)
                .enhancedOrgAuthenticationEnabled(true)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));

        rootApplicationId = oAuth2RestClient.createApplication(app);
        assertNotNull(rootApplicationId, "Root application ID should not be null.");

        OpenIDConnectConfiguration createdOidcConfig = oAuth2RestClient.getOIDCInboundDetails(rootApplicationId);
        assertNotNull(createdOidcConfig, "OIDC configuration should not be null.");
        clientId = createdOidcConfig.getClientId();
        clientSecret = createdOidcConfig.getClientSecret();
        assertNotNull(clientId, "Client ID should not be null.");
        assertNotNull(clientSecret, "Client secret should not be null.");
    }

    @Test(priority = 3, dependsOnMethods = "testCreateEnhancedOrgAuthenticationApplication")
    public void testCreateSubOrganization() throws Exception {

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        organizationId = orgMgtRestClient.addOrganizationWithToken(organizationName, organizationHandle, m2mToken);
        assertNotNull(organizationId, "Organization ID should not be null.");
    }

    @Test(priority = 4, dependsOnMethods = "testCreateSubOrganization")
    public void testShareApplicationToSubOrg() throws Exception {

        ApplicationSharePOSTRequest shareRequest = new ApplicationSharePOSTRequest();
        shareRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(rootApplicationId, shareRequest);

        // Allow time for the async share operation to complete.
        Thread.sleep(5000);
    }

    @Test(priority = 5, dependsOnMethods = "testShareApplicationToSubOrg")
    public void testVerifyEnhancedFlagReflectedInAppResponse() throws Exception {

        ApplicationResponseModel appResponse = oAuth2RestClient.getApplication(rootApplicationId);
        assertNotNull(appResponse, "Application response should not be null.");
        assertNotNull(appResponse.getEnhancedOrgAuthenticationEnabled(),
                "enhancedOrgAuthenticationEnabled should not be null in the application response.");
        assertTrue(appResponse.getEnhancedOrgAuthenticationEnabled(),
                "enhancedOrgAuthenticationEnabled should be true for the created application.");

        List<Authenticator> authenticators = appResponse.getAuthenticationSequence().getSteps().stream()
                .flatMap(step -> step.getOptions().stream())
                .toList();
        assertTrue(authenticators.stream()
                        .anyMatch(a -> "OrganizationIdentifierHandler".equals(a.getAuthenticator())),
                "OrganizationIdentifierHandler should be present in authentication steps.");
        Assert.assertFalse(authenticators.stream()
                        .anyMatch(a -> "SSO".equals(a.getIdp())),
                "SSO IDP should not be present in authentication steps.");
    }

    @Test(priority = 6, dependsOnMethods = "testVerifyEnhancedFlagReflectedInAppResponse")
    public void testCreateSubOrgUser() throws Exception {

        switchedM2MToken = orgMgtRestClient.switchM2MToken(organizationId);
        assertNotNull(switchedM2MToken, "Switched M2M token should not be null.");

        UserObject endUser = new UserObject();
        endUser.setUserName(ORG_END_USER_USERNAME);
        endUser.setPassword(ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(ORG_END_USER_EMAIL));

        orgUserId = scim2RestClient.createSubOrgUser(endUser, switchedM2MToken);
        assertNotNull(orgUserId, "Sub-org user ID should not be null.");
    }

    @Test(priority = 7, dependsOnMethods = "testCreateSubOrgUser")
    public void testSendAuthorizeRequest() throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        params.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, params,
                getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain(), organizationId));

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "sessionDataKey not found on login page.");

        sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key should not be null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(priority = 8, dependsOnMethods = "testSendAuthorizeRequest")
    public void testSendLoginPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", ORG_END_USER_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", ORG_END_USER_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, tenantInfo.getDomain(), organizationId));

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        do {
            response = sendGetRequest(client, locationHeader.getValue());
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
        } while (locationHeader != null && !locationHeader.getValue().contains(CALLBACK_URL.split("\\?")[0]));

        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode, "Authorization code should not be null.");
    }

    @Test(priority = 9, dependsOnMethods = "testSendLoginPost")
    public void testGetAccessToken() throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        List<org.apache.http.Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", "Basic " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, params,
                getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain(), organizationId));
        assertNotNull(response, "Token endpoint response should not be null.");

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);

        assertTrue(jsonResponse.has("access_token"), "access_token is missing from token response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token should not be null.");
    }

    @Test(priority = 10, dependsOnMethods = "testGetAccessToken")
    public void testIntrospectAccessToken() throws Exception {

        String introspectUrl = getTenantQualifiedURL(OAuth2Constant.INTRO_SPEC_ENDPOINT, tenantInfo.getDomain());
        JSONObject introspectionResponse = introspectTokenWithTenant(client, accessToken, introspectUrl,
                tenantInfo.getTenantAdmin().getUserName(), tenantInfo.getTenantAdmin().getPassword());

        assertNotNull(introspectionResponse, "Introspection response should not be null.");
        assertTrue(introspectionResponse.containsKey("active"),
                "active field is missing from introspection response.");
        assertTrue((Boolean) introspectionResponse.get("active"), "Token should be active.");
        assertTrue(introspectionResponse.containsKey("username"),
                "username claim is missing from introspection response.");
        assertTrue(introspectionResponse.containsKey("org_id"),
                "org_id claim is missing in the introspection response.");
    }

    @Test(priority = 11, dependsOnMethods = "testIntrospectAccessToken")
    public void testLoginWithInvalidPassword() throws Exception {

        try (CloseableHttpClient freshClient = createHttpClient()) {

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("response_type", "code"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            params.add(new BasicNameValuePair("scope", "openid"));

            HttpResponse authorizeResponse = sendPostRequestWithParameters(freshClient, params,
                    getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain(), organizationId));
            Header locationHeader = authorizeResponse.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Location header for invalid-password authorize request is not available.");
            EntityUtils.consume(authorizeResponse.getEntity());

            HttpResponse loginPageResponse = sendGetRequest(freshClient, locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues =
                    DataExtractUtil.extractDataFromResponse(loginPageResponse, keyPositionMap);
            assertNotNull(keyValues, "sessionDataKey not found for negative test.");
            EntityUtils.consume(loginPageResponse.getEntity());

            String freshSessionDataKey = keyValues.get(0).getValue();
            assertNotNull(freshSessionDataKey, "Fresh session data key should not be null.");

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("username", ORG_END_USER_USERNAME));
            urlParameters.add(new BasicNameValuePair("password", "WrongPassword@123"));
            urlParameters.add(new BasicNameValuePair("sessionDataKey", freshSessionDataKey));

            HttpResponse loginResponse = sendPostRequestWithParameters(freshClient, urlParameters,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, tenantInfo.getDomain(), organizationId));
            Header loginLocationHeader = loginResponse.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(loginResponse.getEntity());

            String location = loginLocationHeader != null ? loginLocationHeader.getValue() : "";
            for (int i = 0; i < 5 && loginLocationHeader != null && !location.contains("code="); i++) {
                HttpResponse followResponse = sendGetRequest(freshClient, location);
                Header nextLocation = followResponse.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
                EntityUtils.consume(followResponse.getEntity());
                if (nextLocation == null) break;
                location = nextLocation.getValue();
            }
            Assert.assertFalse(location.contains("code="),
                    "Authorization code should NOT be present when wrong password is used.");
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() throws Exception {

        if (orgUserId != null && switchedM2MToken != null && scim2RestClient != null) {
            scim2RestClient.deleteSubOrgUser(orgUserId, switchedM2MToken);
        }
        if (organizationId != null && orgMgtRestClient != null) {
            orgMgtRestClient.deleteOrganization(organizationId);
        }
        if (rootApplicationId != null && oAuth2RestClient != null) {
            oAuth2RestClient.deleteApplication(rootApplicationId);
        }
        if (client != null) {
            client.close();
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (oAuth2RestClient != null) {
            oAuth2RestClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
    }

    private CloseableHttpClient createHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {
                        return false;
                    }
                })
                .build();
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }
}
