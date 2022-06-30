/*
 *  Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * Test for temporary claim persistence with adaptive authentication scripts.
 */
public class AdaptiveScriptInitializerTestCase extends AbstractAdaptiveAuthenticationTestCase {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeTest(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        runAdaptiveAuthenticationDependencyScript();
    }

    private void runAdaptiveAuthenticationDependencyScript() {

        ServerLogReader inputStreamHandler;
        ServerLogReader errorStreamHandler;
        String scriptFolder = (System.getProperty("carbon.home"))
                + File.separator + "bin" + File.separator;
        Process tempProcess = null;
        File shFile = new File(scriptFolder);
        Runtime rt = Runtime.getRuntime();

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                log.warn("Operating System is Windows, skipping execution");
            } else {
                log.info("Operating system is not windows. Executing shell script");
                tempProcess = rt.getRuntime().exec(
                        new String[] { "/bin/bash", "dependencydownloader.sh" }, null, shFile);
                errorStreamHandler = new ServerLogReader("errorStream",
                        tempProcess.getErrorStream());
                inputStreamHandler = new ServerLogReader("inputStream",
                        tempProcess.getInputStream());
                inputStreamHandler.start();
                errorStreamHandler.start();
                boolean runStatus = waitForMessage(inputStreamHandler, "Updating Adaptive Authentication Dependencies finished.");
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

    private void restartServer() throws AutomationUtilException, IOException, XPathExpressionException {

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.restartGracefully();
    }

    private boolean waitForMessage(ServerLogReader inputStreamHandler,
                                  String message) {
        long time = System.currentTimeMillis() + 60 * 1000;
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
        serverConfigurationManager.restoreToLastConfiguration(false);
        // TODO: delete - jars
    }
}
