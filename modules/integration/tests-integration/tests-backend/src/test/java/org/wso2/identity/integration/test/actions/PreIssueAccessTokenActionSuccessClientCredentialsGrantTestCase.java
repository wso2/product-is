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

package org.wso2.identity.integration.test.actions;

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
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.actions.model.AccessToken;
import org.wso2.identity.integration.test.actions.model.ActionType;
import org.wso2.identity.integration.test.actions.model.AllowedOperation;
import org.wso2.identity.integration.test.actions.model.Operation;
import org.wso2.identity.integration.test.actions.model.PreIssueAccessTokenActionRequest;
import org.wso2.identity.integration.test.actions.model.PreIssueAccessTokenEvent;
import org.wso2.identity.integration.test.actions.model.Tenant;
import org.wso2.identity.integration.test.actions.model.TokenRequest;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;

/**
 * Integration test class for testing the pre issue access token flow with client credentials grant.
 * This test case extends {@link ActionsBaseTestCase} and focuses on scenarios related
 * to scopes and claims modifications through an external service.
 */
public class PreIssueAccessTokenActionSuccessClientCredentialsGrantTestCase extends ActionsBaseTestCase {

    private static final String EXTERNAL_SERVICE_NAME = "TestExternalService";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";

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

    private static final int UPDATED_EXPIRY_TIME_PERIOD = 7200;
    private static final int CURRENT_EXPIRY_TIME_PERIOD = 3600;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private List<String> customScopes;
    private List<String> requestedScopes;
    private String accessToken;
    private String clientId;
    private String clientSecret;
    private String subjectType;
    private String tokenType;
    private String actionId;
    private String applicationId;
    private String domainAPIId;
    private String tenantId;
    private JWTClaimsSet jwtClaims;
    private TestUserMode userMode;
    private ActionsMockServer actionsMockServer;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionSuccessClientCredentialsGrantTestCase(TestUserMode testUserMode) {

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

    /**
     * Initializes Test environment and sets up necessary configurations.
     *
     * @throws Exception If an error occurs during initialization
     */
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

        customScopes = Arrays.asList(CUSTOM_SCOPE_1, CUSTOM_SCOPE_2, CUSTOM_SCOPE_3);

        ApplicationResponseModel application = addApplicationWithGrantType(CLIENT_CREDENTIALS_GRANT_TYPE);
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();
        subjectType = oidcConfig.getSubject().getSubjectType();
        tokenType = oidcConfig.getAccessToken().getType();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
        }
        domainAPIId = createDomainAPI(EXTERNAL_SERVICE_NAME, EXTERNAL_SERVICE_URI, customScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, customScopes);

        requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,
                INTERNAL_ORG_USER_MANAGEMENT_LIST,
                INTERNAL_ORG_USER_MANAGEMENT_VIEW,
                INTERNAL_ORG_USER_MANAGEMENT_CREATE,
                INTERNAL_ORG_USER_MANAGEMENT_UPDATE,
                INTERNAL_ORG_USER_MANAGEMENT_DELETE);
        requestedScopes.addAll(customScopes);

        actionId = createPreIssueAccessTokenAction();

        actionsMockServer = new ActionsMockServer();
        actionsMockServer.startServer();
        actionsMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-issue-access-token-response.json"));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        actionsMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteApp(applicationId);
        deleteDomainAPI(domainAPIId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        actionsMockServer = null;
        accessToken = null;
        jwtClaims = null;
    }

    @Test(groups = "wso2.is", description = "Get access token with client credentials grant")
    public void testGetAccessTokenWithClientCredentialsGrant() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

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

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        jwtClaims = extractJwtClaims(accessToken);
        assertNotNull(jwtClaims);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant", description =
            "Verify the pre issue access token action request")
    public void testPreIssueAccessTokenActionRequest() throws Exception {

        String actualRequestPayload = actionsMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreIssueAccessTokenActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreIssueAccessTokenActionRequest.class);

        PreIssueAccessTokenActionRequest expectedRequest = getRequest();

        assertEquals(actualRequest, expectedRequest);
    }

    @Test(groups = "wso2.is", description = "Verify that the access token contains the updated scopes " +
            "after action execution", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenScopeOperations() throws Exception {

        String[] scopes = jwtClaims.getStringClaim("scope").split("\\s+");

        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_1));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_2));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_3));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_4));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_DELETE));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_CREATE));
    }

    @Test(groups = "wso2.is", description = "Verify that the access token contains the updated 'aud' claims " +
            "after action execution", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenAUDClaimOperations() throws Exception {

        String[] audValueArray = jwtClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string claim in the access " +
            "token", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenStringClaimAddOperation() throws Exception {

        String claimStr = jwtClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimStr, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom number claim in the access " +
            "token", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenNumberClaimAddOperation() throws Exception {

        Number claimValue = jwtClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom boolean claim in the access " +
            "token", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenBooleanClaimAddOperation() throws Exception {

        Boolean claimValue = jwtClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string array claim in the " +
            "access token", dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenStringArrayClaimAddOperation()
            throws Exception {

        String[] expectedClaimArrayInToken = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};

        String[] addedClaimArrayToToken = jwtClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(addedClaimArrayToToken, expectedClaimArrayInToken);
    }

    @Test(groups = "wso2.is", description = "Verify the replacement of the 'expires_in' claim in the access token",
            dependsOnMethods = "testGetAccessTokenWithClientCredentialsGrant")
    public void testTokenExpiresInClaimReplaceOperation() throws Exception {

        Date exp = jwtClaims.getDateClaim("exp");
        Date iat = jwtClaims.getDateClaim("iat");
        long expiresIn = (exp.getTime() - iat.getTime()) / 1000;

        Assert.assertEquals(expiresIn, UPDATED_EXPIRY_TIME_PERIOD);
    }

    /**
     * Retrieves pre issue access token action request.
     *
     * @return pre issue access token request object
     */
    private PreIssueAccessTokenActionRequest getRequest() {

        TokenRequest tokenRequest = createTokenRequest();
        AccessToken accessTokenInRequest = createAccessToken();

        Tenant tenant = new Tenant(tenantId, tenantInfo.getDomain());

        PreIssueAccessTokenEvent event = new PreIssueAccessTokenEvent.Builder()
                .request(tokenRequest)
                .accessToken(accessTokenInRequest)
                .tenant(tenant)
                .organization(null)
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
     * Creates token request.
     *
     * @return token request object
     */
    private TokenRequest createTokenRequest() {

        return new TokenRequest.Builder()
                .grantType(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS)
                .scopes(requestedScopes)
                .clientId(clientId)
                .build();
    }

    /**
     * Creates access token.
     *
     * @return access token object
     */
    private AccessToken createAccessToken() {

        List<AccessToken.Claim> claims = new ArrayList<>();
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.ISS.getName(),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.CLIENT_ID.getName(), clientId));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUTHORIZED_USER_TYPE.getName(), "APPLICATION"));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.EXPIRES_IN.getName(), CURRENT_EXPIRY_TIME_PERIOD));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUD.getName(), Collections.singletonList(clientId)));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUBJECT_TYPE.getName(), subjectType));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUB.getName(), clientId));

        return new AccessToken.Builder()
                .tokenType(tokenType)
                .claims(claims)
                .scopes(requestedScopes)
                .build();
    }

    private AllowedOperation createAllowedOperation(Operation op, List<String> paths) {

        AllowedOperation operation = new AllowedOperation();
        operation.setOp(op);
        operation.setPaths(new ArrayList<>(paths));
        return operation;
    }

    /**
     * Creates an action for pre-issuing an access token with basic authentication.
     *
     * @return ID of the created action
     * @throws IOException If an error occurred while creating the action
     */
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

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
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
}
