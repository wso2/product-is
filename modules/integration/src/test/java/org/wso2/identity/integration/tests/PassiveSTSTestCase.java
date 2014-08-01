/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.identity.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.sts.passive.stub.IdentityPassiveSTSServiceStub;
import org.wso2.carbon.identity.sts.passive.stub.types.ClaimDTO;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

public class PassiveSTSTestCase {
    private static Log logger = LogFactory.getLog(PassiveSTSTestCase.class);
    private IdentityPassiveSTSServiceStub passiveSTSServiceStub = null;

    @BeforeClass(groups = {"wso2.is"})
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(9763);
        logger.debug("Running Passive STS Tests...");
        passiveSTSServiceStub = new IdentityPassiveSTSServiceStub(UserAdminConstants.PASSIVE_STS_SERVICE_URL);
        assertNotEquals(passiveSTSServiceStub, null, "Error in instantiation passive sts service stub.");
    }

    /**
     * This test case is based on https://wso2.org/jira/browse/CARBON-12175.
     *
     * @throws Exception
     */
    @Test(groups = "wso2.is")
    public void addTrustedService() throws Exception {
        String realmName = "http://sharepoint.server/wso2is";
        String claimDialect = "http://wso2.org/claims";
        String claims = "http://wso2.org/claims/emailaddress,http://wso2.org/claims/givenname";
        passiveSTSServiceStub.addTrustedService(realmName, claimDialect, claims);

        ClaimDTO claimDTO = passiveSTSServiceStub.getTrustedServiceClaims(realmName);
        assertNotNull(claimDTO);
        assertEquals(claimDialect, claimDTO.getClaimDialect());
        assertEquals(realmName, claimDTO.getRealm());
    }

}