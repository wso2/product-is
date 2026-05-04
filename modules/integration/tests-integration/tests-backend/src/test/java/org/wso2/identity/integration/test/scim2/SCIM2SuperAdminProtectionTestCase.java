/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

package org.wso2.identity.integration.test.scim2;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.FAMILY_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GIVEN_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PASSWORD_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_GROUPS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

/**
 * Integration test cases to verify that the super admin user is protected from deletion
 * via the SCIM2 API. These tests validate the backend enforcement that ensures
 * the super admin cannot be deleted by any user (including themselves or other
 * admin-level users), which corresponds to the UI fix that hides the delete option
 * for the super admin user in the users list.
 *
 * Related issue: https://github.com/wso2/product-is/issues/27728
 */
public class SCIM2SuperAdminProtectionTestCase extends ISIntegrationTest {

    private static final Log LOG = LogFactory.getLog(SCIM2SuperAdminProtectionTestCase.class);

    private static final String NON_SUPER_ADMIN_USERNAME = "nonsuperadmintest";
    private static final String NON_SUPER_ADMIN_PASSWORD = "Wso2@test123";
    private static final String NON_SUPER_ADMIN_EMAIL = "nonsuperadmintest@wso2.com";
    private static final String ADMIN_ROLE_NAME = "admin";

    private CloseableHttpClient client;
    private String adminUsername;
    private String adminPassword;
    private String superAdminUserId;
    private String nonSuperAdminUserId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        client = HttpClients.createDefault();

        AutomationContext context = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();

        superAdminUserId = getSuperAdminUserId();
        assertNotNull(superAdminUserId, "Super admin user ID should not be null");
        nonSuperAdminUserId = createNonSuperAdminUser();
        assertNotNull(nonSuperAdminUserId, "Non-super-admin user ID should not be null");

        addUserToAdminRole(nonSuperAdminUserId);
    }

    @AfterClass(alwaysRun = true)
    public void testClean() throws Exception {

        if (nonSuperAdminUserId != null) {
            deleteUser(nonSuperAdminUserId, adminUsername, adminPassword);
        }
        client.close();
    }

    /**
     * Verify that the super admin user cannot delete themselves via the SCIM2 API.
     * The API should reject self-deletion of the super admin.
     */
    @Test
    public void testSuperAdminCannotDeleteSelf() throws Exception {

        String userResourcePath = SERVER_URL + SCIM2_USERS_ENDPOINT + "/" + superAdminUserId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(adminUsername, adminPassword));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        LOG.info("Response status code for super admin self-deletion attempt: " + statusCode);

        // The API should reject the deletion of the super admin user.
        // A 400 (Bad Request) or 403 (Forbidden) response is expected.
        Assert.assertTrue(statusCode >= HttpStatus.SC_BAD_REQUEST
                        && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Super admin self-deletion should have been rejected by the API with a 4xx error, "
                        + "but got status code: " + statusCode);
    }

    /**
     * Verify that a non-super-admin user with admin-level privileges cannot delete
     * the super admin user via the SCIM2 API.
     * This test directly validates the backend enforcement corresponding to the UI fix
     * described in https://github.com/wso2/product-is/issues/27728.
     */
    @Test
    public void testNonSuperAdminCannotDeleteSuperAdmin() throws Exception {

        String userResourcePath = SERVER_URL + SCIM2_USERS_ENDPOINT + "/" + superAdminUserId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION,
                getAuthzHeader(NON_SUPER_ADMIN_USERNAME, NON_SUPER_ADMIN_PASSWORD));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        LOG.info("Response status code for non-super-admin deleting super admin attempt: " + statusCode);

        // The API should reject any deletion attempt targeting the super admin user.
        Assert.assertTrue(statusCode >= HttpStatus.SC_BAD_REQUEST
                        && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Non-super-admin deletion of super admin should have been rejected by the API with a 4xx error, "
                        + "but got status code: " + statusCode);
    }

    /**
     * Retrieves the SCIM2 ID of the super admin user by filtering on the admin username
     * retrieved from the automation context.
     *
     * @return The SCIM2 ID of the super admin user.
     * @throws Exception if the retrieval fails.
     */
    private String getSuperAdminUserId() throws Exception {

        String filterPath = SERVER_URL + SCIM2_USERS_ENDPOINT
                + "?filter=" + USER_NAME_ATTRIBUTE + "+Eq+" + adminUsername;
        HttpGet request = new HttpGet(filterPath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(adminUsername, adminPassword));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.ACCEPT, "application/scim+json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Failed to retrieve the super admin user.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        JSONArray resources = (JSONArray) ((JSONObject) responseObj).get("Resources");
        if (resources == null || resources.isEmpty()) {
            return null;
        }
        return ((JSONObject) resources.get(0)).get(ID_ATTRIBUTE).toString();
    }

    /**
     * Creates a non-super-admin test user via the SCIM2 API.
     *
     * @return The SCIM2 ID of the created user.
     * @throws Exception if the creation fails.
     */
    private String createNonSuperAdminUser() throws Exception {

        HttpPost request = new HttpPost(SERVER_URL + SCIM2_USERS_ENDPOINT);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(adminUsername, adminPassword));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, "Non Super Admin");
        names.put(GIVEN_NAME_ATTRIBUTE, "Test");

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, NON_SUPER_ADMIN_USERNAME);
        rootObject.put(PASSWORD_ATTRIBUTE, NON_SUPER_ADMIN_PASSWORD);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, "work");
        emailWork.put(VALUE_PARAM, NON_SUPER_ADMIN_EMAIL);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        rootObject.put(EMAILS_ATTRIBUTE, emails);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Non-super-admin user creation failed.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        return ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
    }

    /**
     * Adds the given user to the admin role using the SCIM2 Groups/Roles API.
     *
     * @param userId The SCIM2 ID of the user to add to the admin role.
     * @throws Exception if the operation fails.
     */
    private void addUserToAdminRole(String userId) throws Exception {

        // Find the admin group ID.
        String filterPath = SERVER_URL + SCIM2_GROUPS_ENDPOINT
                + "?filter=displayName+Eq+" + ADMIN_ROLE_NAME;
        HttpGet getRequest = new HttpGet(filterPath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(adminUsername, adminPassword));
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        getRequest.addHeader(HttpHeaders.ACCEPT, "application/scim+json");

        HttpResponse getResponse = client.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Failed to retrieve the admin group.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(getResponse.getEntity()));
        EntityUtils.consume(getResponse.getEntity());

        JSONArray resources = (JSONArray) ((JSONObject) responseObj).get("Resources");
        if (resources == null || resources.isEmpty()) {
            LOG.warn("Admin group not found; skipping role assignment.");
            return;
        }
        String adminGroupId = ((JSONObject) resources.get(0)).get(ID_ATTRIBUTE).toString();

        // Patch the admin group to add the non-super-admin user.
        String groupResourcePath = SERVER_URL + SCIM2_GROUPS_ENDPOINT + "/" + adminGroupId;
        HttpPatch patchRequest = new HttpPatch(groupResourcePath);
        patchRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(adminUsername, adminPassword));
        patchRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject patchBody = new JSONObject();
        JSONArray patchSchemas = new JSONArray();
        patchSchemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        patchBody.put(SCHEMAS_ATTRIBUTE, patchSchemas);

        JSONObject memberEntry = new JSONObject();
        memberEntry.put("display", NON_SUPER_ADMIN_USERNAME);
        memberEntry.put(VALUE_PARAM, userId);

        JSONArray memberValues = new JSONArray();
        memberValues.add(memberEntry);

        JSONObject operation = new JSONObject();
        operation.put("op", "add");
        operation.put("path", "members");
        operation.put(VALUE_PARAM, memberValues);

        JSONArray operations = new JSONArray();
        operations.add(operation);
        patchBody.put("Operations", operations);

        StringEntity patchEntity = new StringEntity(patchBody.toString());
        patchRequest.setEntity(patchEntity);

        HttpResponse patchResponse = client.execute(patchRequest);
        int patchStatus = patchResponse.getStatusLine().getStatusCode();
        EntityUtils.consume(patchResponse.getEntity());

        LOG.info("Add user to admin role response status: " + patchStatus);
    }

    /**
     * Deletes a user with the given SCIM2 ID using the specified credentials.
     *
     * @param userId   The SCIM2 ID of the user to delete.
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @throws Exception if the deletion fails.
     */
    private void deleteUser(String userId, String username, String password) throws Exception {

        String userResourcePath = SERVER_URL + SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());
    }

    private String getAuthzHeader(String username, String password) {

        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
    }
}
