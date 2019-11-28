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
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Roles;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Identity Provider Management REST APIs success paths.
 */
public class IdPSuccessTest extends IdPTestBase {

    private String idPId;
    private Claims claimsResponse;
    private Roles rolesResponse;
    private JustInTimeProvisioning justInTimeProvisioningResponse;

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
    public void testListMetaFederatedAuthenticators() throws Exception {

        String key1Identifier = "find{ it.authenticatorId == 'T2ZmaWNlMzY1QXV0aGVudGljYXRvcg' }.";
        String key2Identifier = "find{ it.authenticatorId == 'VHdpdHRlckF1dGhlbnRpY2F0b3I' }.";
        String key3Identifier = "find{ it.authenticatorId == 'RmFjZWJvb2tBdXRoZW50aWNhdG9y' }.";

        String key4Identifier = "find{ it.authenticatorId == 'R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I' }.";
        String key5Identifier = "find{ it.authenticatorId == 'TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y' }.";
        String key6Identifier = "find{ it.authenticatorId == 'UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I' }.";
        String key7Identifier = "find{ it.authenticatorId == 'WWFob29PQXV0aDJBdXRoZW50aWNhdG9y' }.";
        String key8Identifier = "find{ it.authenticatorId == 'SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y' }.";
        String key9Identifier = "find{ it.authenticatorId == 'U0FNTFNTT0F1dGhlbnRpY2F0b3I' }.";
        String key10Identifier = "find{ it.authenticatorId == 'T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I' }.";
        String key11Identifier = "find{ it.authenticatorId == 'RW1haWxPVFA' }.";
        String key12Identifier = "find{ it.authenticatorId == 'U01TT1RQ' }.";

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_FEDERATED_AUTHENTICATORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(key1Identifier + "name", equalTo("Office365Authenticator"))
                .body(key1Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/T2ZmaWNlMzY1QXV0aGVudGljYXRvcg"))
                .body(key2Identifier + "name", equalTo("TwitterAuthenticator"))
                .body(key2Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/VHdpdHRlckF1dGhlbnRpY2F0b3I"))
                .body(key3Identifier + "name", equalTo("FacebookAuthenticator"))
                .body(key3Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/RmFjZWJvb2tBdXRoZW50aWNhdG9y"))
                .body(key4Identifier + "name", equalTo("GoogleOIDCAuthenticator"))
                .body(key4Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I"))
                .body(key5Identifier + "name", equalTo("MicrosoftWindowsLiveAuthenticator"))
                .body(key5Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y"))
                .body(key6Identifier + "name", equalTo("PassiveSTSAuthenticator"))
                .body(key6Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I"))
                .body(key7Identifier + "name", equalTo("YahooOAuth2Authenticator"))
                .body(key7Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/WWFob29PQXV0aDJBdXRoZW50aWNhdG9y"))
                .body(key8Identifier + "name", equalTo("IWAKerberosAuthenticator"))
                .body(key8Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y"))
                .body(key9Identifier + "name", equalTo("SAMLSSOAuthenticator"))
                .body(key9Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/U0FNTFNTT0F1dGhlbnRpY2F0b3I"))
                .body(key10Identifier + "name", equalTo("OpenIDConnectAuthenticator"))
                .body(key10Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I"))
                .body(key11Identifier + "name", equalTo("EmailOTP"))
                .body(key11Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/RW1haWxPVFA"))
                .body(key12Identifier + "name", equalTo("SMSOTP"))
                .body(key12Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/U01TT1RQ"));
    }

    @Test(dependsOnMethods = {"testListMetaFederatedAuthenticators"})
    public void testGetMetaFederatedAuthenticator() throws IOException {

        String key1Identifier = "properties.find{ it.key == 'AdditionalQueryParameters' }.";
        String key2Identifier = "properties.find{ it.key == 'callbackUrl' }.";
        String key3Identifier = "properties.find{ it.key == 'ClientId' }.";
        String key4Identifier = "properties.find{ it.key == 'ClientSecret' }.";

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_FEDERATED_AUTHENTICATORS_PATH
                + PATH_SEPARATOR + SAMPLE_FEDERATED_AUTHENTICATOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("authenticatorId", equalTo(SAMPLE_FEDERATED_AUTHENTICATOR_ID))
                .body("name", equalTo("GoogleOIDCAuthenticator"))
                .body("displayName", equalTo("Google"))
                .body("properties", notNullValue())
                .body(key1Identifier + "displayName", equalTo("Additional Query Parameters"))
                .body(key1Identifier + "description", equalTo("Additional query parameters. e.g: paramName1=value1"))
                .body(key1Identifier + "type", equalTo("STRING"))
                .body(key1Identifier + "displayOrder", equalTo(4))
                .body(key1Identifier + "regex", equalTo(".*"))
                .body(key1Identifier + "isMandatory", equalTo(false))
                .body(key1Identifier + "isConfidential", equalTo(false))
                .body(key1Identifier + "defaultValue", equalTo(""))

                .body(key2Identifier + "displayName", equalTo("Callback Url"))
                .body(key2Identifier + "description", equalTo("Enter value corresponding to callback url."))
                .body(key2Identifier + "type", equalTo("STRING"))
                .body(key2Identifier + "displayOrder", equalTo(3))
                .body(key2Identifier + "regex", equalTo(".*"))
                .body(key2Identifier + "isMandatory", equalTo(false))
                .body(key2Identifier + "isConfidential", equalTo(false))
                .body(key2Identifier + "defaultValue", equalTo(""))

                .body(key3Identifier + "displayName", equalTo("Client Id"))
                .body(key3Identifier + "description", equalTo("Enter Google IDP client identifier value"))
                .body(key3Identifier + "type", equalTo("STRING"))
                .body(key3Identifier + "displayOrder", equalTo(1))
                .body(key3Identifier + "regex", equalTo(".*"))
                .body(key3Identifier + "isMandatory", equalTo(true))
                .body(key3Identifier + "isConfidential", equalTo(false))
                .body(key3Identifier + "defaultValue", equalTo(""))

                .body(key4Identifier + "displayName", equalTo("Client Secret"))
                .body(key4Identifier + "description", equalTo("Enter Google IDP client secret value"))
                .body(key4Identifier + "type", equalTo("STRING"))
                .body(key4Identifier + "displayOrder", equalTo(2))
                .body(key4Identifier + "regex", equalTo(".*"))
                .body(key4Identifier + "isMandatory", equalTo(true))
                .body(key4Identifier + "isConfidential", equalTo(true))
                .body(key4Identifier + "defaultValue", equalTo(""));
    }

    @Test(dependsOnMethods = {"testGetMetaFederatedAuthenticator"})
    public void testListMetaOutboundConnectors() throws Exception {

        String key1Identifier = "find{ it.connectorId == 'Z29vZ2xlYXBwcw' }.";
        String key2Identifier = "find{ it.connectorId == 'c2FsZXNmb3JjZQ' }.";
        String key3Identifier = "find{ it.connectorId == 'c2NpbQ' }.";
        String key4Identifier = "find{ it.connectorId == 'c3BtbA' }.";

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_OUTBOUND_CONNECTORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(key1Identifier + "name", equalTo("googleapps"))
                .body(key1Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/Z29vZ2xlYXBwcw"))
                .body(key2Identifier + "name", equalTo("salesforce"))
                .body(key2Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/c2FsZXNmb3JjZQ"))
                .body(key3Identifier + "name", equalTo("scim"))
                .body(key3Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/c2NpbQ"))
                .body(key4Identifier + "name", equalTo("spml"))
                .body(key4Identifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/c3BtbA"));
    }

    @Test(dependsOnMethods = {"testListMetaOutboundConnectors"})
    public void testGetMetaOutboundConnector() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_OUTBOUND_CONNECTORS_PATH +
                PATH_SEPARATOR + SAMPLE_OUTBOUND_CONNECTOR_ID);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("connectorId", equalTo(SAMPLE_OUTBOUND_CONNECTOR_ID))
                .body("name", equalTo("scim"))
                .body("displayName", equalTo("scim"))
                .body("blockingEnabled", equalTo(false))
                .body("rulesEnabled", equalTo(false));
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
        assertNotNull(location);
        idPId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(idPId);
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
    public void testGetIdPs() throws Exception {

        String baseIdentifier = "identityProviders.find{ it.id == '" + idPId + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "description", equalTo("IDP for Google Federation"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "image", equalTo("google-logo-url"))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/" + idPId));
    }

    @Test(dependsOnMethods = {"testGetIdPs"})
    public void testGetIdPFederatedAuthenticators() throws Exception {

        String baseIdentifier =
                "authenticators.find{ it.authenticatorId == '" + SAMPLE_FEDERATED_AUTHENTICATOR_ID + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_FEDERATED_AUTHENTICATORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("GoogleOIDCAuthenticator"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/" + idPId + "/federated-authenticators/" +
                        SAMPLE_FEDERATED_AUTHENTICATOR_ID));
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
    public void testGetIdPOutboundConnectors() throws Exception {

        String baseIdentifier = "connectors.find{ it.connectorId == '" + SAMPLE_OUTBOUND_CONNECTOR_ID + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_PROVISIONING_PATH + PATH_SEPARATOR + IDP_OUTBOUND_CONNECTORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("scim"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/" + idPId + "/provisioning/outbound-connectors/" +
                        SAMPLE_OUTBOUND_CONNECTOR_ID));
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
                .statusCode(HttpStatus.SC_OK)
                .body("userIdClaim.uri", equalTo("country"))
                .body("roleClaim.uri", equalTo("role"));
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
                .statusCode(HttpStatus.SC_OK)
                .body("mappings[0].idpRole", equalTo("google-admin"));
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
                .statusCode(HttpStatus.SC_OK)
                .body("isEnabled", equalTo(false));
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
