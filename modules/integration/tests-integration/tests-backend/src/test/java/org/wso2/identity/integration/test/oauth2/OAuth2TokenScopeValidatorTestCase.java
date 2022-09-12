/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
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
import java.io.File;
import java.net.URI;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

public class OAuth2TokenScopeValidatorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String ADD_SCOPE_DEPLOYMENT_CONFIG = "add_scope_deployment.toml";
    private static final String TENANT_DOMAIN = "wso2.com";
    private final String tokenType = "Default";
    private AccessToken privilegedAccessToken;
    private ClientID consumerKey;
    private Secret consumerSecret;
    private String activeTenant;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File emailLoginConfigFile = new File(
                getISResourceLocation() + File.separator + "oauth" + File.separator + ADD_SCOPE_DEPLOYMENT_CONFIG);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(emailLoginConfigFile, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();

        super.init();
        AutomationContext context = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        this.activeTenant = context.getContextTenant().getDomain();

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType(tokenType);
        appDTO.setGrantTypes("client_credentials");

        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication(appDTO, SERVICE_PROVIDER_NAME);

        consumerKey = new ClientID(oAuthConsumerAppDTO.getOauthConsumerKey());
        consumerSecret = new Secret(oAuthConsumerAppDTO.getOauthConsumerSecret());

        privilegedAccessToken = requestAccessToken("internal_application_mgt_view");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Generate access token and check if the added scope is returned in introspect response " +
                    "when single internal scope is added"
    )
    public void testScopeReturnedWithSingleInternalScopeAdded() throws Exception {

        String scope = "internal_test";
        AccessToken accessToken = requestAccessToken(scope);

        TokenIntrospectionResponse activeTokenIntrospectionResponse =
                introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertEquals(String.valueOf(activeTokenIntrospectionResponse.toSuccessResponse().getScope()), scope);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Generate access token and check if the added scope is returned in introspect response " +
                    "when single scope is added"
    )
    public void testScopeReturnedWithSingleNonInternalScopeAdded() throws Exception {

        String scope = "test";
        AccessToken accessToken = requestAccessToken(scope);

        TokenIntrospectionResponse activeTokenIntrospectionResponse =
                introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertEquals(String.valueOf(activeTokenIntrospectionResponse.toSuccessResponse().getScope()), scope);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Generate access token and check if the added scope is returned in introspect response " +
                    "when single random internal scope is added"
    )
    public void testScopeReturnedWithRandomScopeAdded() throws Exception {

        String scope = "random";
        AccessToken accessToken = requestAccessToken(scope);

        TokenIntrospectionResponse activeTokenIntrospectionResponse =
                introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertEquals(String.valueOf(activeTokenIntrospectionResponse.toSuccessResponse().getScope()), scope);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Generate access token and check if the added scope is returned in introspect response " +
                    "when multiple scopes are added"
    )
    public void testScopeReturnedWithMoreThanOneScopeAdded() throws Exception {

        String scope = "internal_test test";
        AccessToken accessToken = requestAccessToken(scope);

        TokenIntrospectionResponse activeTokenIntrospectionResponse =
                introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertEquals(String.valueOf(activeTokenIntrospectionResponse.toSuccessResponse().getScope()), scope);
    }

    @Test(
            alwaysRun = true,
            groups = "wso2.is",
            description = "Generate access token and check if the added scope is returned in introspect response " +
                    "when with invalid internal scope"
    )
    public void testScopeReturnedWithInvalidInternalScopeAdded() throws Exception {

        String scope = "internal_claim_meta_create";
        AccessToken accessToken = requestAccessToken(scope);

        TokenIntrospectionResponse activeTokenIntrospectionResponse =
                introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertEquals(String.valueOf(activeTokenIntrospectionResponse.toSuccessResponse().getScope()), scope);
    }

    private AccessToken requestAccessToken(String requestedScope) throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ClientCredentialsGrant();

        Scope scope = new Scope(requestedScope);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private TokenIntrospectionResponse introspectAccessToken(AccessToken accessToken, AccessToken privilegedAccessToken)
            throws Exception {

        URI introSpecEndpoint;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        }
        BearerAccessToken bearerAccessToken = new BearerAccessToken(privilegedAccessToken.getValue());
        TokenIntrospectionRequest TokenIntroRequest =
                new TokenIntrospectionRequest(introSpecEndpoint, bearerAccessToken, accessToken);
        HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();

        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }
}
