package org.wso2.identity.integration.test.provisioning;

/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.authorization.mgt.RemoteAuthorizationManagerServiceClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.saml.SAMLIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.rmi.RemoteException;

public class JustInTimeProvisioningTestCase extends SAMLIdentityFederationTestCase {

    private UserManagementClient userMgtClient;
    private RemoteUserStoreManagerServiceClient userStoreClient;
    private final String INTERNAL_LOGIN_ROLE = "Internal/loginJIT";
    public static final String DOMAIN_ID = "WSO2TEST.COM";
    private static final String USER_STORE_DB_NAME = "JDBC_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        try {
            super.initTest();
            userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
            userStoreClient = new RemoteUserStoreManagerServiceClient(getBackendURL(), getSessionCookie());
            userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(getBackendURL(), sessionCookie);
            RemoteAuthorizationManagerServiceClient remoteAuthorizationManagerServiceClient =
                    new RemoteAuthorizationManagerServiceClient(getBackendURL(), getSessionCookie());

            log.info("login user name : " + isServer.getSuperTenant().getTenantAdmin().getUserName());
            log.info("login password : " + isServer.getSuperTenant().getTenantAdmin().getPassword());
            for (String role : userStoreClient.getRoleListOfUser(isServer.getSuperTenant().getTenantAdmin().getUserName())) {
                boolean roleAuthorized = remoteAuthorizationManagerServiceClient.isRoleAuthorized(role,
                        "/permission/admin/manage/identity/rolemgt/create", "ui.execute");
                log.info("Role Authorization :" + roleAuthorized + " role: " + role);
            }

            boolean isUserAuthorized = remoteAuthorizationManagerServiceClient.isUserAuthorized(isServer.getSuperTenant().getTenantAdmin()
                    .getUserName(), "/permission/admin/manage/identity/rolemgt/create", "ui.execute");
            log.info("User Authorization :" + isUserAuthorized);
            remoteAuthorizationManagerServiceClient.authorizeUser(isServer.getSuperTenant().getTenantAdmin().
                    getUserName(), "/permission/admin/", "ui.execute");
            boolean isAuthorized = remoteAuthorizationManagerServiceClient.isUserAuthorized(isServer.getSuperTenant().getTenantAdmin()
                    .getUserName(), "/permission/admin/manage/identity/rolemgt/create", "ui.execute");
            log.info("User Authorization details :" + isAuthorized);

            userMgtClient.addInternalRole("loginJIT", null, new String[]{"/permission/admin/login"});
            addSecondaryUserStore();

        } catch (Exception e) {
            log.error("Failure occured due to :" + e.getMessage(), e);
            throw e;
        }
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        super.endTest();
        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
    }

    @Override
    @Test(priority = 1, groups = "wso2.is", description = "test testCreateIdentityProviderInPrimaryIS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {
        super.testCreateIdentityProviderInPrimaryIS();
    }

    @Override
    @Test(priority = 2, groups = "wso2.is", description = "test testCreateServiceProviderInPrimaryIS")
    public void testCreateServiceProviderInPrimaryIS() throws Exception {
        super.testCreateServiceProviderInPrimaryIS();
    }

    @Override
    @Test(priority = 3, groups = "wso2.is", description = "test testCreateServiceProviderInSecondaryIS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {
        super.testCreateServiceProviderInSecondaryIS();
    }

    @Override
    @Test(priority = 4, groups = "wso2.is", description = "Check functionality of attribute consumer index")
    public void testAttributeConsumerIndex() throws Exception {
        super.testAttributeConsumerIndex();
    }

    @Override
    @Test(priority = 5, groups = "wso2.is", description = "test testSAMLToSAMLFederation")
    public void testSAMLToSAMLFederation() throws Exception {

        updateIdentityProviderJitConfiguration(false, false, false, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        super.testSAMLToSAMLFederation();
    }

    @Test(priority = 6, groups = "wso2.is", description = "test Just in time provisioning")
    public void testJustInTimeProvisioning()
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        Assert.assertTrue(userStoreClient.isExistingUser(getFederatedTestUser()));
        userStoreClient.deleteUser(getFederatedTestUser());
    }

    @Test(priority = 7, groups = "wso2.is", description = "Test Just in time provisioning with password provisioning")
    public void testSAMLToSAMLFederationWithPasswordProvisioning() throws Exception {

        try {
            updateIdentityProviderJitConfiguration(true, false, true, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
            super.testSAMLToSAMLFederation();
            userMgtClient.updateUserListOfRole(INTERNAL_LOGIN_ROLE, new String[] { getFederatedTestUser() }, null);
            checkNewlyCreatedUserLogin(getFederatedTestUser());
        } finally {
            userStoreClient.deleteUser(getFederatedTestUser());
        }

    }

    @Test(priority = 7, groups = "wso2.is", description = "test Just in time provisioning with username "
            + "and password provisioning")
    public void testSAMLToSAMLFederationWithUserNameAndPasswordProvisioning() throws Exception {

        try {
            updateIdentityProviderJitConfiguration(true, true, true, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
            super.testSAMLToSAMLFederation();
            userMgtClient.updateUserListOfRole(INTERNAL_LOGIN_ROLE, new String[] { Utils.MODIFIED_USER_NAME }, null);
            checkNewlyCreatedUserLogin(Utils.MODIFIED_USER_NAME);
        } finally {
            userStoreClient.deleteUser(Utils.MODIFIED_USER_NAME);
        }
    }

    @Test(priority = 7, groups = "wso2.is", description = "test just in time provisioning with only prompt consent")
    public void testSAMLToSAMLFederationWithPromptConsent() throws Exception {

        try {
            updateIdentityProviderJitConfiguration(false, false, true, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
            super.testSAMLToSAMLFederation();
            Assert.assertTrue(userStoreClient.isExistingUser(getFederatedTestUser()),
                    "User addition failed for JIT with prompt consent");
        } finally {
            userStoreClient.deleteUser(getFederatedTestUser());
        }
    }

    @Test(priority = 7, groups = "wso2.is", description = "test just in time provisioning with as in username")
    public void testSAMLToSAMLFederationWithAsIsUserNameUserStore() throws Exception {

        String userName = DOMAIN_ID + UserCoreConstants.DOMAIN_SEPARATOR + Utils.MODIFIED_USER_NAME;
        try {
            updateIdentityProviderJitConfiguration(true, true, true, "As in username");
            super.testSAMLToSAMLFederation();
            userMgtClient.updateUserListOfRole(INTERNAL_LOGIN_ROLE, new String[] {userName}, null);
            checkNewlyCreatedUserLogin(userName);
        } finally {
            userStoreClient.deleteUser(userName);
        }
    }

    /**
     * To check whether login is success for the newly created user with the provided password.
     *
     * @param userName Relevant user name of the created user.
     * @throws XPathExpressionException              XPathExpression Exception.
     * @throws RemoteException                       Remote Exception.
     * @throws LoginAuthenticationExceptionException Login Authentication Exception.
     */
    private void checkNewlyCreatedUserLogin(String userName)
            throws XPathExpressionException, RemoteException, LoginAuthenticationExceptionException {

        AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(
                getBackendURL() + "AuthenticationAdmin");
        boolean loginStatus = authenticationAdminStub.login(userName, Utils.PASSWORD, "localhost");
        Assert.assertTrue(loginStatus, "Login failed for newly created user");
    }

    /**
     * To update the Identity provider JIT configuration.
     *
     * @param isModifyUserNameAllowed To mention whether isModifyUserNameAllowed.
     * @throws Exception Exception.
     */
    private void updateIdentityProviderJitConfiguration(boolean isPasswordProvisioningEnabled,
            boolean isModifyUserNameAllowed, boolean isPromptConsent, String userStore) throws Exception {

        IdentityProvider identityProvider = super.getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);
        JustInTimeProvisioningConfig justInTimeProvisioningConfig = identityProvider.getJustInTimeProvisioningConfig();
        justInTimeProvisioningConfig.setPasswordProvisioningEnabled(isPasswordProvisioningEnabled);
        justInTimeProvisioningConfig.setModifyUserNameAllowed(isModifyUserNameAllowed);
        justInTimeProvisioningConfig.setPromptConsent(isPromptConsent);
        justInTimeProvisioningConfig.setProvisioningUserStore(userStore);
        identityProvider.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);
        super.updateIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME, identityProvider);
    }

    /**
     * To add the secondary user store.
     *
     * @throws Exception Relevant exception.
     */
    private void addSecondaryUserStore() throws Exception {

        String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
        H2DataBaseManager dbmanager = new H2DataBaseManager(
                "jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/" + USER_STORE_DB_NAME,
                DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dbmanager.disconnect();

        PropertyDTO[] propertyDTOs = new PropertyDTO[10];
        for (int i = 0; i < 10; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue(
                "jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/" + USER_STORE_DB_NAME);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(DB_USER_NAME);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(DB_USER_PASSWORD);

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient
                .createUserStoreDTO(jdbcClass, DOMAIN_ID, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);
    }
}
