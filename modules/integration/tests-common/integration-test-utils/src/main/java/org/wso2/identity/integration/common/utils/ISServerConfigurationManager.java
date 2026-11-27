/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.utils.ClientConnectionUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.xpath.XPathExpressionException;

/**
 * ServerConfigurationManager that restarts the server without fixed sleeps.
 *
 * The automation framework's restart waits for the old server to go down, then sleeps 40 seconds (25 for a forced
 * restart) before checking the new one, while IS restarts in about 15 seconds. This class does the same restart and
 * the same checks, but waits for events instead: the old server's port closes, the new server's port opens, admin
 * login works, and the server log shows a new "WSO2 Carbon started" line (it is fully started). Tests see the same
 * freshly started server with the same configuration, sooner.
 */
public class ISServerConfigurationManager extends ServerConfigurationManager {

    private static final Log LOG = LogFactory.getLog(ISServerConfigurationManager.class);
    private static final String STARTED = "WSO2 Carbon started";
    private static final long DEFAULT_TIMEOUT_MS = 600000;
    private static final long LOG_WAIT_MS = 120000;

    private final AutomationContext context;
    private final String backEndUrl;
    private final int port;
    private final String hostname;

    public ISServerConfigurationManager(AutomationContext context)
            throws AutomationUtilException, XPathExpressionException, MalformedURLException {

        super(context);
        this.context = context;
        this.backEndUrl = context.getContextUrls().getBackEndUrl();
        URL url = new URL(backEndUrl);
        this.port = url.getPort();
        this.hostname = url.getHost();
    }

    public ISServerConfigurationManager(String productGroup, TestUserMode userMode)
            throws AutomationUtilException, XPathExpressionException, MalformedURLException {

        this(new AutomationContext(productGroup, userMode));
    }

    @Override
    public void restartGracefully(long timeout) throws AutomationUtilException {

        restart(false, timeout);
    }

    @Override
    public void restartForcefully() throws AutomationUtilException {

        restart(true, DEFAULT_TIMEOUT_MS);
    }

    private void restart(boolean force, long timeout) throws AutomationUtilException {

        long start = System.currentTimeMillis();
        Path log = Paths.get(getCarbonHome(), "repository", "logs", "wso2carbon.log");
        int startedBefore = countStarted(log, port);
        long down;
        long up;
        long loggedIn;
        try {
            String sessionCookie = new LoginLogoutClient(context).login();
            ServerAdminClient admin = new ServerAdminClient(backEndUrl, sessionCookie);
            if (force) {
                admin.restart();
            } else {
                admin.restartGracefully();
            }
            ClientConnectionUtil.waitForPortClose(port, timeout, true, hostname);
            down = System.currentTimeMillis();
            ClientConnectionUtil.waitForPort(port, timeout, true, hostname);
            up = System.currentTimeMillis();
            ClientConnectionUtil.waitForLogin(context);
            loggedIn = System.currentTimeMillis();
        } catch (Exception e) {
            throw new AutomationUtilException("Error while " + (force ? "forcefully" : "gracefully")
                    + " restarting the server", e);
        }
        boolean logChecked = waitForStartedLine(log, startedBefore);
        long end = System.currentTimeMillis();
        // One line per restart, for measuring restart cost (grep RESTART_METRIC in the test logs). "old_ms" is what
        // the framework's restart would have taken: same shutdown, then a fixed 40 s (25 s when forced) instead of
        // waiting for the server, then the port and login checks.
        long oldMs = (down - start) + Math.max(force ? 25000 : 40000, loggedIn - down);
        LOG.info(String.format("RESTART_METRIC caller=%s type=%s port=%d shutdown_ms=%d boot_ms=%d login_ms=%d "
                        + "started_ms=%d total_ms=%d old_ms=%d log_checked=%s", caller(), force ? "forced" : "graceful",
                port, down - start, up - down, loggedIn - up, end - loggedIn, end - start, oldMs, logChecked));
    }

    /** Waits until the server log has a "WSO2 Carbon started" line that wasn't there before the restart. */
    private boolean waitForStartedLine(Path log, int startedBefore) throws AutomationUtilException {

        if (startedBefore < 0) {
            return false; // No log to check (e.g. a server outside carbon.home); port and login checks passed.
        }
        long deadline = System.currentTimeMillis() + LOG_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            int now = countStarted(log, port);
            // A smaller count means the log was rotated; any started line in the new file is from this restart.
            if (now > startedBefore || (now >= 0 && now < startedBefore && now > 0)) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AutomationUtilException("Interrupted while waiting for the server to start", e);
            }
        }
        LOG.warn("No new '" + STARTED + "' line in " + log + " within " + LOG_WAIT_MS / 1000
                + " s after the restart; continuing since the server accepts logins.");
        return false;
    }

    /** The test class that asked for the restart. */
    private static String caller() {

        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            String c = e.getClassName();
            if (c.startsWith("org.wso2.identity.integration.test.")) {
                return c.substring(c.lastIndexOf('.') + 1);
            }
        }
        return "unknown";
    }

    /**
     * Number of "WSO2 Carbon started" lines in the log, or -1 if there is no log or it isn't this server's log (its
     * startup lines don't mention this server's port; carbon.home can point at another server, e.g. a secondary IS).
     */
    private static int countStarted(Path log, int port) {

        if (!Files.exists(log)) {
            return -1;
        }
        try {
            String text = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            if (!text.contains(":" + port + "/")) {
                return -1;
            }
            int count = 0;
            for (int i = text.indexOf(STARTED); i >= 0; i = text.indexOf(STARTED, i + 1)) {
                count++;
            }
            return count;
        } catch (IOException e) {
            return -1;
        }
    }
}
