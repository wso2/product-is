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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
    protected static final String APPROVAL_WORKFLOW_NAME = "User Approval Workflow";
    private static final Duration APPROVAL_TASK_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration APPROVAL_TASK_POLL_INTERVAL = Duration.ofSeconds(1);

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

        AtomicInteger lastStatus = new AtomicInteger(-1);
        try {
            return Awaitility.await("approval task assignment")
                    .atMost(APPROVAL_TASK_TIMEOUT)
                    .pollInterval(APPROVAL_TASK_POLL_INTERVAL)
                    .ignoreExceptions()
                    .until(() -> {
                        Response listResp = getResponseOfGetNoFilter(approvalListPath);
                        lastStatus.set(listResp.getStatusCode());
                        if (listResp.getStatusCode() != HttpStatus.SC_OK) {
                            return null;
                        }
                        return listResp.jsonPath().getString("[0].id");
                    }, Objects::nonNull);
        } catch (ConditionTimeoutException e) {
            Assert.fail("Approval task has not been assigned within " + APPROVAL_TASK_TIMEOUT.getSeconds()
                    + " seconds. Last approval task listing status: " + lastStatus.get() + ".", e);
            return null;
        }
    }

    /**
     * Removes the given associations and workflow, then sweeps any leftover associations and workflows named
     * {@link #APPROVAL_WORKFLOW_NAME}. A workflow association left behind keeps engaging the approval flow for
     * every later test in the suite (user creation returns 202 instead of 201), so cleanup must not depend on a
     * single tracked id. Individual deletes are best-effort, since the tests may already have removed the resource
     * (the API answers 400 then); the outcome is verified by listing associations and failing if an approval
     * workflow association still remains.
     */
    protected void deleteWorkflowArtifacts(List<String> associationIds, String workflowId) {

        String previousBasePath = RestAssured.basePath;
        RestAssured.basePath = basePath;
        List<String> failures = new ArrayList<>();
        try {
            for (String associationId : associationIds) {
                getResponseOfDelete(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + associationId);
            }
            if (workflowId != null) {
                getResponseOfDelete(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId);
            }
            cleanupWorkflowByName(APPROVAL_WORKFLOW_NAME);

            Response assocResp = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH);
            if (assocResp.getStatusCode() != HttpStatus.SC_OK) {
                failures.add("Unable to list workflow associations to verify cleanup, status "
                        + assocResp.getStatusCode() + ".");
            } else {
                List<Map<String, Object>> remaining = assocResp.jsonPath().getList("workflowAssociations");
                if (remaining != null) {
                    for (Map<String, Object> assoc : remaining) {
                        if (APPROVAL_WORKFLOW_NAME.equals(assoc.get("workflowName"))) {
                            failures.add("Workflow association " + assoc.get("id") + " was left behind and would "
                                    + "engage the approval flow for later tests.");
                        }
                    }
                }
            }
        } finally {
            RestAssured.basePath = previousBasePath;
        }
        Assert.assertTrue(failures.isEmpty(), "Workflow cleanup failed: " + failures);
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
