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

package org.wso2.identity.integration.test.apiAuthorization;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceCreationModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourcePatchModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeCreationModel;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.restclients.APIResourceManagementClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;

/**
 * Test case for API resource inheritance.
 */
public class APIResourceInheritanceTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON = "authorized-apis-for-api-inheritance-test.json";
    private static final String BUSINESS_API_IDENTIFIER = "business_api";
    private static final String BUSINESS_API_NAME = "Business API";
    private static final String READ_SCOPE_NAME = "read";
    private static final String READ_SCOPE_DISPLAY_NAME = "Read";
    private static final String READ_SCOPE_DESCRIPTION = "Allow Reading";
    private static final String WRITE_SCOPE_NAME = "write";
    private static final String WRITE_SCOPE_DISPLAY_NAME = "Write";
    private static final String WRITE_SCOPE_DESCRIPTION = "Allow Writing";
    private static final String DELETE_SCOPE_NAME = "delete";
    private static final String DELETE_SCOPE_DISPLAY_NAME = "Delete";
    private static final String DELETE_SCOPE_DESCRIPTION = "Allow Deleting";
    private static final String L1_SUB_ORG_NAME = "L1_Sub_Org";
    private static final String L2_SUB_ORG_NAME = "L2_Sub_Org";
    private static final String IDENTIFIER_KEY = "identifier";
    private static final String API_TYPE = "type";
    private static final String ORGANIZATION_API_TYPE = "ORGANIZATION";
    private static final String SYSTEM_API_TYPE = "SYSTEM";
    private static final String TENANT_API_TYPE = "TENANT";
    private static final String CONSOLE_FEATURE_API_TYPE = "CONSOLE_FEATURE";
    private static final String CONSOLE_ORG_FEATURE_API_TYPE = "CONSOLE_ORG_FEATURE";
    private static final String CONSOLE_ORG_LEVEL_API_TYPE = "CONSOLE_ORG_LEVEL";
    private static final String EQ = "eq";
    private final TestUserMode userMode;
    private APIResourceManagementClient apiResourceManagementClient;
    private OrgMgtRestClient orgMgtRestClient;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private String level1OrgId;
    private String level2OrgId;
    private String switchedM2MTokenForLevel1Org;
    private String switchedM2MTokenForLevel2Org;
    private String businessApiResourceId;

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public APIResourceInheritanceTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        apiResourceManagementClient = new APIResourceManagementClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON, this.getClass())));

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        level1OrgId = orgMgtRestClient.addOrganization(L1_SUB_ORG_NAME);
        level2OrgId = orgMgtRestClient.addSubOrganization(L2_SUB_ORG_NAME, level1OrgId);
        switchedM2MTokenForLevel1Org = orgMgtRestClient.switchM2MToken(level1OrgId);
        switchedM2MTokenForLevel2Org = orgMgtRestClient.switchM2MToken(level2OrgId);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Delete sub organizations.
        orgMgtRestClient.deleteSubOrganization(level2OrgId, level1OrgId);
        orgMgtRestClient.deleteOrganization(level1OrgId);
        idpMgtServiceClient.deleteIdP("SSO");
        orgMgtRestClient.closeHttpClient();
        apiResourceManagementClient.closeHttpClient();
    }

    @Test(description = "Test organization API and org level console specific API are inherited from root organization.")
    public void testOrganizationSpecificAPIInheritance() throws Exception {

        verifyOrganizationSpecificAPIInheritanceInSubOrg(switchedM2MTokenForLevel1Org);
        verifyOrganizationSpecificAPIInheritanceInSubOrg(switchedM2MTokenForLevel2Org);
    }

    private void verifyOrganizationSpecificAPIInheritanceInSubOrg(String switchedM2MToken) throws Exception {

        // Check whether the organization APIs are inherited by the sub organizations.
        List<APIResourceListItem> organizationAPIsInSubOrg =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + ORGANIZATION_API_TYPE, switchedM2MToken);
        Assert.assertNotEquals(organizationAPIsInSubOrg.size(), 0, "Organization APIs are not inherited properly.");

        // Check whether the org level console feature APIs are inherited by the sub organizations.
        List<APIResourceListItem> consoleOrgLevelAPIsInSubOrg =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + CONSOLE_ORG_LEVEL_API_TYPE, switchedM2MToken);
        Assert.assertNotEquals(consoleOrgLevelAPIsInSubOrg.size(), 0,
                "Organization level APIs are not inherited properly.");

        // Check whether the org level console feature APIs are inherited by the sub organizations.
        List<APIResourceListItem> consoleOrgFeatureAPIsInSubOrg =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + CONSOLE_ORG_FEATURE_API_TYPE, switchedM2MToken);
        Assert.assertNotEquals(consoleOrgFeatureAPIsInSubOrg.size(), 0,
                "Organization feature APIs are not inherited properly.");
    }

    @Test(dependsOnMethods = {"testOrganizationSpecificAPIInheritance"},
            description = "Test system APIs, tenant APIs and  are not inherited by the sub organizations.")
    public void testAPINonInheritanceForSubOrgLevel() throws Exception {

        verifyAPINonInheritanceInSubOrg(switchedM2MTokenForLevel1Org);
        verifyAPINonInheritanceInSubOrg(switchedM2MTokenForLevel2Org);
    }

    private void verifyAPINonInheritanceInSubOrg(String switchedM2MToken) throws Exception {

        // Check whether the system APIs are not inherited by the sub organizations.
        List<APIResourceListItem> systemAPIs =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + SYSTEM_API_TYPE, switchedM2MToken);
        Assert.assertEquals(systemAPIs.size(), 0, "System APIs are inherited by the sub org.");

        // Check whether the tenant APIs are not inherited by the sub organizations.
        List<APIResourceListItem> tenantAPIs =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + TENANT_API_TYPE, switchedM2MToken);
        Assert.assertEquals(tenantAPIs.size(), 0, "Tenant APIs are inherited by the sub org.");

        // Check whether the console feature APIs are not inherited by the sub organizations.
        List<APIResourceListItem> consoleFeatureAPIs =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        API_TYPE + "+" + EQ + "+" + CONSOLE_FEATURE_API_TYPE, switchedM2MToken);
        Assert.assertEquals(consoleFeatureAPIs.size(), 0, "Console feature APIs are inherited by the sub org.");
    }

    @Test(dependsOnMethods = {"testAPINonInheritanceForSubOrgLevel"},
            description = "Test sub organization inherit custom API resources from root organization.")
    public void testSubOrgInheritCustomAPIResourcesFromRootOrg() throws Exception {

        // Create a business API resource in root org.
        APIResourceCreationModel businessAPIResource = buildApiResourceCreationModel();
        businessApiResourceId = apiResourceManagementClient.createAPIResource(businessAPIResource);
        Assert.assertNotNull(businessApiResourceId, "API resource creation failed.");

        // Check whether the API resource is inherited by the sub organizations.
        verifyBusinessAPIInheritanceInSubOrg(businessApiResourceId, switchedM2MTokenForLevel1Org);
        verifyBusinessAPIInheritanceInSubOrg(businessApiResourceId, switchedM2MTokenForLevel2Org);
    }

    private void verifyBusinessAPIInheritanceInSubOrg(String apiResourceId, String switchedM2MToken) throws Exception {

        List<APIResourceListItem> apiResourcesWithFilteringFromSubOrg =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        IDENTIFIER_KEY + "+" + EQ + "+" + BUSINESS_API_IDENTIFIER, switchedM2MToken);
        Assert.assertEquals(apiResourcesWithFilteringFromSubOrg.size(), 1, "Business API is not inherited properly.");
        Assert.assertEquals(apiResourcesWithFilteringFromSubOrg.get(0).getIdentifier(), BUSINESS_API_IDENTIFIER,
                "Business API is not inherited properly.");
        Assert.assertEquals(apiResourcesWithFilteringFromSubOrg.get(0).getName(), BUSINESS_API_NAME,
                "Business API is not inherited properly.");
        Assert.assertEquals(apiResourcesWithFilteringFromSubOrg.get(0).getId(), apiResourceId,
                "Business API is not inherited properly.");
        List<ScopeGetModel> apiResourceScopesFromSubOrg =
                apiResourceManagementClient.getAPIResourceScopesFromSubOrg(businessApiResourceId, switchedM2MToken);
        Assert.assertEquals(apiResourceScopesFromSubOrg.size(), 2, "Scopes are not inherited properly.");
    }

    @Test(dependsOnMethods = {"testSubOrgInheritCustomAPIResourcesFromRootOrg"},
            description = "Added scopes to business API reflect in sub organization inherited API.")
    public void testAddedScopesInheritedBySubOrg() throws Exception {

        // Get the scopes count in sub orgs before adding new scopes.
        List<ScopeGetModel> scopesFromSubOrgLevel1 =
                apiResourceManagementClient.getAllScopesInSubOrg(switchedM2MTokenForLevel1Org);
        List<ScopeGetModel> scopesFromSubOrgLevel2 =
                apiResourceManagementClient.getAllScopesInSubOrg(switchedM2MTokenForLevel1Org);
        int orgL1ScopesCount = scopesFromSubOrgLevel1.size();
        int orgL2ScopesCount = scopesFromSubOrgLevel2.size();

        // Add a scope to the business API.
        ScopeGetModel deleteScope = new ScopeGetModel();
        deleteScope.setName(DELETE_SCOPE_NAME);
        deleteScope.setDisplayName(DELETE_SCOPE_DISPLAY_NAME);
        deleteScope.setDescription(DELETE_SCOPE_DESCRIPTION);
        List<ScopeGetModel> addedScopes = new ArrayList<>();
        addedScopes.add(deleteScope);
        APIResourcePatchModel apiResourcePatchModel = new APIResourcePatchModel();
        apiResourcePatchModel.setAddedScopes(addedScopes);

        apiResourceManagementClient.updateAPIResource(businessApiResourceId, apiResourcePatchModel);
        List<ScopeGetModel> scopesOfRootAPI = apiResourceManagementClient.getAPIResourceScopes(businessApiResourceId);
        Assert.assertEquals(scopesOfRootAPI.size(), 3, "Scopes are not added properly.");

        // Check whether the new scope is inherited by the sub organizations.
        verifyInheritedAPIScopeCount(3, switchedM2MTokenForLevel1Org);
        verifyInheritedAPIScopeCount(3, switchedM2MTokenForLevel2Org);

        // Check whether the new scope inherited by the sub organizations are properly returned in org level /scopes endpoint.
        List<ScopeGetModel> scopesFromSubOrgLevel1AfterNewScopeAddition =
                apiResourceManagementClient.getAllScopesInSubOrg(switchedM2MTokenForLevel1Org);
        Assert.assertEquals(scopesFromSubOrgLevel1AfterNewScopeAddition.size(), orgL1ScopesCount + 1,
                "New scope is not inherited properly.");
        List<ScopeGetModel> scopesFromSubOrgLevel2AfterNewScopeAddition =
                apiResourceManagementClient.getAllScopesInSubOrg(switchedM2MTokenForLevel2Org);
        Assert.assertEquals(scopesFromSubOrgLevel2AfterNewScopeAddition.size(), orgL2ScopesCount + 1,
                "New scope is not inherited properly.");
    }

    private void verifyInheritedAPIScopeCount(int expectedScopeCount, String switchedM2MToken) throws Exception {

        List<ScopeGetModel> apiResourceScopesFromSubOrg =
                apiResourceManagementClient.getAPIResourceScopesFromSubOrg(businessApiResourceId, switchedM2MToken);
        Assert.assertEquals(apiResourceScopesFromSubOrg.size(), expectedScopeCount,
                "Scopes are not inherited properly.");
    }

    @Test(dependsOnMethods = {"testAddedScopesInheritedBySubOrg"},
            description = "Deleted scopes from business API reflect in sub organization inherited API.")
    public void testDeletedScopesInheritedBySubOrg() throws Exception {

        // Get the scopes count in sub orgs before adding new scopes.
        List<ScopeGetModel> scopesFromSubOrgLevel1 = apiResourceManagementClient.getAllScopesInSubOrg(
                switchedM2MTokenForLevel1Org);
        List<ScopeGetModel> scopesFromSubOrgLevel2 = apiResourceManagementClient.getAllScopesInSubOrg(
                switchedM2MTokenForLevel1Org);
        int orgL1ScopesCount = scopesFromSubOrgLevel1.size();
        int orgL2ScopesCount = scopesFromSubOrgLevel2.size();

        // Delete "read" and "delete" scopes from the business API.
        apiResourceManagementClient.deleteScopeOfAPIResource(businessApiResourceId, READ_SCOPE_NAME);
        apiResourceManagementClient.deleteScopeOfAPIResource(businessApiResourceId, DELETE_SCOPE_NAME);
        List<ScopeGetModel> updatedScopesOfRootAPI =
                apiResourceManagementClient.getAPIResourceScopes(businessApiResourceId);
        Assert.assertEquals(updatedScopesOfRootAPI.size(), 1, "Scopes are not deleted properly.");

        // Check whether the deleted scopes are properly reflected in the sub organizations.
        verifyInheritedAPIScopeCount(1, switchedM2MTokenForLevel1Org);
        verifyInheritedAPIScopeCount(1, switchedM2MTokenForLevel2Org);

        /*
        Check whether the deleted scopes reflected in the sub organizations properly and properly returned through
        org level /scopes endpoint.
         */
        List<ScopeGetModel> scopesFromSubOrgLevel1AfterScopeDeletion = apiResourceManagementClient.getAllScopesInSubOrg(
                switchedM2MTokenForLevel1Org);
        Assert.assertEquals(scopesFromSubOrgLevel1AfterScopeDeletion.size(), orgL1ScopesCount - 2,
                "Deleted scopes are not reflected properly.");
        List<ScopeGetModel> scopesFromSubOrgLevel2AfterScopeDeletion = apiResourceManagementClient.getAllScopesInSubOrg(
                switchedM2MTokenForLevel2Org);
        Assert.assertEquals(scopesFromSubOrgLevel2AfterScopeDeletion.size(), orgL2ScopesCount - 2,
                "Deleted scopes are not reflected properly.");
    }

    @Test(dependsOnMethods = {"testDeletedScopesInheritedBySubOrg"},
            description = "Test API resource deletion.")
    public void testAPIResourceDeletionReflectInSubOrg() throws Exception {

        // Delete the business API resource.
        apiResourceManagementClient.deleteAPIResource(businessApiResourceId);
        List<APIResourceListItem> filteredAPIResource = apiResourceManagementClient.getAPIResourcesWithFiltering(
                IDENTIFIER_KEY + "+" + EQ + "+" + BUSINESS_API_IDENTIFIER);
        Assert.assertEquals(filteredAPIResource.size(), 0, "API resource deletion failed.");

        verifyAPIDeletionReflectsInSubOrg(switchedM2MTokenForLevel1Org);
        verifyAPIDeletionReflectsInSubOrg(switchedM2MTokenForLevel2Org);
    }

    private void verifyAPIDeletionReflectsInSubOrg(String switchedM2MToken) throws Exception {

        List<APIResourceListItem> apiResourcesWithFilteringFromSubOrg =
                apiResourceManagementClient.getAPIResourcesWithFilteringFromSubOrg(
                        IDENTIFIER_KEY + "+" + EQ + "+" + BUSINESS_API_IDENTIFIER, switchedM2MToken);
        Assert.assertEquals(apiResourcesWithFilteringFromSubOrg.size(), 0,
                "API resource deletion is not reflected in sub org.");
    }

    @Test(dependsOnMethods = {"testAPIResourceDeletionReflectInSubOrg"},
            description = "Test API modification is not allowed in sub organization.")
    public void testAPIModificationNotAllowedInSubOrg() throws Exception {

        List<APIResourceListItem> orgLevelAPIResourceMgtAPI = apiResourceManagementClient.getAPIResourcesWithFiltering(
                IDENTIFIER_KEY + "+" + EQ + "+" + "/o/api/server/v1/api-resources");
        Assert.assertEquals(orgLevelAPIResourceMgtAPI.size(), 1, "API resource not found.");
        String orgLevelAPIResourceMgtAPIId = orgLevelAPIResourceMgtAPI.get(0).getId();
        List<ScopeGetModel> scopesOfOrgLevelAPIResourceMgtAPI =
                apiResourceManagementClient.getAPIResourceScopes(orgLevelAPIResourceMgtAPIId);
        Assert.assertEquals(scopesOfOrgLevelAPIResourceMgtAPI.size(), 1, "Wrong number of scopes in the API resource.");
        Assert.assertEquals(scopesOfOrgLevelAPIResourceMgtAPI.get(0).getName(), "internal_org_api_resource_view",
                "Wrong scope in the API resource.");
    }

    private static APIResourceCreationModel buildApiResourceCreationModel() {

        APIResourceCreationModel businessAPIResource = new APIResourceCreationModel();
        businessAPIResource.setName(BUSINESS_API_NAME);
        businessAPIResource.setIdentifier(BUSINESS_API_IDENTIFIER);
        businessAPIResource.setRequiresAuthorization(true);
        ScopeCreationModel readScope = new ScopeCreationModel();
        readScope.setName(READ_SCOPE_NAME);
        readScope.setDisplayName(READ_SCOPE_DISPLAY_NAME);
        readScope.setDescription(READ_SCOPE_DESCRIPTION);
        businessAPIResource.addScopesItem(readScope);
        ScopeCreationModel writeScope = new ScopeCreationModel();
        writeScope.setName(WRITE_SCOPE_NAME);
        writeScope.setDisplayName(WRITE_SCOPE_DISPLAY_NAME);
        writeScope.setDescription(WRITE_SCOPE_DESCRIPTION);
        businessAPIResource.addScopesItem(writeScope);
        return businessAPIResource;
    }
}
