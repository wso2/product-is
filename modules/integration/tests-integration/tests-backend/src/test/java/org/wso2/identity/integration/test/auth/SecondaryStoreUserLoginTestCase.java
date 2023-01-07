package org.wso2.identity.integration.test.auth;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

/**
 * This class contains test case for authentication of users in both primary and secondary user stores.
 */

public class SecondaryStoreUserLoginTestCase extends ISIntegrationTest {

//    protected String adminUsername;
//    protected String adminPassword;

    protected static String primUsername = "primUsername";
    protected static String primPassword = "primPassword";

    protected static String secUsername = "secUsername";
    protected static String secPassword = "secPassword";
    private AuthenticatorClient logManager;
    private UserManagementClient userMgtClient;
    private RemoteUserStoreManagerServiceClient usmClient;
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private static final String DOMAIN_ID = "SECONDARY_USERSTORE";
    private static final String USER_STORE_DB_NAME = "SECONDARY_USER_STORE_DB";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        usmClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);

//        TODO: Register a secondary user store
        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(JDBC_CLASS, DOMAIN_ID,
                userStoreConfigUtils.getJDBCUserStoreProperties(USER_STORE_DB_NAME));
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        Boolean isUserStoreDeployed = userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);

        if (isUserStoreDeployed) {
            logManager = new AuthenticatorClient(backendURL);
//            userMgtClient = new UserManagementClient(backendURL, getSessionCookie());

            //        Login to the management console
//            adminUsername = userInfo.getUserName();
//            adminPassword = userInfo.getPassword();
            logManager.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                    isServer.getSuperTenant().getTenantAdmin().getPassword(),
                    isServer.getInstance().getHosts().get("default"));

            //        Make a user (from primary user store)
            usmClient.addUser(primUsername, primPassword, new String[]{"admin"}, new ClaimValue[0], null, false);

            //        TODO: Make a user (from secondary user store)
            usmClient.addUser(secUsername, secPassword, new String[]{"admin"}, new ClaimValue[0], null, false);
            logManager.logOut();
        }
        else {
            log.error("Secondary user store is not deployed");
        }
    }

    @Test(groups = "wso2.is", description = "Check the secondary user store user login flow")
    public void testPrimaryStoreUserLogin() throws Exception {
        Boolean primLoginSuccess = isAuthSuccessful(primUsername, primPassword);
        Assert.assertTrue(primLoginSuccess);
    }

    @Test(groups = "wso2.is", description = "Check the secondary user store user login flow")
    public void testSecondaryStoreUserLogin() throws Exception {
        Boolean secLoginSuccess = isAuthSuccessful(secUsername, secPassword);
        Assert.assertTrue(secLoginSuccess);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        usmClient.deleteUser(primUsername);
        usmClient.deleteUser(secUsername);
        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
    }

    private Boolean isAuthSuccessful(String username, String password) {
        Boolean authenticationSuccess = false;
        try {
            authenticationSuccess = usmClient.authenticate(username, password);
        } catch (Exception e) {
            log.error("Error occurred when authenticating the user.", e);
        }
        return authenticationSuccess;
    }
}

//  Doubtful classes I used
    //  RemoteUserStoreManagerServiceClient ==> for adding users && checking authentication  ==> from AccountLockEnabledTestCase && RemoteUserStoreManagerServiceTestCase
    //  UserStoreConfigAdminServiceClient   ==> for adding user stores                       ==> from ClaimMappingsOnSecondaryUserStoreTestCase
    //  AuthenticatorClient                 ==> for logging in and out of management console ==> from ConditionalAuthenticationTestCase