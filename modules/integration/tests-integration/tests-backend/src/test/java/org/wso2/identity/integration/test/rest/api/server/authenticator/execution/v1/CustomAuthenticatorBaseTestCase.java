package org.wso2.identity.integration.test.rest.api.server.authenticator.execution.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.AuthenticationType;
import org.wso2.carbon.identity.api.server.authenticators.v1.model.UserDefinedLocalAuthenticatorCreation;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.test.integration.service.dao.Attribute;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.AuthenticatorTestBase;
import org.wso2.identity.integration.test.rest.api.server.authenticator.management.v1.util.UserDefinedLocalAuthenticatorPayload;
import org.wso2.identity.integration.test.rest.api.server.authenticator.mockserver.MockCustomAuthenticatorService;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.rest.api.server.authenticator.mockserver.MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

public class CustomAuthenticatorBaseTestCase extends AuthenticatorTestBase {

    public static final String TEST_USER_FIRST_NAME = "Emily";
    public static final String TEST_USER_LAST_NAME = "Ellon";
    public static final String TEST_USER_EMAIL = "emily@aol.com";
    public static final String GIVEN_NAME = "given_name";
    public static final String EMAIL = "email";
    public static final String FAMILY_NAME = "family_name";
    MockCustomAuthenticatorService mockCustomAuthenticatorService;

    protected OAuth2RestClient oAuth2RestClient;
    protected ApplicationManagementServiceClient appMgtClient;
    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;

    protected String consumerKey;
    protected String consumerSecret;
    protected String applicationId;
    private UserDefinedLocalAuthenticatorCreation creationPayload;

    private static final String ADMIN_WSO2 = "Admin@wso2";
    private String authorizationCode = null;
    private String scopes;
    UserDTO internalUser;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

        appMgtClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
        mockCustomAuthenticatorService = new MockCustomAuthenticatorService();
        scopes = String.join(" ", OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL, OAuth2Constant.OAUTH2_SCOPE_PROFILE);

        // Creating an internal user.
        internalUser = createUser();
        assertNotNull(internalUser);
        assertTrue(StringUtil.isNotBlank(internalUser.getUserID()));

        /*
        Sharing the internally created user information with the mock custom
        authenticator service as a mock step.
         */
        mockCustomAuthenticatorService.start(serverURL, internalUser);
    }

    @Test(priority = 1)
    public void testLoginFlowSetup() throws Exception {

        // Custom authenticator setup.
        UserDefinedLocalAuthenticatorConfig testAuthenticatorConfig = createBaseUserDefinedLocalAuthenticator(
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        creationPayload = UserDefinedLocalAuthenticatorPayload
                .getBasedUserDefinedLocalAuthenticatorCreation(testAuthenticatorConfig);
        testCreateUserDefinedLocalAuthenticator();

        // Application setup
        String applicationID = createApplication();
        assertNotNull(applicationID, "Failed to create an application with custom authenticator");
        ApplicationResponseModel applicationResponse = oAuth2RestClient.getApplication(applicationID);
        assertNotNull(applicationResponse, "Failed to retrieve the created application with custom authenticator");

        OpenIDConnectConfiguration oidcInboundConfig = oAuth2RestClient.getOIDCInboundDetails(applicationID);
        consumerKey = oidcInboundConfig.getClientId();
        assertNotNull(consumerKey, "Consumer Key is null.");
        consumerSecret = oidcInboundConfig.getClientSecret();
        assertNotNull(consumerSecret, "Consumer Secret is null.");

    }

    @Test(dependsOnMethods = "testLoginFlowSetup")
    public void testCustomAuthenticate() throws IOException {

        // Authorize request to the application.
        HttpResponse appAuthorizeRQ = sendPostRequestWithParameters(client, buildOAuth2Parameters(consumerKey),
                OAuth2Constant.AUTHORIZED_USER_URL);
        assertNotNull(appAuthorizeRQ, "Authorization request failed. Authorized response is null.");
        assertEquals(appAuthorizeRQ.getStatusLine().getStatusCode(), 302);

        // App returns a header with redirect url to the custom authenticator page.
        Header locationHeader =
                appAuthorizeRQ.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(appAuthorizeRQ.getEntity());

        String customAuthenticatorPageUrl = locationHeader.getValue();
        assertTrue(StringUtil.isNotBlank(customAuthenticatorPageUrl));

        // Custom authenticator page request.
        HttpResponse customAuthenticatorPageRS = sendGetRequest(client, customAuthenticatorPageUrl);

        assertEquals(customAuthenticatorPageRS.getStatusLine().getStatusCode(), 200);
        assertTrue(customAuthenticatorPageRS.containsHeader("Content-Type"));
        assertTrue(customAuthenticatorPageRS.getHeaders("Content-Type")[0].getValue().contains("text/html"),
                "Response is not HTML");

        Document doc = Jsoup.parse(EntityUtils.toString(customAuthenticatorPageRS.getEntity()));
        EntityUtils.consume(customAuthenticatorPageRS.getEntity());
        assertNotNull(doc);

        // Assert the presence of the expected HTML elements
        assertFalse(doc.select("form[action='/api/validate-pin']").isEmpty(), "Form not found in HTML");
        assertFalse(doc.select("input[name='flowId']").isEmpty(), "FlowId input not found in HTML");
        assertFalse(doc.select("input[name='username']").isEmpty(), "Username input not found in HTML");
        assertFalse(doc.select("input[name='pin']").isEmpty(), "PIN input not found in HTML");
        assertFalse(doc.select("button[type='submit']").isEmpty(), "Submit button not found in HTML");

        Element form = doc.selectFirst("form");

        String customAuthenticatorAuthUrl = mockCustomAuthenticatorService.getCustomAuthenticatorURL();
        if (form != null) {
            String actionUrl = form.attr("action");
            assertTrue(StringUtil.isNotBlank(actionUrl));

            customAuthenticatorAuthUrl = customAuthenticatorAuthUrl + actionUrl;
        }
        Element flowIdElement = doc.selectFirst("input[name=flowId]");
        String flowId = (flowIdElement != null) ? flowIdElement.attr("value") : null;
        assertTrue(StringUtil.isNotBlank(flowId));

        //Submit the credentials to custom authenticator.
        HttpPost customAuthenticatorAuthRQ = generateCustomAuthenticatorAuthRQ(customAuthenticatorAuthUrl, flowId);
        HttpResponse customAuthenticatorAuthRS = client.execute(customAuthenticatorAuthRQ);
        assertNotNull(customAuthenticatorAuthRS);
        assertEquals(customAuthenticatorAuthRS.getStatusLine().getStatusCode(), 200);

        // Custom authenticator returns a page with commonAuth url.
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(customAuthenticatorAuthRS.getEntity()));
        EntityUtils.consume(customAuthenticatorAuthRS.getEntity());

        String commonAuthUrl = jsonNode.get("redirectingTo").asText();
        assertTrue(StringUtil.isNotBlank(commonAuthUrl));

        // Sending the commonAuth request.
        HttpResponse commonAuthRS = sendGetRequest(client, commonAuthUrl);
        assertNotNull(commonAuthRS);
        assertEquals(commonAuthRS.getStatusLine().getStatusCode(), 200);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractTableRowDataFromResponse(commonAuthRS, keyPositionMap);

        if (keyValues != null) {
            authorizationCode = keyValues.get(0).getValue();
        }
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(commonAuthRS.getEntity());
    }

    @Test(dependsOnMethods = "testCustomAuthenticate")
    public void testGetAccessTokenWithAuthCodeGrant() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(consumerKey, consumerSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive a response for access token request.");

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

        assertNotNull(jwtClaimsOfIdToken.getClaim(GIVEN_NAME));
        String firstName = jwtClaimsOfIdToken.getClaim(GIVEN_NAME).toString();
        assertEquals(firstName, TEST_USER_FIRST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(FAMILY_NAME));
        String lastName = jwtClaimsOfIdToken.getClaim(FAMILY_NAME).toString();
        assertEquals(lastName, TEST_USER_LAST_NAME);

        assertNotNull(jwtClaimsOfIdToken.getClaim(EMAIL));
        String email = jwtClaimsOfIdToken.getClaim(EMAIL).toString();
        assertEquals(email, TEST_USER_EMAIL);
    }

    @Test(dependsOnMethods = "testGetAccessTokenWithAuthCodeGrant")
    public void testDeleteUserDefinedLocalAuthenticator() throws JsonProcessingException {

        Response response = getResponseOfDelete(AUTHENTICATOR_CUSTOM_API_BASE_PATH + PATH_SEPARATOR
                + customIdPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    /**
     * Extracts the JWT claims set from a given JWT token.
     *
     * @param jwtToken JWT token from which claims are to be extracted
     * @return JWTClaimsSet extracted from the provided JWT token
     * @throws ParseException If there is an error in parsing the JWT token
     */
    private JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    /**
     * Get base64 encoded string of consumer key and secret.
     *
     * @param consumerKey    Consumer key of the application.
     * @param consumerSecret Consumer secret of the application.
     * @return Base 64 encoded string.
     */
    public String getBase64EncodedString(String consumerKey, String consumerSecret) {

        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

    private static HttpPost generateCustomAuthenticatorAuthRQ(String customAuthenticatorAuthUrl, String flowId)
            throws UnsupportedEncodingException {

        HttpPost customAuthenticatorAuthRQ = new HttpPost(customAuthenticatorAuthUrl);
        customAuthenticatorAuthRQ.setHeader("Content-Type", "application/json");
        customAuthenticatorAuthRQ.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        String jsonBody = "{"
                + "\"flowId\": \"" + flowId + "\","
                + "\"username\": \"emily@aol.com\","
                + "\"pin\": \"1234\""
                + "}";

        customAuthenticatorAuthRQ.setEntity(new StringEntity(jsonBody));
        return customAuthenticatorAuthRQ;
    }

    public HttpResponse sendGetRequest(HttpClient client, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return client.execute(getRequest);
    }

    /**
     * Send post request with parameters.
     *
     * @param client        HttpClient.
     * @param urlParameters Url parameters.
     * @param url           Endpoint.
     * @return HttpResponse.
     * @throws ClientProtocolException If an error occurred while executing http POST request.
     * @throws java.io.IOException     If an error occurred while executing http POST request.
     */
    public HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters, String url)
            throws ClientProtocolException, IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    /**
     * Builds a list of OAuth 2.0 parameters required for initiating the authorization process.
     * The method constructs and returns a list of parameters necessary for initiating the OAuth 2.0 authorization process.
     *
     * @param consumerKey The client's unique identifier in the OAuth 2.0 system
     * @return A list of NameValuePair representing the OAuth 2.0 parameters
     */
    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, scopes));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_PLAYGROUND_NAME, OAuth2Constant
                .OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CONSUMER_KEY_PLAYGROUND_NAME, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.CALLBACKURL_PLAYGROUND_NAME, OAuth2Constant
                .CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_ENDPOINT_PLAYGROUND_NAME, OAuth2Constant
                .APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZE_PLAYGROUND_NAME, OAuth2Constant
                .AUTHORIZE_PARAM));

        return urlParameters;
    }

    // Configure the custom authenticator in WSO2 Identity Server
    private void testCreateUserDefinedLocalAuthenticator() throws JsonProcessingException {

        String body = UserDefinedLocalAuthenticatorPayload.convertToJasonPayload(creationPayload);
        Response response = getResponseOfPost(AUTHENTICATOR_CUSTOM_API_BASE_PATH, body);
        String CUSTOM_TAG = "Custom";
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("id", equalTo(customIdPId))
                .body("name", equalTo(AUTHENTICATOR_NAME))
                .body("displayName", equalTo(AUTHENTICATOR_DISPLAY_NAME))
                .body("type", equalTo("LOCAL"))
                .body("definedBy", equalTo("USER"))
                .body("isEnabled", equalTo(true))
                .body("tags", hasItem(CUSTOM_TAG))
                .body("self", equalTo(getTenantedRelativePath(
                        AUTHENTICATOR_CONFIG_API_BASE_PATH + customIdPId, tenant)));
    }

    protected UserDefinedLocalAuthenticatorConfig createBaseUserDefinedLocalAuthenticator(
            AuthenticatorPropertyConstants.AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig config = new UserDefinedLocalAuthenticatorConfig(type);
        config.setName(AUTHENTICATOR_NAME);
        config.setDisplayName(AUTHENTICATOR_DISPLAY_NAME);
        config.setEnabled(true);

        UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder endpointConfig =
                new UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfig.uri(mockCustomAuthenticatorService.getCustomAuthenticatorURL() + API_AUTHENTICATE_ENDPOINT);
        endpointConfig.authenticationType(String.valueOf(AuthenticationType.TypeEnum.BASIC));
        endpointConfig.authenticationProperties(new HashMap<String, String>() {{
            put("username", "adminUsername");
            put("password", "adminPassword");
        }});
        config.setEndpointConfig(endpointConfig.build());

        return config;
    }

    private String createApplication() throws JSONException, IOException {

        ApplicationModel application = new ApplicationModel();
        application.setName("App for custom authenticator");

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE,
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
                .addStepsItem(
                        new org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep()
                                .id(1)
                                .addOptionsItem(new Authenticator().idp("LOCAL").authenticator(AUTHENTICATOR_NAME)));

        application.authenticationSequence(authenticationSequence);
        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(true).skipLogoutConsent(true));

        return oAuth2RestClient.createApplication(application);
    }

    public HttpResponse sendPostRequest(HttpClient client, List<Header> headerList, List<NameValuePair> urlParameters,
                                        String url) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeaders(headerList.toArray(new Header[0]));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private UserDTO createUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(CustomAuthenticatorBaseTestCase.TEST_USER_EMAIL);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(CustomAuthenticatorBaseTestCase.TEST_USER_FIRST_NAME));
        userInfo.getName().setFamilyName(CustomAuthenticatorBaseTestCase.TEST_USER_LAST_NAME);
        userInfo.addEmail(new Email().value(CustomAuthenticatorBaseTestCase.TEST_USER_EMAIL));

        String userId = scim2RestClient.createUser(userInfo);

        UserDTO internalUser = new UserDTO();
        internalUser.setUserID(userId);

        List<Attribute> userAttributes = new ArrayList<>();
        userAttributes.add(createUserAttribute("http://wso2.org/claims/username",
                CustomAuthenticatorBaseTestCase.TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/emailaddress",
                CustomAuthenticatorBaseTestCase.TEST_USER_EMAIL));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/lastname",
                CustomAuthenticatorBaseTestCase.TEST_USER_LAST_NAME));
        userAttributes.add(createUserAttribute("http://wso2.org/claims/givenname",
                CustomAuthenticatorBaseTestCase.TEST_USER_FIRST_NAME));
        internalUser.setAttributes(userAttributes.toArray(new Attribute[0]));

        return internalUser;
    }

    private Attribute createUserAttribute(String attributeName, String attributeValue) {

        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeName);
        attribute.setAttributeValue(attributeValue);

        return attribute;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        mockCustomAuthenticatorService.stop();
        if (internalUser != null) {
            scim2RestClient.deleteUser(internalUser.getUserID());
        }
        oAuth2RestClient.deleteApplication(applicationId);
        // Nullifying attributes.
        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        client.close();
        oAuth2RestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

}
