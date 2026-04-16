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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectConfig;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;

/**
 * Integration test for sub-organization application authentication with shared users via federated login.
 * <p>
 * This test uses two IS instances (primary IS at port offset 0 and secondary IS at port offset 1) and verifies:
 * 1. Create a sub-organization on the primary IS.
 * 2. Create an application on the secondary IS (representing the external IdP's relying party).
 * 3. Create an application directly in the sub-organization on the primary IS and enable use linked local account
 * property.
 * 4. Create a federated OIDC IdP in the sub-organization pointing to the secondary IS.
 * 5. Update the sub-org application's authentication sequence to use the federated IdP.
 * 6. Create a user in the root organization and share the user to the sub-organization.
 * 7. Create a user on the secondary IS (the federated user).
 * 8. Use the user association API to create an association between the shared user in the sub-org
 * and the federated IdP (with the secondary IS user's ID as the federated user ID).
 * 9. Authenticate to the sub-organization application via federated login through the secondary IS.
 * 10. Verify the token is received and the sub claim contains the root organization user ID.
 */
public class SharedUserSubOrgAppFederatedAssociationTestCase extends AbstractIdentityFederationTestCase {

    private static final String SUPER_TENANT = "carbon.super";
    private static final String TENANT_PATH = "t/";
    private static final String ORGANIZATION_PATH = "/o/";
    public static final String OAUTH_2_AUTHORIZE = "/oauth2/authorize";
    public static final String OAUTH_2_TOKEN = "/oauth2/token";
    private static final String SUB_ORG_APP_NAME = "SharedUserFedAssocApp";
    private static final String SECONDARY_IS_APP_NAME = "ExternalIdPRelayApp";
    private static final String FEDERATED_IDP_NAME = "ExternalOIDCIdP";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "shared-user-federation-authorized-apis.json";
    private static final String ASSOCIATION_MGT_API_RESOURCE_NAME = "Association Management API";
    private static final String API_RESOURCE_ORG_TYPE = "ORGANIZATION";
    private static final List<String> ASSOCIATION_MGT_API_SCOPES = new ArrayList<String>() {{
        add("internal_org_user_association_create");
        add("internal_org_user_association_view");
        add("internal_org_user_association_delete");
    }};
    private static final String ROOT_USER_USERNAME = "fedAssocUser";
    private static final String ROOT_USER_PASSWORD = "FedAssocUser@wso2";
    private static final String ROOT_USER_EMAIL = "fedassocuser@wso2.com";
    private static final String SECONDARY_IS_USER_USERNAME = "externalFedUser";
    private static final String SECONDARY_IS_USER_PASSWORD = "ExternalFed@wso2";

    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;

    // Base64URL encoding of "OpenIDConnectAuthenticator".
    private static final String OIDC_AUTHENTICATOR_ID = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    private static final String OIDC_AUTHENTICATOR_NAME = "OpenIDConnectAuthenticator";

    private static final String PRIMARY_IS_CALLBACK_URL = "https://localhost:9853/commonauth";
    private static final String SECONDARY_IS_AUTHORIZE_ENDPOINT = "https://localhost:9854/oauth2/authorize";
    private static final String SECONDARY_IS_TOKEN_ENDPOINT = "https://localhost:9854/oauth2/token";
    private static final String SECONDARY_IS_LOGOUT_ENDPOINT = "https://localhost:9854/oidc/logout";
    private static final String SECONDARY_IS_COMMONAUTH_URL = "https://localhost:9854/commonauth";

    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s/";

    private CloseableHttpClient client;
    private SCIM2RestClient primaryISSCIM2RestClient;
    private SCIM2RestClient secondaryISSCIM2RestClient;
    private OAuth2RestClient primaryISOAuth2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private UserSharingRestClient userSharingRestClient;

    private String organizationId;
    private String switchedM2MToken;
    private String rootUserId;
    private String secondaryISUserId;
    private String secondaryISAppId;
    private String secondaryISClientId;
    private String secondaryISClientSecret;
    private String subOrgAppId;
    private String subOrgClientId;
    private String subOrgClientSecret;
    private String sharedUserIdInSubOrg;
    private String authorizationCode;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public SharedUserSubOrgAppFederatedAssociationTestCase(TestUserMode userMode) throws Exception {

    }

    @Test(priority = 1)
    public void testInit() throws Exception {

        super.initTest();

        createServiceClients(PORT_OFFSET_0, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT,
                IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT});
        createServiceClients(PORT_OFFSET_1, new IdentityConstants.ServiceClientType[]{
                IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT});

        client = createHttpClient();
        primaryISSCIM2RestClient = new SCIM2RestClient(getPrimaryISURI(), tenantInfo);
        secondaryISSCIM2RestClient = new SCIM2RestClient(getSecondaryISURI(), tenantInfo);
        primaryISOAuth2RestClient = new OAuth2RestClient(getPrimaryISURI(), tenantInfo);
        userSharingRestClient = new UserSharingRestClient(getPrimaryISURI(), tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, getPrimaryISURI(),
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass())));
        orgMgtRestClient.authorizeAPIForB2BApp(ASSOCIATION_MGT_API_RESOURCE_NAME, API_RESOURCE_ORG_TYPE,
                ASSOCIATION_MGT_API_SCOPES);
    }

    @Test(priority = 2, dependsOnMethods = "testInit")
    public void testCreateSubOrganization() throws Exception {

        String m2mToken = orgMgtRestClient.getM2MAccessToken();
        organizationId = orgMgtRestClient.addOrganizationWithToken(
                "fed_assoc_sub_org", "fed_assoc_sub_org", m2mToken);
        assertNotNull(organizationId, "Organization ID should not be null.");

        switchedM2MToken = orgMgtRestClient.switchM2MToken(organizationId);
        assertNotNull(switchedM2MToken, "Switched M2M token should not be null.");
    }

    @Test(priority = 3, dependsOnMethods = "testCreateSubOrganization")
    public void testCreateApplicationInSecondaryIS() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(new ArrayList<>(Collections.singletonList(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)));
        oidcConfig.addCallbackURLsItem(PRIMARY_IS_CALLBACK_URL);

        ApplicationModel app = new ApplicationModel()
                .name(SECONDARY_IS_APP_NAME)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig))
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));

        secondaryISAppId = addApplication(PORT_OFFSET_1, app);
        assertNotNull(secondaryISAppId, "Secondary IS application ID should not be null.");

        OpenIDConnectConfiguration createdOidcConfig = getOIDCInboundDetailsOfApplication(PORT_OFFSET_1,
                secondaryISAppId);
        assertNotNull(createdOidcConfig, "Secondary IS OIDC configuration should not be null.");
        secondaryISClientId = createdOidcConfig.getClientId();
        secondaryISClientSecret = createdOidcConfig.getClientSecret();
        assertNotNull(secondaryISClientId, "Secondary IS client ID should not be null.");
        assertNotNull(secondaryISClientSecret, "Secondary IS client secret should not be null.");
    }

    @Test(priority = 4, dependsOnMethods = "testCreateApplicationInSecondaryIS")
    public void testCreateApplicationInSubOrg() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem(OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE);
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        ApplicationModel app = new ApplicationModel()
                .name(SUB_ORG_APP_NAME)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig))
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true))
                .claimConfiguration(new ClaimConfiguration()
                        .subject(new SubjectConfig().useMappedLocalSubject(true)));

        subOrgAppId = primaryISOAuth2RestClient.createOrganizationApplication(app, switchedM2MToken);
        assertNotNull(subOrgAppId, "Sub-organization application ID should not be null.");

        OpenIDConnectConfiguration createdOidcConfig =
                primaryISOAuth2RestClient.getOIDCInboundDetailsOfOrganizationApp(subOrgAppId, switchedM2MToken);
        assertNotNull(createdOidcConfig, "Sub-org OIDC configuration should not be null.");
        subOrgClientId = createdOidcConfig.getClientId();
        subOrgClientSecret = createdOidcConfig.getClientSecret();
        assertNotNull(subOrgClientId, "Sub-org client ID should not be null.");
        assertNotNull(subOrgClientSecret, "Sub-org client secret should not be null.");
    }

    @Test(priority = 5, dependsOnMethods = "testCreateApplicationInSubOrg")
    public void testCreateFederatedIdpInSubOrg() throws Exception {

        FederatedAuthenticator oidcAuthenticator = new FederatedAuthenticator()
                .authenticatorId(OIDC_AUTHENTICATOR_ID)
                .name(OIDC_AUTHENTICATOR_NAME)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.IDP_NAME)
                        .value(FEDERATED_IDP_NAME))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_ID)
                        .value(secondaryISClientId))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.CLIENT_SECRET)
                        .value(secondaryISClientSecret))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL)
                        .value(SECONDARY_IS_AUTHORIZE_ENDPOINT))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL)
                        .value(SECONDARY_IS_TOKEN_ENDPOINT))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.CALLBACK_URL)
                        .value(PRIMARY_IS_CALLBACK_URL))
                .addProperty(new Property()
                        .key(IdentityConstants.Authenticator.OIDC.OIDC_LOGOUT_URL)
                        .value(SECONDARY_IS_LOGOUT_ENDPOINT));

        FederatedAuthenticatorRequest oidcAuthnConfig = new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(OIDC_AUTHENTICATOR_ID)
                .addAuthenticator(oidcAuthenticator);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(FEDERATED_IDP_NAME)
                .federatedAuthenticators(oidcAuthnConfig);

        String subOrgIdpId = createIdpInSubOrg(idpPostRequest);
        assertNotNull(subOrgIdpId, "Sub-org federated IDP ID should not be null.");
    }

    @Test(priority = 6, dependsOnMethods = "testCreateFederatedIdpInSubOrg")
    public void testUpdateSubOrgAppAuthSequence() {

        AuthenticationSequence authSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator()
                                .idp(FEDERATED_IDP_NAME)
                                .authenticator(OIDC_AUTHENTICATOR_NAME)));

        ApplicationPatchModel patchModel = new ApplicationPatchModel();
        patchModel.setAuthenticationSequence(authSequence);

        primaryISOAuth2RestClient.updateSubOrgApplication(subOrgAppId, patchModel, switchedM2MToken);
    }

    @Test(priority = 7, dependsOnMethods = "testUpdateSubOrgAppAuthSequence")
    public void testCreateRootOrgUser() throws Exception {

        UserObject rootUser = new UserObject();
        rootUser.setUserName(ROOT_USER_USERNAME);
        rootUser.setPassword(ROOT_USER_PASSWORD);
        rootUser.addEmail(new Email().value(ROOT_USER_EMAIL));

        rootUserId = primaryISSCIM2RestClient.createUser(rootUser);
        assertNotNull(rootUserId, "Root organization user ID should not be null.");
    }

    @Test(priority = 8, dependsOnMethods = "testCreateRootOrgUser")
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

        boolean isUserShared = primaryISSCIM2RestClient.isSharedUserCreationCompleted(userSearchReq, switchedM2MToken);
        assertTrue(isUserShared, "User should be shared to the sub-organization.");

        // Retrieve the shared user's ID in the sub-organization.
        org.json.simple.JSONObject searchResult = primaryISSCIM2RestClient.searchSubOrgUser(
                userSearchReq, switchedM2MToken);
        org.json.simple.JSONArray resources = (org.json.simple.JSONArray) searchResult.get("Resources");
        assertNotNull(resources, "Resources array should not be null in search results.");
        assertFalse(resources.isEmpty(), "Should have at least one shared user.");
        org.json.simple.JSONObject firstUser = (org.json.simple.JSONObject) resources.get(0);
        sharedUserIdInSubOrg = (String) firstUser.get("id");
        assertNotNull(sharedUserIdInSubOrg, "Shared user ID in sub-org should not be null.");
    }

    @Test(priority = 9, dependsOnMethods = "testShareUserToSubOrg")
    public void testCreateUserInSecondaryIS() throws Exception {

        UserObject secondaryUser = new UserObject();
        secondaryUser.setUserName(SECONDARY_IS_USER_USERNAME);
        secondaryUser.setPassword(SECONDARY_IS_USER_PASSWORD);

        secondaryISUserId = secondaryISSCIM2RestClient.createUser(secondaryUser);
        assertNotNull(secondaryISUserId, "Secondary IS user ID should not be null.");
    }

    @Test(priority = 10, dependsOnMethods = "testCreateUserInSecondaryIS")
    public void testCreateFederatedAssociation() throws Exception {

        createFederatedAssociationInSubOrg(sharedUserIdInSubOrg, FEDERATED_IDP_NAME, secondaryISUserId);
    }

    @Test(priority = 11, dependsOnMethods = "testCreateFederatedAssociation")
    public void testAuthenticateViaFederatedLogin() throws Exception {

        // Step 1: Send authorize request to sub-org.
        String subOrgAuthorizeUrl = getPrimaryISURI() + TENANT_PATH + SUPER_TENANT + ORGANIZATION_PATH +
                organizationId + OAUTH_2_AUTHORIZE;

        List<NameValuePair> authorizeParams = new ArrayList<>();
        authorizeParams.add(new BasicNameValuePair("response_type", "code"));
        authorizeParams.add(new BasicNameValuePair("client_id", subOrgClientId));
        authorizeParams.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
        authorizeParams.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, authorizeParams, subOrgAuthorizeUrl);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Authorize response location header should not be null.");
        EntityUtils.consume(response.getEntity());

        // Step 2: Follow redirect to commonauth which will redirect to the federated IDP (secondary IS).
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Commonauth redirect to federated IDP should not be null.");
        EntityUtils.consume(response.getEntity());

        // Step 3: Extract sessionDataKey from secondary IS login page redirect.
        String secondaryISSessionDataKey = DataExtractUtil.getParamFromURIString(
                locationHeader.getValue(), "sessionDataKey");
        assertNotNull(secondaryISSessionDataKey, "Secondary IS session data key should not be null.");

        // Step 4: Authenticate at the secondary IS.
        List<NameValuePair> loginParams = new ArrayList<>();
        loginParams.add(new BasicNameValuePair("sessionDataKey", secondaryISSessionDataKey));
        loginParams.add(new BasicNameValuePair("username", SECONDARY_IS_USER_USERNAME));
        loginParams.add(new BasicNameValuePair("password", SECONDARY_IS_USER_PASSWORD));

        response = sendPostRequestWithParameters(client, loginParams, SECONDARY_IS_COMMONAUTH_URL);
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Secondary IS login response location header should not be null.");
        EntityUtils.consume(response.getEntity());

        // Step 5: Follow redirect chain back through secondary IS authorize to primary IS commonauth.
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Secondary IS authorize callback redirect should not be null.");
        EntityUtils.consume(response.getEntity());

        // Step 6: Follow redirect to primary IS commonauth (federated response processing).
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Primary IS commonauth redirect should not be null.");
        EntityUtils.consume(response.getEntity());

        // Step 7: Follow redirect to sub-org authorize endpoint to get the authorization code.
        response = sendGetRequest(client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        assertNotNull(locationHeader, "Sub-org authorize redirect to callback should not be null.");
        EntityUtils.consume(response.getEntity());

        authorizationCode = DataExtractUtil.getParamFromURIString(locationHeader.getValue(), "code");
        assertNotNull(authorizationCode, "Authorization code should not be null.");
    }

    @Test(priority = 12, dependsOnMethods = "testAuthenticateViaFederatedLogin")
    public void testGetAccessTokenAndVerifySubClaim() throws Exception {

        String subOrgTokenUrl = getPrimaryISURI() + TENANT_PATH + SUPER_TENANT + ORGANIZATION_PATH + organizationId +
                OAUTH_2_TOKEN;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("code", authorizationCode));
        params.add(new BasicNameValuePair("grant_type", OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        params.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Authorization", "Basic " +
                Base64.encodeBase64String((subOrgClientId + ":" + subOrgClientSecret).getBytes()).trim()));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, params, subOrgTokenUrl);
        assertNotNull(response, "Token endpoint response should not be null.");

        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseBody);

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

        if (secondaryISAppId != null) {
            try {
                deleteApplication(PORT_OFFSET_1, secondaryISAppId);
            } catch (Exception e) {
                log.error("Failed to delete secondary IS application: " + secondaryISAppId, e);
            }
        }
        if (organizationId != null && orgMgtRestClient != null) {
            try {
                orgMgtRestClient.deleteOrganization(organizationId);
            } catch (Exception e) {
                log.error("Failed to delete organization: " + organizationId, e);
            }
        }
        if (rootUserId != null && primaryISSCIM2RestClient != null) {
            try {
                primaryISSCIM2RestClient.deleteUser(rootUserId);
            } catch (Exception e) {
                log.error("Failed to delete root org user: " + rootUserId, e);
            }
        }
        if (secondaryISUserId != null && secondaryISSCIM2RestClient != null) {
            try {
                secondaryISSCIM2RestClient.deleteUser(secondaryISUserId);
            } catch (Exception e) {
                log.error("Failed to delete secondary IS user: " + secondaryISUserId, e);
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (primaryISSCIM2RestClient != null) {
            try {
                primaryISSCIM2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (secondaryISSCIM2RestClient != null) {
            try {
                secondaryISSCIM2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (primaryISOAuth2RestClient != null) {
            try {
                primaryISOAuth2RestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (orgMgtRestClient != null) {
            try {
                orgMgtRestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
        if (userSharingRestClient != null) {
            try {
                userSharingRestClient.closeHttpClient();
            } catch (Exception e) {
                log.error("Failed to close HTTP client.", e);
            }
        }
    }

    /**
     * Create an Identity Provider in the sub-organization using the switched M2M token.
     */
    private String createIdpInSubOrg(IdentityProviderPOSTRequest idpRequest) throws Exception {

        String endPointUrl = getPrimaryISURI() + "o/api/server/v1/identity-providers";
        String jsonRequest = new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(idpRequest);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endPointUrl);
            request.setHeader("Authorization", "Bearer " + switchedM2MToken);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
            request.setEntity(new StringEntity(jsonRequest));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                        "Sub-org IDP creation failed. Response: " + responseBody);
                JSONParser parser = new JSONParser();
                org.json.simple.JSONObject jsonResponse =
                        (org.json.simple.JSONObject) parser.parse(responseBody);
                return jsonResponse.get("id").toString();
            }
        }
    }

    /**
     * Create a federated association for a user in the sub-organization.
     */
    private void createFederatedAssociationInSubOrg(String userId, String idpName,
                                                    String federatedUserId) throws Exception {

        String endPointUrl = getPrimaryISURI() + "o/api/users/v1/" + userId + "/federated-associations";
        String body = "{\"idp\":\"" + idpName + "\",\"federatedUserId\":\"" + federatedUserId + "\"}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endPointUrl);
            request.setHeader("Authorization", "Bearer " + switchedM2MToken);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
            request.setEntity(new StringEntity(body));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                int statusCode = response.getStatusLine().getStatusCode();
                Assert.assertTrue(statusCode == HttpServletResponse.SC_OK ||
                                statusCode == HttpServletResponse.SC_CREATED ||
                                statusCode == HttpServletResponse.SC_NO_CONTENT,
                        "Federated association creation failed with status: " + statusCode +
                                ". Response: " + responseBody);
            }
        }
    }

    private String getPrimaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT);
    }

    private String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    private HttpResponse sendPostRequestWithParameters(CloseableHttpClient httpClient,
                                                       List<NameValuePair> urlParameters,
                                                       String url) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(request);
    }

    private HttpResponse sendGetRequest(CloseableHttpClient httpClient, String locationURL) throws IOException {

        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return httpClient.execute(getRequest);
    }

    private HttpResponse sendPostRequest(CloseableHttpClient httpClient, List<Header> headerList,
                                         List<NameValuePair> urlParameters, String url) throws IOException {

        HttpPost request = new HttpPost(url);
        request.setHeaders(headerList.toArray(new Header[0]));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(request);
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
