/*
 *  Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.auth;

import java.io.File;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

/**
 * Initiation Test for adaptive authentication.
 */
public class AdaptiveScriptInitializerTestCase extends AbstractAdaptiveAuthenticationTestCase {

    private ServerConfigurationManager serverConfigurationManager;

    private int javaVersion;
    private static String enableInfo = "Adaptive authentication successfully enabled.";
    private static String disableInfo = "Adaptive authentication successfully disabled.";

    @BeforeTest(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        javaVersion = getJavaVersion();
        // Download OpenJDK Nashorn only if the JDK version is Higher or Equal to 15.
        if (javaVersion >= 15) {
            runAdaptiveAuthenticationDependencyScript(false);
        }
    }

    /**
     * Get Java Major Version from System Property.
     *
     * @return Java Major Version
     */
    private int getJavaVersion() {

        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    private void runAdaptiveAuthenticationDependencyScript(boolean disable) {

        ServerLogReader inputStreamHandler;
        ServerLogReader errorStreamHandler;
        String targetFolder = System.getProperty("carbon.home");
        String scriptFolder = getTestArtifactLocation() + File.separator;
        Process tempProcess = null;
        File scriptFile = new File(scriptFolder);
        Runtime runtime = Runtime.getRuntime();

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                log.info("Operating System is Windows. Executing batch script");
                if (disable) {
                    // TODO https://github.com/wso2/product-is/issues/14301
                    restartServer();
                    tempProcess = runtime.exec(
                            new String[] { "cmd", "/c", "adaptive.bat", targetFolder, "DISABLE" }, null, scriptFile);
                } else {
                    tempProcess = runtime.exec(
                            new String[] { "cmd", "/c", "adaptive.bat", targetFolder }, null, scriptFile);
                }
                errorStreamHandler = new ServerLogReader("errorStream",
                        tempProcess.getErrorStream());
                inputStreamHandler = new ServerLogReader("inputStream",
                        tempProcess.getInputStream());
                inputStreamHandler.start();
                errorStreamHandler.start();
                boolean runStatus = waitForMessage(inputStreamHandler, disable);
                log.info("Status Message : " + runStatus);
                restartServer();
            } else {
                log.info("Operating system is not windows. Executing shell script");
                if (disable) {
                    tempProcess = runtime.getRuntime().exec(
                            new String[] { "/bin/bash", "adaptive.sh", targetFolder, "DISABLE" }, null, scriptFile);
                } else {
                    tempProcess = runtime.getRuntime().exec(
                            new String[] { "/bin/bash", "adaptive.sh", targetFolder }, null, scriptFile);
                }
                errorStreamHandler = new ServerLogReader("errorStream",
                        tempProcess.getErrorStream());
                inputStreamHandler = new ServerLogReader("inputStream",
                        tempProcess.getInputStream());
                inputStreamHandler.start();
                errorStreamHandler.start();
                boolean runStatus = waitForMessage(inputStreamHandler, disable);
                log.info("Status Message : " + runStatus);
                restartServer();
            }
        } catch (Exception e) {
            log.error("Failed to execute adaptive authentication dependency script", e);
        } finally {
            if (tempProcess != null) {
                tempProcess.destroy();
            }
        }
    }

    private void restartServer() throws AutomationUtilException {

        serverConfigurationManager.restartGracefully();
    }

    private boolean waitForMessage(ServerLogReader inputStreamHandler, boolean disable) {
        long time = System.currentTimeMillis() + 60 * 1000;
        String message = enableInfo;
        if (disable) {
            message = disableInfo;
        }
        while (System.currentTimeMillis() < time) {
            if (inputStreamHandler.getOutput().contains(message)) {
                return true;
            }
        }
        return false;
    }

    @AfterTest(alwaysRun = true)
    public void resetUserstoreConfig() throws Exception {

        super.init();
        javaVersion = (javaVersion == 0) ? getJavaVersion() : javaVersion;
        if (javaVersion >= 15) {
            runAdaptiveAuthenticationDependencyScript(false);
        }
        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
