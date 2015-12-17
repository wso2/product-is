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
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.mgt.UserIdentityManagementAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class UserIdentityManagementServiceTestCase extends ISIntegrationTest {

    private UserManagementClient userMgtClient;
    private AuthenticatorClient loginManger;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private UserIdentityManagementAdminServiceClient userIdentityManagementAdminServiceClient;

    private static final String PROFILE_NAME = "default";
    private static final String TEST_USER_USERNAME = "testUser";
    private static final String TEST_USER_PASSWORD = "Ab@123";
    private static final String TEST_ROLE = "testRole";


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        loginManger = new AuthenticatorClient(backendURL);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        userIdentityManagementAdminServiceClient = new UserIdentityManagementAdminServiceClient(backendURL, sessionCookie);

        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(TEST_USER_USERNAME, TEST_USER_PASSWORD, null,
                null, PROFILE_NAME, false);
        userMgtClient.addRole(TEST_ROLE, new String[]{TEST_USER_USERNAME}, new String[]{"/permission/admin/login"}, false);
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        loginManger.logOut();
        if(nameExists(userMgtClient.listAllUsers(TEST_USER_USERNAME, 100), TEST_USER_USERNAME)) {
            userMgtClient.deleteUser(TEST_USER_USERNAME);
        }
        if(nameExists(userMgtClient.listRoles(TEST_ROLE, 100), "TEST_ROLE")){
            userMgtClient.deleteRole("TEST_ROLE");
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Getting challenge questions of a user")
    public void testGetChallengeQuestionsOfUser() throws Exception {
        UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
        userChallengesDTO.setId("http://wso2.org/claims/challengeQuestion1");
        userChallengesDTO.setQuestion("Favorite food ?");
        userChallengesDTO.setOrder(0);
        userChallengesDTO.setAnswer("answer1");
        UserChallengesDTO[] userChallengesDTOs = new UserChallengesDTO[]{userChallengesDTO};
        userIdentityManagementAdminServiceClient.setChallengeQuestionsOfUser(TEST_USER_USERNAME, userChallengesDTOs);

        UserChallengesDTO[] userChallengesDTOsReceived = userIdentityManagementAdminServiceClient
                .getChallengeQuestionsOfUser(TEST_USER_USERNAME);
        Assert.assertEquals(1, userChallengesDTOsReceived.length);
        Assert.assertEquals(userChallengesDTOsReceived[0].getQuestion(), "Favorite food ?");
        Assert.assertNotNull(userChallengesDTOsReceived[0].getAnswer(), "Answer of the challenge question is null");
    }


    /**
     * Checks whether the passed Name exists in the FlaggedName array.
     *
     * @param allNames
     * @param inputName
     * @return
     */
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
