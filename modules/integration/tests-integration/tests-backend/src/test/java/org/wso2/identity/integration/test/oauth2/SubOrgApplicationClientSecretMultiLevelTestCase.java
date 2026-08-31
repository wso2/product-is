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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretCreationRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretList;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.GRANT_TYPE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;

/**
 * Tests the multiple client secrets feature across a multi level B2B organization hierarchy. A root application is
 * shared with a level one organization, a level two organization created under it and a level one sibling, and
 * every client secret change performed at the root is observed at the token endpoint of each organization.
 * Organization perspective secret operations are exercised on applications created natively in an organization.
 */
public class SubOrgApplicationClientSecretMultiLevelTestCase extends SubOrgClientSecretTestBase {

    private static final String ROOT_APPLICATION_NAME = "MultiLevelClientSecretApp";
    private static final String SECRET_MGT_APPLICATION_NAME = "MultiLevelClientSecretMgtApp";
    private static final String NATIVE_APPLICATION_NAME = "MultiLevelNativeClientSecretApp";
    private static final String LEVEL2_NATIVE_APPLICATION_NAME = "MultiLevelNativeClientSecretAppInLevel2Org";
    private static final String LEVEL1_ORGANIZATION_NAME = "mcs-multi-level-org1";
    private static final String LEVEL1_ORGANIZATION_HANDLE = "mcsmultilevelorg1";
    private static final String LEVEL1_SIBLING_ORGANIZATION_NAME = "mcs-multi-level-org2";
    private static final String LEVEL1_SIBLING_ORGANIZATION_HANDLE = "mcsmultilevelorg2";
    private static final String LEVEL2_ORGANIZATION_NAME = "mcs-multi-level-org1-1";
    private static final String LEVEL2_ORG_END_USER_USERNAME = "level2SecretUser";
    private static final String LEVEL2_ORG_END_USER_PASSWORD = "Level2SecretUser@wso2";
    private static final String LEVEL2_ORG_END_USER_EMAIL = "level2secretuser@wso2.com";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";
    private static final String ORG_ORGANIZATION_MGT_API = "/o/api/server/v1/organizations";

    private static final String ORG_CLIENT_SECRET_VIEW_SCOPE = "internal_org_application_mgt_client_secret_view";

    private static final String REFRESH_TOKEN = "refresh_token";

    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;

    private String level1OrgId;
    private String level1SiblingOrgId;
    private String level2OrgId;
    private String level2OrgEndUserId;

    private String level1OrgAdminToken;
    private String level1SiblingOrgAdminToken;
    private String level2OrgAdminToken;

    private String level1TokenEndpoint;
    private String level1SiblingTokenEndpoint;
    private String level2TokenEndpoint;
    private List<String> organizationTreeTokenEndpoints;

    private String sharedApplicationIdInLevel2Org;
    private String nativeApplicationId;
    private String nativeApplicationClientId;
    private String nativeApplicationInitialSecretValue;
    private String level2NativeApplicationId;

    private String rootCreatedSecretValue;

    @BeforeClass(alwaysRun = true)
    public void initTestClass() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        client = createHttpClient();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        /* The shared resource is extended with the organization management API, which creating and deleting the
           level two organization under a switched-token flow requires. */
        JSONObject authorizedApis = new JSONObject(
                RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass()));
        authorizedApis.put(ORG_ORGANIZATION_MGT_API, new JSONArray(Arrays.asList(
                "internal_org_organization_view", "internal_org_organization_create",
                "internal_org_organization_delete")));
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL, authorizedApis);

        createRootApplication(ROOT_APPLICATION_NAME);
        createClientSecretManagementApplication(SECRET_MGT_APPLICATION_NAME,
                Collections.singletonList(ORG_APPLICATION_MGT_API));
        /* The management application is shared before the organizations are created, so that every organization
           accepts the organization switch grant from the moment it exists. */
        shareApplicationWithAllChildOrganizations(secretMgtApplicationId);

        /* The organization tree is created before the root application is shared so that a share with all child
           organizations reaches the level two organization without depending on the propagation of the sharing
           policy to organizations created later. */
        level1OrgId = orgMgtRestClient.addOrganization(LEVEL1_ORGANIZATION_NAME, LEVEL1_ORGANIZATION_HANDLE);
        level1SiblingOrgId = orgMgtRestClient.addOrganization(LEVEL1_SIBLING_ORGANIZATION_NAME,
                LEVEL1_SIBLING_ORGANIZATION_HANDLE);
        level2OrgId = orgMgtRestClient.addSubOrganization(LEVEL2_ORGANIZATION_NAME, level1OrgId);
        assertNotNull(level1OrgId, "Level one organization id should not be null.");
        assertNotNull(level1SiblingOrgId, "Level one sibling organization id should not be null.");
        assertNotNull(level2OrgId, "Level two organization id should not be null.");

        level1TokenEndpoint = getOrgTokenEndpoint(level1OrgId);
        level1SiblingTokenEndpoint = getOrgTokenEndpoint(level1SiblingOrgId);
        level2TokenEndpoint = getOrgTokenEndpoint(level2OrgId);
        organizationTreeTokenEndpoints = Arrays.asList(level1TokenEndpoint, level2TokenEndpoint,
                level1SiblingTokenEndpoint);

        shareApplicationWithAllChildOrganizations(rootApplicationId);

        level1OrgAdminToken = getOrganizationSwitchedToken(SYSTEM_SCOPE, level1OrgId);
        level1SiblingOrgAdminToken = getOrganizationSwitchedToken(SYSTEM_SCOPE, level1SiblingOrgId);
        level2OrgAdminToken = getOrganizationSwitchedToken(SYSTEM_SCOPE, level2OrgId);

        waitForApplicationSharedToOrganization(ROOT_APPLICATION_NAME, level1OrgAdminToken);
        waitForApplicationSharedToOrganization(ROOT_APPLICATION_NAME, level1SiblingOrgAdminToken);
        sharedApplicationIdInLevel2Org = waitForApplicationSharedToOrganization(ROOT_APPLICATION_NAME,
                level2OrgAdminToken);

        createLevel2OrgEndUser();
        createNativeApplications();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() {

        if (level2OrgEndUserId != null) {
            try {
                scim2RestClient.deleteSubOrgUser(level2OrgEndUserId, orgMgtRestClient.switchM2MToken(level2OrgId));
            } catch (Exception e) {
                log.error("Failed to delete the level two organization user: " + level2OrgEndUserId, e);
            }
        }
        if (level2OrgId != null) {
            try {
                orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
            } catch (Exception e) {
                log.error("Failed to delete the level two organization: " + level2OrgId, e);
            }
        }
        if (level1OrgId != null) {
            try {
                orgMgtRestClient.deleteOrganization(level1OrgId);
            } catch (Exception e) {
                log.error("Failed to delete the organization: " + level1OrgId, e);
            }
        }
        if (level1SiblingOrgId != null) {
            try {
                orgMgtRestClient.deleteOrganization(level1SiblingOrgId);
            } catch (Exception e) {
                log.error("Failed to delete the organization: " + level1SiblingOrgId, e);
            }
        }
        if (rootApplicationId != null) {
            try {
                restClient.deleteApplication(rootApplicationId);
            } catch (Exception e) {
                log.error("Failed to delete the application: " + rootApplicationId, e);
            }
        }
        if (secretMgtApplicationId != null) {
            try {
                restClient.deleteApplication(secretMgtApplicationId);
            } catch (Exception e) {
                log.error("Failed to delete the application: " + secretMgtApplicationId, e);
            }
        }
        try {
            scim2RestClient.closeHttpClient();
            orgMgtRestClient.closeHttpClient();
            restClient.closeHttpClient();
            client.close();
        } catch (IOException e) {
            log.error("Failed to close the HTTP clients.", e);
        }
    }

    @Test(groups = "wso2.is", priority = 1, description = "Authenticate the shared application with its initial " +
            "secret at every level of the organization tree.")
    public void testSharedApplicationAuthenticatesAtEveryLevel() throws Exception {

        assertTrue(StringUtils.isNotBlank(sharedApplicationIdInLevel2Org),
                "Shared application id in the level two organization should not be blank.");
        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootInitialSecretValue),
                    HttpStatus.SC_OK, "The initial secret of the application should authenticate at " +
                            tokenEndpoint);
        }
    }

    @Test(groups = "wso2.is", priority = 2, description = "Authenticate at every level of the organization tree " +
            "with a client secret created at the root organization.",
            dependsOnMethods = "testSharedApplicationAuthenticatesAtEveryLevel")
    public void testClientSecretCreatedAtRootPropagatesToEveryLevel() throws Exception {

        ClientSecretResponse clientSecret = restClient.createClientSecret(rootApplicationId,
                new ClientSecretCreationRequest().expiresAt(0L));
        rootCreatedSecretValue = clientSecret.getSecretValue();
        assertTrue(StringUtils.isNotBlank(rootCreatedSecretValue),
                "The created client secret value is not present in the creation response.");

        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootCreatedSecretValue),
                    HttpStatus.SC_OK, "A client secret created at the root organization should authenticate at " +
                            tokenEndpoint);
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootInitialSecretValue),
                    HttpStatus.SC_OK, "A non latest client secret should keep authenticating at " + tokenEndpoint);
        }
    }

    @Test(groups = "wso2.is", priority = 3, description = "Run the authorization code grant of the shared " +
            "application at the level two organization with a non latest client secret.",
            dependsOnMethods = "testClientSecretCreatedAtRootPropagatesToEveryLevel")
    public void testAuthorizationCodeGrantWithNonLatestSecretAtLevel2Org() throws Exception {

        String authorizationCode = getAuthorizationCodeFromOrgLogin(level2OrgId, LEVEL2_ORG_END_USER_USERNAME,
                LEVEL2_ORG_END_USER_PASSWORD);
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        parameters.add(new BasicNameValuePair("code", authorizationCode));
        parameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        HttpResponse response = sendTokenRequest(level2TokenEndpoint, parameters, rootClientId, rootInitialSecretValue);
        JSONObject codeGrantResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(codeGrantResponse.has(ACCESS_TOKEN),
                "Access token is not present in the authorization code grant response.");
        assertTrue(codeGrantResponse.has(REFRESH_TOKEN),
                "Refresh token is not present in the authorization code grant response.");
    }

    @Test(groups = "wso2.is", priority = 4, description = "Regenerate the client secret at the root organization " +
            "and validate the effect at every level of the organization tree.",
            dependsOnMethods = "testAuthorizationCodeGrantWithNonLatestSecretAtLevel2Org")
    public void testRegenerateAtRootPropagatesToEveryLevel() throws Exception {

        /*
         Both secrets are used at every level immediately before the regeneration to warm the per organization
         application caches, so a replaced secret that keeps authenticating below is a stale cache entry the root
         regeneration failed to evict.
        */
        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootInitialSecretValue),
                    HttpStatus.SC_OK, "A non latest client secret should authenticate at " + tokenEndpoint +
                            " before the regeneration.");
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootCreatedSecretValue),
                    HttpStatus.SC_OK, "The latest client secret should authenticate at " + tokenEndpoint +
                            " before the regeneration.");
        }

        OpenIDConnectConfiguration oidcConfig = restClient.regenerateClientSecret(rootApplicationId);
        String regeneratedSecretValue = oidcConfig.getClientSecret();
        assertTrue(StringUtils.isNotBlank(regeneratedSecretValue),
                "The regenerated client secret is not present in the response.");

        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootInitialSecretValue),
                    HttpStatus.SC_UNAUTHORIZED, "A client secret replaced by a regeneration at the root should " +
                            "stop authenticating at " + tokenEndpoint + " immediately.");
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, rootCreatedSecretValue),
                    HttpStatus.SC_UNAUTHORIZED, "A client secret replaced by a regeneration at the root should " +
                            "stop authenticating at " + tokenEndpoint + " immediately.");
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, regeneratedSecretValue),
                    HttpStatus.SC_OK, "The regenerated client secret should authenticate at " + tokenEndpoint +
                            " immediately.");
        }
    }

    @Test(groups = "wso2.is", priority = 5, description = "Authenticate at every organization in the tree with a " +
            "client secret deleted at the root organization.",
            dependsOnMethods = "testRegenerateAtRootPropagatesToEveryLevel")
    public void testClientSecretDeletedAtRootIsRejectedAtEveryLevel() throws Exception {

        ClientSecretResponse clientSecret = restClient.createClientSecret(rootApplicationId,
                new ClientSecretCreationRequest().expiresAt(0L));
        String secretValueUnderTest = clientSecret.getSecretValue();

        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, secretValueUnderTest),
                    HttpStatus.SC_OK, "A client secret created at the root organization should authenticate at " +
                            tokenEndpoint + ".");
        }

        /* Deleting the latest secret is rejected with a conflict by contract, hence the regenerated secret is
           removed and a further secret is created at the root to demote the secret under test. */
        ClientSecretList clientSecrets = restClient.getClientSecrets(rootApplicationId);
        assertEquals(restClient.deleteClientSecret(rootApplicationId, getNonLatestSecret(clientSecrets).getSecretId()),
                HttpStatus.SC_NO_CONTENT, "Deleting a non latest client secret at the root organization should " +
                        "succeed.");
        restClient.createClientSecret(rootApplicationId, new ClientSecretCreationRequest().expiresAt(0L));

        assertEquals(restClient.deleteClientSecret(rootApplicationId, clientSecret.getSecretId()),
                HttpStatus.SC_NO_CONTENT, "Deleting the demoted client secret at the root organization should " +
                        "succeed.");
        for (String tokenEndpoint : organizationTreeTokenEndpoints) {
            assertEquals(getClientCredentialsTokenStatusCode(tokenEndpoint, rootClientId, secretValueUnderTest),
                    HttpStatus.SC_UNAUTHORIZED, "A client secret deleted at the root organization should stop " +
                            "authenticating at " + tokenEndpoint + " immediately.");
        }
    }

    @Test(groups = "wso2.is", priority = 6, description = "Authenticate an application created natively in the " +
            "level one organization at the sibling and the child organization token endpoints.",
            dependsOnMethods = "testClientSecretDeletedAtRootIsRejectedAtEveryLevel")
    public void testNativeOrganizationApplicationIsIsolatedFromOtherOrganizations() throws Exception {

        assertEquals(getClientCredentialsTokenStatusCode(level1TokenEndpoint, nativeApplicationClientId,
                        nativeApplicationInitialSecretValue), HttpStatus.SC_OK,
                "A natively created application should authenticate at its own organization token endpoint.");
        assertEquals(getClientCredentialsTokenStatusCode(level1SiblingTokenEndpoint, nativeApplicationClientId,
                        nativeApplicationInitialSecretValue), HttpStatus.SC_UNAUTHORIZED,
                "A natively created application should not authenticate at a sibling organization token endpoint.");
        assertEquals(getClientCredentialsTokenStatusCode(level2TokenEndpoint, nativeApplicationClientId,
                        nativeApplicationInitialSecretValue), HttpStatus.SC_UNAUTHORIZED,
                "A natively created application should not authenticate at a child organization token endpoint.");

        ClientSecretResponse nativeClientSecret = restClient.createClientSecretOfOrganizationApp(nativeApplicationId,
                new ClientSecretCreationRequest().expiresAt(0L), level1OrgAdminToken);
        assertEquals(getClientCredentialsTokenStatusCode(level1TokenEndpoint, nativeApplicationClientId,
                        nativeClientSecret.getSecretValue()), HttpStatus.SC_OK,
                "A client secret created for a natively created application should authenticate at its own " +
                        "organization token endpoint.");
        assertEquals(getClientCredentialsTokenStatusCode(level1SiblingTokenEndpoint, nativeApplicationClientId,
                        nativeClientSecret.getSecretValue()), HttpStatus.SC_UNAUTHORIZED,
                "A client secret created for a natively created application should not authenticate at a sibling " +
                        "organization token endpoint.");

        /* The converse direction: a secret operation on the shared root application leaves the natively created
           application of the same organization untouched. */
        OpenIDConnectConfiguration oidcConfig = restClient.regenerateClientSecret(rootApplicationId);
        String regeneratedSecretValue = oidcConfig.getClientSecret();
        assertEquals(getClientCredentialsTokenStatusCode(level1TokenEndpoint, nativeApplicationClientId,
                        nativeApplicationInitialSecretValue), HttpStatus.SC_OK,
                "A regeneration on the shared root application should not affect a natively created application.");
        assertEquals(getClientCredentialsTokenStatusCode(level1TokenEndpoint, rootClientId, regeneratedSecretValue),
                HttpStatus.SC_OK, "The regenerated client secret of the shared application should authenticate at " +
                        "the level one organization.");
    }

    @Test(groups = "wso2.is", priority = 7, description = "Invoke the client secret endpoints of an application " +
            "created natively in the level two organization with a token carrying only the organization client " +
            "secret view scope.",
            dependsOnMethods = "testNativeOrganizationApplicationIsIsolatedFromOtherOrganizations")
    public void testLevel2OrgClientSecretViewScope() throws Exception {

        String viewToken = getOrganizationSwitchedToken(ORG_CLIENT_SECRET_VIEW_SCOPE, level2OrgId);

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(level2NativeApplicationId,
                viewToken);
        assertEquals(clientSecrets.getCount(), Integer.valueOf(1),
                "The organization client secret view scope should grant listing the client secrets at the level " +
                        "two organization.");
        assertTrue(Boolean.TRUE.equals(clientSecrets.getList().get(0).getLatest()),
                "The single listed client secret is not marked as the latest secret.");

        /* The application holds a single secret, hence the create attempt below is rejected for the missing scope
           and not for the maximum secret count. */
        assertEquals(restClient.getClientSecretCreationStatusCodeOfOrganizationApp(level2NativeApplicationId,
                        new ClientSecretCreationRequest().expiresAt(0L), viewToken), HttpStatus.SC_FORBIDDEN,
                "The organization client secret view scope should not grant creating a client secret at the level " +
                        "two organization.");
    }

    private void createNativeApplications() throws Exception {

        nativeApplicationId = createNativeApplicationInOrganization(NATIVE_APPLICATION_NAME, level1OrgAdminToken,
                level1OrgId);
        OpenIDConnectConfiguration oidcConfig = restClient.getOIDCInboundDetailsOfOrganizationApp(
                nativeApplicationId, level1OrgAdminToken);
        nativeApplicationClientId = oidcConfig.getClientId();
        nativeApplicationInitialSecretValue = oidcConfig.getClientSecret();
        assertNotNull(nativeApplicationClientId, "Client id of the natively created application should not be null.");
        assertNotNull(nativeApplicationInitialSecretValue,
                "Client secret of the natively created application should not be null.");

        level2NativeApplicationId = createNativeApplicationInOrganization(LEVEL2_NATIVE_APPLICATION_NAME,
                level2OrgAdminToken, level2OrgId);
        assertNotNull(level2NativeApplicationId,
                "Application id of the natively created level two organization application should not be null.");
    }

    /**
     * Creates an application natively in the given organization through the organization perspective API.
     *
     * @param applicationName Name of the application to create.
     * @param orgAdminToken   Organization switched token of the organization to create the application in.
     * @param organizationId  Id of the organization to create the application in.
     * @return Application id of the created application within that organization.
     * @throws Exception If an error occurred while creating the application.
     */
    private String createNativeApplicationInOrganization(String applicationName, String orgAdminToken,
                                                         String organizationId) throws Exception {

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .tokenType(ApplicationConfig.TokenType.OPAQUE)
                .grantTypes(Collections.singletonList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS))
                .claimsList(Collections.emptyList())
                .expiryTime(300)
                .skipConsent(true)
                .build();

        ApplicationResponseModel nativeApplication = addOrganizationApplication(applicationName, applicationConfig,
                orgAdminToken, organizationId);
        return nativeApplication.getId();
    }

    private void createLevel2OrgEndUser() throws Exception {

        UserObject endUser = new UserObject();
        endUser.setUserName(LEVEL2_ORG_END_USER_USERNAME);
        endUser.setPassword(LEVEL2_ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(LEVEL2_ORG_END_USER_EMAIL));
        level2OrgEndUserId = scim2RestClient.createSubOrgUser(endUser, orgMgtRestClient.switchM2MToken(level2OrgId));
        assertNotNull(level2OrgEndUserId, "Level two organization user id should not be null.");
    }

    private String getOrgTokenEndpoint(String organizationId) {

        return getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain(), organizationId);
    }
}
