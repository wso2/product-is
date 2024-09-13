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
package org.wso2.carbon.identity.jacoco;

import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for code coverage related operations.
 */
public class CodeCoverageUtils {

    private static final int BUFFER_SIZE = 1024;
    private static final String INVALID_EXTENSION_ERROR = "Invalid extension: %s is invalid";
    private static final String EXTRACTION_ERROR = "Error on archive extraction";

    /**
     * Extract jar files given at jar file path
     *
     * @param jarFilePath - Jar file path
     * @param tempDir     - Temporary directory to extract jar file
     * @return - Jar file extracted directory.
     * @throws IOException - Throws if jar extraction fails
     */
    public synchronized static String extractJarFile(String jarFilePath, File tempDir) throws IOException {

        if (!jarFilePath.endsWith(".war") && !jarFilePath.endsWith(".jar")) {
            throw new IllegalArgumentException(String.format(INVALID_EXTENSION_ERROR, jarFilePath));
        }

        String jarFileName = new File(jarFilePath).getName();
        String tempExtractedDir = new File(tempDir, jarFileName.substring(0, jarFileName.lastIndexOf('.'))).getPath();

        try {
            extractFile(jarFilePath, tempExtractedDir);
        } catch (IOException e) {
            throw new IOException("Could not extract the file " + jarFileName, e);
        }
        return tempExtractedDir;
    }

    /**
     * Scan given directory for include and exclude patterns.
     *
     * @param jarExtractedDir - Path to check for given include/exclude pattern
     * @param includes        - Include pattern array
     * @param excludes        - Exclude class pattern array
     * @return - Included files
     * @throws IOException - Throws if given directory path cannot be found.
     */
    public static String[] scanDirectory(String jarExtractedDir, String[] includes,
                                         String[] excludes) throws IOException {

        DirectoryScanner ds = new DirectoryScanner();

        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(new File(jarExtractedDir));
        ds.setCaseSensitive(true);

        ds.scan();
        return ds.getIncludedFiles();
    }

    private static void extractFile(String sourceFilePath, String extractedDir) throws IOException {

        byte[] buf = new byte[BUFFER_SIZE];
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(sourceFilePath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File newFile = new File(extractedDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zipInputStream.read(buf)) > 0) {
                            fos.write(buf, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new IOException(EXTRACTION_ERROR, e);
        }
    }
}
