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
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

/**
 * This test case tests provisioning functionality when UM tables and IDN tables are separated into two databases
 */
public class DBSeperationTestCase extends ISIntegrationTest {

    private static final String TENANT_IDP = "tenantIdp";
    private static final String TENANT_ADMIN = "admin";
    private File masterDatasourceXml;
    private File identityXml;
    private File userMgtXml;
    private File registryXml;
    private ServerConfigurationManager serverConfigurationManager;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private TenantManagementServiceClient tenantServiceClient;
    private String TENANT_DOMAIN = "tenant.com";
    private LogViewerClient logViewer;
    private String serverConfDir;
    private String artifactsDir;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();

        serverConfDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf";
        artifactsDir = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS" + File
                .separator + "provisioning";

        masterDatasourceXml = new File(serverConfDir + File.separator + "datasources" + File.separator +
                "master-datasources.xml");
        File masterDatasourcesXmlToCopy = new File(artifactsDir + File.separator + "master-datasources-dbseperated" +
                ".xml");

        identityXml = new File(serverConfDir + File.separator + "identity" + File.separator + "identity.xml");
        File identityXmlToCopy = new File(artifactsDir + File.separator + "identity-dbseperated.xml");

        userMgtXml = new File(serverConfDir + File.separator + "user-mgt.xml");
        File userMgtXmlToCopy = new File(artifactsDir + File.separator + "user-mgt-dbseperated.xml");

        registryXml = new File(serverConfDir + File.separator + "registry.xml");
        File registryXmlToCopy = new File(artifactsDir + File.separator + "registry-dbseperated.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(masterDatasourcesXmlToCopy, masterDatasourceXml,
                true);
        serverConfigurationManager.applyConfigurationWithoutRestart(identityXmlToCopy, identityXml, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(userMgtXmlToCopy, userMgtXml, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(registryXmlToCopy, registryXml,
                true);
        serverConfigurationManager.restartGracefully();

        super.init();

        tenantServiceClient = new TenantManagementServiceClient(backendURL, sessionCookie);
        tenantServiceClient.addTenant(TENANT_DOMAIN, TENANT_ADMIN, "password", TENANT_ADMIN + "@" + TENANT_DOMAIN,
                TENANT_ADMIN, "User");
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(TENANT_ADMIN + "@" + TENANT_DOMAIN,
                "password", backendURL);
        logViewer = new LogViewerClient(backendURL, getSessionCookie());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        File masterDatasourcesXmlToCopy = new File(artifactsDir + File.separator + "master-datasources-default.xml");
        File identityXmlToCopy = new File(getISResourceLocation() + File.separator + "default-identity.xml");
        File userMgtXmlToCopy = new File(artifactsDir + File.separator + "user-mgt-default.xml");
        File registryXmlToCopy = new File(artifactsDir + File.separator + "registry-default.xml");

        serverConfigurationManager.applyConfigurationWithoutRestart(masterDatasourcesXmlToCopy, masterDatasourceXml,
                true);
        serverConfigurationManager.applyConfigurationWithoutRestart(identityXmlToCopy, identityXml, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(userMgtXmlToCopy, userMgtXml, true);
        serverConfigurationManager.applyConfigurationWithoutRestart(registryXmlToCopy, registryXml,
                true);
        serverConfigurationManager.restartGracefully();
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
