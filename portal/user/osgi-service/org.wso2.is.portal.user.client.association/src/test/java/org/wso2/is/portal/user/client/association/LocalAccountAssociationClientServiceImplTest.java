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

package org.wso2.is.portal.user.client.association;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.claim.InMemoryClaimManager;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStoreImpl;
import org.wso2.carbon.security.caas.user.core.store.IdentityStoreImpl;

public class LocalAccountAssociationClientServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(LocalAccountAssociationClientServiceImplTest.class);

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {

    }

    @org.testng.annotations.AfterMethod
    public void tearDown() throws Exception {

    }

    @org.testng.annotations.Test
    public void testListUserAssociations() throws Exception {
        LocalAccountAssociationClientService localAccountAssociationClientService = getUserAccountAssociationClientService();
        User testUser = new User.UserBuilder().setUserId("testUser").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();
        Assert.assertNotNull(localAccountAssociationClientService.listUserAssociations(testUser));
    }

    @Test
    public void testAddUserAssociation() throws Exception {
        LocalAccountAssociationClientService localAccountAssociationClientService = getUserAccountAssociationClientService();
        User testUser = new User.UserBuilder().setUserId("testUser").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();

        User associationUser1 = new User.UserBuilder().setUserId("testUser1").setIdentityStore(new IdentityStoreImpl())
                .setAuthorizationStore(new AuthorizationStoreImpl()).setPrimaryAttributeValue("userId")
                .setClaimManager(new InMemoryClaimManager()).build();

        //Expect no exception
        localAccountAssociationClientService.addUserAssociation(testUser, associationUser1);

        //Check error condition handling
        try{
            localAccountAssociationClientService.addUserAssociation(null, associationUser1);
            Assert.fail("There should be an exception when primary user is null");
        } catch (UserAccountAssociationException e) {
            //Ignore as it is expected
        }

        //Null associated user will not cause any exceptions
        localAccountAssociationClientService.addUserAssociation(testUser, null);

        //Should not allow association of the same user to itself
        try{
            localAccountAssociationClientService.addUserAssociation(testUser, testUser);
            Assert.fail("The same user is associated itself. Should throw an error.");
        } catch (UserAccountAssociationException e) {
            //Ignore as it is expected
        }
    }

    private LocalAccountAssociationClientService getUserAccountAssociationClientService() {
        return new LocalAccountAssociationClientServiceImpl();
    }
}