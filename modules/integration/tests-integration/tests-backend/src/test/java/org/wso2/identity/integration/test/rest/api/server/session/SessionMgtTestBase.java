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

package org.wso2.identity.integration.test.rest.api.server.session;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Base test class for Session Management REST APIs.
 */
public class SessionMgtTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "configs.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.configs.v1";

    // OAuth2 and OIDC related constants.
    public static final String CODE_GRANT_TYPE = "authorization_code";
    public static final String OPENID_SCOPE = "openid";
    public static final String CODE_RESPONSE_TYPE = "code";
    
    // URL paths.
    public static final String AUTH_URL = "/oauth2/authorize";
    public static final String COMMONAUTH_URL = "/commonauth";
    public static final String CONFIGS_API_PATH = "/configs";
    public static final String PATH_SEPARATOR = "/";
    public static final String TENANT_PATH = "t/";
    
    // Request parameter keys.
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String REDIRECT_URI_KEY = "redirect_uri";
    public static final String SCOPE_KEY = "scope";
    public static final String RESPONSE_TYPE_KEY = "response_type";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String USERNAME_PARAM = "username";
    public static final String PASSWORD_PARAM = "password";
    public static final String CHKREMEMBER_PARAM = "chkRemember";
    
    // Session configuration parameter keys.
    public static final String IDLE_SESSION_TIMEOUT_KEY = "idleSessionTimeoutPeriod";
    public static final String REMEMBER_ME_PERIOD_KEY = "rememberMePeriod";
    public static final String ENABLE_MAX_TIMEOUT_KEY = "enableMaximumSessionTimeoutPeriod";
    public static final String MAX_TIMEOUT_KEY = "maximumSessionTimeoutPeriod";
    
    // Default configuration values.
    public static final boolean DEFAULT_ENABLE_MAX_TIMEOUT = false;
    public static final String DEFAULT_MAX_TIMEOUT = "43200";
    
    // Patch operations.
    public static final String OPERATION_REPLACE = "REPLACE";
    public static final String OPERATION_REMOVE = "REMOVE";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    /**
     * Restores session configuration to system defaults by removing all customizations.
     * Uses REMOVE operations to restore default values for all session configuration keys.
     *
     * @throws Exception If an error occurs while restoring configuration.
     */
    protected void restoreDefaultConfiguration() throws Exception {

        List<Map<String, String>> patches = new ArrayList<>();
        patches.add(createPatchOperation(OPERATION_REMOVE, IDLE_SESSION_TIMEOUT_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, REMEMBER_ME_PERIOD_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, ENABLE_MAX_TIMEOUT_KEY, null));
        patches.add(createPatchOperation(OPERATION_REMOVE, MAX_TIMEOUT_KEY, null));
        
        String patchBody = convertPatchesToJson(patches);
        Response response = getResponseOfPatch(CONFIGS_API_PATH, patchBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    /**
     * Updates server configuration for session timeouts.
     *
     * @param idleTimeout      Idle session timeout period in minutes.
     * @param rememberMe       Remember me period in minutes.
     * @param enableMaxTimeout Enable/disable maximum session timeout.
     * @param maxTimeout       Maximum session timeout period in minutes.
     * @throws Exception If an error occurs while updating configuration.
     */
    protected void updateServerConfiguration(String idleTimeout, String rememberMe,
                                              String enableMaxTimeout, String maxTimeout) throws Exception {

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
            String patchBody = convertPatchesToJson(patches);
            Response response = getResponseOfPatch(CONFIGS_API_PATH, patchBody);
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
        }
    }

    /**
     * Creates a patch operation for configuration update.
     *
     * @param operation Operation type (REPLACE or REMOVE).
     * @param path      Configuration path.
     * @param value     Configuration value (can be null for REMOVE operation).
     * @return Map representing a patch operation.
     */
    protected Map<String, String> createPatchOperation(String operation, String path, String value) {

        Map<String, String> patch = new HashMap<>();
        patch.put("operation", operation);
        patch.put("path", "/" + path);
        if (value != null) {
            patch.put("value", value);
        }
        return patch;
    }

    /**
     * Converts patch operations to JSON string.
     *
     * @param patches List of patch operations.
     * @return JSON string representation of patches.
     */
    protected String convertPatchesToJson(List<Map<String, String>> patches) {

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

    /**
     * Performs login flow and returns the cookie store with session cookies.
     *
     * @param clientId           OAuth2 client ID.
     * @param callbackUrl        Application callback URL.
     * @param rememberMe         Whether to enable remember me.
     * @param authenticatingUser Username for authentication.
     * @param authenticatingPass Password for authentication.
     * @return CookieStore containing session cookies.
     * @throws Exception If an error occurs during login.
     */
    protected CookieStore performLogin(String clientId, String callbackUrl, boolean rememberMe,
                                       String authenticatingUser, String authenticatingPass) throws Exception {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        CookieStore cookieStore = new BasicCookieStore();
        
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {
            
            // Step 1: Initiate authorize request.
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put(CLIENT_ID_KEY, clientId);
            queryParams.put(REDIRECT_URI_KEY, callbackUrl);
            queryParams.put(SCOPE_KEY, OPENID_SCOPE);
            queryParams.put(RESPONSE_TYPE_KEY, CODE_RESPONSE_TYPE);
            
            URIBuilder uriBuilder = new URIBuilder(buildUrl(AUTH_URL));
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            
            HttpGet authRequest = new HttpGet(uriBuilder.build());
            String sessionDataKey = null;
            
            try (CloseableHttpResponse response = client.execute(authRequest)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                    if (location != null && location.contains(SESSION_DATA_KEY)) {
                        sessionDataKey = extractQueryParam(location, SESSION_DATA_KEY);
                    }
                }
            }
            
            assertNotNull(sessionDataKey, "Session data key should be present");
            
            // Step 2: Submit login credentials.
            HttpPost loginPost = new HttpPost(buildUrl(COMMONAUTH_URL));
            List<NameValuePair> loginParams = new ArrayList<>();
            loginParams.add(new BasicNameValuePair(USERNAME_PARAM, removeTenantDomain(authenticatingUser)));
            loginParams.add(new BasicNameValuePair(PASSWORD_PARAM, authenticatingPass));
            loginParams.add(new BasicNameValuePair(SESSION_DATA_KEY, sessionDataKey));
            if (rememberMe) {
                loginParams.add(new BasicNameValuePair(CHKREMEMBER_PARAM, "on"));
            }
            
            loginPost.setEntity(new UrlEncodedFormEntity(loginParams, StandardCharsets.UTF_8));
            loginPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            
            try (CloseableHttpResponse loginResponse = client.execute(loginPost)) {
                int statusCode = loginResponse.getStatusLine().getStatusCode();
                assertEquals(statusCode, HttpStatus.SC_MOVED_TEMPORARILY, "Login should succeed");
            }
        }
        
        return cookieStore;
    }

    /**
     * Checks if the session is still active by making an authorize request.
     *
     * @param cookieStore Cookie store containing session cookies.
     * @param clientId    OAuth2 client ID.
     * @param callbackUrl Application callback URL.
     * @return True if session is active, false otherwise.
     * @throws Exception If an error occurs during session check.
     */
    protected boolean checkSessionActive(CookieStore cookieStore, String clientId, String callbackUrl) 
            throws Exception {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {
            
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put(CLIENT_ID_KEY, clientId);
            queryParams.put(REDIRECT_URI_KEY, callbackUrl);
            queryParams.put(SCOPE_KEY, OPENID_SCOPE);
            queryParams.put(RESPONSE_TYPE_KEY, CODE_RESPONSE_TYPE);
            
            URIBuilder uriBuilder = new URIBuilder(buildUrl(AUTH_URL));
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            
            HttpGet authRequest = new HttpGet(uriBuilder.build());
            
            try (CloseableHttpResponse response = client.execute(authRequest)) {
                String location = null;
                if (response.getFirstHeader(HttpHeaders.LOCATION) != null) {
                    location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                }
                
                // If redirected to callback with code, session is active.
                return location != null && location.contains(callbackUrl) && location.contains("code=");
            }
        }
    }

    /**
     * Builds the full URL for the given path with tenant context.
     *
     * @param path The path to append to the base URL.
     * @return The full URL with tenant context.
     */
    protected String buildUrl(String path) {

        String baseUrl = serverURL;
        if (!StringUtils.endsWith(baseUrl, PATH_SEPARATOR)) {
            baseUrl = baseUrl + PATH_SEPARATOR;
        }
        return baseUrl + TENANT_PATH + tenant + path;
    }

    /**
     * Extracts a query parameter value from a URL.
     *
     * @param url       The URL containing query parameters.
     * @param paramName The name of the parameter to extract.
     * @return The value of the parameter, or null if not found.
     * @throws Exception If an error occurs during extraction.
     */
    protected String extractQueryParam(String url, String paramName) throws Exception {

        URI uri = new URI(url);
        String query = uri.getQuery();
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

    /**
     * Removes tenant domain from the username if present.
     *
     * @param username The username which may contain tenant domain.
     * @return Username without tenant domain.
     */
    private String removeTenantDomain(String username) {

        if (username != null && username.endsWith("@" + tenant)) {
            return username.substring(0, username.indexOf("@"));
        }
        return username;
    }
}
