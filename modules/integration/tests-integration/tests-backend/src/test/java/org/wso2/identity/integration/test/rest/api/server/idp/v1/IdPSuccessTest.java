/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.idp.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Claims;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.JustInTimeProvisioning;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.MetaFederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.MetaFederatedAuthenticatorListItem;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.MetaOutboundConnector;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.MetaOutboundConnectorListItem;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Roles;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test class for Identity Provider Management REST APIs success paths.
 */
public class IdPSuccessTest extends IdPTestBase {

    private static final String SUPER_TENANT = "carbon.super";

    private String idPId;
    private Claims claimsResponse;
    private Roles rolesResponse;
    private JustInTimeProvisioning justInTimeProvisioningResponse;
    private List<MetaFederatedAuthenticatorListItem> metaFederatedAuthenticatorListResponse;
    private MetaFederatedAuthenticator metaFederatedAuthenticatorResponse;
    private List<MetaOutboundConnectorListItem> metaOutboundConnectorListResponse;
    private MetaOutboundConnector metaOutboundConnectorResponse;


    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdPSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        String expectedResponse;
        ObjectMapper jsonWriter;

        if (SUPER_TENANT.equals(tenant)) {
            expectedResponse = readResource("list-meta-federated-authenticators-response.json");
            jsonWriter = new ObjectMapper(new JsonFactory());
            metaFederatedAuthenticatorListResponse = Arrays.asList(jsonWriter.readValue(expectedResponse,
                    MetaFederatedAuthenticatorListItem[].class));
        } else {
            expectedResponse = readResource("list-meta-federated-authenticators-tenant-response.json");
            jsonWriter = new ObjectMapper(new JsonFactory());
            metaFederatedAuthenticatorListResponse = Arrays.asList(jsonWriter.readValue(expectedResponse,
                    MetaFederatedAuthenticatorListItem[].class));
        }

        expectedResponse = readResource("get-meta-federated-authenticator-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        metaFederatedAuthenticatorResponse = jsonWriter.readValue(expectedResponse, MetaFederatedAuthenticator.class);

        if (SUPER_TENANT.equals(tenant)) {
            expectedResponse = readResource("list-meta-outbound-connectors-response.json");
            jsonWriter = new ObjectMapper(new JsonFactory());
            metaOutboundConnectorListResponse = Arrays.asList(jsonWriter.readValue(expectedResponse,
                    MetaOutboundConnectorListItem[].class));
        } else {
            expectedResponse = readResource("list-meta-outbound-connectors-tenant-response.json");
            jsonWriter = new ObjectMapper(new JsonFactory());
            metaOutboundConnectorListResponse = Arrays.asList(jsonWriter.readValue(expectedResponse,
                    MetaOutboundConnectorListItem[].class));
        }

        expectedResponse = readResource("get-meta-outbound-connector-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        metaOutboundConnectorResponse = jsonWriter.readValue(expectedResponse, MetaOutboundConnector.class);

        expectedResponse = readResource("get-claims-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        claimsResponse = jsonWriter.readValue(expectedResponse, Claims.class);

        expectedResponse = readResource("get-roles-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        rolesResponse = jsonWriter.readValue(expectedResponse, Roles.class);

        expectedResponse = readResource("get-jit-response.json");
        jsonWriter = new ObjectMapper(new JsonFactory());
        justInTimeProvisioningResponse = jsonWriter.readValue(expectedResponse, JustInTimeProvisioning.class);
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
    public void testListMetaFederatedAuthenticators() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_FEDERATED_AUTHENTICATORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<MetaFederatedAuthenticatorListItem> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), MetaFederatedAuthenticatorListItem[].class));
        Assert.assertEquals(responseFound, metaFederatedAuthenticatorListResponse,
                "Response of the get all meta information of federated authenticators doesn't match.");
    }

    @Test(dependsOnMethods = {"testListMetaFederatedAuthenticators"})
    public void testGetMetaFederatedAuthenticator() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_FEDERATED_AUTHENTICATORS_PATH
                + PATH_SEPARATOR + SAMPLE_FEDERATED_AUTHENTICATOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        MetaFederatedAuthenticator responseFound = jsonWriter.readValue(response.asString(),
                MetaFederatedAuthenticator.class);
        Assert.assertEquals(responseFound, metaFederatedAuthenticatorResponse,
                "Response of the get meta federated authenticator doesn't match.");
    }

    @Test(dependsOnMethods = {"testGetMetaFederatedAuthenticator"})
    public void testListMetaOutboundConnectors() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_OUTBOUND_CONNECTORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<MetaOutboundConnectorListItem> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), MetaOutboundConnectorListItem[].class));
        Assert.assertEquals(responseFound, metaOutboundConnectorListResponse,
                "Response of the get all meta information of outbound provisioning connectors doesn't match.");
    }

    @Test(dependsOnMethods = {"testListMetaOutboundConnectors"})
    public void testGetMetaOutboundConnector() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_OUTBOUND_CONNECTORS_PATH +
                PATH_SEPARATOR + SAMPLE_OUTBOUND_CONNECTOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        MetaOutboundConnector responseFound = jsonWriter.readValue(response.asString(),
                MetaOutboundConnector.class);
        Assert.assertEquals(responseFound, metaOutboundConnectorResponse,
                "Response of the get meta outbound provisioning connector doesn't match.");
    }

    @Test(dependsOnMethods = {"testGetMetaOutboundConnector"})
    public void testAddIdP() throws IOException {

        String body = readResource("add-idp.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        idPId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"testAddIdP"})
    public void testGetIdP() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(idPId))
                .body("name", equalTo("Google"))
                .body("description", equalTo("IDP for Google Federation"))
                .body("isEnabled", equalTo(true))
                .body("isPrimary", equalTo(false))
                .body("image", equalTo("google-logo-url"))
                .body("isFederationHub", equalTo(false))
                .body("homeRealmIdentifier", equalTo("localhost"))
                .body("alias", equalTo("https://localhost:9444/oauth2/token"));
    }

    @Test(dependsOnMethods = {"testGetIdP"})
    public void testGetIdPs() throws IOException {

        String baseIdentifier = "identityProviders.find{ it.id == '" + idPId + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "description", equalTo("IDP for Google Federation"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "image", equalTo("google-logo-url"));
    }

    @Test(dependsOnMethods = {"testGetIdPs"})
    public void testGetIdPFederatedAuthenticators() throws IOException {

        String baseIdentifier =
                "authenticators.find{ it.authenticatorId == '" + SAMPLE_FEDERATED_AUTHENTICATOR_ID + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_FEDERATED_AUTHENTICATORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("GoogleOIDCAuthenticator"))
                .body(baseIdentifier + "isEnabled", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetIdPFederatedAuthenticators"})
    public void testUpdateIdPFederatedAuthenticator() throws IOException {

        String body = readResource("update-idp-federated-authenticator.json");
        Response response = getResponseOfPut(
                IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH
                        + PATH_SEPARATOR + SAMPLE_FEDERATED_AUTHENTICATOR_ID, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPFederatedAuthenticator"})
    public void testGetIdPFederatedAuthenticator() throws IOException {

        String propertyKey1Identifier = "properties.find{ it.key == '" +
                "ClientId" + "' }.";
        String propertyKey2Identifier = "properties.find{ it.key == '" +
                "ClientSecret" + "' }.";
        String propertyKey3Identifier = "properties.find{ it.key == '" +
                "callbackUrl" + "' }.";
        String propertyKey4Identifier = "properties.find{ it.key == '" +
                "AdditionalQueryParameters" + "' }.";

        Response response = getResponseOfGet(
                IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH +
                        PATH_SEPARATOR + SAMPLE_FEDERATED_AUTHENTICATOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("GoogleOIDCAuthenticator"))
                .body("isEnabled", equalTo(true))
                .body("isDefault", equalTo(true))
                .body("properties", notNullValue())
                .body(propertyKey1Identifier + "value", equalTo("165474950684-7mvqd8m6hieb8mdnffcarnku2aua0tpl.apps" +
                        ".googleusercontent.com"))
                .body(propertyKey2Identifier + "value", equalTo("testclientsecret"))
                .body(propertyKey3Identifier + "value", equalTo("https://mydomain1.com:9443/commonauth"))
                .body(propertyKey4Identifier + "value", equalTo("scope=openid email profile"));
    }

    @Test(dependsOnMethods = {"testGetIdPFederatedAuthenticator"})
    public void testGetIdPOutboundConnectors() throws IOException {

        String baseIdentifier = "connectors.find{ it.connectorId == '" + SAMPLE_OUTBOUND_CONNECTOR_ID + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_OUTBOUND_CONNECTORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("scim"))
                .body(baseIdentifier + "isEnabled", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetIdPOutboundConnectors"})
    public void testUpdateIdPOutboundConnector() throws IOException {

        String body = readResource("update-idp-outbound-connector.json");
        Response response = getResponseOfPut(
                IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_PROVISIONING_PATH + PATH_SEPARATOR
                        + IDP_OUTBOUND_CONNECTORS_PATH + PATH_SEPARATOR + SAMPLE_OUTBOUND_CONNECTOR_ID, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPOutboundConnector"})
    public void testGetIdPOutboundConnector() throws IOException {

        String propertyKey1Identifier = "properties.find{ it.key == '" +
                "scim-enable-pwd-provisioning" + "' }.";
        String propertyKey2Identifier = "properties.find{ it.key == '" +
                "scim-password" + "' }.";
        String propertyKey3Identifier = "properties.find{ it.key == '" +
                "scim-user-ep" + "' }.";
        String propertyKey4Identifier = "properties.find{ it.key == '" +
                "scim-username" + "' }.";

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_OUTBOUND_CONNECTORS_PATH + PATH_SEPARATOR +
                SAMPLE_OUTBOUND_CONNECTOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo("scim"))
                .body("isEnabled", equalTo(true))
                .body("isDefault", equalTo(true))
                .body("blockingEnabled", equalTo(false))
                .body("rulesEnabled", equalTo(false))
                .body("properties", notNullValue())
                .body(propertyKey1Identifier + "value", equalTo("true"))
                .body(propertyKey2Identifier + "value", equalTo("admin"))
                .body(propertyKey3Identifier + "value", equalTo("https://localhost:9445/userinfo"))
                .body(propertyKey4Identifier + "value", equalTo("admin"));
    }

    @Test(dependsOnMethods = {"testGetIdPOutboundConnector"})
    public void testUpdateIdPClaims() throws IOException {

        String body = readResource("update-idp-claims.json");
        Response response = getResponseOfPut(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_CLAIMS_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPClaims"})
    public void testGetIdPClaims() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_CLAIMS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        Claims responseFound =
                jsonWriter.readValue(response.asString(), Claims.class);
        Assert.assertEquals(responseFound, claimsResponse,
                "Response of get IDP claims doesn't match.");
    }

    @Test(dependsOnMethods = {"testGetIdPClaims"})
    public void testUpdateIdPRoles() throws IOException {

        String body = readResource("update-idp-roles.json");
        Response response = getResponseOfPut(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_ROLES_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPRoles"})
    public void testGetIdPRoles() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_ROLES_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        Roles responseFound = jsonWriter.readValue(response.asString(), Roles.class);
        Assert.assertEquals(responseFound, rolesResponse,
                "Response of the get IDP roles doesn't match.");
    }

    @Test(dependsOnMethods = {"testGetIdPRoles"})
    public void testUpdateIdPJIT() throws IOException {

        String body = readResource("update-idp-jit.json");
        Response response =
                getResponseOfPut(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR + IDP_PROVISIONING_PATH
                        + PATH_SEPARATOR + IDP_JIT_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPJIT"})
    public void testGetIdPJIT() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_JIT_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        JustInTimeProvisioning responseFound =
                jsonWriter.readValue(response.asString(), JustInTimeProvisioning.class);
        Assert.assertEquals(responseFound, justInTimeProvisioningResponse,
                "Response of the get IDP Just-In-Time provisioning config doesn't match.");
    }

    @Test(dependsOnMethods = {"testGetIdPJIT"})
    public void testPatchIdP() throws IOException {

        String body = readResource("patch-idp-home-realm.json");
        Response response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("homeRealmIdentifier", equalTo("google"));
    }

    @Test(dependsOnMethods = {"testPatchIdP"})
    public void testDeleteIdP() {

        getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
