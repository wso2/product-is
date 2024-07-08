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

package org.wso2.identity.integration.test.rest.api.server.organization.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.OAuth2Util;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for failure cases of the Organization Management REST APIs.
 */
public class OrganizationManagementFailureTest extends OrganizationManagementBaseTest {

    private final String invalidM2MToken = "06c1f4e2-3339-44e4-a825-96585e3653b1";
    private final String invalidOrganizationID = "06c1f4e2-3339-44e4-a825-96585e3653b1";

    private List<String> organizationIDs = new ArrayList<>();
    private String applicationID;
    private String m2mToken;
    private HttpClient client;
    protected OAuth2RestClient restClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        client = HttpClientBuilder.create().build();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        OAuth2Util.deleteApplication(oAuth2RestClient, applicationID);
        oAuth2RestClient.closeHttpClient();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "organizationRequestBodies")
    public Object[][] organizationRequestBodyFilePaths() {

        return new Object[][] {
                {"add-greater-hospital-organization-request-body.json"},
                {"add-smaller-hospital-organization-request-body.json"}
        };
    }

    @Test
    public void testGetM2MAccessToken() throws Exception {

        String apiAuthorizations = readResource("organization-self-service-apis.json");
        URI tokenEndpoint = new URI(getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT,
                                tenantInfo.getDomain()));
        applicationID = OAuth2Util.createOIDCApplication(oAuth2RestClient, apiAuthorizations, authenticatingUserName,
                                                    authenticatingCredential);
        m2mToken = OAuth2Util.getM2MAccessToken(oAuth2RestClient, applicationID, tokenEndpoint);
    }

    @Test(dependsOnMethods = "testGetM2MAccessToken", dataProvider = "organizationRequestBodies")
    public void testSelfOnboardOrganization(String requestBodyPath) throws Exception {

        String body = readResource(requestBodyPath);
        Response response = getResponseOfPostWithOAuth2(ORGANIZATION_MANAGEMENT_API_BASE_PATH, body, m2mToken);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        String organizationID = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(organizationID);
        organizationIDs.add(organizationID);
    }

    @Test(dependsOnMethods = "testSelfOnboardOrganization")
    public void testGetOrganizationsWithInvalidOperator() {

        String filterQuery = "?filter=name ca G&limit=10&recursive=false";
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + filterQuery;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_BAD_REQUEST, "ORG-60059");
    }

    @Test(dependsOnMethods = "testGetOrganizationsWithInvalidOperator")
    public void testGetOrganizationsWithUnsupportedAttribute() {

        String filterQuery = "?filter=attribute.Country co S&limit=10&recursive=false";
        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + filterQuery;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_BAD_REQUEST, "ORG-60023");
    }

    @Test(dependsOnMethods = "testGetOrganizationsWithUnsupportedAttribute")
    public void testGetDiscoveryConfigWithoutAddingConfig() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, "OCM-60002");
    }

    @Test(dependsOnMethods = "testGetDiscoveryConfigWithoutAddingConfig")
    public void testAddDiscoveryAttributesWithoutAddingConfig() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", organizationIDs.get(0));
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_BAD_REQUEST, "ORG-60080");
    }

    @Test(dependsOnMethods = "testAddDiscoveryAttributesWithoutAddingConfig")
    public void testAddInvalidDiscoveryConfig() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String invalidRequestBody = readResource("invalid-discovery-config-request-body.json");
        Response response = getResponseOfPostWithOAuth2(endpointURL, invalidRequestBody, m2mToken);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "UE-10000");
    }

    @Test(dependsOnMethods = "testAddInvalidDiscoveryConfig")
    public void testAddDiscoveryConfigUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-config-request-body.json");
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testAddDiscoveryConfigUnauthorized")
    public void testAddExistingDiscoveryConfig() throws IOException {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-config-request-body.json");
        Response firstResponse = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_CREATED);
        Response secondResponse = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateErrorResponse(secondResponse, HttpStatus.SC_CONFLICT, "OCM-60003");
    }

    @Test(dependsOnMethods = "testAddExistingDiscoveryConfig")
    public void testGetDiscoveryConfigUnauthorized() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testGetDiscoveryConfigUnauthorized")
    public void testAddInvalidDiscoveryAttributesToOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String invalidRequestBody = readResource("add-invalid-discovery-attributes-request-body.json");
        invalidRequestBody = invalidRequestBody.replace("${organizationID}", organizationIDs.get(0));
        Response response = getResponseOfPostWithOAuth2(endpointURL, invalidRequestBody, m2mToken);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "UE-10000");
    }

    @Test(dependsOnMethods = "testAddInvalidDiscoveryAttributesToOrganization")
    public void testAddDiscoveryAttributesToNonExistingOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", invalidOrganizationID);
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_NOT_FOUND, "ORG-60015",
                                        invalidOrganizationID);
    }

    @Test(dependsOnMethods = "testAddDiscoveryAttributesToNonExistingOrganization")
    public void testAddDiscoveryAttributesToOrganizationUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", organizationIDs.get(0));
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testAddDiscoveryAttributesToOrganizationUnauthorized")
    public void testAddDiscoveryAttributesWhenAlreadyAdded() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("add-discovery-attributes-request-body.json");
        requestBody = requestBody.replace("${organizationID}", organizationIDs.get(0));
        Response firstResponse = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_CREATED);
        Response secondResponse = getResponseOfPostWithOAuth2(endpointURL, requestBody, m2mToken);
        validateErrorResponseWithoutTraceId(secondResponse, HttpStatus.SC_BAD_REQUEST, "ORG-60085",
                                        organizationIDs.get(0));
    }

    @Test(dependsOnMethods = "testAddDiscoveryAttributesWhenAlreadyAdded")
    public void testGetDiscoveryAttributesOfOrganizationsUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testGetDiscoveryAttributesOfOrganizationsUnauthorized")
    public void testGetDiscoveryAttributesOfNonExistingOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_NOT_FOUND, "ORG-60015",
                                        invalidOrganizationID);
    }

    @Test(dependsOnMethods = "testGetDiscoveryAttributesOfNonExistingOrganization")
    public void testGetDiscoveryAttributesToOrganizationUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationIDs.get(0)
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfGetWithOAuth2(endpointURL, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testGetDiscoveryAttributesToOrganizationUnauthorized")
    public void testDeleteDiscoveryAttributesOfNonExistingOrganization() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfDeleteWithOAuth2(endpointURL, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_NOT_FOUND, "ORG-60015",
                                        invalidOrganizationID);
    }

    @Test(dependsOnMethods = "testDeleteDiscoveryAttributesOfNonExistingOrganization")
    public void testDeleteDiscoveryAttributesUnauthorized() {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationIDs.get(0)
                        + ORGANIZATION_DISCOVERY_API_PATH;
        Response response = getResponseOfDeleteWithOAuth2(endpointURL, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testDeleteDiscoveryAttributesUnauthorized")
    public void testUpdateWithUnavailableDiscoveryAttributes() throws IOException {

        String firstEndpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationIDs.get(0)
                        + ORGANIZATION_DISCOVERY_API_PATH;
        String secondEndpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationIDs.get(1)
                        + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-attributes-request-body.json");
        Response firstResponse = getResponseOfPutWithOAuth2(firstEndpointURL, requestBody, m2mToken);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_OK);
        Response secondResponse = getResponseOfPutWithOAuth2(secondEndpointURL, requestBody, m2mToken);
        validateErrorResponseWithoutTraceId(secondResponse, HttpStatus.SC_BAD_REQUEST, "ORG-60083");
    }

    @Test(dependsOnMethods = "testUpdateWithUnavailableDiscoveryAttributes")
    public void testUpdateDiscoveryAttributesOfNonExistingOrganization() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + invalidOrganizationID
                        + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-attributes-request-body.json");
        Response response = getResponseOfPutWithOAuth2(endpointURL, requestBody, m2mToken);
        validateErrorResponseWithoutTraceId(response, HttpStatus.SC_NOT_FOUND, "ORG-60015",
                                        invalidOrganizationID);
    }

    @Test(dependsOnMethods = "testUpdateDiscoveryAttributesOfNonExistingOrganization")
    public void testUpdateDiscoveryAttributesUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationIDs.get(0)
                        + ORGANIZATION_DISCOVERY_API_PATH;
        String requestBody = readResource("update-discovery-attributes-request-body.json");
        Response response = getResponseOfPutWithOAuth2(endpointURL, requestBody, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testUpdateDiscoveryAttributesUnauthorized")
    public void testCheckDiscoveryAttributeExistsUnauthorized() throws IOException {

        String endpointURL = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "check-discovery";
        String requestBody = readResource("check-discovery-attributes-available-request-body.json");
        Response response = getResponseOfPostWithOAuth2(endpointURL, requestBody, invalidM2MToken);
        validateHttpStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(dependsOnMethods = "testCheckDiscoveryAttributeExistsUnauthorized")
    public void testDeleteDiscoveryConfigUnauthorized() {

        String endpointURL = ORGANIZATION_CONFIGS_API_BASE_PATH + ORGANIZATION_DISCOVERY_API_PATH;
        Response firstResponse = getResponseOfDeleteWithOAuth2(endpointURL, invalidM2MToken);
        validateHttpStatusCode(firstResponse, HttpStatus.SC_UNAUTHORIZED);
        Response secondResponse = getResponseOfDeleteWithOAuth2(endpointURL, m2mToken);
        validateHttpStatusCode(secondResponse, HttpStatus.SC_NO_CONTENT);
    }

    @Test(dependsOnMethods = "testDeleteDiscoveryConfigUnauthorized")
    public void deleteOrganizations() {

        for (String organizationID : organizationIDs) {
            String organizationPath = ORGANIZATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + organizationID;
            Response responseOfDelete = getResponseOfDelete(organizationPath);
            responseOfDelete.then()
                    .log()
                    .ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }
    }
}
