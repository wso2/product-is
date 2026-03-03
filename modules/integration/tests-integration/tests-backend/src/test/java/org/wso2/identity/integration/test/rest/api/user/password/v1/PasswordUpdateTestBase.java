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

package org.wso2.identity.integration.test.rest.api.user.password.v1;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.rest.api.user.password.v1.model.PasswordChangeRequest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;

/**
 * Base test class for Password Update API integration tests.
 * Provides helpers for creating apps, users, obtaining tokens, and calling the change-password API.
 */
public class PasswordUpdateTestBase extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String CHANGE_PASSWORD_PATH = "api/users/v1/me/change-password";
    protected static final String ORG_CHANGE_PASSWORD_PATH = "o/api/users/v1/me/change-password";
    protected static final String SERVER_CONFIGS_PATH = "api/server/v1/configs";

    protected static final String PASSWORD_UPDATE_SCOPE = "internal_user_password_update";
    protected static final String ORG_PASSWORD_UPDATE_SCOPE = "internal_org_user_password_update";

    protected static final String TEST_USER_GIVEN_NAME = "PwdTest";
    protected static final String TEST_USER_LAST_NAME = "User";
    protected static final String TEST_USER_EMAIL = "pwdtest.user@gmail.com";

    protected static final String APP_CALLBACK_URL = CALLBACK_URL;

    protected static final String AUTHORIZED_APIS_JSON = "app-authorized-apis.json";

    protected SCIM2RestClient scim2RestClient;
    protected RestBaseClient restBaseClient;

    /**
     * Initialize the test case.
     *
     * @param userMode User Mode.
     * @throws Exception If an error occurred while initializing.
     */
    protected void initBase(TestUserMode userMode) throws Exception {

        super.init(userMode);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        restBaseClient = new RestBaseClient();
    }

    /**
     * Create an OAuth2 application with authorization_code, client_credentials, and organization_switch grant types.
     * Optionally shares the application with all child organizations.
     *
     * @param appName          Application name.
     * @param shareWithSubOrgs If true, shares the app with all sub-organizations after creation.
     * @return ApplicationResponseModel of the created application.
     * @throws Exception If an error occurred while creating or sharing the application.
     */
    protected ApplicationResponseModel createApp(String appName, boolean shareWithSubOrgs) throws Exception {

        ApplicationModel application = new ApplicationModel();
        application.setName(appName);

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "client_credentials", "organization_switch");

        List<String> callBackUrls = new ArrayList<>();
        callBackUrls.add(APP_CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration();
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setIsManagementApp(true);
        application.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(true).skipLogoutConsent(true));

        String appId = addApplication(application);

        if (shareWithSubOrgs) {
            shareApplicationWithAllChildren(appId);
        }

        return getApplication(appId);
    }

    /**
     * Authorize the password update APIs for the given application with No Policy (no RBAC).
     * All scopes of the following APIs are authorized:
     *   /api/users/v1/me/change-password
     *   /o/api/users/v1/me/change-password
     *
     * @param applicationId Application ID.
     * @throws Exception If an error occurred while authorizing the APIs.
     */
    protected void authorizePasswordUpdateScope(String applicationId) throws Exception {

        List<String> apiIdentifiers = new ArrayList<>();
        apiIdentifiers.add("/api/users/v1/me/change-password");
        apiIdentifiers.add("/o/api/users/v1/me/change-password");

        for (String apiIdentifier : apiIdentifiers) {
            List<APIResourceListItem> filteredAPIResource =
                    restClient.getAPIResourcesWithFiltering("identifier+eq+" + apiIdentifier);
            if (filteredAPIResource == null || filteredAPIResource.isEmpty()) {
                Assert.fail("Required API resource not found for identifier: " + apiIdentifier);
            }
            String apiId = filteredAPIResource.get(0).getId();
            List<ScopeGetModel> apiResourceScopes = restClient.getAPIResourceScopes(apiId);
            AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
            authorizedAPICreationModel.setId(apiId);
            authorizedAPICreationModel.setPolicyIdentifier("No Policy");
            apiResourceScopes.forEach(scope -> authorizedAPICreationModel.addScopesItem(scope.getName()));
            restClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
        }
    }

    /**
     * Create a test user in the super organization via SCIM2.
     *
     * @param username Username.
     * @param password Password.
     * @return User ID.
     * @throws Exception If an error occurred while creating the user.
     */
    protected String createTestUser(String username, String password) throws Exception {

        UserObject userInfo = new UserObject()
                .userName(username)
                .password(password)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LAST_NAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        return scim2RestClient.createUser(userInfo);
    }

    /**
     * Create a user in the given sub-organization via SCIM2.
     *
     * @param username         Username.
     * @param password         Password.
     * @param switchedM2MToken Switched M2M token for the sub-organization.
     * @return User ID.
     * @throws Exception If an error occurred while creating the user.
     */
    protected String createSubOrgUser(String username, String password, String switchedM2MToken) throws Exception {

        UserObject userInfo = new UserObject()
                .userName(username)
                .password(password)
                .name(new Name().givenName(TEST_USER_GIVEN_NAME).familyName(TEST_USER_LAST_NAME))
                .addEmail(new Email().value(TEST_USER_EMAIL));
        return scim2RestClient.createSubOrgUser(userInfo, switchedM2MToken);
    }

    /**
     * Obtain a user access token for the super organization using the authorization code grant.
     * Each call uses a fresh HTTP client to avoid session contamination between users.
     *
     * @param clientId     Client ID.
     * @param clientSecret Client secret.
     * @param username     Username.
     * @param password     Password.
     * @param scope        Requested scope.
     * @return Access token string.
     * @throws Exception If an error occurred while obtaining the token.
     */
    protected String getUserAccessToken(String clientId, String clientSecret, String username, String password,
                                        String scope) throws Exception {

        try (CloseableHttpClient authClient = createLocalHttpClient()) {
            // Step 1: Send authorization request.
            List<NameValuePair> authzParams = new ArrayList<>();
            authzParams.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
            authzParams.add(new BasicNameValuePair("client_id", clientId));
            authzParams.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));
            authzParams.add(new BasicNameValuePair("scope", scope));

            HttpResponse response = sendPostRequestWithParameters(authClient, authzParams,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Authorization response location header is null.");
            EntityUtils.consume(response.getEntity());

            // Step 2: Follow redirect to login page.
            response = sendGetRequest(authClient, locationHeader.getValue());
            String sessionDataKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            EntityUtils.consume(response.getEntity());

            // Step 3: Submit login credentials.
            response = sendLoginPostForCustomUsers(authClient, sessionDataKey, username, password);
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Login response location header is null.");
            EntityUtils.consume(response.getEntity());

            // Step 4: Follow redirect to callback URL with authorization code.
            response = sendGetRequest(authClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Redirect to callback URL with authorization code is null.");
            EntityUtils.consume(response.getEntity());
            String authorizationCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "code");
            Assert.assertNotNull(authorizationCode, "Authorization code not found in callback URL.");

            // Step 5: Exchange authorization code for access token.
            return exchangeCodeForToken(authClient, clientId, clientSecret, authorizationCode, scope,
                    getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        }
    }

    /**
     * Obtain a user access token for a sub-organization user via the Organization SSO flow.
     *
     * @param clientId     Client ID of the shared app (root org).
     * @param clientSecret Client secret of the shared app (root org).
     * @param username     Sub-org user username.
     * @param password     Sub-org user password.
     * @param scope        Requested scope.
     * @param orgName      Name of the sub-organization (for OrganizationAuthenticator).
     * @param orgId        ID of the sub-organization (for sub-org commonauth URL).
     * @return Access token string scoped to the sub-organization.
     * @throws Exception If an error occurred while obtaining the token.
     */
    protected String getSubOrgUserAccessToken(String clientId, String clientSecret, String username, String password,
                                               String scope, String orgName, String orgId) throws Exception {

        try (CloseableHttpClient authClient = createLocalHttpClient()) {
            // Step 1: Initiate auth at ROOT org authorize with fidp=OrganizationSSO.
            List<NameValuePair> authzParams = new ArrayList<>();
            authzParams.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
            authzParams.add(new BasicNameValuePair("client_id", clientId));
            authzParams.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));
            authzParams.add(new BasicNameValuePair("scope", scope));
            authzParams.add(new BasicNameValuePair(OAuth2Constant.FIDP_PARAM, "OrganizationSSO"));

            HttpResponse response = sendPostRequestWithParameters(authClient, authzParams,
                    getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
            Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Organization SSO auth response location header is null.");
            EntityUtils.consume(response.getEntity());

            String sessionDataKey = DataExtractUtil.getParamFromURIString(
                    locationHeader.getValue(), "sessionDataKey");
            Assert.assertNotNull(sessionDataKey, "Session data key from Organization SSO auth is null.");

            // Step 2: Switch to sub-org via OrganizationAuthenticator.
            List<NameValuePair> orgSwitchParams = new ArrayList<>();
            orgSwitchParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
            orgSwitchParams.add(new BasicNameValuePair("org", orgName));
            orgSwitchParams.add(new BasicNameValuePair("idp", "SSO"));
            orgSwitchParams.add(new BasicNameValuePair("authenticator", "OrganizationAuthenticator"));

            response = sendPostRequestWithParameters(authClient, orgSwitchParams,
                    getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Organization switch response location header is null.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(authClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Sub-org authorize redirect location header is null.");
            EntityUtils.consume(response.getEntity());

            String subOrgSessionDataKey = DataExtractUtil.getParamFromURIString(
                    locationHeader.getValue(), "sessionDataKey");
            Assert.assertNotNull(subOrgSessionDataKey, "Sub-org session data key is null.");

            // Step 3: Submit credentials at sub-org commonauth (/o/{orgId}/commonauth).
            List<NameValuePair> loginParams = new ArrayList<>();
            loginParams.add(new BasicNameValuePair("sessionDataKey", subOrgSessionDataKey));
            loginParams.add(new BasicNameValuePair("username", username));
            loginParams.add(new BasicNameValuePair("password", password));

            String subOrgCommonAuthUrl = getTenantQualifiedURL(serverURL + "o/" + orgId + "/commonauth",
                    tenantInfo.getDomain());
            response = sendPostRequestWithParameters(authClient, loginParams, subOrgCommonAuthUrl);
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Sub-org login response location header is null.");
            EntityUtils.consume(response.getEntity());

            // Step 4: Follow 3 redirect hops (sub-org authorize -> root commonauth -> root authorize).
            response = sendGetRequest(authClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Sub-org authorized response redirect is null.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(authClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Root org commonauth redirect is null.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(authClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(locationHeader, "Root org authorize redirect to callback is null.");
            EntityUtils.consume(response.getEntity());

            String authorizationCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "code");
            Assert.assertNotNull(authorizationCode, "Authorization code not found in callback URL.");

            // Step 5: Exchange code at ROOT org token endpoint.
            return exchangeCodeForToken(authClient, clientId, clientSecret, authorizationCode, scope,
                    getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        }
    }

    /**
     * Call the change-password API for the super organization.
     *
     * @param accessToken     Bearer access token.
     * @param currentPassword Current password.
     * @param newPassword     New password.
     * @return HTTP response.
     * @throws IOException If an error occurred while making the request.
     */
    protected CloseableHttpResponse changePassword(String accessToken, String currentPassword, String newPassword)
            throws IOException {

        return doChangePasswordRequest(getTenantQualifiedURL(serverURL + CHANGE_PASSWORD_PATH, tenantInfo.getDomain()),
                accessToken, currentPassword, newPassword);
    }

    /**
     * Call the change-password API for a sub-organization.
     *
     * @param accessToken     Bearer access token.
     * @param currentPassword Current password.
     * @param newPassword     New password.
     * @return HTTP response.
     * @throws IOException If an error occurred while making the request.
     */
    protected CloseableHttpResponse changePasswordInSubOrg(String accessToken, String currentPassword,
                                                        String newPassword) throws IOException {

        return doChangePasswordRequest(getTenantQualifiedURL(serverURL + ORG_CHANGE_PASSWORD_PATH, tenantInfo.getDomain()),
                accessToken, currentPassword, newPassword);
    }

    /**
     * Update the preserveCurrentSessionAtPasswordUpdate server configuration using admin Basic Auth
     * credentials.
     *
     * @param preserve Whether to preserve the current session after a password update.
     * @throws Exception If an error occurred while updating the configuration.
     */
    protected void setPreserveSessionConfig(boolean preserve) throws Exception {

        if (tenantInfo == null || tenantInfo.getContextUser() == null) {
            return;
        }
        String credentials = tenantInfo.getContextUser().getUserName() + ":" +
                tenantInfo.getContextUser().getPassword();

        Header[] headers = new Header[3];
        headers[0] = new BasicHeader("Authorization",
                "Basic " + Base64.encodeBase64String(credentials.getBytes()));
        headers[1] = new BasicHeader("Content-Type", "application/json");
        headers[2] = new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT);
        try (CloseableHttpResponse response = restBaseClient.getResponseOfHttpPatch(
                getTenantQualifiedURL(
                        serverURL + SERVER_CONFIGS_PATH, tenantInfo.getDomain()),
                buildPreserveSessionPatchBody(preserve),
                headers)) {
            int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            Assert.assertTrue(statusCode >= 200 && statusCode < 300,
                    "Failed to update preserveCurrentSessionAtPasswordUpdate config. Status: " + statusCode);
        }
    }

    /**
     * Create an OrgMgtRestClient for sub-organization management.
     *
     * @return OrgMgtRestClient instance.
     * @throws Exception If an error occurred while creating the client.
     */
    protected OrgMgtRestClient createOrgMgtRestClient() throws Exception {

        JSONObject authorizedAPIs = new JSONObject(
                RESTTestBase.readResource(AUTHORIZED_APIS_JSON, PasswordUpdateTestBase.class));
        return new OrgMgtRestClient(isServer, tenantInfo, serverURL, authorizedAPIs);
    }

    /**
     * Enables or disables the password history governance connector for the current tenant.
     *
     * @param enable true to enable password history enforcement; false to disable it.
     * @throws Exception If an error occurred while updating the governance connector.
     */
    protected void setPasswordHistoryEnabled(boolean enable) throws Exception {

        IdentityGovernanceRestClient governanceClient =
                new IdentityGovernanceRestClient(serverURL, tenantInfo);
        try {
            PropertyReq property = new PropertyReq();
            property.setName("passwordHistory.enable");
            property.setValue(String.valueOf(enable));

            ConnectorsPatchReq connectorPatch = new ConnectorsPatchReq();
            connectorPatch.setOperation(OperationEnum.UPDATE);
            connectorPatch.addProperties(property);

            governanceClient.updateConnectors("UGFzc3dvcmQgUG9saWNpZXM", "cGFzc3dvcmRIaXN0b3J5",
                    connectorPatch);
        } finally {
            governanceClient.closeHttpClient();
        }
    }

    /**
     * Reset a super-organization user's password via SCIM2 admin PATCH, bypassing the
     * current-password check. Used to establish a known password baseline at the start of each test.
     *
     * @param userId   SCIM2 user ID.
     * @param password Password to set.
     * @throws Exception If an error occurred while resetting the password.
     */
    protected void adminResetSuperOrgUserPassword(String userId, String password) throws Exception {

        scim2RestClient.updateUser(buildPasswordResetPatchOp(password), userId);
    }

    /**
     * Reset a sub-organization user's password via SCIM2 admin API (used for test isolation).
     *
     * @param userId           User ID.
     * @param password         New password to set.
     * @param switchedM2MToken Switched M2M token for the sub-organization.
     * @throws Exception If an error occurred while resetting the password.
     */
    protected void adminResetSubOrgUserPassword(String userId, String password, String switchedM2MToken)
            throws Exception {

        scim2RestClient.updateSubOrgUser(buildPasswordResetPatchOp(password), userId, switchedM2MToken);
    }

    /**
     * Clean up shared REST clients.
     *
     * @throws Exception If an error occurred during cleanup.
     */
    protected void cleanupBase() throws Exception {

        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (restClient != null) {
            restClient.closeHttpClient();
        }
        if (restBaseClient != null) {
            restBaseClient.client.close();
        }
    }

    /**
     * Execute a cleanup action, swallowing any exception so that subsequent cleanup steps still run.
     *
     * @param action Cleanup action to execute.
     */
    protected void safeCleanup(CleanupAction action) {

        try {
            action.execute();
        } catch (Exception ignored) {
            // Intentionally suppressed so that remaining cleanup steps and the finally block always execute.
        }
    }

    @FunctionalInterface
    protected interface CleanupAction {

        void execute() throws Exception;
    }

    /**
     * Share an application with all child organizations.
     *
     * @param applicationId Application ID.
     * @throws Exception If an error occurred while sharing the application.
     */
    private void shareApplicationWithAllChildren(String applicationId) throws Exception {

        ApplicationSharePOSTRequest shareRequest = new ApplicationSharePOSTRequest();
        shareRequest.setShareWithAllChildren(true);
        restClient.shareApplication(applicationId, shareRequest);
    }
    
    /**
     * Make a change-password request to the given URL with the given access token, current password, and new password.
     *
     * @param url URL to make the request to.
     * @param accessToken Access token.
     * @param currentPassword Current password.
     * @param newPassword New password.
     * @return HTTP response.
     * @throws IOException If an error occurred while making the request.
     */
    private CloseableHttpResponse doChangePasswordRequest(String url, String accessToken, String currentPassword,
                                                          String newPassword) throws IOException {

        Header[] headers = new Header[3];
        headers[0] = new BasicHeader("Authorization", "Bearer " + accessToken);
        headers[1] = new BasicHeader("Content-Type", "application/json");
        headers[2] = new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT);
        PasswordChangeRequest requestBody = new PasswordChangeRequest()
                .currentPassword(currentPassword)
                .newPassword(newPassword);
        return restBaseClient.getResponseOfHttpPost(url,
                restBaseClient.toJSONString(requestBody), headers);
    }

    /**
     * Builds the password reset patch operation.
     *
     * @param password Password to set.
     * @return the password reset patch operation.
     */
    private PatchOperationRequestObject buildPasswordResetPatchOp(String password) {

        Map<String, String> passwordValue = new HashMap<>();
        passwordValue.put("password", password);
        return new PatchOperationRequestObject()
                .schemas(Collections.singletonList("urn:ietf:params:scim:api:messages:2.0:PatchOp"))
                .addOperations(new UserItemAddGroupobj()
                        .op(UserItemAddGroupobj.OpEnum.REPLACE)
                        .value(passwordValue));
    }

    /**
     * Build the JSON body for patching the preserveCurrentSessionAtPasswordUpdate configuration.
     *
     * @param preserve Whether to preserve the current session after a password update.
     * @return JSON string for the patch request body.
     */
    private String buildPreserveSessionPatchBody(boolean preserve) throws JSONException {

        JSONObject operation = new JSONObject();
        operation.put("operation", "REPLACE");
        operation.put("path", "/preserveCurrentSessionAtPasswordUpdate");
        operation.put("value", String.valueOf(preserve));
        return new JSONArray().put(operation).toString();
    }

    /**
     * Exchange a code for an access token.
     *
     * @param httpClient HTTP client.
     * @param clientId Client ID.
     * @param clientSecret Client secret.
     * @param code Code to exchange.
     * @param scope Scope to use.
     * @param tokenEndpoint Token endpoint.
     * @return Access token.
     * @throws Exception If an error occurred while exchanging the code.
     */
    private String exchangeCodeForToken(CloseableHttpClient httpClient, String clientId, String clientSecret,
                                        String code, String scope, String tokenEndpoint) throws Exception {

        List<NameValuePair> tokenParams = new ArrayList<>();
        tokenParams.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        tokenParams.add(new BasicNameValuePair("code", code));
        tokenParams.add(new BasicNameValuePair("redirect_uri", APP_CALLBACK_URL));
        tokenParams.add(new BasicNameValuePair("scope", scope));

        List<Header> tokenHeaders = new ArrayList<>();
        tokenHeaders.add(new BasicHeader(OAuth2Constant.AUTHORIZATION_HEADER,
                OAuth2Constant.BASIC_HEADER + " " + getBase64EncodedString(clientId, clientSecret)));
        tokenHeaders.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        tokenHeaders.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(httpClient, tokenHeaders, tokenParams, tokenEndpoint);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);
        if (!jsonResponse.has("access_token")) {
            throw new Exception("Failed to obtain access token. Response: " + responseString);
        }
        return jsonResponse.getString("access_token");
    }

    /**
     * Create a local HTTP client.
     *
     * @return Local HTTP client.
     */
    private CloseableHttpClient createLocalHttpClient() {

        CookieStore cookieStore = new BasicCookieStore();
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore)
                .disableRedirectHandling()
                .build();
    }
}
