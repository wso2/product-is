/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

/**
 * This class contains test cases for SAML SSO for Admin Console.
 */
public class SAMLSSOForAdminLoginTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLSSOForAdminLoginTestCase.class);
    private ServerConfigurationManager serverConfigurationManager;

    protected static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    protected static final String MANAGEMENT_CONSOLE_LOGIN_URL = "https://localhost:9853/carbon/admin/login.jsp";
    protected static final String MANAGEMENT_CONSOLE_HOME_URL = "https://localhost:9853/carbon/admin/index.jsp";

    private SAMLConfig config;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLSSOForAdminLoginTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.info(String.format("SAML SSO for Admin Console Test initialized for %s", SAML_SSO_URL));
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(config.getUserMode());
        super.testInit();
        changeISConfiguration();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        resetISConfiguration();
        super.testClear();
    }

    @Test(alwaysRun = true, description = "Testing SSO for admin login", groups = "wso2.is")
    public void testSSOForAdminLogin() {

        try {
            // Get to management console login page
            HttpResponse response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_LOGIN_URL, USER_AGENT, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "User may have already logged in");

            // Extract saml request from the response and send the post request to samlsso endpoint
            String samlRequest = Utils.extractDataFromResponseForManagementConsoleRequests(response, "value", 1);
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, config);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, SAML_SSO_URL, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "SAML SSO Login failed for " + config);

            // check whether the user is logged in
            response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_HOME_URL, USER_AGENT, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_ADMIN, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.MANAGEMENT_CONSOLE_SSO_APP_WITH_SIGNING)}
        };
    }

    /**
     * This method will reset the deployment.toml file to default configurations.
     */
    private void changeISConfiguration() throws AutomationUtilException, IOException, XPathExpressionException {

        serverConfigurationManager = new ServerConfigurationManager(isServer);

        log.info("Changing deployment.toml file configs to enable sso for admin login.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "saml"
                + File.separator + "saml-sso-for-admin-console.toml");
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, defaultConfigFile, true);

        // creating a new sp from file based configs
        File SAMLSSOSpXml = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS"
                + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File.separator +
                "management-console-sso-carbonServer.xml");
        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank("service-providers")) {
            identityConfigPath = identityConfigPath.concat(File.separator + "service-providers");
        }
        FileManager.copyResourceToFileSystem(SAMLSSOSpXml.getAbsolutePath(), identityConfigPath,
                SAMLSSOSpXml.getName());

        // update sso idp configs to enable saml sso
        File ssoIdPConfigXml = new File(Utils.getResidentCarbonHome() + File.separator + "repository" + File
                .separator + "conf" + File.separator + "identity" + File.separator + "sso-idp-config.xml");
        File ssoIdPConfigXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
                .separator + "IS" + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File
                .separator + "management-console-sso-idp-config.xml");
        serverConfigurationManager.applyConfigurationWithoutRestart(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, true);

        // restart server to apply configs
        serverConfigurationManager.restartGracefully();
    }

    /**
     * This method will reset the deployment.toml file to default configurations.
     */
    private void resetISConfiguration() throws IOException, AutomationUtilException {

        // delete the sp created file.
        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank("service-providers")) {
            identityConfigPath = identityConfigPath.concat(File.separator + "service-providers");
        }
        File file = new File(identityConfigPath + File.separator + "management-console-sso-carbonServer.xml");
        if (file.exists()) {
            FileManager.deleteFile(file.getAbsolutePath());
        }

        // restore sso idp configs to original.
        File ssoIdPConfigXml = new File(Utils.getResidentCarbonHome() + File.separator + "repository" + File
                .separator + "conf" + File.separator + "identity" + File.separator + "sso-idp-config.xml");
        File ssoIdPConfigXmlToCopy = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
                .separator + "IS" + File.separator + "saml" + File.separator + "filebasedspidpconfigs" + File
                .separator + "original-sso-idp-config.xml");

        // Not restarting since the next test will restart the server.
        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, false,
                false);
        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
