package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_CODE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_DESCRIPTION;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_MESSAGE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_TRACE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.CHANNEL_EMAIL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.CHANNEL_SMS;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_CODE_INVALID_NOTIFICATION_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_CODE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_DESCRIPTION_INVALID_NOTIFICATION_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_DESCRIPTION_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_MESSAGE_INVALID_NOTIFICATION_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ERROR_MESSAGE_INVALID_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_CHANNEL;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .PLACE_HOLDER_TEMPLATE_TYPE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .RESET_TEMPLATE_TYPE_PATH;

/**
 * Integration tests for Notification Template API.
 * Test class for Email Templates REST API positive paths.
 */
public class NotificationTemplateFunctionsNegativeTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_INVALID = "InvalidTemplateType";
    private static final String NOTIFICATION_CHANNEL_INVALID = "InvalidNotificationChannel";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationTemplateFunctionsNegativeTest(TestUserMode userMode) throws Exception {

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

    @DataProvider(name = "systemTemplateInvalidDataProvider")
    public static Object[][] systemTemplateDataProvider() {

        return new Object[][] {
                //Invalid type id SMS
                { CHANNEL_SMS, TEMPLATE_TYPE_INVALID, HttpStatus.SC_NOT_FOUND, equalTo(ERROR_CODE_INVALID_TYPE)
                        ,equalTo(ERROR_MESSAGE_INVALID_TYPE), equalTo(ERROR_DESCRIPTION_INVALID_TYPE),
                        any(String.class) },
                //Invalid type id EMAIL
                { CHANNEL_EMAIL, TEMPLATE_TYPE_INVALID, HttpStatus.SC_NOT_FOUND, equalTo(ERROR_CODE_INVALID_TYPE)
                        ,equalTo(ERROR_MESSAGE_INVALID_TYPE), equalTo(ERROR_DESCRIPTION_INVALID_TYPE),
                        any(String.class) },
                // Invalid channel
                { NOTIFICATION_CHANNEL_INVALID, TEMPLATE_TYPE_INVALID, HttpStatus.SC_BAD_REQUEST,
                        equalTo(ERROR_CODE_INVALID_NOTIFICATION_CHANNEL)
                        ,equalTo(ERROR_MESSAGE_INVALID_NOTIFICATION_CHANNEL),
                        equalTo(ERROR_DESCRIPTION_INVALID_NOTIFICATION_CHANNEL),
                        any(String.class) },
        };
    }

    @Test(groups = "wso2.is", dataProvider = "systemTemplateInvalidDataProvider")
    public void testResetTemplateType(String channel, String templateType, int expectedStatus,
                                      Matcher<String> codeMatcher, Matcher<String> messageMatcher,
                                      Matcher<String> descriptionMatcher, Matcher<String> traceIdMatcher)
            throws Exception {

        String templateTypeId = base64String(templateType);
        String requestBodyTemplate = readResource("request-body-reset-template-type.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_TEMPLATE_TYPE_ID, templateTypeId)
                .replace(PLACE_HOLDER_CHANNEL, channel);
        Response response = getResponseOfPost(RESET_TEMPLATE_TYPE_PATH, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(expectedStatus)
                .body(ATTRIBUTE_CODE, codeMatcher)
                .body(ATTRIBUTE_MESSAGE, messageMatcher)
                .body(ATTRIBUTE_DESCRIPTION, descriptionMatcher)
                .body(ATTRIBUTE_TRACE_ID, traceIdMatcher);
    }
}
