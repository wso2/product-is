package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.APP_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_CODE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_DESCRIPTION;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_MESSAGE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_TRACE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.CHANNEL_SMS;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_CODE_DUPLICATE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_CODE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_CODE_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_DESCRIPTION_DUPLICATE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_DESCRIPTION_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_MESSAGE_DUPLICATE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_MESSAGE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ERROR_MESSAGE_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_TEMPLATE_TYPE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.RESET_TEMPLATE_TYPE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.SAMPLE_APPLICATION_UUID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.SMS_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.TEMPLATE_TYPES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_US;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ORG_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_LOCALE;

/**
 * Integration tests for Notification Template API.
 * Test class for SMS Templates REST API positive paths.
 */
public class NotificationSMSTemplatesNegativeTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_VALID = "SMSOTP";
    private static final String TEMPLATE_TYPE_INVALID = "SMSOTP_INVALID";

    private static final String TEST_DATA_BODY_1 = "Test SMS Template Body 1";
    private static final String TEST_DATA_BODY_2 = "Test SMS Template Body 2";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationSMSTemplatesNegativeTest(TestUserMode userMode) throws Exception {

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

    @BeforeMethod(alwaysRun = true)
    public void resetTemplateType() throws IOException {

        String templateTypeId = base64String(TEMPLATE_TYPE_VALID);
        String requestBodyTemplate = readResource("request-body-reset-template-type.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_TEMPLATE_TYPE_ID, templateTypeId)
                .replace(PLACE_HOLDER_CHANNEL, CHANNEL_SMS);
        getResponseOfPost(RESET_TEMPLATE_TYPE_PATH, requestBody)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "smsTemplateValidDataProvider")
    public static Object[][] smsTemplateDataProvider() {

        String testTemplateTypeId = base64String(TEMPLATE_TYPE_VALID);
        String orgTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_TEMPLATES_PATH;
        String appTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + APP_TEMPLATES_PATH + PATH_SEPARATOR + SAMPLE_APPLICATION_UUID;
        return new Object[][]{
                {orgTemplateRequestPath},
                {appTemplateRequestPath}
        };
    }

    @DataProvider(name = "smsTemplateInvalidTypeDataProvider")
    public static Object[][] smsTemplateInvalidTypeDataProvider() {

        String testTemplateTypeId = base64String(TEMPLATE_TYPE_INVALID);
        String orgTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_TEMPLATES_PATH;
        String appTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + APP_TEMPLATES_PATH + PATH_SEPARATOR + SAMPLE_APPLICATION_UUID;
        return new Object[][]{
                {orgTemplateRequestPath},
                {appTemplateRequestPath}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "smsTemplateValidDataProvider")
    public void testAddTemplateWithDuplicateData(String requestPath) throws Exception {

        String requestBodyTemplate = readResource("request-body-add-sms-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_1)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);
        getResponseOfPost(requestPath, requestBody).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        getResponseOfPost(requestPath, requestBody).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_DUPLICATE_TEMPLATE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_DUPLICATE_TEMPLATE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_DUPLICATE_TEMPLATE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(groups = "wso2.is", dataProvider = "smsTemplateInvalidTypeDataProvider")
    public void testAddTemplateWithInvalidTemplateType(String requestPath) throws Exception {

        String requestBodyTemplate = readResource("request-body-add-sms-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_1)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);
        getResponseOfPost(requestPath, requestBody).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateInvalidTypeDataProvider")
    public void testGetTemplatesOfTemplateTypeWithInvalidTemplateType(String requestPath) {

        getResponseOfGet(requestPath).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateValidDataProvider")
    public void testGetTemplateOfTemplateTypeWithNonExistingLocale(String requestPath)  {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateInvalidTypeDataProvider")
    public void testGetTemplateOfTemplateTypeWithInvalidType(String requestPath)  {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateInvalidTypeDataProvider")
    public void testUpdateTemplateWithInvalidTemplateType(String requestPath) throws Exception {

        String requestBodyTemplate = readResource("request-body-update-sms-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_2);
        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfPut(resourcePath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateValidDataProvider")
    public void testUpdateTemplateWithInvalidLocale(String requestPath) throws Exception {

        String requestBodyTemplate = readResource("request-body-update-sms-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_2);
        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        getResponseOfPut(resourcePath, requestBody).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateDataProvider")
    public void testDeleteTemplateWithNonExistingLocale(String requestPath) {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        getResponseOfGet(resourcePath).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "smsTemplateInvalidTypeDataProvider")
    public void testDeleteTemplateWithNonInvalidTemplateType(String requestPath) {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        getResponseOfGet(resourcePath).then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }
}
