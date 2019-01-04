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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;


public class UpdateProvisionedUserSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;
    private String updateURL;
    private String firstName;
    private String SEPERATOR = "/";
    private String NEW_NAME = "testuser";
    private String NEW_LAST_NAME = "lastname";

    JSONObject responseObj;

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        client = HttpClients.createDefault();
        super.init();
        createUser();
    }

    public void createUser() throws Exception {

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.FAMILY_NAME_ATTRIBUTE,SCIMConstants.FAMILY_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);

        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject, Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created " +
                "successfully");

        userNameResponse = rootObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");

        firstName = rootObject.get(SCIMConstants.NAME_ATTRIBUTE).toString();
        assertEquals(firstName.substring(14,19),SCIMConstants.GIVEN_NAME_CLAIM_VALUE,"The given first name " +
                "does not exist");
    }

    @Test(description = "1.1.2.1.2.18")
    public void testUpdateUser() throws Exception {

        responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        updateURL = backendURL + SEPERATOR  + SCIMConstants.SCIM2_USERS_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER + SEPERATOR + userId;

        JSONObject updateUserObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        updateUserObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, NEW_NAME);
        names.put(SCIMConstants.FAMILY_NAME_ATTRIBUTE,NEW_LAST_NAME);
        updateUserObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        updateUserObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        updateUserObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        updateUserRequest(client, updateURL, updateUserObject, getCommonHeaders());
        userNameResponse = updateUserObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");

        firstName = updateUserObject.get(SCIMConstants.NAME_ATTRIBUTE).toString();
        assertEquals(firstName.substring(14,22),NEW_NAME,"The given first name " +
                "does not exist");
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been deleted successfully");
    }

    public static HttpResponse updateUserRequest(HttpClient client, String url, JSONObject jsonObject, Header[] headers) throws IOException {

        HttpPut request = new HttpPut(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        request.setEntity(new StringEntity(jsonObject.toString()));
        return client.execute(request);
    }

    private static Header[] getCommonHeaders() {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(ADMIN_USERNAME, ADMIN_PASSWORD))
        };
        return headers;
    }

}
