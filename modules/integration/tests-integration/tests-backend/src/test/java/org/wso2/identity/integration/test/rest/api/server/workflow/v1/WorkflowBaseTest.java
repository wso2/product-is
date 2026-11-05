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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base test class for workflow management REST APIs.
 */
public class WorkflowBaseTest extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "workflow.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.server.workflow.v1";
    public static final String WORKFLOW_API_BASE_PATH = "/workflows";
    public static final String WORKFLOW_ASSOCIATION_API_BASE_PATH = "/workflow-associations";

    public static final String PATH_SEPARATOR = "/";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
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

    /**
     * Waits for an approval task to show up in the given approval task listing and returns its id. The task is
     * created asynchronously after the request is accepted (202), so a single immediate lookup is racy.
     */
    protected String awaitFirstApprovalTaskId(String approvalListPath) {

        try {
            return Awaitility.await("approval task assignment")
                    .atMost(Duration.ofSeconds(60))
                    .pollInterval(Duration.ofSeconds(1))
                    .ignoreExceptions()
                    .until(() -> {
                        Response listResp = getResponseOfGetNoFilter(approvalListPath);
                        return listResp.getStatusCode() == HttpStatus.SC_OK
                                ? listResp.jsonPath().getString("[0].id") : null;
                    }, Objects::nonNull);
        } catch (ConditionTimeoutException e) {
            Assert.fail("Approval task has not been assigned within 60 seconds.", e);
            return null;
        }
    }

    /**
     * Deletes the workflow associations and the workflow a test class created, then checks that none of those
     * associations is left behind. A leaked association with no rule engages the approval flow for every later
     * user operation in the tenant, so later tests get 202 instead of 201/204.
     *
     * This runs from @AfterClass, where RestAssured.basePath is not the workflow API path: testFinish() resets it
     * after every test method, and other test classes interleave with this one and overwrite it. Without restoring
     * it here, the deletes never reach the workflow API.
     *
     * Delete status codes are not asserted: the API refuses to delete a workflow's last association (400), and
     * deleting the workflow cascades its associations, so a later delete of one of them reports it as not found.
     * Only this class's own ids are touched, since other workflow test classes use the same workflow name and may
     * still be running.
     */
    protected void deleteWorkflowArtifacts(List<String> associationIds, String workflowId) {

        String previousBasePath = RestAssured.basePath;
        RestAssured.basePath = basePath;
        try {
            for (String associationId : associationIds) {
                getResponseOfDelete(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + associationId);
            }
            if (workflowId != null) {
                getResponseOfDelete(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId);
            }
            for (String associationId : associationIds) {
                Response assocResp = getResponseOfGetNoFilter(
                        WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + associationId);
                Assert.assertNotEquals(assocResp.getStatusCode(), HttpStatus.SC_OK, "Workflow association "
                        + associationId + " was left behind and would engage the approval flow for later tests.");
            }
        } finally {
            RestAssured.basePath = previousBasePath;
        }
    }

    /**
     * Deletes any workflow associations and workflows matching the given name.
     * Used at test setup to handle dirty state left by previous failed runs.
     */
    protected void cleanupWorkflowByName(String name) {

        RestAssured.basePath = basePath;
        Response assocResp = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH);
        if (assocResp.getStatusCode() == HttpStatus.SC_OK) {
            List<Map<String, Object>> assocs = assocResp.jsonPath().getList("workflowAssociations");
            if (assocs != null) {
                for (Map<String, Object> assoc : assocs) {
                    if (name.equals(assoc.get("workflowName"))) {
                        getResponseOfDelete(
                                WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + assoc.get("id"));
                    }
                }
            }
        }
        Response workflowsResp = getResponseOfGet(WORKFLOW_API_BASE_PATH);
        if (workflowsResp.getStatusCode() == HttpStatus.SC_OK) {
            List<Map<String, Object>> workflows = workflowsResp.jsonPath().getList("workflows");
            if (workflows != null) {
                for (Map<String, Object> workflow : workflows) {
                    if (name.equals(workflow.get("name"))) {
                        getResponseOfDelete(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflow.get("id"));
                    }
                }
            }
        }
    }
}
