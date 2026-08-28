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

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.assertNotBlank;
import static org.wso2.identity.integration.test.rest.api.server.application.management.v1.Utils.extractApplicationIdFromLocationHeader;

/**
 * Tests for exporting and importing client secrets of an application using the Application Management REST API.
 */
public class ApplicationImportExportClientSecretTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private static final String CLIENT_SECRETS_CONTEXT_PATH = INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH + "/secrets";
    private static final String APPLICATION_IMPORT_PATH = "/import";
    private static final String APPLICATION_EXPORT_FILE_PATH = "/exportFile";
    private static final String EXPORT_SECRETS_QUERY_PARAM = "?exportSecrets=";
    private static final String IMPORT_FILE_CONTROL_NAME = "file";

    private static final String MEDIA_TYPE_XML = "application/xml";
    private static final String MEDIA_TYPE_JSON = "application/json";
    private static final String MEDIA_TYPE_YAML = "application/yaml";
    private static final String XML_FILE_EXTENSION = ".xml";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String YAML_FILE_EXTENSION = ".yaml";

    private static final String APPLICATION_NAME_PREFIX = "ClientSecretImportExportApp";
    private static final String APPLICATION_DESCRIPTION = "Application exercising client secret export and import.";
    private static final String UPDATED_APPLICATION_DESCRIPTION = "Application updated through an import.";

    private static final String CREATE_APPLICATION_PAYLOAD = "{\"name\":\"%s\",\"description\":\"%s\"," +
            "\"inboundProtocolConfiguration\":{\"oidc\":{\"grantTypes\":[\"client_credentials\"]," +
            "\"clientSecretExpiresAt\":%d,\"publicClient\":false}}}";
    private static final String CREATE_CLIENT_SECRET_PAYLOAD = "{\"expiresAt\":%d}";

    private static final String EXPORTED_LATEST_SECRET_ELEMENT = "oauthConsumerSecret";
    private static final String EXPORTED_ADDITIONAL_SECRET_VALUE_FIELD = "secretValue";
    private static final String EXPORTED_ADDITIONAL_SECRET_EXPIRY_ELEMENT = "expiryTime";

    /* Client secret metadata that must never leave the server through an application export. */
    private static final String[] CLIENT_SECRET_METADATA_FIELDS =
            {"secretId", "secretHash", "createdTime", "consumerKeyId"};

    private static final long INITIAL_SECRET_LIFETIME_IN_SECONDS = 3600L;
    private static final long LATEST_SECRET_LIFETIME_IN_SECONDS = 7200L;
    private static final long MILLIS_PER_SECOND = 1000L;
    private static final long INVALID_EXPIRY_TIME_IN_MILLIS = -1000L;

    private final Set<String> appsToCleanUp = new LinkedHashSet<>();
    private final List<Path> temporaryFiles = new ArrayList<>();
    private final String tokenEndpoint;

    private String sourceAppId;
    private String sourceAppName;
    private String sourceClientId;
    private String initialSecretValue;
    private String latestSecretValue;
    private long initialSecretExpiresAt;
    private long latestSecretExpiresAt;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationImportExportClientSecretTest(TestUserMode userMode) throws Exception {

        super(userMode);

        if (StringUtils.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, tenant)) {
            tokenEndpoint = OAuth2Constant.ACCESS_TOKEN_ENDPOINT;
        } else {
            tokenEndpoint = OAuth2Constant.TENANT_TOKEN_ENDPOINT.replace(OAuth2Constant.TENANT_PLACEHOLDER, tenant);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        /* The base class clears the RestAssured base path after every test method, so restore it before the
           cleanup calls. */
        RestAssured.basePath = basePath;
        cleanUpApplications(appsToCleanUp);
        for (Path temporaryFile : temporaryFiles) {
            Files.deleteIfExists(temporaryFile);
        }
    }

    @DataProvider(name = "exportMediaTypeProvider")
    public static Object[][] exportMediaTypeProvider() {

        return new Object[][]{
                {MEDIA_TYPE_XML, XML_FILE_EXTENSION},
                {MEDIA_TYPE_JSON, JSON_FILE_EXTENSION},
                {MEDIA_TYPE_YAML, YAML_FILE_EXTENSION}
        };
    }

    @Test
    public void testCreateApplicationWithTwoClientSecrets() throws Exception {

        initialSecretExpiresAt = Instant.now().getEpochSecond() + INITIAL_SECRET_LIFETIME_IN_SECONDS;
        latestSecretExpiresAt = Instant.now().getEpochSecond() + LATEST_SECRET_LIFETIME_IN_SECONDS;
        sourceAppName = APPLICATION_NAME_PREFIX + newRandomIdentifier();

        String payload = String.format(CREATE_APPLICATION_PAYLOAD, sourceAppName, APPLICATION_DESCRIPTION,
                initialSecretExpiresAt);
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, payload);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        sourceAppId = extractApplicationIdFromLocationHeader(responseOfPost.getHeader(HttpHeaders.LOCATION));
        assertNotBlank(sourceAppId);
        appsToCleanUp.add(sourceAppId);

        JsonPath inboundDetails = getInboundOidcDetails(sourceAppId);
        sourceClientId = inboundDetails.getString("clientId");
        initialSecretValue = inboundDetails.getString("clientSecret");
        assertNotBlank(sourceClientId);
        assertNotBlank(initialSecretValue);
        Assert.assertEquals(inboundDetails.getLong("clientSecretExpiresAt"), initialSecretExpiresAt,
                "The expiry of the initial secret of the application was not persisted as requested.");

        Response responseOfSecretCreate = getResponseOfPost(secretsPath(sourceAppId),
                String.format(CREATE_CLIENT_SECRET_PAYLOAD, latestSecretExpiresAt));
        responseOfSecretCreate.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        latestSecretValue = responseOfSecretCreate.jsonPath().getString("secretValue");
        assertNotBlank(latestSecretValue);

        Assert.assertEquals(getInboundOidcDetails(sourceAppId).getString("clientSecret"), latestSecretValue,
                "The application record secret is not the latest client secret.");
        Assert.assertEquals(getClientSecrets(sourceAppId).size(), 2,
                "The application should hold two client secrets before it is exported.");
    }

    @Test(dependsOnMethods = "testCreateApplicationWithTwoClientSecrets", dataProvider = "exportMediaTypeProvider")
    public void testExportAndCreateImportOfClientSecrets(String mediaType, String fileExtension) throws Exception {

        String exportWithoutSecrets = getExportedApplication(sourceAppId, false, mediaType);
        Assert.assertFalse(exportWithoutSecrets.contains(latestSecretValue),
                "The latest client secret was exported although secret export was not requested.");
        Assert.assertFalse(exportWithoutSecrets.contains(initialSecretValue),
                "A non-latest client secret was exported although secret export was not requested.");
        Assert.assertFalse(exportWithoutSecrets.contains(EXPORTED_ADDITIONAL_SECRET_VALUE_FIELD),
                "The non-latest client secrets were exported although secret export was not requested.");

        String exportWithSecrets = getExportedApplication(sourceAppId, true, mediaType);
        assertExportedClientSecrets(exportWithSecrets);

        String importedAppName = APPLICATION_NAME_PREFIX + newRandomIdentifier();
        String importedClientId = newRandomIdentifier();
        Path importFile = writeImportFile(
                withNewApplicationIdentity(exportWithSecrets, importedAppName, importedClientId), fileExtension);

        Response responseOfImport = getResponseOfImport(importFile, mediaType, false);
        responseOfImport.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());

        String importedAppId = extractApplicationIdFromLocationHeader(responseOfImport.getHeader(HttpHeaders.LOCATION));
        assertNotBlank(importedAppId);
        appsToCleanUp.add(importedAppId);

        assertRestoredClientSecrets(importedAppId, importedClientId);
    }

    @Test(dependsOnMethods = "testExportAndCreateImportOfClientSecrets")
    public void testUpdateImportKeepsClientSecretsUntouched() throws Exception {

        List<Map<String, Object>> secretsBeforeImport = getClientSecrets(sourceAppId);
        String exportWithSecrets = getExportedApplication(sourceAppId, true, MEDIA_TYPE_XML);
        Path importFile = writeImportFile(
                exportWithSecrets.replace(APPLICATION_DESCRIPTION, UPDATED_APPLICATION_DESCRIPTION),
                XML_FILE_EXTENSION);

        getResponseOfImport(importFile, MEDIA_TYPE_XML, true).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        /* Fetched without the OpenAPI validation filter: the application response omits the inbound protocol
           name the response schema declares required, which is unrelated to this test. */
        given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .when()
                .get(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + sourceAppId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("description", equalTo(UPDATED_APPLICATION_DESCRIPTION));

        Assert.assertEquals(getClientSecrets(sourceAppId), secretsBeforeImport,
                "The client secrets of the application were altered by an update import.");
        Assert.assertEquals(getStatusOfTokenRequest(sourceClientId, latestSecretValue), HttpStatus.SC_OK,
                "The latest client secret stopped authenticating after an update import.");
        Assert.assertEquals(getStatusOfTokenRequest(sourceClientId, initialSecretValue), HttpStatus.SC_OK,
                "The non-latest client secret stopped authenticating after an update import.");
    }

    @Test(dependsOnMethods = "testUpdateImportKeepsClientSecretsUntouched")
    public void testCreateImportWithNegativeExpiryOnNonLatestSecret() throws Exception {

        String exportWithSecrets = getExportedApplication(sourceAppId, true, MEDIA_TYPE_XML);
        String exportedExpiry = element(EXPORTED_ADDITIONAL_SECRET_EXPIRY_ELEMENT,
                String.valueOf(initialSecretExpiresAt * MILLIS_PER_SECOND));
        Assert.assertTrue(exportWithSecrets.contains(exportedExpiry),
                "The exported application does not carry the expiry of the non-latest client secret.");

        String importContent = withNewApplicationIdentity(exportWithSecrets,
                APPLICATION_NAME_PREFIX + newRandomIdentifier(), newRandomIdentifier())
                .replace(exportedExpiry, element(EXPORTED_ADDITIONAL_SECRET_EXPIRY_ELEMENT,
                        String.valueOf(INVALID_EXPIRY_TIME_IN_MILLIS)));

        assertImportRejected(importContent);
    }

    @Test(dependsOnMethods = "testUpdateImportKeepsClientSecretsUntouched")
    public void testCreateImportWithoutLatestClientSecret() throws Exception {

        String exportWithSecrets = getExportedApplication(sourceAppId, true, MEDIA_TYPE_XML);
        String importContent = withNewApplicationIdentity(exportWithSecrets,
                APPLICATION_NAME_PREFIX + newRandomIdentifier(), newRandomIdentifier())
                .replace(element(EXPORTED_LATEST_SECRET_ELEMENT, latestSecretValue),
                        element(EXPORTED_LATEST_SECRET_ELEMENT, StringUtils.EMPTY));
        Assert.assertFalse(importContent.contains(latestSecretValue),
                "The latest client secret is still present in the import file.");

        assertImportRejected(importContent);
    }

    /**
     * Assert that an export carrying secrets holds both client secret values with their expiry times in milliseconds
     * and no client secret metadata.
     *
     * @param exportedApplication Exported application content.
     */
    private void assertExportedClientSecrets(String exportedApplication) {

        Assert.assertTrue(exportedApplication.contains(latestSecretValue),
                "The latest client secret is missing from the export.");
        Assert.assertTrue(exportedApplication.contains(initialSecretValue),
                "The non-latest client secret is missing from the export.");
        Assert.assertTrue(exportedApplication.contains(String.valueOf(latestSecretExpiresAt * MILLIS_PER_SECOND)),
                "The expiry of the latest client secret is not exported in milliseconds.");
        Assert.assertTrue(exportedApplication.contains(String.valueOf(initialSecretExpiresAt * MILLIS_PER_SECOND)),
                "The expiry of the non-latest client secret is not exported in milliseconds.");

        for (String metadataField : CLIENT_SECRET_METADATA_FIELDS) {
            Assert.assertFalse(exportedApplication.contains(metadataField),
                    "Client secret metadata '" + metadataField + "' was leaked into the export.");
        }
    }

    /**
     * Assert that an application created by importing an export holds the same client secrets as the source
     * application and that each of them authenticates at the token endpoint.
     *
     * @param applicationId Identifier of the imported application.
     * @param clientId      Client ID of the imported application.
     * @throws Exception If an error occurs while calling the token endpoint.
     */
    private void assertRestoredClientSecrets(String applicationId, String clientId) throws Exception {

        List<Map<String, Object>> restoredSecrets = getClientSecrets(applicationId);
        Assert.assertEquals(restoredSecrets.size(), 2, "The imported application does not hold both client secrets.");

        Map<String, Long> expiryTimesBySecretValue = new HashMap<>();
        String restoredLatestSecret = null;
        for (Map<String, Object> restoredSecret : restoredSecrets) {
            expiryTimesBySecretValue.put((String) restoredSecret.get("secretValue"),
                    ((Number) restoredSecret.get("expiresAt")).longValue());
            if (Boolean.TRUE.equals(restoredSecret.get("latest"))) {
                Assert.assertNull(restoredLatestSecret, "More than one client secret is flagged as the latest.");
                restoredLatestSecret = (String) restoredSecret.get("secretValue");
            }
        }

        Assert.assertEquals(restoredLatestSecret, latestSecretValue,
                "The latest client secret was not restored as the latest secret of the imported application.");
        Assert.assertEquals(expiryTimesBySecretValue.get(latestSecretValue), Long.valueOf(latestSecretExpiresAt),
                "The expiry of the latest client secret did not round trip through the import.");
        Assert.assertEquals(expiryTimesBySecretValue.get(initialSecretValue), Long.valueOf(initialSecretExpiresAt),
                "The expiry of the non-latest client secret did not round trip through the import.");
        Assert.assertEquals(getInboundOidcDetails(applicationId).getString("clientSecret"), latestSecretValue,
                "The application record secret of the imported application is not the restored latest secret.");

        Assert.assertEquals(getStatusOfTokenRequest(clientId, latestSecretValue), HttpStatus.SC_OK,
                "The restored latest client secret does not authenticate at the token endpoint.");
        Assert.assertEquals(getStatusOfTokenRequest(clientId, initialSecretValue), HttpStatus.SC_OK,
                "The restored non-latest client secret does not authenticate at the token endpoint.");
    }

    /**
     * Assert that importing the given malformed application content as a new application is rejected.
     *
     * @param importContent Content of the application import file.
     * @throws IOException If an error occurs while writing the import file.
     */
    private void assertImportRejected(String importContent) throws IOException {

        Path importFile = writeImportFile(importContent, XML_FILE_EXTENSION);
        getResponseOfImport(importFile, MEDIA_TYPE_XML, false).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * Export an application in the given media type.
     *
     * @param applicationId Identifier of the application.
     * @param exportSecrets Whether the client secrets should be exported.
     * @param mediaType     Media type requested through the Accept header.
     * @return Exported application content.
     */
    private String getExportedApplication(String applicationId, boolean exportSecrets, String mediaType) {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + applicationId +
                APPLICATION_EXPORT_FILE_PATH + EXPORT_SECRETS_QUERY_PARAM + exportSecrets;
        Response response = getResponseOfGet(path, mediaType);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        return response.asString();
    }

    /**
     * Upload an application import file whose media type matches the format the file was exported in.
     *
     * @param importFile Application import file.
     * @param mediaType  Media type of the file content.
     * @param isUpdate   Whether the import updates an existing application.
     * @return Response of the import.
     */
    private Response getResponseOfImport(Path importFile, String mediaType, boolean isUpdate) {

        String endpoint = APPLICATION_MANAGEMENT_API_BASE_PATH + APPLICATION_IMPORT_PATH;
        RequestSpecification requestSpecification = given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(new EncoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart(IMPORT_FILE_CONTROL_NAME, importFile.toFile(), mediaType)
                .log().ifValidationFails()
                .when();
        return isUpdate ? requestSpecification.put(endpoint) : requestSpecification.post(endpoint);
    }

    /**
     * Rewrite an exported application so that it is imported as a new application.
     *
     * @param exportedApplication Exported application content.
     * @param applicationName     Name of the application to be created.
     * @param clientId            Client ID of the application to be created.
     * @return Content of the application import file.
     */
    private String withNewApplicationIdentity(String exportedApplication, String applicationName, String clientId) {

        return exportedApplication.replace(sourceAppName, applicationName).replace(sourceClientId, clientId);
    }

    private Path writeImportFile(String content, String fileExtension) throws IOException {

        Path importFile = Files.createTempFile(APPLICATION_NAME_PREFIX, fileExtension);
        Files.write(importFile, content.getBytes(StandardCharsets.UTF_8));
        temporaryFiles.add(importFile);
        return importFile;
    }

    private List<Map<String, Object>> getClientSecrets(String applicationId) {

        Response response = getResponseOfGet(secretsPath(applicationId));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        return response.jsonPath().getList("list");
    }

    private JsonPath getInboundOidcDetails(String applicationId) {

        Response response = getResponseOfGet(APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + applicationId +
                INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        return response.jsonPath();
    }

    /**
     * Request a client credentials access token authenticating with the given client secret.
     *
     * @param clientId     Client ID of the application.
     * @param clientSecret Client secret to authenticate with.
     * @return Status code of the token response.
     * @throws Exception If an error occurs while calling the token endpoint.
     */
    private int getStatusOfTokenRequest(String clientId, String clientSecret) throws Exception {

        TokenRequest tokenRequest = new TokenRequest(new URI(tokenEndpoint),
                new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret)),
                new ClientCredentialsGrant());
        return tokenRequest.toHTTPRequest().send().getStatusCode();
    }

    private String secretsPath(String applicationId) {

        return APPLICATION_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + applicationId + CLIENT_SECRETS_CONTEXT_PATH;
    }

    private static String element(String name, String value) {

        return "<" + name + ">" + value + "</" + name + ">";
    }

    private static String newRandomIdentifier() {

        return UUID.randomUUID().toString().replace("-", "");
    }
}
