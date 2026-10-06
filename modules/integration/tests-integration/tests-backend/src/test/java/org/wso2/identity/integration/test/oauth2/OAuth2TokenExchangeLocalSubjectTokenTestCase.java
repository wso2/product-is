/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test cases for the token exchange grant with a locally issued subject token.
 */
public class OAuth2TokenExchangeLocalSubjectTokenTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APPLICATION_NAME = "LocalSubjectTokenServiceProvider";
    private static final String END_USER_USERNAME = "LocalSubjectTokenEndUser";
    private static final String END_USER_PASSWORD = "LocalSubjectTokenEndUser@123";
    private static final String END_USER_EMAIL = "LocalSubjectTokenEndUser@wso2.com";
    private static final String LOCAL_SUBJECT_TOKEN_ROLE_NAME = "LocalSubjectTokenRole";
    private static final String AUDIENCE_TYPE = "APPLICATION";
    private static final String USERS = "users";
    private static final String LOCAL_SUBJECT_TOKEN_API_NAME = "LocalSubjectTokenTestService";
    private static final String LOCAL_SUBJECT_TOKEN_API_IDENTIFIER = "http://localhost:8590/local-subject-token";
    private static final String SCOPE_BOOKING_READ = "local_subject_token_booking_read";
    private static final String SCOPE_BOOKING_WRITE = "local_subject_token_booking_write";
    private static final String SCOPE_BOOKING_EXPORT = "local_subject_token_booking_export";
    private static final String SUBJECT_TOKEN_KEY = "subject_token";
    private static final String SUBJECT_TOKEN_TYPE_KEY = "subject_token_type";
    private static final String REQUESTED_TOKEN_TYPE_KEY = "requested_token_type";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String SCOPE_KEY = "scope";
    private static final String ACCESS_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:access_token";
    private static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:token-exchange";

    private static final List<String> SUBJECT_TOKEN_SCOPES = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE);

    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;
    private List<String> localSubjectTokenScopes;
    private String applicationId;
    private String domainAPIId;
    private String roleId;
    private String endUserId;
    private String subjectToken;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        localSubjectTokenScopes = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE, SCOPE_BOOKING_EXPORT);

        ApplicationResponseModel application = createLocalSubjectTokenApplication();
        applicationId = application.getId();
        domainAPIId = createDomainAPI(LOCAL_SUBJECT_TOKEN_API_NAME, LOCAL_SUBJECT_TOKEN_API_IDENTIFIER,
                localSubjectTokenScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, localSubjectTokenScopes);
        createLocalSubjectTokenRole(applicationId);
        endUserId = addUserWithLocalSubjectTokenRole(END_USER_USERNAME, END_USER_PASSWORD, END_USER_EMAIL);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2RestClient.deleteUser(endUserId);
        deleteRole(roleId);
        deleteApp(applicationId);
        deleteDomainAPI(domainAPIId);

        scim2RestClient.closeHttpClient();
        restClient.closeHttpClient();
        client.close();
    }

    @Test(groups = "wso2.is", description = "Get the subject token of the end user with the password grant.")
    public void testGetSubjectTokenWithPasswordGrant() throws Exception {

        subjectToken = getPasswordGrantToken(END_USER_USERNAME, END_USER_PASSWORD, SUBJECT_TOKEN_SCOPES);

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(subjectToken).getJWTClaimsSet();
        assertTrue(getScopes(jwtClaimsSet).containsAll(SUBJECT_TOKEN_SCOPES),
                "Approved scopes are not found in the subject token.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for a scope which is authorized for the " +
            "application but not approved in the subject token.",
            dependsOnMethods = "testGetSubjectTokenWithPasswordGrant")
    public void testTokenExchangeLimitsScopesToSubjectTokenScopes() throws Exception {

        List<NameValuePair> urlParameters = getTokenExchangeRequestParameters(
                Arrays.asList(SCOPE_BOOKING_WRITE, SCOPE_BOOKING_EXPORT));

        String exchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(exchangedToken).getJWTClaimsSet();

        List<String> scopes = getScopes(jwtClaimsSet);
        assertTrue(scopes.contains(SCOPE_BOOKING_WRITE),
                "Scope approved in the subject token is not found in the exchanged access token.");
        assertFalse(scopes.contains(SCOPE_BOOKING_EXPORT),
                "Scope which is not approved in the subject token is found in the exchanged access token.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for an OIDC scope which is not approved " +
            "in the subject token.", dependsOnMethods = "testGetSubjectTokenWithPasswordGrant")
    public void testTokenExchangeLimitsOIDCScopesToSubjectTokenScopes() throws Exception {

        List<NameValuePair> urlParameters = getTokenExchangeRequestParameters(
                Arrays.asList(SCOPE_BOOKING_READ, OAuth2Constant.OAUTH2_SCOPE_OPENID));

        String exchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(exchangedToken).getJWTClaimsSet();

        List<String> scopes = getScopes(jwtClaimsSet);
        assertTrue(scopes.contains(SCOPE_BOOKING_READ),
                "Scope approved in the subject token is not found in the exchanged access token.");
        assertFalse(scopes.contains(OAuth2Constant.OAUTH2_SCOPE_OPENID),
                "OIDC scope which is not approved in the subject token is found in the exchanged access token.");
    }

    /**
     * Build the request parameters of a token exchange request with a locally issued subject token.
     *
     * @param scopes Requested scopes.
     * @return Token exchange request parameters.
     */
    private List<NameValuePair> getTokenExchangeRequestParameters(List<String> scopes) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_KEY, subjectToken));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(REQUESTED_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(SCOPE_KEY, String.join(" ", scopes)));
        return urlParameters;
    }

    private String getPasswordGrantToken(String username, String password, List<String> scopes) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        urlParameters.add(new BasicNameValuePair("username", username));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair(SCOPE_KEY, String.join(" ", scopes)));

        JSONObject jsonResponse = parseResponse(sendTokenRequest(urlParameters));
        String accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        assertNotNull(accessToken, "Access token is null in the password grant response of the user: " + username);
        return accessToken;
    }

    private String exchangeToken(List<NameValuePair> urlParameters) throws Exception {

        JSONObject jsonResponse = parseResponse(sendTokenRequest(urlParameters));
        String accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        assertNotNull(accessToken, "Access token is null in the token exchange response.");
        return accessToken;
    }

    private HttpResponse sendTokenRequest(List<NameValuePair> urlParameters) throws Exception {

        HttpPost httpPost = new HttpPost(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(httpPost);
    }

    private JSONObject parseResponse(HttpResponse response) throws Exception {

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONObject json = (JSONObject) new JSONParser().parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the token response.");
        }
        return json;
    }

    private List<String> getScopes(JWTClaimsSet jwtClaimsSet) throws ParseException {

        String scope = jwtClaimsSet.getStringClaim(SCOPE_KEY);
        if (scope == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(scope.split("\\s+"));
    }

    private ApplicationResponseModel createLocalSubjectTokenApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER, GRANT_TYPE_VALUE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(APPLICATION_NAME);

        return getApplication(addApplication(application));
    }

    private void createLocalSubjectTokenRole(String appId) throws Exception {

        List<Permission> permissions = new ArrayList<>();
        localSubjectTokenScopes.forEach(scope -> permissions.add(new Permission(scope)));
        Audience roleAudience = new Audience(AUDIENCE_TYPE, appId);
        RoleV2 role = new RoleV2(roleAudience, LOCAL_SUBJECT_TOKEN_ROLE_NAME, permissions, Collections.emptyList());
        roleId = addRole(role);
    }

    private String addUserWithLocalSubjectTokenRole(String username, String password, String email) throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(username);
        userInfo.setPassword(password);
        userInfo.setName(new Name().givenName(username));
        userInfo.addEmail(new Email().value(email));

        String userId = scim2RestClient.createUser(userInfo);

        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(userId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);

        return userId;
    }
}
