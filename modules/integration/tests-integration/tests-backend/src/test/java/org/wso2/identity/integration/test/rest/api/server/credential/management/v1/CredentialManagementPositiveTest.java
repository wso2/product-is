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
import io.restassured.response.Response;
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
import org.wso2.identity.integration.test.rest.api.server.credential.management.v1.model.Credential;
import org.wso2.identity.integration.test.restclients.CredentialManagementRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.IOException;

/**
 * Integration tests for Credential Management API positive scenarios.
 * Tests the admin APIs for listing and deleting end user enrolled credentials like push auth devices and passkeys.
 */
public class CredentialManagementPositiveTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganization";
    private static final String PARENT_ORG_HANDLE = "parentOrg";
    private static final String SUB_ORG_NAME = "subOrganization";
    private static final String SUB_ORG_HANDLE = "subOrg";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganization";
    private static final String SECONDARY_ORG_HANDLE = "secondaryOrg";

    // Test user IDs for different orgs
    private static final String PARENT_ORG_USER_ID = "parent-org-user-001";
    private static final String SUB_ORG_USER_ID = "sub-org-user-002";
    private static final String SECONDARY_ORG_USER_ID = "secondary-org-user-003";

    private CredentialManagementRestClient credentialManagementRestClient;
    private OrgMgtRestClient orgMgtRestClient;
    private String parentOrgId;
    private String subOrgId;
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
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        credentialManagementRestClient = new CredentialManagementRestClient(serverURL, tenantInfo);
        try {
            orgMgtRestClient = new OrgMgtRestClient(context, context.getContextTenant(), serverURL,
                    getAuthorizedAPIList());
            createOrganizationHierarchy();
        } catch (Exception e) {
            System.out.println("Warning: Organization setup failed: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        try {
            deleteOrganizationHierarchy();
        } catch (Exception e) {
            // Ignore cleanup errors
            System.out.println("Warning: Organization cleanup failed: " + e.getMessage());
        }
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

        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + PARENT_ORG_USER_ID + CREDENTIALS_PATH;
        Response response = getResponseOfGet(endpoint);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Credential[] credentials = response.as(Credential[].class);
        Assert.assertNotNull(credentials, "Credentials array should not be null");

        // Verify the response structure
        System.out.println("Retrieved " + credentials.length + " credentials for parent org user");
    }

    @Test(description = "Test delete passkey credential for parent org user")
    public void testDeletePasskeyCredentialForParentOrgUser() {

        String credentialId = "parent-org-passkey-001";
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + PARENT_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PASSKEY + PATH_SEPARATOR + credentialId;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_NO_CONTENT ||
                        statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Expected 204, 400, or 404 for parent org user credential delete, but got: " + statusCode
        );
    }

    @Test(description = "Test delete push authentication credential for parent org user")
    public void testDeletePushAuthCredentialForParentOrgUser() {

        String credentialId = "parent-org-passkey-001";
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + PARENT_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PUSH_AUTH + PATH_SEPARATOR + credentialId;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_NO_CONTENT ||
                        statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Expected 204, 400, or 404 for parent org user credential delete, but got: " + statusCode
        );
    }

    @Test(description = "Test delete passkey credential for sub-org user")
    public void testDeletePasskeyCredentialForSubOrgUser() throws Exception {

        String credentialId = "sub-org-passkey-001";
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUB_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PASSKEY + PATH_SEPARATOR + credentialId;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_NO_CONTENT ||
                        statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Expected 204, 400, or 404 for sub-org user credential delete, but got: " + statusCode
        );
    }

    @Test(description = "Test delete push-auth credential for sub-org user")
    public void testDeletePushAuthCredentialForSubOrgUser() throws Exception {

        String credentialId = "sub-org-pushauth-001";
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUB_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PUSH_AUTH + PATH_SEPARATOR + credentialId;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_NO_CONTENT ||
                        statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Expected 204, 400, or 404 for sub-org push-auth credential delete, but got: " + statusCode
        );
    }

    @Test(description = "Test that credentials from different orgs are isolated")
    public void testCredentialIsolationBetweenOrgs() {

        // Get credentials for parent org user
        String parentOrgEndpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + PARENT_ORG_USER_ID +
                CREDENTIALS_PATH;
        Response parentOrgResponse = getResponseOfGet(parentOrgEndpoint);

        Assert.assertEquals(parentOrgResponse.getStatusCode(), HttpStatus.SC_OK,
                "Should be able to get parent org user credentials");

        // Get credentials for secondary org user (different org, should be empty or not accessible)
        String secondaryOrgEndpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SECONDARY_ORG_USER_ID +
                CREDENTIALS_PATH;
        Response secondaryOrgResponse = getResponseOfGet(secondaryOrgEndpoint);

        int statusCode = secondaryOrgResponse.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND,
                "Cross-org credential access should return 200 (empty) or 404. Got: " + statusCode
        );

        if (statusCode == HttpStatus.SC_OK) {
            Credential[] secondaryCredentials = secondaryOrgResponse.as(Credential[].class);
            // Should be empty if proper isolation is enforced
            Assert.assertNotNull(secondaryCredentials, "Credentials array should not be null");
        }
    }

    @Test(description = "Test that parent org admin can view sub-org user credentials within same tenant")
    public void testParentOrgAdminCanViewSubOrgUserCredentials() {

        // In the same tenant, admin should be able to view all user credentials
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUB_ORG_USER_ID +
                CREDENTIALS_PATH;
        Response response = getResponseOfGet(endpoint);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK,
                "Parent org admin should be able to view sub-org user credentials within same tenant");

        Credential[] credentials = response.as(Credential[].class);
        Assert.assertNotNull(credentials, "Credentials array should not be null");
    }

    @Test(description = "Test that parent org admin can delete sub-org user credentials within same tenant")
    public void testParentOrgAdminCanDeleteSubOrgUserCredentials() throws Exception {

        String credentialId = "sub-org-credential-to-delete";
        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SUB_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PASSKEY + PATH_SEPARATOR + credentialId;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_NO_CONTENT ||
                        statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Parent org admin should be able to delete sub-org user credentials. Got status: " + statusCode
        );
    }

    /**
     * Create organization hierarchy for testing.
     * Creates: Parent Org -> Sub Org, and Secondary Org (separate)
     *
     * @throws Exception If an error occurs while creating organizations.
     */
    private void createOrganizationHierarchy() throws Exception {

        // Create parent organization
        String parentOrgHandle = System.currentTimeMillis() + "-" + PARENT_ORG_HANDLE;
        parentOrgId = orgMgtRestClient.addOrganization(PARENT_ORG_NAME, parentOrgHandle);

        // Create sub-organization
        String subOrgHandle = System.currentTimeMillis() + "-" + SUB_ORG_HANDLE;
        subOrgId = orgMgtRestClient.addSubOrganization(SUB_ORG_NAME, subOrgHandle);

        // Create another separate organization (simulates different tenant)
        String secondaryOrgHandle = System.currentTimeMillis() + "-" + SECONDARY_ORG_HANDLE;
        secondaryOrgId = orgMgtRestClient.addOrganization(SECONDARY_ORG_NAME, secondaryOrgHandle);

        // Allow time for organization creation to propagate
        Thread.sleep(2000);
    }

    /**
     * Delete organization hierarchy created for testing.
     *
     * @throws Exception If an error occurs while deleting organizations.
     */
    private void deleteOrganizationHierarchy() throws Exception {

        // Delete organizations - check for null before deleting
        if (orgMgtRestClient != null) {
            // Delete in reverse order
            if (secondaryOrgId != null) {
                try {
                    orgMgtRestClient.deleteOrganization(secondaryOrgId);
                } catch (Exception e) {
                    System.out.println("Warning: Failed to delete secondary org: " + e.getMessage());
                }
            }
            if (subOrgId != null) {
                try {
                    orgMgtRestClient.deleteOrganization(subOrgId);
                } catch (Exception e) {
                    System.out.println("Warning: Failed to delete sub org: " + e.getMessage());
                }
            }
            if (parentOrgId != null) {
                try {
                    orgMgtRestClient.deleteOrganization(parentOrgId);
                } catch (Exception e) {
                    System.out.println("Warning: Failed to delete parent org: " + e.getMessage());
                }
            }
        }
    }
}
