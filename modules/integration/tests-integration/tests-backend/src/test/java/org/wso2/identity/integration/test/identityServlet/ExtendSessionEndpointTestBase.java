package org.wso2.identity.integration.test.identityServlet;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

/**
 * Base test class for Session Extender Endpoint.
 */
public class ExtendSessionEndpointTestBase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        changeISConfiguration();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        resetISConfiguration();
    }

    private void changeISConfiguration() throws Exception {

        log.info("Replacing deployment.toml to enable session extender endpoint.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator + "oauth" +
                File.separator + "session_extender_enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing deployment.toml with default configurations.");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
