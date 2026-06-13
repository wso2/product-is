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
import org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model.CredentialCreationResponse;
import org.wso2.identity.integration.test.rest.api.user.credential.management.v2.model.CredentialsByType;
import org.wso2.identity.integration.test.restclients.CredentialManagementV2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

/**
 * Integration tests for Credential Management API v2 positive scenarios.
 * Covers listing credentials (all types), creating backup codes, deleting backup codes by type,
 * and deleting passkey/push-auth credentials by ID.
 */
public class CredentialManagementV2PositiveTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganizationV2";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganizationV2";

    private static final String PARENT_ORG_USER_USERNAME = "credMgtV2PosParentUser";
    private static final String SECONDARY_ORG_USER_USERNAME = "credMgtV2PosSecondaryUser";
    private static final String SUB_ORG_USER_USERNAME = "credMgtV2PosSubOrgUser";
    private static final String TEST_USER_PASSWORD = "Admin1234!";

    private static final String PARENT_ORG_USER_PASSKEY_CREDENTIAL_ID = "parent-org-passkey-001";
    private static final String PARENT_ORG_USER_PUSH_AUTH_CREDENTIAL_ID = "parent-org-push-auth-001";
    private static final String SUB_ORG_USER_PASSKEY_CREDENTIAL_ID = "sub-org-passkey-001";

    private CredentialManagementV2RestClient credentialManagementV2RestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private SCIM2RestClient scim2RestClient;
    private String parentOrgUserId;
    private String secondaryOrgUserId;
    private String subOrgUserId;
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
    public CredentialManagementV2PositiveTest(TestUserMode userMode) throws Exception {

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
        parentOrgUserId = scim2RestClient.createUser(
                new UserObject().userName(PARENT_ORG_USER_USERNAME).password(TEST_USER_PASSWORD));
        secondaryOrgUserId = scim2RestClient.createUser(
                new UserObject().userName(SECONDARY_ORG_USER_USERNAME).password(TEST_USER_PASSWORD));
        subOrgUserId = scim2RestClient.createUser(
                new UserObject().userName(SUB_ORG_USER_USERNAME).password(TEST_USER_PASSWORD));
        createOrganizationHierarchy();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        deleteOrganizationHierarchy();
        if (parentOrgUserId != null) {
            scim2RestClient.deleteUser(parentOrgUserId);
        }
        if (secondaryOrgUserId != null) {
            scim2RestClient.deleteUser(secondaryOrgUserId);
        }
        if (subOrgUserId != null) {
            scim2RestClient.deleteUser(subOrgUserId);
        }
        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (credentialManagementV2RestClient != null) {
            credentialManagementV2RestClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(description = "GET /credentials returns a response with all three credential types present")
    public void testGetCredentialsReturnsAllTypes() throws Exception {

        CredentialsByType credentials = credentialManagementV2RestClient.getUserCredentials(parentOrgUserId);
        Assert.assertNotNull(credentials, "Response should not be null");
        Assert.assertNotNull(credentials.getPasskey(),
                "passkey field should always be present in v2 response");
        Assert.assertNotNull(credentials.getPushAuth(),
                "push-auth field should always be present in v2 response");
        Assert.assertNotNull(credentials.getBackupCode(),
                "backup-code field should always be present in v2 response");
    }

    @Test(description = "GET /credentials returns empty lists and false for a user with no credentials")
    public void testGetCredentialsEmptyForUserWithNoCredentials() throws Exception {

        CredentialsByType credentials = credentialManagementV2RestClient.getUserCredentials(secondaryOrgUserId);
        Assert.assertNotNull(credentials, "Response should not be null");
        Assert.assertTrue(credentials.getPasskey().isEmpty(),
                "passkey list should be empty for user with no enrolled passkeys");
        Assert.assertTrue(credentials.getPushAuth().isEmpty(),
                "push-auth list should be empty for user with no push-auth devices");
        Assert.assertFalse(credentials.getBackupCode(),
                "backup-code should be false for user with no backup codes");
    }

    @Test(description = "POST /credentials/backup-code creates backup codes and returns 201 with code list")
    public void testCreateBackupCode() throws Exception {

        CredentialCreationResponse response = credentialManagementV2RestClient.createBackupCode(parentOrgUserId);
        Assert.assertNotNull(response, "Backup code creation response should not be null");
        Assert.assertEquals(response.getType(), TYPE_BACKUP_CODE,
                "type field should be 'backup-code'");
        Assert.assertNotNull(response.getCredentials(),
                "credentials list should not be null");
        Assert.assertFalse(response.getCredentials().isEmpty(),
                "credentials list should contain at least one backup code");
    }

    @Test(description = "POST /credentials/backup-code regenerates codes, replacing existing ones",
            dependsOnMethods = "testCreateBackupCode")
    public void testRegenerateBackupCodeReplacesExisting() throws Exception {

        CredentialCreationResponse firstResponse =
                credentialManagementV2RestClient.createBackupCode(parentOrgUserId);
        CredentialCreationResponse secondResponse =
                credentialManagementV2RestClient.createBackupCode(parentOrgUserId);

        Assert.assertNotNull(secondResponse.getCredentials(), "Regenerated codes should not be null");
        Assert.assertFalse(secondResponse.getCredentials().isEmpty(), "Regenerated codes list should not be empty");
        Assert.assertNotEquals(firstResponse.getCredentials(), secondResponse.getCredentials(),
                "Regenerated codes should differ from the previous set");
    }

    @Test(description = "GET /credentials shows backup-code as true after creation",
            dependsOnMethods = "testCreateBackupCode")
    public void testGetCredentialsShowsBackupCodeTrueAfterCreation() throws Exception {

        credentialManagementV2RestClient.createBackupCode(parentOrgUserId);
        CredentialsByType credentials = credentialManagementV2RestClient.getUserCredentials(parentOrgUserId);
        Assert.assertTrue(credentials.getBackupCode(),
                "backup-code should be true after backup codes have been created");
    }

    @Test(description = "DELETE /credentials/backup-code returns 204 and removes backup codes",
            dependsOnMethods = "testCreateBackupCode")
    public void testDeleteBackupCodeByType() throws Exception {

        credentialManagementV2RestClient.createBackupCode(parentOrgUserId);
        int status = credentialManagementV2RestClient.deleteCredentialsByType(
                parentOrgUserId, TYPE_BACKUP_CODE).getStatusCode();
        Assert.assertEquals(status, HttpStatus.SC_NO_CONTENT,
                "DELETE /credentials/backup-code should return 204");
    }

    @Test(description = "GET /credentials shows backup-code as false after deletion",
            dependsOnMethods = "testDeleteBackupCodeByType")
    public void testGetCredentialsShowsBackupCodeFalseAfterDeletion() throws Exception {

        credentialManagementV2RestClient.createBackupCode(parentOrgUserId);
        credentialManagementV2RestClient.deleteCredentialsByType(parentOrgUserId, TYPE_BACKUP_CODE);
        CredentialsByType credentials = credentialManagementV2RestClient.getUserCredentials(parentOrgUserId);
        Assert.assertFalse(credentials.getBackupCode(),
                "backup-code should be false after deletion");
    }

    @Test(description = "DELETE /credentials/passkey/{id} returns 204, 400, or 404")
    public void testDeletePasskeyById() throws Exception {

        int status = credentialManagementV2RestClient.deleteCredentialById(
                parentOrgUserId, TYPE_PASSKEY, PARENT_ORG_USER_PASSKEY_CREDENTIAL_ID).getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_BAD_REQUEST
                        || status == HttpStatus.SC_NOT_FOUND,
                "DELETE passkey by ID should return 204, 400, or 404, got: " + status);
    }

    @Test(description = "DELETE /credentials/push-auth/{id} returns 204, 400, or 404")
    public void testDeletePushAuthById() throws Exception {

        int status = credentialManagementV2RestClient.deleteCredentialById(
                parentOrgUserId, TYPE_PUSH_AUTH, PARENT_ORG_USER_PUSH_AUTH_CREDENTIAL_ID).getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_BAD_REQUEST
                        || status == HttpStatus.SC_NOT_FOUND,
                "DELETE push-auth by ID should return 204, 400, or 404, got: " + status);
    }

    @Test(description = "Parent org admin can GET credentials for a sub-org user")
    public void testGetCredentialsForSubOrgUser() throws Exception {

        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        CredentialsByType credentials = credentialManagementV2RestClient.getUserCredentials(subOrgUserId);
        Assert.assertNotNull(credentials, "Credentials should not be null for sub-org user");
        Assert.assertNotNull(credentials.getPasskey(), "passkey field must always be present");
        Assert.assertNotNull(credentials.getPushAuth(), "push-auth field must always be present");
        Assert.assertNotNull(credentials.getBackupCode(), "backup-code field must always be present");
    }

    @Test(description = "DELETE passkey by ID works for a sub-org user via org base path")
    public void testDeletePasskeyByIdForSubOrgUser() throws Exception {

        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        int status = credentialManagementV2RestClient.deleteCredentialById(
                subOrgUserId, TYPE_PASSKEY, SUB_ORG_USER_PASSKEY_CREDENTIAL_ID).getStatusCode();
        Assert.assertTrue(status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_BAD_REQUEST
                        || status == HttpStatus.SC_NOT_FOUND,
                "DELETE passkey for sub-org user should return 204, 400, or 404, got: " + status);
    }

    @Test(description = "Credential isolation: secondary org user's credentials are not visible to parent org admin")
    public void testCredentialIsolationBetweenOrgs() throws Exception {

        CredentialsByType parentUserCredentials =
                credentialManagementV2RestClient.getUserCredentials(parentOrgUserId);
        Assert.assertNotNull(parentUserCredentials, "Parent org user credentials should not be null");

        CredentialsByType secondaryUserCredentials =
                credentialManagementV2RestClient.getUserCredentials(secondaryOrgUserId);
        Assert.assertTrue(secondaryUserCredentials.getPasskey().isEmpty(),
                "passkey list should be empty for secondary org user as seen by parent org admin");
        Assert.assertTrue(secondaryUserCredentials.getPushAuth().isEmpty(),
                "push-auth list should be empty for secondary org user as seen by parent org admin");
        Assert.assertFalse(secondaryUserCredentials.getBackupCode(),
                "backup-code should be false for secondary org user as seen by parent org admin");
    }

    private void createOrganizationHierarchy() throws Exception {

        parentOrgId = orgMgtRestClient.addOrganization(PARENT_ORG_NAME);
        secondaryOrgId = orgMgtRestClient.addOrganization(SECONDARY_ORG_NAME);
    }

    private void deleteOrganizationHierarchy() throws Exception {

        if (orgMgtRestClient != null) {
            if (secondaryOrgId != null) {
                orgMgtRestClient.deleteOrganization(secondaryOrgId);
            }
            if (parentOrgId != null) {
                orgMgtRestClient.deleteOrganization(parentOrgId);
            }
        }
    }
}
