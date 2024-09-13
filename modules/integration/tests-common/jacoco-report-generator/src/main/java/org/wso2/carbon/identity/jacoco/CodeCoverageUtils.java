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

public class CodeCoverageUtils {

    /**
     * Extract jar files given at jar file path
     *
     * @param jarFilePath - Jar file patch
     * @return - Jar file extracted directory.
     * @throws IOException - Throws if jar extraction fails
     */
    public synchronized static String extractJarFile(String jarFilePath, File tempDir)
            throws IOException {

        if (!jarFilePath.endsWith(".war") && !jarFilePath.endsWith(".jar")) {
            throw new IllegalArgumentException("Invalid extension" + jarFilePath + " is invalid");
        }

        String fileSeparator = (File.separatorChar == '\\') ? "\\" : File.separator;
        String jarFileName = jarFilePath;

        if (jarFilePath.lastIndexOf(fileSeparator) != -1) {
            jarFileName = jarFilePath.substring(jarFilePath.lastIndexOf(fileSeparator) + 1);
        }

        String tempExtractedDir = null;
        try {
            tempExtractedDir = tempDir + File.separator +
                    jarFileName.substring(0, jarFileName.lastIndexOf('.'));

            extractFile(jarFilePath, tempExtractedDir);
        } catch (IOException e) {
            System.out.println("Could not extract the file " + jarFileName);
        }
        return tempExtractedDir;
    }

    /**
     * Method to scan given directory for include and exclude patterns.
     *
     * @param jarExtractedDir - Patch to check for given include/exclude pattern
     * @param includes        - Include pattern array
     * @param excludes        - Exclude class pattern array
     * @return - Included files
     * @throws IOException - Throws if given directory patch cannot be found.
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

    public static void extractFile(String sourceFilePath, String extractedDir) throws IOException {
        FileOutputStream fileoutputstream = null;
        String fileDestination = extractedDir + File.separator;
        byte[] buf = new byte[1024];
        ZipInputStream zipinputstream = null;
        ZipEntry zipentry;
        try {
            zipinputstream = new ZipInputStream(new FileInputStream(sourceFilePath));
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = fileDestination + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                int n;
                File newFile = new File(entryName);
                if (zipentry.isDirectory()) {
                    if (!newFile.exists()) {
                        if (!newFile.mkdirs()) {
                            throw new IOException("Error occurred created new directory");
                        }
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                } else {
                    File resourceFile =
                            new File(entryName.substring(0, entryName.lastIndexOf(File.separator)));
                    if (!resourceFile.exists()) {
                        if (!resourceFile.mkdirs()) {
                            break;
                        }
                    }
                }
                fileoutputstream = new FileOutputStream(entryName);
                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }
                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }
            zipinputstream.close();
        } catch (IOException e) {
            System.out.println("Error on archive extraction ");
            throw new IOException("Error on archive extraction ", e);
        } finally {
            if (fileoutputstream != null) {
                fileoutputstream.close();
            }
            if (zipinputstream != null) {
                zipinputstream.close();
            }
        }
    }
}
