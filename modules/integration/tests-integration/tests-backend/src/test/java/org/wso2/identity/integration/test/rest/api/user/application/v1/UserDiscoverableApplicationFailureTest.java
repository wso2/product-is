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

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.util.HashMap;
import java.util.Map;

public class UserDiscoverableApplicationFailureTest extends UserDiscoverableApplicationServiceTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserDiscoverableApplicationFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() {

        super.conclude();
    }

    @Test
    public void testGetApplicationWithInvalidApplicationId() {

        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI + "/" + "randomApp");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "APP-10001", "randomApp");
    }

    @Test
    public void testFilterApplicationsWithInvalidFilterAttribute() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "imageURL co APP_1 ");

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-10003", "imageURL");
    }

    @Test
    public void testFilterApplicationsWithInvalidFilterQuery() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("filter", "nameeq APP_1 ");

        }};
        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "APP-10004");
    }

    @Test
    public void testAttributeFilteringNotImplemented() {

        Map<String, Object> params = new HashMap<String, Object>() {{
            put("attributes", "id name");

        }};

        Response response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-15004");
    }

    @Test
    public void testSortingNotImplemented() {

        Map<String, Object> params;
        Response response;

        params = new HashMap<String, Object>() {{
            put("sortOrder", "asc");

        }};

        response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-15007");

        params = new HashMap<String, Object>() {{
            put("sortBy", "name");

        }};

        response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-15007");

        params = new HashMap<String, Object>() {{
            put("sortBy", "name");
            put("sortBy", "asc");

        }};

        response = getResponseOfGet(USER_APPLICATION_ENDPOINT_URI, params);
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-15007");
    }
}
