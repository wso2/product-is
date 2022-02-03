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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Identity Provider Management REST APIs success paths.
 */
public class IdPSuccessTest extends IdPTestBase {

    private String idPId;
    private String idPTemplateId;

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

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_FEDERATED_AUTHENTICATORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find{ it.authenticatorId == 'T2ZmaWNlMzY1QXV0aGVudGljYXRvcg' }.name", equalTo
                        ("Office365Authenticator"))
                .body("find{ it.authenticatorId == 'T2ZmaWNlMzY1QXV0aGVudGljYXRvcg' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/T2ZmaWNlMzY1QXV0aGVudGljYXRvcg"))
                .body("find{ it.authenticatorId == 'VHdpdHRlckF1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("TwitterAuthenticator"))
                .body("find{ it.authenticatorId == 'VHdpdHRlckF1dGhlbnRpY2F0b3I' }.self", equalTo("/t/" + context
                        .getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/VHdpdHRlckF1dGhlbnRpY2F0b3I"))
                .body("find{ it.authenticatorId == 'RmFjZWJvb2tBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("FacebookAuthenticator"))
                .body("find{ it.authenticatorId == 'RmFjZWJvb2tBdXRoZW50aWNhdG9y' }.self", equalTo("/t/" + context
                        .getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/RmFjZWJvb2tBdXRoZW50aWNhdG9y"))
                .body("find{ it.authenticatorId == 'R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("GoogleOIDCAuthenticator"))
                .body("find{ it.authenticatorId == 'R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I"))
                .body("find{ it.authenticatorId == 'TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("MicrosoftWindowsLiveAuthenticator"))
                .body("find{ it.authenticatorId == 'TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y' }.self", equalTo
                        ("/t/" + context.getContextTenant().getDomain() +
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y"))
                .body("find{ it.authenticatorId == 'UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("PassiveSTSAuthenticator"))
                .body("find{ it.authenticatorId == 'UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I"))
                .body("find{ it.authenticatorId == 'WWFob29PQXV0aDJBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("YahooOAuth2Authenticator"))
                .body("find{ it.authenticatorId == 'WWFob29PQXV0aDJBdXRoZW50aWNhdG9y' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/WWFob29PQXV0aDJBdXRoZW50aWNhdG9y"))
                .body("find{ it.authenticatorId == 'SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("IWAKerberosAuthenticator"))
                .body("find{ it.authenticatorId == 'SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y"))
                .body("find{ it.authenticatorId == 'U0FNTFNTT0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("SAMLSSOAuthenticator"))
                .body("find{ it.authenticatorId == 'U0FNTFNTT0F1dGhlbnRpY2F0b3I' }.self", equalTo("/t/" + context
                        .getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators/U0FNTFNTT0F1dGhlbnRpY2F0b3I"))
                .body("find{ it.authenticatorId == 'T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("OpenIDConnectAuthenticator"))
                .body("find{ it.authenticatorId == 'T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I' }.self", equalTo("/t/" +
                        context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators" +
                        "/T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I"))
                .body("find{ it.authenticatorId == 'RW1haWxPVFA' }.name", equalTo("EmailOTP"))
                .body("find{ it.authenticatorId == 'RW1haWxPVFA' }.self", equalTo("/t/" + context.getContextTenant
                        ().getDomain() +
                        "/api/server/v1/identity-providers/meta/federated-authenticators/RW1haWxPVFA"))
                .body("find{ it.authenticatorId == 'U01TT1RQ' }.name", equalTo("SMSOTP"))
                .body("find{ it.authenticatorId == 'U01TT1RQ' }.self", equalTo("/t/" + context.getContextTenant()
                        .getDomain() + "/api/server/v1/identity-providers/meta/federated-authenticators/U01TT1RQ"));
    }

    @Test(dependsOnMethods = {"testListMetaFederatedAuthenticators"})
    public void testGetMetaFederatedAuthenticator() throws IOException {

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
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.displayName", equalTo("Additional " +
                        "Query Parameters"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.description", equalTo("Additional " +
                        "query parameters. e.g: paramName1=value1"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.displayOrder", equalTo(4))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.isMandatory", equalTo(false))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'callbackUrl' }.displayName", equalTo("Callback Url"))
                .body("properties.find{ it.key == 'callbackUrl' }.description", equalTo("Enter value corresponding " +
                        "to callback url."))
                .body("properties.find{ it.key == 'callbackUrl' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'callbackUrl' }.displayOrder", equalTo(3))
                .body("properties.find{ it.key == 'callbackUrl' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'callbackUrl' }.isMandatory", equalTo(false))
                .body("properties.find{ it.key == 'callbackUrl' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'callbackUrl' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'ClientId' }.displayName", equalTo("Client Id"))
                .body("properties.find{ it.key == 'ClientId' }.description", equalTo("Enter Google IDP client " +
                        "identifier value"))
                .body("properties.find{ it.key == 'ClientId' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'ClientId' }.displayOrder", equalTo(1))
                .body("properties.find{ it.key == 'ClientId' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'ClientId' }.isMandatory", equalTo(true))
                .body("properties.find{ it.key == 'ClientId' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'ClientId' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'ClientSecret' }.displayName", equalTo("Client Secret"))
                .body("properties.find{ it.key == 'ClientSecret' }.description", equalTo("Enter Google IDP client " +
                        "secret value"))
                .body("properties.find{ it.key == 'ClientSecret' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'ClientSecret' }.displayOrder", equalTo(2))
                .body("properties.find{ it.key == 'ClientSecret' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'ClientSecret' }.isMandatory", equalTo(true))
                .body("properties.find{ it.key == 'ClientSecret' }.isConfidential", equalTo(true))
                .body("properties.find{ it.key == 'ClientSecret' }.defaultValue", equalTo(""));
    }

    @Test(dependsOnMethods = {"testGetMetaFederatedAuthenticator"})
    public void testListMetaOutboundConnectors() throws Exception {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + META_OUTBOUND_CONNECTORS_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find{ it.connectorId == 'Z29vZ2xlYXBwcw' }.name", equalTo("googleapps"))
                .body("find{ it.connectorId == 'Z29vZ2xlYXBwcw' }.self", equalTo("/t/" + context.getContextTenant()
                        .getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/Z29vZ2xlYXBwcw"))
                .body("find{ it.connectorId == 'c2FsZXNmb3JjZQ' }.name", equalTo("salesforce"))
                .body("find{ it.connectorId == 'c2FsZXNmb3JjZQ' }.self", equalTo("/t/" + context.getContextTenant()
                        .getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/c2FsZXNmb3JjZQ"))
                .body("find{ it.connectorId == 'c2NpbQ' }.name", equalTo("scim"))
                .body("find{ it.connectorId == 'c2NpbQ' }.self", equalTo("/t/" + context.getContextTenant()
                        .getDomain() +
                        "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                        "/c2NpbQ"));
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
    public void testGetIdPsWithRequiredAttribute() throws Exception {

        String baseIdentifier = "identityProviders.find{ it.id == '" + idPId + "' }.";
        Map<String, Object> requiredAttributeParam = new HashMap<>();
        requiredAttributeParam.put("requiredAttributes", "homeRealmIdentifier");
        Response response =
                getResponseOfGetWithQueryParams(IDP_API_BASE_PATH, requiredAttributeParam);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "description", equalTo("IDP for Google Federation"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "image", equalTo("google-logo-url"))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/" + idPId))
                .body(baseIdentifier + "homeRealmIdentifier", equalTo("localhost"));
    }

    @Test(dependsOnMethods = {"testGetIdPsWithRequiredAttribute"})
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
                .body("properties.find{ it.key == 'ClientId' }.value", equalTo
                        ("165474950684-7mvqd8m6hieb8mdnffcarnku2aua0tpl.apps.googleusercontent.com"))
                .body("properties.find{ it.key == 'ClientSecret' }.value", equalTo("testclientsecret"))
                .body("properties.find{ it.key == 'callbackUrl' }.value", equalTo
                        ("https://mydomain1.com:9443/commonauth"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.value", equalTo("scope=openid email" +
                        " profile"));
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
                .body("properties.find{ it.key == 'scim-enable-pwd-provisioning' }.value", equalTo("true"))
                .body("properties.find{ it.key == 'scim-password' }.value", equalTo("admin"))
                .body("properties.find{ it.key == 'scim-user-ep' }.value", equalTo("https://localhost:9445/userinfo"))
                .body("properties.find{ it.key == 'scim-username' }.value", equalTo("admin"));
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
                .body("userIdClaim", notNullValue())
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
                .statusCode(HttpStatus.SC_OK)
                .body("userIdClaim", notNullValue())
                .body("userIdClaim.uri", equalTo("country"))
                .body("roleClaim", notNullValue())
                .body("roleClaim.uri", equalTo("role"))
                .body("mappings", notNullValue())
                .body("mappings[0].idpClaim", equalTo("country"))
                .body("mappings[0].localClaim.id", equalTo("aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ"))
                .body("mappings[0].localClaim.uri", equalTo("http://wso2.org/claims/username"))
                .body("mappings[0].localClaim.displayName", equalTo("Username"))
                .body("mappings[1].idpClaim", equalTo("role"))
                .body("mappings[1].localClaim.id", equalTo("aHR0cDovL3dzbzIub3JnL2NsYWltcy9yb2xl"))
                .body("mappings[1].localClaim.uri", equalTo("http://wso2.org/claims/role"))
                .body("mappings[1].localClaim.displayName", equalTo("Role"))
                .body("provisioningClaims", notNullValue())
                .body("provisioningClaims[0].claim.uri", equalTo("country"))
                .body("provisioningClaims[0].defaultValue", equalTo("sathya"));
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
                .body("mappings", notNullValue())
                .body("mappings[0].idpRole", equalTo("google-admin"));
    }

    @Test(dependsOnMethods = {"testUpdateIdPRoles"})
    public void testGetIdPRoles() throws IOException {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                IDP_ROLES_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("mappings", notNullValue())
                .body("mappings[0].idpRole", equalTo("google-admin"))
                .body("mappings[0].localRole", equalTo("admin"))
                .body("outboundProvisioningRoles", notNullValue())
                .body("outboundProvisioningRoles[0]", equalTo("admin"));
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
                .statusCode(HttpStatus.SC_OK)
                .body("isEnabled", equalTo(false))
                .body("scheme", equalTo("PROVISION_SILENTLY"))
                .body("userstore", equalTo("PRIMARY"));
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

    @Test
    public void testGetFilBasedIdpTemplates() throws Exception {

        String fileBasedIdpTemplateId = "d7c8549f-32af-4f53-9013-f66f1a6c67bf";
        String baseIdentifier = "templates.find{ it.id == '" + fileBasedIdpTemplateId + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Facebook"))
                .body(baseIdentifier + "category", equalTo("DEFAULT"))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/templates/" + fileBasedIdpTemplateId))
                .body("templates.size()", notNullValue());
    }

    @Test
    public void testAddIdPTemplate() throws IOException {

        String body = readResource("add-idp-template.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        idPTemplateId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(idPTemplateId);
    }

    @Test(dependsOnMethods = "testAddIdPTemplate")
    public void testGetIdPTemplate() throws Exception {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH +
                PATH_SEPARATOR + idPTemplateId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(idPTemplateId))
                .body("name", equalTo("Google"))
                .body("description", equalTo("Template for google IdPs."))
                .body("category", equalTo("DEFAULT"))
                .body("image", equalTo("google-logo-url"))
                .body("displayOrder", equalTo(10))
                .body("idp", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetIdPTemplate"})
    public void testGetIdPTemplates() throws Exception {

        String baseIdentifier = "templates.find{ it.id == '" + idPTemplateId + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "description", equalTo("Template for google IdPs."))
                .body(baseIdentifier + "category", equalTo("DEFAULT"))
                .body(baseIdentifier + "image", equalTo("google-logo-url"))
                .body(baseIdentifier + "displayOrder", equalTo(10))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/templates/" + idPTemplateId))
                .body("templates.size()", notNullValue());
    }

    @Test(dependsOnMethods = {"testGetIdPTemplates"})
    public void testFilterIdPTemplates() throws Exception {

        String baseIdentifier = "templates.find{ it.id == '" + idPTemplateId + "' }.";
        String url = IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH;
        Map<String, Object> filterParam = new HashMap<>();
        filterParam.put("filter", "category eq 'DEFAULT' and name eq 'Google'");
        Response response = getResponseOfGetWithQueryParams(url, filterParam);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "category", equalTo("DEFAULT"))
                .body(baseIdentifier + "self", equalTo("/t/" + context.getContextTenant().getDomain() +
                        "/api/server/v1/identity-providers/templates/" + idPTemplateId));
    }

    @Test(dependsOnMethods = {"testFilterIdPTemplates"})
    public void testUpdateIdPTemplate() throws Exception {

        String body = readResource("update-idp-template.json");
        Response response = getResponseOfPut(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH +
                PATH_SEPARATOR + idPTemplateId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateIdPTemplate"})
    public void testDeleteIdPTemplate() throws Exception {

        Response response = getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + IDP_TEMPLATE_PATH +
                PATH_SEPARATOR + idPTemplateId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
