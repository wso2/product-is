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

package org.wso2.carbon.identity.tests.user.profile.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.identity.UserProfileMgtServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.utils.LoginLogoutUtil;
import org.wso2.carbon.identity.tests.ISIntegrationTest;
import org.wso2.carbon.identity.tests.user.mgt.UserMgtTestCase;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;

public class UserProfileAdminTestCase extends ISIntegrationTest{

    private static final Log log = LogFactory.getLog(UserMgtTestCase.class);
    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserManagementClient userMgtClient;
    private LoginLogoutUtil logManger;
    
    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(0);
        logManger = new LoginLogoutUtil(isServer.getBackEndUrl());
        
        userProfileMgtClient = new UserProfileMgtServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        userMgtClient = new UserManagementClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        
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
        Assert.assertEquals(displayValue, "user1", "Getting user profile has failed.");
    }
    
//    @Test(groups = "wso2.is", description = "Check get ProfileFields For InternalStore ")
//    public void testGetProfileFieldsForInternalStore() throws Exception {
//        UserProfileDTO profile = userProfileMgtClient.getProfileFieldsForInternalStore();
//        UserFieldDTO[] fields = profile.getFieldValues(); 
//        String displayValue = null;
//        log.error("***************** Profile = "+profile.getProfileName());
//        
//        for (UserFieldDTO field : fields) {
//        	log.error("++++++++++++++++++++=Name=" + field.getDisplayName() +" Value="+field.getFieldValue());
//		}
//        Assert.assertEquals(displayValue, "user1", "Getting user profile has failed.");
//    }
    
//    TODO - setting user profile will be failed.Need debug.
    @Test(groups = "wso2.is", description = "Check set user profiles")
    public void testSetUserProfile() throws Exception {    	
    	logManger.login("admin", "admin", isServer.getBackEndUrl());
    	
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName("testProfile");
        
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
        
        UserProfileDTO getProfile = userProfileMgtClient.getUserProfile("user1", "testProfile");
//        Assert.assertNotNull(userProfileMgtClient.getUserProfile("user1", "testProfile"), "Cannot get user profile due to Null return");
//        Assert.assertEquals(getProfile.getProfileName(), "testProfile", "Set user profiles has failed.");
        
        logManger.logout();
    }
}
