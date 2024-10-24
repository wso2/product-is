package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_DISPLAY_NAME;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_SELF;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.COLLECTION_QUERY_BY_ID_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.EMAIL_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.SMS_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.TEMPLATE_TYPES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_DISPLAY_NAME;

/**
 * Integration tests for Notification Template API.
 * Test class for Templates Types REST API positive paths.
 */
public class NotificationTypesPositiveTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_EMAIL = "email-positive-template-type";
    private static final String TEMPLATE_TYPE_SMS = "sms-positive-template-type";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationTypesPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }


    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "notificationChannelDataProvider")
    public static Object[][] notificationChannelDataProvider() {

        return new Object[][]{
                {TEMPLATE_TYPE_EMAIL, EMAIL_TEMPLATES_PATH},
                {TEMPLATE_TYPE_SMS, SMS_TEMPLATES_PATH}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "notificationChannelDataProvider")
    public void testAddTemplateType(String templateTypeDisplayName, String channelBasePath) throws Exception {

        String testTemplateTypeId = base64String(templateTypeDisplayName);
        String requestBodyTemplate = readResource("request-body-add-template-type.template");
        String requestBody = requestBodyTemplate.replace(PLACE_HOLDER_DISPLAY_NAME, templateTypeDisplayName);
        String requestPath = channelBasePath + TEMPLATE_TYPES_PATH;
        String resourcePath = requestPath + PATH_SEPARATOR + testTemplateTypeId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, containsString(selfPath))
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(templateTypeDisplayName))
                .body(ATTRIBUTE_ID, equalTo(testTemplateTypeId));
        Assert.assertEquals(response.as(Map.class).size(), 3, "Response body should have 3 attributes.");
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "notificationChannelDataProvider",
            dependsOnMethods = {"testAddTemplateType"})
    public void testGetTemplateTypesList(String templateTypeDisplayName, String channelBasePath) throws Exception {

        String testTemplateTypeId = base64String(templateTypeDisplayName);
        String requestPath = channelBasePath + TEMPLATE_TYPES_PATH;
        String resourcePath = requestPath + PATH_SEPARATOR + testTemplateTypeId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(requestPath);
        String collectionQueryById = String.format(COLLECTION_QUERY_BY_ID_TEMPLATE, testTemplateTypeId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body(collectionQueryById + ATTRIBUTE_DISPLAY_NAME, equalTo(templateTypeDisplayName))
                .body(collectionQueryById + ATTRIBUTE_SELF, equalTo(selfPath));
    }

    @Test(groups = "wso2.is",
            dataProvider = "notificationChannelDataProvider",
            dependsOnMethods = {"testAddTemplateType"})
    public void testGetEmailTemplateType(String templateTypeDisplayName, String channelBasePath) throws Exception {

        String testTemplateTypeId = base64String(templateTypeDisplayName);
        String resourcePath = channelBasePath + TEMPLATE_TYPES_PATH + PATH_SEPARATOR + testTemplateTypeId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_ID, equalTo(testTemplateTypeId))
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(templateTypeDisplayName))
                .body(ATTRIBUTE_SELF, equalTo(selfPath));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "notificationChannelDataProvider",
            dependsOnMethods = {"testAddTemplateType", "testGetEmailTemplateType", "testGetTemplateTypesList"})
    public void testDeleteEmailTemplateType(String templateTypeDisplayName, String channelBasePath) {

        String testTemplateId = base64String(templateTypeDisplayName);
        String resourcePath = channelBasePath + TEMPLATE_TYPES_PATH + PATH_SEPARATOR + testTemplateId;
        Response response = getResponseOfDelete(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // Verify that the template type is deleted.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
