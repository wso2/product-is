/*
 * Copyright (c) 2016-2025, WSO2 LLC. (https://www.wso2.com).
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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.AuthenticatorRestClient;
import org.wso2.identity.integration.test.restclients.EmailTemplatesRestClient;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

public class AccountLockEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockEnabledTestCase.class.getName());
    private static final String DEFAULT_LOCALITY_CLAIM_VALUE = "en_US";
    private static final String TEST_LOCK_USER_1 = "TestLockUser1";
    private static final String TEST_LOCK_USER_1_PASSWORD = "TestLockUser1@Password";
    private static final String TEST_LOCK_USER_1_WRONG_PASSWORD = "TestLockUser1Wrong@Password";
    private static final String TEST_LOCK_USER_2 = "TestLockUser2";
    private static final String TEST_LOCK_USER_2_PASSWORD = "TestLockUser2@Password";
    private static final String TEST_LOCK_USER_3 = "TestLockUser3";
    private static final String TEST_LOCK_USER_3_PASSWORD = "TestLockUser3@Password";

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
    private static final String SCIM_SYSTEM_USER_SCHEMA = "urn:scim:wso2:schema";


    private SCIM2RestClient scim2RestClient;
    private AuthenticatorRestClient authenticatorRestClient;
    private EmailTemplatesRestClient emailTemplatesRestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private ConnectorsPatchReq connectorPatchRequest;


    private String testLockUserId;
    private String testLockUser2Id;
    private String testLockUser3Id;

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        authenticatorRestClient = new AuthenticatorRestClient(serverURL);
        enableAccountLocking();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        emailTemplatesRestClient = new EmailTemplatesRestClient(serverURL, tenantInfo);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock successfully")
    public void testSuccessfulLockedInitially() {

        try {
            testLockUserId = addAdminUser(TEST_LOCK_USER_1, TEST_LOCK_USER_1_PASSWORD, null);

            int maximumAllowedFailedLogins = 5;
            for (int i = 0; i < maximumAllowedFailedLogins; i++) {
                JSONObject response = authenticatorRestClient.login(TEST_LOCK_USER_1, TEST_LOCK_USER_1_WRONG_PASSWORD);

                if (!response.containsKey("token")) {
                    log.error("Login attempt: " + i + " for user: " + TEST_LOCK_USER_1 + " failed");
                }
            }


            JSONObject userParameters = (JSONObject) scim2RestClient.getUser(testLockUserId, null)
                    .get(SCIM_SYSTEM_USER_SCHEMA);
            Assert.assertTrue((Boolean) userParameters.get(ACCOUNT_LOCK_ATTRIBUTE),
                    "Test Failure : User Account Didn't Locked Properly");
        } catch (Exception e) {
            log.error("Error occurred when locking the test user.", e);
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

        scim2RestClient.deleteUser(testLockUserId);
        scim2RestClient.deleteUser(testLockUser2Id);
        scim2RestClient.deleteUser(testLockUser3Id);
        disableAccountLocking();
        emailTemplatesRestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        authenticatorRestClient.closeHttpClient();
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
}
