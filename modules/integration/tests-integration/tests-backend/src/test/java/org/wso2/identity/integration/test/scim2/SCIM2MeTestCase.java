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
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.EMAILS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.FAMILY_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GIVEN_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PASSWORD_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_ME_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

public class SCIM2MeTestCase extends ISIntegrationTest {

    private static final String FAMILY_NAME_CLAIM_VALUE = "scim";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String EMAIL_TYPE_WORK_CLAIM_URI = "work";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE = "scim2user@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_URI = "home";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE = "scim2user@gmail.com";
    private static final String USERNAME = "scim2user";
    private static final String PASSWORD = "testPassword";

    private CloseableHttpClient client;

    private String adminUsername;
    private String admiPassword;
    private String userId;

    private String tenant;

    @BeforeClass(alwaysRun = true)
    public void initClass() throws Exception {
        super.init();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        HttpResponse response = deleteUser(userId);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private HttpResponse deleteUser(String userId) throws IOException {
        String userResourcePath = getUsersPath() + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAdminAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        if (client == null) {
            client = HttpClients.createDefault();
        }
        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getPath() + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAdminAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        return client.execute(request);
    }

    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2MeTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.admiPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "SCIM2MeConfigProvider")
    public static Object[][] SCIM2MeConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testCreateMe() throws Exception {
        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, FAMILY_NAME_CLAIM_VALUE);
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_CLAIM_URI);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_CLAIM_URI);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = "testCreateMe")
    public void testGetMe() throws Exception {
        HttpGet request = new HttpGet(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = "testGetMe")
    public void testDeleteMe() throws Exception {
        HttpDelete request = new HttpDelete(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 501);
    }

    private String getPath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_ME_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_ME_ENDPOINT;
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
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim();
        } else {
            return "Basic " + Base64.encodeBase64String((USERNAME + "@" + tenant + ":" + PASSWORD).getBytes()).trim();
        }
    }

    private String getAdminAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + admiPassword).getBytes()).trim();
    }
}
