package org.wso2.identity.integration.test.base;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.net.URL;

/**
 * Test class that will start and stop tomcat server for the tests in the test suite.
 *
 */
public class TomcatInitializerTestCase extends ISIntegrationTest {

    private static final String[] APPLICATIONS = {
            "travelocity.com",
            "travelocity.com-saml-tenantwithoutsigning",
            "travelocity.com-registrymount",
            "avis.com",
            "PassiveSTSSampleApp",
            "playground.appone",
            "playground.apptwo",
            "playground2",
            // TODO: Check and remove the following with openid tests
            "travelocity.com-openid-smartconsumerwithclaims",
            "travelocity.com-openid-smartconsumerwithoutclaims",
            "travelocity.com-openid-dumbconsumerwithclaims",
            "travelocity.com-openid-dumbconsumerwithoutclaims"
            // TODO: End of openid apps

    };
    private static final Log LOG = LogFactory.getLog(TomcatInitializerTestCase.class);

    private Tomcat tomcat;

    @BeforeSuite(alwaysRun = true)
    public void initTest() throws Exception {

        super.init();
        startTomcat();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownTest() throws Exception {

        super.init();
        stopTomcat();
    }

    private void startTomcat() throws LifecycleException {

        tomcat = Utils.getTomcat(getClass());
        for (String application : APPLICATIONS) {
            URL resourceUrl = getClass()
                    .getResource(File.separator + "samples" + File.separator + application + ".war");
            tomcat.addWebapp(tomcat.getHost(), "/" + application, resourceUrl.getPath());
            LOG.info("Deployed tomcat application " + application);
        }
        tomcat.start();
        LOG.info("Tomcat server started.");
    }

    private void stopTomcat() throws LifecycleException {

        tomcat.stop();
        tomcat.destroy();
        LOG.info("Tomcat server stopped.");
    }
}
