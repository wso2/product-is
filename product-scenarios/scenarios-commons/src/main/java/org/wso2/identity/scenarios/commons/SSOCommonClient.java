/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
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
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractDataFromResponse;

public class SSOCommonClient {

    private static final String SERVICE_PROVIDERS_LOCATION = "service.providers.location";

    private String commonauthEndpoint;

    private String sessionDataKey;

    private String sessionDataKeyConsent;

    private String identityServerHttpsUrl;

    private ApplicationManagementServiceClient applicationManagementServiceClient;

    public SSOCommonClient(String sessionCookie, String backendServiceURL, String identityServerHttpsUrl,
            ConfigurationContext configContext) throws Exception {

        this.identityServerHttpsUrl = identityServerHttpsUrl;
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendServiceURL,
                configContext);
    }

    public String getCommonauthEndpoint() {

        if (this.commonauthEndpoint == null) {
            this.commonauthEndpoint = this.identityServerHttpsUrl + COMMONAUTH_URI_CONTEXT;
        }
        return this.commonauthEndpoint;
    }

    public void setCommonauthEndpoint(String commonauthEndpoint) {

        this.commonauthEndpoint = commonauthEndpoint;
    }

    public String getSessionDataKey() {

        return this.sessionDataKey;
    }

    /**
     * Extract and set session data key.
     *
     * @param response HttpResponse.
     * @throws Exception If error occurs while extracting SessionDataKey.
     */
    public void setSessionDataKey(HttpResponse response) throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = extractDataFromResponse(response, keyPositionMap);
        if (keyValues != null && !keyValues.isEmpty()) {
            this.sessionDataKey = keyValues.get(0).getValue();
        }
    }

    public String getSessionDataKeyConsent() {

        return this.sessionDataKeyConsent;
    }

    /**
     * Extract and set SessionDataKeyConsent.
     *
     * @param response Http Response.
     * @throws Exception exception if error occurs while extracting SessionDataKeyConsent.
     */
    public void setSessionDataKeyConsent(HttpResponse response) throws Exception {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil
                .extractSessionConsentDataFromResponse(response, keyPositionMap);

        if (keyValues.get(0) != null) {
            this.sessionDataKeyConsent = keyValues.get(0).getValue();
        }
    }

    public String getIdentityServerHttpsUrl() {

        return identityServerHttpsUrl;
    }

    public void setIdentityServerHttpsUrl(String identityServerHttpsUrl) {

        this.identityServerHttpsUrl = identityServerHttpsUrl;
    }

    /**
     * Clear run time variables.
     */
    public void clearRuntimeVariables() {

        this.sessionDataKey = null;
        this.sessionDataKeyConsent = null;
    }

    /**
     * Create service provider.
     *
     * @param fileName Service provider configuration file name.
     * @return Service provider name.
     * @throws Exception If error occurs while creating service provider.
     */
    public String createServiceProvider(String fileName) throws Exception {

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
    public ServiceProvider getServiceProvider(String serviceProviderName) throws Exception {

        return applicationManagementServiceClient.getApplication(serviceProviderName);
    }

    /**
     * Delete service provider.
     *
     * @param serviceProviderName Service provider name.
     * @throws Exception If error occurs while deleting service provider.
     */
    public void deleteServiceProvider(String serviceProviderName) throws Exception {

        applicationManagementServiceClient.deleteApplication(serviceProviderName);
    }

    /**
     * Send logging post.
     *
     * @param client   HttpClient to be used for request sending.
     * @param username username.
     * @param password password.
     * @return Http Response.
     * @throws Exception If error occurs while sending login post.
     */
    public HttpResponse sendLoginPost(CloseableHttpClient client, String username, String password) throws Exception {

        return SSOUtil.sendLoginPost(client, sessionDataKey, getCommonauthEndpoint(), username, password);
    }
}
