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
import org.testng.IExecutionListener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

/**
 * Test class that will start and stop external ldap server for the tests in the test suite.
 */
public class SMTPServerInitializerListener implements IExecutionListener {

    private static final Log LOG = LogFactory.getLog(SMTPServerInitializerListener.class);

    @Override
    public void onExecutionStart() {

        try {
            GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
            greenMail.setUser("admin", "admin");
            greenMail.start();
            Utils.setGreenMail(greenMail);

            changeDeploymentToml();
        } catch (Exception e) {
            LOG.error("Failed to start SMTP server.", e);
        }
    }

    @Override
    public void onExecutionFinish() {

        try {
            Utils.getMailServer().stop();
            LOG.info("SMTP server is stopped.");
        } catch (Exception e) {
            LOG.error("Failed to stop SMTP server.", e);
        }
    }
    @BeforeSuite(alwaysRun = true)
    public void initTest() throws Exception {

    }

    @AfterSuite(alwaysRun = true)
    public void tearDownTest() throws Exception {


    }

    private void changeDeploymentToml() throws IOException, XPathExpressionException, AutomationUtilException {
        String carbonHome = Utils.getResidentCarbonHome();

        String smtpEnabledDeploymentToml = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "default_deployment.toml";
        File deploymentTomlFile = getDeploymentTomlFile(carbonHome);
        ServerConfigurationManager serverConfigurationManager
                = new ServerConfigurationManager(new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyConfigurationWithoutRestart(new File(smtpEnabledDeploymentToml),
                deploymentTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private File getDeploymentTomlFile(String carbonHome) {
        File deploymentToml = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "deployment.toml");
        return deploymentToml;
    }
}
