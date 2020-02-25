/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.wso2.identity.integration.test.util.Utils.getBasicAuthHeader;

public class OAuth2ScopesTestCase extends ISIntegrationTest {

    public static final String SCOPE_ENDPOINT_SUFFIX = "/api/identity/oauth2/v1.0/scopes";
    private String isServerBackendUrl;
    private String scopeEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        scopeEndpoint = isServerBackendUrl + "/t/" + isServer.getContextTenant().getDomain() + SCOPE_ENDPOINT_SUFFIX;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add Scope test")
    public void testAddScope() throws IOException {

        String name = "profile";
        String displayName = "profile";
        String description = "get all profile information";
        String bindings = "[\"role1\",\"role2\"]";
        String locationHeader = scopeEndpoint + "/name/" + name;

        JSONObject response = addScope(name, displayName, description, bindings);

        Assert.assertEquals(name, response.get("name").toString());
        Assert.assertEquals(displayName, response.get("displayName").toString());
        Assert.assertEquals(description, response.get("description").toString());
        Assert.assertEquals(bindings, response.get("bindings").toString());
        Assert.assertEquals(locationHeader, response.get("location").toString());
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Get All Scope test", dependsOnMethods = {"testAddScope"})
    public void testGetAllScopes() throws IOException {

        String name1 = "profile";
        String name2 = "phone";
        String displayName = "phone";
        String description = "get the phone information";
        String bindings = "[\"role3\",\"role4\"]";

        addScope(name2, displayName, description, bindings);

        JSONArray response = getAllScopes();

        Map<String, JSONObject> responseItems = IntStream.range(0, response.size())
                .mapToObj(i -> (JSONObject) JSONValue.parse(response.get(i).toString()))
                .collect(Collectors.toMap(item -> (String) item.get("name"), item -> item, (a, b) -> b));

        Assert.assertNotNull(responseItems.get(name1));
        Assert.assertNotNull(responseItems.get(name2));
    }


    @Test(alwaysRun = true, groups = "wso2.is", description = "Get Scope test", dependsOnMethods = { "testGetAllScopes" })
    public void testGetScope() throws IOException {

        String name = "profile";
        String displayName = "profile";
        String description = "get all profile information";
        String bindings = "[\"role1\",\"role2\"]";

        JSONObject response = getScope(name);

        Assert.assertEquals(name, response.get("name").toString());
        Assert.assertEquals(displayName, response.get("displayName").toString());
        Assert.assertEquals(description, response.get("description").toString());
        Assert.assertEquals(bindings, response.get("bindings").toString());
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Update Scope test", dependsOnMethods = { "testGetScope" })
    public void testUpdateScope() throws IOException {

        String name = "profile";
        String displayName = "profile";
        String updatedDescription = "get all user profile information";
        String updatedBindings = "[\"role3\",\"role2\"]";

        JSONObject response = updateScope(name, displayName, updatedDescription, updatedBindings);

        Assert.assertEquals(name, response.get("name").toString());
        Assert.assertEquals(displayName, response.get("displayName").toString());
        Assert.assertEquals(updatedDescription, response.get("description").toString());
        Assert.assertEquals(updatedBindings, response.get("bindings").toString());
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Check existence of Scope test", dependsOnMethods = { "testUpdateScope" })
    public void testScopeExistence() throws IOException {

        String name = "profile";
        ClientResponse response = isScopeExists(name);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Delete Scope test", dependsOnMethods = { "testScopeExistence" })
    public void testDeleteScope() throws IOException {

        String name = "profile";

        ClientResponse response = deleteScope(name);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), isScopeExists(name).getStatusCode());
    }


    private JSONObject addScope(String name, String displayName, String description, String bindings) {

        Resource userResource = getUserResource(null);

        String addScopeString = "{\"name\": " + "\""+name+"\"" + ", \"displayName\": " + "\""+displayName+"\"" + ", " +
                "\"description\": " + "\""+description+"\"" + ", \"bindings\": " + bindings + "}";

        ClientResponse clientResponse = userResource.contentType(MediaType.APPLICATION_JSON_TYPE).
                accept(MediaType.APPLICATION_JSON).post(addScopeString);

        String locationHeader = clientResponse.getHeaders().get("Location").get(0);

        JSONObject response = (JSONObject) JSONValue.parse(clientResponse.getEntity(String.class));
        response.put("location", locationHeader);

        return (response);
    }

    private JSONArray getAllScopes() {

        Resource userResource = getUserResource(null);

        String response = userResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType
                .APPLICATION_JSON).get(String.class);

        return (JSONArray) JSONValue.parse(response);
    }

    private JSONObject getScope(String name) {

        Resource userResource = getUserResource("/name/" + name);

        String response = userResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType
                .APPLICATION_JSON).get(String.class);

        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject updateScope(String name, String displayName, String updatedDescription, String updatedBindings) {

        Resource userResource = getUserResource("/name/" + name);

        String updateScopeString = "{\"description\": " + "\""+updatedDescription+"\"" + ", \"displayName\": " +
                "\""+displayName+"\"" + ", \"bindings\": " + updatedBindings + "}";

        String response = userResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .put(String.class, updateScopeString);

        return (JSONObject) JSONValue.parse(response);
    }

    private ClientResponse isScopeExists(String name) {

        Resource userResource = getUserResource("/name/" + name);

        return userResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON).head();
    }

    private ClientResponse deleteScope(String name) {

        Resource userResource = getUserResource("/name/" + name);

        return userResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .delete(ClientResponse.class);
    }

    private Resource getUserResource(String scopeEndpointAppender) {

        RestClient restClient = new RestClient();
        Resource userResource;
        if(StringUtils.isNotBlank(scopeEndpointAppender)) {
            userResource = restClient.resource(scopeEndpoint + scopeEndpointAppender);
        } else {
            userResource = restClient.resource(scopeEndpoint);
        }
        return userResource.header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader(userInfo));
    }
}
