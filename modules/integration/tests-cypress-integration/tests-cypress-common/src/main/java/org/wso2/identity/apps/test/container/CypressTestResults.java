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

import java.util.ArrayList;
import java.util.List;

/**
 * Cypress test results class.
 */
public class CypressTestResults {

    private int numberOfTests;

    private int numberOfPassingTests;

    private int numberOfFailingTests;

    private List<CypressTestSuite> suites = new ArrayList<>();

    public int getNumberOfTests() {

        return numberOfTests;
    }

    public void setNumberOfTests(int numberOfTests) {

        this.numberOfTests = numberOfTests;
    }

    public int getNumberOfPassedTests() {

        return numberOfPassingTests;
    }

    public void setNumberOfPassedTests(int numberOfPassedTests) {

        this.numberOfPassingTests = numberOfPassedTests;
    }

    public int getNumberOfFailedTests() {

        return numberOfFailingTests;
    }

    public void setNumberOfFailedTests(int numberOfFailedTests) {

        this.numberOfFailingTests = numberOfFailedTests;
    }

    public void addNumberOfTests(int tests) {

        numberOfTests += tests;
    }

    public void addNumberOfPassingTests(int passes) {

        numberOfPassingTests += passes;
    }

    public void addNumberOfFailingTests(int failures) {

        numberOfFailingTests += failures;
    }

    public List<CypressTestSuite> getSuites() {

        return suites;
    }

    public void addSuites(List<CypressTestSuite> suites) {

        this.suites.addAll(suites);
    }

    @Override
    public String toString() {

        return String.format("Cypress tests run: %s%n Cypress tests passing: %s%n Cypress tests failing: %s",
                numberOfTests, numberOfPassingTests, numberOfFailingTests);
    }
}
