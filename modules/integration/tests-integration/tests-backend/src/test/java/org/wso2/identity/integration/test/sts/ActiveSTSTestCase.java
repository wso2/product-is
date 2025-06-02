/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.sts;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import javax.xml.soap.SOAPMessage;

import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.admin.client.SecurityAdminServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sts.ws.trust.util.ClientUtils;
import org.wso2.identity.integration.common.clients.sts.ws.trust.util.TestUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.Constants.ACTION_RENEW;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.Constants.ACTION_REQUEST;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.Constants.ACTION_VALIDATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.CHANGING_XPATHS_FOR_RENEW_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.CHANGING_XPATHS_FOR_REQUEST_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.RENEW_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.REQUEST_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.VALIDATE_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        constants.TestConstants.XML_DECLARATION;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.
        util.TestUtils.convertSoapMessageToString;

public class ActiveSTSTestCase extends ISIntegrationTest {

    private static final String SERVICE_PROVIDER_NAME = "ActiveSTSTest";
    private static final String SERVICE_PROVIDER_DESCRIPTION = "ActiveSTS Service Provider";

    private static final String SERVICE_NAME = "wso2carbon-sts";
    private static final String POLICY_ID = "1";
    private static final String USER_GROUP = "admin";

    private static final String WS_TRUST_AUTH_TYPE = "wstrust";
    private static final String INBOUND_CONFIG_TYPE = "standardAPP";
    private static final String ENDPOINT_ADDRESS_VALUE = "https://localhost:9444/services/echo";

    private ServiceProvider serviceProvider;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private SecurityAdminServiceClient securityAdminServiceClient;
    private String uri;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        super.login();
        super.setSystemproperties();

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(
                sessionCookie, backendURL, configContext);
        securityAdminServiceClient = new SecurityAdminServiceClient(backendURL, sessionCookie);
        securityAdminServiceClient.applySecurity(SERVICE_NAME, POLICY_ID,
                new String[]{USER_GROUP}, new String[]{}, null);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        removeServiceProvider();
        serviceProvider = null;
        applicationManagementServiceClient = null;
        securityAdminServiceClient.disableSecurity(SERVICE_NAME);
        securityAdminServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Add service provider for the testcase.")
    public void testAddSP() throws Exception {

        addServiceProvider();
        serviceProvider = applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Service provider registration failed.");
    }

    @Test(alwaysRun = true, description = "Update service provider with the required configurations."
            , dependsOnMethods = {"testAddSP"})
    public void testUpdateSP() throws Exception {

        List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigs = new ArrayList<>();
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig.setInboundAuthKey(ENDPOINT_ADDRESS_VALUE);
        inboundAuthenticationRequestConfig.setInboundAuthType(WS_TRUST_AUTH_TYPE);
        inboundAuthenticationRequestConfig.setInboundConfigType(INBOUND_CONFIG_TYPE);

        inboundAuthenticationRequestConfigs.add(inboundAuthenticationRequestConfig);

        serviceProvider.getInboundAuthenticationConfig()
                .setInboundAuthenticationRequestConfigs(
                        inboundAuthenticationRequestConfigs
                                .toArray(new InboundAuthenticationRequestConfig[0]));
        applicationManagementServiceClient.updateApplicationData(serviceProvider);

        Assert.assertNotEquals(applicationManagementServiceClient.getApplication(SERVICE_PROVIDER_NAME)
                        .getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs().length,
                0, "Fail to update service provider with active STS configurations.");
    }

    @Test(alwaysRun = true, description = "Validate the response obtained " +
            "when a security token is requested from the sts.", dependsOnMethods = {"testUpdateSP"})
    public void testRequestSecurityTokenRSTR() throws Exception {

        SOAPMessage[] requestSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_REQUEST);
        uri = (requestSTRequestAndResponse[1].getSOAPBody().getElementsByTagName("wsse:Reference")
                .item(0).getAttributes().getNamedItem("URI").getNodeValue()).substring(1);

        String requestSTResponseTemplate = TestUtils.loadTemplate(REQUEST_ST_RSTR_TEMPLATE);
        String requestSTResponse = convertSoapMessageToString(requestSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(requestSTResponseTemplate, requestSTResponse,
                CHANGING_XPATHS_FOR_REQUEST_ST_RSTR, NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR);
    }

    @Test(alwaysRun = true, description = "Validate the response obtained when " +
            "a renewal request for a security token is sent to the sts.",
            dependsOnMethods = {"testRequestSecurityTokenRSTR"})
    public void testRenewSecurityTokenRSTR() throws Exception {

        SOAPMessage[] renewSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_RENEW, uri);

        String renewSTResponseTemplate = TestUtils.loadTemplate(RENEW_ST_RSTR_TEMPLATE);
        String renewSTResponse = convertSoapMessageToString(renewSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(renewSTResponseTemplate, renewSTResponse,
                CHANGING_XPATHS_FOR_RENEW_ST_RSTR, NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR);
    }

    @Test(alwaysRun = true, description = "Validate the response obtained when a " +
            "request is sent to validate a security token from the sts.",
            dependsOnMethods = {"testRenewSecurityTokenRSTR"})
    public void testValidateSecurityTokenRSTR() throws Exception {

        SOAPMessage[] validateSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_VALIDATE, uri);

        String validateSTResponseTemplate = TestUtils.loadTemplate(VALIDATE_ST_RSTR_TEMPLATE);
        String validateSTResponse = convertSoapMessageToString(validateSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(validateSTResponseTemplate, validateSTResponse,
                CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR, NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR);
    }

    private void addServiceProvider() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(SERVICE_PROVIDER_NAME);
        serviceProvider.setDescription(SERVICE_PROVIDER_DESCRIPTION);

        applicationManagementServiceClient.createApplication(serviceProvider);
    }

    private void removeServiceProvider() throws Exception {

        applicationManagementServiceClient.deleteApplication(SERVICE_PROVIDER_NAME);
    }

    private void compareWithTemplate(String staticTemplate, String obtainedResponse,
                                     List<String> expectedValues, int expectedNoOfChanges) {

        Diff diff = DiffBuilder.compare(staticTemplate).ignoreWhitespace()
                .withTest(obtainedResponse).ignoreWhitespace().checkForSimilar().build();

        List<String> xPaths = new ArrayList<>();
        for (Difference difference : diff.getDifferences()) {
            xPaths.add(difference.getComparison().getTestDetails().getXPath());
        }

        Assert.assertTrue(xPaths.containsAll(expectedValues),
                "Expected changes did not match with the actual changes.");
        Assert.assertEquals(xPaths.size(), expectedNoOfChanges,
                "Expected " + expectedNoOfChanges + " changes "
                        + "but found " + xPaths.size() + " changes.");
    }
}
