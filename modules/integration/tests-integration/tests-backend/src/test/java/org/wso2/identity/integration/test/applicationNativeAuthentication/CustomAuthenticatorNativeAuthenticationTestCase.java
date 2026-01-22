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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.test.integration.service.dao.Attribute;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.CustomAuthenticatorManagementClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ADDITIONAL_DATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_SESSION_STATE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ENDPOINT_URL;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.INTERNAL_PROMPT;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.IDP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.serviceextensions.mockservices.MockCustomAuthenticatorService.API_AUTHENTICATE_ENDPOINT;

/**
 * Integration test class for testing native authentication flow with custom authenticators.
 * This test case extends {@link OAuth2ServiceAbstractIntegrationTest} and focuses on scenarios related
 * to custom authenticator integration with native authentication.
 * It covers custom authentication during the native authentication flow, including interaction between
 * the application, authorization server, custom authenticator service, and user.
 */
public class CustomAuthenticatorNativeAuthenticationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String appId;
    private String flowId;
    private String flowStatus;
    private String authenticatorId;
    private String authenticatorIdFromAuthnResponse;
    private String href;
    private String endpointUrl;
    private CloseableHttpClient client;
    private UserManagementClient userMgtServiceClient;
    private String code;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private CustomAuthenticatorManagementClient customAuthenticatorMgtClient;
    private MockCustomAuthenticatorService mockCustomAuthenticatorService;
    private SCIM2RestClient scim2RestClient;
    private UserDTO user;

    // Custom authenticator properties
    private static final String CUSTOM_AUTHENTICATOR_NAME = "custom-native-auth";
    private static final String CUSTOM_AUTHENTICATOR_DISPLAY_NAME = "Custom Native Authenticator";
    private static final String CUSTOM_AUTHENTICATOR_USERNAME = "endpointUsername";
    private static final String CUSTOM_AUTHENTICATOR_PASSWORD = "endpointPassword";
    
    // User properties for testing
    private static final String TEST_USER_FIRST_NAME = "TestFirst";
    private static final String TEST_USER_LAST_NAME = "TestLast";
    private static final String TEST_USER_EMAIL = "emily@aol.com";
    private static final String TEST_USER_PASSWORD = "Test@wso2";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        initializeClients();
        
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
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.addUser(Constants.TEST_USER_NAME, Constants.TEST_PASSWORD, null, Constants.TEST_PROFILE);

        user = createAndValidateUser();

        mockCustomAuthenticatorService = new MockCustomAuthenticatorService();
        mockCustomAuthenticatorService.start(getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()), user);

        authenticatorId = setupCustomAuthenticator();
        
        setSystemproperties();
        // Reset the idp cache object to remove effects from previous test cases.
        resetResidentIDPCache();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (mockCustomAuthenticatorService != null) {
            mockCustomAuthenticatorService.stop();
        }
        
        if (user != null && scim2RestClient != null) {
            scim2RestClient.deleteUser(user.getUserID());
        }
        
        if (appId != null) {
            deleteApp(appId);
        }
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.deleteUser(Constants.TEST_USER_NAME);
        
        if (authenticatorId != null && customAuthenticatorMgtClient != null) {
            customAuthenticatorMgtClient.deleteCustomAuthenticator(authenticatorId);
        }
        
        // Nullifying attributes.
        consumerKey = null;
        consumerSecret = null;
        appId = null;
        flowId = null;
        flowStatus = null;
        code = null;
        authenticatorId = null;
        authenticatorIdFromAuthnResponse = null;
        href = null;
        user = null;
        mockCustomAuthenticatorService = null;
        if (client != null) {
            client.close();
        }
        if (restClient != null) {
            restClient.closeHttpClient();
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (customAuthenticatorMgtClient != null) {
            customAuthenticatorMgtClient.closeHttpClient();
        }
    }

    @Test(groups = "wso2.is", description = "Register OAuth2 application with custom authenticator support")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createAppWithCustomAuthenticator();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        appId = application.getId();
        Assert.assertTrue(application.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API Based Authentication expected to true but set as false.");
    }

    private String setupCustomAuthenticator() throws Exception {

         authenticatorId = customAuthenticatorMgtClient.createCustomInternalUserAuthenticator(
                CUSTOM_AUTHENTICATOR_NAME, 
                CUSTOM_AUTHENTICATOR_DISPLAY_NAME, 
                mockCustomAuthenticatorService.getCustomAuthenticatorURL() + API_AUTHENTICATE_ENDPOINT,
                CUSTOM_AUTHENTICATOR_USERNAME, 
                CUSTOM_AUTHENTICATOR_PASSWORD);
        Assert.assertNotNull(authenticatorId, "Failed to setup custom authenticator.");
        return authenticatorId;
    }

    @Test(groups = "wso2.is", description = "Send initialize native auth request with custom authenticator",
            dependsOnMethods = "testRegisterApplication")
    public void testSendInitAuthRequestPost() throws Exception {

        HttpResponse response = sendPostRequestWithParameters(client, buildOAuth2Parameters(consumerKey),
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String responseString = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "Client Native Authentication Init response is null.");
        validInitClientNativeAuthnResponse(json);

        authenticateWithCustomAuthenticator(endpointUrl);
    }

    @Test(groups = "wso2.is", description = "Send custom authentication POST request.",
            dependsOnMethods = "testSendInitAuthRequestPost")
    public void testSendCustomAuthRequestPost() throws Exception {

        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + authenticatorIdFromAuthnResponse + "\",\n" +
                "        \"params\": {}\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost(href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Custom Authentication request failed. Authentication response is null.");

        validateCustomAuthenticationResponseBody(extractableResponse);
    }

    /**
     * Create Application with custom authenticator configuration
     *
     * @return ApplicationResponseModel
     * @throws Exception exception
     */
    private ApplicationResponseModel createAppWithCustomAuthenticator() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(Constants.TEST_APP_NAME);

        // Add authentication sequence with custom authenticator
        AuthenticationSequence authenticationSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator().idp("LOCAL").authenticator(CUSTOM_AUTHENTICATOR_NAME)));

        application.authenticationSequence(authenticationSequence);
        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().enableAPIBasedAuthentication(true));

        String appId = addApplication(application);
        return getApplication(appId);
    }

    /**
     * Builds a list of OAuth 2.0 parameters required for initiating the authorization process.
     *
     * @param consumerKey The client's unique identifier in the OAuth 2.0 system
     * @return A list of NameValuePair representing the OAuth 2.0 parameters
     */
    private List<NameValuePair> buildOAuth2Parameters(String consumerKey) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.AUTHORIZATION_CODE_NAME));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, Constants.RESPONSE_MODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));

        return urlParameters;
    }

    /**
     * Validates the structure and content of a Client Native Authentication JSON response.
     *
     * @param json The JSON object representing the Client Native Authentication response
     */
    private void validInitClientNativeAuthnResponse(JSONObject json) {

        // Check for the presence of required keys and their expected types
        if (json.containsKey(FLOW_ID) && json.containsKey(FLOW_STATUS) && json.containsKey(FLOW_TYPE) &&
                json.containsKey(NEXT_STEP) && json.containsKey(LINKS)) {

            flowId = (String) json.get(FLOW_ID);
            flowStatus = (String) json.get(FLOW_STATUS);

            JSONObject nextStepNode = (JSONObject) json.get(NEXT_STEP);
            if (nextStepNode.containsKey(STEP_TYPE) && nextStepNode.containsKey(AUTHENTICATORS)) {
                JSONArray authenticatorsArray = (JSONArray) nextStepNode.get(AUTHENTICATORS);
                if (!authenticatorsArray.isEmpty()) {
                    JSONObject authenticator = (JSONObject) authenticatorsArray.get(0);
                    if (authenticator.containsKey(AUTHENTICATOR_ID) && authenticator.containsKey(AUTHENTICATOR) &&
                            authenticator.containsKey(IDP) && authenticator.containsKey(METADATA)) {

                        authenticatorIdFromAuthnResponse = (String) authenticator.get(AUTHENTICATOR_ID);

                        JSONObject metadataNode = (JSONObject) authenticator.get(METADATA);
                        if (!INTERNAL_PROMPT.equals(metadataNode.get(PROMPT_TYPE).toString())) {
                            Assert.fail("The promptType must be INTERNAL_PROMPT " +
                                    "in Client native authentication JSON Response.");
                        }
                        if (metadataNode.containsKey(REQUIRED_PARAMS)) {
                            Assert.fail("The requiredParams must not be available " +
                                    "in Client native authentication JSON Response.");
                        }
                        if (metadataNode.containsKey(ADDITIONAL_DATA)) {
                            JSONObject additionalDataNode = (JSONObject) metadataNode.get(ADDITIONAL_DATA);
                            if (additionalDataNode.containsKey(ENDPOINT_URL)) {
                                endpointUrl = (String) additionalDataNode.get(ENDPOINT_URL);
                            } else {
                                Assert.fail("endpointUrl is not available in additionalData in " +
                                        "Client native authentication JSON Response.");
                            }
                        } else {
                            Assert.fail("additionalData is not available in metadata in " +
                                    "Client native authentication JSON Response.");
                        }
                    }
                } else {
                    Assert.fail("Authenticator is not expected format in Client native authentication");
                }
            } else {
                Assert.fail("Authenticators in Client native authentication JSON Response is null, " +
                        "expecting list of Authentication.");
            }
            JSONArray links = (JSONArray) json.get(LINKS);
            JSONObject link = (JSONObject) links.get(0);
            if (link.containsKey(HREF)) {
                href = link.get(HREF).toString();
            } else {
                Assert.fail("Link is not available for next step in Client native authentication JSON Response.");
            }
        } else {
            Assert.fail("Client native authentication JSON Response is not in expected format.");
        }
    }

    /**
     * Invoke given endpointUri for JSON POST request with given body and headers.
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @param headers     list of headers to be added to the request
     * @return response
     */
    protected Response getResponseOfJSONPost(String endpointUri, String body, Map<String, String> headers) {

        return given()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(body)
                .when()
                .post(endpointUri);
    }

    /**
     * Validates specific fields in the JSON response of custom authentication response.
     *
     * @param extractableResponse The ExtractableResponse containing the JSON response
     */
    private void validateCustomAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse) {

        // Validate specific fields in the JSON response
        flowStatus = extractableResponse
                .jsonPath()
                .getString(FLOW_STATUS);
        Assert.assertEquals(flowStatus, SUCCESS_COMPLETED);

        code = extractableResponse
                .jsonPath()
                .getString(AUTH_DATA_CODE);
        Assert.assertNotNull(code, "Authorization Code is null in the authData");

        Assert.assertNotNull(extractableResponse
                .jsonPath()
                .getString(AUTH_DATA_SESSION_STATE), "Session state is null in the authData");
    }

    private void resetResidentIDPCache() throws Exception {

        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        IdentityProvider residentIdp = superTenantIDPMgtClient.getResidentIdP();

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdp.getFederatedAuthenticatorConfigs();
        List<FederatedAuthenticatorConfig> configsToKeep = new ArrayList<>();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                configsToKeep.add(authenticatorConfig);
            }
        }
        federatedAuthenticatorConfigs = configsToKeep.toArray(new FederatedAuthenticatorConfig[0]);
        residentIdp.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        superTenantIDPMgtClient.updateResidentIdP(residentIdp);
    }

    private UserDTO createAndValidateUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_EMAIL);
        userInfo.setPassword(TEST_USER_PASSWORD);
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

        return user;
    }

    private Attribute createUserAttribute(String attributeName, String attributeValue) {

        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeName);
        attribute.setAttributeValue(attributeValue);

        return attribute;
    }

    private void initializeClients() throws Exception {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        customAuthenticatorMgtClient = new CustomAuthenticatorManagementClient(serverURL, tenantInfo);
    }

    private void authenticateWithCustomAuthenticator(String endpointUrl) throws Exception {

        Document customAuthenticatorPage = fetchCustomAuthenticatorPage(endpointUrl);
        submitCredentialsToCustomAuthenticator(customAuthenticatorPage);
    }

    private Document fetchCustomAuthenticatorPage(String customAuthenticatorPageUrl) throws Exception {

        String separator = customAuthenticatorPageUrl.contains("?") ? "&" : "?";
        String urlWithSpId = customAuthenticatorPageUrl + separator + "spId=" + appId;
        HttpResponse customAuthenticatorPageRequest = sendGetRequest(client, urlWithSpId);
        assertEquals(customAuthenticatorPageRequest.getStatusLine().getStatusCode(), 200);
        assertTrue(customAuthenticatorPageRequest.containsHeader("Content-Type"));
        assertTrue(customAuthenticatorPageRequest.getHeaders("Content-Type")[0].getValue().contains("text/html"),
                "Response is not HTML");

        Document doc = Jsoup.parse(EntityUtils.toString(customAuthenticatorPageRequest.getEntity()));
        EntityUtils.consume(customAuthenticatorPageRequest.getEntity());
        assertNotNull(doc, "Custom authenticator page is null.");
        return doc;
    }

    private void submitCredentialsToCustomAuthenticator(Document customAuthenticatorPage) throws Exception {

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
        HttpResponse customAuthenticatorLoginResponse = client.execute(customAuthenticatorLoginRequest);
        assertNotNull(customAuthenticatorLoginResponse);
        assertEquals(customAuthenticatorLoginResponse.getStatusLine().getStatusCode(), 200);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(EntityUtils.toString(customAuthenticatorLoginResponse.getEntity()));
        EntityUtils.consume(customAuthenticatorLoginResponse.getEntity());

        String commonAuthUrl = jsonNode.get("redirectingTo").asText();
        assertTrue(StringUtil.isNotBlank(commonAuthUrl));
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
}
