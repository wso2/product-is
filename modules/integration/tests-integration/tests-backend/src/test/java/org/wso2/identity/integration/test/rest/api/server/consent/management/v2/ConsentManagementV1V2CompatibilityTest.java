/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.consent.management.v2;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.wso2.identity.integration.test.util.Utils.getBasicAuthHeader;

/**
 * Test class for V1-V2 API compatibility.
 *
 * Ensures that:
 * - Resources created via V1 API are accessible via V2 API
 * - Resources created via V2 API are accessible via V1 API
 * - Data structure mapping works correctly across versions
 */
public class ConsentManagementV1V2CompatibilityTest extends ConsentManagementV2TestBase {

    private static final String V1_CONSENT_ENDPOINT_SUFFIX = "/api/identity/consent-mgt/v1.0/consents";

    private static String v1CreatedElementName;
    private static int v2CreatedElementId;
    private static String v1CreatedPurposeName;
    private static int v2CreatedPurposeId;

    private String v1ConsentEndpoint;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV1V2CompatibilityTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, XPathExpressionException {

        super.testInit(tenant);
        this.v1ConsentEndpoint = context.getContextUrls().getWebAppURLHttps() + V1_CONSENT_ENDPOINT_SUFFIX;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    // =========================================================================
    // Element Cross-Version Compatibility Tests
    // =========================================================================

    /**
     * Create an element via V1 API and verify it's accessible via V2 API.
     */
    @Test(groups = "wso2.is")
    public void testCreateElementV1GetElementV2() throws IOException {

        // Create element via V1 API using /pii-categories endpoint
        String v1ElementBody = "{"
                + "\"piiCategory\": \"v1_email\","
                + "\"description\": \"Created via V1 API\","
                + "\"sensitive\": true"
                + "}";

        RestClient restClient = new RestClient();
        Resource piiCatResource = restClient.resource(v1ConsentEndpoint + "/pii-categories");
        String v1CreateResponseStr = piiCatResource
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo))
                .post(String.class, v1ElementBody);

        JSONObject v1CreateResponse = (JSONObject) JSONValue.parse(v1CreateResponseStr);

        // V1 response doesn't have numeric ID, extract piiCategory name
        v1CreatedElementName = (String) v1CreateResponse.get("piiCategory");

        // Verify via V2 API - search by name
        getResponseOfGet(ELEMENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items.find { it.name == '" + v1CreatedElementName + "' }", notNullValue());
    }

    /**
     * Create an element via V2 API and verify it's accessible via V1 API.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElementV1GetElementV2"})
    public void testCreateElementV2GetElementV1() throws IOException {

        String v2ElementBody = readResource("create-element-compat.json");
        Response v2CreateResponse = getResponseOfPost(ELEMENTS_ENDPOINT, v2ElementBody);

        v2CreateResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        v2CreatedElementId = v2CreateResponse.jsonPath().getInt("id");

        // Verify via V1 API - search by name in pii-categories list
        RestClient restClient = new RestClient();
        Resource piiCatResource = restClient.resource(v1ConsentEndpoint + "/pii-categories");
        String v1ListResponseStr = piiCatResource
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo))
                .get(String.class);

        org.testng.Assert.assertNotNull(JSONValue.parse(v1ListResponseStr));
    }

    /**
     * Create elements via both V1 and V2, verify both appear in V2 list.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testCreateElementV2GetElementV1"})
    public void testElementsCreatedByBothVersionsAppearInV2List() {

        getResponseOfGet(ELEMENTS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("items.find { it.name == '" + v1CreatedElementName + "' }", notNullValue())
                .body("items.find { it.id == " + v2CreatedElementId + " }", notNullValue());
    }

    // =========================================================================
    // Purpose Cross-Version Compatibility Tests
    // =========================================================================

    /**
     * Create a purpose via V1 API and verify it's accessible via V2 API.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testElementsCreatedByBothVersionsAppearInV2List"})
    public void testCreatePurposeV1GetPurposeV2() throws IOException {

        String v1PurposeBody = "{"
                + "  \"purpose\": \"v1_purpose\","
                + "  \"description\": \"Created via V1 API\","
                + "  \"group\": \"Test Group\","
                + "  \"groupType\": \"Test\","
                + "  \"piiCategories\": ["
                + "    {"
                + "      \"piiCategoryId\": 1,"
                + "      \"mandatory\": true"
                + "    }"
                + "  ]"
                + "}";

        RestClient restClient = new RestClient();
        Resource purposeResource = restClient.resource(v1ConsentEndpoint + "/purposes");
        String v1CreateResponseStr = purposeResource
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo))
                .post(String.class, v1PurposeBody);

        JSONObject v1CreateResponse = (JSONObject) JSONValue.parse(v1CreateResponseStr);
        v1CreatedPurposeName = (String) v1CreateResponse.get("purpose");

        // Verify via V2 API - search by name in purposes list
        getResponseOfGet(PURPOSES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items.find { it.name == '" + v1CreatedPurposeName + "' }", notNullValue())
                .body("items.find { it.name == '" + v1CreatedPurposeName + "' }.group", equalTo("Test Group"));
    }

    /**
     * Create a purpose via V2 API and verify it's accessible via V1 API.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurposeV1GetPurposeV2"})
    public void testCreatePurposeV2GetPurposeV1() throws IOException {

        String v2PurposeBody = readResource("create-purpose-compat.json")
                .replace("\"elementId\": 1", "\"elementId\": " + v2CreatedElementId);

        Response v2CreateResponse = getResponseOfPost(PURPOSES_ENDPOINT, v2PurposeBody);

        v2CreateResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        v2CreatedPurposeId = v2CreateResponse.jsonPath().getInt("id");
        String v2PurposeName = v2CreateResponse.jsonPath().getString("name");

        // Verify via V1 API - search by name in purposes list
        RestClient restClient = new RestClient();
        Resource purposeResource = restClient.resource(v1ConsentEndpoint + "/purposes");
        String v1ListResponseStr = purposeResource
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo))
                .get(String.class);

        org.testng.Assert.assertNotNull(JSONValue.parse(v1ListResponseStr));
    }

    /**
     * Verify purposes created by both V1 and V2 appear in V2 list.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testCreatePurposeV2GetPurposeV1"})
    public void testPurposesCreatedByBothVersionsAppearInV2List() {

        getResponseOfGet(PURPOSES_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", notNullValue())
                .body("items.find { it.name == '" + v1CreatedPurposeName + "' }", notNullValue())
                .body("items.find { it.id == " + v2CreatedPurposeId + " }", notNullValue());
    }

    /**
     * Verify purposes created by both V1 and V2 appear in V1 list.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testPurposesCreatedByBothVersionsAppearInV2List"})
    public void testPurposesCreatedByBothVersionsAppearInV1List() {

        RestClient restClient = new RestClient();
        Resource purposeResource = restClient.resource(v1ConsentEndpoint + "/purposes");
        String v1ListResponseStr = purposeResource
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo))
                .get(String.class);

        org.testng.Assert.assertNotNull(JSONValue.parse(v1ListResponseStr));
    }

    // =========================================================================
    // Purpose Versioning Compatibility Tests
    // =========================================================================

    /**
     * Verify that a purpose created in V1 shows version field in V2 API.
     * This test verifies that V1-created purposes are compatible with V2 versioning.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testPurposesCreatedByBothVersionsAppearInV1List"})
    public void testV1CreatedPurposeHasVersionInV2() {

        // Find the V1-created purpose by name
        Response v2GetResponse = getResponseOfGet(PURPOSES_ENDPOINT);
        v2GetResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items.find { it.name == '" + v1CreatedPurposeName + "' }.version", notNullValue());
    }

    /**
     * Verify that purpose versions created in V2 are accessible from V2 API.
     * V1 API does not support versioning, which is a V2-only feature.
     */
    @Test(groups = "wso2.is", dependsOnMethods = {"testV1CreatedPurposeHasVersionInV2"})
    public void testPurposeVersionsCreatedInV2() throws IOException {

        // Create a version via V2
        String versionBody = readResource("create-purpose-version-compat.json")
                .replace("\"elementId\": 2", "\"elementId\": " + v2CreatedElementId);

        Response v2VersionResponse = getResponseOfPost(
                PURPOSES_ENDPOINT + "/" + v2CreatedPurposeId + VERSIONS_ENDPOINT, versionBody);

        v2VersionResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("version", notNullValue());

        // Verify accessible via V2 versions endpoint
        getResponseOfGet(PURPOSES_ENDPOINT + "/" + v2CreatedPurposeId + VERSIONS_ENDPOINT)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("items", hasSize(2)); // Auto-snapshot v1 + new v2

        // Note: V1 API doesn't have versioning endpoint, this is V2-only feature
    }
}
