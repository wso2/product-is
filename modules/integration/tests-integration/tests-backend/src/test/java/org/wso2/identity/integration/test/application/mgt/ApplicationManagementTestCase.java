/*
*  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementTestCase extends ISIntegrationTest {

    private ApplicationManagementServiceClient applicationManagementServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);

    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        applicationManagementServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Testing create Service Provider")
    public void testCreateApplication() {
        String applicationName = "TestServiceProvider1";
        try {
            createApplication(applicationName);
            Assert.assertEquals(applicationManagementServiceClient.getApplication(applicationName).getApplicationName(),
                                applicationName, "Failed to create a Service Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to create a Service Provider", e);
        }

    }

    @Test(alwaysRun = true, description = "Testing delete Service Provider", dependsOnMethods = {
            "testCreateApplication" })
    public void testDeleteApplication() {
        String applicationName = "TestServiceProvider1";
        try {
            deleteApplication(applicationName);

            ApplicationBasicInfo[] applicationBasicInfos =
                    applicationManagementServiceClient.getAllApplicationBasicInfo();

            boolean applicationExists = false;

            for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
                if (applicationBasicInfo.getApplicationName().equals(applicationName)) {
                    applicationExists = true;
                }
            }

            Assert.assertFalse(applicationExists, applicationName + " has not been deleted.");
        } catch (Exception e) {
            Assert.fail("Error while trying to delete a Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing retrieve all applications basic information")
    public void testGetApplicationBasicInfo() {
        String applicationName = "TestServiceProvider";
        try {
            ApplicationBasicInfo[] applicationBasicInfos
                    = applicationManagementServiceClient.getAllApplicationBasicInfo();

            boolean applicationExists = false;

            for (ApplicationBasicInfo applicationBasicInfo: applicationBasicInfos){
                if (applicationBasicInfo.getApplicationName().equals(applicationName)){
                    Assert.assertEquals(applicationBasicInfo.getDescription(), "This is a test Service Provider",
                                        "Reading description failed");
                    applicationExists = true;
                }
            }

            if (!applicationExists){
                Assert.fail("Could not find application " + applicationName);
            }
        } catch (Exception e) {
            Assert.fail("Error while trying to all applications basic information", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing read Service Provider")
    public void testReadApplication() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);
            Assert.assertEquals(serviceProvider.getApplicationName(), applicationName,
                    "Failed to read Service Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to read Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update Service Provider")
    public void testUpdateApplication() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);
            serviceProvider.setDescription("Updated description");
            serviceProvider.setSaasApp(true);
            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);
            Assert.assertEquals(updatedServiceProvider.getDescription(), "Updated description",
                    "Failed update application description");
            Assert.assertEquals(updatedServiceProvider.getSaasApp(), true, "Set isSaasApp failed");
        } catch (Exception e) {
            Assert.fail("Error while trying to update Service Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update Inbound Provisioning Data")
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

    @Test(alwaysRun = true, description = "Testing update Outbound Provisioning Data")
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

    @Test(alwaysRun = true, description = "Testing update Request Path Authenticators")
    public void testUpdateRequestPathAuthenticators() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<RequestPathAuthenticatorConfig> reqAuthList = new ArrayList<RequestPathAuthenticatorConfig>();
            RequestPathAuthenticatorConfig reqAuth = new RequestPathAuthenticatorConfig();
            reqAuth.setName("test");
            reqAuth.setDisplayName("req_path_auth_test");
            reqAuthList.add(reqAuth);

            serviceProvider.setRequestPathAuthenticatorConfigs(reqAuthList
                    .toArray(new RequestPathAuthenticatorConfig[reqAuthList.size()]));

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);
            Assert.assertEquals(updatedServiceProvider.getRequestPathAuthenticatorConfigs()[0]
                            .getName(), "test",
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

    @Test(alwaysRun = true, description = "Testing update Outbound Authenticator Configuration")
    public void testOutboundAuthenticatorConfiguration() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType
                    ("federated");
            AuthenticationStep authStep = new AuthenticationStep();
            IdentityProvider idp = new IdentityProvider();
            idp.setIdentityProviderName("fed_idp");
            FederatedAuthenticatorConfig customConfig = new FederatedAuthenticatorConfig();
            customConfig.setName("test_auth");
            customConfig.setEnabled(true);
            idp.setDefaultAuthenticatorConfig(customConfig);

            createIdp(idp);
            authStep.setFederatedIdentityProviders(new IdentityProvider[]{idp});

            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    new AuthenticationStep[]{authStep});

            serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .setSubjectClaimUri("subject_claim_uri");


            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);

            Assert.assertEquals(updatedServiceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getFederatedIdentityProviders()[0]
                    .getIdentityProviderName(), "fed_idp"
                    , "Failed update Authentication step");

            Assert.assertEquals(updatedServiceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getSubjectClaimUri(), "subject_claim_uri", "Failed update subject claim uri");

        } catch (Exception e) {
            Assert.fail("Error while trying to update Outbound Authenticator Configuration", e);
        }
    }

    private void createIdp(IdentityProvider idp) throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        IdentityProviderMgtServiceClient identityProviderMgtServiceClient
                = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider identityProvider
                = new org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider();

        identityProvider.setIdentityProviderName(idp.getIdentityProviderName());
        org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig
                federatedAuthenticatorConfig
                = new org.wso2.carbon.identity.application.common.model.idp.xsd.
                FederatedAuthenticatorConfig();

        federatedAuthenticatorConfig.setName(idp.getDefaultAuthenticatorConfig().getName());
        federatedAuthenticatorConfig.setEnabled(idp.getDefaultAuthenticatorConfig().getEnabled());

        identityProvider.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity
                .application.common.model
                .idp.xsd.FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);


        identityProviderMgtServiceClient.addIdP(identityProvider);
    }

    @Test(alwaysRun = true, description = "Testing update Application Permissions")
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

    @Test(alwaysRun = true, description = "Testing update Roles")
    public void testUpdateRoles() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            PermissionsAndRoleConfig permAndRoleConfig = new PermissionsAndRoleConfig();
            List<RoleMapping> roleMappingList = new ArrayList<RoleMapping>();

            RoleMapping mapping = new RoleMapping();
            LocalRole localRole = new LocalRole();
            localRole.setLocalRoleName("idpRole_1");
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
                    .getLocalRoleName(), "idpRole_1", "Failed update local role");

            Assert.assertEquals(updatedPermissionsAndRoleConfig.getRoleMappings()[0].
                            getRemoteRole(), "spRole_1",
                    "Failed update remote role");

        } catch (Exception e) {
            Assert.fail("Error while trying to update Roles", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update Claim Configuration")
    public void testUpdateClaimConfiguration() {
        String applicationName = "TestServiceProvider";

        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication
                    (applicationName);

            List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

            ClaimMapping mapping = new ClaimMapping();

            Claim localClaim = new Claim();
            localClaim.setClaimUri("idpClaim_1");

            Claim spClaim = new Claim();
            spClaim.setClaimUri("spClaim_1");

            mapping.setRequested(true);
            mapping.setLocalClaim(localClaim);
            mapping.setRemoteClaim(spClaim);

            claimMappingList.add(mapping);

            serviceProvider.getClaimConfig().setClaimMappings(
                    claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]));

            serviceProvider.getClaimConfig().setLocalClaimDialect(true);
            serviceProvider.getClaimConfig().setRoleClaimURI("roleClaim");
            serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(true);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient.
                    getApplication(applicationName);

            ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

            Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getLocalClaim().
                            getClaimUri(), "idpClaim_1",
                    "Failed update local claim uri");

            Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getRemoteClaim().
                            getClaimUri(), "spClaim_1",
                    "Failed update remote claim uri");

            Assert.assertEquals(updatedClaimConfig.getLocalClaimDialect(), true,
                    "Failed update localClaimDialect property.");

            Assert.assertEquals(updatedClaimConfig.getRoleClaimURI(), "roleClaim",
                    "Failed update role claim uri");

            Assert.assertEquals(updatedClaimConfig.getAlwaysSendMappedLocalSubjectId(), true,
                    "Failed update alwaysSendMappedLocalSubjectId property.");


        } catch (Exception e) {
            Assert.fail("Error while trying to update Claim Configuration", e);
        }
    }

    @BeforeMethod
    public void setUp() {
        createApplication("TestServiceProvider");
    }

    @AfterMethod
    public void tearDown() {
        deleteApplication("TestServiceProvider");
    }

    public void deleteApplication(String applicationName) {
        try {
            applicationManagementServiceClient.deleteApplication(applicationName);
        } catch (Exception e) {
            Assert.fail("Error while trying to delete Service Provider", e);
        }
    }

    private void createApplication(String applicationName) {
        try {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("This is a test Service Provider");
            applicationManagementServiceClient.createApplication(serviceProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }


}
