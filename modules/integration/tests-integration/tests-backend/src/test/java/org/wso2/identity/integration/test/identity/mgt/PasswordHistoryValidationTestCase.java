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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.mgt.IdentityGovernanceServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class PasswordHistoryValidationTestCase extends ISIntegrationTest {

    private UserManagementClient userMgtClient;
    private AuthenticatorClient loginManger;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private static final String PROFILE_NAME = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String TEST_ROLE = "testRole";
    private IdentityGovernanceServiceClient identityGovernanceServiceClient;
    private static final String EXCEPTION_MESSAGE = "This password has been used in recent history. Please choose a different password";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        loginManger = new AuthenticatorClient(backendURL);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        identityGovernanceServiceClient = new IdentityGovernanceServiceClient(sessionCookie, backendURL);

        Thread.sleep(5000);
        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        Property[] newProperties = new Property[1];
        Property prop = new Property();
        prop.setName("passwordHistory.enable");
        prop.setValue("true");
        newProperties[0] = prop;
        identityGovernanceServiceClient.updateConfigurations(newProperties);

        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null,
                null, PROFILE_NAME, false);
        userMgtClient.addRole(TEST_ROLE, new String[]{TEST_USER_USERNAME}, new String[]{"/permission/admin/login"}, false);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        loginManger.logOut();
        userMgtClient.deleteUser(TEST_USER_USERNAME);
        userMgtClient.deleteRole(TEST_ROLE);

    }


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Testing reusing existing password")
    public void testReuseExistingPassword() throws Exception {

        try {
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD);
            Assert.fail("Password History Validation failed.");
        } catch (Exception e) {
            Assert.assertEquals(EXCEPTION_MESSAGE, e.getMessage());
        }

    }

    @Test(groups = "wso2.is", description = "Testing reusing same password in 3rd attempt", dependsOnMethods =
            "testReuseExistingPassword")
    public void testFailPasswordHistory() throws Exception {

        try {
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD + 1);
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD + 2);
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD);
            Assert.fail("Password History Validation failed.");
        } catch (Exception e) {
            Assert.assertEquals(EXCEPTION_MESSAGE, e.getMessage());
        }
    }

    @Test(groups = "wso2.is", description = "Testing reusing same password in 6rd attempt", dependsOnMethods = "testFailPasswordHistory")
    public void testSuccessPasswordHistory() throws Exception {

        try {
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD + 3);
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD + 4);
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD + 5);
            remoteUSMServiceClient.updateCredentialByAdmin(TEST_USER_USERNAME, TEST_USER_PASSWORD);
        } catch (Exception e) {
            Assert.fail("Password History Validation failed.");
        }

    }
}
