package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;
import java.util.Base64;

public class NotificationTemplatesTestBase extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "notification-template.yml";
    static final String API_VERSION = "v1";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.server.notification.template.v1";

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

    protected static String base64String(String value) {

        return Base64.getEncoder().withoutPadding().encodeToString(value.getBytes());
    }
}
