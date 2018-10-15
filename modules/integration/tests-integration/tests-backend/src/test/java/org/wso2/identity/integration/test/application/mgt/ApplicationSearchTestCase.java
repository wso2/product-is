/*
*  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class ApplicationSearchTestCase extends ISIntegrationTest {

    private ConfigurationContext configContext;
    private ApplicationManagementServiceClient applicationManagementServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        applicationManagementServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Testing retrieve all applications basic information for a matching filter")
    public void testGetApplicationBasicInfoForFilter() {

        String applicationName = "TestServiceProvider";
        try {
            //filter 1 - all applications.
            String filter = "*";
            ApplicationBasicInfo[] applicationBasicInfos
                    = applicationManagementServiceClient.getApplicationBasicInfo(filter);

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                Assert.assertEquals(applicationBasicInfo.getApplicationName(), applicationName,
                        "Could not find application '" + applicationName + "' for filter:" + filter);
            }

            //filter 2
            filter = "*Provider";
            applicationBasicInfos = applicationManagementServiceClient.getApplicationBasicInfo(filter);

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                Assert.assertEquals(applicationBasicInfo.getApplicationName(), applicationName,
                        "Could not find application '" + applicationName + "' for filter:" + filter);
            }

            //filter 3
            filter = "*Provi*";
            applicationBasicInfos = applicationManagementServiceClient.getApplicationBasicInfo(filter);

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                Assert.assertEquals(applicationBasicInfo.getApplicationName(), applicationName,
                        "Could not find application '" + applicationName + "' for filter:" + filter);
            }

            //filter 4 - no applications.
            filter = "Provider";
            applicationBasicInfos = applicationManagementServiceClient.getApplicationBasicInfo(filter);
            Assert.assertEquals(applicationBasicInfos == null || applicationBasicInfos.length == 0, true,
                    "Does not return zero applications for filter:" + filter);
        } catch (Exception e) {
            Assert.fail("Error while trying to get all applications basic information for matching filter", e);
        }
    }

    @BeforeMethod
    public void setUp() {

        createApplication("TestServiceProvider");
    }

    @AfterMethod
    public void tearDown() {

        deleteApplication("TestServiceProvider");
    }

    public void deleteApplication(String applicationName) {

        try {
            applicationManagementServiceClient.deleteApplication(applicationName);
        } catch (Exception e) {
            Assert.fail("Error while trying to delete Service Provider", e);
        }
    }

    private void createApplication(String applicationName) {

        try {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("This is a test Service Provider");
            applicationManagementServiceClient.createApplication(serviceProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }
}
