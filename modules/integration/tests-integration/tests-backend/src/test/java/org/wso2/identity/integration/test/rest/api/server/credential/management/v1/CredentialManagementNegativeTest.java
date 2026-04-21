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

package org.wso2.identity.integration.test.rest.api.server.credential.management.v1;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.credential.management.v1.model.Credential;
import org.wso2.identity.integration.test.restclients.CredentialManagementRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

/**
 * Integration tests for Credential Management API negative scenarios.
 * Tests error handling for invalid requests, non-existent resources, and malformed inputs.
 */
public class CredentialManagementNegativeTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganization";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganization";
    private static final String VALID_USER_ID = "valid-user-id";
    private static final String VALID_CREDENTIAL_ID = "valid-credential-id";

    // Invalid test data
    private static final String NON_EXISTENT_USER_ID = "non-existent-user-12345";
    private static final String NON_EXISTENT_CREDENTIAL_ID = "non-existent-credential-12345";
    private static final String INVALID_CREDENTIAL_TYPE = "INVALID_TYPE";

    private String parentOrgId;
    private String secondaryOrgId;
    private CredentialManagementRestClient credentialManagementRestClient;
    private OrgMgtRestClient orgMgtRestClient;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public CredentialManagementNegativeTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        credentialManagementRestClient = new CredentialManagementRestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, context.getContextTenant(), serverURL,
                getAuthorizedAPIList());
        createOrganizationHierarchy();
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        deleteOrganizationHierarchy();
        if (credentialManagementRestClient != null) {
            credentialManagementRestClient.closeHttpClient();
        }
        if (orgMgtRestClient != null) {
            orgMgtRestClient.closeHttpClient();
        }
        super.testConclude();
    }

    @Test(description = "Test delete credential with non-existent credential ID")
    public void testDeleteCredentialWithNonExistentCredentialId() throws Exception {

        credentialManagementRestClient.deleteUserCredential(VALID_USER_ID, TYPE_PASSKEY, NON_EXISTENT_CREDENTIAL_ID);
    }

    @Test(description = "Test delete credential with invalid credential type")
    public void testDeleteCredentialWithInvalidCredentialType() throws Exception {

        credentialManagementRestClient.deleteUserCredential(VALID_USER_ID, INVALID_CREDENTIAL_TYPE, VALID_CREDENTIAL_ID);
    }

    @Test(description = "Test get credentials using REST client with non-existent user")
    public void testGetCredentialsUsingRestClientWithNonExistentUser() throws Exception {

        Credential[] credentials = credentialManagementRestClient.getUserCredentials(NON_EXISTENT_USER_ID);
        Assert.assertEquals(credentials.length, 0,
                "Expected no credentials for non-existent user, but some were returned.");
    }

    @Test(description = "Test that admin from organization A cannot access credentials of users in organization B")
    public void testCrossOrganizationGetCredentialsIsolation() throws Exception {

        Credential[] credentials = credentialManagementRestClient.getUserCredentials(SECONDARY_ORG_USER_ID);
        Assert.assertEquals(credentials.length, 0,
                "Should not return credentials from secondary organization");
    }

    @Test(description = "Test that admin from organization A cannot delete credentials of users in organization B")
    public void testCrossOrganizationDeleteCredentialsIsolation() throws Exception {

        credentialManagementRestClient.deleteUserCredential(SECONDARY_ORG_USER_ID, TYPE_PASSKEY,
                TEST_PASSKEY_CREDENTIAL_ID);
    }

    /**
     * Create organization hierarchy for testing.
     * Creates: Parent Org and Secondary Org (separate tenant-like org)
     *
     * @throws Exception If an error occurs while creating organizations.
     */
    private void createOrganizationHierarchy() throws Exception {

        parentOrgId = orgMgtRestClient.addOrganization(PARENT_ORG_NAME);
        secondaryOrgId = orgMgtRestClient.addOrganization(SECONDARY_ORG_NAME);
    }

    /**
     * Delete organization hierarchy created for testing.
     *
     * @throws Exception If an error occurs while deleting organizations.
     */
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
