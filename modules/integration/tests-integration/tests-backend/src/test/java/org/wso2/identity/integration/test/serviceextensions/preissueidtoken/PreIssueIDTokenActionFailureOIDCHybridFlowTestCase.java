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

package org.wso2.identity.integration.test.serviceextensions.preissueidtoken;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.UserClaimConfig;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ExpectedTokenResponse;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * This class tests the pre issue ID token action failure scenarios with OIDC Hybrid flow.
 * Tests both "code id_token" and "code id_token token" response types.
 */
public class PreIssueIDTokenActionFailureOIDCHybridFlowTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String TEST_WSO2 = "Test@wso2";
    private static final String PRE_ISSUE_ID_TOKEN_API_PATH = "preIssueIdToken";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN = "code id_token";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN = "code id_token token";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String OPENID_SCOPE = "openid";
    private static final String PROFILE_SCOPE = "profile";
    private static final String FIRST_NAME_CLAIM = "given_name";
    private static final String LAST_NAME_CLAIM = "family_name";
    private static final String USER_NAME_CLAIM = "username";

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private List<String> requestedScopes;
    private String clientId;
    private String actionId;
    private String applicationId;
    private String userId;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private final ActionResponse actionResponse;
    private final ExpectedTokenResponse expectedResponse;
    private final String responseType;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueIDTokenActionFailureOIDCHybridFlowTestCase(TestUserMode testUserMode, String responseType,
                                                               ActionResponse actionResponse,
                                                               ExpectedTokenResponse expectedResponse) {

        this.userMode = testUserMode;
        this.responseType = responseType;
        this.actionResponse = actionResponse;
        this.expectedResponse = expectedResponse;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{

                // Both server and client error scenarios are currently handled identically in the /authz endpoint.
                // As a result, incomplete and failure responses return the same error response.
                // This behavior should be improved by introducing distinct error handling
                // for different failure scenarios.
                // TODO: Improve error handling in the /authz endpoint.
                // Ref: https://github.com/wso2/product-is/issues/26555
                {TestUserMode.SUPER_TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/incomplete-response.json")),
                        new ExpectedTokenResponse(500, "server_error",
                                "Error occurred when processing the authorization request")},
                {TestUserMode.SUPER_TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "server_error",
                                "Error occurred when processing the authorization request")},
                {TestUserMode.SUPER_TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "server_error",
                                "Error occurred when processing the authorization request")},
                {TestUserMode.TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "server_error",
                                "Error occurred when processing the authorization request")},
                {TestUserMode.TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedTokenResponse(500, "server_error",
                                "Error occurred when processing the authorization request")},
                {TestUserMode.TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN, new ActionResponse(401, "Unauthorized"),
                        new ExpectedTokenResponse(500, "server_error",
                                "Error occurred when processing the authorization request")},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        applicationId = createOIDCAppWithHybridFlow();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();

        actionId = createPreIssueIDTokenAction();
        addUser();

        requestedScopes = new ArrayList<>();
        requestedScopes.add(OPENID_SCOPE);
        requestedScopes.add(PROFILE_SCOPE);

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();

        deleteAction(PRE_ISSUE_ID_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        serviceExtensionMockServer = null;
    }

    @Test(groups = "wso2.is", description = "Verify error response when pre-issue ID token action fails with " +
            "OIDC hybrid flow.")
    public void testPreIssueIDTokenActionFailure() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, responseType));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, clientId));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, scopes));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "SessionDataKey key value is null");

        String sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null");
        EntityUtils.consume(response.getEntity());

        response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, TEST_WSO2);

        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with tokens is null.");
        EntityUtils.consume(response.getEntity());

        // Extract error parameters from the fragment
        String fragment = locationHeader.getValue();
        String error = DataExtractUtil.extractParamFromURIFragment(fragment, "error");
        String errorDescription = DataExtractUtil.extractParamFromURIFragment(fragment, "error_description");

        // Verify error matches expected response
        assertNotNull(error, "Error parameter is null");
        assertEquals(error, expectedResponse.getErrorMessage(), "Expected " + expectedResponse.getErrorMessage() +
                " for action failure in hybrid flow");

        // Verify error description matches expected response
        if (errorDescription != null) {
            String decodedErrorDescription = URLDecoder.decode(errorDescription, StandardCharsets.UTF_8.name());
            assertNotNull(decodedErrorDescription, "Error description should not be null");
            assertTrue(decodedErrorDescription.contains(expectedResponse.getErrorDescription()),
                    "Expected error description to contain: " + expectedResponse.getErrorDescription() +
                    ", but got: " + decodedErrorDescription);
        }
    }


    private String createPreIssueIDTokenAction() throws IOException {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME);
        authProperties.put(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);

        ActionModel actionModel = new ActionModel();
        actionModel.setName("ID Token Pre Issue - Hybrid Flow Failure");
        actionModel.setDescription("This is a test pre issue ID token type for hybrid flow failure scenarios");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ID_TOKEN_API_PATH, actionModel);
    }

    private void addUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(TEST_WSO2);
        userInfo.setName(new Name().givenName("Test"));
        userInfo.getName().setFamilyName("User");
        userInfo.addEmail(new Email().value("test.user@gmail.com"));
        userId = scim2RestClient.createUser(userInfo);
    }

    /**
     * Creates an OIDC application with hybrid flow configuration and claims.
     *
     * @return Application ID
     * @throws Exception If there is an error during application creation
     */
    private String createOIDCAppWithHybridFlow() throws Exception {

        List<UserClaimConfig> userClaimConfigs = Arrays.asList(
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/givenname").
                        oidcClaimUri(FIRST_NAME_CLAIM).build(),
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/lastname").
                        oidcClaimUri(LAST_NAME_CLAIM).build(),
                new UserClaimConfig.Builder().localClaimUri("http://wso2.org/claims/username").
                        oidcClaimUri(USER_NAME_CLAIM).build()
        );

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .claimsList(userClaimConfigs)
                .grantTypes(new ArrayList<>(Collections.singletonList(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)))
                .enableHybridFlow(true)
                .responseTypes(new ArrayList<>(Collections.singletonList(responseType)))
                .tokenType(ApplicationConfig.TokenType.JWT)
                .expiryTime(3600)
                .skipConsent(true)
                .build();

        ApplicationResponseModel application = addApplication(applicationConfig);
        return application.getId();
    }
}

