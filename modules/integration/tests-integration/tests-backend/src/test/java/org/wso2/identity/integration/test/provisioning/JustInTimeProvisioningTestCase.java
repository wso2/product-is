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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
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

import java.io.File;
import java.rmi.RemoteException;

import javax.xml.xpath.XPathExpressionException;

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

    private AuthenticatorClient logManger;
    private final String username;
    private final String userPassword;
    private final AutomationContext context;
    private String backendURL;
    private String sessionCookie;

    @DataProvider(name = "configProvider")
    public static Object[][] configProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}};
    }

    @Factory(dataProvider = "configProvider")
    public JustInTimeProvisioningTestCase(TestUserMode userMode) throws Exception {

        context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.userPassword = context.getContextTenant().getTenantAdmin().getPassword();
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        try {
            super.initTest();
            backendURL = context.getContextUrls().getBackEndUrl();
            logManger = new AuthenticatorClient(backendURL);
            sessionCookie = logManger.login(username, userPassword, context.getInstance().getHosts().get("default"));

            userMgtClient = new UserManagementClient(backendURL, sessionCookie);
            userStoreClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
            userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
            RemoteAuthorizationManagerServiceClient remoteAuthorizationManagerServiceClient =
                    new RemoteAuthorizationManagerServiceClient(backendURL, sessionCookie);

            log.info("login user name : " + username);
            log.info("login password : " + userPassword);
            for (String role : userStoreClient.getRoleListOfUser(username)) {
                boolean roleAuthorized = remoteAuthorizationManagerServiceClient.isRoleAuthorized(role,
                        "/permission/admin/manage/identity/rolemgt/create", "ui.execute");
                log.info("Role Authorization :" + roleAuthorized + " role: " + role);
            }

            boolean isUserAuthorized =
                    remoteAuthorizationManagerServiceClient.isUserAuthorized(username, "/permission" +
                            "/admin/manage/identity/rolemgt/create", "ui.execute");
            log.info("User Authorization :" + isUserAuthorized);
            remoteAuthorizationManagerServiceClient.authorizeUser(username, "/permission/admin/", "ui.execute");
            boolean isAuthorized = remoteAuthorizationManagerServiceClient.isUserAuthorized(username, "/permission" +
                    "/admin/manage/identity/rolemgt/create", "ui.execute");
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

        try {
            super.endTest();
            userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
            userStoreConfigUtils.waitForUserStoreUnDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);
        } catch (AutomationFrameworkException e) {
            log.error("Error while shutting down the server. ", e);
        }

    }

    @Override
    @Test(groups = "wso2.is", description = "test testCreateIdentityProviderInPrimaryIS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {
        super.testCreateIdentityProviderInPrimaryIS();
    }

    @Override
    @Test(dependsOnMethods = {"testCreateIdentityProviderInPrimaryIS"}, groups = "wso2.is", description = "test testCreateServiceProviderInPrimaryIS")
    public void testCreateServiceProviderInPrimaryIS() throws Exception {
        super.testCreateServiceProviderInPrimaryIS();
    }

    @Override
    @Test(dependsOnMethods = {"testCreateServiceProviderInPrimaryIS"}, groups = "wso2.is", description = "test testCreateServiceProviderInSecondaryIS")
    public void testCreateServiceProviderInSecondaryIS() throws Exception {
        super.testCreateServiceProviderInSecondaryIS();
    }

    @Override
    @Test(dependsOnMethods = {"testCreateServiceProviderInSecondaryIS"}, groups = "wso2.is", description = "Check functionality of attribute consumer index")
    public void testAttributeConsumerIndex() throws Exception {
        super.testAttributeConsumerIndex();
    }

    @Override
    @Test(dependsOnMethods = {"testAttributeConsumerIndex"}, groups = "wso2.is", description = "test testSAMLToSAMLFederation")
    public void testSAMLToSAMLFederation() throws Exception {

        updateIdentityProviderJitConfiguration(false, false, false, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        super.testSAMLToSAMLFederation();
    }

    @Test(dependsOnMethods = {"testSAMLToSAMLFederation"}, groups = "wso2.is", description = "test Just in time provisioning")
    public void testJustInTimeProvisioning()
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        Assert.assertTrue(userStoreClient.isExistingUser(getFederatedTestUser()));
        userStoreClient.deleteUser(getFederatedTestUser());
    }

    @Test(dependsOnMethods = {"testJustInTimeProvisioning"}, groups = "wso2.is", description = "Test Just in time provisioning with password provisioning")
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

    @Test(dependsOnMethods = {"testSAMLToSAMLFederationWithPasswordProvisioning"}, groups = "wso2.is", description = "test Just in time provisioning with username "
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

    @Test(dependsOnMethods = {"testSAMLToSAMLFederationWithUserNameAndPasswordProvisioning"}, groups = "wso2.is", description = "test just in time provisioning with only prompt consent")
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

    @Test(dependsOnMethods = {"testSAMLToSAMLFederationWithPromptConsent"}, groups = "wso2.is", description = "test just in time provisioning with as in username")
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
            throws RemoteException, LoginAuthenticationExceptionException {

        AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(
                backendURL + "AuthenticationAdmin");
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

        String jdbcClass = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
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

        propertyDTOs[4].setName("UserIDEnabled");
        propertyDTOs[4].setValue("true");

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient
                .createUserStoreDTO(jdbcClass, DOMAIN_ID, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);
    }
}
