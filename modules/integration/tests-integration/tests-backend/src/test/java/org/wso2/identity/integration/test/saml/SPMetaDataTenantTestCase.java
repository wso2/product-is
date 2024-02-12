/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.saml;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData;
import org.wso2.identity.integration.common.clients.KeyStoreAdminClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * Test case for adding a meta data file in tenant domain.
 */
public class SPMetaDataTenantTestCase extends ISIntegrationTest {

    private SAMLSSOConfigServiceClient ssoConfigServiceClient;

    private static final String ISSUER = "metadata-sp";
    private static final String CERT_ALIAS = "metadata-sp";
    private static final String ADMIN_USERNAME = "admin@wso2.com";
    private static final String ADMIN_PASSWORD = "Wso2@test";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test(testName = "metaDataTestcase")
    public void addSPMetadataForTenant() throws Exception {

        String filePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator + "sp-metadata.xml";

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String metadataXml = new String(encoded, StandardCharsets.UTF_8);

        //Load the metadata file to the client.
        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = ssoConfigServiceClient.uploadServiceProvider(metadataXml);
        Assert.assertEquals(samlssoServiceProviderDTO.getIssuer(), ISSUER);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTOGet = null;
        for (SAMLSSOServiceProviderDTO serviceProviderDTO : samlssoServiceProviderDTOs) {
            if (ISSUER.equals(serviceProviderDTO.getIssuer())) {
                samlssoServiceProviderDTOGet = serviceProviderDTO;
            }
        }
        Assert.assertNotNull(samlssoServiceProviderDTOGet);
        Assert.assertEquals(samlssoServiceProviderDTOGet.getCertAlias(), CERT_ALIAS);

        KeyStoreAdminClient keyStoreAdminClient = new KeyStoreAdminClient(backendURL, ADMIN_USERNAME, ADMIN_PASSWORD);
        KeyStoreData[] keyStoreDataArray = keyStoreAdminClient.getKeyStores();
        Assert.assertTrue(keyStoreDataArray != null && keyStoreDataArray.length > 0);

        String [] storeEntries = keyStoreAdminClient.getStoreEntries("wso2-com.jks");
        Assert.assertTrue(storeEntries != null && storeEntries.length > 0);
        Assert.assertTrue(Arrays.asList(storeEntries).contains(CERT_ALIAS));
    }

    @Test(testName = "metaDataTestcase", dependsOnMethods = "addSPMetadataForTenant")
    public void restartTestServer() throws Exception {

        super.init();
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.restartGracefully();
        super.init(TestUserMode.TENANT_ADMIN);

        KeyStoreAdminClient keyStoreAdminClient = new KeyStoreAdminClient(backendURL, ADMIN_USERNAME, ADMIN_PASSWORD);
        KeyStoreData[] keyStoreDataArray = keyStoreAdminClient.getKeyStores();
        Assert.assertTrue(keyStoreDataArray != null && keyStoreDataArray.length > 0);

        String [] storeEntries = keyStoreAdminClient.getStoreEntries("wso2-com.jks");
        Assert.assertTrue(storeEntries != null && storeEntries.length > 0);
        Assert.assertTrue(Arrays.asList(storeEntries).contains(CERT_ALIAS));
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {

        ssoConfigServiceClient.removeServiceProvider(ISSUER);
    }
}
