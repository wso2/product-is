/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.identity.mgt;


import junit.framework.Assert;
import org.apache.commons.lang.ArrayUtils;
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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.restclients.EmailTemplatesRestClient;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;

public class AccountLockEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockEnabledTestCase.class.getName());

    private String defaultLocalityClaimUri = IdentityUtil.getClaimUriLocale();
    private String accountLockClaimUri = "http://wso2.org/claims/identity/accountLocked";
    private String defaultLocalityClaimValue = "en_us";
    private String registryResourcePath = "/_system/config/identity/email/";

    private String testLockUser1 = "TestLockUser1";
    private String testLockUser1Password = "TestLockUser1Password";
    private String testLockUser1WrongPassword = "TestLockUser1WrongPassword";
    private String testLockUser2 = "TestLockUser2";
    private String testLockUser2Password = "TestLockUser2Password";
    private String testLockUser3 = "TestLockUser3";
    private String testLockUser3Password = "TestLockUser3Password";

    private String accountLockTemplateWhenUserExceedsFailedAttempts = "accountlockfailedattempt";
    private String accountLockTemplateWhenAdminTriggered = "accountlockadmin";
    private String accountUnlockTemplateAdminTriggered = "accountunlockadmin";
    private String accountUnlockTemplateTimeBased = "accountunlocktimebased";

    private AuthenticatorClient authenticatorClient;
    private RemoteUserStoreManagerServiceClient usmClient;
    private EmailTemplatesRestClient emailTemplatesRestClient;
    private IdentityGovernanceRestClient identityGovernanceRestClient;
    private ConnectorsPatchReq connectorPatchRequest;

    private static final String ENABLE_ACCOUNT_LOCK = "account.lock.handler.lock.on.max.failed.attempts.enable";
    private static final String CATEGORY_LOGIN_ATTEMPTS_SECURITY = "TG9naW4gQXR0ZW1wdHMgU2VjdXJpdHk";
    private static final String CONNECTOR_ACCOUNT_LOCK_HANDLER = "YWNjb3VudC5sb2NrLmhhbmRsZXI";

    private static final String TRUE_STRING = "true";
    private static final String DEFAULT = "default";
    private static final String USER_LOCALE = "en_US";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        authenticatorClient = new AuthenticatorClient(backendURL);
        enableAccountLocking(ENABLE_ACCOUNT_LOCK);
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        emailTemplatesRestClient = new EmailTemplatesRestClient(backendURL.replace("services/",
                ""), tenantInfo);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock successfully")
    public void testSuccessfulLockedInitially() {
        try {
            usmClient.addUser(testLockUser1, testLockUser1Password, new String[]{"admin"}, new ClaimValue[0], null, false);

            int maximumAllowedFailedLogins = 5;
            for (int i = 0; i < maximumAllowedFailedLogins; i++) {
                try {
                    authenticatorClient.login(testLockUser1, testLockUser1WrongPassword, "localhost");
                } catch (Exception e) {
                    log.error("Login attempt: " + i + " for user: " + testLockUser1 + " failed");
                }
            }

            ClaimValue[] claimValues = usmClient.getUserClaimValuesForClaims(testLockUser1, new String[]
                    {accountLockClaimUri}, "default");

            String userAccountLockClaimValue = null;

            if (ArrayUtils.isNotEmpty(claimValues)) {
                userAccountLockClaimValue = claimValues[0].getValue();
            }

            Assert.assertTrue
                    ("Test Failure : User Account Didn't Locked Properly", Boolean.valueOf(userAccountLockClaimValue));

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

        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI(defaultLocalityClaimUri);
        claimValue.setValue(defaultLocalityClaimValue);
        ClaimValue[] claimvalues = { claimValue };
        usmClient.addUser(testLockUser2, testLockUser2Password, new String[] { "admin" }, claimvalues, null, false);

        JSONObject emailTemplateResourceContent =
                emailTemplatesRestClient.getEmailTemplate(accountLockTemplateWhenUserExceedsFailedAttempts,
                        USER_LOCALE);
        Assert.assertTrue("Test Failure : Email Content applicable for account lock is not available.",
                StringUtils.isNotEmpty((String) emailTemplateResourceContent.get("body")));

        JSONObject emailTemplateResourceContentAdminTriggered =
                emailTemplatesRestClient.getEmailTemplate(accountLockTemplateWhenAdminTriggered, USER_LOCALE);
        Assert.assertTrue("Test Failure : Email Content applicable for account lock is not available.",
                StringUtils.isNotEmpty((String) emailTemplateResourceContentAdminTriggered.get("body")));
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is",
          description = "Check whether the user account unlocklock email "
                  + "template successfully retrieved when admin triggered account lock.")
    public void testSuccessfulEmailTemplateRetrievalAccountUnLock() throws Exception {

            ClaimValue claimValue = new ClaimValue();
            claimValue.setClaimURI(defaultLocalityClaimUri);
            claimValue.setValue(defaultLocalityClaimValue);
            ClaimValue[] claimvalues = { claimValue };
            usmClient.addUser(testLockUser3, testLockUser3Password, new String[] { "admin" }, claimvalues, null, false);

            JSONObject emailTemplateResourceContent =
                    emailTemplatesRestClient.getEmailTemplate(accountUnlockTemplateTimeBased, USER_LOCALE);
            Assert.assertTrue("Test Failure : Email Content applicable for account unlock is not available.",
                    StringUtils.isNotEmpty((String) emailTemplateResourceContent.get("body")));

            JSONObject emailTemplateResourceContentAdminTriggered =
                    emailTemplatesRestClient.getEmailTemplate(accountUnlockTemplateAdminTriggered, USER_LOCALE);
            Assert.assertTrue("Test Failure : Email Content applicable for account unlock is not available.",
                    StringUtils.isNotEmpty((String) emailTemplateResourceContentAdminTriggered.get("body")));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        usmClient.deleteUser(testLockUser1);
        usmClient.deleteUser(testLockUser2);
        disableAccountLocking(ENABLE_ACCOUNT_LOCK);
        emailTemplatesRestClient.closeHttpClient();
        identityGovernanceRestClient.closeHttpClient();
    }

    protected String getISResourceLocation() {
        return TestConfigurationProvider.getResourceLocation("IS");
    }

    protected void enableAccountLocking(String option) throws Exception {
        identityGovernanceRestClient = new IdentityGovernanceRestClient(backendURL.replace("services/",
                ""), tenantInfo);

        Thread.sleep(5000);
        authenticatorClient.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get(DEFAULT));

        PropertyReq property = new PropertyReq();
        property.setName(option);
        property.setValue("true");

        connectorPatchRequest = new ConnectorsPatchReq();
        connectorPatchRequest.setOperation(OperationEnum.UPDATE);
        connectorPatchRequest.addProperties(property);

        identityGovernanceRestClient.updateConnectors(CATEGORY_LOGIN_ATTEMPTS_SECURITY , CONNECTOR_ACCOUNT_LOCK_HANDLER,
                connectorPatchRequest);
    }

    protected void disableAccountLocking(String option) throws Exception {
        connectorPatchRequest.getProperties().get(0).setValue("false");
        identityGovernanceRestClient.updateConnectors(CATEGORY_LOGIN_ATTEMPTS_SECURITY , CONNECTOR_ACCOUNT_LOCK_HANDLER,
                connectorPatchRequest);
    }

}
