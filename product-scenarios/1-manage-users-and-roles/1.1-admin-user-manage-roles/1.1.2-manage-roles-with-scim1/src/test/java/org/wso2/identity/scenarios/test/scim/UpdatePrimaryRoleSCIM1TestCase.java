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
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIM1CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class UpdatePrimaryRoleSCIM1TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String groupId;
    private String secoundGroupId;

    private SCIM1CommonClient scim1Group;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        scim1Group = new SCIM1CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
    }

    @Test
    public void testSCIM1CreateGroup() throws Exception {
        JSONObject userJSON = scim1Group.getRoleJSON("scim1group.json");
        HttpResponse response = scim1Group.provisionGroup(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
            "Group has not been created successfully");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        groupId = returnedUserJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();

        assertNotNull(groupId, "SCIM1 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1CreateGroup")
    public void testSCIM1CreateSecondGroup() throws Exception {
        JSONObject userJSON = scim1Group.getRoleJSON("scim1SecondGroup.json");
        HttpResponse response = scim1Group.provisionGroup(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
            "Group has not been created successfully");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        secoundGroupId = returnedUserJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();

        assertNotNull(secoundGroupId, "SCIM1 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1CreateSecondGroup")
    private void testSCIM1UpdateExistingGroup() throws Exception {
        JSONObject updateUserJSON = scim1Group.getRoleJSON("scim1SecondGroup.json");
        HttpResponse response = scim1Group.updateGroup(client, updateUserJSON, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
            "Failed to delete the group");
    }

    @Test(dependsOnMethods = "testSCIM1UpdateExistingGroup")
    private void testSCIM1UpdateGroup() throws Exception {
        JSONObject updateUserJSON = scim1Group.getRoleJSON("scim1UpdateGroup.json");
        HttpResponse response = scim1Group.updateGroup(client, updateUserJSON, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the group");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        groupId = returnedUserJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();
        assertNotNull(groupId, "SCIM1 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1UpdateGroup")
    private void testSCIM1DeleteGroup() throws Exception {

        HttpResponse response = scim1Group.deleteGroup(client, groupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1DeleteGroup")
    private void testSCIM1DeleteSecondGroup() throws Exception {

        HttpResponse response = scim1Group.deleteGroup(client, secoundGroupId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

}
