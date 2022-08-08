/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendDeleteRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendUpdateRequest;
import static org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil.getCommonHeaders;

public class SCIM2CommonClient {

    private static final String SCIM2_USERS_LOCATION = "scim2.users.location";

    private static final String SCIM2_GROUPS_LOCATION = "scim2.groups.location";

    private static final String SCIM2_ENDPOINT = "scim2";

    private static final String SCIM2_ENDPOINT_USERS = "Users";

    private static final String SCIM2_ENDPOINT_GROUPS = "Groups";

    private String identityServerHttpsUrl;

    private JSONParser parser;

    public SCIM2CommonClient(String identityServerHttpsUrl) {

        this.identityServerHttpsUrl = identityServerHttpsUrl;
        parser = new JSONParser();
    }

    /**
     * Get user json object from a file.
     *
     * @param fileName File name.
     * @return User json object.
     * @throws Exception Exception.
     */
    public JSONObject getUserJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(fileName, SCIM2_USERS_LOCATION)));
    }

    /**
     * Get role json object from a file.
     *
     * @param fileName File name.
     * @return Role json object.
     * @throws Exception Exception.
     */
    public JSONObject getRoleJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(fileName, SCIM2_GROUPS_LOCATION)));
    }

    /**
     * Get file path.
     *
     * @param fileName File name.
     * @return File path.
     * @throws Exception Exception.
     */
    public String getFilePath(String fileName, String fileLocation) throws Exception {

        Path path = Paths.get(System.getProperty(fileLocation) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    /**
     * Provision user.
     *
     * @param client   Http client.
     * @param userJSON User object.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse provisionUser(HttpClient client, JSONObject userJSON, String username, String password)
            throws Exception {

        return sendPostRequestWithJSON(client, getSCIM2UsersEndpoint(), userJSON, getCommonHeaders(username, password));
    }

    /**
     * Provision user.
     *
     * @param client    Http client.
     * @param groupJSON Group object.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse provisionGroup(HttpClient client, JSONObject groupJSON, String username, String password)
            throws Exception {

        return sendPostRequestWithJSON(client, getSCIM2GroupsEndpoint(), groupJSON,
                getCommonHeaders(username, password));
    }

    /**
     * Get user.
     *
     * @param client Http client.
     * @param userId User id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse getUser(HttpClient client, String userId, String username, String password) throws Exception {

        return sendGetRequest(client, getSCIM2UsersEndpoint() + "/" + userId, null,
                getCommonHeaders(username, password));
    }

    public HttpResponse filterUserByAttribute(HttpClient client, String attribute, String operation, String value,
                                              String admin, String password) throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("filter", attribute + " " + operation + " " + value);
        return sendGetRequest(client, getSCIM2UsersEndpoint() + "/", params, getCommonHeaders(admin, password));
    }

    /**
     * Delete group.
     *
     * @param client  Http client.
     * @param groupId Group id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse getGroup(HttpClient client, String groupId, String username, String password) throws Exception {

        return sendGetRequest(client, getSCIM2GroupsEndpoint() + "/" + groupId, null,
                getCommonHeaders(username, password));
    }

    /**
     * Delete user.
     *
     * @param client Http client.
     * @param userId User id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse deleteUser(HttpClient client, String userId, String username, String password)
            throws Exception {

        return sendDeleteRequest(client, getSCIM2UsersEndpoint() + "/" + userId, getCommonHeaders(username, password));
    }

    /**
     * Delete group.
     *
     * @param client  Http client.
     * @param groupId Group id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse deleteGroup(HttpClient client, String groupId, String username, String password)
            throws Exception {

        return sendDeleteRequest(client, getSCIM2GroupsEndpoint() + "/" + groupId,
                getCommonHeaders(username, password));
    }

    /**
     * Update group.
     *
     * @param client    Http client.
     * @param groupJSON updated Group object.
     * @param groupId   group id.
     * @param username  username.
     * @param password  password.
     * @return Http response.
     * @throws Exception Exception.
     */
    public HttpResponse updateGroup(HttpClient client, JSONObject groupJSON, String groupId, String username,
                                    String password) throws Exception {

        return sendUpdateRequest(client, groupJSON, getSCIM2GroupsEndpoint() + "/" + groupId,
                getCommonHeaders(username, password));
    }

    /**
     * SCIM2 user endpoint.
     *
     * @return Path to user endpoint.
     */
    public String getSCIM2UsersEndpoint() {

        return this.identityServerHttpsUrl + "/" + SCIM2_ENDPOINT + "/" + SCIM2_ENDPOINT_USERS;
    }

    /**
     * SCIM2 group endpoint.
     *
     * @return Path to group endpoint.
     */
    public String getSCIM2GroupsEndpoint() {

        return this.identityServerHttpsUrl + "/" + SCIM2_ENDPOINT + "/" + SCIM2_ENDPOINT_GROUPS;
    }
}
