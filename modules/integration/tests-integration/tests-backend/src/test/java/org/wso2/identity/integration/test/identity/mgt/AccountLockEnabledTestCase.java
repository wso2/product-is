/*
 * Copyright (c) 2016, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.identity.mgt;


import org.testng.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.AuthenticatorRestClient;
import org.wso2.identity.integration.test.restclients.EmailTemplatesRestClient;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;

import java.io.File;
import java.io.IOException;

public class AccountLockEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockEnabledTestCase.class.getName());
    private static final String DEFAULT_LOCALITY_CLAIM_VALUE = "en_US";
    private static final String TEST_LOCK_USER_1 = "TestLockUser1";
    private static final String TEST_LOCK_USER_1_PASSWORD = "TestLockUser1Password";
    private static final String TEST_LOCK_USER_1_WRONG_PASSWORD = "TestLockUser1WrongPassword";
    private static final String TEST_LOCK_USER_2 = "TestLockUser2";
    private static final String TEST_LOCK_USER_2_PASSWORD = "TestLockUser2Password";
    private static final String TEST_LOCK_USER_3 = "TestLockUser3";
    private static final String TEST_LOCK_USER_3_PASSWORD = "TestLockUser3Password";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";
    private static final String USER_STORE_TYPE = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String TEST_LOCK_USER_SECONDARY = "TestLockUserSecondary";
    private static final String TEST_LOCK_USER_SECONDARY_PASSWORD = "TestLockUserSecondaryPassword";
    private static final String TEST_LOCK_USER_SECONDARY_WRONG_PASSWORD = "TestLockUserSecondaryWrongPassword";
    private static final String PRIMARY_USER_ROLE = "PRIMARY_USER_ROLE";
    private static final String SECONDARY_USER_ROLE = "SECONDARY_USER_ROLE";
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private static final String ACCOUNT_LOCK_TEMPLATE_WHEN_USER_EXCEEDS_FAILED_ATTEMPTS = "accountlockfailedattempt";
    private static final String ACCOUNT_LOCK_TEMPLATE_WHEN_ADMIN_TRIGGERED = "accountlockadmin";
    private static final String ACCOUNT_UNLOCK_TEMPLATE_ADMIN_TRIGGERED = "accountunlockadmin";
    private static final String ACCOUNT_UNLOCK_TEMPLATE_TIME_BASED = "accountunlocktimebased";
    private static final String ACCOUNT_LOCK_ATTRIBUTE = "accountLocked";
    private static final String ENABLE_ACCOUNT_LOCK = "account.lock.handler.lock.on.max.failed.attempts.enable";
    private static final String CATEGORY_LOGIN_ATTEMPTS_SECURITY = "TG9naW4gQXR0ZW1wdHMgU2VjdXJpdHk";
    private static final String CONNECTOR_ACCOUNT_LOCK_HANDLER = "YWNjb3VudC5sb2NrLmhhbmRsZXI";
    private static final String LOCALE_ATTRIBUTE = "locale";
    private static final String USERS_PATH = "users";
    private static final String USER_SCHEMA = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";


    private SCIM2RestClient scim2RestClient;
    private AuthenticatorRestClient authenticatorRestClient;
    private EmailTemplatesRestClient emailTemplatesRestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private ConnectorsPatchReq connectorPatchRequest;
    private UserStoreMgtRestClient userStoreMgtRestClient;

    private String testLockUserId;
    private String testLockUser2Id;
    private String testLockUser3Id;
    private String testLockRoleId;
    private String userStoreId;

    @DataProvider(name = "userDetailsProvider")
    public Object[][] getUserDetails() {
        return new Object[][]{
                {TEST_LOCK_USER_1, TEST_LOCK_USER_1_PASSWORD, TEST_LOCK_USER_1_WRONG_PASSWORD, false},
                {TEST_LOCK_USER_SECONDARY, TEST_LOCK_USER_SECONDARY_PASSWORD, TEST_LOCK_USER_SECONDARY_WRONG_PASSWORD, true}
        };
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
        addSecondaryJDBCUserStore();
        authenticatorRestClient = new AuthenticatorRestClient(serverURL);
        enableAccountLocking();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        emailTemplatesRestClient = new EmailTemplatesRestClient(serverURL, tenantInfo);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock successfully", dataProvider = "userDetailsProvider")
    public void testSuccessfulLockedInitially(String username, String password, String wrongPassword,
                                              boolean isSecondaryUserStore) throws IOException {

        try {
            addUserIntoJDBCUserStore(username, password, isSecondaryUserStore);

            int maximumAllowedFailedLogins = 5;
            for (int i = 0; i < maximumAllowedFailedLogins - 1; i++) {
                JSONObject response = authenticatorRestClient.login(username, wrongPassword);

                if (!response.containsKey("token")) {
                    log.error("Login attempt: " + (i + 1) + " for user: " + username + " failed");
                }
            }
            // Check whether the user is locked before the maximum allowed failed login attempts.
            JSONObject userParameters = (JSONObject) scim2RestClient.getUser(testLockUserId, null).get(USER_SCHEMA);
            Assert.assertFalse(Boolean.parseBoolean(String.valueOf(userParameters.get(ACCOUNT_LOCK_ATTRIBUTE))),
                    "Test Failure : User is Locked before the maximum allowed failed login attempts");

            // Check whether the user is locked after the maximum allowed failed login attempts.
            JSONObject response = authenticatorRestClient.login(username, wrongPassword);
            if (!response.containsKey("token")) {
                log.error(
                        "Login attempt: " + maximumAllowedFailedLogins + " for user: " + username + " failed");
            }
            userParameters = (JSONObject) scim2RestClient.getUser(testLockUserId, null).get(USER_SCHEMA);
            Assert.assertTrue(Boolean.parseBoolean(String.valueOf(userParameters.get(ACCOUNT_LOCK_ATTRIBUTE))),
                    "Test Failure : User Account Didn't Locked Properly");
        } catch (Exception e) {
            log.error("Error occurred when locking the test user.", e);
            Assert.fail("Error occurred when locking the test user.");
        } finally {
            scim2RestClient.deleteUser(testLockUserId);
            scim2RestClient.deleteRole(testLockRoleId);
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is",
          description = "Check whether the user account lock email "
                  + "template successfully retrieved when admin triggered account lock.",
          dependsOnMethods = "testSuccessfulLockedInitially")
    public void testSuccessfulEmailTemplateRetrieval() throws Exception {

        testLockUser2Id = addAdminUser(TEST_LOCK_USER_2, TEST_LOCK_USER_2_PASSWORD, DEFAULT_LOCALITY_CLAIM_VALUE);
        String locale = scim2RestClient.getUser(testLockUser2Id, null).get(LOCALE_ATTRIBUTE).toString();

        JSONObject emailTemplateResourceContent =
                emailTemplatesRestClient.getEmailTemplate(ACCOUNT_LOCK_TEMPLATE_WHEN_USER_EXCEEDS_FAILED_ATTEMPTS, locale);
        Assert.assertTrue(StringUtils.isNotEmpty((String) emailTemplateResourceContent.get("body")),
                "Test Failure : Email Content applicable for account lock is not available.");

        JSONObject emailTemplateResourceContentAdminTriggered =
                emailTemplatesRestClient.getEmailTemplate(ACCOUNT_LOCK_TEMPLATE_WHEN_ADMIN_TRIGGERED, locale);
        Assert.assertTrue(StringUtils.isNotEmpty((String) emailTemplateResourceContentAdminTriggered.get("body")),
                "Test Failure : Email Content applicable for account lock is not available.");
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is",
          description = "Check whether the user account unlocklock email "
                  + "template successfully retrieved when admin triggered account lock.")
    public void testSuccessfulEmailTemplateRetrievalAccountUnLock() throws Exception {

        testLockUser3Id = addAdminUser(TEST_LOCK_USER_3, TEST_LOCK_USER_3_PASSWORD, DEFAULT_LOCALITY_CLAIM_VALUE);
        String locale = scim2RestClient.getUser(testLockUser3Id, null).get(LOCALE_ATTRIBUTE).toString();

        JSONObject emailTemplateResourceContent =
                emailTemplatesRestClient.getEmailTemplate(ACCOUNT_UNLOCK_TEMPLATE_TIME_BASED, locale);
        Assert.assertTrue(StringUtils.isNotEmpty((String) emailTemplateResourceContent.get("body")),
                "Test Failure : Email Content applicable for account unlock is not available.");

        JSONObject emailTemplateResourceContentAdminTriggered =
                emailTemplatesRestClient.getEmailTemplate(ACCOUNT_UNLOCK_TEMPLATE_ADMIN_TRIGGERED, locale);
        Assert.assertTrue(StringUtils.isNotEmpty((String) emailTemplateResourceContentAdminTriggered.get("body")),
                    "Test Failure : Email Content applicable for account unlock is not available.");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        scim2RestClient.deleteUser(testLockUser2Id);
        scim2RestClient.deleteUser(testLockUser3Id);
        disableAccountLocking();
        emailTemplatesRestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        authenticatorRestClient.closeHttpClient();
        userStoreMgtRestClient.deleteUserStore(userStoreId);
    }

    protected String getISResourceLocation() {
        return TestConfigurationProvider.getResourceLocation("IS");
    }

    protected void enableAccountLocking() throws Exception {
        identityGovernanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

        PropertyReq property = new PropertyReq();
        property.setName(ENABLE_ACCOUNT_LOCK);
        property.setValue("true");

        connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        identityGovernanceRestClient.updateConnectors(CATEGORY_LOGIN_ATTEMPTS_SECURITY , CONNECTOR_ACCOUNT_LOCK_HANDLER,
                connectorPatchRequest);
    }

    protected void disableAccountLocking() throws Exception {
        connectorPatchRequest.getProperties().get(0).setValue("false");
        identityGovernanceRestClient.updateConnectors(CATEGORY_LOGIN_ATTEMPTS_SECURITY , CONNECTOR_ACCOUNT_LOCK_HANDLER,
                connectorPatchRequest);
    }

    private void addUserIntoJDBCUserStore(String username, String password, boolean isSecondaryStoreUser)
            throws Exception {

        if (isSecondaryStoreUser) {
            testLockUserId = scim2RestClient.createUser(new UserObject()
                    .userName(DOMAIN_ID + "/" + username)
                    .password(password));
            testLockRoleId = scim2RestClient.addRole(new RoleRequestObject()
                    .displayName(SECONDARY_USER_ROLE)
                    .addPermissions(PERMISSION_LOGIN)
                    .addUsers(new ListObject().value(testLockUserId)));
        } else {
            testLockUserId = scim2RestClient.createUser(new UserObject()
                    .userName(username)
                    .password(password));
            testLockRoleId = scim2RestClient.addRole(new RoleRequestObject()
                    .displayName(PRIMARY_USER_ROLE)
                    .addPermissions(PERMISSION_LOGIN)
                    .addUsers(new ListObject().value(testLockUserId)));
        }
    }

    protected String addAdminUser(String username, String password, String locale) throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(username);
        userInfo.setPassword(password);
        userInfo.setLocale(locale);

        String userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
        return userId;
    }

    private void addSecondaryJDBCUserStore() throws Exception {

        // Creating database.
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager.getCarbonHome()
                + "/repository/database/" + USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        // Register a secondary user store.
        UserStoreReq userStore = new UserStoreReq()
                .typeId(USER_STORE_TYPE)
                .name(DOMAIN_ID)
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("driverName")
                        .value("org.h2.Driver"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("url")
                        .value("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("userName")
                        .value(DB_USER_NAME))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("password")
                        .value(DB_USER_PASSWORD))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UsernameJavaRegEx")
                        .value("^[\\S]{5,30}$"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("Disabled")
                        .value("false"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("PasswordDigest")
                        .value("SHA-256"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("StoreSaltedPassword")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("SCIMEnabled")
                        .value("true"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("CountRetrieverClass")
                        .value("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever"))
                .addPropertiesItem(new UserStoreReq.Property()
                        .name("UserIDEnabled")
                        .value("true"));

        userStoreId = userStoreMgtRestClient.addUserStore(userStore);
        Thread.sleep(5000);
        boolean isSecondaryUserStoreDeployed = userStoreMgtRestClient.waitForUserStoreDeployment(DOMAIN_ID);
        Assert.assertTrue(isSecondaryUserStoreDeployed);
    }
}
