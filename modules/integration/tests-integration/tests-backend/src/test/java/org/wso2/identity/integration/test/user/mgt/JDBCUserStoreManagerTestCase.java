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

package org.wso2.identity.integration.test.user.mgt;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.File;

public class JDBCUserStoreManagerTestCase extends UserManagementServiceAbstractTest {


    private ServerConfigurationManager scm;
    private File userMgtServerFile;

    @BeforeClass(alwaysRun = true)
    public void configureServer() throws Exception {
        super.init();
        String carbonHome = ServerConfigurationManager.getCarbonHome();
        userMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator
                                     + "conf" + File.separator + "user-mgt.xml");
        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                                          + File.separator + "JdbcUserMgtConfig.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfiguration(userMgtConfigFile, userMgtServerFile, true, true);
        doInit();

    }

    @AfterClass(alwaysRun = true)
    public void restoreServer() throws Exception {
        try {
            super.clean();
        } finally {
            scm.restoreToLastConfiguration();
        }

    }

    @Override
    protected void setUserName() {
        newUserName = "JDBCUserName";
    }

    @Override
    protected void setUserPassword() {
        newUserPassword = "JDBCUserName123";
    }

    @Override
    protected void setUserRole() {
        newUserRole = "JDBCUserRole";
    }
}
