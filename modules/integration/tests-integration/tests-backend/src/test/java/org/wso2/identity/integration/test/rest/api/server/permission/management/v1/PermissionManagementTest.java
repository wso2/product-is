package org.wso2.identity.integration.test.rest.api.server.permission.management.v1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * The main test class for permission management.
 */
public class PermissionManagementTest extends RESTAPIServerTestBase {

    private static final String API_DEFINITION_NAME = "permission-management.yaml";
    static final String API_VERSION = "v1";
    private static String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.permission.management.v1";
    public static final String PERMISSION_MANAGEMENT_ENDPOINT = "/permission-management";
    public static final String PERMISSIONS_ENDPOINT = "/permissions";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public PermissionManagementTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws AxisFault {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Test
    public void testGetPermissions() throws JSONException {

        getResponseOfGet(PERMISSION_MANAGEMENT_ENDPOINT + PERMISSIONS_ENDPOINT)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .contentType(ContentType.JSON)
                .log().ifValidationFails()
                .extract().response().body();
    }

}
