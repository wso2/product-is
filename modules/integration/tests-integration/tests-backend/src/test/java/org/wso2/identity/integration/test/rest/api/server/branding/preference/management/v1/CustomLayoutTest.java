/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.FIDP_PARAM;

/**
 * Test class for verifying the custom layout feature in both configuration and runtime scenarios.
 */
public class CustomLayoutTest extends BrandingPreferenceManagementTestBase {

    private static final String APPLICATION_NAME_1 = "TestApp1";
    private static final String APPLICATION_NAME_2 = "TestApp2";
    private static final String ORGANIZATION_NAME = "TestOrganization";
    private static final String RESOURCE_ID_PLACEHOLDER = "{{resourceId}}";
    private static final String BRANDING_TYPE_PLACEHOLDER = "{{type}}";
    private static final String BRANDING_RESOURCE_NAME = "{{name}}";
    private static final String BRANDING_TEST_JSON_FILE = "add-custom-layout.json";
    private static final String UPDATED_BRANDING_TEST_JSON_FILE = "update-custom-layout.json";
    private static final String ROOT_TENANT = "RootTenant";
    private static final String ROOT_APP = "RootApp";
    private static final String SUB_ORG = "SubOrg";
    private static final String SUB_ORG_APP = "SubOrgApp";
    private static final String AUTH_URL = "/oauth2/authorize";
    private static final String COMMON_AUTH_URL = "/commonauth";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String REDIRECT_URI_KEY = "redirect_uri";
    private static final String SCOPE_KEY = "scope";
    private static final String RESPONSE_TYPE_KEY = "response_type";
    private static final String OPENID_SCOPE = "openid";
    private static final String CODE_RESPONSE_TYPE = "code";
    private static final String ORG_TYPE = "ORG";
    private static final String APP_TYPE = "APP";
    private static final String CODE_GRANT_TYPE = "authorization_code";
    private static final String CALLBACK_URL = "https://example.com/oidc-callback";
    private static final String SUB_ORG_LOGIN_IDP = "OrganizationSSO";
    private static final String ORG_NAME_PAGE = "org_name.do";
    private static final String SESSION_DATA_KEY = "sessionDataKey";
    private static final String HTML_ELEMENT = "<div class=\"custom-tag-class-{{resourceId}}\" id=\"custom-tag\">" +
            "Resource id: {{resourceId}}</div>";
    private static final String CSS_CONTENT = ".custom-tag-class-{{resourceId}} {\n" +
            "    color: red !important;\n" +
            "}";
    private static final String JS_CONTENT = "const reference = document.getElementById(\"custom-tag\");\n" +
            "\n" +
            "const newEl = document.createElement(\"div\");\n" +
            "newEl.textContent = \"This is a text from JS - {{resourceId}}\";\n" +
            "\n" +
            "reference.after(newEl);";
    private static final String UPDATED_HTML_ELEMENT = "<div class=\"custom-tag-class-{{resourceId}}-updated\" " +
            "id=\"custom-tag\">Resource id: {{resourceId}}-updated</div>";
    private static final String UPDATED_CSS_CONTENT = ".custom-tag-class-{{resourceId}}-updated {\n" +
            "    color: red !important;\n" +
            "}";
    private static final String UPDATED_JS_CONTENT = "const reference = document.getElementById(\"custom-tag\");\n" +
            "\n" +
            "const newEl = document.createElement(\"div\");\n" +
            "newEl.textContent = \"This is a text from JS - {{resourceId}}-updated\";\n" +
            "\n" +
            "reference.after(newEl);";

    private String subOrgID;
    private String subOrgToken;
    private String appId1;
    private String sharedAppId1;
    private String appId2;
    private String sharedAppId2;
    private String brandingJSON;

    private OAuth2RestClient restClient;
    private OrgMgtRestClient orgMgtRestClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CustomLayoutTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        restClient = new OAuth2RestClient(serverURL, context.getContextTenant());
        orgMgtRestClient =
                new OrgMgtRestClient(context, context.getContextTenant(), serverURL, getAuthorizedAPIList());

        createInitialData();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();

        deleteInitialData();
        restClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
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
     * Create initial data required for the test cases.
     *
     * @throws Exception If an error occurs while creating the initial data.
     */
    private void createInitialData() throws Exception {

        // Create a new sub-organization.
        subOrgID = orgMgtRestClient.addOrganization(ORGANIZATION_NAME);
        subOrgToken = orgMgtRestClient.switchM2MToken(subOrgID);

        // Create a second application for testing.
        ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setName(APPLICATION_NAME_1);
        InboundProtocols inboundProtocols = new InboundProtocols();
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(CODE_GRANT_TYPE)));
        oidcConfig.setCallbackURLs(new ArrayList<>(Collections.singletonList(CALLBACK_URL)));
        inboundProtocols.setOidc(oidcConfig);
        applicationModel.setInboundProtocolConfiguration(inboundProtocols);
        appId1 = restClient.createApplication(applicationModel);
        restClient.shareApplication(appId1, new ApplicationSharePOSTRequest().shareWithAllChildren(true));
        Thread.sleep(3000);
        sharedAppId1 = restClient.getAppIdUsingAppNameInOrganization(APPLICATION_NAME_1, subOrgToken);

        // Create a second application for testing.
        applicationModel = new ApplicationModel();
        applicationModel.setName(APPLICATION_NAME_2);
        inboundProtocols = new InboundProtocols();
        oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(CODE_GRANT_TYPE)));
        oidcConfig.setCallbackURLs(new ArrayList<>(Collections.singletonList(CALLBACK_URL)));
        inboundProtocols.setOidc(oidcConfig);
        applicationModel.setInboundProtocolConfiguration(inboundProtocols);
        appId2 = restClient.createApplication(applicationModel);
        restClient.shareApplication(appId2, new ApplicationSharePOSTRequest().shareWithAllChildren(true));
        Thread.sleep(3000);
        sharedAppId2 = restClient.getAppIdUsingAppNameInOrganization(APPLICATION_NAME_2, subOrgToken);

        brandingJSON = readResource(BRANDING_TEST_JSON_FILE);
    }

    /**
     * Deletes the initial data created for the test cases.
     *
     * @throws Exception If an error occurs while deleting the initial data.
     */
    private void deleteInitialData() throws Exception {

        // Delete the applications created for testing.
        restClient.deleteApplication(appId1);
        restClient.deleteApplication(appId2);

        // Delete the sub-organization created for testing.
        orgMgtRestClient.deleteOrganization(subOrgID);
    }

    @Test(description = "Add custom layout for root the tenant.")
    public void testAddCustomLayoutForRootTenant() throws Exception {

        String body = brandingJSON.replace(RESOURCE_ID_PLACEHOLDER, ROOT_TENANT);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, ORG_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, tenant);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);
    }

    @Test(description = "Add custom layout for root organization application.",
            dependsOnMethods = "testAddCustomLayoutForRootTenant")
    public void testAddCustomLayoutForRootOrgApplication() throws Exception {

        String body = brandingJSON.replace(RESOURCE_ID_PLACEHOLDER, ROOT_APP);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, APP_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, appId1);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayout(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);
    }

    @Test(description = "Add custom layout for sub-organization.",
            dependsOnMethods = "testAddCustomLayoutForRootOrgApplication")
    public void testAddCustomLayoutForSubOrg() throws Exception {

        String body = brandingJSON.replace(RESOURCE_ID_PLACEHOLDER, SUB_ORG);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, ORG_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, subOrgID);
        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body, subOrgToken);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayout(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);
    }

    @Test(description = "Add custom layout for sub-organization application.",
            dependsOnMethods = "testAddCustomLayoutForSubOrg")
    public void testAddCustomLayoutForSubOrgApplication() throws Exception {

        String body = brandingJSON.replace(RESOURCE_ID_PLACEHOLDER, SUB_ORG_APP);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, APP_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, sharedAppId1);
        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        Response response = getResponseOfPostWithOAuth2(BRANDING_PREFERENCE_API_BASE_PATH, body, subOrgToken);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayout(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, SUB_ORG_APP);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);
    }

    @Test(description = "Update custom layout for root application.",
            dependsOnMethods = "testAddCustomLayoutForSubOrgApplication")
    public void testUpdateCustomLayoutForRootOrgApp() throws Exception {

        String body = readResource(UPDATED_BRANDING_TEST_JSON_FILE).replace(RESOURCE_ID_PLACEHOLDER, ROOT_APP);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, APP_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, appId1);
        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateUpdatedCustomLayout(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, SUB_ORG_APP);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);
    }

    @Test(description = "Delete custom layout for root organization application.",
            dependsOnMethods = "testUpdateCustomLayoutForRootOrgApp")
    public void testDeleteCustomLayoutForRootOrgApp() throws Exception {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", APP_TYPE);
        queryParams.put("name", appId1);
        queryParams.put("locale", DEFAULT_LOCALE);
        Response response = getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH, queryParams);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId2, false);
        validateCustomLayout(loginPageResponse, ROOT_TENANT);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, SUB_ORG_APP);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);

        queryParams.put("type", ORG_TYPE);
        queryParams.put("name", tenant);
        queryParams.put("locale", DEFAULT_LOCALE);
        response = getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH, queryParams);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(description = "Add custom layout with only HTML content.",
            dependsOnMethods = "testDeleteCustomLayoutForRootOrgApp")
    public void testAddCustomLayoutWithOnlyHtml() throws Exception {

        JSONObject jsonObject = new JSONObject(brandingJSON);
        jsonObject.getJSONObject("preference").getJSONObject("layout").getJSONObject("content").put("css", "");
        jsonObject.getJSONObject("preference").getJSONObject("layout").getJSONObject("content").put("js", "");
        String body = jsonObject.toString();
        body = body.replace(RESOURCE_ID_PLACEHOLDER, ROOT_APP);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, APP_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, appId1);
        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        String loginPageResponse = getLoginPageResponse(appId1, false);
        validateCustomLayoutWithOnlyHtml(loginPageResponse, ROOT_APP);

        loginPageResponse = getLoginPageResponse(appId1, true);
        validateCustomLayout(loginPageResponse, SUB_ORG_APP);

        loginPageResponse = getLoginPageResponse(appId2, true);
        validateCustomLayout(loginPageResponse, SUB_ORG);
    }

    @Test(description = "Test failure when adding custom layout with empty content.",
            dependsOnMethods = "testAddCustomLayoutWithOnlyHtml")
    public void testFailureWhenAddingCustomLayoutWithEmptyContent() throws Exception {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", APP_TYPE);
        queryParams.put("name", appId1);
        queryParams.put("locale", DEFAULT_LOCALE);
        Response response = getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH, queryParams);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        JSONObject jsonObject = new JSONObject(brandingJSON);
        jsonObject.getJSONObject("preference").getJSONObject("layout").getJSONObject("content").put("html", "");
        jsonObject.getJSONObject("preference").getJSONObject("layout").getJSONObject("content").put("css", "");
        jsonObject.getJSONObject("preference").getJSONObject("layout").getJSONObject("content").put("js", "");
        String body = jsonObject.toString();
        body = body.replace(RESOURCE_ID_PLACEHOLDER, ROOT_APP);
        body = body.replace(BRANDING_TYPE_PLACEHOLDER, APP_TYPE);
        body = body.replace(BRANDING_RESOURCE_NAME, appId1);

        response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH, body);

        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "BRANDINGM_00034",
                "Invalid custom layout content.");
    }

    /**
     * Get the list of APIs that need to be authorized for the B2B application.
     *
     * @return A JSON object containing the API and scopes list.
     * @throws JSONException If an error occurs while creating the JSON object.
     */
    private JSONObject getAuthorizedAPIList() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        // Organization management.
        jsonObject.put("/api/server/v1/organizations",
                new String[] {"internal_organization_create", "internal_organization_delete"});
        // Application management.
        jsonObject.put("/o/api/server/v1/applications",
                new String[] {"internal_org_application_mgt_view"});
        // Branding management.
        jsonObject.put("/api/server/v1/branding-preference", new String[] {"internal_branding_preference_update"});
        jsonObject.put("/o/api/server/v1/branding-preference",
                new String[] {"internal_org_branding_preference_update"});

        return jsonObject;
    }

    /**
     * Get the login page response for the given application ID.
     *
     * @param appId The application ID.
     * @param isSubOrg Indicates whether the application is in a sub-organization.
     * @return The login page response as a String.
     * @throws Exception If an error occurs while getting the response.
     */
    private String getLoginPageResponse(String appId, boolean isSubOrg) throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = restClient.getOIDCInboundDetails(appId);
        String clientId = openIDConnectConfiguration.getClientId();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(CLIENT_ID_KEY, clientId);
        queryParams.put(REDIRECT_URI_KEY, CALLBACK_URL);
        queryParams.put(SCOPE_KEY, OPENID_SCOPE);
        queryParams.put(RESPONSE_TYPE_KEY, CODE_RESPONSE_TYPE);
        if (isSubOrg) {
            queryParams.put(FIDP_PARAM, SUB_ORG_LOGIN_IDP);
        }
        return callGet(AUTH_URL, queryParams, isSubOrg);
    }

    /**
     * Makes a GET request to the specified endpoint with the provided query parameters.
     * If response is a redirect (HTTP 302), it follows the redirect and returns the final response.
     *
     * @param endpoint    The API endpoint to call.
     * @param queryParams The query parameters to include in the request.
     *                    This can be null or empty if no parameters are needed.
     * @param isSubOrg    Indicates whether the request is for a sub-organization.
     * @return The response body as a String.
     * @throws Exception If an error occurs while making the request or processing the response.
     */
    private String callGet(String endpoint, Map<String, String> queryParams, boolean isSubOrg) throws Exception {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider()).build();
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        CookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig).setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry).build()) {
            URI uri;
            if (queryParams == null || queryParams.isEmpty()) {
                uri = new URIBuilder(resolveTenantedPath(endpoint)).build();
            } else {
                URIBuilder uriBuilder = new URIBuilder(resolveTenantedPath(endpoint));
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue());
                }
                uri = uriBuilder.build();
            }
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                            .lines().collect(Collectors.joining("\n"));
                    return responseBody;
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                    if (isSubOrg) {
                        location = selectSubOrg(client, location);
                    }
                    if (location != null) {
                        return callGet(location, new HashMap<>(), isSubOrg);
                    }
                }
                throw new Exception("Failed to get response from " + endpoint + ". Status code: "
                        + response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * Selects the sub-organization from the given location URL.
     *
     * @param client   The HTTP client to use for the request.
     * @param location The location URL to select the sub-organization from.
     * @return The new location URL after selecting the sub-organization.
     * @throws Exception If an error occurs while making the request or processing the response.
     */
    private String selectSubOrg(CloseableHttpClient client, String location) throws Exception {

        if (StringUtils.isBlank(location)) {
            return null;
        }
        if (!location.contains(ORG_NAME_PAGE)) {
            return location;
        }
        List<NameValuePair> formParams = new ArrayList<>();
        String sessionDataKey = extractQueryParams(location).get(SESSION_DATA_KEY);
        formParams.add(new BasicNameValuePair(SESSION_DATA_KEY, sessionDataKey));
        formParams.add(new BasicNameValuePair("org", ORGANIZATION_NAME));
        formParams.add(new BasicNameValuePair("idp", "SSO"));
        formParams.add(new BasicNameValuePair("authenticator", "OrganizationAuthenticator"));
        HttpPost post = new HttpPost(resolveTenantedPath(COMMON_AUTH_URL));
        post.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try (CloseableHttpResponse response = client.execute(post)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                String locationHeader = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                if (locationHeader != null) {
                    return locationHeader;
                }
            }
            throw new Exception("Failed to select sub-organization. Status code: "
                    + response.getStatusLine().getStatusCode());
        }
    }

    /**
     * Resolves the tenanted path for the given path.
     *
     * @param path The path to resolve.
     * @return The resolved tenanted absolute path.
     */
    private String resolveTenantedPath(String path) {

        if (StringUtils.startsWith(path, "http")) {
            return path;
        }

        String baseUrl = serverURL;
        if (!StringUtils.endsWith(baseUrl, PATH_SEPARATOR)) {
            baseUrl = baseUrl + PATH_SEPARATOR;
        }
        return baseUrl + TENANT_PATH + tenant + path;
    }

    /**
     * Validate the custom layout in the HTML page.
     *
     * @param htmlPage The HTML page content.
     * @param resourceId The resource ID to validate.
     */
    private void validateCustomLayout(String htmlPage, String resourceId) {

        String htmlElement = HTML_ELEMENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String cssContent = CSS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String jsContent = JS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        assert htmlPage.contains(htmlElement) : "HTML element not found in the page.";
        assert htmlPage.contains(cssContent) : "CSS content not found in the page.";
        assert htmlPage.contains(jsContent) : "JS content not found in the page.";
    }

    /**
     * Validate the updated custom layout in the HTML page.
     *
     * @param htmlPage   The HTML page content.
     * @param resourceId The resource ID to validate.
     */
    private void validateUpdatedCustomLayout(String htmlPage, String resourceId) {

        String htmlElement = UPDATED_HTML_ELEMENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String cssContent = UPDATED_CSS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String jsContent = UPDATED_JS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        assert htmlPage.contains(htmlElement) : "Updated HTML element not found in the page.";
        assert htmlPage.contains(cssContent) : "Updated CSS content not found in the page.";
        assert htmlPage.contains(jsContent) : "Updated JS content not found in the page.";
    }

    /**
     * Validate the custom layout in the HTML page with only HTML content.
     *
     * @param htmlPage   The HTML page content.
     * @param resourceId The resource ID to validate.
     */
    private void validateCustomLayoutWithOnlyHtml(String htmlPage, String resourceId) {

        String htmlElement = HTML_ELEMENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String cssContent = CSS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        String jsContent = JS_CONTENT.replace(RESOURCE_ID_PLACEHOLDER, resourceId);
        assert htmlPage.contains(htmlElement) : "HTML element not found in the page.";
        assert !htmlPage.contains(cssContent) : "CSS content should not be present in the page.";
        assert !htmlPage.contains(jsContent) : "JS content should not be present in the page.";
    }

    /**
     * Get the organization base path.
     *
     * @param basePath Tenant base path.
     * @return Organization base path.
     */
    private String convertToOrgBasePath(String basePath) {

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            return TENANTED_URL_PATH_SPECIFIER + tenant + ORGANIZATION_PATH_SPECIFIER + basePath;
        } else {
            return basePath.replace(tenant, tenant + ORGANIZATION_PATH_SPECIFIER);
        }
    }

    /**
     * Extract query parameters from the URL.
     *
     * @param url URL with query parameters.
     * @return Map of query parameters.
     * @throws Exception If an error occurred while extracting query parameters.
     */
    private Map<String, String> extractQueryParams(String url) throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        if (params.isEmpty()) {
            return queryParams;
        }

        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }

        return queryParams;
    }
}
