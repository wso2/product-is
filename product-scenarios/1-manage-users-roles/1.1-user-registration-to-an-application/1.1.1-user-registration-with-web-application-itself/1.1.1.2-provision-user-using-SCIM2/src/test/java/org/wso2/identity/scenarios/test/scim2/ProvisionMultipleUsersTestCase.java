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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ProvisionMultipleUsersTestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String USERNAME1 = "scimuser1";
    private String PASSWORD1 = "scimuser1";
    private String USERNAME2 = "scimuser2";
    private String PASSWORD2 = "scimuser2";
    private String userID1;
    private String userID2;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
    }

    @Test(description = "1.1.1.2.8")
    public void testSCIM2MultipleUsers() throws Exception {

        String scimEndpoint =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_BULK_USERS_ENDPOINT;
        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        JSONObject rootObject = new JSONObject();
        rootObject.put(SCIMConstants.FAIL_ON_ERROR_ATTRIBUTE, "1");
        JSONArray schemaBulk = new JSONArray();
        schemaBulk.add(SCIMConstants.BULK_SCHEMA);
        JSONArray schemaUser = new JSONArray();
        schemaUser.add(SCIMConstants.USER_SCHEMA);
        rootObject.put("schemas", schemaBulk);

        JSONArray operations = new JSONArray();
        JSONObject data1 = new JSONObject();
        data1.put("schemas", schemaUser);
        data1.put(SCIMConstants.USER_NAME_ATTRIBUTE, USERNAME1);
        data1.put(SCIMConstants.PASSWORD_ATTRIBUTE, PASSWORD1);

        JSONObject data2 = new JSONObject();
        data2.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemaUser);
        data2.put(SCIMConstants.USER_NAME_ATTRIBUTE, USERNAME2);
        data2.put(SCIMConstants.PASSWORD_ATTRIBUTE, PASSWORD2);

        JSONObject bulkuser1 = new JSONObject();
        bulkuser1.put(SCIMConstants.METHOD_ATTRIBUTE, SCIMConstants.METHOD_PARM);
        bulkuser1.put(SCIMConstants.PATH_ATTRIBUTE, SCIMConstants.PATH_PARM);
        bulkuser1.put(SCIMConstants.BULK_ATTRIBUTE, "bulkid01");
        bulkuser1.put("data", data1);

        JSONObject bulkuser2 = new JSONObject();
        bulkuser2.put(SCIMConstants.METHOD_ATTRIBUTE, SCIMConstants.METHOD_PARM);
        bulkuser2.put(SCIMConstants.PATH_ATTRIBUTE, SCIMConstants.PATH_PARM);
        bulkuser2.put(SCIMConstants.BULK_ATTRIBUTE, "bulkid02");
        bulkuser2.put("data", data2);

        operations.add(bulkuser1);
        operations.add(bulkuser2);
        rootObject.put(SCIMConstants.OPERATIONS_ATTRIBUTE, operations);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200,
                "Users bulk schema has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        assertTrue(
                ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get(SCIMConstants.OPERATIONS_ATTRIBUTE)).get(0))
                        .get(SCIMConstants.STATUS_ATTRIBUTE).toString().contains("201"));
        assertTrue(
                ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get(SCIMConstants.OPERATIONS_ATTRIBUTE)).get(1))
                        .get(SCIMConstants.STATUS_ATTRIBUTE).toString().contains("201"));

        userID1 = (((JSONObject) ((JSONArray) ((JSONObject) responseObj).get(SCIMConstants.OPERATIONS_ATTRIBUTE))
                .get(0)).get(SCIMConstants.LOCATION_ATTRIBUTE).toString().split("/"))[5];
        userID2 = (((JSONObject) ((JSONArray) ((JSONObject) responseObj).get(SCIMConstants.OPERATIONS_ATTRIBUTE))
                .get(1)).get(SCIMConstants.LOCATION_ATTRIBUTE).toString().split("/"))[5];
        assertNotNull(userID1);
        assertNotNull(userID2);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUsers() throws Exception {

        testDeleteUser(userID1);
        testDeleteUser(userID2);
    }

    private void testDeleteUser(String bulkUserId) throws Exception {

        String userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT + "/"
                        + bulkUserId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User has not been retrieved successfully");
        EntityUtils.consume(response.getEntity());
        userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT + "/"
                        + bulkUserId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

}
