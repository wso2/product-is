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
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 test with Consented Token column integration abstraction.
 */
public class OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest extends
        OAuth2ServiceAbstractIntegrationTest {

    protected static final String TELEPHONE_CLAIM_URI = "http://wso2.org/claims/telephone";
    protected static final String USERNAME = "TestUser_1";
    protected static final String PASSWORD = "pass123";
    protected static final String PHONE_NUMBER = "0123456789";
    protected static final String USER_EMAIL = "abcrqo@wso2.com";
    protected static final String TELEPHONE_OIDC_CLAIM = "phone_number";
    protected static final String EMAIL_OIDC_CLAIM = "email";

    protected CookieStore cookieStore = new BasicCookieStore();

    protected HttpClient client;
    protected String accessToken;
    protected String refreshToken;

    protected void initConsentedTokenTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    protected void createUser() throws Exception {

        remoteUSMServiceClient.addUser(USERNAME, PASSWORD, new String[]{"admin"}, getUserClaims(), "default", true);
    }

    protected void removeUser() throws Exception {

        remoteUSMServiceClient.deleteUser(USERNAME);
    }

    protected void registerApplication() throws Exception {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token urn:ietf"
                + ":params:oauth:grant-type:saml2-bearer iwa:ntlm");
        appDTO.setTokenType("JWT"); // To get access token in JWT format.
        appDTO = createApplication(appDTO);
        updateApplicationClaimConfig();
        Assert.assertNotNull(appDTO, "Application creation failed.");

        consumerKey = appDTO.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDTO.getOauthConsumerSecret();
    }

    protected JSONObject sendRequestToUserInfoEndpoint() throws IOException {

        HttpGet request = new HttpGet(OAuth2Constant.USER_INFO_ENDPOINT);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        return ((JSONObject) obj);
    }

    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);
    }

    protected ClaimValue[] getUserClaims() {

        ClaimValue emailClaim = getClaimValue(EMAIL_CLAIM_URI, USER_EMAIL);
        ClaimValue telephone = getClaimValue(TELEPHONE_CLAIM_URI, PHONE_NUMBER);
        return new ClaimValue[] {emailClaim, telephone};
    }

    private ClaimValue getClaimValue(String claimURL, String claimVal) {

        ClaimValue claim = new ClaimValue();
        claim.setClaimURI(claimURL);
        claim.setValue(claimVal);
        return claim;
    }

    protected OIDCTokens makeTokenRequest(AuthorizationGrant authorizationGrant) throws URISyntaxException,
            IOException, ParseException {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        // email scope is to retrieve the email address of the user.
        // phone scope is to retrieve the phone number of the user.
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                        OAuth2Constant.OAUTH2_SCOPE_PHONE));

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token");
        return oidcTokens;
    }

    private void setServiceProviderClaimConfig(ServiceProvider serviceProvider) {

        ClaimConfig claimConfig = new ClaimConfig();
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(EMAIL_CLAIM_URI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);

        Claim phoneNumberClaim = new Claim();
        phoneNumberClaim.setClaimUri(TELEPHONE_CLAIM_URI);
        ClaimMapping phoneNumberClaimMapping = new ClaimMapping();
        phoneNumberClaimMapping.setRequested(true);
        phoneNumberClaimMapping.setLocalClaim(phoneNumberClaim);
        phoneNumberClaimMapping.setRemoteClaim(phoneNumberClaim);

        claimConfig.setClaimMappings(
                new org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping[] { emailClaimMapping,
                        phoneNumberClaimMapping});
        serviceProvider.setClaimConfig(claimConfig);
    }

    protected void updateApplicationClaimConfig() throws Exception {

        ServiceProvider serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        setServiceProviderClaimConfig(serviceProvider);
        appMgtclient.updateApplicationData(serviceProvider);
    }

    protected void resetVariables() {

        accessToken = null;
        refreshToken = null;
        consumerKey = null;
        consumerSecret = null;
    }
}
