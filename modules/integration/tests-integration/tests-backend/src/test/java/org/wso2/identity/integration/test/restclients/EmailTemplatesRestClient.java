package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

import java.io.IOException;

public class EmailTemplatesRestClient {

    protected static final String TENANT_PATH = "t/%s";
    protected static final String API_SERVER_BASE_PATH = "/api/server/v1";
    public static final String EMAIL_TEMPLATES_EMAIL_BASE_PATH = "/email";
    public static final String EMAIL_TEMPLATE_TYPES_PATH = "/template-types";
    public static final String EMAIL_TEMPLATES_PATH = "/templates";
    public static final String PATH_SEPARATOR = "/";
    public static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    private final String EMAIL_TEMPLATE_API_BASE_PATH;
    private final CloseableHttpClient client;
    private final String USERNAME;
    private final String PASSWORD;


    public EmailTemplatesRestClient(String backendURL, Tenant tenantInfo) {
        client = HttpClients.createDefault();

        this.USERNAME = tenantInfo.getContextUser().getUserName();
        this.PASSWORD = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        EMAIL_TEMPLATE_API_BASE_PATH = backendURL + String.format(TENANT_PATH, tenantDomain) + API_SERVER_BASE_PATH +
                EMAIL_TEMPLATES_EMAIL_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
    }

    public JSONObject getEmailTemplate(String templateTypeId, String templateId) throws Exception {

        String endPointUrl = EMAIL_TEMPLATE_API_BASE_PATH + PATH_SEPARATOR +
                getEncodedEmailTemplateTypeId(templateTypeId) + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId;

        HttpResponse response = getResponseOfHttpGet(endPointUrl);

        String responseBody = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        return getJSONObject(responseBody);
    }

    private HttpResponse getResponseOfHttpGet(String endPointUrl) throws IOException {

        HttpGet request = new HttpGet(endPointUrl);
        request.setHeaders(getHeaders());

        return client.execute(request);
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private String getEncodedEmailTemplateTypeId(String emailTemplateTypeId) {
        return Base64.encodeBase64String(emailTemplateTypeId.getBytes());
    }

    private JSONObject getJSONObject(String responseString) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);
        if (json == null) {
            throw new Exception(
                    "Error occurred while getting the response");
        }

        return json;
    }
}
