/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.HybridFlowConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.ERROR;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.INVALID_AUTHORIZATION_DETAILS;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.TEST_TYPE_1;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.TEST_TYPE_2;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.TEST_TYPE_3;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.createTestAPIResource;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getDoubleTestAuthorizationDetails;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getInvalidAuthorizationDetails;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getSingleTestAuthorizationDetail;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getTestAuthorizationDetailsType;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getTripleTestAuthorizationDetails;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.getUnknownAuthorizationDetails;
import static org.wso2.identity.integration.test.utils.RichAuthorizationRequestsUtils.isRequestTypesMatchResponseTypes;

/**
 * Integration test class for testing the OAuth2 grant flows with authorization details.
 */
public class OAuth2RichAuthorizationRequestsGrantTypesTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String USERS_PATH = "users";
    private static final String USER_EMAIL = "rar_user@wso2.com";
    private static final String USERNAME = "richAuthorizationRequestsUser";
    private static final String PASSWORD = "Pass@123";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN = "code id_token token";

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private OpenIDConnectConfiguration opaqueOidcConfig;
    private OpenIDConnectConfiguration jwtOidcConfig;
    private String opaqueAppId;
    private String jwtAppId;
    private String apiResourceId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        super.setSystemproperties();
        this.scim2RestClient = new SCIM2RestClient(super.serverURL, super.tenantInfo);
        this.client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.DEFAULT)
                        .build())
                .setDefaultCookieSpecRegistry(RegistryBuilder.<CookieSpecProvider>create()
                        .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                        .build())
                .build();

        this.opaqueAppId = super.addApplication(this.getApplicationWithOpaqueTokens());
        this.jwtAppId = super.addApplication(this.getApplicationWithJWTTokens());
        assertNotNull(this.opaqueAppId, "OAuth App creation failed");
        assertNotNull(this.jwtAppId, "OAuth App creation failed");

        this.opaqueOidcConfig = super.restClient.getOIDCInboundDetails(this.opaqueAppId);
        this.jwtOidcConfig = super.restClient.getOIDCInboundDetails(this.jwtAppId);
        assertNotNull(this.opaqueOidcConfig, "Application creation failed.");
        assertNotNull(this.jwtOidcConfig, "Application creation failed.");

        this.apiResourceId = createTestAPIResourceWithThreeTypes();
        this.addAPIAuthorization(this.apiResourceId, this.opaqueAppId);
        this.addAPIAuthorization(this.apiResourceId, this.jwtAppId);
        this.addRARUser();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        super.restClient.deleteAPIResource(this.apiResourceId);
        super.deleteApp(this.opaqueAppId);
        super.deleteApp(this.jwtAppId);
        super.restClient.closeHttpClient();
        this.scim2RestClient.closeHttpClient();
        this.client.close();
    }

    private Asserter buildOpaqueTokenAsserter(JSONArray expectedAuthorizationDetails, String expectedScopes) {

        return new OpaqueTokenAsserter(this.opaqueOidcConfig.getClientId(), this.opaqueOidcConfig.getClientSecret(),
                expectedAuthorizationDetails, expectedScopes);
    }

    private Asserter buildJwtTokenAsserter(JSONArray expectedAuthorizationDetails, String expectedScopes) {

        return new JwtTokenAsserter(this.jwtOidcConfig.getClientId(), this.jwtOidcConfig.getClientSecret(),
                expectedAuthorizationDetails, expectedScopes);
    }

    @DataProvider(name = "authorizationDetailsProvider")
    public Object[][] authorizationDetailsProvider(Method testMethod) throws JSONException {

        switch (testMethod.getName()) {
            case "testAuthorizationCodeGrantFlowWithAuthorizationDetails":
            case "testAccessTokenRequestWithoutAuthorizationDetails":
            case "testClientCredentialsGrantFlowWithAuthorizationDetails":
            case "testPasswordGrantFlowWithAuthorizationDetails":
            case "testImplicitGrantFlowWithAuthorizationDetails":
                return new Object[][]{
                        {buildOpaqueTokenAsserter(null, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(null, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildOpaqueTokenAsserter(getSingleTestAuthorizationDetail(), null)},
                        {buildJwtTokenAsserter(getSingleTestAuthorizationDetail(), null)},
                        {buildOpaqueTokenAsserter(getSingleTestAuthorizationDetail(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(getSingleTestAuthorizationDetail(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildOpaqueTokenAsserter(getDoubleTestAuthorizationDetails(), null)},
                        {buildJwtTokenAsserter(getDoubleTestAuthorizationDetails(), null)},
                        {buildOpaqueTokenAsserter(getDoubleTestAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(getDoubleTestAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)}
                };
            case "testHybridFlowsWithAuthorizationDetails":
                return new Object[][]{
                        {buildOpaqueTokenAsserter(getSingleTestAuthorizationDetail(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(getSingleTestAuthorizationDetail(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildOpaqueTokenAsserter(getDoubleTestAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(getDoubleTestAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)}
                };
            case "testAuthorizationCodeGrantFlowWithInvalidAuthorizationDetails":
                return new Object[][]{
                        {buildOpaqueTokenAsserter(getUnknownAuthorizationDetails(), null)},
                        {buildJwtTokenAsserter(getUnknownAuthorizationDetails(), null)},
                        {buildOpaqueTokenAsserter(getUnknownAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildJwtTokenAsserter(getUnknownAuthorizationDetails(), OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN)},
                        {buildOpaqueTokenAsserter(getInvalidAuthorizationDetails(), null)},
                        {buildJwtTokenAsserter(getInvalidAuthorizationDetails(), null)},
                        {buildOpaqueTokenAsserter(getTripleTestAuthorizationDetails(), null)}
                };
        }
        return null;
    }

    /**
     * Tests the authorization code grant flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Sends an authorization request with authorization details for the authorization code grant type.</li>
     *     <li>Perform access token request with authorization details and validates.</li>
     *     <li>Refreshes the access token and validates the refreshed token response.</li>
     *     <li>Introspect the token and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test authorization code grant flow with authorization details")
    public void testAuthorizationCodeGrantFlowWithAuthorizationDetails(Asserter asserter) throws Exception {

        // sending authorize request with authorization_details parameter
        final String sessionDataKeyConsent = this.sendAuthCodeGrantAuthRequestPost(getAuthzRequestParams(asserter));
        final String authorizationCode = this.sendAuthCodeGrantApprovalPost(sessionDataKeyConsent);

        // retrieving a new access token
        JSONObject tokenResponse =
                this.sendTokenRequestPost(asserter, this.getAccessTokenRequestParams(asserter, authorizationCode));
        asserter.assertTokenResponse(tokenResponse);

        // refreshing the access token
        final String refreshToken = tokenResponse.getString(OAuth2Constant.REFRESH_TOKEN);
        tokenResponse = this.sendTokenRequestPost(asserter, this.getRefreshTokenRequestParams(asserter, refreshToken));
        asserter.assertTokenResponse(tokenResponse);

        // revoking the access token
        final String accessToken = tokenResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    /**
     * Tests the authorization code grant flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Sends an authorization request with authorization details for the authorization code grant type.</li>
     *     <li>Perform access token request without authorization details and validates.</li>
     *     <li>Refreshes the access token and validates the refreshed token response.</li>
     *     <li>Introspect the token and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test access token request without authorization details")
    public void testAccessTokenRequestWithoutAuthorizationDetails(Asserter asserter) throws Exception {

        final List<NameValuePair> authzRequestParams = getAuthzRequestParams(asserter);
        // sending authorize request with authorization_details parameter
        final String sessionDataKeyConsent = this.sendAuthCodeGrantAuthRequestPost(authzRequestParams);
        final String authorizationCode = this.sendAuthCodeGrantApprovalPost(sessionDataKeyConsent);

        // retrieving a new access token
        JSONObject tokenResponse =
                this.sendTokenRequestPost(asserter, this.getAccessTokenRequestParams(asserter, authorizationCode));
        asserter.assertTokenResponse(tokenResponse);

        // refreshing the access token
        final String refreshToken = tokenResponse.getString(OAuth2Constant.REFRESH_TOKEN);
        tokenResponse = this.sendTokenRequestPost(asserter, this.getRefreshTokenRequestParams(asserter, refreshToken));
        asserter.assertTokenResponse(tokenResponse);

        // revoking the access token
        final String accessToken = tokenResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    /**
     * Tests the authorization code grant flow with invalid authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Sends an authorization request with invalid authorization details.</li>
     *     <li>Assert the error response.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            dependsOnMethods = {"testAuthorizationCodeGrantFlowWithAuthorizationDetails"},
            description = "Test authorization code grant flow with invalid authorization details")
    public void testAuthorizationCodeGrantFlowWithInvalidAuthorizationDetails(Asserter asserter) throws Exception {

        final HttpResponse response = super.sendPostRequestWithParameters(this.client,
                this.getAuthzRequestParams(asserter), OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        assertEquals(DataExtractUtil.getParamFromURIString(locationHeader.getValue(), ERROR),
                INVALID_AUTHORIZATION_DETAILS);
    }

    /**
     * Tests the client credentials grant flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Perform access token request with authorization details and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test client credentials grant flow with authorization details")
    public void testClientCredentialsGrantFlowWithAuthorizationDetails(Asserter asserter) throws Exception {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        final JSONObject tokenResponse = this.sendTokenRequestPost(asserter, urlParameters);
        final String accessToken = tokenResponse.optString(OAuth2Constant.ACCESS_TOKEN);

        asserter.assertAccessToken(accessToken);
        asserter.assertAuthorizationDetails(tokenResponse
                .optString(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), false);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    /**
     * Tests the password grant flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Perform access token request with authorization details and validates.</li>
     *     <li>Refreshes the access token and validates the refreshed token response.</li>
     *     <li>Introspect the token and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test password grant flow with authorization details")
    public void testPasswordGrantFlowWithAuthorizationDetails(Asserter asserter) throws Exception {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));

        JSONObject tokenResponse = this.sendTokenRequestPost(asserter, urlParameters);
        asserter.assertTokenResponse(tokenResponse);

        // refreshing the access token
        final String refreshToken = tokenResponse.getString(OAuth2Constant.REFRESH_TOKEN);
        tokenResponse = this.sendTokenRequestPost(asserter, this.getRefreshTokenRequestParams(asserter, refreshToken));
        asserter.assertTokenResponse(tokenResponse);

        final String accessToken = tokenResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    /**
     * Tests the <code>token</code> implicit grant flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Sends an authorization request with authorization details for the implicit grant type.</li>
     *     <li>Introspect the token and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test implicit grant flow with authorization details")
    public void testImplicitGrantFlowWithAuthorizationDetails(Asserter asserter) throws Exception {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, asserter.getClientId()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));

        final String sessionDataKeyConsent = this.sendAuthCodeGrantAuthRequestPost(urlParameters);
        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        final String accessToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                OAuth2Constant.ACCESS_TOKEN);

        asserter.assertHttpResponse(response);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    /**
     * Tests the <code>code id_token token</code> hybrid flow with authorization details included.
     * This method performs the following actions:
     * <ul>
     *     <li>Sends an authorization request with authorization details for the authorization code grant type.</li>
     *     <li>Perform access token request without authorization details and validates.</li>
     *     <li>Refreshes the access token and validates the refreshed token response.</li>
     *     <li>Introspect the token and validates.</li>
     *     <li>Revokes the access token at the end of the flow.</li>
     * </ul>
     *
     * @param asserter The assertion utility used for validating responses in the test flow.
     * @throws Exception If an error occurs during the flow.
     */
    @Test(groups = "wso2.is", dataProvider = "authorizationDetailsProvider",
            description = "Test hybrid flow with authorization details")
    public void testHybridFlowsWithAuthorizationDetails(Asserter asserter) throws Exception {

        final String sessionDataKeyConsent =
                this.sendAuthCodeGrantAuthRequestPost(this.getHybridRequestParams(asserter));
        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        final String authorizationCode = DataExtractUtil
                .extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.AUTHORIZATION_CODE_NAME);

        asserter.assertHttpResponse(response);

        JSONObject tokenResponse =
                this.sendTokenRequestPost(asserter, this.getAccessTokenRequestParams(asserter, authorizationCode));
        asserter.assertTokenResponse(tokenResponse);

        final String accessToken = tokenResponse.getString(OAuth2Constant.ACCESS_TOKEN);
        sendTokenRevokeRequestPost(asserter, accessToken);
    }

    private String createTestAPIResourceWithThreeTypes() throws IOException {

        return createTestAPIResource(super.restClient, Arrays.asList(getTestAuthorizationDetailsType(TEST_TYPE_1),
                getTestAuthorizationDetailsType(TEST_TYPE_2), getTestAuthorizationDetailsType(TEST_TYPE_3)));
    }

    private void addAPIAuthorization(final String apiResourceId, final String appId) throws IOException {
        final AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(apiResourceId);
        authorizedAPICreationModel.addAuthorizationDetailsTypesItem(TEST_TYPE_1);
        authorizedAPICreationModel.addAuthorizationDetailsTypesItem(TEST_TYPE_2);

        final int statusCode = super.restClient.addAPIAuthorizationToApplication(appId, authorizedAPICreationModel);
        assertEquals(statusCode, HttpServletResponse.SC_OK,
                "Failed to add API authorization to the application");
    }

    private void addRARUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME);
        userInfo.setPassword(PASSWORD);
        userInfo.addEmail(new Email().value(USER_EMAIL));

        final String userId = this.scim2RestClient.createUser(userInfo);
        String roleId = this.scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        this.scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    public JSONObject sendTokenRequestPost(Asserter asserter, List<NameValuePair> urlParameters) throws Exception {

        final HttpPost httpPost = getDefaultHttpPost(asserter, OAuth2Constant.ACCESS_TOKEN_ENDPOINT, urlParameters);
        final HttpResponse response = this.client.execute(httpPost);

        JSONObject tokenResponse = new JSONObject(EntityUtils.toString(response.getEntity(), UTF_8));
        EntityUtils.consume(response.getEntity());
        assertNotNull(tokenResponse, "Token response is null");

        return tokenResponse;
    }

    private void sendTokenRevokeRequestPost(Asserter asserter, String accessToken) throws IOException {

        final List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("token_type_hint", OAuth2Constant.ACCESS_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE_TOKEN, accessToken));

        final HttpPost httpPost = getDefaultHttpPost(asserter, OAuth2Constant.TOKEN_REVOKE_ENDPOINT, urlParameters);
        final HttpResponse response = this.client.execute(httpPost);
        assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK);
    }

    private HttpPost getDefaultHttpPost(Asserter asserter, String url, List<NameValuePair> urlParameters)
            throws UnsupportedEncodingException {

        final HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(OAuth2Constant.AUTHORIZATION_HEADER, "Basic " +
                getBase64EncodedString(asserter.getClientId(), asserter.getClientSecret()));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

        return httpPost;
    }

    /**
     * Initiates an authorization request to IS and obtain session data key.
     */
    private String sendAuthCodeGrantAuthRequestPost(final List<NameValuePair> urlParameters) throws Exception {

        final HttpResponse response = super.sendPostRequestWithParameters(this.client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        String sessionDataKey = DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                OAuth2Constant.SESSION_DATA_KEY);
        EntityUtils.consume(response.getEntity());
        if (StringUtils.isBlank(sessionDataKey)) {
            // A user session might already available - try retrieving session data key consent
            return DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                    OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        } else {
            return this.sendAuthCodeGrantAuthenticatePost(sessionDataKey);
        }
    }

    /**
     * Provide user credentials and authenticate to the system.
     */
    private String sendAuthCodeGrantAuthenticatePost(String sessionDataKey) throws Exception {

        HttpResponse response = sendLoginPost(this.client, sessionDataKey);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(this.client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        return DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    private String sendAuthCodeGrantApprovalPost(String sessionDataKeyConsent) throws Exception {

        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());

        return DataExtractUtil.getParamFromURIString(locationHeader.getValue(), OAuth2Constant.AUTHORIZATION_CODE_NAME);
    }

    @Override
    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.SESSION_DATA_KEY, sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(COMMON_AUTH_URL, super.tenantInfo.getDomain()));
    }

    private List<NameValuePair> getAuthzRequestParams(Asserter asserter) {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, asserter.getClientId()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));

        return urlParameters;
    }

    private List<NameValuePair> getAccessTokenRequestParams(Asserter asserter, String authorizationCode) {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.AUTHORIZATION_CODE_NAME, authorizationCode));
        return urlParameters;
    }

    private List<NameValuePair> getRefreshTokenRequestParams(Asserter asserter, String refreshToken) {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.GRANT_TYPE_NAME,
                OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.REFRESH_TOKEN, refreshToken));
        return urlParameters;
    }

    private List<NameValuePair> getHybridRequestParams(Asserter asserter) {

        final List<NameValuePair> urlParameters = this.getDefaultNameValuePairs(asserter);
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, asserter.getClientId()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        return urlParameters;
    }

    private List<NameValuePair> getDefaultNameValuePairs(Asserter asserter) {
        final List<NameValuePair> urlParameters = new ArrayList<>();
        if (asserter.getExpectedAuthorizationDetails() != null) {
            urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS,
                    asserter.getExpectedAuthorizationDetails().toString()));
        }
        if (StringUtils.isNotEmpty(asserter.getExpectedScopes())) {
            urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, asserter.getExpectedScopes()));
        }
        return urlParameters;
    }

    private ApplicationModel getApplicationWithOpaqueTokens() {

        final ApplicationModel application = new ApplicationModel();

        HybridFlowConfiguration hybridFlow = new HybridFlowConfiguration();
        hybridFlow.setEnable(true);
        hybridFlow.setResponseType(RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setHybridFlow(hybridFlow);
        oidcConfig.setGrantTypes(Arrays.asList("authorization_code", "implicit", "password",
                "client_credentials", "refresh_token"));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("RarServiceProvider1");
        application.setDescription(SERVICE_PROVIDER_DESC);
        application.setIsManagementApp(true);
        application.setClaimConfiguration(super.setApplicationClaimConfig());

        return application;
    }

    private ApplicationModel getApplicationWithJWTTokens() {

        ApplicationModel application = this.getApplicationWithOpaqueTokens();

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration();
        accessTokenConfig.type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        InboundProtocols inboundProtocolConfiguration = application.getInboundProtocolConfiguration();
        OpenIDConnectConfiguration oidc = inboundProtocolConfiguration.getOidc();
        oidc.setAccessToken(accessTokenConfig);
        inboundProtocolConfiguration.setOidc(oidc);

        application.setInboundProtocolConfiguration(inboundProtocolConfiguration);
        application.setName("RarServiceProvider2");

        return application;
    }

    public static class Asserter {

        private final String clientId;
        private final String clientSecret;
        private final JSONArray expectedAuthorizationDetails;
        private final String expectedScopes;

        Asserter(String clientId, String clientSecret, JSONArray expectedAuthorizationDetails, String expectedScopes) {

            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.expectedAuthorizationDetails = expectedAuthorizationDetails;
            this.expectedScopes = expectedScopes;
        }

        String getClientId() {
            return this.clientId;
        }

        String getClientSecret() {
            return this.clientSecret;
        }

        JSONArray getExpectedAuthorizationDetails() {
            return this.expectedAuthorizationDetails;
        }

        String getExpectedScopes() {
            return this.expectedScopes;
        }

        void assertTokenResponse(JSONObject tokenResponse) throws Exception {

            this.assertAccessToken(tokenResponse.optString(OAuth2Constant.ACCESS_TOKEN));
            this.assertRefreshToken(tokenResponse.optString(OAuth2Constant.REFRESH_TOKEN));
            this.assertAuthorizationDetails(tokenResponse
                    .optString(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), false);
            this.assertScopes(tokenResponse.optString(OAuth2Constant.OAUTH2_SCOPE), false);
        }

        void assertHttpResponse(HttpResponse httpResponse) throws Exception {

            Header locationHeader = httpResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(httpResponse.getEntity());

            this.assertAccessToken(DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                    OAuth2Constant.ACCESS_TOKEN));
            this.assertAuthorizationDetails(DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                    OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), true);
            this.assertScopes(DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                    OAuth2Constant.OAUTH2_SCOPE), true);
        }

        void assertAuthorizationDetails(String authorizationDetails, boolean isUrlEncoded) throws JSONException {

            if (expectedAuthorizationDetails == null) {
                assertTrue(StringUtils.isEmpty(authorizationDetails));
                return;
            }

            if (isUrlEncoded) {
                authorizationDetails = URLDecoder.decode(authorizationDetails, UTF_8);
            }

            final JSONArray actualAuthorizationDetails = new JSONArray(authorizationDetails);
            assertEquals(actualAuthorizationDetails.length(), expectedAuthorizationDetails.length());
            assertTrue(isRequestTypesMatchResponseTypes(expectedAuthorizationDetails, actualAuthorizationDetails));
        }

        void assertAccessToken(String accessToken) {

            assertNotNull(accessToken, "Access token is null");
        }

        void assertRefreshToken(String refreshToken) {

            assertNotNull(refreshToken, "Refresh token is null");
        }

        void assertScopes(String actualScopes, boolean isUrlEncoded) {

            if (StringUtils.isNotEmpty(expectedScopes)) {
                if (isUrlEncoded) {
                    actualScopes = URLDecoder.decode(actualScopes, UTF_8).replaceAll("\\+", " ");
                }
                assertNotNull(actualScopes);
                assertTrue(Arrays.asList(expectedScopes.split("\\s+"))
                        .containsAll(Arrays.asList(actualScopes.split("\\s+"))));
            }
        }
    }

    class OpaqueTokenAsserter extends Asserter {

        OpaqueTokenAsserter(String clientId, String clientSecret, JSONArray expectedAuthorizationDetails,
                            String expectedScopes) {

            super(clientId, clientSecret, expectedAuthorizationDetails, expectedScopes);
        }

        @Override
        public void assertTokenResponse(JSONObject tokenResponse) throws Exception {

            super.assertTokenResponse(tokenResponse);
            final org.json.simple.JSONObject introspectJson =
                    introspectTokenWithTenant(
                            client,
                            tokenResponse.getString(OAuth2Constant.ACCESS_TOKEN),
                            OAuth2Constant.INTRO_SPEC_ENDPOINT,
                            USERNAME, PASSWORD
                    );
            JSONObject introspectResponse = new JSONObject(introspectJson.toJSONString());
            super.assertAuthorizationDetails(introspectResponse
                    .optString(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), false);
            super.assertScopes(introspectResponse.optString(OAuth2Constant.OAUTH2_SCOPE), false);
        }

        @Override
        void assertHttpResponse(HttpResponse httpResponse) throws Exception {

            Header locationHeader = httpResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            final String accessToken =
                    DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.ACCESS_TOKEN);
            super.assertHttpResponse(httpResponse);

            final org.json.simple.JSONObject introspectJson = introspectTokenWithTenant(client, accessToken,
                    OAuth2Constant.INTRO_SPEC_ENDPOINT, USERNAME, PASSWORD);
            JSONObject introspectResponse = new JSONObject(introspectJson.toJSONString());
            super.assertAuthorizationDetails(introspectResponse
                    .optString(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), false);
            super.assertScopes(introspectResponse.optString(OAuth2Constant.OAUTH2_SCOPE), false);
        }
    }

    static class JwtTokenAsserter extends Asserter {

        JwtTokenAsserter(String clientId, String clientSecret, JSONArray expectedAuthorizationDetails,
                         String expectedScopes) {

            super(clientId, clientSecret, expectedAuthorizationDetails, expectedScopes);
        }

        @Override
        public void assertTokenResponse(JSONObject tokenResponse) throws Exception {

            super.assertAccessToken(tokenResponse.optString(OAuth2Constant.ACCESS_TOKEN));
            super.assertRefreshToken(tokenResponse.optString(OAuth2Constant.REFRESH_TOKEN));

            final String jwtAccessToken = tokenResponse.optString(OAuth2Constant.ACCESS_TOKEN);
            JSONObject jsonAccessToken =
                    new JSONObject(new String(Base64.getDecoder().decode(jwtAccessToken.split("\\.")[1])));

            super.assertAuthorizationDetails(jsonAccessToken
                    .optString(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS), false);

            if (this.getExpectedAuthorizationDetails() != null) {

                JSONArray claimAuthorizationDetails =
                        tokenResponse.getJSONArray(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS);
                JSONArray responseAuthorizationDetails =
                        jsonAccessToken.getJSONArray(OAuth2Constant.OAUTH2_AUTHORIZATION_DETAILS);

                assertEquals(claimAuthorizationDetails.length(), responseAuthorizationDetails.length());
                isRequestTypesMatchResponseTypes(claimAuthorizationDetails, responseAuthorizationDetails);
            }

            super.assertScopes(jsonAccessToken.optString(OAuth2Constant.OAUTH2_SCOPE), false);
        }
    }
}
