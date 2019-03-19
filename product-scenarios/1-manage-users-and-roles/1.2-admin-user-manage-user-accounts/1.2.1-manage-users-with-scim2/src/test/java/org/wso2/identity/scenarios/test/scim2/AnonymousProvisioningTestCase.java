/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.SCIMConstants.ID_ATTRIBUTE;

import org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil;

public class AnonymousProvisioningTestCase extends ScenarioTestBase {

    private SCIM2CommonClient scim2CommonClient;
    private IdentityScenarioUtil identityScenarioUtil;
    private CloseableHttpClient client;
    private String inputFile;
    private String username;
    private String password;
    private String tenantDomain;
    private String userId;

    @Factory(dataProvider = "anonymousProvConfigProvider")
    public AnonymousProvisioningTestCase(String tenantDomain, String username, String password, String inputFile) {

        this.inputFile = inputFile;
        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
    }

    @DataProvider(name = "anonymousProvConfigProvider")
    private static Object[][] dcrConfigProvider() throws Exception {

        return new Object[][] {
                { SUPER_TENANT_DOMAIN, ADMIN_USERNAME, ADMIN_PASSWORD, "selfRegisterSCIM2User.json" }
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        client = HttpClients.createDefault();
        scim2CommonClient = new SCIM2CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        identityScenarioUtil = new IdentityScenarioUtil();
        super.init();

    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        HttpResponse response = scim2CommonClient.deleteUser(client, userId, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), org.apache.commons.httpclient.HttpStatus.SC_NO_CONTENT,
                "User has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
        client.close();
    }

    @Test(description = "1.2.1.15")
    private void selfRegister() throws Exception {

        //User creation
        JSONObject requestJSONCreateUser = scim2CommonClient.getUserJSON(inputFile);
        HttpResponse responseCreateUser = scim2CommonClient
                .provisionUserMe(client, requestJSONCreateUser, username, password);
        assertEquals(responseCreateUser.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "User has not been created" + " successfully");
        JSONObject responseJSONCreateUser = identityScenarioUtil.getJSONFromResponse(responseCreateUser);
        scim2CommonClient.validateResponse(responseJSONCreateUser, requestJSONCreateUser);
        userId = responseJSONCreateUser.get(ID_ATTRIBUTE).toString();
        EntityUtils.consume(responseCreateUser.getEntity());

        //created user validation
        HttpResponse responseGetUser = scim2CommonClient.getUser(client, userId, username, password);
        assertEquals(responseGetUser.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "User not available in the system");
        JSONObject responseJSONGetUser = identityScenarioUtil.getJSONFromResponse(responseGetUser);
        scim2CommonClient.validateResponse(responseJSONGetUser, requestJSONCreateUser);
        EntityUtils.consume(responseCreateUser.getEntity());

    }
}
