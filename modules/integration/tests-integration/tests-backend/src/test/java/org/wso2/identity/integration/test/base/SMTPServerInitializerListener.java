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
import org.wso2.identity.integration.test.util.Utils;

/**
 * Listener that will start and stop mock SMTP server for the tests in the test suite.
 */
public class SMTPServerInitializerListener implements IExecutionListener {

    private static final Log LOG = LogFactory.getLog(SMTPServerInitializerListener.class);

    @Override
    public void onExecutionStart() {

        try {
            GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);
            greenMail.setUser("admin", "admin");
            greenMail.start();
            Utils.setMailServer(greenMail);
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
}
