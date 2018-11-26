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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;


public class ProvisionUserSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private String userNameResponse;
    private String userId;

    HttpResponse response;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();

        super.init();
    }

    @Test(description = "1.1.2.1.2.1")
    public void testSCIM2CreateUser() throws Exception {

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject, SCIMConstants.SCIM2_USERS_ENDPOINT, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");

        userNameResponse = rootObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");
    }

    @Test(dependsOnMethods = "testSCIM2CreateUser")
    private void testDeleteUser() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, SCIMConstants.SCIM2_USERS_ENDPOINT, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been deleted successfully");
    }

}

