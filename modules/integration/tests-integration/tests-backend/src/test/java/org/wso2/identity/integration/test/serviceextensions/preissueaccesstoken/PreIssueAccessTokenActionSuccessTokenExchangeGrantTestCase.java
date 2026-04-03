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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.ArrayUtils;
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
import org.testng.Assert;
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
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.AccessToken;
import org.wso2.identity.integration.test.serviceextensions.model.ActionType;
import org.wso2.identity.integration.test.serviceextensions.model.AllowedOperation;
import org.wso2.identity.integration.test.serviceextensions.model.Operation;
import org.wso2.identity.integration.test.serviceextensions.model.PreIssueAccessTokenActionRequest;
import org.wso2.identity.integration.test.serviceextensions.model.PreIssueAccessTokenEvent;
import org.wso2.identity.integration.test.serviceextensions.model.Tenant;
import org.wso2.identity.integration.test.serviceextensions.model.TokenRequest;
import org.wso2.identity.integration.test.serviceextensions.model.User;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;

/**
 * Tests the pre-issue access token action success scenarios with token exchange grant type.
 */
public class PreIssueAccessTokenActionSuccessTokenExchangeGrantTestCase extends ActionsBaseTestCase {

    private static final String EXTERNAL_SERVICE_NAME = "TestExternalService";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String SUBJECT_APP_NAME = "SubjectTokenApplication";

    private static final String TOKEN_EXCHANGE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt";
    private static final String REQUESTED_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    private static final String INTERNAL_ORG_USER_MANAGEMENT_LIST = "internal_org_user_mgt_list";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_VIEW = "internal_org_user_mgt_view";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_CREATE = "internal_org_user_mgt_create";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_UPDATE = "internal_org_user_mgt_update";
    private static final String INTERNAL_ORG_USER_MANAGEMENT_DELETE = "internal_org_user_mgt_delete";
    private static final String CUSTOM_SCOPE_1 = "test_custom_scope_1";
    private static final String CUSTOM_SCOPE_2 = "test_custom_scope_2";
    private static final String CUSTOM_SCOPE_3 = "test_custom_scope_3";
    private static final String NEW_SCOPE_1 = "new_test_custom_scope_1";
    private static final String NEW_SCOPE_2 = "new_test_custom_scope_2";
    private static final String NEW_SCOPE_3 = "new_test_custom_scope_3";
    private static final String NEW_SCOPE_4 = "replaced_scope";

    private static final String SCIM2_USERS_API = "/o/scim2/Users";
    private static final String CLAIMS_PATH_PREFIX = "/accessToken/claims/";
    private static final String SCOPES_PATH_PREFIX = "/accessToken/scopes/";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String TRUSTED_IDP_NAME = "TrustedTokenIssuer";
    private static final String TRUSTED_IDP_ALIAS = "trustedTokenAlias";
    private static final String IS_JWKS_URI = "https://localhost:9853/oauth2/jwks";

    private static final int UPDATED_EXPIRY_TIME_PERIOD = 7200;
    private static final int CURRENT_EXPIRY_TIME_PERIOD = 3600;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private IdpMgtRestClient idpMgtRestClient;
    private List<String> customScopes;
    private List<String> requestedScopes;
    private String subjectToken;
    private String accessToken;
    private String subjectAppClientId;
    private String subjectAppClientSecret;
    private String clientId;
    private String clientSecret;
    private String subjectType;
    private String tokenType;
    private String actionId;
    private String subjectApplicationId;
    private String applicationId;
    private String domainAPIId;
    private String trustedIdpId;
    private String tenantId;
    private JWTClaimsSet jwtClaims;
    private TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionSuccessTokenExchangeGrantTestCase(TestUserMode testUserMode) {

        this.userMode = testUserMode;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER},
                {TestUserMode.TENANT_USER}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
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

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        customScopes = Arrays.asList(CUSTOM_SCOPE_1, CUSTOM_SCOPE_2, CUSTOM_SCOPE_3);

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
        subjectType = oidcConfig.getSubject().getSubjectType();
        tokenType = oidcConfig.getAccessToken().getType();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
            authorizeSystemAPIs(subjectApplicationId, Collections.singletonList(SCIM2_USERS_API));
        }

        domainAPIId = createDomainAPI(EXTERNAL_SERVICE_NAME, EXTERNAL_SERVICE_URI, customScopes);

        authorizeDomainAPIs(subjectApplicationId, domainAPIId, customScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, customScopes);

        trustedIdpId = createTrustedTokenIssuerIdp();

        requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,
                INTERNAL_ORG_USER_MANAGEMENT_LIST,
                INTERNAL_ORG_USER_MANAGEMENT_VIEW,
                INTERNAL_ORG_USER_MANAGEMENT_CREATE,
                INTERNAL_ORG_USER_MANAGEMENT_UPDATE,
                INTERNAL_ORG_USER_MANAGEMENT_DELETE);
        requestedScopes.addAll(customScopes);

        actionId = createPreIssueAccessTokenAction();

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME,
                        MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-issue-access-token-response.json"));

        deactivateAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        subjectToken = obtainSubjectToken();
        activateAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        deleteApp(subjectApplicationId);
        deleteDomainAPI(domainAPIId);

        idpMgtRestClient.deleteIdp(trustedIdpId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        idpMgtRestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        serviceExtensionMockServer = null;
        subjectToken = null;
        accessToken = null;
        jwtClaims = null;
    }

    @Test(groups = "wso2.is", description = "Get subject token with client credentials grant")
    public void testGetSubjectTokenWithClientCredentialsGrant() {

        assertNotNull(subjectToken, "Subject token is null.");
    }

    @Test(groups = "wso2.is", description = "Exchange the subject token for a new access token in token exchange grant",
            dependsOnMethods = "testGetSubjectTokenWithClientCredentialsGrant")
    public void testGetAccessTokenWithTokenExchangeGrant() throws Exception {

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

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"),
                "Access token not found in the token exchange response. Response: " + responseString);
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");


        jwtClaims = extractJwtClaims(accessToken);
        assertNotNull(jwtClaims);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant", description =
            "Verify the pre issue access token action request")
    public void testPreIssueAccessTokenActionRequest() throws Exception {

        String actualRequestPayload =
                serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreIssueAccessTokenActionRequest actualRequest =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(actualRequestPayload, PreIssueAccessTokenActionRequest.class);

        PreIssueAccessTokenActionRequest expectedRequest = getRequest();

        Assert.assertEquals(actualRequest, expectedRequest);
    }

    @Test(groups = "wso2.is", description = "Verify that the access token contains the updated scopes " +
            "after action execution", dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenScopeOperations() throws Exception {

        String[] scopes = jwtClaims.getStringClaim("scope").split("\\s+");

        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_1));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_2));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_3));
        // For token exchange, the final JWT only contains action-added scopes
        // original subject token scopes are not carried over
        Assert.assertFalse(ArrayUtils.contains(scopes, NEW_SCOPE_4));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_DELETE));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_CREATE));
    }

    @Test(groups = "wso2.is", description = "Verify that the access token contains the updated 'aud' claims " +
            "after action execution", dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenAUDClaimOperations() throws Exception {

        String[] audValueArray = jwtClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", description = "Verify custom string claim added to the exchanged access token",
            dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenStringClaimAddOperation() throws Exception {

        String claimStr = jwtClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimStr, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", description = "Verify custom number claim added to the exchanged access token",
            dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenNumberClaimAddOperation() throws Exception {

        Number claimValue = jwtClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", description = "Verify custom boolean claim added to the exchanged access token",
            dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenBooleanClaimAddOperation() throws Exception {

        Boolean claimValue = jwtClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify custom string array claim added to the exchanged access token",
            dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenStringArrayClaimAddOperation() throws Exception {

        String[] expectedClaimArrayInToken = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};
        String[] addedClaimArrayToToken = jwtClaims.getStringArrayClaim("custom_claim_string_array_1");

        Assert.assertEquals(addedClaimArrayToToken, expectedClaimArrayInToken);
    }

    @Test(groups = "wso2.is", description = "Verify expires_in claim replaced in the exchanged access token",
            dependsOnMethods = "testGetAccessTokenWithTokenExchangeGrant")
    public void testTokenExpiresInClaimReplaceOperation() throws Exception {

        Date exp = jwtClaims.getDateClaim("exp");
        Date iat = jwtClaims.getDateClaim("iat");
        long expiresIn = (exp.getTime() - iat.getTime()) / 1000;

        Assert.assertEquals(expiresIn, UPDATED_EXPIRY_TIME_PERIOD);
    }


    /**
     * Builds the expected pre issue access token action request for the token exchange grant.
     *
     * @return pre issue access token request object
     */
    private PreIssueAccessTokenActionRequest getRequest() {

        TokenRequest tokenRequest = createTokenRequest();
        AccessToken accessTokenInRequest = createAccessToken();

        Tenant tenant = new Tenant(tenantId, tenantInfo.getDomain());
        // For token exchange, the user is a federated user with no local id.
        User user = new User.Builder().build();

        PreIssueAccessTokenEvent event = new PreIssueAccessTokenEvent.Builder()
                .request(tokenRequest)
                .accessToken(accessTokenInRequest)
                .tenant(tenant)
                .organization(null)
                .user(user)
                .build();

        List<AllowedOperation> allowedOperations = Arrays.asList(
                createAllowedOperation(Operation.ADD, Arrays.asList(CLAIMS_PATH_PREFIX, SCOPES_PATH_PREFIX,
                        CLAIMS_PATH_PREFIX + AccessToken.ClaimNames.AUD.getName() + "/")),
                createAllowedOperation(Operation.REMOVE, Arrays.asList(SCOPES_PATH_PREFIX,
                        CLAIMS_PATH_PREFIX + AccessToken.ClaimNames.AUD.getName() + "/")),
                createAllowedOperation(Operation.REPLACE, Arrays.asList(SCOPES_PATH_PREFIX,
                        CLAIMS_PATH_PREFIX + AccessToken.ClaimNames.AUD.getName() + "/",
                        CLAIMS_PATH_PREFIX + AccessToken.ClaimNames.EXPIRES_IN.getName()))
        );

        return new PreIssueAccessTokenActionRequest.Builder()
                .actionType(ActionType.PRE_ISSUE_ACCESS_TOKEN)
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    /**
     * Creates the token request for the token exchange grant.
     *
     * @return token request object
     */
    private TokenRequest createTokenRequest() {

        return new TokenRequest.Builder()
                .grantType(TOKEN_EXCHANGE_GRANT_TYPE)
                .scopes(requestedScopes)
                .clientId(clientId)
                .build();
    }

    /**
     * Creates the expected access token object for the token exchange grant.
     * Note: For token exchange, the server does not include scopes in the accessToken context object.
     *
     * @return access token object
     */
    private AccessToken createAccessToken() {

        List<AccessToken.Claim> claims = new ArrayList<>();
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.ISS.getName(),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.CLIENT_ID.getName(), clientId));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUTHORIZED_USER_TYPE.getName(),
                "APPLICATION_USER"));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.EXPIRES_IN.getName(), CURRENT_EXPIRY_TIME_PERIOD));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUD.getName(), Collections.singletonList(clientId)));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUBJECT_TYPE.getName(), subjectType));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUB.getName(), subjectAppClientId));

        return new AccessToken.Builder()
                .tokenType(tokenType)
                .claims(claims)
                .scopes(Collections.emptyList())
                .build();
    }

    private AllowedOperation createAllowedOperation(Operation op, List<String> paths) {

        AllowedOperation operation = new AllowedOperation();
        operation.setOp(op);
        operation.setPaths(new ArrayList<>(paths));
        return operation;
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

    /**
     * Obtains a subject JWT from the subject application via the client credentials grant.
     */
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

    /**
     * Extracts the JWT claims from the given JWT token string.
     *
     * @param jwtToken The JWT token string to extract claims from.
     * @return The extracted JWT claims set.
     * @throws ParseException If the JWT token string cannot be parsed.
     */
    private JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }


    /**
     * Creates a trusted token issuer IDP.
     *
     * @return The ID of the created identity provider.
     * @throws Exception If an error occurs while creating the identity provider.
     */
    private String createTrustedTokenIssuerIdp() throws Exception {

        IdentityProviderPOSTRequest.Certificate certificate = new IdentityProviderPOSTRequest.Certificate();
        certificate.setJwksUri(getTenantQualifiedURL(IS_JWKS_URI, tenantInfo.getDomain()));

        IdentityProviderPOSTRequest idpRequest = new IdentityProviderPOSTRequest();
        idpRequest.setName(TRUSTED_IDP_NAME);
        idpRequest.setAlias(TRUSTED_IDP_ALIAS);
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

        // Add TRUSTED_IDP_ALIAS as an additional audience so the issued JWT's aud claim
        // contains this alias, satisfying the trusted-token-issuer IDP's audience validation
        // during token exchange.
        IdTokenConfiguration idTokenConfig = new IdTokenConfiguration();
        idTokenConfig.addAudienceItem(TRUSTED_IDP_ALIAS);
        oidcConfig.setIdToken(idTokenConfig);

        // Enable subject-token issuance so this app's access tokens can serve as subject tokens in a
        // token exchange request and so that the pre-issue access token action is invoked during the exchange.
        oidcConfig.setSubjectToken(new SubjectTokenConfiguration().enable(true)
                .applicationSubjectTokenExpiryInSeconds(3600));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SUBJECT_APP_NAME);
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
        application.setName(SERVICE_PROVIDER_NAME);
        application.setIsManagementApp(true);

        String appId = addApplication(application);
        return getApplication(appId);
    }
}
