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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;


public class AnonymousProvisioningTestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private String userNameResponse;
    private String userId;
    private String SEPERATOR = "/";
    private String WORKEMAIL = "scimwrk@test.com";
    private String HOMEEMAIL = "scimhome@test.com";
    private String PRIMARYSTATE = "true";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
    }

    @Test(description = "1.1.2.1.2.15")
    private  void selfRegister() throws  Exception {

        scimUsersEndpoint = backendURL + SEPERATOR + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ANONYMOUS_USER;

        HttpPost request = new HttpPost(scimUsersEndpoint);
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
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created" +
                " successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        userNameResponse = ((JSONObject) responseObj).get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME);

        userId = ((JSONObject) responseObj).get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId);
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        String scimUsersEndpoint = backendURL + SEPERATOR + Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER + SEPERATOR + userId;

        HttpDelete delete = new HttpDelete(scimUsersEndpoint);
        delete.addHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(ADMIN_USERNAME, ADMIN_PASSWORD));
        delete.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = client.execute(delete);
        assertEquals(response.getStatusLine().getStatusCode(), org.apache.commons.httpclient.HttpStatus.SC_NO_CONTENT,
                "User has not been deleted successfully");

        EntityUtils.consume(response.getEntity());
    }

}
