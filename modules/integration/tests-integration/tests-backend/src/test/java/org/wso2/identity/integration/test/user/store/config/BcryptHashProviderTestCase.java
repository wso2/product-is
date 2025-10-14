/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.user.store.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import java.rmi.RemoteException;

public class BcryptHashProviderTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(BcryptHashProviderTestCase.class);
    private static final String userStoreDBName = "BcryptIntegrationTestDB";
    private static final String USER_STORE_DOMAIN_NAME = "BCryptTestDomain";
    private static final String TEST_USERNAME = "bcrypt_user_01";
    private static final String TEST_PASSWORD = "TestPassword@123";
    private static final String DEFAULT_PROFILE = "default";
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private static final String DIGEST_TYPE = "BCRYPT";
    private static final String STORE_SALTED = "false";
    private static final String BCRYPT_PROPERTIES_JSON = "{\"bcrypt.version\":\"2a\",\"bcrypt.cost.factor\":\"10\"}";

    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private RemoteUserStoreManagerServiceClient remoteUserManagerClient;

    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        remoteUserManagerClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        deployAndConfigureUserStore();
    }

    private void deployAndConfigureUserStore() throws Exception {

        PropertyDTO[] bcryptProperties = userStoreConfigUtils.getJDBCUserStoreProperties(
                userStoreDBName,
                DIGEST_TYPE,
                STORE_SALTED,
                BCRYPT_PROPERTIES_JSON
        );

        UserStoreDTO userStoreDTO = userStoreConfigurationClient
                .createUserStoreDTO(JDBC_CLASS, USER_STORE_DOMAIN_NAME, bcryptProperties);
        userStoreConfigurationClient.addUserStore(userStoreDTO);

        Assert.assertTrue(
                userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigurationClient, USER_STORE_DOMAIN_NAME),
                "User store deployment with BCRYPT configuration failed."
        );
        log.info("User store " + USER_STORE_DOMAIN_NAME + " deployed successfully with BCRYPT settings.");
    }

    @Test(groups = "wso2.is",
            description = "Verifies user creation uses Bcrypt and authentication succeeds.",
            priority = 1)
    public void testBcryptUserAuthentication() throws Exception {

        final String fullUsername = USER_STORE_DOMAIN_NAME + "/" + TEST_USERNAME;
        try {
            remoteUserManagerClient.addUser(
                    fullUsername,
                    TEST_PASSWORD,
                    new String[]{},
                    null,
                    DEFAULT_PROFILE,
                    false
            );
            log.info("User " + fullUsername + " added successfully. Password should be Bcrypt hashed.");
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException | RemoteException e) {
            log.error("Failed to add user: " + fullUsername, e);
            Assert.fail("Failed to add user to Bcrypt configured store: " + e.getMessage());
        }

        boolean isAuthenticated = remoteUserManagerClient.authenticate(
                fullUsername,
                TEST_PASSWORD
        );

        Assert.assertTrue(isAuthenticated, "User authentication failed. The Bcrypt hash provider " +
                "may not be correctly hashing or verifying passwords after configuration update."
        );
        log.info("User " + fullUsername + " authenticated successfully, verifying Bcrypt functionality.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {

        final String fullUsername = USER_STORE_DOMAIN_NAME + "/" + TEST_USERNAME;
        try {
            remoteUserManagerClient.deleteUser(fullUsername);
            log.info("Test user " + fullUsername + " deleted.");
        } catch (Exception e) {
            log.error("Failed to delete user: " + fullUsername, e);
        }

        userStoreConfigurationClient.deleteUserStore(USER_STORE_DOMAIN_NAME);
        Assert.assertTrue(
                userStoreConfigUtils.waitForUserStoreUnDeployment(userStoreConfigurationClient, USER_STORE_DOMAIN_NAME),
                "Deletion of user store has failed");
        log.info("User store " + USER_STORE_DOMAIN_NAME + " deleted successfully.");
    }
}












