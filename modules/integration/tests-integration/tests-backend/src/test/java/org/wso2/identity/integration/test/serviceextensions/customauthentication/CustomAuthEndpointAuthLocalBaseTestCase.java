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

package org.wso2.identity.integration.test.serviceextensions.customauthentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.test.integration.service.dao.Attribute;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.CustomAuthenticatorManagementClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Concrete base for end-to-end integration tests that exercise a user-defined <strong>local</strong>
 * custom authenticator. Each instance is parameterised by a {@link TestUserMode} and an
 * {@link EndpointAuthScenario} so a single test class can drive every supported endpoint
 * authentication mode via TestNG {@code @Factory}.
 *
 * Subclasses choose between success or failure stub installation via
 * {@link #installTokenEndpointStubs(ServiceExtensionMockServer)}.
 */
public abstract class CustomAuthEndpointAuthLocalBaseTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String TEST_USER_FIRST_NAME = "Emily";
    public static final String TEST_USER_LAST_NAME = "Ellon";
    public static final String TEST_USER_EMAIL = "emily@aol.com";
    private static final String TEST_WSO2 = "Test@wso2";

    public static final String GIVEN_NAME_ATTRIBUTE_NAME = "given_name";
    public static final String EMAIL_ATTRIBUTE_NAME = "email";
    public static final String FAMILY_NAME_ATTRIBUTE_NAME = "family_name";

    protected OAuth2RestClient oAuth2RestClient;
    protected SCIM2RestClient scim2RestClient;
    protected CustomAuthenticatorManagementClient customAuthenticatorManagementClient;

    protected String consumerKey;
    protected String consumerSecret;
    protected String applicationId;
    protected String authenticatorId;
    protected String requestedScopes;
    protected String authorizationCode;
    protected UserDTO internalUser;

    protected CloseableHttpClient httpClient;
    protected MockCustomAuthenticatorService mockCustomAuthenticatorService;
    protected ServiceExtensionMockServer serviceExtensionMockServer;

    protected final TestUserMode userMode;
    protected final EndpointAuthScenario scenario;
    protected final String authenticatorName;

    protected CustomAuthEndpointAuthLocalBaseTestCase(TestUserMode testUserMode, EndpointAuthScenario scenario) {

        this.userMode = testUserMode;
        this.scenario = scenario;
        this.authenticatorName = "custom-internal-auth-" + scenario.name().toLowerCase(Locale.ROOT).replace('_', '-')
                + "-" + variantSuffix();
    }

    /**
     * Distinguishes success / failure subclasses so the authenticator name doesn't collide when
     * both are wired into the same testng suite.
     */
    protected abstract String variantSuffix();

    /**
     * Subclasses install either a success or failure stub on the token endpoint via
     * {@link EndpointAuthScenario#installSuccessTokenEndpointStub(ServiceExtensionMockServer)} or
     * {@link EndpointAuthScenario#installFailureTokenEndpointStub(ServiceExtensionMockServer)}.
     */
    protected abstract void installTokenEndpointStubs(ServiceExtensionMockServer server) throws IOException;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        customAuthenticatorManagementClient = new CustomAuthenticatorManagementClient(serverURL, tenantInfo);

        httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();
        requestedScopes = String.join(" ", OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                OAuth2Constant.OAUTH2_SCOPE_PROFILE);

        internalUser = createInternalUser();

        if (scenario.requiresTokenEndpoint()) {
            serviceExtensionMockServer = new ServiceExtensionMockServer();
            serviceExtensionMockServer.startServer();
            installTokenEndpointStubs(serviceExtensionMockServer);
        }

        mockCustomAuthenticatorService = new MockCustomAuthenticatorService();
        startMockAuthenticatorService();

        authenticatorId = scenario.createLocalAuthenticator(customAuthenticatorManagementClient, authenticatorName,
                "Custom Auth " + scenario.name(),
                mockCustomAuthenticatorService.getCustomAuthenticatorURL() + API_AUTHENTICATE_ENDPOINT);
        assertNotNull(authenticatorId);
        applicationId = setupApplication();
        retrieveApplicationDetails();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (mockCustomAuthenticatorService != null) {
            mockCustomAuthenticatorService.stop();
        }
        if (serviceExtensionMockServer != null) {
            serviceExtensionMockServer.stopServer();
        }

        if (internalUser != null && internalUser.getUserID() != null && scim2RestClient != null) {
            scim2RestClient.deleteUser(internalUser.getUserID());
        }
        if (applicationId != null && oAuth2RestClient != null) {
            oAuth2RestClient.deleteApplication(applicationId);
        }
        if (authenticatorId != null && customAuthenticatorManagementClient != null) {
            customAuthenticatorManagementClient.deleteCustomAuthenticator(authenticatorId);
        }

        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        authenticatorId = null;
        mockCustomAuthenticatorService = null;
        serviceExtensionMockServer = null;

        if (httpClient != null) {
            httpClient.close();
        }
        if (oAuth2RestClient != null) {
            oAuth2RestClient.closeHttpClient();
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (customAuthenticatorManagementClient != null) {
            customAuthenticatorManagementClient.closeHttpClient();
        }
    }

    // -- Internal setup --------------------------------------------------------------------------

    private void startMockAuthenticatorService() {

        String idpAuthURL = getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain());
        mockCustomAuthenticatorService.start(idpAuthURL, internalUser,
                scenario.expectedInboundHeaderName(), scenario.expectedInboundHeaderValue());
    }

    private String setupApplication() throws JSONException, IOException {

        ApplicationModel application = new ApplicationModel();
        application.setName("App for " + authenticatorName);

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        RequestedClaimConfiguration requestedClaimLastName = new RequestedClaimConfiguration();
        requestedClaimLastName.setClaim(new Claim().uri(OIDCUtilTest.lastNameClaimUri));

        RequestedClaimConfiguration requestedClaimFirstName = new RequestedClaimConfiguration();
        requestedClaimFirstName.setClaim(new Claim().uri(OIDCUtilTest.firstNameClaimUri));

        RequestedClaimConfiguration requestedClaimEmail = new RequestedClaimConfiguration();
        requestedClaimEmail.setClaim(new Claim().uri(OIDCUtilTest.emailClaimUri));

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();
        claimConfiguration.addRequestedClaimsItem(requestedClaimLastName);
        claimConfiguration.addRequestedClaimsItem(requestedClaimFirstName);
        claimConfiguration.addRequestedClaimsItem(requestedClaimEmail);

        application.setClaimConfiguration(claimConfiguration);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);

        AuthenticationSequence authenticationSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator().idp("LOCAL").authenticator(authenticatorName)));

        application.authenticationSequence(authenticationSequence);
        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(true).skipLogoutConsent(true));

        return oAuth2RestClient.createApplication(application);
    }

    private void retrieveApplicationDetails() throws Exception {

        ApplicationResponseModel applicationResponse = oAuth2RestClient.getApplication(applicationId);
        assertNotNull(applicationResponse, "Failed to retrieve the created application with custom authenticator");

        OpenIDConnectConfiguration oidcInboundConfig = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        consumerKey = oidcInboundConfig.getClientId();
        assertNotNull(consumerKey);
        consumerSecret = oidcInboundConfig.getClientSecret();
        assertNotNull(consumerSecret);
    }

    private UserDTO createInternalUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_EMAIL);
        userInfo.setPassword(TEST_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_FIRST_NAME));
        userInfo.getName().setFamilyName(TEST_USER_LAST_NAME);
        userInfo.addEmail(new Email().value(TEST_USER_EMAIL));

        String userId = scim2RestClient.createUser(userInfo);

        UserDTO user = new UserDTO();
        user.setUserID(userId);

        List<Attribute> userAttributes = new ArrayList<>();
        userAttributes.add(createUserAttribute("http://wso2.org/claims/username", TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/emailaddress", TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/lastname", TEST_USER_LAST_NAME));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/givenname", TEST_USER_FIRST_NAME));
        user.setAttributes(userAttributes.toArray(new Attribute[0]));

        assertNotNull(user);
        assertTrue(StringUtil.isNotBlank(user.getUserID()));
        return user;
    }

    private Attribute createUserAttribute(String attributeName, String attributeValue) {

        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeName);
        attribute.setAttributeValue(attributeValue);
        return attribute;
    }

    // -- Flow helpers (exposed to subclasses) ----------------------------------------------------

    protected String drivePinAuthenticationFlow() throws Exception {

        String customAuthenticatorPageUrl = authorizeApplication();
        Document customAuthenticatorPage = fetchCustomAuthenticatorPage(customAuthenticatorPageUrl);
        String commonAuthUrl = submitCredentialsToCustomAuthenticator(customAuthenticatorPage);
        return handleCommonAuthRequest(commonAuthUrl);
    }

    protected HttpResponse drivePinAuthenticationFlowExpectingFailure() throws Exception {

        String customAuthenticatorPageUrl = authorizeApplication();
        HttpResponse pageResponse = sendGetRequest(httpClient, customAuthenticatorPageUrl);
        // When the token endpoint is stubbed to fail, IS may short-circuit before serving the
        // authenticator form. Return the early response so the caller can verify no authorization
        // code was issued without requiring the full pin flow to complete.
        Document customAuthenticatorPage = parsePageOrNull(pageResponse);
        if (customAuthenticatorPage == null || customAuthenticatorPage.selectFirst("input[name=flowId]") == null) {
            return pageResponse;
        }
        String commonAuthUrl = submitCredentialsToCustomAuthenticator(customAuthenticatorPage);
        return sendGetRequest(httpClient, commonAuthUrl);
    }

    private Document parsePageOrNull(HttpResponse response) throws IOException {

        if (response == null || response.getStatusLine().getStatusCode() != 200
                || response.getEntity() == null) {
            return null;
        }
        Header contentType = response.getFirstHeader("Content-Type");
        if (contentType == null || !contentType.getValue().contains("text/html")) {
            EntityUtils.consume(response.getEntity());
            return null;
        }
        Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        return doc;
    }

    private String authorizeApplication() throws Exception {

        List<NameValuePair> urlParameters = buildOAuth2Parameters(consumerKey);
        urlParameters.add(new BasicNameValuePair("testParam", "123_abc"));
        HttpResponse authorizeRequest = sendPostRequestWithParameters(httpClient, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        assertNotNull(authorizeRequest, "Authorization request failed. Authorized response is null.");
        assertEquals(authorizeRequest.getStatusLine().getStatusCode(), 302);

        Header locationHeader = authorizeRequest.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(authorizeRequest.getEntity());

        String customAuthenticatorPageUrl = locationHeader.getValue();
        assertTrue(StringUtil.isNotBlank(customAuthenticatorPageUrl),
                "Custom authenticator page URL is blank or null.");
        return customAuthenticatorPageUrl;
    }

    private Document fetchCustomAuthenticatorPage(String customAuthenticatorPageUrl) throws Exception {

        HttpResponse customAuthenticatorPageRequest = sendGetRequest(httpClient, customAuthenticatorPageUrl);
        assertEquals(customAuthenticatorPageRequest.getStatusLine().getStatusCode(), 200);
        assertTrue(customAuthenticatorPageRequest.containsHeader("Content-Type"));
        assertTrue(customAuthenticatorPageRequest.getHeaders("Content-Type")[0].getValue().contains("text/html"),
                "Response is not HTML");

        Document doc = Jsoup.parse(EntityUtils.toString(customAuthenticatorPageRequest.getEntity()));
        EntityUtils.consume(customAuthenticatorPageRequest.getEntity());
        assertNotNull(doc, "Custom authenticator page is null.");
        return doc;
    }

    private String submitCredentialsToCustomAuthenticator(Document customAuthenticatorPage) throws Exception {

        Element form = customAuthenticatorPage.selectFirst("form");
        String customAuthenticatorAuthUrl = mockCustomAuthenticatorService.getCustomAuthenticatorURL();
        if (form != null) {
            String actionUrl = form.attr("action");
            assertTrue(StringUtil.isNotBlank(actionUrl));
            customAuthenticatorAuthUrl = customAuthenticatorAuthUrl + actionUrl;
        }

        Element flowIdElement = customAuthenticatorPage.selectFirst("input[name=flowId]");
        String flowId = (flowIdElement != null) ? flowIdElement.attr("value") : null;
        assertTrue(StringUtil.isNotBlank(flowId));

        HttpPost customAuthenticatorLoginRequest = buildCustomAuthenticatorLoginRequest(customAuthenticatorAuthUrl,
                flowId);
        HttpResponse customAuthenticatorLoginResponse = httpClient.execute(customAuthenticatorLoginRequest);
        assertNotNull(customAuthenticatorLoginResponse);
        assertEquals(customAuthenticatorLoginResponse.getStatusLine().getStatusCode(), 200);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(customAuthenticatorLoginResponse.getEntity()));
        EntityUtils.consume(customAuthenticatorLoginResponse.getEntity());

        String commonAuthUrl = jsonNode.get("redirectingTo").asText();
        assertTrue(StringUtil.isNotBlank(commonAuthUrl));
        return commonAuthUrl;
    }

    private String handleCommonAuthRequest(String commonAuthUrl) throws Exception {

        HttpResponse commonAuthResponse = sendGetRequest(httpClient, commonAuthUrl);
        assertNotNull(commonAuthResponse);

        Header locationHeader = commonAuthResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login, is not available.");
        EntityUtils.consume(commonAuthResponse.getEntity());

        HttpResponse authorizedResponse = sendGetRequest(httpClient, locationHeader.getValue());
        locationHeader = authorizedResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(authorizedResponse.getEntity());

        return getAuthorizationCodeFromURL(locationHeader.getValue());
    }

    private HttpPost buildCustomAuthenticatorLoginRequest(String customAuthenticatorAuthUrl, String flowId)
            throws UnsupportedEncodingException {

        HttpPost customAuthenticatorLoginRequest = new HttpPost(customAuthenticatorAuthUrl);
        customAuthenticatorLoginRequest.setHeader("Content-Type", "application/json");
        customAuthenticatorLoginRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        String jsonBody = "{"
                + "\"flowId\": \"" + flowId + "\","
                + "\"username\": \"" + TEST_USER_EMAIL + "\","
                + "\"pin\": \"1234\""
                + "}";

        customAuthenticatorLoginRequest.setEntity(new StringEntity(jsonBody));
        return customAuthenticatorLoginRequest;
    }

    protected String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    protected List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("scope", requestedScopes));
        return urlParameters;
    }

    // -- Token exchange + claim assertions -------------------------------------------------------

    protected HttpResponse exchangeAuthorizationCodeForTokens() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("scope", requestedScopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(consumerKey, consumerSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(httpClient, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive a response for access token request.");
        return response;
    }

    protected void assertExpectedTokenClaims(HttpResponse response) throws Exception {

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has(OAuth2Constant.ACCESS_TOKEN), "Access token not found in the token response.");
        String accessToken = jsonResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        assertNotNull(accessToken, "Access token is null.");
        assertNotNull(extractJwtClaims(accessToken));

        assertTrue(jsonResponse.has(OAuth2Constant.ID_TOKEN), "ID token not found in the token response.");
        String idToken = jsonResponse.getString(OAuth2Constant.ID_TOKEN);
        assertNotNull(idToken, "Id token is null.");

        JWTClaimsSet jwtClaimsOfIdToken = extractJwtClaims(idToken);
        assertNotNull(jwtClaimsOfIdToken);

        assertNotNull(jwtClaimsOfIdToken.getClaim(GIVEN_NAME_ATTRIBUTE_NAME));
        assertEquals(jwtClaimsOfIdToken.getClaim(GIVEN_NAME_ATTRIBUTE_NAME).toString(), TEST_USER_FIRST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(FAMILY_NAME_ATTRIBUTE_NAME));
        assertEquals(jwtClaimsOfIdToken.getClaim(FAMILY_NAME_ATTRIBUTE_NAME).toString(), TEST_USER_LAST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(EMAIL_ATTRIBUTE_NAME));
        assertEquals(jwtClaimsOfIdToken.getClaim(EMAIL_ATTRIBUTE_NAME).toString(), TEST_USER_EMAIL);
    }

    protected JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }
}
