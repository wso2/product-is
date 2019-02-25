/*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.scenarios.test.scim;

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
import org.wso2.identity.scenarios.commons.SCIM1CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class CreateRoleWithUserSCIM1TestCase extends ScenarioTestBase {

    private String username;
    private String password;
    private String tenantDomain;
    private String groupInputFileName;
    private String userInputFileName;
    private CloseableHttpClient client;
    private SCIM1CommonClient scim1Client;
    private String groupId;
    private String userId;
    private String userName;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public CreateRoleWithUserSCIM1TestCase(String username, String password, String tenantDomain,
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
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim1user.json", "scim1group.json"
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        scim1Client = new SCIM1CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
    }

    @Test
    public void testProvisionUser() throws Exception {

        JSONObject userJSON = scim1Client.getUserJSON(userInputFileName);
        HttpResponse response = scim1Client.provisionUser(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "User has not been created successfully");
        JSONObject returnedUserJSON = getJSONFromResponse(response);
        userId = returnedUserJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();
        userName = userJSON.get("userName").toString();
        assertNotNull(userId, "SCIM1 user id not available in the response.");
    }

    @Test(dependsOnMethods = "testProvisionUser")
    public void testCreateGroupWithMembers() throws Exception {

        JSONObject groupJSON = scim1Client.getRoleJSON(groupInputFileName);
        JSONArray members = new JSONArray();
            JSONObject member = new JSONObject();
            member.put(SCIM1Constants.DISPLAY_ATTRIBUTE, userName);
            member.put(SCIM1Constants.VALUE_PARAM, userId);
            members.add(member);
        groupJSON.put(SCIM1Constants.MEMBERS_ATTRIBUTE, members);
        HttpResponse response = scim1Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created " + "with members");
        JSONObject returnedGroupJSON = getJSONFromResponse(response);
        groupId = returnedGroupJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();
        assertNotNull(groupId, "SCIM1 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateGroupWithMembers")
    private void testDeleteUser() throws Exception {
        HttpResponse response = scim1Client.deleteUser(client, userId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Failed to delete the user");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testDeleteUser")
    public void testDeleteCreatedGroup() throws Exception {

        HttpResponse response = scim1Client.deleteGroup(client, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

}
