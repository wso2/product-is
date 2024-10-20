package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.
        Constants.ATTRIBUTE_DISPLAY_NAME;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_ID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SELF;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SUBJECT;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.COLLECTION_QUERY_BY_ID_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.COLLECTION_QUERY_BY_LOCALE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.EMAIL_TEMPLATES_BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.EMAIL_TEMPLATE_TYPES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_AU;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_US;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ORG_EMAIL_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_DISPLAY_NAME;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_SUBJECT;

/**
 * Integration tests for Notification Template API Email Templates.
 * Test class for Organization Email Templates REST API positive paths.
 */
public class NotificationTemplateOrganizationEmailTemplatesPositiveTest extends NotificationTemplateEmailTemplatesTestBase {

    private static final String TEMPLATE_TYPE_ADD_TEMPLATE = "email-positive-add-org-template";
    private static final String TEMPLATE_TYPE_ADD_TEMPLATE_TYPE = "email-positive-add-template-type";
    private static final String TEMPLATE_TYPE_DELETE_TEMPLATE_TYPE = "email-positive-delete-template-type";
    private static final String TEMPLATE_TYPE_DELETE_TEMPLATE = "email-positive-delete-org-template";
    private static final String TEMPLATE_TYPE_GET_TEMPLATE_LIST = "email-positive-get-org-template-list";
    private static final String TEMPLATE_TYPE_GET_TEMPLATE = "email-positive-get-org-template";
    private static final String TEMPLATE_TYPE_SYSTEM = "AccountConfirmation";
    private static final String TEMPLATE_TYPE_UPDATE_TEMPLATE = "email-positive-update-org-template";

    private static final String TEST_DATA_BODY_1 = "Test Org Email Template Body 1";
    private static final String TEST_DATA_BODY_2 = "Test Org Email Template Body 2";
    private static final String TEST_DATA_FOOTER_1 = "Test Org Email Template Footer 1";
    private static final String TEST_DATA_FOOTER_2 = "Test Org Email Template Footer 2";
    private static final String TEST_DATA_SUBJECT_1 = "Test Org Email Template 1";
    private static final String TEST_DATA_SUBJECT_2 = "Test Org Email Template 2";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationTemplateOrganizationEmailTemplatesPositiveTest(TestUserMode userMode) throws Exception {

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

    /**
     * Test get email template types list.
     * 
     * @throws Exception Any error in the process.
     */
    @Test
    public void testGetEmailTemplateTypesList() throws Exception {

        String testTemplateId = base64String(TEMPLATE_TYPE_SYSTEM);
        String requestPath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        String resourcePath = requestPath + PATH_SEPARATOR + testTemplateId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(requestPath);
        String collectionQueryById = String.format(COLLECTION_QUERY_BY_ID_TEMPLATE, testTemplateId);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body(collectionQueryById + ATTRIBUTE_DISPLAY_NAME, equalTo(TEMPLATE_TYPE_SYSTEM))
                .body(collectionQueryById + ATTRIBUTE_SELF, equalTo(selfPath));
    }

    @Test
    public void testGetEmailTemplateType() throws Exception {

        String testTemplateId = base64String(TEMPLATE_TYPE_SYSTEM);
        String resourcePath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR + testTemplateId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_ID, equalTo(testTemplateId))
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(TEMPLATE_TYPE_SYSTEM))
                .body(ATTRIBUTE_SELF, equalTo(selfPath));
    }

    @Test
    public void testAddEmailTemplateType() throws Exception {

        String testTemplateTypeId = base64String(TEMPLATE_TYPE_ADD_TEMPLATE_TYPE);
        String requestBodyTemplate = readResource("request-body-add-template-type.template");
        String requestBody = requestBodyTemplate.replace(PLACE_HOLDER_DISPLAY_NAME, TEMPLATE_TYPE_ADD_TEMPLATE_TYPE);
        String requestPath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        String resourcePath = requestPath + PATH_SEPARATOR + testTemplateTypeId;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, containsString(selfPath))
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(TEMPLATE_TYPE_ADD_TEMPLATE_TYPE))
                .body(ATTRIBUTE_ID, equalTo(testTemplateTypeId));
        Assert.assertEquals(response.as(Map.class).size(), 3, "Response body should have 3 attributes.");
        // Verify that the template type is added.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_DISPLAY_NAME, equalTo(TEMPLATE_TYPE_ADD_TEMPLATE_TYPE))
                .body(ATTRIBUTE_SELF, equalTo(selfPath))
                .body(ATTRIBUTE_ID, equalTo(testTemplateTypeId));
    }

    // Delete email template type.
    @Test
    public void testDeleteEmailTemplateType() throws Exception {

        String testTemplateId = base64String(TEMPLATE_TYPE_DELETE_TEMPLATE_TYPE);
        String resourcePath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR + testTemplateId;
        addEmailTemplateType(TEMPLATE_TYPE_DELETE_TEMPLATE_TYPE);
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

    @Test
    public void testAddOrganizationEmailTemplate() throws Exception {

        // Add email template type for the test.
        addEmailTemplateType(TEMPLATE_TYPE_ADD_TEMPLATE);
        // Add the template.
        String testTemplateTypeId = base64String(TEMPLATE_TYPE_ADD_TEMPLATE);
        String requestBodyTemplate = readResource("request-body-add-org-email-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .replace(PLACE_HOLDER_SUBJECT, TEST_DATA_SUBJECT_1)
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_1)
                .replace(PLACE_HOLDER_FOOTER, TEST_DATA_FOOTER_1)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);
        String requestPath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_EMAIL_TEMPLATES_PATH;
        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body(ATTRIBUTE_LOCALE, equalTo(LOCALE_EN_US))
                .body(ATTRIBUTE_SELF, equalTo(selfPath));
        // Verify that the template is added.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testGetOrgTemplatesListOfEmailTemplateType() throws Exception {

        // Add email template type for the test.
        addEmailTemplateType(TEMPLATE_TYPE_GET_TEMPLATE_LIST);
        // Add the templates to retrieve in the list.
        addEmailTemplate(TEMPLATE_TYPE_GET_TEMPLATE_LIST, ContentType.TEXT_HTML.getMimeType(),
                TEST_DATA_SUBJECT_1, TEST_DATA_BODY_1, TEST_DATA_FOOTER_1,
                LOCALE_EN_US);
        addEmailTemplate(TEMPLATE_TYPE_GET_TEMPLATE_LIST, ContentType.TEXT_HTML.getMimeType(),
                TEST_DATA_SUBJECT_2, TEST_DATA_BODY_2, TEST_DATA_FOOTER_2,
                LOCALE_EN_AU);
        // Retrieve the list of templates.
        String testTemplateTypeId = base64String(TEMPLATE_TYPE_GET_TEMPLATE_LIST);
        String collectionQueryByLocaleENUS = String.format(COLLECTION_QUERY_BY_LOCALE_TEMPLATE, LOCALE_EN_US);
        String collectionQueryByLocaleENAU= String.format(COLLECTION_QUERY_BY_LOCALE_TEMPLATE, LOCALE_EN_AU);
        String requestPath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + testTemplateTypeId + ORG_EMAIL_TEMPLATES_PATH;
        String resourcePathUS = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        String resourcePathAU = requestPath + PATH_SEPARATOR + LOCALE_EN_AU;
        String selfPathUS = getTenantedRelativePath(BASE_PATH + resourcePathUS,
                context.getContextTenant().getDomain());
        String selfPathAU = getTenantedRelativePath(BASE_PATH + resourcePathAU,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(requestPath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body("$", hasSize(2))
                .body(collectionQueryByLocaleENUS + ATTRIBUTE_SELF, equalTo(selfPathUS))
                .body(collectionQueryByLocaleENAU + ATTRIBUTE_SELF, equalTo(selfPathAU));
    }

    @Test
    public void testGetOrganizationEmailTemplateOfEmailTemplateType() throws Exception {

        // Add email template type for the test.
        addEmailTemplateType(TEMPLATE_TYPE_GET_TEMPLATE);
        // Add the template to retrieve.
        addEmailTemplate(TEMPLATE_TYPE_GET_TEMPLATE, ContentType.TEXT_HTML.getMimeType(),
                TEST_DATA_SUBJECT_1, TEST_DATA_BODY_1, TEST_DATA_FOOTER_1,
                LOCALE_EN_US);
        // Retrieve the template.
        String testTemplateTypeId = base64String(TEMPLATE_TYPE_GET_TEMPLATE);
        String resourcePath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + testTemplateTypeId + ORG_EMAIL_TEMPLATES_PATH +
                PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_CONTENT_TYPE, equalTo(ContentType.TEXT_HTML.getMimeType()))
                .body(ATTRIBUTE_SUBJECT, equalTo(TEST_DATA_SUBJECT_1))
                .body(ATTRIBUTE_BODY, equalTo(TEST_DATA_BODY_1))
                .body(ATTRIBUTE_FOOTER, equalTo(TEST_DATA_FOOTER_1));
    }

    @Test
    public void testUpdateOrganizationEmailTemplate() throws Exception {

        // Add email template type for the test.
        addEmailTemplateType(TEMPLATE_TYPE_UPDATE_TEMPLATE);
        // Add the template to update in the test.
        addEmailTemplate(TEMPLATE_TYPE_UPDATE_TEMPLATE, ContentType.TEXT_HTML.getMimeType(),
                TEST_DATA_SUBJECT_1, TEST_DATA_BODY_1, TEST_DATA_FOOTER_1,
                LOCALE_EN_US);
        // Update the template.
        String testTemplateTypeId = base64String(TEMPLATE_TYPE_UPDATE_TEMPLATE);
        String requestBodyTemplate = readResource("request-body-update-org-email-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .replace(PLACE_HOLDER_SUBJECT, TEST_DATA_SUBJECT_2)
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_2)
                .replace(PLACE_HOLDER_FOOTER, TEST_DATA_FOOTER_2);
        String resourcePath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfPut(resourcePath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        // Verify that the template is updated.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_CONTENT_TYPE, equalTo(ContentType.TEXT_HTML.getMimeType()))
                .body(ATTRIBUTE_SUBJECT, equalTo(TEST_DATA_SUBJECT_2))
                .body(ATTRIBUTE_BODY, equalTo(TEST_DATA_BODY_2))
                .body(ATTRIBUTE_FOOTER, equalTo(TEST_DATA_FOOTER_2));
    }

    @Test
    public void testDeleteOrganizationEmailTemplate() throws Exception {

        // Add email template type for the test.
        addEmailTemplateType(TEMPLATE_TYPE_DELETE_TEMPLATE);
        // Add the template to delete in the test.
        addEmailTemplate(TEMPLATE_TYPE_DELETE_TEMPLATE, ContentType.TEXT_HTML.getMimeType(),
                TEST_DATA_SUBJECT_1, TEST_DATA_BODY_1, TEST_DATA_FOOTER_1,
                LOCALE_EN_US);
        // Delete the template.
        String testTemplateId = base64String(TEMPLATE_TYPE_DELETE_TEMPLATE);
        String resourcePath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR + testTemplateId
                + ORG_EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfDelete(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // Verify that the template is deleted.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void addEmailTemplateType(String templateType) throws Exception {

        String  requestBodyTemplate = readResource("request-body-add-template-type.template");
        String requestBody = requestBodyTemplate.replace(PLACE_HOLDER_DISPLAY_NAME, templateType);
        String path = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
        Response response = getResponseOfPost(path, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    private void addEmailTemplate(String templateType, String contentType, String subject, String body,
                                  String footer, String locale) throws IOException {

        String testTemplateTypeId = base64String(templateType);
        String requestBodyTemplate = readResource("request-body-add-org-email-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, contentType)
                .replace(PLACE_HOLDER_SUBJECT, subject)
                .replace(PLACE_HOLDER_BODY, body)
                .replace(PLACE_HOLDER_FOOTER, footer)
                .replace(PLACE_HOLDER_LOCALE, locale);
        String requestPath = EMAIL_TEMPLATES_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_EMAIL_TEMPLATES_PATH;
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }
}
