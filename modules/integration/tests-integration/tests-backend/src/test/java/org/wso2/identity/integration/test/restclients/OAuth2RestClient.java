package org.wso2.identity.integration.test.restclients;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListResponse;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2RestClient {

    public static final String LOCATION_HEADER = "Location";
    public static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    private static final String USER_AGENT_ATTRIBUTE = "User-Agent";
    protected static final String TENANT_PATH = "t/%s";
    protected static final String API_SERVER_BASE_PATH = "/api/server/v1";
    protected static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    protected static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private final CloseableHttpClient client;
    private final String APPLICATION_MANAGEMENT_API_BASE_PATH;
    private final String USERNAME;
    private final String PASSWORD;

    public OAuth2RestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();
        this.USERNAME = tenantInfo.getContextUser().getUserName();
        this.PASSWORD = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        APPLICATION_MANAGEMENT_API_BASE_PATH = backendURL + String.format(TENANT_PATH, tenantDomain) +
                API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
    }

    public String createApplication(ApplicationModel application) throws IOException, JSONException {

        String jsonRequest = toJSONString(application);

        HttpResponse response = getResponseOfHttpPost(APPLICATION_MANAGEMENT_API_BASE_PATH, jsonRequest);
        EntityUtils.consume(response.getEntity());

        String[] self = response.getHeaders(LOCATION_HEADER)[0].toString().split("/");

        return self[self.length-1];
    }

    public ApplicationResponseModel getApplication(String appId) throws IOException {

        HttpResponse response = getResponseOfHttpGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId);

        String responseBody = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, ApplicationResponseModel.class);
    }

    public void updateApplication(String appId, ApplicationPatchModel application) throws IOException {

        String jsonRequest = toJSONString(application);

        HttpResponse response = getResponseOfHttpPatch(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId,
                jsonRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                "Application update failed");
        EntityUtils.consume(response.getEntity());
    }

    public String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    public HttpResponse getResponseOfHttpPost(String endPointUrl, String jsonRequest) throws IOException {

        HttpPost request = new HttpPost(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpGet(String endPointUrl) throws IOException {

        HttpGet request = new HttpGet(endPointUrl);
        request.setHeaders(getHeaders());

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpPatch(String endPointUrl, String jsonRequest) throws IOException {

        HttpPatch request = new HttpPatch(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpDelete(String endPointUrl) throws IOException {

        HttpDelete request = new HttpDelete(endPointUrl);
        request.setHeaders(getHeaders());

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpPut(String endPointUrl, String jsonRequest) throws IOException {

        HttpPut request = new HttpPut(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    public Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    public ApplicationListResponse getAllApplications() throws IOException {

        HttpResponse response = getResponseOfHttpGet(APPLICATION_MANAGEMENT_API_BASE_PATH);

        String responseBody = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, ApplicationListResponse.class);
    }

    public OpenIDConnectConfiguration getOIDCInboundDetails(String appId) throws Exception {

        HttpResponse response = getResponseOfHttpGet(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" +
                appId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH);

        String responseBody = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());

        return jsonWriter.readValue(responseBody, OpenIDConnectConfiguration.class);
    }

    public void updateOIDCInboundDetailsOfApplication(String appId, OpenIDConnectConfiguration oidcInboundConfig)
            throws IOException {

        String jsonRequest = toJSONString(oidcInboundConfig);

        HttpResponse response = getResponseOfHttpPut(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId +
                INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH, jsonRequest);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                "Application oidc inbound config update failed");
        EntityUtils.consume(response.getEntity());
    }

    public void deleteApplication(String appId) throws IOException {

        HttpResponse response = getResponseOfHttpDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                "Application deletion failed");
        EntityUtils.consume(response.getEntity());
    }
}
