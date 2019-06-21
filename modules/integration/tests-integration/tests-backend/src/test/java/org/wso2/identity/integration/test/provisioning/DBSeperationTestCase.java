/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.provisioning;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

/**
 * This test case tests provisioning functionality when UM tables and IDN tables are separated into two databases
 */
public class DBSeperationTestCase extends ISIntegrationTest {

    private static final String TENANT_IDP = "tenantIdp";
    private static final String TENANT_ADMIN = "admin";
    private ServerConfigurationManager serverConfigurationManager;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private static final String TENANT_DOMAIN = "tenant.com";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + File.separator +
                "provisioning" + File.separator + "db_separation_config.toml");

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        super.init();
        TenantManagementServiceClient tenantServiceClient = new TenantManagementServiceClient(backendURL, sessionCookie);
        tenantServiceClient.addTenant(TENANT_DOMAIN, TENANT_ADMIN, "password", TENANT_ADMIN + "@" + TENANT_DOMAIN,
                TENANT_ADMIN, "User");
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(TENANT_ADMIN + "@" + TENANT_DOMAIN,
                "password", backendURL);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    @Test(alwaysRun = true, description = "Testing update Identity Provider")
    public void updateIdpTest() {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(TENANT_IDP);
            ProvisioningConnectorConfig scimConfig = new ProvisioningConnectorConfig();
            scimConfig.setName("scim");
            Property userProperty = new Property();
            userProperty.setName("scim-username");
            userProperty.setValue("admin");
            scimConfig.addProvisioningProperties(userProperty);
            identityProvider.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] { scimConfig });
            identityProviderMgtServiceClient.addIdP(identityProvider);
            identityProviderMgtServiceClient.deleteIdP(TENANT_IDP);
            IdentityProvider deletedIdP = identityProviderMgtServiceClient.getIdPByName(TENANT_IDP);
            Assert.assertNull("Failed to delete the IdP :" + TENANT_IDP, deletedIdP);
        } catch (Exception e) {
            Assert.fail("Identity Provider addition or deletion failed at database separation test.");
        }
    }
}
