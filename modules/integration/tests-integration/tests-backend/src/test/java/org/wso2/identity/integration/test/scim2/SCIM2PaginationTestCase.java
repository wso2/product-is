/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.TYPE_PARAM;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.USER_NAME_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.VALUE_PARAM;

/**
 * Test class to test the SCIM2 pagination.
 */
public class SCIM2PaginationTestCase extends ISIntegrationTest {

    public static final String TOTAL_RESULTS_ATTRIBUTE = "totalResults";
    public static final String RESOURCES_ATTRIBUTE = "Resources";
    public static final String START_INDEX_ATTRIBUTE = "startIndex";
    public static final String ITEMS_PER_PAGE_ATTRIBUTE = "itemsPerPage";
    public static final String START_INDEX = "2";
    public static final String COUNT = "4";
    private String adminUsername;
    private String adminPassword;
    private String tenant;
    List<String> userIds = new ArrayList<>();
    UserManagementClient userMgtClient;
    TestUserMode userMode;
    private CloseableHttpClient client;

    @Factory(dataProvider = "scim2UserConfigProvider")
    public SCIM2PaginationTestCase(TestUserMode userMode) throws Exception {

        this.userMode = userMode;
    }

    @DataProvider(name = "scim2UserConfigProvider")
    public static Object[][] scim2UserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(userMode);
        this.adminUsername = isServer.getContextTenant().getTenantAdmin().getUserName();
        this.adminPassword = isServer.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = isServer.getContextTenant().getDomain();

        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        client = HttpClients.createDefault();
    }

    /*
    Validate user listing with pagination.
    */
    @Test(dependsOnMethods = "testCreateUser")
    public void testUserListingPagination() throws Exception {

        int currentUsers = getCurrentUsersCount();
        HttpGet request = new HttpGet(getUserPath() + "?startIndex=" + START_INDEX + "&count=" + COUNT);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Users " +
                "has not been retrieved successfully.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String totalResults = ((JSONObject) responseObj).get(TOTAL_RESULTS_ATTRIBUTE).toString();
        assertEquals(totalResults, String.valueOf(currentUsers), "Total results in pagination listing is incorrect.");

        String startIndex = ((JSONObject) responseObj).get(START_INDEX_ATTRIBUTE).toString();
        assertEquals(startIndex, START_INDEX, "StartIndex in pagination listing is incorrect.");

        String itemsPerPage = ((JSONObject) responseObj).get(ITEMS_PER_PAGE_ATTRIBUTE).toString();
        assertEquals(itemsPerPage, COUNT, "ItemsPerPage in pagination listing is incorrect.");

        int resourcesSize = ((JSONArray) ((JSONObject) responseObj).get(RESOURCES_ATTRIBUTE)).size();
        assertEquals(String.valueOf(resourcesSize), COUNT, "Resources size in pagination listing is incorrect.");

    }

    /*
    Validating legacy behavior support (returning all the results for not specified list param).
     */
    @Test(dependsOnMethods = "testCreateUser")
    public void testUserListingPaginationWithoutParams() throws Exception {

        int currentUsers = getCurrentUsersCount();

        HttpGet request = new HttpGet(getUserPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "Users " +
                "has not been retrieved successfully.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        JSONArray resourcesResponse = (JSONArray) ((JSONObject) ((Object) responseObj)).get(RESOURCES_ATTRIBUTE);

        String totalResults = ((JSONObject) responseObj).get(TOTAL_RESULTS_ATTRIBUTE).toString();
        assertEquals(totalResults, String.valueOf(currentUsers), "Total results in pagination listing is incorrect.");

        String startIndex = ((JSONObject) responseObj).get(START_INDEX_ATTRIBUTE).toString();
        assertEquals(startIndex, "1", "StartIndex in pagination listing is incorrect.");

        String itemsPerPage = ((JSONObject) responseObj).get(ITEMS_PER_PAGE_ATTRIBUTE).toString();
        assertEquals(itemsPerPage, String.valueOf(currentUsers), "ItemsPerPage in pagination listing is incorrect.");

        int resourcesSize = ((JSONArray) ((JSONObject) responseObj).get(RESOURCES_ATTRIBUTE)).size();
        assertEquals(resourcesSize, currentUsers, "Resources size in pagination listing is incorrect.");

        assertTrue(isAllUsersExists(resourcesResponse));

    }

    @Test
    public void testCreateUser() throws Exception {

        for (int i = 0; i < 10; i++) {
            createUser(String.format("Family%s", i), String.format("user%s", i), String.format("user%s", i),
                    String.format("user%s@gmail.com", i), String.format("user%s@gmail.com", i), String.format("dummy@PW%s", i));
        }
    }

    private void createUser(String familyName, String givenName, String userName, String workEmail, String homeEmail,
                            String password) throws IOException {

        HttpPost request = new HttpPost(getUserPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(FAMILY_NAME_ATTRIBUTE, familyName);
        names.put(GIVEN_NAME_ATTRIBUTE, givenName);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, userName);

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

        rootObject.put(PASSWORD_ATTRIBUTE, password);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User has not been created successfully.");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, userName);

        String userId;
        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        userIds.add(userId);
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = {"testUserListingPagination", "testUserListingPaginationWithoutParams"})
    public void testDeleteUser() throws Exception {

        for (String userId : userIds) {
            deleteUser(userId);
        }
    }

    private void deleteUser(String userId) throws IOException {

        String userResourcePath = getUserPath() + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User has not been retrieved successfully.");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getUserPath() + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User has not been deleted successfully.");
        EntityUtils.consume(response.getEntity());
    }

    private int getCurrentUsersCount() throws Exception {

        return userMgtClient.getUserList().size();
    }

    private boolean isAllUsersExists(JSONArray response) throws Exception {

        boolean isUsersExists = false;
        Set<String> usersList = new HashSet<>();
        for (Object user : response) {
            usersList.add(((JSONObject) user).get(USER_NAME_ATTRIBUTE).toString());
        }
        if (userMgtClient.getUserList().equals(usersList)) {
            isUsersExists = true;
        }
        return isUsersExists;
    }

    private String getUserPath() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }
}
