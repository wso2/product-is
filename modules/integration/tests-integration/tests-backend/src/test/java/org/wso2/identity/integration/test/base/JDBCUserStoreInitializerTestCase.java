package org.wso2.identity.integration.test.base;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class JDBCUserStoreInitializerTestCase extends ISIntegrationTest {

    private ServerConfigurationManager scm;
    private File userMgtServerFile;

    @BeforeTest(alwaysRun = true)
    public void initUserStoreConfig() throws Exception {

        super.init();

        String carbonHome = CarbonUtils.getCarbonHome();
        userMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "user-mgt.xml");
        File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
                + File.separator + "JdbcUserMgtConfig.xml");

        scm = new ServerConfigurationManager(isServer);
        scm.applyConfiguration(userMgtConfigFile, userMgtServerFile, true, true);
    }

    @AfterTest(alwaysRun = true)
    public void resetUserstoreConfig() throws Exception {

        super.init();
        scm.restoreToLastConfiguration(false);
    }

}
