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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration test for cross-organization SSO via sub-organization applications of shared users.
 */
public class SharedUserSubOrgAppCrossOrgSsoTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APP_NAME = "SharedUserSubOrgCrossOrgSsoApp";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String ROOT_USER_USERNAME = "suborgssouser";
    private static final String ROOT_USER_PASSWORD = "SharedUser@wso2";
    private static final String ROOT_USER_EMAIL = "sharedsuborgcrossorguser@wso2.com";
    private static final String ACCOUNT_MGT_CATEGORY_ID = "QWNjb3VudCBNYW5hZ2VtZW50";
    private static final String MULTI_ATTRIBUTE_CONNECTOR_ID = "bXVsdGlhdHRyaWJ1dGUubG9naW4uaGFuZGxlcg";
    private static final String MULTI_ATTRIBUTE_ENABLE_PROPERTY = "account.multiattributelogin.handler.enable";
    private static final String ORGANIZATION_PATH = "/o/";
    private static final String TENANT_PATH = "t/";
    private static final String OAUTH_2_AUTHORIZE = "/oauth2/authorize";
    private static final String OAUTH_2_TOKEN = "/oauth2/token";

    private final TestUserMode userMode;
    private final String organization1Name;
    private final String organization1Handle;
    private final String organization2Name;
    private final String organization2Handle;

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private UserSharingRestClient userSharingRestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;

    private String organization1Id;
    private String organization2Id;
    private String rootUserId;
    private String subOrgApp1Id;
    private String subOrgApp2Id;
    private String clientId1;
    private String clientSecret1;
    private String clientId2;
    private String clientSecret2;
    private String switchedM2MToken1;
    private String switchedM2MToken2;
    private String subOrgSessionDataKey;
    private String authorizationCode;
    private String ssoAuthorizationCode;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "suborg_app_cross_org_sso_sub_org1",
                        "suborg_app_cross_org_sso_sub_org1", "suborg_app_cross_org_sso_sub_org2",
                        "suborg_app_cross_org_sso_sub_org2"},
                {TestUserMode.TENANT_ADMIN, "suborg_app_cross_org_sso_t_sub_org1",
                        "suborg_app_cross_org_sso_t_sub_org1", "suborg_app_cross_org_sso_t_sub_org2",
                        "suborg_app_cross_org_sso_t_sub_org2"}};
    }

    @Factory(dataProvider = "configProvider")
    public SharedUserSubOrgAppCrossOrgSsoTestCase(TestUserMode userMode, String org1Name, String org1Handle,
                                                  String org2Name, String org2Handle) {

        this.userMode = userMode;
        this.organization1Name = org1Name;
        this.organization1Handle = org1Handle;
        this.organization2Name = org2Name;
        this.organization2Handle = org2Handle;
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
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
    }

    @Test(priority = 2, dependsOnMethods = "testInit")
    public void testDisableMultiAttributeLogin() throws Exception {

        ConnectorsPatchReq connectorPatchReq = new ConnectorsPatchReq();
        connectorPatchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        PropertyReq enableProperty = new PropertyReq();
        enableProperty.setName(MULTI_ATTRIBUTE_ENABLE_PROPERTY);
        enableProperty.setValue("false");
        connectorPatchReq.addProperties(enableProperty);
        identityGovernanceRestClient.updateConnectors(ACCOUNT_MGT_CATEGORY_ID, MULTI_ATTRIBUTE_CONNECTOR_ID,
                connectorPatchReq);
    }

    @Test(priority = 3, dependsOnMethods = "testDisableMultiAttributeLogin")
    public void testCreateSubOrganizations() throws Exception {

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        organization1Id = orgMgtRestClient.addOrganizationWithToken(organization1Name, organization1Handle, m2mToken);
        assertNotNull(organization1Id, "Organization 1 ID should not be null.");
        organization2Id = orgMgtRestClient.addOrganizationWithToken(organization2Name, organization2Handle, m2mToken);
        assertNotNull(organization2Id, "Organization 2 ID should not be null.");

        switchedM2MToken1 = orgMgtRestClient.switchM2MToken(organization1Id);
        assertNotNull(switchedM2MToken1, "Switched M2M token for sub-organization 1 should not be null.");
        switchedM2MToken2 = orgMgtRestClient.switchM2MToken(organization2Id);
        assertNotNull(switchedM2MToken2, "Switched M2M token for sub-organization 2 should not be null.");
    }

    @Test(priority = 4, dependsOnMethods = "testCreateSubOrganizations")
    public void testCreateApplicationsInSubOrgs() throws Exception {

        subOrgApp1Id = oAuth2RestClient.createOrganizationApplication(buildSubOrgApp(), switchedM2MToken1);
        assertNotNull(subOrgApp1Id, "Sub-organization 1 application ID should not be null.");

        OpenIDConnectConfiguration oidcConfig1 =
                oAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(subOrgApp1Id, switchedM2MToken1);
        assertNotNull(oidcConfig1, "OIDC configuration for sub-organization 1 application should not be null.");
        clientId1 = oidcConfig1.getClientId();
        clientSecret1 = oidcConfig1.getClientSecret();
        assertNotNull(clientId1, "Client ID for sub-organization 1 application should not be null.");
        assertNotNull(clientSecret1, "Client secret for sub-organization 1 application should not be null.");

        subOrgApp2Id = oAuth2RestClient.createOrganizationApplication(buildSubOrgApp(), switchedM2MToken2);
        assertNotNull(subOrgApp2Id, "Sub-organization 2 application ID should not be null.");

        OpenIDConnectConfiguration oidcConfig2 =
                oAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(subOrgApp2Id, switchedM2MToken2);
        assertNotNull(oidcConfig2, "OIDC configuration for sub-organization 2 application should not be null.");
        clientId2 = oidcConfig2.getClientId();
        clientSecret2 = oidcConfig2.getClientSecret();
        assertNotNull(clientId2, "Client ID for sub-organization 2 application should not be null.");
        assertNotNull(clientSecret2, "Client secret for sub-organization 2 application should not be null.");
    }

    @Test(priority = 5, dependsOnMethods = "testCreateApplicationsInSubOrgs")
    public void testUpdateSubOrgAppAuthenticationSequences() {

        oAuth2RestClient.updateSubOrgApplication(subOrgApp1Id, buildSharedUserAuthSequencePatch(), switchedM2MToken1);
        oAuth2RestClient.updateSubOrgApplication(subOrgApp2Id, buildSharedUserAuthSequencePatch(), switchedM2MToken2);
    }

    @Test(priority = 6, dependsOnMethods = "testUpdateSubOrgAppAuthenticationSequences")
    public void testCreateRootOrgUser() throws Exception {

        UserObject rootUser = new UserObject();
        rootUser.setUserName(ROOT_USER_USERNAME);
        rootUser.setPassword(ROOT_USER_PASSWORD);
        rootUser.addEmail(new Email().value(ROOT_USER_EMAIL));

        rootUserId = scim2RestClient.createUser(rootUser);
        assertNotNull(rootUserId, "Root organization user ID should not be null.");
    }

    @Test(priority = 7, dependsOnMethods = "testCreateRootOrgUser")
    public void testShareUserToSubOrgs() throws Exception {

        UserShareWithAllRequestBody shareRequest = new UserShareWithAllRequestBody();
        shareRequest.setUserCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(rootUserId));
        shareRequest.setPolicy(ALL_EXISTING_ORGS_ONLY);
        userSharingRestClient.shareUsersWithAll(shareRequest);

        String userSearchReq = new JSONObject()
                .put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:SearchRequest"))
                .put("attributes", new JSONArray().put("id"))
                .put("filter", "userName eq " + ROOT_USER_USERNAME)
                .toString();

        assertTrue(scim2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MToken1),
                "User should be shared to sub-organization 1.");
        assertTrue(scim2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MToken2),
                "User should be shared to sub-organization 2.");
    }

    @Test(priority = 8, dependsOnMethods = "testShareUserToSubOrgs")
    public void testSendAuthorizeRequestToSubOrg1() throws Exception {

        String subOrgAuthorizeUrl = serverURL + TENANT_PATH + tenantInfo.getDomain() + ORGANIZATION_PATH +
                organization1Id + OAUTH_2_AUTHORIZE;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("client_id", clientId1));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        params.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, params, subOrgAuthorizeUrl);

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        subOrgSessionDataKey = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "sessionDataKey");
        assertNotNull(subOrgSessionDataKey, "Sub-org session data key should not be null.");
    }

    @Test(priority = 9, dependsOnMethods = "testSendAuthorizeRequestToSubOrg1")
    public void testAuthenticateAtSubOrganization1() throws Exception {

        String subOrgCommonAuthUrl = serverURL + TENANT_PATH + tenantInfo.getDomain() + ORGANIZATION_PATH +
                organization1Id + "/commonauth";

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

        // Follow redirect to sub-org authorize endpoint to get the authorization code.
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org authorized response redirect is null.");
        EntityUtils.consume(response.getEntity());

        authorizationCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "code");
        assertNotNull(authorizationCode, "Authorization code should not be null.");
    }

    @Test(priority = 10, dependsOnMethods = "testAuthenticateAtSubOrganization1")
    public void testGetAccessTokenForSubOrg1() throws Exception {

        String subClaim = getSubClaimFromAuthorizationCode(authorizationCode, organization1Id, clientId1,
                clientSecret1);
        Assert.assertEquals(subClaim, rootUserId,
                "Sub claim should contain the user ID of the user created in the root organization.");
    }

    @Test(priority = 11, dependsOnMethods = "testGetAccessTokenForSubOrg1")
    public void testSsoToSubOrganization2() throws Exception {

        String subOrgAuthorizeUrl = serverURL + TENANT_PATH + tenantInfo.getDomain() + ORGANIZATION_PATH +
                organization2Id + OAUTH_2_AUTHORIZE;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("client_id", clientId2));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        params.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, params, subOrgAuthorizeUrl);

        // The user should be silently single-signed-on.
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Location header expected for SSO authorize request is not available.");
        EntityUtils.consume(response.getEntity());

        String location = locationHeader.getValue();
        assertNull(DataExtractUtil.getParamFromURIString(location, "sessionDataKey"),
                "User should not be prompted for login during SSO, but a login page was presented.");

        ssoAuthorizationCode = DataExtractUtil.getParamFromURIString(location, "code");
        assertNotNull(ssoAuthorizationCode,
                "Authorization code should be issued via SSO without prompting the user for login.");
    }

    @Test(priority = 12, dependsOnMethods = "testSsoToSubOrganization2")
    public void testGetAccessTokenForSubOrg2() throws Exception {

        String subClaim = getSubClaimFromAuthorizationCode(ssoAuthorizationCode, organization2Id, clientId2,
                clientSecret2);
        Assert.assertEquals(subClaim, rootUserId,
                "Sub claim of the SSO token should contain the root organization user ID.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() {

        if (identityGovernanceRestClient != null) {
            try {
                identityGovernanceRestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close identity governance REST client.", e);
            }
        }
        if (organization1Id != null && orgMgtRestClient != null) {
            try {
                orgMgtRestClient.deleteOrganization(organization1Id);
            } catch (Exception e) {
                log.error("Failed to delete organization: " + organization1Id, e);
            }
        }
        if (organization2Id != null && orgMgtRestClient != null) {
            try {
                orgMgtRestClient.deleteOrganization(organization2Id);
            } catch (Exception e) {
                log.error("Failed to delete organization: " + organization2Id, e);
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

    private ApplicationModel buildSubOrgApp() {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        return new ApplicationModel()
                .name(APP_NAME)
                .enhancedOrgAuthenticationEnabled(false)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));
    }

    private ApplicationPatchModel buildSharedUserAuthSequencePatch() {

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
        return patchModel;
    }

    /**
     * Exchanges the given authorization code for tokens at the given sub-organization token endpoint and returns the
     * sub claim of the issued ID token.
     */
    private String getSubClaimFromAuthorizationCode(String authCode, String organizationId, String clientId,
                                                    String clientSecret) throws Exception {

        String subOrgTokenUrl = serverURL + TENANT_PATH + tenantInfo.getDomain() + ORGANIZATION_PATH +
                organizationId + OAUTH_2_TOKEN;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authCode));
        params.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", "Basic " + getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, params, subOrgTokenUrl);
        assertNotNull(response, "Token endpoint response should not be null.");

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseBody);

        assertTrue(jsonResponse.has("access_token"), "access_token is missing from token response.");
        assertNotNull(jsonResponse.getString("access_token"), "Access token should not be null.");

        assertTrue(jsonResponse.has("id_token"), "id_token is missing from token response.");
        String idToken = jsonResponse.getString("id_token");
        assertNotNull(idToken, "ID token should not be null.");

        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        assertNotNull(claimsSet, "JWT claims set should not be null.");

        String subClaim = claimsSet.getSubject();
        assertNotNull(subClaim, "Sub claim should not be null.");
        return subClaim;
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
