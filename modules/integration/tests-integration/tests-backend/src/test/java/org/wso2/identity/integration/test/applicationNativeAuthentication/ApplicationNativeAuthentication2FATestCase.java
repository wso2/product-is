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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

import com.icegreen.greenmail.util.GreenMailUtil;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.mail.Message;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_SESSION_STATE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONFIDENTIAL;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.DESCRIPTION;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.DISPLAY_NAME;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FAIL_INCOMPLETE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.I18N_KEY;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.IDP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGES;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.MESSAGE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ORDER;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PARAM;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.RESPONSE_MODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_APP_NAME;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_PASSWORD;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_USER_NAME;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TRACE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;

/**
 * Integration test class for testing the native authentication flow in an OAuth 2.0-enabled application.
 * This test case extends {@link OAuth2ServiceAbstractIntegrationTest} and focuses on scenarios related
 * to native authentication, covering the interaction between the application, authorization server, and user.
 * The app contains basic as first authentication matrix and email otp as second authentication step.
 */
public class ApplicationNativeAuthentication2FATestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String appId;
    private String flowId;
    private String flowStatus;
    private String authenticatorId;
    private String href;
    private JSONArray paramsArray;
    private CloseableHttpClient client;
    private String code;
    protected SCIM2RestClient scim2RestClient;
    private UserObject userObject;
    private String userId;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        Utils.getMailServer().purgeEmailFromAllMailboxes();
        super.init(TestUserMode.SUPER_TENANT_USER);

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

        setSystemproperties();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userObject = initUser();
        createUser(userObject);
        // Reset the idp cache object to remove effects from previous test cases.
        resetResidentIDPCache();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(appId);
        deleteUser(userObject);
        scim2RestClient = null;

        // Nullifying attributes.
        consumerKey = null;
        consumerSecret = null;
        appId = null;
        flowId = null;
        flowStatus = null;
        code = null;
        authenticatorId = null;
        href = null;
        paramsArray = null;
        client.close();
        restClient.closeHttpClient();
        Utils.getMailServer().purgeEmailFromAllMailboxes();
    }

    private UserObject initUser() {

        UserObject user = new UserObject();
        user.setUserName(TEST_USER_NAME);
        user.setPassword(TEST_PASSWORD);
        user.setName(new Name().givenName(OIDCUtilTest.firstName).familyName(OIDCUtilTest.lastName));
        user.addEmail(new Email().value(OIDCUtilTest.email));
        return user;
    }

    /**
     * Creates a user.
     *
     * @param user user instance.
     * @throws Exception If an error occurred while creating a user.
     */
    private void createUser(UserObject user) throws Exception {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userId = scim2RestClient.createUser(user);

        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath("users");
        rolePatchReqObject.addValue(new ListObject().value(userId));

        String roleId = scim2RestClient.getRoleIdByName("everyone");
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);
    }

    /**
     * Deletes a user.
     *
     * @param user user instance.
     * @throws Exception If an error occurred while deleting a user.
     */
    private void deleteUser(UserObject user) throws Exception {

        log.info("Deleting User " + user.getUserName());
        scim2RestClient.deleteUser(userId);
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow for default configurations.")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createApp();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        appId = application.getId();
        Assert.assertTrue(application.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API Base Authentication expected to false by default  but set as true.");

    }

    @Test(groups = "wso2.is", description = "Send init authorize POST request.",
            dependsOnMethods = "testRegisterApplication")
    public void testSendInitAuthRequestPost() throws Exception {

        HttpResponse response = sendPostRequestWithParameters(client, buildOAuth2Parameters(consumerKey),
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String responseString = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "Client Native Authentication Init response is null.");
        validInitClientNativeAuthnResponse(json);
    }

    @Test(groups = "wso2.is", description = "Send Basic authentication POST request.",
            dependsOnMethods = "testSendInitAuthRequestPost")
    public void testSendBasicAuthRequestWithFalseAuthenticator() throws Exception {

        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + "falseAuthenticatorId" + "\",\n" +
                "        \"params\": {\n" +
                "           \"username\": \"" + TEST_USER_NAME + "\",\n" +
                "           \"password\": \"" + TEST_PASSWORD + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost( href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Basic Authentication request failed. Authentication response is null.");

        validateFailedBasicAuthenticationResponseBody(extractableResponse);
    }

    @Test(groups = "wso2.is", description = "Send Basic authentication POST request.",
            dependsOnMethods = "testSendBasicAuthRequestWithFalseAuthenticator")
    public void testSendBasicAuthRequestPostWithFalsePassword() throws Exception {

        testSendInitAuthRequestPost();
        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + authenticatorId + "\",\n" +
                "        \"params\": {\n" +
                "           \"username\": \"" + TEST_USER_NAME + "\",\n" +
                "           \"password\": \"" + "FALSE_TEST_PASSWORD" + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost( href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Basic Authentication request failed. Authentication response is null.");

        validateBasicFailedAuthenticationResponseBody(extractableResponse);
    }

    @Test(groups = "wso2.is", description = "Send Basic authentication POST request.",
            dependsOnMethods = "testSendBasicAuthRequestPostWithFalsePassword")
    public void testSendBasicAuthRequestPost() throws Exception {

        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + authenticatorId + "\",\n" +
                "        \"params\": {\n" +
                "           \"username\": \"" + TEST_USER_NAME + "\",\n" +
                "           \"password\": \"" + TEST_PASSWORD + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost( href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Basic Authentication request failed. Authentication response is null.");

        validateBasicAuthenticationResponseBody(extractableResponse);
    }

    @Test(groups = "wso2.is", description = "Send Email OTP POST request.",
            dependsOnMethods = "testSendBasicAuthRequestPost")
    public void testSendEmailOTPRequestPost() {

        String emailOTP = getOTPFromEmail();

        if (emailOTP == null) {
            Assert.fail("Unable to retrieve email otp from the email otp body");
        }
        String body = "{\n" +
                "    \"flowId\": \"" + flowId + "\",\n" +
                "    \"selectedAuthenticator\": {\n" +
                "        \"authenticatorId\": \"" + authenticatorId + "\",\n" +
                "        \"params\": {\n" +
                "           \"OTPCode\": \"" + emailOTP + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        Response authnResponse = getResponseOfJSONPost( href, body, new HashMap<>());
        ExtractableResponse<Response> extractableResponse = authnResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .extract();
        Assert.assertNotNull(extractableResponse, "Email OTP Authentication request failed. " +
                "Authentication response is null.");

        validateEmailOTPAuthenticationResponseBody(extractableResponse);
    }

    /**
     * Validates specific fields in the JSON response of a basic authentication response.
     *
     * @param extractableResponse The ExtractableResponse containing the JSON response
     */
    private void validateEmailOTPAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse) {

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

    /**
     * Create Application with the given app configurations
     *
     * @return ApplicationResponseModel
     * @throws Exception exception
     */
    private ApplicationResponseModel createApp() throws Exception {

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
        application.setName(TEST_APP_NAME);
        application.advancedConfigurations(new AdvancedApplicationConfiguration());
        application.getAdvancedConfigurations().setEnableAPIBasedAuthentication(true);
        application.setAuthenticationSequence(new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new org.wso2.identity.integration.test.rest.api.server.application.management.
                        v1.model.AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("BasicAuthenticator"))));
        application.getAuthenticationSequence()
                .addStepsItem(new org.wso2.identity.integration.test.rest.api.server.application.management.
                        v1.model.AuthenticationStep()
                        .id(2)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("email-otp-authenticator")));
        String appId = addApplication(application);
        return getApplication(appId);
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
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, RESPONSE_MODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));

        return urlParameters;
    }

    /**
     * Validates the structure and content of a Client Native Authentication JSON response.
     * The method checks for the presence of required keys and their expected types in the provided JSON.
     * It verifies the format of the authentication flow, authenticators, metadata, and required parameters.
     * If the JSON response is not in the expected format, the method asserts failures using JUnit's Assert.fail().
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
                            authenticator.containsKey(IDP) && authenticator.containsKey(METADATA) &&
                            authenticator.containsKey(REQUIRED_PARAMS)) {

                        authenticatorId = (String) authenticator.get(AUTHENTICATOR_ID);
                        JSONObject metadataNode = (JSONObject) authenticator.get(METADATA);
                        if (metadataNode.containsKey(PROMPT_TYPE) && metadataNode.containsKey(PARAMS)) {
                            paramsArray = (JSONArray) metadataNode.get(PARAMS);
                            if (paramsArray.isEmpty()) {
                                Assert.fail("Content of param for the authenticator is null in " +
                                        "Client native authentication JSON Response.");
                            }
                        } else {
                            Assert.fail("Params for the authenticator is null in " +
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
     * Invoke given endpointUri for JSON POST request with given body, headers and Basic.
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

    private String getOTPFromEmail() {

        Assert.assertTrue(Utils.getMailServer().waitForIncomingEmail(10000, 1));
        Message[] messages = Utils.getMailServer().getReceivedMessages();
        String body = GreenMailUtil.getBody(messages[0]).replaceAll("=\r?\n", "");

        String otpPattern = "\\s*<b>(\\d+)</b>";
        Pattern pattern = Pattern.compile(otpPattern);
        Matcher matcher = pattern.matcher(body);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void resetResidentIDPCache() throws Exception {

        IdentityProviderMgtServiceClient superTenantIDPMgtClient =
                new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        IdentityProvider residentIdp = superTenantIDPMgtClient.getResidentIdP();

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdp.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig authenticatorConfig : federatedAuthenticatorConfigs) {
            if (!authenticatorConfig.getName().equalsIgnoreCase("samlsso")) {
                federatedAuthenticatorConfigs = (FederatedAuthenticatorConfig[])
                        ArrayUtils.removeElement(federatedAuthenticatorConfigs,
                                authenticatorConfig);
            }
        }
        residentIdp.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        superTenantIDPMgtClient.updateResidentIdP(residentIdp);
    }

    private void validateFailedBasicAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse)
            throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(extractableResponse.body().asString());


        // Check if the required keys are present
        if (json.containsKey(CODE) && json.containsKey(MESSAGE) &&
                json.containsKey(DESCRIPTION) && json.containsKey(TRACE_ID)) {

            // Extract and validate the values (optional)
            String code = (String) json.get(CODE);
            String message = (String) json.get(MESSAGE);
            String description = (String) json.get(DESCRIPTION);
            String traceId = (String) json.get(TRACE_ID);

            // Example validation: ensure no fields are null or empty
            if (code == null || code.isEmpty()) {
                Assert.fail("Code is missing or empty in the JSON response.");
            }

            if (message == null || message.isEmpty()) {
                Assert.fail("Message is missing or empty in the JSON response.");
            }

            if (description == null || description.isEmpty()) {
                Assert.fail("Description is missing or empty in the JSON response.");
            }

            if (traceId == null || traceId.isEmpty()) {
                Assert.fail("TraceId is missing or empty in the JSON response.");
            }

        } else {
            Assert.fail("JSON response is missing one or more required fields.");
        }
    }

    private void validateBasicFailedAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse)
            throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(extractableResponse.body().asString());

        // Check for the presence of required keys and their expected types
        if (json.containsKey(FLOW_ID) && json.containsKey(FLOW_STATUS) && json.containsKey(FLOW_TYPE) &&
                json.containsKey(NEXT_STEP) && json.containsKey(LINKS)) {

            Assert.assertEquals(flowId, (String) json.get(FLOW_ID), "Basic authentication " +
                    "JSON Response flow id is not same as init response.");
            flowId = (String) json.get(FLOW_ID);
            flowStatus = (String) json.get(FLOW_STATUS);
            Assert.assertEquals(flowStatus, FAIL_INCOMPLETE);

            JSONObject nextStepNode = (JSONObject) json.get(NEXT_STEP);
            if (nextStepNode.containsKey(STEP_TYPE) && nextStepNode.containsKey(AUTHENTICATORS)
                    && nextStepNode.containsKey(MESSAGES)) {
                JSONArray messagesArray = (JSONArray) nextStepNode.get(MESSAGES);
                // Ensure the array is not empty
                if (!messagesArray.isEmpty()) {
                    JSONObject messageObject = (JSONObject) messagesArray.get(0);

                    // Check for required fields within each message object
                    if (messageObject.containsKey(TYPE) && messageObject.containsKey(MESSAGE_ID) &&
                            messageObject.containsKey(MESSAGE) && messageObject.containsKey(I18N_KEY)) {

                        // Extract and validate values (optional)
                        String type = (String) messageObject.get(TYPE);
                        String messageId = (String) messageObject.get(MESSAGE_ID);
                        String message = (String) messageObject.get(MESSAGE);
                        String i18nKey = (String) messageObject.get(I18N_KEY);

                        // Example validation: Ensure none of the values are null or empty
                        if (type == null || type.isEmpty()) {
                            Assert.fail("Type is missing or empty in the messages array.");
                        }

                        if (messageId == null || messageId.isEmpty()) {
                            Assert.fail("Message ID is missing or empty in the messages array.");
                        }

                        if (message == null || message.isEmpty()) {
                            Assert.fail("Message is missing or empty in the messages array.");
                        }

                        if (i18nKey == null || i18nKey.isEmpty()) {
                            Assert.fail("i18nKey is missing or empty in the messages array.");
                        }

                    } else {
                        Assert.fail("A required field is missing in the message object.");
                    }
                } else {
                    Assert.fail("Messages array is empty.");
                }
            } else {
                Assert.fail("NextStep is missing required fields in Basic authentication JSON Response.");
            }
        } else {
            Assert.fail("Basic authentication JSON Response is missing required fields.");
        }
    }

    private void validateBasicAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse)
            throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(extractableResponse.body().asString());

        // Check for the presence of required keys and their expected types
        if (json.containsKey(FLOW_ID) && json.containsKey(FLOW_STATUS) && json.containsKey(FLOW_TYPE) &&
                json.containsKey(NEXT_STEP) && json.containsKey(LINKS)) {

            Assert.assertEquals(flowId, (String) json.get(FLOW_ID), "Basic authentication " +
                    "JSON Response flow id is not same as init response.");
            flowId = (String) json.get(FLOW_ID);
            flowStatus = (String) json.get(FLOW_STATUS);

            JSONObject nextStepNode = (JSONObject) json.get(NEXT_STEP);
            if (nextStepNode.containsKey(STEP_TYPE) && nextStepNode.containsKey(AUTHENTICATORS)) {
                JSONArray authenticatorsArray = (JSONArray) nextStepNode.get(AUTHENTICATORS);
                if (!authenticatorsArray.isEmpty()) {
                    JSONObject authenticator = (JSONObject) authenticatorsArray.get(0);
                    if (authenticator.containsKey(AUTHENTICATOR_ID) && authenticator.containsKey(AUTHENTICATOR) &&
                            authenticator.containsKey(IDP) && authenticator.containsKey(METADATA) &&
                            authenticator.containsKey(REQUIRED_PARAMS)) {

                        authenticatorId = (String) authenticator.get(AUTHENTICATOR_ID);
                        JSONObject metadataNode = (JSONObject) authenticator.get(METADATA);
                        if (metadataNode.containsKey(PROMPT_TYPE) && metadataNode.containsKey(PARAMS)) {
                            JSONArray paramsArray = (JSONArray) metadataNode.get(PARAMS);
                            if (!paramsArray.isEmpty()) {
                                JSONObject param = (JSONObject) paramsArray.get(0);
                                if (!param.containsKey(PARAM) || !param.containsKey(TYPE) ||
                                        !param.containsKey(ORDER) || !param.containsKey(I18N_KEY) ||
                                        !param.containsKey(DISPLAY_NAME) || !param.containsKey(CONFIDENTIAL)) {
                                    Assert.fail("Param for the authenticator is not in the expected format.");
                                }

                            } else {
                                Assert.fail("Params for the authenticator is empty in Basic authentication " +
                                        "JSON Response.");
                            }
                        } else {
                            Assert.fail("Metadata for the authenticator is missing required fields in Basic " +
                                    "authentication JSON Response.");
                        }
                    } else {
                        Assert.fail("Authenticator is missing required fields in Basic authentication JSON Response.");
                    }
                } else {
                    Assert.fail("Authenticators list is empty in Basic authentication JSON Response.");
                }
            } else {
                Assert.fail("NextStep is missing required fields in Basic authentication JSON Response.");
            }

            JSONArray linksArray = (JSONArray) json.get(LINKS);
            if (!linksArray.isEmpty()) {
                JSONObject link = (JSONObject) linksArray.get(0);
                if (!link.containsKey(HREF)) {
                    Assert.fail("Href for the link is missing in Client native authentication JSON Response.");
                }
            } else {
                Assert.fail("Links array is empty in Client native authentication JSON Response.");
            }
        } else {
            Assert.fail("Basic authentication JSON Response is missing required fields.");
        }
    }
}
