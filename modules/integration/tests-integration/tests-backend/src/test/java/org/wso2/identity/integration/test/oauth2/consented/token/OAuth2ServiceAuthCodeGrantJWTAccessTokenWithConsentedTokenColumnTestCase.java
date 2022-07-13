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

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains test cases for Authorization Grant & Refresh Grant with JWT access token when consented token
 * column is enabled.
 */
public class OAuth2ServiceAuthCodeGrantJWTAccessTokenWithConsentedTokenColumnTestCase extends
        OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest {

    private final List<NameValuePair> consentParameters = new ArrayList<>();
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

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

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        // email scope is to retrieve the email address of the user.
        // phone scope is to retrieve the telephone number of the user.
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " "
                + OAuth2Constant.OAUTH2_SCOPE_EMAIL + " " + OAuth2Constant.OAUTH2_SCOPE_PHONE));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed. Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorizedPost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        EntityUtils.consume(response.getEntity());
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendConsentGetRequest(locationHeader.getValue(), consentParameters);
        // Remove the consent from phone number claim.
        consentParameters.remove(1);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null");

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send approval post request", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractTableRowDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(response, "Authorization Code key value is invalid.");

        assert keyValues != null;
        authorizationCode = keyValues.get(0).getValue();
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessToken() throws Exception {

        URI callbackURI = new URI(OAuth2Constant.CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant =
                new AuthorizationCodeGrant(new AuthorizationCode(authorizationCode), callbackURI);
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        // email scope is to retrieve the email address of the user.
        // phone scope is to retrieve the telephone number of the user.
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationCodeGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                        OAuth2Constant.OAUTH2_SCOPE_PHONE));

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains "
                + "errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        validateUserClaims(oidcTokens);
    }

    @Test(groups = "wso2.is", description = "Validate the user claim values", dependsOnMethods = "testGetAccessToken")
    public void testClaims() throws Exception {

        org.json.simple.JSONObject userInfoEndpointResponse = sendRequestToUserInfoEndpoint();
        Assert.assertEquals(USER_EMAIL, userInfoEndpointResponse.get(EMAIL_OIDC_CLAIM).toString(),
                "Incorrect email claim value");
        Assert.assertNull(userInfoEndpointResponse.get(TELEPHONE_OIDC_CLAIM), "A value for telephone claim is "
                + "present in the response.");
    }

    @Test(description = "This test case tests refresh token flow of JWTBearerGrant.", dependsOnMethods = "testClaims")
    public void testRefreshTokenFlow() throws Exception {

        for (int i = 0; i < 3; i++) { // Test the refresh token flow for 3 times.
            AuthorizationGrant refreshGrant = new RefreshTokenGrant(new RefreshToken(refreshToken));
            OIDCTokens oidcTokens = makeTokenRequest(refreshGrant);
            validateUserClaims(oidcTokens);
        }
    }

    private void validateUserClaims(OIDCTokens oidcTokens) throws JSONException, ParseException {

        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token");
        accessToken = oidcTokens.getAccessToken().getValue();
        refreshToken = oidcTokens.getRefreshToken().getValue();

        // Get the user info from the JWT access token.
        JSONObject jwtJsonObject = new JSONObject(new String(Base64.decodeBase64(accessToken.split("\\.")[1])));
        String email = jwtJsonObject.getString(EMAIL_OIDC_CLAIM);
        Assert.assertEquals(USER_EMAIL, email, "Requested user claim (Email) is not present in the JWT access "
                + "token.");
        Assert.assertTrue(jwtJsonObject.isNull(TELEPHONE_OIDC_CLAIM), "Non-consented user claim (Phone Number) is"
                + " present in the JWT access token.");

        // Get the user info from the ID token.
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM).toString(), USER_EMAIL,
                "Requested user claims is not returned back with the ID token.");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(TELEPHONE_OIDC_CLAIM), "Non-requested "
                + "user claim (phone_number) is returned back with the ID Token.");
    }

    private HttpResponse sendConsentGetRequest(String locationURL,
            List<NameValuePair> consentRequiredClaimsFromResponse) throws Exception {

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = httpClientWithoutAutoRedirections.execute(getRequest);

        consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        return httpResponse;
    }
}
