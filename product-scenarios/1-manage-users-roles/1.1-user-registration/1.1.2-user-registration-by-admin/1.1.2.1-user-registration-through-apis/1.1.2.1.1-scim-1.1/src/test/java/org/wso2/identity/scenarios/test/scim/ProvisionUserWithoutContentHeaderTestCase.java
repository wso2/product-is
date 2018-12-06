/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.test.scim;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * This test class is to validate when
 * provisioning a user without content headers via SCIM 1.1.
 */
public class ProvisionUserWithoutContentHeaderTestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String scimEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
    }

    @Test(description = "1.1.2.1.1.6")
    public void testSCIMUserWithoutContentHeader() throws Exception {

        scimEndpoint = getDeploymentProperties().getProperty(IS_HTTPS_URL) + SCIMConstants.URL_PATH_SEPARATOR +
                Constants.SCIMEndpoints.SCIM1_ENDPOINT + SCIMConstants.URL_PATH_SEPARATOR + Constants.SCIMEndpoints.SCIM_ENDPOINT_USER;

        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        JSONObject rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE, schemas);
        JSONObject names = new JSONObject();
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_ACCEPTABLE,
                "User creation should fail without auth header hence server should have returned an unauthorized message");
        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        JSONArray schemasArray = (JSONArray) ((JSONObject) responseObj).get("Errors");
        assertNotNull(schemasArray);
        verifyUser();
    }

    /**
     * This method is used to verify whether added user is not exists.
     */
    private void verifyUser() throws IOException {

        String userResourcePath = scimEndpoint + "?filter=" + SCIMConstants.USER_NAME_ATTRIBUTE + SCIMConstants.Operators.EQUAL + SCIMConstants.USERNAME;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND, "Users not found in the user store for the filter");
    }

}

