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
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIM2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class ManageRolesSCIM2TestCase extends ScenarioTestBase {

    public static final String ID_ATTRIBUTE = "id";
    private CloseableHttpClient client;
    private String username;
    private String password;
    private String tenantDomain;
    private String groupId;
    private String inputFileName;
    private SCIM2CommonClient scim2Client;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManageRolesSCIM2TestCase(String username, String password, String tenantDomain, String inputFile) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.inputFileName = inputFile;

    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() throws Exception {

        return new Object[][]{
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim2Group1.json"
                },
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim2Internalgroup.json"
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
    public void testCreateGroupWithoutPermission() throws Exception {

        JSONObject groupJSON = scim2Client.getRoleJSON(inputFileName);
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created successfully");
        JSONObject returnedUserJSON = getJSONFromResponse(response);
        groupId = returnedUserJSON.get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId, "SCIM2 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateGroupWithoutPermission")
    public void testGetGroup() throws Exception {

        HttpResponse response = scim2Client.getGroup(client, groupId, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Unable to retrieve the group");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testGetGroup")
    public void testCreateExistingGroup() throws Exception {

        JSONObject groupJSON = scim2Client.getRoleJSON(inputFileName);
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CONFLICT,
                "Group has been added successfully without conflict");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateExistingGroup")
    public void testDeleteGroup() throws Exception {

        HttpResponse response = scim2Client.deleteGroup(client, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testDeleteGroup")
    public void testGetDeletedGroup() throws Exception {

        HttpResponse response = scim2Client.getGroup(client, groupId, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND,
                "Group has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testGetDeletedGroup")
    public void testReCreateGroup() throws Exception {

        JSONObject groupJSON = scim2Client.getRoleJSON(inputFileName);
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been re created successfully");
        JSONObject returnedUserJSON = getJSONFromResponse(response);
        groupId = returnedUserJSON.get(ID_ATTRIBUTE).toString();
        assertNotNull(groupId, "SCIM2 re created group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testReCreateGroup")
    public void testDeleteReCreatedGroup() throws Exception {

        HttpResponse response = scim2Client.deleteGroup(client, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }
}
