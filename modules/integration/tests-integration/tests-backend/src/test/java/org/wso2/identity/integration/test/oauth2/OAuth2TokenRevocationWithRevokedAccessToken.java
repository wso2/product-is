/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenRevocationRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;

/**
 * This test class is used to check the behaviour of OAuth token revocation flow.
 */
public class OAuth2TokenRevocationWithRevokedAccessToken extends OAuth2ServiceAbstractIntegrationTest {

    private ClientID consumerKey;
    private Secret consumerSecret;

    private final String tokenType;
    private final String username;
    private final String userPassword;
    private final String activeTenant;
    private static final String TENANT_DOMAIN = "wso2.com";

    @Factory(dataProvider = "oAuthConsumerApplicationProvider")
    public OAuth2TokenRevocationWithRevokedAccessToken(String tokenType, TestUserMode userMode) throws Exception {

        super.init(userMode);
        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.activeTenant = context.getContextTenant().getDomain();
        this.tokenType = tokenType;
    }

    @DataProvider(name = "oAuthConsumerApplicationProvider")
    public static Object[][] oAuthConsumerApplicationProvider() {

        // This test will be carried out for both default and JWT access tokens
        return new Object[][]{
                {"Default", TestUserMode.SUPER_TENANT_ADMIN},
                {"Default", TestUserMode.TENANT_ADMIN},
                {"JWT", TestUserMode.SUPER_TENANT_ADMIN},
                {"JWT", TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setTokenType(tokenType);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication(appDTO);

        consumerKey = new ClientID(oAuthConsumerAppDTO.getOauthConsumerKey());
        consumerSecret = new Secret(oAuthConsumerAppDTO.getOauthConsumerSecret());
    }

    @Test(description = "Call revocation request with a revoked access token")
    public void testRevokedAccessTokenRevocation() throws Exception {

        // Request an access token
        AccessToken accessToken = requestAccessToken();
        AccessToken privilegedAccessToken = requestPrivilegedAccessToken();

        // Introspect the returned access token to verify the validity before revoking
        TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
        Assert.assertTrue(activeTokenIntrospectionResponse.toSuccessResponse().isActive(),
                "Introspection response of an active access token is unsuccessful.");

        // Revoke the access token returned above
        HTTPResponse activeTokenRevocationResponse = revokeAccessToken(accessToken);
        Assert.assertEquals(activeTokenRevocationResponse.getStatusCode(), 200, "Revocation request with " +
                "an active access token has been failed.");

        // Introspect the revoked access token to verify the token has been revoked
        TokenIntrospectionResponse revokedTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
        // According to the spec 200 status code will be returned when when token is has been revoked or is otherwise
        // invalid. Need to check token active status here.
        Assert.assertFalse(revokedTokenIntrospectionResponse.toSuccessResponse().isActive(),
                "Introspection response of a revoked access token is successful.");

        // Make a revocation request with the same access token which has been revoked already
        HTTPResponse revokedTokenRevocationResponse = revokeAccessToken(accessToken);
        Assert.assertEquals(revokedTokenRevocationResponse.getStatusCode(), 200, "Revocation request with " +
                "an already revoked access token has been failed.");
    }

    @Test(dependsOnMethods = {"testRevokedAccessTokenRevocation"},
            description = "Call revocation request with a revoked access token but invalid auth credentials")
    public void testRevokedAccessTokenRevocationWithInvalidClientCredentials() throws Exception {

        // Request an access token
        AccessToken accessToken = requestAccessToken();
        AccessToken privilegedAccessToken = requestPrivilegedAccessToken();

        // Introspect the returned access token to verify the validity before revoking
        TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
        Assert.assertTrue(activeTokenIntrospectionResponse.toSuccessResponse().isActive(),
                "Introspection response of an active access token is unsuccessful.");

        // Revoke the access token returned above
        HTTPResponse activeTokenRevocationResponse = revokeAccessToken(accessToken);
        Assert.assertEquals(activeTokenRevocationResponse.getStatusCode(), 200, "Revocation request with " +
                "an active access token has been failed.");

        // Introspect the revoked access token to verify the token has been revoked
        TokenIntrospectionResponse revokedTokenIntrospectionResponse = introspectAccessToken(accessToken, privilegedAccessToken);
        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Failed to receive a success response.");
        // According to the spec 200 status code will be returned when when token is has been revoked or is otherwise
        // invalid. Need to check token active status here.
        Assert.assertFalse(revokedTokenIntrospectionResponse.toSuccessResponse().isActive(),
                "Introspection response of a revoked access token is successful.");

        // Make a revocation request with the same access token which has been revoked already and verify the authentication failure error response
        Secret invalidConsumerSecret = new Secret("dummyConsumerSecret");
        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, invalidConsumerSecret);
        HTTPResponse revokedTokenRevocationResponse = revokeAccessToken(accessToken, clientAuth);
        Assert.assertEquals(revokedTokenRevocationResponse.getStatusCode(), 401,
                "Client credentials are invalid.");
    }

    private AccessToken requestAccessToken() throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(username,
                new Secret(userPassword));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private AccessToken requestPrivilegedAccessToken() throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant(username,
                new Secret(userPassword));

        Scope scope = new Scope("internal_application_mgt_view");

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, scope);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private HTTPResponse revokeAccessToken(AccessToken accessToken) throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        String tokenRevokeUrl = activeTenant.equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.TOKEN_REVOKE_ENDPOINT : OAuth2Constant.TENANT_TOKEN_REVOKE_ENDPOINT;
        URI tokenRevokeEndpoint = new URI(tokenRevokeUrl);

        TokenRevocationRequest revocationRequest =
                new TokenRevocationRequest(tokenRevokeEndpoint, clientAuth, accessToken);
        return revocationRequest.toHTTPRequest().send();
    }

    private HTTPResponse revokeAccessToken(AccessToken accessToken, ClientAuthentication clientAuth) throws Exception {

        URI tokenRevokeEndpoint = new URI(OAuth2Constant.TOKEN_REVOKE_ENDPOINT);

        TokenRevocationRequest revocationRequest =
                new TokenRevocationRequest(tokenRevokeEndpoint, clientAuth, accessToken);
        return revocationRequest.toHTTPRequest().send();
    }

    private TokenIntrospectionResponse introspectAccessToken(AccessToken accessToken, AccessToken privilegedAccessToken) throws Exception {

        URI introSpecEndpoint;
        if (TENANT_DOMAIN.equals(activeTenant)) {
            introSpecEndpoint = new URI(OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT);
        } else {
            introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        }
        BearerAccessToken bearerAccessToken = new BearerAccessToken(privilegedAccessToken.getValue());
        TokenIntrospectionRequest TokenIntroRequest = new TokenIntrospectionRequest(introSpecEndpoint,
                bearerAccessToken,
                accessToken);
        HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();
        Assert.assertNotNull(introspectionHTTPResp, "Introspection http response is null.");

        return TokenIntrospectionResponse.parse(introspectionHTTPResp);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
    }
}
