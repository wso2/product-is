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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests the fine grained scope enforcement of the OAuth2/OIDC client secret endpoints of the Application Management
 * REST API. Every scenario authorizes a dedicated machine to machine application to a single scope combination and
 * invokes the client secret endpoints of a separate application with the resulting access token.
 */
public class ApplicationClientSecretAuthorizationTestCase extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRETS_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH + "/secrets";
    private static final String REGENERATE_SECRET_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH +
            "/regenerate-secret";

    private static final String APPLICATION_MANAGEMENT_API_IDENTIFIER = "/api/server/v1/applications";
    private static final String RBAC_POLICY_IDENTIFIER = "RBAC";
    private static final String SYSTEM_API_SCOPE = "SYSTEM";

    private static final String APPLICATION_MGT_VIEW_SCOPE = "internal_application_mgt_view";
    private static final String APPLICATION_MGT_UPDATE_SCOPE = "internal_application_mgt_update";
    private static final String APPLICATION_MGT_CREATE_SCOPE = "internal_application_mgt_create";
    private static final String CLIENT_SECRET_VIEW_SCOPE = "internal_application_mgt_client_secret_view";
    private static final String CLIENT_SECRET_CREATE_SCOPE = "internal_application_mgt_client_secret_create";
    private static final String CLIENT_SECRET_DELETE_SCOPE = "internal_application_mgt_client_secret_delete";
    private static final List<String> CLIENT_SECRET_SCOPES = Arrays.asList(CLIENT_SECRET_VIEW_SCOPE,
            CLIENT_SECRET_CREATE_SCOPE, CLIENT_SECRET_DELETE_SCOPE);

    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    private static final String SECRET_HOLDER_APPLICATION_NAME = "Client Secret Authorization Application";
    private static final String APPLICATION_SCOPES_APPLICATION_NAME = "Client Secret Authorization App Mgt Scopes App";
    private static final String CLIENT_SECRET_VIEW_APPLICATION_NAME = "Client Secret Authorization View Scope App";
    private static final String CLIENT_SECRET_CREATE_APPLICATION_NAME = "Client Secret Authorization Create Scope App";
    private static final String CLIENT_SECRET_DELETE_APPLICATION_NAME = "Client Secret Authorization Delete Scope App";

    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_SECRET_EXPIRES_AT = "clientSecretExpiresAt";
    private static final String MULTIPLE_CLIENT_SECRETS_CONFIGURED = "multipleClientSecretsConfigured";
    private static final String GRANT_TYPES = "grantTypes";
    private static final String STATE = "state";
    private static final String COUNT = "count";
    private static final String LIST = "list";
    private static final String SECRET_ID = "secretId";
    private static final String LATEST = "latest";
    private static final String EMPTY_JSON_BODY = "{}";

    private OAuth2RestClient oAuth2RestClient;
    private final List<String> createdApplicationIds = new ArrayList<>();
    private String oidcInboundPath;
    private String clientSecretsPath;
    private String regenerateSecretPath;
    private String applicationScopesToken;
    private String clientSecretViewToken;
    private String clientSecretCreateToken;
    private String clientSecretDeleteToken;

    public ApplicationClientSecretAuthorizationTestCase() throws Exception {

        super(TestUserMode.SUPER_TENANT_ADMIN);
    }

    @BeforeClass(alwaysRun = true)
    public void initClientSecretAuthorization() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);

        String secretHolderApplicationId = createApplication(SECRET_HOLDER_APPLICATION_NAME, false,
                AUTHORIZATION_CODE_GRANT_TYPE);
        String secretHolderApplicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR +
                secretHolderApplicationId;
        oidcInboundPath = secretHolderApplicationPath + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
        clientSecretsPath = secretHolderApplicationPath + CLIENT_SECRETS_CONTEXT_PATH;
        regenerateSecretPath = secretHolderApplicationPath + REGENERATE_SECRET_CONTEXT_PATH;

        applicationScopesToken = getAccessTokenAuthorizedTo(APPLICATION_SCOPES_APPLICATION_NAME,
                Arrays.asList(APPLICATION_MGT_VIEW_SCOPE, APPLICATION_MGT_UPDATE_SCOPE, APPLICATION_MGT_CREATE_SCOPE));
        clientSecretViewToken = getAccessTokenAuthorizedTo(CLIENT_SECRET_VIEW_APPLICATION_NAME,
                Collections.singletonList(CLIENT_SECRET_VIEW_SCOPE));
        clientSecretCreateToken = getAccessTokenAuthorizedTo(CLIENT_SECRET_CREATE_APPLICATION_NAME,
                Collections.singletonList(CLIENT_SECRET_CREATE_SCOPE));
        clientSecretDeleteToken = getAccessTokenAuthorizedTo(CLIENT_SECRET_DELETE_APPLICATION_NAME,
                Collections.singletonList(CLIENT_SECRET_DELETE_SCOPE));
    }

    @AfterClass(alwaysRun = true)
    public void concludeClientSecretAuthorization() throws Exception {

        for (String applicationId : createdApplicationIds) {
            oAuth2RestClient.deleteApplication(applicationId);
        }
        createdApplicationIds.clear();
        oAuth2RestClient.closeHttpClient();
    }

    @Test(description = "Verifies that the generic application management scopes do not grant access to any of the " +
            "client secret endpoints.")
    public void testApplicationManagementScopesDoNotGrantClientSecretEndpoints() throws JSONException {

        String secretId = getClientSecretId(true);

        assertForbidden(getResponseOfPostWithOAuth2(clientSecretsPath, EMPTY_JSON_BODY, applicationScopesToken),
                "Client secret creation");
        assertForbidden(getResponseOfGetWithOAuth2(clientSecretsPath, applicationScopesToken),
                "Client secret listing");
        assertForbidden(getResponseOfGetWithOAuth2(getClientSecretPath(secretId), applicationScopesToken),
                "Client secret retrieval");
        assertForbidden(getResponseOfDeleteWithOAuth2(getClientSecretPath(secretId), applicationScopesToken),
                "Client secret deletion");
        assertForbidden(getResponseOfPostWithOAuth2(regenerateSecretPath, StringUtils.EMPTY, applicationScopesToken),
                "Client secret regeneration");
    }

    @Test(description = "Verifies that the client secret view scope grants the read operations on client secrets " +
            "and nothing else.",
            dependsOnMethods = "testApplicationManagementScopesDoNotGrantClientSecretEndpoints")
    public void testClientSecretViewScopeGrantsOnlyReadOperations() throws JSONException {

        String secretId = getClientSecretId(true);

        getResponseOfGetWithOAuth2(clientSecretsPath, clientSecretViewToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(COUNT, greaterThan(0))
                .body(LIST + "." + SECRET_ID, hasItem(secretId));

        getResponseOfGetWithOAuth2(getClientSecretPath(secretId), clientSecretViewToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(SECRET_ID, equalTo(secretId))
                .body(LATEST, equalTo(true));

        assertForbidden(getResponseOfPostWithOAuth2(clientSecretsPath, EMPTY_JSON_BODY, clientSecretViewToken),
                "Client secret creation");
        assertForbidden(getResponseOfDeleteWithOAuth2(getClientSecretPath(secretId), clientSecretViewToken),
                "Client secret deletion");
        assertForbidden(getResponseOfPostWithOAuth2(regenerateSecretPath, StringUtils.EMPTY, clientSecretViewToken),
                "Client secret regeneration");
    }

    @Test(description = "Verifies that the client secret create scope grants both the creation and the regeneration " +
            "of client secrets and nothing else.",
            dependsOnMethods = "testClientSecretViewScopeGrantsOnlyReadOperations")
    public void testClientSecretCreateScopeGrantsCreationAndRegeneration() throws JSONException {

        String secretId = getClientSecretId(true);

        assertForbidden(getResponseOfGetWithOAuth2(clientSecretsPath, clientSecretCreateToken),
                "Client secret listing");
        assertForbidden(getResponseOfGetWithOAuth2(getClientSecretPath(secretId), clientSecretCreateToken),
                "Client secret retrieval");
        assertForbidden(getResponseOfDeleteWithOAuth2(getClientSecretPath(secretId), clientSecretCreateToken),
                "Client secret deletion");

        /* The application holds a single secret at this point, hence the creation is expected to succeed. A conflict
           is tolerated so that the assertion survives a packaged max secret count of one. */
        int creationStatusCode = getResponseOfPostWithOAuth2(clientSecretsPath, EMPTY_JSON_BODY,
                clientSecretCreateToken).getStatusCode();
        Assert.assertTrue(creationStatusCode == HttpStatus.SC_CREATED || creationStatusCode == HttpStatus.SC_CONFLICT,
                "Client secret creation with the client secret create scope responded with: " + creationStatusCode);

        /* Regeneration is authorized by the client secret create scope. There is no dedicated regeneration scope. */
        getResponseOfPostWithOAuth2(regenerateSecretPath, StringUtils.EMPTY, clientSecretCreateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(CLIENT_SECRET, notNullValue());
    }

    @Test(description = "Verifies that the client secret delete scope grants the deletion of a client secret and " +
            "nothing else.",
            dependsOnMethods = "testClientSecretCreateScopeGrantsCreationAndRegeneration")
    public void testClientSecretDeleteScopeGrantsOnlyDeletion() throws JSONException {

        /* Regeneration left the application with a single secret. A second one is added as the tenant admin so that
           a deletable non latest secret exists, keeping the application within the default max secret count of two. */
        getResponseOfPost(clientSecretsPath, EMPTY_JSON_BODY)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String nonLatestSecretId = getClientSecretId(false);

        getResponseOfDeleteWithOAuth2(getClientSecretPath(nonLatestSecretId), clientSecretDeleteToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        assertForbidden(getResponseOfPostWithOAuth2(clientSecretsPath, EMPTY_JSON_BODY, clientSecretDeleteToken),
                "Client secret creation");
        assertForbidden(getResponseOfGetWithOAuth2(clientSecretsPath, clientSecretDeleteToken),
                "Client secret listing");
        assertForbidden(getResponseOfGetWithOAuth2(getClientSecretPath(getClientSecretId(true)),
                clientSecretDeleteToken), "Client secret retrieval");
        assertForbidden(getResponseOfPostWithOAuth2(regenerateSecretPath, StringUtils.EMPTY, clientSecretDeleteToken),
                "Client secret regeneration");
        Assert.assertEquals(getClientSecretList().getInt(COUNT), 1, "The deleted client secret is still listed.");
    }

    @Test(description = "Verifies that the OIDC inbound configuration omits the client secret properties for a " +
            "caller without the client secret view scope.",
            dependsOnMethods = "testClientSecretDeleteScopeGrantsOnlyDeletion")
    public void testClientSecretPropertiesStrippedWithoutClientSecretViewScope() throws JSONException {

        Response scopedResponse = getResponseOfGetWithOAuth2(oidcInboundPath, applicationScopesToken);
        scopedResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        JSONObject scopedConfiguration = new JSONObject(scopedResponse.asString());
        assertPropertyAbsent(scopedConfiguration, CLIENT_SECRET);
        assertPropertyAbsent(scopedConfiguration, CLIENT_SECRET_EXPIRES_AT);
        assertPropertyAbsent(scopedConfiguration, MULTIPLE_CLIENT_SECRETS_CONFIGURED);
        assertPropertyPresent(scopedConfiguration, CLIENT_ID);
        assertPropertyPresent(scopedConfiguration, GRANT_TYPES);
        assertPropertyPresent(scopedConfiguration, STATE);

        Response adminResponse = getResponseOfGet(oidcInboundPath);
        adminResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        JSONObject adminConfiguration = new JSONObject(adminResponse.asString());
        assertPropertyPresent(adminConfiguration, CLIENT_SECRET);
        assertPropertyPresent(adminConfiguration, CLIENT_SECRET_EXPIRES_AT);
        assertPropertyPresent(adminConfiguration, MULTIPLE_CLIENT_SECRETS_CONFIGURED);
    }

    /**
     * Create an OAuth2/OIDC application with the given name and grant types.
     *
     * @param applicationName Name of the application.
     * @param isManagementApp Whether the application is a management application.
     * @param grantTypes      Grant types to be enabled on the application.
     * @return Id of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    private String createApplication(String applicationName, boolean isManagementApp, String... grantTypes)
            throws Exception {

        OpenIDConnectConfiguration oidcConfiguration = new OpenIDConnectConfiguration();
        oidcConfiguration.setGrantTypes(Arrays.asList(grantTypes));
        oidcConfiguration.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));

        ApplicationModel application = new ApplicationModel();
        application.setName(applicationName);
        application.setIsManagementApp(isManagementApp);
        application.setInboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfiguration));

        String applicationId = oAuth2RestClient.createApplication(application);
        createdApplicationIds.add(applicationId);
        return applicationId;
    }

    /**
     * Create a machine to machine application authorized to the given Application Management API scopes and obtain a
     * client credentials access token for it.
     *
     * @param applicationName Name of the machine to machine application.
     * @param scopes          Application Management API scopes to authorize.
     * @return Access token carrying exactly the given scopes.
     * @throws Exception If an error occurred while creating the application or obtaining the token.
     */
    private String getAccessTokenAuthorizedTo(String applicationName, List<String> scopes) throws Exception {

        String applicationId = createApplication(applicationName, true,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS);
        authorizeApplicationManagementScopes(applicationId, scopes);

        OpenIDConnectConfiguration oidcConfiguration = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        ClientAuthentication clientAuthentication = new ClientSecretBasic(
                new ClientID(oidcConfiguration.getClientId()), new Secret(oidcConfiguration.getClientSecret()));
        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                tenantInfo.getDomain()));
        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuthentication,
                new ClientCredentialsGrant(), new Scope(SYSTEM_API_SCOPE));

        HTTPResponse tokenHttpResponse = tokenRequest.toHTTPRequest().send();
        Assert.assertNotNull(tokenHttpResponse, "Access token http response is null.");
        AccessTokenResponse accessTokenResponse = TokenResponse.parse(tokenHttpResponse).toSuccessResponse();
        AccessToken accessToken = accessTokenResponse.getTokens().getAccessToken();
        Assert.assertNotNull(accessToken, "The retrieved access token is null in the token response.");

        assertGrantedScopes(accessToken.getScope(), scopes);
        return accessToken.getValue();
    }

    /**
     * Authorize the given Application Management API scopes to an application.
     *
     * @param applicationId Id of the application.
     * @param scopes        Scopes to be authorized.
     * @throws Exception If an error occurred while authorizing the scopes.
     */
    private void authorizeApplicationManagementScopes(String applicationId, List<String> scopes) throws Exception {

        List<APIResourceListItem> apiResources = oAuth2RestClient.getAPIResourcesWithFiltering(
                "identifier+eq+" + APPLICATION_MANAGEMENT_API_IDENTIFIER);
        Assert.assertFalse(apiResources == null || apiResources.isEmpty(),
                "The Application Management API resource is not available in the tenant.");

        AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(apiResources.get(0).getId());
        authorizedAPICreationModel.setPolicyIdentifier(RBAC_POLICY_IDENTIFIER);
        scopes.forEach(authorizedAPICreationModel::addScopesItem);
        oAuth2RestClient.addAPIAuthorizationToApplication(applicationId, authorizedAPICreationModel);
    }

    /**
     * Assert that the token carries every authorized scope and none of the client secret scopes it was not
     * authorized to.
     *
     * @param grantedScopes    Scopes present in the token response.
     * @param authorizedScopes Scopes authorized to the application.
     */
    private void assertGrantedScopes(Scope grantedScopes, List<String> authorizedScopes) {

        Assert.assertNotNull(grantedScopes, "No scopes are present in the token response.");
        for (String authorizedScope : authorizedScopes) {
            Assert.assertTrue(grantedScopes.contains(authorizedScope),
                    "Authorized scope " + authorizedScope + " is missing in the token response.");
        }
        for (String clientSecretScope : CLIENT_SECRET_SCOPES) {
            if (!authorizedScopes.contains(clientSecretScope)) {
                Assert.assertFalse(grantedScopes.contains(clientSecretScope),
                        "Unauthorized scope " + clientSecretScope + " is granted in the token response.");
            }
        }
    }

    /**
     * Retrieve the id of the latest or of a non latest client secret of the application as the tenant admin.
     *
     * @param latest Whether the id of the latest client secret is required.
     * @return Id of a matching client secret.
     * @throws JSONException If the client secret list could not be parsed.
     */
    private String getClientSecretId(boolean latest) throws JSONException {

        JSONArray clientSecrets = getClientSecretList().getJSONArray(LIST);
        for (int i = 0; i < clientSecrets.length(); i++) {
            JSONObject clientSecret = clientSecrets.getJSONObject(i);
            if (clientSecret.getBoolean(LATEST) == latest) {
                return clientSecret.getString(SECRET_ID);
            }
        }
        Assert.fail("The application has no " + (latest ? "latest" : "non latest") + " client secret.");
        return null;
    }

    private JSONObject getClientSecretList() throws JSONException {

        Response response = getResponseOfGet(clientSecretsPath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        return new JSONObject(response.asString());
    }

    private String getClientSecretPath(String secretId) {

        return clientSecretsPath + PATH_SEPARATOR + secretId;
    }

    private void assertForbidden(Response response, String operation) {

        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_FORBIDDEN,
                operation + " must be rejected with a forbidden response when the required scope is missing. " +
                        "Response: " + response.asString());
    }

    private void assertPropertyAbsent(JSONObject configuration, String property) {

        Assert.assertFalse(configuration.has(property),
                "Property " + property + " must be absent from the OIDC inbound configuration of a caller without " +
                        "the client secret view scope.");
    }

    private void assertPropertyPresent(JSONObject configuration, String property) {

        Assert.assertTrue(configuration.has(property),
                "Property " + property + " is missing from the OIDC inbound configuration.");
    }
}
