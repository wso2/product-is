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

package org.wso2.identity.integration.test.webhooks.suborg;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.apache.http.client.utils.URIBuilder;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
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
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OIDC_LOGOUT_ENDPOINT;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createApplicationObject;
import static org.wso2.identity.integration.test.webhooks.suborg.eventpayloadbuilder.SubOrgEventPayloadUtils.createRootOrganizationObject;

/**
 * Base class for the tests of the events triggered when sub organization users log in, such as the login, session
 * and token events. Creates a user and two applications in each sub organization, and two root organization
 * applications shared with all the sub organizations, one with enhanced organization authentication enabled and
 * one with it disabled. Provides the
 * following logins for the user of a sub organization.
 * <ul>
 *     <li>Shared application with enhanced login enabled, through the organization qualified path of the root
 *     tenant.</li>
 *     <li>Shared application with enhanced login enabled, starting from the root organization and discovering the
 *     sub organization.</li>
 *     <li>Shared application with enhanced login disabled, federating from the root organization to the sub
 *     organization through the organization SSO authenticator.</li>
 *     <li>Application created in the sub organization.</li>
 * </ul>
 */
public abstract class SubOrgLoginFlowTestBase extends SubOrgEventTestBase {

    private static final String INVALID_PASSWORD = "InvalidPassword@123";
    private static final String ORGANIZATION_SSO_IDP = "SSO";
    private static final String ORGANIZATION_SSO_FIDP = "OrganizationSSO";
    private static final String ORGANIZATION_AUTHENTICATOR = "OrganizationAuthenticator";
    private static final String ORG_ID_DISCOVERY_PARAM = "orgId";
    private static final String AUTHENTICATION_ENDPOINT_PATH = "/authenticationendpoint/";
    private static final String AUTH_FAILURE_PARAM = "authFailure=true";
    private static final int MAX_REDIRECT_HOPS = 10;
    private static final long APP_SHARING_TIMEOUT_MILLIS = 20000;

    protected RootApp enhancedLoginApp;
    protected RootApp legacyLoginApp;

    protected SubOrgLoginFlowTestBase(TestUserMode userMode, String[] subOrgNames, String testName) {

        super(userMode, subOrgNames, testName);
    }

    /**
     * Creates the sub organizations with a user and an application each, and the root applications shared with all
     * the sub organizations.
     */
    protected void setUpLoginTestData() throws Exception {

        setUpSubOrgs();
        for (int i = 0; i < subOrgs.size(); i++) {
            createSubOrgUserAndApp(subOrgs.get(i), i + 1);
        }
        enhancedLoginApp = createAndShareRootApp(testName + "-enhanced-login-app", true);
        legacyLoginApp = createAndShareRootApp(testName + "-legacy-login-app", false);
    }

    /**
     * Deletes the root applications along with the test data created by {@link #setUpSubOrgs()}.
     */
    protected void tearDownLoginTestData() throws Exception {

        if (enhancedLoginApp != null) {
            oAuth2RestClient.deleteApplication(enhancedLoginApp.id);
        }
        if (legacyLoginApp != null) {
            oAuth2RestClient.deleteApplication(legacyLoginApp.id);
        }
        tearDownSubOrgs();
    }

    /**
     * Logs in the sub organization user to the shared application with enhanced login enabled, through the
     * organization qualified path of the root tenant. A successful login is completed by obtaining an access token.
     */
    protected void loginToEnhancedLoginApp(SubOrg subOrg, boolean validCredentials) throws Exception {

        try (CloseableHttpClient client = createHttpClient()) {
            String sessionDataKey = initiateAuthorizationRequest(client, enhancedLoginApp.clientId,
                    getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, rootTenantDomain, subOrg.id));
            completeLogin(client, subOrg, validCredentials, sessionDataKey,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, rootTenantDomain, subOrg.id),
                    enhancedLoginApp.clientId, enhancedLoginApp.clientSecret,
                    getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain, subOrg.id));
        }
    }

    /**
     * Logs in the sub organization user to the shared application with enhanced login enabled, starting from the
     * root organization and discovering the sub organization with the organization ID. A successful login is
     * completed by obtaining an access token.
     */
    protected void loginToEnhancedLoginAppWithOrgDiscovery(SubOrg subOrg, boolean validCredentials)
            throws Exception {

        try (CloseableHttpClient client = createHttpClient()) {
            String sessionDataKey = initiateAuthorizationRequest(client, enhancedLoginApp.clientId,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, rootTenantDomain));

            List<NameValuePair> orgDiscoveryParams = new ArrayList<>();
            orgDiscoveryParams.add(new BasicNameValuePair(OAuth2Constant.SESSION_DATA_KEY, sessionDataKey));
            orgDiscoveryParams.add(new BasicNameValuePair(ORG_ID_DISCOVERY_PARAM, subOrg.id));

            HttpResponse response = sendPostRequestWithParameters(client, orgDiscoveryParams,
                    getTenantQualifiedURL(COMMON_AUTH_URL, rootTenantDomain));
            String location = getLocationHeaderValue(response);
            // Follows the redirects until the login page of the sub organization login.
            for (int hop = 0; hop < MAX_REDIRECT_HOPS && !location.contains(AUTHENTICATION_ENDPOINT_PATH); hop++) {
                response = sendGetRequest(client, location);
                location = getLocationHeaderValue(response);
            }
            assertTrue(location.contains(AUTHENTICATION_ENDPOINT_PATH),
                    "Organization discovery did not lead to a login page. Last redirect location: " + location);

            // The login continues on the organization qualified path if the login page is served there.
            String commonAuthUrl;
            String tokenUrl;
            if (location.contains("/o/" + subOrg.id + "/")) {
                commonAuthUrl = getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, rootTenantDomain, subOrg.id);
                tokenUrl = getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain, subOrg.id);
            } else {
                commonAuthUrl = getTenantQualifiedURL(COMMON_AUTH_URL, rootTenantDomain);
                tokenUrl = getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain);
            }
            completeLogin(client, subOrg, validCredentials, getSessionDataKey(client, location), commonAuthUrl,
                    enhancedLoginApp.clientId, enhancedLoginApp.clientSecret, tokenUrl);
        }
    }

    /**
     * Logs in the sub organization user to the shared application with enhanced login disabled, selecting the sub
     * organization with the organization SSO authenticator of the root organization. A successful login is
     * completed by obtaining an access token.
     */
    protected void loginToLegacyLoginApp(SubOrg subOrg, boolean validCredentials) throws Exception {

        try (CloseableHttpClient client = createHttpClient()) {
            List<NameValuePair> authzParams = buildAuthorizationParams(legacyLoginApp.clientId);
            authzParams.add(new BasicNameValuePair(OAuth2Constant.FIDP_PARAM, ORGANIZATION_SSO_FIDP));

            HttpResponse response = sendPostRequestWithParameters(client, authzParams,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, rootTenantDomain));
            String sessionDataKey = DataExtractUtil.getParamFromURIString(getLocationHeaderValue(response),
                    OAuth2Constant.SESSION_DATA_KEY);
            assertNotNull(sessionDataKey, "Session data key not found in the root organization authorize response.");

            List<NameValuePair> orgDiscoveryParams = new ArrayList<>();
            orgDiscoveryParams.add(new BasicNameValuePair(OAuth2Constant.SESSION_DATA_KEY, sessionDataKey));
            orgDiscoveryParams.add(new BasicNameValuePair("org", subOrg.name));
            orgDiscoveryParams.add(new BasicNameValuePair("idp", ORGANIZATION_SSO_IDP));
            orgDiscoveryParams.add(new BasicNameValuePair("authenticator", ORGANIZATION_AUTHENTICATOR));

            response = sendPostRequestWithParameters(client, orgDiscoveryParams,
                    getTenantQualifiedURL(COMMON_AUTH_URL, rootTenantDomain));
            // Redirects to the authorize endpoint of the sub organization, which redirects to its login page.
            response = sendGetRequest(client, getLocationHeaderValue(response));
            String subOrgSessionDataKey = getSessionDataKey(client, getLocationHeaderValue(response));

            // A successful login redirects through the sub organization authorize, root commonauth and root
            // authorize endpoints, and the token is obtained from the root organization.
            completeLogin(client, subOrg, validCredentials, subOrgSessionDataKey,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, rootTenantDomain, subOrg.id),
                    legacyLoginApp.clientId, legacyLoginApp.clientSecret,
                    getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain));
        }
    }

    /**
     * Logs in the sub organization user to the application created in the sub organization. A successful login is
     * completed by obtaining an access token.
     */
    protected void loginToSubOrgApp(SubOrg subOrg, boolean validCredentials) throws Exception {

        try (CloseableHttpClient client = createHttpClient()) {
            loginToSubOrgApp(client, subOrg, validCredentials);
        }
    }

    /**
     * Logs in the sub organization user to the application created in the sub organization, keeping the session
     * cookies in the given client. A successful login is completed by obtaining an access token.
     *
     * @return ID token of a successful login, or null if the login fails.
     */
    protected String loginToSubOrgApp(CloseableHttpClient client, SubOrg subOrg, boolean validCredentials)
            throws Exception {

        String sessionDataKey = initiateAuthorizationRequest(client, subOrg.clientId,
                getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, rootTenantDomain, subOrg.id));
        return completeLogin(client, subOrg, validCredentials, sessionDataKey,
                getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, rootTenantDomain, subOrg.id),
                subOrg.clientId, subOrg.clientSecret,
                getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain, subOrg.id));
    }

    /**
     * Logs in the sub organization user to the second application created in the sub organization, with single
     * sign-on from the session already held by the given client. The login is completed by obtaining an access
     * token.
     *
     * @return ID token of the login.
     */
    protected String loginToSubOrgSsoAppWithSSO(CloseableHttpClient client, SubOrg subOrg) throws Exception {

        HttpResponse response = sendPostRequestWithParameters(client, buildAuthorizationParams(subOrg.ssoClientId),
                getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, rootTenantDomain, subOrg.id));
        String location = getLocationHeaderValue(response);
        Assert.assertFalse(location.contains(AUTHENTICATION_ENDPOINT_PATH),
                "Single sign-on did not happen, the login page was prompted. Location: " + location);

        JSONObject tokenResponse = requestAccessToken(client, getAuthorizationCode(client, location),
                subOrg.ssoClientId, subOrg.ssoClientSecret,
                getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, rootTenantDomain, subOrg.id));
        return tokenResponse.optString("id_token", null);
    }

    /**
     * Logs out the session held by the given client through the OIDC logout endpoint of the sub organization, and
     * follows the redirects until the post logout redirect URI.
     */
    protected void logoutFromSubOrg(CloseableHttpClient client, SubOrg subOrg, String idTokenHint) throws Exception {

        assertNotNull(idTokenHint, "ID token not found to log out with.");
        String logoutUrl = new URIBuilder(getRootTenantQualifiedOrgURL(OIDC_LOGOUT_ENDPOINT, rootTenantDomain,
                subOrg.id))
                .addParameter("id_token_hint", idTokenHint)
                .addParameter("post_logout_redirect_uri", CALLBACK_URL)
                .build().toString();

        String callbackUrl = CALLBACK_URL.split("\\?")[0];
        String location = logoutUrl;
        for (int hop = 0; hop < MAX_REDIRECT_HOPS && !location.startsWith(callbackUrl); hop++) {
            location = getLocationHeaderValue(sendGetRequest(client, location));
        }
        assertTrue(location.startsWith(callbackUrl),
                "Logout did not redirect to the post logout redirect URI. Last redirect location: " + location);
    }

    /**
     * Builds the application object of a root application, as published in the events.
     */
    protected JSONObject toApplicationObject(RootApp rootApp) throws Exception {

        return createApplicationObject(rootApp.id, rootApp.name, createRootOrganizationObject(rootTenantDomain));
    }

    /**
     * Builds the application object of the application created in the sub organization, as published in the
     * events.
     */
    protected JSONObject toSubOrgApplicationObject(SubOrg subOrg) throws Exception {

        return createApplicationObject(subOrg.appId, subOrg.appName, subOrg.toOrganizationObject());
    }

    /**
     * Builds the application object of the second application created in the sub organization, which is logged in
     * to with single sign-on, as published in the events.
     */
    protected JSONObject toSubOrgSsoApplicationObject(SubOrg subOrg) throws Exception {

        return createApplicationObject(subOrg.ssoAppId, subOrg.ssoAppName, subOrg.toOrganizationObject());
    }

    private void createSubOrgUserAndApp(SubOrg subOrg, int index) throws Exception {

        subOrg.username = testName + "-user-" + index;
        subOrg.email = subOrg.username + "@test.com";
        subOrg.userId = scim2RestClient.createSubOrgUser(new UserObject()
                .userName(subOrg.username)
                .password(PASSWORD)
                .addEmail(new Email().value(subOrg.email)), subOrg.switchedToken);
        assertNotNull(subOrg.userId, "Failed to create user in sub organization: " + subOrg.name);

        subOrg.appName = testName + "-sub-org-app-" + index;
        subOrg.appId = oAuth2RestClient.createOrganizationApplication(buildOIDCApplication(subOrg.appName),
                subOrg.switchedToken);
        OpenIDConnectConfiguration oidcConfig =
                oAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(subOrg.appId, subOrg.switchedToken);
        subOrg.clientId = oidcConfig.getClientId();
        subOrg.clientSecret = oidcConfig.getClientSecret();

        subOrg.ssoAppName = testName + "-sub-org-sso-app-" + index;
        subOrg.ssoAppId = oAuth2RestClient.createOrganizationApplication(buildOIDCApplication(subOrg.ssoAppName),
                subOrg.switchedToken);
        OpenIDConnectConfiguration ssoOidcConfig =
                oAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(subOrg.ssoAppId, subOrg.switchedToken);
        subOrg.ssoClientId = ssoOidcConfig.getClientId();
        subOrg.ssoClientSecret = ssoOidcConfig.getClientSecret();
    }

    private RootApp createAndShareRootApp(String appName, boolean enhancedOrgAuthenticationEnabled)
            throws Exception {

        RootApp rootApp = new RootApp();
        rootApp.name = appName;
        rootApp.id = oAuth2RestClient.createApplication(buildOIDCApplication(appName)
                .enhancedOrgAuthenticationEnabled(enhancedOrgAuthenticationEnabled));
        OpenIDConnectConfiguration oidcConfig = oAuth2RestClient.getOIDCInboundDetails(rootApp.id);
        rootApp.clientId = oidcConfig.getClientId();
        rootApp.clientSecret = oidcConfig.getClientSecret();

        ApplicationSharePOSTRequest shareRequest = new ApplicationSharePOSTRequest();
        shareRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(rootApp.id, shareRequest);

        for (SubOrg subOrg : subOrgs) {
            waitForApplicationSharedToSubOrg(appName, subOrg);
        }
        return rootApp;
    }

    private ApplicationModel buildOIDCApplication(String appName) {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        return new ApplicationModel()
                .name(appName)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));
    }

    private void waitForApplicationSharedToSubOrg(String appName, SubOrg subOrg) throws Exception {

        long deadline = System.currentTimeMillis() + APP_SHARING_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            try {
                String sharedAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName,
                        subOrg.switchedToken);
                if (sharedAppId != null && !sharedAppId.isEmpty()) {
                    return;
                }
            } catch (IOException e) {
                log.debug("Transient error while polling for shared application '" + appName + "', retrying.", e);
            }
            Thread.sleep(500);
        }
        Assert.fail("Application '" + appName + "' was not shared to sub organization '" + subOrg.name +
                "' within " + APP_SHARING_TIMEOUT_MILLIS + " ms.");
    }

    private List<NameValuePair> buildAuthorizationParams(String clientId) {

        List<NameValuePair> authzParams = new ArrayList<>();
        authzParams.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        authzParams.add(new BasicNameValuePair("client_id", clientId));
        authzParams.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        authzParams.add(new BasicNameValuePair("scope", "openid"));
        return authzParams;
    }

    /**
     * Sends the authorization request and returns the session data key of the login page it leads to.
     */
    private String initiateAuthorizationRequest(CloseableHttpClient client, String clientId, String authorizeUrl)
            throws Exception {

        HttpResponse response = sendPostRequestWithParameters(client, buildAuthorizationParams(clientId),
                authorizeUrl);
        return getSessionDataKey(client, getLocationHeaderValue(response));
    }

    /**
     * Submits the credentials of the sub organization user at the login page. With valid credentials, follows the
     * redirects to the callback URL and obtains an access token with the authorization code. With invalid
     * credentials, asserts that the login fails.
     *
     * @return ID token of a successful login, or null if the login fails.
     */
    private String completeLogin(CloseableHttpClient client, SubOrg subOrg, boolean validCredentials,
                               String sessionDataKey, String commonAuthUrl, String clientId, String clientSecret,
                               String tokenUrl) throws Exception {

        List<NameValuePair> loginParams = new ArrayList<>();
        loginParams.add(new BasicNameValuePair("username", subOrg.username));
        loginParams.add(new BasicNameValuePair("password", validCredentials ? PASSWORD : INVALID_PASSWORD));
        loginParams.add(new BasicNameValuePair(OAuth2Constant.SESSION_DATA_KEY, sessionDataKey));
        HttpResponse response = sendPostRequestWithParameters(client, loginParams, commonAuthUrl);
        String location = getLocationHeaderValue(response);

        if (!validCredentials) {
            assertTrue(location.contains(AUTH_FAILURE_PARAM),
                    "Login with invalid credentials did not redirect with an authentication failure. Location: " +
                            location);
            return null;
        }
        return requestAccessToken(client, getAuthorizationCode(client, location), clientId, clientSecret, tokenUrl)
                .optString("id_token", null);
    }

    /**
     * Returns the session data key of the login page, taken from the redirect URL, or from the login page itself
     * when the redirect URL does not carry it.
     */
    private String getSessionDataKey(CloseableHttpClient client, String loginPageUrl) throws Exception {

        String sessionDataKey = DataExtractUtil.getParamFromURIString(loginPageUrl, OAuth2Constant.SESSION_DATA_KEY);
        if (sessionDataKey != null) {
            return sessionDataKey;
        }

        HttpResponse response = sendGetRequest(client, loginPageUrl);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        EntityUtils.consume(response.getEntity());
        assertTrue(keyValues != null && !keyValues.isEmpty(), "Session data key not found in the login page.");
        return keyValues.get(0).getValue();
    }

    /**
     * Follows the redirects from the given location until the callback URL, and returns the authorization code.
     */
    private String getAuthorizationCode(CloseableHttpClient client, String location) throws Exception {

        String callbackUrl = CALLBACK_URL.split("\\?")[0];
        for (int hop = 0; hop < MAX_REDIRECT_HOPS && !location.startsWith(callbackUrl); hop++) {
            HttpResponse response = sendGetRequest(client, location);
            location = getLocationHeaderValue(response);
        }

        String code = DataExtractUtil.getParamFromURIString(location, "code");
        assertNotNull(code, "Authorization code not found in the callback URL. Login has not been successful. " +
                "Last redirect location: " + location);
        return code;
    }

    private JSONObject requestAccessToken(CloseableHttpClient client, String code, String clientId,
                                          String clientSecret, String tokenUrl) throws Exception {

        List<NameValuePair> tokenParams = new ArrayList<>();
        tokenParams.add(new BasicNameValuePair("code", code));
        tokenParams.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        tokenParams.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", "Basic " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, tokenParams, tokenUrl);
        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject tokenResponse = new JSONObject(responseBody);
        assertTrue(tokenResponse.has("access_token"), "Access token not found in the token response: " + responseBody);
        return tokenResponse;
    }

    private String getLocationHeaderValue(HttpResponse response) throws Exception {

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());
        assertNotNull(locationHeader, "Location header not found in the response.");
        return locationHeader.getValue();
    }

    /**
     * Creates an HTTP client with its own cookie store, so that each login starts without an existing session.
     */
    protected CloseableHttpClient createHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(new BasicCookieStore())
                .disableRedirectHandling()
                .build();
    }

    /**
     * A root organization application shared with all the sub organizations.
     */
    protected static class RootApp {

        String id;
        String name;
        String clientId;
        String clientSecret;
    }
}
