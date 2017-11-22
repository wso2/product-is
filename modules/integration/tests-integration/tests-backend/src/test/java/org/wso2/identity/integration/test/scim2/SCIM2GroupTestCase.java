package org.wso2.identity.integration.test.scim2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_TYPE_HOME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAIL_TYPE_WORK_ATTRIBUTE;
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

public class SCIM2GroupTestCase extends ISIntegrationTest {

    private static final String GROUPNAME = "scim2Group";
    private static final String PASSWORD = "testPassword";

    private static final String FAMILY_NAME_CLAIM_VALUE_1 = "scim1";
    private static final String GIVEN_NAME_CLAIM_VALUE_1 = "user1";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE_1 = "scim2user1@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_1 = "scim2user1@gmail.com";
    private static final String USERNAME_1 = "scim2user1";

    private static final String FAMILY_NAME_CLAIM_VALUE_2 = "scim2";
    private static final String GIVEN_NAME_CLAIM_VALUE_2 = "user2";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE_2 = "scim2user2@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_2 = "scim2user2@gmail.com";
    private static final String USERNAME_2 = "scim2user2";

    private CloseableHttpClient client;
    private String userId1;
    private String userId2;

    private String adminUsername;
    private String password;
    private String tenant;

    private String groupId;


    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2GroupTestCase(TestUserMode userMode) throws Exception {

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

    @BeforeClass
    public void testInit() throws Exception {
        super.init();
        client = HttpClients.createDefault();

        HttpResponse response = createUser(
                USERNAME_1,
                FAMILY_NAME_CLAIM_VALUE_1,
                GIVEN_NAME_CLAIM_VALUE_1,
                EMAIL_TYPE_WORK_CLAIM_VALUE_1,
                EMAIL_TYPE_HOME_CLAIM_VALUE_1);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId1 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();

        response = createUser(
                USERNAME_2,
                FAMILY_NAME_CLAIM_VALUE_2,
                GIVEN_NAME_CLAIM_VALUE_2,
                EMAIL_TYPE_WORK_CLAIM_VALUE_2,
                EMAIL_TYPE_HOME_CLAIM_VALUE_2);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId2 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
    }

    @AfterClass
    public void deleteUsers() throws IOException {
        HttpResponse response = deleteUser(userId1);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());

        response = deleteUser(userId2);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
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

    private HttpResponse createUser(String username, String familyName, String givenName, String workEmail, String
            homeEmail) throws IOException {
        HttpPost request = new HttpPost(getUsersPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, familyName);
        names.put(GIVEN_NAME_ATTRIBUTE, givenName);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, username);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(VALUE_PARAM, workEmail);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(VALUE_PARAM, homeEmail);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        return client.execute(request);
    }

    @Test
    public void testCreateGroup() throws Exception {
        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONObject member1 = new JSONObject();
        member1.put(SCIM2BaseTestCase.DISPLAY_ATTRIBUTE, USERNAME_1);
        member1.put(VALUE_PARAM, userId1);

        JSONObject member2 = new JSONObject();
        member2.put(SCIM2BaseTestCase.DISPLAY_ATTRIBUTE, USERNAME_2);
        member2.put(VALUE_PARAM, userId2);

        JSONArray members = new JSONArray();
        members.add(member1);
        members.add(member2);

        rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, GROUPNAME);

        rootObject.put(SCIM2BaseTestCase.MEMBERS_ATTRIBUTE, members);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);

        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String groupNameFromResponse = ((JSONObject) responseObj).get(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE)
                .toString();
        //TODO: groupsName comes from this is <Userstore_domain>/displayName
        assertTrue(groupNameFromResponse.contains(GROUPNAME));

        groupId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId);
    }

    @Test(dependsOnMethods = "testCreateGroup")
    public void testGetGroup() throws Exception {
        String userResourcePath = getPath() + "/" + groupId;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String groupNameFromResponse = ((JSONObject) responseObj).get(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE)
                .toString();
        //TODO: groupsName comes from this is <Userstore_domain>/displayName
        assertTrue(groupNameFromResponse.contains(GROUPNAME));

        groupId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId);
    }

    @Test(dependsOnMethods = "testGetGroup")
    public void testFilterGroup() throws Exception {
        String userResourcePath = getPath() + "?filter=" + SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE + "+Eq+" +
                GROUPNAME;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String groupNameFromResponse = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0))
                .get(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE).toString();
        //TODO: groupsName comes from this is <Userstore_domain>/displayName
        assertTrue(groupNameFromResponse.contains(GROUPNAME));

        String groupId = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                (ID_ATTRIBUTE).toString();
        assertEquals(groupId, this.groupId);
    }

    @Test(dependsOnMethods = "testFilterGroup")
    public void testDeleteGroup() throws Exception {
        String userResourcePath = getPath() + "/" + groupId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getPath() + "/" + groupId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private String getPath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_GROUPS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_GROUPS_ENDPOINT;
        }
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
}
