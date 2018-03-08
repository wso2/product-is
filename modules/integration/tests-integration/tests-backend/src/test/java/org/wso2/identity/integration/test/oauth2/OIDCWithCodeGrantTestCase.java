/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.apache.oltu.oauth2.common.message.types.GrantType.AUTHORIZATION_CODE;

/**
 * This class tests the OIDC flow with Oauth2 authorization code grant.
 */
public class OIDCWithCodeGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String STATE = "request-state";

    private static final String NONCE = "request-nonce";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String REFRESH_TOKEN = "refresh_token";

    private static final String SCOPE = "scope";

    private static final String ID_TOKEN = "id_token";

    private static final String TOKEN_TYPE = "token_type";

    private static final String EXPIRES_IN = "expires_in";

    private TestConfig config;

    private String consumerKey;

    private String consumerSecret;

    private OkHttpClient client;

    private String sessionDataKey;

    private String sessionDataKeyConsent;

    private String authorizationCode;

    @DataProvider(name = "testConfigProvider")
    public static TestConfig[][] testConfigProvider() {

        return new TestConfig[][]{
                {new TestConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER)},
                {new TestConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER)}
        };
    }

    @Factory(dataProvider = "testConfigProvider")
    public OIDCWithCodeGrantTestCase(TestConfig config) {

        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(config.getUserMode());
        remoteUSMServiceClient.addUser(config.getUser().getTenantAwareUsername(), config.getUser().getPassword(), new
                String[]{"admin"}, getUserClaims(), "default", true);
        client = new OkHttpClient();
    }

    @Test(groups = "wso2.is", description = "Register Oauth2 application")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO appDTO = createApplication();
        Assert.assertNotNull(appDTO, "Oauth2 application creation failed.");
        consumerKey = appDTO.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Invalid consumer key. Oauth2 application creation failed.");
        consumerSecret = appDTO.getOauthConsumerSecret();
        Assert.assertNotNull(consumerSecret, "Invalid consumer secret. Oauth2 application creation failed.");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        consumerKey = null;
        consumerSecret = null;
        client = null;
        sessionDataKey = null;
        sessionDataKeyConsent = null;
        authorizationCode = null;
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testAuthorizationRequest() throws Exception {

        String url = getAuthorizationRequest();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertNotNull(response, "Authorization request failed.");
            Assert.assertNotNull(response.request().url().queryParameter("sessionDataKey"), "Failed to get the " +
                    "sessionDataKey value.");
            sessionDataKey = response.request().url().queryParameter("sessionDataKey");
        }
    }

    @Test(groups = "wso2.is", description = "Authenticate the user", dependsOnMethods = "testAuthorizationRequest")
    public void testAuthentication() throws Exception {

        String url = getResourceUrl("commonauth");
        RequestBody requestBody = new FormBody.Builder()
                .add("username", config.getUser().getUsername())
                .add("password", config.getUser().getPassword())
                .add("sessionDataKey", sessionDataKey)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertNotNull(response, "Authentication failed.");
            Assert.assertNotNull(response.request().url().queryParameter("sessionDataKeyConsent"), "Failed to get " +
                    "the  sessionDataKeyConsent value.");
            sessionDataKeyConsent = response.request().url().queryParameter("sessionDataKeyConsent");
        }
    }

    @Test(groups = "wso2.is", description = "Grant user consent", dependsOnMethods = "testAuthentication")
    public void testUserConsent() throws Exception {

        String url = getResourceUrl("oauth2/authorize");
        RequestBody requestBody = new FormBody.Builder()
                .add("hasApprovedAlways", "false")
                .add("sessionDataKeyConsent", sessionDataKeyConsent)
                .add("consent", "approve")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        OkHttpClient noRedirectionClient = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        try (Response response = noRedirectionClient.newCall(request).execute()) {
            Assert.assertNotNull(response, "User consent failed.");
            Assert.assertNotNull(response.header("Location"), "Failed to retrieve the Location header.");

            authorizationCode = getParameterValue(response.header("Location"), "code");
            Assert.assertNotNull(authorizationCode, "Failed to receive the authorization code.");
        }
    }

    @Test(groups = "wso2.is", description = "Grant user consent", dependsOnMethods = "testUserConsent")
    public void testTokenRequest() throws Exception {

        String url = getResourceUrl("oauth2/token");
        OAuthClientRequest.TokenRequestBuilder oAuthTokenRequestBuilder =
                new OAuthClientRequest.TokenRequestBuilder(url);
        OAuthClientRequest accessRequest = oAuthTokenRequestBuilder.setGrantType(AUTHORIZATION_CODE)
                .setClientId(consumerKey)
                .setClientSecret(consumerSecret)
                .setRedirectURI(OAuth2Constant.CALLBACK_URL)
                .setCode(authorizationCode)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientResponse response = oAuthClient.accessToken(accessRequest);
        Assert.assertNotNull(response, "Failed to get the token response.");

        Assert.assertNotNull(response.getParam(ACCESS_TOKEN), "Invalid access token.");
        Assert.assertNotNull(response.getParam(REFRESH_TOKEN), "Invalid refresh token.");

        String scopes = response.getParam(SCOPE);
        Assert.assertNotNull(scopes, "Did not receive scopes.");
        List<String> scopesList = Arrays.asList(scopes.split(" "));
        Assert.assertTrue(scopesList.contains(OAuth2Constant.OAUTH2_SCOPE_OPENID), "Did not receive the openid " +
                "scope.");
        Assert.assertTrue(scopesList.contains(OAuth2Constant.OAUTH2_SCOPE_EMAIL), "Did not receive the email scope.");

        String idToken = response.getParam(ID_TOKEN);
        Assert.assertNotNull(idToken, "Did not receive the id_token.");

        ReadOnlyJWTClaimsSet jwtClaimsSet = SignedJWT.parse(idToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), config.getUser().getTenantAwareUsername(), "Invalid subject " +
                "value received.");
        Assert.assertEquals(jwtClaimsSet.getClaim("email"), config.getUser().getEmail(), "Invalid email claim " +
                "received.");
        Assert.assertEquals(jwtClaimsSet.getClaim("nonce"), NONCE, "Invalid nonce received.");

        Assert.assertEquals(response.getParam(TOKEN_TYPE), "Bearer", "Invalid response type.");
        Assert.assertNotNull(response.getParam(EXPIRES_IN), "Did not receive the expires_in value.");
    }

    private String getAuthorizationRequest() throws OAuthSystemException {

        OAuthClientRequest.AuthenticationRequestBuilder oAuthAuthenticationRequestBuilder =
                new OAuthClientRequest.AuthenticationRequestBuilder(getResourceUrl("oauth2/authorize"));
        oAuthAuthenticationRequestBuilder
                .setClientId(consumerKey)
                .setRedirectURI(OAuth2Constant.CALLBACK_URL)
                .setResponseType(OAuth2Constant.OAUTH2_GRANT_TYPE_CODE)
                .setScope(OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant.OAUTH2_SCOPE_EMAIL)
                .setState(STATE);
        OAuthClientRequest authzRequest = oAuthAuthenticationRequestBuilder.buildQueryMessage();
        return authzRequest.getLocationUri() + "&nonce=" + NONCE;
    }

    private ClaimValue[] getUserClaims() {

        ClaimValue[] claimValues = new ClaimValue[1];
        ClaimValue email = new ClaimValue();
        email.setClaimURI("http://wso2.org/claims/emailaddress");
        email.setValue(config.getUser().getEmail());
        claimValues[0] = email;
        return claimValues;
    }

    private String getResourceUrl(String resource) {

        return backendURL.replace("services/", resource);
    }

    private String getParameterValue(String url, String paramName) throws URISyntaxException {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
        for (NameValuePair param : params) {
            if (param.getName().equals(paramName)) {
                return param.getValue();
            }
        }
        return null;
    }

    private static class TestConfig {

        private TestUserMode userMode;
        private User user;

        public TestConfig(TestUserMode userMode, User user) {

            this.userMode = userMode;
            this.user = user;
        }

        public TestUserMode getUserMode() {

            return userMode;
        }

        public void setUserMode(TestUserMode userMode) {

            this.userMode = userMode;
        }

        public User getUser() {

            return user;
        }

        public void setUser(User user) {

            this.user = user;
        }

        @Override
        public String toString() {

            return "TestConfig[" +
                    ", userMode=" + userMode.name() +
                    ", user=" + user.getUsername() +
                    ']';
        }
    }

    private enum User {

        SUPER_TENANT_USER("openiduser1", "pass123", "carbon.super", "openiduser1", "openiduser1@abc.com"),
        TENANT_USER("openiduser2@wso2.com", "pass123", "wso2.com", "openiduser2", "openiduser2@abc.com");

        private String username;
        private String password;
        private String tenantDomain;
        private String tenantAwareUsername;
        private String email;

        User(String username, String password, String tenantDomain, String tenantAwareUsername, String email) {

            this.username = username;
            this.password = password;
            this.tenantDomain = tenantDomain;
            this.tenantAwareUsername = tenantAwareUsername;
            this.email = email;
        }

        public String getUsername() {

            return username;
        }

        public String getPassword() {

            return password;
        }

        public String getTenantDomain() {

            return tenantDomain;
        }

        public String getTenantAwareUsername() {

            return tenantAwareUsername;
        }

        public String getEmail() {

            return email;
        }
    }
}
