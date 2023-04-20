package org.wso2.identity.integration.test.rest.api.server.admin.advisory.management.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base test class for the Admin Advisory Management Rest APIs.
 */
public class AdminAdvisoryManagementTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "admin-advisory-management.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.admin.advisory.management.v1";
    public static final String ADMIN_ADVISORY_MGT_API_BASE_PATH = "/admin-advisory-management";
    public static final String ADMIN_ADVISORY_BANNER_PATH = "/banner";
    public static final boolean ENABLE_BANNER = false;
    public static final String BANNER_CONTENT = "Warning - unauthorized use of this tool is strictly prohibited. " +
            "All activities performed using this tool are logged and monitored.";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }
}
