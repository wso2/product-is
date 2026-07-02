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

package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * REST client for consent management API operations used in webhook event tests.
 *
 * <p>Request bodies for element and purpose creation are loaded from the shared JSON
 * resources under {@code org/wso2/identity/integration/test/rest/api/server/consent/management/v2/}.
 */
public class ConsentManagementRestClient extends RestBaseClient {

    private static final String CONSENT_ADMIN_API_PATH = "api/identity/consent-mgt/v2.0";
    private static final String CONSENT_USER_API_PATH = "api/users/v1/me/consents";
    private static final String ELEMENTS_PATH = "/elements";
    private static final String PURPOSES_PATH = "/purposes";

    private static final String RESOURCE_BASE =
            "org/wso2/identity/integration/test/rest/api/server/consent/management/v2/";

    private final String tenantDomain;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminApiBaseUrl;
    private final String userConsentApiBaseUrl;

    public ConsentManagementRestClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.adminUsername = tenantInfo.getContextUser().getUserName();
        this.adminPassword = tenantInfo.getContextUser().getPassword();
        this.adminApiBaseUrl = buildAdminApiBaseUrl(serverUrl, tenantDomain);
        this.userConsentApiBaseUrl = buildUserApiBaseUrl(serverUrl, tenantDomain);
    }

    /**
     * Creates a PII element using the body from {@code create-element.json}.
     *
     * @return The created element's UUID.
     */
    public String createElement() throws Exception {

        String body = readResource("create-element.json");
        try (CloseableHttpResponse response = getResponseOfHttpPost(
                adminApiBaseUrl + ELEMENTS_PATH, body, getAdminHeaders())) {
            return new ObjectMapper().readTree(readResponse(response)).get("id").asText();
        }
    }

    /**
     * Deletes a PII element by ID.
     */
    public void deleteElement(String elementId) throws IOException {

        try (CloseableHttpResponse ignored = getResponseOfHttpDelete(
                adminApiBaseUrl + ELEMENTS_PATH + "/" + elementId, getAdminHeaders())) {
            // expects 204
        }
    }

    /**
     * Creates a consent purpose using the body from {@code create-purpose.json},
     * substituting the element ID placeholder with {@code elementId}.
     *
     * @param elementId UUID of the PII element to attach to the purpose.
     * @return The created purpose's UUID.
     */
    public String createPurpose(String elementId) throws Exception {

        String body = readResource("create-purpose.json")
                .replace("\"id\": \"1\"", "\"id\": \"" + elementId + "\"");
        try (CloseableHttpResponse response = getResponseOfHttpPost(
                adminApiBaseUrl + PURPOSES_PATH, body, getAdminHeaders())) {
            return new ObjectMapper().readTree(readResponse(response)).get("id").asText();
        }
    }

    /**
     * Deletes a consent purpose by ID.
     */
    public void deletePurpose(String purposeId) throws IOException {

        try (CloseableHttpResponse ignored = getResponseOfHttpDelete(
                adminApiBaseUrl + PURPOSES_PATH + "/" + purposeId, getAdminHeaders())) {
            // expects 204
        }
    }

    /**
     * Creates a consent receipt via the user consent API (POST /api/users/v1/me/consents).
     * The authenticated user becomes the consent subject.
     *
     * @param serviceId  Service identifier stored in the receipt.
     * @param purposeId  UUID of the consent purpose.
     * @param elementId  UUID of the PII element to consent to.
     * @param username   Username for authenticating the consent subject (not the tenant admin).
     * @param password   Password of the consent subject.
     * @return The created consent receipt UUID.
     */
    public String createConsent(String serviceId, String purposeId, String elementId,
                                String username, String password) throws Exception {

        String body = String.format(
                "{\"serviceId\":\"%s\",\"language\":\"en\","
                        + "\"purposes\":[{\"id\":\"%s\",\"elements\":[{\"id\":\"%s\"}]}]}",
                serviceId, purposeId, elementId);
        try (CloseableHttpResponse response = getResponseOfHttpPost(
                userConsentApiBaseUrl, body, getUserHeaders(username, password))) {
            return new ObjectMapper().readTree(readResponse(response)).get("id").asText();
        }
    }

    /**
     * Revokes a consent receipt via the user consent API
     * (POST /api/users/v1/me/consents/{receiptId}/revoke).
     *
     * @param receiptId UUID of the consent receipt to revoke.
     * @param username  Username of the consent subject.
     * @param password  Password of the consent subject.
     */
    public void revokeConsent(String receiptId, String username, String password) throws IOException {

        try (CloseableHttpResponse ignored = getResponseOfHttpPost(
                userConsentApiBaseUrl + "/" + receiptId + "/revoke", "", getUserHeaders(username, password))) {
            // expects 200
        }
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }

    private String buildAdminApiBaseUrl(String serverUrl, String tenantDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return serverUrl + CONSENT_ADMIN_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + CONSENT_ADMIN_API_PATH;
    }

    private String buildUserApiBaseUrl(String serverUrl, String tenantDomain) {

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return serverUrl + CONSENT_USER_API_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + CONSENT_USER_API_PATH;
    }

    private String readResource(String fileName) throws IOException {

        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(RESOURCE_BASE + fileName)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + RESOURCE_BASE + fileName);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Header[] getAdminHeaders() {

        return new Header[]{
                new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT),
                new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                        Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim()),
                new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON))
        };
    }

    private Header[] getUserHeaders(String username, String password) {

        return new Header[]{
                new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT),
                new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                        Base64.encodeBase64String((username + ":" + password).getBytes()).trim()),
                new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON))
        };
    }

    private String readResponse(CloseableHttpResponse response) throws IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
