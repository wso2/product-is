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

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for negative paths of managing OAuth2/OIDC client secrets using the Application Management REST API.
 */
public class ApplicationManagementClientSecretFailureTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRETS_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH + "/secrets";

    private static final String INVALID_REQUEST_ERROR_CODE = "OAUTH-60001";
    private static final String INVALID_DELETE_ERROR_CODE = "OAUTH-60006";
    private static final String INVALID_SECRET_ID_ERROR_CODE = "OAUTH-60010";
    private static final String CLIENT_SECRET_LIMIT_REACHED_ERROR_CODE = "OAUTH-60011";
    private static final String APPLICATION_INVALID_REQUEST_ERROR_CODE = "APP-60001";

    private static final String PAST_EXPIRY_DESCRIPTION = "is in the past";
    private static final String INVALID_EXPIRY_DESCRIPTION = "is invalid";
    private static final String UNKNOWN_SECRET_DESCRIPTION = "does not exist for this application";
    private static final String LATEST_SECRET_DELETE_DESCRIPTION = "cannot be deleted as it is the latest secret";
    private static final String SECRET_LIMIT_DESCRIPTION = "Maximum number of client secrets reached";
    private static final String IMMUTABLE_EXPIRY_DESCRIPTION =
            "The client secret expiry time cannot be modified with the application update";

    private static final long EXPIRY_OFFSET_IN_SECONDS = 3600L;
    private static final long NEGATIVE_EXPIRY = -1L;

    private final Set<String> createdApps = new HashSet<>();

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementClientSecretFailureTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @AfterMethod(alwaysRun = true)
    @Override
    public void testFinish() {

        cleanUpApplications(createdApps);
        createdApps.clear();
        super.testFinish();
    }

    @Test
    public void testCreateClientSecretWithPastExpiry() throws Exception {

        String appId = createOAuthApplication("App With Past Client Secret Expiry", null);
        long pastExpiry = Instant.now().getEpochSecond() - EXPIRY_OFFSET_IN_SECONDS;

        Response response = getResponseOfPost(getClientSecretsPath(appId), buildCreateSecretPayload(pastExpiry));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, INVALID_REQUEST_ERROR_CODE);
        validateResponseElement(response, "description", containsString(PAST_EXPIRY_DESCRIPTION));
    }

    @Test
    public void testCreateClientSecretWithNegativeExpiry() throws Exception {

        String appId = createOAuthApplication("App With Negative Client Secret Expiry", null);

        Response response = getResponseOfPost(getClientSecretsPath(appId), buildCreateSecretPayload(NEGATIVE_EXPIRY));
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, INVALID_REQUEST_ERROR_CODE);
        validateResponseElement(response, "description", containsString(INVALID_EXPIRY_DESCRIPTION));
    }

    @Test
    public void testGetClientSecretWithUnknownSecretId() throws Exception {

        String appId = createOAuthApplication("App For Unknown Client Secret Retrieval", null);

        Response response = getResponseOfGet(getClientSecretPath(appId, UUID.randomUUID().toString()));
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, INVALID_SECRET_ID_ERROR_CODE);
        validateResponseElement(response, "description", containsString(UNKNOWN_SECRET_DESCRIPTION));
    }

    @Test
    public void testDeleteClientSecretWithUnknownSecretId() throws Exception {

        String appId = createOAuthApplication("App For Unknown Client Secret Deletion", null);

        Response response = getResponseOfDelete(getClientSecretPath(appId, UUID.randomUUID().toString()));
        validateErrorResponse(response, HttpStatus.SC_NOT_FOUND, INVALID_SECRET_ID_ERROR_CODE);
        validateResponseElement(response, "description", containsString(UNKNOWN_SECRET_DESCRIPTION));
    }

    @Test
    public void testAccessClientSecretOfAnotherApplication() throws Exception {

        String firstAppId = createOAuthApplication("First App With Client Secrets", null);
        String secondAppId = createOAuthApplication("Second App With Client Secrets", null);
        String secondAppSecretId = getLatestClientSecretId(secondAppId);

        Response responseOfGet = getResponseOfGet(getClientSecretPath(firstAppId, secondAppSecretId));
        validateErrorResponse(responseOfGet, HttpStatus.SC_NOT_FOUND, INVALID_SECRET_ID_ERROR_CODE);
        validateResponseElement(responseOfGet, "description", containsString(UNKNOWN_SECRET_DESCRIPTION));

        Response responseOfDelete = getResponseOfDelete(getClientSecretPath(firstAppId, secondAppSecretId));
        validateErrorResponse(responseOfDelete, HttpStatus.SC_NOT_FOUND, INVALID_SECRET_ID_ERROR_CODE);
        validateResponseElement(responseOfDelete, "description", containsString(UNKNOWN_SECRET_DESCRIPTION));
    }

    @Test
    public void testDeleteLatestClientSecret() throws Exception {

        String appId = createOAuthApplication("App For Latest Client Secret Deletion", null);
        String latestSecretId = createClientSecret(appId);

        Response response = getResponseOfDelete(getClientSecretPath(appId, latestSecretId));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, INVALID_DELETE_ERROR_CODE);
        validateResponseElement(response, "description", containsString(LATEST_SECRET_DELETE_DESCRIPTION));
    }

    @Test
    public void testDeleteOnlyClientSecret() throws Exception {

        String appId = createOAuthApplication("App For Only Client Secret Deletion", null);
        String onlySecretId = getLatestClientSecretId(appId);

        Response response = getResponseOfDelete(getClientSecretPath(appId, onlySecretId));
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, INVALID_DELETE_ERROR_CODE);
        validateResponseElement(response, "description", containsString(LATEST_SECRET_DELETE_DESCRIPTION));
    }

    @Test
    public void testCreateClientSecretWhenLimitReached() throws Exception {

        /* The packaged maximum is two client secrets per application and the secret the application is created
           with counts towards it, so a single additional secret already reaches the limit. */
        String appId = createOAuthApplication("App For Client Secret Limit", null);
        createClientSecret(appId);

        Response response = getResponseOfPost(getClientSecretsPath(appId), new JSONObject().toString());
        validateErrorResponse(response, HttpStatus.SC_CONFLICT, CLIENT_SECRET_LIMIT_REACHED_ERROR_CODE);
        validateResponseElement(response, "description", containsString(SECRET_LIMIT_DESCRIPTION));
    }

    @Test
    public void testCreateApplicationWithPastClientSecretExpiry() throws Exception {

        long pastExpiry = Instant.now().getEpochSecond() - EXPIRY_OFFSET_IN_SECONDS;
        String payload = buildOAuthAppPayload("App Created With Past Client Secret Expiry", pastExpiry);

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, INVALID_REQUEST_ERROR_CODE);
        validateResponseElement(response, "description", containsString(PAST_EXPIRY_DESCRIPTION));
    }

    @Test
    public void testCreateApplicationWithNegativeClientSecretExpiry() throws Exception {

        String appName = "App Created With Negative Client Secret Expiry";
        String payload = buildOAuthAppPayload(appName, NEGATIVE_EXPIRY);

        Response response = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, INVALID_REQUEST_ERROR_CODE);
        validateResponseElement(response, "description", containsString(INVALID_EXPIRY_DESCRIPTION));

        // A rejected creation must not leave a consumer application behind, so the same name is still available.
        createOAuthApplication(appName, null);
    }

    @Test
    public void testUpdateOAuthInboundWithChangedClientSecretExpiry() throws Exception {

        long expiry = Instant.now().getEpochSecond() + EXPIRY_OFFSET_IN_SECONDS;
        String appId = createOAuthApplication("App For Client Secret Expiry Update", expiry);

        JSONObject updateRequest = buildOIDCUpdatePayload(getResponseOfGet(getOIDCConfigPath(appId)));
        updateRequest.put("clientSecretExpiresAt", expiry + EXPIRY_OFFSET_IN_SECONDS);

        Response response = getResponseOfPut(getOIDCConfigPath(appId), updateRequest.toString());
        validateErrorResponse(response, HttpStatus.SC_BAD_REQUEST, APPLICATION_INVALID_REQUEST_ERROR_CODE);
        validateResponseElement(response, "description", containsString(IMMUTABLE_EXPIRY_DESCRIPTION));
    }

    @Test
    public void testUpdateOAuthInboundWithUnchangedClientSecretExpiry() throws Exception {

        long expiry = Instant.now().getEpochSecond() + EXPIRY_OFFSET_IN_SECONDS;
        String appId = createOAuthApplication("App For Unchanged Client Secret Expiry Update", expiry);

        JSONObject updateRequest = buildOIDCUpdatePayload(getResponseOfGet(getOIDCConfigPath(appId)));
        updateRequest.put("clientSecretExpiresAt", expiry);
        getResponseOfPut(getOIDCConfigPath(appId), updateRequest.toString())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // An update omitting the field keeps the expiry of the latest client secret.
        JSONObject updateRequestWithoutExpiry = buildOIDCUpdatePayload(getResponseOfGet(getOIDCConfigPath(appId)));
        getResponseOfPut(getOIDCConfigPath(appId), updateRequestWithoutExpiry.toString())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Response responseOfGet = getResponseOfGet(getOIDCConfigPath(appId));
        responseOfGet.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
        Assert.assertEquals(responseOfGet.jsonPath().getLong("clientSecretExpiresAt"), expiry,
                "The client secret expiry was not preserved by an application update omitting the field.");
    }

    /**
     * Create an OAuth application with the given client secret expiry and register it for clean up.
     *
     * @param appName               Name of the application.
     * @param clientSecretExpiresAt Client secret expiry as Unix epoch seconds; null to omit the field.
     * @return Application ID of the created application.
     * @throws Exception If an error occurs while creating the application.
     */
    private String createOAuthApplication(String appName, Long clientSecretExpiresAt) throws Exception {

        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH,
                buildOAuthAppPayload(appName, clientSecretExpiresAt));
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String appId = extractApplicationIdFromLocationHeader(responseOfPost.getHeader(HttpHeaders.LOCATION));
        assertNotBlank(appId);
        createdApps.add(appId);
        return appId;
    }

    /**
     * Create a non expiring client secret for the given application.
     *
     * @param appId Application ID.
     * @return Secret ID of the created client secret.
     */
    private String createClientSecret(String appId) {

        Response responseOfPost = getResponseOfPost(getClientSecretsPath(appId), new JSONObject().toString());
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("secretId", notNullValue());

        return responseOfPost.jsonPath().getString("secretId");
    }

    /**
     * Retrieve the secret ID of the latest client secret of the given application.
     *
     * @param appId Application ID.
     * @return Secret ID of the latest client secret.
     */
    private String getLatestClientSecretId(String appId) {

        Response responseOfGet = getResponseOfGet(getClientSecretsPath(appId));
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("list[0].secretId", notNullValue());

        return responseOfGet.jsonPath().getString("list[0].secretId");
    }

    private String buildCreateSecretPayload(long expiresAt) throws Exception {

        JSONObject createRequest = new JSONObject();
        createRequest.put("expiresAt", expiresAt);
        return createRequest.toString();
    }

    private String getOIDCConfigPath(String appId) {

        return APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + appId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
    }

    private String getClientSecretsPath(String appId) {

        return APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + appId + CLIENT_SECRETS_CONTEXT_PATH;
    }

    private String getClientSecretPath(String appId, String secretId) {

        return getClientSecretsPath(appId) + PATH_SEPARATOR + secretId;
    }
}
