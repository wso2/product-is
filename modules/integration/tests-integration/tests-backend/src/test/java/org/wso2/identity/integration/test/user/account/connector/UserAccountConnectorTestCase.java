/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.user.account.connector;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.account.association.stub.UserAccountAssociationServiceStub;
import org.wso2.carbon.identity.user.account.association.stub.types.UserAccountAssociationDTO;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.account.connector.UserAccountConnectorServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class UserAccountConnectorTestCase extends ISIntegrationTest {

    private static final String PERMISSION_ADMIN_LOGIN = "/permission/admin/login";
    private UserAccountConnectorServiceClient serviceClient;
    private final static String ADMIN_USER = "admin";
    private final static String USER_1 = "testuser11";
    private final static String USER_PASSWORD_1 = "testuser11";
    private final static String USER_2 = "testuser11@wso2.com";
    private final static String USER_PASSWORD_2 = "testuser11";
    private ConfigurationContext configContext;
    private UserManagementClient userMgtClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        serviceClient = new UserAccountConnectorServiceClient(sessionCookie, backendURL, configContext);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        serviceClient = null;
    }

    @Test(alwaysRun = true, description = "Test create user account associations",
            dependsOnMethods = { "testAssociateTwoAccountsAsAdmin" })
    public void testConnectUserAccount() throws Exception {

        // Create associations
        serviceClient.createUserAccountAssociation(USER_1, USER_PASSWORD_1.split("(?!^)"));
        serviceClient.createUserAccountAssociation(USER_2, USER_PASSWORD_2.split("(?!^)"));

        // Retrieve associations
        UserAccountAssociationDTO[] associations = serviceClient.getAccountAssociationsOfUser();

        Assert.assertTrue(associations != null && associations.length > 0, "Unable to create user account association" +
                                                                           " for user" );
        Assert.assertTrue(isAssociationAvailable(associations, USER_1), "Unable to create user association with a " +
                                                                        "super tenant user");
        Assert.assertTrue(isAssociationAvailable(associations, USER_2), "Unable to create user association with a " +
                                                                        "tenant user");

    }

    @Test(alwaysRun = true, description = "Test create user account associations as admin")
    public void testAssociateTwoAccountsAsAdmin() throws Exception {

        String loginRole = "loginRole";
        String testAssociationUser1 = "testAssociationUser1";
        String testPassword1 = "testPassword1";
        String testAssociationUser2 = "testAssociationUser2";
        String testPassword2 = "testPassword2";
        String testAssociationUser3 = "testAssociationUser3";
        String testPassword3 = "testPassword3";


        userMgtClient.addRole(loginRole, new String[0], new String[]{PERMISSION_ADMIN_LOGIN});
        userMgtClient.addUser(testAssociationUser1, testPassword1, new String[]{loginRole}, null);
        userMgtClient.addUser(testAssociationUser2, testPassword2, new String[]{loginRole}, null);
        userMgtClient.addUser(testAssociationUser3, testPassword3, new String[]{loginRole}, null);
        UserAccountConnectorServiceClient loginClient = new UserAccountConnectorServiceClient(testAssociationUser1,
                testPassword1, backendURL, configContext);
        try {
            loginClient.associateTwoAccounts(testAssociationUser2, testAssociationUser3);
            Assert.fail("User without admin permission can't associate two other users.");
        } catch (Exception e) {

        }
        UserAccountAssociationDTO[] associationDTOs = serviceClient.getAccountAssociations(testAssociationUser2);
        Assert.assertTrue(associationDTOs == null || associationDTOs.length == 0, "Accounts has been associated by a" +
                " user without proper permissions.");
        serviceClient.associateTwoAccounts(testAssociationUser2, testAssociationUser3);
        associationDTOs = serviceClient.getAccountAssociations(testAssociationUser2);
        Assert.assertTrue(associationDTOs.length > 0, "Error while associating two accounts as admin user.");
        userMgtClient.deleteUser(testAssociationUser1);
        userMgtClient.deleteUser(testAssociationUser2);
        userMgtClient.deleteUser(testAssociationUser3);
        userMgtClient.deleteRole(loginRole);

    }

    @Test(alwaysRun = true, description = "Test switch user logged in account ",
          dependsOnMethods = { "testConnectUserAccount" })
    public void switchLoggedInUser() throws Exception {

        serviceClient.switchLoggedInUser(USER_1);

        UserAccountAssociationDTO [] associations = serviceClient.getAccountAssociationsOfUser();

        Assert.assertTrue(isAccountSwitched(associations, USER_1), "Unable to switch user to a super tenant " +
                                                                         "user");

        serviceClient.switchLoggedInUser(USER_2);

        associations = serviceClient.getAccountAssociationsOfUser();

        Assert.assertTrue(isAccountSwitched(associations, USER_2), "Unable to switch user to a tenant user");

        serviceClient.switchLoggedInUser(ADMIN_USER);
    }

    @Test(alwaysRun = true, description = "Test delete user account association",
          dependsOnMethods = { "switchLoggedInUser" })
    public void testDeleteUserAccountConnection() throws Exception {

        serviceClient.deleteUserAccountAssociation(USER_1);

        UserAccountAssociationDTO [] associations = serviceClient.getAccountAssociationsOfUser();

        Assert.assertFalse(isAssociationAvailable(associations, USER_1), "Unable to delete user association of a " +
                                                                         "super tenant user");

        serviceClient.deleteUserAccountAssociation(USER_2);

        associations = serviceClient.getAccountAssociationsOfUser();

        Assert.assertFalse(isAssociationAvailable(associations, USER_2),  "Unable to delete user association of a " +
                                                                          "tenant user");

    }

    private boolean isAssociationAvailable(UserAccountAssociationDTO [] associations, String userName){
        if(associations != null){
            for(UserAccountAssociationDTO association : associations){
                String connectedUserName = association.getUsername();
                if(!"PRIMARY".equals(association.getDomain())) {
                    connectedUserName = association.getDomain() + "/" + connectedUserName;
                }
                if(!"carbon.super".equals(association.getTenantDomain())) {
                    connectedUserName = connectedUserName + "@" + association.getTenantDomain();
                }
                if(userName.equals(connectedUserName)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAccountSwitched(UserAccountAssociationDTO [] associations, String userName){
        if(associations != null && associations.length > 0){
            for(UserAccountAssociationDTO association : associations){
                String connectedUserName = association.getUsername();
                if(!"PRIMARY".equals(association.getDomain())) {
                    connectedUserName = association.getDomain() + "/" + connectedUserName;
                }
                if(!"carbon.super".equals(association.getTenantDomain())) {
                    connectedUserName = connectedUserName + "@" + association.getTenantDomain();
                }
                if(userName.equals(connectedUserName)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
