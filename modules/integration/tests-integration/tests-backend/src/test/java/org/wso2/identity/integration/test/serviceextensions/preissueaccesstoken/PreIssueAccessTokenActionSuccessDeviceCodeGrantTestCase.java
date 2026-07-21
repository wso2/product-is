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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
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
import org.wso2.identity.integration.test.serviceextensions.model.UserStore;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Integration test class for testing the pre issue access token action success scenarios with device code grant.
 * This test case extends {@link ActionsBaseTestCase} and focuses on scenarios related
 * to scopes and claims modifications through an external service.
 */
public class PreIssueAccessTokenActionSuccessDeviceCodeGrantTestCase extends ActionsBaseTestCase {

    private static final String USERS = "users";
    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String TEST_USER_GIVEN = "test_user_given";
    private static final String TEST_USER_GMAIL_COM = "test.user@gmail.com";
    private static final String EXTERNAL_SERVICE_NAME = "TestExternalService";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";

    private static final String DEVICE_CODE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";
    private static final String TEST_ROLE_APPLICATION = "test_role_application";
    private static final String OPENID_SCOPE = "openid";
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
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String DEVICE_CODE_PARAM = "device_code";
    private static final String USER_CODE_PARAM = "user_code";
    private static final int UPDATED_EXPIRY_TIME_PERIOD = 7200;
    private static final int CURRENT_EXPIRY_TIME_PERIOD = 3600;

    private SCIM2RestClient scim2RestClient;
    private final CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
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
    private String userId;
    private String roleId;
    private final String tenantId;
    private String deviceCode;
    private String userCode;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private JWTClaimsSet jwtClaims;
    private final TestUserMode userMode;
    private ServiceExtensionMockServer serviceExtensionMockServer;

    private String deviceAuthEndpoint;
    private String deviceAuthPageEndpoint;
    private String deviceEndpoint;
    private String tokenEndpoint;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionSuccessDeviceCodeGrantTestCase(TestUserMode testUserMode) {

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
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        customScopes = Arrays.asList(CUSTOM_SCOPE_1, CUSTOM_SCOPE_2, CUSTOM_SCOPE_3);

        ApplicationResponseModel application = addApplicationWithGrantType(DEVICE_CODE_GRANT_TYPE);
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
        addUserWithRole(applicationId, customScopes);

        requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,
                INTERNAL_ORG_USER_MANAGEMENT_LIST,
                INTERNAL_ORG_USER_MANAGEMENT_VIEW,
                INTERNAL_ORG_USER_MANAGEMENT_CREATE,
                INTERNAL_ORG_USER_MANAGEMENT_UPDATE,
                INTERNAL_ORG_USER_MANAGEMENT_DELETE,
                OPENID_SCOPE);
        requestedScopes.addAll(customScopes);

        setServerEndpoints();

        actionId = createPreIssueAccessTokenAction();

        serviceExtensionMockServer = new ServiceExtensionMockServer();
        serviceExtensionMockServer.startServer();
        serviceExtensionMockServer.setupStub(MOCK_SERVER_ENDPOINT_RESOURCE_PATH,
                "Basic " + getBase64EncodedString(MOCK_SERVER_AUTH_BASIC_USERNAME, MOCK_SERVER_AUTH_BASIC_PASSWORD),
                FileUtils.readFileInClassPathAsString("actions/response/pre-issue-access-token-response.json"));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        serviceExtensionMockServer.stopServer();

        deleteAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId);
        deleteRole(roleId);
        deleteApp(applicationId);
        deleteDomainAPI(domainAPIId);
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        cookieStore.clear();
        client.close();

        serviceExtensionMockServer = null;
        accessToken = null;
        jwtClaims = null;
    }

    @Test(groups = "wso2.is", description = "Send device authorize request to get device code and user code")
    public void testSendDeviceAuthorize() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        org.json.simple.JSONObject responseObject = responseObjectNew(urlParameters, deviceAuthEndpoint);
        deviceCode = responseObject.get(DEVICE_CODE_PARAM).toString();
        userCode = responseObject.get(USER_CODE_PARAM).toString();
        Assert.assertNotNull(deviceCode, "device_code is null");
        Assert.assertNotNull(userCode, "user_code is null");
    }

    @Test(groups = "wso2.is", description = "Send user code to device authorization page",
            dependsOnMethods = "testSendDeviceAuthorize")
    public void testSendDeviceAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE_PARAM, userCode));
        String response = responsePost(urlParameters, deviceAuthPageEndpoint);
        Assert.assertNotNull(response, "Authorized response is null");
    }

    @Test(groups = "wso2.is", description = "Submit user code to device endpoint",
            dependsOnMethods = "testSendDeviceAuthorizedPost")
    public void testDevicePost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(USER_CODE_PARAM, userCode));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, deviceEndpoint);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(response);
        Assert.assertNotNull(locationHeader, "Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request",
            dependsOnMethods = "testDevicePost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, ADMIN_WSO2);
        Assert.assertNotNull(response, "Login request failed. Login response is null.");

        if (Utils.requestMissingClaims(response)) {
            Assert.assertTrue(response.getFirstHeader("Set-Cookie").getValue().contains("pastr"),
                    "pastr cookie not found in response.");
            String pastreCookie = response.getFirstHeader("Set-Cookie").getValue().split(";")[0];
            EntityUtils.consume(response.getEntity());

            response = Utils.sendPOSTConsentMessage(response,
                    getTenantQualifiedURL(COMMON_AUTH_URL, tenantInfo.getDomain()),
                    USER_AGENT, Utils.getRedirectUrl(response), client, pastreCookie);
            EntityUtils.consume(response.getEntity());
        }
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + OAuth2Constant.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");
        sessionDataKeyConsent = keyValues.get(0).getValue();
        EntityUtils.consume(response.getEntity());

        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request",
            dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Get access token with device code grant",
            dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessTokenWithDeviceCodeGrant() throws Exception {

        // Wait 5 seconds because of the token polling interval.
        Thread.sleep(5000);
        org.json.simple.JSONObject obj = sendTokenRequest(DEVICE_CODE_GRANT_TYPE, clientId, deviceCode);
        Assert.assertNotNull(obj, "Token response is null.");

        String tokenValue = obj.get("access_token").toString();
        Assert.assertNotNull(tokenValue, "Access token is null.");
        accessToken = tokenValue;

        jwtClaims = extractJwtClaims(accessToken);
        assertNotNull(jwtClaims);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testGetAccessTokenWithDeviceCodeGrant", description =
            "Verify the pre issue access token action request")
    public void testPreIssueAccessTokenActionRequest() throws Exception {

        String actualRequestPayload =
                serviceExtensionMockServer.getReceivedRequestPayload(MOCK_SERVER_ENDPOINT_RESOURCE_PATH);
        PreIssueAccessTokenActionRequest actualRequest =
                new ObjectMapper().readValue(actualRequestPayload, PreIssueAccessTokenActionRequest.class);

        PreIssueAccessTokenActionRequest expectedRequest = getRequest();

        assertEquals(actualRequest, expectedRequest);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the updated scopes in the access token")
    public void testTokenScopeOperations() throws Exception {

        String[] scopes = jwtClaims.getStringClaim("scope").split("\\s+");

        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_1));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_2));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_3));
        Assert.assertTrue(ArrayUtils.contains(scopes, NEW_SCOPE_4));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_DELETE));
        Assert.assertFalse(ArrayUtils.contains(scopes, INTERNAL_ORG_USER_MANAGEMENT_CREATE));
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the updated aud claims in the access token")
    public void testTokenAUDClaimOperations() throws Exception {

        String[] audValueArray = jwtClaims.getStringArrayClaim("aud");

        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz1.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz2.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzz3.com"));
        Assert.assertTrue(ArrayUtils.contains(audValueArray, "zzzR.com"));
        Assert.assertFalse(ArrayUtils.contains(audValueArray, clientId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the specified custom string claim in the access token")
    public void testTokenStringClaimAddOperation() throws Exception {

        String claimStr = jwtClaims.getStringClaim("custom_claim_string_1");
        Assert.assertEquals(claimStr, "testCustomClaim1");
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the specified custom number claim in the access token")
    public void testTokenNumberClaimAddOperation() throws Exception {

        Number claimValue = jwtClaims.getIntegerClaim("custom_claim_number_1");
        Assert.assertEquals(claimValue, 78);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the specified custom boolean claim in the access token")
    public void testTokenBooleanClaimAddOperation() throws Exception {

        Boolean claimValue = jwtClaims.getBooleanClaim("custom_claim_boolean_1");
        Assert.assertTrue(claimValue);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the presence of the specified custom string array claim in the access token")
    public void testTokenStringArrayClaimAddOperation() throws Exception {

        String[] claimArray1 = {"TestCustomClaim1", "TestCustomClaim2", "TestCustomClaim3"};

        String[] claimArray = jwtClaims.getStringArrayClaim("custom_claim_string_array_1");
        Assert.assertEquals(claimArray, claimArray1);
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testPreIssueAccessTokenActionRequest", description =
            "Verify the replacement of the 'expires_in' claim in the access token")
    public void testTokenExpiresInClaimReplaceOperation() throws Exception {

        Date exp = jwtClaims.getDateClaim("exp");
        Date iat = jwtClaims.getDateClaim("iat");
        long expiresIn = (exp.getTime() - iat.getTime()) / 1000;

        Assert.assertEquals(expiresIn, UPDATED_EXPIRY_TIME_PERIOD);
    }

    /**
     * Sets the device flow server endpoints based on the tenant context.
     */
    private void setServerEndpoints() {

        deviceAuthEndpoint = getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT.replace("oauth2/token", "oauth2/device_authorize"),
                tenantInfo.getDomain());
        deviceAuthPageEndpoint = getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT.replace("oauth2/token",
                        "authenticationendpoint/device.do"),
                tenantInfo.getDomain());
        deviceEndpoint = getTenantQualifiedURL(
                OAuth2Constant.ACCESS_TOKEN_ENDPOINT.replace("oauth2/token", "oauth2/device"),
                tenantInfo.getDomain());
        tokenEndpoint = getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
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
        User user = new User(userId);
        UserStore userStore =
                new UserStore(Base64.getEncoder().encodeToString("PRIMARY".getBytes(StandardCharsets.UTF_8)),
                        "PRIMARY");

        PreIssueAccessTokenEvent event = new PreIssueAccessTokenEvent.Builder()
                .request(tokenRequest)
                .accessToken(accessTokenInRequest)
                .tenant(tenant)
                .organization(null)
                .user(user)
                .userStore(userStore)
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
     * Creates token request for device code grant.
     *
     * @return token request object
     */
    private TokenRequest createTokenRequest() {

        return new TokenRequest.Builder()
                .grantType(DEVICE_CODE_GRANT_TYPE)
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
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUTHORIZED_USER_TYPE.getName(), "APPLICATION_USER"));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.EXPIRES_IN.getName(), CURRENT_EXPIRY_TIME_PERIOD));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.AUD.getName(), Collections.singletonList(clientId)));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUBJECT_TYPE.getName(), subjectType));
        claims.add(new AccessToken.Claim(AccessToken.ClaimNames.SUB.getName(), userId));

        return new AccessToken.Builder()
                .tokenType(tokenType)
                .claims(claims)
                .scopes(requestedScopes)
                .build();
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
     * Creates an action for pre-issuing an access token with basic authentication.
     *
     * @return ID of the created action
     */
    private String createPreIssueAccessTokenAction() {

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

        try {
            return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating pre issue access token action " +
                    actionModel.getName(), e);
        }
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
     * Adds a user with a role and specific permissions based on custom scopes.
     *
     * @param appID        Application ID to which the role is associated
     * @param customScopes The custom scopes based on which permissions are added
     * @throws Exception If there is an error during user or role creation
     */
    private void addUserWithRole(String appID, List<String> customScopes) throws Exception {

        // Creates roles
        List<Permission> permissions = addPermissions(customScopes);
        Audience roleAudience = new Audience(APPLICATION_AUDIENCE, appID);
        RoleV2 role = new RoleV2(roleAudience, TEST_ROLE_APPLICATION, permissions, Collections.emptyList());
        roleId = addRole(role);

        // Creates user
        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN));
        userInfo.addEmail(new Email().value(TEST_USER_GMAIL_COM));
        userId = scim2RestClient.createUser(userInfo);

        // Assigns role to the created user
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(userId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);
    }

    /**
     * Adds permissions based on the provided custom scopes.
     *
     * @param customScopes A list of custom scopes to add as permissions
     * @return A list of permissions including both predefined and custom scope-based permissions
     */
    private List<Permission> addPermissions(List<String> customScopes) {

        List<Permission> userPermissions = new ArrayList<>();

        Collections.addAll(userPermissions,
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_LIST),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_VIEW),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_CREATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_UPDATE),
                new Permission(INTERNAL_ORG_USER_MANAGEMENT_DELETE)
        );

        customScopes.forEach(scope -> userPermissions.add(new Permission(scope)));

        return userPermissions;
    }

    /**
     * Sends a token request with device code grant parameters.
     *
     * @param grantType  Grant type
     * @param clientId   Client ID
     * @param deviceCode Device code
     * @return JSON response object
     * @throws IOException If an error occurred while sending the request
     */
    private org.json.simple.JSONObject sendTokenRequest(String grantType, String clientId, String deviceCode)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        if (grantType != null) {
            urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME, grantType));
        }
        if (clientId != null) {
            urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, clientId));
        }
        if (deviceCode != null) {
            urlParameters.add(new BasicNameValuePair(DEVICE_CODE_PARAM, deviceCode));
        }

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader(CommonConstants.USER_AGENT_HEADER, USER_AGENT);
        request.setHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(this.clientId, this.clientSecret));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);

        try (BufferedReader responseBuffer =
                     new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            return (org.json.simple.JSONObject) org.json.simple.JSONValue.parse(responseBuffer);
        }
    }

    /**
     * Sends a POST request and parses the response as a JSON object.
     *
     * @param postParameters POST parameters
     * @param uri            Target URI
     * @return JSON response object
     * @throws Exception If an error occurred while sending the request
     */
    private org.json.simple.JSONObject responseObjectNew(List<NameValuePair> postParameters, String uri)
            throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response");
        }

        return json;
    }

    /**
     * Sends a POST request and returns the response body as a string.
     *
     * @param postParameters POST parameters
     * @param uri            Target URI
     * @return Response body as string
     * @throws Exception If an error occurred while sending the request
     */
    private String responsePost(List<NameValuePair> postParameters, String uri) throws Exception {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        return responseString;
    }
}
