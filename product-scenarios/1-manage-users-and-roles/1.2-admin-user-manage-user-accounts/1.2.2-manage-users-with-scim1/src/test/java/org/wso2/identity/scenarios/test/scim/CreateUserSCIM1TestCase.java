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
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SCIM1CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class CreateUserSCIM1TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String userId;
    private SCIM1CommonClient scim1Client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        scim1Client = new SCIM1CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
    }

    @Test(description = "1.2.2.1")
    public void testSCIM1ProvisionUser() throws Exception {

        JSONObject userJSON = scim1Client.getUserJSON("scim1User.json");
        HttpResponse response = scim1Client.provisionUser(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "User has not been created successfully");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        userId = returnedUserJSON.get(SCIMConstants.ID_ATTRIBUTE).toString();

        assertNotNull(userId, "SCIM2 user id not available in the response.");
    }

    @Test(dependsOnMethods = "testSCIM1ProvisionUser")
    public void testSCIM1GetUser() throws Exception {

        HttpResponse response = scim1Client.getUser(client, userId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to retrieve the user");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        assertEquals(userId, returnedUserJSON.get(SCIMConstants.ID_ATTRIBUTE).toString(),
                "Invalid user id found in " + "the response.");
    }

    @Test(dependsOnMethods = "testSCIM1GetUser")
    private void testSCIM1DeleteUser() throws Exception {

        HttpResponse response = scim1Client.deleteUser(client, userId, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the user");
    }
}
