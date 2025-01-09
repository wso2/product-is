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

package org.wso2.identity.integration.test.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Mock SMS Provider for testing SMS related flows.
 */
public class MockSMSProvider {

    public static final String SMS_SENDER_URL = "https://localhost:8090/sms/send";
    public static final String SMS_SENDER_PROVIDER_TYPE = "Custom";

    private WireMockServer wireMockServer;
    private final AtomicReference<String> otp = new AtomicReference<>();

    public void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpsPort(8090)
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

                                // Extract the content value from the request body.
                                String content =
                                        JsonPath.parse(serveEvent.getRequest().getBodyAsString()).read("$.content");

                                String regex = "\\b\\d{6}\\b";

                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(content);

                                if (matcher.find()) {
                                    String extractedOtp = matcher.group();
                                    // Store the content value for later use.
                                    otp.set(extractedOtp);
                                }
                                return response;
                            }

                            @Override
                            public boolean applyGlobally() {
                                return false;
                            }

                            @Override
                            public String getName() {
                                return "otp-transformer";
                            }
                        }));

        wireMockServer.start();

        // Configure the mock SMS endpoints.
        configureMockEndpoints();
    }

    public void stop() {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void configureMockEndpoints() {

        try {
            wireMockServer.stubFor(post(urlEqualTo("/sms/send"))
                    .withRequestBody(matchingJsonPath("$.content"))
                    .withRequestBody(matchingJsonPath("$.to"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "otp-transformer")
                            .withStatus(200)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getOTP() {

        return otp.get();
    }
}
