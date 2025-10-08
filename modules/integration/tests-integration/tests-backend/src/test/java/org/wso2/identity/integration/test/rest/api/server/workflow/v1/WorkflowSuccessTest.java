/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.workflow.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.workflow.v1.model.WorkflowAssociationResponse;
import org.wso2.identity.integration.test.rest.api.server.workflow.v1.model.WorkflowResponse;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class for workflow management REST APIs success paths.
 */
public class WorkflowSuccessTest extends WorkflowBaseTest {

    private String workflowId;
    private String workflowAssociationId;
    private SCIM2RestClient scim2RestClient;
    private String createdUserName;
    private String createdUserId;
    private String createdRoleName;
    private String createdRoleId;

    private static final String APPROVAL_API_PATH = "/api/users/v2/me/approval-tasks";
    private static final String USER_SEARCH_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";


    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WorkflowSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        // Prepare SCIM2 client for user operations used in approval flow test.
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {

        if (createdRoleId != null) {
            scim2RestClient.deleteV2Role(createdRoleId);
        }
        if (createdUserId != null) {
            scim2RestClient.deleteUser(createdUserId);
        }
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][] {
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test(description = "Test adding workflows.")
    public void testAddWorkflow() throws IOException {

        String body = readResource("add-workflow.json");
        Response response = getResponseOfPost(WORKFLOW_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        workflowId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(workflowId);
    }

    @Test(dependsOnMethods = {"testAddWorkflow"})
    public void testGetWorkflow() {

        Response response = getResponseOfGet(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId);
        int stepNumber = 1;

        String stepBase = "template.steps.find{ it.step == " + stepNumber + " }.";
        String rolesEntityPath = stepBase + "options.find{ it.entity == 'roles' }.values";
        String usersEntityPath = stepBase + "options.find{ it.entity == 'users' }.values";

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(workflowId))
                .body("name", equalTo("User Approval Workflow"))
                .body("description", equalTo("Workflow to approve user role related requests"))
                .body("engine", equalTo("WorkflowEngine"))
                .body("template.name", equalTo("MultiStepApprovalTemplate"))
                .body(stepBase + "step", equalTo(stepNumber))
                .body(rolesEntityPath, notNullValue())
                .body(usersEntityPath, notNullValue());

        WorkflowResponse workflowResponse = response.getBody().as(WorkflowResponse.class);
        workflowId = workflowResponse.getId();
    }

    @Test(dependsOnMethods = {"testGetWorkflow"})
    public void testGetWorkflows() {

        String baseIdentifier = "workflows.find{ it.id == '" + workflowId + "' }.";
        Response response = getResponseOfGet(WORKFLOW_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "id", equalTo(workflowId))
                .body(baseIdentifier + "name", equalTo("User Approval Workflow"))
                .body(baseIdentifier + "description", equalTo("Workflow to approve user role related requests"))
                .body(baseIdentifier + "engine", equalTo("WorkflowEngine"))
                .body(baseIdentifier + "template", equalTo("MultiStepApprovalTemplate"));
    }

    @Test(dependsOnMethods = {"testGetWorkflows"})
    public void testUpdateWorkflow() throws Exception {

        String body = readResource("update-workflow.json");
        String adminRoleId = scim2RestClient.getRoleIdByName("admin");
        body = body.replace("{role-id}", adminRoleId);
        Response response = getResponseOfPut(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateWorkflow"})
    public void testAddWorkflowAssociations() throws IOException {

        addWorkflowAssociation("ADD_USER");
    }

    @Test(dependsOnMethods = {"testAddWorkflowAssociations"})
    public void testCreateUserPendingApprovalAndApprove() throws Exception {

        createdUserName = "John@wso2.com+" + new Random().nextInt(10);

        UserObject user = new UserObject().userName(createdUserName).password("Test@12345");
        SCIM2RestClient.CreateUserResponse createResp = scim2RestClient.attemptUserCreation(user);
        assertEquals(createResp.getStatusCode(), HttpStatus.SC_ACCEPTED,
                "User create should be accepted pending approval");

        approveFirstPendingTask();

        // Return user id after approval via search.
        createdUserId = searchUserIdByUsername(createdUserName);
        assertNotNull(createdUserId, "Approved user id not found by username");
    }

    @Test(dependsOnMethods = {"testCreateUserPendingApprovalAndApprove"})
    public void testCreateRolePendingApprovalAndApprove() throws Exception {

        // 1) Add association for ADD_ROLE.
        addWorkflowAssociation("ADD_ROLE");

        createdRoleName = "Role-" + new Random().nextInt(10);
        RoleRequestObject roleReq = new RoleRequestObject().displayName(createdRoleName);

        SCIM2RestClient.RoleCreateResponse roleResp = scim2RestClient.attemptRoleV2Creation(roleReq);
        assertEquals(roleResp.getStatusCode(), HttpStatus.SC_ACCEPTED,
                        "Role create should be accepted pending approval");

        // Approve creation and verify the role exists.
        approveFirstPendingTask();
        createdRoleId = scim2RestClient.getRoleIdByName(createdRoleName);
        assertNotNull(createdRoleId, "Approved role id not found by name");
    }

    @Test(dependsOnMethods = {"testCreateRolePendingApprovalAndApprove"})
    public void testAssignUsersToRolePendingApprovalAndApprove() throws Exception {

        addWorkflowAssociation("UPDATE_ROLES_OF_USERS");

        // Ensure a real user exists to assign: create and approve.
        String userId = searchUserIdByUsername(createdUserName);

        // Prepare PATCH op to add the user to the role.
        Map<String, Object> value = new HashMap<>();
        Map<String, String> userEntry = new HashMap<>();
        userEntry.put("value", userId);
        List<Map<String, String>> users = new ArrayList<>();
        users.add(userEntry);
        value.put("users", users);

        Map<String, Object> op = new HashMap<>();
        op.put("op", "add");
        op.put("value", value);

        PatchOperationRequestObject patch = new PatchOperationRequestObject().addOperations(op);

        int status = scim2RestClient.attemptUpdateUsersOfRoleV2(createdRoleId, patch);
        assertEquals(status, HttpStatus.SC_ACCEPTED,
                        "Role users update should be accepted pending approval");

        // Approve and verify membership.
        approveFirstPendingTask();
        JSONObject roleJson = scim2RestClient.getRole(createdRoleId);
        JSONArray roleUsers = (JSONArray) roleJson.get("users");
        assertNotNull(roleUsers, "Users array not returned for role");
        boolean contains = false;
        for (Object roleUser : roleUsers) {
            JSONObject userObject = (JSONObject) roleUser;
            if (userObject.get("value") != null && userObject.get("value").equals(userId)) {
                contains = true;
                break;
            }
        }
        assertTrue(contains, "Assigned user id not found in role users after approval");
    }

    @Test(dependsOnMethods = {"testAssignUsersToRolePendingApprovalAndApprove"})
    public void testDeleteUserPendingApprovalAndApprove() throws Exception {

        // 1) Add association for DELETE_USER.
        addWorkflowAssociation("DELETE_USER");
        int statusCode = scim2RestClient.attemptUserDelete(createdUserId);
        assertEquals(statusCode, HttpStatus.SC_ACCEPTED, "User delete should be accepted pending approval");

        // 2) User should still be present before approval.
        assertUserExists(createdUserName, true);

        // 3) Approve deletion and verify user is removed.
        approveFirstPendingTask();
        assertUserExists(createdUserName, false);
        createdUserId = null; // Clear user ID after deletion approval.
    }

    @Test(dependsOnMethods = {"testAssignUsersToRolePendingApprovalAndApprove"})
    public void testGetWorkflowAssociation() {

        Response response =
                getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(workflowAssociationId))
                .body("associationName", equalTo("User Registration Workflow Association"))
                .body("operation", equalTo("DELETE_USER"))
                .body("workflowName", notNullValue())
                .body("isEnabled", equalTo(true));

        WorkflowAssociationResponse workflowAssociationResponse =
                response.getBody().as(WorkflowAssociationResponse.class);
        workflowAssociationId = workflowAssociationResponse.getId();
    }

    @Test(dependsOnMethods = {"testGetWorkflowAssociation"})
    public void testGetWorkflowAssociations() {

        String baseIdentifier = "workflowAssociations.find{ it.id == '" + workflowAssociationId + "' }.";
        Response response = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "id", equalTo(workflowAssociationId))
                .body(baseIdentifier + "associationName", equalTo("User Registration Workflow Association"))
                .body(baseIdentifier + "operation", equalTo("DELETE_USER"))
                .body(baseIdentifier + "workflowName", notNullValue())
                .body(baseIdentifier + "isEnabled", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetWorkflowAssociations"})
    public void testPatchWorkflowAssociations() throws IOException {

        // PATCH: Update associationName.
        String body = readResource("patch-association-name.json");
        Response response =
                getResponseOfPatch(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("associationName", equalTo("User Deletion Workflow Association"));

        // PATCH: Update operation.
        body = readResource("patch-association-operation.json");
        response =
                getResponseOfPatch(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("operation", equalTo("DELETE_USER"));
    }

    @Test(dependsOnMethods = {"testPatchWorkflowAssociations"})
    public void testDeleteWorkflowAssociation() {

        getResponseOfDelete(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = {"testDeleteWorkflowAssociation"})
    public void testDeleteWorkflow() {

        getResponseOfDelete(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void assertUserExists(String userName, boolean shouldExist) throws Exception {

        JSONObject searchResp = scim2RestClient.searchUser(buildUserSearchRequest(userName));
        Object resources = searchResp.get("Resources");
        if (shouldExist) {
            assertNotNull(resources, "User search response does not contain resources.");
        } else {
            assertNull(resources, "User search response should not contain resources after deletion approval.");
        }
    }

    private String searchUserIdByUsername(String userName) throws Exception {

        JSONObject filterResponse = scim2RestClient.searchUser(buildUserSearchRequest(userName));
        if (filterResponse.get("Resources") == null) {
            return null;
        }
        org.json.simple.JSONArray resources = (org.json.simple.JSONArray) filterResponse.get("Resources");
        if (resources.isEmpty()) {
            return null;
        }
        JSONObject userResource = (JSONObject) resources.get(0);
        return userResource.get("id").toString();
    }

    private String buildUserSearchRequest(String userName) {

        try {
            JSONObject req = new JSONObject();
            req.put("schemas", new JSONArray() {{ add(USER_SEARCH_SCHEMA); }});
            req.put("attributes", new JSONArray() {{ add("id"); add("userName"); }});
            req.put("filter", "userName eq \"" + userName + "\"");
            return req.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SCIM search request", e);
        }
    }

    private void approveFirstPendingTask() {

        Response listResp = getResponseOfGetNoFilter(APPROVAL_API_PATH);
        listResp.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);

        String taskId = listResp.jsonPath().getString("[0].id");
        assertNotNull(taskId, "Approval task has not get assigned.");

        String endpoint = String.format(APPROVAL_API_PATH + "/%s/state", taskId);
        String approvePayload = "{\n  \"action\":\"APPROVE\"\n}";
        getResponseOfPutWithNoFilter(endpoint, approvePayload).then().statusCode(HttpStatus.SC_OK);
    }

    private void addWorkflowAssociation(String operation) throws IOException {

        String body = readResource("add-workflow-association.json");
        body = body.replace("{workflowId}", workflowId);
        body = body.replace("{operation}", operation);
        Response response = getResponseOfPost(WORKFLOW_ASSOCIATION_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        workflowAssociationId = location.substring(location.lastIndexOf("/") + 1);
        assertNotNull(workflowAssociationId);
    }
}
