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
import org.testng.annotations.*;
import org.wso2.identity.scenarios.commons.SCIM2CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.SCIMConstants;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class ManageRolesSCIM2TestCase extends ScenarioTestBase {

    private String username;
    private String password;
    private String tenantDomain;
    private String inputFileName;
    private CloseableHttpClient client;
    private SCIM2CommonClient scim2Client;
    private String groupId;
    private JSONObject requestJSON;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManageRolesSCIM2TestCase(String username, String password, String tenantDomain, String inputFile) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.inputFileName = inputFile;
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim2Group1.json"
                }, {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim2Internalgroup.json"
                }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        scim2Client = new SCIM2CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
        requestJSON = scim2Client.getRoleJSON(inputFileName);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        scim2Client.deleteGroup(client, groupId, username, password);
        client.close();
    }

    @Test(description = "1.1.1.3")
    public void testCreateGroupWithoutPermission() throws Exception {

        // Create group
        HttpResponse response = scim2Client.provisionGroup(client, requestJSON, username, password);

        // Validate create group response.
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created successfully");
        JSONObject responseJSON = getJSONFromResponse(response);
        validateResponse(requestJSON, responseJSON);
        EntityUtils.consume(response.getEntity());
        groupId = responseJSON.get(SCIMConstants.ID_ATTRIBUTE).toString();
        // Retrieve the created group for validation
        response = scim2Client.getGroup(client, groupId, username, password);

        // Validate response
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Unable to retrieve the group");
        responseJSON = getJSONFromResponse(response);
        validateResponse(requestJSON, responseJSON);
        EntityUtils.consume(response.getEntity());
    }

    @Test(description = "1.1.1.4",
          dependsOnMethods = "testCreateGroupWithoutPermission")
    public void testCreateExistingGroup() throws Exception {

        // Try to create an existing group
        HttpResponse response = scim2Client.provisionGroup(client, requestJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CONFLICT,
                "Group has been added successfully without conflict");
        EntityUtils.consume(response.getEntity());

    }

    @Test(description = "1.1.1.5",
          dependsOnMethods = "testCreateExistingGroup")
    public void testDeleteGroup() throws Exception {

        HttpResponse response = scim2Client.deleteGroup(client, groupId, username, password);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "Failed to delete the group");

        // Retrieve the created group for validation
        response = scim2Client.getGroup(client, groupId, username, password);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND, "Unable to retrieve the group");

        // Validate create group response.
        response = scim2Client.provisionGroup(client, requestJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created successfully");
        JSONObject responseJSON = getJSONFromResponse(response);
        validateResponse(requestJSON, responseJSON);
        EntityUtils.consume(response.getEntity());
        groupId = responseJSON.get(SCIMConstants.ID_ATTRIBUTE).toString();
    }

    private void validateResponse(JSONObject requestJSON, JSONObject returnedGroupJSON) throws IOException {

        String resourceType = null;
        String modifiedTime = null;

        assertNotNull(returnedGroupJSON.get(SCIMConstants.ID_ATTRIBUTE),
                "Received ID value is null. Request Object: " + requestJSON.toJSONString() + ", Response Object: "
                        + returnedGroupJSON.toJSONString());

        if (((JSONObject) returnedGroupJSON.get(SCIMConstants.META_ATTRIBUTE))
                .get(SCIMConstants.RESOURCE_TYPE_ATTRIBUTE) != null) {
            resourceType = ((JSONObject) returnedGroupJSON.get(SCIMConstants.META_ATTRIBUTE))
                    .get(SCIMConstants.RESOURCE_TYPE_ATTRIBUTE).toString();
            assertNotNull(resourceType, "Received resource type value is null. Request Object:");
            assertEquals(resourceType, "Group", "Received resource type is incorrect.");
        }

        if (((JSONObject) returnedGroupJSON.get(SCIMConstants.META_ATTRIBUTE))
                .get(SCIMConstants.LAST_MODIFIED_ATTRIBUTE) != null) {
            modifiedTime = ((JSONObject) returnedGroupJSON.get(SCIMConstants.META_ATTRIBUTE))
                    .get(SCIMConstants.LAST_MODIFIED_ATTRIBUTE).toString();
            assertNotNull(modifiedTime, "Received time value is null. Request Object:");
        }

        assertNotNull(returnedGroupJSON.get(SCIMConstants.SCHEMAS_ATTRIBUTE),
                "Received schema value is null. Request Object: " + requestJSON.toJSONString() + ", Response Object: "
                        + returnedGroupJSON.toJSONString());

        assertNotNull(requestJSON.get(SCIMConstants.GROUP_NAME_ATTRIBUTE),
                "Received group name value is null. Request Object: " + requestJSON.toJSONString()
                        + ", Response Object: " + returnedGroupJSON.toJSONString());
    }
}

