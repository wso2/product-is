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

package org.wso2.identity.integration.test.serviceextensions.mockservices;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

public class MockCustomAuthenticatorService {

    public static final String API_PIN_ENTRY = "/api/pin-entry";
    public static final String SCENARIO_INTERNAL_USER = "internal-user";
    public static final String SCENARIO_FEDERATED_USER = "federated-user";
    public static final String SCENARIO_SESSION_INITIALIZED = "SESSION_INITIALIZED";
    public static final String SCENARIO_SESSION_CHALLENGED = "SESSION_CHALLENGED";
    public static final String SCENARIO_SESSION_VALIDATED = "SESSION_VALIDATED";
    public static final String SCENARIO_SESSION_COMPLETED = "SESSION_COMPLETED";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String FEDERATED_USER_EXTERNAL_ID = "fed-user-external-id-001";

    private WireMockServer wireMockServer;
    private static final int port = 3999;
    private static final String host = "localhost";
    public static final String API_AUTHENTICATE_ENDPOINT = "/api/authenticate";
    public static final String API_VALIDATE_PIN_ENDPOINT = "/api/validate-pin";

    private String identityProviderAuthURL;
    private UserDTO user;
    private String scenarioName;
    private String expectedInboundHeaderName;
    private String expectedInboundHeaderValue;

    private static final Logger LOG = LoggerFactory.getLogger(MockCustomAuthenticatorService.class);

    /**
     * Start the mock service in the internal-user scenario with no inbound header assertions.
     */
    public void start(String identityProviderAuthURL, UserDTO internalUser) {

        startInternal(identityProviderAuthURL, internalUser, SCENARIO_INTERNAL_USER, null, null);
    }

    /**
     * Start the mock service in the internal-user scenario; assert each inbound call carries the
     * given {@code Authorization} header value.
     */
    public void start(String identityProviderAuthURL, UserDTO internalUser, String expectedAuthorizationHeader) {

        startInternal(identityProviderAuthURL, internalUser, SCENARIO_INTERNAL_USER,
                AUTHORIZATION_HEADER, expectedAuthorizationHeader);
    }

    /**
     * Start the mock service in the internal-user scenario; assert each inbound call carries the
     * given custom header (used for the API_KEY endpoint auth mode).
     */
    public void start(String identityProviderAuthURL, UserDTO internalUser, String expectedHeaderName,
                      String expectedHeaderValue) {

        startInternal(identityProviderAuthURL, internalUser, SCENARIO_INTERNAL_USER,
                expectedHeaderName, expectedHeaderValue);
    }

    /**
     * Start the mock service in the federated-user scenario. The {@code SUCCESS} response carries a
     * federated user shape so the framework's JIT provisioner can persist the user.
     */
    public void startForFederated(String identityProviderAuthURL, UserDTO federatedUser) {

        startInternal(identityProviderAuthURL, federatedUser, SCENARIO_FEDERATED_USER, null, null);
    }

    public void startForFederated(String identityProviderAuthURL, UserDTO federatedUser,
                                  String expectedAuthorizationHeader) {

        startInternal(identityProviderAuthURL, federatedUser, SCENARIO_FEDERATED_USER,
                AUTHORIZATION_HEADER, expectedAuthorizationHeader);
    }

    public void startForFederated(String identityProviderAuthURL, UserDTO federatedUser,
                                  String expectedHeaderName, String expectedHeaderValue) {

        startInternal(identityProviderAuthURL, federatedUser, SCENARIO_FEDERATED_USER,
                expectedHeaderName, expectedHeaderValue);
    }

    private void startInternal(String identityProviderAuthURL, UserDTO user, String scenarioName,
                               String expectedInboundHeaderName, String expectedInboundHeaderValue) {

        this.identityProviderAuthURL = identityProviderAuthURL;
        this.user = user;
        this.scenarioName = scenarioName;
        this.expectedInboundHeaderName = expectedInboundHeaderName;
        this.expectedInboundHeaderValue = expectedInboundHeaderValue;
        this.startWireMock();
    }

    private void startWireMock() {

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        WireMock.configureFor(host, port);

        wireMockServer.addMockServiceRequestListener((request, response) -> {
            LOG.info("Received Request: {}", request);
            LOG.info("Response Sent: {}", response.getBodyAsString());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));

        // /api/authenticate — initial call (returns a redirect to /api/pin-entry).
        wireMockServer.stubFor(applyExpectedInboundHeader(
                post(urlEqualTo(API_AUTHENTICATE_ENDPOINT)).inScenario(scenarioName)
                        .whenScenarioStateIs(STARTED)
                        .withRequestBody(matchingJsonPath("$.actionType", equalTo("AUTHENTICATION")))
                        .withRequestBody(matchingJsonPath("$.flowId")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "  \"actionStatus\": \"INCOMPLETE\"," +
                                "  \"operations\": [" +
                                "    {" +
                                "      \"op\": \"redirect\"," +
                                "      \"url\": \"http://" + host + ":" + port + API_PIN_ENTRY +
                                "?flowId={{jsonPath request.body '$.flowId'}}\"" +
                                "    }" +
                                "  ]" +
                                "}")
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_INITIALIZED));

        // /api/authenticate — second call after pin validation (returns SUCCESS with user claims).
        wireMockServer.stubFor(applyExpectedInboundHeader(
                post(urlEqualTo(API_AUTHENTICATE_ENDPOINT)).inScenario(scenarioName)
                        .whenScenarioStateIs(SCENARIO_SESSION_VALIDATED)
                        .withRequestBody(matchingJsonPath("$.actionType", equalTo("AUTHENTICATION")))
                        .withRequestBody(matchingJsonPath("$.flowId")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(buildSuccessResponseBody())
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_COMPLETED));

        // GET /api/pin-entry — serves the pin entry HTML page.
        // Note: spId is not asserted because IS only adds it for local custom authenticators; the
        // federated flow forwards the redirect URL verbatim (flowId only).
        wireMockServer.stubFor(get(urlPathEqualTo(API_PIN_ENTRY)).inScenario(scenarioName)
                .whenScenarioStateIs(SCENARIO_SESSION_INITIALIZED)
                .withQueryParam("flowId", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody(
                                "<html>" +
                                        "<body>" +
                                        "<h2>Enter Your PIN</h2>" +
                                        "<form action=\"" + API_VALIDATE_PIN_ENDPOINT + "\" method=\"POST\">" +
                                        "    <input type=\"hidden\" name=\"flowId\" value=\"{{request.query.flowId}}\" />" +
                                        "    <input type=\"text\" name=\"username\" required placeholder=\"Username\" />" +
                                        "    <input type=\"password\" name=\"pin\" required placeholder=\"PIN\"/>" +
                                        "    <button type=\"submit\">Submit</button>" +
                                        "</form>" +
                                        "</body>" +
                                        "</html>"
                        )
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_CHALLENGED));

        // POST /api/validate-pin — validates the user-supplied PIN and signals success back to IS.
        // This endpoint is the authenticator's own internal endpoint, not protected by the configured
        // endpoint authentication, so we don't apply the inbound-header matcher here.
        wireMockServer.stubFor(post(urlEqualTo(API_VALIDATE_PIN_ENDPOINT)).inScenario(scenarioName)
                .whenScenarioStateIs(SCENARIO_SESSION_CHALLENGED)
                .withRequestBody(matchingJsonPath("$.username"))
                .withRequestBody(matchingJsonPath("$.pin", equalTo("1234")))
                .withRequestBody(matchingJsonPath("$.flowId"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"redirectingTo\": \"" + identityProviderAuthURL +
                                "?flowId={{jsonPath request.body '$.flowId'}}\"}")
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_VALIDATED));
    }

    /**
     * Apply the configured inbound-header matcher to a stub builder if one was set, otherwise pass
     * through unchanged. Mirrors how the Actions framework tests guard their action endpoint stubs.
     */
    private ScenarioMappingBuilder applyExpectedInboundHeader(ScenarioMappingBuilder mappingBuilder) {

        if (expectedInboundHeaderName != null && expectedInboundHeaderValue != null) {
            return (ScenarioMappingBuilder) mappingBuilder.withHeader(expectedInboundHeaderName,
                    equalTo(expectedInboundHeaderValue));
        }
        return mappingBuilder;
    }

    /**
     * Build a SUCCESS response body. The shape is the same for the internal-user and federated-user
     * scenarios; the framework treats the response identically at the action layer — the
     * authenticator's own implementation decides whether to look up an internal user or JIT-provision
     * a federated user based on the returned claims.
     */
    private String buildSuccessResponseBody() {

        String userId = SCENARIO_FEDERATED_USER.equals(scenarioName)
                ? FEDERATED_USER_EXTERNAL_ID
                : this.user.getUserID();
        StringBuilder claims = new StringBuilder();
        for (int i = 0; i < user.getAttributes().length; i++) {
            if (i > 0) {
                claims.append(",");
            }
            claims.append("{ \"uri\": \"")
                    .append(user.getAttributes()[i].getAttributeName())
                    .append("\", \"value\": \"")
                    .append(user.getAttributes()[i].getAttributeValue())
                    .append("\" }");
        }
        return "{" +
                "  \"actionStatus\": \"SUCCESS\"," +
                "  \"data\": {" +
                "    \"user\": {" +
                "      \"id\": \"" + userId + "\"," +
                "      \"claims\": [" + claims + "]" +
                "    }" +
                "  }" +
                "}";
    }

    public void stop() {

        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public String getCustomAuthenticatorURL() {

        return "http://" + host + ":" + port;
    }
}
