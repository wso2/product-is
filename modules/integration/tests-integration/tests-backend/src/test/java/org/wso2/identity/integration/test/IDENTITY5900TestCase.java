/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.UserInformationRecoveryServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

/**
 * Test case for the issue IDENTITY-5900.
 */
public class IDENTITY5900TestCase extends ISIntegrationTest {

    private UserProfileMgtServiceClient profileClient;
    private String confKey;
    private UserInformationRecoveryServiceClient infoRecoveryClient;
    private UserManagementClient userMgtClient;
    private TenantManagementServiceClient tenantServiceClient;
    private AuthenticatorClient loginManger;
    private ServerConfigurationManager scm;
    private File identityMgtServerFile;
    private File userMgtXml;

    private static final String TENANT_DOMAIN = "inforecovery.com";
    private static final String TENANT_ADMIN_USERNAME = "admin@inforecovery.com";
    private static final String TENANT_ADMIN_PASSWORD = "admin";
    private static final String TENANT_ADMIN_TENANT_AWARE_USERNAME = "admin";
    private static final String profileName = "default";

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @BeforeClass
    public void init() throws Exception {

        super.init();
        String carbonHome = Utils.getResidentCarbonHome();

        identityMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "identity" + File.separator + "identity-mgt.properties");
        File identityMgtConfigFile = new File(getISResourceLocation() + File.separator + "identityMgt" +
                File.separator + "identity-mgt-enabled.properties");

        userMgtXml = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator +
                "user-mgt.xml");
        File userMgtConfig = new File(getISResourceLocation() + File.separator + "identityMgt" + File.separator +
                "user-mgt-regex-changed.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(identityMgtConfigFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(userMgtConfig, userMgtXml, true);
        scm.restartGracefully();

        super.init();

        tenantServiceClient = new TenantManagementServiceClient(isServer.getContextUrls().getBackEndUrl(),
                sessionCookie);
        tenantServiceClient.addTenant(TENANT_DOMAIN, TENANT_ADMIN_TENANT_AWARE_USERNAME, TENANT_ADMIN_PASSWORD,
                TENANT_ADMIN_USERNAME, "Info", "Recovery");
        loginManger = new AuthenticatorClient(backendURL);
        sessionCookie = this.loginManger.login(TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD, isServer.getInstance()
                .getHosts().get(profileName));
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtClient.addUser("admin@inforecovery.com", "admin", null, "default");
        infoRecoveryClient = new UserInformationRecoveryServiceClient(backendURL, sessionCookie);
        profileClient = new UserProfileMgtServiceClient(backendURL,sessionCookie);
    }

    /*
     * To validate password reset without captcha validation is to follow the method calls as
     * verifyUser() -> sendRecoveryNotification() -> verifyConfirmationCode() -> updatePassword()
     * Since cannot answer the question the test need to carryout with Captcha.Verification.Internally.Managed=false
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check verify user")
    public void testVerifyUser() throws Exception {
        VerificationBean bean = infoRecoveryClient.verifyUser("admin@inforecovery.com", null);
        Assert.assertNotNull(bean, "Verify User has failed with null return");
        confKey = bean.getKey();
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check recovery notification sending", dependsOnMethods = "testVerifyUser")
    public void testSendRecoveryNotification() throws Exception {
        UserProfileDTO profile = profileClient.getUserProfile("admin@inforecovery.com", "default");
        UserFieldDTO email = new UserFieldDTO();
        email.setFieldValue("testuser@wso2.com");
        email.setClaimUri("http://wso2.org/claims/emailaddress");
        UserFieldDTO[] params = new UserFieldDTO[1];
        params[0] = email;
        profile.setFieldValues(params);
        profileClient.setUserProfile("admin@inforecovery.com", profile);
        VerificationBean bean = infoRecoveryClient.sendRecoveryNotification("admin@inforecovery.com", confKey, "EMAIL");
        Assert.assertNotNull(bean, "Notification sending has failed with null return");

    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        loginManger.logOut();
        if(nameExists(userMgtClient.listAllUsers("user11@inforecovery.com", 100), "user11@inforecovery.com")) {
            userMgtClient.deleteUser("user11@inforecovery.com");
        }
        File identityMgtDefaultFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "identity-mgt-default.properties");
        File userMgtConfig = new File(getISResourceLocation() + File.separator + "identityMgt" + File.separator +
                "user-mgt-default.xml");
        scm.applyConfigurationWithoutRestart(identityMgtDefaultFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(userMgtConfig, userMgtXml, true);
        scm.restartGracefully();
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