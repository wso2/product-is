/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

    // Constants.
    protected static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    protected static final String MANAGEMENT_CONSOLE_LOGIN_URL = "https://localhost:9853/carbon/admin/login.jsp";
    protected static final String MANAGEMENT_CONSOLE_HOME_URL = "https://localhost:9853/carbon/admin/index.jsp";

    // File paths.
    private final String carbonHome = Utils.getResidentCarbonHome();
    private final String configuredIdentityXMLPath = buildPath(getISResourceLocation(), "saml",
            "saml-sso-for-admin-console.toml");
    private final String SAMLSSOSpXmlPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(), "artifacts",
            "IS", "saml", "filebasedspidpconfigs", "management-console-sso-carbonServer.xml");
    private final String identityConfigSAMLSSOSpXmlPath = buildPath(carbonHome, "repository", "conf",
            "identity", "service-providers", "management-console-sso-carbonServer.xml");
    private final String identityConfigPath = buildPath(carbonHome, "repository", "conf", "identity",
            "service-providers");
    private final String ssoIdPConfigXmlPath = buildPath(carbonHome, "repository", "conf", "identity",
            "sso-idp-config.xml");
    private final String ssoIdPConfigXmlToCopyPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(),
            "artifacts", "IS", "saml", "filebasedspidpconfigs", "management-console-sso-idp-config.xml");
    String ssoIdPConfigXmlOriginalConfigPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(),
            "artifacts", "IS", "saml", "filebasedspidpconfigs", "original-sso-idp-config.xml");

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
            // Get the management console login page.
            HttpResponse response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_LOGIN_URL, USER_AGENT, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "User may have already logged in");

            // Extract saml request from the response and send the post request to samlsso endpoint.
            String samlRequest = Utils.extractDataFromResponseForManagementConsoleRequests(response, "value", 1);
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, config);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, SAML_SSO_URL, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "SAML SSO Login failed for " + config);

            // Check whether the user is successfully logged in.
            response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_HOME_URL, USER_AGENT, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200, "SAML SSO Login failed for " + config);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {

        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_ADMIN, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.MANAGEMENT_CONSOLE_SSO_APP_WITH_SIGNING)}};
    }

    /**
     * This method will reset the deployment.toml file to default configurations.
     */
    private void changeISConfiguration() throws AutomationUtilException, IOException, XPathExpressionException {

        serverConfigurationManager = new ServerConfigurationManager(isServer);

        // Changing deployment.toml file configs to enable sso for admin login.
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File configuredIdentityXML = new File(configuredIdentityXMLPath);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, defaultConfigFile, true);

        // Create a new service provider from file based configs.
        File SAMLSSOSpXml = new File(SAMLSSOSpXmlPath);
        FileManager.copyResourceToFileSystem(SAMLSSOSpXml.getAbsolutePath(), identityConfigPath,
                SAMLSSOSpXml.getName());

        // Update sso idp configs to enable saml sso.
        File ssoIdPConfigXml = new File(ssoIdPConfigXmlPath);
        File ssoIdPConfigXmlToCopy = new File(ssoIdPConfigXmlToCopyPath);
        serverConfigurationManager.applyConfigurationWithoutRestart(ssoIdPConfigXmlToCopy, ssoIdPConfigXml, true);

        // Restart server to apply configs.
        serverConfigurationManager.restartGracefully();
    }

    /**
     * This method will construct the file path for a given path elements.
     *
     * @param basePath     base path of the file.
     * @param pathElements path elements to the file.
     */
    private String buildPath(String basePath, String... pathElements) {

        StringBuilder path = new StringBuilder(basePath);
        for (String pathElement : pathElements) {
            path.append(File.separator).append(pathElement);
        }
        return path.toString();
    }

    /**
     * This method will reset the deployment.toml file to default configurations.
     */
    private void resetISConfiguration() throws IOException, AutomationUtilException {

        // Delete the config file used to create the service provider.
        File file = new File(identityConfigSAMLSSOSpXmlPath);
        if (file.exists()) {
            FileManager.deleteFile(file.getAbsolutePath());
        }

        // Restore sso idp configs to original.
        File ssoIdPConfigXml = new File(ssoIdPConfigXmlPath);
        File ssoIdPConfigXmlOriginal = new File(ssoIdPConfigXmlOriginalConfigPath);

        // Not restarting since the next test will restart the server.
        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlOriginal, ssoIdPConfigXml, false, false);
        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
