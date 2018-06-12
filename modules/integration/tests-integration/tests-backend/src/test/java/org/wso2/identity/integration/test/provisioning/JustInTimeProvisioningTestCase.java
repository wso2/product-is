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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.saml.SAMLIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class JustInTimeProvisioningTestCase extends SAMLIdentityFederationTestCase {

    private UserManagementClient userMgtClient;
    private RemoteUserStoreManagerServiceClient userStoreClient;
    private final String LOGIN_ROLE = "loginJIT";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.initTest();
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        userStoreClient = new RemoteUserStoreManagerServiceClient(getBackendURL(), getSessionCookie());
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {
        super.endTest();
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

        updateIdentityProviderJitConfiguration(true, false);
        super.testSAMLToSAMLFederation();
        userMgtClient.addRole(LOGIN_ROLE, new String[] { getFederatedTestUser() },
                new String[] { "/permission/admin/login" }, false);
        checkNewlyCreatedUserLogin(getFederatedTestUser());
        userStoreClient.deleteUser(getFederatedTestUser());
    }

    @Test(priority = 7, groups = "wso2.is", description = "test Just in time provisioning with username "
            + "and password provisioning")
    public void testSAMLToSAMLFederationWithUserNameAndPasswordProvisioning() throws Exception {

        updateIdentityProviderJitConfiguration(true, true);
        super.testSAMLToSAMLFederation();
        userMgtClient.updateUserListOfRole(LOGIN_ROLE, new String[] { Utils.MODIFIED_USER_NAME }, null);
        checkNewlyCreatedUserLogin(Utils.MODIFIED_USER_NAME);
        userStoreClient.deleteUser(Utils.MODIFIED_USER_NAME);
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
            boolean isModifyUserNameAllowed) throws Exception {

        IdentityProvider identityProvider = super.getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);
        JustInTimeProvisioningConfig justInTimeProvisioningConfig = identityProvider.getJustInTimeProvisioningConfig();
        justInTimeProvisioningConfig.setPasswordProvisioningEnabled(isPasswordProvisioningEnabled);
        justInTimeProvisioningConfig.setModifyUserNameAllowed(isModifyUserNameAllowed);
        identityProvider.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);
        super.updateIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME, identityProvider);
    }
}