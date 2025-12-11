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

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.credential.management.v1.model.Credential;
import org.wso2.identity.integration.test.restclients.CredentialManagementRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

/**
 * Integration tests for Credential Management API positive scenarios.
 * Tests the admin APIs for listing and deleting end user enrolled credentials like push auth devices and passkeys.
 */
public class CredentialManagementPositiveTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganization";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganization";
    private static final String PARENT_ORG_USER_PASSKEY_CREDENTIAL_ID = "parent-org-passkey-001";
    private static final String PARENT_ORG_USER_PUSH_AUTH_CREDENTIAL_ID = "parent-org-push-auth-001";
    private static final String SUB_ORG_USER_PASSKEY_CREDENTIAL_ID = "sub-org-passkey-001";
    private static final String SUB_ORG_USER_PUSH_AUTH_CREDENTIAL_ID = "sub-org-pushauth-001";

    private CredentialManagementRestClient credentialManagementRestClient;
    private OrgMgtRestClient orgMgtRestClient;
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
    public CredentialManagementPositiveTest(TestUserMode userMode) throws Exception {

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

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @Test(description = "Test get credentials for parent org user")
    public void testGetCredentialsForParentOrgUser() throws Exception {

        Credential[] credentials = credentialManagementRestClient.getUserCredentials(PARENT_ORG_USER_ID);
        Assert.assertNotNull(credentials, "Credentials array should not be null");
    }

    @Test(description = "Test delete passkey credential for parent org user")
    public void testDeletePasskeyCredentialForParentOrgUser() throws Exception {

        credentialManagementRestClient.deleteUserCredential(PARENT_ORG_USER_ID, TYPE_PASSKEY,
                PARENT_ORG_USER_PASSKEY_CREDENTIAL_ID);
    }

    @Test(description = "Test delete push authentication credential for parent org user")
    public void testDeletePushAuthCredentialForParentOrgUser() throws Exception {

        credentialManagementRestClient.deleteUserCredential(PARENT_ORG_USER_ID, TYPE_PUSH_AUTH,
                PARENT_ORG_USER_PUSH_AUTH_CREDENTIAL_ID);
    }

    @Test(description = "Test delete passkey credential for sub-org user")
    public void testDeletePasskeyCredentialForSubOrgUser() throws Exception {

        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        credentialManagementRestClient.deleteUserCredential(SUB_ORG_USER_ID, TYPE_PASSKEY,
                SUB_ORG_USER_PASSKEY_CREDENTIAL_ID);
    }

    @Test(description = "Test delete push-auth credential for sub-org user")
    public void testDeletePushAuthCredentialForSubOrgUser() throws Exception {

        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        credentialManagementRestClient.deleteUserCredential(SUB_ORG_USER_ID, TYPE_PASSKEY,
                SUB_ORG_USER_PUSH_AUTH_CREDENTIAL_ID);
    }

    @Test(description = "Test that credentials from different orgs are isolated")
    public void testCredentialIsolationBetweenOrgs() throws Exception {

        // Get credentials for parent org user
        Credential[] credentials = credentialManagementRestClient.getUserCredentials(PARENT_ORG_USER_ID);
        Assert.assertNotNull(credentials, "Credentials array should not be null");

        // Get credentials for secondary org user
        Credential[] secondaryUserCredentials = credentialManagementRestClient.getUserCredentials(SECONDARY_ORG_USER_ID);
        Assert.assertEquals(secondaryUserCredentials.length, 0,
                "Should not return credentials from secondary organization");
    }

    @Test(description = "Test that parent org admin can view sub-org user credentials within same tenant")
    public void testGetCredentialsForSubOrgUser() throws Exception {

        RestAssured.basePath = convertToOrgBasePath(this.basePath);
        Credential[] subOrgUserCredentials = credentialManagementRestClient.getUserCredentials(SUB_ORG_USER_ID);
        Assert.assertNotNull(subOrgUserCredentials, "Credentials array should not be null");
    }

    /**
     * Create organization hierarchy for testing.
     * Creates: Parent Org -> Sub Org, and Secondary Org (separate)
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
            // Delete in reverse order
            if (secondaryOrgId != null) {
                orgMgtRestClient.deleteOrganization(secondaryOrgId);
            }
            if (parentOrgId != null) {
                orgMgtRestClient.deleteOrganization(parentOrgId);
            }
        }
    }
}
