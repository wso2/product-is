/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalRole;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RoleMapping;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.scenarios.commons.clients.UserManagementClient;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.scenarios.commons.clients.claims.ClaimMetadataManagementServiceClient;

import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementTestCase extends ScenarioTestBase {

    private static final String PERMISSION_ADMIN_LOGIN = "/permission/admin/login";
    private static final String TEST_SERVICE_PROVIDER_NAME = "SimpleServiceProvider";
    private static final String IDP_ROLE_1 = "idpRole_1";
    private static final String BASIC_AUTH_REQUEST_PATH_AUTHENTICATOR = "BasicAuthRequestPathAuthenticator";
    private static final String BASIC_AUTH_REQUEST_AUTHENTICATOR_DISPLAYNAME = "basic-auth";
    private static final String SUBJECT_CLAIM_URI = "subject_claim_uri";
    private static final String LOGIN_ROLE = "LoginRole";
    private static final String SP_CLAIM_1 = "spClaim_1";
    private static final String ROLE_CLAIM = "roleClaim";
    private static final String IDP_CLAIM_1 = "idpClaim_1";
    private static final String TEST_ASSOCIATION_USERNAME_1 = "testAssociationUser";
    private static final String TEST_ASSOCIATION_PASSWORD_1 = "testAssociationPass";
    private static final String INTERNAL_ADMIN = "Internal/admin";
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private UserManagementClient userMgtClient;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        loginAndObtainSessionCookie();
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendServiceURL,
                configContext);
        userMgtClient = new UserManagementClient(backendServiceURL, sessionCookie);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendServiceURL);
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendServiceURL, sessionCookie);
        populateData();

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        applicationManagementServiceClient = null;
//        userMgtClient.deleteRole(LOGIN_ROLE);
//        userMgtClient.deleteUser(TEST_ASSOCIATION_USERNAME_1);
        claimMetadataManagementServiceClient.removeLocalClaim(SUBJECT_CLAIM_URI);
        claimMetadataManagementServiceClient.removeLocalClaim(SP_CLAIM_1);
        claimMetadataManagementServiceClient.removeLocalClaim(IDP_CLAIM_1);
        claimMetadataManagementServiceClient.removeLocalClaim(ROLE_CLAIM);
    }

    public void populateData() throws Exception {
        LocalClaimDTO claimMapping1 = getClaimMappingDTO(SP_CLAIM_1, "SP Cliam 1",
                "description");
        LocalClaimDTO claimMapping2 = getClaimMappingDTO(IDP_CLAIM_1, "IDP Cliam 1",
                "mapping2");
        LocalClaimDTO claimMapping3 = getClaimMappingDTO(SUBJECT_CLAIM_URI, "Subject" +
                " Claim URI", "employeeType");
        LocalClaimDTO claimMapping4 = getClaimMappingDTO(ROLE_CLAIM, "Role " +
                " Claim URI", "mapping3");
        claimMetadataManagementServiceClient.addLocalClaim(claimMapping1);
        claimMetadataManagementServiceClient.addLocalClaim(claimMapping2);
        claimMetadataManagementServiceClient.addLocalClaim(claimMapping3);
        claimMetadataManagementServiceClient.addLocalClaim(claimMapping4);
    }

    private LocalClaimDTO getClaimMappingDTO(String claim, String description, String mappedAttribute) {

        LocalClaimDTO localClaimDTO1 = new LocalClaimDTO();
        localClaimDTO1.setLocalClaimURI(claim);

        AttributeMappingDTO attributeMappingDTO1 = new AttributeMappingDTO();
        attributeMappingDTO1.setUserStoreDomain(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        attributeMappingDTO1.setAttributeName(mappedAttribute);


        AttributeMappingDTO[] attributeMappingDTOs = new AttributeMappingDTO[]{attributeMappingDTO1};
        localClaimDTO1.setAttributeMappings(attributeMappingDTOs);

        ClaimPropertyDTO claimPropertyDTO1 = new ClaimPropertyDTO();
        claimPropertyDTO1.setPropertyName("DisplayName");
        claimPropertyDTO1.setPropertyValue(description);

        ClaimPropertyDTO[] claimPropertyDTOs = new ClaimPropertyDTO[]{claimPropertyDTO1};
        localClaimDTO1.setClaimProperties(claimPropertyDTOs);

        return localClaimDTO1;
    }


    @BeforeMethod
    public void setUp() {
        createApplication("TestServiceProvider");
    }

    @AfterMethod
    public void tearDown() {
        deleteApplication("TestServiceProvider");
    }


    private void createApplication(String applicationName) {
        try {
            String description = "This is a test Service Provider";
            applicationManagementServiceClient.createApplication(applicationName, description);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }

    private void deleteApplication(String applicationName) {
        try {
            applicationManagementServiceClient.deleteApplication(applicationName);
        } catch (Exception e) {
            Assert.fail("Error while trying to delete Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.1.1")
    public void testCreateApplication() {

        try {
            createApplication(TEST_SERVICE_PROVIDER_NAME);
            Assert.assertEquals(applicationManagementServiceClient.getApplication(TEST_SERVICE_PROVIDER_NAME).getApplicationName(),
                    TEST_SERVICE_PROVIDER_NAME, "Failed to create a Service Provider: " + TEST_SERVICE_PROVIDER_NAME);
        } catch (Exception e) {
            Assert.fail("Error while trying to create a Service Provider", e);
        }

    }

    @Test(alwaysRun = true, description = "2.1.1.2", dependsOnMethods = {
            "testCreateApplication"})
    public void testReadApplication() {

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (TEST_SERVICE_PROVIDER_NAME);
            Assert.assertEquals(serviceProvider.getApplicationName(), TEST_SERVICE_PROVIDER_NAME,
                    "Failed to read Service Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to read Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.1.3", dependsOnMethods = {
            "testReadApplication"})
    public void testGetApplicationBasicInfo() {

        try {
            ApplicationBasicInfo[] applicationBasicInfos
                    = applicationManagementServiceClient.getAllApplicationBasicInfo();

            boolean applicationExists = false;

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                if (applicationBasicInfo.getApplicationName().equals(TEST_SERVICE_PROVIDER_NAME)) {
                    Assert.assertEquals(applicationBasicInfo.getDescription(), "This is a test Service Provider",
                            "Reading description failed");
                    applicationExists = true;
                }
            }

            if (!applicationExists) {
                Assert.fail("Could not find application " + TEST_SERVICE_PROVIDER_NAME);
            }
        } catch (Exception e) {
            Assert.fail("Error while trying to all applications basic information", e);
        }
    }


    @Test(alwaysRun = true, description = "2.1.1.4", dependsOnMethods = {
            "testGetApplicationBasicInfo"})
    public void testUpdateApplication() {

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (TEST_SERVICE_PROVIDER_NAME);
            serviceProvider.setDescription("Updated description");
            serviceProvider.setSaasApp(true);
            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(TEST_SERVICE_PROVIDER_NAME);
            Assert.assertEquals(updatedServiceProvider.getDescription(), "Updated description",
                    "Failed update application description");
            Assert.assertEquals(updatedServiceProvider.getSaasApp(), true, "Set isSaasApp failed");
        } catch (Exception e) {
            Assert.fail("Error while trying to update Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.1.5", dependsOnMethods = {
            "testUpdateApplication"})
    public void testDeleteApplication() {

        try {
            deleteApplication(TEST_SERVICE_PROVIDER_NAME);

            ApplicationBasicInfo[] applicationBasicInfos =
                    applicationManagementServiceClient.getAllApplicationBasicInfo();

            boolean applicationExists = false;

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                if (applicationBasicInfo.getApplicationName().equals(TEST_SERVICE_PROVIDER_NAME)) {
                    applicationExists = true;
                }
            }

            Assert.assertFalse(applicationExists, TEST_SERVICE_PROVIDER_NAME + " has not been deleted.");
        } catch (Exception e) {
            Assert.fail("Error while trying to delete a Service Provider", e);
        }
    }


    @Test(alwaysRun = true, description = "2.1.2.6")
    public void testUpdateInboundProvisioningData() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            String provisioningUserStore = "scim-inbound-userstore";
            InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
            inBoundProConfig.setProvisioningUserStore(provisioningUserStore);
            serviceProvider.setInboundProvisioningConfig(inBoundProConfig);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);
            Assert.assertEquals(updatedServiceProvider.getInboundProvisioningConfig()
                            .getProvisioningUserStore()
                    , "scim-inbound-userstore", "Failed update provisioning user store");
        } catch (Exception e) {
            Assert.fail("Error while trying to update inbound provisioning data", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.2.7")
    public void testUpdateOutboundProvisioningData() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<IdentityProvider> provisioningIdps = new ArrayList<IdentityProvider>();

            String connector = "provisioning_con_idp_test";

            IdentityProvider proIdp = new IdentityProvider();
            proIdp.setIdentityProviderName("idp_test");

            JustInTimeProvisioningConfig jitpro = new JustInTimeProvisioningConfig();
            jitpro.setProvisioningEnabled(true);
            proIdp.setJustInTimeProvisioningConfig(jitpro);

            ProvisioningConnectorConfig proCon = new ProvisioningConnectorConfig();
            proCon.setBlocking(true);

            proCon.setName(connector);
            proIdp.setDefaultProvisioningConnectorConfig(proCon);
            provisioningIdps.add(proIdp);

            OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
            outboundProConfig.setProvisioningIdentityProviders(provisioningIdps
                    .toArray(new IdentityProvider[provisioningIdps.size()]));
            serviceProvider.setOutboundProvisioningConfig(outboundProConfig);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);

            IdentityProvider identityProvider = updatedServiceProvider
                    .getOutboundProvisioningConfig().getProvisioningIdentityProviders()[0];

            Assert.assertEquals(identityProvider.getIdentityProviderName(), "idp_test",
                    "Update IDP failed");
            Assert.assertEquals(identityProvider.getJustInTimeProvisioningConfig()
                    .getProvisioningEnabled(), true, "Update JIT provisioning config failed");
            Assert.assertEquals(identityProvider.getDefaultProvisioningConnectorConfig()
                            .getBlocking(), true
                    , "Set provisioning connector blocking failed");
            Assert.assertEquals(identityProvider.getDefaultProvisioningConnectorConfig().getName
                            (), connector
                    , "Set default provisioning connector failed");

        } catch (Exception e) {
            Assert.fail("Error while trying to update outbound provisioning data", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.2.8")
    public void testUpdateRequestPathAuthenticators() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<RequestPathAuthenticatorConfig> reqAuthList = new ArrayList<RequestPathAuthenticatorConfig>();
            RequestPathAuthenticatorConfig reqAuth = new RequestPathAuthenticatorConfig();
            reqAuth.setName(BASIC_AUTH_REQUEST_PATH_AUTHENTICATOR);
            reqAuth.setDisplayName(BASIC_AUTH_REQUEST_AUTHENTICATOR_DISPLAYNAME);
            reqAuthList.add(reqAuth);

            serviceProvider.setRequestPathAuthenticatorConfigs(reqAuthList
                    .toArray(new RequestPathAuthenticatorConfig[reqAuthList.size()]));

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);
            Assert.assertEquals(updatedServiceProvider.getRequestPathAuthenticatorConfigs()[0]
                            .getName(), BASIC_AUTH_REQUEST_PATH_AUTHENTICATOR,
                    "Failed update Request path authenticator name");
        } catch (Exception e) {
            Assert.fail("Error while trying to update Request Path Authenticators", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update In-bound Authenticator Configuration")
    public void testUpdateInboundAuthenticatorConfiguration() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<InboundAuthenticationRequestConfig> authRequestList
                    = new ArrayList<InboundAuthenticationRequestConfig>();

            InboundAuthenticationRequestConfig samlAuthenticationRequest = new
                    InboundAuthenticationRequestConfig();
            samlAuthenticationRequest.setInboundAuthKey("samlIssuer");
            samlAuthenticationRequest.setInboundAuthType("samlsso");
            Property property = new Property();
            property.setName("attrConsumServiceIndex");
            property.setValue("attrConsumServiceIndexValue");
            Property[] properties = {property};
            samlAuthenticationRequest.setProperties(properties);
            authRequestList.add(samlAuthenticationRequest);

            serviceProvider.getInboundAuthenticationConfig()
                    .setInboundAuthenticationRequestConfigs(
                            authRequestList
                                    .toArray(new InboundAuthenticationRequestConfig[authRequestList
                                            .size()]));

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);
            Assert.assertEquals(updatedServiceProvider.getInboundAuthenticationConfig()
                            .getInboundAuthenticationRequestConfigs()[0].getInboundAuthKey(), "samlIssuer"
                    , "Failed update Inbound Auth key");

            Assert.assertEquals(updatedServiceProvider.getInboundAuthenticationConfig()
                            .getInboundAuthenticationRequestConfigs()[0].getInboundAuthType(), "samlsso"
                    , "Failed update Inbound Auth type");

            Property[] updatedProperties = updatedServiceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs()[0].getProperties();

            Assert.assertEquals(updatedProperties[0].getName(), "attrConsumServiceIndex",
                    "Failed to add property");
            Assert.assertEquals(updatedProperties[0].getValue(), "attrConsumServiceIndexValue"
                    , "Failed to add property");
        } catch (Exception e) {
            Assert.fail("Error while trying to update Inbound Authenticator Configuration", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.2.9")
    public void testUpdateApplicationPermissions() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            PermissionsAndRoleConfig permAndRoleConfig = new PermissionsAndRoleConfig();
            List<ApplicationPermission> appPermList = new ArrayList<ApplicationPermission>();

            ApplicationPermission appPermission = new ApplicationPermission();
            appPermission.setValue("app_permission");
            appPermList.add(appPermission);

            permAndRoleConfig.setPermissions(appPermList
                    .toArray(new ApplicationPermission[appPermList.size()]));

            serviceProvider.setPermissionAndRoleConfig(permAndRoleConfig);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient.
                    getApplication(applicationName);

            PermissionsAndRoleConfig updatedPermissionsAndRoleConfig
                    = updatedServiceProvider.getPermissionAndRoleConfig();

            Assert.assertEquals(updatedPermissionsAndRoleConfig.getPermissions()[0].getValue(),
                    "app_permission",
                    "Failed update application permissions");

        } catch (Exception e) {
            Assert.fail("Error while trying to update Application Permissions", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.2.10")
    public void testUpdateRoles() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            PermissionsAndRoleConfig permAndRoleConfig = new PermissionsAndRoleConfig();
            List<RoleMapping> roleMappingList = new ArrayList<RoleMapping>();

            RoleMapping mapping = new RoleMapping();
            LocalRole localRole = new LocalRole();
            localRole.setLocalRoleName(INTERNAL_ADMIN);
            mapping.setLocalRole(localRole);
            mapping.setRemoteRole("spRole_1");
            roleMappingList.add(mapping);

            permAndRoleConfig.setRoleMappings(roleMappingList.toArray(new RoleMapping[roleMappingList
                    .size()]));
            serviceProvider.setPermissionAndRoleConfig(permAndRoleConfig);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient.
                    getApplication(applicationName);

            PermissionsAndRoleConfig updatedPermissionsAndRoleConfig
                    = updatedServiceProvider.getPermissionAndRoleConfig();

            Assert.assertEquals(updatedPermissionsAndRoleConfig.getRoleMappings()[0].getLocalRole()
                    .getLocalRoleName(), "Internal/admin", "Failed update local role");

            Assert.assertEquals(updatedPermissionsAndRoleConfig.getRoleMappings()[0].
                            getRemoteRole(), "spRole_1",
                    "Failed update remote role");

        } catch (Exception e) {
            Assert.fail("Error while trying to update Roles", e);
        }
    }

    @Test(alwaysRun = true, description = "2.1.2.11")
    public void testUpdateClaimConfiguration() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

            ClaimMapping mapping = new ClaimMapping();

            Claim localClaim = new Claim();
            localClaim.setClaimUri(IDP_CLAIM_1);

            Claim spClaim = new Claim();
            spClaim.setClaimUri(SP_CLAIM_1);

            mapping.setRequested(true);
            mapping.setLocalClaim(localClaim);
            mapping.setRemoteClaim(spClaim);

            claimMappingList.add(mapping);

            serviceProvider.getClaimConfig().setClaimMappings(
                    claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]));

            serviceProvider.getClaimConfig().setLocalClaimDialect(true);
            serviceProvider.getClaimConfig().setRoleClaimURI(ROLE_CLAIM);
            serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(true);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient.
                    getApplication(applicationName);

            ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

            Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getLocalClaim().
                            getClaimUri(), IDP_CLAIM_1,
                    "Failed update local claim uri");

            Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getRemoteClaim().
                            getClaimUri(), SP_CLAIM_1,
                    "Failed update remote claim uri");

            Assert.assertEquals(updatedClaimConfig.getLocalClaimDialect(), true,
                    "Failed update localClaimDialect property.");

            Assert.assertEquals(updatedClaimConfig.getRoleClaimURI(), ROLE_CLAIM,
                    "Failed update role claim uri");

            Assert.assertEquals(updatedClaimConfig.getAlwaysSendMappedLocalSubjectId(), true,
                    "Failed update alwaysSendMappedLocalSubjectId property.");


        } catch (Exception e) {
            Assert.fail("Error while trying to update Claim Configuration", e);
        }
    }

//    @Test(alwaysRun = true, description = "Retrieve all federated IdPs with login permission")
    public void testRetrieveFederatedIdPsWithLoginPermission() {
        try {
            userMgtClient.addRole(LOGIN_ROLE, new String[0], new String[]{PERMISSION_ADMIN_LOGIN});
            userMgtClient.addUser(TEST_ASSOCIATION_USERNAME_1, TEST_ASSOCIATION_PASSWORD_1, new String[]{LOGIN_ROLE}, null);

            ApplicationManagementServiceClient appManageServiceClient = new ApplicationManagementServiceClient
                    (TEST_ASSOCIATION_USERNAME_1, TEST_ASSOCIATION_PASSWORD_1, backendServiceURL, configContext);
            IdentityProvider[] idps = appManageServiceClient.getAllFederatedIdentityProvider();

            Assert.assertTrue(idps != null && idps.length != 0, "Federated IdPs have been retrieved by a user with " +
                    "login permission.");
        } catch (Exception e) {
            Assert.fail("Error while trying to retrieve federated idps with login permission", e);
        }
    }
}
