/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.scim2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ADMIN_ROLE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.FAMILY_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GIVEN_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PASSWORD_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_ME_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

public class SCIM2MeTestCase extends ISIntegrationTest {

    private static final String FAMILY_NAME_CLAIM_VALUE = "scim";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String FAMILY_NAME_CLAIM_VALU_1 = "scimUser";
    private static final String GIVEN_NAME_CLAIM_VALUE_1 = "testScimUser";
    private static final String EMAIL_TYPE_WORK_CLAIM_URI = "work";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE = "scim2user@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_URI = "home";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE = "scim2user@gmail.com";
    private static final String USERNAME = "scim2user";
    private static final String USERNAME_1 = "testScim2user";
    private static final String PASSWORD = "Wso2@test123";
    private static final String SCIM2_ME_API_IDENTIFIER = "/scim2/Me";
    private static final String SCIM2_USERS_API_IDENTIFIER = "/scim2/Users";

    // Scopes for admin (client_credentials) token: create via /scim2/Me, delete/view via /scim2/Users for cleanup.
    private static final String ADMIN_TOKEN_SCOPES =
            "internal_user_mgt_create internal_user_mgt_delete internal_user_mgt_view";
    // Scopes for user (ROPC) token used to call /scim2/Me endpoints.
    private static final String USER_TOKEN_SCOPES = "internal_login internal_user_mgt_delete";

    private CloseableHttpClient client;

    private String userId;
    private String userId2;
    private String userToken;
    private String appId;
    private String clientId;
    private String clientSecret;
    private final Tenant tenantBean;
    private OAuth2RestClient oauth2RestClient;
    private final String tenant;

    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2MeTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.tenantBean = context.getContextTenant();
        this.tenant = tenantBean.getDomain();
    }

    @DataProvider(name = "SCIM2MeConfigProvider")
    public static Object[][] SCIM2MeConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void initClass() throws Exception {
        super.init();
        client = HttpClients.createDefault();

        oauth2RestClient = new OAuth2RestClient(serverURL, tenantBean);
        ApplicationModel appModel = new ApplicationModel();
        appModel.setName("SCIM2MeTestApp");
        appModel.setIsManagementApp(true);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList("authorization_code", "client_credentials", "password"));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        appModel.setInboundProtocolConfiguration(inboundProtocols);
        appId = oauth2RestClient.createApplication(appModel);

        OpenIDConnectConfiguration oidcDetails = oauth2RestClient.getOIDCInboundDetails(appId);
        clientId = oidcDetails.getClientId();
        clientSecret = oidcDetails.getClientSecret();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeAPIForApp(SCIM2_ME_API_IDENTIFIER);
            authorizeAPIForApp(SCIM2_USERS_API_IDENTIFIER);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        // scim2user may already be deleted by testDeleteMe; attempt deletion and then verify it is gone.
        if (userId != null) {
            deleteUserById(userId);
            String adminToken = getAdminToken();
            HttpGet getRequest = new HttpGet(getUsersPath() + "/" + userId);
            getRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
            getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            HttpResponse getResponse = client.execute(getRequest);
            assertEquals(getResponse.getStatusLine().getStatusCode(), 404, "User has not been deleted successfully");
            EntityUtils.consume(getResponse.getEntity());
        }
        // Clean up the second user created in testCreateMeWithCharsetEncodingHeader.
        if (userId2 != null) {
            deleteUserById(userId2);
        }
        if (appId != null) {
            try {
                oauth2RestClient.deleteApplication(appId);
            } catch (Exception e) {
                // ignore cleanup errors
            }
        }
        if (oauth2RestClient != null) {
            oauth2RestClient.closeHttpClient();
        }
    }

    @Test
    public void testCreateMe() throws Exception {

        String adminToken = getAdminToken();
        HttpPost request = new HttpPost(getMePath());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, FAMILY_NAME_CLAIM_VALUE);
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_CLAIM_URI);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_CLAIM_URI);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);
        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
        assignUserToGroup(USERNAME);
    }

    @Test
    public void testCreateMeWithCharsetEncodingHeader() throws Exception {
        String adminToken = getAdminToken();
        HttpPost request = new HttpPost(getMePath());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, FAMILY_NAME_CLAIM_VALU_1);
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE_1);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME_1);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_CLAIM_URI);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_CLAIM_URI);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);
        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME_1);

        userId2 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId2);
        assignUserToGroup(USERNAME_1);
    }

    @Test(dependsOnMethods = "testCreateMe")
    public void testGetMe() throws Exception {

        userToken = getUserToken();

        HttpGet request = new HttpGet(getMePath());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

//    @Test(dependsOnMethods = "testGetMe")
//    public void testDeleteMe() throws Exception {
//
//        HttpDelete request = new HttpDelete(getMePath());
//        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userToken);
//
//        HttpResponse response = client.execute(request);
//        assertEquals(response.getStatusLine().getStatusCode(), 204);
//    }

    private String getMePath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_ME_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_ME_ENDPOINT;
        }
    }

    private String getUsersPath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getTokenEndpoint() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverURL + "oauth2/token";
        }
        return serverURL + "t/" + tenant + "/oauth2/token";
    }

    private String getAdminToken() throws Exception {

        HttpPost post = new HttpPost(getTokenEndpoint());
        post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes()).trim());
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("scope", ADMIN_TOKEN_SCOPES));
        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(post);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject json = (JSONObject) JSONValue.parse(responseStr);
        assertNotNull(json.get("access_token"), "Failed to get admin token: " + responseStr);
        return json.get("access_token").toString();
    }

    private String getUserToken() throws Exception {

        HttpPost post = new HttpPost(getTokenEndpoint());
        post.addHeader(HttpHeaders.AUTHORIZATION, OAuth2Constant.BASIC_HEADER + " " +
                Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes()).trim());
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.addHeader("User-Agent", OAuth2Constant.USER_AGENT);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
        params.add(new BasicNameValuePair("username", USERNAME));
        params.add(new BasicNameValuePair("password", PASSWORD));
        params.add(new BasicNameValuePair("scope", USER_TOKEN_SCOPES));
        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(post);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject json = (JSONObject) JSONValue.parse(responseStr);
        assertNotNull(json.get("access_token"), "Failed to get user token: " + responseStr);
        return json.get("access_token").toString();
    }

    private void deleteUserById(String id) throws Exception {
        if (client == null) {
            client = HttpClients.createDefault();
        }
        String adminToken = getAdminToken();
        HttpDelete request = new HttpDelete(getUsersPath() + "/" + id);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());
    }

    private void authorizeAPIForApp(String apiIdentifier) throws IOException {
        List<APIResourceListItem> filteredAPIResource =
                oauth2RestClient.getAPIResourcesWithFiltering("identifier+eq+" + apiIdentifier);
        if (filteredAPIResource != null && !filteredAPIResource.isEmpty()) {
            String apiId = filteredAPIResource.get(0).getId();
            List<ScopeGetModel> apiResourceScopes = oauth2RestClient.getAPIResourceScopes(apiId);
            AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
            authorizedAPICreationModel.setId(apiId);
            authorizedAPICreationModel.setPolicyIdentifier("RBAC");
            apiResourceScopes.forEach(scope -> authorizedAPICreationModel.addScopesItem(scope.getName()));
            oauth2RestClient.addAPIAuthorizationToApplication(appId, authorizedAPICreationModel);
        }
    }

    private void assignUserToGroup(String username) throws Exception {

        AutomationContext automationContext;
        if (tenant.equals(org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            automationContext = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        } else {
            automationContext = new AutomationContext("IDENTITY", TestUserMode.TENANT_ADMIN);
        }

        String backendUrl = automationContext.getContextUrls().getBackEndUrl();
        String sessionCookie = new LoginLogoutClient(automationContext).login();
        UserManagementClient userMgtClient = new UserManagementClient(backendUrl, sessionCookie);

        String[] roles = {ADMIN_ROLE};
        userMgtClient.addRemoveRolesOfUser(username, roles, null);
    }
}
