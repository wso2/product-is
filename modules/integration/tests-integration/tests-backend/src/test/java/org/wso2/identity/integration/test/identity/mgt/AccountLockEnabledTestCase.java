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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.rmi.RemoteException;

public class AccountLockEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockEnabledTestCase.class.getName());

    private String defaultLocalityClaimUri = "http://wso2.org/claims/locality";
    private String accountLockClaimUri = "http://wso2.org/claims/identity/accountLocked";
    private String defaultLocalityClaimValue = "en_US";
    private String registryResourcePath = "/_system/config/identity/Email/accountlock/";

    private String testLockUser1 = "TestLockUser1";
    private String testLockUser1Password = "TestLockUser1Password";
    private String testLockUser1WrongPassword = "TestLockUser1WrongPassword";
    private String testLockUser2 = "TestLockUser2";
    private String testLockUser2Password = "TestLockUser2Password";

    private String accountLockTemplate = "accountlock";

    private AuthenticatorClient authenticatorClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RemoteUserStoreManagerServiceClient usmClient;
    private IdentityGovernanceServiceClient identityGovernanceServiceClient;

    private static final String ENABLE_ACCOUNT_LOCK = "account.lock.handler.enable";
    private static final String ENABLE_MAX_ACCOUNT_LOCK_COUNT = "account.lock.handler.On.Failure.Max.Attempts";
    private static final String MAX_ACCOUNT_LOCK_COUNT = "5";
    private static final String TRUE_STRING = "true";
    private static final String DEFAULT = "default";

    private String testLockUser3 = "TestLockUser3";
    private String testLockUser3Password = "TestLockUser3Password";
    private String testLockUser3WrongPassword = "TestLockUser3WrongPassword";
    private String testLockUser4 = "TestLockUser4";
    private String testLockUser4Password = "TestLockUser4Password";
    private String testLockUser4WrongPassword = "TestLockUser4WrongPassword";
    private static final String ACCOUNT_LOCK_BYPASS_ROLE = "Internal/system";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        authenticatorClient = new AuthenticatorClient(backendURL);
        enableAccountLocking(ENABLE_ACCOUNT_LOCK);
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
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

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock email " +
            "template successfully retrieved ", dependsOnMethods = "testSuccessfulLockedInitially")
    public void testSuccessfulEmailRetrieval() {

        try {
            ClaimValue claimValue = new ClaimValue();
            claimValue.setClaimURI(defaultLocalityClaimUri);
            claimValue.setValue(defaultLocalityClaimValue);
            ClaimValue[] claimvalues = {claimValue};
            usmClient.addUser(testLockUser2, testLockUser2Password, new String[]{"admin"}, claimvalues, null, false);

            String userLocale = usmClient.
                    getUserClaimValue(testLockUser2, defaultLocalityClaimUri, "default");

            String emailTemplateResourceName = accountLockTemplate + "." + userLocale;
            String emailTemplateResourceContent = resourceAdminServiceClient.
                    getTextContent(registryResourcePath + emailTemplateResourceName);
            Assert.assertTrue("Test Failure : Email Content applicable for " +
                    "Account lock is not available ", StringUtils.isNotEmpty(emailTemplateResourceContent));

        } catch (Exception e) {
            log.error("Error occurred when retrieving the Account lock email template.", e);
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        usmClient.deleteRole(ACCOUNT_LOCK_BYPASS_ROLE);
        usmClient.deleteUser(testLockUser1);
        usmClient.deleteUser(testLockUser2);
        usmClient.deleteUser(testLockUser3);
        usmClient.deleteUser(testLockUser4);
        disableAccountLocking(ENABLE_ACCOUNT_LOCK);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user bypasses account locking successfully")
    public void testLockByPassUnLockedAccount() throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException, UserStoreException, LoginAuthenticationExceptionException {

        usmClient.addUser(testLockUser3, testLockUser3Password, new String[]{"admin"}, new ClaimValue[0], null,
         false);
        usmClient.updateRoleListOfUser(testLockUser3, new String[]{"admin"}, new String[]{"admin",
                ACCOUNT_LOCK_BYPASS_ROLE});

        int maximumAllowedFailedLogins = Integer.parseInt(MAX_ACCOUNT_LOCK_COUNT);
        for (int i = 0; i < maximumAllowedFailedLogins; i++) {

            try {
                authenticatorClient.login(testLockUser3, testLockUser3WrongPassword, "localhost");
            } catch (LoginAuthenticationExceptionException e) {
                log.error("Login attempt: " + i + " for user: " + testLockUser3 + " failed");
            }
        }

        authenticatorClient.login(testLockUser3, testLockUser3Password, "localhost");

        ClaimValue[] claimValues = usmClient.getUserClaimValuesForClaims(testLockUser3, new String[]
                {accountLockClaimUri}, "default");

        String userAccountLockClaimValue = null;

        if (ArrayUtils.isNotEmpty(claimValues)) {
            userAccountLockClaimValue = claimValues[0].getValue();
        }

        Assert.assertFalse
                ("Lock claim should always be false for a privilege user ", Boolean.valueOf(userAccountLockClaimValue));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "For a locked account, after attaching the account lock bypass role check" +
            "whether the account gets access to the system")
    public void testLockByPassLockedAccount() throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException, UserStoreException,
            LoginAuthenticationExceptionException {

        usmClient.addUser(testLockUser4, testLockUser4Password, new String[]{"admin"}, new ClaimValue[0], null,
                false);
        int maximumAllowedFailedLogins = Integer.parseInt(MAX_ACCOUNT_LOCK_COUNT);
        for (int i = 0; i < maximumAllowedFailedLogins; i++) {
            try {
                authenticatorClient.login(testLockUser4, testLockUser4WrongPassword, "localhost");
            } catch (LoginAuthenticationExceptionException e) {
                log.error("Login attempt: " + i + " for user: " + testLockUser4 + " failed");
            }
        }

        ClaimValue[] claimValues = usmClient.getUserClaimValuesForClaims(testLockUser4, new String[]
                {accountLockClaimUri}, "default");

        String userAccountLockClaimValue = null;

        if (ArrayUtils.isNotEmpty(claimValues)) {
            userAccountLockClaimValue = claimValues[0].getValue();
        }

        Assert.assertTrue
                ("User must be locked after unsuccessful attempts exceed configured no of unsuccessful " +
                                "attempts",
                        Boolean.valueOf(userAccountLockClaimValue));

        usmClient.updateRoleListOfUser(testLockUser4, new String[]{"admin"}, new String[]{"admin",
                ACCOUNT_LOCK_BYPASS_ROLE});
        authenticatorClient.login(testLockUser4, testLockUser4Password, "localhost");

        claimValues = usmClient.getUserClaimValuesForClaims(testLockUser4, new String[]
                {accountLockClaimUri}, "default");

        userAccountLockClaimValue = null;

        if (ArrayUtils.isNotEmpty(claimValues)) {
            userAccountLockClaimValue = claimValues[0].getValue();
        }

        Assert.assertFalse("Lock Claim must be cleared after a successful login for a privilege user",
                Boolean.valueOf(userAccountLockClaimValue));
    }

    protected String getISResourceLocation() {

        return TestConfigurationProvider.getResourceLocation("IS");
    }

    protected void enableAccountLocking(String option) throws Exception {

        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);

        Thread.sleep(5000);
        authenticatorClient.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get(DEFAULT));

        Property[] newProperties = new Property[2];
        Property prop = new Property();
        prop.setName(option);
        prop.setValue(TRUE_STRING);
        newProperties[0] = prop;
        Property propMaxCount = new Property();
        propMaxCount.setName(ENABLE_MAX_ACCOUNT_LOCK_COUNT);
        propMaxCount.setValue(MAX_ACCOUNT_LOCK_COUNT);
        newProperties[1] = propMaxCount;
        identityGovernanceServiceClient.updateConfigurations(newProperties);
    }

    protected void disableAccountLocking(String option) throws Exception {

        Property[] newProperties = new Property[1];
        Property prop = new Property();
        prop.setName(option);
        prop.setValue("false");
        newProperties[0] = prop;
        identityGovernanceServiceClient.updateConfigurations(newProperties);
    }
}