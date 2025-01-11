/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains tests for OAuth apps with same client id in multiple tenants.
 */
public class OAuthAppsWithSameClientIdTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public final static String DUMMY_CLIENT_ID = "dummy_client_id";
    public final static String DUMMY_CLIENT_SECRET = "dummy_client_secret";
    private static final String TENANT_1_DOMAIN = "tenant1.com";
    private static final String TENANT_1_ADMIN_USERNAME = "admin@tenant1.com";
    private static final String TENANT_1_ADMIN_PASSWORD = "Admin@123";
    private static final String TENANT_1_ADMIN_TENANT_AWARE_USERNAME = "admin";
    private static final String TENANT_1_USER_USERNAME = "userTenant1";
    private static final String TENANT_1_USER_PASSWORD = "User@Tenant1";
    private static final String TENANT_1_USER_EMAIL = "userTenant1@wso2.com";
    private static final String TENANT_2_DOMAIN = "tenant2.com";
    private static final String TENANT_2_ADMIN_USERNAME = "admin@tenant2.com";
    private static final String TENANT_2_ADMIN_PASSWORD = "Admin@123";
    private static final String TENANT_2_ADMIN_TENANT_AWARE_USERNAME = "admin";
    private static final String TENANT_2_USER_USERNAME = "userTenant2";
    private static final String TENANT_2_USER_PASSWORD = "User@Tenant2";
    private static final String TENANT_2_USER_EMAIL = "userTenant2@wso2.com";
    public final static String TENANT_1_AUTHORIZE_URL = "https://localhost:9853/t/tenant1.com/oauth2/authorize";

    private TenantMgtRestClient tenantMgtRestClient;
    private OAuth2RestClient oAuth2RestClientTenant1;
    private OAuth2RestClient oAuth2RestClientTenant2;
    private SCIM2RestClient scim2RestClientTenant1;
    private SCIM2RestClient scim2RestClientTenant2;
    private HttpClient client;

    private String tenant1UserId = null;
    private String tenant2UserId = null;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private String authorizationCode;
    private String accessToken;

    @BeforeClass(alwaysRun = true)
    private void testInit() throws Exception {

        super.init();
        tenantMgtRestClient = new TenantMgtRestClient(serverURL, tenantInfo);

        // Create the test tenants.
        addTenant(TENANT_1_DOMAIN, TENANT_1_ADMIN_USERNAME, TENANT_1_ADMIN_PASSWORD,
                TENANT_1_ADMIN_TENANT_AWARE_USERNAME);
        addTenant(TENANT_2_DOMAIN, TENANT_2_ADMIN_USERNAME, TENANT_2_ADMIN_PASSWORD,
                TENANT_2_ADMIN_TENANT_AWARE_USERNAME);

        // Create rest clients.
        Tenant tenant1 = getTenantInfo(TENANT_1_DOMAIN);
        oAuth2RestClientTenant1 = new OAuth2RestClient(serverURL, tenant1);
        scim2RestClientTenant1 = new SCIM2RestClient(serverURL, tenant1);
        Tenant tenant2 = getTenantInfo(TENANT_2_DOMAIN);
        oAuth2RestClientTenant2 = new OAuth2RestClient(serverURL, tenant2);
        scim2RestClientTenant2 = new SCIM2RestClient(serverURL, tenant2);

        // Create http client.
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();

        client = HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws IOException, AutomationUtilException {

        tenantMgtRestClient.closeHttpClient();
        oAuth2RestClientTenant1.closeHttpClient();
        oAuth2RestClientTenant2.closeHttpClient();
    }

    @AfterMethod
    public void afterMethod() throws IOException {

        deleteAllApplications(TENANT_1_DOMAIN);
        deleteAllApplications(TENANT_2_DOMAIN);

        if (StringUtils.isNotEmpty(tenant1UserId)) {
            getSCIMRestClient(TENANT_1_DOMAIN).deleteUser(tenant1UserId);
            tenant1UserId = null;
        }
        if (StringUtils.isNotEmpty(tenant2UserId)) {
            getSCIMRestClient(TENANT_2_DOMAIN).deleteUser(tenant2UserId);
            tenant2UserId = null;
        }

        sessionDataKey = null;
        sessionDataKeyConsent = null;
        authorizationCode = null;
        accessToken = null;
    }

    @Test(description = "Create two OAuth apps in the same tenant with same client id.")
    public void testCreateAppsWithSameClientIdInSameTenant() throws Exception {

        // Create first app.
        ApplicationResponseModel app1 = createApplication(TENANT_1_DOMAIN);
        Assert.assertNotNull(app1, "OAuth app creation failed for tenant 1.");

        // Try to create second app with the same client id and expect a conflict.
        StatusLine statusLine = createApplicationWithResponse(TENANT_1_DOMAIN);
        Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.SC_CONFLICT, "Expected status code not received.");
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id.",
            dependsOnMethods = "testCreateAppsWithSameClientIdInSameTenant")
    public void testCreateAppsWithSameClientIdInMultipleTenants() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);

        // Assertions for inbound configurations.
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        OpenIDConnectConfiguration tenant1AppConfig = getOAuthRestClient(TENANT_1_DOMAIN)
                .getOIDCInboundDetails(tenant1App.getId());
        Assert.assertNotNull(tenant1AppConfig.getClientId());
        Assert.assertNotNull(tenant1AppConfig.getClientSecret());

        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");
        OpenIDConnectConfiguration tenant2AppConfig = getOAuthRestClient(TENANT_2_DOMAIN)
                .getOIDCInboundDetails(tenant2App.getId());
        Assert.assertNotNull(tenant2AppConfig.getClientId());
        Assert.assertNotNull(tenant2AppConfig.getClientSecret());
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and retrieve " +
            "tenant app by client id.", dependsOnMethods = "testCreateAppsWithSameClientIdInMultipleTenants")
    public void testRetrieveTenantAppByClientId() throws JSONException, IOException {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);

        // Assertions for successful app creation.
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Retrieve app by client id.
        List<ApplicationListItem> applications = getOAuthRestClient(TENANT_1_DOMAIN)
                .getApplicationsByClientId(DUMMY_CLIENT_ID);
        Assert.assertEquals(applications.size(), 1);
        Assert.assertEquals(applications.get(0).getId(), tenant1App.getId());
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and update " +
            "inbound configurations of one app.", dependsOnMethods = "testRetrieveTenantAppByClientId")
    public void testCreateAppsAndUpdateInboundConfigurationsOfOne() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);

        // Assert for created app.
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Update inbound configs of tenant 1 app.
        OpenIDConnectConfiguration tenant1AppConfig = getOAuthRestClient(TENANT_1_DOMAIN)
                .getOIDCInboundDetails(tenant1App.getId());
        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        tenant1AppConfig.setGrantTypes(grantTypes);
        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, "http://localhost:8490/updated/callback");
        tenant1AppConfig.setCallbackURLs(callBackUrls);

        getOAuthRestClient(TENANT_1_DOMAIN).updateInboundDetailsOfApplication(tenant1App.getId(), tenant1AppConfig,
                RestBaseClient.OIDC);

        // Assert for updated inbound configs.
        OpenIDConnectConfiguration updatedTenant1AppConfig = getOAuthRestClient(TENANT_1_DOMAIN)
                .getOIDCInboundDetails(tenant1App.getId());
        Assert.assertNotNull(updatedTenant1AppConfig.getClientId());
        Assert.assertNotNull(updatedTenant1AppConfig.getClientSecret());
        Assert.assertEquals(updatedTenant1AppConfig.getGrantTypes(), grantTypes);
        Assert.assertEquals(updatedTenant1AppConfig.getCallbackURLs(), callBackUrls);

        // Assert inbound config of tenant 2 app is not updated.
        OpenIDConnectConfiguration tenant2AppConfig = getOAuthRestClient(TENANT_2_DOMAIN)
                .getOIDCInboundDetails(tenant2App.getId());
        Assert.assertNotNull(tenant2AppConfig.getClientId());
        Assert.assertNotNull(tenant2AppConfig.getClientSecret());

        List<String> originalCallBackUrls = new ArrayList<>();
        Collections.addAll(originalCallBackUrls, OAuth2Constant.CALLBACK_URL);
        Assert.assertEquals(tenant2AppConfig.getCallbackURLs(), originalCallBackUrls);

        List<String> originalGrantTypes = new ArrayList<>();
        Collections.addAll(originalGrantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        Assert.assertEquals(tenant2AppConfig.getGrantTypes(), originalGrantTypes);
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and delete an app " +
            "in one tenant.", dependsOnMethods = "testCreateAppsAndUpdateInboundConfigurationsOfOne")
    public void testCreateAppsAndDeleteOne() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);

        // Assertions for successful app creation.
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Delete tenant 1 app and assert for tenant 2 app.
        getOAuthRestClient(TENANT_1_DOMAIN).deleteApplication(tenant1App.getId());
        ApplicationListResponse tenant1Apps = getOAuthRestClient(TENANT_1_DOMAIN).getAllApplications();
        int appCount = getAppCount(tenant1Apps);
        Assert.assertEquals(appCount, 0);
        ApplicationListResponse tenant2Apps = getOAuthRestClient(TENANT_2_DOMAIN).getAllApplications();
        appCount = getAppCount(tenant2Apps);
        Assert.assertEquals(appCount, 1);
    }

    private static int getAppCount(ApplicationListResponse apps) {

        int appCount = 0;
        for (ApplicationListItem application : apps.getApplications()) {
            if ("Console".equalsIgnoreCase(application.getName())
                    || "My Account".equalsIgnoreCase(application.getName())) {
                continue;
            }
            appCount++;
        }
        return appCount;
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and delete " +
            "inbound configurations of one app.", dependsOnMethods = "testCreateAppsAndDeleteOne")
    public void testCreateAppsAndDeleteInboundConfigurationsOfOne() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);

        // Assertions for successful app creation.
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Delete OIDC inbound configurations of tenant 1 app.
        getOAuthRestClient(TENANT_1_DOMAIN).deleteInboundConfiguration(tenant1App.getId(), RestBaseClient.OIDC);

        // Try to retrieve inbound configs of tenant 1 app and expect an exception.
        try {
            OpenIDConnectConfiguration tenant1AppConfig = getOAuthRestClient(TENANT_1_DOMAIN)
                    .getOIDCInboundDetails(tenant1App.getId());
            Assert.fail("Expected exception not received.");
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }

        // Assert inbound config of tenant 2 app is not deleted.
        OpenIDConnectConfiguration tenant2AppConfig = getOAuthRestClient(TENANT_2_DOMAIN)
                .getOIDCInboundDetails(tenant2App.getId());
        Assert.assertNotNull(tenant2AppConfig.getClientId());
        Assert.assertNotNull(tenant2AppConfig.getClientSecret());
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and try to login " +
            "with the user for the tenant.", dependsOnMethods = "testCreateAppsAndDeleteInboundConfigurationsOfOne")
    public void testOAuthApplicationLoginSuccess() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Create users.
        tenant1UserId = createUser(TENANT_1_DOMAIN);
        tenant2UserId = createUser(TENANT_2_DOMAIN);

        // Authenticate.
        initiateAuthorizationRequest(true);
        authenticateUser(true, TENANT_1_USER_USERNAME, TENANT_1_USER_PASSWORD, TENANT_1_DOMAIN);
        performConsentApproval(true, TENANT_1_DOMAIN);
        generateAuthzCodeAccessToken(true, TENANT_1_DOMAIN);
        introspectActiveAccessToken(TENANT_1_DOMAIN, TENANT_1_ADMIN_USERNAME, TENANT_1_ADMIN_PASSWORD);
    }

    @Test(description = "Create two OAuth apps in two tenants with the same client id and try to login " +
            "with the user from the other tenant.", dependsOnMethods = "testOAuthApplicationLoginSuccess",
            expectedExceptions = AssertionError.class)
    public void testOAuthApplicationLoginIncorrectTenant() throws Exception {

        // Create oauth apps in both tenants.
        ApplicationResponseModel tenant1App = createApplication(TENANT_1_DOMAIN);
        ApplicationResponseModel tenant2App = createApplication(TENANT_2_DOMAIN);
        Assert.assertNotNull(tenant1App, "OAuth app creation failed for tenant 1.");
        Assert.assertNotNull(tenant2App, "OAuth app creation failed for tenant 2.");

        // Create users.
        tenant1UserId = createUser(TENANT_1_DOMAIN);
        tenant2UserId = createUser(TENANT_2_DOMAIN);

        // Authenticate.
        initiateAuthorizationRequest(true);
        authenticateUser(true, TENANT_2_USER_USERNAME, TENANT_2_USER_PASSWORD, TENANT_1_DOMAIN);
        Assert.fail("Expected exception not received.");
    }

    private void addTenant(String tenantDomain, String adminUsername, String adminPassword,
                           String adminTenantAwareUsername) throws Exception {

        Owner tenantOwner = new Owner();
        tenantOwner.setUsername(adminTenantAwareUsername);
        tenantOwner.setPassword(adminPassword);
        tenantOwner.setEmail(adminUsername);
        tenantOwner.setFirstname("FirstName");
        tenantOwner.setLastname("LastName");
        tenantOwner.setProvisioningMethod("inline-password");

        TenantModel tenantReqModel = new TenantModel();
        tenantReqModel.setDomain(tenantDomain);
        tenantReqModel.addOwnersItem(tenantOwner);
        String tenantId = tenantMgtRestClient.addTenant(tenantReqModel);
        Assert.assertNotNull(tenantId, "Tenant creation failed for " + tenantDomain);
    }

    private Tenant getTenantInfo(String tenantDomain) {

        User tenantAdmin = new User();
        if (StringUtils.equals(tenantDomain, TENANT_1_DOMAIN)) {
            tenantAdmin.setUserName(TENANT_1_ADMIN_USERNAME);
            tenantAdmin.setPassword(TENANT_1_ADMIN_PASSWORD);
        } else if (StringUtils.equals(tenantDomain, TENANT_2_DOMAIN)) {
            tenantAdmin.setUserName(TENANT_2_ADMIN_USERNAME);
            tenantAdmin.setPassword(TENANT_2_ADMIN_PASSWORD);
        }

        Tenant tenant = new Tenant();
        tenant.setDomain(tenantDomain);
        tenant.setContextUser(tenantAdmin);
        return tenant;
    }

    private OAuth2RestClient getOAuthRestClient(String tenantDomain) {

        OAuth2RestClient restClient;
        if (StringUtils.equals(tenantDomain, TENANT_1_DOMAIN)) {
            restClient = oAuth2RestClientTenant1;
        } else {
            restClient = oAuth2RestClientTenant2;
        }
        return restClient;
    }

    private SCIM2RestClient getSCIMRestClient(String tenantDomain) {

        SCIM2RestClient restClient;
        if (StringUtils.equals(tenantDomain, TENANT_1_DOMAIN)) {
            restClient = scim2RestClientTenant1;
        } else {
            restClient = scim2RestClientTenant2;
        }
        return restClient;
    }

    private ApplicationModel getApplicationModel() {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setClientId(DUMMY_CLIENT_ID);
        oidcConfig.setClientSecret(DUMMY_CLIENT_SECRET);
        oidcConfig.setPublicClient(false);

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        oidcConfig.setGrantTypes(grantTypes);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel();
        application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setClaimConfiguration(setApplicationClaimConfig());

        return  application;
    }

    private ApplicationResponseModel createApplication(String tenantDomain) throws JSONException, IOException {

        OAuth2RestClient restClient = getOAuthRestClient(tenantDomain);
        String appId = restClient.createApplication(getApplicationModel());
        return restClient.getApplication(appId);
    }

    private void deleteAllApplications(String tenantDomain) throws IOException {

        OAuth2RestClient restClient = getOAuthRestClient(tenantDomain);
        ApplicationListResponse applications = restClient.getAllApplications();
        for (ApplicationListItem application : applications.getApplications()) {
            if ("Console".equalsIgnoreCase(application.getName())
                    || "My Account".equalsIgnoreCase(application.getName())) {
                continue;
            }
            restClient.deleteApplication(application.getId());
        }
    }

    private StatusLine createApplicationWithResponse(String tenantDomain)
            throws JSONException, IOException {

        OAuth2RestClient restClient = getOAuthRestClient(tenantDomain);
        return restClient.createApplicationWithResponse(getApplicationModel());
    }

    private String createUser(String tenantDomain) throws Exception {

        SCIM2RestClient restClient = getSCIMRestClient(tenantDomain);
        UserObject user = new UserObject();

        if (StringUtils.equals(tenantDomain, TENANT_1_DOMAIN)) {
            user.setUserName(TENANT_1_USER_USERNAME);
            user.setPassword(TENANT_1_USER_PASSWORD);
            user.addEmail(new Email().value(TENANT_1_USER_EMAIL));
        } else {
            user.setUserName(TENANT_2_USER_USERNAME);
            user.setPassword(TENANT_2_USER_PASSWORD);
            user.addEmail(new Email().value(TENANT_2_USER_EMAIL));
        }

        return restClient.createUser(user);
    }

    /**
     * Playground app will initiate authorization request to IS and obtain session data key.
     *
     * @param tenantQualified Whether tenant qualified urls are enabled.
     * @throws IOException IOException.
     */
    private void initiateAuthorizationRequest(boolean tenantQualified) throws IOException {

        List<NameValuePair> urlParameters = getOIDCInitiationRequestParams(tenantQualified);
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization response header is null.");

        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
        EntityUtils.consume(response.getEntity());
    }

    private List<NameValuePair> getOIDCInitiationRequestParams(boolean tenantQualified) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", DUMMY_CLIENT_ID));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        if (tenantQualified) {
            urlParameters.add(new BasicNameValuePair("authorizeEndpoint", TENANT_1_AUTHORIZE_URL));
        } else {
            urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.AUTHORIZE_ENDPOINT_URL));
        }
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " +
                OAuth2Constant.OAUTH2_SCOPE_EMAIL));

        return urlParameters;
    }

    /**
     * Provide user credentials and authenticate to the system.
     *
     * @param tenantQualified   Whether tenant qualified urls are enabled.
     * @param username          Username of the user.
     * @param password          Password of the user.
     * @param tenantDomain      Tenant domain.
     * @throws IOException IOException.
     */
    private void authenticateUser(boolean tenantQualified, String username, String password, String tenantDomain)
            throws Exception {

        // Pass user credentials to commonauth endpoint and authenticate the user.
        HttpResponse response;
        if (tenantQualified) {
            response = sendLoginPostForCustomUsers(client, sessionDataKey, username, password, tenantDomain);
        } else {
            response = sendLoginPostForCustomUsers(client, sessionDataKey, username, password);
        }
        Assert.assertNotNull(response, "OIDC login request response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC login response header is null.");
        EntityUtils.consume(response.getEntity());

        // Get the sessionDatakeyConsent from the redirection after authenticating the user.
        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil
                .extractSessionConsentDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent keyValues map is null.");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Approve the consent.
     *
     * @param tenantQualified   Whether tenant qualified urls are enabled.
     * @param tenantDomain      Tenant domain.
     * @throws IOException IOException.
     */
    private void performConsentApproval(boolean tenantQualified, String tenantDomain) throws IOException {

        HttpResponse response;
        if (tenantQualified) {
            response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, null, tenantDomain);
        } else {
            response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, null);
        }
        Assert.assertNotNull(response, "OIDC consent approval request response is null.");
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "OIDC consent approval request location header is null.");
        EntityUtils.consume(response.getEntity());

        // Get authorization code flow.
        String[] queryParams = new URL(locationHeader.getValue()).getQuery().split("&");
        Assert.assertNotEquals(queryParams.length, 0, "Authorization code not received.");
        for (String param : queryParams) {
            if (param.contains(OAuth2Constant.OAUTH2_GRANT_TYPE_CODE)) {
                authorizationCode = param.split("=")[1];
            }
        }
        Assert.assertNotNull(authorizationCode, "Authorization code not received.");
    }

    /**
     * Exchange authorization code and get access token.
     *
     * @param tenantQualified   Whether tenant qualified urls are enabled.
     * @param tenantDomain      Tenant domain.
     * @throws Exception IOException.
     */
    private void generateAuthzCodeAccessToken(boolean tenantQualified, String tenantDomain) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));

        String url;
        if (tenantQualified) {
            url = OAuth2Constant.TENANT_TOKEN_ENDPOINT.replace(OAuth2Constant.TENANT_PLACEHOLDER, tenantDomain);
        } else {
            url = OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        }
        JSONObject jsonResponse = responseObject(url, urlParameters, DUMMY_CLIENT_ID, DUMMY_CLIENT_SECRET);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
    }

    /**
     * Introspect the obtained access token and it should be an active token.
     *
     * @param tenantDomain Tenant domain.
     * @throws Exception Exception.
     */
    private void introspectActiveAccessToken(String tenantDomain, String adminUsername, String adminPassword)
            throws Exception {

        String url = OAuth2Constant.TENANT_INTROSPECT_ENDPOINT.replace(
                OAuth2Constant.TENANT_PLACEHOLDER, tenantDomain);
        JSONObject object = introspectTokenWithTenant(client, accessToken, url, adminUsername, adminPassword);
        Assert.assertEquals(object.get("active"), true);
    }

    /**
     * Build post request and return json response object.
     *
     * @param endpoint       Endpoint.
     * @param postParameters postParameters.
     * @param key            Basic authentication key.
     * @param secret         Basic authentication secret.
     * @return JSON object of the response.
     * @throws Exception Exception.
     */
    private JSONObject responseObject(String endpoint, List<NameValuePair> postParameters, String key, String secret)
            throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(key, secret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }

        return json;
    }
}
