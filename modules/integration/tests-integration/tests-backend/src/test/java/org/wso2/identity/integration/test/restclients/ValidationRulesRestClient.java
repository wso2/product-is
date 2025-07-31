/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.RevertFields;
import org.wso2.identity.integration.test.rest.api.server.input.validation.v1.model.ValidationConfigModelForField;

import javax.servlet.http.HttpServletResponse;

/**
 * REST client for validation rules API.
 */
public class ValidationRulesRestClient extends RestBaseClient {

    private static final String API_SERVER_BASE_PATH = "/api/server/v1";
    private static final String VALIDATION_RULES_PATH = "/validation-rules";
    private static final String REVERT_PATH = "/revert";
    private static final String ORG_PATH = "/o";
    private static final String ORG_ID_PLACEHOLDER = "{orgId}";

    private final CloseableHttpClient client;
    private final String username;
    private final String password;
    private final String validationRulesBasePath;
    private final String validationRulesSubOrgBasePath;
    private final String validationRulesGetSubOrgBasePath;

    public ValidationRulesRestClient(String backendURL, Tenant tenantInfo) {

        client = HttpClients.createDefault();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        String tenantDomain = tenantInfo.getContextUser().getUserDomain();

        validationRulesBasePath = backendURL + ISIntegrationTest.getTenantedRelativePath(
                API_SERVER_BASE_PATH + VALIDATION_RULES_PATH, tenantDomain);
        validationRulesSubOrgBasePath = backendURL + ISIntegrationTest.getTenantedOrgRelativePath(
                API_SERVER_BASE_PATH + VALIDATION_RULES_PATH, tenantDomain);
        validationRulesGetSubOrgBasePath = backendURL + ORG_PATH + PATH_SEPARATOR + ORG_ID_PLACEHOLDER +
                API_SERVER_BASE_PATH + VALIDATION_RULES_PATH;
    }

    /**
     * Retrieves validation rules in a sub-organization for a specific field.
     *
     * @param field            The field for which validation rules are to be retrieved.
     * @param orgId            The ID of the sub-organization.
     * @return A JSON object containing the validation rules for the specified field.
     * @throws Exception If an error occurs while retrieving the validation rules.
     */
    public JSONObject getValidationRulesForFieldInSubOrg(String field, String orgId) throws Exception {

        String endpointUrl = validationRulesGetSubOrgBasePath.replace(ORG_ID_PLACEHOLDER, orgId) +
                PATH_SEPARATOR + field;

        try (CloseableHttpResponse response = getResponseOfHttpGet(
                endpointUrl, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "validation rules retrieval failed.");
            return getJSONObject(EntityUtils.toString(response.getEntity()));
        }
    }

    /**
     * Update validation rules for a specific field.
     *
     * @param field            The field for which validation rules are to be updated.
     * @param validationConfig The validation configuration model for the field to be updated.
     * @throws Exception If an error occurs while updating the validation rules.
     */
    public void updateValidationRulesForField(String field, ValidationConfigModelForField validationConfig)
            throws Exception {

        String endpointUrl = validationRulesBasePath + PATH_SEPARATOR + field;

        try (CloseableHttpResponse response = getResponseOfHttpPut(endpointUrl,
                toJSONString(validationConfig), getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "validation rules update failed.");
        }
    }

    /**
     * Update validation rules for a specific field in a sub-organization.
     *
     * @param field            The field for which validation rules are to be updated.
     * @param validationConfig The validation configuration model for the field to be updated.
     * @param switchedM2MToken The M2M token for the sub-organization.
     * @throws Exception If an error occurs while updating the validation rules.
     */
    public void updateValidationRulesForFieldInSubOrg(String field, ValidationConfigModelForField validationConfig,
                                                      String switchedM2MToken) throws Exception {

        String endpointUrl = validationRulesSubOrgBasePath + PATH_SEPARATOR + field;

        try (CloseableHttpResponse response = getResponseOfHttpPut(endpointUrl,
                toJSONString(validationConfig), getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "validation rules update failed.");
        }
    }

    /**
     * Reverts validation rules in a sub-organization.
     *
     * @param requestBody      The request body containing fields to revert.
     * @param switchedM2MToken The M2M token for the sub-organization.
     * @throws Exception If an error occurs while reverting the validation rules.
     */
    public void revertValidationRulesInSubOrg(RevertFields requestBody, String switchedM2MToken)
            throws Exception {

        String endpointUrl = validationRulesSubOrgBasePath + REVERT_PATH;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endpointUrl,
                toJSONString(requestBody), getHeadersWithBearerToken(switchedM2MToken))) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "validation rules revert failed.");
        }
    }

    /**
     * Reverts validation rules.
     *
     * @param requestBody The request body containing fields to revert.
     * @throws Exception If an error occurs while reverting the validation rules.
     */
    public void revertValidationRules(RevertFields requestBody) throws Exception {

        String endpointUrl = validationRulesBasePath + REVERT_PATH;

        try (CloseableHttpResponse response = getResponseOfHttpPost(endpointUrl,
                toJSONString(requestBody), getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                    "validation rules revert failed.");
        }
    }

    /**
     * Close the HTTP client.
     *
     * @throws Exception If an error occurred while closing the Http Client.
     */
    public void closeHttpClient() throws Exception {

        client.close();
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }

    private Header[] getHeadersWithBearerToken(String accessToken) {

        Header[] headerList = new Header[2];
        headerList[0] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BEARER_TOKEN_AUTHORIZATION_ATTRIBUTE +
                accessToken);
        headerList[1] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }
}
