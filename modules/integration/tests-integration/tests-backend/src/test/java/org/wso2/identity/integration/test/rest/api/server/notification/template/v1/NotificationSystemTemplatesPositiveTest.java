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

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SELF;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SUBJECT;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .COLLECTION_QUERY_BY_LOCALE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants
        .EMAIL_TEMPLATES_PATH;
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
public class NotificationSystemTemplatesPositiveTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_SYSTEM_EMAIL = "AccountConfirmation";
    private static final String TEMPLATE_TYPE_SYSTEM_SMS = "SMSOTP";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationSystemTemplatesPositiveTest(TestUserMode userMode) throws Exception {

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

    @DataProvider(name = "systemTemplateDataProvider")
    public static Object[][] systemTemplateDataProvider() {

        String testSystemEmailTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_EMAIL);
        String testSystemSMSTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_SMS);

        String emailTemplateRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemEmailTemplateTypeId + SYSTEM_TEMPLATES_PATH;
        String smsTemplateRequestPath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemSMSTemplateTypeId + SYSTEM_TEMPLATES_PATH;
        return new Object[][]{
                {emailTemplateRequestPath},
                {smsTemplateRequestPath}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "systemTemplateDataProvider")
    public void testGetTemplatesOfTemplateType(String requestPath) throws Exception {

        String collectionQueryByLocaleENUS = String.format(COLLECTION_QUERY_BY_LOCALE_TEMPLATE, LOCALE_EN_US);
        String resourcePathUS = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePathUS,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(requestPath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body("$", hasSize(1))
                .body(collectionQueryByLocaleENUS + ATTRIBUTE_SELF, equalTo(selfPath));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemSMSTemplateOfTemplateType()  {

        String testSystemSMSTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_SMS);
        String resourcePath = SMS_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemSMSTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_BODY, notNullValue(String.class))
                .body(ATTRIBUTE_LOCALE, equalTo(LOCALE_EN_US));
    }

    @Test(groups = "wso2.is")
    public void testGetSystemEmailTemplateOfTemplateType()  {

        String testSystemEmailTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM_EMAIL);
        String resourcePath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testSystemEmailTemplateTypeId + SYSTEM_TEMPLATES_PATH + PATH_SEPARATOR
                + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_CONTENT_TYPE, equalTo(ContentType.TEXT_HTML.getMimeType()))
                .body(ATTRIBUTE_SUBJECT, notNullValue(String.class))
                .body(ATTRIBUTE_BODY, notNullValue(String.class))
                .body(ATTRIBUTE_FOOTER, nullValue(String.class)) // No footer in the template
                .body(ATTRIBUTE_LOCALE, equalTo(LOCALE_EN_US));
    }
}
