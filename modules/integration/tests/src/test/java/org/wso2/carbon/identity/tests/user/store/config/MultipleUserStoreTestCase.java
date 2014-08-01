/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.tests.user.store.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.identity.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.LoginLogoutUtil;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

public class MultipleUserStoreTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(MultipleUserStoreTestCase.class);
    private static final String TEST_SECONDARY_USER = "testSecondaryUser";
    private static final String SECONDARY_ROLE = "secondaryRole";
    private UserManagementClient userManagementClient;
    private String domain = "wso2999.org";
    private String newDomain = "wso2new.org";
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private String dbURL = "jdbc:h2:repository/database/WSO2CARBON_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000";
    private String driverName = "org.h2.Driver";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(0);
        userManagementClient =
                new UserManagementClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        userStoreConfigurationClient =
                new UserStoreConfigAdminServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Add new user store")
    public void testAddNewUserStore() throws Exception {
        addJDBCUserStore(dbURL, driverName, "wso2carbon", "wso2carbon", false, "testUserStore", domain);
        assertTrue("User store not deployed within expected time interval",
                waitForUserStoreDeployment(domain));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Add a new user and new Role to store",
            dependsOnMethods = "testAddNewUserStore")
    public void testNewUserLogin() throws Exception {
        userManagementClient.addUser(domain + "/" + TEST_SECONDARY_USER, TEST_SECONDARY_USER, null, null);
        userManagementClient.addRole(domain + "/" + SECONDARY_ROLE, new String[]{domain + "/" + TEST_SECONDARY_USER},
                new String[]{"/permission/admin/login"});

        LoginLogoutUtil loginLogoutUtil =
                new LoginLogoutUtil(Integer.parseInt(isServer.getProductVariables().getHttpsPort()),
                        isServer.getProductVariables().getHostName());

        assertNotNull("User not logged in",
                loginLogoutUtil.login(domain + "/" + TEST_SECONDARY_USER, TEST_SECONDARY_USER,
                        isServer.getBackEndUrl()));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Disable user store", dependsOnMethods = "testNewUserLogin",
            expectedExceptions = LoginAuthenticationExceptionException.class)
    public void testDisableUserStore() throws Exception {
        userStoreConfigurationClient.changeUserStoreState(domain, "true");
        assertTrue("User store is active even after 15 min", waitForUserStoreUnDeployment(domain));

        LoginLogoutUtil loginLogoutUtil =
                new LoginLogoutUtil(Integer.parseInt(isServer.getProductVariables().getHttpsPort()),
                        isServer.getProductVariables().getHostName());

        assertNull("User is logged in after disabling the user store",
                loginLogoutUtil.login(domain + "/" + TEST_SECONDARY_USER, TEST_SECONDARY_USER, isServer.getBackEndUrl()));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Re-enable user store", dependsOnMethods = "testDisableUserStore")
    public void testEnableUserStoreAgain() throws Exception {
        userStoreConfigurationClient.changeUserStoreState(domain, "false");
        Thread.sleep(10000);
        assertTrue("User store not deployed within expected time interval",
                waitForUserStoreDeployment(domain));

        LoginLogoutUtil loginLogoutUtil =
                new LoginLogoutUtil(Integer.parseInt(isServer.getProductVariables().getHttpsPort()),
                        isServer.getProductVariables().getHostName());

        assertNotNull("User not logged in",
                loginLogoutUtil.login(domain + "/" + TEST_SECONDARY_USER, TEST_SECONDARY_USER,
                        isServer.getBackEndUrl()));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Re-enable user store", dependsOnMethods = "testEnableUserStoreAgain")
    public void testUpdateUserStore() throws Exception {

        UserStoreDTO userStoreDTO = getUserStoreDTO(dbURL, driverName, "wso2carbon", "wso2carbon",
                false, "testUserStore", newDomain);
        userStoreConfigurationClient.updateUserStoreWithDomainName(domain, userStoreDTO);
        assertTrue("User store not deployed within expected time interval",
                waitForUserStoreDeployment(newDomain));
    }


    /**
     * https://wso2.org/jira/browse/IDENTITY-1848 - Role permission reset when update domain name of
     * secondary user store
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Re-enable user store", dependsOnMethods = "testUpdateUserStore")
    public void testLoginAfterDomainChange() throws Exception {
        LoginLogoutUtil loginLogoutUtil =
                new LoginLogoutUtil(Integer.parseInt(isServer.getProductVariables().getHttpsPort()),
                        isServer.getProductVariables().getHostName());

        userManagementClient.addRole(newDomain + "/" + SECONDARY_ROLE + "1", new String[]{newDomain +
                "/" + TEST_SECONDARY_USER}, new String[]{"/permission/admin/login"});

        assertNotNull("User not logged in",
                loginLogoutUtil.login(newDomain + "/" + TEST_SECONDARY_USER, TEST_SECONDARY_USER,
                        isServer.getBackEndUrl()));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Add new user store", dependsOnMethods = "testLoginAfterDomainChange",
            expectedExceptions = Exception.class)
    public void testAddUserStoreAgain() throws Exception {
        addJDBCUserStore(dbURL, driverName, "wso2carbon", "wso2carbon", false, "testUserStore", newDomain);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.is", description = "Add new user store", dependsOnMethods = "testAddUserStoreAgain")
    public void testDeleteStoreAndUsers() throws Exception {

        userManagementClient.deleteUser(newDomain + "/" + TEST_SECONDARY_USER);
        userManagementClient.deleteRole(newDomain + "/" + SECONDARY_ROLE + "1");
        userManagementClient.deleteRole(newDomain + "/" + SECONDARY_ROLE);

        userStoreConfigurationClient.deleteUserStoresSet(new String[]{newDomain});
        assertTrue("User store was not deleted successfully", waitForUserStoreUnDeployment(newDomain));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @AfterClass(alwaysRun = true)
    public void cleanupTest() throws Exception {
        if (waitForUserStoreDeployment(newDomain)) {
            userStoreConfigurationClient.deleteUserStoresSet(new String[]{newDomain});
            log.info("User store - " + newDomain + "deleted");
        }
    }
}
