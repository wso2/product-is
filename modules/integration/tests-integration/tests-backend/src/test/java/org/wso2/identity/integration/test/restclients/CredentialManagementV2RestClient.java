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

package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model.CredentialCreationResponse;
import org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model.CredentialsByType;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

/**
 * Rest client for the Credential Management REST API v2 (/api/users/v2).
 */
public class CredentialManagementV2RestClient extends RestBaseClient {

    private static final String API_USERS_V2_PATH = "api/users/v2";
    private static final String CREDENTIALS_PATH = "/credentials";

    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String credentialManagementBasePath;

    public CredentialManagementV2RestClient(String serverUrl, Tenant tenantInfo) {

        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.credentialManagementBasePath = buildBasePath(serverUrl, tenantDomain);
    }

    /**
     * GET /api/users/v2/{user-id}/credentials — retrieve all credentials grouped by type.
     *
     * @param userId User ID.
     * @return CredentialsByType response.
     * @throws Exception If the request fails or returns an unexpected status.
     */
    public CredentialsByType getUserCredentials(String userId) throws Exception {

        String url = credentialManagementBasePath + PATH_SEPARATOR + userId + CREDENTIALS_PATH;
        try (CloseableHttpResponse response = getResponseOfHttpGet(url, getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Unexpected status " + statusCode + " while getting credentials for user: "
                        + userId);
            }
            String body = EntityUtils.toString(response.getEntity());
            return new ObjectMapper(new JsonFactory()).readValue(body, CredentialsByType.class);
        }
    }

    /**
     * POST /api/users/v2/{user-id}/credentials/backup-code — create (or regenerate) backup codes.
     *
     * @param userId User ID.
     * @return CredentialCreationResponse containing the generated backup codes.
     * @throws Exception If the request fails or returns an unexpected status.
     */
    public CredentialCreationResponse createBackupCode(String userId) throws Exception {

        String url = credentialManagementBasePath + PATH_SEPARATOR + userId + CREDENTIALS_PATH
                + PATH_SEPARATOR + "backup-code";
        try (CloseableHttpResponse response = getResponseOfHttpPost(url, "", getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new Exception("Unexpected status " + statusCode
                        + " while creating backup codes for user: " + userId);
            }
            String body = EntityUtils.toString(response.getEntity());
            return new ObjectMapper(new JsonFactory()).readValue(body, CredentialCreationResponse.class);
        }
    }

    /**
     * POST /api/users/v2/{user-id}/credentials/{type} — attempt to create credential for an unsupported type.
     * Only backup-code is creatable; passkey and push-auth must return 400.
     *
     * @param userId User ID.
     * @param type   Credential type (e.g. "passkey", "push-auth").
     * @return ApiErrorResponse containing HTTP status code and UCM error code (if any).
     * @throws Exception If an I/O error occurs.
     */
    public ApiErrorResponse createCredentialByType(String userId, String type) throws Exception {

        String url = credentialManagementBasePath + PATH_SEPARATOR + userId + CREDENTIALS_PATH
                + PATH_SEPARATOR + type;
        try (CloseableHttpResponse response = getResponseOfHttpPost(url, "", getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            String errorCode = statusCode >= 400 ? extractErrorCode(EntityUtils.toString(response.getEntity())) : null;
            return new ApiErrorResponse(statusCode, errorCode);
        }
    }

    /**
     * DELETE /api/users/v2/{user-id}/credentials/{type} — delete all credentials of a type.
     * Only backup-code supports this; passkey and push-auth must return 400.
     *
     * @param userId User ID.
     * @param type   Credential type.
     * @return ApiErrorResponse containing HTTP status code and UCM error code (if any).
     * @throws Exception If an I/O error occurs.
     */
    public ApiErrorResponse deleteCredentialsByType(String userId, String type) throws Exception {

        String url = credentialManagementBasePath + PATH_SEPARATOR + userId + CREDENTIALS_PATH
                + PATH_SEPARATOR + type;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(url, getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            String errorCode = statusCode >= 400 ? extractErrorCode(EntityUtils.toString(response.getEntity())) : null;
            return new ApiErrorResponse(statusCode, errorCode);
        }
    }

    /**
     * DELETE /api/users/v2/{user-id}/credentials/{type}/{credential-id} — delete a specific credential.
     * Supported for passkey and push-auth; backup-code must return 400.
     *
     * @param userId       User ID.
     * @param type         Credential type.
     * @param credentialId Credential ID.
     * @return ApiErrorResponse containing HTTP status code and UCM error code (if any).
     * @throws Exception If an I/O error occurs.
     */
    public ApiErrorResponse deleteCredentialById(String userId, String type, String credentialId) throws Exception {

        String url = credentialManagementBasePath + PATH_SEPARATOR + userId + CREDENTIALS_PATH
                + PATH_SEPARATOR + type + PATH_SEPARATOR + credentialId;
        try (CloseableHttpResponse response = getResponseOfHttpDelete(url, getHeadersWithBasicAuth())) {
            int statusCode = response.getStatusLine().getStatusCode();
            String errorCode = statusCode >= 400 ? extractErrorCode(EntityUtils.toString(response.getEntity())) : null;
            return new ApiErrorResponse(statusCode, errorCode);
        }
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException If an error occurred while closing.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }

    private String extractErrorCode(String responseBody) {

        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                return new JSONObject(responseBody).optString("code", null);
            }
        } catch (Exception ignored) {
            // not valid JSON or missing code field
        }
        return null;
    }

    /**
     * Holds the HTTP status code and UCM error code extracted from an API error response body.
     */
    public static class ApiErrorResponse {

        private final int statusCode;
        private final String errorCode;

        public ApiErrorResponse(int statusCode, String errorCode) {

            this.statusCode = statusCode;
            this.errorCode = errorCode;
        }

        public int getStatusCode() {

            return statusCode;
        }

        public String getErrorCode() {

            return errorCode;
        }
    }

    private String buildBasePath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_USERS_V2_PATH;
        }
        return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_USERS_V2_PATH;
    }

    private Header[] getHeadersWithBasicAuth() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));
        return headerList;
    }
}
