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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.saml;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

public class MetadataTestCase extends ISIntegrationTest {

    private SAMLSSOConfigServiceClient ssoConfigServiceClient;

    private static final String ISSUER = "metadata-sp";
    private static final String[] ACSs = {"https://metadata-sp:8080/federation/Consumer1/metaAlias/sp",
            "https://metadata-sp:8080/federation/Consumer2/metaAlias/sp"};

    @Test
    public void addSPMetadata() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);

        String filePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator
                + "sp-metadata.xml";

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String metadataXml = new String(encoded, StandardCharsets.UTF_8);

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = ssoConfigServiceClient.uploadServiceProvider(metadataXml);
        Assert.assertEquals(samlssoServiceProviderDTO.getIssuer(), ISSUER);

        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTOGet = null;
        for (SAMLSSOServiceProviderDTO serviceProviderDTO : samlssoServiceProviderDTOs){
            if (ISSUER.equals(serviceProviderDTO.getIssuer())){
                samlssoServiceProviderDTOGet = serviceProviderDTO;
            }
        }
        Assert.assertNotNull(samlssoServiceProviderDTOGet);
        Assert.assertEquals(samlssoServiceProviderDTOGet.getDefaultAssertionConsumerUrl(), ACSs[0]);
        Assert.assertEquals(samlssoServiceProviderDTOGet.getAssertionConsumerUrls(), ACSs);
    }

    @AfterClass
    public void endTest() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        ssoConfigServiceClient.removeServiceProvider(ISSUER);
    }
}
