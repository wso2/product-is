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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.client.association;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

//@Listeners(PaxExam.class)
//@ExamReactorStrategy(PerClass.class)
public class LocalAccountAssociationClientServiceImplOsgiTest extends LocalAccountAssociationClientServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(LocalAccountAssociationClientServiceImplOsgiTest.class);

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] createConfiguration() {
        return options(mavenBundle().artifactId("org.wso2.carbon.selfcare.profile.bundle").
                groupId("org.wso2.carbon.is.userportal").versionAsInProject());
    }

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {

    }

    @org.testng.annotations.AfterMethod
    public void tearDown() throws Exception {

    }

}