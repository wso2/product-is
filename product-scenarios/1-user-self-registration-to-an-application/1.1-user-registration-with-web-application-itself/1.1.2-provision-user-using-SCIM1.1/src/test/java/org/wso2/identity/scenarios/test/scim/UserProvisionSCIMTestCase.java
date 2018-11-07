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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UserProvisionSCIMTestCase extends ScenarioTestBase {

    protected Log log = LogFactory.getLog(getClass());
    private static final String SCIM_USERS_ENDPOINT = "/wso2/scim/Users";
    private static final String SCHEMAS_ATTRIBUTE = "schemas";
    private static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String USER_NAME_ATTRIBUTE = "userName";
    private static final String PASSWORD_ATTRIBUTE = "password";
    private static final String ID_ATTRIBUTE = "id";
    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String USERNAME = "scim1user";
    private static final String PASSWORD = "scim1pwd";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private String userId;
    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
    }

    @Test(description = "1.1.2.1")
    public void testSCIMCreateUser() throws Exception {

        String scimEndpoint = getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIM_USERS_ENDPOINT;
        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);
        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        testDeleteUser();
    }

    private void testDeleteUser() throws Exception {

        String userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIM_USERS_ENDPOINT + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200, "User has not been retrieved successfully");
        EntityUtils.consume(response.getEntity());
        userResourcePath = getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIM_USERS_ENDPOINT + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

}
