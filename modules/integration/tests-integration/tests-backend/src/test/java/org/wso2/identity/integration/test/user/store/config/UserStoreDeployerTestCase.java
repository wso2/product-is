/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.identity.integration.test.user.store.config;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

import java.io.File;
import java.rmi.RemoteException;


public class UserStoreDeployerTestCase extends ISIntegrationTest {

    public static final String USERSTORES = "userstores";
    private static final String deploymentDirectory = CarbonUtils.getCarbonRepository() + USERSTORES;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private String userStoreConfigFilePath;
    private File srcFile;
    private File destFile;
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private UserManagementClient userMgtClient;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        srcFile = new File(getTestArtifactLocation() + File.separator + "wso2_com.xml");
        userStoreConfigFilePath = deploymentDirectory + File.separator;
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.is", description = "Deploy a user store config file")
    public void testDroppingFile() throws Exception {
        destFile = new File(userStoreConfigFilePath + srcFile.getName());

        FileUtils.copyFile(srcFile, destFile);
        Assert.assertTrue("After 30s user store is still not deployed.", userStoreConfigUtils
                .waitForUserStoreDeployment(userStoreConfigurationClient, "wso2.com"));
    }

    @Test(groups = "wso2.is", description = "Test multiple user stores", dependsOnMethods = "testDroppingFile")
    public void testMultipleUserStores() throws RemoteException, UserAdminUserAdminException {
        Assert.assertTrue("Multiple user stores not detected.",userMgtClient.hasMultipleUserStores());
    }

    @Test(groups = "wso2.is", description = "Test user store add user", dependsOnMethods = "testMultipleUserStores")
    public void testAddUser() throws Exception {
        userMgtClient.addUser("wso2.com/pushpalanka", "pushpalanka", new String[] {}, null);
        Assert.assertTrue("Couldn't add user to newly added user store",
                          userMgtClient.getUserList().contains("WSO2.COM/pushpalanka"));
    }

    @Test(groups = "wso2.is", description = "Test enable/disable user stores", dependsOnMethods = "testMultipleUserStores")
    public void testChangeUserStoreState() throws Exception {
        Boolean isDisabled = false;
        userStoreConfigurationClient.changeUserStoreState("wso2.com", true);
        Thread.sleep(30000);
        UserStoreDTO[] userStoreDTOs = userStoreConfigurationClient.getActiveDomains();
        if (userStoreDTOs[0] != null) {
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                if (userStoreDTO.getDomainId().equalsIgnoreCase("wso2.com")) {
                    isDisabled = userStoreDTO.getDisabled();
                }
            }
        }
        Assert.assertTrue("Disabling user store has failed", isDisabled);
    }

    @Test(groups = "wso2.is", description = "Delete a user store config file", dependsOnMethods = "testChangeUserStoreState")
    public void testDeletingFile() throws Exception {
        destFile = new File(userStoreConfigFilePath + srcFile.getName());

        FileUtils.forceDelete(destFile);
        Assert.assertTrue("After 30s user store is still not deleted.", userStoreConfigUtils
                .waitForUserStoreUnDeployment(userStoreConfigurationClient, "wso2.com"));
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
    }
}
