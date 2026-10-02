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
import org.apache.http.HttpStatus;
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
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.IdTokenConfiguration;
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
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test cases for the OAuth2 token exchange delegation flow.
 */
public class Oauth2DelegationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APPLICATION_NAME = "DelegationServiceProvider";
    private static final String END_USER_USERNAME = "DelegationEndUser";
    private static final String END_USER_PASSWORD = "DelegationEndUser@123";
    private static final String END_USER_EMAIL = "DelegationEndUser@wso2.com";
    private static final String DELEGATE_USERNAME = "Delegate";
    private static final String DELEGATE_PASSWORD = "Delegate@123";
    private static final String DELEGATE_EMAIL = "Delegate@wso2.com";
    private static final String SECOND_DELEGATE_USERNAME = "SecondDelegate";
    private static final String SECOND_DELEGATE_PASSWORD = "SecondDelegate@123";
    private static final String SECOND_DELEGATE_EMAIL = "SecondDelegate@wso2.com";
    private static final String DELEGATION_ROLE_NAME = "DelegationRole";
    private static final String AUDIENCE_TYPE = "APPLICATION";
    private static final String USERS = "users";
    private static final String DELEGATION_API_NAME = "DelegationTestService";
    private static final String DELEGATION_API_IDENTIFIER = "http://localhost:8590/delegation";
    private static final String SCOPE_BOOKING_READ = "delegation_booking_read";
    private static final String SCOPE_BOOKING_WRITE = "delegation_booking_write";
    private static final String SCOPE_BOOKING_DELETE = "delegation_booking_delete";
    private static final String SCOPE_BOOKING_UPDATE = "delegation_booking_update";
    private static final String SCOPE_BOOKING_EXPORT = "delegation_booking_export";
    private static final String DELEGATION_AUDIENCE = "delegation-audience";
    private static final String UNREGISTERED_AUDIENCE = "unregistered-audience";
    public static final String SUBJECT_TOKEN_KEY = "subject_token";
    public static final String SUBJECT_TOKEN_TYPE_KEY = "subject_token_type";
    public static final String REQUESTED_TOKEN_TYPE_KEY = "requested_token_type";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String ACTOR_TOKEN_KEY = "actor_token";
    public static final String ACTOR_TOKEN_TYPE_KEY = "actor_token_type";
    public static final String SCOPE_KEY = "scope";
    public static final String AUDIENCE_KEY = "audience";
    public static final String ACCESS_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:access_token";
    public static final String REFRESH_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:refresh_token";
    public static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String ACT = "act";
    private static final String SUB = "sub";
    private static final String ERROR = "error";
    private static final String INVALID_TARGET = "invalid_target";
    private static final String INVALID_REQUEST = "invalid_request";

    private static final List<String> SUBJECT_TOKEN_SCOPES = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE,
            SCOPE_BOOKING_DELETE, SCOPE_BOOKING_UPDATE);
    private static final List<String> DELEGATED_TOKEN_SCOPES = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE,
            SCOPE_BOOKING_DELETE);
    private static final List<String> CHAINED_TOKEN_SCOPES = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE);

    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;
    private List<String> delegationScopes;
    private String applicationId;
    private String domainAPIId;
    private String roleId;
    private String endUserId;
    private String delegateId;
    private String secondDelegateId;
    private String subjectToken;
    private String actorToken;
    private String secondActorToken;
    private String delegatedToken;
    private String chainedDelegatedToken;

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

        delegationScopes = Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_WRITE, SCOPE_BOOKING_DELETE,
                SCOPE_BOOKING_UPDATE, SCOPE_BOOKING_EXPORT);

        ApplicationResponseModel application = createDelegationApplication();
        applicationId = application.getId();
        domainAPIId = createDomainAPI(DELEGATION_API_NAME, DELEGATION_API_IDENTIFIER, delegationScopes);
        authorizeDomainAPIs(applicationId, domainAPIId, delegationScopes);
        createDelegationRole(applicationId);
        endUserId = addUserWithDelegationRole(END_USER_USERNAME, END_USER_PASSWORD, END_USER_EMAIL);
        delegateId = addUserWithDelegationRole(DELEGATE_USERNAME, DELEGATE_PASSWORD, DELEGATE_EMAIL);
        secondDelegateId = addUserWithDelegationRole(SECOND_DELEGATE_USERNAME, SECOND_DELEGATE_PASSWORD,
                SECOND_DELEGATE_EMAIL);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2RestClient.deleteUser(endUserId);
        scim2RestClient.deleteUser(delegateId);
        scim2RestClient.deleteUser(secondDelegateId);
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
        assertEquals(jwtClaimsSet.getSubject(), endUserId, "Subject Id is not the end user Id in the subject token.");
        assertNull(jwtClaimsSet.getClaim(ACT), "Act claim is present in the subject token of the end user.");
        assertTrue(getScopes(jwtClaimsSet).containsAll(SUBJECT_TOKEN_SCOPES),
                "Approved scopes are not found in the subject token.");
    }

    @Test(groups = "wso2.is", description = "Get the actor tokens of the delegates with the password grant.",
            dependsOnMethods = "testGetSubjectTokenWithPasswordGrant")
    public void testGetActorTokensWithPasswordGrant() throws Exception {

        actorToken = getPasswordGrantToken(DELEGATE_USERNAME, DELEGATE_PASSWORD,
                Collections.singletonList(SCOPE_BOOKING_READ));
        secondActorToken = getPasswordGrantToken(SECOND_DELEGATE_USERNAME, SECOND_DELEGATE_PASSWORD,
                Collections.singletonList(SCOPE_BOOKING_READ));

        assertEquals(SignedJWT.parse(actorToken).getJWTClaimsSet().getSubject(), delegateId,
                "Subject Id is not the delegate Id in the actor token.");
        assertEquals(SignedJWT.parse(secondActorToken).getJWTClaimsSet().getSubject(), secondDelegateId,
                "Subject Id is not the second delegate Id in the actor token.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request with an actor token for delegation.",
            dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationWithActorToken() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(subjectToken, actorToken,
                DELEGATED_TOKEN_SCOPES);

        delegatedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(delegatedToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not the end user Id in the delegation flow.");

        Map<String, Object> actClaimSet = getActClaim(jwtClaimsSet);
        assertNotNull(actClaimSet, "Act claim of the delegated access token is empty.");
        assertEquals(actClaimSet.get(SUB), delegateId, "Delegate Id is not in the act claim.");
        assertNull(actClaimSet.get(ACT), "Act claim is nested for the first level of the delegation chain.");
        assertTrue(getScopes(jwtClaimsSet).containsAll(DELEGATED_TOKEN_SCOPES),
                "Requested scopes are not found in the delegated access token.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request with a new actor token to chain the " +
            "delegation.", dependsOnMethods = "testDelegationWithActorToken")
    public void testChainedDelegationWithNewActorToken() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(delegatedToken, secondActorToken,
                CHAINED_TOKEN_SCOPES);

        chainedDelegatedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(chainedDelegatedToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not the end user Id in the chained delegation flow.");

        Map<String, Object> actClaimSet = getActClaim(jwtClaimsSet);
        assertNotNull(actClaimSet, "Act claim of the chained delegated access token is empty.");
        assertEquals(actClaimSet.get(SUB), secondDelegateId,
                "The new actor is not the current actor of the delegation chain.");

        Map<String, Object> nestedActClaimSet = (Map<String, Object>) actClaimSet.get(ACT);
        assertNotNull(nestedActClaimSet, "The existing act claim is not nested under the new actor.");
        assertEquals(nestedActClaimSet.get(SUB), delegateId,
                "The previous actor is not preserved in the nested act claim.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request without an actor token to carry the " +
            "existing delegation chain forward.", dependsOnMethods = "testChainedDelegationWithNewActorToken")
    public void testDelegationReExchangeWithoutActorToken() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(chainedDelegatedToken, null,
                Collections.singletonList(SCOPE_BOOKING_READ));

        String reExchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(reExchangedToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not the end user Id in the delegation re-exchange flow.");

        Map<String, Object> actClaimSet = getActClaim(jwtClaimsSet);
        assertNotNull(actClaimSet, "Act claim of the re-exchanged access token is empty.");
        assertEquals(actClaimSet.get(SUB), secondDelegateId,
                "The current actor of the delegation chain is changed by the re-exchange.");

        Map<String, Object> nestedActClaimSet = (Map<String, Object>) actClaimSet.get(ACT);
        assertNotNull(nestedActClaimSet, "The existing delegation chain is not carried forward.");
        assertEquals(nestedActClaimSet.get(SUB), delegateId,
                "The previous actor is not preserved in the nested act claim.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request with an actor token of the current actor " +
            "of the delegation chain.", dependsOnMethods = "testChainedDelegationWithNewActorToken")
    public void testDelegationWithActorTokenOfCurrentActor() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(chainedDelegatedToken, secondActorToken,
                Collections.singletonList(SCOPE_BOOKING_WRITE));

        String reExchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(reExchangedToken).getJWTClaimsSet();

        Map<String, Object> actClaimSet = getActClaim(jwtClaimsSet);
        assertNotNull(actClaimSet, "Act claim of the re-exchanged access token is empty.");
        assertEquals(actClaimSet.get(SUB), secondDelegateId,
                "The current actor of the delegation chain is changed by the re-exchange.");

        Map<String, Object> nestedActClaimSet = (Map<String, Object>) actClaimSet.get(ACT);
        assertNotNull(nestedActClaimSet, "The existing delegation chain is not carried forward.");
        assertEquals(nestedActClaimSet.get(SUB), delegateId,
                "The previous actor is not preserved in the nested act claim.");
        assertNull(nestedActClaimSet.get(ACT),
                "A duplicate delegation level is added for the current actor of the chain.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for delegation with a scope which is not " +
            "approved in the subject token.", dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationLimitsScopesToSubjectTokenScopes() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(subjectToken, actorToken,
                Arrays.asList(SCOPE_BOOKING_DELETE, SCOPE_BOOKING_EXPORT));

        String exchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(exchangedToken).getJWTClaimsSet();

        List<String> scopes = getScopes(jwtClaimsSet);
        assertTrue(scopes.contains(SCOPE_BOOKING_DELETE),
                "Scope approved in the subject token is not found in the delegated access token.");
        assertFalse(scopes.contains(SCOPE_BOOKING_EXPORT),
                "Scope which is not approved in the subject token is found in the delegated access token.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for delegation with a registered audience.",
            dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationWithRequestedAudience() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(subjectToken, actorToken,
                Collections.singletonList(SCOPE_BOOKING_UPDATE));
        urlParameters.add(new BasicNameValuePair(AUDIENCE_KEY, DELEGATION_AUDIENCE));

        String exchangedToken = exchangeToken(urlParameters);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(exchangedToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getAudience(), Collections.singletonList(DELEGATION_AUDIENCE),
                "Audience of the delegated access token is not limited to the requested audience.");
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for delegation with an audience which is " +
            "not registered for the application.", dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationWithUnregisteredAudience() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(subjectToken, actorToken,
                Arrays.asList(SCOPE_BOOKING_READ, SCOPE_BOOKING_UPDATE));
        urlParameters.add(new BasicNameValuePair(AUDIENCE_KEY, UNREGISTERED_AUDIENCE));

        assertErrorResponse(urlParameters, INVALID_TARGET);
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for delegation with multiple audience " +
            "values.", dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationWithMultipleAudienceValues() throws Exception {

        List<NameValuePair> urlParameters = getDelegationRequestParameters(subjectToken, actorToken,
                Arrays.asList(SCOPE_BOOKING_WRITE, SCOPE_BOOKING_DELETE));
        urlParameters.add(new BasicNameValuePair(AUDIENCE_KEY, DELEGATION_AUDIENCE + " " + consumerKey));

        assertErrorResponse(urlParameters, INVALID_TARGET);
    }

    @Test(groups = "wso2.is", description = "Send a token exchange request for delegation with an unsupported actor " +
            "token type.", dependsOnMethods = "testGetActorTokensWithPasswordGrant")
    public void testDelegationWithUnsupportedActorTokenType() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_KEY, subjectToken));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(REQUESTED_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_KEY, actorToken));
        urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_TYPE_KEY, REFRESH_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(SCOPE_KEY, SCOPE_BOOKING_READ));

        assertErrorResponse(urlParameters, INVALID_REQUEST);
    }

    /**
     * Build the request parameters of a delegation token exchange request.
     *
     * @param subjectToken Subject token of the request.
     * @param actorToken   Actor token of the request. No actor token is sent when this is null.
     * @param scopes       Requested scopes.
     * @return Token exchange request parameters.
     */
    private List<NameValuePair> getDelegationRequestParameters(String subjectToken, String actorToken,
                                                               List<String> scopes) {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_KEY, subjectToken));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(REQUESTED_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        if (actorToken != null) {
            urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_KEY, actorToken));
            urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE_VALUE));
        }
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

    private void assertErrorResponse(List<NameValuePair> urlParameters, String expectedErrorCode) throws Exception {

        HttpResponse response = sendTokenRequest(urlParameters);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Token exchange request is not rejected with a bad request response.");

        JSONObject jsonResponse = parseResponse(response);
        assertEquals(jsonResponse.get(ERROR), expectedErrorCode,
                "Token exchange request is not rejected with the expected error code.");
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

    private Map<String, Object> getActClaim(JWTClaimsSet jwtClaimsSet) {

        return (Map<String, Object>) jwtClaimsSet.getClaim(ACT);
    }

    private List<String> getScopes(JWTClaimsSet jwtClaimsSet) throws ParseException {

        String scope = jwtClaimsSet.getStringClaim(SCOPE_KEY);
        if (scope == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(scope.split("\\s+"));
    }

    private ApplicationResponseModel createDelegationApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER, GRANT_TYPE_VALUE);

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setIdToken(new IdTokenConfiguration().audience(Collections.singletonList(DELEGATION_AUDIENCE)));

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

    private void createDelegationRole(String appId) throws Exception {

        List<Permission> permissions = new ArrayList<>();
        delegationScopes.forEach(scope -> permissions.add(new Permission(scope)));
        Audience roleAudience = new Audience(AUDIENCE_TYPE, appId);
        RoleV2 role = new RoleV2(roleAudience, DELEGATION_ROLE_NAME, permissions, Collections.emptyList());
        roleId = addRole(role);
    }

    private String addUserWithDelegationRole(String username, String password, String email) throws Exception {

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
