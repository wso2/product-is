package org.wso2.identity.integration.test.base;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.net.URL;

/**
 * Test class that will start and stop tomcat server for the tests in the test suite. This can be used once per test
 * as belows.
 *
 *     <test name="is-test-xxxxx" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.TomcatInitializerTestCase"/>
 *             <class name="testClassX1"/>
 *             <class name="testClassX2"/>
 *             <class name="testClassX3"/>
 *         </classes>
 *     </test>
 *     <test name="is-test-yyyyy" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.TomcatInitializerTestCase"/>
 *             <class name="testClassY1"/>
 *             <class name="testClassY2"/>
 *             <class name="testClassY3"/>
 *         </classes>
 *     </test>
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

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {

        super.init();
        startTomcat();
    }

    @AfterTest(alwaysRun = true)
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
