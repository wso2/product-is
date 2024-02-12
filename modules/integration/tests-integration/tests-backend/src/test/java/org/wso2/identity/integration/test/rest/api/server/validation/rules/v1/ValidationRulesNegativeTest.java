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

package org.wso2.identity.integration.test.rest.api.server.validation.rules.v1;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

public class ValidationRulesNegativeTest extends ValidationRulesTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ValidationRulesNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test(description = "Negative test case to test bad request where the alpha numeric validation doesn't " +
            "have proper numeric validation properties.")
    public void testNegativeUsernameValidationUpdate() throws Exception {

        String NegativeInputRequestBody =
                readResource("put-enable-alphanumeric-type-username-validation-negative.json");
        Response responseOfPut = getResponseOfPut(VALIDATION_RULES_PATH, NegativeInputRequestBody);
        ValidatableResponse validatableResponse = responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
        String responseBody = validatableResponse.extract().body().asString();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseBody);
        if (json == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        Assert.assertEquals(json.get("code"),"IVM-60027","Error code of Bad Request for " +
                "password validation update is not match.");
        Assert.assertEquals(json.get("message"),"Unable to update input validation configurations.",
                "Error message of Bad Request for password validation update is not match");
        Assert.assertEquals(json.get("description"),"LengthValidator must be configured along " +
                        "with the AlphanumericValidator for username.",
                "Error description of Bad Request for password validation update is not match");
        Assert.assertTrue(json.containsKey("traceId"));
    }
}
