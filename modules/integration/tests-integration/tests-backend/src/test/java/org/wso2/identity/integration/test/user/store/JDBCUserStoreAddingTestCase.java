/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.test.user.store;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.clients.user.store.count.UserStoreCountServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JDBCUserStoreAddingTestCase extends ISIntegrationTest{
    private static final String PERMISSION_LOGIN = "/permission/admin/login";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreCountServiceClient userStoreCountServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils =  new UserStoreConfigUtils();
    private final String jdbcClass = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private final String rwLDAPClass = "org.wso2.carbon.user.core.ldap.UniqueIDReadWriteLDAPUserStoreManager";
    private final String roLDAPClass = "org.wso2.carbon.user.core.ldap.UniqueIDReadOnlyLDAPUserStoreManager";
    private final String adLDAPClass = "org.wso2.carbon.user.core.ldap.UniqueIDActiveDirectoryUserStoreManager";
    private final String domainId = "WSO2TEST.COM";
    private final static String USER_STORE_DB_NAME = "JDBC_USER_STORE_ADDING_DB";
    private UserManagementClient userMgtClient;
    private AuthenticatorClient authenticatorClient;
    private String newUserName = "WSO2TEST.COM/userStoreUser";
    private String newUserRole = "WSO2TEST.COM/jdsbUserStoreRole";
    private String  newUserPassword = "Wso2@test";


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userStoreCountServiceClient = new UserStoreCountServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userStoreConfigAdminServiceClient.deleteUserStore(domainId);
    }

    @Test(groups = "wso2.is", description = "Check user store manager implementations")
    public void testAvailableUserStoreClasses() throws Exception {
        String[] classes = userStoreConfigAdminServiceClient.getAvailableUserStoreClasses();
        List<String> classNames = Arrays.asList(classes);
        Assert.assertTrue(classNames.contains(jdbcClass), jdbcClass + " not present in User Store List.");
        Assert.assertTrue(classNames.contains(rwLDAPClass), rwLDAPClass + " not present.");
        Assert.assertTrue(classNames.contains(roLDAPClass), roLDAPClass + " not present.");
        Assert.assertTrue(classNames.contains(adLDAPClass), adLDAPClass + " not present.");

    }

    @Test(groups = "wso2.is", description = "Check add user store via DTO",
            dependsOnMethods = "testAvailableUserStoreClasses")
    private void testAddJDBCUserStore() throws Exception {

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(jdbcClass, domainId,
                userStoreConfigUtils.getJDBCUserStoreProperties(USER_STORE_DB_NAME));
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Assert.assertTrue(userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, domainId)
                , "Domain addition via DTO has failed.");

    }

    @Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore")
    public void addUserIntoJDBCUserStore() throws Exception {
        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);


        userMgtClient.addRole(newUserRole, null, new String[]{PERMISSION_LOGIN});
        Assert.assertTrue(userMgtClient.roleNameExists(newUserRole)
                , "Role name doesn't exists");

        userMgtClient.addUser(newUserName, newUserPassword, new String[]{newUserRole}, null);
        Assert.assertTrue(userMgtClient.userNameExists(newUserRole, newUserName), "User name doesn't exists");

        String sessionCookie = authenticatorClient.login(newUserName, newUserPassword, isServer
                .getInstance().getHosts().get("default"));
        Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
        authenticatorClient.logOut();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore")
    public void testUserRoleCacheWithSecondary() throws Exception {

        String loginRole = "login";
        String secondaryUsername = "WSO2TEST.COM/user4219";
        String secondaryUserPassword = "Password@2";
        String primaryUsername = "user4219";
        String primaryUserPassword = "Password@1";

        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        authenticatorClient = new AuthenticatorClient(backendURL);
        userMgtClient.addRole(loginRole, null, new String[]{PERMISSION_LOGIN});
        userMgtClient.addUser(secondaryUsername, secondaryUserPassword, new String[]{}, null);
        userMgtClient.addUser(primaryUsername, primaryUserPassword, new String[]{loginRole}, null);
        authenticatorClient.unsuccessfulLogin(primaryUsername, primaryUserPassword, isServer
                .getInstance().getHosts().get("default"));
        Assert.assertFalse(authenticatorClient.unsuccessfulLogin(secondaryUsername, secondaryUserPassword, isServer
                .getInstance().getHosts().get("default")), "User from secondary user store logged in without login " +
                "permissions.");
        userMgtClient.deleteUser(primaryUsername);
        userMgtClient.deleteUser(secondaryUsername);
        userMgtClient.deleteRole(loginRole);
        authenticatorClient.logOut();
    }

    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
    public void changePassWordByUserTest() throws Exception {
        try{
            userMgtClient.changePasswordByUser(newUserName, newUserPassword, "Password@2");
        } catch (UserAdminUserAdminException e) {
            Assert.fail("password change by user for secondary User Store failed");
        }

    }

    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
    public void getCountEnabledUserStores() throws Exception {
        Assert.assertTrue(userStoreCountServiceClient.getCountableUserStores().contains(domainId), "no count enabled " +
                "user stores");
    }

//    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
//    public void countUsersInDomain() throws Exception {
//        Assert.assertEquals(1, userStoreCountServiceClient.countUsersInDomain("%",domainId), "user count failed");
//    }

//    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
//    public void countRolesInDomain() throws Exception {
//        Assert.assertEquals(1, userStoreCountServiceClient.countRolesInDomain("%", domainId), "role count failed");
//    }

//    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
//    public void countUsers() throws Exception {
//        Map<String, String> users =  userStoreCountServiceClient.countUsers("%");
//        Assert.assertEquals(Long.valueOf(1), Long.valueOf(users.get(domainId)));
//    }

//    @Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore")
//    public void countRoles() throws Exception {
//        Map<String, String> roles =  userStoreCountServiceClient.countRoles("%");
//        Assert.assertEquals(Long.valueOf(1), Long.valueOf(roles.get(domainId)));
//        Assert.assertNull(roles.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME));
//    }
//
@Test(groups = "wso2.is", dependsOnMethods = {"getCountEnabledUserStores"})
    public void deleteUserFromJDBCUserStore() throws Exception {
        userMgtClient.deleteUser(newUserName);
        Assert.assertFalse(Utils.nameExists(userMgtClient.listAllUsers(newUserName, 10)
                , newUserName), "User Deletion failed");

        userMgtClient.deleteRole(newUserRole);
        Assert.assertFalse(Utils.nameExists(userMgtClient.getAllRolesNames(newUserRole, 100), newUserRole), "User Role still exist");
    }
}
