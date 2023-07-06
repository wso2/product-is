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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

/**
 * This class contains test cases for SAML SSO for Admin Console.
 */
public class SAMLSSOForAdminLoginTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLSSOForAdminLoginTestCase.class);
    private ServerConfigurationManager serverConfigurationManager;
    private final SAMLConfig samlConfig = new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, null, null,
            null, null);

    // Constants.
    private static final String SAML_ACS_URL = "https://localhost:9853/acs";
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String MANAGEMENT_CONSOLE_LOGIN_URL = "https://localhost:9853/carbon/admin/login.jsp";
    private static final String MANAGEMENT_CONSOLE_LOGOUT_URL = "https://localhost:9853/carbon/admin/logout_action.jsp";
    private static final String MANAGEMENT_CONSOLE_HOME_URL = "https://localhost:9853/carbon/admin/index.jsp";
    private static final String AUTHENTICATION_PORTAL_LOGIN_URL =
            "https://localhost:9853/authenticationendpoint/login.do";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    // File paths.
    private final String carbonHome = Utils.getResidentCarbonHome();
    private final String serverConfigFilePath = buildPath(getISResourceLocation(), "saml",
            "saml-sso-for-admin-console.toml");
    private final String SAMLSSOSpXmlPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(), "artifacts",
            "IS", "saml", "filebasedspidpconfigs", "self.xml");
    private final String identityConfigSAMLSSOSpXmlPath = buildPath(carbonHome, "repository", "conf",
            "identity", "service-providers", "self.xml");
    private final String identityConfigPath = buildPath(carbonHome, "repository", "conf", "identity",
            "service-providers");
    private final String ssoIdPConfigXmlPath = buildPath(carbonHome, "repository", "conf", "identity",
            "sso-idp-config.xml");
    private final String ssoIdPConfigXmlToCopyPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(),
            "artifacts", "IS", "saml", "filebasedspidpconfigs", "management-console-sso-idp-config.xml");
    private final String ssoIdPConfigXmlOriginalConfigPath = buildPath(FrameworkPathUtil.getSystemResourceLocation(),
            "artifacts", "IS", "saml", "filebasedspidpconfigs", "original-sso-idp-config.xml");

    public SAMLSSOForAdminLoginTestCase() {

        if (log.isDebugEnabled()) {
            log.info(String.format("SAML SSO for Admin Console Test initialized for %s", SAML_SSO_URL));
        }
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
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
            // Verify the server has started properly.
            while (!isServerAvailable()) {
                log.info("Waiting for server to start up.");
                Thread.sleep(5000);
            }

            log.debug("Sending GET request for management console login page.");
            HttpResponse response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_LOGIN_URL, USER_AGENT, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "User may have already logged in");

            log.debug("Extracting SAML request from the response and sending the post request to samlsso endpoint.");
            String samlRequest = Utils.extractDataFromResponseForManagementConsoleRequests(
                    response, "value", 1);
            EntityUtils.consume(response.getEntity());
            response = sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, samlConfig);
            Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            Assert.assertTrue(location.getValue().contains(AUTHENTICATION_PORTAL_LOGIN_URL),
                    "Invalid response received for SAML SSO request.");
            EntityUtils.consume(response.getEntity());

            log.debug("Sending redirection request to get the sessionDataKey.");
            response = Utils.sendRedirectRequest(response, USER_AGENT, SAML_ACS_URL, "", httpClient);
            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            EntityUtils.consume(response.getEntity());

            log.debug("Sending login request to SAML SSO endpoint.");
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, AUTHENTICATION_PORTAL_LOGIN_URL,
                    "", USERNAME, PASSWORD, httpClient);
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200,
                    "SAML SSO Login failed for admin user.");

            log.debug("Extracting SAML response from the login request and sending the request to ACS to get the " +
                    "home page.");
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());
            response = super.sendSAMLMessage(SAML_ACS_URL, CommonConstants.SAML_RESPONSE_PARAM, samlResponse,
                    samlConfig);
            location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertTrue(location.getValue().contains(MANAGEMENT_CONSOLE_HOME_URL),
                    "SAML SSO Login failed for admin user.");

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for admin user.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing SSO for admin logout", groups = "wso2.is", dependsOnMethods =
            {"testSSOForAdminLogin"})
    public void testSAMLSSOLogout() {

        try {
            log.debug("Sending GET request for management console logout page.");
            HttpResponse response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_LOGOUT_URL, USER_AGENT, httpClient);
            String samlRequest = Utils.extractDataFromResponseForManagementConsoleRequests(
                    response, "value", 1);
            EntityUtils.consume(response.getEntity());

            log.debug("Sending logout request to SAML SSO endpoint.");
            response = super.sendSAMLMessage(SAML_SSO_URL, CommonConstants.SAML_REQUEST_PARAM, samlRequest, samlConfig);
            String samlResponse = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 5);
            EntityUtils.consume(response.getEntity());

            log.debug("Sending logout response to ACS endpoint and verify the logout.");
            response = super.sendSAMLMessage(SAML_ACS_URL, CommonConstants.SAML_RESPONSE_PARAM, samlResponse,
                    samlConfig);
            Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
            EntityUtils.consume(response.getEntity());
            Assert.assertTrue(location.getValue().contains(MANAGEMENT_CONSOLE_LOGOUT_URL),
                    "SAML SSO Logout failed for admin user.");
        } catch (Exception e) {
            Assert.fail("SAML SSO Logout test failed for admin user.", e);
        }
    }

    /**
     * This method will reset the deployment.toml file to default configurations.
     */
    private void changeISConfiguration() throws AutomationUtilException, IOException, XPathExpressionException {

        serverConfigurationManager = new ServerConfigurationManager(isServer);

        // Changing deployment.toml file configs to enable sso for admin login.
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File serverConfigFile = new File(serverConfigFilePath);
        serverConfigurationManager.applyConfigurationWithoutRestart(serverConfigFile, defaultConfigFile, true);

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

        // Restarting to apply the old configs back.
        serverConfigurationManager.applyConfiguration(ssoIdPConfigXmlOriginal, ssoIdPConfigXml, false, false);
        serverConfigurationManager.restoreToLastConfiguration();
    }

    /**
     * Test whether the server is available.
     *
     * @return true if the server is available.
     */
    private boolean isServerAvailable() throws Exception {

        HttpResponse response = Utils.sendGetRequest(MANAGEMENT_CONSOLE_LOGIN_URL, USER_AGENT, httpClient);
        if (response.getStatusLine().getStatusCode() == 200) {
            EntityUtils.consume(response.getEntity());
            return true;
        }
        return false;
    }
}
