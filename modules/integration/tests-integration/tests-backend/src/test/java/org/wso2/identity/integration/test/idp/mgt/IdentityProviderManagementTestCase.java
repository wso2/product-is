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
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.user.mgt.JDBCBasedUserMgtTestCase;
import org.wso2.identity.integration.test.user.store.JDBCUserStoreAddingTestCase;

import java.util.ArrayList;
import java.util.List;

public class IdentityProviderManagementTestCase extends ISIntegrationTest {

    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private UserManagementClient userMgtClient;
    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private AuthenticatorClient logManger;
    private String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        /*userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        Property[] properties = (new JDBCUserStoreManager()).getDefaultUserStoreProperties().getMandatoryProperties();
        PropertyDTO[] propertyDTOs = new PropertyDTO[properties.length];
        for (int i = 0; i < properties.length; i++) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(properties[i].getName());
            propertyDTO.setValue(properties[i].getValue());
            propertyDTOs[i] = propertyDTO;
        }
        UserStoreDTO userStoreDTO = userStoreConfigurationClient.createUserStoreDTO(jdbcClass, "indu.com", propertyDTOs);
        userStoreConfigurationClient.addUserStore(userStoreDTO);


        userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        logManger = new AuthenticatorClient(backendURL);

        userMgtClient.addRole("indu.com/umRole1", null, new String[] { "login" }, false);
        Assert.assertTrue(userMgtClient.roleNameExists("indu.com/umRole1"), "Role name doesn't exists");
        userMgtClient.addUser("indu.com/user1", "passWord1@", new String[]{"indu.com/umRole1"}, "default");
        Assert.assertTrue(userMgtClient.userNameExists("indu.com/umRole1", "indu.com/user1"), "User name doesn't exists");*/
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        identityProviderMgtServiceClient = null;
        /*userMgtClient.deleteUser("indu.com/user1");
        userMgtClient.deleteRole("indu.com/umRole1");*/
        logManger = null;
    }

    @Test(alwaysRun = true, description = "Testing create Identity Provider")
    public void testCreateIdp() {
        String idpName = "TestIdentityProvider1";
        try {
            createIdp(idpName);
            Assert.assertEquals(identityProviderMgtServiceClient.getIdPByName(idpName).getIdentityProviderName(),
                    idpName, "Failed to create an Identity Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to create a identity Provider", e);
        }
    }

/*    @Test(alwaysRun = true, description = "Testing create Identity Provider with role mappings")
    public void testCreateIdpWithRoleMappings() {
        String idpName = "TestIdentityProvider1";
        try {
            createIdpWithRoleMappings(idpName);
            Assert.assertEquals(identityProviderMgtServiceClient.getIdPByName(idpName).getIdentityProviderName(),
                    idpName, "Failed to create an Identity Provider");
        } catch (Exception e) {
            Assert.fail("Error while trying to create a identity Provider", e);
        }
    }*/

/*    private void createIdpWithRoleMappings(String idpName) {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(idpName);
            PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
            RoleMapping roleMapping = new RoleMapping();
            LocalRole localRole = new LocalRole();
            localRole.setLocalRoleName("umRole1");
            localRole.setUserStoreId("indu.com");
            roleMapping.setLocalRole(localRole);
            roleMapping.setRemoteRole("role1");
            permissionsAndRoleConfig.addRoleMappings(roleMapping);
            identityProvider.setPermissionAndRoleConfig(permissionsAndRoleConfig);
            identityProviderMgtServiceClient.addIdP(identityProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }*/

    private void createIdp(String idpName) {
        try {
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(idpName);
            identityProviderMgtServiceClient.addIdP(identityProvider);
        } catch (Exception e) {
            Assert.fail("Error while trying to create Service Provider", e);
        }
    }
}
