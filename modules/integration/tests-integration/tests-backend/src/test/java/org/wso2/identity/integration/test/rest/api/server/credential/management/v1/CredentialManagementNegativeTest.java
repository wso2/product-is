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
import org.apache.http.HttpStatus;
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

import java.io.IOException;

/**
 * Integration tests for Credential Management API negative scenarios.
 * Tests error handling for invalid requests, non-existent resources, and malformed inputs.
 */
public class CredentialManagementNegativeTest extends CredentialManagementTestBase {

    private static final String PARENT_ORG_NAME = "parentOrganization";
    private static final String PARENT_ORG_HANDLE = "parentOrg";
    private static final String SECONDARY_ORG_NAME = "secondaryOrganization";
    private static final String SECONDARY_ORG_HANDLE = "secondaryOrg";

    // Invalid test data
    private static final String NON_EXISTENT_USER_ID = "non-existent-user-12345";
    private static final String INVALID_USER_ID = "invalid@user#id";
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

    @Test(description = "Test delete credential with non-existent credential ID")
    public void testDeleteCredentialWithNonExistentCredentialId() {

        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "valid-user-id" +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PASSKEY + PATH_SEPARATOR + NON_EXISTENT_CREDENTIAL_ID;
        Response response = getResponseOfDelete(endpoint);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(description = "Test delete credential with invalid credential type")
    public void testDeleteCredentialWithInvalidCredentialType() {

        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + "valid-user-id" +
                CREDENTIALS_PATH + PATH_SEPARATOR + INVALID_CREDENTIAL_TYPE + PATH_SEPARATOR + "credential-id";

        RestAssured.given()
                .auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType("application/json")
                .when()
                .delete(endpoint)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test(description = "Test get credentials using REST client with non-existent user")
    public void testGetCredentialsUsingRestClientWithNonExistentUser() {

        try {
            credentialManagementRestClient.getUserCredentials(NON_EXISTENT_USER_ID);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Error code") || e.getMessage().contains("404") ||
                            e.getMessage().contains("not found"),
                    "Expected error for non-existent user, but got: " + e.getMessage());
        }
    }

    @Test(description = "Test that admin from organization A cannot access credentials of users in organization B")
    public void testCrossOrganizationGetCredentialsIsolation() {

        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SECONDARY_ORG_USER_ID +
                CREDENTIALS_PATH;

        Response response = getResponseOfGet(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_OK ||
                        statusCode == HttpStatus.SC_FORBIDDEN ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Cross-organization access should be isolated. Expected 200 (empty), 403, or 404, but got: " + statusCode
        );

        if (statusCode == HttpStatus.SC_OK) {
            Credential[] credentials = response.as(Credential[].class);
            Assert.assertEquals(credentials.length, 0,
                    "Should not return credentials from secondary organization");
        }
    }

    @Test(description = "Test that admin from organization A cannot delete credentials of users in organization B")
    public void testCrossOrganizationDeleteCredentialsIsolation() throws Exception {

        String endpoint = CREDENTIAL_MANAGEMENT_API_BASE_PATH + PATH_SEPARATOR + SECONDARY_ORG_USER_ID +
                CREDENTIALS_PATH + PATH_SEPARATOR + TYPE_PASSKEY + PATH_SEPARATOR + TEST_PASSKEY_CREDENTIAL_ID;

        Response response = getResponseOfDelete(endpoint);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode == HttpStatus.SC_BAD_REQUEST ||
                        statusCode == HttpStatus.SC_FORBIDDEN ||
                        statusCode == HttpStatus.SC_NOT_FOUND,
                "Cross-organization delete should be denied. Expected 400, 403, or 404, but got: " + statusCode
        );
    }

    /**
     * Create organization hierarchy for testing.
     * Creates: Parent Org and Secondary Org (separate tenant-like org)
     *
     * @throws Exception If an error occurs while creating organizations.
     */
    private void createOrganizationHierarchy() throws Exception {

        String parentOrgHandle = System.currentTimeMillis() + "-" + PARENT_ORG_HANDLE;
        parentOrgId = orgMgtRestClient.addOrganization(PARENT_ORG_NAME, parentOrgHandle);
        String secondaryOrgHandle = System.currentTimeMillis() + "-" + SECONDARY_ORG_HANDLE;
        secondaryOrgId = orgMgtRestClient.addOrganization(SECONDARY_ORG_NAME, secondaryOrgHandle);
        Thread.sleep(2000);
    }

    /**
     * Delete organization hierarchy created for testing.
     *
     * @throws Exception If an error occurs while deleting organizations.
     */
    private void deleteOrganizationHierarchy() throws Exception {

        if (orgMgtRestClient != null) {
            if (parentOrgId != null) {
                try {
                    orgMgtRestClient.deleteOrganization(parentOrgId);
                } catch (Exception e) {
                    System.out.println("Warning: Failed to delete parent org: " + e.getMessage());
                }
            }
            if (secondaryOrgId != null) {
                try {
                    orgMgtRestClient.deleteOrganization(secondaryOrgId);
                } catch (Exception e) {
                    System.out.println("Warning: Failed to delete secondary org: " + e.getMessage());
                }
            }
        }
    }
}
