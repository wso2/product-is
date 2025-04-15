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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.recovery.model.v2.ConfirmModel;
import org.wso2.identity.integration.test.recovery.model.v2.InitModel;
import org.wso2.identity.integration.test.recovery.model.v2.RecoverModel;
import org.wso2.identity.integration.test.recovery.model.v2.ResetModel;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.Error;

import java.io.IOException;

/**
 * Client class for interacting with the WSO2 Identity Server's
 * Password Recovery V2 REST API endpoints.
 * This client is responsible for initiating, handling, and confirming
 * password recovery flows using the API exposed under `/api/users/v2/recovery/password`.
 */
public class PasswordRecoveryV2RestClient extends RestBaseClient {

    private static final String V2_PASSWORD_RECOVERY_PATH = "api/users/v2/recovery/password";
    private static final String CHANNEL_INFO = "channelInfo";
    private static final String FLOW_CONFIRMATION_CODE = "flowConfirmationCode";

    private final String username;
    private final String password;
    private final String v2PasswordRecoveryBasePath;

    private final ObjectMapper mapper;

    public PasswordRecoveryV2RestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        v2PasswordRecoveryBasePath = generateV2PasswordRecoveryBasePath(serverUrl, tenantDomain);
        mapper = new ObjectMapper();
    }

    /**
     * Initiates the password recovery flow by posting the given user claims.
     *
     * @param initModel   The initialization request body containing user claims.
     * @param channelType The preferred channel type (e.g., "SMS").
     * @return RecoverModel containing recoveryCode and matching channelId.
     * @throws IOException If an error occurs during HTTP communication or JSON parsing.
     */
    public RecoverModel init(InitModel initModel, String channelType) throws IOException {

        String endPointUrl = v2PasswordRecoveryBasePath + "/init";
        String jsonRequestBody = toJSONString(initModel);

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {

            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response status for password recovery initialization: " + status);
            }

            JsonNode root = mapper.readTree(response.getEntity().getContent());
            if (root == null || !root.isArray() || root.isEmpty()) {
                throw new IllegalStateException("Recovery API returned an empty response.");
            }

            JsonNode firstMode = root.get(0);
            JsonNode channelInfo = firstMode.get(CHANNEL_INFO);
            if (channelInfo == null || !channelInfo.has("recoveryCode") || !channelInfo.has("channels")) {
                throw new IllegalStateException("Response missing expected channel information.");
            }

            String recoveryCode = channelInfo.get("recoveryCode").asText();
            for (JsonNode channel : channelInfo.get("channels")) {
                if (channelType.equalsIgnoreCase(channel.get("type").asText())) {
                    String channelId = channel.get("id").asText();
                    return new RecoverModel()
                            .channelId(channelId)
                            .recoveryCode(recoveryCode);
                }
            }

            throw new IllegalStateException("No channel of type " + channelType + " found in the response.");
        }
    }

    /**
     * Calls the password recovery endpoint using the provided recovery code and channel ID,
     * and returns the flowConfirmationCode if available.
     *
     * @param recoverModel Model containing recoveryCode and channelId.
     * @return The flowConfirmationCode returned by the server, or null if not present.
     * @throws IOException If the request fails or response parsing fails.
     */
    public String recover(RecoverModel recoverModel) throws IOException {

        String jsonRequestBody = toJSONString(recoverModel);
        String endPointUrl = v2PasswordRecoveryBasePath + "/recover";

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {

            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK && status != HttpStatus.SC_ACCEPTED) {
                throw new IOException("Unexpected response status for password recover call: " + status);
            }

            JsonNode root = mapper.readTree(response.getEntity().getContent());
            if (root != null && root.has(FLOW_CONFIRMATION_CODE)) {
                return root.get(FLOW_CONFIRMATION_CODE).asText();
            }
        }

        return null;
    }

    /**
     * Confirms the password recovery using the confirmation code or OTP,
     * and returns the resetCode if the confirmation is successful.
     *
     * @param confirmModel Model containing confirmationCode and optional OTP.
     * @return The resetCode returned by the server, or null if not present.
     * @throws IOException If the request fails or the response is invalid.
     */
    public String confirm(ConfirmModel confirmModel) throws IOException {

        String jsonRequestBody = toJSONString(confirmModel);
        String endPointUrl = v2PasswordRecoveryBasePath + "/confirm";

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {

            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response status for password confirm: " + status);
            }

            JsonNode root = mapper.readTree(response.getEntity().getContent());
            if (root != null && root.has("resetCode")) {
                return root.get("resetCode").asText();
            }
        }

        return null;
    }

    /**
     * Executes the password reset operation using the provided reset code and new password.
     *
     * @param resetModel The reset request containing resetCode, password, and flowConfirmationCode.
     * @return An ErrorResponse if the reset fails, or null if successful.
     * @throws IOException If the request fails or response can't be parsed.
     */
    public Error reset(ResetModel resetModel) throws IOException {

        String jsonRequestBody = toJSONString(resetModel);
        String endPointUrl = v2PasswordRecoveryBasePath + "/reset";

        try (CloseableHttpResponse response = getResponseOfHttpPost(endPointUrl, jsonRequestBody, getHeaders())) {

            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return null;
            } else {
                return mapper.readValue(response.getEntity().getContent(), Error.class);
            }
        }
    }

    private String generateV2PasswordRecoveryBasePath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + V2_PASSWORD_RECOVERY_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + V2_PASSWORD_RECOVERY_PATH;
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    public void closeHttpClient() throws IOException {

        client.close();
    }
}
