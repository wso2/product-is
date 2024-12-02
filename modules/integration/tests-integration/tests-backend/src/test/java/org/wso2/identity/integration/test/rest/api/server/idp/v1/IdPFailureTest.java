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

package org.wso2.identity.integration.test.rest.api.server.idp.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.util.UserDefinedAuthenticatorPayload;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Identity Provider Management REST APIs failure paths.
 */
public class IdPFailureTest extends IdPTestBase {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String FEDERATED_AUTHENTICATOR_ID_PLACEHOLDER = "<FEDERATED_AUTHENTICATOR_ID>";
    private static final String FEDERATED_AUTHENTICATOR_PLACEHOLDER = "\"<FEDERATED_AUTHENTICATOR>\"";
    private static final String FEDERATED_AUTHENTICATOR_PLACEHOLDER_1 = "\"<FEDERATED_AUTHENTICATOR_1>\"";
    private static final String FEDERATED_AUTHENTICATOR_PLACEHOLDER_2 = "\"<FEDERATED_AUTHENTICATOR_2>\"";
    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";
    private static final String CUSTOM_IDP_NAME = "CustomAuthIDP";
    private static final String USER_DEFINED_AUTHENTICATOR_ID_1 = "Y3VzdG9tQXV0aGVudGljYXRvcjE=";
    private static final String USER_DEFINED_AUTHENTICATOR_ID_2 = "Y3VzdG9tQXV0aGVudGljYXRvcg==";
    private static final String SYSTEM_DEFINED_AUTHENTICATOR_ID = "R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I";
    private static final String ENDPOINT_URI = "https://abc.com/authenticate";
    private String idPId;
    private String idpCreatePayload;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdPFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        idpCreatePayload = readResource("add-idp-with-custom-fed-auth.json");
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

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetIdPWithInvalidId() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60002", "random-id");
    }

    @Test(dependsOnMethods = {"testGetIdPWithInvalidId"})
    public void addIdPConflict() throws IOException {

        Response response = getResponseOfPost(IDP_API_BASE_PATH, readResource("add-idp2.json"));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        idPId = location.substring(location.lastIndexOf("/") + 1);

        response = getResponseOfPost(IDP_API_BASE_PATH, readResource("add-idp-conflict.json"));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "IDP-60001", "Google-2");
    }

    @Test()
    public void addIdPWithDuplicateProperties() throws IOException {

        Response response = getResponseOfPost(IDP_API_BASE_PATH, readResource("add-idp-duplicate-properties.json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "IDP-60025");
    }


    @Test(dependsOnMethods = {"addIdPConflict"})
    public void testGetIdPFederatedAuthenticatorWithInvalidAuthId() {

        Response response = getResponseOfGet(
                IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH +
                        PATH_SEPARATOR + "random-fed-auth-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60022", "random-fed-auth-id");
    }

    @Test(dependsOnMethods = {"testGetIdPFederatedAuthenticatorWithInvalidAuthId"})
    public void testGetIdPOutboundConnectorWithInvalidConnectorId() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_OUTBOUND_CONNECTORS_PATH + PATH_SEPARATOR +
                "random-connector-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-60023", "random-connector-id");
    }

    @Test
    public void testGetIdPTemplateWithInvalidId() throws Exception {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH +
                PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "TMM_00021", "random-id");
    }

    @Test(dependsOnMethods = {"testGetIdPTemplateWithInvalidId"})
    public void testAddIdPTemplateConflict() throws IOException {

        String body = readResource("add-idp-template2.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        response = getResponseOfPost(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH,
                readResource("add-idp-template-conflict.json"));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, "TMM_00014", "Google-2");
    }

    @Test(dependsOnMethods = {"testAddIdPTemplateConflict"})
    public void testFilterIdPTemplatesWithInvalidSearchKey() throws Exception {

        String url = IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH;
        Map<String, Object> filterParam = new HashMap<>();
        filterParam.put("filter", "test eq 'DEFAULT'");
        Response response = getResponseOfGetWithQueryParams(url, filterParam);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "IDP-65055", "Invalid search filter");
    }

    @Test(dependsOnMethods = {"addIdPConflict"})
    public void testPatchIdPNonExistentProperties() throws IOException {

        // Test patch REMOVE operation for non-existent JWKS URI property.
        String body = readResource("patch-remove-jwks-uri.json");
        Response response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "IDP-65005", "JWKS URI");
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWhenEndpointUriIsEmpty() throws IOException {

        UserDefinedAuthenticatorPayload userDefAuthPayload = createUserDefinedAuthenticatorPayload(
                USER_DEFINED_AUTHENTICATOR_ID_1,
                "",
                "testUser",
                "testPassword");
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, userDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Invalid Request"))
                .body("description", equalTo("must match \"^https?://.+\""));
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWhenEndpointUriIsInvalid() throws IOException {

        UserDefinedAuthenticatorPayload useDefAuthPayload = createUserDefinedAuthenticatorPayload(
                USER_DEFINED_AUTHENTICATOR_ID_1,
                "ftp://test.com",
                "testUser",
                "testPassword");
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, useDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Invalid Request"))
                .body("description", equalTo("must match \"^https?://.+\""));
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWhenEndpointConfigIsEmpty() throws IOException {

        UserDefinedAuthenticatorPayload userDefAuthPayload =
                createUserDefinedAuthenticatorPayloadWithEmptyEndpointConfig(USER_DEFINED_AUTHENTICATOR_ID_1);
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, userDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Endpoint configuration must be provided for the user defined " +
                        "federated authenticators " + new String(Base64.getDecoder().decode(
                        USER_DEFINED_AUTHENTICATOR_ID_1)) + "."));
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWhenAuthenticatorAuthDetailsIsEmpty() throws IOException {

        UserDefinedAuthenticatorPayload userDefAuthPayload =
                createUserDefinedAuthenticatorPayloadWithEmptyAuthenticationProperties(USER_DEFINED_AUTHENTICATOR_ID_1, ENDPOINT_URI);
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, userDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Invalid Request"))
                .body("description", equalTo("Property authentication cannot be null."));
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWhenAuthenticatorPasswordIsEmpty()
            throws JsonProcessingException {

        UserDefinedAuthenticatorPayload userDefAuthPayload = createInvalidUserDefinedAuthenticatorPayload(
                "USER", USER_DEFINED_AUTHENTICATOR_ID_1, ENDPOINT_URI, USERNAME, "");
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, userDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("The property password must be provided as an authentication " +
                        "property for the BASIC authentication type."));
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWithExistingAuthenticatorName() throws IOException {

        UserDefinedAuthenticatorPayload useDefAuthPayload = createUserDefinedAuthenticatorPayload(
                USER_DEFINED_AUTHENTICATOR_ID_1, ENDPOINT_URI, "testUser", "testPassword");
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, useDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String customIdPId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(customIdPId);

        // duplicate the authenticator creation
        UserDefinedAuthenticatorPayload duplicateUseDefAuthPayload =
                createUserDefinedAuthenticatorPayload(USER_DEFINED_AUTHENTICATOR_ID_1,
                        "https://xyz.com/authenticate",
                        "testUser1",
                        "testPassword1");
        Response responseOfDuplicate = createUserDefAuthenticator("CustomAuthIDP2",
                duplicateUseDefAuthPayload);
        responseOfDuplicate.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Federated authenticator name " +
                        new String(Base64.getDecoder().decode(USER_DEFINED_AUTHENTICATOR_ID_1)) + " is already taken."));

        deleteCreatedIdP(customIdPId);
    }

    @Test
    public void testAddIdPWithUserDefinedAuthenticatorWithMultipleAuthenticators() throws IOException {

        String idpCreateErrorPayload = readResource("add-idp-with-custom-fed-multi-auth.json");
        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload1 = createUserDefinedAuthenticatorPayload(
                USER_DEFINED_AUTHENTICATOR_ID_1,
                ENDPOINT_URI,
                "testUser",
                "testPassword");
        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload2 = createUserDefinedAuthenticatorPayload(
                USER_DEFINED_AUTHENTICATOR_ID_2,
                ENDPOINT_URI,
                "testUser",
                "testPassword");

        Response response = createMultiUserDefAuthenticators("CustomAuthIDPX", idpCreateErrorPayload,
                userDefinedAuthenticatorPayload1, userDefinedAuthenticatorPayload2);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Multiple authenticators found."));
    }

    @Test
    public void testAddUserDeAuthenticatorWithSystemProperty() throws JsonProcessingException {

        UserDefinedAuthenticatorPayload useDefAuthPayload = createInvalidUserDefinedAuthenticatorPayload(
                "SYSTEM", USER_DEFINED_AUTHENTICATOR_ID_1, ENDPOINT_URI, USERNAME, PASSWORD);
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, useDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("No endpoint configuration must be provided for the system defined " +
                        "federated authenticators " +
                        new String(Base64.getDecoder().decode(USER_DEFINED_AUTHENTICATOR_ID_1)) + "."));
    }

    @Test
    public void testAddUserDefAuthenticatorWithExistingSystemDefAuthenticatorName() throws JsonProcessingException {

        UserDefinedAuthenticatorPayload useDefAuthPayload = createUserDefinedAuthenticatorPayload(
                SYSTEM_DEFINED_AUTHENTICATOR_ID, ENDPOINT_URI, "testUser", "testPassword");
        Response response = createUserDefAuthenticator(CUSTOM_IDP_NAME, useDefAuthPayload);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Federated authenticator name " + new String(Base64.getDecoder().
                        decode(SYSTEM_DEFINED_AUTHENTICATOR_ID)) + " is already taken."));
    }

    /**
     * Create multiple user-defined authenticators for an IdP and sends a POST request to the IDP API.
     *
     * @param idpName Name of the identity provider.
     * @param idpCreatePayload Base payload template for the identity provider.
     * @param userDefinedAuthenticatorPayload1 First authenticator payload.
     * @param userDefinedAuthenticatorPayload2 Second authenticator payload.
     * @return Response received from the API call.
     * @throws JsonProcessingException If there's an error while processing the JSON.
     */
    private Response createMultiUserDefAuthenticators(String idpName, String idpCreatePayload,
                                                      UserDefinedAuthenticatorPayload
                                                              userDefinedAuthenticatorPayload1,
                                                      UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload2)
            throws JsonProcessingException {

        String body = idpCreatePayload.replace(FEDERATED_AUTHENTICATOR_ID_PLACEHOLDER,
                userDefinedAuthenticatorPayload1.getAuthenticatorId());
        body = body.replace(FEDERATED_AUTHENTICATOR_PLACEHOLDER_1,
                userDefinedAuthenticatorPayload1.convertToJasonPayload());
        body = body.replace(FEDERATED_AUTHENTICATOR_PLACEHOLDER_2,
                userDefinedAuthenticatorPayload2.convertToJasonPayload());
        body = body.replace(IDP_NAME_PLACEHOLDER, idpName);
        return getResponseOfPost(IDP_API_BASE_PATH, body);
    }

    /**
     * Create a user-defined authenticator payload with provided details.
     *
     * @param id Authenticator ID.
     * @param endpoint_uri Endpoint URI for the authenticator.
     * @param username Username for basic authentication.
     * @param password Password for basic authentication.
     * @return A user-defined authenticator payload.
     */
    private UserDefinedAuthenticatorPayload createUserDefinedAuthenticatorPayload(String id, String endpoint_uri,
                                                                                  String username, String password) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(id);
        userDefinedAuthenticatorPayload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpoint_uri);
        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> properties = new HashMap<>();
        properties.put(USERNAME, username);
        properties.put(PASSWORD, password);
        authenticationType.setProperties(properties);
        endpoint.authentication(authenticationType);
        userDefinedAuthenticatorPayload.setEndpoint(endpoint);

        return userDefinedAuthenticatorPayload;
    }

    /**
     * Creates an invalid user-defined authenticator payload.
     * This method enables the creation of an invalid authenticator payload, either by defining the definedBy property
     * as SYSTEM or by leaving the password field empty.
     *
     * @param definedBy    Entity that defines the authenticator, either "SYSTEM" or "USER".
     * @param id           IDof the authenticator.
     * @param endpoint_uri URI of the endpoint.
     * @param username     Username for basic authentication.
     * @param password     Password for basic authentication. If empty, no password will be set.
     * @return A {@link UserDefinedAuthenticatorPayload} containing the invalid authenticator setup.
     */
    private UserDefinedAuthenticatorPayload createInvalidUserDefinedAuthenticatorPayload(
            String definedBy, String id, String endpoint_uri, String username, String password) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(id);
        switch (definedBy) {
            case "SYSTEM":
                userDefinedAuthenticatorPayload.setDefinedBy(
                        FederatedAuthenticatorRequest.DefinedByEnum.SYSTEM.toString());
                break;
            case "USER":
                userDefinedAuthenticatorPayload.setDefinedBy(
                        FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());
                break;
        }

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpoint_uri);
        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> properties = new HashMap<>();
        properties.put(USERNAME, username);
        if (!password.isEmpty()) {
            properties.put(PASSWORD, password);
        }
        authenticationType.setProperties(properties);
        endpoint.authentication(authenticationType);
        userDefinedAuthenticatorPayload.setEndpoint(endpoint);

        return userDefinedAuthenticatorPayload;
    }

    /**
     * Create a user-defined authenticator payload with an empty endpoint configuration.
     *
     * @param id Authenticator ID.
     * @return A user-defined authenticator payload with no endpoint configuration.
     */
    private UserDefinedAuthenticatorPayload createUserDefinedAuthenticatorPayloadWithEmptyEndpointConfig(String id) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(id);
        userDefinedAuthenticatorPayload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        return userDefinedAuthenticatorPayload;
    }

    /**
     * Create a user-defined authenticator payload with an endpoint URI with empty authentication properties.
     *
     * @param id Authenticator ID.
     * @param endpoint_uri The endpoint URI for the authenticator.
     * @return A user-defined authenticator payload with endpoint URI but without authentication details.
     */
    private UserDefinedAuthenticatorPayload createUserDefinedAuthenticatorPayloadWithEmptyAuthenticationProperties(
            String id, String endpoint_uri) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(id);
        userDefinedAuthenticatorPayload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpoint_uri);
        userDefinedAuthenticatorPayload.setEndpoint(endpoint);

        return userDefinedAuthenticatorPayload;
    }

    /**
     * Create a user-defined authenticator and sends a POST request to the IDP API.
     *
     * @param idpName                         Name of the identity provider.
     * @param userDefinedAuthenticatorPayload Payload containing authenticator details.
     * @return Response received from the API call.
     * @throws JsonProcessingException If there's an error while processing the JSON.
     */
    private Response createUserDefAuthenticator(String idpName, UserDefinedAuthenticatorPayload
            userDefinedAuthenticatorPayload) throws JsonProcessingException {

        String body = idpCreatePayload.replace(FEDERATED_AUTHENTICATOR_ID_PLACEHOLDER,
                userDefinedAuthenticatorPayload.getAuthenticatorId());
        body = body.replace(FEDERATED_AUTHENTICATOR_PLACEHOLDER,
                userDefinedAuthenticatorPayload.convertToJasonPayload());
        body = body.replace(IDP_NAME_PLACEHOLDER, idpName);
        return getResponseOfPostNoFilter(IDP_API_BASE_PATH, body);
    }

    /**
     * Deletes an Identity Provider by its ID and verifies the deletion.
     *
     * @param idPId ID of the Identity Provider to be deleted.
     */
    private void deleteCreatedIdP(String idPId) {

        Response response = getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Resource not found."))
                .body("description", equalTo("Unable to find a resource matching the provided identity " +
                        "provider identifier " + idPId + "."));
    }
}
