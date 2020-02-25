/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.mgt.uuid;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.FileFilter;

public class UUIDUserManagerInitializerTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(UUIDUserManagerInitializerTestCase.class);
    private File uuidServiceJar;

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {

        log.info("Initializing UUID User manager initializer test case.");
        super.init();

        uuidServiceJar = getServiceJar();
        log.info("Copying the service jar to the dropins folder before restarting the server.");

        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.copyToComponentDropins(uuidServiceJar);
        serverConfigurationManager.restartGracefully();
    }

    private File getServiceJar() throws Exception {

        File directory = new File(getISResourceLocation() + File.separator + "userMgt");

        String pattern = "^(\\w*\\W)*(test\\.integration\\.service)(\\w*\\W)*(jar)";
        FileFilter filter = new RegexFileFilter(pattern);
        File [] files = directory.listFiles(filter);

        if (files == null || files.length == 0) {
            log.error("Unable to find the services JAR file.");
            throw new Exception("Cannot find the services JAR file.");
        }

        return files[0];
    }

    @AfterTest
    public void deInitTest() throws Exception {

        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.removeFromComponentDropins(uuidServiceJar.getName());
    }
}
