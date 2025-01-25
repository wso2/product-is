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

package org.wso2.identity.integration.test.rest.api.server.rules.metadata.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class RulesMetadataSuccessTest extends RulesMetadataTestBase {

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public RulesMetadataSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.initTestClass(tenant);
    }

    @AfterClass(alwaysRun = true)
    public void conclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @DataProvider(name = "flowProvider")
    public static Object[][] getFlows() {

        return new Object[][]{
                {"preIssueAccessToken"},
        };
    }

    @Test(dataProvider = "flowProvider")
    public void testGetRuleMetadata(String flow) throws Exception {

        Response responseOfGet = getResponseOfGet(getAPIRequestForValidFlow(flow));
        validateResponse(flow, responseOfGet);
    }

    private static void validateResponse(String flow, Response response) {

        if (flow.equals("preIssueAccessToken")) {
            validateResponseForPreIssueAccessTokenFlow(response);
        } else {
            throw new IllegalArgumentException("Invalid flow: " + flow);
        }
    }

    private static void validateResponseForPreIssueAccessTokenFlow(Response response) {

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("[0].field.name", equalTo("application"))
                .body("[0].field.displayName", equalTo("application"))
                .body("[0].operators.name", hasItems("equals", "notEquals"))
                .body("[0].operators.displayName", hasItems("equals", "not equals"))
                .body("[0].value.inputType", equalTo("options"))
                .body("[0].value.valueType", equalTo("reference"))
                .body("[0].value.valueReferenceAttribute", equalTo("id"))
                .body("[0].value.valueDisplayAttribute", equalTo("name"))
                .body("[0].value.links.href",
                        hasItems("/applications?excludeSystemPortals=true&offset=0&limit=10",
                                "/applications?excludeSystemPortals=true&filter=&limit=10"))
                .body("[0].value.links.method", hasItems("GET"))
                .body("[0].value.links.rel", hasItems("values", "filter"))
                .body("[1].field.name", equalTo("grantType"))
                .body("[1].field.displayName", equalTo("grant type"))
                .body("[1].operators.name", hasItems("equals", "notEquals"))
                .body("[1].operators.displayName", hasItems("equals", "not equals"))
                .body("[1].value.inputType", equalTo("options"))
                .body("[1].value.valueType", equalTo("string"))
                .body("[1].value.values.name",
                        hasItems("authorization_code", "password", "refresh_token", "client_credentials"))
                .body("[1].value.values.displayName",
                        hasItems("authorization code", "password", "refresh token", "client credentials"));
    }
}
