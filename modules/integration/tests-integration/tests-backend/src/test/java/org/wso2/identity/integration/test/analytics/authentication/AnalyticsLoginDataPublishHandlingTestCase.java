/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.analytics.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.h2.osgi.utils.CarbonUtils;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.analytics.commons.AnalyticsDataHolder;
import org.wso2.identity.integration.test.analytics.commons.ThriftServer;

import java.io.File;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class tests Analytics login data publish handling and Analytics session data publish handling functionalities.
 */
public class AnalyticsLoginDataPublishHandlingTestCase extends AbstractAnalyticsLoginTestCase {

    private static final Log log = LogFactory.getLog(AnalyticsLoginDataPublishHandlingTestCase.class);

    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "samlConfigProvider")
    public AnalyticsLoginDataPublishHandlingTestCase(SAMLConfig config) {

        setConfig(config);
        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config);
        }
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        changeIdentityXml();
        startThriftServer();
        super.testInit();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {
        super.testClear();
        stopThriftServer();
        replaceIdentityXml();
    }

    public void changeIdentityXml() {

        log.info("Changing identity-event.properties file to enable analytics");

        String carbonHome = CarbonUtils.getCarbonHome();

        String analyticsEnabledIdentityEventProperties = getISResourceLocation() + File.separator + "analytics" +
                File.separator + "config" + File.separator + "identity-event_analytics_enabled.properties";
        File defaultIdentityEventProperties = new File(carbonHome + File.separator + "repository" +
                File.separator + "conf" + File.separator + "identity" + File.separator + "identity-event.properties");

        String authnDataPublisherWithOffset = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-AuthenticationData.xml";
        File defaultAuthenticationDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-AuthenticationData.xml");

        String sessionDataPublisherWithOffset = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-SessionData.xml";
        File defaultSessionDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-SessionData.xml");
        try {
            File configuredNotificationProperties = new File(analyticsEnabledIdentityEventProperties);
            File configuredAuthnPublisherFile = new File(authnDataPublisherWithOffset);
            File configuredSessionPublisherFile = new File(sessionDataPublisherWithOffset);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityEventProperties, true);
//            copyAuthenticationDataPublisher();
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredAuthnPublisherFile,
                    defaultAuthenticationDataPublisher, true);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredSessionPublisherFile,
                    defaultSessionDataPublisher, true);
            serverConfigurationManager.restartForcefully();

        } catch (AutomationUtilException | XPathExpressionException | IOException e) {
            log.error("Error while changing configurations in identity-event.properties", e);
        }
    }

    public void replaceIdentityXml() throws IOException, AutomationUtilException {

        log.info("Replacing default identity-event.properties file.");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    private void startThriftServer() throws DataBridgeException, StreamDefinitionStoreException {

        ThriftServer thriftServer = new ThriftServer("Wso2EventTestCase", 8021, true);
        thriftServer.start(8021);
        AnalyticsDataHolder.getInstance().setThriftServer(thriftServer);
        log.info("Thrift Server is Started on port 8021");
    }

    private void stopThriftServer() {

        ThriftServer thriftServer = AnalyticsDataHolder.getInstance().getThriftServer();
        if (thriftServer != null) {
            thriftServer.stop();
            AnalyticsDataHolder.getInstance().setThriftServer(null);
        }
        log.info("Thrift Server stopped on port 8021");
    }


    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
        };
    }

    @Test(description = "Add service provider", groups = "wso2.is", priority = 1)
    public void testAddSP() throws Exception {

        super.testAddSP();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is", dependsOnMethods = {"testAddSP"})
    public void testSAMLSSOIsPassiveLogin() throws IOException {

        super.testSAMLSSOIsPassiveLogin();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
            dependsOnMethods = {"testSAMLSSOIsPassiveLogin"})
    public void testSAMLSSOLogin() throws IOException {

        super.testSAMLSSOLogin();
    }
}
