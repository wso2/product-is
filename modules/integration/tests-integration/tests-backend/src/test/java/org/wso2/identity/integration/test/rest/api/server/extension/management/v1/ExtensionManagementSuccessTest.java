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
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for happy paths of the Extension Management REST API.
 */
public class ExtensionManagementSuccessTest extends ExtensionManagementBaseTest{

    private final static String SPA_TEMPLATE_ID = "single-page-application";
    private final static String APPLICATION_EXTENSIONS = "applications";
    private final static String VERSION_KEY = "version";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ExtensionManagementSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @Test
    public void testApplicationExtensionListResponseIncludesTemplateVersion () {

        Response response =
                getResponseOfGet(EXTENSION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + APPLICATION_EXTENSIONS);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        List<Map<String, Object>> applicationExtensions = response.jsonPath().getList(".");
        for (Map<String, Object> extension: applicationExtensions) {
            Assert.assertNotNull(extension.get(VERSION_KEY));
        }
    }

    @Test
    public void testApplicationExtensionResponseIncludesTemplateVersion () {

        Response response = getResponseOfGet(
                EXTENSION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + APPLICATION_EXTENSIONS + PATH_SEPARATOR +
                        SPA_TEMPLATE_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("version", notNullValue());
    }
}
