package org.wso2.identity.integration.test.rest.api.server.authenticator.mockserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.wso2.carbon.identity.test.integration.service.dao.UserDTO;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

public class MockCustomAuthenticatorService {

    public static final String API_PIN_ENTRY = "/api/pin-entry";
    public static final String SCENARIO_INTERNAL_USER = "internal-user";
    public static final String SCENARIO_SESSION_INITIALIZED = "SESSION_INITIALIZED";
    public static final String SCENARIO_SESSION_CHALLENGED = "SESSION_CHALLENGED";
    public static final String SCENARIO_SESSION_VALIDATED = "SESSION_VALIDATED";
    public static final String SCENARIO_SESSION_COMPLETED = "SESSION_COMPLETED";
    private WireMockServer wireMockServer;
    private static final int port = 3999;
    private static final String host = "localhost";
    public static final String API_AUTHENTICATE_ENDPOINT = "/api/authenticate";
    public static final String API_VALIDATE_PIN_ENDPOINT = "/api/validate-pin";

    private String identityProviderBaseUrl;
    private UserDTO internalUser;

    public void start(String identityProviderBaseUrl, UserDTO internalUser) {

        this.identityProviderBaseUrl = identityProviderBaseUrl;
        this.internalUser = internalUser;
        this.start();
    }

    private void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        WireMock.configureFor(host, port);

        // Log all received requests
        wireMockServer.addMockServiceRequestListener((request, response) -> {
            System.out.println("Received Request: " + request);
            System.out.println("Response Sent: " + response.getBodyAsString());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(wireMockServer::stop));

        // Mock /api/authenticate (Initial Request)
        wireMockServer.stubFor(post(urlEqualTo(API_AUTHENTICATE_ENDPOINT)).inScenario(SCENARIO_INTERNAL_USER)
                .whenScenarioStateIs(STARTED)
                .withRequestBody(matchingJsonPath("$.actionType", equalTo("AUTHENTICATION")))
                .withRequestBody(matchingJsonPath("$.flowId"))
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

        // Mock /api/authenticate (Initial Request)
        wireMockServer.stubFor(post(urlEqualTo(API_AUTHENTICATE_ENDPOINT)).inScenario(SCENARIO_INTERNAL_USER)
                .whenScenarioStateIs(SCENARIO_SESSION_VALIDATED)
                .withRequestBody(matchingJsonPath("$.actionType", equalTo("AUTHENTICATION")))
                .withRequestBody(matchingJsonPath("$.flowId"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "  \"actionStatus\": \"SUCCESS\"," +
                                "  \"data\": {" +
                                "    \"user\": {" +
                                "      \"id\": \"" + this.internalUser.getUserID() + "\"," +
                                "      \"claims\": [" +
                                "        {" +
                                "          \"uri\": \"" + this.internalUser.getAttributes()[0].getAttributeName() +
                                "\"," +
                                "          \"value\": \"" + this.internalUser.getAttributes()[0].getAttributeValue() +
                                "\"" +
                                "        }," +
                                "        {" +
                                "          \"uri\": \"" + this.internalUser.getAttributes()[1].getAttributeName() +
                                "\"," +
                                "          \"value\": \"" + this.internalUser.getAttributes()[1].getAttributeValue() +
                                "\"" +
                                "        }," +
                                "        { \"uri\": \"" + this.internalUser.getAttributes()[2].getAttributeName() +
                                "\", \"value\": \"" + this.internalUser.getAttributes()[2].getAttributeValue() +
                                "\" }," +
                                "        { \"uri\": \"" + this.internalUser.getAttributes()[3].getAttributeName() +
                                "\", \"value\": \"" + this.internalUser.getAttributes()[3].getAttributeValue() +
                                "\" }" +
                                "      ]" +
                                "    }" +
                                "  } }")
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_COMPLETED));

        // Stub for serving an HTML page when a GET request is made to /api/pin-entry
        wireMockServer.stubFor(get(urlPathEqualTo(API_PIN_ENTRY)).inScenario(SCENARIO_INTERNAL_USER)
                .whenScenarioStateIs(SCENARIO_SESSION_INITIALIZED)
                .withQueryParam("flowId", matching(".+"))
                .withQueryParam("spId", matching(".+"))
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

        wireMockServer.stubFor(post(urlEqualTo(API_VALIDATE_PIN_ENDPOINT)).inScenario(SCENARIO_INTERNAL_USER)
                .whenScenarioStateIs(SCENARIO_SESSION_CHALLENGED)
                .withRequestBody(matchingJsonPath("$.username", equalTo("emily@aol.com")))
                .withRequestBody(matchingJsonPath("$.pin", equalTo("1234")))
                .withRequestBody(matchingJsonPath("$.flowId"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"redirectingTo\": \"" + identityProviderBaseUrl +
                                "t/carbon.super/commonauth?flowId={{jsonPath request.body '$.flowId'}}\"}")
                        .withTransformers("response-template"))
                .willSetStateTo(SCENARIO_SESSION_VALIDATED));
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
