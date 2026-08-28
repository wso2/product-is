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

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for happy paths of managing OAuth2/OIDC client secrets using the Application Management REST API.
 */
public class ApplicationManagementClientSecretSuccessTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRETS_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH + "/secrets";
    private static final String REGENERATE_SECRET_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH
            + "/regenerate-secret";

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final long NEVER_EXPIRES = 0L;
    private static final long INITIAL_SECRET_EXPIRY_OFFSET_IN_SECONDS = 3600L;
    private static final long CREATED_SECRET_EXPIRY_OFFSET_IN_SECONDS = 7200L;

    /* The three accepted spellings of a create request for a non-expiring client secret. */
    private static final String[] NON_EXPIRING_CREATE_PAYLOADS =
            {"{}", StringUtils.EMPTY, "{\"expiresAt\": 0}"};

    private String createdAppId;
    private long initialSecretExpiresAt;
    private long createdSecretExpiresAt;
    private String initialSecretId;
    private String initialSecretValue;
    private String latestSecretId;
    private String latestSecretValue;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementClientSecretSuccessTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() {

        /* An application is only present here when a dependent test failed before the test that deletes it ran. */
        if (StringUtils.isNotBlank(createdAppId)) {
            RestAssured.basePath = basePath;
            cleanUpApplications(Collections.singleton(createdAppId));
            createdAppId = null;
        }
    }

    @Test
    public void testCreateOAuthAppWithClientSecretExpiry() throws Exception {

        initialSecretExpiresAt = Instant.now().getEpochSecond() + INITIAL_SECRET_EXPIRY_OFFSET_IN_SECONDS;
        String body = buildOAuthAppPayload("OAuth Application With Client Secret Expiry", initialSecretExpiresAt);

        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        createdAppId = extractApplicationIdFromLocationHeader(responseOfPost.getHeader(HttpHeaders.LOCATION));
        assertNotBlank(createdAppId);
    }

    @Test(dependsOnMethods = "testCreateOAuthAppWithClientSecretExpiry")
    public void testGetOAuthInboundDetailsWithClientSecretExpiry() {

        Response responseOfGet = getResponseOfGet(getOIDCConfigPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientId", notNullValue())
                .body("clientSecret", notNullValue())
                .body("multipleClientSecretsConfigured", equalTo(false));

        Assert.assertEquals(responseOfGet.jsonPath().getLong("clientSecretExpiresAt"), initialSecretExpiresAt,
                "The client secret expiry provided at application creation is not echoed by the OIDC config.");
    }

    @Test(dependsOnMethods = "testGetOAuthInboundDetailsWithClientSecretExpiry")
    public void testGetClientSecretsOfNewApplication() {

        String appClientSecret = getResponseOfGet(getOIDCConfigPath()).jsonPath().getString("clientSecret");

        Response responseOfGet = getResponseOfGet(getClientSecretsPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].status", equalTo(STATUS_ACTIVE))
                .body("list[0].latest", equalTo(true));

        JsonPath initialSecret = responseOfGet.jsonPath();
        initialSecretId = initialSecret.getString("list[0].secretId");
        initialSecretValue = initialSecret.getString("list[0].secretValue");
        assertNotBlank(initialSecretId);
        Assert.assertEquals(initialSecretValue, appClientSecret,
                "The listed client secret value does not match the client secret of the application.");
        Assert.assertEquals(initialSecret.getLong("list[0].expiresAt"), initialSecretExpiresAt,
                "The expiry of the secret the application was created with is not reported by the list.");
        Assert.assertTrue(initialSecret.getLong("list[0].createdAt") > 0,
                "The creation time of the secret the application was created with is not reported by the list.");
    }

    @Test(dependsOnMethods = "testGetClientSecretsOfNewApplication")
    public void testCreateClientSecretWithFutureExpiry() throws Exception {

        createdSecretExpiresAt = Instant.now().getEpochSecond() + CREATED_SECRET_EXPIRY_OFFSET_IN_SECONDS;
        JSONObject createRequest = new JSONObject();
        createRequest.put("expiresAt", createdSecretExpiresAt);

        Response responseOfPost = getResponseOfPost(getClientSecretsPath(), createRequest.toString());
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("secretId", notNullValue())
                .body("secretValue", notNullValue())
                .body("status", equalTo(STATUS_ACTIVE))
                .body("latest", equalTo(true));

        JsonPath createdSecret = responseOfPost.jsonPath();
        latestSecretId = createdSecret.getString("secretId");
        latestSecretValue = createdSecret.getString("secretValue");
        Assert.assertNotEquals(latestSecretId, initialSecretId, "The created secret reused the existing secret ID.");
        Assert.assertNotEquals(latestSecretValue, initialSecretValue,
                "The created secret reused the existing secret value.");
        Assert.assertEquals(createdSecret.getLong("expiresAt"), createdSecretExpiresAt,
                "The requested expiry is not echoed by the created client secret.");
        Assert.assertTrue(createdSecret.getLong("createdAt") > 0,
                "The created client secret does not carry a creation time.");
    }

    @Test(dependsOnMethods = "testCreateClientSecretWithFutureExpiry")
    public void testGetOAuthInboundDetailsWithMultipleClientSecrets() {

        Response responseOfGet = getResponseOfGet(getOIDCConfigPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientSecret", equalTo(latestSecretValue))
                .body("multipleClientSecretsConfigured", equalTo(true));

        Assert.assertEquals(responseOfGet.jsonPath().getLong("clientSecretExpiresAt"), createdSecretExpiresAt,
                "The OIDC config does not track the expiry of the latest client secret.");
    }

    @Test(dependsOnMethods = "testGetOAuthInboundDetailsWithMultipleClientSecrets")
    public void testListClientSecrets() {

        Response responseOfGet = getResponseOfGet(getClientSecretsPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(2))
                .body("list[0].secretId", equalTo(latestSecretId))
                .body("list[0].secretValue", equalTo(latestSecretValue))
                .body("list[0].status", equalTo(STATUS_ACTIVE))
                .body("list[0].latest", equalTo(true))
                .body("list[1].secretId", equalTo(initialSecretId))
                .body("list[1].secretValue", equalTo(initialSecretValue))
                .body("list[1].status", equalTo(STATUS_ACTIVE))
                .body("list[1].latest", equalTo(false));

        JsonPath clientSecrets = responseOfGet.jsonPath();
        Assert.assertEquals(clientSecrets.getLong("list[0].expiresAt"), createdSecretExpiresAt,
                "The expiry of the latest client secret is not reported by the list.");
        Assert.assertEquals(clientSecrets.getLong("list[1].expiresAt"), initialSecretExpiresAt,
                "The expiry of the non latest client secret is not reported by the list.");
    }

    @Test(dependsOnMethods = "testListClientSecrets")
    public void testGetClientSecretById() {

        getResponseOfGet(getClientSecretPath(latestSecretId))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(latestSecretId))
                .body("secretValue", equalTo(latestSecretValue))
                .body("status", equalTo(STATUS_ACTIVE))
                .body("latest", equalTo(true));

        getResponseOfGet(getClientSecretPath(initialSecretId))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("secretId", equalTo(initialSecretId))
                .body("secretValue", equalTo(initialSecretValue))
                .body("status", equalTo(STATUS_ACTIVE))
                .body("latest", equalTo(false));
    }

    @Test(dependsOnMethods = "testGetClientSecretById")
    public void testDeleteNonLatestClientSecret() {

        getResponseOfDelete(getClientSecretPath(initialSecretId))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(getClientSecretsPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].secretId", equalTo(latestSecretId))
                .body("list[0].latest", equalTo(true));

        getResponseOfGet(getOIDCConfigPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("multipleClientSecretsConfigured", equalTo(false));
    }

    @Test(dependsOnMethods = "testDeleteNonLatestClientSecret")
    public void testCreateNonExpiringClientSecrets() {

        for (String createPayload : NON_EXPIRING_CREATE_PAYLOADS) {
            String previousLatestSecretId = latestSecretId;

            Response responseOfPost = getResponseOfPost(getClientSecretsPath(), createPayload);
            responseOfPost.then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED)
                    .body("status", equalTo(STATUS_ACTIVE))
                    .body("latest", equalTo(true));
            Assert.assertEquals(responseOfPost.jsonPath().getLong("expiresAt"), NEVER_EXPIRES,
                    "A client secret created with the payload '" + createPayload + "' is not a non expiring secret.");

            latestSecretId = responseOfPost.jsonPath().getString("secretId");
            latestSecretValue = responseOfPost.jsonPath().getString("secretValue");

            // The newly created secret becomes the latest, which in turn makes the previous latest deletable.
            getResponseOfGet(getClientSecretsPath())
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("count", equalTo(2))
                    .body("list[0].secretId", equalTo(latestSecretId))
                    .body("list[0].latest", equalTo(true))
                    .body("list[1].secretId", equalTo(previousLatestSecretId))
                    .body("list[1].latest", equalTo(false));

            getResponseOfDelete(getClientSecretPath(previousLatestSecretId))
                    .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }

        getResponseOfGet(getClientSecretsPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].secretId", equalTo(latestSecretId));

        Response responseOfGet = getResponseOfGet(getOIDCConfigPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientSecret", equalTo(latestSecretValue))
                .body("multipleClientSecretsConfigured", equalTo(false));
        Assert.assertEquals(responseOfGet.jsonPath().getLong("clientSecretExpiresAt"), NEVER_EXPIRES,
                "The OIDC config does not track the expiry of the latest non expiring client secret.");
    }

    @Test(dependsOnMethods = "testCreateNonExpiringClientSecrets")
    public void testUpdateOAuthInboundDetailsKeepsClientSecrets() throws Exception {

        JSONObject updateRequest = buildOIDCUpdatePayload(getResponseOfGet(getOIDCConfigPath()));
        JSONObject pkce = new JSONObject();
        pkce.put("mandatory", true);
        pkce.put("supportPlainTransformAlgorithm", false);
        updateRequest.put("pkce", pkce);

        getResponseOfPut(getOIDCConfigPath(), updateRequest.toString())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        getResponseOfGet(getOIDCConfigPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("pkce.mandatory", equalTo(true));

        getResponseOfGet(getClientSecretsPath())
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].secretId", equalTo(latestSecretId))
                .body("list[0].secretValue", equalTo(latestSecretValue));
    }

    @Test(dependsOnMethods = "testUpdateOAuthInboundDetailsKeepsClientSecrets")
    public void testRegenerateClientSecret() {

        String previousSecretId = latestSecretId;
        String previousSecretValue = latestSecretValue;

        Response responseOfPost = getResponseOfPost(
                APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId + REGENERATE_SECRET_CONTEXT_PATH,
                StringUtils.EMPTY);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("clientId", notNullValue())
                .body("clientSecret", notNullValue())
                .body("multipleClientSecretsConfigured", equalTo(false));

        String regeneratedSecretValue = responseOfPost.jsonPath().getString("clientSecret");
        Assert.assertNotEquals(regeneratedSecretValue, previousSecretValue,
                "The regenerated client secret is the same as the previous one.");
        Assert.assertEquals(responseOfPost.jsonPath().getLong("clientSecretExpiresAt"), NEVER_EXPIRES,
                "The regenerated client secret is not a non expiring secret.");

        Response responseOfGet = getResponseOfGet(getClientSecretsPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].secretValue", equalTo(regeneratedSecretValue))
                .body("list[0].status", equalTo(STATUS_ACTIVE))
                .body("list[0].latest", equalTo(true));

        latestSecretId = responseOfGet.jsonPath().getString("list[0].secretId");
        latestSecretValue = regeneratedSecretValue;
        Assert.assertNotEquals(latestSecretId, previousSecretId,
                "The regenerated client secret reused the previous secret ID.");
        Assert.assertEquals(responseOfGet.jsonPath().getLong("list[0].expiresAt"), NEVER_EXPIRES,
                "The regenerated client secret is not listed as a non expiring secret.");
    }

    @Test(dependsOnMethods = "testRegenerateClientSecret")
    public void testDeleteAppWithClientSecrets() {

        String applicationPath = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId;

        getResponseOfDelete(applicationPath)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        getResponseOfGet(applicationPath).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
        createdAppId = null;
    }

    @Test(dependsOnMethods = "testDeleteAppWithClientSecrets")
    public void testCreateOAuthAppWithoutClientSecretExpiry() throws Exception {

        assertApplicationCreatedWithNonExpiringClientSecret("OAuth Application With Zero Secret Expiry",
                NEVER_EXPIRES);
        assertApplicationCreatedWithNonExpiringClientSecret("OAuth Application Without Secret Expiry", null);
    }

    /**
     * Create an OAuth application with the given client secret expiry and assert its secret never expires.
     *
     * @param appName               Name of the application to be created.
     * @param clientSecretExpiresAt Client secret expiry as Unix epoch seconds; null to omit the field.
     * @throws Exception If an error occurs while building the application payload.
     */
    private void assertApplicationCreatedWithNonExpiringClientSecret(String appName, Long clientSecretExpiresAt)
            throws Exception {

        String body = buildOAuthAppPayload(appName, clientSecretExpiresAt);
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        createdAppId = extractApplicationIdFromLocationHeader(responseOfPost.getHeader(HttpHeaders.LOCATION));
        assertNotBlank(createdAppId);

        Response responseOfGet = getResponseOfGet(getOIDCConfigPath());
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("multipleClientSecretsConfigured", equalTo(false));
        Assert.assertEquals(responseOfGet.jsonPath().getLong("clientSecretExpiresAt"), NEVER_EXPIRES,
                "The OIDC config of application: " + appName + " reports an expiring client secret.");

        Response responseOfSecrets = getResponseOfGet(getClientSecretsPath());
        responseOfSecrets.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("count", equalTo(1))
                .body("list[0].secretValue", equalTo(responseOfGet.jsonPath().getString("clientSecret")))
                .body("list[0].status", equalTo(STATUS_ACTIVE))
                .body("list[0].latest", equalTo(true));
        Assert.assertEquals(responseOfSecrets.jsonPath().getLong("list[0].expiresAt"), NEVER_EXPIRES,
                "The client secret of application: " + appName + " is listed as an expiring secret.");

        getResponseOfDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        createdAppId = null;
    }

    /**
     * Build an OAuth application creation payload carrying the given client secret expiry.
     *
     * @param appName               Name of the application.
     * @param clientSecretExpiresAt Client secret expiry as Unix epoch seconds; null to omit the field.
     * @return Application creation payload.
     * @throws Exception If an error occurs while reading or building the payload.
     */
    private String buildOAuthAppPayload(String appName, Long clientSecretExpiresAt) throws Exception {

        JSONObject payload = new JSONObject(readResource("create-oauth-app.json"));
        payload.put("name", appName);
        if (clientSecretExpiresAt != null) {
            payload.getJSONObject("inboundProtocolConfiguration").getJSONObject("oidc")
                    .put("clientSecretExpiresAt", clientSecretExpiresAt);
        }
        return payload.toString();
    }

    /**
     * Build an OIDC inbound update payload from the current configuration of the application.
     *
     * @param responseOfGet Response of the OIDC inbound configuration retrieval.
     * @return OIDC inbound update payload.
     * @throws Exception If an error occurs while building the payload.
     */
    private JSONObject buildOIDCUpdatePayload(Response responseOfGet) throws Exception {

        JsonPath currentConfig = responseOfGet.jsonPath();
        JSONObject payload = new JSONObject();
        payload.put("clientId", currentConfig.getString("clientId"));
        payload.put("clientSecret", currentConfig.getString("clientSecret"));
        payload.put("grantTypes", new JSONArray(currentConfig.getList("grantTypes")));
        payload.put("callbackURLs", new JSONArray(currentConfig.getList("callbackURLs")));
        payload.put("publicClient", currentConfig.getBoolean("publicClient"));
        payload.put("allowedOrigins", new JSONArray(Optional.ofNullable(currentConfig.getList("allowedOrigins"))
                .orElse(Collections.emptyList())));
        return payload;
    }

    private String getOIDCConfigPath() {

        return APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId
                + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;
    }

    private String getClientSecretsPath() {

        return APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + createdAppId + CLIENT_SECRETS_CONTEXT_PATH;
    }

    private String getClientSecretPath(String secretId) {

        return getClientSecretsPath() + PATH_SEPARATOR + secretId;
    }
}
