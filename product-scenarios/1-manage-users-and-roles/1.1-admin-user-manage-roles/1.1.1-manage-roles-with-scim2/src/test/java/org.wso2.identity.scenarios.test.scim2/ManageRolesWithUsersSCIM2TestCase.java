/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.scenarios.test.scim2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIM2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class ManageRolesWithUsersSCIM2TestCase extends ScenarioTestBase {

    public static final String ID_ATTRIBUTE = "id";
    public static final String DISPLAY_ATTRIBUTE = "display";
    public static final String VALUE_PARAM = "value";
    public static final String MEMBERS_ATTRIBUTE = "members";

    private String username;
    private String password;
    private String tenantDomain;
    private String groupInputFileName;
    private String userInputFileName;
    private CloseableHttpClient client;
    private SCIM2CommonClient scim2Client;
    private String groupId;
    private HashMap<String, String> users = new HashMap<>();

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManageRolesWithUsersSCIM2TestCase(String username, String password, String tenantDomain,
            String userInputFileName, String groupInputFileName) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.userInputFileName = userInputFileName;
        this.groupInputFileName = groupInputFileName;
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim2User.json", "scim2Group1.json"
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        scim2Client = new SCIM2CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
    }

    @Test
    public void testProvisionUser() throws Exception {
        JSONObject userJSON = scim2Client.getUserJSON(userInputFileName);
        HttpResponse response = scim2Client.provisionUser(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "User has not been created successfully");
        JSONObject returnedUserJSON = getJSONFromResponse(response);
        String userId = returnedUserJSON.get(ID_ATTRIBUTE).toString();
        users.put(userJSON.get("userName").toString(), userId);
        assertNotNull(userId, "SCIM2 user id not available in the response.");
    }

    @Test(dependsOnMethods = "testProvisionUser")
    public void testCreateGroupWithMembers() throws Exception {
        // Build Group object with members.
        JSONObject groupJSON = scim2Client.getRoleJSON(groupInputFileName);
        JSONArray members = new JSONArray();
        for (Map.Entry<String, String> entry : users.entrySet()) {
            JSONObject member = new JSONObject();
            member.put(DISPLAY_ATTRIBUTE, entry.getKey());
            member.put(VALUE_PARAM, entry.getValue());
            members.add(member);
        }
        groupJSON.put(MEMBERS_ATTRIBUTE, members);

        // Create object
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created " + "with members");
        JSONObject returnedGroupJSON = getJSONFromResponse(response);
        groupId = returnedGroupJSON.get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId, "SCIM2 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateGroupWithMembers")
    private void testDeleteUser() throws Exception {

        for (Map.Entry<String, String> user : users.entrySet()) {
            HttpResponse response = scim2Client.deleteUser(client, user.getValue(), ADMIN_USERNAME, ADMIN_PASSWORD);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                    "Failed to delete the user");
        }
    }

    @Test(dependsOnMethods = "testDeleteUser")
    public void testDeleteCreatedGroup() throws Exception {

        HttpResponse response = scim2Client.deleteGroup(client, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

}
