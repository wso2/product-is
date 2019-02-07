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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.BASIC;

/**
 * Utility class for common functions across all scenarios.
 */
public class IdentityScenarioUtil {

    private static final Log log = LogFactory.getLog(IdentityScenarioUtil.class);

    /**
     * Send POST request with a payload.
     *
     * @param client        HttpClient to be used for request sending.
     * @param urlParameters Request POST parameters.
     * @param url           Request URL.
     * @param headers       Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public static HttpResponse sendPostRequestWithParameters(HttpClient client, List<NameValuePair> urlParameters,
                                                             String url, Header[] headers) throws IOException {

        HttpPost request = new HttpPost(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
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
    public static HttpResponse sendPostRequestWithJSON(HttpClient client, String url,
                                                       JSONObject jsonObject, Header[] headers) throws IOException {

        HttpPost request = new HttpPost(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonObject.toString()));

        return client.execute(request);
    }

    /**
     * Send GET request for a given URL with the request query parameters.
     *
     * @param client HttpClient to be used for request sending.
     * @param url    Request URL.
     * @param params Request query parameters.
     * @return HttpResponse containing the response.
     * @throws IOException        If error occurs while sending the request.
     * @throws URISyntaxException If error occurs while constructing the request URL.
     */
    public static HttpResponse sendGetRequest(HttpClient client, String url, Map<String, String> params) throws
            IOException, URISyntaxException {

        return sendGetRequest(client, url, params, null);
    }

    /**
     * Send GET request for a given URL with the request query parameters.
     *
     * @param client  HttpClient to be used for request sending.
     * @param url     Request URL.
     * @param params  Request query parameters.
     * @param headers Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException        If error occurs while sending the request.
     * @throws URISyntaxException If error occurs while constructing the request URL.
     */
    public static HttpResponse sendGetRequest(HttpClient client, String url, Map<String, String> params,
                                              Header[] headers) throws IOException, URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        URI uri = uriBuilder.build();

        log.info("Error Info:7 " +  uri.toString());

        HttpGet getRequest = new HttpGet(uri);
        if (headers != null) {
            getRequest.setHeaders(headers);
        }

        return client.execute(getRequest);
    }

    /**
     * Send DELETE request for a given URL.
     *
     * @param client  HttpClient to be used for request sending.
     * @param url     Request URL.
     * @param headers Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public static HttpResponse sendDeleteRequest(HttpClient client, String url, Header[] headers) throws IOException {

        HttpDelete deleteRequest = new HttpDelete(url);
        if (headers != null) {
            deleteRequest.setHeaders(headers);
        }

        return client.execute(deleteRequest);
    }

    /**
     * Constructs basic authorization header.
     *
     * @param key    Credential identifier.
     * @param secret Credential secret.
     * @return Basic authorization header.
     */
    public static String constructBasicAuthzHeader(String key, String secret) {

        Base64.Encoder encoder = Base64.getEncoder();
        String encodedHeader = encoder.encodeToString(String.join(":", key, secret).getBytes());
        return String.join(" ", BASIC, encodedHeader);
    }

    /**
     * Read JSON payload from the response.
     *
     * @param response HttpResponse with data.
     * @return JSON parsed payload.
     * @throws IOException If error occurs while parsing JSON payload.
     */
    public static JSONObject getJSONFromResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return (JSONObject) JSONValue.parse(rd);
    }

    /**
     * Decode base64 encoded String
     *
     * @param encodedString string tobe Base64 decoded
     * @return Base64 decoded string
     */
    public static String bese64Decode(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }
}
