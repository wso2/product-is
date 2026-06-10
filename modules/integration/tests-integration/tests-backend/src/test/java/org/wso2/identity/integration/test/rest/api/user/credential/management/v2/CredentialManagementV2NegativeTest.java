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

package org.wso2.identity.integration.test.rest.api.user.credential.management.v2;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.CredentialManagementV2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

/**
 * Integration tests for Credential Management API v2 negative scenarios.
 * Verifies that unsupported operations, invalid types/IDs, and cross-org access
 * are rejected with the correct HTTP error codes.
 */
public class CredentialManagementV2NegativeTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganizationV2Neg";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganizationV2Neg";

    private static final String TEST_USER_USERNAME = "credMgtV2NegTestUser";
    private static final String TEST_USER_PASSWORD = "Admin1234!";
    private static final String NON_EXISTENT_USER_ID = "non-existent-user-v2-99999";
    private static final String NON_EXISTENT_CREDENTIAL_ID = "non-existent-credential-v2-99999";
    private static final String INVALID_CREDENTIAL_TYPE = "INVALID_TYPE_V2";
    private static final String VALID_CREDENTIAL_ID = "valid-credential-id-v2";

    private CredentialManagementV2RestClient credentialManagementV2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private SCIM2RestClient scim2RestClient;
    private String testUserId;
    private String parentOrgId;
    private String secondaryOrgId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CredentialManagementV2NegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        credentialManagementV2RestClient = new CredentialManagementV2RestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, context.getContextTenant(), serverURL,
                getAuthorizedAPIList());
        testUserId = scim2RestClient.createUser(new UserObject().userName(TEST_USER_USERNAME)
                .password(TEST_USER_PASSWORD));
        createOrganizationHierarchy();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        if (testUserId != null) {
            scim2RestClient.deleteUser(testUserId);
        }
        deleteOrganizationHierarchy();
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (credentialManagementV2RestClient != null) {
            credentialManagementV2RestClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
        super.testConclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(description = "POST /credentials/passkey must return 400 — passkey creation is not supported in v2")
    public void testCreatePasskeyUnsupported() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.createCredentialByType(testUserId, TYPE_PASSKEY);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Creating a passkey via POST should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60005",
                "Creating a passkey via POST should return error code UCM-60005");
    }

    @Test(description = "POST /credentials/push-auth must return 400 — push-auth creation is not supported in v2")
    public void testCreatePushAuthUnsupported() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.createCredentialByType(testUserId, TYPE_PUSH_AUTH);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Creating a push-auth credential via POST should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60005",
                "Creating a push-auth credential via POST should return error code UCM-60005");
    }

    @Test(description = "POST /credentials/{invalid-type} must return 400 for an unrecognized type")
    public void testCreateCredentialWithInvalidType() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.createCredentialByType(testUserId, INVALID_CREDENTIAL_TYPE);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "Creating a credential with an invalid type should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60002",
                "Creating a credential with an invalid type should return error code UCM-60002");
    }

    @Test(description = "DELETE /credentials/passkey must return 400 — deleting passkey by type is not supported in v2")
    public void testDeletePasskeyByTypeUnsupported() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialsByType(testUserId, TYPE_PASSKEY);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "DELETE /credentials/passkey (by type) should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60006",
                "DELETE /credentials/passkey (by type) should return error code UCM-60006");
    }

    @Test(description = "DELETE /credentials/push-auth must return 400 — deleting push-auth by type is not supported in v2")
    public void testDeletePushAuthByTypeUnsupported() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialsByType(testUserId, TYPE_PUSH_AUTH);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "DELETE /credentials/push-auth (by type) should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60006",
                "DELETE /credentials/push-auth (by type) should return error code UCM-60006");
    }

    @Test(description = "DELETE /credentials/{invalid-type} must return 400 for an unrecognized type")
    public void testDeleteCredentialsByTypeWithInvalidType() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialsByType(testUserId, INVALID_CREDENTIAL_TYPE);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "DELETE /credentials with invalid type should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60002",
                "DELETE /credentials with invalid type should return error code UCM-60002");
    }

    @Test(description = "DELETE /credentials/backup-code/{id} must return 400 — backup-code does not support delete by ID")
    public void testDeleteBackupCodeByIdUnsupported() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialById(
                        testUserId, TYPE_BACKUP_CODE, VALID_CREDENTIAL_ID);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "DELETE /credentials/backup-code/{id} should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60007",
                "DELETE /credentials/backup-code/{id} should return error code UCM-60007");
    }

    @Test(description = "DELETE /credentials/passkey/{id} with a non-existent credential ID returns 400 or 404")
    public void testDeletePasskeyWithNonExistentCredentialId() throws Exception {

        int status = credentialManagementV2RestClient.deleteCredentialById(
                        testUserId, TYPE_PASSKEY, NON_EXISTENT_CREDENTIAL_ID).getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_BAD_REQUEST || status == HttpStatus.SC_NOT_FOUND,
                "DELETE passkey with non-existent credential ID should return 400 or 404, got: " + status);
    }

    @Test(description = "DELETE /credentials/{type}/{id} with an invalid type must return 400")
    public void testDeleteCredentialByIdWithInvalidType() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialById(
                        testUserId, INVALID_CREDENTIAL_TYPE, VALID_CREDENTIAL_ID);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_BAD_REQUEST,
                "DELETE credential by ID with invalid type should return 400");
        Assert.assertEquals(response.getErrorCode(), "UCM-60002",
                "DELETE credential by ID with invalid type should return error code UCM-60002");
    }

    @Test(description = "GET /credentials for a non-existent user must return 404")
    public void testGetCredentialsForNonExistentUser() throws Exception {

        try {
            credentialManagementV2RestClient.getUserCredentials(NON_EXISTENT_USER_ID);
            Assert.fail("Expected an exception for non-existent user, but none was thrown");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("not found"),
                    "GET credentials for non-existent user should yield a 404, got: " + e.getMessage());
        }
    }

    @Test(description = "POST /credentials/backup-code for a non-existent user must return 404")
    public void testCreateBackupCodeForNonExistentUser() throws Exception {

        try {
            credentialManagementV2RestClient.createBackupCode(NON_EXISTENT_USER_ID);
            Assert.fail("Expected an exception for non-existent user, but none was thrown");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("not found"),
                    "POST backup-code for non-existent user should yield a 404, got: " + e.getMessage());
        }
    }

    @Test(description = "Admin from org A cannot GET credentials of a user from org B (cross-org isolation)")
    public void testCrossOrganizationGetCredentialsIsolation() throws Exception {

        try {
            credentialManagementV2RestClient.getUserCredentials(SECONDARY_ORG_USER_ID);
            Assert.fail("Cross-org GET credentials must not succeed; expected 403 or 404 but the call returned successfully");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("403"),
                    "Cross-org GET credentials should yield 403 or 404, got: " + e.getMessage());
        }
    }

    @Test(description = "Admin from org A cannot DELETE credentials of a user from org B (cross-org isolation)")
    public void testCrossOrganizationDeleteCredentialIsolation() throws Exception {

        CredentialManagementV2RestClient.ApiErrorResponse response =
                credentialManagementV2RestClient.deleteCredentialById(
                        SECONDARY_ORG_USER_ID, TYPE_PASSKEY, TEST_PASSKEY_CREDENTIAL_ID);
        int status = response.getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_BAD_REQUEST
                        || status == HttpStatus.SC_FORBIDDEN,
                "Cross-org DELETE should not succeed; expected 403/404/400, got: " + status);
    }

    private void createOrganizationHierarchy() throws Exception {

        parentOrgId = orgMgtRestClient.addOrganization(PARENT_ORG_NAME);
        secondaryOrgId = orgMgtRestClient.addOrganization(SECONDARY_ORG_NAME);
    }

    private void deleteOrganizationHierarchy() throws Exception {

        if (orgMgtRestClient != null) {
            if (parentOrgId != null) {
                orgMgtRestClient.deleteOrganization(parentOrgId);
            }
            if (secondaryOrgId != null) {
                orgMgtRestClient.deleteOrganization(secondaryOrgId);
            }
        }
    }
}
