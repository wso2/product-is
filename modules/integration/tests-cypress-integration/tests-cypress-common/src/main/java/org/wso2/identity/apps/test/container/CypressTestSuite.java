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
import java.util.StringJoiner;

/**
 * Cypress test suite class.
 */
public class CypressTestSuite {

    private List<CypressTest> tests = new ArrayList<>();

    private String title;

    public CypressTestSuite(String title) {

        this.title = title;
    }

    public String getTitle() {

        return title;
    }

    public List<CypressTest> getTests() {

        return tests;
    }

    public void add(CypressTest cypressTest) {

        tests.add(cypressTest);
    }

    @Override
    public String toString() {

        return new StringJoiner(", ", CypressTestSuite.class.getSimpleName() + "[", "]")
                .add("title='" + title + "'")
                .add("tests=" + tests.size())
                .toString();
    }

    public static class CypressTest {

        private String description;

        private boolean success;

        public CypressTest(String description, boolean success) {

            this.description = description;
            this.success = success;
        }

        public String getDescription() {

            return description;
        }

        public boolean isSuccess() {

            return success;
        }
    }
}
