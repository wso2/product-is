/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.profile.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.InMemoryClaimManager;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStoreImpl;
import org.wso2.carbon.security.caas.user.core.store.IdentityStoreImpl;
import org.wso2.carbon.user.profile.service.UserAccountAssociationClientService;
import org.wso2.carbon.user.profile.service.UserProfileManagementException;

public class UserAccountAssociationClientServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountAssociationClientServiceImplTest.class);

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {

    }

    @org.testng.annotations.AfterMethod
    public void tearDown() throws Exception {

    }

    @org.testng.annotations.Test
    public void testListUserAssociations() throws Exception {
        UserAccountAssociationClientService userAccountAssociationClientService = getUserAccountAssociationClientService();
        User testUser = new User.UserBuilder().setUserId("testUser").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();
        Assert.assertNotNull(userAccountAssociationClientService.listUserAssociations(testUser));
    }

    @Test
    public void testAddUserAssociation() throws Exception {
        UserAccountAssociationClientService userAccountAssociationClientService = getUserAccountAssociationClientService();
        User testUser = new User.UserBuilder().setUserId("testUser").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();

        User associationUser1 = new User.UserBuilder().setUserId("testUser1").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();

        //Expect no exception
        userAccountAssociationClientService.addUserAssociation(testUser, associationUser1);

        //Check error condition handling
        try{
            userAccountAssociationClientService.addUserAssociation(null, associationUser1);
            Assert.fail("There should be an exception when primary user is null");
        } catch (UserProfileManagementException e) {
            //Ignore as it is expected
        }

        //Null associated user will not cause any exceptions
        userAccountAssociationClientService.addUserAssociation(testUser, null);

        //Should not allow association of the same user to itself
        try{
            userAccountAssociationClientService.addUserAssociation(testUser, testUser);
            Assert.fail("The same user is associated itself. Should throw an error.");
        } catch (UserProfileManagementException e) {
            //Ignore as it is expected
        }
    }

    private UserAccountAssociationClientService getUserAccountAssociationClientService() {
        return new UserAccountAssociationClientServiceImpl();
    }
}