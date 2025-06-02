/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATORS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTHENTICATOR_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.AUTH_DATA_SESSION_STATE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.ERROR_CODE_CLIENT_NATIVE_AUTHENTICATION_DISABLED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_STATUS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.FLOW_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.HREF;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.IDP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.LINKS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.METADATA;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.NEXT_STEP;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.PROMPT_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.REQUIRED_PARAMS;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.RESPONSE_MODE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.STEP_TYPE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.SUCCESS_COMPLETED;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_APP_NAME;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_PASSWORD;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_PROFILE;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TEST_USER_NAME;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.TRACE_ID;
import static org.wso2.identity.integration.test.applicationNativeAuthentication.Constants.UTF_8;

/**
 * Integration test class for testing the native authentication flow in an OAuth 2.0-enabled application.
 * This test case extends {@link OAuth2ServiceAbstractIntegrationTest} and focuses on scenarios related
 * to native authentication, covering the interaction between the application, authorization server, and user.
 * It includes test cases for initiating authentication, handling responses, and ensuring the correct behavior
 * of the OAuth 2.0 service in the context of native authentication.
 */
public class ApplicationNativeAuthenticationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String appId;
    private String flowId;
    private String flowStatus;
    private String authenticatorId;
    private String href;
    private JSONArray paramsArray;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private UserManagementClient userMgtServiceClient;
    private String code;
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.addUser(TEST_USER_NAME, TEST_PASSWORD, null, TEST_PROFILE);

        setSystemproperties();
        // Reset the idp cache object to remove effects from previous test cases.
        resetResidentIDPCache();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(appId);
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.deleteUser(TEST_USER_NAME);
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
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow for default configurations.")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createAppWithoutClientNativeAuthentication();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        appId = application.getId();
        Assert.assertFalse(application.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API Base Authentication expected to false by default  but set as true.");

    }

    @Test(groups = "wso2.is", description = "Send authorize POST request without enabling Client Native Authentication.",
            dependsOnMethods = "testRegisterApplication")
    public void testSendAuthRequestPostWithDefaultConfiguration() throws Exception {

        HttpResponse response = sendPostRequestWithParameters(client, buildOAuth2Parameters(consumerKey),
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String responseString = EntityUtils.toString(response.getEntity(), UTF_8);
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        Assert.assertNotNull(json, "Client Native Authentication Init response is null.");
        Assert.assertNotNull(json.get(TRACE_ID), "Trace Id is not found int the response.");
        Assert.assertNotNull(json.get(CODE), "Error Code is not found int the response.");
        Assert.assertEquals(json.get(CODE), ERROR_CODE_CLIENT_NATIVE_AUTHENTICATION_DISABLED,
                "Expected disabled App native authentication error code ABA-60007, but got "
                        + json.get(CODE));
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow for client native authentication",
            dependsOnMethods = "testSendAuthRequestPostWithDefaultConfiguration")
    public void testUpdateApplication() throws Exception {

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch = applicationPatch.advancedConfigurations(new AdvancedApplicationConfiguration());
        applicationPatch.getAdvancedConfigurations().setEnableAPIBasedAuthentication(true);

        updateApplication(appId, applicationPatch);
        ApplicationResponseModel application = getApplication(appId);
        Assert.assertTrue(application.getAdvancedConfigurations().getEnableAPIBasedAuthentication(),
                "API Base Authentication expected to true but set as false.");
    }

    @Test(groups = "wso2.is", description = "Send init authorize POST request.",
            dependsOnMethods = "testUpdateApplication")
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
    }

    @Test(groups = "wso2.is", description = "Send Basic authentication POST request.",
            dependsOnMethods = "testSendInitAuthRequestPost")
    public void testSendBasicAuthRequestPost() {

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

    /**
     * Create Application with the given app configurations
     *
     * @return ApplicationResponseModel
     * @throws Exception exception
     */
    private ApplicationResponseModel createAppWithoutClientNativeAuthentication() throws Exception {

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
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.AUTHORIZATION_CODE_NAME));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, RESPONSE_MODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
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

    /**
     * Validates specific fields in the JSON response of a basic authentication response.
     *
     * @param extractableResponse The ExtractableResponse containing the JSON response
     */
    private void validateBasicAuthenticationResponseBody(ExtractableResponse<Response> extractableResponse) {

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
}
