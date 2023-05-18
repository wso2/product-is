package org.wso2.identity.integration.test.oauth2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.*;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public class OAuth2RestClient {

    //    static final String APPLICATION_MANAGEMENT_API_BASE_PATH = "https://localhost:9853/api/server/v1/applications";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String GRANT_TYPE_ATTRIBUTE = "grantTypes";
    public static final String CALL_BACK_URLS_ATTRIBUTE = "callbackURLs";
    public static final String OIDC_ATTRIBUTE = "oidc";
    public static final String INBOUND_PROTOCOL_CONFIG_ATTRIBUTE = "inboundProtocolConfiguration";
    private static final String PUBLIC_CLIENT_ATTRIBUTE = "publicClient";
    private final CloseableHttpClient client;
    private final String APPLICATION_MANAGEMENT_API_BASE_PATH;
    protected static final String TENANT_PATH = "t/%s";
    protected static final String API_SERVER_BASE_PATH = "/api/server/v1";
    protected static final String APPLICATION_MANAGEMENT_PATH = "/applications";
    protected static final String INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH = "/inbound-protocols/oidc";
    private final String USERNAME;
    private final String PASSWORD;

    public OAuth2RestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();
        this.USERNAME = tenantInfo.getContextUser().getUserName();
        this.PASSWORD = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        APPLICATION_MANAGEMENT_API_BASE_PATH = backendURL + String.format(TENANT_PATH, tenantDomain) + API_SERVER_BASE_PATH + APPLICATION_MANAGEMENT_PATH;
    }

    public String createApplication(ApplicationModel application) throws IOException, JSONException {

        HashMap<String, Object> rootMap = new HashMap<>();

        JSONArray grantTypes = new JSONArray(application.getInboundProtocolConfiguration().getOidc().getGrantTypes());
        JSONArray callBackUrls = new JSONArray(application.getInboundProtocolConfiguration().getOidc().getCallbackURLs());

        HashMap<String, Object> oidcMap = new HashMap<>();
        oidcMap.put(GRANT_TYPE_ATTRIBUTE, grantTypes);
        oidcMap.put(CALL_BACK_URLS_ATTRIBUTE, callBackUrls);
        oidcMap.put(PUBLIC_CLIENT_ATTRIBUTE, application.getInboundProtocolConfiguration().getOidc().getPublicClient());

        HashMap<String, Object> inboundConfigMap = new HashMap<>();
        inboundConfigMap.put(OIDC_ATTRIBUTE, new JSONObject(oidcMap));

        rootMap.put(NAME_ATTRIBUTE, application.getName());
        rootMap.put(INBOUND_PROTOCOL_CONFIG_ATTRIBUTE, new JSONObject(inboundConfigMap));

        JSONObject rootObject = new JSONObject(rootMap);

        HttpPost request = new HttpPost(APPLICATION_MANAGEMENT_API_BASE_PATH);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader("Content-Type", String.valueOf(ContentType.JSON));

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        EntityUtils.consume(response.getEntity());
        String[] self = response.getHeaders("Location")[0].toString().split("/");

        return self[self.length-1];
    }

    public ApplicationListItem[] getAllApplications() throws IOException {

        HttpGet request = new HttpGet(APPLICATION_MANAGEMENT_API_BASE_PATH);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader("Content-Type", String.valueOf(ContentType.JSON));
        HttpResponse response = client.execute(request);

        String responseBody = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());

        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ApplicationListResponse listResponse = jsonWriter.readValue(responseBody, ApplicationListResponse.class);

        List<ApplicationListItem> applicationsList = listResponse.getApplications();

        return applicationsList.toArray(new ApplicationListItem[0]);
    }

    public ApplicationModel getOAuthInboundDetailsOfApp(String appId) throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId + INBOUND_PROTOCOLS_OIDC_CONTEXT_PATH;

        ApplicationModel resultApp = new ApplicationModel();
        resultApp.setId(appId);

        HttpGet request = new HttpGet(path);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader("Content-Type", String.valueOf(ContentType.JSON));
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setClientId(((JSONObject) obj).get("clientId").toString());
        oidcConfig.setClientSecret(((JSONObject) obj).get("clientSecret").toString());

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        resultApp.setInboundProtocolConfiguration(inboundProtocolsConfig);

        return resultApp;
    }

    public void deleteApplication(String appId) throws IOException {

        HttpDelete request = new HttpDelete(APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + appId);

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        client.execute(request);
    }
}
