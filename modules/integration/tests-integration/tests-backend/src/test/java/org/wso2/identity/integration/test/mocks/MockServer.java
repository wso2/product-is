package org.wso2.identity.integration.test.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Provides a mock server using WireMock for testing purposes.
 * This class starts a mock server on a specified port and sets up predefined
 * responses for POST requests to simulate various operations relation to action execution.
 */
public class MockServer {

    private static WireMockServer wireMockServer;

    /**
     * Create a mock server with wiremock.
     *
     * @throws Exception If an error occurred while creating the server
     */
    public static void createMockServer(String mockEndpoint) throws Exception {

        wireMockServer = new WireMockServer(wireMockConfig().port(8587));

        wireMockServer.start();

        try {
            // TODO: Read the response from a file
            // TODO: Filter the response from the action type
            String jsonResponse = "{\n" +
                    "    \"actionStatus\": \"SUCCESS\",\n" +
                    "    \"operations\": [\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/scopes/-\",\n" +
                    "            \"value\": \"new_test_custom_scope_1\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/scopes/-\",\n" +
                    "            \"value\": \"new_test_custom_scope_2\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/scopes/-\",\n" +
                    "            \"value\": \"new_test_custom_scope_3\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/aud/-\",\n" +
                    "            \"value\": \"zzz1.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/aud/-\",\n" +
                    "            \"value\": \"zzz2.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/aud/-\",\n" +
                    "            \"value\": \"zzz3.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/-\",\n" +
                    "            \"value\": {\n" +
                    "                \"name\": \"custom_claim_string_1\",\n" +
                    "                \"value\": \"testCustomClaim1\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/-\",\n" +
                    "            \"value\": {\n" +
                    "                \"name\": \"custom_claim_number_1\",\n" +
                    "                \"value\": 78\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/-\",\n" +
                    "            \"value\": {\n" +
                    "                \"name\": \"custom_claim_boolean_1\",\n" +
                    "                \"value\": true\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"add\",\n" +
                    "            \"path\": \"/accessToken/claims/-\",\n" +
                    "            \"value\": {\n" +
                    "                \"name\": \"custom_claim_string_array_1\",\n" +
                    "                \"value\": [\n" +
                    "                    \"TestCustomClaim1\",\n" +
                    "                    \"TestCustomClaim2\",\n" +
                    "                    \"TestCustomClaim3\"\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"replace\",\n" +
                    "            \"path\": \"/accessToken/scopes/7\",\n" +
                    "            \"value\": \"replaced_scope\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"replace\",\n" +
                    "            \"path\": \"/accessToken/claims/aud/-\",\n" +
                    "            \"value\": \"zzzR.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"remove\",\n" +
                    "            \"path\": \"/accessToken/scopes/6\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"op\": \"remove\",\n" +
                    "            \"path\": \"/accessToken/claims/aud/-\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}\n";

            // TODO: Handle status codes related to different scenarios
            wireMockServer.stubFor(post(urlEqualTo(mockEndpoint))
                    .withRequestBody(matchingJsonPath("$.event.request.grantType", equalTo("password")))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jsonResponse)));

        } catch (Exception e) {
            throw new Exception("Error occurred while creating the mock server: " + e);
        }
    }

    /**
     * Shut down the wiremock server instance.
     */
    public static void shutDownMockServer() {

        wireMockServer.stop();
    }
}
