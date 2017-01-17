/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api.unit.test.util;

import org.ops4j.pax.exam.Option;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.util.ArrayList;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * This class contains the utility methods for carbon-security-caas OSGI tests.
 */
public class UserPortalOSGiTestUtils {

    /**
     * Returns the default list of PAX options needed for carbon-security-caas OSGI test.
     *
     * @return list of Options
     */
    public static List<Option> getDefaultSecurityPAXOptions() {

        List<Option> defaultOptionList = new ArrayList<>();

        defaultOptionList.add(mavenBundle()
                .groupId("org.ops4j.pax.logging")
                .artifactId("pax-logging-api")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.ops4j.pax.logging")
                .artifactId("pax-logging-log4j2")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("net.minidev.wso2")
                .artifactId("json-smart")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("commons-io.wso2")
                .artifactId("commons-io")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("net.minidev.wso2")
                .artifactId("json-smart")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("net.minidev")
                .artifactId("asm")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon")
                .artifactId("org.wso2.carbon.core")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.messaging")
                .artifactId("org.wso2.carbon.messaging")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.caching")
                .artifactId("org.wso2.carbon.caching")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("net.minidev.wso2")
                .artifactId("json-smart")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.caas")
                .artifactId("org.wso2.carbon.security.caas.boot")
                .versionAsInProject().noStart());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.caas")
                .artifactId("org.wso2.carbon.security.caas")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.identity.mgt")
                .artifactId("org.wso2.carbon.identity.mgt")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.identity.mgt")
                .artifactId("org.wso2.carbon.identity.meta.claim.mgt")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.uuf")
                .artifactId("org.wso2.carbon.uuf.core")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.ow2.asm")
                .artifactId("asm")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("com.google.guava")
                .artifactId("guava")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("com.google.code.gson")
                .artifactId("gson")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.apache.commons")
                .artifactId("commons-lang3")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.caas")
                .artifactId("org.wso2.carbon.security.caas.boot")
                .versionAsInProject().noStart());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.security.caas")
                .artifactId("org.wso2.carbon.security.caas")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.identity.mgt")
                .artifactId("org.wso2.carbon.identity.mgt")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.identity.mgt")
                .artifactId("org.wso2.carbon.identity.meta.claim.mgt")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.datasources")
                .artifactId("org.wso2.carbon.datasource.core")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.jndi")
                .artifactId("org.wso2.carbon.jndi")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("com.zaxxer")
                .artifactId("HikariCP")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("com.h2database")
                .artifactId("h2")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.is")
                .artifactId("org.wso2.is.portal.user.client.api")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.carbon.deployment")
                .artifactId("org.wso2.carbon.deployment.engine")
                .versionAsInProject());
        defaultOptionList.add(mavenBundle()
                .groupId("org.wso2.orbit.org.yaml")
                .artifactId("snakeyaml")
                .versionAsInProject());


        CarbonSysPropConfiguration sysPropConfiguration = new CarbonSysPropConfiguration();
        sysPropConfiguration.setCarbonHome(getCarbonHome());
        sysPropConfiguration.setServerKey("carbon-security");
        sysPropConfiguration.setServerName("WSO2 Carbon Security Server");
        sysPropConfiguration.setServerVersion("1.0.0");

        defaultOptionList = OSGiTestConfigurationUtils.getConfiguration(defaultOptionList, sysPropConfiguration);

        return defaultOptionList;
    }

    public static String getCarbonHome() {
        return System.getProperty("carbon.home");
    }
}

