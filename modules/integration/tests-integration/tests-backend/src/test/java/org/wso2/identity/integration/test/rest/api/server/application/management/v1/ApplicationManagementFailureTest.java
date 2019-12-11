/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

/**
 * Tests for negative paths of the Application Management REST API.
 */
public class ApplicationManagementFailureTest extends ApplicationManagementBaseTest {

    private static final String INVALID_APPLICATION_ID = "xxx";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementFailureTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testGetApplicationWithInvalidId() {

        Response response =
                getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + INVALID_APPLICATION_ID);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "APP-60006");
    }

    @Test
    public void testGetApplicationsWithSortByQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortBy=xxx");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65002");
    }

    @Test
    public void testGetApplicationsWithSortOrderQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?sortOrder=ASC");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65002");
    }

    @Test
    public void testGetApplicationsWithAttributesQueryParam() {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "?attributes=name,imageUrl");
        validateErrorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED, "APP-65003");
    }
}
