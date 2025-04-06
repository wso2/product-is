/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.organizationDiscovery;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.UserClaimConfig;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.OrgDiscoveryConfigRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_CLIENT_ID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.REDIRECT_URI_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SCOPE_PLAYGROUND_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.SESSION_DATA_KEY;

/**
 * This class contains the tests for the organization discovery feature with email domain based organization discovery.
 */
public class OrganizationDiscoveryTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String EMAIL_AS_USERNAME_TOML = "email_as_username.toml";
    private static final String LOCAL_CLAIM_DIALECT = "local";
    private static final String USERNAME_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ";
    private static final String EMAIL_WITH_VALID_DOMAIN = "john@wso2.com";
    private static final String EMAIL_WITH_INVALID_DOMAIN = "john@gmail.com";
    private static final String REGISTER_ENDPOINT_PATH = "/accountrecoveryendpoint/register.do";
    private static final String EMAIL_AS_USERNAME_CLAIM_JSON = "email_as_username_request.json";
    private static final String REVERT_EMAIL_AS_USERNAME_CLAIM_JSON = "revert_email_as_username_request.json";
    private static final String ENABLE_EMAIL_DOMAIN_ORG_DISCOVERY_JSON = "enable_email_domain_org_discovery.json";
    private static final String ORG_ONBOARDING_APIS_JSON = "organization-onboarding-apis.json";
    private static final String EMAIL_DOMAIN_SUB_ORG_JSON = "map_email_domain_to_sub_org.json";
    public static final String AUTHENTICATOR = "authenticator";
    public static final String IDP = "idp";
    public static final String LOGIN_HINT = "login_hint";
    public static final String ORGANIZATION_AUTHENTICATOR = "OrganizationAuthenticator";
    public static final String SSO = "SSO";
    private String subOrgId;
    private ApplicationResponseModel application;
    private ServerConfigurationManager serverConfigurationManager;
    private CloseableHttpClient client;
    private ClaimManagementRestClient claimManagementRestClient;
    private OrgDiscoveryConfigRestClient organizationDiscoveryConfigRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;

    @BeforeClass(dependsOnGroups = "wso2.is", alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        // Creating the http client which is used during the tests.
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();

        List<UserClaimConfig> userClaimConfigs = Collections.singletonList(
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/emailaddress").oidcClaimUri("email")
                        .build());

        // Create an application.
        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .claimsList(userClaimConfigs)
                .grantTypes(new ArrayList<>(Collections.singleton(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)))
                .tokenType(ApplicationConfig.TokenType.OPAQUE)
                .expiryTime(3600)
                .build();
        application = addApplication(applicationConfig);

        // Apply toml configuration to set email as username and restart the server.
        applyEmailAsUsernameConfig();
        // Init again after restart.
        super.init();

        // Create a sub-organization
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL, new JSONObject(
                RESTTestBase.readResource(ORG_ONBOARDING_APIS_JSON, this.getClass())));
        subOrgId = orgMgtRestClient.addOrganization("subOrg");

        // Update mapped attribute of username claim to email.
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        String emailAsUsernameRequestBody = RESTTestBase.readResource(EMAIL_AS_USERNAME_CLAIM_JSON, this.getClass());
        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, USERNAME_CLAIM_ID, emailAsUsernameRequestBody);

        // Enable email domain based org discovery for self-registration
        organizationDiscoveryConfigRestClient = new OrgDiscoveryConfigRestClient(serverURL, tenantInfo);
        String orgDiscoveryConfigRequestBody = RESTTestBase.readResource(
                ENABLE_EMAIL_DOMAIN_ORG_DISCOVERY_JSON, this.getClass());
        organizationDiscoveryConfigRestClient.addOrganizationDiscoveryConfig(orgDiscoveryConfigRequestBody);

        // Map an email domain to the created sub-organization
        String mapEmailDomainRequestBody = RESTTestBase.readResource(EMAIL_DOMAIN_SUB_ORG_JSON, this.getClass());
        organizationDiscoveryConfigRestClient.mapDiscoveryAttributes(subOrgId, mapEmailDomainRequestBody);

        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
    }

    @Test(dependsOnGroups = "wso2.is", description = "Test email domain based organization discovery not initiated " +
            "for unshared app", priority = 1)
    public void testSelfRegistrationOrgDiscoveryForUnsharedApp() throws Exception {

        // Initiate the authorize request to get the login page and retrieve the session data key.
        Map<String, String> loginPageQueryParams = initiateAuthorizeRequest();

        // Send a GET request to the register endpoint with the query parameters.
        HttpResponse response = sendGetRequest(client, isServer.getContextUrls().getWebAppURLHttps() +
                REGISTER_ENDPOINT_PATH + "?" + buildQueryString(loginPageQueryParams));

        // Parse the page content to check if the common auth request will be sent based on the configurations.
        String pageContent = EntityUtils.toString(response.getEntity());
        Assert.assertFalse(willRedirectToDomainDiscovery(pageContent),
                "Should not be redirected to domain discovery page for an unshared app.");
    }

    @Test(dependsOnGroups = "wso2.is", dependsOnMethods = "testSelfRegistrationOrgDiscoveryForUnsharedApp",
            description = "Test email domain based organization discovery for self-registration",
            priority = 2)
    public void testSelfRegistrationOrgDiscovery() throws Exception {

        // Share the app
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(application.getId(), applicationSharePOSTRequest);

        // Initiate the authorize request to get the login page and retrieve the session data key.
        Map<String, String> loginPageQueryParams = initiateAuthorizeRequest();

        // Send a GET request to the register endpoint with the query parameters.
        HttpResponse response = sendGetRequest(client, isServer.getContextUrls().getWebAppURLHttps() +
                REGISTER_ENDPOINT_PATH + "?" + buildQueryString(loginPageQueryParams));

        // Parse the page content to check if the common auth request will be sent based on the configurations.
        String pageContent = EntityUtils.toString(response.getEntity());
        Assert.assertTrue(willRedirectToDomainDiscovery(pageContent),
                "Register page will not send the common auth request.");

        // Send the common auth request which will be sent from the register page.
        response = initiateCommonAuthGet(loginPageQueryParams);
        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        // Check the response from the common auth request.
        Assert.assertNotNull(locationHeader, "Location header is not present in the common auth response.");
        Assert.assertTrue(locationHeader.getValue().contains("org_discovery.do"),
                "Organization authenticator did not redirect to domain discovery page.");
        EntityUtils.consume(response.getEntity());

        // Send the form post from domain discovery page.
        response = initiateCommonAuthPost(loginPageQueryParams, EMAIL_WITH_VALID_DOMAIN);
        // Check if the response is a redirect to the sub-organization with the matching email domain.
        locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Location header is not present in the common auth response.");
        Assert.assertTrue(locationHeader.getValue().contains(subOrgId),
                "Failed to redirect to the sub-organization with the matching email domain.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnGroups = "wso2.is",
            description = "Test email domain based organization discovery for self-registration with an " +
                    "invalid email domain", priority = 3)
    public void testSelfRegistrationOrgDiscoveryWithInvalidEmail() throws Exception {

        // Initiate the authorize request to get the login page and retrieve the session data key.
        Map<String, String> loginPageQueryParams = initiateAuthorizeRequest();

        // Send a GET request to the register endpoint with the query parameters.
        HttpResponse response = sendGetRequest(client, isServer.getContextUrls().getWebAppURLHttps() +
                REGISTER_ENDPOINT_PATH + "?" + buildQueryString(loginPageQueryParams));

        // Parse the page content to check if the common auth request will be sent based on the configurations.
        String pageContent = EntityUtils.toString(response.getEntity());
        Assert.assertTrue(willRedirectToDomainDiscovery(pageContent),
                "Register page will not send the common auth request.");

        // Send the common auth request which will be sent from the register page.
        response = initiateCommonAuthGet(loginPageQueryParams);
        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        // Check the response from the common auth request.
        Assert.assertNotNull(locationHeader, "Location header is not present in the common auth response.");
        Assert.assertTrue(locationHeader.getValue().contains("org_discovery.do"),
                "Organization authenticator did not redirect to domain discovery page.");
        EntityUtils.consume(response.getEntity());

        // Send the form post from domain discovery page.
        response = initiateCommonAuthPost(loginPageQueryParams, EMAIL_WITH_INVALID_DOMAIN);
        locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        // Assert that the response is a redirect to the register page with an auth failure.
        Assert.assertTrue(locationHeader.getValue().contains("authFailure=true"));
        EntityUtils.consume(response.getEntity());
    }

    @AfterClass(dependsOnGroups = "wso2.is", alwaysRun = true)
    public void atEnd() throws Exception {

        organizationDiscoveryConfigRestClient.deleteOrganizationDiscoveryConfig();
        deleteApp(application.getId());
        orgMgtRestClient.deleteOrganization(subOrgId);
        String revertEmailAsUsernameClaimRequestBody =
                RESTTestBase.readResource(REVERT_EMAIL_AS_USERNAME_CLAIM_JSON, this.getClass());
        claimManagementRestClient.updateClaim(
                LOCAL_CLAIM_DIALECT, USERNAME_CLAIM_ID, revertEmailAsUsernameClaimRequestBody);

        serverConfigurationManager.restoreToLastConfiguration(false);
        organizationDiscoveryConfigRestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
        client.close();
        idpMgtServiceClient.deleteIdP("SSO");
        idpMgtServiceClient = null;
    }

    private void applyEmailAsUsernameConfig() throws Exception {

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File emailAsUsernameConfigFile = new File(getISResourceLocation() + File.separator +
                "organizationDiscovery" + File.separator + EMAIL_AS_USERNAME_TOML);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailAsUsernameConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private Map<String, String> initiateAuthorizeRequest() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_CLIENT_ID, application.getClientId()));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(SCOPE_PLAYGROUND_NAME, "internal_login"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Location header is not present in the authorization response.");
        EntityUtils.consume(response.getEntity());

        return extractQueryParams(locationHeader.getValue());
    }

    private HttpResponse initiateCommonAuthGet(Map<String, String> loginPageQueryParams) throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(IDP, SSO);
        queryParams.put(SESSION_DATA_KEY, loginPageQueryParams.get(SESSION_DATA_KEY));
        queryParams.put(AUTHENTICATOR, ORGANIZATION_AUTHENTICATOR);
        queryParams.put("isSelfRegistration", "true");

        return sendGetRequest(client, getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()) +
                "?" + buildQueryString(queryParams));
    }

    private HttpResponse initiateCommonAuthPost(Map<String, String> loginPageQueryParams, String loginHint)
            throws Exception {

        List<NameValuePair> urlParams = new ArrayList<>();
        urlParams.add(new BasicNameValuePair(LOGIN_HINT, loginHint));
        urlParams.add(new BasicNameValuePair(SESSION_DATA_KEY, loginPageQueryParams.get(SESSION_DATA_KEY)));
        urlParams.add(new BasicNameValuePair(AUTHENTICATOR, ORGANIZATION_AUTHENTICATOR));
        urlParams.add(new BasicNameValuePair(IDP, SSO));

        return sendPostRequestWithParameters(client, urlParams,
                getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

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

    private String buildQueryString(Map<String, String> queryParams) throws Exception {

        if (queryParams.isEmpty()) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            queryString.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return queryString.toString();
    }

    /**
     * This method parses the user registration page content and checks the values of three specific variables in the
     * page. If all three variables are true, it will return true indicating that the page will redirect to the domain
     * discovery page.
     *
     * @param pageContent The content of the registration page.
     * @return true if the page content will redirect to the domain discovery page.
     */
    private boolean willRedirectToDomainDiscovery(String pageContent) {

        Document page = Jsoup.parse(pageContent);
        Elements scriptElements = page.select("script");

        Map<String, Pattern> patterns = Map.of(
                "isSSOLoginAuthenticatorConfigured",
                Pattern.compile("var isSSOLoginAuthenticatorConfigured = JSON.parse\\((.*?)\\);"),
                "emailDomainDiscoveryEnabled",
                Pattern.compile("var emailDomainDiscoveryEnabled = JSON.parse\\((.*?)\\);"),
                "emailDomainBasedSelfSignupEnabled",
                Pattern.compile("var emailDomainBasedSelfSignupEnabled = JSON.parse\\((.*?)\\);")
                                              );

        boolean isSSOLoginAuthenticatorConfigured = false;
        boolean emailDomainDiscoveryEnabled = false;
        boolean emailDomainBasedSelfSignupEnabled = false;

        for (Element script : scriptElements) {
            // Skip if the script is not inline.
            if (!script.hasAttr("src")) {
                String jsContent = script.html();

                for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
                    String variableName = entry.getKey();
                    Pattern pattern = entry.getValue();
                    Matcher matcher = pattern.matcher(jsContent);

                    if (matcher.find() && "true".equals(matcher.group(1))) {
                        switch (variableName) {
                            case "isSSOLoginAuthenticatorConfigured":
                                isSSOLoginAuthenticatorConfigured = true;
                                break;
                            case "emailDomainDiscoveryEnabled":
                                emailDomainDiscoveryEnabled = true;
                                break;
                            case "emailDomainBasedSelfSignupEnabled":
                                emailDomainBasedSelfSignupEnabled = true;
                                break;
                        }
                    }
                }
            }
        }

        return isSSOLoginAuthenticatorConfigured && emailDomainDiscoveryEnabled && emailDomainBasedSelfSignupEnabled;
    }
}
