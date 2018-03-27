/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.extension.server;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IdentityServerExtension extends CarbonServerExtension {

    private Logger log = LoggerFactory.getLogger(IdentityServerExtension.class);

    private static final String BACKUP_TEST_PACK = "backup-test-pack";
    private static final String BACKUP_LOCATION = "backup-location";

    @Override
    public void initiate() {

        // We have to override this method since this class is loaded by reflection and "getDeclaredMethod()" is used to
        // call this method. "getDeclaredMethod()" does not go through inheritance hierarchy.
        super.initiate();
    }

    @Override
    public void onExecutionStart() {

        // We have to override this method since this class is loaded by reflection and "getDeclaredMethod()" is used to
        // call this method. "getDeclaredMethod()" does not go through inheritance hierarchy.
        super.onExecutionStart();
    }

    @Override
    public void onExecutionFinish() {

        Map<String, String> testParameters = getParameters();

        if (testParameters.containsKey(BACKUP_TEST_PACK)
                && Boolean.parseBoolean(testParameters.get(BACKUP_TEST_PACK))) {

            String backupLocation = testParameters.get(BACKUP_LOCATION);

            if (StringUtils.isNotEmpty(backupLocation)) {
                String carbonHome = System.getProperty(ExtensionConstants.CARBON_HOME);
                try {
                    backUpTheDirectory(carbonHome, backupLocation);
                } catch (IOException e) {
                    log.error("Error while backing up the test pack.", e);
                }
            }
        }

        super.onExecutionFinish();
    }

    private void backUpTheDirectory(String sourceDirPath, String zipFilePath) throws IOException {

        zipFilePath = zipFilePath + "wso2is-bak-"  + System.currentTimeMillis() + ".zip";

        Path zipPath = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Path sourcePath = Paths.get(sourceDirPath);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zipOutputStream.putNextEntry(zipEntry);
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                            log.info("Compressing: {}", path);
                        } catch (IOException e) {
                            log.error("Error while performing zip operation.", e);
                        }
                    });
        }
    }
}
