/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.integration.test.oidc.OIDCAbstractIntegrationTest;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject.MemberItem;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import java.io.IOException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.identity.integration.test.oidc.OIDCUtilTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;

public class PasswordExpirationTestCase extends OIDCAbstractIntegrationTest {

    private static final String TEST_USER1_USERNAME = "pwdExpiryTestUser1";
    private static final String TEST_USER2_USERNAME = "pwdExpiryTestUser2";
    private static final String TEST_USER3_USERNAME = "pwdExpiryTestUser3";
    private static final String TEST_USER4_USERNAME = "pwdExpiryTestUser4";
    private static final String TEST_USER_PASSWORD = "Test@123";
    private static final String TEST_USER_NEW_PASSWORD = "NewTest@123";
    private static final String TEST_ROLE1 = "pwdExpiryTestRole1";
    private static final String TEST_ROLE2 = "pwdExpiryTestRole2";
    private static final String TEST_GROUP1 = "pwdExpiryTestGroup1";
    private static final String TEST_GROUP2 = "pwdExpiryTestGroup2";

    private static final String USERS_PATH = "users";
    private static final String LAST_PASSWORD_UPDATE_CLAIM = "http://wso2.org/claims/identity/lastPasswordUpdateTime";
    private static final String PASSWORD_EXPIRY_CATEGORY_ID = "UGFzc3dvcmQgUG9saWNpZXM";
    private static final String PASSWORD_EXPIRY_CONNECTOR_ID = "cGFzc3dvcmRFeHBpcnk";
    private static final String PASSWORD_EXPIRY_ENABLED = "passwordExpiry.enablePasswordExpiry";
    private static final String PASSWORD_EXPIRY_TIME = "passwordExpiry.passwordExpiryInDays";
    private static final String PASSWORD_EXPIRY_SKIP_IF_NO_APPLICABLE_RULES = "passwordExpiry.skipIfNoApplicableRules";
    private static final String PASSWORD_EXPIRY_RULE1 = "passwordExpiry.rule1";
    private static final String PASSWORD_EXPIRY_RULE2 = "passwordExpiry.rule2";
    private static final String PASSWORD_EXPIRY_RULE3 = "passwordExpiry.rule3";
    private static final String PASSWORD_EXPIRY_RULE4 = "passwordExpiry.rule4";
    private static final int DEFAULT_EXPIRY_TIME = 30;
    
    private String user1Id;
    private String user2Id;
    private String user3Id;
    private String user4Id;
    private String role1Id;
    private String role2Id;
    private String group1Id;
    private String group2Id;

    private SCIM2RestClient scim2RestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private RemoteUserStoreManagerServiceClient userStoreClient;

    private OIDCApplication oidcApplication;
    private CloseableHttpClient oidcClient;
    private final CookieStore cookieStore = new BasicCookieStore();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);
        userStoreClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

        // Update the admin's last password update time to current time to prevent lockout.
        setLastPasswordUpdateTime(userInfo.getUserName(), 0);

        /*
        * 1. Add users - user1, user2, user3, user4.
        * 2. Create roles - role1, role2.
        * 3. Assign users to roles - user1 -> role1, user2 -> role1, role2.
        * 4. Create groups - group1, group2.
        * 5. Assign users to groups - user3 -> group1, user4 -> group1, group2.
         */
        initUsers();
        initRoles();
        initGroups();
        initOIDCClient();
        
        // Initialize OIDC application
        oidcApplication = initApplication();
        createApplication(oidcApplication);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        // Reset password expiration policy.
        setPasswordExpirationPolicy(false, false, true);

        if (user1Id != null) scim2RestClient.deleteUser(user1Id);
        if (user2Id != null) scim2RestClient.deleteUser(user2Id);
        if (user3Id != null) scim2RestClient.deleteUser(user3Id);
        if (user4Id != null) scim2RestClient.deleteUser(user4Id);
        if (role1Id != null) scim2RestClient.deleteV2Role(role1Id);
        if (role2Id != null) scim2RestClient.deleteV2Role(role2Id);
        if (group1Id != null) scim2RestClient.deleteGroup(group1Id);
        if (group2Id != null) scim2RestClient.deleteGroup(group2Id);

        if (scim2RestClient != null) scim2RestClient.closeHttpClient();
        if (identityGovernanceRestClient != null) identityGovernanceRestClient.closeHttpClient();

        deleteApplication(oidcApplication);
        if (oidcClient != null) {
            oidcClient.close();
        }
        userStoreClient = null;
    }

    @AfterMethod(alwaysRun = true)
    public void clear() {

        cookieStore.clear();
    }

    /**
     * Test scenario: Ensure user1 can log in when password expiry is disabled.
     * Even though the password was last updated 100 days ago.
     */
    @Test(description = "Test user1 login with password expiry disabled.")
    public void testUser1LoginWhenExpiryDisabled() throws Exception {

        // Set last password update time to 100 days ago.
        setLastPasswordUpdateTime(TEST_USER1_USERNAME, 100);
        setPasswordExpirationPolicy(false, false, false);

        performLoginAndAssert(TEST_USER1_USERNAME, TEST_USER_PASSWORD, false, false);
    }

    /**
     * Test scenario: If password expiry is enabled but no rules are set, the default behavior is "apply".
     * user1's password is expired since last update > default expiry.
     */
    @Test(description = "Test user1 login with password expiry enabled, no rules, default = apply.")
    public void testUser1LoginWithNoRulesDefaultApply() throws Exception {

        // Set last password update time to 100 days ago.
        setLastPasswordUpdateTime(TEST_USER1_USERNAME, 100);
        setPasswordExpirationPolicy(true, false, true);

        performLoginAndAssert(TEST_USER1_USERNAME, TEST_USER_PASSWORD, true, false);
    }

    /**
     * Test scenario: If password expiry is enabled but no rules are set, the default behavior is "skip".
     * user1 can still login if last password update is old.
     */
    @Test(description = "Test user1 login with password expiry enabled, no rules, default = skip.")
    public void testUser1LoginWithNoRulesDefaultSkip() throws Exception {

        // Set last password update time to 100 days ago.
        setLastPasswordUpdateTime(TEST_USER1_USERNAME, 100);
        setPasswordExpirationPolicy(true, true, true);

        performLoginAndAssert(TEST_USER1_USERNAME, TEST_USER_PASSWORD, false, false);
    }

    /**
     * Test scenario: user1 is subject to rule2 => password expiry after 20 days, default = skip.
     * user1 with 25 days old password => expired.
     */
    @Test(priority = 99, description = "Test user1 login with password expiry enabled, older than 20 days => expired.")
    public void testUser1LoginExpiredPasswordRule2() throws Exception {

        // Set last password update time to 25 days ago.
        setLastPasswordUpdateTime(TEST_USER1_USERNAME, 25);
        setPasswordExpirationPolicy(true, true, false);

        // Expect expired and reset password.
        performLoginAndAssert(TEST_USER1_USERNAME, TEST_USER_PASSWORD, true, true);

        // Try to log in with the new password.
        cookieStore.clear();
        performLoginAndAssert(TEST_USER1_USERNAME, TEST_USER_NEW_PASSWORD, false, false);
    }

    /**
     * Perform login and assert the result.
     *
     * @param userName        Username to log in with.
     * @param password        Password to log in with.
     * @param expectedExpired Whether the password is expected to be expired.
     * @param resetPassword   Whether to reset the password.
     * @throws Exception If an error occurs.
     */
    private void performLoginAndAssert(String userName, String password, boolean expectedExpired,
                                       boolean resetPassword) throws Exception {

        String loginPageURL = getLoginPageURL();
        HttpResponse response = sendGetRequest(oidcClient, loginPageURL);
        System.out.println("DEBUG BODY login get: " + response);
        String sessionDataKey = extractSessionDataKey(response);

        response = sendLoginPostForCustomUsers(oidcClient, sessionDataKey, userName, password);
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Location header expected post login is not available.");
        EntityUtils.consume(response.getEntity());

        if (expectedExpired) {
            System.out.println("DEBUG BODY after expired login attempt: " + response);
            EntityUtils.consume(response.getEntity());
            String expiredRedirect = locationHeader.getValue();
            Assert.assertTrue(expiredRedirect.contains("passwordExpired=true"),
                    "Password not flagged as expired in redirect?");
            if (!resetPassword) {
                return;
            }

            HttpResponse resetConfirmationPage = sendGetRequest(oidcClient, expiredRedirect);
            HttpResponse finalResetResponse = resetExpiredPassword(resetConfirmationPage, TEST_USER_NEW_PASSWORD);

            String body = EntityUtils.toString(finalResetResponse.getEntity(), StandardCharsets.UTF_8);
            Assert.assertTrue(body.contains("Password Reset Successfully"),
                    "Password reset confirmation message not found.");
            return;
        }

        response = sendGetRequest(oidcClient, locationHeader.getValue());
        System.out.println("Second GET body: " + EntityUtils.toString(response.getEntity())); // Debug
        System.out.println("Second GET status: " + response.getStatusLine());

        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Redirection URL to the application with authorization code is null.");
        EntityUtils.consume(response.getEntity());

        String authorizationCode = getAuthorizationCodeFromURL(locationHeader.getValue());
        Assert.assertNotNull(authorizationCode, "Authorization code not found in the response.");
    }

    /**
     * Get the OIDC login page URL by sending a request to the authorize endpoint.
     *
     * @return The URL of the login page
     * @throws Exception If an error occurs during the request
     */
    private String getLoginPageURL() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", oidcApplication.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", oidcApplication.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(oidcClient, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader,
                "Location header expected for authorize request is not available.");
        EntityUtils.consume(response.getEntity());
        return locationHeader.getValue();
    }

    /**
     * Extract the session data key from the login page response.
     *
     * @param response The HTTP response containing the login page.
     * @return The extracted session data key.
     * @throws Exception If an error occurs during extraction
     */
    private String extractSessionDataKey(HttpResponse response) throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKey key value is null.");

        String sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
        return sessionDataKey;
    }

    /**
     * Get the authorization code from the URL.
     *
     * @param location The URL containing the authorization code.
     * @return The extracted authorization code.
     */
    private String getAuthorizationCodeFromURL(String location) {

        URI uri = URI.create(location);
        return URLEncodedUtils.parse(uri, StandardCharsets.UTF_8).stream()
                .filter(param -> "code".equals(param.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Submits the password reset form with a new password.
     *
     * @param resetPageResponse The HTTP response containing the password reset form (password-reset.jsp).
     * @param newPassword       The new password to set.
     * @return The final HttpResponse after submitting the form.
     * @throws Exception        If parsing or submission fails.
     */
    private HttpResponse resetExpiredPassword(HttpResponse resetPageResponse, String newPassword) throws Exception {

        String responseContent = EntityUtils.toString(resetPageResponse.getEntity(), StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(responseContent);

        Element passwordResetForm = doc.selectFirst("form#passwordResetForm");
        Assert.assertNotNull(passwordResetForm, "Password reset form not found on page.");

        String formAction = passwordResetForm.attr("action");
        // If formAction is a relative path like "passwordreset.do", build the absolute URL.
        // e.g. https://localhost:9853/accountrecoveryendpoint/passwordreset.do
        String fullFormAction = resolveURL(formAction);

        List<NameValuePair> resetParams = new ArrayList<>();
        for (Element input : passwordResetForm.select("input[type=hidden]")) {
            String name = input.attr("name");
            String value = input.attr("value");
            resetParams.add(new BasicNameValuePair(name, value));
        }

        resetParams.add(new BasicNameValuePair("reset-password", newPassword));
        resetParams.add(new BasicNameValuePair("reset-password2", newPassword));
        return sendPostRequestWithParameters(oidcClient, resetParams, fullFormAction);
    }

    /**
     * Resolve a relative form action against the base URL from the original response.
     * If the form action is already absolute, just return it as is.
     */
    private String resolveURL(String formAction) {

        // If it starts with http(s), it's absolute.
        if (formAction.startsWith("http")) {
            return formAction;
        }
        // Otherwise, parse the original response's request URL.
        return serverURL + "/accountrecoveryendpoint/" + formAction;
    }

    private void initOIDCClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        oidcClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore)
                .disableRedirectHandling()
                .build();
    }

    private OIDCApplication initApplication() {

        OIDCApplication playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        return playgroundApp;
    }
    
    private void initUsers() throws Exception {

        user1Id = addUser(TEST_USER1_USERNAME);
        Assert.assertNotNull(user1Id, "Failed to create user1");

        user2Id = addUser(TEST_USER2_USERNAME);
        Assert.assertNotNull(user2Id, "Failed to create user2");

        user3Id = addUser(TEST_USER3_USERNAME);
        Assert.assertNotNull(user3Id, "Failed to create user3");

        user4Id = addUser(TEST_USER4_USERNAME);
        Assert.assertNotNull(user4Id, "Failed to create user4");
    }

    private String addUser(String userName) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(userName);
        user.setPassword(TEST_USER_PASSWORD);
        user.setName(new Name().givenName(userName));
        user.addEmail(new Email().value(userName + "@example.com"));
        return scim2RestClient.createUser(user);
    }

    private void initRoles() throws IOException {

        // Create roles.
        RoleV2 role1 = new RoleV2(null, TEST_ROLE1, Collections.emptyList(), Collections.emptyList());
        role1Id = scim2RestClient.addV2Role(role1);
        Assert.assertNotNull(role1Id, "Failed to create role1");

        RoleV2 role2 = new RoleV2(null, TEST_ROLE2, Collections.emptyList(), Collections.emptyList());
        role2Id = scim2RestClient.addV2Role(role2);
        Assert.assertNotNull(role2Id, "Failed to create role2");

        // Assign user1 & user2 -> role1.
        RoleItemAddGroupobj role1PatchReqObject = new RoleItemAddGroupobj();
        role1PatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        role1PatchReqObject.setPath(USERS_PATH);
        role1PatchReqObject.addValue(new ListObject().value(user1Id));
        scim2RestClient.updateUsersOfRoleV2(role1Id,
                new PatchOperationRequestObject().addOperations(role1PatchReqObject));

        role1PatchReqObject.addValue(new ListObject().value(user2Id));
        scim2RestClient.updateUsersOfRoleV2(role1Id,
                new PatchOperationRequestObject().addOperations(role1PatchReqObject));

        // Assign user2 -> role2.
        RoleItemAddGroupobj role2PatchReqObject = new RoleItemAddGroupobj();
        role2PatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        role2PatchReqObject.setPath(USERS_PATH);
        role2PatchReqObject.addValue(new ListObject().value(user2Id));
        scim2RestClient.updateUsersOfRoleV2(role2Id,
                new PatchOperationRequestObject().addOperations(role2PatchReqObject));
    }

    private void initGroups() throws Exception {

        // Create group1.
        group1Id = scim2RestClient.createGroup(
                new GroupRequestObject()
                        .displayName(TEST_GROUP1)
                        .addMember(new MemberItem().value(user3Id))
                        .addMember(new MemberItem().value(user4Id)));
        Assert.assertNotNull(group1Id, "Failed to create group1");

        // Create group2.
        group2Id = scim2RestClient.createGroup(
                new GroupRequestObject()
                        .displayName(TEST_GROUP2)
                        .addMember(new MemberItem().value(user4Id)));
        Assert.assertNotNull(group2Id, "Failed to create group2");
    }

    /**
     * Update password expiration policy with rules.
     *
     * @param enabled Whether password expiration is enabled.
     * @param skipIfNoApplicableRules Whether to skip expiration if no rules apply.
     * @param resetRules Whether to skip adding rules.
     * @throws Exception If an error occurs.
     */
    private void setPasswordExpirationPolicy(boolean enabled, boolean skipIfNoApplicableRules, boolean resetRules)
            throws Exception {

        ConnectorsPatchReq patchReq = new ConnectorsPatchReq();
        patchReq.setOperation(ConnectorsPatchReq.OperationEnum.UPDATE);
        
        // Enable/disable password expiration
        PropertyReq enableProperty = new PropertyReq();
        enableProperty.setName(PASSWORD_EXPIRY_ENABLED);
        enableProperty.setValue(String.valueOf(enabled));
        patchReq.addProperties(enableProperty);
        
        // Set default expiry time in days.
        PropertyReq expiryDaysProperty = new PropertyReq();
        expiryDaysProperty.setName(PASSWORD_EXPIRY_TIME);
        expiryDaysProperty.setValue(String.valueOf(DEFAULT_EXPIRY_TIME));
        patchReq.addProperties(expiryDaysProperty);
        
        // Set skip if no applicable rules.
        PropertyReq skipProperty = new PropertyReq();
        skipProperty.setName(PASSWORD_EXPIRY_SKIP_IF_NO_APPLICABLE_RULES);
        skipProperty.setValue(String.valueOf(skipIfNoApplicableRules));
        patchReq.addProperties(skipProperty);
        
        // Rule 1: Skip password expiration for users in role1 and role2.
        PropertyReq rule1Property = new PropertyReq();
        rule1Property.setName(PASSWORD_EXPIRY_RULE1);
        rule1Property.setValue(resetRules ? StringUtils.EMPTY : String.format("1,0,roles,ne,%s,%s", role1Id, role2Id));
        patchReq.addProperties(rule1Property);
        
        // Rule 2: Apply password expiration for 20 days for users in role1.
        PropertyReq rule2Property = new PropertyReq();
        rule2Property.setName(PASSWORD_EXPIRY_RULE2);
        rule2Property.setValue(resetRules ? StringUtils.EMPTY : String.format("2,20,roles,eq,%s", role1Id));
        patchReq.addProperties(rule2Property);
        
        // Rule 3: Skip password expiration for users in group1 and group2.
        PropertyReq rule3Property = new PropertyReq();
        rule3Property.setName(PASSWORD_EXPIRY_RULE3);
        rule3Property.setValue(resetRules ? StringUtils.EMPTY : String.format("3,0,groups,ne,%s,%s", group1Id, group2Id));
        patchReq.addProperties(rule3Property);
        
        // Rule 4: Apply password expiration for 10 days for users in group1.
        PropertyReq rule4Property = new PropertyReq();
        rule4Property.setName(PASSWORD_EXPIRY_RULE4);
        rule4Property.setValue(resetRules ? StringUtils.EMPTY : String.format("4,10,groups,eq,%s", group1Id));
        patchReq.addProperties(rule4Property);

        identityGovernanceRestClient.updateConnectors(PASSWORD_EXPIRY_CATEGORY_ID, PASSWORD_EXPIRY_CONNECTOR_ID,
                patchReq);
        Thread.sleep(5000);
    }
    
    /**
     * Set the last password update time for a user to test password expiry.
     * 
     * @param username Username of the user.
     * @param daysInPast Number of days in the past to set the last password update time.
     * @throws Exception If an error occurs during the update.
     */
    private void setLastPasswordUpdateTime(String username, int daysInPast) throws Exception {
        
        // Calculate timestamp in milliseconds.
        long pastTimestamp = System.currentTimeMillis() - (daysInPast * 24L * 60L * 60L * 1000L);

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI(LAST_PASSWORD_UPDATE_CLAIM);
        claimValue.setValue(String.valueOf(pastTimestamp));
        ClaimValue[] claimValues = new ClaimValue[]{claimValue};

        // Update claim value through userStoreClient since last password update is read-only in SCIM2.
        userStoreClient.setUserClaimValues(username, claimValues, UserCoreConstants.DEFAULT_PROFILE);
    }
}
