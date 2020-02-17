/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.user.store;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

import java.rmi.RemoteException;

/**
 * This class contains test cases for claim mappings on secondary userstores.
 */
public class ClaimMappingsOnSecondaryUserStoreTestCase extends ISIntegrationTest {

    private static final String CLAIM_URI = "http://wso2.org/claims/test";
    private static final String PRIMARY_DOMAIN_MAPPED_ATTRIBUTE = "testPrimary";
    private static final String SECONDARY_DOMAIN_MAPPED_ATTRIBUTE = "testSec";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
    private static final String DOMAIN_ID = "CLAIM.MAPPING.ON.SECONDARY.USERSTORE.TEST.COM";
    private static final String USER_STORE_DB_NAME = "CLAIM_MAPPING_ON_SECONDARY_USER_STORE_DB";
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        claimMetadataManagementServiceClient.removeLocalClaim(CLAIM_URI);
    }

    private void addLocalClaims() throws RemoteException, ClaimMetadataManagementServiceClaimMetadataException {

        LocalClaimDTO localClaimDTO = new LocalClaimDTO();
        localClaimDTO.setLocalClaimURI(CLAIM_URI);
        AttributeMappingDTO[] attributeMappingDTO = new AttributeMappingDTO[2];
        AttributeMappingDTO attributeMappingDTO1 = new AttributeMappingDTO();
        attributeMappingDTO1.setAttributeName(PRIMARY_DOMAIN_MAPPED_ATTRIBUTE);
        attributeMappingDTO1.setUserStoreDomain(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        attributeMappingDTO[0] = attributeMappingDTO1;

        AttributeMappingDTO attributeMappingDTO2 = new AttributeMappingDTO();
        attributeMappingDTO2.setAttributeName(SECONDARY_DOMAIN_MAPPED_ATTRIBUTE);
        attributeMappingDTO2.setUserStoreDomain(DOMAIN_ID);
        attributeMappingDTO[1] = attributeMappingDTO2;
        localClaimDTO.setAttributeMappings(attributeMappingDTO);
        claimMetadataManagementServiceClient.addLocalClaim(localClaimDTO);
    }

    @Test(groups = "wso2.is", description = "Check add user store via DTO")
    private void testAddJDBCUserStore() throws Exception {

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(JDBC_CLASS, DOMAIN_ID,
                userStoreConfigUtils.getJDBCUserStoreProperties(USER_STORE_DB_NAME));
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        Assert.assertTrue(userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID)
                , "Domain addition via DTO has failed.");
    }

    @Test(groups = "wso2.is", description = "Check userstore delete", dependsOnMethods = {"testAddJDBCUserStore"})
    public void testUserstoreDeletion() throws Exception {

        addLocalClaims();
        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
        for (LocalClaimDTO localClaimDTO : claimMetadataManagementServiceClient.getLocalClaims()) {
            for (AttributeMappingDTO attributeMappingDTO : localClaimDTO.getAttributeMappings()) {
                Assert.assertNotEquals(attributeMappingDTO.getUserStoreDomain(), DOMAIN_ID);
            }
        }
    }
}
