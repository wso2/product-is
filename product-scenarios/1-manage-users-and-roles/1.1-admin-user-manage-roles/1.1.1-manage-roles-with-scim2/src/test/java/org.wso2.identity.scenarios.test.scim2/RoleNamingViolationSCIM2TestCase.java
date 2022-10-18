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
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;

/**
 * This claass tests the role naming violation when creating roles via SCIM2.
 */
public class RoleNamingViolationSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String username;
    private String password;
    private String tenantDomain;
    private SCIM2CommonClient scim2Client;

    @Factory(dataProvider = "manageRolesConfigProvider")
    public RoleNamingViolationSCIM2TestCase(String username, String password, String tenantDomain) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
    }

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN
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
    public void testCreateGroupWithLessThanMinCharacters() throws Exception {

        JSONObject groupJSON = scim2Client.getRoleJSON("scim2GroupWithLessThanMinCharacters.json");
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Group has been created with less than minimum characters");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testCreateGroupWithLessThanMinCharacters")
    public void testCreateGroupWithSpecialCharacters() throws Exception {

        JSONObject groupJSON = scim2Client.getRoleJSON("scim2GroupWithSpecialCharacters.json");
        HttpResponse response = scim2Client.provisionGroup(client, groupJSON, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Group has been created with special characters by violating the schema");
        EntityUtils.consume(response.getEntity());
    }
}
