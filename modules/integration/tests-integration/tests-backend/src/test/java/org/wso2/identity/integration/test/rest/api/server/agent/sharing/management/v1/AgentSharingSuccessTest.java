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
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Success-path integration tests for Agent Sharing v1 APIs.
 */
public class AgentSharingSuccessTest extends AgentSharingBaseTest {

    private static final String ORG_ADMIN_ROLE_NAME = "Org Admin";
    private static final String ORG_VIEWER_ROLE_NAME = "Org Viewer";
    private static final String GLOBAL_ORG_VIEWER_ROLE_NAME = "Global Org Viewer";
    private static final String AUDIENCE_DISPLAY_NAME = "My Org";
    private static final String AUDIENCE_TYPE_ORGANIZATION = "organization";

    private static final String PROCESSING_STATUS_FIELD = "status";
    private static final String PROCESSING_STATUS_VALUE = "Processing";
    private static final String DETAILS_FIELD = "details";

    private static final String SHARE_AGENTS_WITH_SELECTED_ORGS_REQUEST =
            "share-agents-with-selected-organizations-request-body.json";
    private static final String PATCH_AGENT_SHARING_REQUEST = "patch-agent-sharing-request-body.json";
    private static final String SHARE_AGENTS_WITH_ALL_ORGS_REQUEST =
            "share-agents-with-all-organizations-request-body.json";
    private static final String UNSHARE_AGENTS_FROM_SELECTED_ORGS_REQUEST =
            "unshare-agents-from-selected-organizations-request-body.json";
    private static final String UNSHARE_AGENTS_FROM_ALL_ORGS_REQUEST =
            "unshare-agents-from-all-organizations-request-body.json";

    private static final String TEST_AGENT_DISPLAY_NAME = "Integration Test Agent";
    private static final String TEST_ORG_NAME = "IntegrationTestOrg";

    private String agentId;
    private String orgId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public AgentSharingSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON)));
        agentId = scim2RestClient.createAgent(TEST_AGENT_DISPLAY_NAME);
        orgId = orgMgtRestClient.addOrganization(TEST_ORG_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        try {
            if (agentId != null) {
                scim2RestClient.deleteAgent(agentId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete agent during cleanup.", e);
        }
        try {
            if (orgId != null) {
                orgMgtRestClient.deleteOrganization(orgId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete organization during cleanup.", e);
        }
        try {
            if (scim2RestClient != null) {
                scim2RestClient.closeHttpClient();
            }
        } catch (Exception e) {
            log.warn("Failed to close SCIM2 REST client during cleanup.", e);
        }
        try {
            if (orgMgtRestClient != null) {
                orgMgtRestClient.closeHttpClient();
            }
        } catch (Exception e) {
            log.warn("Failed to close OrgMgt REST client during cleanup.", e);
        }
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
    public void testShareAgentsWithSelectedOrganizations() throws IOException {

        String body = readResource(SHARE_AGENTS_WITH_SELECTED_ORGS_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, agentId)
                .replace(ORG_ID_PLACEHOLDER, orgId)
                .replace(ROLE_PLACEHOLDER,
                        createRole(ORG_ADMIN_ROLE_NAME, AUDIENCE_DISPLAY_NAME, AUDIENCE_TYPE_ORGANIZATION));

        getResponseOfPost(AGENT_API_BASE_PATH + SHARE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(PROCESSING_STATUS_FIELD, equalTo(PROCESSING_STATUS_VALUE))
                .body(DETAILS_FIELD, notNullValue());

        awaitAgentSharedToOrg(agentId, orgId);
    }

    @Test(dependsOnMethods = "testShareAgentsWithSelectedOrganizations")
    public void testPatchAgentSharing() throws IOException {

        String body = readResource(PATCH_AGENT_SHARING_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, agentId)
                .replace(ORG_ID_PLACEHOLDER, orgId)
                .replace(ROLE_PLACEHOLDER,
                        createRole(ORG_VIEWER_ROLE_NAME, AUDIENCE_DISPLAY_NAME, AUDIENCE_TYPE_ORGANIZATION));

        getResponseOfPatch(AGENT_API_BASE_PATH + SHARE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(PROCESSING_STATUS_FIELD, equalTo(PROCESSING_STATUS_VALUE))
                .body(DETAILS_FIELD, notNullValue());

        awaitAgentSharedToOrg(agentId, orgId);
    }

    @Test(dependsOnMethods = "testPatchAgentSharing")
    public void testUnshareAgentsFromSelectedOrganizations() throws IOException {

        String body = readResource(UNSHARE_AGENTS_FROM_SELECTED_ORGS_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, agentId)
                .replace(ORG_ID_PLACEHOLDER, orgId);

        getResponseOfPost(AGENT_API_BASE_PATH + UNSHARE_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(PROCESSING_STATUS_FIELD, equalTo(PROCESSING_STATUS_VALUE))
                .body(DETAILS_FIELD, notNullValue());

        awaitAgentFullyUnshared(agentId);
    }

    @Test(dependsOnMethods = "testUnshareAgentsFromSelectedOrganizations")
    public void testShareAgentsWithAllOrganizations() throws IOException {

        String body = readResource(SHARE_AGENTS_WITH_ALL_ORGS_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, agentId)
                .replace(ROLE_PLACEHOLDER,
                        createRole(GLOBAL_ORG_VIEWER_ROLE_NAME, AUDIENCE_DISPLAY_NAME, AUDIENCE_TYPE_ORGANIZATION));

        getResponseOfPost(AGENT_API_BASE_PATH + SHARE_WITH_ALL_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(PROCESSING_STATUS_FIELD, equalTo(PROCESSING_STATUS_VALUE))
                .body(DETAILS_FIELD, notNullValue());

        awaitAgentSharedToOrg(agentId, orgId);
    }

    @Test(dependsOnMethods = "testShareAgentsWithAllOrganizations")
    public void testGetSharedOrganizationsOfAgent() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(LIMIT_QUERY_PARAM, 5);
        queryParams.put(ATTRIBUTES_QUERY_PARAM, ROLES_SHARING_MODE_ATTRIBUTES);

        getResponseOfGet(AGENT_API_BASE_PATH + "/" + agentId + SHARE_PATH, queryParams)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ORGANIZATIONS_FIELD, notNullValue())
                .body("organizations.size()", greaterThan(0));
    }

    @Test(dependsOnMethods = "testGetSharedOrganizationsOfAgent")
    public void testUnshareAgentsFromAllOrganizations() throws IOException {

        String body = readResource(UNSHARE_AGENTS_FROM_ALL_ORGS_REQUEST)
                .replace(AGENT_ID_PLACEHOLDER, agentId);

        getResponseOfPost(AGENT_API_BASE_PATH + UNSHARE_WITH_ALL_PATH, body)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(PROCESSING_STATUS_FIELD, equalTo(PROCESSING_STATUS_VALUE))
                .body(DETAILS_FIELD, notNullValue());

        awaitAgentFullyUnshared(agentId);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(LIMIT_QUERY_PARAM, 5);
        queryParams.put(ATTRIBUTES_QUERY_PARAM, ROLES_SHARING_MODE_ATTRIBUTES);

        getResponseOfGet(AGENT_API_BASE_PATH + "/" + agentId + SHARE_PATH, queryParams)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ORGANIZATIONS_FIELD, notNullValue())
                .body("organizations.size()", equalTo(0));
    }
}
