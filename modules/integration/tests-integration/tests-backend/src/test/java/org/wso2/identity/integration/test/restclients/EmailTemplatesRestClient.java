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
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;

public class EmailTemplatesRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String EMAIL_TEMPLATES_EMAIL_BASE_PATH = "/email";
    private static final String EMAIL_TEMPLATE_TYPES_PATH = "/template-types";
    private static final String EMAIL_TEMPLATES_PATH = "/templates";
    private final String emailTemplateApiBasePath;
    private final CloseableHttpClient client;
    private final String username;
    private final String password;

    public EmailTemplatesRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();

        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        emailTemplateApiBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(API_SERVER_BASE_PATH
                + EMAIL_TEMPLATES_EMAIL_BASE_PATH + EMAIL_TEMPLATE_TYPES_PATH, tenantDomain);
    }

    /**
     * Get Email template.
     *
     * @param templateTypeId Template type id.
     * @param templateId Template id.
     * @return JSONObject with email template details.
     * @throws Exception If an error occurred while getting Email Template.
     */
    public JSONObject getEmailTemplate(String templateTypeId, String templateId) throws Exception {

        String endPointUrl = emailTemplateApiBasePath + PATH_SEPARATOR +
                getEncodedEmailTemplateTypeId(templateTypeId) + EMAIL_TEMPLATES_PATH + PATH_SEPARATOR + templateId;

        try (CloseableHttpResponse response = getResponseOfHttpGet(endPointUrl, getHeaders())) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return getJSONObject(responseBody);
        }
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

    /**
     * Close the HTTP client.
     *
     * @throws IOException Exception.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }
}
