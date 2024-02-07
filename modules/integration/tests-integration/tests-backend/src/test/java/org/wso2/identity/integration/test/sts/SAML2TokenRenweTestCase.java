/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied. See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.identity.integration.test.sts;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.SecurityAdminServiceClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class SAML2TokenRenweTestCase extends ISIntegrationTest {

    String carbonHome = CarbonUtils.getCarbonHome();
    SecurityAdminServiceClient sasc;
    ServerConfigurationManager scm;
    File carbonStsFile = new File(carbonHome + File.separator + "repository"
                                  + File.separator + "deployment" + File.separator + "server"
                                  + File.separator + "servicemetafiles" + File.separator
                                  + "org.wso2.carbon.sts.xml");

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        scm = new ServerConfigurationManager(isServer);
        sasc = new SecurityAdminServiceClient(backendURL, sessionCookie);
        String KeyStoreName = "wso2carbon.p12";
        SecurityAdminServiceClient securityAdminServiceClient = new SecurityAdminServiceClient(
                backendURL, sessionCookie);
        securityAdminServiceClient.applySecurity("wso2carbon-sts", "1",
                                                 new String[] { "everyone", "*", "admin" },
                                                 new String[] { KeyStoreName }, KeyStoreName);
        log.info("wso2carbon-sts Service Secured with UT");
    }

    @Test(alwaysRun = true, description = "runStsClient", priority = 1)
    public void runStsClient() {
        ServerLogReader inputStreamHandler;
        ServerLogReader errorStreamHandler;
        Process tempProcess = null;
        String testPath = getTestArtifactLocation() + File.separator + "stsclient" + File.separator;
        File shFile = new File(testPath);
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                log.info("Operating System is Windows, executing .bat file");
                tempProcess = rt.getRuntime().exec(
                        new String[] { "sts-client.bat" }, null, shFile);
            } else {
                log.info("Operating system is not windows. Executing shell script");
                tempProcess = rt.getRuntime().exec(
                        new String[] { "/bin/bash", "sts-client.sh" }, null, shFile);
            }
            errorStreamHandler = new ServerLogReader("errorStream",
                                                     tempProcess.getErrorStream());
            inputStreamHandler = new ServerLogReader("inputStream",
                                                     tempProcess.getInputStream());
            inputStreamHandler.start();
            errorStreamHandler.start();
            boolean runStatus = waitForMessage(inputStreamHandler, "Renewed SAML 2.0 Token is valid");
            log.info("Status Message : " + runStatus);
            assertTrue(runStatus);
        } catch (Exception e) {
            log.error("Failed to execute sts client script", e);
        } finally {
            if (tempProcess != null) {
                tempProcess.destroy();
            }
        }
    }

    public boolean waitForMessage(ServerLogReader inputStreamHandler,
                                  String message) {
        long time = System.currentTimeMillis() + 60 * 1000;
        while (System.currentTimeMillis() < time) {
            if (inputStreamHandler.getOutput().contains(message)) {
                return true;
            }
        }
        return false;
    }

    @AfterClass(alwaysRun = true)
    public void endTest() {
        try {
            scm.restoreToLastConfiguration();
        } catch (Exception e) {
            log.error("Failed to restore server configurations");
        }
    }
}
