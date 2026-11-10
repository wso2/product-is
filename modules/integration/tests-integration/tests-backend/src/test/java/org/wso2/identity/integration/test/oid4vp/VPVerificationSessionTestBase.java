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

package org.wso2.identity.integration.test.oid4vp;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;

import java.io.IOException;
import java.util.UUID;

/**
 * Base class for VP verification session integration tests.
 */
public abstract class VPVerificationSessionTestBase extends VPTestBase {

    protected static final String DEFINITION_IDENTIFIER = "test-vp-session-definition";

    protected static final String STATUS_ACTIVE   = "ACTIVE";
    protected static final String STATUS_VERIFIED = "VERIFIED";
    protected static final String STATUS_FAILED   = "FAILED";

    /**
     * Create a presentation definition via the admin REST API.
     *
     * @return The {@code id} of the newly created definition.
     * @throws Exception If the HTTP call fails or the server does not return 201.
     */
    protected String createPresentationDefinition() throws Exception {

        String json = "{\"identifier\":\"test-vp-session-definition\","
                + "\"displayName\":\"Test VP Session Definition\","
                + "\"description\":\"Presentation definition for VP session integration tests\","
                + "\"credentials\":[{\"id\":\"PersonCredential\",\"type\":\"PersonCredential\","
                + "\"format\":\"dc+sd-jwt\"}]}";
        return doCreatePresentationDefinition(json);
    }

    protected String createPresentationDefinitionFromJson(String json) throws Exception {

        return doCreatePresentationDefinition(json);
    }

    private String doCreatePresentationDefinition(String json) throws Exception {

        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH;
        Response response = adminPost(url, json);
        String body = response.getBody().asString();
        int status = response.getStatusCode();

        if (status == HttpStatus.SC_CONFLICT) {
            String identifier = new JSONObject(json).optString("identifier");
            if (!identifier.isEmpty()) {
                Response listResp = adminGet(serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "?limit=100");
                Assert.assertEquals(listResp.getStatusCode(), HttpStatus.SC_OK,
                        "Failed to list definitions during 409-recovery: " + listResp.getBody().asString());
                org.json.JSONArray defs = new JSONObject(listResp.getBody().asString())
                        .optJSONArray("presentationDefinitions");
                if (defs != null) {
                    for (int i = 0; i < defs.length(); i++) {
                        JSONObject def = defs.getJSONObject(i);
                        if (identifier.equals(def.optString("identifier"))) {
                            adminDelete(serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + def.getString("id"));
                            break;
                        }
                    }
                }
                response = adminPost(url, json);
                body = response.getBody().asString();
                status = response.getStatusCode();
            }
        }

        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create presentation definition (status " + status + "): " + body);
        JSONObject jsonResponse = new JSONObject(body);
        Object idObj = jsonResponse.opt("id");
        Assert.assertNotNull(idObj, "Presentation definition response does not contain 'id'.");
        return idObj.toString();
    }

    /**
     * Delete a presentation definition by its server-assigned ID.
     *
     * @param definitionId The ID returned at creation time.
     * @throws IOException If the HTTP call fails.
     */
    protected void deletePresentationDefinition(String definitionId) throws IOException {

        if (definitionId == null) {
            return;
        }
        String url = serverBaseUrl() + PRESENTATION_DEFINITIONS_PATH + "/" + definitionId;
        Response response = adminDelete(url);
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status when deleting presentation definition: " + status);
    }

    /**
     * Create a "digital wallet connection" IDP that has the PresentationAuthenticator as its
     * federated authenticator.  Creating this IDP fires {@code VPIdPMgtListener.doPostAddIdP},
     * which provisions the ECDSA P-256 keypair needed to sign VP request JWTs.
     *
     * @return The server-assigned IDP {@code id}.
     */
    protected String createDigitalWalletConnection() throws IOException {

        String idpJson = "{"
                + "\"name\": \"VP-Session-Test-IDP-" + UUID.randomUUID() + "\","
                + "\"description\": \"VP session test IDP for ECDSA key provisioning\","
                + "\"federatedAuthenticators\": {"
                + "  \"defaultAuthenticatorId\": \"" + VPTestUtils.PRESENTATION_AUTHENTICATOR_ID + "\","
                + "  \"authenticators\": [{"
                + "    \"authenticatorId\": \"" + VPTestUtils.PRESENTATION_AUTHENTICATOR_ID + "\","
                + "    \"isEnabled\": true,"
                + "    \"properties\": [{"
                + "      \"key\": \"presentationDefinitionId\","
                + "      \"value\": \"" + DEFINITION_IDENTIFIER + "\""
                + "    }]"
                + "  }]"
                + "}"
                + "}";
        String url = serverBaseUrl() + "/api/server/v1/identity-providers";
        Response response = adminPost(url, idpJson);
        String body = response.getBody().asString();
        int status = response.getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_CREATED,
                "Failed to create digital wallet connection IDP (status " + status + "): " + body);
        try {
            JSONObject json = new JSONObject(body);
            Object idObj = json.opt("id");
            Assert.assertNotNull(idObj, "IDP creation response does not contain 'id'.");
            return idObj.toString();
        } catch (Exception e) {
            throw new IOException("Failed to parse IDP creation response: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a digital wallet connection IDP by its server-assigned ID.
     *
     * @param idpId The ID returned at creation time.
     */
    protected void deleteDigitalWalletConnection(String idpId) throws IOException {

        if (idpId == null) {
            return;
        }
        String url = serverBaseUrl() + "/api/server/v1/identity-providers/" + idpId;
        Response response = adminDelete(url);
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_NOT_FOUND,
                "Unexpected status when deleting digital wallet connection IDP: " + status);
    }
}
