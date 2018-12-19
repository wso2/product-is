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

package org.wso2.identity.scenarios.commons;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import org.wso2.identity.scenarios.test.scim2.SCIMConstants;
import java.io.IOException;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;


public class SCIM2TestBase extends ScenarioTestBase {

    private static String scimEndpointURL;
    private static  final String URL_PATH_SEPARATOR = "/";
    private static String meEndpoint = "Me";


    /**
     * Headers
     * @param username Authenticating username.
     * @param password Authenticating user password.
     */
    public static Header[] getCommonHeaders(String username, String password) {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(username, password))
        };
        return headers;
    }

   /**
     * Provision a new user from Me endpoint
     *
     * @param serverURL        the server url
     * @param scimEndPoint     the scim endpoint for SCIM 2.0
     * @param scimResource the scim endpoint for Me endpoint
     * @param username         Authenticating username.
     * @param password         Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
    public static HttpResponse provisionMe(String serverURL, JSONObject jsonObject, String scimEndPoint, String
            scimResource, String username, String password) throws IOException {

        return provisionMeEntity(serverURL, jsonObject, scimEndPoint, scimResource, username, password);
    }

    /**
     * Provision a new  Me endpoint SCIM entity
     *
     * @param serverURL        the server url
     * @param scimEndPoint     the scim endpoint SCIM 2.0
     * @param scimResource     the scim endpoint for Me endpoint
     * @param jsonObject       the json object of the scim payload
     * @param username         Authenticating username.
     * @param password         Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
    public static HttpResponse provisionMeEntity(String serverURL, JSONObject jsonObject, String scimEndPoint, String
            scimResource, String username, String password) throws IOException {

        HttpClient client = HttpClients.createDefault();
        scimEndpointURL = serverURL + URL_PATH_SEPARATOR + scimEndPoint + URL_PATH_SEPARATOR + meEndpoint;

        return sendJSONRequest(client, scimEndpointURL, jsonObject, getCommonHeaders(username, password));
    }



    /**
     * Send POST request with a JSON payload.
     *
     * @param client     HttpClient to be used for request sending.
     * @param url        Request URL.
     * @param jsonObject JSON object for post request.
     * @param headers    Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public static HttpResponse sendJSONRequest(HttpClient client, String url,
                                               JSONObject jsonObject, Header[] headers) throws IOException {

        HttpPost request = new HttpPost(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonObject.toString()));

        return client.execute(request);
    }



    /**
     * Provision a new user without auth header
     *
     * @param serverURL        the server url
     * @param scimEndPoint     the scim endpoint for  SCIM 2.0
     * @param scimResource the scim endpoint for Me endpoint
     * @param username         Authenticating username.
     * @param password         Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
    public static HttpResponse provisionMeWithoutAuth(String serverURL, JSONObject jsonObject, String scimEndPoint,
                String scimResource, String username, String password) throws IOException {

            return provisionMeEntityWithoutAuth(serverURL, jsonObject, scimEndPoint, scimResource, username, password);
    }

    /**
     * Provision a new SCIM entity for Me endpoint without auth header
     *
     * @param serverURL        the server url
     * @param scimEndPoint     the scim endpoint for SCIM 2.0
     * @param scimResource     the scim endpoint for Me endpoint
     * @param jsonObject       the json object of the scim payload
     * @param username         Authenticating username.
     * @param password         Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during user creation.
     */
      public static HttpResponse provisionMeEntityWithoutAuth(String serverURL, JSONObject jsonObject, String
                scimEndPoint, String scimResource, String username, String password) throws IOException {

            HttpClient client = HttpClients.createDefault();
            scimEndpointURL = serverURL + URL_PATH_SEPARATOR + scimEndPoint + URL_PATH_SEPARATOR + meEndpoint;

            return sendJSONRequest(client, scimEndpointURL, jsonObject, getContentTypeHeaders());
     }


    /**
     * This method calls only the content type header
     */
    public static Header[] getContentTypeHeaders() {

        Header[] headers = {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, SCIMConstants.CONTENT_TYPE_APPLICATION_JSON),
        };
        return headers;

    }

}
