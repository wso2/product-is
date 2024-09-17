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
import org.wso2.carbon.identity.rest.api.server.notification.template.v1.model.TemplateTypeWithID;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Integration tests for Notification Template API Email Templates.
 * Test class for Email Templates REST API positive paths.
 */
public class NotificationTemplateEmailTemplatesPositiveTest extends NotificationTemplateEmailTemplatesTestBase {

    private static final String BASE_PATH = "/api/server/v1/notification/email";
    private static final String SUB_PATH_TEMPLATE_TYPES = "/template-types";
    private static final String PLACE_HOLDER_DISPLAY_NAME = "{{displayName}}";
    private static final String DEFAULT_EMAIL_TEMPLATE_TYPE = "AccountConfirmation";
    private static final String TEST_EMAIL_TEMPLATE_TYPE = "integrationTestEmailTemplateType";
    private static final String COLLECTION_QUERY_BY_ID_TEMPLATE = "find{ it.id == '%s' }.";
    private static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
    private static final String ATTRIBUTE_SELF = "self";
    private static final String ATTRIBUTE_ID = "id";


    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationTemplateEmailTemplatesPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        // todo: init responses
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

    // Get all default email template types from the API and match.
    @Test
    public void givenValidRequest_whenGetEmailTemplateTypes_shouldReturnTemplateTypeWithIdList() throws Exception {

        Response response = getResponseOfGet(
                EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH);
        String collectionQueryById = String.format(COLLECTION_QUERY_BY_ID_TEMPLATE, base64String(
                DEFAULT_EMAIL_TEMPLATE_TYPE));
        String expectedSelfPath = String.format(BASE_PATH + SUB_PATH_TEMPLATE_TYPES + PATH_SEPARATOR
                        + STRING_PLACEHOLDER, base64String(DEFAULT_EMAIL_TEMPLATE_TYPE));
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body(collectionQueryById + ATTRIBUTE_DISPLAY_NAME, equalTo(DEFAULT_EMAIL_TEMPLATE_TYPE))
                .body(collectionQueryById + ATTRIBUTE_SELF, equalTo(getTenantedRelativePath(expectedSelfPath,
                        context.getContextTenant().getDomain())));
    }

    // Add email template type with a valid request.
    @Test
    public void givenValidRequest_whenAddEmailTemplateType_shouldReturnTemplateTypeWithId() throws Exception {

        String  requestBodyTemplate = readResource("request-post-email-template-type.template");
        String requestBody = requestBodyTemplate.replace(PLACE_HOLDER_DISPLAY_NAME, TEST_EMAIL_TEMPLATE_TYPE);
        String path = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        String expectedSelfPath = String.format(BASE_PATH + SUB_PATH_TEMPLATE_TYPES + PATH_SEPARATOR
                + STRING_PLACEHOLDER, base64String(TEST_EMAIL_TEMPLATE_TYPE));
        Response response = getResponseOfPost(path, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(TEST_EMAIL_TEMPLATE_TYPE))
                .body(ATTRIBUTE_SELF, containsString(expectedSelfPath))
                .body(ATTRIBUTE_ID, equalTo(base64String(TEST_EMAIL_TEMPLATE_TYPE)));
        Assert.assertEquals(response.as(Map.class).size(), 3, "Response body should only have 3 attributes.");
    }
}
