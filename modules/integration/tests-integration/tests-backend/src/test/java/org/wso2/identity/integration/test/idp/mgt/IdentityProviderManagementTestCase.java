package org.wso2.identity.integration.test.idp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.LocalRole;
import org.wso2.carbon.identity.application.common.model.idp.xsd.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.RoleMapping;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class IdentityProviderManagementTestCase extends ISIntegrationTest {

    public static final String TEST_IDENTITY_PROVIDER_NAME_1 = "TestIdentityProvider1";
    public static final String TEST_IDENTITY_PROVIDER_UPDATED_NAME_1 = "TestIdentityProvider1_updated";
    public static final String TEST_IDENTITY_PROVIDER_NAME_2 = "TestIdentityProvider2";
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private UserManagementClient userMgtClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());

        userMgtClient.addRole("umRole1", null, new String[]{"login"}, false);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        identityProviderMgtServiceClient.deleteIdP(TEST_IDENTITY_PROVIDER_UPDATED_NAME_1);
        identityProviderMgtServiceClient.deleteIdP(TEST_IDENTITY_PROVIDER_NAME_2);
        identityProviderMgtServiceClient = null;
        userMgtClient.deleteRole("umRole1");
    }

    @Test(alwaysRun = true, description = "Testing create Identity Provider")
    public void createIdpTest() {

        try {
            createIdp(TEST_IDENTITY_PROVIDER_NAME_1);
            Assert.assertEquals(identityProviderMgtServiceClient.getIdPByName(TEST_IDENTITY_PROVIDER_NAME_1)
                            .getIdentityProviderName(),
                    TEST_IDENTITY_PROVIDER_NAME_1, "Failed to create an Identity Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to create an identity Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update Identity Provider", dependsOnMethods = {"createIdpTest", "testCreateIdpWithRoleMappings"})
    public void updateIdpTest() {

        try {
            updateIdp(TEST_IDENTITY_PROVIDER_NAME_1, TEST_IDENTITY_PROVIDER_UPDATED_NAME_1);
            Assert.assertEquals(identityProviderMgtServiceClient.getIdPByName(TEST_IDENTITY_PROVIDER_UPDATED_NAME_1)
                            .getIdentityProviderName(),
                    TEST_IDENTITY_PROVIDER_UPDATED_NAME_1, "Failed to update an Identity Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to update an identity Provider", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing create Identity Provider with role mappings")
    public void testCreateIdpWithRoleMappings() {
        try {
            createIdpWithRoleMappings(TEST_IDENTITY_PROVIDER_NAME_2);
            Assert.assertEquals(identityProviderMgtServiceClient.getIdPByName(TEST_IDENTITY_PROVIDER_NAME_2).getIdentityProviderName(),
                    TEST_IDENTITY_PROVIDER_NAME_2, "Failed to create an Identity Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to create a identity Provider", e);
        }
    }

    private void createIdp(String idpName) {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(idpName);
            identityProviderMgtServiceClient.addIdP(identityProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }

    private void updateIdp(String oldIdpName, String newIdpName) {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(newIdpName);
            identityProviderMgtServiceClient.updateIdP(oldIdpName, identityProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to update Service Provider", e);
        }
    }

    private void createIdpWithRoleMappings(String idpName) {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(idpName);
            PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
            RoleMapping roleMapping = new RoleMapping();
            LocalRole localRole = new LocalRole();
            localRole.setLocalRoleName("umRole1");
            localRole.setUserStoreId("primary");
            roleMapping.setLocalRole(localRole);
            roleMapping.setRemoteRole("role1");
            permissionsAndRoleConfig.addRoleMappings(roleMapping);
            identityProvider.setPermissionAndRoleConfig(permissionsAndRoleConfig);
            identityProviderMgtServiceClient.addIdP(identityProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create identity provider", e);
        }
    }
}
