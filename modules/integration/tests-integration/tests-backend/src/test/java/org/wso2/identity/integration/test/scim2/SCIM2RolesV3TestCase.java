package org.wso2.identity.integration.test.scim2;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.*;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.UUID;

import static org.testng.Assert.*;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.*;

public class SCIM2RolesV3TestCase extends ISIntegrationTest {

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;

    private String roleId;
    private String uniqueUserName;
    private final String adminUsername;
    private final String password;
    private final String tenant;

    private static final String GROUP_NAME = "dummyGroup";
    public static final String ID_ATTRIBUTE = "id";
    private static final String TEST_USER_PASSWORD = "Sample1$";
    private static final String TEST_USER_NAME = "dummyName1";

    @DataProvider(name = "SCIM2MeConfigProvider")
    public static Object[][] SCIM2MeConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2RolesV3TestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        client = HttpClients.createDefault();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    @Test(priority = 1)
    public void testCreateRole() throws Exception {

        JSONObject roleCreationRequest = new JSONObject();
        roleCreationRequest.put("schemas", new JSONArray().put("[]"));
        roleCreationRequest.put("displayName", "Manager19");

        JSONArray permissions = new JSONArray();
        JSONObject permission = new JSONObject();
        permission.put("value", "internal_offline_invite");
        permission.put("display", "Offline invite link");
        permissions.put(permission);
        roleCreationRequest.put("permissions", permissions);

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity entity = new StringEntity(roleCreationRequest.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        roleId = json.getString(ID_ATTRIBUTE);
        assertNotNull(roleId, "Role id is null");
    }

    @Test(priority = 2, dependsOnMethods = "testCreateRole")
    public void testPatchAssignUserToRole() throws Exception {

        String userId = createUsersForTesting();
        assertNotNull(userId, "User ID is null");

        // Prepare SCIM PATCH body
        JSONObject patchRequest = new JSONObject();
        patchRequest.put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:PatchOp"));

        JSONArray operations = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("op", "add");

        // Add user to be assigned
        JSONObject userObj = new JSONObject();
        userObj.put("display", uniqueUserName);
        userObj.put("value", userId);

        JSONObject valueObj = new JSONObject();
        valueObj.put("users", new JSONArray().put(userObj));
        operation.put("value", valueObj);
        operations.put(operation);

        patchRequest.put("Operations", operations);

        HttpPatch patch = new HttpPatch(getPath() + "/" + roleId + "/Users");
        patch.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        patch.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        patch.setEntity(new StringEntity(patchRequest.toString(), "UTF-8"));

        // Execute and validate
        HttpResponse response = client.execute(patch);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        assertEquals(statusCode, 200, "Failed to assign user to role via PATCH.");
    }

    @Test(priority = 3, dependsOnMethods = "testCreateRole")
    public void testPutAssignUserToRole() throws Exception {

        // Create a new user for testing
        String userId = createUsersForTesting();
        assertNotNull(userId, "User ID is null");

        // Construct the request body
        JSONObject requestBody = new JSONObject();
        JSONArray usersArray = new JSONArray();
        usersArray.put(new JSONObject().put("value", userId));
        requestBody.put("users", usersArray);

        // Create PUT request to /Roles/{roleId}/Users
        HttpPut put = new HttpPut(getPath() + "/" + roleId + "/Users");
        put.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        put.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

        // Execute request and validate
        HttpResponse response = client.execute(put);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        assertEquals(statusCode, 200, "Failed to assign user to role via PUT.");
    }

    @Test(priority = 4, dependsOnMethods = "testCreateRole")
    public void testPutUpdateRoleMetaData() throws Exception {

        JSONObject request = new JSONObject();
        request.put("displayName", "loginRoleUpdated");

        JSONArray permissions = new JSONArray();
        JSONObject permission = new JSONObject();
        permission.put("value", "internal_group_mgt_view");
        permission.put("display", "View Group");
        permissions.put(permission);
        request.put("permissions", permissions);

        HttpPut put = new HttpPut(getPath() + "/" + roleId);
        put.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        put.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity(request.toString()));

        HttpResponse response = client.execute(put);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    @Test(priority = 5, dependsOnMethods = "testCreateRole")
    public void testPatchUpdateMetaData() throws Exception {

        JSONObject patchRequest = new JSONObject();
        patchRequest.put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:PatchOp"));
        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "replace");
        JSONObject value = new JSONObject();
        JSONArray permissions = new JSONArray();
        permissions.put(new JSONObject().put("value", "internal_role_mgt_view"));
        value.put("permissions", permissions);
        op.put("value", value);

        operations.put(op);
        patchRequest.put("Operations", operations);

        HttpPatch patch = new HttpPatch(getPath() + "/" + roleId);
        patch.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        patch.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        patch.setEntity(new StringEntity(patchRequest.toString()));

        HttpResponse response = client.execute(patch);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    @Test(priority = 6, dependsOnMethods = "testCreateRole")
    public void testPatchAssignGroupToRole() throws Exception {

        // Create group and retrieve its ID
        String groupId = createGroupForTesting();
        assertNotNull(groupId, "Group ID is null");

        // Construct SCIM PATCH request
        JSONObject patchRequest = new JSONObject();
        patchRequest.put("schemas", new JSONArray().put("urn:ietf:params:scim:api:messages:2.0:PatchOp"));

        JSONArray operations = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("op", "add");

        JSONObject groupValue = new JSONObject();
        groupValue.put("value", groupId);

        JSONObject valueObj = new JSONObject();
        valueObj.put("groups", new JSONArray().put(groupValue));
        operation.put("value", valueObj);

        operations.put(operation);
        patchRequest.put("Operations", operations);

        // Send PATCH request to /Roles/{roleId}/Groups
        HttpPatch patch = new HttpPatch(getPath() + "/" + roleId + "/Groups");
        patch.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        patch.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        patch.setEntity(new StringEntity(patchRequest.toString(), "UTF-8"));

        HttpResponse response = client.execute(patch);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        assertEquals(statusCode, 200, "Failed to assign group to role via PATCH.");
    }

    @Test(priority = 7, dependsOnMethods = "testCreateRole")
    public void testPutAssignGroupToRole() throws Exception {

        String groupId = createGroupForTesting();
        assertNotNull(groupId, "Group ID is null");

        // Build JSON payload for PUT request
        JSONObject requestBody = new JSONObject();
        JSONArray groupsArray = new JSONArray();
        groupsArray.put(new JSONObject().put("value", groupId));
        requestBody.put("groups", groupsArray);

        // Prepare PUT request to /Roles/{roleId}/Groups
        HttpPut put = new HttpPut(getPath() + "/" + roleId + "/Groups");
        put.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        put.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

        // Execute and validate
        HttpResponse response = client.execute(put);
        int statusCode = response.getStatusLine().getStatusCode();
        EntityUtils.consume(response.getEntity());

        assertEquals(statusCode, 200, "Failed to assign group to role via PUT.");
    }

    private String getPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_ROLES_V3_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_ROLES_V3_ENDPOINT;
        }
    }

    private String getAuthzHeader() {

        return "Basic " + org.apache.commons.codec.binary.Base64.encodeBase64String(
                (adminUsername + ":" + password).getBytes()).trim();
    }

    private String createUsersForTesting() throws Exception {

        uniqueUserName = getUniqueUserName();

         return scim2RestClient.createUser(new UserObject()
                .userName(uniqueUserName)
                .password(TEST_USER_PASSWORD));
    }

    public String createGroupForTesting() throws Exception {

        String uniqueGroupName = GROUP_NAME + "_" + UUID.randomUUID().toString().split("-")[0];
        return scim2RestClient.createGroup(new GroupRequestObject()
                .displayName(uniqueGroupName));
    }

    private String getUniqueUserName() {

        return TEST_USER_NAME + "_" + UUID.randomUUID().toString().split("-")[0];
    }
}
