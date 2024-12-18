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
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for Identity Provider Management REST APIs success paths.
 */
public class IdPSuccessTest extends IdPTestBase {

    private static final String FEDERATED_AUTHENTICATOR_ID_PLACEHOLDER = "<FEDERATED_AUTHENTICATOR_ID>";
    private static final String FEDERATED_AUTHENTICATOR_PLACEHOLDER = "\"<FEDERATED_AUTHENTICATOR>\"";
    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";
    private static final String OIDC_IDP_NAME_PLACEHOLDER = "<OIDC_IDP_NAME>";
    private static final String METADATA_SAML_PLACEHOLDER = "<METADATA_SAML>";
    private static final String OIDC_SCOPES_PLACEHOLDER = "\"<OIDC_SCOPES>\"";
    private static final String AUTHENTICATOR_PROPERTIES_PLACEHOLDER = "\"<AUTHENTICATOR_PROPERTIES>\"";
    private static final String FEDERATED_AUTHENTICATOR_ID = "Y3VzdG9tQXV0aGVudGljYXRvcg";
    private static final String OIDC_AUTHENTICATOR_ID = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    private static final String SAML_AUTHENTICATOR_ID = "U0FNTFNTT0F1dGhlbnRpY2F0b3I";
    private static final String CUSTOM_IDP_NAME = "Custom Auth IDP";
    private static final String SAML_IDP_NAME = "SAML IdP";
    private static final String ENDPOINT_URI = "https://abc.com/authenticate";
    private static final String UPDATED_ENDPOINT_URI = "https://xyz.com/authenticate";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String USERNAME_VALUE = "testUser";
    private static final String ACCESS_TOKEN_VALUE = "testBearerToken";
    private static final String PASSWORD_VALUE = "testPassword";
    private static final String IDP_NAME = "Google";
    private static final String TRUSTED_TOKEN_ISS_IDP_NAME = "Trusted Token Issuer IdP";
    private static final String AUTHENTICATOR_NAME = "GoogleOIDCAuthenticator";
    private static final String DEFINED_BY_SYSTEM = "SYSTEM";
    private static final String DEFINED_BY_USER = "USER";
    private static final String CUSTOM_TAGS = "Custom";
    private UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload;
    private String idpCreatePayload;
    private String idPId;
    private String trustedTokenIdPId;
    private String customIdPId;
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
        userDefinedAuthenticatorPayload = createUserDefinedAuthenticatorPayloadWithBasic(ENDPOINT_URI);
        idpCreatePayload = readResource("add-idp-with-custom-fed-auth.json");
    }

    private UserDefinedAuthenticatorPayload createUserDefinedAuthenticatorPayloadWithBasic(String endpointUri) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(FEDERATED_AUTHENTICATOR_ID);
        userDefinedAuthenticatorPayload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpointUri);
        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BASIC);
        Map<String, Object> properties = new HashMap<>();
        properties.put(USERNAME, USERNAME_VALUE);
        properties.put(PASSWORD, PASSWORD_VALUE);
        authenticationType.setProperties(properties);
        endpoint.authentication(authenticationType);
        userDefinedAuthenticatorPayload.setEndpoint(endpoint);

        return userDefinedAuthenticatorPayload;
    }

    private UserDefinedAuthenticatorPayload createUserDefinedAuthenticatorPayloadWithBearer(String endpointUri) {

        UserDefinedAuthenticatorPayload userDefinedAuthenticatorPayload = new UserDefinedAuthenticatorPayload();
        userDefinedAuthenticatorPayload.setIsEnabled(true);
        userDefinedAuthenticatorPayload.setAuthenticatorId(FEDERATED_AUTHENTICATOR_ID);
        userDefinedAuthenticatorPayload.setDefinedBy(FederatedAuthenticatorRequest.DefinedByEnum.USER.toString());

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(endpointUri);
        AuthenticationType authenticationType = new AuthenticationType();
        authenticationType.setType(AuthenticationType.TypeEnum.BEARER);
        Map<String, Object> properties = new HashMap<>();
        authenticationType.setType(AuthenticationType.TypeEnum.BEARER);
        properties.put(ACCESS_TOKEN, ACCESS_TOKEN_VALUE);
        authenticationType.setProperties(properties);
        endpoint.authentication(authenticationType);
        userDefinedAuthenticatorPayload.setEndpoint(endpoint);

        return userDefinedAuthenticatorPayload;
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
                .body("find{ it.authenticatorId == 'T2ZmaWNlMzY1QXV0aGVudGljYXRvcg' }.self", equalTo(
                        getTenantedRelativePath("/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/T2ZmaWNlMzY1QXV0aGVudGljYXRvcg", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'VHdpdHRlckF1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("TwitterAuthenticator"))
                .body("find{ it.authenticatorId == 'VHdpdHRlckF1dGhlbnRpY2F0b3I' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/VHdpdHRlckF1dGhlbnRpY2F0b3I", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'RmFjZWJvb2tBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("FacebookAuthenticator"))
                .body("find{ it.authenticatorId == 'RmFjZWJvb2tBdXRoZW50aWNhdG9y' }.self", equalTo(
                        getTenantedRelativePath("/api/server/v1/identity-providers/meta/federated-authenticators"
                                + "/RmFjZWJvb2tBdXRoZW50aWNhdG9y", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("GoogleOIDCAuthenticator"))
                .body("find{ it.authenticatorId == 'R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I' }.self", equalTo(
                        getTenantedRelativePath("/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("MicrosoftWindowsLiveAuthenticator"))
                .body("find{ it.authenticatorId == 'TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y' }.self", equalTo
                        (getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/TWljcm9zb2Z0V2luZG93c0xpdmVBdXRoZW50aWNhdG9y", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("PassiveSTSAuthenticator"))
                .body("find{ it.authenticatorId == 'UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/UGFzc2l2ZVNUU0F1dGhlbnRpY2F0b3I", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y' }.name", equalTo
                        ("IWAKerberosAuthenticator"))
                .body("find{ it.authenticatorId == 'SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/SVdBS2VyYmVyb3NBdXRoZW50aWNhdG9y", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'U0FNTFNTT0F1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("SAMLSSOAuthenticator"))
                .body("find{ it.authenticatorId == 'U0FNTFNTT0F1dGhlbnRpY2F0b3I' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators/U0FNTFNTT0F1dGhlbnRpY2F0b3I",
                                context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I' }.name", equalTo
                        ("OpenIDConnectAuthenticator"))
                .body("find{ it.authenticatorId == 'T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators" +
                                "/T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I", context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'RW1haWxPVFA' }.name", equalTo("EmailOTP"))
                .body("find{ it.authenticatorId == 'RW1haWxPVFA' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators/RW1haWxPVFA",
                                context.getContextTenant().getDomain())))
                .body("find{ it.authenticatorId == 'U01TT1RQ' }.name", equalTo("SMSOTP"))
                .body("find{ it.authenticatorId == 'U01TT1RQ' }.self", equalTo(
                        getTenantedRelativePath(
                                "/api/server/v1/identity-providers/meta/federated-authenticators/U01TT1RQ",
                                context.getContextTenant().getDomain())));
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
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.description", equalTo("Additional" +
                        " query parameters to be sent to Google."))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.displayOrder", equalTo(4))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.isMandatory", equalTo(false))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'AdditionalQueryParameters' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'callbackUrl' }.displayName", equalTo("Callback URL"))
                .body("properties.find{ it.key == 'callbackUrl' }.description", equalTo("The callback URL " +
                        "used to obtain Google credentials."))
                .body("properties.find{ it.key == 'callbackUrl' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'callbackUrl' }.displayOrder", equalTo(3))
                .body("properties.find{ it.key == 'callbackUrl' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'callbackUrl' }.isMandatory", equalTo(false))
                .body("properties.find{ it.key == 'callbackUrl' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'callbackUrl' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'ClientId' }.displayName", equalTo("Client ID"))
                .body("properties.find{ it.key == 'ClientId' }.description", equalTo("The client identifier " +
                        "value of the Google identity provider."))
                .body("properties.find{ it.key == 'ClientId' }.type", equalTo("STRING"))
                .body("properties.find{ it.key == 'ClientId' }.displayOrder", equalTo(1))
                .body("properties.find{ it.key == 'ClientId' }.regex", equalTo(".*"))
                .body("properties.find{ it.key == 'ClientId' }.isMandatory", equalTo(true))
                .body("properties.find{ it.key == 'ClientId' }.isConfidential", equalTo(false))
                .body("properties.find{ it.key == 'ClientId' }.defaultValue", equalTo(""))

                .body("properties.find{ it.key == 'ClientSecret' }.displayName", equalTo("Client secret"))
                .body("properties.find{ it.key == 'ClientSecret' }.description", equalTo("The client secret " +
                        "value of the Google identity provider."))
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
                .body("find{ it.connectorId == 'Z29vZ2xlYXBwcw' }.self", equalTo(
                        getTenantedRelativePath(
                            "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                            "/Z29vZ2xlYXBwcw", context.getContextTenant().getDomain())))
                .body("find{ it.connectorId == 'c2FsZXNmb3JjZQ' }.name", equalTo("salesforce"))
                .body("find{ it.connectorId == 'c2FsZXNmb3JjZQ' }.self", equalTo(
                        getTenantedRelativePath(
                            "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                            "/c2FsZXNmb3JjZQ", context.getContextTenant().getDomain())))
                .body("find{ it.connectorId == 'c2NpbQ' }.name", equalTo("scim"))
                .body("find{ it.connectorId == 'c2NpbQ' }.self", equalTo(getTenantedRelativePath(
                            "/api/server/v1/identity-providers/meta/outbound-provisioning-connectors" +
                            "/c2NpbQ", context.getContextTenant().getDomain())));
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

    @Test
    public void testAddIdPWithUserDefinedAuthenticator() throws IOException, XPathExpressionException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                FEDERATED_AUTHENTICATOR_ID + "' }.";

        String body = idpCreatePayload.replace(FEDERATED_AUTHENTICATOR_ID_PLACEHOLDER,
                userDefinedAuthenticatorPayload.getAuthenticatorId());
        body = body.replace(FEDERATED_AUTHENTICATOR_PLACEHOLDER,
                userDefinedAuthenticatorPayload.convertToJasonPayload());
        body = body.replace(IDP_NAME_PLACEHOLDER, CUSTOM_IDP_NAME);

        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("name", equalTo(CUSTOM_IDP_NAME))
                .body(baseIdentifier + "authenticatorId", equalTo(FEDERATED_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().decode(FEDERATED_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_USER))
                .body(baseIdentifier + "tags", hasItems(CUSTOM_TAGS))
                .body(baseIdentifier + "self", notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        customIdPId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(customIdPId);
    }

    @Test(dependsOnMethods = "testAddIdPWithUserDefinedAuthenticator")
    public void testGetUserDefinedAuthenticatorsOfIdP() throws XPathExpressionException {

        String baseIdentifier = "authenticators.find { it.authenticatorId == '" + FEDERATED_AUTHENTICATOR_ID + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + customIdPId +
                PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("defaultAuthenticatorId", equalTo(FEDERATED_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().decode(FEDERATED_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_USER))
                .body(baseIdentifier + "tags", hasItems(CUSTOM_TAGS))
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + customIdPId +
                                "/federated-authenticators/" + FEDERATED_AUTHENTICATOR_ID,
                        context.getContextTenant().getDomain())));
    }

    @Test(dependsOnMethods = "testGetUserDefinedAuthenticatorsOfIdP")
    public void testUpdateUserDefinedAuthenticatorOfIdP() throws JsonProcessingException, XPathExpressionException {

        Response response = getResponseOfPut(IDP_API_BASE_PATH + PATH_SEPARATOR + customIdPId +
                        PATH_SEPARATOR + IDP_FEDERATED_AUTHENTICATORS_PATH + PATH_SEPARATOR + FEDERATED_AUTHENTICATOR_ID,
                createUserDefinedAuthenticatorPayloadWithBearer(UPDATED_ENDPOINT_URI)
                        .convertToJasonPayload());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("authenticatorId", equalTo(FEDERATED_AUTHENTICATOR_ID))
                .body("name", equalTo(new String(Base64.getDecoder().decode(FEDERATED_AUTHENTICATOR_ID))))
                .body("isEnabled", equalTo(true))
                .body("definedBy", equalTo(DEFINED_BY_USER))
                .body("tags", hasItems(CUSTOM_TAGS))
                .body("endpoint.uri", equalTo(UPDATED_ENDPOINT_URI))
                .body("endpoint.authentication.type", equalTo(AuthenticationType.TypeEnum.BEARER.value()));
    }

    @Test(dependsOnMethods = {"testGetIdPs", "testUpdateUserDefinedAuthenticatorOfIdP"})
    public void testDeleteIdPWithUserDefinedAuthenticator() {

        Response response = getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + customIdPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + customIdPId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Resource not found."))
                .body("description", equalTo("Unable to find a resource matching the provided identity " +
                        "provider identifier " + customIdPId + "."));

    }

    @Test(dependsOnMethods = {"testGetMetaOutboundConnector"})
    public void testAddIdP() throws IOException, XPathExpressionException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                SAMPLE_FEDERATED_AUTHENTICATOR_ID + "' }.";

        String addIdpPayload = readResource("add-idp.json");
        String properties = convertDuplicatedPropertiesToJson(
                createAuthenticatorProperties("username","admin"), null);
        String body = addIdpPayload.replace(AUTHENTICATOR_PROPERTIES_PLACEHOLDER, properties);

        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("name", equalTo(IDP_NAME))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().
                        decode(SAMPLE_FEDERATED_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_SYSTEM))
                .body(baseIdentifier + "tags", hasItems("Social-Login", "APIAuth"))
                .body(baseIdentifier + "self", notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        idPId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(idPId);
    }

    @Test()
    public void addIdPWithoutAuthenticator() throws IOException {

        String body = readResource("add-idp-without-authenticator.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("federatedAuthenticators.authenticators", emptyIterable())
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String idpIdWithoutAuth = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(idpIdWithoutAuth);

        deleteCreatedIdP(idpIdWithoutAuth);
    }


    /* This test method has been added in order to test the current behaviour.
     * There seem to be some concerns related to internal validations used in functionality associated with this.
     * This is being tracked with the issue: https://github.com/wso2/product-is/issues/21928
     */
    @Test
    public void addIdPWithDuplicatedOIDCScopes() throws IOException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                OIDC_AUTHENTICATOR_ID + "' }.";

        String oidcIdpPayload = readResource("add-oidc-idp.json");
        String oidcScopesProperties = convertDuplicatedPropertiesToJson(
                createAuthenticatorProperties("Scopes","openid country profile"),
                createAuthenticatorProperties("commonAuthQueryParams","scope=openid country profile"));
        String body = oidcIdpPayload.replace(OIDC_SCOPES_PLACEHOLDER, oidcScopesProperties);
        body = body.replace(OIDC_IDP_NAME_PLACEHOLDER, "OIDC-IdP-1");

        Response response = getResponseOfPostNoFilter(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("name", equalTo("OIDC-IdP-1"))
                .body(baseIdentifier + "authenticatorId", equalTo(OIDC_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().decode(OIDC_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_SYSTEM))
                .body(baseIdentifier + "tags", hasItems("OIDC", "APIAuth"))
                .body(baseIdentifier + "self", notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String oidcIdpId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(oidcIdpId);

        deleteCreatedIdP(oidcIdpId);
    }

    /* This test method has been added in order to test the current behaviour.
     * There seem to be some concerns related to internal validations used in functionality associated with this.
     * This is being tracked with the issue: https://github.com/wso2/product-is/issues/21928
     */
    @Test(dependsOnMethods = "addIdPWithDuplicatedOIDCScopes")
    public void addOIDCIdPWithoutOpenidScope() throws IOException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                OIDC_AUTHENTICATOR_ID + "' }.";

        String oidcIdpPayload = readResource("add-oidc-idp.json");
        String oidcScopesProperties = convertDuplicatedPropertiesToJson(
                createAuthenticatorProperties("Scopes","country profile"), null);
        String body = oidcIdpPayload.replace(OIDC_SCOPES_PLACEHOLDER, oidcScopesProperties);
        body = body.replace(OIDC_IDP_NAME_PLACEHOLDER, "OIDC-IdP-2");

        Response response = getResponseOfPostNoFilter(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("name", equalTo("OIDC-IdP-2"))
                .body(baseIdentifier + "authenticatorId", equalTo(OIDC_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().decode(OIDC_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_SYSTEM))
                .body(baseIdentifier + "tags", hasItems("OIDC", "APIAuth"))
                .body(baseIdentifier + "self", notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String oidcIdpId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(oidcIdpId);

        deleteCreatedIdP(oidcIdpId);
    }

    @Test
    public void addSAMLStandardBasedIdP() throws IOException, XPathExpressionException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                SAML_AUTHENTICATOR_ID + "' }.";

        String samlIdpPayload = readResource("add-saml-idp.json");
        String body = samlIdpPayload.replace(METADATA_SAML_PLACEHOLDER, loadMetadataSamlFile(
                "test-metadata-saml.xml"));

        Response response = getResponseOfPostNoFilter(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body("name", equalTo(SAML_IDP_NAME))
                .body(baseIdentifier + "authenticatorId", equalTo(SAML_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().decode(SAML_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_SYSTEM))
                .body(baseIdentifier + "tags", hasItems("SAML"))
                .body(baseIdentifier + "self", notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String samlIdpId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(samlIdpId);

        deleteCreatedIdP(samlIdpId);
    }

    @Test(dependsOnMethods = {"testAddIdP"})
    public void testGetIdP() throws IOException, XPathExpressionException {

        String baseIdentifier = "federatedAuthenticators.authenticators.find { it.authenticatorId == '" +
                SAMPLE_FEDERATED_AUTHENTICATOR_ID + "' }.";

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
                .body(baseIdentifier + "name", equalTo(new String(Base64.getDecoder().
                        decode(SAMPLE_FEDERATED_AUTHENTICATOR_ID))))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "definedBy", equalTo(DEFINED_BY_SYSTEM))
                .body(baseIdentifier + "tags", hasItems("Social-Login", "APIAuth"))
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId + "/federated-authenticators/"
                                + SAMPLE_FEDERATED_AUTHENTICATOR_ID, context.getContextTenant().getDomain())))
                .body("image", equalTo("google-logo-url"))
                .body("isFederationHub", equalTo(false))
                .body("homeRealmIdentifier", equalTo("localhost"))
                .body("alias", equalTo("https://localhost:9444/oauth2/token"));
    }

    @Test(dependsOnMethods = {"testGetIdP"})
    public void testGetIdPs() throws Exception {

        String baseIdentifier = "identityProviders.find{ it.id == '" + idPId + "' }.";
        String baseIdentifierUserDef = "identityProviders.find{ it.id == '" + customIdPId + "' }.";
        Response response = getResponseOfGet(IDP_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "name", equalTo("Google"))
                .body(baseIdentifier + "description", equalTo("IDP for Google Federation"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "image", equalTo("google-logo-url"))
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId,
                        context.getContextTenant().getDomain())))
                .body(baseIdentifierUserDef + "name", equalTo(CUSTOM_IDP_NAME))
                .body(baseIdentifierUserDef + "isEnabled", equalTo(true))
                .body(baseIdentifierUserDef + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + customIdPId,
                        context.getContextTenant().getDomain())));
    }

    @Test(dependsOnMethods = "testGetIdP")
    public void testSearchAllIdPs() throws XPathExpressionException {

        Response response = getResponseOfGetWithQueryParams(IDP_API_BASE_PATH, Collections.singletonMap("filter",
                "name sw " + IDP_NAME));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("identityProviders.find { it.id == '" + idPId + "' }.name", equalTo(IDP_NAME))
                .body("identityProviders.find { it.id == '" + idPId + "' }.isEnabled", equalTo(true))
                .body("identityProviders.find { it.id == '" + idPId + "' }.self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId,
                        context.getContextTenant().getDomain())));
    }

    @Test
    public void testSearchIdPByNonExistentIdPName() {

        Response response = getResponseOfGetWithQueryParams(IDP_API_BASE_PATH, Collections.singletonMap("filter",
                "name sw InvalidIdP"));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", equalTo(0))
                .body("count", equalTo(0));
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
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId,
                        context.getContextTenant().getDomain())))
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
                .body(baseIdentifier + "authenticatorId", equalTo(SAMPLE_FEDERATED_AUTHENTICATOR_ID))
                .body(baseIdentifier + "name", equalTo("GoogleOIDCAuthenticator"))
                .body(baseIdentifier + "tags", hasItems("Social-Login", "APIAuth"))
                .body(baseIdentifier + "isEnabled", equalTo(true))
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId + "/federated-authenticators/" +
                        SAMPLE_FEDERATED_AUTHENTICATOR_ID, context.getContextTenant().getDomain())));
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
                .statusCode(HttpStatus.SC_OK)
                .body("definedBy", equalTo(DEFINED_BY_SYSTEM));
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
                .body("definedBy", equalTo(DEFINED_BY_SYSTEM))
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
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/" + idPId + "/provisioning/outbound-connectors/" +
                        SAMPLE_OUTBOUND_CONNECTOR_ID, context.getContextTenant().getDomain())));
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
                .body("roleClaim.uri", equalTo("roles"));
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
                .body("roleClaim.uri", equalTo("roles"))
                .body("mappings", notNullValue())
                .body("mappings[0].idpClaim", equalTo("country"))
                .body("mappings[0].localClaim.id", equalTo("aHR0cDovL3dzbzIub3JnL2NsYWltcy91c2VybmFtZQ"))
                .body("mappings[0].localClaim.uri", equalTo("http://wso2.org/claims/username"))
                .body("mappings[0].localClaim.displayName", equalTo("Username"))
                .body("mappings[1].idpClaim", equalTo("roles"))
                .body("mappings[1].localClaim.id", equalTo("aHR0cDovL3dzbzIub3JnL2NsYWltcy9yb2xlcw"))
                .body("mappings[1].localClaim.uri", equalTo("http://wso2.org/claims/roles"))
                .body("mappings[1].localClaim.displayName", equalTo("Roles"))
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
                .body("mappings[0].localRole", equalTo("Internal/admin"))
                .body("outboundProvisioningRoles", notNullValue())
                .body("outboundProvisioningRoles[0]", equalTo("Internal/admin"));
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

        // Test patch ADD operation for JWKS URI property.
        body = readResource("patch-add-jwks-uri.json");
        response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("certificate.jwksUri", equalTo("http://SAMPLE.JWKS.URI/"))
                .body("certificate.certificates", nullValue());

        // Test patch REMOVE operation for JWKS URI property.
        body = readResource("patch-remove-jwks-uri.json");
        response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("certificate.jwksUri", nullValue());

        // Test patch ADD operation for certificates.
        body = readResource("patch-add-certificate.json");
        response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("certificate.certificates[0]", equalTo("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR" +
                        "zRENDQXBpZ0F3SUJBZ0lKQUs0eml2ckVsYzBJTUEwR0NTcUdTSWIzRFFFQkN3VUFNSUdETVJFd0R3WUQKVlFRRERBaE" +
                        "NkV1JrYUdsdFlURUxNQWtHQTFVRUJoTUNVMHd4RURBT0JnTlZCQWdNQjFkbGMzUmxjbTR4RURBTwpCZ05WQkFjTUIwT" +
                        "nZiRzl0WW04eERUQUxCZ05WQkFvTUJGZFRUekl4Q3pBSkJnTlZCQXNNQWxGQk1TRXdId1lKCktvWklodmNOQVFrQkZo" +
                        "SmlkV1JrYUdsdFlYVkFkM052TWk1amIyMHdJQmNOTVRrd056RTJNRFF5TXpFd1doZ1AKTXpBeE9ERXhNVFl3TkRJek1" +
                        "UQmFNSUdETVJFd0R3WURWUVFEREFoQ2RXUmthR2x0WVRFTE1Ba0dBMVVFQmhNQwpVMHd4RURBT0JnTlZCQWdNQjFkbG" +
                        "MzUmxjbTR4RURBT0JnTlZCQWNNQjBOdmJHOXRZbTh4RFRBTEJnTlZCQW9NCkJGZFRUekl4Q3pBSkJnTlZCQXNNQWxGQ" +
                        "k1TRXdId1lKS29aSWh2Y05BUWtCRmhKaWRXUmthR2x0WVhWQWQzTnYKTWk1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFF" +
                        "QkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDcFo3V09VMTZpeGpiQwpiWGR3R3JhTW5xbmxnb2kzMDN5aVFxbHAySzlWTmZ" +
                        "HT21nTlFhdFdlbjB0MVVWcjYxd0Y4eVlHaDJyc1lnbithCjhwYXVmUVVQQ1laeFRFR1FpT2RPZ0RNcE5tWW82ZHU2K2" +
                        "MvenJqcHNncGh5SHIxNEZPVHAxaVRDSXBmanVwVjEKd1BUeXJveURySGRvMkpuOHI3V3F1cklJVTRBYllBN2NrdVVqL" +
                        "0tqYUovTTZrZitwRFd5SVJvaDBKTFJlWWM4UQp5bmhYcjdrQWp5RnFqNitnWndBYkh4ckhrckVzYTJoVjQ0UFJXWjFQ" +
                        "UERxTCswVU8veE1hQW5udndsdGd4QlVpCkhLUTFXWDVwdVVPaC9kQTQ5b0RsbEpraHpxd2d5eDQxc1FYbFNhVmdKakl" +
                        "UZVdSQmdvNnh6ajNmd3VvenBGS1gKbzRaeXBITDNBZ01CQUFHakl6QWhNQjhHQTFVZEVRUVlNQmFDQkhkemJ6S0NDSG" +
                        "R6YnpJdVkyOXRnZ1IzYzI4eQpNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUJTSzBKa1pyYlpvYmRDNHhZSG1IcnlVb" +
                        "kZVbkZZWUFvZmc0TFVGCkJRbWxDY0NKR0ZwR1BtN2ZDWHM0Y0h4Z0hPVTN5SkhtQ2pYaU9FRTc2dzhIU0NRcVhkNmRO" +
                        "SEwxRkxtN0pqQTUKTEZmbHhiWXNOcmVVNVpJTmREVGZvWmxSSXR0Mkd4MlpIa3pjQVRJZm1yUFNwODV2WDhGem1mbTN" +
                        "BVTVpM3FXZQo4a2YyZk5nQjlMbE5XRFk1V09paVlHUWMrRk13WWdLcDJkNGM3dzMrWnRTUXJWRy9YdGpqYTJYV09Xdm" +
                        "1sV3dLCnB4b3pyNjIvTTdUUmVkc3hJNU90bzJvWExGZXp1MUdCWHdpNEFaempMSFVsNWpSR2hMbkNZa05qdWZGZi9EQ" +
                        "0cKeUFWdnpMVXQwZ2F0b0dJdTV2eG9la05JVWV5YTZpRzJBaG9jSmM0SEJMT3l4TXE3Ci0tLS0tRU5EIENFUlRJRklD" +
                        "QVRFLS0tLS0K"))
                .body("certificate.jwksUri", nullValue());

        // Test patch REMOVE operation for certificates.
        body = readResource("patch-remove-certificate.json");
        response = getResponseOfPatch(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("certificate.certificates", nullValue());
    }

    @Test(dependsOnMethods = "testPatchIdP")
    public void testExportIDPToFile() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + idPId + PATH_SEPARATOR +
                "export");
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("identityProviderName", equalTo(IDP_NAME))
                .body("federatedAuthenticatorConfigs.find { it.name == '" + AUTHENTICATOR_NAME + "' }.definedByType",
                        equalTo(DEFINED_BY_SYSTEM));
    }

    @Test(dependsOnMethods = {"testExportIDPToFile"})
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
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/templates/"
                                + fileBasedIdpTemplateId, context.getContextTenant().getDomain())))
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
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/templates/"
                                + idPTemplateId, context.getContextTenant().getDomain())))
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
                .body(baseIdentifier + "self", equalTo(getTenantedRelativePath(
                        "/api/server/v1/identity-providers/templates/"
                                + idPTemplateId, context.getContextTenant().getDomain())));
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
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Resource not found."))
                .body("description", equalTo("Unable to find a resource matching the provided identity " +
                        "provider identifier " + idPId + "."));
    }

    /**
     * Load saml metadata content from the provided file.
     *
     * @return content of file as String
     * @throws IOException if an error occurred while reading the file.
     */
    private String loadMetadataSamlFile(String xmlFileName) throws IOException {

        String metadata = readResource(xmlFileName);
        return new String(Base64.getEncoder().encode(metadata.getBytes()));
    }

    /**
     * Creates a map of authenticator properties with a provided key and value.
     *
     * @param key   Authenticator key.
     * @param value Authenticator value.
     * @return a map containing the authenticator properties.
     */
    private Map<String, String> createAuthenticatorProperties(String key, String value) {

        Map<String, String> authenticatorProps = new HashMap<>();
        authenticatorProps.put("key", key);
        authenticatorProps.put("value", value);
        return authenticatorProps;
    }

    /**
     * Converts a map of properties and an optional map of duplicated properties into a JSON string.
     * If duplicated properties are provided, they are appended to the JSON string of the original properties.
     *
     * @param properties           Main map of properties.
     * @param duplicatedProperties Map of duplicated properties.
     * @return a JSON string representation of the properties and duplicated properties.
     * @throws JsonProcessingException if there is an error during JSON conversion.
     */
    private String convertDuplicatedPropertiesToJson(Map<String, String> properties,
                                                     Map<String, String> duplicatedProperties)
            throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        if (duplicatedProperties != null) {
            return objectMapper.writeValueAsString(properties) + "," + objectMapper.writeValueAsString(duplicatedProperties);
        }
        return objectMapper.writeValueAsString(properties);
    }

    @Test
    public void testAddTrustedTokenIssuerIdP() throws IOException {

        String body = readResource("add-trusted-token-issuer-idp.json");
        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        trustedTokenIdPId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(trustedTokenIdPId);
    }

    @Test(dependsOnMethods = "testAddTrustedTokenIssuerIdP")
    public void testGetTrustedTokenIssuerIdP() {

        Response response = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + trustedTokenIdPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(trustedTokenIdPId))
                .body("name", equalTo(TRUSTED_TOKEN_ISS_IDP_NAME));
    }

    @Test (dependsOnMethods = "testGetTrustedTokenIssuerIdP")
    public void testDeleteTrustedTokenIssuerIdP() {

        Response response = getResponseOfDelete(IDP_API_BASE_PATH + PATH_SEPARATOR + trustedTokenIdPId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        Response responseOfGet = getResponseOfGet(IDP_API_BASE_PATH + PATH_SEPARATOR + trustedTokenIdPId);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Resource not found."))
                .body("description", equalTo("Unable to find a resource matching the provided identity " +
                        "provider identifier " + trustedTokenIdPId + "."));
    }
}
