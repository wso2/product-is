/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class SCIM2MultiAttributeUserFilterTestCase extends ISIntegrationTest {

    private static final List<String> GROUPNAMES = new ArrayList<>(Arrays.asList("scim2Group1", "scim2Group2"));

    private static final List<String> FAMILY_NAME_CLAIM_VALUES = new ArrayList<>(
            Arrays.asList("scim1user", "scim2user", "scim3user", "scim4user", "scim5user"));
    private static final List<String> GIVEN_NAME_CLAIM_VALUES = new ArrayList<>(
            Arrays.asList("user1", "user2", "user3", "user4", "user5"));
    private static final List<String> EMAIL_TYPE_WORK_CLAIM_VALUES = new ArrayList<>(
            Arrays.asList("user1@wso2.com", "user2@wso2.com", "user3@wso2.com", "user4@wso2.com", "user5@wso2.com"));
    private static final List<String> EMAIL_TYPE_HOME_CLAIM_VALUES = new ArrayList<>(
            Arrays.asList("scim1@gmail.com", "scim2@gmail.com", "scim3@gmail.com", "scim4@gmail.com",
                    "scim5@gmail.com"));
    public static final List<String> USERNAMES = new ArrayList<>(
            Arrays.asList("scim2user1", "scim2user2", "scim2user3", "scim2user4", "scim2user5"));
    public static final List<String> PASSWORDS = new ArrayList<>(
            Arrays.asList("testPassword1", "testPassword2", "testPassword3", "testPassword4", "testPassword5"));

    private CloseableHttpClient client;

    private static final String EQUAL = "+Eq+";
    private static final String STARTWITH = "+Sw+";
    private static final String ENDWITH = "+Ew+";
    private static final String CONTAINS = "+Co+";
    private static final String AND_OPERATION = "+and+";
    private static final String GROUPS = "groups";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        client = HttpClients.createDefault();
    }

    List<String> userIds = new ArrayList<>();
    List<String> groupIds = new ArrayList<>();

    private String adminUsername;
    private String adminPassword;
    private String tenant;

    @Factory(dataProvider = "SCIM2UserConfigProvider")
    public SCIM2MultiAttributeUserFilterTestCase(TestUserMode userMode) throws Exception {

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

        for (int i = 0; i < USERNAMES.size(); i++) {
            createUser(FAMILY_NAME_CLAIM_VALUES.get(i), GIVEN_NAME_CLAIM_VALUES.get(i), USERNAMES.get(i),
                    EMAIL_TYPE_WORK_CLAIM_VALUES.get(i), EMAIL_TYPE_HOME_CLAIM_VALUES.get(i), PASSWORDS.get(i));
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
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, userName);

        String userId;
        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        userIds.add(userId);
        assertNotNull(userId);
    }

    @Test(dependsOnMethods = "testCreateUser")
    public void testCreateGroup() throws Exception {

        createGroup(USERNAMES.subList(0, 3), userIds.subList(0, 3), GROUPNAMES.get(0));
        createGroup(USERNAMES.subList(2, USERNAMES.size()), userIds.subList(2, USERNAMES.size()), GROUPNAMES.get(1));
    }

    private void createGroup(List<String> userNames, List<String> userIDs, String groupName) throws IOException {

        HttpPost request = new HttpPost(getGroupPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray members = new JSONArray();
        for (int i = 0; i < userNames.size(); i++) {
            JSONObject member = new JSONObject();
            member.put(SCIM2BaseTestCase.DISPLAY_ATTRIBUTE, userNames.get(i));
            member.put(VALUE_PARAM, userIDs.get(i));
            members.add(member);
        }

        rootObject.put(SCIM2BaseTestCase.DISPLAY_NAME_ATTRIBUTE, groupName);

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
        assertTrue(groupNameFromResponse.contains(groupName));

        String groupId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        groupIds.add(groupId);
        assertNotNull(groupId);
    }

    @Test(dependsOnMethods = "testCreateGroup")
    public void testMultiAttributeFilterUser() throws Exception {

        //Validate username and claim filter
        StringBuilder query = new StringBuilder(USER_NAME_ATTRIBUTE).append(ENDWITH).
                append(USERNAMES.get(2).substring(4, USERNAMES.get(2).length())).append(AND_OPERATION).
                append(NAME_ATTRIBUTE).append(".").append(FAMILY_NAME_ATTRIBUTE).append(EQUAL).
                append(FAMILY_NAME_CLAIM_VALUES.get(2));

        try {
            validateMultiAttributeFilteredUser(query.toString(), Arrays.asList(userIds.get(2)),
                    Arrays.asList(USERNAMES.get(2)));
        } catch (Exception e) {
            log.error("Error while filtering the attributes of the user: " + e.getMessage(), e);
            throw e;
        }

        //Validate username, claim and group filter
        query = new StringBuilder(USER_NAME_ATTRIBUTE).append(ENDWITH).
                append(USERNAMES.get(1).substring(4, USERNAMES.get(1).length())).append(AND_OPERATION).append(GROUPS).
                append(EQUAL).append(GROUPNAMES.get(0)).append(AND_OPERATION).append(NAME_ATTRIBUTE).append(".").
                append(GIVEN_NAME_ATTRIBUTE).append(EQUAL).append(GIVEN_NAME_CLAIM_VALUES.get(1));

        validateMultiAttributeFilteredUser(query.toString(), Arrays.asList(userIds.get(1)),
                Arrays.asList(USERNAMES.get(1)));

        //Validate groups filter
        query = new StringBuilder(GROUPS).append(EQUAL).append(GROUPNAMES.get(0)).append(AND_OPERATION).
                append(GROUPS).append(EQUAL).append(GROUPNAMES.get(1));

        validateMultiAttributeFilteredUser(query.toString(), Arrays.asList(userIds.get(2)),
                Arrays.asList(USERNAMES.get(2)));

        query = new StringBuilder(USER_NAME_ATTRIBUTE).append(STARTWITH).
                append(USERNAMES.get(0).substring(0, 3)).append(AND_OPERATION).append(USER_NAME_ATTRIBUTE).
                append(CONTAINS).append(USERNAMES.get(0).substring(2, 4)).append(AND_OPERATION).append(NAME_ATTRIBUTE).
                append(".").append(FAMILY_NAME_ATTRIBUTE).append(ENDWITH).append(
                FAMILY_NAME_CLAIM_VALUES.get(0).substring(5, 9));

        validateMultiAttributeFilteredUser(query.toString(), userIds, USERNAMES);

        //Validate pagination
        query.append("&count=3&startIndex=2");
        validateMultiAttributeFilteredUser(query.toString(), userIds.subList(1, 4), USERNAMES.subList(1, 4));
    }

    private void validateMultiAttributeFilteredUser(String filter, List<String> userIds, List<String> userNames)
            throws IOException {

        String userResourcePath = getUserPath() + "?filter=" + filter;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);

        String responseString = EntityUtils.toString(response.getEntity());
        Object responseObj = JSONValue.parse(responseString);

        if (response.getStatusLine().getStatusCode() != 200) {
            log.error("Incorrect response code received. Received error code: "
                    + response.getStatusLine().getStatusCode() + " . Error message: " + responseString);
        }

        assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        int resultCount = (((JSONArray) ((JSONObject) responseObj).get("Resources")).size());
        List<String> usernamesFromResponse = new ArrayList<>();
        List<String> userIDsFromResponse = new ArrayList<>();
        for (int i = 0; i < resultCount; i++) {
            usernamesFromResponse.add(((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).
                    get(i)).get(USER_NAME_ATTRIBUTE).toString());

            userIDsFromResponse.add(((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).
                    get(i)).get(ID_ATTRIBUTE).toString());
        }

        assertEquals(resultCount, userIds.size());
        assertEquals(usernamesFromResponse, userNames);
        assertEquals(userIDsFromResponse, userIds);
    }

    @Test(dependsOnMethods = "testMultiAttributeFilterUser")
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
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getUserPath() + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testDeleteUser")
    public void testDeleteGroup() throws Exception {

        for (String groupId : groupIds) {
            deleteGroup(groupId);
        }
    }

    private void deleteGroup(String groupId) throws IOException {

        String userResourcePath = getGroupPath() + "/" + groupId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getGroupPath() + "/" + groupId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        response = client.execute(getRequest);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private String getUserPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getGroupPath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_GROUPS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenant + SCIM2_GROUPS_ENDPOINT;
        }
    }

    private String getAuthzHeader() {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }

}
