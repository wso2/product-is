/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.Header;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionEnterprise;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceAuthCodeGrantOpenIdRequestObjectTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String ENCODED_OIDC_CLAIM_DIALECT = "aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0";
    private static final String USERS_PATH = "users";
    private static final String LOCALE = "en_US";
    private ServerConfigurationManager serverConfigurationManager;

    private String adminUsername;
    private String adminPassword;
    private String accessToken;
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;

    private String consumerKey;
    private String consumerSecret;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;
    private List<NameValuePair> consentParameters = new ArrayList<>();
    private CookieStore cookieStore = new BasicCookieStore();
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";
    private static final String givenNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String countryClaimURI = "http://wso2.org/claims/country";
    private static final String customClaimURI1 = "http://wso2.org/claims/department";
    private static final String customClaimURI2 = "http://wso2.org/claims/stateorprovince";
    private static final String externalClaimURI1 = "externalClaim1";
    private static final String externalClaimURI2 = "externalClaim2";
    private static final String USER_EMAIL = "abcrqo@wso2.com";
    private static final String USERNAME = "authcodegrantreqobjuser";
    private static final String PASSWORD = "Pass@123";
    private static final String GIVEN_NAME = "TestName";
    private static final String COUNTRY = "Country";
    private static final String CUSTOM_CLAIM1 = "customVal1";
    private static final String CUSTOM_CLAIM2 = "customVal2";

    private static final String REQUEST = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwi" +
            "YXVkIjpbImh0dHBzOi8vbG9jYWxob3N0Ojk0NDMvb2F1dGgyL3Rva2VuIl0sImNsYWltcyI6eyJ1c2VyaW5mbyI6eyJnaXZlbl9uYW" +
            "1lIjp7ImVzc2VudGlhbCI6dHJ1ZX0sIm5pY2tuYW1lIjpudWxsLCJlbWFpbCI6eyJlc3NlbnRpYWwiOnRydWV9LCJleHRlcm5hbENs" +
            "YWltMSI6eyJlc3NlbnRpYWwiOnRydWV9LCJwaWN0dXJlIjpudWxsfSwiaWRfdG9rZW4iOnsiZ2VuZGVyIjpudWxsLCJiaXJ0aGRhdGU" +
            "iOnsiZXNzZW50aWFsIjp0cnVlfSwiY3VzdG9tQ2xhaW0xIjp7ImVzc2VudGlhbCI6dHJ1ZX0sImFjciI6eyJ2YWx1ZXMiOlsidXJuOm1" +
            "hY2U6aW5jb21tb246aWFwOnNpbHZlciJdfX19LCJpc3MiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwiaWF0IjoxNTE2Nzg" +
            "zMjc4LCJqdGkiOiIxMDAzIn0=.";

    private SCIM2RestClient scim2RestClient;
    private ClaimManagementRestClient claimManagementRestClient;

    private String applicationId;
    private String userId;
    private String claimId1;
    private String claimId2;

    @BeforeTest(alwaysRun = true)
    public void initConfiguration() throws Exception {

        super.init();
        changeISConfiguration();
    }

    @AfterTest(alwaysRun = true)
    public void restoreConfiguration() throws Exception {

        resetISConfiguration();
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
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
        setSystemproperties();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);

        addAdminUser();
        claimId1 = addOIDCClaims(externalClaimURI1, customClaimURI1);
        claimId2 = addOIDCClaims(externalClaimURI2, customClaimURI2);

    }

    private String addOIDCClaims(String externalClaim, String localClaim) throws Exception {

        ExternalClaimReq externalClaimReq = new ExternalClaimReq();
        externalClaimReq.setClaimURI(externalClaim);
        externalClaimReq.setMappedLocalClaimURI(localClaim);
        return claimManagementRestClient.addExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, externalClaimReq);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        // Delete the added OIDC claims.
        claimManagementRestClient.deleteExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, claimId1);
        claimManagementRestClient.deleteExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, claimId2);

        consumerKey = null;
        accessToken = null;

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        claimManagementRestClient.closeHttpClient();
        client.close();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "OAuth App creation failed.");
        applicationId = application.getId();
        UpdateApplicationClaimConfig(applicationId);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorozedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL
                + "?request=" + REQUEST));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
                        OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Authorization request failed. Authorized response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response,
                "Authorization request failed. Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request", dependsOnMethods = "testSendAuthorozedPost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        EntityUtils.consume(response.getEntity());
        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        response = sendConsentGetRequest(client, locationHeader.getValue(), cookieStore, consentParameters);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractSessionConsentDataFromResponse(response,
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

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("Authorization Code", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractTableRowDataFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(response, "Authorization Code key value is invalid.");

        if (keyValues != null) {
            authorizationCode = keyValues.get(0).getValue();
        }
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Get access token", dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessToken() throws Exception {

        HttpResponse response = sendGetAccessTokenPost(client, consumerSecret);
        Assert.assertNotNull(response, "Approval response is invalid.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"accessToken\"", 1);
        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");

        accessToken = keyValues.get(0).getValue();
        Assert.assertNotNull(accessToken, "Access token is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, OAuth2Constant.AUTHORIZED_URL);

        keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("id=\"loggedUser\"", 1);
        keyValues = DataExtractUtil.extractLabelValueFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");

        String loggedUser = keyValues.get(0).getValue();
        Assert.assertNotNull(loggedUser, "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "null", "Logged user is null.");
        Assert.assertNotEquals(loggedUser, "", "Logged user is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testGetAccessToken")
    public void testValidateAccessToken() throws Exception {

        HttpResponse response = sendValidateAccessTokenPost(client, accessToken);
        Assert.assertNotNull(response, "Validate access token response is invalid.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"valid\"", 1);

        List<KeyValue> keyValues =
                DataExtractUtil.extractInputValueFromResponse(response,
                        keyPositionMap);
        Assert.assertNotNull(keyValues, "Access token Key value is null.");
        String valid = keyValues.get(0).getValue();
        Assert.assertEquals(valid, "true", "Token Validation failed");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Validate the user claim values", dependsOnMethods = "testGetAccessToken")
    public void testClaims() throws Exception {

        HttpGet request = new HttpGet(OAuth2Constant.USER_INFO_ENDPOINT);

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        String email = ((JSONObject) obj).get("email").toString();
        String givenName = ((JSONObject) obj).get("given_name").toString();
        Object externalClaim1 = ((JSONObject) obj).get("externalClaim1");
        Object externalClaim2 = ((JSONObject) obj).get("externalClaim2");

        EntityUtils.consume(response.getEntity());
        Assert.assertEquals(USER_EMAIL, email, "Incorrect email claim value");
        Assert.assertEquals(GIVEN_NAME, givenName, "Incorrect given_name claim value");
        Assert.assertEquals(CUSTOM_CLAIM1, externalClaim1, "Incorrect externalClaim1 claim value");
        Assert.assertNull(externalClaim2, "A value for externalClaim2 claim is present in the response.");
    }

    @Test(groups = "wso2.is", description = "Validate Token Expiration Time",
            dependsOnMethods = "testValidateAccessToken")
    public void testValidateTokenExpirationTime() throws Exception {

        JSONObject tokenResponse = introspectToken();
        
        Assert.assertNotNull(tokenResponse.get("exp"), "'exp' value is not included");
        long expValue = Long.parseLong(tokenResponse.get("exp").toString());
        // ratio between these vales is normally 999, used 975 just to be in the safe side
        Assert.assertTrue(System.currentTimeMillis() / expValue > 975, "'exp' time is not in milliseconds");

    }

    @Test(groups = "wso2.is", description = "Validate Authorization Context of jwt Token", dependsOnMethods =
            "testValidateAccessToken")
    public void testValidateTokenScope() throws Exception {

        JSONObject tokenResponse = introspectToken();
        Assert.assertTrue(tokenResponse.size() > 1, "Invalid JWT token received");
        Assert.assertNotNull(tokenResponse.get("scope"), "'scope' is not included");

        String scopes = tokenResponse.get("scope").toString();
        Assert.assertTrue(scopes.contains("openid"), "Invalid JWT Token scope Value");
    }

    private void changeISConfiguration() throws Exception {

        log.info("Adding entity id of SSOService to deployment.toml file");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "oauth"
                + File.separator + "jwt-token-gen-enabled-identity.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    public HttpResponse sendLoginPost(HttpClient client, String sessionDataKey) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", USERNAME));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));

        return sendPostRequestWithParameters(client, urlParameters, OAuth2Constant.COMMON_AUTH_URL);
    }

    private JSONObject introspectToken() throws Exception {

        String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
        return introspectTokenWithTenant(client, accessToken, introspectionUrl, adminUsername, adminPassword);
    }

    private void addAdminUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(USERNAME);
        userInfo.setPassword(PASSWORD);
        userInfo.setName(new Name().givenName(GIVEN_NAME));
        userInfo.addEmail(new Email().value(USER_EMAIL));

        ScimSchemaExtensionEnterprise scimSchemaExtensionEnterprise = new ScimSchemaExtensionEnterprise();
        scimSchemaExtensionEnterprise.setCountry(COUNTRY);
        scimSchemaExtensionEnterprise.setDepartment(CUSTOM_CLAIM1);
        scimSchemaExtensionEnterprise.setStateorprovince(CUSTOM_CLAIM2);
        userInfo.setScimSchemaExtensionEnterprise(scimSchemaExtensionEnterprise);

        userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }
}
