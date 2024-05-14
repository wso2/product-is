/*
 * Copyright (c) 2019, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AssociatedRolesConfig;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test cases to check the functionality of the Permission based scope validator.
 */
public class PermissionBasedScopeValidatorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String INTROSPECT_SCOPE = "internal_application_mgt_view";
    private static final String INTROSPECT_SCOPE_IN_NEW_AUTHZ_RUNTIME = "internal_oauth2_introspect";
    private static final String SYSTEM_SCOPE = "SYSTEM";
    private static final String CALLBACK_URL = "https://localhost/callback";
    private CloseableHttpClient client;
    private String applicationId;
    private static boolean isLegacyRuntimeEnabled;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        isLegacyRuntimeEnabled = CarbonUtils.isLegacyAuthzRuntimeEnabled();
        createOauthApplication();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        consumerKey = null;
        consumerSecret = null;
        applicationId = null;
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Testing secured API without authentication.")
    public void testIntrospectionWithoutAuthentication() throws Exception {

        AccessToken accessToken = new BearerAccessToken("243242323123213");
        TokenIntrospectionResponse tokenIntrospectionResponse = invokeIntrospectionService(accessToken, null);
        Assert.assertFalse(tokenIntrospectionResponse.indicatesSuccess(), "Introspection endpoint called without authentication");
    }

    @Test(groups = "wso2.is", description = "Request access token without scopes and validate.",
            dependsOnMethods = "testIntrospectionWithoutAuthentication")
    public void testValidateTokenWithoutScope() throws Exception {

        Assert.assertFalse(getTokenAndValidate(null), "Introspection endpoint called without authentication valid scope");
    }

    @Test(groups = "wso2.is", description = "Request access token with valid scope and validate it.",
            dependsOnMethods = "testValidateTokenWithoutScope")
    public void testValidateTokenWithValidScope() throws Exception {

        if (isLegacyRuntimeEnabled) {
            Assert.assertTrue(getTokenAndValidate(new Scope(INTROSPECT_SCOPE)),
                    "Introspection endpoint cannot call with the valid scope");
        } else {
            Assert.assertTrue(getTokenAndValidate(new Scope(INTROSPECT_SCOPE_IN_NEW_AUTHZ_RUNTIME)),
                    "Introspection endpoint cannot call with the valid scope");
        }
    }

    @Test(groups = "wso2.is", description = "Request access token with valid system scope and validate it.",
            dependsOnMethods = "testValidateTokenWithoutScope")
    public void testValidateTokenWithSystemScope() throws Exception {

        Assert.assertTrue(getTokenAndValidate(new Scope(SYSTEM_SCOPE)), "Introspection endpoint cannot call with the SYSTEM scope");
    }

    /**
     * Request access token with the scope and validate the token.
     *
     * @param scope scope
     * @return whether validation success or not
     * @throws Exception exception
     */
    private boolean getTokenAndValidate(Scope scope) throws Exception {

        client = HttpClientBuilder.create().disableRedirectHandling().build();

        try {
            Secret password = new Secret(userInfo.getPassword());
            AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(
                    userInfo.getUserNameWithoutDomain(), password);
            ClientID clientID = new ClientID(consumerKey);
            Secret clientSecret = new Secret(consumerSecret);
            ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
            URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, scope);

            HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
            Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");
            AccessTokenResponse tokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
            Assert.assertNotNull(tokenResponse, "Access token response is null.");
            AccessToken accessToken = tokenResponse.getTokens().getAccessToken();
            BearerAccessToken bearerAccessToken = new BearerAccessToken(accessToken.getValue());
            TokenIntrospectionResponse introspectionResponse =  invokeIntrospectionService(accessToken, bearerAccessToken);
            Assert.assertNotNull(introspectionResponse, "Introspection response is null.");
            return introspectionResponse.indicatesSuccess();
        } finally {
            client.close();
        }
    }

    private TokenIntrospectionResponse invokeIntrospectionService(AccessToken accessToken,
                                                                  BearerAccessToken bearerAccessToken)
            throws URISyntaxException, IOException, ParseException {

        URI introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        TokenIntrospectionRequest TokenIntroRequest = new TokenIntrospectionRequest(introSpecEndpoint,
                bearerAccessToken, accessToken);
        HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection http response is null.");

        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }

    private void createOauthApplication() throws Exception {

        ApplicationResponseModel application = getBasicOAuthApplication(CALLBACK_URL);
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration  oidcInboundConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcInboundConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        consumerSecret = oidcInboundConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");

        applicationId = application.getId();
        if (!isLegacyRuntimeEnabled) {
            // Authorize few system APIs.
            authorizeSystemAPIs(applicationId,
                    new ArrayList<>(Arrays.asList("/api/server/v1/tenants", "/scim2/Users", "/oauth2/introspect")));
            // Associate roles.
            ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
            AssociatedRolesConfig associatedRolesConfig =
                    new AssociatedRolesConfig().allowedAudience(AssociatedRolesConfig.AllowedAudienceEnum.ORGANIZATION);
            applicationPatch = applicationPatch.associatedRoles(associatedRolesConfig);
            updateApplication(applicationId, applicationPatch);
        }
    }
}
