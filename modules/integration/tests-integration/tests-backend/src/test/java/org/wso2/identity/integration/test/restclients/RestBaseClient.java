/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.restclients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class RestBaseClient {

    public static final String LOCATION_HEADER = "Location";
    public static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    public static final String BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE = "Bearer ";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    public static final String USER_AGENT_ATTRIBUTE = "User-Agent";
    public static final String API_SERVER_PATH = "api/server/v1";
    public static final String TENANT_PATH = "t/";
    public static final String ORGANIZATION_PATH = "o/";
    public static final String PATH_SEPARATOR = "/";
    public static final String OIDC = "oidc";
    public static final String SAML = "saml";
    public final CloseableHttpClient client;

    public RestBaseClient() {
        client = HttpClients.createDefault();
    }

    /**
     * To convert object to a json string.
     *
     * @param object Respective java object.
     * @return Relevant json string.
     */
    public String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    /**
     * To convert a string from a Http response to a json object.
     *
     * @param responseString Respective Http response.
     * @return Relevant json object.
     * @throws Exception If an error occurred while getting a JSON object from a json string.
     */
    public JSONObject getJSONObject(String responseString) throws Exception {

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception("Error occurred while getting the response");
        }

        return json;
    }

    /**
     * To convert a string from a Http response to a json array.
     *
     * @param responseString Respective Http response.
     * @return Relevant JSONArray object.
     * @throws Exception If an error occurred while getting a JSON array from a JSON string.
     */
    public JSONArray getJSONArray(String responseString) throws Exception {

        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(responseString);
        if (jsonArray == null) {
            throw new Exception("Error occurred while getting the response");
        }

        return jsonArray;
    }

    /**
     * Execute and get the response of HTTP POST.
     *
     * @param endPointUrl REST endpoint.
     * @param jsonRequest Json body.
     * @param headers Header list of the request.
     * @return Response of the Http request.
     * @throws IOException If an error occurred while executing http POST request.
     */
    public CloseableHttpResponse getResponseOfHttpPost(String endPointUrl, String jsonRequest, Header[] headers)
            throws IOException {

        HttpPost request = new HttpPost(endPointUrl);
        request.setHeaders(headers);
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    /**
     * Execute and get the response of HTTP GET.
     *
     * @param endPointUrl REST endpoint.
     * @param headers     Header list of the request.
     * @return Response of the Http request.
     * @throws IOException If an error occurred while executing http GET request.
     */
    public CloseableHttpResponse getResponseOfHttpGet(String endPointUrl, Header[] headers)
            throws IOException {

        HttpGet request = new HttpGet(endPointUrl);
        request.setHeaders(headers);

        return client.execute(request);
    }

    /**
     * Execute and get the response of HTTP GET with query parameters.
     *
     * @param endPointUrl REST endpoint.
     * @param headers     Header list of the request.
     * @param queryParams Query parameters.
     * @return Response of the Http request.
     * @throws Exception If an error occurred while executing http GET request with query parameters.
     */
    public CloseableHttpResponse getResponseOfHttpGetWithQueryParams(String endPointUrl, Header[] headers,
                                                                     Map<String, String> queryParams)
            throws Exception {

        URIBuilder uriBuilder = new URIBuilder(endPointUrl);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), entry.getValue());
        }
        URI requestUri = uriBuilder.build();
        HttpGet request = new HttpGet(requestUri);
        request.setHeaders(headers);

        return client.execute(request);
    }

    /**
     * Execute and get the response of HTTP PATCH.
     *
     * @param endPointUrl REST endpoint.
     * @param jsonRequest Json body.
     * @param headers     Header list of the request.
     * @return Response of the Http request.
     * @throws IOException If an error occurred while executing http PATCH request.
     */
    public CloseableHttpResponse getResponseOfHttpPatch(String endPointUrl, String jsonRequest, Header[] headers) throws IOException {

        HttpPatch request = new HttpPatch(endPointUrl);
        request.setHeaders(headers);
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    /**
     * Execute and get the response of HTTP DELETE.
     *
     * @param endPointUrl REST endpoint.
     * @param headers     Header list of the request.
     * @return Response of the Http request.
     * @throws IOException If an error occurred while executing http DELETE request.
     */
    public CloseableHttpResponse getResponseOfHttpDelete(String endPointUrl, Header[] headers) throws IOException {

        HttpDelete request = new HttpDelete(endPointUrl);
        request.setHeaders(headers);

        return client.execute(request);
    }

    /**
     * Execute and get the response of HTTP PUT.
     *
     * @param endPointUrl REST endpoint.
     * @param jsonRequest Json body.
     * @param headers     Header list of the request.
     * @return Response of the Http request.
     * @throws IOException If an error occurred while executing http PUT request.
     */
    public CloseableHttpResponse getResponseOfHttpPut(String endPointUrl, String jsonRequest, Header[] headers)
            throws IOException {

        HttpPut request = new HttpPut(endPointUrl);
        request.setHeaders(headers);
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }
}
