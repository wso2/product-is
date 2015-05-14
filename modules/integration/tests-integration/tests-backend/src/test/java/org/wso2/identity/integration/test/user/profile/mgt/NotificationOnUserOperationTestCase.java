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
package org.wso2.identity.integration.test.user.profile.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class NotificationOnUserOperationTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(NotificationOnUserOperationTestCase.class);

    private ServerConfigurationManager serverConfigurationManager;
    private File notificationMgtProperties;
    private UserManagementClient userMgtServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.addUser("NotificationUser", "passWord1@", new String[]{"admin"}, "default");
        userMgtServiceClient.addRole("NotificationRole", new String[]{}, new String[]{"admin"});
        changeISConfiguration();
        super.init();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        resetISConfiguration();
        super.init();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.deleteUser("scimUser");
        userMgtServiceClient.deleteUser("NotificationUser");
        userMgtServiceClient.deleteRole("NotificationRole");
    }

    @Test(groups = "wso2.is", description = "Check rest endpoint call on adding a policy")
    public void testAddUser() throws Exception {
        userMgtServiceClient.addRemoveRolesOfUser("NotificationUser", new String[]{"NotificationRole"}, new String[]{});
        Thread.sleep(2000);
        Assert.assertTrue(isUserExists("scimUser"));
    }

    private void changeISConfiguration() throws Exception {
        changeNotificationMgtPropertyConfig();
        serverConfigurationManager.restartForcefully();
    }


    private void changeNotificationMgtPropertyConfig() throws Exception {
        log.info("Changing msg-mgt.properties to add EntitlementNotificationExtension");

        String carbonHome = CarbonUtils.getCarbonHome();
        String templateLocation = getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "templates" + File.separator
                + "userOperation";
        String msgMgtPropertiesFileLocation = getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "config" + File.separator
                + "userOperationNotificationMgt.properties";

        HashMap<String, String> newProperties = new HashMap<String, String>();
        newProperties.put("json.subscription.userOperation.jsonContentTemplate", templateLocation);
        replaceProperties(newProperties, msgMgtPropertiesFileLocation);
        notificationMgtProperties = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "msg-mgt.properties");

        File configuredNotificationProperties = new File(msgMgtPropertiesFileLocation);

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                notificationMgtProperties, true);
    }

    private boolean isUserExists(String userName) throws Exception {
        FlaggedName[] nameList = userMgtServiceClient.listAllUsers(userName, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(userName)) {
                return true;
            }
        }
        return false;
    }

    public void replaceProperties(Map<String, String> properties, String filePath) throws IOException {

        Properties prop = new Properties();
        FileInputStream input = null;
        FileOutputStream outputStream = null;
        input = new FileInputStream(filePath);

        prop.load(input);

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            prop.put(entry.getKey(), entry.getValue());
        }

        outputStream = new FileOutputStream(filePath);
        prop.store(outputStream, null);
    }

    private void resetISConfiguration()  {

        try{
            resetMsgMgtProperties();
            serverConfigurationManager.restartGracefully();
        } catch (Exception e){
            log.info("Errors occur due to resource duplication..Ignoring..");
        }

    }


    private void resetMsgMgtProperties() throws Exception {
        log.info("Replacing msg-mgt.properties with default configurations");

        File defaultMsgMgtPropertiesFile = new File(getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "config" + File.separator
                + "msg-mgt-default.properties");

        serverConfigurationManager.applyConfigurationWithoutRestart(defaultMsgMgtPropertiesFile, notificationMgtProperties, true);

    }

}
