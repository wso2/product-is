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
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OAuth2 test with Consented Token column integration abstraction.
 */
public class OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest extends OAuth2ServiceAbstractIntegrationTest {

    protected static final String USERNAME = "TestUser_1";
    protected static final String PASSWORD = "Pass@123";
    protected static final String COUNTRY = "Sri Lanka";
    protected static final String USER_EMAIL = "abcrqo@wso2.com";
    protected static final String ADDRESS_OIDC_CLAIM = "address";
    protected static final String EMAIL_OIDC_CLAIM = "email";
    private static final String USERS_PATH = "users";

    protected CookieStore cookieStore = new BasicCookieStore();

    protected Lookup<CookieSpecProvider> cookieSpecRegistry;
    protected RequestConfig requestConfig;
    protected HttpClient client;
    protected String accessToken;
    protected String refreshToken;
    protected String applicationId;
    private SCIM2RestClient scim2RestClient;
    private String userId;

    protected void initConsentedTokenTest() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore).build();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    protected void createUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME);
        userInfo.setPassword(PASSWORD);
        userInfo.addEmail(new Email().value(USER_EMAIL));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country(COUNTRY));

        userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    protected void removeUser() throws Exception {

        scim2RestClient.deleteUser(userId);
    }

    protected void registerApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);
        application.setClaimConfiguration(setApplicationClaimConfig());

        applicationId = addApplication(application);
        Assert.assertNotNull(applicationId, "Application creation failed.");

        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();
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

    protected OIDCTokens makeTokenRequest(AuthorizationGrant authorizationGrant) throws URISyntaxException,
            IOException, ParseException {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        // email scope is to retrieve the email address of the user.
        // country scope is to retrieve the phone number of the user.
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID, OAuth2Constant.OAUTH2_SCOPE_EMAIL,
                        OAuth2Constant.OAUTH2_SCOPE_ADDRESS));

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

    protected void resetVariables() throws IOException {

        accessToken = null;
        refreshToken = null;
        consumerKey = null;
        consumerSecret = null;
        scim2RestClient.closeHttpClient();
        restClient.closeHttpClient();
    }
}
