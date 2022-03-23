package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

public class ServiceProviderUserRoleValidationTestCase extends ISIntegrationTest {

    private static final String ROLE_VALIDATION_ENABLED_TOML = "role_validation_enabled.toml";
    private static final String APPLICATION_PREFIX = "app";
    private static final String SERVICE_PROVIDER_DESCRIPTION = "This is a test Service Provider for AZ test.";
    private static final String ROLE_PREFIX = "Application/role";
    private static final String USERNAME = "admin";
    private static final String[] ROLES = new String[]{"Application/role1", "Application/role2", "Application/role3"};
    private static final String APPLICATION_FILTER = "role*";
    private static final int APPLICATION_COUNT_WITHOUT_FILTER = 9;
    private static final int APPLICATION_COUNT_WITH_FILTER = 0;

    protected Log log = LogFactory.getLog(getClass());
    protected ApplicationManagementServiceClient applicationManagementServiceClient;
    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null, null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        createApplications();
        createApplicationRoles();
        updateAdminUser();
    }

    private void changeISConfiguration() throws IOException,
            XPathExpressionException, AutomationUtilException {

        log.info("Replacing the deployment.toml file to reduce items per page and to enable role validation.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File
                (getISResourceLocation() + File.separator + "application" + File.separator + "mgt" +
                        File.separator + ROLE_VALIDATION_ENABLED_TOML);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartForcefully();
    }

    private void createApplications() throws Exception {

        log.info("Creating the service providers required for the testcase.");
        for (int i = 1; i < 10; i++) {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(APPLICATION_PREFIX + i);
            serviceProvider.setDescription(SERVICE_PROVIDER_DESCRIPTION);
            applicationManagementServiceClient.createApplication(serviceProvider);
        }
    }

    private void deleteApplications() throws Exception {

        log.info("Deleting the service providers used during the testcase.");
        for (int i = 1; i < 10; i++) {
            applicationManagementServiceClient.deleteApplication(APPLICATION_PREFIX + i);
        }
    }

    private void createApplicationRoles() throws Exception {

        log.info("Creating the application roles required for the testcase.");
        for (int i = 1; i <= 5; i++) {
            remoteUSMServiceClient.addRole(ROLE_PREFIX + i, new String[0], null);
        }
    }

    private void deleteApplicationRoles() throws Exception {

        log.info("Deleting the application roles used during the testcase.");
        for (int i = 1; i <= 5; i++) {
            remoteUSMServiceClient.deleteRole(ROLE_PREFIX + i);
        }
    }

    private void updateAdminUser() throws Exception {

        log.info("Updating the role list of the user : " + USERNAME + ".");
        remoteUSMServiceClient.updateRoleListOfUser(USERNAME, null, ROLES);
    }

    @Test(description = "Retrieve application count for the admin user without filter.")
    public void testApplicationCountForUserWithoutFilter() throws Exception {

        int count = applicationManagementServiceClient.getCountOfAllApplications();
        Assert.assertEquals(count, APPLICATION_COUNT_WITHOUT_FILTER,
                String.format("The expected application count without a filter does not match the actual application " +
                                "count: Expected Application Count: %d Actual Application Count: %d.",
                        APPLICATION_COUNT_WITHOUT_FILTER, count)
        );
    }

    @Test(description = "Retrieve application count for the admin user with filter.")
    public void testApplicationCountForUserWithFilter() throws Exception {

        int count = applicationManagementServiceClient.getCountOfApplications(APPLICATION_FILTER);
        Assert.assertEquals(count, APPLICATION_COUNT_WITH_FILTER,
                String.format("The expected application count with a filter does not match the actual application " +
                                "count: Expected Application Count: %d Actual Application Count: %d.",
                        APPLICATION_COUNT_WITH_FILTER, count)
        );
    }

    @AfterClass(alwaysRun = true)
    public void clearObjects() throws Exception {

        deleteObjects();
        clear();
    }

    private void deleteObjects() throws Exception {

        deleteApplications();
        deleteApplicationRoles();
    }

    private void clear() {

        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
    }
}
