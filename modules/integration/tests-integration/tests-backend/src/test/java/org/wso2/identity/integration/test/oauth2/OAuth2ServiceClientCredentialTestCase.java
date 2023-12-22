/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com).
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
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID;

/**
 * Tests for OAuth2 client credentials grant type.
 */
public class OAuth2ServiceClientCredentialTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private String accessToken;
    private String consumerKey;
    private String consumerSecret;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;
    private Tenant tenantInfo;
    private String applicationId;

    private CloseableHttpClient client;

    private static final String VALID_RANDOM_SCOPE = "device_01";

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public OAuth2ServiceClientCredentialTestCase(TestUserMode userMode) throws Exception {
        super.init(userMode);
        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        tenantInfo = context.getContextTenant();
        userInfo = tenantInfo.getContextUser();
        restClient = new OAuth2RestClient(serverURL, tenantInfo);

        setSystemproperties();
        client = HttpClientBuilder.create().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApp(applicationId);
        client.close();
        restClient.closeHttpClient();

        consumerKey = null;
        accessToken = null;
        applicationId = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");

        applicationId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Send client credentials token request.", dependsOnMethods = "testRegisterApplication")
    public void testGetTokenUsingClientCredentialsGrant() throws Exception {

        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        Scope scope = new Scope(OAUTH2_SCOPE_OPENID, "xyz", VALID_RANDOM_SCOPE);

        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        Assert.assertTrue(tokenResponse.indicatesSuccess(),
                "Token response did not indicate success. Token request has failed.");

        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
        Assert.assertNotNull(accessTokenResponse.getTokens().getAccessToken(), "Access Token is null in response.");

        accessToken = accessTokenResponse.getTokens().getAccessToken().getValue();
        Assert.assertNotNull(accessToken, "Access Token is null in the token response.");

        Scope scopesInResponse = accessTokenResponse.getTokens().getAccessToken().getScope();
        Assert.assertFalse(scopesInResponse.contains("xyz"), "Not allowed random scope is issued for client credential " +
                "grant type.");
        Assert.assertTrue(scopesInResponse.contains(VALID_RANDOM_SCOPE), "Allowed random scope is not issued for " +
                "client credential grant type.");

        // This ensures that openid scopes are not issued for client credential grant type.
        Assert.assertFalse(accessTokenResponse instanceof OIDCTokenResponse, "Client credential grant type cannot " +
                "get a OIDC Token Response.");
        Assert.assertFalse(scopesInResponse.contains(OAUTH2_SCOPE_OPENID), "Client credentials cannot get openid scope.");
    }

    @Test(groups = "wso2.is", description = "Validate access token",
            dependsOnMethods = "testGetTokenUsingClientCredentialsGrant")
    public void testValidateAccessToken() throws Exception {

        String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
        org.json.simple.JSONObject responseObj = introspectTokenWithTenant(client, accessToken, introspectionUrl,
                username, userPassword);
        Assert.assertNotNull(responseObj, "Validate access token failed. response is invalid.");
        Assert.assertEquals(responseObj.get("active"), true, "Token Validation failed");
    }
}
