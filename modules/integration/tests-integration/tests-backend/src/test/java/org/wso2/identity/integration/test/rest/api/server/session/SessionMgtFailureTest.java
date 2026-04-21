/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.session;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Test class for verifying session management configuration validation with invalid values.
 * This class tests negative scenarios where invalid values are provided for session timeout configurations.
 */
public class SessionMgtFailureTest extends SessionMgtTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SessionMgtFailureTest(TestUserMode userMode) throws Exception {

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
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    /**
     * Data provider for invalid non-boolean values for enableMaximumSessionTimeoutPeriod.
     *
     * @return Array of test data with invalid values and descriptions.
     */
    @DataProvider(name = "invalidBooleanValues")
    public Object[][] invalidBooleanValues() {

        return new Object[][]{
            {"invalid"},
            {"yes"},
            {"no"},
            {"1"},
            {"0"},
            {""}
        };
    }

    /**
     * Test updating enableMaximumSessionTimeoutPeriod with invalid non-boolean values.
     *
     * @param invalidValue Invalid value to test.
     */
    @Test(dataProvider = "invalidBooleanValues",
            description = "Test enableMaximumSessionTimeoutPeriod with invalid non-boolean values")
    public void testEnableMaxTimeoutWithInvalidValues(String invalidValue) {

        Response response = updateConfigurationAndGetResponse(ENABLE_MAX_TIMEOUT_KEY, invalidValue);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", notNullValue())
                .body("message", notNullValue());
    }

    /**
     * Data provider for invalid non-numeric values for timeout period fields.
     *
     * @return Array of test data with invalid values and descriptions.
     */
    @DataProvider(name = "invalidNumericValues")
    public Object[][] invalidNumericValues() {

        return new Object[][]{
            {"abc"},
            {"true"},
            {"-1"},
            {"-100"},
            {"0"},
            {"10.5"},
            {""}
        };
    }

    /**
     * Test updating maximumSessionTimeoutPeriod with invalid non-numeric values.
     *
     * @param invalidValue Invalid value to test.
     */
    @Test(dataProvider = "invalidNumericValues", 
          description = "Test maximumSessionTimeoutPeriod with invalid non-numeric values")
    public void testMaxTimeoutWithInvalidValues(String invalidValue) {

        Response response = updateConfigurationAndGetResponse(MAX_TIMEOUT_KEY, invalidValue);
        
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", notNullValue())
                .body("message", notNullValue());
    }

    /**
     * Helper method to update a single configuration field and return the response.
     *
     * @param configKey Configuration key to update.
     * @param value     Value to set.
     * @return Response from the PATCH request.
     */
    private Response updateConfigurationAndGetResponse(String configKey, String value) {

        List<Map<String, String>> patches = new ArrayList<>();
        patches.add(createPatchOperation(OPERATION_REPLACE, configKey, value));
        
        String patchBody = convertPatchesToJson(patches);
        return getResponseOfPatch(CONFIGS_API_PATH, patchBody);
    }
}
