/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.analytics.base;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.h2.osgi.utils.CarbonUtils;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.analytics.commons.AnalyticsDataHolder;
import org.wso2.identity.integration.test.analytics.commons.ThriftServer;

import java.io.File;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

public class AnalyticsBaseTestCase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeTest(alwaysRun = true)
    public void enableAnalytics() throws Exception {

        super.init();
        changeConfiguration();
        startThriftServer();
    }

    @AfterTest(alwaysRun = true)
    public void disableAnalytics() throws Exception {

        stopThriftServer();
        revertConfiguration();
    }

    private void changeConfiguration() {

        log.info("Changing identity.xml file to enable analytics");

        String carbonHome = CarbonUtils.getCarbonHome();

        String analyticsEnabledIdentityXml = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "identit_analytics_enabled.xml";
        File defaultIdentityXml = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator + "identity.xml");

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

        String tokenDataPublisherWithOffset = getISResourceLocation() + File.separator + "analytics" + File.separator
                + "config" + File.separator + "IsAnalytics-Publisher-wso2event-OauthTokenIssueRefresh.xml";
        File defaultTokenDataPublisher = new File(carbonHome + File.separator
                + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                "eventpublishers" + File.separator + "IsAnalytics-Publisher-wso2event-OauthTokenIssueRefresh.xml");



        try {
            File configuredNotificationProperties = new File(analyticsEnabledIdentityXml);
            File configuredAuthnPublisherFile = new File(authnDataPublisherWithOffset);
            File configuredSessionPublisherFile = new File(sessionDataPublisherWithOffset);
            File configuredTokenPublisherFile = new File(tokenDataPublisherWithOffset);

            serverConfigurationManager = new ServerConfigurationManager(isServer);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredTokenPublisherFile,
                    defaultTokenDataPublisher, true);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                    defaultIdentityXml, true);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredAuthnPublisherFile,
                    defaultAuthenticationDataPublisher, true);
            serverConfigurationManager.applyConfigurationWithoutRestart(configuredSessionPublisherFile,
                    defaultSessionDataPublisher, true);
            serverConfigurationManager.restartGracefully();
        } catch (AutomationUtilException | IOException | XPathExpressionException e) {
            log.error("Error while changing configurations in identity.xml", e);
        }
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

    private void revertConfiguration() throws IOException, AutomationUtilException {

        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
