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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Integration tests for the login page background image configured through the branding theme.
 * Verifies that the background image URL saved in the branding preference is rendered into the
 * login and error pages only for the supported layouts and only when it is a safe http(s) URL.
 */
public class LoginPageBackgroundImageTest extends BrandingPreferenceManagementTestBase {

    private static final String APPLICATION_NAME = "BackgroundImageTestApp";
    private static final String BRANDING_TEST_JSON_FILE = "add-login-page-background-image.json";
    private static final String BRANDING_TYPE_PLACEHOLDER = "{{type}}";
    private static final String BRANDING_RESOURCE_NAME = "{{name}}";
    private static final String CALLBACK_URL = "https://example.com/oidc-callback";
    private static final String AUTH_URL = "/oauth2/authorize";
    private static final String ERROR_PAGE_URL = "/authenticationendpoint/oauth2_error.do";
    private static final String CODE_GRANT_TYPE = "authorization_code";

    private static final String BACKGROUND_IMAGE_URL = "https://example.com/images/login-background.png";
    private static final String DARK_BACKGROUND_IMAGE_URL = "https://example.com/images/login-background-dark.png";
    private static final String BACKGROUND_IMAGE_CSS_VARIABLE = "--asg-login-page-background-image: ";
    private static final String NO_BACKGROUND_IMAGE = BACKGROUND_IMAGE_CSS_VARIABLE + "none;";
    private static final Pattern BACKGROUND_IMAGE_URL_PATTERN =
            Pattern.compile("--asg-login-page-background-image:\\s*url\\(");
    // Transparent background declaration of the footer language selector, rendered only with a background image.
    private static final Pattern FOOTER_BLEND_STYLES_PATTERN = Pattern.compile(
            "language-selector-dropdown:hover\\s*\\{[^}]*background:\\s*transparent\\s*!important");
    private static final String LOGIN_FORM_MARKER = "id=\"loginForm\"";
    private static final int PAGE_REQUEST_TIMEOUT_MS = 30000;

    private static final String CENTERED_LAYOUT = "centered";
    private static final String LIGHT_THEME = "LIGHT";
    private static final String DARK_THEME = "DARK";

    private OAuth2RestClient restClient;
    private String appId;
    private String clientId;
    private String brandingJSON;
    private boolean isBrandingPreferenceAdded;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public LoginPageBackgroundImageTest(TestUserMode userMode) throws Exception {

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

        ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setName(APPLICATION_NAME);
        InboundProtocols inboundProtocols = new InboundProtocols();
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(CODE_GRANT_TYPE)));
        oidcConfig.setCallbackURLs(new ArrayList<>(Collections.singletonList(CALLBACK_URL)));
        inboundProtocols.setOidc(oidcConfig);
        applicationModel.setInboundProtocolConfiguration(inboundProtocols);
        appId = restClient.createApplication(applicationModel);
        clientId = restClient.getOIDCInboundDetails(appId).getClientId();

        brandingJSON = readResource(BRANDING_TEST_JSON_FILE)
                .replace(BRANDING_TYPE_PLACEHOLDER, ORGANIZATION_TYPE)
                .replace(BRANDING_RESOURCE_NAME, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        try {
            if (isBrandingPreferenceAdded) {
                requestBasePath = basePath;
                getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH, getBrandingQueryParams())
                        .then()
                        .log().ifValidationFails()
                        .assertThat()
                        .statusCode(HttpStatus.SC_NO_CONTENT);
            }
        } finally {
            requestBasePath = StringUtils.EMPTY;
            try {
                if (restClient != null && appId != null) {
                    restClient.deleteApplication(appId);
                }
            } finally {
                if (restClient != null) {
                    restClient.closeHttpClient();
                }
                super.conclude();
            }
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        requestBasePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        requestBasePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "backgroundImageSupportedLayouts")
    public static Object[][] backgroundImageSupportedLayouts() {

        return new Object[][]{
                {"left-aligned"},
                {"right-aligned"}
        };
    }

    @DataProvider(name = "imageLayouts")
    public static Object[][] imageLayouts() {

        return new Object[][]{
                {"left-image", "layouts/left-image/styles.css"},
                {"right-image", "layouts/right-image/styles.css"}
        };
    }

    @DataProvider(name = "nonHttpBackgroundImageURLs")
    public static Object[][] nonHttpBackgroundImageURLs() {

        return new Object[][]{
                {"javascript:alert(1)"},
                {"data:image/svg+xml;base64,PHN2Zz48L3N2Zz4="},
                {"ftp://example.com/images/login-background.png"},
                {"/images/login-background.png"},
                {"   "}
        };
    }

    @Test(description = "Add a background image for the centered layout and verify it on the login page.")
    public void testAddBackgroundImageForCenteredLayout() throws Exception {

        // Remove any preference left behind by other test classes so that this class starts from a clean state.
        getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH, getBrandingQueryParams());

        Response response = getResponseOfPost(BRANDING_PREFERENCE_API_BASE_PATH,
                buildBrandingPreference(CENTERED_LAYOUT, LIGHT_THEME, BACKGROUND_IMAGE_URL));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        isBrandingPreferenceAdded = true;

        String loginPage = getLoginPage();
        assertBackgroundImage(loginPage, BACKGROUND_IMAGE_URL);
        Assert.assertTrue(FOOTER_BLEND_STYLES_PATTERN.matcher(loginPage).find(),
                "Footer language selector styles for the background image are not rendered.");
    }

    @Test(description = "Verify the background image is applied to the error page.",
            dependsOnMethods = "testAddBackgroundImageForCenteredLayout")
    public void testBackgroundImageOnErrorPage() throws Exception {

        assertBackgroundImage(getErrorPage(), BACKGROUND_IMAGE_URL);
    }

    @Test(description = "Verify the background image is applied to the other background image supported layouts.",
            dependsOnMethods = "testBackgroundImageOnErrorPage",
            dataProvider = "backgroundImageSupportedLayouts")
    public void testBackgroundImageForSupportedLayouts(String layout) throws Exception {

        updateBrandingPreference(buildBrandingPreference(layout, LIGHT_THEME, BACKGROUND_IMAGE_URL));

        assertBackgroundImage(getLoginPage(), BACKGROUND_IMAGE_URL);
        assertBackgroundImage(getErrorPage(), BACKGROUND_IMAGE_URL);
    }

    @Test(description = "Verify the background image is not applied to the layouts that show a side image.",
            dependsOnMethods = "testBackgroundImageForSupportedLayouts",
            dataProvider = "imageLayouts")
    public void testBackgroundImageIgnoredForImageLayouts(String layout, String layoutMarker) throws Exception {

        updateBrandingPreference(buildBrandingPreference(layout, LIGHT_THEME, BACKGROUND_IMAGE_URL));

        String loginPage = getLoginPage();
        Assert.assertTrue(loginPage.contains(layoutMarker), "Login page is not rendered with the " + layout
                + " layout.");
        assertNoBackgroundImage(loginPage);
        Assert.assertFalse(FOOTER_BLEND_STYLES_PATTERN.matcher(loginPage).find(),
                "Footer language selector styles must not be rendered without a background image.");
    }

    @Test(description = "Verify the background image of the active dark theme is applied.",
            dependsOnMethods = "testBackgroundImageIgnoredForImageLayouts")
    public void testBackgroundImageForDarkTheme() throws Exception {

        JSONObject preference = new JSONObject(
                buildBrandingPreference(CENTERED_LAYOUT, DARK_THEME, DARK_BACKGROUND_IMAGE_URL));
        // Set a different image on the inactive theme to make sure only the active theme is used.
        getLoginPageBackground(preference, LIGHT_THEME).put("backgroundImageURL", BACKGROUND_IMAGE_URL);
        updateBrandingPreference(preference.toString());

        String loginPage = getLoginPage();
        assertBackgroundImage(loginPage, DARK_BACKGROUND_IMAGE_URL);
        Assert.assertFalse(loginPage.contains(toCssUrl(BACKGROUND_IMAGE_URL)),
                "Background image of the inactive theme is rendered on the login page.");
    }

    @Test(description = "Verify background image URLs that are not absolute http(s) URLs are ignored.",
            dependsOnMethods = "testBackgroundImageForDarkTheme",
            dataProvider = "nonHttpBackgroundImageURLs")
    public void testNonHttpBackgroundImageURLIsIgnored(String backgroundImageURL) throws Exception {

        updateBrandingPreference(buildBrandingPreference(CENTERED_LAYOUT, LIGHT_THEME, backgroundImageURL));

        String loginPage = getLoginPage();
        assertNoBackgroundImage(loginPage);
        if (StringUtils.isNotBlank(backgroundImageURL)) {
            Assert.assertFalse(loginPage.contains(backgroundImageURL),
                    "Rejected background image URL is rendered on the login page: " + backgroundImageURL);
        }
    }

    @Test(description = "Verify a background image URL cannot break out of the CSS url() value.",
            dependsOnMethods = "testNonHttpBackgroundImageURLIsIgnored")
    public void testBackgroundImageURLIsEncodedForCss() throws Exception {

        String maliciousURL = BACKGROUND_IMAGE_URL + "\");}body{display:none}/*";
        updateBrandingPreference(buildBrandingPreference(CENTERED_LAYOUT, LIGHT_THEME, maliciousURL));

        String loginPage = getLoginPage();
        assertBackgroundImage(loginPage, maliciousURL);
        // The quote must be escaped so that the injected rule stays inside the url() value.
        Assert.assertFalse(loginPage.contains("\");}body{display:none}"),
                "Background image URL is rendered without CSS encoding.");
    }

    @Test(description = "Remove the background image and verify it is no longer applied.",
            dependsOnMethods = "testBackgroundImageURLIsEncodedForCss")
    public void testRemoveBackgroundImage() throws Exception {

        updateBrandingPreference(buildBrandingPreference(CENTERED_LAYOUT, LIGHT_THEME, StringUtils.EMPTY));

        String loginPage = getLoginPage();
        assertNoBackgroundImage(loginPage);
        Assert.assertFalse(FOOTER_BLEND_STYLES_PATTERN.matcher(loginPage).find(),
                "Footer language selector styles must not be rendered without a background image.");
    }

    @Test(description = "Delete the branding preference and verify the background image is not applied.",
            dependsOnMethods = "testRemoveBackgroundImage")
    public void testDeleteBrandingPreferenceRemovesBackgroundImage() throws Exception {

        updateBrandingPreference(buildBrandingPreference(CENTERED_LAYOUT, LIGHT_THEME, BACKGROUND_IMAGE_URL));
        assertBackgroundImage(getLoginPage(), BACKGROUND_IMAGE_URL);

        Response response = getResponseOfDeleteWithQueryParams(BRANDING_PREFERENCE_API_BASE_PATH,
                getBrandingQueryParams());
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        isBrandingPreferenceAdded = false;

        Assert.assertFalse(BACKGROUND_IMAGE_URL_PATTERN.matcher(getLoginPage()).find(),
                "Background image is rendered after deleting the branding preference.");
    }

    /**
     * Build the branding preference request body with the given layout, active theme and background image URL.
     *
     * @param layout             Active layout.
     * @param activeTheme        Active theme.
     * @param backgroundImageURL Background image URL of the active theme.
     * @return Branding preference request body.
     * @throws JSONException If the request body cannot be built.
     */
    private String buildBrandingPreference(String layout, String activeTheme, String backgroundImageURL)
            throws JSONException {

        JSONObject body = new JSONObject(brandingJSON);
        JSONObject preference = body.getJSONObject("preference");
        preference.getJSONObject("layout").put("activeLayout", layout);
        preference.getJSONObject("theme").put("activeTheme", activeTheme);
        getLoginPageBackground(body, activeTheme).put("backgroundImageURL", backgroundImageURL);

        return body.toString();
    }

    /**
     * Get the login page background object of the given theme from the branding preference request body.
     *
     * @param body  Branding preference request body.
     * @param theme Theme name.
     * @return Login page background object.
     * @throws JSONException If the theme does not have a login page background.
     */
    private JSONObject getLoginPageBackground(JSONObject body, String theme) throws JSONException {

        return body.getJSONObject("preference").getJSONObject("theme").getJSONObject(theme)
                .getJSONObject("loginPage").getJSONObject("background");
    }

    /**
     * Update the branding preference of the tenant.
     *
     * @param body Branding preference request body.
     */
    private void updateBrandingPreference(String body) {

        Response response = getResponseOfPut(BRANDING_PREFERENCE_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    private Map<String, Object> getBrandingQueryParams() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", ORGANIZATION_TYPE);
        queryParams.put("name", tenant);
        queryParams.put("locale", DEFAULT_LOCALE);
        return queryParams;
    }

    /**
     * Assert that the page applies the given background image.
     *
     * @param page               HTML page content.
     * @param backgroundImageURL Expected background image URL.
     */
    private void assertBackgroundImage(String page, String backgroundImageURL) {

        Assert.assertTrue(page.contains(BACKGROUND_IMAGE_CSS_VARIABLE + toCssUrl(backgroundImageURL) + ";"),
                "Background image " + backgroundImageURL + " is not rendered on the page.");
    }

    /**
     * Build the CSS url() value the login pages render for the given image URL.
     * The pages encode the URL for CSS, so characters such as "/" are rendered as escape sequences.
     *
     * @param backgroundImageURL Background image URL.
     * @return CSS url() value.
     */
    private String toCssUrl(String backgroundImageURL) {

        return "url(\"" + Encode.forCssUrl(backgroundImageURL) + "\")";
    }

    /**
     * Assert that the page is branded but does not apply a background image.
     *
     * @param page HTML page content.
     */
    private void assertNoBackgroundImage(String page) {

        Assert.assertTrue(page.contains(NO_BACKGROUND_IMAGE),
                "Branded page does not render the background image CSS variable as none.");
        Assert.assertFalse(BACKGROUND_IMAGE_URL_PATTERN.matcher(page).find(),
                "Branded page renders a background image.");
    }

    /**
     * Get the login page of the test application.
     *
     * @return Login page content.
     * @throws Exception If an error occurs while getting the login page.
     */
    private String getLoginPage() throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("client_id", clientId);
        queryParams.put("redirect_uri", CALLBACK_URL);
        queryParams.put("scope", "openid");
        queryParams.put("response_type", "code");
        String loginPage = getPage(AUTH_URL, queryParams);
        Assert.assertTrue(loginPage.contains(LOGIN_FORM_MARKER), "Authorization request did not reach the login page.");
        return loginPage;
    }

    /**
     * Get the OAuth2 error page.
     *
     * @return Error page content.
     * @throws Exception If an error occurs while getting the error page.
     */
    private String getErrorPage() throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("oauthErrorCode", "invalid_request");
        queryParams.put("oauthErrorMsg", "Background image test");
        return getPage(ERROR_PAGE_URL, queryParams);
    }

    /**
     * Get the content of the given tenanted page, following redirects.
     *
     * @param path        Page path relative to the tenant.
     * @param queryParams Query parameters of the request.
     * @return Page content.
     * @throws Exception If an error occurs while getting the page.
     */
    private String getPage(String path, Map<String, String> queryParams) throws Exception {

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectTimeout(PAGE_REQUEST_TIMEOUT_MS)
                .setConnectionRequestTimeout(PAGE_REQUEST_TIMEOUT_MS)
                .setSocketTimeout(PAGE_REQUEST_TIMEOUT_MS)
                .build();
        try (CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig).setDefaultCookieStore(new BasicCookieStore()).build()) {
            URIBuilder uriBuilder = new URIBuilder(resolveTenantedPath(path));
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            URI uri = uriBuilder.build();

            // Follow the redirects within the same client to keep the session cookies.
            for (int redirects = 0; redirects < 10; redirects++) {
                try (CloseableHttpResponse response = client.execute(new HttpGet(uri))) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    }
                    if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY || response.getFirstHeader(
                            HttpHeaders.LOCATION) == null) {
                        throw new Exception("Failed to get " + uri + ". Status code: " + statusCode);
                    }
                    uri = uri.resolve(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
                }
            }
            throw new Exception("Too many redirects while getting " + path);
        }
    }

    /**
     * Resolve the tenanted absolute URL of the given path.
     *
     * @param path Path to resolve.
     * @return Tenanted absolute URL.
     */
    private String resolveTenantedPath(String path) {

        String baseUrl = serverURL;
        if (!StringUtils.endsWith(baseUrl, PATH_SEPARATOR)) {
            baseUrl = baseUrl + PATH_SEPARATOR;
        }
        return baseUrl + TENANT_PATH + tenant + path;
    }
}
