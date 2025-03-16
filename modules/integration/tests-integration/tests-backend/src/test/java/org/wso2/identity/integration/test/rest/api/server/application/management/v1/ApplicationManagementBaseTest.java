/*
 * Copyright (c) 2019-2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

/**
 * Base test class for Application Management REST APIs.
 */
public class ApplicationManagementBaseTest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "applications.yaml";
    static final String API_VERSION = "v1";

    static final String APPLICATION_MANAGEMENT_API_BASE_PATH = "/applications";
    static final String METADATA_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH + "/meta";
    static final String RESIDENT_APP_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH + "/resident";
    static final String APPLICATION_TEMPLATE_MANAGEMENT_API_BASE_PATH = APPLICATION_MANAGEMENT_API_BASE_PATH +
            "/templates";
    static final String GROUPS_METADATA_PATH = METADATA_API_BASE_PATH + "/groups";
    static final String PATH_SEPARATOR = "/";

    protected static String swaggerDefinition;
    private SCIM2RestClient scim2RestClient;

    static {
        String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.application.management.v1";
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    public ApplicationManagementBaseTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
        scim2RestClient.closeHttpClient();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    protected void cleanUpApplications(Set<String> appsToCleanUp) {

        appsToCleanUp.forEach(appId -> {
            String applicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId;
            Response responseOfDelete = getResponseOfDelete(applicationPath);
            responseOfDelete.then()
                    .log()
                    .ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);

            // Make sure we don't have deleted application details.
            getResponseOfGet(applicationPath).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        });
    }

    /**
     * Create a set of groups and return the group IDs.
     *
     * @param groupCount      The number of groups to be created.
     * @param groupNamePrefix The prefix of the group name.
     * @return The group IDs.
     * @throws Exception If an error occurs while creating the groups.
     */
    protected String[] createGroups(int groupCount, String groupNamePrefix) throws Exception {

        String[] groupIDs = new String[groupCount];
        for (int i = 0; i < groupCount; i++) {
            String groupName = groupNamePrefix + i;
            groupIDs[i] = scim2RestClient.createGroup(new GroupRequestObject().displayName(groupName));
        }
        return groupIDs;
    }

    /**
     * Delete the groups with the given group IDs.
     *
     * @param groupIDs The group IDs.
     * @throws Exception If an error occurs while deleting the groups.
     */
    protected void deleteGroups(String[] groupIDs) throws Exception {

        for (String groupID : groupIDs) {
            scim2RestClient.deleteGroup(groupID);
        }
    }

    /**
     * Add discoverable groups to the application payload.
     *
     * @param payload   Application payload.
     * @param userStore User store.
     * @param groupIDs  Group IDs.
     * @throws JSONException If an error occurs while adding discoverable groups to the payload.
     */
    protected JSONObject addDiscoverableGroupsToApplicationPayload(JSONObject payload, String userStore,
                                                                 String[] groupIDs)
            throws JSONException {

        JSONObject discoverableGroup = new JSONObject();
        discoverableGroup.put("userStore", userStore);
        JSONArray groups = new JSONArray();
        for (String groupID : groupIDs) {
            JSONObject group = new JSONObject();
            group.put("id", groupID);
            groups.put(group);
        }
        discoverableGroup.put("groups", groups);
        JSONArray discoverableGroups = new JSONArray();
        discoverableGroups.put(discoverableGroup);
        JSONObject advancedConfigs = payload.getJSONObject("advancedConfigurations");
        advancedConfigs.put("discoverableGroups", discoverableGroups);
        return payload;
    }

    /**
     * Verify the discoverable groups in the response.
     *
     * @param response  The response.
     * @param userStore User store.
     * @param groupIDs  Group IDs.
     * @throws JSONException If an error occurs while verifying the discoverable groups.
     */
    protected void verifyDiscoverableGroups(JSONObject response, String userStore, String[] groupIDs)
            throws JSONException {

        JSONArray discoverableGroups = response.getJSONArray("discoverableGroups");
        Assert.assertEquals(discoverableGroups.length(), 1, "Discoverable groups count mismatched.");
        JSONObject discoverableGroup = discoverableGroups.getJSONObject(0);
        Assert.assertEquals(discoverableGroup.getString("userStore"), userStore, "User store mismatched.");
        JSONArray groups = discoverableGroup.getJSONArray("groups");
        Assert.assertEquals(groups.length(), groupIDs.length, "Group count mismatched.");
        for (String groupID : groupIDs) {
            Assert.assertTrue(groups.toString().contains(groupID), "Group ID not found in the response.");
        }
    }
}
