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
import org.apache.http.message.BasicHeader;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.apache.http.client.methods.HttpDelete;

import org.wso2.identity.scenarios.commons.util.Constants;
import static org.testng.Assert.assertEquals;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;


public class ProvisionTenantUserSCIM2TestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;;
    private static String TENANT_USER = "scim2user";
    private static String TENANT_PASSWORD = "scim2user";
    private String provisionURL;
    private String SEPERATOR = "/";
    private String USERS_ATTRIBUTE = "Users";

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
    }

    @Test(description = "1.1.2.1.2.20")
    public void testSCIM2CreateTenantUser() throws Exception {

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, TENANT_USER);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, TENANT_PASSWORD);

        provisionURL =
                backendURL + SEPERATOR + SCIMConstants.TENANT_DOMAIN + SEPERATOR + SCIMConstants.SCIM2_USERS_ENDPOINT + SEPERATOR +
                        USERS_ATTRIBUTE + SEPERATOR;
        response = sendPostRequestWithJSON(client, provisionURL, rootObject, getCommonHeaders());
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");

        userNameResponse = rootObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, TENANT_USER, "username not found");
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();

        provisionURL =
                backendURL + SEPERATOR  +  SCIMConstants.TENANT_DOMAIN + SEPERATOR +Constants.SCIMEndpoints.SCIM2_ENDPOINT + SEPERATOR
                        + USERS_ATTRIBUTE + SEPERATOR + userId;
        deleteUserRequest(client,provisionURL,getCommonHeaders());
    }

    public static HttpResponse deleteUserRequest(HttpClient client, String url, Header[] headers) throws Exception {

        HttpDelete request = new HttpDelete(url);
        if (headers != null) {
            request.setHeaders(headers);
        }

        return client.execute(request);
    }

    private static Header[] getCommonHeaders() {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(SCIMConstants.TENANT_ADMIN_UN,
                        SCIMConstants.TENANT_ADMIN_PW))
        };
        return headers;
    }

}
