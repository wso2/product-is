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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendDeleteRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;

public class UserProvisionSCIMTestCase extends ScenarioTestBase {

    private static final Log log = LogFactory.getLog(UserProvisionSCIMTestCase.class);

    private String userId;
    private CloseableHttpClient client;
    private String scimUsersEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        client = HttpClients.createDefault();
        scimUsersEndpoint = getDeploymentProperty(IS_HTTPS_URL) + SCIMConstants.SCIM_ENDPOINT + "/Users";
    }

    @Test(description = "1.1.1.2.1")
    public void testSCIMCreateUser() throws Exception {

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        HttpResponse response = sendPostRequestWithJSON(client, scimUsersEndpoint, rootObject,
                new Header[]{getBasicAuthzHeader(), getContentTypeApplicationJSONHeader()});

        log.info("Error Info:1 " + response.getStatusLine().getStatusCode());
        log.info("Error Info:2 " + Arrays.toString(response.getAllHeaders()));
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");

        JSONObject responseObj = getJSONFromResponse(response);
        String usernameFromResponse = responseObj.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, SCIMConstants.USERNAME, "Username not found.");

        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId, "User id not found.");
        EntityUtils.consume(response.getEntity());
    }

    @AfterClass(alwaysRun = true)
    public void testDeleteUser() throws Exception {

        String endpoint = String.join("/", scimUsersEndpoint, userId);
        HttpResponse response = sendDeleteRequest(client, endpoint,
                new Header[]{getBasicAuthzHeader(), getContentTypeApplicationJSONHeader()});

        log.info("Error Info:3 " + response.getStatusLine().getStatusCode());
        log.info("Error Info:4 " + Arrays.toString(response.getAllHeaders()));
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "User has not been retrieved successfully");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, endpoint, null,
                new Header[]{getBasicAuthzHeader(), getContentTypeApplicationJSONHeader()});
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND, "User has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private Header getBasicAuthzHeader() {

        return new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    private Header getContentTypeApplicationJSONHeader() {

        return new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON);
    }
}
