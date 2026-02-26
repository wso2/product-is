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

package org.wso2.identity.integration.test.rest.api.user.password.v1;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.ConnectorsPatchReq.OperationEnum;
import org.wso2.identity.integration.test.rest.api.server.identity.governance.v1.dto.PropertyReq;
import org.wso2.identity.integration.test.rest.api.user.password.v1.model.PasswordChangeRequest;
import org.wso2.identity.integration.test.restclients.IdentityGovernanceRestClient;
import org.wso2.identity.integration.test.restclients.RestBaseClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import static org.testng.Assert.assertEquals;

/**
 * Integration tests for verifying all validation and error scenarios of the Password Update API.
 */
public class PasswordUpdateValidationTest extends PasswordUpdateTestBase {

    private final TestUserMode userMode;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PasswordUpdateValidationTest(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    // Base64-encoded category and connector IDs for the password policies governance connector.
    private static final String PASSWORD_POLICIES_CATEGORY_ID = "UGFzc3dvcmQgUG9saWNpZXM";
    private static final String PASSWORD_HISTORY_CONNECTOR_ID = "cGFzc3dvcmRIaXN0b3J5";

    private static final String VALIDATION_USER_1 = "validationTestUser1";
    private static final String VALIDATION_USER_1_PASSWORD = "ValidTest1@123";
    private static final String VALID_NEW_PASSWORD = "ValidNew@123";

    private static final String VALIDATION_USER_2 = "validationTestUser2";
    private static final String VALIDATION_USER_2_PASSWORD = "ValidTest2@123";
    private static final String VALIDATION_USER_2_NEW_PASSWORD_1 = "ValidNew2A@123";
    private static final String VALIDATION_USER_2_NEW_PASSWORD_2 = "ValidNew2B@123";

    private String user1Id;
    private String user2Id;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String user1AccessToken;
    private String user2AccessToken;
    private IdentityGovernanceRestClient governanceRestClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        initBase(userMode);

        ApplicationResponseModel application = createApp("PasswordUpdateValidationTestApp", false);
        appId = application.getId();
        authorizePasswordUpdateScope(appId);

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(appId);
        clientId = oidcConfig.getClientId();
        clientSecret = oidcConfig.getClientSecret();

        // Enable session preservation so tokens remain valid after password changes.
        setPreserveSessionConfig(true);

        // Create test users.
        user1Id = createTestUser(VALIDATION_USER_1, VALIDATION_USER_1_PASSWORD);
        user2Id = createTestUser(VALIDATION_USER_2, VALIDATION_USER_2_PASSWORD);

        governanceRestClient = new IdentityGovernanceRestClient(serverURL, tenantInfo);

        // Obtain access tokens once; they remain valid for the full class run due to session preservation.
        user1AccessToken = getUserAccessToken(clientId, clientSecret, VALIDATION_USER_1,
                VALIDATION_USER_1_PASSWORD, PASSWORD_UPDATE_SCOPE);
        user2AccessToken = getUserAccessToken(clientId, clientSecret, VALIDATION_USER_2,
                VALIDATION_USER_2_PASSWORD, PASSWORD_UPDATE_SCOPE);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        if (user1Id != null) {
            scim2RestClient.deleteUser(user1Id);
        }
        if (user2Id != null) {
            scim2RestClient.deleteUser(user2Id);
        }
        if (governanceRestClient != null) {
            governanceRestClient.closeHttpClient();
        }
        if (appId != null) {
            deleteApp(appId);
        }
        setPreserveSessionConfig(false);
        cleanupBase();
    }

    // ========================
    // Policy & History Validation
    // ========================

    @Test(priority=1, description = "Verify error when current password is wrong")
    public void testWrongCurrentPassword() throws Exception {

        try (CloseableHttpResponse response = changePassword(user1AccessToken, "WrongPassword@123",
                VALID_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for wrong current password.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10001",
                    "Expected error code PWD-10001 for wrong current password.");
        }
    }

    @Test(priority=1, description = "Verify error when new password is the same as the current password")
    public void testSameAsCurrentPassword() throws Exception {

        try (CloseableHttpResponse response = changePassword(user1AccessToken, VALIDATION_USER_1_PASSWORD,
                VALIDATION_USER_1_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 when new password equals current password.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10002",
                    "Expected error code PWD-10002 for same password.");
        }
    }

    @Test(priority=1, description = "Verify error when new password is too short")
    public void testPasswordPolicyViolationTooShort() throws Exception {

        try (CloseableHttpResponse response = changePassword(user1AccessToken, VALIDATION_USER_1_PASSWORD, "Ab@1")) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for password policy violation (too short).");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10003",
                    "Expected error code PWD-10003 for password policy violation.");
        }
    }

    @Test(priority=1, description = "Verify error when new password exceeds the maximum allowed length")
    public void testVeryLongPassword() throws Exception {

        String longPassword = "Abcdefgh".repeat(260) + "@1";

        try (CloseableHttpResponse response = changePassword(user1AccessToken, VALIDATION_USER_1_PASSWORD,
                longPassword)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for very long password.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10003",
                    "Expected error code PWD-10003 for password policy violation.");
        }
    }

    @Test(priority=1, description = "Verify error when an old password cannot be reused due to history policy")
    public void testPasswordHistoryViolation() throws Exception {

        // Password history is explicitly enabled only for this test and disabled in the finally block.
        setPasswordHistoryEnabled(governanceRestClient, true);

        try {
            // First change: initial password → NEW_PASSWORD_1.
            try (CloseableHttpResponse response = changePassword(user2AccessToken, VALIDATION_USER_2_PASSWORD,
                    VALIDATION_USER_2_NEW_PASSWORD_1)) {
                assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                        "Expected 204 for first password change.");
            }

            // Second change: NEW_PASSWORD_1 → NEW_PASSWORD_2.
            try (CloseableHttpResponse response = changePassword(user2AccessToken, VALIDATION_USER_2_NEW_PASSWORD_1,
                    VALIDATION_USER_2_NEW_PASSWORD_2)) {
                assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                        "Expected 204 for second password change.");
            }

            // Attempt to reuse NEW_PASSWORD_1 — must fail because it is already in the history.
            try (CloseableHttpResponse response = changePassword(user2AccessToken, VALIDATION_USER_2_NEW_PASSWORD_2,
                    VALIDATION_USER_2_NEW_PASSWORD_1)) {
                assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                        "Expected 400 when attempting to reuse a previously used password.");
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(responseBody);
                assertEquals(json.getString("code"), "PWD-10003",
                        "Expected error code PWD-10003 for password history violation.");
            }
        } finally {
            setPasswordHistoryEnabled(governanceRestClient, false);
        }
    }

    // ========================
    // Request Validation
    // ========================

    @Test(priority=2, description = "Verify error when currentPassword field is missing from the request body")
    public void testMissingCurrentPasswordField() throws Exception {

        String body = "{\"newPassword\":\"" + VALID_NEW_PASSWORD + "\"}";
        try (CloseableHttpResponse response = changePasswordWithBody(user1AccessToken, body)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for missing currentPassword field.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "UE-10000",
                    "Expected error code UE-10000 for bean validation error.");
        }
    }

    @Test(priority=2, description = "Verify error when newPassword field is missing from the request body")
    public void testMissingNewPasswordField() throws Exception {

        String body = "{\"currentPassword\":\"" + VALIDATION_USER_1_PASSWORD + "\"}";
        try (CloseableHttpResponse response = changePasswordWithBody(user1AccessToken, body)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for missing newPassword field.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "UE-10000",
                    "Expected error code UE-10000 for bean validation error.");
        }
    }

    @Test(priority=2, description = "Verify error when request body is an empty JSON object")
    public void testEmptyJsonBody() throws Exception {

        String body = "{}";
        try (CloseableHttpResponse response = changePasswordWithBody(user1AccessToken, body)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for empty JSON body.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "UE-10000",
                    "Expected error code UE-10000 for bean validation error.");
        }
    }

    @Test(priority=2, description = "Verify error when request body is completely empty")
    public void testEmptyRequestBody() throws Exception {

        String body = "";
        try (CloseableHttpResponse response = changePasswordWithBody(user1AccessToken, body)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_BAD_REQUEST,
                    "Expected 400 for empty request body.");
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(responseBody);
            assertEquals(json.getString("code"), "PWD-10000",
                    "Expected error code PWD-10000 for empty body.");
        }
    }

    // ========================
    // Authentication & Authorization
    // ========================

    @Test(priority=3, description = "Verify error when no Authorization header is provided")
    public void testNoAuthToken() throws Exception {

        try (CloseableHttpResponse response = changePasswordNoAuth(VALIDATION_USER_1_PASSWORD, VALID_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED,
                    "Expected 401 for missing auth token.");
        }
    }

    @Test(priority=3, description = "Verify error when an invalid token is provided")
    public void testInvalidToken() throws Exception {

        try (CloseableHttpResponse response = changePassword("invalid-token-value",
                VALIDATION_USER_1_PASSWORD, VALID_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED,
                    "Expected 401 for invalid token.");
        }
    }

    @Test(priority=3, description = "Verify error when an expired token is used")
    public void testRevokedToken() throws Exception {

        String revokedToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiJleHBpcmVkVXNlciIsImV4cCI6MTAwMDAwMDAwMH0." +
                "invalid-signature-for-revoked-token";

        try (CloseableHttpResponse response = changePassword(revokedToken,
                VALIDATION_USER_1_PASSWORD, VALID_NEW_PASSWORD)) {
            assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED,
                    "Expected 401 for revoked token.");
        }
    }

    @Test(priority=3, description = "Verify error when attempting to change password for a locked account")
    public void testLockedUserPasswordChange() throws Exception {

        setUserAccountLocked(user1Id, true);

        try {
            try (CloseableHttpResponse response = changePassword(user1AccessToken,
                    VALIDATION_USER_1_PASSWORD, VALID_NEW_PASSWORD)) {
                assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED,
                        "Expected 401 when attempting a password change with a locked account.");
            }
        } finally {
            setUserAccountLocked(user1Id, false);
            // Locking the account revokes existing tokens; re-obtain one so that subsequent tests
            // using user1AccessToken receive a valid bearer token.
            user1AccessToken = getUserAccessToken(clientId, clientSecret, VALIDATION_USER_1,
                    VALIDATION_USER_1_PASSWORD, PASSWORD_UPDATE_SCOPE);
        }
    }

    /**
     * Sends a password change request with a raw JSON body, allowing callers to test malformed or
     * incomplete payloads that the standard changePassword helper would never produce.
     *
     * @param accessToken bearer token used in the Authorization header
     * @param body        raw JSON string to use as the request body
     * @return the HTTP response; the caller is responsible for closing it
     * @throws IOException if the HTTP request fails
     */
    private CloseableHttpResponse changePasswordWithBody(String accessToken, String body) throws IOException {

        RestBaseClient restBaseClient = new RestBaseClient();
        Header[] headers = new Header[3];
        headers[0] = new BasicHeader("Authorization", "Bearer " + accessToken);
        headers[1] = new BasicHeader("Content-Type", "application/json");
        headers[2] = new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT);
        return restBaseClient.getResponseOfHttpPost(
                getTenantQualifiedURL(serverURL + CHANGE_PASSWORD_PATH, tenantInfo.getDomain()), body, headers);
    }

    /**
     * Sends a password change request without an Authorization header to verify that the endpoint
     * rejects unauthenticated calls.
     *
     * @param currentPassword the user's current password to include in the request body
     * @param newPassword     the desired new password to include in the request body
     * @return the HTTP response; the caller is responsible for closing it
     * @throws IOException if the HTTP request fails
     */
    private CloseableHttpResponse changePasswordNoAuth(String currentPassword, String newPassword) throws IOException {

        RestBaseClient restBaseClient = new RestBaseClient();
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("Content-Type", "application/json");
        headers[1] = new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT);
        PasswordChangeRequest requestBody = new PasswordChangeRequest()
                .currentPassword(currentPassword)
                .newPassword(newPassword);
        return restBaseClient.getResponseOfHttpPost(
                getTenantQualifiedURL(serverURL + CHANGE_PASSWORD_PATH, tenantInfo.getDomain()),
                restBaseClient.toJSONString(requestBody),
                headers);
    }

    /**
     * Enables or disables the password history governance connector for the tenant.
     *
     * @param governanceClient the client used to call the Identity Governance REST API
     * @param enable           true to enable password history enforcement; false to disable it
     * @throws IOException if the REST call fails
     */
    private void setPasswordHistoryEnabled(IdentityGovernanceRestClient governanceClient, boolean enable)
            throws IOException {

        PropertyReq property = new PropertyReq();
        property.setName("passwordHistory.enable");
        property.setValue(String.valueOf(enable));

        ConnectorsPatchReq connectorPatch = new ConnectorsPatchReq();
        connectorPatch.setOperation(OperationEnum.UPDATE);
        connectorPatch.addProperties(property);

        governanceClient.updateConnectors(PASSWORD_POLICIES_CATEGORY_ID, PASSWORD_HISTORY_CONNECTOR_ID, connectorPatch);
    }

    /**
     * Locks or unlocks a user account via a SCIM2 PATCH operation.
     *
     * @param userId the SCIM2 ID of the user to update
     * @param locked true to lock the account; false to unlock it
     * @throws Exception if the SCIM2 PATCH call fails
     */
    private void setUserAccountLocked(String userId, boolean locked) throws Exception {

        org.json.simple.JSONObject payload = new org.json.simple.JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        org.json.simple.JSONObject wso2Schema = new org.json.simple.JSONObject();
        wso2Schema.put("accountLocked", locked);

        org.json.simple.JSONObject value = new org.json.simple.JSONObject();
        value.put("urn:scim:wso2:schema", wso2Schema);

        org.json.simple.JSONObject op = new org.json.simple.JSONObject();
        op.put("op", "replace");
        op.put("value", value);

        JSONArray operations = new JSONArray();
        operations.add(op);
        payload.put("Operations", operations);

        scim2RestClient.patchUserWithRawJSON(payload.toJSONString(), userId);
    }
}
