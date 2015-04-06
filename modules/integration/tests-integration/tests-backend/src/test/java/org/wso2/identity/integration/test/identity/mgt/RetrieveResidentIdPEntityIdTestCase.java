/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.TenantManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class RetrieveResidentIdPEntityIdTestCase extends ISIntegrationTest {

    public static final String IDP_ENTITY_ID = "IdPEntityId";
    public static final String AUTHENTICATOR_NAME = "samlsso";

    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private ServerConfigurationManager serverConfigurationManager;
    private AuthenticatorClient loginManger;
    private File identityXML;
    private TenantManagementServiceClient tenantServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        changeISConfiguration();
        super.init();
        tenantServiceClient = new TenantManagementServiceClient( isServer.getContextUrls().getBackEndUrl(),
                                                                 sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        resetISConfiguration();
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing create new tenant")
    public void testCreateTenant() throws Exception {

        tenantServiceClient.addTenant("friends.com", "phoebe", "password", "phoebe@friends.com", "Phoebe", "Buffay");
        Assert.assertNotNull(tenantServiceClient.getTenant("friends.com"), "Unable to create a new tenant");
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing resident IdP entity Id value for new tenant",
          dependsOnMethods = {"testCreateTenant"})
    public void testResidentIdPEntityId() throws Exception {

        boolean residentIdPEntityIdChanged = false;
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                (null, null);
        loginManger = new AuthenticatorClient(isServer.getContextUrls().getBackEndUrl());
        String cookie = loginManger.login("phoebe@friends.com", "password", isServer.getInstance().getHosts().get("default"));
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(cookie, isServer.getContextUrls().getBackEndUrl(),
                                                                   configContext);

        IdentityProvider residentIdP = idpMgtServiceClient.getResidentIdP();
        FederatedAuthenticatorConfig[] configs = residentIdP.getFederatedAuthenticatorConfigs();
        if (configs != null) {
            for (FederatedAuthenticatorConfig config : configs) {
                if (AUTHENTICATOR_NAME.equals(config.getName())) {
                    Property[] properties = config.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IDP_ENTITY_ID.equals(property.getName())) {
                                residentIdPEntityIdChanged = "this_is_a_test".equals(property.getValue());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        Assert.assertTrue(residentIdPEntityIdChanged, "Unable to modify resident IdP entity value through identity" +
                                                      ".xml");
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing identity.xml changing the entity id of SSOService");

        String carbonHome = CarbonUtils.getCarbonHome();
        identityXML = new File(carbonHome + File.separator
                               + "repository" + File.separator + "conf" + File.separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation()
                                              + File.separator + "identityMgt" + File.separator
                                              + "identity-ssoservice-entityid-changed.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }
}
