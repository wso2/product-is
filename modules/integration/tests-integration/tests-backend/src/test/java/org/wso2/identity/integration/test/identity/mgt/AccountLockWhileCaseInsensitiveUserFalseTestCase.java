/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.identity.mgt;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.xpath.XPathExpressionException;

/**
 * This test class is to test the user account locking process while the caseInsensitiveUserName and
 * useCaseSensitiveUsernameForCacheKey properties are false in the primary user store.
 */
public class AccountLockWhileCaseInsensitiveUserFalseTestCase extends ISIntegrationTest {

    private static final String TEST_USER_1 = "testDemo";
    private static final String TEST_USER_2 = "TestDemo";
    private static final String TEST_USER_1_PASSWORD = "testDemoPass";
    private static final String ROLE_ADMIN = "admin";
    private static final String ACCOUNT_LOCK_CLAIM_URI = "http://wso2.org/claims/identity/accountLocked";
    private ServerConfigurationManager configurationManager;
    private RemoteUserStoreManagerServiceClient usmServiceClient;
    private AuthenticatorClient authClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        String carbonHome = Utils.getResidentCarbonHome();
        configureServerWithRestart(carbonHome);

        //Initiating after the restart.
        super.init();

        usmServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        createLockedUser(TEST_USER_1, new String[]{ROLE_ADMIN}, TEST_USER_1_PASSWORD);
        authClient = new AuthenticatorClient(backendURL);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        log.info("Deleting the user : " + TEST_USER_1 + ".");
        usmServiceClient.deleteUser(TEST_USER_1);

        log.info("Replacing the default configurations.");
        configurationManager.restoreToLastConfiguration(false);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user is locked under CaseInsensitiveUsername property " +
            "is false.", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testCaseInsensitiveUsernameFalseUserLocking() throws Exception {

        log.info("Login attempt to " + TEST_USER_1 + " user from " + TEST_USER_2 + " user.");
        authClient.login(TEST_USER_2, TEST_USER_1_PASSWORD, isServer.getInstance().getHosts().get("default"));
    }

    private void configureServerWithRestart(String carbonHome)
            throws AutomationUtilException, XPathExpressionException, IOException {

        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "identityMgt"
                + File.separator + "case_insensitive_user_false.toml");

        log.info("Applying configured toml file.");
        configurationManager = new ServerConfigurationManager(isServer);
        configurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        configurationManager.restartGracefully();
        log.info("Toml configurations applied.");
    }

    private void createLockedUser(String userName, String[] roles, String password) {

        log.info("Creating a locked user account.");
        ClaimValue[] claimValues = setAccountLockClaim();

        try {
            usmServiceClient.addUser(userName, password, roles, claimValues, null, false);
            log.info("Locked user account created.");
        } catch (RemoteException | RemoteUserStoreManagerServiceUserStoreExceptionException | UserStoreException e) {
            Assert.fail("Error occurred while creating the user.", e);
        }
    }

    private ClaimValue[] setAccountLockClaim() {

        ClaimValue[] claimValues = new ClaimValue[1];
        ClaimValue lockedClaim = new ClaimValue();
        lockedClaim.setClaimURI(ACCOUNT_LOCK_CLAIM_URI);
        lockedClaim.setValue(Boolean.TRUE.toString());
        claimValues[0] = lockedClaim;
        return claimValues;
    }
}
