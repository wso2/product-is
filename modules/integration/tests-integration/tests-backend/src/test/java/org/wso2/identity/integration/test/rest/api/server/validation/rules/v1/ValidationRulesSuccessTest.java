/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.validation.rules.v1.model.MappingModel;
import org.wso2.identity.integration.test.rest.api.server.validation.rules.v1.model.RuleModel;
import org.wso2.identity.integration.test.rest.api.server.validation.rules.v1.model.ValidationConfigModel;

import java.util.Arrays;
import java.util.List;

public class ValidationRulesSuccessTest extends ValidationRulesTestBase {

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ValidationRulesSuccessTest(TestUserMode userMode) throws Exception {

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

    @Test (description = "test default response from get /validation-rules end point")
    public void testDefaultResponse() throws Exception {

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        String expectedResponse = readResource("default-response-positive.json");
        List<ValidationConfigModel> expectedValidationConfigModels =
                Arrays.asList(jsonWriter.readValue(expectedResponse, ValidationConfigModel[].class));

        Response response = getResponseOfGet(VALIDATION_RULES_PATH);
        response.then().log().ifValidationFails().assertThat()
                .statusCode(HttpStatus.SC_OK);
        List<ValidationConfigModel> retrievedValidationConfigModels =
                Arrays.asList(jsonWriter.readValue(response.asString(), ValidationConfigModel[].class));
        Assert.assertEquals(retrievedValidationConfigModels, expectedValidationConfigModels,
                "Response of the get /validation-rules doesn't match.");
    }

    @Test (description = "test for put /validation-rules/password end point",
            dependsOnMethods = "testDefaultResponse")
    public void testUpdatePasswordValidation() throws Exception {

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        String passwordValidationUpdateRequestBody
                = readResource("put-password-validation-update-positive.json");

        Response responseOfPut = getResponseOfPut(VALIDATION_RULES_PATH + PASSWORD,
                passwordValidationUpdateRequestBody);
        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ValidationConfigModel retrievedValidationConfigModel =
                jsonWriter.readValue(responseOfPut.asString(), ValidationConfigModel.class);
        Assert.assertEquals(retrievedValidationConfigModel.getField(), "password",
                "Response of the put /validation-rules/password doesn't contain password filed.");
        RuleModel ruleModel = new RuleModel();
        ruleModel.setValidator("LengthValidator");
        ruleModel.addPropertiesItem(new MappingModel().key("min.length").value("10"));
        ruleModel.addPropertiesItem(new MappingModel().key("max.length").value("30"));
        Assert.assertTrue(retrievedValidationConfigModel.getRules().contains(ruleModel),
                "Response of the put /validation-rules/password doesn't contain 10 as min length.");

        ruleModel = new RuleModel();
        ruleModel.setValidator("UpperCaseValidator");
        ruleModel.addPropertiesItem(new MappingModel().key("min.length").value("0"));
        Assert.assertTrue(retrievedValidationConfigModel.getRules().contains(ruleModel),
                "Response of the put /validation-rules/password doesn't contain 0 as UpperCaseValidator.");

        ruleModel = new RuleModel();
        ruleModel.setValidator("LowerCaseValidator");
        ruleModel.addPropertiesItem(new MappingModel().key("min.length").value("0"));
        Assert.assertTrue(retrievedValidationConfigModel.getRules().contains(ruleModel),
                "Response of the put /validation-rules/password doesn't contain 0 as LowerCaseValidator.");

        ruleModel = new RuleModel();
        ruleModel.setValidator("SpecialCharacterValidator");
        ruleModel.addPropertiesItem(new MappingModel().key("min.length").value("0"));
        Assert.assertTrue(retrievedValidationConfigModel.getRules().contains(ruleModel),
                "Response of the put /validation-rules/password doesn't contain 0 as SpecialCharacterValidator.");
    }

    @Test (description = "Test to set configuration to default.",dependsOnMethods = "testUpdatePasswordValidation")
    public void testSetBackToDefault() throws Exception{

        String defaultSetting = readResource("default-response-positive.json");
        Response responseOfPut = getResponseOfPut(VALIDATION_RULES_PATH, defaultSetting);
        responseOfPut.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}
