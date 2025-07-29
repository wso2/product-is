/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.webhooks.mockservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Mock service for handling webhook events.
 */
public class WebhookMockService {

    private WireMockServer wireMockServer;

    private final List<Request> orderedRequests = Collections.synchronizedList(new ArrayList<>());

    private static final Logger LOG = LoggerFactory.getLogger(WebhookMockService.class);

    public void startServer(int port) {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);

        // Log all received requests
        wireMockServer.addMockServiceRequestListener((request, response) -> {
            LOG.info("Received request: {} for webhook: {}", request, wireMockServer.baseUrl());
            orderedRequests.add(request);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));
    }

    public void stopServer() {

        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public String registerWebhookEndpoint(String path) {

        stubFor(post(urlEqualTo(path))
                .willReturn(aResponse().withStatus(200)));

        int port = wireMockServer.getOptions().portNumber();
        return "http://localhost:" + port + path;
    }

    public List<Request> getOrderedRequests() {

        return new ArrayList<>(orderedRequests);
    }
}
