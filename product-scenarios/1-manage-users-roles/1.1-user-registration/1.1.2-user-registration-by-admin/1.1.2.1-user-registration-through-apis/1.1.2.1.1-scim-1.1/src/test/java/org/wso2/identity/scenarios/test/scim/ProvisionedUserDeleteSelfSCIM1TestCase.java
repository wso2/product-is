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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.*;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

public class ProvisionedUserDeleteSelfSCIM1TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private final String SEPERATOR = "/";
    private String userId;
    private String userEndPoint;
    private String ADMIN_ROLE = "admin";
    private String groupId;
    private String groupResponse;
    private String groupURL;
    private String roleNameFromResponse;

    HttpResponse response,roleResponse;
    JSONObject rootObject,responseObj,groupObject;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
        scimUsersEndpoint = backendURL + SEPERATOR +  Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;
        createUser();
    }

    private void createUser() throws Exception {

        rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject,
                Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER , ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created" +
                " successfully");
        addMemberToRole();
    }

    public void addMemberToRole() throws Exception {

        groupObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);

        groupURL = backendURL + SEPERATOR + Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPERATOR +
               SCIMConstants.GROUP_ENDPOINT;

        roleResponse = getGroups(client,groupURL,getCommonHeaders());

        responseObj = getJSONFromResponse(this.roleResponse);
        groupResponse = responseObj.get("Resources").toString();
        groupId = groupResponse.substring(213,249);

        groupObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONArray members = new JSONArray();
        for (int i = 0; i < SCIMConstants.USER_NAME_ATTRIBUTE.length(); i++) {
            JSONObject member = new JSONObject();
            member.put(SCIMConstants.DISPLAY, SCIMConstants.USERNAME);
            member.put(SCIMConstants.VALUE_PARAM, userId);
            members.add(member);
        }

        groupObject.put(SCIMConstants.MEMBERS, members);

        groupURL = backendURL + SEPERATOR  + Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPERATOR +
                SCIMConstants.GROUP_ENDPOINT + SEPERATOR + groupId;

        updateRoleRequest(client,groupURL,groupObject,getCommonHeaders());
    }


    public static HttpResponse getGroups(HttpClient client, String url, Header[] headers) throws Exception {

        HttpGet request = new HttpGet(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        return client.execute(request);
    }

    public static HttpResponse updateRoleRequest(HttpClient client, String url, JSONObject jsonObject,
                                                 Header[] headers) throws Exception {

        HttpPatch request = new HttpPatch(url);
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

    @Test(description = "1.1.2.1.1.17")
    public void testDeleteSelf() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId=responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        userEndPoint = backendURL + SEPERATOR  + Constants.SCIMEndpoints.SCIM1_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER + SEPERATOR + userId;

        response= deleteUserRequest(client,userEndPoint,getUnauthorizeHeaders());
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN, "User has not been " +
                "deleted successfully");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM1_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public static HttpResponse deleteUserRequest(HttpClient client, String url, Header[] headers) throws Exception {

        HttpDelete request = new HttpDelete(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        return client.execute(request);
    }

    private static Header[] getUnauthorizeHeaders() {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(SCIMConstants.USERNAME,
                        SCIMConstants.PASSWORD))
        };
        return headers;
    }
}
