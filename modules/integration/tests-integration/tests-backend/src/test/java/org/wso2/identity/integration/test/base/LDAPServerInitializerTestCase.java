package org.wso2.identity.integration.test.base;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.ExternalLDAPServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class LDAPServerInitializerTestCase extends ISIntegrationTest {

    private static final Log LOG = LogFactory.getLog(LDAPServerInitializerTestCase.class);
    private static final String workingDirectoryPath = "ldapWorkingDirectory";
    private static final int ldapServerPort = 10389;
    private static final int offset = 410;
    private ExternalLDAPServer ldapServer;

    @BeforeSuite(alwaysRun = true)
    public void initTest() throws Exception {
        try {

            File workDir = new File(workingDirectoryPath);
            if (workDir.exists()){
                FileUtils.deleteDirectory(workDir);
            }
            workDir.mkdir();

            String schemaZipFilePath = getISResourceLocation() + File.separator + "is-default-schema.zip";

            ldapServer = new ExternalLDAPServer(workingDirectoryPath, schemaZipFilePath,
                    ldapServerPort + offset);
            ldapServer.init();
            ldapServer.startServer(true);

            LOG.info("External LDAP is started.");

        } catch (Exception e) {
            throw new Exception("Failed to start external LDAP.", e);
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownTest() throws Exception {

        try {
            ldapServer.stopServer();
        } catch (Exception e) {
            throw new Exception("Failed to stop external LDAP.", e);
        }
    }
}
