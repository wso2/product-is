/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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
package org.wso2.identity.integration.test.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.test.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class SAMLECPSSOTestCase extends AbstractSAMLSSOTestCase {

    private SAMLConfig config;
    private static final Log log = LogFactory.getLog(SAMLECPSSOTestCase.class);
    private static final String APPLICATION_NAME = "SAML-ECP-TestApplication";
    private File identityXML;
    private ServerConfigurationManager serverConfigurationManager;
    private static final String SAML_ECP_ISSUER = "https://localhost/ecp-sp";

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLECPSSOTestCase(SAMLConfig config) {
        if (log.isDebugEnabled()) {
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_SOAP,
                        ClaimType.LOCAL, App.ECP_APP)},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init(config.getUserMode());
        changeISConfiguration();
        super.init(config.getUserMode());
        super.testInit();
        super.createUser(config);
        super.createApplication(config, APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        super.deleteUser(config);
        super.deleteApplication(APPLICATION_NAME);
        super.testClear();
        resetISConfiguration();
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {
        Boolean isAddSuccess = ssoConfigServiceClient.addServiceProvider(super.createECPServiceProviderDTO(config));
        Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);
        SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                .getServiceProviders().getServiceProviders();
        Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                "Adding a service provider has failed for " + config);
    }

    @Test(alwaysRun = true, description = "Testing SAML ECP login", groups = "wso2.is", dependsOnMethods = {"testAddSP"})
    public void testSAMLECPLogin() {
        try {
            HttpResponse response;
            String samlECPReq = buildECPSAMLRequest(SAML_ECP_ACS_URL, SAML_ECP_ISSUER);
            response = Utils.sendECPPostRequest(SAML_ECP_SSO_URL, USER_AGENT, httpClient, config.getUser().getUsername(), config.getUser().getPassword(), samlECPReq);
            String result = extractDataFromResponse(response);
            if(log.isDebugEnabled()){
                log.debug("Response : " + result);
            }
            int responseCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(responseCode, 200, "Login failure with response returned code " + responseCode);
            Assert.assertTrue(result.contains("urn:oasis:names:tc:SAML:2.0:status:Success"), "Failed to authenticate the user for " + config);
            Assert.assertTrue(result.contains("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"), "Failed to retrieve the SAML Response bound with SOAP");

        } catch (Exception e) {
            Assert.fail("SAML ECP Login test failed for " + config, e);
        }
    }

    @Test(description = "Testing SAML ECP login failure", groups = "wso2.is", dependsOnMethods = {"testSAMLECPLogin"})
    public void testSAMLECPAuthnFailLogin() {
        try {
            httpClient = new DefaultHttpClient();
            HttpResponse response;
            String samlECPReq = buildECPSAMLRequest(SAML_ECP_ACS_URL, SAML_ECP_ISSUER);
            response = Utils.sendECPPostRequest(SAML_ECP_SSO_URL, USER_AGENT, httpClient, config.getUser().getUsername(), "RandomPassword", samlECPReq);
            String result = extractDataFromResponse(response);
            if(log.isDebugEnabled()){
                log.debug("Response : " + result);
            }
            int responseCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(responseCode, 200, "Login failure with response returned code " + responseCode);
            Assert.assertTrue(result.contains("urn:oasis:names:tc:SAML:2.0:status:AuthnFailed"), "Fail to identify the login failure " + config);
        } catch (Exception e) {
            Assert.fail("SAML ECP Login failure test failed for " + config, e);
        }
    }

    @Test(description = "Testing SAML ECP SOAP faults for invalid requests", groups = "wso2.is", dependsOnMethods = {"testSAMLECPAuthnFailLogin"})
    public void testSOAPFault() {
        try {
            httpClient = new DefaultHttpClient();
            HttpResponse response;
            String samlECPReq = buildInvalidECPSAMLRequest(SAML_ECP_ACS_URL, SAML_ECP_ISSUER);
            response = Utils.sendECPPostRequest(SAML_ECP_SSO_URL, USER_AGENT, httpClient, config.getUser().getUsername(), config.getUser().getPassword(), samlECPReq);
            String result = extractDataFromResponse(response);
            if(log.isDebugEnabled()){
                log.debug("Response : " + result);
            }
            int responseCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(responseCode, 500, "Fail to return a SOAP fault with internal server error " + responseCode);
        } catch (Exception e) {
            Assert.fail("SAML ECP SOAP fault test failed for " + config, e);
        }
    }

    @Test(description = "Remove service provider", groups = "wso2.is", dependsOnMethods = {"testSOAPFault"})
    public void testRemoveSP()
            throws Exception {
        Boolean isServiceProviderRemoved = ssoConfigServiceClient.removeServiceProvider(config.getApp().getArtifact());
        Assert.assertTrue(isServiceProviderRemoved, "Removing a service provider has failed for " + config);
    }


    private String extractDataFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private void changeISConfiguration() throws Exception {
        log.info("Replacing identity.xml disabling the consent Management");
        String carbonHome = CarbonUtils.getCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File
                .separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation()
                + File.separator + "saml" + File.separator
                + "saml-ecp-consent-management-disabled-identity.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {
        log.info("Replacing identity.xml with default configurations");
        File defaultIdentityXml = new File(getISResourceLocation() + File.separator + "default-identity.xml");
        serverConfigurationManager.applyConfigurationWithoutRestart(defaultIdentityXml,
                identityXML, true);
        serverConfigurationManager.restartForcefully();
    }


    private String buildECPSAMLRequest(String acsUrl, String isuuer) {
        String samlReq = null;
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String dtStr = fmt.print(dt);
        samlReq = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><samlp:AuthnRequest xmlns" +
                ":samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" AssertionConsumerServiceURL=\"" + acsUrl + "\" ID=\"_ec102" +
                "5e786e6fff206ef63909029202a\" IssueInstant=\"" + dtStr + "\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0" +
                ":bindings:PAOS\" Version=\"2.0\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" + isuuer
                + "</saml:Issuer><samlp:NameIDPolicy AllowCreate=\"1\"/><samlp:Scoping><samlp:IDPList><samlp:IDPEntry Provi" +
                "derID=\"https://idp.is.com\"/></samlp:IDPList></samlp:Scoping></samlp:AuthnRequest></S:Body></S:Envelope>";
        return samlReq;
    }

    private String buildInvalidECPSAMLRequest(String acsUrl, String isuuer) {
        String samlReq = null;
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String dtStr = fmt.print(dt);
        samlReq = "<S: xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><samlp:AuthnRequest xmlns" +
                ":samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" AssertionConsumerServiceURL=\"" + acsUrl + "\" ID=\"_ec102" +
                "5e786e6fff206ef63909029202a\" IssueInstant=\"" + dtStr + "\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0" +
                ":bindings:PAOS\" Version=\"2.0\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" + isuuer
                + "</saml:Issuer><samlp:NameIDPolicy AllowCreate=\"1\"/><samlp:Scoping><samlp:IDPList><samlp:IDPEntry Provi" +
                "derID=\"https://idp.is.com\"/></samlp:IDPList></samlp:Scoping></samlp:AuthnRequest></S:Body></S:Envelope>";
        return samlReq;
    }
}
