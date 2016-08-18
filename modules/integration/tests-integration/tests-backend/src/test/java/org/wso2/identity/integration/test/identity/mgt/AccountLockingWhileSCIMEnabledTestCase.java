/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.ClaimManagementServiceClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.UserInformationRecoveryServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class AccountLockingWhileSCIMEnabledTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountLockingWhileSCIMEnabledTestCase.class.getName());
    private UserInformationRecoveryServiceClient infoRecoveryClient;
    private UserManagementClient userMgtClient;
    private UserProfileMgtServiceClient profileClient;
    private ClaimManagementServiceClient claimMgtClient;
    private AuthenticatorClient loginManger;
    private ServerConfigurationManager scm;
    private File identityMgtServerFile;
    private File userMgtServerFile;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    private static final String PROFILE_NAME = "default";
    private static final String SCIM_ACTIVE_URI = "urn:scim:schemas:core:1.0:active";
    private static final String TEST_USER_USERNAME = "lockUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String TEST_ROLE = "testRole";
    private static final String DISABLED_CLAIM = "http://wso2.org/claims/identity/accountDisabled";


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        String carbonHome = CarbonUtils.getCarbonHome();
        identityMgtServerFile = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator
                + "identity" + File.separator + "identity-mgt.properties");
        File identityMgtConfigFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "identity-mgt-account-lock-enabled.properties");

        userMgtServerFile = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator
                + "user-mgt.xml");
        File scimEnabledUserMgtCofigFile = new File(getISResourceLocation()
                + File.separator + "userMgt" + File.separator
                + "scim-enabled-user-mgt.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(identityMgtConfigFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(scimEnabledUserMgtCofigFile, userMgtServerFile, true);
        scm.restartGracefully();

        super.init();

        loginManger = new AuthenticatorClient(backendURL);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        infoRecoveryClient = new UserInformationRecoveryServiceClient(backendURL, sessionCookie);
        profileClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);

        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        claimMgtClient = new ClaimManagementServiceClient(backendURL, sessionCookie);

        ClaimValue[] claimValues = new ClaimValue[1];
        // Need to add this claim and have the value true in order to test the fix
        ClaimValue scimActiveClaim = new ClaimValue();
        scimActiveClaim.setClaimURI(SCIM_ACTIVE_URI);
        scimActiveClaim.setValue("true");
        claimValues[0] = scimActiveClaim;


        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null,
                claimValues, PROFILE_NAME, false);
        userMgtClient.addRole(TEST_ROLE, new String[]{TEST_USER_USERNAME}, new String[]{"/permission/admin/login"}, false);

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user can log successfully at the start")
    public void testSuccessfulLoginInitially() throws Exception {

        loginManger.login(TEST_USER_USERNAME, TEST_USER_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user cannot login when account is disabled",
    dependsOnMethods = "testSuccessfulLoginInitially", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testUnsuccessfulLoginWithAccountDisabled() throws Exception {
        remoteUSMServiceClient.setUserClaimValue(TEST_USER_USERNAME, DISABLED_CLAIM, "true", null);
        loginManger.login(TEST_USER_USERNAME, TEST_USER_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user can login when account is enabled again",
            dependsOnMethods = "testUnsuccessfulLoginWithAccountDisabled", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testSuccessfulLoginWithAccountEnabled() throws Exception {
        remoteUSMServiceClient.setUserClaimValue(TEST_USER_USERNAME, DISABLED_CLAIM, "false", null);
        loginManger.login(TEST_USER_USERNAME, TEST_USER_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check user account verification",
            dependsOnMethods = "testSuccessfulLoginWithAccountEnabled", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testUnsuccessfulFirstLogin() throws Exception {
        loginManger.login(TEST_USER_USERNAME, WRONG_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check user account verification",
            dependsOnMethods = "testUnsuccessfulFirstLogin", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testUnsuccessfulSecondLogin() throws Exception {
        loginManger.login(TEST_USER_USERNAME, WRONG_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check user account verification",
            dependsOnMethods = "testUnsuccessfulSecondLogin", expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testAccountLock() throws Exception {
        loginManger.login(TEST_USER_USERNAME, TEST_USER_PASSWORD, isServer.getInstance().getHosts().get("default"));
        loginManger.logOut();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        loginManger.logOut();
        loginManger.logOut();
        if(nameExists(userMgtClient.listAllUsers(TEST_USER_USERNAME, 100), TEST_USER_USERNAME)) {
            userMgtClient.deleteUser(TEST_USER_USERNAME);
        }
        if(nameExists(userMgtClient.listRoles(TEST_ROLE, 100), "TEST_ROLE")){
            userMgtClient.deleteRole("TEST_ROLE");
        }



        File identityMgtDefaultFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "identity-mgt-default.properties");
        File axisConfigDefaultFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "axis2-default.xml");
        File userMgtDefaultFile = new File(getISResourceLocation()
                + File.separator + "userMgt" + File.separator
                + "default-user-mgt.xml");
        scm.applyConfigurationWithoutRestart(identityMgtDefaultFile, identityMgtServerFile, true);
        // scm.applyConfigurationWithoutRestart(axisConfigDefaultFile, axisServerFile, true);
        scm.applyConfigurationWithoutRestart(userMgtDefaultFile, userMgtServerFile, true);
        scm.restartGracefully();

    }

    protected boolean nameExists(FlaggedName[] allNames, String inputName) {
        boolean exists = false;

        for (FlaggedName flaggedName : allNames) {
            String name = flaggedName.getItemName();

            if (name.equals(inputName)) {
                exists = true;
                break;
            } else {
                exists = false;
            }
        }

        return exists;
    }
}
