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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.h2.osgi.utils.CarbonUtils;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

/**
 * This class tests Analytics login data publish handling and Analytics session data publish handling functionalities.
 */
public class AnalyticsLoginDataPublishHandlingTestCase extends AnalyticsLoginTestCase {

    private static final Log log = LogFactory.getLog(AnalyticsLoginDataPublishHandlingTestCase.class);

    @Factory(dataProvider = "samlConfigProvider")
    public AnalyticsLoginDataPublishHandlingTestCase(SAMLConfig config) {
        super(config);
        if (log.isDebugEnabled()) {
            log.debug("SAML SSO Test initialized for " + config);
        }
    }

    @BeforeClass
    @Override
    public void testInit() throws Exception {
        super.testInit();
    }

    @AfterClass
    @Override
    public void testClear() throws Exception {
        super.testClear();
    }

    @Override
    public void changeIdentityXml() {

        log.info("Changing identity-event.properties file to enable analytics");

        String carbonHome = CarbonUtils.getCarbonHome();

        String analyticsEnabledIdentityEventProperties = getISResourceLocation() + File.separator + "analytics" +
                File.separator + "config" + File.separator + "identity-event_analytics_enabled.properties";
        File defaultIdentityEventProperties = new File(carbonHome + File.separator + "repository" +
                File.separator + "conf" + File.separator + "identity" + File.separator + "identity-event.properties");
        try {
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            File configuredNotificationProperties = new File(analyticsEnabledIdentityEventProperties);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityEventProperties, true);
            copyAuthenticationDataPublisher();
            serverConfigurationManager.restartForcefully();

        } catch (AutomationUtilException | XPathExpressionException | IOException e) {
            log.error("Error while changing configurations in identity-event.properties", e);
        }
    }

    @Override
    public void replaceIdentityXml() {

        log.info("Replacing default identity-event.properties file.");

        String carbonHome = CarbonUtils.getCarbonHome();

        String defaultIdentityEventProperties = getISResourceLocation() + File.separator +
                "default-identity-event.properties";
        File defaultIdentityEventPropertiesLocation = new File(carbonHome + File.separator + "repository" +
                File.separator + "conf" + File.separator + "identity" + File.separator + "identity-event.properties");
        try {
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            File configuredNotificationProperties = new File(defaultIdentityEventProperties);
            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityEventPropertiesLocation, true);
            copyAuthenticationDataPublisher();
            serverConfigurationManager.restartForcefully();

        } catch (AutomationUtilException | XPathExpressionException | IOException e) {
            log.error("Error while changing configurations in identity-event.properties to default configuration", e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static SAMLConfig[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
        };
    }

    @Override
    public void testAddSP() throws Exception {
        super.testAddSP();
    }

    @Override
    public void testSAMLSSOIsPassiveLogin() throws IOException {
        super.testSAMLSSOIsPassiveLogin();
    }

    @Override
    public void testSAMLSSOLogin() throws IOException {
        super.testSAMLSSOLogin();
    }
}
