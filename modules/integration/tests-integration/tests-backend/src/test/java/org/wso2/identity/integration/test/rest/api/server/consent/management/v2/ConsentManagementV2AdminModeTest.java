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

package org.wso2.identity.integration.test.rest.api.server.consent.management.v2;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.given;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Integration tests for Consent Management API v2 admin-mode paths.
 *
 * <p>Admin mode is active when an OAuth2 M2M token carrying an operation-level scope
 * (e.g. {@code internal_consent_mgt_consent_view}) is used. In admin mode the server
 * allows cross-subject access that is forbidden to regular (basic-auth) users.
 *
 * <p>Prerequisites: the consent management v2 API resource must be registered with
 * {@code isOperationScopeMandatory=true} so that {@code AuthorizationValve} sets
 * {@code OperationScopeValidationContext.validationRequired=true} and
 * {@code ConsentManagementService.isAdminMode()} returns {@code true}.
 */
public class ConsentManagementV2AdminModeTest extends ConsentManagementV2TestBase {

    private static final String CONSENT_MANAGEMENT_API_IDENTIFIER = "/api/identity/consent-mgt/v2.0/consents";
    private static final String SCOPE_CONSENT_VIEW = "internal_consent_mgt_consent_view";
    private static final String SCOPE_CONSENT_CREATE = "internal_consent_mgt_consent_create";
    private static final String SCOPE_CONSENT_UPDATE = "internal_consent_mgt_consent_update";

    private static final String ADMIN_MODE_USER = "consent_admin_mode_user";
    private static final String ADMIN_MODE_USER_PASSWORD = "Admin@123";

    private CloseableHttpClient httpClient;
    private OAuth2RestClient oauth2RestClient;
    private SCIM2RestClient scim2RestClient;

    private String m2mAppId;
    private String m2mClientId;
    private String m2mClientSecret;

    private String adminTestUserId;
    private String adminTestPurposeId;
    private String adminTestElementId;
    private String adminTestConsentId;
    private String updateTestConsentId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ConsentManagementV2AdminModeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(tenant);
        httpClient = HttpClients.createDefault();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oauth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);

        ApplicationModel appModel = new ApplicationModel();
        appModel.setName("ConsentAdminModeTestApp");
        appModel.setIsManagementApp(true);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(Arrays.asList("client_credentials"));
        InboundProtocols inboundProtocols = new InboundProtocols();
        inboundProtocols.setOidc(oidcConfig);
        appModel.setInboundProtocolConfiguration(inboundProtocols);
        m2mAppId = oauth2RestClient.createApplication(appModel);

        OpenIDConnectConfiguration oidcDetails = oauth2RestClient.getOIDCInboundDetails(m2mAppId);
        m2mClientId = oidcDetails.getClientId();
        m2mClientSecret = oidcDetails.getClientSecret();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeAPIForApp(CONSENT_MANAGEMENT_API_IDENTIFIER);
        }

        RestAssured.basePath = basePath;
        try {
            Response elementResponse = getResponseOfPost(ELEMENTS_ENDPOINT,
                    "{\"name\": \"consent_admin_mode_elem\", \"displayName\": \"Admin Mode Elem\","
                            + " \"description\": \"Admin mode test element\"}");
            elementResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            adminTestElementId = elementResponse.jsonPath().getString("id");

            Response purposeResponse = getResponseOfPost(PURPOSES_ENDPOINT,
                    "{\"name\": \"consent_admin_mode_purpose\", \"description\": \"Admin mode purpose\","
                            + " \"type\": \"Core\", \"version\": \"1\","
                            + " \"elements\": [{\"id\": \"" + adminTestElementId + "\", \"mandatory\": true}]}");
            purposeResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            adminTestPurposeId = purposeResponse.jsonPath().getString("id");

            adminTestUserId = createTestUser(ADMIN_MODE_USER);
            String userAuthName = buildUserAuthName(ADMIN_MODE_USER);

            // The user creates their own consent via the user API; the admin tests then read it via the admin API.
            String consentBody = "{\"serviceId\": \"admin-mode-test\", \"language\": \"en\","
                    + " \"purposes\": [{\"id\": \"" + adminTestPurposeId + "\","
                    + " \"elements\": [{\"id\": \"" + adminTestElementId + "\"}]}]}";
            Response consentResponse = given()
                    .auth().preemptive().basic(userAuthName, ADMIN_MODE_USER_PASSWORD)
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.ACCEPT, ContentType.JSON)
                    .body(consentBody)
                    .post(getUserConsentApiBaseUrl());
            consentResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
            adminTestConsentId = consentResponse.jsonPath().getString("id");
        } finally {
            RestAssured.basePath = "";
        }
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void testConclude() throws Exception {

        RestAssured.basePath = basePath;
        try {
            if (adminTestUserId != null) {
                scim2RestClient.deleteUser(adminTestUserId);
            }
            if (adminTestPurposeId != null) {
                getResponseOfDelete(PURPOSES_ENDPOINT + "/" + adminTestPurposeId);
            }
            if (adminTestElementId != null) {
                getResponseOfDelete(ELEMENTS_ENDPOINT + "/" + adminTestElementId);
            }
            if (m2mAppId != null) {
                oauth2RestClient.deleteApplication(m2mAppId);
            }
        } finally {
            if (scim2RestClient != null) {
                scim2RestClient.closeHttpClient();
            }
            if (oauth2RestClient != null) {
                oauth2RestClient.closeHttpClient();
            }
            if (httpClient != null) {
                httpClient.close();
            }
            RestAssured.basePath = "";
            super.testConclude();
        }
    }

    // =========================================================================
    // Admin mode tests — OAuth2 M2M token with operation scopes
    // =========================================================================

    @Test
    public void testAdminGetConsentOfAnotherUser() throws Exception {

        String token = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "/" + adminTestConsentId, token)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("subjectId", equalTo(ADMIN_MODE_USER));
    }

    @Test
    public void testAdminListConsentsWithSubjectIdFilter() throws Exception {

        String token = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "?subjectId=" + ADMIN_MODE_USER, token)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", greaterThanOrEqualTo(1));
    }

    @Test
    public void testAdminListConsentsAcrossAllUsers() throws Exception {

        String token = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT, token)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("totalResults", greaterThanOrEqualTo(1));
    }

    @Test
    public void testAdminCreateConsentOnBehalfOfUser() throws Exception {

        String token = getAdminToken(SCOPE_CONSENT_CREATE);
        String consentBody = "{\"subjectId\": \"" + ADMIN_MODE_USER + "\","
                + " \"serviceId\": \"admin-mode-created-service\", \"language\": \"en\","
                + " \"purposes\": [{\"id\": \"" + adminTestPurposeId + "\","
                + " \"elements\": [{\"id\": \"" + adminTestElementId + "\"}]}]}";
        // The created consent is cascade-deleted when adminTestUserId is deleted in @AfterClass.
        getResponseOfPostWithOAuth2(CONSENTS_ENDPOINT, consentBody, token)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("subjectId", equalTo(ADMIN_MODE_USER));
    }

    /**
     * Creates a dedicated consent (so the shared {@code adminTestConsentId} read by other tests is
     * left untouched) and upserts an authorizer through PUT /consents/{consentId}. The added
     * authorizer (APPROVED) must surface in the consent's authorization list, and the recomputed
     * consent state must remain ACTIVE. Verifies both the PUT response and a follow-up GET.
     */
    @Test
    public void testAdminUpdateConsentAuthorizations() throws Exception {

        String createToken = getAdminToken(SCOPE_CONSENT_CREATE);
        String consentBody = "{\"subjectId\": \"" + ADMIN_MODE_USER + "\","
                + " \"serviceId\": \"admin-mode-update-service\", \"language\": \"en\","
                + " \"purposes\": [{\"id\": \"" + adminTestPurposeId + "\","
                + " \"elements\": [{\"id\": \"" + adminTestElementId + "\"}]}]}";
        // The created consent is cascade-deleted when adminTestUserId is deleted in @AfterClass.
        Response createResponse = getResponseOfPostWithOAuth2(CONSENTS_ENDPOINT, consentBody, createToken);
        createResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        updateTestConsentId = createResponse.jsonPath().getString("id");

        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                + " \"type\": \"USER\", \"state\": \"APPROVED\"}]}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + updateTestConsentId, updateBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(updateTestConsentId))
                .body("state", equalTo("ACTIVE"))
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }.state",
                        equalTo("APPROVED"));

        // Verify the change is persisted.
        String viewToken = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "/" + updateTestConsentId, viewToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }.state",
                        equalTo("APPROVED"));
    }

    /**
     * Overriding an existing authorizer's state to REVOKED through PUT must transition the consent
     * to the REVOKED state.
     */
    @Test(dependsOnMethods = {"testAdminUpdateConsentAuthorizations"})
    public void testAdminUpdateConsentAuthorizationRevoke() throws Exception {

        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                + " \"state\": \"REVOKED\"}]}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + updateTestConsentId, updateBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(updateTestConsentId))
                .body("state", equalTo("REVOKED"))
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }.state",
                        equalTo("REVOKED"));
    }

    /**
     * Updating a consent that does not exist must return 404.
     */
    @Test
    public void testUpdateConsentNotFoundForUnknownId() throws Exception {

        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/non-existent-consent-id",
                "{\"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                        + " \"type\": \"USER\", \"state\": \"APPROVED\"}]}", updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    /**
     * Setting {@code expiryTime} through PUT must persist a (future) expiry on the consent.
     * The PUT response and a follow-up GET must both echo the millis-since-epoch value.
     */
    @Test
    public void testAdminUpdateConsentExpiryTime() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-expiry-service");
        long expiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);

        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"expiryTime\": " + expiry + "}";
        Response patchResponse = getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, updateBody, updateToken);
        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(consentId));
        assertEquals(patchResponse.jsonPath().getLong("expiryTime"), expiry,
                "Expiry time was not updated in the PUT response.");

        // Verify the change is persisted.
        String viewToken = getAdminToken(SCOPE_CONSENT_VIEW);
        Response getResponse = getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, viewToken);
        getResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        assertEquals(getResponse.jsonPath().getLong("expiryTime"), expiry,
                "Expiry time was not persisted.");
    }

    /**
     * The {@code properties} field is a full replace: a first PUT sets two properties, and a second
     * PUT with a different single property must drop the previously stored keys (not merge them).
     */
    @Test
    public void testAdminUpdateConsentProperties() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-properties-service");
        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);

        String setBody = "{\"properties\": {\"dataCategory\": \"personal\", \"region\": \"EU\"}}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, setBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.dataCategory", equalTo("personal"))
                .body("properties.region", equalTo("EU"));

        // A second PUT replaces the full property map — the previous "dataCategory" key must disappear.
        String replaceBody = "{\"properties\": {\"region\": \"US\"}}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, replaceBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.region", equalTo("US"))
                .body("properties.dataCategory", nullValue());

        // Verify the replace is persisted.
        String viewToken = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, viewToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.region", equalTo("US"))
                .body("properties.dataCategory", nullValue());
    }

    /**
     * Clearing properties: a PUT with an empty {@code properties} object removes all stored
     * properties (an empty map deletes existing rows and inserts nothing). A {@code null}/omitted
     * {@code properties} would instead leave them untouched, so {@code {}} is required to clear.
     */
    @Test
    public void testAdminUpdateConsentClearProperties() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-clear-properties-service");
        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);

        String setBody = "{\"properties\": {\"dataCategory\": \"personal\", \"region\": \"EU\"}}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, setBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.dataCategory", equalTo("personal"))
                .body("properties.region", equalTo("EU"));

        // An empty map clears every property.
        String clearBody = "{\"properties\": {}}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, clearBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.dataCategory", nullValue())
                .body("properties.region", nullValue());

        // Verify the clear is persisted.
        String viewToken = getAdminToken(SCOPE_CONSENT_VIEW);
        getResponseOfGetWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, viewToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("properties.dataCategory", nullValue())
                .body("properties.region", nullValue());
    }

    /**
     * Setting an authorizer's state to REJECTED through PUT must transition the consent to REJECTED.
     */
    @Test
    public void testAdminUpdateConsentAuthorizationReject() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-reject-service");
        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                + " \"type\": \"USER\", \"state\": \"REJECTED\"}]}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, updateBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(consentId))
                .body("state", equalTo("REJECTED"))
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }.state",
                        equalTo("REJECTED"));
    }

    /**
     * Adding a new authorizer without a {@code state} must leave it PENDING. PENDING authorizations
     * are not exposed in the response DTO, so the user must be absent from the authorization list
     * while the recomputed consent state becomes PENDING.
     */
    @Test
    public void testAdminAddAuthorizerWithoutStateRemainsPending() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-pending-service");
        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                + " \"type\": \"USER\"}]}";
        getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, updateBody, updateToken)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(consentId))
                .body("state", equalTo("PENDING"))
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }", nullValue());
    }

    /**
     * A single PUT carrying {@code expiryTime}, {@code properties} and {@code authorizations}
     * must apply all three in one request. The APPROVED authorizer drives the state to ACTIVE.
     */
    @Test
    public void testAdminUpdateConsentCombinedFields() throws Exception {

        String consentId = createConsentForUpdate("admin-mode-combined-service");
        long expiry = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);

        String updateToken = getAdminToken(SCOPE_CONSENT_UPDATE);
        String updateBody = "{\"expiryTime\": " + expiry + ","
                + " \"properties\": {\"region\": \"EU\"},"
                + " \"authorizations\": [{\"userId\": \"" + ADMIN_MODE_USER + "\","
                + " \"type\": \"USER\", \"state\": \"APPROVED\"}]}";
        Response patchResponse = getResponseOfPatchWithOAuth2(CONSENTS_ENDPOINT + "/" + consentId, updateBody, updateToken);
        patchResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(consentId))
                .body("state", equalTo("ACTIVE"))
                .body("properties.region", equalTo("EU"))
                .body("authorizations.find { it.userId == '" + ADMIN_MODE_USER + "' }.state",
                        equalTo("APPROVED"));
        assertEquals(patchResponse.jsonPath().getLong("expiryTime"), expiry,
                "Expiry time was not updated in the combined update.");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String getUserConsentApiBaseUrl() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "api/users/v1/me/consents";
        }
        return serverURL + "t/" + tenant + "/api/users/v1/me/consents";
    }

    /**
     * Creates a consent for {@code ADMIN_MODE_USER} via the admin create scope and returns its id.
     * The created consent is cascade-deleted when {@code adminTestUserId} is deleted in @AfterClass.
     */
    private String createConsentForUpdate(String serviceId) throws Exception {

        String createToken = getAdminToken(SCOPE_CONSENT_CREATE);
        String consentBody = "{\"subjectId\": \"" + ADMIN_MODE_USER + "\","
                + " \"serviceId\": \"" + serviceId + "\", \"language\": \"en\","
                + " \"purposes\": [{\"id\": \"" + adminTestPurposeId + "\","
                + " \"elements\": [{\"id\": \"" + adminTestElementId + "\"}]}]}";
        Response createResponse = getResponseOfPostWithOAuth2(CONSENTS_ENDPOINT, consentBody, createToken);
        createResponse.then().assertThat().statusCode(HttpStatus.SC_CREATED);
        return createResponse.jsonPath().getString("id");
    }

    private void authorizeAPIForApp(String apiIdentifier) throws IOException {

        List<APIResourceListItem> filteredAPIResource =
                oauth2RestClient.getAPIResourcesWithFiltering("identifier+eq+" + apiIdentifier);
        if (filteredAPIResource != null && !filteredAPIResource.isEmpty()) {
            String apiId = filteredAPIResource.get(0).getId();
            List<ScopeGetModel> apiResourceScopes = oauth2RestClient.getAPIResourceScopes(apiId);
            AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
            authorizedAPICreationModel.setId(apiId);
            authorizedAPICreationModel.setPolicyIdentifier("RBAC");
            apiResourceScopes.forEach(scope -> authorizedAPICreationModel.addScopesItem(scope.getName()));
            oauth2RestClient.addAPIAuthorizationToApplication(m2mAppId, authorizedAPICreationModel);
        }
    }

    private String getAdminToken(String scopes) throws Exception {

        HttpPost post = new HttpPost(getTokenEndpoint());
        post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " +
                Base64.encodeBase64String((m2mClientId + ":" + m2mClientSecret).getBytes()).trim());
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("scope", scopes));
        post.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = httpClient.execute(post);
        String responseStr = EntityUtils.toString(response.getEntity());
        JSONObject json = (JSONObject) JSONValue.parse(responseStr);
        assertNotNull(json.get("access_token"), "Failed to get admin token: " + responseStr);
        return json.get("access_token").toString();
    }

    private String getTokenEndpoint() {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "oauth2/token";
        }
        return serverURL + "t/" + tenant + "/oauth2/token";
    }

    private String createTestUser(String userName) throws Exception {

        UserObject user = new UserObject();
        user.setUserName(userName);
        user.setPassword(ADMIN_MODE_USER_PASSWORD);
        user.setName(new Name().givenName("Admin").familyName("ModeUser"));
        user.addEmail(new Email().value(userName + "@wso2.com"));
        return scim2RestClient.createUser(user);
    }

    private String buildUserAuthName(String userName) {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenant)
                ? userName
                : userName + "@" + tenant;
    }
}
