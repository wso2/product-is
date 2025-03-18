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

package org.wso2.identity.integration.test.actions;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.actions.mockserver.ActionsMockServer;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ANDRule;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Expression;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ORRule;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.FileUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Test class to cover the scenarios of pre issue access token action execution with rules at authorization code grant.
 */
public class PreIssueAccessTokenActionWithRulesAtAuthorizationCodeGrantTestCase extends ActionsBaseTestCase {

    private static final String TEST_USER = "test_user";
    private static final String ADMIN_WSO2 = "Admin@wso2";
    private static final String TEST_USER_GIVEN = "test_user_given";
    private static final String TEST_USER_GMAIL_COM = "test.user@gmail.com";
    private static final String PRE_ISSUE_ACCESS_TOKEN_API_PATH = "preIssueAccessToken";
    private static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;
    private List<String> requestedScopes;
    private String clientId;
    private String clientSecret;
    private String actionId;
    private String applicationId;
    private String userId;
    private TestUserMode userMode;
    private ActionsMockServer actionsMockServer;
    private boolean isSSOLogin = false;

    @Factory(dataProvider = "testExecutionContextProvider")
    public PreIssueAccessTokenActionWithRulesAtAuthorizationCodeGrantTestCase(TestUserMode testUserMode) {

        this.userMode = testUserMode;
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

        client = HttpClientBuilder.create()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                }).build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        ApplicationResponseModel application = addApplicationWithGrantType(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        addUser();

        requestedScopes = Arrays.asList("openid", "email", "profile");

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
        scim2RestClient.deleteUser(userId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        actionsRestClient.closeHttpClient();
        client.close();

        actionsMockServer = null;
    }

    @DataProvider(name = "rulesProvider")
    private Object[][] rulesProvider() {

        return new Object[][]{
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))), true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value(applicationId))), true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("equals")
                                .value(applicationId))
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))), true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("application").operator("equals")
                                        .value(applicationId)))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))), true},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("grantType").operator("notEquals")
                                .value(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))), false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("notEquals")
                                .value(applicationId))), false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("application").operator("notEquals")
                                .value(applicationId))
                        .addExpressionsItem(new Expression().field("grantType").operator("notEquals")
                                .value(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE))), false},
                {new ORRule()
                        .condition(ORRule.ConditionEnum.OR)
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                                .addExpressionsItem(new Expression().field("application").operator("equals")
                                        .value("other_application")))
                        .addRulesItem(new ANDRule().condition(ANDRule.ConditionEnum.AND)
                        .addExpressionsItem(new Expression().field("grantType").operator("equals")
                                .value("password"))), false},
        };
    }

    @Test(dataProvider = "rulesProvider", groups = "wso2.is", description = "Get access token with password grant")
    public void testPreIssueAccessTokenActionInvocationForRules(ORRule rule, boolean shouldActionExecute)
            throws Exception {

        assertTrue(updatePreIssueAccessTokenActionRule(rule),
                "Updating the pre issue access token action rule returned an error.");

        String authorizationCode = getAuthorizationCode();
        String accessToken = getAccessToken(authorizationCode);

        loggedIn();

        assertActionExecution(accessToken, shouldActionExecute);
    }

    private String getAuthorizationCode() throws IOException {

        HttpResponse response = sendAuthorizeRequest();
        if (isLoggedIn()) {
            return getAuthorizationCodeFromLocationHeader(response);
        } else {
            response = getLoginPage(response);
            String sessionDataKey = getSessionDataKey(response);
            return sendLoginPost(sessionDataKey);
        }
    }

    private String getAccessToken(String authorizationCode) throws IOException, JSONException {

        HttpResponse response = sendAccessTokenRequest(authorizationCode);
        return getAccessTokenFromResponse(response);
    }

    private void assertActionExecution(String accessToken, boolean shouldActionExecute) throws Exception {

        if (shouldActionExecute) {
            assertIfActionHasExecuted(accessToken);
        } else {
            assertIfNoActionHasExecuted(accessToken);
        }
    }

    private static void assertIfActionHasExecuted(String accessToken) throws Exception {

        JWTClaimsSet jwtClaims = extractJwtClaims(accessToken);
        assertNotNull(jwtClaims.getClaim("custom_claim_string_1"),
                "Custom claim not found in the access token. Action was expected to execute but has not executed.");
    }

    private static void assertIfNoActionHasExecuted(String accessToken) throws Exception {

        JWTClaimsSet jwtClaims = extractJwtClaims(accessToken);
        assertNull(jwtClaims.getClaim("custom_claim_string_1"),
                "Custom claim found in the access token. Action was not expected to execute but has executed.");
    }

    private HttpResponse sendAuthorizeRequest() throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
    }

    private static String getSessionDataKey(HttpResponse response) throws IOException {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        assertNotNull(keyValues, "SessionDataKey key value is null");

        String sessionDataKey = keyValues.get(0).getValue();
        assertNotNull(sessionDataKey, "Session data key is null");
        EntityUtils.consume(response.getEntity());
        return sessionDataKey;
    }

    private HttpResponse getLoginPage(HttpResponse response) throws IOException {

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available");
        EntityUtils.consume(response.getEntity());

        return sendGetRequest(client, locationHeader.getValue());
    }

    private String sendLoginPost(String sessionDataKey) throws IOException {

        HttpResponse response = sendLoginPostForCustomUsers(client, sessionDataKey, TEST_USER, ADMIN_WSO2);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());

        return getAuthorizationCodeFromLocationHeader(response);
    }

    private String getAuthorizationCodeFromLocationHeader(HttpResponse response) throws IOException {

        Header locationHeader;
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        String authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        assertNotNull(authorizationCode);
        return authorizationCode;
    }

    private HttpResponse sendAccessTokenRequest(String authorizationCode)
            throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("code", authorizationCode));
        urlParameters.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("client_id", clientId));

        String scopes = String.join(" ", requestedScopes);
        urlParameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, urlParameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Failed to receive a response for access token request.");

        return response;
    }

    private String getAccessTokenFromResponse(HttpResponse response) throws IOException, JSONException {

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"), "Access token not found in the token response.");
        String accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token is null.");

        return accessToken;
    }

    private void loggedIn() {

        isSSOLogin = true;
    }

    private boolean isLoggedIn() {

        return isSSOLogin;
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

        ActionModel actionModel = new ActionModel();
        actionModel.setName("Access Token Pre Issue");
        actionModel.setDescription("This is a test pre issue access token type");
        actionModel.setEndpoint(endpoint);

        return createAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionModel);
    }

    private boolean updatePreIssueAccessTokenActionRule(ORRule rule) throws IOException {

        ActionUpdateModel actionModel = new ActionUpdateModel().rule(rule);
        return updateAction(PRE_ISSUE_ACCESS_TOKEN_API_PATH, actionId, actionModel);
    }

    private void addUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER);
        userInfo.setPassword(ADMIN_WSO2);
        userInfo.setName(new Name().givenName(TEST_USER_GIVEN));
        userInfo.addEmail(new Email().value(TEST_USER_GMAIL_COM));
        userId = scim2RestClient.createUser(userInfo);
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    private static JWTClaimsSet extractJwtClaims(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }
}
