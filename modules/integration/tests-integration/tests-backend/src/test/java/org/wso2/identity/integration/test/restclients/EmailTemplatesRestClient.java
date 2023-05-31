/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import java.io.IOException;

public class EmailTemplatesRestClient {

    private static final String TENANT_PATH = "t/%s";
    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String EMAIL_TEMPLATES_EMAIL_BASE_PATH = "/email";
    private static final String EMAIL_TEMPLATE_TYPES_PATH = "/template-types";
    private static final String EMAIL_TEMPLATES_PATH = "/templates";
    private static final String PATH_SEPARATOR = "/";
    public static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    private final String emailTemplateApiBasePath;
    private final CloseableHttpClient client;
    private final String username;
    private final String password;

    public EmailTemplatesRestClient(String backendURL, Tenant tenantInfo) {
        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        emailTemplateApiBasePath = backendURL + String.format(TENANT_PATH, tenantDomain) + API_SERVER_BASE_PATH +
                EMAIL_TEMPLATES_EMAIL_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH;
    }

    public JSONObject getEmailTemplate(String templateTypeId, String templateId) throws Exception {
        String endPointUrl = emailTemplateApiBasePath + PATH_SEPARATOR +
                getEncodedEmailTemplateTypeId(templateTypeId) + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId;

        CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl);

        String responseBody = EntityUtils.toString(response.getEntity());
        response.close();

        return getJSONObject(responseBody);
    }

    private CloseableHttpResponse getResponseOfHttpGet(String endPointUrl) throws IOException {
        HttpGet request = new HttpGet(endPointUrl);
        request.setHeaders(getHeaders());

        return client.execute(request);
    }

    private Header[] getHeaders() {
        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
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

    public void closeHttpClient() throws IOException {
        client.close();
    }
}
