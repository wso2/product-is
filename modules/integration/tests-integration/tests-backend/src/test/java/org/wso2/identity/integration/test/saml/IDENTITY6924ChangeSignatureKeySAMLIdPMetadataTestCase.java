/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.saml;

import org.springframework.util.FileCopyUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class IDENTITY6924ChangeSignatureKeySAMLIdPMetadataTestCase extends ISIntegrationTest {
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;


    private static final String CERTIFICATE =
            "MIICNTCCAZ6gAwIBAgIES343gjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJVUzELMAkGA1UE\n" +
            "CAwCQ0ExFjAUBgNVBAcMDU1vdW50YWluIFZpZXcxDTALBgNVBAoMBFdTTzIxEjAQBgNVBAMMCWxv\n" +
            "Y2FsaG9zdDAeFw0xMDAyMTkwNzAyMjZaFw0zNTAyMTMwNzAyMjZaMFUxCzAJBgNVBAYTAlVTMQsw\n" +
            "CQYDVQQIDAJDQTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzENMAsGA1UECgwEV1NPMjESMBAGA1UE\n" +
            "AwwJbG9jYWxob3N0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUp/oV1vWc8/TkQSiAvTou\n" +
            "sMzOM4asB2iltr2QKozni5aVFu818MpOLZIr8LMnTzWllJvvaA5RAAdpbECb+48FjbBe0hseUdN5\n" +
            "HpwvnH/DW8ZccGvk53I6Orq7hLCv1ZHtuOCokghz/ATrhyPq+QktMfXnRS4HrKGJTzxaCcU7OQID\n" +
            "AQABoxIwEDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQEFBQADgYEAW5wPR7cr1LAdq+IrR44i\n" +
            "QlRG5ITCZXY9hI0PygLP2rHANh+PYfTmxbuOnykNGyhM6FjFLbW2uZHQTY1jMrPprjOrmyK5sjJR\n" +
            "O4d1DeGHT/YnIjs9JogRKv4XHECwLtIVdAbIdWHEtVZJyMSktcyysFcvuhPQK8Qc/E/Wq8uHSCo=";
    private static final String X509CERTIFICATE_START_TAG = "<X509Certificate>";
    private static final String X509CERTIFICATE_END_TAG = "</X509Certificate>";

    private ServerConfigurationManager serverConfigurationManager;

    private File identityXML;
    private File wso2carbonJKS;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_USER);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        resetISConfiguration();
    }

    /**
     * This tests written for SAML idp metadata with keystore change.
     *
     * @throws Exception
     */
    @Test(description = "Test SAML idp metadata with keystore change")
    public void testSAMLIdPMetadataWithKeystoreChange() throws Exception {
        String metadata = identityProviderMgtServiceClient.getResidentIDPMetadata();

        int startIndex = metadata.indexOf(X509CERTIFICATE_START_TAG) + X509CERTIFICATE_START_TAG.length();
        int endIndex = metadata.indexOf(X509CERTIFICATE_END_TAG);

        String certificate = metadata.substring(startIndex, endIndex);
        Assert.assertEquals(certificate, CERTIFICATE);
    }

    /**
     * Change the server configuration to reduce token expiry time.
     *
     * @throws Exception
     */
    private void changeISConfiguration() throws Exception {
        log.info("Replacing repository/conf/identity/carbon.xml to configure SAMLSignKeyStore");

        String carbonHome = CarbonUtils.getCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "carbon.xml");
        File configuredIdentityXml = new File(getISResourceLocation()
                + File.separator + "saml" + File.separator + "IDENTITY6924-carbon.xml");

        wso2carbonJKS = new File(carbonHome + File.separator + "repository" + File.separator
                + "resources" + File.separator + "security" + File.separator + "IDENTITY6924-wso2carbon.jks");
        File newCarbonJKS = new File(getISResourceLocation()
                + File.separator + "saml" + File.separator + "IDENTITY6924-wso2carbon.jks");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXml, identityXML, true);
        FileCopyUtils.copy(newCarbonJKS, wso2carbonJKS);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Restore  the server configuration.
     *
     * @throws Exception
     */
    private void resetISConfiguration() throws Exception {
        log.info("Replacing carbon.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }
}
