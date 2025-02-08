/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.extension.management.v1;

import io.restassured.response.Response;

import java.io.IOException;
import org.apache.http.HttpStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

/**
 * Tests for checking application templates.
 */
public class ExtensionManagementSuccessTest extends ExtensionManagementBaseTest{

    private final static String APPLICATION_EXTENSIONS = "applications";
    private final static  String SIMPLE_TEMPLATE = "google";
    private final static String COMPLEX_TEMPLATE = "salesforce";
    private final static String TEMPLATE = "template";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ExtensionManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }
    
    @Test
    public void getSimpleApplicationTemplate() throws IOException {

        Response response = getResponseOfGet(
                EXTENSION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + APPLICATION_EXTENSIONS + PATH_SEPARATOR +
                        SIMPLE_TEMPLATE+PATH_SEPARATOR+TEMPLATE);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getComplexApplicationTemplate() throws IOException {

        Response response = getResponseOfGet(
                EXTENSION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + APPLICATION_EXTENSIONS + PATH_SEPARATOR +
                        COMPLEX_TEMPLATE+PATH_SEPARATOR+TEMPLATE);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}
