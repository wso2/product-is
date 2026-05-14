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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Base class for Agent Sharing v1 integration tests.
 */
public class AgentSharingBaseTest extends RESTAPIServerTestBase {

    protected static final String API_DEFINITION_NAME = "organization-agent-share-v1.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.organization.agent.sharing.management.v1";
    protected static final String AUTHORIZED_APIS_JSON = "agent-sharing-apis.json";

    protected static final String AGENT_API_BASE_PATH = "/agents";
    protected static final String SHARE_PATH = "/share";
    protected static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    protected static final String UNSHARE_PATH = "/unshare";
    protected static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";

    protected static final String LIMIT_QUERY_PARAM = "limit";
    protected static final String ATTRIBUTES_QUERY_PARAM = "attributes";
    protected static final String ROLES_SHARING_MODE_ATTRIBUTES = "roles,sharingMode";
    protected static final String ORGANIZATIONS_FIELD = "organizations";
    protected static final String ORG_ID_FIELD = "orgId";

    private static final String ROLE_REQUEST_BODY_JSON = "role-request-body.json";
    private static final String DISPLAY_NAME_PLACEHOLDER = "DISPLAY_NAME";
    private static final String AUDIENCE_DISPLAY_PLACEHOLDER = "AUDIENCE_DISPLAY";
    private static final String AUDIENCE_TYPE_PLACEHOLDER = "AUDIENCE_TYPE";

    protected static String swaggerDefinition;

    protected SCIM2RestClient scim2RestClient;
    protected OrgMgtRestClient orgMgtRestClient;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail("Failed to load API definition: " + API_DEFINITION_NAME, e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void clearBasePath() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    protected String randomUuid() {

        return UUID.randomUUID().toString();
    }

    protected String createRole(String displayName, String audienceDisplay, String audienceType)
            throws IOException {

        return readResource(ROLE_REQUEST_BODY_JSON)
                .replace(DISPLAY_NAME_PLACEHOLDER, displayName)
                .replace(AUDIENCE_DISPLAY_PLACEHOLDER, audienceDisplay)
                .replace(AUDIENCE_TYPE_PLACEHOLDER, audienceType);
    }

    /**
     * Waits until the agent is sharing with the given organization, polling the GET endpoint.
     * Asserts that the org ID appears in the organizations list.
     */
    protected void awaitAgentSharedToOrg(String agentId, String orgId) {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(LIMIT_QUERY_PARAM, 10);
        queryParams.put(ATTRIBUTES_QUERY_PARAM, ROLES_SHARING_MODE_ATTRIBUTES);

        await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = getResponseOfGet(
                            AGENT_API_BASE_PATH + "/" + agentId + SHARE_PATH, queryParams);
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
                        return false;
                    }
                    List<String> orgIds = response.jsonPath()
                            .getList(ORGANIZATIONS_FIELD + "." + ORG_ID_FIELD, String.class);
                    return orgIds != null && orgIds.contains(orgId);
                });
    }

    /**
     * Waits until the agent has no active sharing with any organization.
     */
    protected void awaitAgentFullyUnshared(String agentId) {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(LIMIT_QUERY_PARAM, 10);
        queryParams.put(ATTRIBUTES_QUERY_PARAM, ROLES_SHARING_MODE_ATTRIBUTES);

        await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = getResponseOfGet(
                            AGENT_API_BASE_PATH + "/" + agentId + SHARE_PATH, queryParams);
                    if (response.getStatusCode() != HttpStatus.SC_OK) {
                        return false;
                    }
                    List<?> orgs = response.jsonPath().getList(ORGANIZATIONS_FIELD);
                    return orgs != null && orgs.isEmpty();
                });
    }
}
