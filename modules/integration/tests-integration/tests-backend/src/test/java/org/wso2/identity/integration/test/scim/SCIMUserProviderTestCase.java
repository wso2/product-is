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

package org.wso2.identity.integration.test.scim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.scim.common.stub.config.SCIMProviderDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.identity.integration.common.clients.scim.SCIMConfigAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class SCIMUserProviderTestCase extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(SCIMUserProviderTestCase.class);
    private SCIMConfigAdminClient scimConfigAdminClient;
    public static final String providerId = "testProvider";
    private String scim_Provider_url;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        scimConfigAdminClient = new SCIMConfigAdminClient(backendURL, sessionCookie);
        scim_Provider_url = backendURL.substring(0, 22) + "wso2/scim/";
    }

    @Test(description = "Add user service provider", priority = 1)
    public void testAddUserProvider() throws Exception {
        boolean providerAvailable = false;
        scimConfigAdminClient.addUserProvider(userInfo.getUserName(), providerId,
                                              MultitenantUtils.getTenantAwareUsername(userInfo.getUserName()),
                                              userInfo.getPassword(), scim_Provider_url + "Users",
                                              scim_Provider_url + "Groups");
        SCIMProviderDTO[] scimProviders = scimConfigAdminClient.listUserProviders(userInfo.getUserName(), providerId);
        for (SCIMProviderDTO scimProvider : scimProviders) {
            if (scimProvider.getProviderId().equals(providerId)) {
                providerAvailable = true;
            }
        }
        Assert.assertTrue(providerAvailable, "Provider adding failed");
    }

    @Test(description = "delete user service provider", dependsOnMethods = {"testAddUserProvider"}, priority = 2)
    public void testDeleteUserProvider() {
        boolean providerDeleted = false;
        try {
            scimConfigAdminClient.deleteUserProvider(userInfo.getUserName(), providerId);
            providerDeleted = true;
        }  catch (Exception e) {
            log.error("Provider [" + providerId + "] delete failed.", e);
        }
        Assert.assertTrue(providerDeleted, "Provider [" + providerId + "] delete failed");
    }
}
