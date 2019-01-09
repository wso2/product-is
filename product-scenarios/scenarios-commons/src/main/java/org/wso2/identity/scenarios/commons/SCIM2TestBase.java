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

import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendDeleteRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;
import static org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil.getCommonHeaders;

public class SCIM2TestBase extends ScenarioTestBase {

    private static final String SCIM2_USERS_LOCATION = "scim2.users.location";

    private static final String SCIM2_ENDPOINT = "scim2";

    private static final String SCIM2_ENDPOINT_USERS = "Users";

    private static final String SCIM2_ENDPOINT_GROUPS = "Groups";

    private JSONParser parser;

    public void init() throws Exception {

        super.init();
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

        return (JSONObject) parser.parse(new FileReader(getFilePath(fileName)));
    }

    /**
     * Get role json object from a file.
     *
     * @param fileName File name.
     * @return Role json object.
     * @throws Exception Exception.
     */
    public JSONObject getRoleJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(fileName)));
    }

    /**
     * Get file path.
     *
     * @param fileName File name.
     * @return File path.
     * @throws Exception Exception.
     */
    public String getFilePath(String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(SCIM2_USERS_LOCATION) + fileName);
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
    public HttpResponse provisionUser(HttpClient client, JSONObject userJSON) throws Exception {

        System.out.println(getSCIM2UsersEndpoint());
        System.out.println(userJSON.toJSONString());
        return sendPostRequestWithJSON(client, getSCIM2UsersEndpoint(), userJSON,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * Provision user.
     *
     * @param client    Http client.
     * @param groupJSON Group object.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse provisionGroup(HttpClient client, JSONObject groupJSON) throws Exception {

        return sendPostRequestWithJSON(client, getSCIM2GroupsEndpoint(), groupJSON,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * Get user.
     *
     * @param client Http client.
     * @param userId User id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse getUser(HttpClient client, String userId) throws Exception {

        return sendGetRequest(client, getSCIM2UsersEndpoint() + "/" + userId, null,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * Delete group.
     *
     * @param client Http client.
     * @param groupId Group id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse getGroup(HttpClient client, String groupId) throws Exception {

        return sendGetRequest(client, getSCIM2GroupsEndpoint() + "/" + groupId, null,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * Delete user.
     *
     * @param client Http client.
     * @param userId User id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse deleteUser(HttpClient client, String userId) throws Exception {

        return sendDeleteRequest(client, getSCIM2UsersEndpoint() + "/" + userId,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * Delete group.
     *
     * @param client  Http client.
     * @param groupId Group id.
     * @return Http Response.
     * @throws Exception Exception.
     */
    public HttpResponse deleteGroup(HttpClient client, String groupId) throws Exception {

        return sendDeleteRequest(client, getSCIM2GroupsEndpoint() + "/" + groupId,
                getCommonHeaders(ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * SCIM2 user endpoint.
     *
     * @return Path to user endpoint.
     */
    public String getSCIM2UsersEndpoint() {

        return backendURL + "/" + SCIM2_ENDPOINT + "/" + SCIM2_ENDPOINT_USERS;
    }

    /**
     * SCIM2 group endpoint.
     *
     * @return Path to group endpoint.
     */
    public String getSCIM2GroupsEndpoint() {

        return backendURL + "/" + SCIM2_ENDPOINT + "/" + SCIM2_ENDPOINT_GROUPS;
    }
}
