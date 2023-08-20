/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.application.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDiscoverableApplicationServiceTestBase extends RESTAPIUserTestBase {

    public static final String API_DEFINITION_NAME = "application.yaml";
    public static final String API_VERSION = "v1";
    public static final String USER_APPLICATION_ENDPOINT_URI = "/me/applications";
    protected static String swaggerDefinition;
    protected static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.application.v1";
    protected static int TOTAL_DISCOVERABLE_APP_COUNT = 13;
    private static final String APP_NAME_PREFIX = "APP_";
    private static final String APP_DESC_PREFIX = "This is APP_";
    private static final String APP_IMAGE_URL = "https://dummy-image-url.com";
    private static final String APP_ACCESS_URL = "https://dummy-access-url.com";

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    protected List<ServiceProvider> serviceProviders = new ArrayList<>();
    protected ApplicationManagementServiceClient appMgtclient;

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        createServiceProviders();
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        super.conclude();
        deleteServiceProviders();
    }

    @BeforeMethod(alwaysRun = true)
    public void testMethodStart() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testMethodEnd() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    private void createServiceProviders() throws Exception {

        for (int i = 1; i <= TOTAL_DISCOVERABLE_APP_COUNT; i++) {
            ServiceProvider serviceProvider = createServiceProvider(APP_NAME_PREFIX + i, APP_DESC_PREFIX + i);
            if (serviceProvider != null) {
                serviceProviders.add(serviceProvider);
            }
        }

        // Reverse the SP list as they are ordered by created timestamp.
        Collections.reverse(serviceProviders);
    }

    private void deleteServiceProviders() throws Exception {

        for (int i = 1; i <= TOTAL_DISCOVERABLE_APP_COUNT; i++) {

            ServiceProvider serviceProvider = appMgtclient.getApplication(APP_NAME_PREFIX + i);
            if (serviceProvider != null) {
                appMgtclient.deleteApplication(serviceProvider.getApplicationName());
                log.info("############## " + "Deleted app: " + serviceProvider.getApplicationName());
            }
        }

        serviceProviders.clear();
    }

    protected ServiceProvider createServiceProvider(String appName, String appDescription) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(appName);
        serviceProvider.setDescription(appDescription);
        appMgtclient.createApplication(serviceProvider);

        serviceProvider = appMgtclient.getApplication(appName);
        if (serviceProvider != null) {
            serviceProvider.setDiscoverable(true);
            serviceProvider.setImageUrl(APP_IMAGE_URL);
            serviceProvider.setAccessUrl(APP_ACCESS_URL);
            appMgtclient.updateApplicationData(serviceProvider);

            return serviceProvider;
        }

        return null;
    }
}
