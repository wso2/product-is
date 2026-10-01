/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.utils.ClientConnectionUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathExpressionException;

/**
 * Drop-in replacement for {@link ServerConfigurationManager} that removes the fixed sleeps from the server
 * restart path.
 * <p>
 * The framework implementation of {@code restartGracefully()} waits for the server port to close, then sleeps for a
 * hard coded 40 seconds, and only then starts polling for the port to come back up. That sleep is redundant: the
 * {@code waitForPortClose} call before it has already confirmed the old server is gone, and the {@code waitForPort}
 * and {@code waitForLogin} calls after it poll until the new server is actually serving requests. With roughly 60
 * restarts in a full integration test run, those sleeps alone account for about 40 minutes of wall clock time.
 * <p>
 * The overrides below keep the same ordering of readiness checks but replace the fixed sleeps with a short settle
 * window, configurable through the {@value #SETTLE_TIME_PROPERTY} system property so it can be tuned without a
 * rebuild. {@code applyConfiguration()} and {@code restoreToLastConfiguration()} call {@code restartGracefully()}
 * virtually, so they pick up the override as well.
 */
public class ISServerConfigurationManager extends ServerConfigurationManager {

    /**
     * System property controlling how long to wait, in milliseconds, between observing the old server shut down and
     * beginning to poll for the new one. Defaults to {@value #DEFAULT_SETTLE_TIME_MILLIS}.
     */
    public static final String SETTLE_TIME_PROPERTY = "server.restart.settle.time";

    private static final long DEFAULT_SETTLE_TIME_MILLIS = 5000L;
    private static final long TIME_OUT = 600000L;

    private static final Log log = LogFactory.getLog(ISServerConfigurationManager.class);

    private final AutomationContext automationContext;
    private final LoginLogoutClient loginLogoutClient;
    private final String backEndUrl;
    private final String hostname;
    private final int port;

    public ISServerConfigurationManager(AutomationContext automationContext)
            throws AutomationUtilException, XPathExpressionException, MalformedURLException {

        super(automationContext);
        this.automationContext = automationContext;
        this.loginLogoutClient = new LoginLogoutClient(automationContext);
        this.backEndUrl = automationContext.getContextUrls().getBackEndUrl();
        URL url = new URL(backEndUrl);
        this.hostname = url.getHost();
        this.port = url.getPort();
    }

    @Override
    public void restartGracefully() throws AutomationUtilException {

        long start = System.currentTimeMillis();
        try {
            ServerAdminClient serverAdmin = new ServerAdminClient(backEndUrl, loginLogoutClient.login());
            serverAdmin.restartGracefully();
            ClientConnectionUtil.waitForPortClose(port, TIME_OUT, true, hostname);
            settle();
            ClientConnectionUtil.waitForPort(port, TIME_OUT, true, hostname);
            ClientConnectionUtil.waitForLogin(automationContext);
        } catch (Exception e) {
            throw new AutomationUtilException("Error while gracefully restarting the server", e);
        }
        log.info("Server restarted gracefully in " + (System.currentTimeMillis() - start) + " ms.");
    }

    @Override
    public void restartForcefully() throws AutomationUtilException {

        long start = System.currentTimeMillis();
        try {
            ServerAdminClient serverAdmin = new ServerAdminClient(backEndUrl, loginLogoutClient.login());
            serverAdmin.restart();
            // The framework implementation sleeps here instead of waiting for the port to close, which means the
            // subsequent waitForPort can succeed against the server that is still shutting down. Wait for the close
            // explicitly so the poll below can only observe the restarted server.
            ClientConnectionUtil.waitForPortClose(port, TIME_OUT, true, hostname);
            settle();
            ClientConnectionUtil.waitForPort(port, TIME_OUT, true, hostname);
            ClientConnectionUtil.waitForLogin(automationContext);
        } catch (Exception e) {
            throw new AutomationUtilException("Error while forcefully restarting the server", e);
        }
        log.info("Server restarted forcefully in " + (System.currentTimeMillis() - start) + " ms.");
    }

    private void settle() throws InterruptedException {

        long settleTime = DEFAULT_SETTLE_TIME_MILLIS;
        String configured = System.getProperty(SETTLE_TIME_PROPERTY);
        if (configured != null) {
            try {
                settleTime = Long.parseLong(configured.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid value '" + configured + "' for " + SETTLE_TIME_PROPERTY + ". Falling back to "
                        + DEFAULT_SETTLE_TIME_MILLIS + " ms.");
            }
        }
        if (settleTime > 0) {
            Thread.sleep(settleTime);
        }
    }
}
