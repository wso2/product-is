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

import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.xml.XMLFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * This will create an XML report projects based on a
 * single execution data store called jacoco.exec.
 */
public class ReportGenerator {

	public static final String CLASS_FILE_PATTERN = "**/*.class";

	private final String title;

	private final File executionDataFile;
	private final Set<File> classDirectories;
	private final File xmlReport;
	private final File tempDirectory;

	private ExecFileLoader execFileLoader;

	/**
	 * Create a new generator based for the given project.
	 *
	 * @param executionDataFile the execution data file
	 * @param classDirectories the set of class directories
	 */
	public ReportGenerator(File executionDataFile, Set<File> classDirectories) {
		this.title = "something";
		this.executionDataFile = executionDataFile;
		this.classDirectories = classDirectories;
		this.xmlReport = new File("./report/jacoco.xml");
		this.tempDirectory = new File("./tmp");

		// Create report directory if it does not exist
		File reportDir = this.xmlReport.getParentFile();
		if (!reportDir.exists()) {
			if (!reportDir.mkdirs()) {
				throw new RuntimeException("Failed to create report directory: " + reportDir.getAbsolutePath());
			}
		}
	}

	/**
	 * Create the report.
	 *
	 * @throws IOException
	 */
	public void create() throws IOException {

		// Read the jacoco.exec file. Multiple data files could be merged
		// at this point
		loadExecutionData();

		// Run the structure analyzer on a single class folder to build up
		// the coverage model. The process would be similar if your classes
		// were in a jar file. Typically you would create a bundle for each
		// class folder and each jar you want in your report. If you have
		// more than one bundle you will need to add a grouping node to your
		// report
		final IBundleCoverage bundleCoverage = analyzeStructure();

		createReport(bundleCoverage);

	}

	private void createReport(final IBundleCoverage bundleCoverage)
			throws IOException {

		// Create a concrete report visitor based on some supplied
		// configuration. In this case we use the defaults
		final XMLFormatter xmlFormatter = new XMLFormatter();
		final IReportVisitor visitor = xmlFormatter.createVisitor(new FileOutputStream(xmlReport));

		// Initialize the report with all the execution and session
		// information. At this point the report doesn't know about the
		// structure of the report being created
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(bundleCoverage,
				new DirectorySourceFileLocator(null, "utf-8", 4));

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();
	}

	private void loadExecutionData() throws IOException {
		execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);
	}

	private IBundleCoverage analyzeStructure() throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		List<File> jarFilesToAnalyze = new ArrayList<>();
		List<File> classFilesToAnalyze = new ArrayList<>();

		for (File classDirectory : classDirectories) {
			// Jar files to analyze
			File [] files = classDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("org.wso2.carbon") && !name.contains(".stub_");
				}
			});

            if (files != null) {
                jarFilesToAnalyze.addAll(Arrays.asList(files));
            }

			// Class files to analyze
			files = classDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".class");
				}
			});

			if (files != null) {
				classFilesToAnalyze.addAll(Arrays.asList(files));
			}
        }

		String[] includes = {CLASS_FILE_PATTERN};
		String[] excludes = {"-*.stub*", "-*.stub_", "-*.stub_4.0.0", "-*.stub-"};

		for (final File jarFile : jarFilesToAnalyze) {

			String extractedDir = CodeCoverageUtils.extractJarFile(jarFile.getAbsolutePath(), tempDirectory);
			String[] classFiles = CodeCoverageUtils.scanDirectory(extractedDir, includes, excludes);

			for (String classFile : classFiles) {
				analyzer.analyzeAll(new File(extractedDir + File.separator + classFile));
			}
			FileUtils.forceDelete(new File(extractedDir));
		}

		for (final File classFile : classFilesToAnalyze) {
			analyzer.analyzeAll(classFile);
		}

		return coverageBuilder.getBundle(title);
	}

	/**
	 * Starts the report generation process
	 *
	 * @param args Arguments to the report generation.
	 *                <executionDataFile> <classDirectory1> [<classDirectory2> ...]
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Usage: java -jar ReportGenerator.jar <executionDataFile> <classDirectory1> [<classDirectory2> ...]");
			System.exit(1);
		}

		File executionDataFile = new File(args[0]);
		Set<File> classDirectories = new HashSet<File>();
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
}
