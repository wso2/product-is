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
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class UserProfileAdminTestCase extends ISIntegrationTest {

    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserManagementClient userMgtClient;
    private AuthenticatorClient logManger;
    
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        logManger = new AuthenticatorClient(backendURL);
        
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        
        userMgtClient.addUser("user1", "passWord1@", new String[]{"admin"}, "default");
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userMgtClient.deleteUser("user1");
        logManger = null;

    }
    
    @Test(groups = "wso2.is", description = "Check get user profiles")
    public void testGetUserProfiles() throws Exception {
        UserProfileDTO[] profiles = userProfileMgtClient.getUserProfiles("user1");
        String profile = null;
        
        for (UserProfileDTO userProfileDTO : profiles) {
			profile = userProfileDTO.getProfileName();
		}
        Assert.assertEquals(profile, "default", "Getting user profiles has failed.");
    }
    
    @Test(groups = "wso2.is", description = "Check get user profile")
    public void testGetUserProfile() throws Exception {
        UserProfileDTO profile = userProfileMgtClient.getUserProfile("user1", "default");
        UserFieldDTO[] fields = profile.getFieldValues(); 
        String displayValue = null;
        
        for (UserFieldDTO field : fields) {
        	if("Last Name".equals(field.getDisplayName())){
        		displayValue = field.getFieldValue();
        		break;
        	}
		}
        Assert.assertTrue("user1".equals(displayValue) || "user1".equals(displayValue), "Getting user profile has failed.");
    }

    /**
     * Setting a user profile updates the user claim values of the existing user profile of the user.
     * This test method tests the above behavior.
     *
     * @throws Exception
     */
    @Test(groups = "wso2.is", description = "Check set user profiles")
    public void testSetUserProfile() throws Exception {
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

        userProfileMgtClient.setUserProfile("user1", profile);

        UserProfileDTO getProfile = userProfileMgtClient.getUserProfile("user1", "default");
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
}
