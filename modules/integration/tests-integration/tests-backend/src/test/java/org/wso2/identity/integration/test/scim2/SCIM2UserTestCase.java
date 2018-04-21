package org.wso2.identity.integration.test.scim2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

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
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PHONE_NUMBERS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PHONE_NUMBERS_TYPE_HOME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PHOTOS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCHEMAS_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

public class SCIM2UserTestCase extends ISIntegrationTest {

    private static final String FAMILY_NAME_CLAIM_VALUE = "scim";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE = "scim2user@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE = "scim2user@gmail.com";
    private static final int PHONE_NUMBERS_TYPE_HOME_CLAIM_VALUE = 123456;
    private static final String PHOTOS_1 = "photo1";
    private static final String PHOTOS_2 = "photo2";
    public static final String USERNAME = "scim2user";
    public static final String PASSWORD = "testPassword";

    private CloseableHttpClient client;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        client = HttpClients.createDefault();
    }

    private String userId;

    private String adminUsername;
    private String adminPassword;
    private String tenant;

    @Factory(dataProvider = "SCIM2UserConfigProvider")
    public SCIM2UserTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "SCIM2UserConfigProvider")
    public static Object[][] sCIM2UserConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testCreateUser() throws Exception {
        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
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
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        JSONArray photos = new JSONArray();
        photos.add(PHOTOS_1);
        photos.add(PHOTOS_2);

        JSONObject phoneNumberHome = new JSONObject();
        phoneNumberHome.put(TYPE_PARAM, PHONE_NUMBERS_TYPE_HOME_ATTRIBUTE);
        phoneNumberHome.put(VALUE_PARAM, PHONE_NUMBERS_TYPE_HOME_CLAIM_VALUE);

        JSONArray phoneNumbers = new JSONArray();
        phoneNumbers.add(phoneNumberHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        rootObject.put(PHOTOS_ATTRIBUTE, photos);

        rootObject.put(PHONE_NUMBERS_ATTRIBUTE, phoneNumbers);

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

    @Test(dependsOnMethods = "testCreateUser")
    public void testGetUser() throws Exception {
        String userResourcePath = getPath() + "/" + userId;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        String addressesFromResponse = ((JSONObject) responseObj).get(PHOTOS_ATTRIBUTE).toString();
        boolean hasAddressClaims = false;
        if (addressesFromResponse.contains(PHOTOS_1) && addressesFromResponse.contains(PHOTOS_2)) {
            hasAddressClaims = true;
        }
        assertTrue(hasAddressClaims, "Address claims have not been retrieved successfully.");

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = "testGetUser")
    public void testFilterUser() throws Exception {
        String userResourcePath = getPath() + "?filter=" + USER_NAME_ATTRIBUTE + "+Eq+" + USERNAME;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0))
                .get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        String userId = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                (ID_ATTRIBUTE).toString();
        assertEquals(userId, this.userId);
    }

    @Test(dependsOnMethods = "testFilterUser")
    public void testFilterUserByMultiValuedClaims() throws Exception {
        String userResourcePath = getPath() + "?filter=" + EMAILS_ATTRIBUTE + "." + EMAIL_TYPE_HOME_ATTRIBUTE + "+Eq+" +
                EMAIL_TYPE_HOME_CLAIM_VALUE;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0))
                .get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        String userId = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                (ID_ATTRIBUTE).toString();
        assertEquals(userId, this.userId);
    }

    @Test(dependsOnMethods = "testFilterUserByMultiValuedClaims")
    public void testFilterUserByNumericClaims() throws Exception {
        String userResourcePath = getPath() + "?filter=" + PHONE_NUMBERS_ATTRIBUTE + "." + PHONE_NUMBERS_TYPE_HOME_ATTRIBUTE + "+Eq+" +
                PHONE_NUMBERS_TYPE_HOME_CLAIM_VALUE;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0))
                .get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        String userId = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                (ID_ATTRIBUTE).toString();
        assertEquals(userId, this.userId);
    }

    @Test(dependsOnMethods = "testFilterUserByNumericClaims")
    public void testUpdateUser() throws Exception {

        String updatedPassword = "updatedPassword";
        String updatedGivenName = "updatedGivenName";

        HttpPut request = new HttpPut(getPath() + "/" + userId);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, FAMILY_NAME_CLAIM_VALUE);
        names.put(GIVEN_NAME_ATTRIBUTE, updatedGivenName);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);

        JSONObject emailWork = new JSONObject();
        emailWork.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);

        JSONArray photos = new JSONArray();
        photos.add(PHOTOS_1);
        photos.add(PHOTOS_2);

        JSONObject phoneNumberHome = new JSONObject();
        phoneNumberHome.put(TYPE_PARAM, PHONE_NUMBERS_TYPE_HOME_ATTRIBUTE);
        phoneNumberHome.put(VALUE_PARAM, PHONE_NUMBERS_TYPE_HOME_CLAIM_VALUE);

        JSONArray phoneNumbers = new JSONArray();
        phoneNumbers.add(phoneNumberHome);

        rootObject.put(EMAILS_ATTRIBUTE, emails);
        rootObject.put(PASSWORD_ATTRIBUTE, updatedPassword);
        rootObject.put(PHOTOS_ATTRIBUTE, photos);
        rootObject.put(PHONE_NUMBERS_ATTRIBUTE, phoneNumbers);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been updated successfully.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String givenNameFromResponse = ((JSONObject) responseObj).get(NAME_ATTRIBUTE).toString();
        boolean hasUpdatedGivenName = false;
        if (givenNameFromResponse.contains(updatedGivenName)) {
            hasUpdatedGivenName = true;
        }
        assertTrue(hasUpdatedGivenName, "Response does not contain updated given name.");

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = "testUpdateUser")
    public void testDeleteUser() throws Exception {
        String userResourcePath = getPath() + "/" + userId;
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

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private String getPath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }
}
