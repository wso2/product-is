/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.scim2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_TYPE_HOME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GIVEN_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PASSWORD_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_ROLES_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

public class SCIM2RoleTestCase extends ISIntegrationTest {

    private static final String TOTAL_COUNT_ATTRIBUTE = "totalResults";
    private static final String PASSWORD = "Wso2@test";

    private static final String USERNAME_1 = "testUser1";
    private static final String USERNAME_2 = "testUser2";
    private static final String USERNAME_3 = "testUser3";
    private static final String USERNAME_4 = "testUser4";

    private static final String GIVEN_NAME_CLAIM_VALUE_1 = "testGivenName1";
    private static final String GIVEN_NAME_CLAIM_VALUE_2 = "testGivenName2";
    private static final String GIVEN_NAME_CLAIM_VALUE_3 = "testGivenName3";
    private static final String GIVEN_NAME_CLAIM_VALUE_4 = "testGivenName4";

    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_1 = "testUser1@gmail.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_2 = "testUser2@gmail.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_3 = "testUser3@gmail.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_4 = "testUser4@gmail.com";

    private static final String ROLE_NAME_1 = "testRole1";
    private static final String PRE_ROLE_NAME = "testRole";
    private static final int ROLE_COUNT = 7; // Number of roles created in the test case.

    private CloseableHttpClient client;
    private String userId1;
    private String userId2;
    private String userId3;
    private String userId4;
    private String roleId1;
    private final String adminUsername;
    private final String password;
    private final String tenant;
    private int totalRolesCount; // To count the number of roles in the pack.
    private List<String> roleIdList;

    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2RoleTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "SCIM2MeConfigProvider")
    public static Object[][] SCIM2MeConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        roleIdList = new ArrayList<>();
        client = HttpClients.createDefault();

        HttpResponse response = createUser(USERNAME_1, GIVEN_NAME_CLAIM_VALUE_1, EMAIL_TYPE_HOME_CLAIM_VALUE_1);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        userId1 = responseObj.getString(ID_ATTRIBUTE);

        response = createUser(USERNAME_2, GIVEN_NAME_CLAIM_VALUE_2, EMAIL_TYPE_HOME_CLAIM_VALUE_2);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId2 = responseObj.getString(ID_ATTRIBUTE);

        response = createUser(USERNAME_3, GIVEN_NAME_CLAIM_VALUE_3, EMAIL_TYPE_HOME_CLAIM_VALUE_3);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId3 = responseObj.getString(ID_ATTRIBUTE);

        response = createUser(USERNAME_4, GIVEN_NAME_CLAIM_VALUE_4, EMAIL_TYPE_HOME_CLAIM_VALUE_4);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId4 = responseObj.getString(ID_ATTRIBUTE);

        HttpGet request = new HttpGet(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpResponse rolesGetResponse = client.execute(request);
        JSONObject rolesResponseObj = new JSONObject(EntityUtils.toString(rolesGetResponse.getEntity()));
        EntityUtils.consume(rolesGetResponse.getEntity());
        assertEquals(rolesGetResponse.getStatusLine().getStatusCode(), 200, "Roles have not been "
                + "retrieved successfully");
        totalRolesCount = rolesResponseObj.getInt(TOTAL_COUNT_ATTRIBUTE); // To get already existing roles count.
    }

    @AfterClass(alwaysRun = true)
    public void deleteUsers() throws IOException {

        HttpResponse response = deleteUser(userId1);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        response = deleteUser(userId2);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        response = deleteUser(userId3);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        response = deleteUser(userId4);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        for (String roleId : roleIdList) { // Delete all roles created.
            response = sendDeleteUserRequest(roleId);
            assertEquals(response.getStatusLine().getStatusCode(), 204, "Role " +
                    "has not been deleted successfully");
            EntityUtils.consume(response.getEntity());
            response = sendGetRoleRequest(roleId);
            assertEquals(response.getStatusLine().getStatusCode(), 404, "Role " +
                    "has not been deleted successfully");
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test
    public void testCreateRoleWithUsers() throws Exception {

        JSONObject rootObject = new JSONObject();

        JSONObject user1 = new JSONObject();
        user1.put(VALUE_PARAM, userId1);

        JSONObject user2 = new JSONObject();
        user2.put(VALUE_PARAM, userId2);

        JSONArray users = new JSONArray();
        users.put(user1);
        users.put(user2);

        rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, ROLE_NAME_1);
        rootObject.put("users", users);

        HttpResponse response = sendCreateRoleRequest(rootObject);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Role " +
                "has not been created successfully");

        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String roleNameFromResponse = responseObj.getString(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE);
        assertTrue(roleNameFromResponse.contains(ROLE_NAME_1), "Role name is not as expected");
        roleId1 = responseObj.getString(ID_ATTRIBUTE);
        assertNotNull(roleId1, "Role id is null");
        totalRolesCount++;

        HttpResponse getRoleResponse = sendGetRoleRequest(roleId1);
        assertEquals(getRoleResponse.getStatusLine().getStatusCode(), 200, "Role has not been "
                + "retrieved successfully");
        responseObj = new JSONObject(EntityUtils.toString(getRoleResponse.getEntity()));
        EntityUtils.consume(getRoleResponse.getEntity());
        JSONArray usersJson = responseObj.getJSONArray("users");
        assertEquals(usersJson.length(), 2, "Number of users in role is not as expected");
        assertEquals(userId1, usersJson.getJSONObject(0).getString(VALUE_PARAM), "User id is not as expected");
        assertEquals(userId2, usersJson.getJSONObject(1).getString(VALUE_PARAM), "User id is not as expected");
    }

    @Test(dependsOnMethods = "testCreateRoleWithUsers")
    public void createRolesWithoutUsers() throws Exception {

        for (int i = 2; i <= ROLE_COUNT; i++) { // Starting with 2 since 'testRole1' has already been created.
            JSONObject rootObject = new JSONObject();
            String roleDisplayName = PRE_ROLE_NAME + i;
            rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, roleDisplayName);
            HttpResponse response = sendCreateRoleRequest(rootObject);
            assertEquals(response.getStatusLine().getStatusCode(), 201, "Role " +
                    "has not been created successfully");

            JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
            EntityUtils.consume(response.getEntity());

            String roleNameFromResponse = responseObj.getString(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE);
            assertTrue(roleNameFromResponse.contains(roleDisplayName), "Role name is not as expected");
            String roleIdFromResponse = responseObj.getString(ID_ATTRIBUTE);
            assertNotNull(roleIdFromResponse, "Role id is null");
            roleIdList.add(roleIdFromResponse);
            totalRolesCount++;
        }
    }

    @Test(dependsOnMethods = "createRolesWithoutUsers")
    public void addRoleWithInvalidUsers() throws Exception {

        JSONObject rootObject = new JSONObject();
        rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, "testRole");

        JSONObject user = new JSONObject();
        user.put(VALUE_PARAM, "11111111111111111"); // Invalid user id.

        JSONArray users = new JSONArray();
        users.put(user);

        rootObject.put("users", users);

        HttpResponse response = sendCreateRoleRequest(rootObject);
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), 500, "Should return 500 when adding"
                + " role with invalid user");
    }

    @Test(dependsOnMethods = "addRoleWithInvalidUsers")
    public void addRoleWithDuplicateDisplayName() throws Exception {

        JSONObject rootObject = new JSONObject();
        rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, ROLE_NAME_1);
        HttpResponse response = sendCreateRoleRequest(rootObject);
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), 409, "Role should not be created"
                + " when duplicate display name is provided");
    }

    @Test(dependsOnMethods = "addRoleWithDuplicateDisplayName")
    public void getRoles() throws Exception {

        HttpGet request = new HttpGet(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Roles have not been retrieved successfully");
        int totalResults = responseObj.getInt(TOTAL_COUNT_ATTRIBUTE);
        assertEquals(totalResults, totalRolesCount, "Total roles count is not as expected");
    }

    @Test(dependsOnMethods = "getRoles")
    public void getRolesWithPagination() throws Exception {

        int count = 3;
        int startIndex = 3;
        HttpGet request = new HttpGet(getPath() + "?startIndex=" + startIndex + "&count=" + count);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Roles have not been retrieved successfully");
        int totalResults = responseObj.getInt(TOTAL_COUNT_ATTRIBUTE);
        assertEquals(totalResults, totalRolesCount, "Total roles count is not as expected");
        int returnedRolesCount = responseObj.getJSONArray("Resources").length();
        assertEquals(returnedRolesCount, count, "Returned roles count is not as expected");
    }

    @Test(dependsOnMethods = "getRolesWithPagination")
    public void getRolesWithFilter() throws Exception {

        HttpPost request = new HttpPost(getPath() + "/.search");
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        schemas.put("urn:ietf:params:scim:api:messages:2.0:SearchRequest");
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);
        rootObject.put("filter", "displayName eq " + ROLE_NAME_1);
        request.setEntity(new StringEntity(rootObject.toString()));
        HttpResponse response = client.execute(request);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Roles have not been retrieved successfully");
        int totalResults = responseObj.getInt(TOTAL_COUNT_ATTRIBUTE);
        assertEquals(totalResults, 1, "Total roles count is not as expected");
        String roleId = responseObj.getJSONArray("Resources").getJSONObject(0).getString(ID_ATTRIBUTE);
        assertEquals(roleId, roleId1, "Role id is not as expected");
    }

    @Test(dependsOnMethods = "getRolesWithFilter")
    public void getRolesWithFilterAndPagination() throws Exception {

        String filter = "displayName sw " + PRE_ROLE_NAME;
        int count = 3;
        int startIndex = 1;
        HttpPost request = new HttpPost(getPath() + "/.search");
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        schemas.put("urn:ietf:params:scim:api:messages:2.0:SearchRequest");
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);
        rootObject.put("filter", filter);
        rootObject.put("startIndex", startIndex);
        rootObject.put("count", count);
        request.setEntity(new StringEntity(rootObject.toString()));
        HttpResponse response = client.execute(request);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Roles have not been retrieved successfully.");
        int returnedRolesCount = responseObj.getJSONArray("Resources").length();
        assertEquals(returnedRolesCount, count, "Returned roles count is not as expected. Only three roles "
                + "should be return with the response");
    }

    @Test(dependsOnMethods = "getRolesWithFilterAndPagination")
    public void testGetRoleWithId() throws Exception {

        HttpResponse response = sendGetRoleRequest(roleId1);
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Role has not been retrieved successfully.");
        String roleName = responseObj.getString(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE);
        assertEquals(roleName, ROLE_NAME_1, "Role name is not as expected.");
    }

    @Test(dependsOnMethods = "testGetRoleWithId")
    public void testGetRoleWithInvalidId() throws Exception {

        HttpResponse response = sendGetRoleRequest("invalidId");
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), 404, "Should not be able to get a "
                + "role with an invalid id.");
    }

    @Test(dependsOnMethods = "testGetRoleWithInvalidId")
    public void testDeleteRoleWithId() throws Exception {

        HttpResponse response = sendDeleteUserRequest(roleId1);
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 204, "Role has not been deleted successfully.");

        HttpResponse response2 = sendGetRoleRequest(roleId1);
        EntityUtils.consume(response2.getEntity());
        assertEquals(response2.getStatusLine().getStatusCode(), 404, "Role has not been deleted successfully.");
    }

    @Test(dependsOnMethods = "testDeleteRoleWithId")
    public void testDeleteRoleWithInvalidId() throws Exception {

        HttpDelete request = new HttpDelete(getPath() + "/" + "invalidId");
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());

        assertEquals(response.getStatusLine().getStatusCode(), 404, "Role has not been deleted successfully.");
    }

    @Test(dependsOnMethods = "testDeleteRoleWithInvalidId")
    public void testPutRoleWithId() throws Exception {

        String updatedRoleName = "updatedRoleName";
        String putBody = "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"displayName\":\"" + updatedRoleName + "\"}";
        HttpPut request = new HttpPut(getPath() + "/" + roleIdList.get(0));
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        StringEntity entity = new StringEntity(putBody);
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Role has not been updated successfully.");

        HttpResponse response2 = sendGetRoleRequest(roleIdList.get(0));
        JSONObject responseObj2 = new JSONObject(EntityUtils.toString(response2.getEntity()));
        EntityUtils.consume(response2.getEntity());
        assertEquals(response2.getStatusLine().getStatusCode(), 200, "Role has not been updated successfully.");
        String roleName = responseObj2.getString(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE);
        assertEquals(roleName, updatedRoleName, "Role has not been updated successfully.");
    }

    @Test(dependsOnMethods = "testPutRoleWithId")
    public void testPatchRoleWithId() throws Exception {

        JSONObject patchBody = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.put("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        patchBody.put("schemas", schemas);

        JSONArray operations = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("op", "replace");
        operation.put("path", "users");
        JSONArray values = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("value", userId3);
        values.put(value);
        operation.put("value", values);
        operations.put(operation);
        patchBody.put("Operations", operations);

        HttpPatch request = new HttpPatch(getPath() + "/" + roleIdList.get(0));
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        StringEntity entity = new StringEntity(patchBody.toString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Role has not been updated successfully.");

        HttpResponse response2 = sendGetRoleRequest(roleIdList.get(0));
        JSONObject responseObj = new JSONObject(EntityUtils.toString(response2.getEntity()));
        String userValue = responseObj.getJSONArray("users").getJSONObject(0).getString("value");
        assertEquals(userValue, userId3, "Role has not been updated successfully.");
        String userName = responseObj.getJSONArray("users").getJSONObject(0).getString("display");
        assertEquals(userName, USERNAME_3, "Role has not been updated successfully.");
    }


    private HttpResponse sendCreateRoleRequest(JSONObject body) throws IOException {

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        StringEntity entity = new StringEntity(body.toString());
        request.setEntity(entity);
        return client.execute(request);
    }

    private HttpResponse sendDeleteUserRequest(String roleId) throws IOException {

        HttpDelete request = new HttpDelete(getPath() + "/" + roleId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        return client.execute(request);
    }

    private HttpResponse sendGetRoleRequest(String roleId) throws IOException {

        HttpGet request = new HttpGet(getPath() + "/" + roleId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        return client.execute(request);
    }

    private String getUsersPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + password).getBytes()).trim();
    }

    private HttpResponse createUser(String username, String givenName, String homeEmail) throws IOException,
            JSONException {

        HttpPost request = new HttpPost(getUsersPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(GIVEN_NAME_ATTRIBUTE, givenName);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, username);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(VALUE_PARAM, homeEmail);

        JSONArray emails = new JSONArray();
        emails.put(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        return client.execute(request);
    }

    private String getPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_ROLES_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_ROLES_ENDPOINT;
        }
    }

    private HttpResponse deleteUser(String userId) throws IOException {

        String userResourcePath = getUsersPath() + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getPath() + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        return client.execute(request);
    }

}
