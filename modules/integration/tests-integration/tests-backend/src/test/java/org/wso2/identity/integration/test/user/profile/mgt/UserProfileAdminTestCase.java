/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.profile.mgt;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.nio.file.Paths;

public class UserProfileAdminTestCase extends ISIntegrationTest {

    public static final String IDP_NAME = "idp1";
    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserManagementClient userMgtClient;
    private AuthenticatorClient logManger;
    private String userId1 = "UserProfileAdminTestUser1";
    private ServerConfigurationManager serverConfigurationManager;
    private IdentityProviderMgtServiceClient idpMgtClient;
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "user" + File.separator
                        + "enable_federated_association.toml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.init();
        logManger = new AuthenticatorClient(backendURL);
        
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(userId1);

        userMgtClient.addUser(userId1, "passWord1@", new String[]{"admin"}, "default", new ClaimValue[]{lastName});
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userMgtClient.deleteUser(userId1);
        idpMgtClient.deleteIdP(IDP_NAME);
        logManger = null;
        log.info("Replacing identity.xml with default configurations");

        serverConfigurationManager.restoreToLastConfiguration(false);

    }
    
    @Test(priority = 1, groups = "wso2.is", description = "Check get user profiles")
    public void testGetUserProfiles() throws Exception {
        super.init();
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);

        UserProfileDTO[] profiles = userProfileMgtClient.getUserProfiles(userId1);
        String profile = null;
        
        for (UserProfileDTO userProfileDTO : profiles) {
			profile = userProfileDTO.getProfileName();
		}
        Assert.assertEquals(profile, "default", "Getting user profiles has failed.");
    }
    
    @Test(priority = 2, groups = "wso2.is", description = "Check get user profile")
    public void testGetUserProfile() throws Exception {
        super.init();
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);

        UserProfileDTO profile = userProfileMgtClient.getUserProfile(userId1, "default");
        UserFieldDTO[] fields = profile.getFieldValues(); 
        String displayValue = null;
        
        for (UserFieldDTO field : fields) {
        	if("Last Name".equals(field.getDisplayName())){
        		displayValue = field.getFieldValue();
        		break;
        	}
		}
        Assert.assertTrue(userId1.equals(displayValue), "Getting user profile has failed.");
    }

    /**
     * Setting a user profile updates the user claim values of the existing user profile of the user.
     * This test method tests the above behavior.
     *
     * @throws Exception
     */
    @Test(priority = 3, groups = "wso2.is", description = "Check set user profiles")
    public void testSetUserProfile() throws Exception {
        super.init();
        logManger = new AuthenticatorClient(backendURL);
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);

        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                        isServer.getSuperTenant().getTenantAdmin().getPassword(),
                        isServer.getInstance().getHosts().get("default"));
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName("default");

        UserFieldDTO lastName = new UserFieldDTO();
        lastName.setClaimUri("http://wso2.org/claims/lastname");
        lastName.setFieldValue("lastname");

        UserFieldDTO givenname = new UserFieldDTO();
        givenname.setClaimUri("http://wso2.org/claims/givenname");
        givenname.setFieldValue("firstname");

        UserFieldDTO email = new UserFieldDTO();
        email.setClaimUri("http://wso2.org/claims/emailaddress");
        email.setFieldValue("email@email.com");

        UserFieldDTO[] fields = new UserFieldDTO[3];
        fields[0] = lastName;
        fields[1] = givenname;
        fields[2] = email;

        profile.setFieldValues(fields);

        userProfileMgtClient.setUserProfile(userId1, profile);

        UserProfileDTO getProfile = userProfileMgtClient.getUserProfile(userId1, "default");
        UserFieldDTO[] updatedFields = getProfile.getFieldValues();
        for (UserFieldDTO updatedField : updatedFields) {
            if (updatedField.getClaimUri().equals("http://wso2.org/claims/lastname")) {
                Assert.assertEquals(updatedField.getFieldValue(), "lastname");
            } else if (updatedField.getClaimUri().equals("http://wso2.org/claims/givenname")) {
                Assert.assertEquals(updatedField.getFieldValue(), "firstname");
            } else if (updatedField.getClaimUri().equals("http://wso2.org/claims/emailaddress")) {
                Assert.assertEquals(updatedField.getFieldValue(), "email@email.com");
            }
        }

        logManger.logOut();
    }

    @Test(priority = 4, groups = "wso2.is", description = "Check Fed User Account Association")
    public void testUserAccountAssociationAdd() throws Exception {

        super.init();
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        String username = "testUser2";
        String password = "passWord1@";

        // create a user
        userMgtClient.addUser(username, password, new String[]{"admin"}, "default");
        Assert.assertTrue(userMgtClient.getUserList().contains(username));

        idpMgtClient = new IdentityProviderMgtServiceClient(username, password, backendURL);
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, username, password);

        IdentityProvider idp = new IdentityProvider();

        idp.setIdentityProviderName(IDP_NAME);
        idpMgtClient.addIdP(idp);
        Assert.assertNotNull(idpMgtClient.getIdPByName(IDP_NAME));

        // create a federated user account association
        userProfileMgtClient.addFedIdpAccountAssociation(IDP_NAME, "dummy_idp_account_1");
        userProfileMgtClient.addFedIdpAccountAssociation(IDP_NAME, "dummy_idp_account_2");

        AssociatedAccountDTO[] associatedFedUserAccountIds = userProfileMgtClient.getAssociatedFedUserAccountIds();
        Assert.assertNotNull(associatedFedUserAccountIds);
        Assert.assertEquals(associatedFedUserAccountIds.length, 2);

        // delete the user, this should clear the federated idp account associations
        userMgtClient.deleteUser(username);
        Assert.assertEquals(userMgtClient.getUserList().contains(username), false);

        // create the same user
        userMgtClient.addUser(username, password, new String[]{"admin"}, "default");
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, username, password);
        associatedFedUserAccountIds = userProfileMgtClient.getAssociatedFedUserAccountIds();
        // assert to make sure there are no federated idp user account associations for this user
        Assert.assertEquals(associatedFedUserAccountIds == null || associatedFedUserAccountIds.length == 0, true);

    }
}
