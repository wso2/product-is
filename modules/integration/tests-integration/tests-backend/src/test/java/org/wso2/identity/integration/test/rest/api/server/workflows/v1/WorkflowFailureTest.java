package org.wso2.identity.integration.test.rest.api.server.workflows.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

/**
 * Test class for Workflow Management REST APIs failure paths.
 */
public class WorkflowFailureTest extends WorkflowBaseTest{

    private String workflowId;
    private String workflowAssociationId;

    public WorkflowFailureTest() throws Exception  {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInitWithoutTenantQualifiedPath(API_VERSION, swaggerDefinition);
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

    @Test
    public void testGetWorkflowWithInvalidId() {

        Response response = getResponseOfGet(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51001", "random-id");
    }

    @Test
    public void addWorkflowWithInvalidTemplate() throws IOException {

        Response response = getResponseOfPost(WORKFLOW_API_BASE_PATH, readResource("add-workflow-invalid-template" +
                ".json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51003", "SingleStepApprovalTemplate");
    }

    @Test
    public void addWorkflowWithInvalidEngine() throws IOException {

        Response response = getResponseOfPost(WORKFLOW_API_BASE_PATH, readResource("add-workflow-invalid-engine" +
                ".json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51003", "Sample Engine");
    }

    @Test
    public void updateWorkflowWithInvalidWorkflowId() throws IOException {

        String body = readResource("update-workflow.json");
        Response response =
                getResponseOfPut(WORKFLOW_API_BASE_PATH + PATH_SEPARATOR + "random-id" + PATH_SEPARATOR + "random-id", body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51004", "random-id");
    }

//    @Test
//    public void testGetWorkflowsWithInvalidFilterFormat() {
//
//        Response response = getResponseOfGet(WORKFLOW_API_BASE_PATH + "?filter=workflowId eq '102'");
//        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-60018");
//    }

    @Test
    public void testGetWorkflowAssociationWithInvalidId() {

        Response response = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + "random-id");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51002", "random-id");
    }

    @Test
    public void addWorkflowAssociationWithInvalidWorkflow() throws IOException {

        Response response = getResponseOfPost(WORKFLOW_ASSOCIATION_API_BASE_PATH, readResource("add-association" +
                "-invalid-workflow.json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51007", "invalid-id");
    }

    @Test
    public void addWorkflowAssociationWithInvalidOperation() throws IOException {

        Response response = getResponseOfPost(WORKFLOW_ASSOCIATION_API_BASE_PATH, readResource("add-association" +
                "-invalid-operation.json"));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "UE-10000");
    }

    @Test
    public void updateWorkflowAssociationWithInvalidAssociationId() throws IOException {

        String body = readResource("patch-association-name.json");
        Response response =
                getResponseOfPut(WORKFLOW_ASSOCIATION_API_BASE_PATH + PATH_SEPARATOR + "random-id" + PATH_SEPARATOR + "random-id", body);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-51008", "random-id");
    }

    @Test
    public void testGetWorkflowAssociationsInvalidFilterFormat() {

        Response response = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + "?filter=invalid format");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-50027");
    }

    @Test
    public void testGetWorkflowAssociationsUnsupportedFilterAttribute() {

        Response response = getResponseOfGet(WORKFLOW_ASSOCIATION_API_BASE_PATH + "?filter=operation eq ADD_USER");
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "WF-50027");
    }
}
