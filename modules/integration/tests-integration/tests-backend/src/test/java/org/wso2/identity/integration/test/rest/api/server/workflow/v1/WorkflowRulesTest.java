/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.workflow.v1;

import io.restassured.RestAssured;
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
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject.MemberItem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Test class for workflow rule evaluation in workflow association REST APIs.
 * Tests that a DELETE_USER workflow association with a rule on {@code user.groups}
 * correctly engages or skips the approval workflow based on the user's group membership.
 *
 * <p>Setup: Two users are created, one assigned to a test group. A DELETE_USER
 * association with rule {@code user.groups contains <groupId>} is then created.
 * Deleting the group member triggers the workflow (202), while deleting the
 * non-member bypasses it (204).</p>
 */
public class WorkflowRulesTest extends WorkflowBaseTest {

    private SCIM2RestClient scim2RestClient;
    private String workflowId;
    private String workflowAssociationId;

    private String groupMemberUserName;
    private String groupMemberUserId;
    private String nonGroupMemberUserName;
    private String nonGroupMemberUserId;
    private String testGroupId;

    private static final String APPROVAL_API_PATH = "/api/users/v2/me/approval-tasks";
    private static final String USER_SEARCH_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";
    private static final String TEST_GROUP_NAME = "workflow_rules_test_group";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WorkflowRulesTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Override
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        RestAssured.basePath = basePath;
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

        // Create the workflow.
        String body = readResource("add-workflow.json");
        Response response = getResponseOfPost(WORKFLOW_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        workflowId = extractIdFromLocationHeader(response);

        String adminRoleId = scim2RestClient.getRoleIdByName("admin");
        String updateBody = readResource("update-workflow.json");
        updateBody = updateBody.replace("{role-id}", adminRoleId);
        getResponseOfPut(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId, updateBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Create two users.
        groupMemberUserName = "wf_rule_member_user";
        UserObject memberUser = new UserObject().userName(groupMemberUserName).password("Test@12345");
        SCIM2RestClient.CreateUserResponse memberResp = scim2RestClient.attemptUserCreation(memberUser);
        assertEquals(memberResp.getStatusCode(), HttpStatus.SC_CREATED,
                "User creation should succeed without any workflow association.");
        groupMemberUserId = memberResp.getUserId();
        assertNotNull(groupMemberUserId, "Group member user ID should not be null.");

        nonGroupMemberUserName = "wf_rule_non_member_user";
        UserObject nonMemberUser = new UserObject().userName(nonGroupMemberUserName).password("Test@12345");
        SCIM2RestClient.CreateUserResponse nonMemberResp = scim2RestClient.attemptUserCreation(nonMemberUser);
        assertEquals(nonMemberResp.getStatusCode(), HttpStatus.SC_CREATED,
                "User creation should succeed without any workflow association.");
        nonGroupMemberUserId = nonMemberResp.getUserId();
        assertNotNull(nonGroupMemberUserId, "Non-group member user ID should not be null.");

        GroupRequestObject group = new GroupRequestObject()
                .displayName(TEST_GROUP_NAME)
                .addMember(new MemberItem().value(groupMemberUserId).display(groupMemberUserName));
        testGroupId = scim2RestClient.createGroup(group);
        assertNotNull(testGroupId, "Test group ID should not be null.");

        // Create the DELETE_USER association.
        addWorkflowAssociationWithRule("DELETE_USER", "user.groups", "contains", testGroupId);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {

        if (workflowAssociationId != null) {
            getResponseOfDelete(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId);
        }
        if (workflowId != null) {
            getResponseOfDelete(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId);
        }
        if (testGroupId != null) {
            scim2RestClient.deleteGroup(testGroupId);
        }
        if (groupMemberUserId != null) {
            scim2RestClient.attemptUserDelete(groupMemberUserId);
        }
        if (nonGroupMemberUserId != null) {
            scim2RestClient.deleteUser(nonGroupMemberUserId);
        }
    }

    @Test(description = "Deleting a user who belongs to the rule's group should trigger the approval workflow.")
    public void testDeleteGroupMember_WorkflowEngages() throws Exception {

        // Attempt to delete the user who is a member of the test group.
        int statusCode = scim2RestClient.attemptUserDelete(groupMemberUserId);
        assertEquals(statusCode, HttpStatus.SC_ACCEPTED,
                "Delete of a group member should be pending approval.");
        
        assertUserExists(groupMemberUserName, true);
        approveFirstPendingTask();
        assertUserExists(groupMemberUserName, false);
        groupMemberUserId = null;
    }

    @Test(dependsOnMethods = {"testDeleteGroupMember_WorkflowEngages"},
          description = "Deleting a user who does not belong to the rule's group should bypass the workflow.")
    public void testDeleteNonGroupMember_WorkflowSkipped() throws Exception {

        // Attempt to delete the user who is not a member of the test group.
        int statusCode = scim2RestClient.attemptUserDelete(nonGroupMemberUserId);
        assertEquals(statusCode, HttpStatus.SC_NO_CONTENT,
                "Delete of a non-group member should succeed immediately.");

        // User should no longer exist.
        assertUserExists(nonGroupMemberUserName, false);
        nonGroupMemberUserId = null;
    }

    /**
     * Creates a workflow association with a single-expression rule.
     */
    private void addWorkflowAssociationWithRule(String operation, String field, String operator, String value)
            throws IOException {

        String body = readResource("add-workflow-association-with-rule.json");
        body = body.replace("{workflowId}", workflowId);
        body = body.replace("{operation}", operation);
        body = body.replace("{field}", field);
        body = body.replace("{operator}", operator);
        body = body.replace("{value}", value);
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

    /**
     * Extracts the resource ID from the Location header returned by a POST.
     */
    private String extractIdFromLocationHeader(Response response) {

        String location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location, "Location header should not be null.");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    /**
     * Approves the first pending approval task for the current admin user.
     */
    private void approveFirstPendingTask() {

        Response listResp = getResponseOfGetNoFilter(APPROVAL_API_PATH);
        listResp.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);

        String taskId = listResp.jsonPath().getString("[0].id");
        assertNotNull(taskId, "Approval task has not get assigned.");

        String endpoint = String.format(APPROVAL_API_PATH + "/%s/state", taskId);
        String approvePayload = "{\n  \"action\":\"APPROVE\"\n}";
        getResponseOfPutWithNoFilter(endpoint, approvePayload).then().statusCode(HttpStatus.SC_OK);
    }

    /**
     * Asserts whether a user exists via a SCIM2 search.
     */
    private void assertUserExists(String userName, boolean shouldExist) throws Exception {

        JSONObject searchResp = scim2RestClient.searchUser(buildUserSearchRequest(userName));
        Object resources = searchResp.get("Resources");
        if (shouldExist) {
            assertNotNull(resources, "Expected user '" + userName + "' to exist but search returned no resources.");
        } else {
            assertNull(resources, "Expected user '" + userName + "' to NOT exist but search returned resources.");
        }
    }

    /**
     * Builds a SCIM2 search request JSON for the given username.
     */
    private String buildUserSearchRequest(String userName) {

        JSONObject req = new JSONObject();
        req.put("schemas", new JSONArray() {{ add(USER_SEARCH_SCHEMA); }});
        req.put("attributes", new JSONArray() {{ add("id"); add("userName"); }});
        req.put("filter", "userName eq \"" + userName + "\"");
        return req.toString();
    }
}
