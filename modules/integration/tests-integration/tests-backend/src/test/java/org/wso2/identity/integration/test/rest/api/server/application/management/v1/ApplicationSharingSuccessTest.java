/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationShareRolePolicy;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_ONLY;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareRequestBodyOrganizations.PolicyEnum.SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN;
import static org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v1.model.UserShareWithAllRequestBody.PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS;

/**
 * Tests for positive paths of the Application Management Share REST API.
 */
public class ApplicationSharingSuccessTest extends ApplicationManagementBaseTest {

    private String createdAppId;
    private final List<String> createdOrganizations = new ArrayList<>();
    private OrgMgtRestClient orgMgtRestClient;

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis-for-application-sharing-test.json";
    private static final String GENERAL_SHARE_WITH_ALL_ROLES_REQUEST
            = "general-share-application-with-all-roles-request.json";
    private static final String SELECTIVE_SHARE_WITH_ALL_ROLES_REQUEST
            = "selective-share-application-with-all-roles-request.json";
    private static final String GENERAL_UNSHARE_REQUEST = "general-unshare-application.json";
    private static final String SELECTIVE_UNSHARE_REQUEST = "selective-unshare-application.json";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationSharingSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @AfterClass(alwaysRun = true)
    public void testComplete() throws Exception {

        Set<String> createdApps = new HashSet<>();
        if (createdAppId != null) {
            createdApps.add(createdAppId);
        }
        cleanUpApplications(createdApps);
        cleanUpOrganizations(createdOrganizations, orgMgtRestClient);

        super.testConclude();
    }

    @BeforeClass(alwaysRun = true)
    public void testStart() throws Exception {

        super.init();
        RestAssured.basePath = basePath;
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON, this.getClass())));

        createdAppId = getApplicationId(createApplication(SAMPLE_APP_NAME));
        assertNotBlank(createdAppId);
    }

    @Test
    public void testGeneralShareApplicationForExistingAndFutureChildren() throws Exception {

        String organizationId = orgMgtRestClient.addOrganization(SAMPLE_ORG_NAME_01);
        createdOrganizations.add(organizationId);

        String requestBody = readResource(GENERAL_SHARE_WITH_ALL_ROLES_REQUEST);
        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId)
                .replace(POLICY_PLACEHOLDER, String.valueOf(ALL_EXISTING_AND_FUTURE_ORGS));

        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_SHARE_WITH_ALL_API_PATH;
        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
                + APPLICATION_SHARE_MODE_PARAM;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        responseOfGet.then().log().ifValidationFails().assertThat()
                .body(JSON_PATH_SHARING_POLICY, equalTo(String.valueOf(ALL_EXISTING_AND_FUTURE_ORGS)))
                .body(JSON_PATH_ROLE_SHARING_MODE, equalTo(String.valueOf(ApplicationShareRolePolicy.Mode.ALL)))
                .body(String.format(JSON_PATH_ORGANIZATION_ID, 0), equalTo(organizationId));
    }

    @Test(dependsOnMethods = "testGeneralShareApplicationForExistingAndFutureChildren")
    public void testGeneralShareApplicationForNewOrganizationCreated() throws Exception {

        // Create a new organization.
        String organizationId = orgMgtRestClient.addOrganization(SAMPLE_ORG_NAME_02);
        createdOrganizations.add(organizationId);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
                + APPLICATION_SHARE_MODE_PARAM;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        responseOfGet.then().log().ifValidationFails().assertThat()
                .body(JSON_PATH_SHARING_POLICY, equalTo(String.valueOf(ALL_EXISTING_AND_FUTURE_ORGS)))
                .body(JSON_PATH_ROLE_SHARING_MODE, equalTo(String.valueOf(ApplicationShareRolePolicy.Mode.ALL)))
                .body(String.format(JSON_PATH_ORGANIZATION_ID, 0), equalTo(organizationId));
    }

//    @Test(dependsOnMethods = "testGeneralShareApplicationForNewOrganizationCreated")
//    public void testGeneralShareApplicationForExistingChildrenOnly() throws IOException, InterruptedException {
//
//        String requestBody = readResource(GENERAL_SHARE_WITH_ALL_ROLES_REQUEST);
//        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId)
//                .replace(POLICY_PLACEHOLDER, String.valueOf(ALL_EXISTING_ORGS_ONLY));
//
//        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_SHARE_WITH_ALL_API_PATH;
//        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
//        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);
//
//        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
//                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
//                + APPLICATION_SHARE_MODE_PARAM;
//        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
//        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);
//
//        responseOfGet.then().log().ifValidationFails().assertThat()
//                .body(JSON_PATH_SHARING_POLICY, nullValue())
//                .body(JSON_PATH_ROLE_SHARING_MODE, nullValue());
//    }

    @Test(dependsOnMethods = "testGeneralShareApplicationForNewOrganizationCreated")
    public void testGeneralApplicationUnShare() throws IOException {

        String requestBody = readResource(GENERAL_UNSHARE_REQUEST);
        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId);

        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_UNSHARE_WITH_ALL_API_PATH;
        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);
        responseOfGet.then().log().ifValidationFails().assertThat().body(equalTo("{}"));
    }

    @Test(dependsOnMethods = "testGeneralApplicationUnShare")
    public void testNewOrganizationCreatedIsNotSharedWithoutFuturePolicy() throws Exception {

        // Create a new organization.
        String organizationId = orgMgtRestClient.addOrganization(SAMPLE_ORG_NAME_03);
        createdOrganizations.add(organizationId);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);
        responseOfGet.then().log().ifValidationFails().assertThat().body(equalTo("{}"));
    }

    @Test(dependsOnMethods = "testNewOrganizationCreatedIsNotSharedWithoutFuturePolicy")
    public void testSelectiveShareApplicationForExistingChildrenOnly() throws IOException {

        String requestBody = readResource(SELECTIVE_SHARE_WITH_ALL_ROLES_REQUEST);
        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId)
                .replace(POLICY_PLACEHOLDER, String.valueOf(SELECTED_ORG_ONLY))
                .replace(ORGANIZATION_PLACEHOLDER, String.valueOf(createdOrganizations.get(0)));

        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_SHARE_API_PATH;
        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
                + APPLICATION_SHARE_MODE_PARAM;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        responseOfGet.then().log().ifValidationFails().assertThat()
                .body(JSON_PATH_SHARING_POLICY, nullValue())
                .body(JSON_PATH_ROLE_SHARING_MODE, nullValue())
                .body(String.format(JSON_PATH_ORGANIZATION_ID, 0), equalTo(createdOrganizations.get(0)));
    }

    @Test(dependsOnMethods = "testSelectiveShareApplicationForExistingChildrenOnly")
    public void testSelectiveShareApplicationForExistingAndFutureChildren() throws IOException {

        String requestBody = readResource(SELECTIVE_SHARE_WITH_ALL_ROLES_REQUEST);
        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId)
                .replace(POLICY_PLACEHOLDER, String.valueOf(SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN))
                .replace(ORGANIZATION_PLACEHOLDER, String.valueOf(createdOrganizations.get(0)));

        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_SHARE_WITH_ALL_API_PATH;
        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
                + APPLICATION_SHARE_MODE_PARAM;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        responseOfGet.then().log().ifValidationFails().assertThat()
                .body(String.format(JSON_PATH_ORGANIZATION_SHARING_POLICY, 0),
                        equalTo(String.valueOf(SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN)))
                .body(String.format(JSON_PATH_ORGANIZATION_SHARING_MODE, 0),
                        equalTo(String.valueOf(ApplicationShareRolePolicy.Mode.ALL)))
                .body(String.format(JSON_PATH_ORGANIZATION_ID, 0), equalTo(createdOrganizations.get(0)));
    }

    @Test(dependsOnMethods = "testSelectiveShareApplicationForExistingAndFutureChildren")
    public void testSelectiveShareApplicationForNewOrganizationCreated() throws Exception {

        // Create a new organization.
        String organizationId = orgMgtRestClient.addSubOrganization(SAMPLE_ORG_NAME_04, createdOrganizations.get(0));
        createdOrganizations.add(organizationId);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_SHARE_API_PATH + QUESTION_MARK + APPLICATION_SHARE_ATTRIBUTE_QUERY_PARAM
                + APPLICATION_SHARE_MODE_PARAM;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        responseOfGet.then().log().ifValidationFails().assertThat()
                .body(String.format(JSON_PATH_ORGANIZATION_ID, 0), equalTo(organizationId));
    }

    @Test(dependsOnMethods = "testSelectiveShareApplicationForNewOrganizationCreated")
    public void testSelectiveApplicationUnShare() throws IOException {

        String requestBody = readResource(SELECTIVE_UNSHARE_REQUEST);
        requestBody = requestBody.replace(APP_ID_PLACEHOLDER, createdAppId)
                .replace(ORGANIZATION_PLACEHOLDER, String.valueOf(createdOrganizations.get(3)));

        String applicationSharePostPath = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_UNSHARE_WITH_ALL_API_PATH;
        Response responseOfPost = getResponseOfPost(applicationSharePostPath, requestBody);
        validateHttpStatusCode(responseOfPost, HttpStatus.SC_ACCEPTED);

        String applicationShareGetPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + APPLICATION_UNSHARE_API_PATH;
        Response responseOfGet = getResponseOfGet(applicationShareGetPath);
        validateHttpStatusCode(responseOfGet, HttpStatus.SC_OK);

        //TODO: validate the organization 04 is not in the list
    }
}
