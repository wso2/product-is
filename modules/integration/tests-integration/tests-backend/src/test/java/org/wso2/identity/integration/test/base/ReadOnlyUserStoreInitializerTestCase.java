/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.base;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.ISTestUtils;

import java.io.File;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Initialize a read-only LDAP user store at the beginning of the test "is-tests-read-only-userstore"
 * and reverts back to a read-write LDAP user store at the end of the test.
 */
public class ReadOnlyUserStoreInitializerTestCase extends ISIntegrationTest {

    private ServerConfigurationManager scm;
    private File defaultConfigFile;
    private UserManagementClient userMgtClient;
    private AuthenticatorClient authenticatorClient;
    private String newUserName = "ReadOnlyLDAPUserName";
    private String newUserRole = "ReadOnlyLDAPUserRole";
    private String newUserPassword = "ReadOnlyLDAPUserPass";

    @BeforeTest(alwaysRun = true)
    public void initUserStoreConfig() throws Exception {

        super.init();
        applyTomlConfigsAndRestart("ldap_user_mgt_config.toml");
        scm.restoreToLastConfiguration(false);

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);

        userMgtClient.addRole(newUserRole, null, new String[]{"/permission/admin/login"});
        userMgtClient.addUser(newUserName, newUserPassword, new String[]{newUserRole}, null);

        applyTomlConfigsAndRestart("read_only_ldap_user_mgt_config.toml");
    }

    @AfterTest(alwaysRun = true)
    public void resetUserStoreConfig() throws Exception {

        scm.restoreToLastConfiguration(false);
        applyTomlConfigsAndRestart("ldap_user_mgt_config.toml");

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());

        if (ISTestUtils.nameExists(userMgtClient.listAllUsers(newUserName, 10), newUserName)) {
            userMgtClient.deleteUser(newUserName);
        }

        if (userMgtClient.roleNameExists(newUserRole)) {
            userMgtClient.deleteRole(newUserRole);
        }
        scm.restoreToLastConfiguration(true);
    }

    private void applyTomlConfigsAndRestart(String deploymentTomlFile) throws AutomationUtilException, XPathExpressionException, IOException {

        String carbonHome = Utils.getResidentCarbonHome();
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        log.info("Default TOML: " + defaultConfigFile.toString());
        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                + File.separator + deploymentTomlFile);
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(userMgtConfigFile, defaultConfigFile, true);
        scm.restartGracefully();
    }
}
