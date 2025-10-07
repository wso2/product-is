/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
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
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementBaseTest.FIDP_QUERY_PARAM;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementBaseTest.ORGANIZATION_SSO;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.constant.UserSharingConstants.QUERY_PARAM_ORG_ID;
import static org.wso2.identity.integration.test.restclients.RestBaseClient.ORGANIZATION_PATH;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_CODE;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.REDIRECT_URI_NAME;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;

/**
 * Integration test cases for OAuth2 Impersonation Flow.
 */
public class Oauth2ImpersonationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String impersonationResourceIdentifier = "system:impersonation";
    private static final String orgImpersonationResourceIdentifier = "org:impersonation";
    private static final String scim2UserResourceIdentifier = "/scim2/Users";
    private static final String orgScim2UserResourceIdentifier = "/o/scim2/Users";
    private static final String IMPERSONATOR_USERNAME = "Impersonator";
    private static final String IMPERSONATOR_PASSWORD = "Impersonator@123";
    private static final String IMPERSONATOR_EMAIL = "Impersonator@wso2.com";
    private static final String ORG_IMPERSONATOR_USERNAME = "OrgImpersonator";
    private static final String ORG_IMPERSONATOR_PASSWORD = "OrgImpersonator@123";
    private static final String ORG_IMPERSONATOR_EMAIL = "OrgImpersonator@wso2.com";
    private static final String END_USER_USERNAME = "EndUser";
    private static final String END_USER_PASSWORD = "EndUser@123";
    private static final String END_USER_EMAIL = "EndUser@wso2.com";
    private static final String ORG_END_USER_USERNAME = "OrgEndUser";
    private static final String ORG_END_USER_PASSWORD = "OrgEndUser@123";
    private static final String ORG_END_USER_EMAIL = "OrgEndUser@wso2.com";
    private static final String PERMISSION_VIEW = "internal_user_mgt_view";
    private static final String PERMISSION_LIST = "internal_user_mgt_list";
    private static final String ORG_PERMISSION_VIEW = "internal_org_user_mgt_view";
    private static final String ORG_PERMISSION_LIST = "internal_org_user_mgt_list";
    private static final String AUDIENCE_TYPE = "APPLICATION";
    public static final String SUBJECT_TOKEN_KEY = "subject_token";
    public static final String SUBJECT_TOKEN_TYPE_KEY = "subject_token_type";
    public static final String REQUESTED_TOKEN_TYPE_KEY = "requested_token_type";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String ACTOR_TOKEN_KEY = "actor_token";
    public static final String ACTOR_TOKEN_TYPE_KEY = "actor_token_type";
    public static final String SUBJECT_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:jwt";
    public static final String REQUESTED_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:access_token";
    public static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:token-exchange";
    public static final String ACTOR_TOKEN_TYPE_VALUE = "urn:ietf:params:oauth:token-type:id_token";
    private static final String USERS = "users";
    private static final String INTERNAL_USER_IMPERSONATE = "internal_user_impersonate";
    private static final String ORG_INTERNAL_USER_IMPERSONATE = "internal_org_user_impersonate";
    private static final String END_USER_ROLE_NAME = "EndUserRole";
    private static final String IMPERSONATOR_ROLE_NAME = "ImpersonatorRole";
    public final static String SCIM2_USERS_ENDPOINT = "https://localhost:9853/scim2/Users";
    public final static String ORG_SCIM2_USERS_ENDPOINT = "https://localhost:9853/o/scim2/Users";
    public static final String CONTENT_TYPE = "application/json";
    private static final String USERS_PATH = "users";
    private static final String COUNTRY_CLAIM_VALUE = "USA";
    private static final String SUB_ORG_NAME = "sub-org";
    private String impersonationRoleID;
    private String endUserRoleID;
    private String applicationId;
    private String impersonatorId;
    private String endUserId;
    private String orgImpersonatorId;
    private String orgEndUserId;
    private SCIM2RestClient scim2RestClient;
    private CloseableHttpClient client;
    protected OrgMgtRestClient orgMgtRestClient;
    private CloseableHttpClient httpClientWithoutAutoRedirections;
    private final CookieStore cookieStore = new BasicCookieStore();
    private String subjectToken;
    private String idToken;
    private String code;
    private String accessToken;
    private String subOrgID;
    private String subOrgToken;

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
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(
                isServer, tenantInfo, serverURL,
                new org.json.JSONObject(readResource("impersonation-org-test-apis.json", this.getClass())));
        httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        addImpersonator();
        addEndUser();
        ApplicationResponseModel application = createImpersonationApplication();
        applicationId = application.getId();
        createImpersonatorRole(applicationId);
        createEndUserRole(applicationId);
        createOrganization();
        createOrgUsers();
        assignOrgRolesToOrgUsers();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        cookieStore.clear();

        scim2RestClient.deleteUser(impersonatorId);
        scim2RestClient.deleteUser(endUserId);
        scim2RestClient.deleteSubOrgUser(orgImpersonatorId, subOrgToken);
        scim2RestClient.deleteSubOrgUser(orgEndUserId, subOrgToken);
        restClient.deleteV2Role(impersonationRoleID);
        restClient.deleteV2Role(endUserRoleID);
        restClient.deleteApplication(applicationId);
        orgMgtRestClient.deleteOrganization(subOrgID);

        scim2RestClient.closeHttpClient();
        orgMgtRestClient.closeHttpClient();
        httpClientWithoutAutoRedirections.close();
        restClient.closeHttpClient();
        client.close();

    }

    @Test(groups = "wso2.is", description = "Send authorize user request with impersonation related response types " +
            "and response modes.")
    public void testInitImpersonationAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, "id_token subject_token"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                "internal_user_mgt_delete internal_login openid internal_user_impersonate " +
                        "internal_user_mgt_delete internal_user_mgt_view internal_user_mgt_list"));
        urlParameters.add(new BasicNameValuePair("requested_subject", endUserId));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());

        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);

        String sessionDataKey;
        if (sessionDataKeyConsent == null) {
            Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                    "sessionDataKey not found in response.");
            sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
            Assert.assertNotNull(sessionDataKey, "sessionDataKey is null.");

            sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        }

        response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        locationValue = getLocationHeaderValue(response);

        subjectToken = getFragmentParam(locationValue, OAuth2Constant.SUBJECT_TOKEN);
        idToken = getFragmentParam(locationValue, OAuth2Constant.ID_TOKEN);

        Assert.assertNotNull(subjectToken, "Subject token is null or could not be found.");
        Assert.assertNotNull(idToken, "Id token is null or could not be found.");
        EntityUtils.consume(response.getEntity());

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(subjectToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );
        Map<String, String>  mayActClaimSet = (Map) jwtClaimsSet.getClaim("may_act");
        Assert.assertNotNull(mayActClaimSet, "may_act claim of subject token is empty");
        Assert.assertEquals(mayActClaimSet.get("sub"), impersonatorId,
                "Impersonator Id is not in the may act claim." );
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with response types and response modes.",
            dependsOnMethods = "testInitImpersonationAuthorizeRequestPost")
    public void testSendTokenExchangeRequestPost() throws Exception {

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
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );
        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim." );
    }

    @Test(groups = "wso2.is", description = "Send authorize user request to SSO as the impersonatee for code.",
            dependsOnMethods = "testSendTokenExchangeRequestPost")
    public void testSSOImpersonationAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                "internal_user_mgt_delete internal_login internal_user_mgt_delete " +
                        "internal_user_mgt_view internal_user_mgt_list"));
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");
        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        String sessionDataKey;
        if (sessionDataKeyConsent == null) {
            Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                    "sessionDataKey not found in response.");
            sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
            Assert.assertNotNull(sessionDataKey, "sessionDataKey is null.");
            sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        }
        response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");
        locationValue = getLocationHeaderValue(response);
        code = getFragmentParam(locationValue, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE);
        Assert.assertNotNull(code, "Code is null or could not be found.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send token request to get an impersonated token using code grant.",
            dependsOnMethods = "testSSOImpersonationAuthorizeRequestPost")
    public void testSendCodeTokenRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_CODE, code));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));

        String url = OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        JSONObject jsonResponse = responseObject(url, urlParameters, consumerKey, consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );

        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim.");
        String refreshToken = (String) jsonResponse.get(OAuth2Constant.REFRESH_TOKEN);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");
        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));

        jsonResponse = responseObject(url, urlParameters, consumerKey, consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );

        actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim.");

        refreshToken = (String) jsonResponse.get(OAuth2Constant.REFRESH_TOKEN);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.REFRESH_TOKEN), "Refresh token is null.");
        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, OAUTH2_GRANT_TYPE_REFRESH_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken));

        jsonResponse = responseObject(url, urlParameters, consumerKey, consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );

        actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request to SSO as the impersonatee using " +
            "implicit grant.",
            dependsOnMethods = "testSendCodeTokenRequestPost")
    public void testSSOImpersonationImplicitAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAUTH2_GRANT_TYPE_IMPLICIT));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
                "internal_user_mgt_delete internal_login internal_user_mgt_delete " +
                        "internal_user_mgt_view internal_user_mgt_list"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String locationValue = getLocationHeaderValue(response);
        log.info("locationValue");
        log.info(locationValue);
        EntityUtils.consume(response.getEntity());
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);

        String sessionDataKey;
        if (sessionDataKeyConsent == null) {
            Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                    "sessionDataKey not found in response.");
            sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
            Assert.assertNotNull(sessionDataKey, "sessionDataKey is null.");

            sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        }
        response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        locationValue = getLocationHeaderValue(response);
        accessToken = getFragmentParam(locationValue, OAuth2Constant.ACCESS_TOKEN);
        Assert.assertNotNull(accessToken, "Access token is null or could not be found.");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
                "Subject Id is not end user Id in the impersonation flow." );

        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim.");

        EntityUtils.consume(response.getEntity());
    }

//    @Test(groups = "wso2.is", description = "Send authorize user request to SSO as the impersonatee.",
//            dependsOnMethods = "testSSOImpersonationImplicitAuthorizeRequestPost")
//    public void testSSOImpersonationAuthorizeRequestPostWithSkipLoginConsent() throws Exception {
//
//        updateApplicationToSkipLoginConsent(true);
//        ApplicationResponseModel application = getApplication(applicationId);
//        if (application == null) {
//            Assert.fail("Application not found for the given application Id: " + applicationId);
//        }
//        Assert.assertTrue(application.getAdvancedConfigurations().getSkipLoginConsent(),
//                "Skip login consent is not enabled for the application.");
//
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
//                OAUTH2_GRANT_TYPE_IMPLICIT));
//        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
//        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
//        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE,
//                "internal_user_mgt_delete internal_login internal_user_mgt_delete " +
//                        "internal_user_mgt_view internal_user_mgt_list"));
//
//        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
//                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
//        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");
//
//        String locationValue = getLocationHeaderValue(response);
//        EntityUtils.consume(response.getEntity());
//        accessToken = getFragmentParam(locationValue, OAuth2Constant.ACCESS_TOKEN);
//        Assert.assertNotNull(accessToken, "Access token is null or could not be found.");
//        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
//        Assert.assertEquals(jwtClaimsSet.getSubject(), endUserId,
//                "Subject Id is not end user Id in the impersonation flow." );
//
//        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
//        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
//        Assert.assertEquals(actClaimSet.get("sub"), impersonatorId, "Impersonator Id is not in the act claim.");
//
//        EntityUtils.consume(response.getEntity());
//
//        updateApplicationToSkipLoginConsent(false);
//    }

    @Test(dependsOnMethods = { "testSendTokenExchangeRequestPost", "testSendCodeTokenRequestPost",
            "testSSOImpersonationImplicitAuthorizeRequestPost" },
            description = "Tests the impersonated access token with user listing API.")
    public void testImpersonatedAccessToken() throws Exception {

        HttpGet request = new HttpGet(SCIM2_USERS_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for User listing is" +
                " failed");
    }

    @Test(groups = "wso2.is", description = "Org - Send authorize user request with impersonation related response types " +
            "and response modes.")
    public void testInitOrgImpersonationAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, URLEncoder.encode(
                "id_token subject_token", "UTF-8")));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, URLEncoder.encode(
                "internal_org_user_mgt_delete internal_login openid internal_org_user_impersonate " +
                        "internal_org_user_mgt_delete internal_org_user_mgt_view internal_org_user_mgt_list", "UTF-8")));
        urlParameters.add(new BasicNameValuePair("requested_subject", orgEndUserId));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(QUERY_PARAM_ORG_ID, subOrgID));
        urlParameters.add(new BasicNameValuePair(FIDP_QUERY_PARAM, ORGANIZATION_SSO));

        String sessionDataKey = sendAuthorizationRequest(buildGetRequestURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, "carbon.super", urlParameters), false);
        if (sessionDataKey != null) {
            sendLoginPost(sessionDataKey);
        }

        Assert.assertNotNull(subjectToken, "Subject token is null or could not be found.");
        Assert.assertNotNull(idToken, "Id token is null or could not be found.");

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(subjectToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), orgEndUserId,
                "Subject Id is not end user Id in the impersonation flow." );
        Map<String, String> mayActClaimSet = (Map) jwtClaimsSet.getClaim("may_act");
        Assert.assertNotNull(mayActClaimSet, "may_act claim of subject token is empty");
        assertEquals(mayActClaimSet.get("sub"), orgImpersonatorId,
                "Impersonator Id is not in the may act claim." );
    }

    @Test(groups = "wso2.is", description = "Org - Send authorize user request with impersonation related response types " +
            "and response modes.")
    public void testSSOOrgImpersonationAuthorizeRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, URLEncoder.encode(
                "internal_org_user_mgt_delete internal_login openid internal_org_user_mgt_delete " +
                        "internal_org_user_mgt_view internal_org_user_mgt_list", "UTF-8")));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(QUERY_PARAM_ORG_ID, subOrgID));
        urlParameters.add(new BasicNameValuePair(FIDP_QUERY_PARAM, ORGANIZATION_SSO));

        String sessionDataKey = sendAuthorizationRequest(buildGetRequestURL(
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL, "carbon.super", urlParameters), true);
        if (sessionDataKey != null) {
            sendLoginPost(sessionDataKey);
        }

        Assert.assertNotNull(subjectToken, "Subject token is null or could not be found.");
        Assert.assertNotNull(idToken, "Id token is null or could not be found.");

        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(subjectToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), orgEndUserId,
                "Subject Id is not end user Id in the impersonation flow." );
        Map<String, String>  mayActClaimSet = (Map) jwtClaimsSet.getClaim("may_act");
        Assert.assertNotNull(mayActClaimSet, "may_act claim of subject token is empty");
        assertEquals(mayActClaimSet.get("sub"), orgImpersonatorId,
                "Impersonator Id is not in the may act claim." );
    }
    @Test(groups = "wso2.is", description = "Org - Send authorize user request with response types and response modes.",
            dependsOnMethods = "testInitOrgImpersonationAuthorizeRequestPost")
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
                "Subject Id is not end user Id in the impersonation flow." );
        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        assertEquals(actClaimSet.get("sub"), orgImpersonatorId, "Impersonator Id is not in the act claim." );
    }

    @Test(groups = "wso2.is", description = "Org - Send token request to get an impersonated token using code grant.",
            dependsOnMethods = "testSSOOrgImpersonationAuthorizeRequestPost")
    public void testOrgSendCodeTokenRequestPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(GRANT_TYPE_KEY, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE));
        urlParameters.add(new BasicNameValuePair(OAUTH2_GRANT_TYPE_CODE, code));
        urlParameters.add(new BasicNameValuePair(REDIRECT_URI_NAME, OAuth2Constant.CALLBACK_URL));

        String url = OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        JSONObject jsonResponse = responseObject(url, urlParameters, consumerKey, consumerSecret);
        Assert.assertNotNull(jsonResponse.get(OAuth2Constant.ACCESS_TOKEN), "Access token is null.");
        accessToken = (String) jsonResponse.get(OAuth2Constant.ACCESS_TOKEN);
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        assertEquals(jwtClaimsSet.getSubject(), orgEndUserId,
                "Subject Id is not end user Id in the impersonation flow." );

        Map<String, String>  actClaimSet = (Map) jwtClaimsSet.getClaim("act");
        Assert.assertNotNull(actClaimSet, "Act claim of impersonated access token is empty");
        assertEquals(actClaimSet.get("sub"), orgImpersonatorId, "Impersonator Id is not in the act claim.");
    }

    @Test(dependsOnMethods = { "testOrgSendTokenExchangeRequestPost", "testOrgSendCodeTokenRequestPost" },
            description = "Tests the impersonated access token with user listing API.")
    public void testOrgImpersonatedAccessToken() throws Exception {

        HttpGet request = new HttpGet(ORG_SCIM2_USERS_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for User listing is" +
                " failed");
    }

    private String sendAuthorizationRequest(String endPointUrl, boolean isSSORequest) throws Exception {

        HttpResponse authorizeResponse = sendGetRequest(endPointUrl, httpClientWithoutAutoRedirections);
        Assert.assertNotNull(authorizeResponse, "Authorize response is null.");
        Assert.assertEquals(authorizeResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Authorize response status code is invalid.");
        Header authorizeLocationHeader = authorizeResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(authorizeLocationHeader, "Authorize response header location is null.");
        EntityUtils.consume(authorizeResponse.getEntity());

        HttpResponse authorizeRedirectResponse =
                sendGetRequest(authorizeLocationHeader.getValue(), httpClientWithoutAutoRedirections);

        if (isSSORequest) {
            // Authorize redirect request to root org.
            Assert.assertEquals(authorizeRedirectResponse.getStatusLine().getStatusCode(),
                    HttpStatus.SC_MOVED_TEMPORARILY, "Root organization common auth redirection status code is invalid.");
            Assert.assertNotNull(authorizeRedirectResponse, "Root organization common auth response is null.");
            Header rootOrgAuthRedirectionLocation =
                    authorizeRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(rootOrgAuthRedirectionLocation,
                    "Root organization common auth response location header is null.");
            EntityUtils.consume(authorizeRedirectResponse.getEntity());

            HttpResponse commonAuthResponse =
                    sendGetRequest(rootOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);

            // Common auth response to parent org.
            Header commonAuthResponseRedirectionLocation =
                    commonAuthResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(commonAuthResponseRedirectionLocation, "Common auth response location header is null.");
            Assert.assertTrue(commonAuthResponseRedirectionLocation.toString().contains(OAuth2Constant.SESSION_DATA_KEY),
                    "sessionDataKey not found in response.");
            EntityUtils.consume(commonAuthResponse.getEntity());

            HttpResponse consentRequestResponse = sendGetRequest(commonAuthResponseRedirectionLocation.getValue(),
                    httpClientWithoutAutoRedirections);

            // Consent request parent org.
            Header consentRequestRedirectionLocation = consentRequestResponse.getFirstHeader(
                    OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(consentRequestRedirectionLocation, "Authorization code response location header is null.");
            Assert.assertTrue(consentRequestRedirectionLocation.toString().contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                    "sessionDataKeyConsent not found in response.");
            String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(
                    consentRequestRedirectionLocation.getValue(), OAuth2Constant.SESSION_DATA_KEY_CONSENT);
            Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
            EntityUtils.consume(consentRequestResponse.getEntity());

            HttpResponse authCodeResponse = sendApprovalPost(client, sessionDataKeyConsent);

            // Code response request parent org.
            Assert.assertNotNull(authCodeResponse, "Approval request failed. response is invalid.");
            Header authCodeRedirectionLocation = authCodeResponse.getFirstHeader(
                    OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

            URI authCodeRedirectionURI = new URI(authCodeRedirectionLocation.getValue());
            code = Arrays.stream(authCodeRedirectionURI.getQuery().split(AMPERSAND))
                    .filter(param -> param.startsWith(OAuth2Constant.AUTHORIZATION_CODE_NAME))
                    .map(param -> param.split(EQUAL)[1])
                    .findFirst()
                    .orElse(null);

            return null;
        } else {
            // Login request to child org.
            Assert.assertNotNull(authorizeRedirectResponse, "Redirected authorize response is null.");
            Assert.assertEquals(authorizeRedirectResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                    "Redirected authorize response status code is invalid.");
            Header authorizeRedirectLocationHeader =
                    authorizeRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertNotNull(authorizeRedirectLocationHeader, "Redirected authorize response header location is null.");
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
    }

    private void sendLoginPost(String sessionDataKey) throws Exception {

        // Common auth org request.
        String commonAuthURL = serverURL + ORGANIZATION_PATH + subOrgID + "/commonauth";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", ORG_IMPERSONATOR_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", ORG_IMPERSONATOR_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        HttpResponse loginPostResponse =
                sendPostRequest(commonAuthURL, urlParameters, httpClientWithoutAutoRedirections);

        // Common auth child redirect request.
        Assert.assertNotNull(loginPostResponse, "Login request failed. Login response is null.");
        Assert.assertEquals(loginPostResponse.getStatusLine().getStatusCode(), HttpStatus.SC_MOVED_TEMPORARILY,
                "Login status code is invalid.");
        Header childOrgAuthRedirectionLocation =
                loginPostResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(childOrgAuthRedirectionLocation, "Login response location header is null.");
        EntityUtils.consume(loginPostResponse.getEntity());

        HttpResponse childOrgAuthRedirectResponse =
                sendGetRequest(childOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);

        // Common auth parent org redirect request.
        Assert.assertEquals(childOrgAuthRedirectResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Child organization auth redirection status code is invalid.");
        Assert.assertNotNull(childOrgAuthRedirectResponse,
                "Child organization authorize redirection response is null.");
        Header rootOrgCommonAuthRedirectionLocation =
                childOrgAuthRedirectResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgCommonAuthRedirectionLocation,
                "Child organization authorize redirection response location header is null.");
        EntityUtils.consume(childOrgAuthRedirectResponse.getEntity());

        HttpResponse rootOrgCommonAuthRedirectionResponse =
                sendGetRequest(rootOrgCommonAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);

        // Authorize redirect request to root org.
        Assert.assertEquals(rootOrgCommonAuthRedirectionResponse.getStatusLine().getStatusCode(),
                HttpStatus.SC_MOVED_TEMPORARILY, "Root organization common auth redirection status code is invalid.");
        Assert.assertNotNull(rootOrgCommonAuthRedirectionResponse, "Root organization common auth response is null.");
        Header rootOrgAuthRedirectionLocation =
                rootOrgCommonAuthRedirectionResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(rootOrgAuthRedirectionLocation,
                "Root organization common auth response location header is null.");
        EntityUtils.consume(rootOrgCommonAuthRedirectionResponse.getEntity());

        HttpResponse consentRequestResponse =
                sendGetRequest(rootOrgAuthRedirectionLocation.getValue(), httpClientWithoutAutoRedirections);

        // Consent request parent org.
        Header consentRequestRedirectionLocation =
                consentRequestResponse.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(consentRequestRedirectionLocation, "Authorization code response location header is null.");
        Assert.assertTrue(consentRequestRedirectionLocation.toString().contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(
                consentRequestRedirectionLocation.getValue(), OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        EntityUtils.consume(consentRequestResponse.getEntity());

        HttpResponse authCodeResponse = sendApprovalPost(client, sessionDataKeyConsent);

        // Code response request parent org.
        Assert.assertNotNull(authCodeResponse, "Approval request failed. response is invalid.");
        Header authCodeRedirectionLocation = authCodeResponse.getFirstHeader(
                OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        subjectToken = getFragmentParam(authCodeRedirectionLocation.getValue(), OAuth2Constant.SUBJECT_TOKEN);
        idToken = getFragmentParam(authCodeRedirectionLocation.getValue(), OAuth2Constant.ID_TOKEN);

        EntityUtils.consume(authCodeResponse.getEntity());
    }

    private ApplicationResponseModel createImpersonationApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:token-exchange");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setSubjectToken(new SubjectTokenConfiguration().enable(true)
                .applicationSubjectTokenExpiryInSeconds(18000));

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);

        oidcConfig.setAccessToken(accessTokenConfig);
        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);
        application.setIsManagementApp(true);

        String appId = addApplication(application);

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            // Authorize few system APIs.
            authorizeSystemAPIs(appId,
                    new ArrayList<>(Arrays.asList(impersonationResourceIdentifier, scim2UserResourceIdentifier,
                            orgImpersonationResourceIdentifier, orgScim2UserResourceIdentifier)));
        }
        shareApplication(appId);
        return getApplication(appId);
    }

    private void shareApplication(String appId) throws IOException {

        ApplicationSharePOSTRequest applicationSharePOSTRequest = new ApplicationSharePOSTRequest();
        applicationSharePOSTRequest.setShareWithAllChildren(true);
        restClient.shareApplication(appId, applicationSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        await().atMost(5, TimeUnit.SECONDS).until(() -> true);
    }

    private void updateApplicationToSkipLoginConsent(boolean skipLoginConsent) throws Exception {

        ApplicationPatchModel updatedApplication = new ApplicationPatchModel();
        updatedApplication.advancedConfigurations(
                new AdvancedApplicationConfiguration().skipLoginConsent(skipLoginConsent));

        updateApplication(applicationId, updatedApplication);
    }

    private void createOrganization() throws Exception {

        subOrgID = orgMgtRestClient.addOrganization(SUB_ORG_NAME);
        subOrgToken = orgMgtRestClient.switchM2MToken(subOrgID);
    }

    private void createOrgUsers() throws Exception {

        // Create sub org end user.
        UserObject endUser = new UserObject();
        endUser.setUserName(ORG_END_USER_USERNAME);
        endUser.setPassword(ORG_END_USER_PASSWORD);
        endUser.addEmail(new Email().value(ORG_END_USER_EMAIL));
        orgEndUserId = scim2RestClient.createSubOrgUser(endUser, subOrgToken);
        // Create sub org impersonator.
        UserObject impersonator = new UserObject();
        impersonator.setUserName(ORG_IMPERSONATOR_USERNAME);
        impersonator.setPassword(ORG_IMPERSONATOR_PASSWORD);
        impersonator.addEmail(new Email().value(ORG_IMPERSONATOR_EMAIL));
        orgImpersonatorId = scim2RestClient.createSubOrgUser(impersonator, subOrgToken);
    }

    private void assignOrgRolesToOrgUsers() throws Exception {

        String sharedAppId = restClient.getAppIdUsingAppNameInOrganization(SERVICE_PROVIDER_NAME, subOrgToken);
        // Assign end user role to sub org end user.
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
        // Assign impersonator role to sub org impersonator.
        String impersonatorSharedAppRoleId =
                scim2RestClient.getRoleIdByNameAndAudienceInSubOrg(IMPERSONATOR_ROLE_NAME, sharedAppId, subOrgToken);
        RoleItemAddGroupobj impersonatorRolePatchObj = new RoleItemAddGroupobj();
        impersonatorRolePatchObj.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        impersonatorRolePatchObj.setPath(USERS_PATH);
        impersonatorRolePatchObj.addValue(new ListObject().value(orgImpersonatorId));
        scim2RestClient.updateUsersOfRoleV2InSubOrg(
                impersonatorSharedAppRoleId,
                new PatchOperationRequestObject().addOperations(impersonatorRolePatchObj),
                subOrgToken);
    }

    private void addImpersonator() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(IMPERSONATOR_USERNAME);
        userInfo.setPassword(IMPERSONATOR_PASSWORD);
        userInfo.setName(new Name().givenName(IMPERSONATOR_USERNAME));
        userInfo.addEmail(new Email().value(IMPERSONATOR_EMAIL));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country(COUNTRY_CLAIM_VALUE));

        impersonatorId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("everyone");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(impersonatorId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    private void addEndUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(END_USER_USERNAME);
        userInfo.setPassword(END_USER_PASSWORD);
        userInfo.setName(new Name().givenName(END_USER_USERNAME));
        userInfo.addEmail(new Email().value(END_USER_EMAIL));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country(COUNTRY_CLAIM_VALUE));

        endUserId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("everyone");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(endUserId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    private void createImpersonatorRole(String appID) throws JSONException, IOException {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(INTERNAL_USER_IMPERSONATE));
        permissions.add(new Permission(ORG_INTERNAL_USER_IMPERSONATE));
        Audience roleAudience = new Audience("APPLICATION", appID);
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleAudience, IMPERSONATOR_ROLE_NAME, permissions, schemas);

        impersonationRoleID = addRole(role);
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(impersonatorId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject),
                impersonationRoleID);
    }

    private void createEndUserRole(String appID) throws JSONException, IOException {

        List<Permission> permissions = new ArrayList<>();
        permissions.add(new Permission(PERMISSION_VIEW));
        permissions.add(new Permission(PERMISSION_LIST));
        permissions.add(new Permission(ORG_PERMISSION_VIEW));
        permissions.add(new Permission(ORG_PERMISSION_LIST));
        Audience roleAudience = new Audience(AUDIENCE_TYPE, appID);
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleAudience, END_USER_ROLE_NAME, permissions, schemas);
        endUserRoleID = addRole(role);
        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath(USERS);
        rolePatchReqObject.addValue(new ListObject().value(endUserId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject),
                endUserRoleID);
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location, "Location header is null.");
        return location.getValue();
    }

    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = impersonatorLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationValue);
        Assert.assertNotNull(response, "GET request response is null.");

        locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        return sessionDataKeyConsent;
    }

    /**
     * Send login post request.
     *
     * @param client         - Http client.
     * @param sessionDataKey - Session data key.
     * @return Http response.
     * @throws ClientProtocolException If an error occurred while executing login post request.
     * @throws java.io.IOException     If an error occurred while executing login post request.
     */
    private HttpResponse impersonatorLoginPost(HttpClient client, String sessionDataKey) throws ClientProtocolException,
            IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", IMPERSONATOR_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", IMPERSONATOR_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        return sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(OAuth2Constant.COMMON_AUTH_URL, tenantInfo.getDomain()));
    }

    private String getFragmentParam(String url, String key) {

        String param = DataExtractUtil.extractParamFromURIFragment(url, key);

        Assert.assertNotNull(param, "Param not found for the key : " + key);
        return param;
    }

    /**
     * Build post request and return json response object.
     *
     * @param endpoint       Endpoint.
     * @param postParameters postParameters.
     * @param key            Basic authentication key.
     * @param secret         Basic authentication secret.
     * @return JSON object of the response.
     * @throws Exception Exception.
     */
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

    private String getAuthzHeader() {

        return "Bearer " + accessToken;
    }
}
