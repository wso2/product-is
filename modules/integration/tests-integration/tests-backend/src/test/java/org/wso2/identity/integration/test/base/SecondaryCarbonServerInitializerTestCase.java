package org.wso2.identity.integration.test.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;

/**
 * Test class that will start and stop secondary carbon server for the tests in the test suite.
 * This can be used once per test as belows.
 *
 *     <test name="is-test-xxxxx" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.SecondaryCarbonServerInitializerTestCase"/>
 *             <class name="testClassX1"/>
 *             <class name="testClassX2"/>
 *             <class name="testClassX3"/>
 *         </classes>
 *     </test>
 *     <test name="is-test-yyyyy" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.SecondaryCarbonServerInitializerTestCase"/>
 *             <class name="testClassY1"/>
 *             <class name="testClassY2"/>
 *             <class name="testClassY3"/>
 *         </classes>
 *     </test>
 */
public class SecondaryCarbonServerInitializerTestCase extends AbstractIdentityFederationTestCase {

    private static final Log LOG = LogFactory.getLog(SecondaryCarbonServerInitializerTestCase.class);
    private static final int PORT_OFFSET_1 = 1;
    private ServerConfigurationManager serverConfigurationManager;
    private static final String DEFAULT_H2_DATABASE_CONFIG = "default_configs_with_h2_db.toml";

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        LOG.info("Starting secondary carbon server...");
        TestDataHolder testDataHolder = TestDataHolder.getInstance();
        Map<String, String> startupParameters = new HashMap<>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        testDataHolder.setAutomationContext(new AutomationContext("IDENTITY", "identity002", TestUserMode
                .SUPER_TENANT_ADMIN));
        startCarbonServer(PORT_OFFSET_1, testDataHolder.getAutomationContext(), startupParameters);

        /*
        When tests are executed under different profiles, the started secondary server above, might use the same
        database as used in the already started initial identity server. This is because under certain profiles, the
        server is configured to use the database defined in the env variables, and those variables are reused for the
        secondary server as well(ex: test-grid profile scenario). But the secondary server is used for the
        provisioning purposes. Thus it does not need to have configurable databases for the secondary server, and
        need to make sure that it's database components are properly isolated from the initial testing identity server.
         */
        changeServerConfiguration(DEFAULT_H2_DATABASE_CONFIG, testDataHolder.getAutomationContext());
        LOG.info("Secondary carbon server started.");
    }

    @AfterTest(alwaysRun = true)
    public void tearDownTest() throws Exception {

        LOG.info("Stopping secondary carbon server...");
        try {
            super.stopCarbonServer(PORT_OFFSET_1);
            LOG.info("Secondary carbon server stopped.");
        } catch (AutomationFrameworkException e1) {
            LOG.error("Error occurred while shutting down the server. ", e1);
        } catch (Exception e) {
            LOG.error("Error while stopping secondary carbon server.", e);
            Assert.fail("Error while stopping secondary carbon server.");
            throw e;
        }
    }

    private void changeServerConfiguration(String fileName, AutomationContext server) throws IOException,
            XPathExpressionException, AutomationUtilException {

        log.info("Using the embedded H2 database for the secondary server.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File
                (getISResourceLocation() + File.separator + "provisioning" + File.separator + fileName);
        serverConfigurationManager = new ServerConfigurationManager(server);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }
}
