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
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Mock client callback endpoint to test OIDC related flows.
 */
public class MockApplicationServer {

    public class MockClient {
        private final AtomicReference<String> authorizationCode = new AtomicReference<>();
        private final AtomicReference<String> errorCode = new AtomicReference<>();

        public AtomicReference<String> getAuthorizationCode() {
            return authorizationCode;
        }
        
        public AtomicReference<String> getErrorCode() {
            return errorCode;
        }
    }

    public static class Constants {
        public static class APP1 {
            public static final String CALLBACK_URL = "https://localhost:8091/dummyApp/oauth2client";
            public static final String NAME = "playground.appone";
            public static final String CALLBACK_URL_PATH = "/dummyApp/oauth2client";
        }

        public static class APP2 {
            public static final String CALLBACK_URL = "https://localhost:8091/dummyApp2/oauth2client";
            public static final String NAME = "playground.apptwo";
            public static final String CALLBACK_URL_PATH = "/dummyApp2/oauth2client";
        }
    }

    private final Map<String, MockClient> apps = new HashMap<>();

    private WireMockServer wireMockServer;

    public MockApplicationServer() {
        
        MockClient app1 = new MockClient();
        MockClient app2 = new MockClient();
        apps.put(Constants.APP1.NAME, app1);
        apps.put(Constants.APP2.NAME, app2);
    }

    public void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpsPort(8091)
                .httpDisabled(true)
                .keystorePath(Paths.get(Utils.getResidentCarbonHome(), "repository", "resources", "security",
                        ISIntegrationTest.KEYSTORE_NAME).toAbsolutePath().toString())
                .keystorePassword("wso2carbon")
                .keyManagerPassword("wso2carbon")
                .extensions(new ResponseTemplateTransformer(null, true, null, null),
                        new ResponseTransformerV2() {

                            @Override
                            public Response transform(Response response, ServeEvent serveEvent) {

                                AtomicReference<String> authorizationCode
                                        = (AtomicReference<String>) serveEvent.getTransformerParameters().get("code");
                                authorizationCode.set(serveEvent.getRequest().getQueryParams().get("code")
                                        .firstValue());
                                return response;
                            }

                            @Override
                            public boolean applyGlobally() {
                                return false;
                            }

                            @Override
                            public String getName() {
                                return "authz-code-transformer";
                            }
                        },
                        new ResponseTransformerV2() {

                            @Override
                            public Response transform(Response response, ServeEvent serveEvent) {

                                AtomicReference<String> errorCode
                                        = (AtomicReference<String>) serveEvent.getTransformerParameters().get("error");
                                errorCode.set(serveEvent.getRequest().getQueryParams().get("error").firstValue());
                                return response;
                            }

                            @Override
                            public boolean applyGlobally() {
                                return false;
                            }

                            @Override
                            public String getName() {
                                return "error-code-transformer";
                            }
                        }));

        wireMockServer.start();

        // Configure the mock client endpoints for App 1
        configureMockEndpointsForApp(Constants.APP1.CALLBACK_URL_PATH, apps.get(Constants.APP1.NAME));
        // Configure the mock client endpoints for App 2
        configureMockEndpointsForApp(Constants.APP2.CALLBACK_URL_PATH, apps.get(Constants.APP2.NAME));
    }

    public void stop() {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void configureMockEndpointsForApp(String urlPath, MockClient app) {

        try {
            wireMockServer.stubFor(get(urlPathEqualTo(urlPath))
                    .withQueryParam("code", matching(".*"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "authz-code-transformer")
                            .withTransformerParameter("code", app.getAuthorizationCode())
                            .withTransformerParameter("error", app.getErrorCode())
                            .withStatus(200)));
            wireMockServer.stubFor(post(urlPathEqualTo(urlPath))
                    .withQueryParam("code", matching(".*"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "authz-code-transformer")
                            .withTransformerParameter("code", app.getAuthorizationCode())
                            .withTransformerParameter("error", app.getErrorCode())
                            .withStatus(200)));
            wireMockServer.stubFor(get(urlPathEqualTo(urlPath))
                    .withQueryParam("code", matching(".*"))
                    .withQueryParam("session_state", matching(".*"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "authz-code-transformer")
                            .withTransformerParameter("code", app.getAuthorizationCode())
                            .withTransformerParameter("error", app.getErrorCode())
                            .withStatus(200)));
            wireMockServer.stubFor(post(urlPathEqualTo(urlPath))
                    .withQueryParam("code", matching(".*"))
                    .withQueryParam("session_state", matching(".*"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "authz-code-transformer")
                            .withTransformerParameter("code", app.getAuthorizationCode())
                            .withTransformerParameter("error", app.getErrorCode())
                            .withStatus(200)));
            wireMockServer.stubFor(get(urlPathEqualTo(urlPath))
                    .withQueryParam("error_description", matching(".*"))
                    .withQueryParam("error", matching(".*"))
                    .willReturn(aResponse()
                            .withTransformers("response-template", "error-code-transformer")
                            .withTransformerParameter("code", app.getAuthorizationCode())
                            .withTransformerParameter("error", app.getErrorCode())
                            .withStatus(200)));
            wireMockServer.stubFor(get(urlEqualTo(urlPath))
                    .willReturn(aResponse()
                            .withTransformers("response-template")
                            .withStatus(200)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void verifyLogoutRedirectionForApp(String appName) {

        wireMockServer.verify(getRequestedFor(urlEqualTo(getCallbackUrlPath(appName))));
    }

    public String getAuthorizationCodeForApp(String appName) {

        return apps.get(appName).getAuthorizationCode().get();
    }

    public String getErrorCode(String appName) {

        return apps.get(appName).getErrorCode().get();
    }

    private String getCallbackUrlPath(String appName) {
        switch (appName) {
            case Constants.APP1.NAME:
                return Constants.APP1.CALLBACK_URL_PATH;
            case Constants.APP2.NAME:
                return Constants.APP2.CALLBACK_URL_PATH;
            default:
                throw new IllegalArgumentException("Unknown app name: " + appName);
        }
    }
}
