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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
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
import org.wso2.identity.integration.test.serviceextensions.mockservices.ServiceExtensionMockServer;
import org.wso2.identity.integration.test.serviceextensions.model.ActionType;
import org.wso2.identity.integration.test.serviceextensions.model.AllowedOperation;
import org.wso2.identity.integration.test.serviceextensions.model.IDToken;
import org.wso2.identity.integration.test.serviceextensions.model.Operation;
import org.wso2.identity.integration.test.serviceextensions.model.Organization;
import org.wso2.identity.integration.test.serviceextensions.model.PreIssueIDTokenActionRequest;
import org.wso2.identity.integration.test.serviceextensions.model.PreIssueIDTokenEvent;
import org.wso2.identity.integration.test.serviceextensions.model.Tenant;
import org.wso2.identity.integration.test.serviceextensions.model.TokenRequest;
import org.wso2.identity.integration.test.serviceextensions.model.User;
import org.wso2.identity.integration.test.serviceextensions.model.UserStore;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration test class for testing the pre issue ID token flow with OIDC Hybrid flow.
 * This test case extends {@link ActionsBaseTestCase} and focuses on scenarios related
 * to claims modifications through an external service for hybrid flow response types.
 * Tests both "code id_token" and "code id_token token" response types.
 */
public class PreIssueIDTokenActionSuccessOIDCHybridFlowTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String TEST_USER_FIRST_NAME = "test_user_given_name";
    private static final String REPLACED_FIRST_NAME = "replaced_given_name";
    private static final String TEST_USER_LAST_NAME = "test_user_last_name";
    private static final String TEST_USER_GMAIL_COM = "test.user@gmail.com";
    private static final String PRE_ISSUE_ID_TOKEN_API_PATH = "preIssueIdToken";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN = "code id_token";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN = "code id_token token";
    private static final String FIRST_NAME_CLAIM = "given_name";
    private static final String LAST_NAME_CLAIM = "family_name";
    private static final String USER_NAME_CLAIM = "username";
    private static final String EXPIRES_IN_CLAIM = "expires_in";
    private static final String CLAIMS_PATH_PREFIX = "/idToken/claims/";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private static final String OPENID_SCOPE = "openid";
    private static final String PROFILE_SCOPE = "profile";

    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;
    private List<String> requestedScopes;
    private String idToken;
    private String clientId;
    private String actionId;
    private String applicationId;
    private String userId;
    private final String tenantId;
    private JWTClaimsSet idTokenJwtClaims;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;
    private final String responseType;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueIDTokenActionSuccessOIDCHybridFlowTestCase(TestUserMode testUserMode, String responseType) {

        this.userMode = testUserMode;
        this.responseType = responseType;
        this.tenantId = testUserMode == TestUserMode.SUPER_TENANT_USER ? "-1234" : "1";
    }

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN},
                {TestUserMode.SUPER_TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN},
                {TestUserMode.TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN},
                {TestUserMode.TENANT_USER, RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN}
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

        addUser();

        requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes, OPENID_SCOPE, PROFILE_SCOPE);

        actionId = createPreIssueIDTokenAction();

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-issue-id-token-response.json"));
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
        idToken = null;
        idTokenJwtClaims = null;
    }

    @Test(groups = "wso2.is", description = "Get ID token with OIDC hybrid flow")
    public void testGetIDTokenWithHybridFlow() throws Exception {

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

        response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, ADMIN_WSO2);

        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with tokens is null.");
        EntityUtils.consume(response.getEntity());

        String locationValue = locationHeader.getValue();
        String authorizationCode = DataExtractUtil
                .extractParamFromURIFragment(locationValue, OAuth2Constant.AUTHORIZATION_CODE_NAME);
        assertNotNull(authorizationCode, "Authorization code is null");

        idToken = DataExtractUtil.extractParamFromURIFragment(locationValue, OAuth2Constant.ID_TOKEN);
        assertNotNull(idToken, "ID token is null");

        // For "code id_token token" response type, access token should also be present
        if (RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN.equals(responseType)) {
            String accessToken = DataExtractUtil.extractParamFromURIFragment(
                    locationValue, OAuth2Constant.ACCESS_TOKEN);
            assertNotNull(accessToken, "Access token is null for code id_token token response type");
        }

        idTokenJwtClaims = extractJwtClaims(idToken);
        assertNotNull(idTokenJwtClaims);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testGetIDTokenWithHybridFlow", description =
            "Verify the pre issue ID token action request")
    public void testPreIssueIDTokenActionRequest() throws Exception {

        String actualRequestPayload =
                serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreIssueIDTokenActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreIssueIDTokenActionRequest.class);

        PreIssueIDTokenActionRequest expectedRequest = getRequest();
        assertEquals(actualRequest.getActionType(), expectedRequest.getActionType());
        assertEquals(actualRequest.getAllowedOperations(), expectedRequest.getAllowedOperations());
        assertEquals(actualRequest.getEvent().getRequest(), expectedRequest.getEvent().getRequest());
        assertEquals(actualRequest.getEvent().getTenant(), expectedRequest.getEvent().getTenant());
        assertNotNull(actualRequest.getEvent().getOrganization());
        assertEquals(actualRequest.getEvent().getUser(), expectedRequest.getEvent().getUser());
        assertEquals(actualRequest.getEvent().getUserStore(), expectedRequest.getEvent().getUserStore());
        assertIDToken(actualRequest.getEvent().getIdToken(), expectedRequest.getEvent().getIdToken());
    }

    @Test(groups = "wso2.is", description = "Verify that the ID token contains the updated OIDC claims " +
            "after action execution", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenOIDCScopeOperations() throws Exception {

        String firstName = idTokenJwtClaims.getStringClaim(FIRST_NAME_CLAIM);
        String lastName = idTokenJwtClaims.getStringClaim(LAST_NAME_CLAIM);
        Assert.assertEquals(firstName, REPLACED_FIRST_NAME);
        Assert.assertNotEquals(firstName, TEST_USER_FIRST_NAME);
        Assert.assertNull(lastName);
    }

    @Test(groups = "wso2.is", description = "Verify that the ID token contains the updated 'aud' claims " +
            "after action execution", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenAUDClaimOperations() throws Exception {

        String[] audValueArray = idTokenJwtClaims.getStringArrayClaim("aud");
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string claim in the ID " +
            "token", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenStringClaimAddOperation() throws Exception {

        String claimStr = idTokenJwtClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimStr, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom number claim in the ID " +
            "token", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenNumberClaimAddOperation() throws Exception {

        Number claimValue = idTokenJwtClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom boolean claim in the ID " +
            "token", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenBooleanClaimAddOperation() throws Exception {

        Boolean claimValue = idTokenJwtClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", description = "Verify the presence of the specified custom string array claim in the " +
            "ID token", dependsOnMethods = "testGetIDTokenWithHybridFlow")
    public void testTokenStringArrayClaimAddOperation() throws Exception {

        String[] expectedClaimArrayInToken = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};
        String[] addedClaimArrayToToken = idTokenJwtClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(addedClaimArrayToToken, expectedClaimArrayInToken);
    }


    /**
     * Retrieves pre issue ID token action request.
     *
     * @return pre issue ID token request object
     */
    private PreIssueIDTokenActionRequest getRequest() {

        TokenRequest tokenRequest = createTokenRequest();
        IDToken idTokenInRequest = createIDToken();

        Tenant tenant = new Tenant(tenantId, tenantInfo.getDomain());

        Organization organization = new Organization(
                "b6fd775d-b2ef-4a50-9b56-5650fd16833b",
                tenantInfo.getDomain(),
                tenantInfo.getDomain(),
                0
        );

        User user = new User.Builder()
                .id(userId)
                .organization(organization)
                .userType("LOCAL")
                .build();

        UserStore userStore =
                new UserStore(Base64.getEncoder().encodeToString("PRIMARY".getBytes(StandardCharsets.UTF_8)),
                        "PRIMARY");

        PreIssueIDTokenEvent event = new PreIssueIDTokenEvent.Builder()
                .request(tokenRequest)
                .idToken(idTokenInRequest)
                .tenant(tenant)
                .organization(organization)
                .user(user)
                .userStore(userStore)
                .build();

        List<AllowedOperation> allowedOperations = Arrays.asList(
                createAllowedOperation(Operation.ADD, Arrays.asList(CLAIMS_PATH_PREFIX,
                        CLAIMS_PATH_PREFIX + IDToken.ClaimNames.AUD.getName() + "/")),
                createAllowedOperation(Operation.REMOVE, Arrays.asList(CLAIMS_PATH_PREFIX + FIRST_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + LAST_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + USER_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + IDToken.ClaimNames.AUD.getName() + "/")),
                createAllowedOperation(Operation.REPLACE, Arrays.asList(CLAIMS_PATH_PREFIX + FIRST_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + LAST_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + USER_NAME_CLAIM,
                        CLAIMS_PATH_PREFIX + IDToken.ClaimNames.AUD.getName() + "/",
                        CLAIMS_PATH_PREFIX + EXPIRES_IN_CLAIM))
        );

        return new PreIssueIDTokenActionRequest.Builder()
                .actionType(ActionType.PRE_ISSUE_ID_TOKEN)
                .requestId("request-id-1234")
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
                .responseType(responseType)
                .scopes(requestedScopes)
                .clientId(clientId)
                .build();
    }

    /**
     * Creates ID token.
     *
     * @return ID token object
     */
    private IDToken createIDToken() {

        List<IDToken.Claim> claims = new ArrayList<>();
        claims.add(new IDToken.Claim(IDToken.ClaimNames.JTI.getName(), "test-jti-value"));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.ISS.getName(),
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain())));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.AZP.getName(), clientId));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.AMR.getName(), Collections.singletonList("BasicAuthenticator")));
        claims.add(new IDToken.Claim("org_id", "10084a8d-113f-4211-a0d5-efe36b082211"));
        claims.add(new IDToken.Claim("org_name", "Super"));
        claims.add(new IDToken.Claim("org_handle", tenantInfo.getDomain()));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.AT_HASH.getName(), "test-at-hash"));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.SUB.getName(), userId));
        claims.add(new IDToken.Claim(FIRST_NAME_CLAIM, TEST_USER_FIRST_NAME));
        claims.add(new IDToken.Claim(LAST_NAME_CLAIM, TEST_USER_LAST_NAME));
        claims.add(new IDToken.Claim(USER_NAME_CLAIM, TEST_USER));
        claims.add(new IDToken.Claim(IDToken.ClaimNames.AUD.getName(), Collections.singletonList(clientId)));
        claims.add(new IDToken.Claim(EXPIRES_IN_CLAIM, 3600));

        return new IDToken(new ArrayList<>(), claims);
    }

    /**
     * Creates allowed operations.
     *
     * @param op    operations allowed
     * @param paths operation paths to be modified
     * @return allowed operations object
     */
    private AllowedOperation createAllowedOperation(Operation op, List<String> paths) {

        AllowedOperation operation = new AllowedOperation();
        operation.setOp(op);
        operation.setPaths(new ArrayList<>(paths));
        return operation;
    }

    /**
     * Creates an action for pre-issuing an ID token with basic authentication.
     *
     * @return ID of the created action
     * @throws IOException If an error occurred while creating the action
     */
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
        actionModel.setName("ID Token Pre Issue - Hybrid Flow");
        actionModel.setDescription("This is a test pre issue ID token type for hybrid flow");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ID_TOKEN_API_PATH, actionModel);
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
     * Adds a test user.
     *
     * @throws Exception If there is an error during user creation
     */
    private void addUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_FIRST_NAME));
        userInfo.getName().setFamilyName(TEST_USER_LAST_NAME);
        userInfo.addEmail(new Email().value(TEST_USER_GMAIL_COM));
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

    private void assertIDToken(IDToken actualToken, IDToken expectedToken) {

        Map<String, Object> expectedClaimsMap = new HashMap<>();
        for (IDToken.Claim claim : expectedToken.getClaims()) {
            expectedClaimsMap.put(claim.getName(), claim.getValue());
        }

        Map<String, Object> actualClaimsMap = new HashMap<>();
        for (IDToken.Claim claim : actualToken.getClaims()) {
            actualClaimsMap.put(claim.getName(), claim.getValue());
        }

        // Assert claims that should match exactly
        assertEquals(actualClaimsMap.get(IDToken.ClaimNames.ISS.getName()),
                expectedClaimsMap.get(IDToken.ClaimNames.ISS.getName()),
                "ISS claim mismatch");

        assertEquals(actualClaimsMap.get(IDToken.ClaimNames.AZP.getName()),
                expectedClaimsMap.get(IDToken.ClaimNames.AZP.getName()),
                "AZP claim mismatch");

        assertEquals(actualClaimsMap.get(IDToken.ClaimNames.AMR.getName()),
                expectedClaimsMap.get(IDToken.ClaimNames.AMR.getName()),
                "AMR claim mismatch");

        assertEquals(actualClaimsMap.get(IDToken.ClaimNames.SUB.getName()),
                expectedClaimsMap.get(IDToken.ClaimNames.SUB.getName()),
                "SUB claim mismatch");

        assertEquals(actualClaimsMap.get(FIRST_NAME_CLAIM),
                expectedClaimsMap.get(FIRST_NAME_CLAIM),
                "Given name claim mismatch");

        assertEquals(actualClaimsMap.get(LAST_NAME_CLAIM),
                expectedClaimsMap.get(LAST_NAME_CLAIM),
                "Family name claim mismatch");

        assertEquals(actualClaimsMap.get(USER_NAME_CLAIM),
                expectedClaimsMap.get(USER_NAME_CLAIM),
                "Username claim mismatch");

        assertEquals(actualClaimsMap.get(IDToken.ClaimNames.AUD.getName()),
                expectedClaimsMap.get(IDToken.ClaimNames.AUD.getName()),
                "AUD claim mismatch");

        assertEquals(actualClaimsMap.get(EXPIRES_IN_CLAIM),
                expectedClaimsMap.get(EXPIRES_IN_CLAIM),
                "Expires in claim mismatch");

        assertEquals(actualClaimsMap.get("org_handle"),
                expectedClaimsMap.get("org_handle"),
                "Organization handle claim mismatch");

        // Assert that test-value claims exist in actual token (values are generated, so just check not null)
        assertNotNull(actualClaimsMap.get(IDToken.ClaimNames.JTI.getName()),
                "JTI claim should be present");

        // AT hash claim should be present only for "code id_token token" response type as both idtoken and
        // access token are issued together.
        if (RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN.equals(responseType)) {
            assertNotNull(actualClaimsMap.get(IDToken.ClaimNames.AT_HASH.getName()),
                    "AT hash claim should be present");
        }

        assertNotNull(actualClaimsMap.get("org_id"),
                "Organization ID claim should be present");

        assertNotNull(actualClaimsMap.get("org_name"),
                "Organization name claim should be present");
    }
}

