/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.organization.management.v1.model.OrganizationLevel;

import java.util.HashSet;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.OrganizationManagementTestData.APPLICATION_PAYLOAD;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.organization.management.v1.Utils.extractOrganizationIdFromLocationHeader;

/**
 * Tests for happy paths of the Organization Management REST API.
 */
public class OrganizationManagementSuccessTest extends OrganizationManagementBaseTest {

    private Set<String> createdOrgs = new HashSet<>();
    private String createdOrganizationId;
    private String createdOrganizationName;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public OrganizationManagementSuccessTest(TestUserMode userMode, OrganizationLevel organizationLevel)
            throws Exception {

        super(userMode, organizationLevel);
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testFinish() {

        cleanUpOrganizations(createdOrgs);
        super.testFinish();
    }

    @Test
    public void createOrganization() throws Exception {

        JSONObject organizationObject = new JSONObject();
        String org;
        String parentId;

        if (OrganizationLevel.SUPER_ORGANIZATION.equals(this.organizationLevel)) {
            org = "Level1Org";
            parentId = SUPER_ORGANIZATION_NAME;
        } else {
            org = "Level2Org";
            parentId = subOrganizationId;
        }
        organizationObject.put(ORGANIZATION_NAME, org);
        organizationObject.put(ORGANIZATION_PARENT_ID, parentId);

        Response responseOfPost = getResponseOfPost(ORGANIZATION_MANAGEMENT_API_BASE_PATH,
                organizationObject.toString());
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        String createdOrgId = extractOrganizationIdFromLocationHeader(location);
        createdOrgs.add(createdOrgId);
        createdOrganizationId = createdOrgId;
        createdOrganizationName = org;

        assertNotBlank(createdOrgId);
        if (organizationLevel == OrganizationLevel.SUB_ORGANIZATION) {
            // Check whether password recovery is enabled in the created sub-organization.
            String governanceURL = "/o/" + createdOrganizationId +
                    "/api/server/v1/identity-governance/QWNjb3VudCBNYW5hZ2VtZW50/connectors/YWNjb3VudC1yZWNvdmVyeQ";
            given()
                    .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .log().ifValidationFails()
                    .when()
                    .get(backendURL.replace(SERVICES, governanceURL))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("properties.find { it.name == 'Recovery.Notification.Password.Enable' }.value",
                            equalTo("true"))
                    .body("properties.find { it.name == 'Recovery.NotifySuccess' }.value", equalTo("true"));

            // Check whether application creation is disabled in the sub-organization.
            String appCreationURL = "/o/" + createdOrganizationId + "/api/server/v1/applications";
            Response response = given()
                    .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .body(APPLICATION_PAYLOAD)
                    .log().ifValidationFails()
                    .when()
                    .post(backendURL.replace(SERVICES, appCreationURL));
            response.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_BAD_REQUEST)
                    .body("code", equalTo("ORG-60078"))
                    .body("message", equalTo("Error creating application."))
                    .body("description", equalTo("Applications cannot be created for sub-organizations."));
        }
    }

    @Test(dependsOnMethods = {"createOrganization"})
    public void testGetOrganizationById() throws Exception {

        getResponseOfGet(ORGANIZATION_MANAGEMENT_API_BASE_PATH + "/" + createdOrganizationId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ORGANIZATION_NAME, equalTo(createdOrganizationName));
    }
}
