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

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration test for legacy organization authentication with shared users.
 * <p>
 * This test verifies the following flow:
 * 1. Create an application in the root org with enhanced org authentication disabled.
 * 2. Create a sub-organization and share the application to it.
 * 3. Create a user in the root organization and share the user to the sub-organization.
 * 4. Login via the shared application by initiating the authorize request from the root organization,
 * discovering the sub-organization via OrganizationAuthenticator, and authenticating at the sub-organization.
 * 5. Verify the token is received and the sub claim contains the root organization user ID.
 */
public class SharedUserLegacyOrgAuthenticationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APP_NAME = "SharedUserLegacyOrgAuthApp";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String ROOT_USER_USERNAME = "user";
    private static final String ROOT_USER_PASSWORD = "SharedUser@wso2";
    private static final String ROOT_USER_EMAIL = "sharedlegacyuser@wso2.com";

    private final TestUserMode userMode;
    private final String organizationName;
    private final String organizationHandle;

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private UserSharingRestClient userSharingRestClient;

    private String organizationId;
    private String rootUserId;
    private String rootApplicationId;
    private String sharedAppId;
    private String clientId;
    private String clientSecret;
    private String switchedM2MToken;
    private String sessionDataKey;
    private String subOrgSessionDataKey;
    private String authorizationCode;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "legacy_shared_sub_org", "legacy_shared_sub_org"},
                {TestUserMode.TENANT_ADMIN, "legacy_shared_t_sub_org", "legacy_shared_t_sub_org"}};
    }

    @Factory(dataProvider = "configProvider")
    public SharedUserLegacyOrgAuthenticationTestCase(TestUserMode userMode, String orgName, String orgHandle) {

        this.userMode = userMode;
        this.organizationName = orgName;
        this.organizationHandle = orgHandle;
    }

    @Test(priority = 1)
    public void testInit() throws Exception {

        super.init(userMode);
        client = createHttpClient();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        userSharingRestClient = new UserSharingRestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass())));
    }

    @Test(priority = 2, dependsOnMethods = "testInit")
    public void testCreateApplicationWithLegacyOrgAuth() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel app = new ApplicationModel()
                .name(APP_NAME)
                .enhancedOrgAuthenticationEnabled(false)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));

        rootApplicationId = oAuth2RestClient.createApplication(app);
        assertNotNull(rootApplicationId, "Root application ID should not be null.");

        OpenIDConnectConfiguration createdOidcConfig = oAuth2RestClient.getOIDCInboundDetails(rootApplicationId);
        assertNotNull(createdOidcConfig, "OIDC configuration should not be null.");
        clientId = createdOidcConfig.getClientId();
        clientSecret = createdOidcConfig.getClientSecret();
        assertNotNull(clientId, "Client ID should not be null.");
        assertNotNull(clientSecret, "Client secret should not be null.");
    }

    @Test(priority = 3, dependsOnMethods = "testCreateApplicationWithLegacyOrgAuth")
    public void testCreateSubOrganization() throws Exception {

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        organizationId = orgMgtRestClient.addOrganizationWithToken(organizationName, organizationHandle, m2mToken);
        assertNotNull(organizationId, "Organization ID should not be null.");
    }

    @Test(priority = 4, dependsOnMethods = "testCreateSubOrganization")
    public void testShareApplicationToSubOrg() throws Exception {

        ApplicationSharePOSTRequest shareRequest = new ApplicationSharePOSTRequest();
        shareRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(rootApplicationId, shareRequest);

        switchedM2MToken = orgMgtRestClient.switchM2MToken(organizationId);
        assertNotNull(switchedM2MToken, "Switched M2M token should not be null.");

        waitForApplicationSharedToSubOrg(APP_NAME, switchedM2MToken, 10000);

        sharedAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(APP_NAME, switchedM2MToken);
        assertNotNull(sharedAppId, "Shared application ID in sub-organization should not be null.");
    }

    @Test(priority = 5, dependsOnMethods = "testShareApplicationToSubOrg")
    public void testUpdateSharedAppAuthenticationSequence() {

        AuthenticationSequence authSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("SharedUserIdentifierExecutor")))
                .addStepsItem(new AuthenticationStep()
                        .id(2)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("BasicAuthenticator")));

        ApplicationPatchModel patchModel = new ApplicationPatchModel();
        patchModel.setAuthenticationSequence(authSequence);

        oAuth2RestClient.updateSubOrgApplication(sharedAppId, patchModel, switchedM2MToken);
    }

    @Test(priority = 6, dependsOnMethods = "testUpdateSharedAppAuthenticationSequence")
    public void testCreateRootOrgUser() throws Exception {

        UserObject rootUser = new UserObject();
        rootUser.setUserName(ROOT_USER_USERNAME);
        rootUser.setPassword(ROOT_USER_PASSWORD);
        rootUser.addEmail(new Email().value(ROOT_USER_EMAIL));

        rootUserId = scim2RestClient.createUser(rootUser);
        assertNotNull(rootUserId, "Root organization user ID should not be null.");
    }

    @Test(priority = 7, dependsOnMethods = "testCreateRootOrgUser")
    public void testShareUserToSubOrg() throws Exception {

        UserShareWithAllRequestBody shareRequest = new UserShareWithAllRequestBody();
        shareRequest.setUserCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(rootUserId));
        shareRequest.setPolicy(ALL_EXISTING_ORGS_ONLY);
        userSharingRestClient.shareUsersWithAll(shareRequest);

        String userSearchReq = new JSONObject()
                .put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:SearchRequest"))
                .put("attributes", new JSONArray().put("id"))
                .put("filter", "userName eq " + ROOT_USER_USERNAME)
                .toString();

        boolean isUserShared = scim2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MToken);
        assertTrue(isUserShared, "User should be shared to the sub-organization.");
    }

    @Test(priority = 8, dependsOnMethods = "testShareUserToSubOrg")
    public void testSendAuthorizeRequestFromRootOrg() throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        params.add(new BasicNameValuePair("scope", "openid"));
        params.add(new BasicNameValuePair(OAuth2Constant.FIDP_PARAM, "OrganizationSSO"));

        HttpResponse response = sendPostRequestWithParameters(client, params,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        sessionDataKey = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "sessionDataKey");
        assertNotNull(sessionDataKey, "Session data key should not be null.");
    }

    @Test(priority = 9, dependsOnMethods = "testSendAuthorizeRequestFromRootOrg")
    public void testDiscoverSubOrganization() throws Exception {

        List<NameValuePair> orgSwitchParams = new ArrayList<>();
        orgSwitchParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        orgSwitchParams.add(new BasicNameValuePair("org", organizationName));
        orgSwitchParams.add(new BasicNameValuePair("idp", "SSO"));
        orgSwitchParams.add(new BasicNameValuePair("authenticator", "OrganizationAuthenticator"));

        HttpResponse response = sendPostRequestWithParameters(client, orgSwitchParams,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Organization switch response location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org authorize redirect location header is null.");
        EntityUtils.consume(response.getEntity());

        subOrgSessionDataKey = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "sessionDataKey");
        assertNotNull(subOrgSessionDataKey, "Sub-org session data key should not be null.");
    }

    @Test(priority = 10, dependsOnMethods = "testDiscoverSubOrganization")
    public void testAuthenticateAtSubOrganization() throws Exception {

        String subOrgCommonAuthUrl = getTenantQualifiedURL(
                serverURL + "o/" + organizationId + "/commonauth", tenantInfo.getDomain());

        List<NameValuePair> sidfLoginParams = new ArrayList<>();
        sidfLoginParams.add(new BasicNameValuePair("sessionDataKey", subOrgSessionDataKey));
        sidfLoginParams.add(new BasicNameValuePair("username", ROOT_USER_USERNAME));

        HttpResponse response = sendPostRequestWithParameters(client, sidfLoginParams, subOrgCommonAuthUrl);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org shared user identifier response location header is null.");
        EntityUtils.consume(response.getEntity());

        List<NameValuePair> loginParams = new ArrayList<>();
        loginParams.add(new BasicNameValuePair("sessionDataKey", subOrgSessionDataKey));
        loginParams.add(new BasicNameValuePair("username", ROOT_USER_USERNAME));
        loginParams.add(new BasicNameValuePair("password", ROOT_USER_PASSWORD));

        response = sendPostRequestWithParameters(client, loginParams, subOrgCommonAuthUrl);

        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org basic auth response location header is null.");
        EntityUtils.consume(response.getEntity());

        // Follow redirect hops: sub-org authorize -> root commonauth -> root authorize -> callback.
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org authorized response redirect is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Root org commonauth redirect is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Root org authorize redirect to callback is null.");
        EntityUtils.consume(response.getEntity());

        authorizationCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "code");
        assertNotNull(authorizationCode, "Authorization code should not be null.");
    }

    @Test(priority = 11, dependsOnMethods = "testAuthenticateAtSubOrganization")
    public void testGetAccessTokenAndVerifySubClaim() throws Exception {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", "Basic " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, params,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        assertNotNull(response, "Token endpoint response should not be null.");

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        org.json.JSONObject jsonResponse = new org.json.JSONObject(responseBody);

        assertTrue(jsonResponse.has("access_token"), "access_token is missing from token response.");
        String accessToken = jsonResponse.getString("access_token");
        assertNotNull(accessToken, "Access token should not be null.");

        assertTrue(jsonResponse.has("id_token"), "id_token is missing from token response.");
        String idToken = jsonResponse.getString("id_token");
        assertNotNull(idToken, "ID token should not be null.");

        // Verify the sub claim in the ID token contains the root organization user ID.
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        assertNotNull(claimsSet, "JWT claims set should not be null.");

        String subClaim = claimsSet.getSubject();
        assertNotNull(subClaim, "Sub claim should not be null.");
        Assert.assertEquals(subClaim, rootUserId,
                "Sub claim should contain the user ID of the user created in the root organization.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() {

        if (organizationId != null && orgMgtRestClient != null) {
            try {
                orgMgtRestClient.deleteOrganization(organizationId);
            } catch (Exception e) {
                log.error("Failed to delete organization: " + organizationId, e);
            }
        }
        if (rootApplicationId != null && oAuth2RestClient != null) {
            try {
                oAuth2RestClient.deleteApplication(rootApplicationId);
            } catch (Exception e) {
                log.error("Failed to delete application: " + rootApplicationId, e);
            }
        }
        if (rootUserId != null && scim2RestClient != null) {
            try {
                scim2RestClient.deleteUser(rootUserId);
            } catch (Exception e) {
                log.error("Failed to delete root org user: " + rootUserId, e);
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (scim2RestClient != null) {
            try {
                scim2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close SCIM2 REST client.", e);
            }
        }
        if (oAuth2RestClient != null) {
            try {
                oAuth2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close OAuth2 REST client.", e);
            }
        }
        if (orgMgtRestClient != null) {
            try {
                orgMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close org management REST client.", e);
            }
        }
        if (userSharingRestClient != null) {
            try {
                userSharingRestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close user sharing REST client.", e);
            }
        }
    }

    private void waitForApplicationSharedToSubOrg(String appName, String subOrgToken, long timeoutMs)
            throws Exception {

        long deadline = System.currentTimeMillis() + timeoutMs;
        long pollInterval = 500;
        while (System.currentTimeMillis() < deadline) {
            try {
                String newSharedAppId = oAuth2RestClient.getAppIdUsingAppNameInOrganization(appName, subOrgToken);
                if (newSharedAppId != null && !newSharedAppId.isEmpty()) {
                    return;
                }
            } catch (IOException e) {
                log.debug("Transient error while polling for shared application '" + appName + "', retrying.", e);
            }
            Thread.sleep(pollInterval);
            pollInterval = Math.min(pollInterval * 2, 5000);
        }
        Assert.fail("Application '" + appName + "' was not shared to sub-organization within " + timeoutMs + " ms.");
    }

    private CloseableHttpClient createHttpClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {

                        return false;
                    }
                })
                .build();
    }
}
