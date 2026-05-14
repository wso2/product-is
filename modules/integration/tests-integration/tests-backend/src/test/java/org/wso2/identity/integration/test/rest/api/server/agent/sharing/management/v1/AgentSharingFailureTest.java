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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.agent.sharing.management.v1;

import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Failure-path integration tests for Agent Sharing v1 APIs.
 */
public class AgentSharingFailureTest extends AgentSharingBaseTest {

    private static final String AGENT_ID_PLACEHOLDER = "AGENT_ID";
    private static final String ORG_ID_PLACEHOLDER = "ORG_ID";
    private static final String ROLE_PLACEHOLDER = "ROLE";

    private static final String ROLE_DISPLAY_NAME = "View Numbers";
    private static final String ROLE_AUDIENCE_DISPLAY = "My App";
    private static final String ROLE_AUDIENCE_TYPE = "application";
    private static final String UNSUPPORTED_ATTRIBUTE_VALUE = "users";

    private static final String ERROR_CODE_FIELD = "code";

    private static final String ERROR_EMPTY_AGENT_IDS = "ASM-60013";
    private static final String ERROR_UNSUPPORTED_POLICY = "ASM-60009";
    private static final String ERROR_INVALID_REQUEST_FORMAT = "UE-10000";
    private static final String ERROR_UNSUPPORTED_ATTRIBUTE = "OAS-10030";
    private static final String ERROR_UNSUPPORTED_PATCH_OPERATION = "ASM-60012";
    private static final String ERROR_UNSUPPORTED_PATCH_PATH = "ASM-60007";

    private static final String SHARE_ALL_ORGS_EMPTY_AGENT_IDS_REQUEST =
            "share-agents-with-all-orgs-empty-agent-ids-request-body.json";
    private static final String SHARE_ALL_ORGS_INVALID_POLICY_REQUEST =
            "share-agents-with-all-orgs-invalid-policy-request-body.json";
    private static final String SHARE_ALL_ORGS_EMPTY_ROLE_MODE_REQUEST =
            "share-agents-with-all-orgs-empty-role-mode-request-body.json";
    private static final String PATCH_UNSUPPORTED_OPERATION_REQUEST =
            "patch-agent-sharing-unsupported-operation-request-body.json";
    private static final String PATCH_UNSUPPORTED_PATH_REQUEST =
            "patch-agent-sharing-unsupported-path-request-body.json";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AgentSharingFailureTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testShareAgentsWithAllOrgsBadRequestWhenAgentIdsEmpty() throws IOException {

        String body = readResource(SHARE_ALL_ORGS_EMPTY_AGENT_IDS_REQUEST)
                .replace(ROLE_PLACEHOLDER, createRole(ROLE_DISPLAY_NAME, ROLE_AUDIENCE_DISPLAY, ROLE_AUDIENCE_TYPE));

        getResponseOfPost(AGENT_API_BASE_PATH + SHARE_WITH_ALL_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_EMPTY_AGENT_IDS));
    }

    @Test
    public void testShareAgentsWithAllOrgsBadRequestWhenPolicyInvalid() throws IOException {

        String body = readResource(SHARE_ALL_ORGS_INVALID_POLICY_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, randomUuid())
                .replace(ROLE_PLACEHOLDER, createRole(ROLE_DISPLAY_NAME, ROLE_AUDIENCE_DISPLAY, ROLE_AUDIENCE_TYPE));

        getResponseOfPost(AGENT_API_BASE_PATH + SHARE_WITH_ALL_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_UNSUPPORTED_POLICY));
    }

    @Test
    public void testShareAgentsWithAllOrgsBadRequestWhenRoleAssignmentModeEmpty() throws IOException {

        String body = readResource(SHARE_ALL_ORGS_EMPTY_ROLE_MODE_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, randomUuid())
                .replace(ROLE_PLACEHOLDER, createRole(ROLE_DISPLAY_NAME, ROLE_AUDIENCE_DISPLAY, ROLE_AUDIENCE_TYPE));

        getResponseOfPostNoFilter(AGENT_API_BASE_PATH + SHARE_WITH_ALL_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_INVALID_REQUEST_FORMAT));
    }

    @Test
    public void testGetSharedOrgsOfAgentBadRequestWhenAttributeNotSupported() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(ATTRIBUTES_QUERY_PARAM, UNSUPPORTED_ATTRIBUTE_VALUE);

        getResponseOfGet(AGENT_API_BASE_PATH + "/" + randomUuid() + SHARE_PATH, queryParams)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_UNSUPPORTED_ATTRIBUTE));
    }

    @Test
    public void testPatchAgentSharingBadRequestWhenOperationNotSupported() throws IOException {

        String body = readResource(PATCH_UNSUPPORTED_OPERATION_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, randomUuid())
                .replace(ORG_ID_PLACEHOLDER, randomUuid())
                .replace(ROLE_PLACEHOLDER, createRole(ROLE_DISPLAY_NAME, ROLE_AUDIENCE_DISPLAY, ROLE_AUDIENCE_TYPE));

        getResponseOfPatch(AGENT_API_BASE_PATH + SHARE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_UNSUPPORTED_PATCH_OPERATION));
    }

    @Test
    public void testPatchAgentSharingBadRequestWhenPathNotSupported() throws IOException {

        String body = readResource(PATCH_UNSUPPORTED_PATH_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, randomUuid())
                .replace(ORG_ID_PLACEHOLDER, randomUuid())
                .replace(ROLE_PLACEHOLDER, createRole(ROLE_DISPLAY_NAME, ROLE_AUDIENCE_DISPLAY, ROLE_AUDIENCE_TYPE));

        getResponseOfPatch(AGENT_API_BASE_PATH + SHARE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_CODE_FIELD, equalTo(ERROR_UNSUPPORTED_PATCH_PATH));
    }
}
