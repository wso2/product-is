package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.Assert;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.email.template.v1.model.EmailTemplateWithID;

import java.io.IOException;

public class ResourceAdminServiceRestClient extends RESTTestBase {

    public static final String EMAIL_TEMPLATES_API_BASE_PATH = "/email";
    public static final String EMAIL_TEMPLATE_TYPES_PATH = "/template-types";
    public static final String EMAIL_TEMPLATES_PATH = "/templates";
    public static final String PATH_SEPARATOR = "/";

    public static final String SAMPLE_TEMPLATE_TYPE_ID = "QWNjb3VudEVuYWJsZQ";
    private final CloseableHttpClient client;
    private EmailTemplateWithID getSpecificEmailTemplateResponse;


    public ResourceAdminServiceRestClient() throws IOException {
        client = HttpClients.createDefault();

        String expectedResponse = readResource("add-email-template-request.json");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        getSpecificEmailTemplateResponse = jsonWriter.readValue(expectedResponse, EmailTemplateWithID.class);
    }

    public EmailTemplateWithID getEmailTemplate(String templateTypeId, String templateId) throws JsonProcessingException {

        Response response = getResponseOfGet(EMAIL_TEMPLATES_API_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH +
                PATH_SEPARATOR + templateTypeId + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        EmailTemplateWithID responseFound = jsonWriter.readValue(response.asString(), EmailTemplateWithID.class);
        Assert.assertEquals(responseFound, getSpecificEmailTemplateResponse,
                "Response of the get specific email template doesn't match.");
        return responseFound;
    }
}
