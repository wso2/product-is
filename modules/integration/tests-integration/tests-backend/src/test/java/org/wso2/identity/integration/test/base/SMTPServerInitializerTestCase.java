/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.base;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

/**
 * Test class that will start and stop external ldap server for the tests in the test suite.
 */
public class SMTPServerInitializerTestCase extends ISIntegrationTest {

    private static final Log LOG = LogFactory.getLog(SMTPServerInitializerTestCase.class);

    @BeforeSuite(alwaysRun = true)
    public void initTest() throws Exception {

        try {
            GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
            greenMail.setUser("admin", "admin");
            greenMail.start();
            Utils.setGreenMail(greenMail);

            super.init();
            changeDeploymentToml();
        } catch (Exception e) {
            throw new Exception("Failed to start SMTP server.", e);
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownTest() throws Exception {

        try {
            Utils.getGreenMail().stop();
            LOG.info("SMTP server is stopped.");
        } catch (Exception e) {
            throw new Exception("Failed to stop SMTP server.", e);
        }
    }

    private void changeDeploymentToml() throws IOException, XPathExpressionException, AutomationUtilException {
        String carbonHome = Utils.getResidentCarbonHome();

        String smtpEnabledDeploymentToml = getISResourceLocation() + File.separator + "default_deployment.toml";
        File deploymentTomlFile = getDeploymentTomlFile(carbonHome);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(new File(smtpEnabledDeploymentToml),
                deploymentTomlFile, true);
        serverConfigurationManager.restartGracefully();

    }
}
