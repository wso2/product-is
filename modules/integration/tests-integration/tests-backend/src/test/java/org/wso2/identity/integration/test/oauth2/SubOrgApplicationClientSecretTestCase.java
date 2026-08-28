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

import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URLEncodedUtils;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.dataprovider.model.ApplicationConfig;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretCreationRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretList;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClientSecretResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.API_SERVER_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.CONTENT_TYPE_ATTRIBUTE;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.USER_AGENT_ATTRIBUTE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZATION_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.BASIC_HEADER;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.CALLBACK_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.COMMON_AUTH_URL;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.GRANT_TYPE_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.INTRO_SPEC_ENDPOINT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Integration tests for the multiple client secrets feature on B2B sub-organization applications. Two
 * applications are exercised in a single sub-organization.
 * <p>
 * The client secret contract of the organization perspective API
 * ({@code /o/api/server/v1/applications/{id}/inbound-protocols/oidc/secrets}) is pinned on an application created
 * natively in the sub-organization, since that API always operates on the secret set of the addressed application
 * in the sub-organization tenant.
 * <p>
 * A root organization application shared with the sub-organization covers the cross organization rows. A shared
 * application receives its own client id in the sub-organization, while the client secrets of the root
 * application authenticate at the sub-organization token endpoint
 * ({@code /t/{tenant}/o/{orgId}/oauth2/token}) through the organization hierarchy walk of the client
 * authenticator. Every shared application row therefore drives the change at the root organization and observes
 * it at the sub-organization token endpoint with the root client id.
 * <p>
 * Token introspection is performed at the root introspection endpoint with tenant administrator (user)
 * authentication because the client credential authenticated introspection path is not organization hierarchy
 * aware; only the organization independent assertions are made here.
 */
public class SubOrgApplicationClientSecretTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String ROOT_APPLICATION_NAME = "SubOrgClientSecretApp";
    private static final String SUB_ORG_APPLICATION_NAME = "SubOrgClientSecretNativeApp";
    private static final String SECRET_MGT_APPLICATION_NAME = "SubOrgClientSecretMgtApp";
    private static final String ORGANIZATION_NAME = "sub-org-client-secret-org";
    private static final String ORGANIZATION_HANDLE = "suborgclientsecret";
    private static final String ORG_END_USER_USERNAME = "subOrgSecretUser";
    private static final String ORG_END_USER_PASSWORD = "SubOrgSecretUser@wso2";
    private static final String ORG_END_USER_EMAIL = "suborgsecretuser@wso2.com";
    private static final String MGT_APP_AUTHORIZED_API_RESOURCES = "management-app-authorized-apis.json";

    private static final String ORG_APPLICATION_MGT_API = "/o/api/server/v1/applications";
    private static final String ORG_DCR_API = "/o/api/identity/oauth2/dcr/v1.1/register";
    private static final String ORG_CLIENT_SECRET_CREATE_SCOPE = "internal_org_application_mgt_client_secret_create";
    private static final String ORG_CLIENT_SECRET_VIEW_SCOPE = "internal_org_application_mgt_client_secret_view";
    private static final String ORG_CLIENT_SECRET_DELETE_SCOPE = "internal_org_application_mgt_client_secret_delete";
    private static final String SYSTEM_SCOPE = "SYSTEM";

    private static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    private static final String INBOUND_PROTOCOLS_OIDC_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRETS_PATH = "/secrets";
    private static final String DCR_REGISTER_ENDPOINT = "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register";

    private static final String CLIENT_NAME = "client_name";
    private static final String GRANT_TYPES = "grant_types";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
    private static final String EXT_PARAM_CLIENT_SECRET_EXPIRES_AT = "ext_param_client_secret_expires_at";
    private static final String ERROR = "error";
    private static final String INVALID_CLIENT_METADATA = "invalid_client_metadata";
    private static final String INVALID_GRANT = "invalid_grant";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACTIVE = "active";
    private static final String SWITCHING_ORGANIZATION = "switching_organization";
    private static final String TOKEN = "token";
    private static final String WRONG_CLIENT_SECRET = "sub-org-wrong-client-secret";

    private static final long FUTURE_EXPIRY_SECONDS = 3600L;
    private static final long SHORT_EXPIRY_SECONDS = 75L;
    private static final long EXPIRY_WAIT_MILLIS = 80000L;
    private static final long APPLICATION_SHARE_WAIT_MILLIS = 20000L;
    private static final int TOKEN_EXPIRY_SECONDS = 3600;

    private CloseableHttpClient client;
    private RestBaseClient restBaseClient;
    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;

    private String organizationId;
    private String orgEndUserId;
    private String rootApplicationId;
    private String sharedApplicationId;
    private String rootClientId;
    private String secretMgtApplicationId;
    private String secretMgtClientId;
    private String secretMgtClientSecret;
    private String orgAdminToken;
    private String subOrgTokenEndpoint;

    private String subOrgApplicationId;
    private String subOrgClientId;
    private String subOrgInitialSecretValue;
    private String subOrgCreatedSecretId;
    private String subOrgCreatedSecretValue;
    private String subOrgShortLivedSecretId;
    private String subOrgShortLivedSecretValue;

    private String rootInitialSecretValue;
    private String rootCreatedSecretValue;
    private String regeneratedSecretValue;
    private String subOrgAccessToken;
    private String subOrgRefreshToken;
    private final List<String> dcrClientIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void initTestClass() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        client = createHttpClient();
        restBaseClient = new RestBaseClient();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(MGT_APP_AUTHORIZED_API_RESOURCES, this.getClass())));

        createRootApplication();
        createClientSecretManagementApplication();

        organizationId = orgMgtRestClient.addOrganization(ORGANIZATION_NAME, ORGANIZATION_HANDLE);
        assertNotNull(organizationId, "Organization id should not be null.");
        subOrgTokenEndpoint = getRootTenantQualifiedOrgURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain(),
                organizationId);

        shareApplicationWithAllChildOrganizations(rootApplicationId);
        shareApplicationWithAllChildOrganizations(secretMgtApplicationId);

        orgAdminToken = getOrganizationSwitchedToken(SYSTEM_SCOPE);
        sharedApplicationId = waitForApplicationSharedToSubOrg(ROOT_APPLICATION_NAME, orgAdminToken);

        createSubOrgApplication();
        createOrgEndUser();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupTest() {

        for (String dcrClientId : dcrClientIds) {
            try {
                deleteDcrApplicationInSubOrg(dcrClientId);
            } catch (Exception e) {
                log.error("Failed to delete the sub-organization DCR application: " + dcrClientId, e);
            }
        }
        if (orgEndUserId != null) {
            try {
                scim2RestClient.deleteSubOrgUser(orgEndUserId, orgMgtRestClient.switchM2MToken(organizationId));
            } catch (Exception e) {
                log.error("Failed to delete the sub-organization user: " + orgEndUserId, e);
            }
        }
        /* The applications created inside the sub-organization are removed along with the organization. */
        if (organizationId != null) {
            try {
                orgMgtRestClient.deleteOrganization(organizationId);
            } catch (Exception e) {
                log.error("Failed to delete the organization: " + organizationId, e);
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
            restBaseClient.client.close();
            restClient.closeHttpClient();
            client.close();
        } catch (IOException e) {
            log.error("Failed to close the HTTP clients.", e);
        }
    }

    @Test(groups = "wso2.is", priority = 1, description = "Resolve the shared application in the sub-organization " +
            "and authenticate the root application client secret at the sub-organization token endpoint.")
    public void testSharedApplicationAuthenticatesAtSubOrgTokenEndpoint() throws Exception {

        /*
         The shared (fragment) application resolves in the sub-organization with a client id of its own, while the
         client secrets of the root application authenticate at the sub-organization token endpoint through the
         organization hierarchy walk of the client authenticator.
        */
        assertTrue(StringUtils.isNotBlank(sharedApplicationId),
                "Shared application id in the sub-organization should not be blank.");
        OpenIDConnectConfiguration sharedOidcConfig = getOIDCInboundDetailsOfOrganizationApplication(
                sharedApplicationId, orgAdminToken);
        assertTrue(StringUtils.isNotBlank(sharedOidcConfig.getClientId()),
                "Client id of the shared application should not be blank.");
        assertNotEquals(sharedOidcConfig.getClientId(), rootClientId,
                "The shared application should carry a client id of its own in the sub-organization.");

        JSONObject tokenResponse = getClientCredentialsTokenAtSubOrg(rootClientId, rootInitialSecretValue);
        assertTrue(tokenResponse.has(ACCESS_TOKEN), "Access token is not present in the token response.");
    }

    @Test(groups = "wso2.is", priority = 2, description = "Create a client secret of the sub-organization " +
            "application through the organization perspective API.",
            dependsOnMethods = "testSharedApplicationAuthenticatesAtSubOrgTokenEndpoint")
    public void testCreateClientSecretOfSubOrgApplication() throws Exception {

        long expiresAt = getCurrentTimeInSeconds() + FUTURE_EXPIRY_SECONDS;
        ClientSecretResponse clientSecret = restClient.createClientSecretOfOrganizationApp(subOrgApplicationId,
                new ClientSecretCreationRequest().expiresAt(expiresAt), orgAdminToken);

        assertNotNull(clientSecret.getSecretId(), "Secret id is not present in the creation response.");
        assertTrue(StringUtils.isNotBlank(clientSecret.getSecretValue()),
                "Secret value is not present in the creation response.");
        assertEquals(clientSecret.getExpiresAt(), Long.valueOf(expiresAt),
                "Requested expiry is not echoed in the creation response.");
        assertNotNull(clientSecret.getCreatedAt(), "Created time is not present in the creation response.");
        assertTrue(clientSecret.getCreatedAt() > 0 && clientSecret.getCreatedAt() < clientSecret.getExpiresAt(),
                "Created time of the secret is not a sane epoch second value.");
        assertEquals(clientSecret.getStatus(), ClientSecretResponse.StatusEnum.ACTIVE,
                "Newly created secret is not in the ACTIVE status.");
        assertTrue(clientSecret.getLatest(), "Newly created secret is not marked as the latest secret.");

        subOrgCreatedSecretId = clientSecret.getSecretId();
        subOrgCreatedSecretValue = clientSecret.getSecretValue();
    }

    @Test(groups = "wso2.is", priority = 3, description = "List the client secrets of the sub-organization " +
            "application through the organization perspective API.",
            dependsOnMethods = "testCreateClientSecretOfSubOrgApplication")
    public void testListClientSecretsOfSubOrgApplication() throws Exception {

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId,
                orgAdminToken);
        assertEquals(clientSecrets.getCount(), Integer.valueOf(2), "The application should have two client secrets.");
        assertEquals(clientSecrets.getList().size(), 2, "The returned secret list size does not match the count.");

        ClientSecretResponse latestSecret = getLatestSecret(clientSecrets);
        assertEquals(latestSecret.getSecretId(), subOrgCreatedSecretId,
                "The created secret is not the latest secret of the application.");
        assertEquals(latestSecret.getSecretValue(), subOrgCreatedSecretValue,
                "The listed secret value does not match the created secret value.");
        assertEquals(latestSecret.getStatus(), ClientSecretResponse.StatusEnum.ACTIVE,
                "The latest secret is not in the ACTIVE status.");
        assertNotNull(latestSecret.getCreatedAt(), "Created time is not present in the listed secret.");

        ClientSecretResponse initialSecret = getNonLatestSecret(clientSecrets);
        assertEquals(initialSecret.getSecretValue(), subOrgInitialSecretValue,
                "The listed secret value does not match the initial secret of the application.");
        assertEquals(initialSecret.getExpiresAt(), Long.valueOf(0),
                "The initial secret of the application should not carry an expiry.");
        assertEquals(initialSecret.getStatus(), ClientSecretResponse.StatusEnum.ACTIVE,
                "The initial secret of the application is not in the ACTIVE status.");
    }

    @Test(groups = "wso2.is", priority = 4, description = "Create a client secret beyond the maximum secret count " +
            "of the sub-organization application.", dependsOnMethods = "testListClientSecretsOfSubOrgApplication")
    public void testCreateClientSecretBeyondMaximumSecretCount() throws Exception {

        int statusCode = restClient.getClientSecretCreationStatusCodeOfOrganizationApp(subOrgApplicationId,
                new ClientSecretCreationRequest().expiresAt(getCurrentTimeInSeconds() + FUTURE_EXPIRY_SECONDS),
                orgAdminToken);
        assertEquals(statusCode, HttpStatus.SC_CONFLICT,
                "Creating a client secret beyond the maximum secret count should be rejected with a conflict.");
    }

    @Test(groups = "wso2.is", priority = 5, description = "Authenticate the sub-organization application at the " +
            "sub-organization token endpoint with every client secret state.",
            dependsOnMethods = "testCreateClientSecretBeyondMaximumSecretCount")
    public void testTokenEndpointAuthenticationMatrixInSubOrg() throws Exception {

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgCreatedSecretValue),
                HttpStatus.SC_OK, "The latest client secret should authenticate at the sub-organization token " +
                        "endpoint.");
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgInitialSecretValue),
                HttpStatus.SC_OK, "A non latest client secret should authenticate at the sub-organization token " +
                        "endpoint.");
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, WRONG_CLIENT_SECRET),
                HttpStatus.SC_UNAUTHORIZED, "A wrong client secret should not authenticate at the " +
                        "sub-organization token endpoint.");
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, StringUtils.EMPTY),
                HttpStatus.SC_UNAUTHORIZED, "A blank client secret should not authenticate at the " +
                        "sub-organization token endpoint.");
    }

    @Test(groups = "wso2.is", priority = 6, description = "Run the client credentials grant of the " +
            "sub-organization application with a non latest client secret.",
            dependsOnMethods = "testTokenEndpointAuthenticationMatrixInSubOrg")
    public void testClientCredentialsGrantWithNonLatestClientSecretInSubOrg() throws Exception {

        /*
         The authorization code and refresh token grants with a non latest secret run on the shared application in
         testGrantsOfSharedApplicationAtSubOrgTokenEndpoint.
        */
        JSONObject tokenResponse = getClientCredentialsTokenAtSubOrg(subOrgClientId, subOrgInitialSecretValue);
        assertTrue(tokenResponse.has(ACCESS_TOKEN),
                "Access token is not present in the client credentials grant response.");
    }

    @Test(groups = "wso2.is", priority = 7, description = "Delete the client secrets of the sub-organization " +
            "application through the organization perspective API.",
            dependsOnMethods = "testClientCredentialsGrantWithNonLatestClientSecretInSubOrg")
    public void testDeleteClientSecretOfSubOrgApplication() throws Exception {

        assertEquals(restClient.deleteClientSecretOfOrganizationApp(subOrgApplicationId, subOrgCreatedSecretId,
                orgAdminToken), HttpStatus.SC_CONFLICT, "Deleting the latest client secret should be rejected.");

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId,
                orgAdminToken);
        String initialSecretId = getNonLatestSecret(clientSecrets).getSecretId();
        assertEquals(restClient.deleteClientSecretOfOrganizationApp(subOrgApplicationId, initialSecretId,
                orgAdminToken), HttpStatus.SC_NO_CONTENT, "Deleting a non latest client secret should succeed.");

        clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId, orgAdminToken);
        assertEquals(clientSecrets.getCount(), Integer.valueOf(1),
                "The application should have a single client secret after the deletion.");
        assertEquals(clientSecrets.getList().get(0).getSecretId(), subOrgCreatedSecretId,
                "The remaining client secret is not the latest secret of the application.");
    }

    @Test(groups = "wso2.is", priority = 8, description = "Authenticate the sub-organization application with a " +
            "deleted client secret.", dependsOnMethods = "testDeleteClientSecretOfSubOrgApplication")
    public void testDeletedClientSecretIsRejectedInSubOrg() throws Exception {

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgInitialSecretValue),
                HttpStatus.SC_UNAUTHORIZED, "A deleted client secret should not authenticate at the " +
                        "sub-organization token endpoint.");
    }

    @Test(groups = "wso2.is", priority = 9, description = "Invoke the client secret endpoints of the " +
            "sub-organization application with a token carrying only the organization client secret view scope.",
            dependsOnMethods = "testDeletedClientSecretIsRejectedInSubOrg")
    public void testOrgClientSecretViewScope() throws Exception {

        String viewToken = getOrganizationSwitchedToken(ORG_CLIENT_SECRET_VIEW_SCOPE);

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId, viewToken);
        assertEquals(clientSecrets.getCount(), Integer.valueOf(1),
                "The organization client secret view scope should grant listing the client secrets.");
        String latestSecretId = clientSecrets.getList().get(0).getSecretId();

        assertEquals(restClient.getClientSecretCreationStatusCodeOfOrganizationApp(subOrgApplicationId,
                        new ClientSecretCreationRequest().expiresAt(0L), viewToken), HttpStatus.SC_FORBIDDEN,
                "The organization client secret view scope should not grant creating a client secret.");
        assertEquals(restClient.deleteClientSecretOfOrganizationApp(subOrgApplicationId, latestSecretId, viewToken),
                HttpStatus.SC_FORBIDDEN,
                "The organization client secret view scope should not grant deleting a client secret.");
    }

    @Test(groups = "wso2.is", priority = 10, description = "Invoke the client secret endpoints of the " +
            "sub-organization application with a token carrying only the organization client secret create scope.",
            dependsOnMethods = "testOrgClientSecretViewScope")
    public void testOrgClientSecretCreateScope() throws Exception {

        String createToken = getOrganizationSwitchedToken(ORG_CLIENT_SECRET_CREATE_SCOPE);

        ClientSecretResponse clientSecret = restClient.createClientSecretOfOrganizationApp(subOrgApplicationId,
                new ClientSecretCreationRequest().expiresAt(0L), createToken);
        assertNotNull(clientSecret.getSecretId(),
                "The organization client secret create scope should grant creating a client secret.");

        assertEquals(getStatusCodeOfListClientSecretsOfOrganizationApp(subOrgApplicationId, createToken),
                HttpStatus.SC_FORBIDDEN,
                "The organization client secret create scope should not grant listing the client secrets.");
        assertEquals(restClient.deleteClientSecretOfOrganizationApp(subOrgApplicationId, clientSecret.getSecretId(),
                        createToken), HttpStatus.SC_FORBIDDEN,
                "The organization client secret create scope should not grant deleting a client secret.");

        /*
         Regeneration is covered at the root by OAuth2ServiceMultipleClientSecretsTestCase and by
         testRegenerateAtRootPropagatesToSubOrg.
        */
    }

    @Test(groups = "wso2.is", priority = 11, description = "Invoke the client secret endpoints of the " +
            "sub-organization application with a token carrying only the organization client secret delete scope.",
            dependsOnMethods = "testOrgClientSecretCreateScope")
    public void testOrgClientSecretDeleteScope() throws Exception {

        String deleteToken = getOrganizationSwitchedToken(ORG_CLIENT_SECRET_DELETE_SCOPE);

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId,
                orgAdminToken);
        String nonLatestSecretId = getNonLatestSecret(clientSecrets).getSecretId();

        assertEquals(getStatusCodeOfListClientSecretsOfOrganizationApp(subOrgApplicationId, deleteToken),
                HttpStatus.SC_FORBIDDEN,
                "The organization client secret delete scope should not grant listing the client secrets.");
        assertEquals(restClient.getClientSecretCreationStatusCodeOfOrganizationApp(subOrgApplicationId,
                        new ClientSecretCreationRequest().expiresAt(0L), deleteToken), HttpStatus.SC_FORBIDDEN,
                "The organization client secret delete scope should not grant creating a client secret.");
        assertEquals(restClient.deleteClientSecretOfOrganizationApp(subOrgApplicationId, nonLatestSecretId,
                        deleteToken), HttpStatus.SC_NO_CONTENT,
                "The organization client secret delete scope should grant deleting a client secret.");
    }

    @Test(groups = "wso2.is", priority = 12, description = "Authenticate the sub-organization application with an " +
            "expired client secret.", dependsOnMethods = "testOrgClientSecretDeleteScope")
    public void testExpiredClientSecretIsRejectedInSubOrg() throws Exception {

        ClientSecretList clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId,
                orgAdminToken);
        String liveSecretValue = getLatestSecret(clientSecrets).getSecretValue();

        long expiresAt = getCurrentTimeInSeconds() + SHORT_EXPIRY_SECONDS;
        ClientSecretResponse clientSecret = restClient.createClientSecretOfOrganizationApp(subOrgApplicationId,
                new ClientSecretCreationRequest().expiresAt(expiresAt), orgAdminToken);
        subOrgShortLivedSecretId = clientSecret.getSecretId();
        subOrgShortLivedSecretValue = clientSecret.getSecretValue();
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgShortLivedSecretValue),
                HttpStatus.SC_OK, "A client secret should authenticate before its expiry.");

        await().atMost(EXPIRY_WAIT_MILLIS, TimeUnit.MILLISECONDS)
                .pollDelay(SHORT_EXPIRY_SECONDS, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgShortLivedSecretValue)
                        == HttpStatus.SC_UNAUTHORIZED);

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, subOrgShortLivedSecretValue),
                HttpStatus.SC_UNAUTHORIZED, "An expired client secret should not authenticate.");
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(subOrgClientId, liveSecretValue), HttpStatus.SC_OK,
                "A live client secret should keep authenticating after a sibling secret expired.");

        clientSecrets = restClient.getClientSecretsOfOrganizationApp(subOrgApplicationId, orgAdminToken);
        assertEquals(getSecretById(clientSecrets, subOrgShortLivedSecretId).getStatus(),
                ClientSecretResponse.StatusEnum.EXPIRED, "The expired secret is not reported in the EXPIRED status.");
    }

    @Test(groups = "wso2.is", priority = 13, description = "Authenticate in the sub-organization with a client " +
            "secret created at the root organization.",
            dependsOnMethods = "testSharedApplicationAuthenticatesAtSubOrgTokenEndpoint")
    public void testClientSecretCreatedAtRootAuthenticatesInSubOrg() throws Exception {

        ClientSecretResponse clientSecret = restClient.createClientSecret(rootApplicationId,
                new ClientSecretCreationRequest().expiresAt(0L));
        rootCreatedSecretValue = clientSecret.getSecretValue();

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(rootClientId, rootCreatedSecretValue),
                HttpStatus.SC_OK, "A client secret created at the root organization should authenticate at the " +
                        "sub-organization token endpoint.");
    }

    @Test(groups = "wso2.is", priority = 14, description = "Run the confidential client grants of the shared " +
            "application at the sub-organization token endpoint with a non latest client secret.",
            dependsOnMethods = "testClientSecretCreatedAtRootAuthenticatesInSubOrg")
    public void testGrantsOfSharedApplicationAtSubOrgTokenEndpoint() throws Exception {

        String authorizationCode = getAuthorizationCodeFromSubOrgLogin();
        JSONObject codeGrantResponse = getTokenOfAuthorizationCodeGrantAtSubOrg(authorizationCode,
                rootInitialSecretValue);
        assertTrue(codeGrantResponse.has(ACCESS_TOKEN),
                "Access token is not present in the authorization code grant response.");
        assertTrue(codeGrantResponse.has(REFRESH_TOKEN),
                "Refresh token is not present in the authorization code grant response.");
        subOrgRefreshToken = codeGrantResponse.getString(REFRESH_TOKEN);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(REFRESH_TOKEN, subOrgRefreshToken));
        HttpResponse response = sendTokenRequestToSubOrg(parameters, rootClientId, rootInitialSecretValue);
        JSONObject refreshGrantResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(refreshGrantResponse.has(ACCESS_TOKEN),
                "Access token is not present in the refresh token grant response.");

        /* The refresh grant supersedes the tokens of the code grant, hence the latest pair is kept for the
           regeneration assertions. */
        subOrgAccessToken = refreshGrantResponse.getString(ACCESS_TOKEN);
        if (refreshGrantResponse.has(REFRESH_TOKEN)) {
            subOrgRefreshToken = refreshGrantResponse.getString(REFRESH_TOKEN);
        }
    }

    @Test(groups = "wso2.is", priority = 15, description = "Authenticate in the sub-organization with a client " +
            "secret deleted at the root organization.",
            dependsOnMethods = "testGrantsOfSharedApplicationAtSubOrgTokenEndpoint")
    public void testClientSecretDeletedAtRootIsRejectedInSubOrg() throws Exception {

        /*
         The sub-organization resolved and cached the application while this secret was authenticating above, so a
         stale acceptance here means the entry cached under the sub-organization tenant was not evicted by the
         root deletion.
        */
        ClientSecretList clientSecrets = restClient.getClientSecrets(rootApplicationId);
        String nonLatestSecretId = getNonLatestSecret(clientSecrets).getSecretId();
        assertEquals(restClient.deleteClientSecret(rootApplicationId, nonLatestSecretId), HttpStatus.SC_NO_CONTENT,
                "Deleting a non latest client secret at the root organization should succeed.");

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(rootClientId, rootInitialSecretValue),
                HttpStatus.SC_UNAUTHORIZED, "A client secret deleted at the root organization should stop " +
                        "authenticating at the sub-organization token endpoint immediately.");
    }

    @Test(groups = "wso2.is", priority = 16, description = "Regenerate the client secret at the root organization " +
            "and validate the effect at the sub-organization token endpoint.",
            dependsOnMethods = "testClientSecretDeletedAtRootIsRejectedInSubOrg")
    public void testRegenerateAtRootPropagatesToSubOrg() throws Exception {

        assertTrue(isTokenActive(subOrgAccessToken),
                "The sub-organization access token should be active before the regeneration.");

        OpenIDConnectConfiguration oidcConfig = restClient.regenerateClientSecret(rootApplicationId);
        regeneratedSecretValue = oidcConfig.getClientSecret();
        assertTrue(StringUtils.isNotBlank(regeneratedSecretValue),
                "The regenerated client secret is not present in the response.");

        ClientSecretList clientSecrets = restClient.getClientSecrets(rootApplicationId);
        assertEquals(clientSecrets.getCount(), Integer.valueOf(1),
                "The application should have a single client secret after the regeneration.");
        assertTrue(clientSecrets.getList().get(0).getLatest(),
                "The remaining client secret is not marked as the latest secret.");

        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(rootClientId, rootCreatedSecretValue),
                HttpStatus.SC_UNAUTHORIZED, "A client secret replaced by a regeneration should not authenticate " +
                        "at the sub-organization token endpoint.");
        assertEquals(getClientCredentialsTokenStatusCodeAtSubOrg(rootClientId, regeneratedSecretValue),
                HttpStatus.SC_OK, "The regenerated client secret should authenticate at the sub-organization " +
                        "token endpoint.");

        Assert.assertFalse(isTokenActive(subOrgAccessToken),
                "An access token issued in the sub-organization should be revoked by a regeneration at the root.");

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        parameters.add(new BasicNameValuePair(REFRESH_TOKEN, subOrgRefreshToken));
        HttpResponse response = sendTokenRequestToSubOrg(parameters, rootClientId, regeneratedSecretValue);
        JSONObject refreshGrantResponse = getResponseBody(response, HttpStatus.SC_BAD_REQUEST);
        assertEquals(refreshGrantResponse.getString(ERROR), INVALID_GRANT,
                "A refresh token issued in the sub-organization should be revoked by a regeneration at the root.");
    }

    @Test(groups = "wso2.is", priority = 17, description = "Register applications with a client secret expiry " +
            "through the sub-organization dynamic client registration endpoint.",
            dependsOnMethods = "testSharedApplicationAuthenticatesAtSubOrgTokenEndpoint")
    public void testDynamicClientRegistrationInSubOrg() throws Exception {

        long expiresAt = getCurrentTimeInSeconds() + FUTURE_EXPIRY_SECONDS;
        JSONObject registrationResponse = registerDcrApplicationInSubOrg("subOrgDcrAppWithExpiry", expiresAt,
                HttpStatus.SC_CREATED);
        assertEquals(registrationResponse.getLong(CLIENT_SECRET_EXPIRES_AT), expiresAt,
                "The requested client secret expiry is not echoed in the registration response.");
        assertTrue(StringUtils.isNotBlank(registrationResponse.getString(CLIENT_SECRET)),
                "The client secret is not present in the registration response.");

        registrationResponse = registerDcrApplicationInSubOrg("subOrgDcrAppWithoutExpiry", null,
                HttpStatus.SC_CREATED);
        assertEquals(registrationResponse.getLong(CLIENT_SECRET_EXPIRES_AT), 0L,
                "A registration without an expiry should report a non expiring client secret.");

        registrationResponse = registerDcrApplicationInSubOrg("subOrgDcrAppWithPastExpiry",
                getCurrentTimeInSeconds() - FUTURE_EXPIRY_SECONDS, HttpStatus.SC_BAD_REQUEST);
        assertEquals(registrationResponse.getString(ERROR), INVALID_CLIENT_METADATA,
                "A registration with a past client secret expiry should be rejected as invalid client metadata.");
    }

    private void createRootApplication() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel()
                .name(ROOT_APPLICATION_NAME)
                .enhancedOrgAuthenticationEnabled(true)
                .inboundProtocolConfiguration(inboundProtocols)
                .advancedConfigurations(new AdvancedApplicationConfiguration()
                        .skipLoginConsent(true)
                        .skipLogoutConsent(true));

        rootApplicationId = addApplication(application);
        OpenIDConnectConfiguration createdOidcConfig = restClient.getOIDCInboundDetails(rootApplicationId);
        rootClientId = createdOidcConfig.getClientId();
        rootInitialSecretValue = createdOidcConfig.getClientSecret();
        assertNotNull(rootClientId, "Client id of the root application should not be null.");
        assertNotNull(rootInitialSecretValue, "Client secret of the root application should not be null.");
    }

    /**
     * Creates an application inside the sub-organization through the organization perspective API.
     *
     * @throws Exception If an error occurred while creating the application.
     */
    private void createSubOrgApplication() throws Exception {

        ApplicationConfig applicationConfig = new ApplicationConfig.Builder()
                .tokenType(ApplicationConfig.TokenType.OPAQUE)
                .grantTypes(Collections.singletonList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS))
                .claimsList(Collections.emptyList())
                .expiryTime(TOKEN_EXPIRY_SECONDS)
                .skipConsent(true)
                .build();

        ApplicationResponseModel application = addOrganizationApplication(SUB_ORG_APPLICATION_NAME,
                applicationConfig, orgAdminToken, organizationId);
        subOrgApplicationId = application.getId();
        assertNotNull(subOrgApplicationId, "Sub-organization application id should not be null.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfOrganizationApplication(subOrgApplicationId,
                orgAdminToken);
        subOrgClientId = oidcConfig.getClientId();
        subOrgInitialSecretValue = oidcConfig.getClientSecret();
        assertNotNull(subOrgClientId, "Client id of the sub-organization application should not be null.");
        assertNotNull(subOrgInitialSecretValue, "Client secret of the sub-organization application should not be null.");
    }

    private void createClientSecretManagementApplication() throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS,
                OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH));

        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel()
                .name(SECRET_MGT_APPLICATION_NAME)
                .isManagementApp(true)
                .enhancedOrgAuthenticationEnabled(false)
                .inboundProtocolConfiguration(inboundProtocols);

        secretMgtApplicationId = addApplication(application);
        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(secretMgtApplicationId, Arrays.asList(ORG_APPLICATION_MGT_API, ORG_DCR_API));
        }

        OpenIDConnectConfiguration oidcConfigOfMgtApp = restClient.getOIDCInboundDetails(secretMgtApplicationId);
        secretMgtClientId = oidcConfigOfMgtApp.getClientId();
        secretMgtClientSecret = oidcConfigOfMgtApp.getClientSecret();
    }

    private void shareApplicationWithAllChildOrganizations(String applicationId) throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        restClient.shareApplication(applicationId, applicationSharePOSTRequest);
    }

    private String waitForApplicationSharedToSubOrg(String applicationName, String accessToken) {

        AtomicReference<String> sharedAppId = new AtomicReference<>();
        await("shared application '" + applicationName + "' in the sub-organization")
                .atMost(APPLICATION_SHARE_WAIT_MILLIS, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    String appId = restClient.getAppIdUsingAppNameInOrganization(applicationName, accessToken);
                    if (StringUtils.isBlank(appId)) {
                        return false;
                    }
                    sharedAppId.set(appId);
                    return true;
                });
        return sharedAppId.get();
    }

    private void createOrgEndUser() throws Exception {

        UserObject endUser = new UserObject();
        endUser.setUserName(ORG_END_USER_USERNAME);
        endUser.setPassword(ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(ORG_END_USER_EMAIL));
        orgEndUserId = scim2RestClient.createSubOrgUser(endUser, orgMgtRestClient.switchM2MToken(organizationId));
        assertNotNull(orgEndUserId, "Sub-organization user id should not be null.");
    }

    /**
     * Retrieves an organization switched token of the client secret management application for the given scope.
     *
     * @param scope Scope requested with the organization switch grant.
     * @return Organization switched access token.
     * @throws Exception If an error occurred while retrieving the token.
     */
    private String getOrganizationSwitchedToken(String scope) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_ORGANIZATION_SWITCH));
        parameters.add(new BasicNameValuePair(TOKEN, getClientSecretManagementAppToken()));
        parameters.add(new BasicNameValuePair(OAUTH2_SCOPE, scope));
        parameters.add(new BasicNameValuePair(SWITCHING_ORGANIZATION, organizationId));

        HttpResponse response = sendPostRequest(client, getTokenRequestHeaders(secretMgtClientId,
                secretMgtClientSecret), parameters, getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        JSONObject tokenResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(tokenResponse.has(ACCESS_TOKEN), "Access token is not present in the organization switch " +
                "grant response.");
        if (!SYSTEM_SCOPE.equals(scope)) {
            assertTrue(tokenResponse.optString(OAUTH2_SCOPE, StringUtils.EMPTY).contains(scope),
                    "The organization switched token does not carry the requested scope: " + scope);
        }
        return tokenResponse.getString(ACCESS_TOKEN);
    }

    private String getClientSecretManagementAppToken() throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        parameters.add(new BasicNameValuePair(OAUTH2_SCOPE, SYSTEM_SCOPE));

        HttpResponse response = sendPostRequest(client, getTokenRequestHeaders(secretMgtClientId,
                secretMgtClientSecret), parameters, getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        JSONObject tokenResponse = getResponseBody(response, HttpStatus.SC_OK);
        assertTrue(tokenResponse.has(ACCESS_TOKEN),
                "Access token is not present in the client credentials grant response.");
        return tokenResponse.getString(ACCESS_TOKEN);
    }

    private int getClientCredentialsTokenStatusCodeAtSubOrg(String clientId, String clientSecretValue)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        HttpResponse response = sendTokenRequestToSubOrg(parameters, clientId, clientSecretValue);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());
        return statusCode;
    }

    private JSONObject getClientCredentialsTokenAtSubOrg(String clientId, String clientSecretValue) throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));

        return getResponseBody(sendTokenRequestToSubOrg(parameters, clientId, clientSecretValue), HttpStatus.SC_OK);
    }

    private JSONObject getTokenOfAuthorizationCodeGrantAtSubOrg(String authorizationCode, String clientSecretValue)
            throws Exception {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair(GRANT_TYPE_NAME, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        parameters.add(new BasicNameValuePair("code", authorizationCode));
        parameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

        return getResponseBody(sendTokenRequestToSubOrg(parameters, rootClientId, clientSecretValue),
                HttpStatus.SC_OK);
    }

    private HttpResponse sendTokenRequestToSubOrg(List<NameValuePair> parameters, String clientId,
                                                  String clientSecretValue) throws Exception {

        return sendPostRequest(client, getTokenRequestHeaders(clientId, clientSecretValue), parameters,
                subOrgTokenEndpoint);
    }

    /**
     * Performs a login of the sub-organization user on the shared application and returns the authorization code.
     *
     * @return Authorization code issued at the sub-organization.
     * @throws Exception If an error occurred while retrieving the authorization code.
     */
    private String getAuthorizationCodeFromSubOrgLogin() throws Exception {

        try (CloseableHttpClient loginClient = createHttpClient()) {
            List<NameValuePair> authorizeParameters = new ArrayList<>();
            authorizeParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
            authorizeParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, rootClientId));
            authorizeParameters.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));
            authorizeParameters.add(new BasicNameValuePair(OAUTH2_SCOPE, OAUTH2_SCOPE_OPENID));

            HttpResponse response = sendPostRequestWithParameters(loginClient, authorizeParameters,
                    getRootTenantQualifiedOrgURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain(), organizationId));
            Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Location header of the authorize request is not available.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(loginClient, locationHeader.getValue());
            Map<String, Integer> keyPositionMap = new HashMap<>(1);
            keyPositionMap.put("name=\"sessionDataKey\"", 1);
            List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                    keyPositionMap);
            assertTrue(keyValues != null && !keyValues.isEmpty(), "No session data key found on the login page.");
            String sessionDataKey = keyValues.get(0).getValue();
            EntityUtils.consume(response.getEntity());

            List<NameValuePair> loginParameters = new ArrayList<>();
            loginParameters.add(new BasicNameValuePair("username", ORG_END_USER_USERNAME));
            loginParameters.add(new BasicNameValuePair("password", ORG_END_USER_PASSWORD));
            loginParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

            response = sendPostRequestWithParameters(loginClient, loginParameters,
                    getRootTenantQualifiedOrgURL(COMMON_AUTH_URL, tenantInfo.getDomain(), organizationId));
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Location header of the login request is not available.");
            EntityUtils.consume(response.getEntity());

            response = sendGetRequest(loginClient, locationHeader.getValue());
            locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
            assertNotNull(locationHeader, "Redirection to the application with the authorization code is null.");
            EntityUtils.consume(response.getEntity());

            String authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
            assertNotNull(authorizationCode, "Authorization code is null.");
            return authorizationCode;
        }
    }

    /**
     * Checks whether the given access token is active.
     *
     * @param accessToken Access token to introspect.
     * @return True if the token is active.
     * @throws Exception If an error occurred while introspecting the token.
     */
    private boolean isTokenActive(String accessToken) throws Exception {

        org.json.simple.JSONObject introspectionResponse = introspectTokenWithTenant(client, accessToken,
                getTenantQualifiedURL(INTRO_SPEC_ENDPOINT, tenantInfo.getDomain()),
                tenantInfo.getTenantAdmin().getUserName(), tenantInfo.getTenantAdmin().getPassword());
        assertNotNull(introspectionResponse, "Introspection response is null.");
        return (Boolean) introspectionResponse.get(ACTIVE);
    }

    private JSONObject registerDcrApplicationInSubOrg(String clientName, Long clientSecretExpiresAt,
                                                      int expectedStatusCode) throws Exception {

        JSONObject requestBody = new JSONObject();
        requestBody.put(CLIENT_NAME, clientName);
        requestBody.put(GRANT_TYPES, new JSONArray().put(OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        if (clientSecretExpiresAt != null) {
            requestBody.put(EXT_PARAM_CLIENT_SECRET_EXPIRES_AT, clientSecretExpiresAt);
        }

        try (CloseableHttpResponse response = restBaseClient.getResponseOfHttpPost(getSubOrgDcrRegisterEndpoint(),
                requestBody.toString(), getHeadersWithBearerToken(orgAdminToken))) {
            assertEquals(response.getStatusLine().getStatusCode(), expectedStatusCode,
                    "Unexpected status code for the sub-organization dynamic client registration request.");
            JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
            if (responseBody.has(CLIENT_ID)) {
                dcrClientIds.add(responseBody.getString(CLIENT_ID));
            }
            return responseBody;
        }
    }

    private void deleteDcrApplicationInSubOrg(String clientId) throws Exception {

        try (CloseableHttpResponse response = restBaseClient.getResponseOfHttpDelete(getSubOrgDcrRegisterEndpoint() +
                PATH_SEPARATOR + clientId, getHeadersWithBearerToken(orgAdminToken))) {
            EntityUtils.consume(response.getEntity());
        }
    }

    private int getStatusCodeOfListClientSecretsOfOrganizationApp(String appId, String accessToken)
            throws IOException {

        try (CloseableHttpResponse response = restBaseClient.getResponseOfHttpGet(getSubOrgClientSecretsPath(appId),
                getHeadersWithBearerToken(accessToken))) {
            EntityUtils.consume(response.getEntity());
            return response.getStatusLine().getStatusCode();
        }
    }

    private String getSubOrgClientSecretsPath(String appId) {

        return serverURL + ORGANIZATION_PATH + API_SERVER_PATH + APPLICATION_MANAGEMENT_PATH + PATH_SEPARATOR +
                appId + INBOUND_PROTOCOLS_OIDC_PATH + CLIENT_SECRETS_PATH;
    }

    private String getSubOrgDcrRegisterEndpoint() {

        return getRootTenantQualifiedOrgURL(DCR_REGISTER_ENDPOINT, tenantInfo.getDomain(), organizationId);
    }

    private ClientSecretResponse getLatestSecret(ClientSecretList clientSecrets) {

        List<ClientSecretResponse> latestSecrets = clientSecrets.getList().stream()
                .filter(ClientSecretResponse::getLatest)
                .toList();
        assertEquals(latestSecrets.size(), 1, "Exactly one client secret should be marked as the latest secret.");
        return latestSecrets.get(0);
    }

    private ClientSecretResponse getNonLatestSecret(ClientSecretList clientSecrets) {

        return clientSecrets.getList().stream()
                .filter(clientSecret -> !clientSecret.getLatest())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No non latest client secret is present in the list."));
    }

    private ClientSecretResponse getSecretById(ClientSecretList clientSecrets, String secretId) {

        return clientSecrets.getList().stream()
                .filter(clientSecret -> secretId.equals(clientSecret.getSecretId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Client secret is not present in the list: " + secretId));
    }

    private JSONObject getResponseBody(HttpResponse response, int expectedStatusCode) throws IOException,
            JSONException {

        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
        assertEquals(response.getStatusLine().getStatusCode(), expectedStatusCode,
                "Unexpected status code received for the request. Response: " + responseBody);
        return new JSONObject(responseBody);
    }

    private List<Header> getTokenRequestHeaders(String clientId, String clientSecretValue) {

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecretValue)));
        headers.add(new BasicHeader(CONTENT_TYPE_ATTRIBUTE, "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader(USER_AGENT_ATTRIBUTE, USER_AGENT));
        return headers;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE + accessToken);
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }

    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(parameter -> "code".equals(parameter.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    private long getCurrentTimeInSeconds() {

        return System.currentTimeMillis() / 1000;
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
