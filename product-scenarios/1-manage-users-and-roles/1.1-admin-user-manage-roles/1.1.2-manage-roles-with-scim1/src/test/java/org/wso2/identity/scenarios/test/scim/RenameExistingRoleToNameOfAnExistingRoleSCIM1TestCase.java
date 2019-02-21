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
package org.wso2.identity.scenarios.test.scim;

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
import org.wso2.identity.scenarios.commons.SCIM1CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class RenameExistingRoleToNameOfAnExistingRoleSCIM1TestCase extends ScenarioTestBase {
    private String username;
    private String password;
    private String tenantDomain;
    private String[] inputFileName;
    private CloseableHttpClient client;
    private SCIM1CommonClient scim1Client;
    private String[] groupId;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public RenameExistingRoleToNameOfAnExistingRoleSCIM1TestCase(String username, String password, String tenantDomain,
            String[] inputFile) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.inputFileName = inputFile;
        this.groupId = new String[inputFile.length];
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN,
                        new String[] { "scim1group.json", "scim1SecondGroup.json" }
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
    public void testCreateGroups() throws Exception {

        for (int i = 0; i < inputFileName.length; i++) {
            JSONObject groupJSON = scim1Client.getRoleJSON(inputFileName[i]);
            HttpResponse response = scim1Client.provisionGroup(client, groupJSON, username, password);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                    "Group has not been created successfully");
            JSONObject returnedGroupJSON = getJSONFromResponse(response);
            groupId[i] = returnedGroupJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();
            assertNotNull(groupId, "SCIM1 group id not available in the response.");
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(dependsOnMethods = "testCreateGroups")
    public void testGetGroups() throws Exception {

        for (int i = 0; i < inputFileName.length; i++) {
            HttpResponse response = scim1Client.getGroup(client, groupId[i], username, password);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Unable to retrieve the group");
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(dependsOnMethods = "testGetGroups")
    public void testReNameExistingGroupToAnExistingGroup() throws Exception {

        JSONObject groupJSON = scim1Client.getRoleJSON(inputFileName[1]);
        HttpResponse response = scim1Client.updateGroup(client, groupJSON, groupId[0], username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Group has been updated successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testReNameExistingGroupToAnExistingGroup")
    public void testDeleteGroups() throws Exception {

        for (int i = 0; i < inputFileName.length; i++) {
            HttpResponse response = scim1Client.deleteGroup(client, groupId[i], ADMIN_USERNAME, ADMIN_PASSWORD);
            assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                    "Failed to delete the group");
            EntityUtils.consume(response.getEntity());
        }
    }
}
