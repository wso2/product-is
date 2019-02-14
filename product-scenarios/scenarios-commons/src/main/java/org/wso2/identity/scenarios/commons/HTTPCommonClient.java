/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.BASIC;

public class HTTPCommonClient {

    private CloseableHttpClient client;

    public HTTPCommonClient() {

        this.client = HttpClients.createDefault();
    }

    /**
     * Send GET request for a given URL with the request query parameters.
     *
     * @param url     Request URL.
     * @param params  Request query parameters.
     * @param headers Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException        If error occurs while sending the request.
     * @throws URISyntaxException If error occurs while constructing the request URL.
     */
    public HttpResponse sendGetRequest(String url, Map<String, String> params, Header[] headers)
            throws IOException, URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        HttpGet getRequest = new HttpGet(uriBuilder.build());
        if (headers != null) {
            getRequest.setHeaders(headers);
        }

        return this.client.execute(getRequest);
    }

    /**
     * Send POST request with a JSON payload.
     *
     * @param url        Request URL.
     * @param jsonObject JSON object for post request.
     * @param headers    Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public HttpResponse sendPostRequestWithJSON(String url, JSONObject jsonObject, Header[] headers)
            throws IOException {

        HttpPost request = new HttpPost(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonObject.toString()));

        return this.client.execute(request);
    }

    /**
     * Send POST request with a payload.
     *
     * @param urlParameters Request POST parameters.
     * @param url           Request URL.
     * @param headers       Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public HttpResponse sendPostRequestWithParameters(String url, List<NameValuePair> urlParameters, Header[] headers)
            throws IOException {

        HttpPost request = new HttpPost(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return this.client.execute(request);
    }

    /**
     * Send PUT request with a JSON payload.
     *
     * @param url        Request URL.
     * @param jsonObject JSON object for post request.
     * @param headers    Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public HttpResponse sendPutRequestWithJSON(String url, JSONObject jsonObject, Header[] headers) throws IOException {

        HttpPut request = new HttpPut(url);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(jsonObject.toString()));

        return this.client.execute(request);
    }

    /**
     * Send DELETE request for a given URL.
     *
     * @param url     Request URL.
     * @param headers Request headers.
     * @return HttpResponse containing the response.
     * @throws IOException If error occurs while sending the request.
     */
    public HttpResponse sendDeleteRequest(String url, Header[] headers) throws IOException {

        HttpDelete deleteRequest = new HttpDelete(url);
        if (headers != null) {
            deleteRequest.setHeaders(headers);
        }

        return client.execute(deleteRequest);
    }

    public void closeHttpClient() throws IOException {

        this.client.close();
    }

    public void consume(HttpResponse response) throws IOException {

        EntityUtils.consume(response.getEntity());
    }

    /**
     * Read JSON payload from the response.
     *
     * @param response HttpResponse with data.
     * @return JSON parsed payload.
     * @throws IOException If error occurs while parsing JSON payload.
     */
    public JSONObject getJSONFromResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return (JSONObject) JSONValue.parse(rd);
    }

    /**
     * Read String payload from the response.
     *
     * @param response HttpResponse with data.
     * @return String payload.
     * @throws IOException If error occurs while building string payload.
     */
    public String getStringFromResponse(HttpResponse response) throws IOException {

        return IOUtils.toString(new InputStreamReader(response.getEntity().getContent()));
    }

    public String getBasicAuthorizeHeader(String key, String secret) {

        Base64.Encoder encoder = Base64.getEncoder();
        String encodedHeader = encoder.encodeToString(String.join(":", key, secret).getBytes());
        return String.join(" ", BASIC, encodedHeader);
    }
}
