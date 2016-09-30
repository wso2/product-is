/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.mb.osgi.test.util;

import org.ops4j.pax.exam.Option;

import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Utility class for PAX OSGi tests for MB product.
 */
public class ISOSGITestConfigurationUtils {

    /**
     * Gets the maven bundles required for andes dependency.
     *
     * @return A list of maven bundles.
     */
    public static List<Option> getBasicOSGiConfigurationList() {
        List<Option> optionList = new ArrayList<>();
        optionList.add(mavenBundle().groupId("com.hazelcast").artifactId("hazelcast").versionAsInProject());
        optionList.add(mavenBundle().groupId("libthrift.wso2").artifactId("libthrift").versionAsInProject());
        optionList.add(mavenBundle().groupId("commons-cli.wso2").artifactId("commons-cli").versionAsInProject());
        optionList.add(mavenBundle().groupId("commons-lang.wso2").artifactId("commons-lang").versionAsInProject());
        optionList.add(mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.datasources")
                .artifactId("org.wso2.carbon.datasource.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.jdbc.reporter").versionAsInProject());
        optionList.add(mavenBundle().groupId("org.wso2.carbon.metrics")
                .artifactId("org.wso2.carbon.metrics.core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics").artifactId("metrics-core").versionAsInProject());
        optionList.add(mavenBundle().groupId("io.dropwizard.metrics").artifactId("metrics-jvm").versionAsInProject());
        return optionList;
    }


    /**
     * Gets the maven bundles required for andes rest service dependency.
     *
     * @return A list of maven bundles.
     */
    public static List<Option> getRESTOSGiConfigurationList() {
        List<Option> optionList = new ArrayList<>();
        optionList.addAll(getBasicOSGiConfigurationList());
        optionList.add(mavenBundle().groupId("org.wso2.msf4j").artifactId("msf4j-core").versionAsInProject());
        optionList.add(mavenBundle().groupId("com.google.code.gson").artifactId("gson").versionAsInProject());
        return optionList;
    }
}
