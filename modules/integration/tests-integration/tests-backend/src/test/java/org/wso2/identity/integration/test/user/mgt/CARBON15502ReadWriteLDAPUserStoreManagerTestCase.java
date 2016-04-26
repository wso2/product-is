/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.user.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.util.Arrays;

public class CARBON15502ReadWriteLDAPUserStoreManagerTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(CARBON15502ReadWriteLDAPUserStoreManagerTestCase.class);
    private ServerConfigurationManager scm;
    private File userMgtServerFile;
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerClient;
    private final static String USERNAME = "gayan";
    private final static String ROLE_NAME = "aliya";

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        start();
        String carbonHome = CarbonUtils.getCarbonHome();
        userMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator
                                     + "conf" + File.separator + "user-mgt.xml");

        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                                          + File.separator + "readWriteLdapUserMgtConfigWildCard.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(userMgtConfigFile, userMgtServerFile, true);
        scm.restartGracefully();

        start();
        remoteUserStoreManagerClient.addUser(USERNAME, "password", null, null, null, false);
        remoteUserStoreManagerClient.addRole(ROLE_NAME, new String[] { USERNAME }, null);
    }

    @Test(description = "Test user existence in newly created role")
    public void getRolesOfUser() throws Exception {
        Assert.assertTrue(isRoleExist(remoteUserStoreManagerClient.getRoleListOfUser(USERNAME), ROLE_NAME),
                          "User :" + USERNAME + " does not contain the Role :" + ROLE_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void restoreServer() throws Exception {

        remoteUserStoreManagerClient.deleteUser(USERNAME);
        remoteUserStoreManagerClient.deleteRole(ROLE_NAME);

        // Reset the user-mgt.xml configuration.
        File userMgtDefaultFile = new File(getISResourceLocation() + File.separator + "userMgt"
                                           + File.separator + "default-user-mgt.xml");
        scm.applyConfigurationWithoutRestart(userMgtDefaultFile, userMgtServerFile, true);
        scm.restartGracefully();

    }

    private void start() throws Exception {
        super.init();
        remoteUserStoreManagerClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
    }

    private boolean isRoleExist(String values[], String targetValue) {
        return Arrays.asList(values).contains(targetValue);
    }

}
