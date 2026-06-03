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
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyUserCriteria;
import org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserSharingRestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementBaseTest.FIDP_QUERY_PARAM;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementBaseTest.ORGANIZATION_SSO;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.QUERY_PARAM_ORG_ID;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_ORGS_ONLY;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Integration test for OAuth2 impersonation where the impersonator is a shared user
 * (originated from the root organization and shared into a sub-organization) and the
 * impersonatee is a sub-organization local user.
 */
public class Oauth2SharedUserImpersonationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String APP_NAME = "SharedUserImpersonationApp";
    private static final String IMPERSONATOR_USERNAME = "SharedImpersonator";
    private static final String IMPERSONATOR_PASSWORD = "SharedImpersonator@123";
    private static final String IMPERSONATOR_EMAIL = "sharedimpersonator@wso2.com";
    private static final String ORG_END_USER_USERNAME = "SharedFlowOrgEndUser";
    private static final String ORG_END_USER_PASSWORD = "OrgEndUser@123";
    private static final String ORG_END_USER_EMAIL = "sharedflow.orgenduser@wso2.com";
    private static final String SUB_ORG_NAME = "shared-impersonation-sub-org";
    private static final String IMPERSONATOR_ROLE_NAME = "SharedFlowImpersonatorRole";
    private static final String END_USER_ROLE_NAME = "SharedFlowEndUserRole";
    private static final String COUNTRY_CLAIM_VALUE = "USA";

    private static final String ORG_IMPERSONATION_RESOURCE_IDENTIFIER = "org:impersonation";
    private static final String ORG_SCIM2_USER_RESOURCE_IDENTIFIER = "/o/scim2/Users";
    private static final String IMPERSONATION_RESOURCE_IDENTIFIER = "system:impersonation";
    private static final String SCIM2_USER_RESOURCE_IDENTIFIER = "/scim2/Users";

    private static final String ORG_PERMISSION_VIEW = "internal_org_user_mgt_view";
    private static final String ORG_PERMISSION_LIST = "internal_org_user_mgt_list";
    private static final String ORG_INTERNAL_USER_IMPERSONATE = "internal_org_user_impersonate";
    private static final String INTERNAL_USER_IMPERSONATE = "internal_user_impersonate";
    private static final String AUDIENCE_TYPE = "APPLICATION";
    private static final String USERS_PATH = "users";

    private static final String SUBJECT_TOKEN_KEY = "subject_token";
    private static final String SUBJECT_TOKEN_TYPE_KEY = "subject_token_type";
    private static final String REQUESTED_TOKEN_TYPE_KEY = "requested_token_type";
    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String ACTOR_TOKEN_KEY = "actor_token";
    private static final String ACTOR_TOKEN_TYPE_KEY = "actor_token_type";
    private static final String SUBJECT_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:jwt";
    private static final String REQUESTED_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:access_token";
    private static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String ACTOR_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:id_token";

    private static final String ORG_SCIM2_USERS_ENDPOINT = "https://localhost:9853/o/scim2/Users";

    private SCIM2RestClient scim2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private UserSharingRestClient userSharingRestClient;
    private CloseableHttpClient client;
    private CloseableHttpClient httpClientWithoutAutoRedirections;
    private final CookieStore cookieStore = new BasicCookieStore();

    private String applicationId;
    private String sharedAppId;
    private String subOrgID;
    private String subOrgToken;
    private String rootImpersonatorId;
    private String sharedImpersonatorId;
    private String orgEndUserId;
    private String impersonationRoleID;
    private String endUserRoleID;

    private String subjectToken;
    private String idToken;
    private String accessToken;

    @BeforeClass
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
        httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userSharingRestClient = new UserSharingRestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new org.json.JSONObject(RESTTestBase.readResource("shared-user-impersonation-apis.json",
                        this.getClass())));

        createRootImpersonator();
        ApplicationResponseModel application = createImpersonationApplication();
        applicationId = application.getId();
        createImpersonatorRole(applicationId);
        createEndUserRole(applicationId);

        createOrganization();
        shareApplicationToSubOrg();
        updateSharedAppAuthenticationSequence();
        createOrgEndUser();
        shareImpersonatorToSubOrg();
        assignRolesInSubOrg();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        cookieStore.clear();

        if (orgEndUserId != null) {
            scim2RestClient.deleteSubOrgUser(orgEndUserId, subOrgToken);
        }
        if (rootImpersonatorId != null) {
            scim2RestClient.deleteUser(rootImpersonatorId);
        }
        if (impersonationRoleID != null) {
            restClient.deleteV2Role(impersonationRoleID);
        }
        if (endUserRoleID != null) {
            restClient.deleteV2Role(endUserRoleID);
        }
        if (applicationId != null) {
            restClient.deleteApplication(applicationId);
        }
        if (subOrgID != null) {
            orgMgtRestClient.deleteOrganization(subOrgID);
        }

        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
        userSharingRestClient.closeHttpClient();
        httpClientWithoutAutoRedirections.close();
        restClient.closeHttpClient();
        client.close();
    }

    @Test(groups = "wso2.is", description = "Shared user - Send authorize user request to initiate impersonation " +
            "of a sub-organization local user.")
    public void testInitSharedUserImpersonationAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, URLEncoder.encode(
                "id_token subject_token", StandardCharsets.UTF_8)));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, URLEncoder.encode(
                "internal_org_user_mgt_delete internal_login openid internal_org_user_impersonate " +
                        "internal_org_user_mgt_delete internal_org_user_mgt_view internal_org_user_mgt_list",
                StandardCharsets.UTF_8)));
        urlParameters.add(new BasicNameValuePair("requested_subject", orgEndUserId));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(QUERY_PARAM_ORG_ID, subOrgID));
        urlParameters.add(new BasicNameValuePair(FIDP_QUERY_PARAM, ORGANIZATION_SSO));

        String sessionDataKey = sendAuthorizationRequest(buildGetRequestURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, "carbon.super", urlParameters));
        Assert.assertNotNull(sessionDataKey, "Sub-org login page session data key should not be null.");
        sendSharedUserLoginPost(sessionDataKey);

        Assert.assertNotNull(subjectToken, "Subject token is null or could not be found.");
        Assert.assertNotNull(idToken, "Id token is null or could not be found.");

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(subjectToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), orgEndUserId,
                "Subject Id is not end user Id in the impersonation flow.");
        Map<String, String> mayActClaimSet = (Map<String, String>) jwtClaimsSet.getClaim("may_act");
        Assert.assertNotNull(mayActClaimSet, "may_act claim of subject token is empty");
        assertEquals(mayActClaimSet.get("sub"), rootImpersonatorId,
                "Impersonator Id is not in the may act claim.");
    }

    @Test(groups = "wso2.is", description = "Shared user - Exchange the subject token for an impersonated access " +
            "token.", dependsOnMethods = "testInitSharedUserImpersonationAuthorizeRequestPost")
    public void testOrgSendTokenExchangeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_KEY, subjectToken));
        urlParameters.add(new BasicNameValuePair(SUBJECT_TOKEN_TYPE_KEY, SUBJECT_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(REQUESTED_TOKEN_TYPE_KEY, REQUESTED_TOKEN_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
        urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_KEY, idToken));
        urlParameters.add(new BasicNameValuePair(ACTOR_TOKEN_TYPE_KEY, ACTOR_TOKEN_TYPE_VALUE));

        String url = OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        JSONObject jsonResponse = responseObject(url, urlParameters, consumerKey, consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), orgEndUserId,
                "Subject Id is not end user Id in the impersonation flow.");
        Map<String, String> actClaimSet = (Map<String, String>) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        assertEquals(actClaimSet.get("sub"), rootImpersonatorId, "Impersonator Id is not in the act claim.");
    }

    @Test(groups = "wso2.is", description = "Shared user - Use the impersonated access token against the sub-org " +
            "user listing API.", dependsOnMethods = "testOrgSendTokenExchangeRequestPost")
    public void testOrgImpersonatedAccessToken() throws Exception {

        HttpGet request = new HttpGet(ORG_SCIM2_USERS_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200,
                "Response for User listing is failed");
    }

    private String sendAuthorizationRequest(String endPointUrl) throws Exception {

        HttpResponse authorizeResponse = sendGetRequest(endPointUrl, httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authorizeResponse, "Authorize response is null.");
        Assert.assertEquals(authorizeResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Authorize response status code is invalid.");
        Header authorizeLocationHeader = authorizeResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authorizeLocationHeader, "Authorize response header location is null.");
        EntityUtils.consume(authorizeResponse.getEntity());

        HttpResponse authorizeRedirectResponse =
                sendGetRequest(authorizeLocationHeader.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authorizeRedirectResponse, "Redirected authorize response is null.");
        Assert.assertEquals(authorizeRedirectResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Redirected authorize response status code is invalid.");
        Header authorizeRedirectLocationHeader =
                authorizeRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authorizeRedirectLocationHeader,
                "Redirected authorize response header location is null.");
        Assert.assertTrue(authorizeRedirectLocationHeader.getValue().contains(ORGANIZATION_PATH + subOrgID),
                "Not redirected to child organization login page.");
        EntityUtils.consume(authorizeRedirectResponse.getEntity());

        HttpResponse childOrgLoginPageResponse =
                sendGetRequest(authorizeRedirectLocationHeader.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertNotNull(childOrgLoginPageResponse, "Child organization login page is empty.");
        Assert.assertEquals(childOrgLoginPageResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Child organization login redirection status code is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(childOrgLoginPageResponse, keyPositionMap);
        Assert.assertNotNull(keyValues, "Retrieved key value pairs are empty.");
        String sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(childOrgLoginPageResponse.getEntity());

        return sessionDataKey;
    }

    private void sendSharedUserLoginPost(String sessionDataKey) throws Exception {

        String commonAuthURL = serverURL + ORGANIZATION_PATH + subOrgID + "/commonauth";

        // Step 1: Submit the username to the SharedUserIdentifierExecutor step.
        List<NameValuePair> identifierParams = new ArrayList<>();
        identifierParams.add(new BasicNameValuePair("username", IMPERSONATOR_USERNAME));
        identifierParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        HttpResponse identifierResponse =
                sendPostRequest(commonAuthURL, identifierParams, httpClientWithoutAutoRedirections);
        Assert.assertNotNull(identifierResponse, "Identifier step response is null.");
        EntityUtils.consume(identifierResponse.getEntity());

        // Step 2: Submit the password to the BasicAuthenticator step.
        List<NameValuePair> passwordParams = new ArrayList<>();
        passwordParams.add(new BasicNameValuePair("username", IMPERSONATOR_USERNAME));
        passwordParams.add(new BasicNameValuePair("password", IMPERSONATOR_PASSWORD));
        passwordParams.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        HttpResponse loginPostResponse =
                sendPostRequest(commonAuthURL, passwordParams, httpClientWithoutAutoRedirections);

        Assert.assertNotNull(loginPostResponse, "Login request failed. Login response is null.");
        Assert.assertEquals(loginPostResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Login status code is invalid.");
        Header childOrgAuthRedirectionLocation =
                loginPostResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(childOrgAuthRedirectionLocation, "Login response location header is null.");
        EntityUtils.consume(loginPostResponse.getEntity());

        HttpResponse childOrgAuthRedirectResponse =
                sendGetRequest(childOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertEquals(childOrgAuthRedirectResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Child organization auth redirection status code is invalid.");
        Header rootOrgCommonAuthRedirectionLocation =
                childOrgAuthRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgCommonAuthRedirectionLocation,
                "Child organization authorize redirection response location header is null.");
        EntityUtils.consume(childOrgAuthRedirectResponse.getEntity());

        HttpResponse rootOrgCommonAuthRedirectionResponse =
                sendGetRequest(rootOrgCommonAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Assert.assertEquals(rootOrgCommonAuthRedirectionResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Root organization common auth redirection status code is invalid.");
        Header rootOrgAuthRedirectionLocation =
                rootOrgCommonAuthRedirectionResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgAuthRedirectionLocation,
                "Root organization common auth response location header is null.");
        EntityUtils.consume(rootOrgCommonAuthRedirectionResponse.getEntity());

        HttpResponse consentRequestResponse =
                sendGetRequest(rootOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);
        Header consentRequestRedirectionLocation =
                consentRequestResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(consentRequestRedirectionLocation,
                "Authorization code response location header is null.");
        Assert.assertTrue(consentRequestRedirectionLocation.toString()
                        .contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(
                consentRequestRedirectionLocation.getValue(), OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(consentRequestResponse.getEntity());

        HttpResponse authCodeResponse = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(authCodeResponse, "Approval request failed. response is invalid.");
        Header authCodeRedirectionLocation = authCodeResponse.getFirstHeader(
                OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authCodeRedirectionLocation, "Auth code redirection location header is null.");

        subjectToken = DataExtractUtil.extractParamFromURIFragment(
                authCodeRedirectionLocation.getValue(), OAuth2Constant.SUBJECT_TOKEN);
        idToken = DataExtractUtil.extractParamFromURIFragment(
                authCodeRedirectionLocation.getValue(), OAuth2Constant.ID_TOKEN);

        EntityUtils.consume(authCodeResponse.getEntity());
    }

    private ApplicationResponseModel createImpersonationApplication() throws Exception {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:token-exchange");

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(OAuth2Constant.CALLBACK_URL);
        oidcConfig.setSubjectToken(new SubjectTokenConfiguration().enable(true)
                .applicationSubjectTokenExpiryInSeconds(18000));

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel();
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(APP_NAME);
        application.setIsManagementApp(true);
        application.setEnhancedOrgAuthenticationEnabled(false);

        String appId = addApplication(application);

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(appId,
                    new ArrayList<>(Arrays.asList(IMPERSONATION_RESOURCE_IDENTIFIER, SCIM2_USER_RESOURCE_IDENTIFIER,
                            ORG_IMPERSONATION_RESOURCE_IDENTIFIER, ORG_SCIM2_USER_RESOURCE_IDENTIFIER)));
        }
        return getApplication(appId);
    }

    private void createOrganization() throws Exception {

        subOrgID = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        subOrgToken = orgMgtRestClient.switchM2MToken(subOrgID);
    }

    private void shareApplicationToSubOrg() throws Exception {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        restClient.shareApplication(applicationId, applicationSharePOSTRequest);

        // Application sharing is asynchronous; wait until the shared application appears in the sub-organization.
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            try {
                String id = restClient.getAppIdUsingAppNameInOrganization(APP_NAME, subOrgToken);
                if (id != null && !id.isEmpty()) {
                    sharedAppId = id;
                    break;
                }
            } catch (IOException e) {
                log.debug("Transient error while polling for shared application '" + APP_NAME +
                        "' in sub-organization, retrying.", e);
            }
            Thread.sleep(500);
        }
        Assert.assertNotNull(sharedAppId, "Shared application ID in sub-organization should not be null.");
        await().atMost(5, TimeUnit.SECONDS).until(() -> true);
    }

    private void updateSharedAppAuthenticationSequence() {

        AuthenticationSequence authSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("SharedUserIdentifierExecutor")))
                .addStepsItem(new AuthenticationStep()
                        .id(2)
                        .addOptionsItem(new Authenticator()
                                .idp("LOCAL")
                                .authenticator("BasicAuthenticator")));

        ApplicationPatchModel patchModel = new ApplicationPatchModel();
        patchModel.setAuthenticationSequence(authSequence);

        restClient.updateSubOrgApplication(sharedAppId, patchModel, subOrgToken);
    }

    private void createRootImpersonator() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(IMPERSONATOR_USERNAME);
        userInfo.setPassword(IMPERSONATOR_PASSWORD);
        userInfo.setName(new Name().givenName(IMPERSONATOR_USERNAME));
        userInfo.addEmail(new Email().value(IMPERSONATOR_EMAIL));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country(COUNTRY_CLAIM_VALUE));

        rootImpersonatorId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("everyone");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(rootImpersonatorId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    private void createOrgEndUser() throws Exception {

        UserObject endUser = new UserObject();
        endUser.setUserName(ORG_END_USER_USERNAME);
        endUser.setPassword(ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(ORG_END_USER_EMAIL));
        orgEndUserId = scim2RestClient.createSubOrgUser(endUser, subOrgToken);
    }

    private void shareImpersonatorToSubOrg() throws Exception {

        UserShareWithAllRequestBody shareRequest = new UserShareWithAllRequestBody();
        shareRequest.setUserCriteria(new UserShareRequestBodyUserCriteria().addUserIdsItem(rootImpersonatorId));
        shareRequest.setPolicy(ALL_EXISTING_ORGS_ONLY);
        userSharingRestClient.shareUsersWithAll(shareRequest);

        String userSearchReq = new org.json.JSONObject()
                .put("schemas", new org.json.JSONArray().put("urn:ietf:params:scim:api:messages:2.0:SearchRequest"))
                .put("attributes", new org.json.JSONArray().put("id"))
                .put("filter", "userName eq " + IMPERSONATOR_USERNAME)
                .toString();

        Assert.assertTrue(scim2RestClient.isSharedUserCreationCompleted(userSearchReq, subOrgToken),
                "Impersonator user should be shared to the sub-organization.");

        JSONObject result = scim2RestClient.searchSubOrgUser(userSearchReq, subOrgToken);
        JSONArray resources = (JSONArray) result.get("Resources");
        Assert.assertTrue(resources != null && !resources.isEmpty(),
                "Shared impersonator not found in sub-organization.");
        sharedImpersonatorId = (String) ((JSONObject) resources.get(0)).get("id");
    }

    private void assignRolesInSubOrg() throws Exception {

        // Assign impersonator role to the shared user in the sub-organization.
        String impersonatorSharedAppRoleId =
                scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(IMPERSONATOR_ROLE_NAME, sharedAppId, subOrgToken);
        RoleItemAddGroupobj impersonatorRolePatchObj = new RoleItemAddGroupobj();
        impersonatorRolePatchObj.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        impersonatorRolePatchObj.setPath(USERS_PATH);
        impersonatorRolePatchObj.addValue(new ListObject().value(sharedImpersonatorId));
        scim2RestClient.updateUsersOfRoleV2InSubOrg(
                impersonatorSharedAppRoleId,
                new PatchOperationRequestObject().addOperations(impersonatorRolePatchObj),
                subOrgToken);

        // Assign end user role to sub-org end user.
        String endUserSharedAppRoleId =
                scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(END_USER_ROLE_NAME, sharedAppId, subOrgToken);
        RoleItemAddGroupobj endUserRolePatchObj = new RoleItemAddGroupobj();
        endUserRolePatchObj.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        endUserRolePatchObj.setPath(USERS_PATH);
        endUserRolePatchObj.addValue(new ListObject().value(orgEndUserId));
        scim2RestClient.updateUsersOfRoleV2InSubOrg(
                endUserSharedAppRoleId,
                new PatchOperationRequestObject().addOperations(endUserRolePatchObj),
                subOrgToken);
    }

    private void createImpersonatorRole(String appID) throws Exception {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(INTERNAL_USER_IMPERSONATE));
        permissions.add(new Permission(ORG_INTERNAL_USER_IMPERSONATE));
        Audience roleAudience = new Audience(AUDIENCE_TYPE, appID);
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleAudience, IMPERSONATOR_ROLE_NAME, permissions, schemas);
        impersonationRoleID = addRole(role);
    }

    private void createEndUserRole(String appID) throws Exception {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(ORG_PERMISSION_VIEW));
        permissions.add(new Permission(ORG_PERMISSION_LIST));
        Audience roleAudience = new Audience(AUDIENCE_TYPE, appID);
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleAudience, END_USER_ROLE_NAME, permissions, schemas);
        endUserRoleID = addRole(role);
    }

    private JSONObject responseObject(String endpoint, List<NameValuePair> postParameters, String key, String secret)
            throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(key, secret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        return json;
    }
}
