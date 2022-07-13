/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.oauth2.consented.token;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OAuth2ServicePasswordGrantJWTAccessTokenWithConsentedTokenColumnTestCase extends
        OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());

    @BeforeClass
    public void setup() throws Exception {

        super.initConsentedTokenTest();
        createUser();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();
        removeUser();
        resetVariables();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        registerApplication();
    }


    @Test(description = "This test case tests the JWT access token generation using password grant type.",
            dependsOnMethods = "testRegisterApplication")
    public void testPasswordGrantBasedAccessTokenGeneration() throws IOException, URISyntaxException, ParseException,
            java.text.ParseException, JSONException {

        Secret password = new Secret(PASSWORD);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(USERNAME, password);
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        // email scope is to retrieve the email address of the user.
        // phone scope is to retrieve the phone number of the user.
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                        OAuth2Constant.OAUTH2_SCOPE_PHONE));

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");
        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        validateUserClaims(oidcTokens);
    }

    @Test(description = "This test case tests refresh token flow of JWTBearerGrant.", dependsOnMethods =
            "testPasswordGrantBasedAccessTokenGeneration")
    public void testRefreshTokenFlow() throws Exception {

        for (int i = 0; i < 3; i++) { // Test the refresh token flow for 3 times.
            AuthorizationGrant refreshGrant = new RefreshTokenGrant(new RefreshToken(refreshToken));
            OIDCTokens oidcTokens = makeTokenRequest(refreshGrant);
            validateUserClaims(oidcTokens);
        }
    }

    @Test(groups = "wso2.is", description = "Validate the user claim values", dependsOnMethods =
            "testPasswordGrantBasedAccessTokenGeneration")
    public void testClaims() throws Exception {

        org.json.simple.JSONObject userInfoEndpointResponse = sendRequestToUserInfoEndpoint();
        String email = userInfoEndpointResponse.get(EMAIL_OIDC_CLAIM).toString();
        String phoneNumber = userInfoEndpointResponse.get(TELEPHONE_OIDC_CLAIM).toString();

        Assert.assertEquals(PHONE_NUMBER, phoneNumber, "Incorrect phone number name claim value");
        Assert.assertEquals(USER_EMAIL, email, "Incorrect email claim value");
    }

    private void validateUserClaims(OIDCTokens oidcTokens) throws JSONException, java.text.ParseException {

        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token");
        accessToken = oidcTokens.getAccessToken().getValue();
        refreshToken = oidcTokens.getRefreshToken().getValue(); // Get the new refresh token.

        // Get the user info from the JWT access token.
        org.json.JSONObject jwtJsonObject = new org.json.JSONObject(new String(Base64.decodeBase64(accessToken.split(
                "\\.")[1])));
        String email = jwtJsonObject.get(EMAIL_OIDC_CLAIM).toString();
        String phoneNumber = jwtJsonObject.get(TELEPHONE_OIDC_CLAIM).toString();

        // Check the user info of the JWT access token.
        Assert.assertEquals(USER_EMAIL, email, "Requested user claim (email) is not present in the JWT access token.");
        Assert.assertEquals(PHONE_NUMBER, phoneNumber, "Requested user claim (phone_number) is not present in the JWT "
                + "access token.");

        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), USER_EMAIL,
                "Requested user claims is not returned back with the ID token.");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(TELEPHONE_OIDC_CLAIM), PHONE_NUMBER,
                "Requested user claims is not returned back with the ID token.");
    }
}
