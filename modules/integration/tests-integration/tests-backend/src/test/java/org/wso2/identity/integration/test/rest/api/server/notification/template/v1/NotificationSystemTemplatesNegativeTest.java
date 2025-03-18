package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;

import static org.hamcrest.Matchers.any;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_CODE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_DESCRIPTION;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_MESSAGE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_TRACE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .EMAIL_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_CODE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_CODE_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_DESCRIPTION_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_MESSAGE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_MESSAGE_TEMPLATE_NOT_FOUND;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_US;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.SMS_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .SYSTEM_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.TEMPLATE_TYPES_PATH;

/**
 * Integration tests for Notification Template API.
 * Test class for Email Templates REST API positive paths.
 */
public class NotificationSystemTemplatesNegativeTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_SYSTEM_EMAIL = "AccountConfirmation";
    private static final String TEMPLATE_TYPE_SYSTEM_SMS = "SMSOTP";
    private static final String TEMPLATE_TYPE_SYSTEM_EMAIL_INVALID = "AccountConfirmationInvalid";
    private static final String TEMPLATE_TYPE_SYSTEM_SMS_INVALID = "SMSOTPInvalid";
    private static final String TEMPLATE_SYSTEM_LOCALE_INVALID = "sn_SL";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationSystemTemplatesNegativeTest(TestUserMode userMode) throws Exception {

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

    @DataProvider(name = "systemTemplateInvalidDataProvider")
    public static Object[][] systemTemplateInvalidDataProvider() {

        String testSystemEmailTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_EMAIL_INVALID);
        String testSystemSMSTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_SMS_INVALID);

        String emailTemplateRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemEmailTemplateTypeId + SYSTEM_TEMPLATES_PATH;
        String smsTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemSMSTemplateTypeId + SYSTEM_TEMPLATES_PATH;
        return new Object[][]{
                {emailTemplateRequestPath},
                {smsTemplateRequestPath}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "systemTemplateInvalidDataProvider")
    public void testGetTemplatesOfTemplateTypeWithInvalidTemplateType(String requestPath) {

        getResponseOfGet(requestPath)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, Matchers.equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, Matchers.equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, Matchers.equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemSMSTemplateOfTemplateTypeWithInvalidTemplateId() {

        String testSystemSMSTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_SMS);
        String resourcePath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemSMSTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + TEMPLATE_SYSTEM_LOCALE_INVALID;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, Matchers.equalTo(ERROR_CODE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_MESSAGE, Matchers.equalTo(ERROR_MESSAGE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_DESCRIPTION, Matchers.equalTo(ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemSMSTemplateOfTemplateTypeWithInvalidTemplateType() {

        String testSystemSMSTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_SMS_INVALID);
        String resourcePath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemSMSTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, Matchers.equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, Matchers.equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, Matchers.equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemEmailTemplateOfTemplateTypeWithInvalidTemplateId()  {

        String testSystemEmailTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_EMAIL);
        String resourcePath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemEmailTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + TEMPLATE_SYSTEM_LOCALE_INVALID;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, Matchers.equalTo(ERROR_CODE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_MESSAGE, Matchers.equalTo(ERROR_MESSAGE_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_DESCRIPTION, Matchers.equalTo(ERROR_DESCRIPTION_TEMPLATE_NOT_FOUND))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemEmailTemplateOfTemplateTypeWithInvalidTemplateType()  {

        String testSystemEmailTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_EMAIL_INVALID);
        String resourcePath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemEmailTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(ATTRIBUTE_CODE, Matchers.equalTo(ERROR_CODE_INVALID_TYPE))
                .body(ATTRIBUTE_MESSAGE, Matchers.equalTo(ERROR_MESSAGE_INVALID_TYPE))
                .body(ATTRIBUTE_DESCRIPTION, Matchers.equalTo(ERROR_DESCRIPTION_INVALID_TYPE))
                .body(ATTRIBUTE_TRACE_ID, any(String.class));
    }
}
