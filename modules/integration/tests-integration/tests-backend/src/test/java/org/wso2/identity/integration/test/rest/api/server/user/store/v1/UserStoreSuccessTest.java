/*
 * Copyright (c) 2019-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.user.store.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.AvailableUserStoreClassesRes;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreConfigurationsRes;
import org.wso2.identity.integration.test.rest.api.server.user.store.v1.model.UserStoreListResponse;
import org.wso2.identity.integration.test.restclients.UserStoreMgtRestClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class UserStoreSuccessTest extends UserStoreTestBase {

    private static String domainId;
    private UserStoreMgtRestClient userStoreMgtRestClient;
    private static final String USER_STORE_TYPE_ID = "VW5pcXVlSURKREJDVXNlclN0b3JlTWFuYWdlcg";
    private static final String USER_STORE_DOMAIN = "JDBC-3";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserStoreSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        userStoreMgtRestClient = new UserStoreMgtRestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws IOException {

        super.conclude();
        userStoreMgtRestClient.closeHttpClient();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testAddSecondaryUserStore() throws Exception {

        String body = readResource("user-store-add-secondary-user-store.json");
        Response response = getResponseOfPost(USER_STORE_PATH_COMPONENT, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        userStoreMgtRestClient.waitForUserStoreDeployment(USER_STORE_DOMAIN);
        String location = response.getHeader(HttpHeaders.LOCATION);
        domainId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testAddSecondaryUserStore"})
    public void testCheckRDBMSConnection() throws IOException {

        String body = readResource("user-store-test-connection.json");
        checkRDBMSConnection(body);
    }

    @Test(dependsOnMethods = {"testAddSecondaryUserStore"})
    public void testCheckRDBMSConnectionWithMaskedPassword() throws IOException {

        String body = readResource("user-store-test-connection-with-masked-password.json");
        checkRDBMSConnection(body);
    }

    @Test(dependsOnMethods = {"testAddSecondaryUserStore"})
    public void testGetAvailableUserStoreClasses() throws IOException {

        Map<String, AvailableUserStoreClassesRes> userStoreClassesResMap;
        String expectedResponse = readResource("get-available-user-store-classes.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<AvailableUserStoreClassesRes> availableUserStoreClassesResList =
                Arrays.asList(jsonWriter.readValue(expectedResponse, AvailableUserStoreClassesRes[].class));
        userStoreClassesResMap = availableUserStoreClassesResList.stream().
                collect(Collectors.toMap(AvailableUserStoreClassesRes::getTypeId, c -> c));
        Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT + USER_STORE_META_COMPONENT);
        ValidatableResponse validatableResponse = response.then().log().ifValidationFails().assertThat()
                .statusCode(HttpStatus.SC_OK);
        for (Map.Entry<String, AvailableUserStoreClassesRes> resEntry : userStoreClassesResMap.entrySet()) {
            validatableResponse.body("find{ it.typeId == '" + resEntry.getKey() + "' }.typeName",
                    equalTo(resEntry.getValue().getTypeName()));
            validatableResponse.body("find{ it.typeId == '" + resEntry.getKey() + "' }.className",
                    equalTo(resEntry.getValue().getClassName()));
            validatableResponse.body("find{ it.typeId == '" + resEntry.getKey() + "' }.self",
                    equalTo(getTenantedRelativePath("/api/server/v1" + USER_STORE_PATH_COMPONENT +
                            PATH_SEPARATOR + "meta/types/" + resEntry.getValue().getTypeId(), tenant)));
        }
    }

    @Test(dependsOnMethods = {"testGetAvailableUserStoreClasses"})
    public void testGetSecondaryUserStoreByDomainId() throws Exception {

            String expectedResponse = readResource("get-user-store_by_domain_id-response.json");
            ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
            List<UserStoreConfigurationsRes> availableUserStoreList =
                    Collections.singletonList(jsonWriter.readValue(expectedResponse, UserStoreConfigurationsRes.class));
            Map<String, UserStoreConfigurationsRes> userStoreMetaResMap = availableUserStoreList.stream().
                    collect(Collectors.toMap(UserStoreConfigurationsRes::getTypeId, c -> c));
            Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR + domainId);
            ValidatableResponse validatableResponse = response.then().log().ifValidationFails().assertThat().
                    statusCode(HttpStatus.SC_OK);
            for (Map.Entry<String, UserStoreConfigurationsRes> resEntry : userStoreMetaResMap.entrySet()) {
                validatableResponse
                        .body("typeName", equalTo(resEntry.getValue().getTypeName()))
                        .body("className", equalTo(resEntry.getValue().getClassName()))
                        .body("name", equalTo(resEntry.getValue().getName()))
                        .body("description", equalTo(resEntry.getValue().getDescription()));
            }
    }

    @Test(dependsOnMethods = {"testGetSecondaryUserStoreByDomainId"})
    public void testGetUserStore() throws IOException {

        String expectedResponse = readResource("get-user-store-response.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<UserStoreListResponse> userStoreListResponseList =
                Arrays.asList(jsonWriter.readValue(expectedResponse, UserStoreListResponse[].class));
        Map<String, UserStoreListResponse> userStoreListResMap = userStoreListResponseList.stream().
                collect(Collectors.toMap(UserStoreListResponse::getId, c -> c));
        Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT);
        ValidatableResponse validatableResponse = response.then().log().ifValidationFails().assertThat().
                statusCode(HttpStatus.SC_OK);
        for (Map.Entry<String, UserStoreListResponse> resEntry : userStoreListResMap.entrySet()) {
            validatableResponse.body("find{ it.id == '" + resEntry.getKey() + "' }.name",
                    equalTo(resEntry.getValue().getName()));
            validatableResponse.body("find{ it.id == '" + resEntry.getKey() + "' }.description",
                    equalTo(resEntry.getValue().getDescription()));
            validatableResponse.body("find{ it.id == '" + resEntry.getKey() + "' }.self",
                    equalTo(getTenantedRelativePath("/api/server/v1" +
                            USER_STORE_PATH_COMPONENT + PATH_SEPARATOR + resEntry.getValue().getId(), tenant)));
        }
    }

    @Test(dependsOnMethods = {"testGetUserStore"})
    public void testGetUserStoreTypeMeta() throws IOException {

        String expectedResponse = readResource("get-user-store_by_type_id_response.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        JsonNode jsonNode = jsonWriter.readTree(expectedResponse);
        Response response = getResponseOfGet(USER_STORE_PATH_COMPONENT + USER_STORE_META_COMPONENT
                + PATH_SEPARATOR + USER_STORE_TYPE_ID);
        ValidatableResponse validatableResponse = response.then().log().ifValidationFails().assertThat().
                statusCode(HttpStatus.SC_OK);
        validatableResponse
                .body("typeName", equalTo(jsonNode.get("typeName").asText()))
                .body("typeId", equalTo(jsonNode.get("typeId").asText()))
                .body("className", equalTo(jsonNode.get("className").asText()))
                .body("properties", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetUserStoreTypeMeta"})
    public void testUpdateUserStoreByDomainId() throws IOException {

        String body = readResource("update-secondary-user-store.json");
        getResponseOfPut(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR + domainId, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK).body("description",
                equalTo("Sample request to update the description of user store"));
    }

    @Test(dependsOnMethods = {"testUpdateUserStoreByDomainId"})
    public void testPatchUserStoreByDomainId() throws IOException {

        String body = readResource("patch-secondary-user-store.json");
        getResponseOfPatch(USER_STORE_PATH_COMPONENT + PATH_SEPARATOR + domainId, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testPatchUserStoreByDomainId"})
    public void testDeleteUserStore() throws Exception {

        String path = USER_STORE_PATH_COMPONENT + PATH_SEPARATOR + domainId;
        getResponseOfDelete(path)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        log.info("deleting the user store with domain id: " + domainId.toString());
        if (userStoreMgtRestClient.waitForUserStoreUnDeployment(domainId)) {
            getResponseOfGet(path)
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND);
        }
    }

    private void checkRDBMSConnection(String body) {

        getResponseOfPost(USER_STORE_PATH_COMPONENT + USER_STORE_TEST_CONNECTION, body)
                .then().log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("connection", equalTo(true));
    }
}
