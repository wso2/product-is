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

package org.wso2.identity.scenarios.commons.util;

import org.apache.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import java.io.IOException;

import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendDeleteRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithJSON;

/**
 *  Utility class for common functions for user management scenario.
 */
public class SCIMProvisioningUtil {

    /**
     * Headers
     * @param username    Authenticating username.
     * @param password    Authenticating user password.
     */
    private static Header[]  getCommonHeaders(String username, String password) {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(username, password))
        };
        return headers;
    }

    /**
     * Provision a new user
     * @param serverURL              the server url
     * @param jsonObject             the json object of the scim payload
     * @param username               Authenticating username.
     * @param password               Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
    public static HttpResponse provisionUserSCIM11(String serverURL, JSONObject jsonObject,
                                                   String username, String password) throws IOException {

        return provisionSCIM11Entity(serverURL, jsonObject, username, password);
    }

    /**
     * Provision a new SCIM 1.1 entity
     * @param serverURL              the server url
     * @param jsonObject             the json object of the scim payload
     * @param username               Authenticating username.
     * @param password               Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
    public static HttpResponse provisionSCIM11Entity(String serverURL, JSONObject jsonObject,
                                                     String username, String password) throws IOException {

        HttpClient client = HttpClients.createDefault();
        String scimEndpoint = serverURL + Constants.SCIM_11_ENDPOINT + "/" + Constants.SCIM_ENDPOINT_USER;

        return sendPostRequestWithJSON(client, scimEndpoint, jsonObject, getCommonHeaders(username, password));
    }

    /**
     * Delete a user
     * @param serverURL              the server url.
     * @param userId                 the ID of the user to be deleted.
     * @param username               Authenticating username.
     * @param password               Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user deletion.
     */
    public static HttpResponse deleteUser(String serverURL, String userId,
                                          String username, String password) throws IOException {

        return deleteSCIM11Entity(serverURL, userId, username, password);
    }

    /**
     * Delete a SCIM 1.1 entity
     * @param serverURL              the server url
     * @param userID                 the ID of the user to be deleted
     * @param username               Authenticating username.
     * @param password               Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user deletion.
     */
    public static HttpResponse deleteSCIM11Entity(String serverURL, String userID,
                                                  String username, String password) throws IOException {

        HttpClient client = HttpClients.createDefault();
        String scimEndpoint = serverURL + Constants.SCIM_11_ENDPOINT + "/" + Constants.SCIM_ENDPOINT_USER+ "/" + userID;

        return sendDeleteRequest(client, scimEndpoint, getCommonHeaders(username, password));
    }
}
