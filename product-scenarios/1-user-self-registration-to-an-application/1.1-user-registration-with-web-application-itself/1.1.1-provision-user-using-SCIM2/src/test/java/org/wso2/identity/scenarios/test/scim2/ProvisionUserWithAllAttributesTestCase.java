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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.test.scim2.ScenarioTestBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ProvisionUserWithAllAttributesTestCase extends ScenarioTestBase {

    private String userId;
    private CloseableHttpClient client;
    private String WORKEMAIL = "scimwrk@test.com";
    private String HOMEEMAIL = "scimhome@test.com";
    private String PRIMARYSTATE = "true";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
    }

    @Test(description = "1.1.1.7")
    public void testSCIM2CreateUserWithAllAttributes() throws Exception {

        String scimEndpoint = getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT;
        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.FAMILY_NAME_ATTRIBUTE, SCIMConstants.FAMILY_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);
        JSONObject emailWork = new JSONObject();
        emailWork.put(SCIMConstants.TYPE_PARAM, SCIMConstants.EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(SCIMConstants.VALUE_PARAM, WORKEMAIL);
        JSONObject emailHome = new JSONObject();
        emailHome.put(SCIMConstants.PRIMARY_PARAM, PRIMARYSTATE);
        emailHome.put(SCIMConstants.TYPE_PARAM, SCIMConstants.EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(SCIMConstants.VALUE_PARAM, HOMEEMAIL);
        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);
        rootObject.put(SCIMConstants.EMAILS_ATTRIBUTE, emails);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, SCIMConstants.USERNAME);

        userId = ((JSONObject) responseObj).get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        testGetSCIMUser();
        testDeleteSCIMUser();
    }

    private void testGetSCIMUser() throws Exception {
        String userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        assertTrue(responseObj.toString().contains(SCIMConstants.USERNAME));
        assertTrue(responseObj.toString().contains(WORKEMAIL));
    }

    private void testDeleteSCIMUser() throws Exception {

        String userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User has not been retrieved successfully");
        EntityUtils.consume(response.getEntity());
        userResourcePath =
                getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

}
