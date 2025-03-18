package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.APP_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.CHANNEL_EMAIL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.CHANNEL_SMS;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.EMAIL_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_US;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ORG_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .PLACE_HOLDER_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .PLACE_HOLDER_DISPLAY_NAME;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .PLACE_HOLDER_SUBJECT;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .PLACE_HOLDER_TEMPLATE_TYPE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .RESET_TEMPLATE_TYPE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .SAMPLE_APPLICATION_UUID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.SMS_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.TEMPLATE_TYPES_PATH;

/**
 * Integration tests for Notification Template API.
 * Test class for Email Templates REST API positive paths.
 */
public class NotificationTemplateFunctionsPositiveTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_SYSTEM_EMAIL = "AccountConfirmation";
    private static final String TEMPLATE_TYPE_SYSTEM_SMS = "SMSOTP";
    private static final String TEMPLATE_TYPE_EMAIL = "function-email-positive-template-type";
    private static final String TEMPLATE_TYPE_SMS = "function-sms-positive-template-type";

    private static final String TEST_DATA_BODY = "Test Email Template Body";
    private static final String TEST_DATA_FOOTER = "Test Email Template Footer";
    private static final String TEST_DATA_SUBJECT = "Test Email Template Subject";

    private static String orgTemplateSystemEmailRequestPath, appTemplateSystemEmailRequestPath,
            orgTemplateEmailRequestPath, appTemplateEmailRequestPath, orgTemplateSmsRequestPath,
            appTemplateSmsRequestPath, orgTemplateSystemSmsRequestPath, appTemplateSystemSmsRequestPath;

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationTemplateFunctionsPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException, XPathExpressionException {

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

    @DataProvider(name = "systemTemplateDataProvider")
    public static Object[][] systemTemplateDataProvider() {

        return new Object[][]{
                { CHANNEL_EMAIL, TEMPLATE_TYPE_SYSTEM_EMAIL, orgTemplateSystemEmailRequestPath
                        ,appTemplateSystemEmailRequestPath},
                { CHANNEL_EMAIL, TEMPLATE_TYPE_EMAIL, orgTemplateEmailRequestPath, appTemplateEmailRequestPath},
                { CHANNEL_SMS, TEMPLATE_TYPE_SYSTEM_SMS, orgTemplateSystemSmsRequestPath
                        ,appTemplateSystemSmsRequestPath},
                { CHANNEL_SMS, TEMPLATE_TYPE_SMS, orgTemplateSmsRequestPath, appTemplateSmsRequestPath}
        };
    }

    @Test
    public void initTests() throws IOException {

        addTemplateType(TEMPLATE_TYPE_EMAIL, EMAIL_TEMPLATES_PATH);
        addTemplateType(TEMPLATE_TYPE_SMS, SMS_TEMPLATES_PATH);

        String emailTemplateRequestBodyTemplate = readResource("request-body-add-email-template.template");
        String emailTemplateRequestBody = emailTemplateRequestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .replace(PLACE_HOLDER_SUBJECT, TEST_DATA_SUBJECT)
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY)
                .replace(PLACE_HOLDER_FOOTER, TEST_DATA_FOOTER)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);

        String smsTemplateRequestBodyTemplate = readResource("request-body-add-sms-template.template");
        String smsTemplateRequestBody = smsTemplateRequestBodyTemplate
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);

        // Define custom email template for system template types
        orgTemplateSystemEmailRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SYSTEM_EMAIL) + ORG_TEMPLATES_PATH;
        addTemplate(orgTemplateSystemEmailRequestPath, emailTemplateRequestBody);
        appTemplateSystemEmailRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SYSTEM_EMAIL) + APP_TEMPLATES_PATH + PATH_SEPARATOR
                + SAMPLE_APPLICATION_UUID;
        addTemplate(appTemplateSystemEmailRequestPath, emailTemplateRequestBody);

        // Define custom email template for custom template types
        orgTemplateEmailRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_EMAIL) + ORG_TEMPLATES_PATH;
        addTemplate(orgTemplateEmailRequestPath, emailTemplateRequestBody);
        appTemplateEmailRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_EMAIL) + APP_TEMPLATES_PATH + PATH_SEPARATOR
                + SAMPLE_APPLICATION_UUID;
        addTemplate(appTemplateEmailRequestPath, emailTemplateRequestBody);

        // Define custom sms template for system template types
        orgTemplateSystemSmsRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SYSTEM_SMS) + ORG_TEMPLATES_PATH;
        addTemplate(orgTemplateSystemSmsRequestPath, smsTemplateRequestBody);
        appTemplateSystemSmsRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SYSTEM_SMS) + APP_TEMPLATES_PATH + PATH_SEPARATOR
                + SAMPLE_APPLICATION_UUID;
        addTemplate(appTemplateSystemSmsRequestPath, smsTemplateRequestBody);

        // Define custom sms template for custom template types
        orgTemplateSmsRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SMS) + ORG_TEMPLATES_PATH;
        addTemplate(orgTemplateSmsRequestPath, smsTemplateRequestBody);
        appTemplateSmsRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + base64String(TEMPLATE_TYPE_SMS) + APP_TEMPLATES_PATH + PATH_SEPARATOR
                + SAMPLE_APPLICATION_UUID;
        addTemplate(appTemplateSmsRequestPath, smsTemplateRequestBody);
    }

    @Test(groups = "wso2.is", dataProvider = "systemTemplateDataProvider", dependsOnMethods = "initTests")
    public void testResetTemplateType(String channel, String templateType, String orgTemplateResourcePath
            ,String appTemplateResourcePath) throws Exception {

        String templateTypeId = base64String(templateType);
        String requestBodyTemplate = readResource("request-body-reset-template-type.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_TEMPLATE_TYPE_ID, templateTypeId)
                .replace(PLACE_HOLDER_CHANNEL, channel);
        Response response = getResponseOfPost(RESET_TEMPLATE_TYPE_PATH, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // Verify that the template type is reset.
        getResponseOfGet(orgTemplateResourcePath)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body("$", hasSize(0));
        getResponseOfGet(appTemplateResourcePath)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body("$", hasSize(0));
    }

    private void addTemplateType(String templateTypeDisplayName, String channelBasePath)
            throws IOException {

        String requestBodyTemplate = readResource("request-body-add-template-type.template");
        String requestBody = requestBodyTemplate.replace(PLACE_HOLDER_DISPLAY_NAME, templateTypeDisplayName);
        String requestPath = channelBasePath + TEMPLATE_TYPES_PATH;
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    private void addTemplate(String requestPath, String requestBody) {

        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }
}
