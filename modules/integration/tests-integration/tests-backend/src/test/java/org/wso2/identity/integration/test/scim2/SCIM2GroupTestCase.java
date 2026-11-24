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
import org.wso2.identity.integration.test.rest.api.user.common.model.GroupRequestObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final String PASSWORD = "Wso2@test";

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

    private static final String FAMILY_NAME_CLAIM_VALUE_3 = "scim2_3";
    private static final String GIVEN_NAME_CLAIM_VALUE_3 = "user_3";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE_3 = "scim2_user3@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_3 = "scim2_user3@gmail.com";
    private static final String USERNAME_3 = "scim2_user3";

    private static final String FAMILY_NAME_CLAIM_VALUE_4 = "scim2_4";
    private static final String GIVEN_NAME_CLAIM_VALUE_4 = "user_4";
    private static final String EMAIL_TYPE_WORK_CLAIM_VALUE_4 = "scim2_user4@wso2.com";
    private static final String EMAIL_TYPE_HOME_CLAIM_VALUE_4 = "scim2_user4@gmail.com";
    private static final String USERNAME_4 = "scim2_user4";

    private static final String EQUAL = "+Eq+";
    private static final String STARTWITH = "+Sw+";
    private static final String ENDWITH = "+Ew+";
    private static final String CONTAINS = "+Co+";

    private CloseableHttpClient client;
    private SCIM2RestClient scim2RestClient;
    private String userId1;
    private String userId2;
    private String userId3;
    private String userId4;

    private TestUserMode userMode;
    private String adminUsername;
    private String password;
    private String tenant;

    private String groupId;
    private String groupId1;

    @Factory(dataProvider = "SCIM2MeConfigProvider")
    public SCIM2GroupTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.adminUsername = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
        this.userMode = userMode;
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
        super.init(userMode);
        client = HttpClients.createDefault();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

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

        response = createUser(
                USERNAME_3,
                FAMILY_NAME_CLAIM_VALUE_3,
                GIVEN_NAME_CLAIM_VALUE_3,
                EMAIL_TYPE_WORK_CLAIM_VALUE_3,
                EMAIL_TYPE_HOME_CLAIM_VALUE_3);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId3 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();

        response = createUser(
                USERNAME_4,
                FAMILY_NAME_CLAIM_VALUE_4,
                GIVEN_NAME_CLAIM_VALUE_4,
                EMAIL_TYPE_WORK_CLAIM_VALUE_4,
                EMAIL_TYPE_HOME_CLAIM_VALUE_4);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userId4 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
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

        scim2RestClient.closeHttpClient();
        client.close();
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

    @Test
    public void testCreateGroupWithCharsetEncodingHeader() throws Exception {

        HttpPost request = new HttpPost(getPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");

        JSONObject rootObject = new JSONObject();

        JSONObject member1 = new JSONObject();
        member1.put(SCIM2BaseTestCase.DISPLAY_ATTRIBUTE, USERNAME_3);
        member1.put(VALUE_PARAM, userId3);

        JSONObject member2 = new JSONObject();
        member2.put(SCIM2BaseTestCase.DISPLAY_ATTRIBUTE, USERNAME_4);
        member2.put(VALUE_PARAM, userId4);

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
        assertTrue(groupNameFromResponse.contains(GROUPNAME));

        groupId1 = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId1);
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

        validateFilteredGroup(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, EQUAL, GROUPNAME);
        validateFilteredGroup(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, STARTWITH, GROUPNAME.substring(0, 4));
        validateFilteredGroup(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, CONTAINS, GROUPNAME.substring(2, 4));
        validateFilteredGroup(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, ENDWITH, GROUPNAME.substring(4, GROUPNAME.length()));
        validateFilteredGroup(SCIM2BaseTestCase.MEMBER_DISPLAY_ATTRIBUTE, EQUAL, USERNAME_1);
        //validateFilteredGroup(SCIM2BaseTestCase.META_LOCATION_ATTRIBUTE, CONTAINS, groupId);
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

        response = client.execute(getRequest);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateGroupWithCharsetEncodingHeader")
    public void testDeleteGroupWithCharsetEncodingHeader() throws Exception {

        String userResourcePath = getPath() + "/" + groupId1;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getPath() + "/" + groupId1;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");

        response = client.execute(getRequest);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testDeleteGroup")
    public void testGetGroupsWithPagination() throws Exception {

        int existingGroupsCount = Integer.parseInt(scim2RestClient.getGroups().get("totalResults").toString());
        List<String> GROUP_LIST = Arrays.asList("testGroup4", "testGroup3", "testGroup2", "testGroup1", "group4",
                "group3", "group2","group1");
        List<String> groupIds = new ArrayList<>();
        for (String groupName : GROUP_LIST) {
            groupIds.add(scim2RestClient.createGroup(new GroupRequestObject().displayName(groupName)));
        }

        validateGroupsFromGetWithPagination(null, null, null, existingGroupsCount + GROUP_LIST.size(),
                existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, 3, null, existingGroupsCount + GROUP_LIST.size() - 2,
                existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, null, 4, 4, existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, 4, 5, 5, existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, 20, 5, 0, existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, Integer.MAX_VALUE, 2, 0, existingGroupsCount + GROUP_LIST.size());
        validateGroupsFromGetWithPagination(null, 6, Integer.MAX_VALUE, existingGroupsCount + GROUP_LIST.size() - 5,
                existingGroupsCount + GROUP_LIST.size());


        String filter = SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE + CONTAINS + "test";
        validateGroupsFromGetWithPagination(filter, null, null, 4, 4);
        validateGroupsFromGetWithPagination(filter, 3, null, 2, 4);
        validateGroupsFromGetWithPagination(filter, null, 3, 3, 4);
        validateGroupsFromGetWithPagination(filter, 1, 2, 2, 4);
        validateGroupsFromGetWithPagination(filter, 9, 3, 0, 4);
        validateGroupsFromGetWithPagination(filter, Integer.MAX_VALUE, 2, 0, 4);
        validateGroupsFromGetWithPagination(filter, 2, Integer.MAX_VALUE, 3, 4);

        for (String groupId : groupIds) {
            scim2RestClient.deleteGroup(groupId);
        }
    }

    private void validateGroupsFromGetWithPagination(String filter, Integer startIndex, Integer count,
                                                     Integer itemsPerPage, Integer totalResult) throws IOException {

        List<String> paramsList = Stream.of(
                        filter != null ? "filter=" + filter : null,
                        startIndex != null ? "startIndex=" + startIndex : null,
                        count != null ? "count=" + count : null)
                .filter(Objects::nonNull).collect(Collectors.toList());

        String endpointUrl = getPath() + (paramsList.isEmpty() ? "" : "?" + String.join("&", paramsList));
        HttpGet request = new HttpGet(endpointUrl);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200,
                "Error while getting groups with pagination");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertEquals(Integer.parseInt(((JSONObject) responseObj).get("totalResults").toString()), totalResult.intValue());
        assertEquals(Integer.parseInt(((JSONObject) responseObj).get("itemsPerPage").toString()), itemsPerPage.intValue());
        assertEquals(Integer.parseInt(((JSONObject) responseObj).get("startIndex").toString()),
                startIndex == null ? 1 : startIndex);
        if (itemsPerPage != 0) {
            assertEquals(((JSONArray) ((JSONObject) responseObj).get("Resources")).size(), itemsPerPage.intValue());
        }
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

    private void validateFilteredGroup(String attributeName, String operator, String searchAttribute) throws IOException {

        String userResourcePath = getPath() + "?filter=" + attributeName + operator +
                searchAttribute;
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
        if (SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE.equals(attributeName)) {
            assertTrue(groupNameFromResponse.contains(searchAttribute));
        } else {
            assertTrue(groupNameFromResponse.contains(GROUPNAME));
        }

        String groupId = ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                (ID_ATTRIBUTE).toString();
        assertEquals(groupId, this.groupId);
    }
}
