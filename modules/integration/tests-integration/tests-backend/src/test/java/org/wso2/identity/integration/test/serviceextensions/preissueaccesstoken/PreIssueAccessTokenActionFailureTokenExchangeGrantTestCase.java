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

package org.wso2.identity.integration.test.serviceextensions.preissueaccesstoken;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.IdTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ActionResponse;
import org.wso2.identity.integration.test.serviceextensions.dataprovider.model.ExpectedTokenResponse;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;

/**
 * Tests the pre-issue access token action failure scenarios with token exchange grant type.
 */
public class PreIssueAccessTokenActionFailureTokenExchangeGrantTestCase extends ActionsBaseTestCase {

    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String TOKEN_EXCHANGE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt";
    private static final String REQUESTED_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";
    private static final String IS_JWKS_URI = "https://localhost:9853/oauth2/jwks";
    private static final String SCIM2_USERS_API = "/o/scim2/Users";
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);

    private final String subjectAppName;
    private final String exchangeAppName;
    private final String trustedIdpName;
    private final String trustedIdpAlias;

    private CloseableHttpClient client;
    private IdpMgtRestClient idpMgtRestClient;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private List<String> requestedScopes;
    private String subjectToken;
    private String subjectAppClientId;
    private String subjectAppClientSecret;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String subjectApplicationId;
    private String applicationId;
    private String trustedIdpId;
    private final TestUserMode userMode;
    private final ActionResponse actionResponse;
    private final ExpectedTokenResponse expectedTokenResponse;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionFailureTokenExchangeGrantTestCase(TestUserMode testUserMode,
                                                                      ActionResponse actionResponse,
                                                                      ExpectedTokenResponse expectedTokenResponse) {

        this.userMode = testUserMode;
        this.actionResponse = actionResponse;
        this.expectedTokenResponse = expectedTokenResponse;
        int index = INSTANCE_COUNTER.incrementAndGet();
        this.subjectAppName = "SubjectTokenApplicationFailure_" + index;
        this.exchangeAppName = "TokenExchangeApplicationFailure_" + index;
        this.trustedIdpName = "TrustedTokenIssuerFailure_" + index;
        this.trustedIdpAlias = "trustedTokenAliasFailure_" + index;
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/incomplete-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.SUPER_TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(200,
                        FileUtils.readFileInClassPathAsString("actions/response/failure-response.json")),
                        new ExpectedTokenResponse(400, "Some failure reason", "Some description")},
                {TestUserMode.TENANT_USER, new ActionResponse(500,
                        FileUtils.readFileInClassPathAsString("actions/response/error-response.json")),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
                {TestUserMode.TENANT_USER, new ActionResponse(401, "Unauthorized"),
                        new ExpectedTokenResponse(500, "server_error", "Internal Server Error.")},
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
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();

        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);

        ApplicationResponseModel subjectApplication = createSubjectApplication();
        subjectApplicationId = subjectApplication.getId();
        OpenIDConnectConfiguration subjectOidcConfig = getOIDCInboundDetailsOfApplication(subjectApplicationId);
        subjectAppClientId = subjectOidcConfig.getClientId();
        subjectAppClientSecret = subjectOidcConfig.getClientSecret();

        ApplicationResponseModel application = createTokenExchangeApplication();
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
            authorizeSystemAPIs(subjectApplicationId, Collections.singletonList(SCIM2_USERS_API));
        }

        trustedIdpId = createTrustedTokenIssuerIdp();

        requestedScopes = new ArrayList<>(Arrays.asList("openid",
                "internal_org_user_mgt_list", "internal_org_user_mgt_view"));

        subjectToken = obtainSubjectToken();

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                actionResponse.getResponseBody(), actionResponse.getStatusCode());

        actionId = createPreIssueAccessTokenAction();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();
        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        deleteApp(subjectApplicationId);
        idpMgtRestClient.deleteIdp(trustedIdpId);

        restClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        idpMgtRestClient.closeHttpClient();
        client.close();

        serviceExtensionMockServer = null;
        subjectToken = null;
    }

    @Test(groups = "wso2.is", description = "Verify token response when pre-issue access token action fails " +
            "with token exchange grant type.")
    public void testPreIssueAccessTokenActionFailure() throws Exception {

        HttpResponse response = sendTokenExchangeRequest();

        assertNotNull(response);
        assertEquals(response.getStatusLine().getStatusCode(), expectedTokenResponse.getStatusCode());

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

        JSONObject jsonResponse = new JSONObject(responseString);
        assertEquals(jsonResponse.getString("error"), expectedTokenResponse.getErrorMessage());
        assertEquals(jsonResponse.getString("error_description"), expectedTokenResponse.getErrorDescription());
    }

    private HttpResponse sendTokenExchangeRequest() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", TOKEN_EXCHANGE_GRANT_TYPE));
        parameters.add(new BasicNameValuePair("subject_token", subjectToken));
        parameters.add(new BasicNameValuePair("subject_token_type", SUBJECT_TOKEN_TYPE));
        parameters.add(new BasicNameValuePair("requested_token_type", REQUESTED_TOKEN_TYPE));

        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        return sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
    }

    private String obtainSubjectToken() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        String scopes = String.join(" ", requestedScopes);
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(subjectAppClientId, subjectAppClientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        if (!jsonResponse.has("access_token")) {
            throw new RuntimeException(
                    "Subject token not found in client credentials response. Response: " + responseString);
        }
        return jsonResponse.getString("access_token");
    }

    private String createPreIssueAccessTokenAction() throws IOException {

        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> authProperties = new HashMap<>();
        authProperties.put(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME);
        authProperties.put(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);
        authenticationType.setProperties(authProperties);

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(EXTERNAL_SERVICE_URI);
        endpoint.setAuthentication(authenticationType);
        endpoint.addAllowedHeadersItem("testHeader");
        endpoint.addAllowedParametersItem("testParam");

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
    }

    private String createTrustedTokenIssuerIdp() throws Exception {

        IdentityProviderPOSTRequest.Certificate certificate = new IdentityProviderPOSTRequest.Certificate();
        certificate.setJwksUri(getTenantQualifiedURL(IS_JWKS_URI, tenantInfo.getDomain()));

        IdentityProviderPOSTRequest idpRequest = new IdentityProviderPOSTRequest();
        idpRequest.setName(trustedIdpName);
        idpRequest.setAlias(trustedIdpAlias);
        idpRequest.setIdpIssuerName(getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        idpRequest.setCertificate(certificate);

        return idpMgtRestClient.createIdentityProvider(idpRequest);
    }

    private ApplicationResponseModel createSubjectApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        // Add trustedIdpAlias as an additional audience so the issued JWT's aud claim
        // contains this alias, satisfying the trusted-token-issuer IDP's audience validation
        // during token exchange.
        IdTokenConfiguration idTokenConfig = new IdTokenConfiguration();
        idTokenConfig.addAudienceItem(trustedIdpAlias);
        oidcConfig.setIdToken(idTokenConfig);

        // Enable subject-token issuance so this app's access tokens can serve as subject tokens
        // in a token exchange request.
        oidcConfig.setSubjectToken(new SubjectTokenConfiguration().enable(true)
                .applicationSubjectTokenExpiryInSeconds(3600));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(subjectAppName);
        application.setIsManagementApp(true);

        String appId = addApplication(application);
        return getApplication(appId);
    }

    private ApplicationResponseModel createTokenExchangeApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, TOKEN_EXCHANGE_GRANT_TYPE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(exchangeAppName);
        application.setIsManagementApp(true);

        String appId = addApplication(application);
        return getApplication(appId);
    }
}
