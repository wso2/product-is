/*
 * Copyright (c) 2020, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.identity.mgt;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionEnterprise;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.AuthenticatorRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

/**
 * This test class is to test the user account locking process while the caseInsensitiveUserName and
 * useCaseSensitiveUsernameForCacheKey properties are false in the primary user store.
 */
public class AccountLockWhileCaseInsensitiveUserFalseTestCase extends ISIntegrationTest {

    private static final String TEST_USER_1 = "testDemo";
    private static final String TEST_USER_2 = "TestDemo";
    private static final String TEST_USER_1_PASSWORD = "testDemo@Pass123";
    private static final String USERS_PATH = "users";
    private ServerConfigurationManager configurationManager;
    private SCIM2RestClient scim2RestClient;
    private String userId;
    private AuthenticatorRestClient authenticatorRestClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        String carbonHome = Utils.getResidentCarbonHome();
        configureServerWithRestart(carbonHome);

        //Initiating after the restart.
        super.init();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        authenticatorRestClient = new AuthenticatorRestClient(serverURL);
        userId = createLockedUser();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        log.info("Deleting the user : " + TEST_USER_1 + ".");
        scim2RestClient.deleteUser(userId);
        log.info("Replacing the default configurations.");
        configurationManager.restoreToLastConfiguration(false);
        scim2RestClient.closeHttpClient();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check whether the user is locked under CaseInsensitiveUsername property " +
            "is false.")
    public void testCaseInsensitiveUsernameFalseUserLocking() throws Exception {

        log.info("Login attempt to " + TEST_USER_1 + " user from " + TEST_USER_2 + " user.");
        JSONObject authenticationResponse =  authenticatorRestClient.login(TEST_USER_2, TEST_USER_1_PASSWORD);
        Assert.assertNull(authenticationResponse.get("token"));
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

    private String createLockedUser() throws Exception {

        log.info("Creating a locked user account.");
        UserObject userInfo = new UserObject();
        userInfo.setUserName(TEST_USER_1);
        userInfo.setPassword(TEST_USER_1_PASSWORD);
        userInfo.setScimSchemaExtensionEnterprise(new ScimSchemaExtensionEnterprise().accountLocked(true));

        String userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
        log.info("Locked user account created.");
        return userId;
    }
}
