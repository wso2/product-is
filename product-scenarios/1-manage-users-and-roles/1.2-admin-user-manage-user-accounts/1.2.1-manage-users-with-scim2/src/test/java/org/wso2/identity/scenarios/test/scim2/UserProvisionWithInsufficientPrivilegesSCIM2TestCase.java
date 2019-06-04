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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;

public class UserProvisionWithInsufficientPrivilegesSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String scimUsersEndpoint;
    private final String SEPERATOR = "/";
    private String newUser = "newusername";
    private String newPass = "newpassword";
    private String userId;
    HttpResponse response;
    JSONObject rootObject;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        client = HttpClients.createDefault();
        super.init();
        scimUsersEndpoint = backendURL + SEPERATOR +  Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR +
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;
        testSCIMCreateFirstUser();
    }

    private void testSCIMCreateFirstUser() throws Exception {

        rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject,
                Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER , ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created" +
                " successfully");
    }

    @Test(description = "1.1.2.1.2.17")
    public void testSCIMCreateSecondUser() throws Exception {


        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, newUser);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, newPass);

        HttpResponse response = sendPostRequestWithJSON(client, scimUsersEndpoint, rootObject,
                new Header[]{getFaultyAuthzHeader(), getContentTypeApplicationJSONHeader()});

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN, "User is not authorized" +
                " to perform provisioning");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = new JSONArray();
        schemasArray.add(responseObj);
    }

   @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId=responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been " +
                "deleted successfully");
    }

    private Header getFaultyAuthzHeader() {

        return new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(SCIMConstants.USERNAME,
                SCIMConstants.PASSWORD));
    }

    private Header getContentTypeApplicationJSONHeader() {

        return new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON);
    }
}

