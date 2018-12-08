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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;
import org.apache.http.client.methods.HttpPut;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;


public class UpdateProvisionedUserRoleSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String roleNameFromResponse;
    private String userId;
    private String groupId;
    private String groupURL;
    private String firstName;
    private String SEPERATOR = "/";
    private String ROLE_DISPLAY_NAME_ATTRIBUTE = "displayName";
    private String GROUP_NAME_ATTRIBUTE = "Groups";

    JSONObject responseObj;
    JSONObject groupObject;

    HttpResponse response,roleResponse;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
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

    @Test
    public void createRole() throws Exception {

        groupObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        groupObject.put(ROLE_DISPLAY_NAME_ATTRIBUTE, SCIMConstants.ROLE_NAME);

        groupURL = backendURL + SEPERATOR  + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                GROUP_NAME_ATTRIBUTE + SEPERATOR;

        roleResponse = sendPostRequestWithJSON(client,groupURL,groupObject,getCommonHeaders());
        roleNameFromResponse = groupObject.get(ROLE_DISPLAY_NAME_ATTRIBUTE).toString();
        assertEquals(roleNameFromResponse,SCIMConstants.ROLE_NAME,"Expected Role name does not exist");
    }

    @Test(description = "1.1.2.1.2.18", dependsOnMethods = "createRole")
    public void testAddMemberToRole() throws Exception {

        responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        responseObj = getJSONFromResponse(this.roleResponse);
        groupId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        JSONArray schemas = new JSONArray();
        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        groupObject.put(ROLE_DISPLAY_NAME_ATTRIBUTE, SCIMConstants.ROLE_NAME);

        JSONArray members = new JSONArray();
        for (int i = 0; i < SCIMConstants.USER_NAME_ATTRIBUTE.length(); i++) {
            JSONObject member = new JSONObject();
            member.put(SCIMConstants.DISPLAY, SCIMConstants.USERNAME);
            member.put(SCIMConstants.VALUE_PARAM, userId);
            members.add(member);
        }

        groupObject.put(SCIMConstants.MEMBERS, members);

        groupURL = backendURL + SEPERATOR  + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                GROUP_NAME_ATTRIBUTE + SEPERATOR + groupId;

        updateRoleRequest(client,groupURL,groupObject,getCommonHeaders());
        roleNameFromResponse = groupObject.get(ROLE_DISPLAY_NAME_ATTRIBUTE).toString();
        assertEquals(roleNameFromResponse,SCIMConstants.ROLE_NAME);
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been deleted successfully");

        groupURL = backendURL + SEPERATOR  + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                GROUP_NAME_ATTRIBUTE + SEPERATOR + groupId;
        deleteRoleRequest(client,groupURL,getCommonHeaders());
    }


    public static HttpResponse updateRoleRequest(HttpClient client, String url, JSONObject jsonObject,
                                               Header[] headers) throws Exception {

        HttpPut request = new HttpPut(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        request.setEntity(new StringEntity(jsonObject.toString()));
        return client.execute(request);
    }

    public static HttpResponse deleteRoleRequest(HttpClient client, String url, Header[] headers) throws Exception {

        HttpDelete request = new HttpDelete(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

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
