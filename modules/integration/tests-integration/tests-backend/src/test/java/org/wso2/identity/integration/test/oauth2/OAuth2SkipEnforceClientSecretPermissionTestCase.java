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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Integration test class to verify the OIDC inbound client secret property visibility when the
 * {@code skip_enforce_client_secret_permission} configuration is enabled.
 */
public class OAuth2SkipEnforceClientSecretPermissionTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APPLICATION_NAME = "SkipEnforceClientSecretPermissionApp";
    private static final String API_CALLER_APPLICATION_NAME = "SkipEnforceClientSecretPermissionApiCallerApp";
    private static final String APPLICATION_MANAGEMENT_API_IDENTIFIER = "/api/server/v1/applications";
    private static final String APPLICATION_MANAGEMENT_VIEW_SCOPE = "internal_application_mgt_view";
    private static final String RBAC_POLICY = "RBAC";
    private static final String BEARER = "Bearer ";
    private static final String APPLICATIONS_API_BASE_PATH = "api/server/v1/applications/";
    private static final String OIDC_INBOUND_PROTOCOL_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_SECRET_EXPIRES_AT = "clientSecretExpiresAt";
    private static final String MULTIPLE_CLIENT_SECRETS_CONFIGURED = "multipleClientSecretsConfigured";

    private ServerConfigurationManager serverConfigurationManager;
    private CloseableHttpClient client;
    private String applicationId;
    private String apiCallerApplicationId;
    private String applicationMgtViewScopedToken;
    private String oidcInboundEndpoint;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        client = HttpClientBuilder.create().build();

        applicationId = addOIDCApplication(APPLICATION_NAME);
        oidcInboundEndpoint = serverURL + APPLICATIONS_API_BASE_PATH + applicationId + OIDC_INBOUND_PROTOCOL_PATH;

        apiCallerApplicationId = addOIDCApplication(API_CALLER_APPLICATION_NAME);
        authorizeApplicationManagementViewScope(apiCallerApplicationId);
        applicationMgtViewScopedToken = getApplicationMgtViewScopedToken(apiCallerApplicationId);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        deleteApp(apiCallerApplicationId);
        restClient.closeHttpClient();
        client.close();

        log.info("Replacing deployment.toml with default configurations.");
        serverConfigurationManager.restoreToLastConfiguration(true);
    }

    @Test(groups = "wso2.is", description = "Test the OIDC inbound retrieval with a token carrying only the "
            + "application view scope while skip enforce client secret permission is enabled.")
    public void testOIDCInboundRetrievalWithApplicationMgtViewScope() throws Exception {

        JSONObject oidcInboundConfig = getOIDCInboundDetailsWithToken(applicationMgtViewScopedToken);

        Assert.assertTrue(oidcInboundConfig.has(CLIENT_SECRET), "Client secret is not returned to a caller without "
                + "the client secret view scope while skip enforce client secret permission is enabled.");
        Assert.assertTrue(StringUtils.isNotBlank(oidcInboundConfig.getString(CLIENT_SECRET)),
                "Returned client secret is blank.");
        Assert.assertTrue(oidcInboundConfig.has(CLIENT_SECRET_EXPIRES_AT), "Client secret expiry is not returned to "
                + "a caller without the client secret view scope while skip enforce client secret permission is "
                + "enabled.");
        Assert.assertFalse(oidcInboundConfig.isNull(CLIENT_SECRET_EXPIRES_AT),
                "Returned client secret expiry is null.");
        Assert.assertTrue(oidcInboundConfig.has(MULTIPLE_CLIENT_SECRETS_CONFIGURED), "Multiple client secrets "
                + "configured flag is not returned to a caller without the client secret view scope while skip "
                + "enforce client secret permission is enabled.");
        Assert.assertFalse(oidcInboundConfig.isNull(MULTIPLE_CLIENT_SECRETS_CONFIGURED),
                "Returned multiple client secrets configured flag is null.");
    }

    @Test(groups = "wso2.is", description = "Test the OIDC inbound retrieval as the tenant admin while skip enforce "
            + "client secret permission is enabled.",
            dependsOnMethods = {"testOIDCInboundRetrievalWithApplicationMgtViewScope"})
    public void testOIDCInboundRetrievalAsAdmin() throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        Assert.assertTrue(StringUtils.isNotBlank(oidcConfig.getClientSecret()),
                "Client secret is not returned to the tenant admin.");
        Assert.assertNotNull(oidcConfig.getClientSecretExpiresAt(),
                "Client secret expiry is not returned to the tenant admin.");
        Assert.assertNotNull(oidcConfig.getMultipleClientSecretsConfigured(),
                "Multiple client secrets configured flag is not returned to the tenant admin.");
    }

    /**
     * Create a management application with an OIDC inbound configured for the client credentials grant.
     *
     * @param applicationName Name of the application.
     * @return Id of the created application.
     * @throws Exception If an error occurred while creating the application.
     */
    private String addOIDCApplication(String applicationName) throws Exception {

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Collections.singletonList(OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel();
        application.setName(applicationName);
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setIsManagementApp(true);

        return addApplication(application);
    }

    /**
     * Authorize the application management API to the given application with only the application view scope.
     *
     * @param appId Application id of the API caller application.
     * @throws Exception If an error occurred while authorizing the application management API.
     */
    private void authorizeApplicationManagementViewScope(String appId) throws Exception {

        List<APIResourceListItem> apiResources = restClient.getAPIResourcesWithFiltering(
                "identifier+eq+" + APPLICATION_MANAGEMENT_API_IDENTIFIER);
        Assert.assertFalse(apiResources == null || apiResources.isEmpty(),
                "Application management API resource is not available.");

        AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(apiResources.get(0).getId());
        authorizedAPICreationModel.setPolicyIdentifier(RBAC_POLICY);
        authorizedAPICreationModel.addScopesItem(APPLICATION_MANAGEMENT_VIEW_SCOPE);
        restClient.addAPIAuthorizationToApplication(appId, authorizedAPICreationModel);
    }

    /**
     * Get a client credentials token carrying only the application view scope.
     *
     * @param appId Application id of the API caller application.
     * @return Access token value.
     * @throws Exception If an error occurred while requesting the access token.
     */
    private String getApplicationMgtViewScopedToken(String appId) throws Exception {

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        ClientAuthentication clientAuth = new ClientSecretBasic(new ClientID(oidcConfig.getClientId()),
                new Secret(oidcConfig.getClientSecret()));
        TokenRequest tokenRequest = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientAuth,
                new ClientCredentialsGrant(), new Scope(APPLICATION_MANAGEMENT_VIEW_SCOPE));

        HTTPResponse tokenHTTPResponse = tokenRequest.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResponse, "Access token http response is null.");

        AccessTokenResponse accessTokenResponse = TokenResponse.parse(tokenHTTPResponse).toSuccessResponse();
        Scope scopesInResponse = accessTokenResponse.getTokens().getAccessToken().getScope();
        Assert.assertTrue(scopesInResponse != null && scopesInResponse.contains(APPLICATION_MANAGEMENT_VIEW_SCOPE),
                "Requested scope is missing in the token response.");

        return accessTokenResponse.getTokens().getAccessToken().getValue();
    }

    /**
     * Get the OIDC inbound configuration of the test application with a bearer token.
     *
     * @param accessToken Access token of the API caller application.
     * @return OIDC inbound configuration as a json object.
     * @throws Exception If an error occurred while retrieving the OIDC inbound configuration.
     */
    private JSONObject getOIDCInboundDetailsWithToken(String accessToken) throws Exception {

        HttpGet request = new HttpGet(oidcInboundEndpoint);
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER + accessToken);

        try (CloseableHttpResponse response = client.execute(request)) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "OIDC inbound configuration retrieval failed.");
            return new JSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing deployment.toml to enable skip enforce client secret permission.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oauth" +
                File.separator + "skip_enforce_client_secret_permission_enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }
}
