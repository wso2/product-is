/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.XPathConstants;
import org.wso2.identity.integration.test.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Listener that will start and stop mock SMTP server for the tests in the test suite.
 */
public class SMTPServerInitializerListener implements IExecutionListener {

    private static final Log LOG = LogFactory.getLog(SMTPServerInitializerListener.class);
    private static final String HEADER_OUTPUT_ADAPTER_EMAIL = "[output_adapter.email]";

    @Override
    public void onExecutionStart() {

        try {
            AutomationContext context = new AutomationContext();
            String smtpUsername = context.getConfigurationValue("//emailSenderConfigs/username");
            String smtpPassword = context.getConfigurationValue("//emailSenderConfigs/password");

            if (smtpUsername != null && smtpPassword != null) {
                GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
                greenMail.setUser(smtpUsername, smtpPassword);
                greenMail.start();
                Utils.setMailServer(greenMail);
            }
        } catch (Exception e) {
            LOG.error("Failed to start SMTP server.", e);
        }
    }

    @Override
    public void onExecutionFinish() {

        try {
            if (Utils.getMailServer() != null) {
                Utils.getMailServer().stop();
                LOG.info("SMTP server is stopped.");
            }
        } catch (Exception e) {
            LOG.error("Failed to stop SMTP server.", e);
        }
    }
}
