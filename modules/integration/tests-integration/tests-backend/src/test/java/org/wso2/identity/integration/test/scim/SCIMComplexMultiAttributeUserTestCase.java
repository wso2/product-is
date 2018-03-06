/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.scim;

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

public class SCIMComplexMultiAttributeUserTestCase extends ISIntegrationTest {

    public static final String SERVER_URL = "https://localhost:9853";
    public static final String SCIM_USERS_ENDPOINT = "/scim/Users";

    public static final String USER_NAME_ATTRIBUTE = "userName";
    public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
    public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    public static final String EMAIL_TYPE_WORK_ATTRIBUTE = "work";
    public static final String EMAIL_TYPE_HOME_ATTRIBUTE = "home";
    public static final String ID_ATTRIBUTE = "id";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String PHONE_NUMBERS_ATTRIBUTE = "phoneNumbers";
    public static final String EMAILS_ATTRIBUTE = "emails";
    public static final String ADDRESSES_ATTRIBUTE = "addresses";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String SCHEMAS_ATTRIBUTE = "schemas";
    public static final String TYPE_PARAM = "type";
    public static final String VALUE_PARAM = "value";

    private static final String FAMILY_NAME_CLAIM_VALUE = "scim";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE = "scimuser@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE = "scimuser@gmail.com";
    public static final String USERNAME = "scimuser";
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

    @Factory(dataProvider = "scimComplexMultiAttributeUserConfigProvider")
    public SCIMComplexMultiAttributeUserTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "scimComplexMultiAttributeUserConfigProvider")
    public static Object[][] scimComplexMultiAttributeUserConfigProvider() {
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

        JSONArray phoneNumbers = getSimpleMultivaluedPhoneNumbersAttribute();
        rootObject.put(PHONE_NUMBERS_ATTRIBUTE, phoneNumbers);

        JSONArray emails = getBasicComplexMultivaluedEmailsAttribute();
        rootObject.put(EMAILS_ATTRIBUTE, emails);

        JSONArray addresses = getAdvancedComplexMultivaluedAddressesAttribute();
        rootObject.put(ADDRESSES_ATTRIBUTE, addresses);

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

    public JSONArray getSimpleMultivaluedPhoneNumbersAttribute() {

        JSONArray phoneNumbers = new JSONArray();
        phoneNumbers.add("tel:+1-201-555-0123'");
        phoneNumbers.add("tel:+1-201-555-0124'");
        return phoneNumbers;
    }

    private JSONArray getBasicComplexMultivaluedEmailsAttribute() {

        JSONObject workEmail = new JSONObject();
        workEmail.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        workEmail.put(VALUE_PARAM, EMAIL_TYPE_WORK_CLAIM_VALUE);

        JSONObject homeEmail = new JSONObject();
        homeEmail.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        homeEmail.put(VALUE_PARAM, EMAIL_TYPE_HOME_CLAIM_VALUE);

        JSONArray emails = new JSONArray();
        emails.add(workEmail);
        emails.add(homeEmail);
        return emails;
    }

    public JSONArray getAdvancedComplexMultivaluedAddressesAttribute() {

        JSONObject workAddress = new JSONObject();
        workAddress.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        workAddress.put("streetAddress", "100 Universal City Plaza");
        workAddress.put("locality", "Manhattan");
        workAddress.put("region", "NY");
        workAddress.put("postalCode", "10022");
        workAddress.put("country", "LK");
        workAddress.put("formatted", "100 Universal City Plaza\\nManhattan, NY 10022 LK");

        JSONObject homeAddress = new JSONObject();
        homeAddress.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        homeAddress.put("streetAddress", "456 Hollywood Blvd");
        homeAddress.put("locality", "Hollywood");
        homeAddress.put("region", "CA");
        homeAddress.put("postalCode", "91608");
        homeAddress.put("country", "USA");
        homeAddress.put("formatted", "456 Hollywood Blvd\\n Hollywood, CA 91608 USA");

        JSONArray addresses = new JSONArray();
        addresses.add(workAddress);
        addresses.add(homeAddress);
        return addresses;
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
            return SERVER_URL + SCIM_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM_USERS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }
}
