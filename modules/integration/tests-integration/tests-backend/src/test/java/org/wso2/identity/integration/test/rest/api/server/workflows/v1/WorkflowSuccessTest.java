package org.wso2.identity.integration.test.rest.api.server.workflows.v1;


import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantResponseModel;
import org.wso2.identity.integration.test.rest.api.server.workflows.v1.model.WorkflowAssociationResponse;
import org.wso2.identity.integration.test.rest.api.server.workflows.v1.model.WorkflowResponse;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for workflow management REST APIs success paths.
 */
public class WorkflowSuccessTest extends WorkflowBaseTest {

    private String workflowId;
    private String workflowAssociationId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public WorkflowSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
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
    public void testGetWorkflow() throws IOException {

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
                .body("engine", equalTo("Simple Workflow Engine"))
                .body("template.name", equalTo("MultiStepApproval"))
                .body(stepBase + "step", equalTo(stepNumber))
                .body(rolesEntityPath, notNullValue())
                .body(usersEntityPath, notNullValue());

        WorkflowResponse workflowResponse = response.getBody().as(WorkflowResponse.class);
        workflowId = workflowResponse.getId();
    }

    @Test(dependsOnMethods = {"testGetWorkflow"})
    public void testGetWorkflows() throws Exception {

        String baseIdentifier = "workflows.find{ it.id == '" + workflowId + "' }.";
        Response response = getResponseOfGet(WORKFLOW_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(baseIdentifier + "id", equalTo(workflowId))
                .body(baseIdentifier + "name", equalTo("User Approval Workflow"))
                .body("description", equalTo("Workflow to approve user role related requests"))
                .body("engine", equalTo("Simple Workflow Engine"))
                .body("template",equalTo("MultiStepApprovalTemplate"));
    }

    @Test(dependsOnMethods = {"testGetWorkflows"})
    public void testUpdateWorkflow() throws Exception {

        String body = readResource("update-workflow.json");
        Response response = getResponseOfPut(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + workflowId, body);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = {"testUpdateWorkflow"})
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

    @Test(description = "Test adding workflow associations.")
    public void testAddWorkflowAssociation() throws IOException {

        String body = readResource("add-workflow-association.json");
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

    @Test(dependsOnMethods = {"testAddWorkflowAssociation"})
    public void testGetWorkflowAssociation() throws IOException {

        Response response =
                getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(workflowId))
                .body("associationName", equalTo("User Registration Workflow Association"))
                .body("operation", equalTo("ADD_USERs"))
                .body("workflowName", notNullValue())
                .body("isEnabled", equalTo(true));

        WorkflowAssociationResponse workflowAssociationResponse =
                response.getBody().as(WorkflowAssociationResponse.class);
        workflowAssociationId = workflowAssociationResponse.getId();
    }

    @Test(dependsOnMethods = {"testGetWorkflowAssociation"})
    public void testGetWorkflowAssociations() throws Exception {

        String baseIdentifier = "workflowAssociations.find{ it.id == '" + workflowAssociationId + "' }.";
        Response response = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(workflowId))
                .body("associationName", equalTo("User Registration Workflow Association"))
                .body("operation", equalTo("ADD_USERs"))
                .body("workflowName", notNullValue())
                .body("isEnabled", equalTo(true));
    }

    @Test(dependsOnMethods = {"testGetWorkflowAssociations"})
    public void testPatchWorkflowAssociations() throws IOException {

        // PATCH: Update associationName
        String body = readResource("patch-association-name.json");
        Response response = getResponseOfPatch(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("associationName", equalTo("Updated Association Name"));

        // PATCH: Update operation
        body = readResource("patch-association-operation.json");
        response = getResponseOfPatch(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("operation", equalTo("DELETE_USER"));

        // PATCH: Update workflowId
        body = readResource("patch-association-workflowId.json");
        response = getResponseOfPatch(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + workflowAssociationId, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("workflowName", notNullValue());
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

}
