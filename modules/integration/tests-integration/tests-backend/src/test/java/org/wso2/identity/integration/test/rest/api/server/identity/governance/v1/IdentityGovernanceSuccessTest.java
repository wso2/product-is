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

package org.wso2.identity.integration.test.rest.api.server.identity.governance.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.CategoriesRes;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.CategoryRes;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorRes;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyRes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test class for Claim Management REST APIs success path.
 */
public class IdentityGovernanceSuccessTest extends IdentityGovernanceTestBase {

    private static final String CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES = "QWNjb3VudCBNYW5hZ2VtZW50IFBvbGljaWVz";
    private static final String CONNECTOR_LOCK_IDLE_ACCOUNTS = "c3VzcGVuc2lvbi5ub3RpZmljYXRpb24";
    private Map<String, CategoriesRes> categories;

    @Factory(dataProvider = "restAPIServerConfigProvider")
    public IdentityGovernanceSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        String expectedResponse = readResource("get-categories-response.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<CategoriesRes> categoryList =
                Arrays.asList(jsonWriter.readValue(expectedResponse, CategoriesRes[].class));
        categories = categoryList.stream().collect(Collectors.toMap(CategoriesRes::getId, c -> c));

    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIServerConfigProvider")
    public static Object[][] restAPIServerConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetGovernanceConnectors() {

        Response response = getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI);
        ValidatableResponse validatableResponse = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        for (Map.Entry<String, CategoriesRes> category : categories.entrySet()) {
            validatableResponse.body("find{ it.id == '" + category.getKey() + "' }.name",
                    equalTo(category.getValue().getName()));

            validatableResponse.body("find{ it.id == '" + category.getKey() + "' }.self",
                    equalTo("/t/" + tenant + "/api/server/v1" + IDENTITY_GOVERNANCE_ENDPOINT_URI + "/" +
                            category.getValue().getId()));
        }
    }

    @Test
    public void testGetGovernanceConnectorCategory() throws IOException {

        for (Map.Entry<String, CategoriesRes> category : categories.entrySet()) {
            String expectedResponse = readResource("get-category-" + category.getKey() + "-response.json");
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            List<ConnectorRes> connectorsList =
                    jsonWriter.readValue(expectedResponse, CategoryRes.class).getConnectors();
            Map<String, ConnectorRes> connectors =
                    connectorsList.stream().collect(Collectors.toMap(ConnectorRes::getId, c -> c));
            Response response = getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/" + category.getKey());
            ValidatableResponse validatableResponse = response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK);
            for (Map.Entry<String, ConnectorRes> connector : connectors.entrySet()) {
                validatableResponse.body("connectors.find{ it.id == '" + connector.getKey() + "' }.name",
                        equalTo(connector.getValue().getName()))
                        .body("connectors.find{ it.id == '" + connector.getKey() + "' }.category",
                                equalTo(connector.getValue().getCategory()))
                        .body("connectors.find{ it.id == '" + connector.getKey() + "' }.friendlyName",
                                equalTo(connector.getValue().getFriendlyName()))
                        .body("connectors.find{ it.id == '" + connector.getKey() + "' }.order",
                                equalTo(connector.getValue().getOrder()))
                        .body("connectors.find{ it.id == '" + connector.getKey() + "' }.subCategory",
                                equalTo(connector.getValue().getSubCategory()));
                Map<String, PropertyRes> properties =
                        connector.getValue().getProperties().stream()
                                .collect(Collectors.toMap(PropertyRes::getName, c -> c));
                for (Map.Entry<String, PropertyRes> property : properties.entrySet()) {
                    validatableResponse
                            .body("connectors.find{ it.id == '" + connector.getKey() + "' }.properties.find{it.name " +
                                            "== '" + property.getValue().getName() + "' }.displayName",
                                    equalTo(property.getValue().getDisplayName()))
                            .body("connectors.find{ it.id == '" + connector.getKey() + "' }.properties.find{it.name " +
                                            "== '" + property.getValue().getName() + "' }.description",
                                    equalTo(property.getValue().getDescription()));
                }
            }
        }
    }

    @Test
    public void testGetGovernanceConnector() throws IOException {

        for (Map.Entry<String, CategoriesRes> category : categories.entrySet()) {
            String expectedResponse = readResource("get-category-" + category.getKey() + "-response.json");
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            List<ConnectorRes> connectorsList =
                    jsonWriter.readValue(expectedResponse, CategoryRes.class).getConnectors();
            Map<String, ConnectorRes> connectors =
                    connectorsList.stream().collect(Collectors.toMap(ConnectorRes::getId, c -> c));

            for (Map.Entry<String, ConnectorRes> connector : connectors.entrySet()) {

                Response response =
                        getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI + "/" + category.getKey() + "/connectors/" +
                                connector.getKey());
                ValidatableResponse validatableResponse = response.then()
                        .log().ifValidationFails()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK);
                validatableResponse
                        .body("id", equalTo(connector.getKey()))
                        .body("name", equalTo(connector.getValue().getName()))
                        .body("category", equalTo(connector.getValue().getCategory()))
                        .body("friendlyName", equalTo(connector.getValue().getFriendlyName()))
                        .body("order", equalTo(connector.getValue().getOrder()))
                        .body("subCategory", equalTo(connector.getValue().getSubCategory()));
                Map<String, PropertyRes> properties =
                        connector.getValue().getProperties().stream()
                                .collect(Collectors.toMap(PropertyRes::getName, c -> c));
                for (Map.Entry<String, PropertyRes> property : properties.entrySet()) {
                    validatableResponse
                            .body("properties.find{it.name == '" + property.getValue().getName() + "' }.displayName",
                                    equalTo(property.getValue().getDisplayName()))
                            .body("properties.find{it.name == '" + property.getValue().getName() + "' }.description",
                                    equalTo(property.getValue().getDescription()));
                }
            }
        }
    }

    @Test
    public void testUpdateGovernanceConnector() throws IOException {

        String body = readResource("update-connector-property.json");
        Response response =
                getResponseOfPatch(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                                "/" + CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES + "/connectors/" +
                                CONNECTOR_LOCK_IDLE_ACCOUNTS,
                        body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Response getResponse =
                getResponseOfGet(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                        "/" + CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES + "/connectors/" + CONNECTOR_LOCK_IDLE_ACCOUNTS);

        getResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.find{it.name == '" +
                                "suspension.notification.account.disable.delay" + "' }.value",
                        equalTo("91"));
    }

    @Test
    public void testRestGovernanceConnector() throws IOException {

        String body = readResource("reset-connector-property.json");
        Response response =
                getResponseOfPatch(IDENTITY_GOVERNANCE_ENDPOINT_URI +
                                "/" + CATEGORY_ACCOUNT_MANAGEMENT_PROPERTIES + "/connectors/" +
                                CONNECTOR_LOCK_IDLE_ACCOUNTS,
                        body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}
