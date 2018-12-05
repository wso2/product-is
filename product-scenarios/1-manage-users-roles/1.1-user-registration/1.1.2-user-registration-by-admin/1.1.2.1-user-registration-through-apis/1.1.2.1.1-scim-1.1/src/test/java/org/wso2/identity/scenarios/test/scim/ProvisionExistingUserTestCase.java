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

package org.wso2.identity.scenarios.test.scim;

import java.util.AbstractMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class ProvisionExistingUserTestCase extends ScenarioTestBase {

    private String userId;
    private HttpResponse response;
    private JSONObject responseObj;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        super.init();
        Map.Entry<HttpResponse, JSONObject> valueAndIndex = testCreateSCSIMUser();
        response = valueAndIndex.getKey();
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");
    }

    @Test
    public void testSCIMCreateExistingUser() throws Exception {

        Map.Entry<HttpResponse, JSONObject> valueAndIndex = testCreateSCSIMUser();
        response = valueAndIndex.getKey();
        responseObj = valueAndIndex.getValue();
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CONFLICT,
                "User already exists hence server should not accept user creation");

        assertTrue(
                responseObj.toString().contains("User with the name: " + SCIMConstants.USERNAME + " already exists in the system"));
    }

    private Map.Entry<HttpResponse, JSONObject> testCreateSCSIMUser() throws Exception {

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject, Constants.SCIMEndpoints.SCIM1_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        responseObj = getJSONFromResponse(this.response);
        if (responseObj.get(SCIMConstants.ID_ATTRIBUTE) != null) {
            userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
        }

        return new AbstractMap.SimpleEntry(response, responseObj);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has been deleted successfully");
    }
}

