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

package org.wso2.identity.integration.test.scim2.rest.api;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;

/**
 * This class contains test cases for SCIM2 Bulk Resource API.
 * It tests the bulk operations for users and roles with Operation scopes.
 */
public class SCIM2BulkResourceTestCase extends ISIntegrationTest {

    public static final String OLD_USER_USERNAME = "oldUser";
    public static final String NEW_USER_USERNAME = "newUser";
    public static final String UNAUTHORIZED_USER_USERNAME = "unauthorizedUser";

    public static final String OLD_ROLE_NAME = "oldBulkRole";
    public static final String NEW_ROLE_NAME = "newBulkRole";
    private static final String DUMMY_PASSWORD = "Wso2@test";

    private static final String USERS_PATH = "users";
    public static final String SCIM2_BULK = "/scim2/Bulk";

    private static final String BULK_RESOURCE_SCOPE = "internal_bulk_resource_create";
    private static final String BULK_USER_CREATE_SCOPE = "internal_bulk_user_create";
    private static final String BULK_USER_UPDATE_SCOPE = "internal_bulk_user_update";
    private static final String BULK_USER_DELETE_SCOPE = "internal_bulk_user_delete";
    private static final String BULK_GROUP_CREATE_SCOPE = "internal_bulk_group_create";
    private static final String BULK_GROUP_UPDATE_SCOPE = "internal_bulk_group_update";
    private static final String BULK_GROUP_DELETE_SCOPE = "internal_bulk_group_delete";
    private static final String BULK_ROLE_CREATE_SCOPE = "internal_bulk_role_create";
    private static final String BULK_ROLE_UPDATE_SCOPE = "internal_bulk_role_update";
    private static final String BULK_ROLE_DELETE_SCOPE = "internal_bulk_role_delete";

    private final TestUserMode userMode;
    private final String tenant;

    protected OAuth2RestClient restClient;
    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;

    private String oldUserId;
    private String newUserId;
    private String oldRoleId;
    private String newRoleId;
    private String unauthorizedUserId;
    private String resourceId;

    @Factory(dataProvider = "SCIM2BulkResourceConfigProvider")
    public SCIM2BulkResourceTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.tenant = context.getContextTenant().getDomain();
        this.userMode = userMode;
    }

    @DataProvider(name = "SCIM2MeConfigProvider")
    public static Object[][] SCIM2BulkResourceConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);
        client = HttpClients.createDefault();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        restClient = new OAuth2RestClient(serverURL, tenantInfo);

        // Create users.
        oldUserId = addUser(OLD_USER_USERNAME);
        assertNotNull(oldUserId, "Failed to create old user");
        newUserId = addUser(NEW_USER_USERNAME);
        assertNotNull(newUserId, "Failed to create new user");
        unauthorizedUserId = addUser(UNAUTHORIZED_USER_USERNAME);
        assertNotNull(unauthorizedUserId, "Failed to create unauthorized user");

        // Create roles.
        oldRoleId = addRole(OLD_ROLE_NAME, Collections.singletonList(BULK_RESOURCE_SCOPE));
        assertNotNull(oldRoleId, "Failed to create old role");
        newRoleId = addRole(NEW_ROLE_NAME, ImmutableList.of(
                BULK_USER_CREATE_SCOPE, BULK_USER_UPDATE_SCOPE, BULK_USER_DELETE_SCOPE,
                BULK_GROUP_CREATE_SCOPE, BULK_GROUP_UPDATE_SCOPE, BULK_GROUP_DELETE_SCOPE,
                BULK_ROLE_CREATE_SCOPE, BULK_ROLE_UPDATE_SCOPE, BULK_ROLE_DELETE_SCOPE));
        assertNotNull(newRoleId, "Failed to create new role");

        // Assign users to roles.
        assignUsersToRole(oldRoleId, oldUserId);
        assignUsersToRole(newRoleId, newUserId);
    }

    @AfterClass(alwaysRun = true)
    public void deleteUsers() throws IOException {

        scim2RestClient.deleteUser(oldUserId);
        scim2RestClient.deleteUser(newUserId);
        scim2RestClient.deleteUser(unauthorizedUserId);
        restClient.deleteV2Role(oldRoleId);
        restClient.deleteV2Role(newRoleId);

        scim2RestClient.closeHttpClient();
        client.close();
        restClient.closeHttpClient();
    }

    @DataProvider
    public static Object[][] bulkEPDataProvider() {

        return new Object[][]{
                {OLD_USER_USERNAME, 200, 201, "add", "user"}, {OLD_USER_USERNAME, 200, 200, "replace", "user"},
                {OLD_USER_USERNAME, 200, 200, "patch", "user"}, {OLD_USER_USERNAME, 200, 204, "delete", "user"},
                {NEW_USER_USERNAME, 200, 201, "add", "user"}, {NEW_USER_USERNAME, 200, 200, "replace", "user"},
                {NEW_USER_USERNAME, 200, 200, "patch", "user"}, {NEW_USER_USERNAME, 200, 204, "delete", "user"},
                {OLD_USER_USERNAME, 200, 201, "add", "user"}, {UNAUTHORIZED_USER_USERNAME, 403, 403, "add", "user"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "replace", "user"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "patch", "user"},
                {UNAUTHORIZED_USER_USERNAME, 403, 401, "delete", "user"},
                {OLD_USER_USERNAME, 200, 204, "delete", "user"},

                {OLD_USER_USERNAME, 200, 201, "add", "role"}, {OLD_USER_USERNAME, 200, 200, "replace", "role"},
                {OLD_USER_USERNAME, 200, 200, "patch", "role"}, {OLD_USER_USERNAME, 200, 204, "delete", "role"},
                {NEW_USER_USERNAME, 200, 201, "add", "role"}, {NEW_USER_USERNAME, 200, 200, "replace", "role"},
                {NEW_USER_USERNAME, 200, 200, "patch", "role"}, {NEW_USER_USERNAME, 200, 204, "delete", "role"},
                {OLD_USER_USERNAME, 200, 201, "add", "role"}, {UNAUTHORIZED_USER_USERNAME, 403, 403, "add", "role"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "replace", "role"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "patch", "role"},
                {UNAUTHORIZED_USER_USERNAME, 403, 401, "delete", "role"},
                {OLD_USER_USERNAME, 200, 204, "delete", "role"},

                {OLD_USER_USERNAME, 200, 201, "add", "group"}, {OLD_USER_USERNAME, 200, 200, "replace", "group"},
                {OLD_USER_USERNAME, 200, 200, "patch", "group"}, {OLD_USER_USERNAME, 200, 204, "delete", "group"},
                {NEW_USER_USERNAME, 200, 201, "add", "group"}, {NEW_USER_USERNAME, 200, 200, "replace", "group"},
                {NEW_USER_USERNAME, 200, 200, "patch", "group"}, {NEW_USER_USERNAME, 200, 204, "delete", "group"},
                {OLD_USER_USERNAME, 200, 201, "add", "group"}, {UNAUTHORIZED_USER_USERNAME, 403, 403, "add", "group"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "replace", "group"},
                {UNAUTHORIZED_USER_USERNAME, 403, 403, "patch", "group"},
                {UNAUTHORIZED_USER_USERNAME, 403, 401, "delete", "group"},
                {OLD_USER_USERNAME, 200, 204, "delete", "group"},
        };
    }

    @Test(dataProvider = "bulkEPDataProvider")
    public void testBulkUserOperations(String username, int statusCode, int bulkRepStatusCode, String operation,
                                       String resourceType) throws Exception {

        String requestBody = FileUtils.readFileInClassPathAsString("bulk/requests/" + resourceType + "/"
                + operation + ".json");
        JSONObject dataJson = new JSONObject(requestBody);
        if (!Objects.equals(operation, "add")) {
            addResourceIdToPath(dataJson);
        }
        HttpResponse response = sendRequest(dataJson, username, SCIM2_BULK);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        assertNotNull(responseString, "Response string is null for request sent by user: " + username);
        extractResourceIdFromResponse(responseString, bulkRepStatusCode, operation);
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), statusCode,
                "Invalid status code for request sent by user: " + username);
    }

    private void addResourceIdToPath(JSONObject dataJson) throws JSONException {

        if (dataJson.has("Operations")) {
            JSONArray operations = dataJson.getJSONArray("Operations");
            if (operations.length() > 0) {
                JSONObject operation = operations.getJSONObject(0);
                if (operation.has("path")) {
                    String path = operation.getString("path");
                    if (path.contains("{{id}}")) {
                        operation.put("path", path.replace("{{id}}", resourceId));
                    }
                }
            }
        } else {
            throw new JSONException("No Operations found in the request body.");
        }
    }

    private void extractResourceIdFromResponse(String responseString, int expectedBulkStatusCode, String operation)
            throws ParseException, JSONException {

        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject jsonResponse = (org.json.simple.JSONObject) parser.parse(responseString);
        if (jsonResponse.containsKey("Operations")) {
            org.json.simple.JSONArray operations = (org.json.simple.JSONArray) jsonResponse.get("Operations");
            if (operations != null && !operations.isEmpty()) {
                org.json.simple.JSONObject operationObject = (org.json.simple.JSONObject) operations.get(0);
                if (operationObject != null && operationObject.containsKey("response")) {

                    Object statusObject = operationObject.get("status");
                    if (statusObject != null) {
                        org.json.simple.JSONObject statusJsonObject =
                                (org.json.simple.JSONObject) parser.parse(statusObject.toString());
                        String statusCodeObject = statusJsonObject.get("code").toString();
                        assertEquals(statusCodeObject, String.valueOf(expectedBulkStatusCode),
                                "Invalid status code in the response.");
                    }
                    Object responseObject = operationObject.get("response");
                    if (responseObject != null) {
                        org.json.simple.JSONObject bulkResponse =
                                (org.json.simple.JSONObject) parser.parse(responseObject.toString());
                        if (bulkResponse.containsKey("id") && Objects.equals(operation, "add")) {
                            resourceId = bulkResponse.get("id").toString();
                        }
                    }
                }
            }
        }
    }

    private HttpResponse sendRequest(JSONObject body, String userName, String apiPath) throws IOException {

        HttpPost request = new HttpPost(getPath(apiPath));
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(userName));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity entity = new StringEntity(body.toString());
        request.setEntity(entity);
        return client.execute(request);
    }

    private String getAuthzHeader(String userName) {

        return "Basic " + Base64.encodeBase64String((userName + ":" + DUMMY_PASSWORD).getBytes()).trim();
    }

    private String getPath(String apiPath) {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + apiPath;
        } else {
            return SERVER_URL + "/t/" + tenant + apiPath;
        }
    }

    private String addUser(String username) throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(username);
        userInfo.setPassword(DUMMY_PASSWORD);
        userInfo.setName(new Name().givenName(username));
        userInfo.addEmail(new Email().value(username + "@example.com"));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country("USA"));

        return scim2RestClient.createUser(userInfo);
    }

    private String addRole(String roleName, List<String> scopes) throws IOException {

        List<Permission> permissions = new ArrayList<>();
        for (String scope : scopes) {
            permissions.add(new Permission(scope));
        }
        List<String> schemas = Collections.emptyList();
        RoleV2 role = new RoleV2(roleName, permissions, schemas);
        return restClient.createV2Roles(role);
    }

    private void assignUsersToRole(String roleId, String userId) throws Exception {

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }
}
