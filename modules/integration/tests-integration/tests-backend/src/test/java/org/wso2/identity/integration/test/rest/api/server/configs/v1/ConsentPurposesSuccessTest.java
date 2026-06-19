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

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Integration tests for the /configs/consent/purposes/{purpose-id}/applications REST API (success paths).
 *
 * <p>Test flow:
 * <ol>
 *   <li>GET with a non-existent purpose ID — expects 404</li>
 *   <li>Setup: create element and purpose (via consent-mgt API), and application (via app management API)</li>
 *   <li>POST to map the app to the purpose — expects 201</li>
 *   <li>GET the mapped apps for the purpose — expects 200 with one entry</li>
 *   <li>DELETE the mapping — expects 204</li>
 *   <li>Delete the app, then GET — expects 200 with empty list</li>
 *   <li>Delete the purpose, then GET — expects 404</li>
 * </ol>
 */
public class ConsentPurposesSuccessTest extends ConsentPurposesTestBase {

    private OAuth2RestClient oAuth2RestClient;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentPurposesSuccessTest(TestUserMode userMode) throws Exception {

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

        String consentMgtBasePath = getConsentMgtBasePath(tenant);

        // Create element (prerequisite for purpose creation).
        RestAssured.basePath = consentMgtBasePath;
        Response elementResponse = getResponseOfPostNoFilter(CONSENT_MGT_ELEMENTS_PATH,
                readResource("create-consent-element.json"));
        elementResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        createdElementId = elementResponse.jsonPath().getString("id");
        Assert.assertNotNull(createdElementId, "Element creation returned a null ID");

        // Create purpose referencing the element.
        String purposeBody = readResource("create-consent-purpose.json")
                .replace("ELEMENT_ID_PLACEHOLDER", createdElementId);
        Response purposeResponse = getResponseOfPostNoFilter(CONSENT_MGT_PURPOSES_PATH, purposeBody);
        purposeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        createdPurposeId = purposeResponse.jsonPath().getString("id");
        Assert.assertNotNull(createdPurposeId, "Purpose creation returned a null ID");

        RestAssured.basePath = basePath;

        // Create application.
        ApplicationModel app = new ApplicationModel();
        app.setName("ConsentPurposeTestApp");
        createdAppId = oAuth2RestClient.createApplication(app);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        try {
            if (createdAppId != null) {
                oAuth2RestClient.deleteApplication(createdAppId);
                createdAppId = null;
            }
            String consentMgtBasePath = getConsentMgtBasePath(tenant);
            RestAssured.basePath = consentMgtBasePath;
            if (createdPurposeId != null) {
                deleteNoFilter(CONSENT_MGT_PURPOSES_PATH + "/" + createdPurposeId);
                createdPurposeId = null;
            }
            if (createdElementId != null) {
                deleteNoFilter(CONSENT_MGT_ELEMENTS_PATH + "/" + createdElementId);
                createdElementId = null;
            }
            RestAssured.basePath = basePath;
        } finally {
            super.testConclude();
        }
    }

    @Test(groups = "wso2.is")
    public void testGetApplicationsForNonExistentPurpose() {

        String nonExistentId = UUID.randomUUID().toString();
        getResponseOfGet(CONSENT_PURPOSES_API_PATH + "/" + nonExistentId + APPLICATIONS_SUFFIX)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetApplicationsForNonExistentPurpose"})
    public void testAddApplicationToPurpose() throws IOException {

        String body = readResource("add-application-to-purpose.json")
                .replace("APP_ID_PLACEHOLDER", createdAppId);
        getResponseOfPost(CONSENT_PURPOSES_API_PATH + "/" + createdPurposeId + APPLICATIONS_SUFFIX, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testAddApplicationToPurpose"})
    public void testGetApplicationsForPurpose() {

        getResponseOfGet(CONSENT_PURPOSES_API_PATH + "/" + createdPurposeId + APPLICATIONS_SUFFIX)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(createdAppId));
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetApplicationsForPurpose"})
    public void testRemoveApplicationFromPurpose() {

        getResponseOfDelete(
                CONSENT_PURPOSES_API_PATH + "/" + createdPurposeId + APPLICATIONS_SUFFIX + "/" + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testRemoveApplicationFromPurpose"})
    public void testGetApplicationsAfterDeletingApp() throws Exception {

        oAuth2RestClient.deleteApplication(createdAppId);
        createdAppId = null;

        getResponseOfGet(CONSENT_PURPOSES_API_PATH + "/" + createdPurposeId + APPLICATIONS_SUFFIX)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", empty());
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetApplicationsAfterDeletingApp"})
    public void testGetApplicationsAfterDeletingPurpose() {

        String purposeId = createdPurposeId;
        String consentMgtBasePath = getConsentMgtBasePath(tenant);
        RestAssured.basePath = consentMgtBasePath;
        deleteNoFilter(CONSENT_MGT_PURPOSES_PATH + "/" + purposeId);
        RestAssured.basePath = basePath;
        createdPurposeId = null;

        getResponseOfGet(CONSENT_PURPOSES_API_PATH + "/" + purposeId + APPLICATIONS_SUFFIX)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    /**
     * Sends a DELETE to the given endpoint path without swagger validation.
     * Caller is responsible for setting RestAssured.basePath before calling.
     */
    private void deleteNoFilter(String endpointPath) {

        given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .delete(endpointPath)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
