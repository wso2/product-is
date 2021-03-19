package org.wso2.identity.integration.test.base;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class GraalJsScriptEngineInitializerTestCase extends ISIntegrationTest {

    private ServerConfigurationManager scm;
    private File defaultConfigFile;

    @BeforeTest(alwaysRun = true)
    public void initScriptEngineConfig() throws Exception {

        super.init();
        String carbonHome = CarbonUtils.getCarbonHome();
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File scriptEngineConfigFile = new File(getISResourceLocation() + File.separator + "scriptengine"
                + File.separator + "graaljs_script_engine_config.toml");
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfiguration(scriptEngineConfigFile, defaultConfigFile, true, true);
    }

    @AfterTest(alwaysRun = true)
    public void resetScriptEngineConfig() throws Exception {

        super.init();
        scm.restoreToLastConfiguration(false);
    }
}
