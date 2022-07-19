/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.approval.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.approval.common.UserApprovalTestBase;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;

public class UserMeApprovalTest extends UserApprovalTestBase {

    private static final Log log = LogFactory.getLog(UserMeApprovalTest.class);

    private static final String API_DEFINITION_NAME = "approval.yaml";
    private static final String API_VERSION = "v1";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.approval.v1";
    private static final String ME_APPROVAL_TASKS_ENDPOINT_URI = "/me/approval-tasks";
    private static final String ME_APPROVAL_TASKS_FILTER_ENDPOINT_URI = "/me/approval-tasks?status=%s";
    private static final String ME_APPROVAL_TASK_ENDPOINT_URI = ME_APPROVAL_TASKS_ENDPOINT_URI + "/%s";
    private static final String ME_APPROVAL_TASK_STATE_ENDPOINT_URI = ME_APPROVAL_TASKS_ENDPOINT_URI + "/%s/state";
    private static final String TEST_WORKFLOW_ADD_USER_FOR_REST_TASK = addUserWorkflowName + "Task";
    private static final String JSON_PATH_MATCHING_REST_API_TEST_APPROVAL_TASK = "findAll{ it.presentationName == '"
            + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }";
    private static final int MAX_WAIT_ITERATIONS_TILL_WORKFLOW_DEPLOYMENT = 15;

    private static String swaggerDefinition;
    private String taskIdToApprove;
    private String taskIdToDeny;
    private String taskIdToKeep;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public UserMeApprovalTest(TestUserMode userMode) throws Exception {

        super(userMode);
        setUpWorkFlowAssociation();
        waitForWorkflowToDeploy();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() throws Exception {

        super.conclude();
        cleanUPWorkFlows();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testListTasksWhenEmpty() {

        getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("findAll{ it.presentationName == '" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }.size()",
                        is(0));
    }

    @Test(dependsOnMethods = {"testListTasksWhenEmpty"})
    public void testListTasksWhenAvailable() throws Exception {

        addAssociationForMatch();
        for (int i = 1; i <= MAX_WAIT_ITERATIONS_TILL_WORKFLOW_DEPLOYMENT; i++) {
            int numOfTasks = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI)
                    .then()
                    .extract()
                    .path("findAll{ it.presentationName == '"
                            + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }.size()");
            if (numOfTasks == 3) {
                break;
            }

            // Wait till workflow is applied.
            log.info("Waiting 5 seconds till the workflow is applied for association, iteration " + i + " of 3");
            Thread.sleep(5000);
        }

        getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("findAll{ it.presentationName == " +
                        "'" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }.size()", is(3))
                .body("findAll{ it.presentationName == " +
                        "'" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }[0].name", containsString(addUserWorkflowName))
                .body("findAll{ it.presentationName == '" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK +
                        "' }[0].presentationName", containsString(addUserWorkflowName))
                .body("findAll{ it.presentationName == '" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK +
                        "' }[0].status", is(STATE.READY.name()));
    }

    @Test(dependsOnMethods = {"testListTasksWhenAvailable"})
    public void testViewTaskWhenAvailable() throws Exception {

        Response response = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI);
        response
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("findAll{ it.presentationName == " +
                        "'" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }.size()", is(3));

        //Extract a taskId
        String taskId = response.then().extract().path("findAll{ it.presentationName == " +
                "'" + TEST_WORKFLOW_ADD_USER_FOR_REST_TASK + "' }[0].id");
        response = getResponseOfGet(String.format(ME_APPROVAL_TASK_ENDPOINT_URI, taskId));
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("id", is(taskId))
                .body("subject", notNullValue());
    }


    @Test(dependsOnMethods = {"testViewTaskWhenAvailable"})
    public void testClaimTask() throws Exception {

        Response response = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI);
        response
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body(JSON_PATH_MATCHING_REST_API_TEST_APPROVAL_TASK + ".size()",
                        is(3));

        // Populate task ids for future reference
        taskIdToApprove = response.then().extract().path(JSON_PATH_MATCHING_REST_API_TEST_APPROVAL_TASK + "[0].id");
        taskIdToDeny = response.then().extract().path(JSON_PATH_MATCHING_REST_API_TEST_APPROVAL_TASK + "[1].id");
        taskIdToKeep = response.then().extract().path(JSON_PATH_MATCHING_REST_API_TEST_APPROVAL_TASK + "[2].id");

        String payLoad = getRequestPayLoadForClaimTask();

        // Claim task One before approve
        String endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToApprove);
        response = getResponseOfPut(endpointUri, payLoad);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Claim task two before approve
        endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToDeny);
        response = getResponseOfPut(endpointUri, payLoad);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        response = getResponseOfGet(String.format(ME_APPROVAL_TASKS_ENDPOINT_URI, taskIdToApprove));
        validateTaskState(response, taskIdToApprove, STATE.RESERVED);
        validateTaskState(response, taskIdToDeny, STATE.RESERVED);
        validateTaskState(response, taskIdToKeep, STATE.READY);
    }

    @Test(dependsOnMethods = {"testClaimTask"})
    public void testApproveTask() throws Exception {

        //Approve task identified by taskIdToApprove
        String payLoad = getRequestPayloadForApproveTask();
        String endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToApprove);
        Response response = getResponseOfPut(endpointUri, payLoad);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Validate state of all pending tasks after approving taskIdToApprove
        response = getResponseOfGet(String.format(ME_APPROVAL_TASKS_ENDPOINT_URI, taskIdToApprove));
        validateTaskState(response, taskIdToApprove, STATE.COMPLETED);
        validateTaskState(response, taskIdToDeny, STATE.RESERVED);
        validateTaskState(response, taskIdToKeep, STATE.READY);
    }

    @Test(dependsOnMethods = {"testApproveTask"})
    public void testListWithFilter() {

        //TODO enable this test
//        // Filter COMPLETED tasks
//        Response response = getResponseOfGet(String.format(ME_APPROVAL_TASKS_FILTER_ENDPOINT_URI, STATE.COMPLETED.toString()));
//        validateTaskListFilterResponse(response, taskIdToApprove, 1, STATE.COMPLETED);
//
//        // Filter RESERVED tasks
//        response = getResponseOfGet(String.format(ME_APPROVAL_TASKS_FILTER_ENDPOINT_URI + STATE.RESERVED));
//        validateTaskListFilterResponse(response, taskIdToDeny, 1, STATE.RESERVED);
//
//        // Filter READY tasks
//        response = getResponseOfGet(String.format(ME_APPROVAL_TASKS_FILTER_ENDPOINT_URI + STATE.READY));
//        validateTaskListFilterResponse(response, taskIdToKeep, 1, STATE.READY);
    }

    @Test(dependsOnMethods = {"testListWithFilter"})
    public void testDenyTask() throws Exception {

        //Reject task identified by taskIdToDeny
        String payLoad = getRequestPayloadForRejectTask();
        String endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToDeny);
        Response response = getResponseOfPut(endpointUri, payLoad);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Validate state of all pending tasks after approving taskIdToApprove
        response = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI);
        validateTaskState(response, taskIdToApprove, STATE.COMPLETED);
        validateTaskState(response, taskIdToDeny, STATE.COMPLETED);
        validateTaskState(response, taskIdToKeep, STATE.READY);

        // validate state after completion of approval task
        response = getResponseOfGet(String.format(ME_APPROVAL_TASK_ENDPOINT_URI, taskIdToApprove));
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("approvalStatus", is(APPROVAL_STATE.APPROVED.name()));

        response = getResponseOfGet(String.format(ME_APPROVAL_TASK_ENDPOINT_URI, taskIdToDeny));
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("approvalStatus", is(APPROVAL_STATE.REJECTED.name()));
    }

    @Test(dependsOnMethods = {"testDenyTask"})
    public void testClaimAndRelease() throws Exception {

        // Claim the task identified by taskIdToKeep
        String payLoad = getRequestPayLoadForClaimTask();
        String endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToKeep);
        getResponseOfPut(endpointUri, payLoad)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Validate state of all the approval tasks after claim
        Response response = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI);
        validateTaskState(response, taskIdToApprove, STATE.COMPLETED);
        validateTaskState(response, taskIdToDeny, STATE.COMPLETED);
        validateTaskState(response, taskIdToKeep, STATE.RESERVED);

        // Release the task identified by taskIdToKeep
        payLoad = getPayLoadForReleaseTask();
        endpointUri = String.format(ME_APPROVAL_TASK_STATE_ENDPOINT_URI, taskIdToKeep);
        getResponseOfPut(endpointUri, payLoad)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // Validate state of all the approval tasks after claim
        response = getResponseOfGet(ME_APPROVAL_TASKS_ENDPOINT_URI);
        validateTaskState(response, taskIdToApprove, STATE.COMPLETED);
        validateTaskState(response, taskIdToDeny, STATE.COMPLETED);
        validateTaskState(response, taskIdToKeep, STATE.READY);
    }

    private void validateTaskState(Response response, String taskId, STATE expectedState) {

        validateResponseElement(response, "find{ it.id == '" + taskId + "' }.status", is(expectedState.toString()));
    }

    private String getPayLoad(APPROVAL_ACTION action) throws IOException {
        return String.format(readResource("approval-state-template.json"), action);
    }

    private String getRequestPayloadForRejectTask() throws IOException {
        return getPayLoad(APPROVAL_ACTION.REJECT);
    }

    private String getRequestPayLoadForClaimTask() throws IOException {
        return getPayLoad(APPROVAL_ACTION.CLAIM);
    }

    private String getRequestPayloadForApproveTask() throws IOException {
        return getPayLoad(APPROVAL_ACTION.APPROVE);
    }

    private String getPayLoadForReleaseTask() throws IOException {
        return getPayLoad(APPROVAL_ACTION.RELEASE);
    }

    private void validateTaskListFilterResponse(Response response, String taskId, int size, STATE state) {

        validateResponseElement(response, "size()", is(size));
        validateResponseElement(response, "id", is(taskId));
        validateResponseElement(response, "state", is(state.toString()));
    }
}
