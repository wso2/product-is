/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.commons;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.SSOUtil;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.COMMONAUTH_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractDataFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;

public class SSOTestBase extends ScenarioTestBase {

    private static final String SERVICE_PROVIDERS_LOCATION = "service.providers.location";

    private String commonauthEndpoint;

    private String sessionDataKey;

    private String consentUrl;

    private String sessionDataKeyConsent;

    private ApplicationManagementServiceClient applicationManagementServiceClient;

    public void init() throws Exception {

        super.init();
        loginAndObtainSessionCookie();
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendServiceURL,
                configContext);
    }

    public String getCommonauthEndpoint() {

        if (commonauthEndpoint == null) {
            commonauthEndpoint = getDeploymentProperty(IS_HTTPS_URL) + COMMONAUTH_URI_CONTEXT;
        }
        return commonauthEndpoint;
    }

    public void setCommonauthEndpoint(String commonauthEndpoint) {

        this.commonauthEndpoint = commonauthEndpoint;
    }

    public String getSessionDataKey() {

        return sessionDataKey;
    }

    public void setSessionDataKey(String sessionDataKey) {

        this.sessionDataKey = sessionDataKey;
    }

    public String getConsentUrl() {

        return consentUrl;
    }

    public void setConsentUrl(String consentUrl) {

        this.consentUrl = consentUrl;
    }

    public String getSessionDataKeyConsent() {

        return sessionDataKeyConsent;
    }

    public void setSessionDataKeyConsent(String sessionDataKeyConsent) {

        this.sessionDataKeyConsent = sessionDataKeyConsent;
    }

    public void clearRuntimeVariables() {

        this.sessionDataKey = null;
        this.sessionDataKeyConsent = null;
        this.consentUrl = null;
    }

    /**
     * Create service provider.
     *
     * @param fileName Service provider configuration file name.
     * @return Service provider name.
     * @throws Exception If error occurs while creating service provider.
     */
    protected String createServiceProvider(String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(SERVICE_PROVIDERS_LOCATION) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        String fileContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        return applicationManagementServiceClient.importApplication(fileName, fileContent);
    }

    /**
     * Get service provider.
     *
     * @param serviceProviderName Service provider name.
     * @return Service Provider instance.
     * @throws Exception If error occurs while retrieving service provider.
     */
    protected ServiceProvider getServiceProvider(String serviceProviderName) throws Exception {

        return applicationManagementServiceClient.getApplication(serviceProviderName);
    }

    /**
     * Delete service provider.
     *
     * @param serviceProviderName Service provider name.
     * @throws Exception If error occurs while deleting service provider.
     */
    protected void deleteServiceProvider(String serviceProviderName) throws Exception {

        applicationManagementServiceClient.deleteApplication(serviceProviderName);
    }

    /**
     * Get session data key.
     *
     * @param response HttpResponse.
     * @return sessionDataKey value.
     * @throws Exception If error occurs while deleting service provider.
     */
    protected String getSessionDataKey(HttpResponse response) throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = extractDataFromResponse(response, keyPositionMap);
        if (keyValues != null && !keyValues.isEmpty()) {
            return keyValues.get(0).getValue();
        }
        return null;
    }

    /**
     * Send logging post.
     *
     * @param client   HttpClient to be used for request sending.
     * @param username username.
     * @param password password.
     * @return location header.
     * @throws Exception If error occurs while sending login post.
     */
    protected String sendLoginPost(CloseableHttpClient client, String username, String password) throws Exception {

        HttpResponse response = SSOUtil
                .sendLoginPost(client, sessionDataKey, getCommonauthEndpoint(), username, password);

        if (response != null) {
            try {
                if (response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION) != null) {
                    return response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION).getValue();
                }
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        }
        return null;
    }

    /**
     * Send OAuth consent request and get sessionDataConsentKey from consent page.
     *
     * @param client HttpClient to be used for request sending.
     * @return sessionDataConsentKey value.
     * @throws Exception If error occurs while consent request.
     */
    protected String sendOAuthConsentRequest(CloseableHttpClient client) throws Exception {

        HttpResponse response = sendGetRequest(client, consentUrl, null);

        if (response != null) {
            try {
                return getSessionDataConsentKeyFromConsentPage(response);
            } finally {
                EntityUtils.consume(response.getEntity());
            }

        }
        return null;
    }

    /**
     * Get sessionDataConsentKey from consent page.
     *
     * @param response HttpResponse.
     * @return sessionDataConsentKey value.
     * @throws Exception If error occurs while sending login post.
     */
    protected String getSessionDataConsentKeyFromConsentPage(HttpResponse response) throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil
                .extractSessionConsentDataFromResponse(response, keyPositionMap);

        if (keyValues.get(0) != null) {
            return keyValues.get(0).getValue();
        } else {
            return null;
        }
    }
}
