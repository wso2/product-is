/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.UserUtil;

/**
 * Integration test cases for sub attribute of the OIDC responses (id_token and JWT access token).
 */
public class OIDCSubAttributeTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String idToken;
    private String userId;
    private final boolean legacyMode;
    private final String tokenType;
    private String accessToken;
    private String tenantAwareUsername;

    @Factory(dataProvider = "subAttributeTypeProvider")
    public OIDCSubAttributeTestCase(boolean legacyMode, String tokenType) {
        this.legacyMode = legacyMode;
        this.tokenType = tokenType;
    }

    @DataProvider(name = "subAttributeTypeProvider")
    public static Object[][] subAttributeTypeProvider() {
        return new Object[][] {
                {true, "Default"},
                {false, "Default"},
                {true, "JWT"},
                {false, "JWT"},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
        tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userInfo.getUserName());
        if (legacyMode) {
            userId = tenantAwareUsername;
        } else {
            userId = UserUtil.getUserId(tenantAwareUsername, isServer.getContextTenant());
        }
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        consumerSecret = null;

        client.close();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration.")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testRegisterApplication")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.",
            dependsOnMethods = "testAuthCodeGrantSendAuthRequestPost")
    public void testAuthCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testAuthCodeGrantSendLoginPost")
    public void testAuthCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send get access token request.",
            dependsOnMethods = "testAuthCodeGrantSendApprovalPost")
    public void testAuthCodeGrantSendGetTokensPost() throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");
        accessToken = oidcTokens.getAccessToken().getValue();
    }

    @Test(groups = "wso2.is", description = "Validate the sub attribute in JWT tokens",
            dependsOnMethods = "testAuthCodeGrantSendGetTokensPost")
    public void testAuthCodeGrantValidateSub() throws Exception {

        SignedJWT jwt = SignedJWT.parse(idToken);
        String subject = jwt.getJWTClaimsSet().getSubject();

        Assert.assertEquals(subject, userId, "Subject received in the id token is different from user id");

        if ("JWT".equals(tokenType)) {
            jwt = SignedJWT.parse(accessToken);
            subject = jwt.getJWTClaimsSet().getSubject();

            Assert.assertEquals(subject, userId, "Subject received in the jwt access token is different from user id");
        }
    }

    @Test(groups = "wso2.is", description = "Validate sub attribute in user info call",
            dependsOnMethods = "testAuthCodeGrantValidateSub")
    public void testAuthCodeGrantValidateUserInfo() throws Exception {

        UserInfoResponse userInfoResponse = getUserInfoResponse();

        if (!userInfoResponse.indicatesSuccess()) {
            Assert.fail("User info API call failed.");
        }

        // Extract the claims
        UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
        Assert.assertEquals(userInfo.getSubject().getValue(), userId,
                "Subject received in the user info response is different from user id");
    }

    @Test(groups = "wso2.is", description = "Validate username attribute in introspection call",
            dependsOnMethods = "testAuthCodeGrantValidateSub")
    public void testAuthCodeGrantValidateIntrospectResponse() throws Exception {

        TokenIntrospectionResponse response = getTokenIntrospectionResponse();

        if (!response.indicatesSuccess()) {
            Assert.fail("Introspect call failed.");
        }

        Assert.assertEquals(response.toSuccessResponse().getUsername(), userInfo.getUserName(),
                "Username received in the introspect response is different from username");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for resource owner grant type.",
            dependsOnMethods = "testAuthCodeGrantValidateSub")
    public void testResourceOwnerGrantSendAuthRequestPost() throws Exception {

        // Remove previous data from variables.
        sessionDataKey = null;
        sessionDataKeyConsent = null;
        idToken = null;

        // Reset client.
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();

        Secret password = new Secret(userInfo.getPassword());
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(tenantAwareUsername, password);

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        Scope scope = new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID);

        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, scope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");

        accessToken = oidcTokens.getAccessToken().getValue();
    }

    @Test(groups = "wso2.is", description = "Validate the sub attribute in JWT tokens",
            dependsOnMethods = "testResourceOwnerGrantSendAuthRequestPost")
    public void testResourceOwnerGrantValidateSub() throws Exception {

        SignedJWT jwt = SignedJWT.parse(idToken);
        String subject = jwt.getJWTClaimsSet().getSubject();
        Assert.assertEquals(subject, userId, "Subject received in the id token is different from user id");

        if ("JWT".equals(tokenType)) {
            jwt = SignedJWT.parse(accessToken);
            subject = jwt.getJWTClaimsSet().getSubject();

            Assert.assertEquals(subject, userId, "Subject received in the jwt access token is different from user id");
        }
    }

    @Test(groups = "wso2.is", description = "Validate sub attribute in user info call",
            dependsOnMethods = "testResourceOwnerGrantSendAuthRequestPost")
    public void testResourceOwnerGrantValidateUserInfo() throws Exception {

        UserInfoResponse userInfoResponse = getUserInfoResponse();

        if (!userInfoResponse.indicatesSuccess()) {
            Assert.fail("User info API call failed.");
        }

        // Extract the claims
        UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
        Assert.assertEquals(userInfo.getSubject().getValue(), userId,
                "Subject received in the user info response is different from user id");
    }

    @Test(groups = "wso2.is", description = "Validate username in introspect response",
            dependsOnMethods = "testResourceOwnerGrantValidateUserInfo")
    public void testResourceOwnerGrantValidateIntrospectResponse() throws Exception {

        TokenIntrospectionResponse response = getTokenIntrospectionResponse();

        if (!response.indicatesSuccess()) {
            Assert.fail("Introspect call failed.");
        }

        Assert.assertEquals(response.toSuccessResponse().getUsername(), userInfo.getUserName(),
                "Username received in the introspect response is different from username");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for implicit grant type.",
            dependsOnMethods = "testResourceOwnerGrantValidateUserInfo")
    public void testImplicitGrantSendAuthRequestPost() throws Exception {

        // Remove previous data from variables.
        sessionDataKey = null;
        sessionDataKeyConsent = null;
        idToken = null;

        // Reset client.
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();

        // Send a direct implicit token request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.ID_TOKEN + " " + "token"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.",
            dependsOnMethods = "testImplicitGrantSendAuthRequestPost")
    public void testImplicitGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testImplicitGrantSendLoginPost")
    public void testImplicitGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        // Extract authorization code from the location value.
        idToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.ID_TOKEN);
        accessToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                OAuth2Constant.ACCESS_TOKEN);
        Assert.assertNotNull(idToken, "ID token is null");
        Assert.assertNotNull(accessToken, "access token is null");
    }

    @Test(groups = "wso2.is", description = "Validate the sub attribute in JWT tokens",
            dependsOnMethods = "testImplicitGrantSendApprovalPost")
    public void testImplicitGrantValidateSub() throws Exception {

        SignedJWT jwt = SignedJWT.parse(idToken);
        String subject = jwt.getJWTClaimsSet().getSubject();
        Assert.assertEquals(subject, userId, "Subject received in the id token is different from user id");

        if ("JWT".equals(tokenType)) {
            jwt = SignedJWT.parse(accessToken);
            subject = jwt.getJWTClaimsSet().getSubject();

            Assert.assertEquals(subject, userId, "Subject received in the jwt access token is different from user id");
        }
    }

    @Test(groups = "wso2.is", description = "Validate sub attribute in user info call",
            dependsOnMethods = "testImplicitGrantValidateSub")
    public void testImplicitGrantValidateUserInfo() throws Exception {

        UserInfoResponse userInfoResponse = getUserInfoResponse();

        if (!userInfoResponse.indicatesSuccess()) {
            Assert.fail("User info API call failed.");
        }

        // Extract the claims
        UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
        Assert.assertEquals(userInfo.getSubject().getValue(), userId,
                "Subject received in the user info response is different from user id");
    }

    @Test(groups = "wso2.is", description = "Validate username in introspect response",
            dependsOnMethods = "testImplicitGrantValidateUserInfo")
    public void testImplicitGrantValidateIntrospectResponse() throws Exception {

        TokenIntrospectionResponse response = getTokenIntrospectionResponse();

        if (!response.indicatesSuccess()) {
            Assert.fail("Introspect call failed.");
        }

        Assert.assertEquals(response.toSuccessResponse().getUsername(), userInfo.getUserName(),
                "Username received in the introspect response is different from username");
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException
     * @throws URISyntaxException
     */
    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    public ServiceProvider registerServiceProviderWithOAuthInboundConfigs(OAuthConsumerAppDTO appDTO)
            throws Exception {

        ServiceProvider serviceProvider = generateServiceProvider(appDTO);

        if (legacyMode) {
            ArrayList<ServiceProviderProperty> serviceProviderProperties
                    = new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));

            boolean containsUseUserIdForSubjectProp = false;
            for (ServiceProviderProperty prop: serviceProviderProperties) {
                if ("useUserIdForDefaultSubject".equals(prop.getName())) {
                    containsUseUserIdForSubjectProp = true;
                    prop.setValue("false");
                    break;
                }
            }

            if (!containsUseUserIdForSubjectProp) {
                ServiceProviderProperty useUserIdForSubject = new ServiceProviderProperty();
                useUserIdForSubject.setName("useUserIdForDefaultSubject");
                useUserIdForSubject.setValue("false");
                serviceProviderProperties.add(useUserIdForSubject);
            }
            serviceProvider.setSpProperties(serviceProviderProperties.toArray(new ServiceProviderProperty[0]));
        }

        return getServiceProvider(serviceProvider);
    }

    public OAuthConsumerAppDTO getBasicOAuthApp(String callBackURL) {
        OAuthConsumerAppDTO basicOAuthApp = super.getBasicOAuthApp(callBackURL);
        basicOAuthApp.setTokenType(tokenType);
        return basicOAuthApp;
    }

    private UserInfoResponse getUserInfoResponse() throws IOException, URISyntaxException, ParseException {
        BearerAccessToken token = new BearerAccessToken(accessToken);
        HTTPResponse httpResponse = new UserInfoRequest(new URI(OAuth2Constant.USER_INFO_ENDPOINT), token)
                .toHTTPRequest()
                .send();

        // Parse the response
        return UserInfoResponse.parse(httpResponse);
    }

    private TokenIntrospectionResponse getTokenIntrospectionResponse()
            throws URISyntaxException, IOException, ParseException {
        // The introspection endpoint
        URI introspectionEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);

        ClientID clientID = new ClientID(tenantAwareUsername);
        Secret clientSecret = new Secret(userInfo.getPassword());

        // Token to validate
        BearerAccessToken inspectedToken = new BearerAccessToken(accessToken);

        // Compose the introspection call
        HTTPRequest httpRequest = new TokenIntrospectionRequest(
                introspectionEndpoint,
                new ClientSecretBasic(clientID, clientSecret),
                inspectedToken)
                .toHTTPRequest();

        // Make the introspection call
        HTTPResponse httpResponse = httpRequest.send();

        return TokenIntrospectionResponse.parse(httpResponse);
    }
}
