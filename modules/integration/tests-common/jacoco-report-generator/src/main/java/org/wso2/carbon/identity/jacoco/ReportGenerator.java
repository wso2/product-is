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
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This will create an XML report projects based on a single execution data store called jacoco.exec.
 */
public class ReportGenerator {

    private static final String CLASS_FILE_PATTERN = "**/*.class";
    private static final int BUFFER_SIZE = 1024;
    private static final String INVALID_EXTENSION_ERROR = "Invalid extension: %s is invalid";
    private static final String EXTRACTION_ERROR = "Error on archive extraction";

    private final String title;
    private final File executionDataFile;
    private final Set<File> classDirectories;
    private final File xmlReport;
    private final File tempDirectory;

    private ExecFileLoader execFileLoader;

    /**
     * Starts the report generation process
     *
     * @param args Arguments to the report generation.
     *             <executionDataFile> <classDirectory1> [<classDirectory2> ...]
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Usage: java -jar ReportGenerator.jar <executionDataFile> <classDirectory1> " +
                    "[<classDirectory2> ...]");
            System.exit(1);
        }

        File executionDataFile = new File(args[0]);
        Set<File> classDirectories = new HashSet<>();
        for (int i = 1; i < args.length; i++) {
            classDirectories.add(new File(args[i]));
        }

        try {
            final ReportGenerator generator = new ReportGenerator(executionDataFile, classDirectories);
            generator.create();
        } catch (Exception e) {
            System.err.println("Error while creating report: " + e.getMessage());
        }
    }

    /**
     * Create a new generator based for the given project.
     *
     * @param executionDataFile the execution data file
     * @param classDirectories  the set of class directories
     */
    public ReportGenerator(File executionDataFile, Set<File> classDirectories) {

        this.title = "Jacoco Coverage Report";
        this.executionDataFile = executionDataFile;
        this.classDirectories = classDirectories;
        this.xmlReport = new File("./report/jacoco.xml");
        this.tempDirectory = new File("./tmp");

        // Create report directory if it does not exist
        File reportDir = this.xmlReport.getParentFile();
        if (!reportDir.exists() && !reportDir.mkdirs()) {
            throw new RuntimeException("Failed to create report directory: " + reportDir.getAbsolutePath());
        }
    }

    /**
     * Create the report based on the jacoco.exec file.
     *
     * @throws IOException - Throws if report creation fails
     */
    public void create() throws IOException {

        // Read the jacoco.exec file. Multiple data files could be merged at this point
        loadExecutionData();

        // Run the structure analyzer on a single class folder to build up the coverage model. The process would be
        // similar if your classes were in a jar file. Typically, you would create a bundle for each class folder and
        // each jar you want in your report. If you have more than one bundle you will need to add a grouping node to
        // your report.
        final IBundleCoverage bundleCoverage = analyzeStructure();

        createReport(bundleCoverage);
    }

    private void loadExecutionData() throws IOException {

        execFileLoader = new ExecFileLoader();
        execFileLoader.load(executionDataFile);
    }

    private IBundleCoverage analyzeStructure() throws IOException {

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        // Track analyzed classes to avoid duplicates
        Set<String> analyzedClasses = new HashSet<>();

        List<File> jarFilesToAnalyze = new ArrayList<>();
        List<File> classFilesToAnalyze = new ArrayList<>();

        for (File classDirectory : classDirectories) {
            // Jar files to analyze
            File[] files =
                    classDirectory.listFiles((dir, name)
                            -> name.startsWith("org.wso2.carbon") && !name.contains(".stub_"));
            if (files != null) {
                jarFilesToAnalyze.addAll(Arrays.asList(files));
            }

            // Class files to analyze
            files = classDirectory.listFiles((dir, name) -> name.endsWith(".class"));
            if (files != null) {
                classFilesToAnalyze.addAll(Arrays.asList(files));
            }
        }

        String[] includes = {CLASS_FILE_PATTERN};
        String[] excludes = {"-*.stub*", "-*.stub_", "-*.stub_4.0.0", "-*.stub-"};

        for (final File jarFile : jarFilesToAnalyze) {
            String extractedDir = extractJarFile(jarFile.getAbsolutePath(), tempDirectory);
            String[] classFiles = scanDirectory(extractedDir, includes, excludes);

            for (String classFile : classFiles) {
                File file = new File(extractedDir + File.separator + classFile);
                String className = getClassNameFromFile(file);
                if (!analyzedClasses.contains(className)) {
                    try (InputStream input = new FileInputStream(file)) {
                        analyzer.analyzeClass(input, className);
                        analyzedClasses.add(className);
                    } catch (Exception e) {
                        System.err.println("Skipping duplicate or invalid class: " + className);
                    }
                }
            }
            FileUtils.forceDelete(new File(extractedDir));
        }

        for (final File classFile : classFilesToAnalyze) {
            String className = getClassNameFromFile(classFile);
            if (!analyzedClasses.contains(className)) {
                try (InputStream input = new FileInputStream(classFile)) {
                    analyzer.analyzeClass(input, className);
                    analyzedClasses.add(className);
                } catch (Exception e) {
                    System.err.println("Skipping duplicate or invalid class: " + className);
                }
            }
        }

        return coverageBuilder.getBundle(title);
    }

    /**
     * Create a concrete report visitor based on some supplied configuration. In this case we use the defaults.
     * @param bundleCoverage - Bundle coverage
     * @throws IOException
     */
    private void createReport(final IBundleCoverage bundleCoverage) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(xmlReport)) {
            final XMLFormatter xmlFormatter = new XMLFormatter();
            final IReportVisitor visitor = xmlFormatter.createVisitor(fos);

            // Initialize the report with all the execution and session information. At this point the report doesn't
            // know about the structure of the report being created
            visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                    execFileLoader.getExecutionDataStore().getContents());

            // Populate the report structure with the bundle coverage information.
            // Call visitGroup if you need groups in your report.
            visitor.visitBundle(bundleCoverage,
                    new DirectorySourceFileLocator(null, "utf-8", 4));

            // Signal end of structure information to allow report to write all information out.
            visitor.visitEnd();
        }
    }

    /**
     * Extract jar files given at jar file path
     *
     * @param jarFilePath - Jar file path
     * @param tempDir     - Temporary directory to extract jar file
     * @return - Jar file extracted directory.
     * @throws IOException - Throws if jar extraction fails
     */
    private String extractJarFile(String jarFilePath, File tempDir) throws IOException {

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
     */
    private String[] scanDirectory(String jarExtractedDir, String[] includes, String[] excludes) {

        DirectoryScanner ds = new DirectoryScanner();

        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(new File(jarExtractedDir));
        ds.setCaseSensitive(true);

        ds.scan();
        return ds.getIncludedFiles();
    }

    /**
     * Extract the given archive file to the given directory
     *
     * @param sourceFilePath - Path to the archive file
     * @param extractedDir   - Path to the directory to extract the archive file
     * @throws IOException - Throws if extraction fails
     */
    private void extractFile(String sourceFilePath, String extractedDir) throws IOException {

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

    /**
     * Get the class name from a class file. Converts file path to fully qualified class name.
     *
     * @param classFile - The class file
     * @return - The fully qualified class name
     */
    private String getClassNameFromFile(File classFile) {
        String path = classFile.getAbsolutePath();
        // Extract the class name by finding the package structure
        // This works by removing .class extension and converting path separators to dots
        int classIndex = path.indexOf("/org/wso2/");
        if (classIndex == -1) {
            classIndex = path.indexOf("\\org\\wso2\\");
        }
        if (classIndex != -1) {
            String className = path.substring(classIndex + 1);
            className = className.replace(".class", "");
            className = className.replace(File.separator, "/");
            return className;
        }
        // Fallback: use the absolute path
        return path;
    }
}
