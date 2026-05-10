/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.serviceextensions.mockservices;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Provides a mock server using WireMock for testing purposes.
 * This class starts a mock server on a specified port and sets up predefined
 * responses for POST requests to simulate various operations relation to action execution.
 */
public class ServiceExtensionMockServer {

    private WireMockServer wireMockServer;

    public void startServer() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8587));
        wireMockServer.start();
    }

    public void stopServer() {

        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public void setupStub(String url, String authMethod, String responseBody) {

        wireMockServer.stubFor(post(urlEqualTo(url))
                .withHeader("Authorization", matching(authMethod))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "Close")
                        .withBody(responseBody)));
    }

    public void setupStub(String url, String authMethod, String responseBody, int statusCode) {

        wireMockServer.stubFor(post(urlEqualTo(url))
                .withHeader("Authorization", matching(authMethod))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "Close")
                        .withBody(responseBody)));
    }

    public String getReceivedRequestPayload(String url) {

        List<LoggedRequest> requestList = wireMockServer.findAll(postRequestedFor(urlEqualTo(url)));
        if (requestList == null || requestList.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return requestList.get(0).getBodyAsString();
    }

    public void resetRequests() {
        wireMockServer.resetRequests();
    }

    /**
     * Stub a token endpoint that returns a Bearer access token only when the request has the
     * expected Basic auth header and a form body containing the password grant fields.
     *
     * @param url             URL of the token endpoint (path).
     * @param basicAuthHeader Expected Authorization header value (regex-matched).
     * @param accessToken     Access token to return on a successful match.
     * @param username        Expected username form value.
     * @param password        Expected password form value.
     */
    public void setupTokenEndpointStub(String url, String basicAuthHeader, String accessToken,
                                       String username, String password) {

        String responseBody = "{\"access_token\":\"" + accessToken +
                "\",\"token_type\":\"Bearer\",\"expires_in\":3600}";

        wireMockServer.stubFor(post(urlEqualTo(url))
                .withHeader("Authorization", matching(basicAuthHeader))
                .withRequestBody(containing("grant_type=password"))
                .withRequestBody(containing("username=" + username))
                .withRequestBody(containing("password=" + password))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "Close")
                        .withBody(responseBody)));
    }

    /**
     * Stub a token endpoint that responds with the given status code and body for any POST.
     * Used in failure tests to simulate an unhealthy IdP.
     */
    public void setupTokenEndpointStubWithError(String url, int statusCode, String responseBody) {

        wireMockServer.stubFor(post(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "Close")
                        .withBody(responseBody)));
    }

    /**
     * Count the number of POST requests received at the given URL.
     */
    public int getReceivedRequestCount(String url) {

        List<LoggedRequest> requestList = wireMockServer.findAll(postRequestedFor(urlEqualTo(url)));
        return requestList == null ? 0 : requestList.size();
    }
}
