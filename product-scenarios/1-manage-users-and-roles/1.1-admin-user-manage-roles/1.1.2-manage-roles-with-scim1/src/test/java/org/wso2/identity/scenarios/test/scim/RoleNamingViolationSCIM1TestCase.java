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
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;

public class RoleNamingViolationSCIM1TestCase extends ScenarioTestBase {
    private CloseableHttpClient client;
    private SCIM1CommonClient scim1Group;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        scim1Group = new SCIM1CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        client = HttpClients.createDefault();
    }

    @Test
    public void testSCAM1CreateGroupWithSpecialCharacter() throws Exception {
        JSONObject userJSON = scim1Group.getRoleJSON("scim1SpecialCharacterGroupName.json");
        HttpResponse response = scim1Group.provisionGroup(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
            "Group has not been created successfully");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCAM1CreateGroupWithSpecialCharacter")
    public void testSCIM1CreateGroupWithMinimumCharacter() throws Exception {
        JSONObject userJSON = scim1Group.getRoleJSON("scim1MinimumSpaceGroupName.json");
        HttpResponse response = scim1Group.provisionGroup(client, userJSON, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR,
            "Group has not been created successfully");
        EntityUtils.consume(response.getEntity());
    }
}
