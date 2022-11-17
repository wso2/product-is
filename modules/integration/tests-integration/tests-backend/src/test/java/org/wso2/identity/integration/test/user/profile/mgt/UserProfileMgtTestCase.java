/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.user.profile.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class UserProfileMgtTestCase extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(UserProfileMgtTestCase.class);
    private String userId1 = "UserProfileMgtTestUser1";
    private UserProfileMgtServiceClient userProfileMgtClient;
    private UserManagementClient userMgtClient;
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(userId1);

        userMgtClient.addUser(userId1, "passWord1@", new String[]{"admin"}, "default", new ClaimValue[]{lastName});
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.is", description = "Check get user profiles")
    public void testGetUserProfiles() throws Exception {
        UserProfileDTO[] profiles = userProfileMgtClient.getUserProfiles(userId1);
        String profile = null;

        for (UserProfileDTO userProfileDTO : profiles) {
            profile = userProfileDTO.getProfileName();
        }
        Assert.assertEquals(profile, "default", "Getting user profiles has failed.");
    }

    @Test(groups = "wso2.is", description = "Check get user profile")
    public void testGetUserProfile() throws Exception {
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

    @Test(groups = "wso2.is", description = "Check is add profile enabled")
    public void testIsAddProfileEnabled() throws Exception {
        boolean isAddProfileEnabled = userProfileMgtClient.isAddProfileEnabled();
        Assert.assertTrue(isAddProfileEnabled, "Getting is add profile enabled has failed.");
    }

    @Test(groups = "wso2.is", description = "Check is add profile enabled")
    public void testIsAddProfileEnabledForDomain() throws Exception {
        boolean isAddProfileEnabledForDomain = userProfileMgtClient.isAddProfileEnabledForDomain("carbon.super");
        Assert.assertFalse(isAddProfileEnabledForDomain, "Getting is add profile enabled for domain has failed.");
    }

    @Test(groups = "wso2.is", description = "Check is add profile enabled")
    public void testIsReadOnlyUserStore() throws Exception {
        boolean isReadOnlyUserStore = userProfileMgtClient.isReadOnlyUserStore();
        Assert.assertFalse(isReadOnlyUserStore, "Getting is is read only user store has failed.");
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        userMgtClient.deleteUser(userId1);
    }
}
