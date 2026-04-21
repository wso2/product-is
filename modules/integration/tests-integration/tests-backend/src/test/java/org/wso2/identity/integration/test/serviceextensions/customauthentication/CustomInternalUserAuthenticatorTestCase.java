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

package org.wso2.identity.integration.test.serviceextensions.customauthentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
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
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

public class CustomInternalUserAuthenticatorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    // Test user details.
    public static final String TEST_USER_FIRST_NAME = "Emily";
    public static final String TEST_USER_LAST_NAME = "Ellon";
    public static final String TEST_USER_EMAIL = "emily@aol.com";
    private static final String TEST_WSO2 = "Test@wso2";

    // OIDC claim names.
    public static final String GIVEN_NAME_ATTRIBUTE_NAME = "given_name";
    public static final String EMAIL_ATTRIBUTE_NAME = "email";
    public static final String FAMILY_NAME_ATTRIBUTE_NAME = "family_name";

    // Authenticator properties
    private final String AUTHENTICATOR_NAME = "custom-internal-auth";

    // API clients
    private OAuth2RestClient oAuth2RestClient;
    private SCIM2RestClient scim2RestClient;
    private CustomAuthenticatorManagementClient customAuthenticatorManagementClient;

    // Test variables
    private String consumerKey;
    private String consumerSecret;
    private String applicationId;
    private String authenticatorId;
    private String requestedScopes;
    private String authorizationCode;
    private UserDTO internalUser;

    private CloseableHttpClient httpClient;
    private MockCustomAuthenticatorService mockCustomAuthenticatorService;

    private final TestUserMode userMode;

    @Factory(dataProvider = "testExecutionContextProvider")
    public CustomInternalUserAuthenticatorTestCase(TestUserMode testUserMode) {

        this.userMode = testUserMode;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);

        initializeClients();
        httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();
        requestedScopes = String.join(" ", OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                OAuth2Constant.OAUTH2_SCOPE_PROFILE);

        internalUser = createAndValidateInternalUser();

        mockCustomAuthenticatorService = new MockCustomAuthenticatorService();
        mockCustomAuthenticatorService.start(getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()),
                internalUser);

        authenticatorId = setupCustomAuthenticator();
        applicationId = setupApplication();
        retrieveApplicationDetails();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        mockCustomAuthenticatorService.stop();

        if (internalUser != null) {
            scim2RestClient.deleteUser(internalUser.getUserID());
        }

        oAuth2RestClient.deleteApplication(applicationId);
        customAuthenticatorManagementClient.deleteCustomAuthenticator(authenticatorId);

        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        authenticatorId = null;
        mockCustomAuthenticatorService = null;

        httpClient.close();
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        customAuthenticatorManagementClient.closeHttpClient();
    }

    @Test
    public void testInitAuthorizeRequestWithCustomInternalUserAuthentication() throws Exception {

        Thread.sleep(5000);
        String customAuthenticatorPageUrl = authorizeApplication();
        Document customAuthenticatorPage = fetchCustomAuthenticatorPage(customAuthenticatorPageUrl);
        String commonAuthUrl = submitCredentialsToCustomAuthenticator(customAuthenticatorPage);
        authorizationCode = handleCommonAuthRequest(commonAuthUrl);
        assertNotNull(authorizationCode);
        
        // Assert that the mock service received the expected payloads
        assertMockServiceReceivedValidateAuthenticateRequest();
    }

    @Test(dependsOnMethods = "testInitAuthorizeRequestWithCustomInternalUserAuthentication")
    public void testGetAccessTokenWithAuthCodeGrant() throws Exception {

        List<NameValuePair> urlParameters = buildAccessTokenRequestParameters();
        List<Header> headers = buildAccessTokenRequestHeaders();

        HttpResponse response = sendPostRequest(httpClient, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive a response for access token request.");

        parseAndValidateAccessTokenResponse(response);
    }

    private void initializeClients() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        customAuthenticatorManagementClient = new CustomAuthenticatorManagementClient(serverURL, tenantInfo);
    }

    private UserDTO createAndValidateInternalUser() throws Exception {

        UserDTO user = createUser();
        assertNotNull(user);
        assertTrue(StringUtil.isNotBlank(user.getUserID()));
        return user;
    }

    private String setupCustomAuthenticator() throws Exception {

        String id = customAuthenticatorManagementClient.createCustomInternalUserAuthenticator(
                AUTHENTICATOR_NAME, "Custom Internal Auth", mockCustomAuthenticatorService
                        .getCustomAuthenticatorURL() + API_AUTHENTICATE_ENDPOINT, "endpointUsername",
                "endpointPassword");
        assertNotNull(id);
        return id;
    }

    private String setupApplication() throws Exception {

        String id = createApplicationWithAuthenticator();
        assertNotNull(id, "Failed to create an application with custom authenticator");
        return id;
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

    private String createApplicationWithAuthenticator() throws JSONException, IOException {

        ApplicationModel application = new ApplicationModel();
        application.setName("App for custom authenticator");

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
                        .addOptionsItem(new Authenticator().idp("LOCAL").authenticator(AUTHENTICATOR_NAME)));

        application.authenticationSequence(authenticationSequence);
        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(true).skipLogoutConsent(true));

        return oAuth2RestClient.createApplication(application);
    }

    private UserDTO createUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_EMAIL);
        userInfo.setPassword(TEST_WSO2);
        userInfo.setName(new Name().givenName(CustomInternalUserAuthenticatorTestCase.TEST_USER_FIRST_NAME));
        userInfo.getName().setFamilyName(CustomInternalUserAuthenticatorTestCase.TEST_USER_LAST_NAME);
        userInfo.addEmail(new Email().value(CustomInternalUserAuthenticatorTestCase.TEST_USER_EMAIL));

        String userId = scim2RestClient.createUser(userInfo);

        UserDTO internalUser = new UserDTO();
        internalUser.setUserID(userId);

        List<Attribute> userAttributes = new ArrayList<>();
        userAttributes.add(createUserAttribute("http://wso2.org/claims/username",
                CustomInternalUserAuthenticatorTestCase.TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/emailaddress",
                CustomInternalUserAuthenticatorTestCase.TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/lastname",
                CustomInternalUserAuthenticatorTestCase.TEST_USER_LAST_NAME));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/givenname",
                CustomInternalUserAuthenticatorTestCase.TEST_USER_FIRST_NAME));
        internalUser.setAttributes(userAttributes.toArray(new Attribute[0]));

        return internalUser;
    }

    private Attribute createUserAttribute(String attributeName, String attributeValue) {

        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeName);
        attribute.setAttributeValue(attributeValue);

        return attribute;
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

        HttpPost customAuthenticatorLoginRequest =
                buildCustomAuthenticatorLoginRequest(customAuthenticatorAuthUrl, flowId);
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

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        urlParameters.add(new BasicNameValuePair("scope", requestedScopes));

        return urlParameters;
    }

    private List<NameValuePair> buildAccessTokenRequestParameters() {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("scope", requestedScopes));
        return urlParameters;
    }

    private List<Header> buildAccessTokenRequestHeaders() {

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(consumerKey, consumerSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));
        return headers;
    }

    private void parseAndValidateAccessTokenResponse(HttpResponse response) throws Exception {

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has(OAuth2Constant.ACCESS_TOKEN), "Access token not found in the token response.");
        String accessToken = jsonResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        assertNotNull(accessToken, "Access token is null.");

        JWTClaimsSet jwtClaimsOfAccessToken = extractJwtClaims(accessToken);
        assertNotNull(jwtClaimsOfAccessToken);

        assertTrue(jsonResponse.has(OAuth2Constant.ID_TOKEN), "ID token not found in the token response.");
        String idToken = jsonResponse.getString(OAuth2Constant.ID_TOKEN);
        assertNotNull(idToken, "Id token is null.");

        JWTClaimsSet jwtClaimsOfIdToken = extractJwtClaims(idToken);
        assertNotNull(jwtClaimsOfIdToken);

        assertNotNull(jwtClaimsOfIdToken.getClaim(GIVEN_NAME_ATTRIBUTE_NAME));
        String firstName = jwtClaimsOfIdToken.getClaim(GIVEN_NAME_ATTRIBUTE_NAME).toString();
        assertEquals(firstName, TEST_USER_FIRST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(FAMILY_NAME_ATTRIBUTE_NAME));
        String lastName = jwtClaimsOfIdToken.getClaim(FAMILY_NAME_ATTRIBUTE_NAME).toString();
        assertEquals(lastName, TEST_USER_LAST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(EMAIL_ATTRIBUTE_NAME));
        String email = jwtClaimsOfIdToken.getClaim(EMAIL_ATTRIBUTE_NAME).toString();
        assertEquals(email, TEST_USER_EMAIL);
    }

    private JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    private void assertMockServiceReceivedValidateAuthenticateRequest() {

        verify(postRequestedFor(
                urlEqualTo(MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT)
        )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.actionType",
                                equalTo("AUTHENTICATION"))
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.flowId")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.request.additionalParams")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath(
                                "$.event.request.additionalParams[?(@.name == 'testParam')]")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath(
                                "$.event.request.additionalParams[?(@.name == 'testParam')].value[0]",
                                equalTo("123_abc"))
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.tenant.id")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.tenant.name")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.organization.id")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.organization.name")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.organization.orgHandle")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.organization.depth",
                                equalTo("0"))
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.application.id")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.application.name")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.event.currentStepIndex",
                                equalTo("1"))
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath(
                                "$.allowedOperations[?(@.op == 'redirect')]")
                )
                .withRequestBody(
                        com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath("$.requestId")
                ));
    }
}
