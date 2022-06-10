package org.wso2.identity.integration.test.base;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class LDAPUserStoreInitializerTestCase extends ISIntegrationTest {

    private ServerConfigurationManager scm;
    private File defaultConfigFile;

    @BeforeTest(alwaysRun = true)
    public void initUserStoreConfig() throws Exception {

        super.init();
        String carbonHome = CarbonUtils.getCarbonHome();
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                + File.separator + "ldap_user_mgt_config.toml");
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfiguration(userMgtConfigFile, defaultConfigFile, true, true);
    }

    @AfterTest(alwaysRun = true)
    public void resetUserstoreConfig() throws Exception {

        super.init();
        scm.restoreToLastConfiguration(false);
        scm.restartGracefully();
    }
}
