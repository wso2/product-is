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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class AccountLockEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockEnabledTestCase.class.getName());

    private ServerConfigurationManager scm;
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
    private File identityXmlServerFile;

    private AuthenticatorClient authenticatorClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RemoteUserStoreManagerServiceClient usmClient;

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        authenticatorClient = new AuthenticatorClient(backendURL);

        String carbonHome = CarbonUtils.getCarbonHome();
        identityXmlServerFile = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator
                + "identity" + File.separator + "identity.xml");
        File identityMgtConfigFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "identity-accountlock-enabled.xml");
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(identityMgtConfigFile, identityXmlServerFile, true);
        scm.restartGracefully();
        super.init();
        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock successfully")
    public void testSuccessfulLockedInitially() {
        try {
            usmClient.addUser(testLockUser1, testLockUser1Password, new String[]{"admin"}, new ClaimValue[0], null, false);

            try {
                authenticatorClient.login(testLockUser1, testLockUser1WrongPassword, "localhost");
            } catch (Exception e) {
                log.info("1 st unsuccessful Login attempt by " + testLockUser1);
            }
            try {
                authenticatorClient.login(testLockUser1, testLockUser1WrongPassword, "localhost");
            } catch (Exception e) {
                log.info("2 nd unsuccessful Login attempt by " + testLockUser1);
            }
            String userAccountLockClaimValue =
                    usmClient.getUserClaimValue(testLockUser1, accountLockClaimUri, "default");
            Assert.assertTrue
                    ("Test Failure : User Account Didn't Locked Properly", Boolean.valueOf(userAccountLockClaimValue));

        } catch (Exception e) {
            log.error("Error occurred when locking the test user.", e);
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user account lock email " +
            "template successfully retrieved ")
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

        File identityMgtConfigFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "identity-accountlock-enabled.xml");

        scm.applyConfigurationWithoutRestart(identityMgtConfigFile, identityXmlServerFile, true);
        scm.restartGracefully();

    }

    protected String getISResourceLocation() {
        return TestConfigurationProvider.getResourceLocation("IS");
    }

}
