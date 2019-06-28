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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
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

    private String tokenType;

    @Factory(dataProvider = "oAuthConsumerApplicationProvider")
    public OAuth2TokenRevocationWithRevokedAccessToken(String tokenType) {

        this.tokenType = tokenType;
    }

    @DataProvider(name = "oAuthConsumerApplicationProvider")
    public static Object[][] oAuthConsumerApplicationProvider() {

        // This test will be carried out for both default and JWT access tokens
        return new Object[][]{
                {"Default"},
                {"JWT"}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

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

        // Introspect the returned access token to verify the validity before revoking
        TokenIntrospectionResponse activeTokenIntrospectionResponse = introspectAccessToken(accessToken);
        Assert.assertTrue(activeTokenIntrospectionResponse.indicatesSuccess(), "Introspection response of an " +
                "active access token is unsuccessful.");

        // Revoke the access token returned above
        HTTPResponse activeTokenRovocationRespose = revokeAccessToken(accessToken);
        Assert.assertEquals(activeTokenRovocationRespose.getStatusCode(), 200, "Revocation request with " +
                "an active access token has been failed.");

        // Introspect the revoked access token to verify the token has been revoked
        TokenIntrospectionResponse revokedTokenIntrospectionResponse = introspectAccessToken(accessToken);
        Assert.assertFalse(revokedTokenIntrospectionResponse.indicatesSuccess(), "Introspection response of a revoked" +
                " access token is successful.");

        // Make a revocation request with the same access token which has been revoked already
        HTTPResponse revokedTokenRevocationResponse = revokeAccessToken(accessToken);
        Assert.assertEquals(revokedTokenRevocationResponse.getStatusCode(), 200, "Revocation request with " +
                "an already revoked access token has been failed.");
    }

    private AccessToken requestAccessToken() throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        AuthorizationGrant authorizationGrant = new ResourceOwnerPasswordCredentialsGrant("admin",
                new Secret("admin"));

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant, null);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();

        AccessTokenResponse accessTokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
        return accessTokenResponse.getTokens().getAccessToken();
    }

    private HTTPResponse revokeAccessToken(AccessToken accessToken) throws Exception {

        ClientAuthentication clientAuth = new ClientSecretBasic(consumerKey, consumerSecret);
        URI tokenRevokeEndpoint = new URI(OAuth2Constant.TOKEN_REVOKE_ENDPOINT);

        TokenRevocationRequest revocationRequest =
                new TokenRevocationRequest(tokenRevokeEndpoint, clientAuth, accessToken);
        return revocationRequest.toHTTPRequest().send();
    }

    private TokenIntrospectionResponse introspectAccessToken(AccessToken accessToken) throws Exception {

        URI introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
        BearerAccessToken bearerAccessToken = new BearerAccessToken(accessToken.getValue());
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
