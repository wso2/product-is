/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.apps.test.container;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Mochaawsome results strategy class.
 */
public class MochawesomeResultsStrategy implements TestResultsStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Path jsonReportsPath;

    private static final Log LOG = LogFactory.getLog(MochawesomeResultsStrategy.class);

    public MochawesomeResultsStrategy(Path jsonReportsPath) {

        this.jsonReportsPath = jsonReportsPath;
    }

    public MochawesomeResultsStrategy() {

        jsonReportsPath = FileSystems.getDefault().getPath("target", "test-classes", "test-utils",
                "output", "results", "mochawesome");
    }

    @Override
    public CypressTestResults gatherTestResults() throws IOException {

        CypressTestResults results = new CypressTestResults();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(jsonReportsPath, "*.json")) {
            for (Path path : paths) {
                try {
                    MochawesomeSpecRunReport specRunReport = objectMapper.readValue(path.toFile(),
                            MochawesomeSpecRunReport.class);
                    specRunReport.fillInTestResults(results);
                } catch (JsonMappingException e) {
                    LOG.warn("No test results were found in the report file:" + " " + path);
                }
            }

            return results;
        }
    }

    @Override
    public Path getReportsPath() {

        return jsonReportsPath;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MochawesomeSpecRunReport {

        private Stats stats;

        private List<Result> results;

        public Stats getStats() {

            return stats;
        }

        public void setStats(Stats stats) {

            this.stats = stats;
        }

        public List<Result> getResults() {

            return results;
        }

        public void setResults(List<Result> results) {

            this.results = results;
        }

        public void fillInTestResults(CypressTestResults results) {

            results.addNumberOfTests(stats.getTests());
            results.addNumberOfPassingTests(stats.getPasses());
            results.addNumberOfFailingTests(stats.getFailures());

            for (Result result : getResults()) {
                List<Suite> suites = result.getSuites();

                List<CypressTestSuite> cypressTestSuites = new ArrayList<>();
                for (Suite suite : suites) {
                    CypressTestSuite cypressTestSuite = new CypressTestSuite(suite.getTitle());
                    List<SuiteTest> tests = suite.getTests();
                    for (SuiteTest test : tests) {
                        cypressTestSuite.add(new CypressTestSuite.CypressTest(test.getTitle(), !test.isFail()));
                    }

                    cypressTestSuites.add(cypressTestSuite);
                }

                results.addSuites(cypressTestSuites);
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Stats {

            private int tests;

            private int passes;

            private int failures;

            public int getTests() {

                return tests;
            }

            public void setTests(int tests) {

                this.tests = tests;
            }

            public int getPasses() {

                return passes;
            }

            public void setPasses(int passes) {

                this.passes = passes;
            }

            public int getFailures() {

                return failures;
            }

            public void setFailures(int failures) {

                this.failures = failures;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Result {

            private List<Suite> suites;

            public List<Suite> getSuites() {

                return suites;
            }

            public void setSuites(List<Suite> suites) {

                this.suites = suites;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Suite {

            private String title;

            private List<SuiteTest> tests;

            public String getTitle() {

                return title;
            }

            public void setTitle(String title) {

                this.title = title;
            }

            public List<SuiteTest> getTests() {

                return tests;
            }

            public void setTests(List<SuiteTest> tests) {

                this.tests = tests;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class SuiteTest {

            private String title;

            private boolean fail;

            public String getTitle() {

                return title;
            }

            public void setTitle(String title) {

                this.title = title;
            }

            public boolean isFail() {

                return fail;
            }

            public void setFail(boolean fail) {

                this.fail = fail;
            }
        }
    }
}
