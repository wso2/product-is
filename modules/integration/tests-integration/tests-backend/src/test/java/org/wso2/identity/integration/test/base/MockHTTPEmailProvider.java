/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Mock HTTP-based Email Provider for testing custom (non-SMTP) email delivery flows.
 */
public class MockHTTPEmailProvider {

    public static final String EMAIL_SENDER_URL = "https://localhost:8094/email/send";
    public static final String EMAIL_SENDER_PATH = "/email/send";

    private WireMockServer wireMockServer;
    private final AtomicReference<String> emailBody = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> headers = new AtomicReference<>(new HashMap<>());

    public void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpsPort(8094)
                .httpDisabled(true)
                .keystorePath(Paths.get(Utils.getResidentCarbonHome(), "repository", "resources", "security",
                        ISIntegrationTest.KEYSTORE_NAME).toAbsolutePath().toString())
                .keystorePassword("wso2carbon")
                .keyManagerPassword("wso2carbon")
                .extensions(
                        new ResponseTemplateTransformer(null, true, null, null),
                        new ResponseTransformerV2() {
                            @Override
                            public Response transform(Response response, ServeEvent serveEvent) {

                                emailBody.set(serveEvent.getRequest().getBodyAsString());

                                clearHeaders();
                                Map<String, String> requestHeaders = new HashMap<>();
                                serveEvent.getRequest().getHeaders().all().forEach(header -> {
                                    requestHeaders.put(header.key(), header.firstValue());
                                });
                                headers.set(requestHeaders);

                                return response;
                            }

                            @Override
                            public boolean applyGlobally() {
                                return false;
                            }

                            @Override
                            public String getName() {
                                return "email-capture-transformer";
                            }
                        }));

        wireMockServer.start();
        configureMockEndpoints();
    }

    public void stop() {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void configureMockEndpoints() {

        try {
            wireMockServer.stubFor(post(urlEqualTo(EMAIL_SENDER_PATH))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "email-capture-transformer")
                            .withStatus(200)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the full body of the last email send request.
     *
     * @return The last email request body.
     */
    public String getEmailBody() {

        return emailBody.get();
    }

    /**
     * Clear stored email body.
     */
    public void clearEmailBody() {

        emailBody.set(null);
    }

    /**
     * Get all headers received from the last email send request.
     *
     * @return Map of header names to header values.
     */
    public Map<String, String> getHeaders() {

        return headers.get();
    }

    /**
     * Get a specific header value from the last email send request.
     *
     * @param headerName The name of the header to retrieve.
     * @return The header value, or null if not found.
     */
    public String getHeader(String headerName) {

        return headers.get().get(headerName);
    }

    /**
     * Clear stored headers.
     */
    public void clearHeaders() {

        headers.set(new HashMap<>());
    }
}
