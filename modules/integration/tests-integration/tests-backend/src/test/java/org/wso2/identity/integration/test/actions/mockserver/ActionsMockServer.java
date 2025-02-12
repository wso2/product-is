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

package org.wso2.identity.integration.test.actions.mockserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Provides a mock server using WireMock for testing purposes.
 * This class starts a mock server on a specified port and sets up predefined
 * responses for POST requests to simulate various operations relation to action execution.
 */
public class ActionsMockServer {

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
}
