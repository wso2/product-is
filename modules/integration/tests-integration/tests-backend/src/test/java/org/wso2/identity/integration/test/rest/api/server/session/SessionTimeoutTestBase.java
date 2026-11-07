/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.session;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.ServerConfigRestClient;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Base class for the session timeout tests.
 * <p>
 * The session timeout configurations are tenant wide, so each test class creates its own tenant and changes the
 * configurations only in that tenant. This lets the test classes run concurrently. The classes also avoid the static
 * RestAssured state used by the REST API test bases.
 */
public class SessionTimeoutTestBase extends ISIntegrationTest {

    // OAuth2 and OIDC related constants.
    private static final String CODE_GRANT_TYPE = "authorization_code";
    private static final String OPENID_SCOPE = "openid";
    private static final String CODE_RESPONSE_TYPE = "code";
    private static final String APPLICATION_NAME = "SessionTestApp";
    private static final String CALLBACK_URL = "https://example.com/callback";

    // URL paths.
    private static final String AUTH_URL = "/oauth2/authorize";
    private static final String COMMONAUTH_URL = "/commonauth";
    private static final String TENANT_PATH = "t/";

    // Request parameter keys.
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String SCOPE_KEY = "scope";
    private static final String RESPONSE_TYPE_KEY = "response_type";
    private static final String SESSION_DATA_KEY = "sessionDataKey";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String CHKREMEMBER_PARAM = "chkRemember";

    // Session configuration keys.
    protected static final String IDLE_SESSION_TIMEOUT_KEY = "idleSessionTimeoutPeriod";
    protected static final String REMEMBER_ME_PERIOD_KEY = "rememberMePeriod";
    protected static final String ENABLE_MAX_TIMEOUT_KEY = "enableMaximumSessionTimeoutPeriod";
    protected static final String MAX_TIMEOUT_KEY = "maximumSessionTimeoutPeriod";

    // Default configuration values.
    protected static final boolean DEFAULT_ENABLE_MAX_TIMEOUT = false;
    protected static final String DEFAULT_MAX_TIMEOUT = "43200";

    // Patch operations.
    private static final String OPERATION_REPLACE = "REPLACE";
    private static final String OPERATION_REMOVE = "REMOVE";

    // Tenant admin credentials.
    private static final String TENANT_ADMIN_USERNAME = "admin";
    private static final String TENANT_ADMIN_PASSWORD = "Admin@123";

    private final String tenantDomain;

    private TenantMgtRestClient tenantMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private ServerConfigRestClient serverConfigRestClient;
    private String appId;
    private String clientId;

    /**
     * The automation context is initialized in the constructor because TestNG creates the test instances one at a
     * time, while the @BeforeClass methods of concurrently running classes run in parallel and the automation
     * context initialization is not thread safe.
     *
     * @param tenantDomain Domain of the tenant created for the test class. It must be unique to the test class.
     * @throws Exception If an error occurs while initializing the automation context.
     */
    protected SessionTimeoutTestBase(String tenantDomain) throws Exception {

        this.tenantDomain = tenantDomain;
        super.init();
    }

    @BeforeClass(alwaysRun = true)
    public void initTenantAndApplication() throws Exception {

        tenantMgtRestClient = new TenantMgtRestClient(serverURL, tenantInfo);
        addTenant();

        Tenant tenant = getTenantInfo();
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenant);
        serverConfigRestClient = new ServerConfigRestClient(serverURL, tenant);
        createTestApplication();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        // The tenant cannot be deleted, so restore its session configurations and remove the application.
        if (serverConfigRestClient != null) {
            restoreDefaultConfiguration();
            serverConfigRestClient.closeHttpClient();
        }
        if (oAuth2RestClient != null) {
            if (appId != null) {
                oAuth2RestClient.deleteApplication(appId);
            }
            oAuth2RestClient.closeHttpClient();
        }
        if (tenantMgtRestClient != null) {
            tenantMgtRestClient.closeHttpClient();
        }
    }

    /**
     * Updates the session timeout configurations of the tenant.
     *
     * @param idleTimeout      Idle session timeout period in minutes.
     * @param rememberMe       Remember me period in minutes.
     * @param enableMaxTimeout Enable/disable maximum session timeout.
     * @param maxTimeout       Maximum session timeout period in minutes.
     * @throws Exception If an error occurs while updating the configurations.
     */
    protected void updateServerConfiguration(String idleTimeout, String rememberMe, String enableMaxTimeout,
                                             String maxTimeout) throws Exception {

        List<Map<String, String>> patches = new ArrayList<>();
        if (idleTimeout != null) {
            patches.add(createPatchOperation(OPERATION_REPLACE, IDLE_SESSION_TIMEOUT_KEY, idleTimeout));
        }
        if (rememberMe != null) {
            patches.add(createPatchOperation(OPERATION_REPLACE, REMEMBER_ME_PERIOD_KEY, rememberMe));
        }
        if (enableMaxTimeout != null) {
            patches.add(createPatchOperation(OPERATION_REPLACE, ENABLE_MAX_TIMEOUT_KEY, enableMaxTimeout));
        }
        if (maxTimeout != null) {
            patches.add(createPatchOperation(OPERATION_REPLACE, MAX_TIMEOUT_KEY, maxTimeout));
        }
        if (!patches.isEmpty()) {
            serverConfigRestClient.patchServerConfigurations(convertPatchesToJson(patches));
        }
    }

    /**
     * Restores the session timeout configurations of the tenant to the defaults.
     *
     * @throws Exception If an error occurs while restoring the configurations.
     */
    protected void restoreDefaultConfiguration() throws Exception {

        List<Map<String, String>> patches = new ArrayList<>();
        patches.add(createPatchOperation(OPERATION_REMOVE, IDLE_SESSION_TIMEOUT_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, REMEMBER_ME_PERIOD_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, ENABLE_MAX_TIMEOUT_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, MAX_TIMEOUT_KEY, null));
        serverConfigRestClient.patchServerConfigurations(convertPatchesToJson(patches));
    }

    /**
     * Gets the server configurations of the tenant.
     *
     * @return Server configurations.
     * @throws Exception If an error occurs while getting the configurations.
     */
    protected JSONObject getServerConfiguration() throws Exception {

        return serverConfigRestClient.getServerConfigurations();
    }

    /**
     * Logs in the tenant admin to the test application and returns the cookie store with the session cookies.
     *
     * @param rememberMe Whether to enable remember me.
     * @return CookieStore containing the session cookies.
     * @throws Exception If an error occurs during login.
     */
    protected CookieStore performLogin(boolean rememberMe) throws Exception {

        CookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient client = createHttpClient(cookieStore)) {
            // Step 1: Initiate the authorize request.
            String sessionDataKey = null;
            try (CloseableHttpResponse response = client.execute(new HttpGet(buildAuthorizeRequestUri()))) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                    if (location != null && location.contains(SESSION_DATA_KEY)) {
                        sessionDataKey = extractQueryParam(location, SESSION_DATA_KEY);
                    }
                }
            }
            assertNotNull(sessionDataKey, "Session data key should be present");

            // Step 2: Submit the login credentials.
            HttpPost loginPost = new HttpPost(buildUrl(COMMONAUTH_URL));
            List<NameValuePair> loginParams = new ArrayList<>();
            loginParams.add(new BasicNameValuePair(USERNAME_PARAM, TENANT_ADMIN_USERNAME));
            loginParams.add(new BasicNameValuePair(PASSWORD_PARAM, TENANT_ADMIN_PASSWORD));
            loginParams.add(new BasicNameValuePair(SESSION_DATA_KEY, sessionDataKey));
            if (rememberMe) {
                loginParams.add(new BasicNameValuePair(CHKREMEMBER_PARAM, "on"));
            }
            loginPost.setEntity(new UrlEncodedFormEntity(loginParams, StandardCharsets.UTF_8));
            loginPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse loginResponse = client.execute(loginPost)) {
                assertEquals(loginResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                        "Login should succeed");
            }
        }
        return cookieStore;
    }

    /**
     * Checks whether the session is still active by making an authorize request.
     *
     * @param cookieStore Cookie store containing the session cookies.
     * @return True if the session is active, false otherwise.
     * @throws Exception If an error occurs during the session check.
     */
    protected boolean checkSessionActive(CookieStore cookieStore) throws Exception {

        try (CloseableHttpClient client = createHttpClient(cookieStore);
             CloseableHttpResponse response = client.execute(new HttpGet(buildAuthorizeRequestUri()))) {
            String location = null;
            if (response.getFirstHeader(HttpHeaders.LOCATION) != null) {
                location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
            }
            // If redirected to the callback with a code, the session is active.
            return location != null && location.contains(CALLBACK_URL) && location.contains("code=");
        }
    }

    private void addTenant() throws Exception {

        Owner tenantOwner = new Owner();
        tenantOwner.setUsername(TENANT_ADMIN_USERNAME);
        tenantOwner.setPassword(TENANT_ADMIN_PASSWORD);
        tenantOwner.setEmail(TENANT_ADMIN_USERNAME + "@" + tenantDomain);
        tenantOwner.setFirstname("FirstName");
        tenantOwner.setLastname("LastName");
        tenantOwner.setProvisioningMethod("inline-password");

        TenantModel tenantReqModel = new TenantModel();
        tenantReqModel.setDomain(tenantDomain);
        tenantReqModel.addOwnersItem(tenantOwner);
        tenantMgtRestClient.addTenant(tenantReqModel);
    }

    private Tenant getTenantInfo() {

        User tenantAdmin = new User();
        tenantAdmin.setUserName(TENANT_ADMIN_USERNAME + "@" + tenantDomain);
        tenantAdmin.setPassword(TENANT_ADMIN_PASSWORD);

        Tenant tenant = new Tenant();
        tenant.setDomain(tenantDomain);
        tenant.setContextUser(tenantAdmin);
        return tenant;
    }

    private void createTestApplication() throws Exception {

        ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setName(APPLICATION_NAME);
        InboundProtocols inboundProtocols = new InboundProtocols();
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(CODE_GRANT_TYPE)));
        oidcConfig.setCallbackURLs(new ArrayList<>(Collections.singletonList(CALLBACK_URL)));
        inboundProtocols.setOidc(oidcConfig);
        applicationModel.setInboundProtocolConfiguration(inboundProtocols);

        appId = oAuth2RestClient.createApplication(applicationModel);
        clientId = oAuth2RestClient.getOIDCInboundDetails(appId).getClientId();
    }

    private Map<String, String> createPatchOperation(String operation, String path, String value) {

        Map<String, String> patch = new HashMap<>();
        patch.put("operation", operation);
        patch.put("path", "/" + path);
        if (value != null) {
            patch.put("value", value);
        }
        return patch;
    }

    private String convertPatchesToJson(List<Map<String, String>> patches) {

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < patches.size(); i++) {
            Map<String, String> patch = patches.get(i);
            json.append("{");
            json.append("\"operation\":\"").append(patch.get("operation")).append("\",");
            json.append("\"path\":\"").append(patch.get("path")).append("\"");
            if (patch.containsKey("value")) {
                json.append(",\"value\":\"").append(patch.get("value")).append("\"");
            }
            json.append("}");
            if (i < patches.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private CloseableHttpClient createHttpClient(CookieStore cookieStore) {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        return HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }

    private URI buildAuthorizeRequestUri() throws Exception {

        return new URIBuilder(buildUrl(AUTH_URL))
                .addParameter(CLIENT_ID_KEY, clientId)
                .addParameter(REDIRECT_URI_KEY, CALLBACK_URL)
                .addParameter(SCOPE_KEY, OPENID_SCOPE)
                .addParameter(RESPONSE_TYPE_KEY, CODE_RESPONSE_TYPE)
                .build();
    }

    private String buildUrl(String path) {

        String baseUrl = serverURL.endsWith("/") ? serverURL : serverURL + "/";
        return baseUrl + TENANT_PATH + tenantDomain + path;
    }

    private String extractQueryParam(String url, String paramName) throws Exception {

        String query = new URI(url).getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals(paramName)) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
