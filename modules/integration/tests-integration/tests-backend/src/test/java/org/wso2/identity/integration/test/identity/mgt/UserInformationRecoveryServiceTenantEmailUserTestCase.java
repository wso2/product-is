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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.mgt.UserInformationRecoveryServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

public class UserInformationRecoveryServiceTenantEmailUserTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UserInformationRecoveryServiceTenantEmailUserTestCase.class);
    private static final String TENANT_USER = "user11@abc.com";
    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private static final String TENANT_DOMAIN = "inforecovery.com";
    private static final String TENANT_ADMIN_USERNAME = "admin@inforecovery.com";
    private static final String TENANT_ADMIN_PASSWORD = "admin";
    private static final String TENANT_ADMIN_TENANT_AWARE_USERNAME = "admin";
    private static final String profileName = "default";
    private UserInformationRecoveryServiceClient infoRecoveryClient;
    private UserManagementClient userMgtClient;
    private TenantManagementServiceClient tenantServiceClient;
    private AuthenticatorClient loginManger;
    private ServerConfigurationManager scm;
    private File identityMgtServerFile;
    private File userMgtXml;

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
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
        infoRecoveryClient = new UserInformationRecoveryServiceClient(backendURL, sessionCookie);

        userMgtClient.addUser(TENANT_USER, "passWord1@", null, profileName);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        loginManger.logOut();
        if (nameExists(userMgtClient.listAllUsers(TENANT_USER, 100), TENANT_USER)) {
            userMgtClient.deleteUser(TENANT_USER);
        }

        File identityMgtDefaultFile = new File(getISResourceLocation() + File.separator + "identityMgt" +
                File.separator + "identity-mgt-default.properties");
        File userMgtConfig = new File(getISResourceLocation() + File.separator + "identityMgt" + File.separator +
                "user-mgt-default.xml");
        scm.applyConfigurationWithoutRestart(identityMgtDefaultFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(userMgtConfig, userMgtXml, true);
        scm.restartGracefully();

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check user account verification")
    public void testVerifyUserAccount() throws Exception {
        UserIdentityClaimDTO[] claims = new UserIdentityClaimDTO[1];
        UserIdentityClaimDTO claimEmail = new UserIdentityClaimDTO();
        claimEmail.setClaimUri(USERNAME_CLAIM_URI);
        claimEmail.setClaimValue(TENANT_USER);

        claims[0] = claimEmail;

        VerificationBean bean = infoRecoveryClient.verifyAccount(claims, null, TENANT_DOMAIN);
        Assert.assertTrue(bean.getVerified(), "Verifying user account has failed with null return");
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
