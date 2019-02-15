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
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.scenarios.commons.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.SSOConstants;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractDataFromResponse;

public class SSOCommonClient {

    private static final String SERVICE_PROVIDERS_LOCATION = "service.providers.location";

    private HTTPCommonClient httpCommonClient;

    private String commonauthEndpoint;

    private String tenantDomain;

    private ApplicationManagementServiceClient applicationManagementServiceClient;

    public SSOCommonClient(HTTPCommonClient httpCommonClient, String serverHTTPsUrl, String tenantDomain,
            String sessionCookie, String backendServiceURL, ConfigurationContext configContext) throws Exception {

        this.httpCommonClient = httpCommonClient;
        this.tenantDomain = tenantDomain;
        this.applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie,
                backendServiceURL, configContext);
        setEndpoints(serverHTTPsUrl);
    }

    public SSOCommonClient(HTTPCommonClient httpCommonClient, String serverHTTPsUrl, String tenantDomain) {

        this.httpCommonClient = httpCommonClient;
        this.tenantDomain = tenantDomain;
        setEndpoints(serverHTTPsUrl);
    }

    /**
     * Get session data key.
     *
     * @param response HttpResponse.
     * @throws Exception If error occurs while extracting SessionDataKey.
     */
    public String getSessionDataKey(HttpResponse response) throws Exception {

        if (response == null) {
            return null;
        }

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + SSOConstants.CommonAuthParams.SESSION_DATA_KEY + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = extractDataFromResponse(response, keyPositionMap);
        if (keyValues != null && !keyValues.isEmpty()) {
            return keyValues.get(0).getValue();
        }
        return null;
    }

    /**
     * Get session data key consent.
     *
     * @param response Http Response.
     * @throws Exception exception if error occurs while extracting SessionDataKeyConsent.
     */
    public String getSessionDataKeyConsent(HttpResponse response) throws Exception {

        if (response == null) {
            return null;
        }

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + SSOConstants.CommonAuthParams.SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil
                .extractSessionConsentDataFromResponse(response, keyPositionMap);

        if (keyValues.get(0) != null) {
            return keyValues.get(0).getValue();
        }
        return null;
    }

    /**
     * Send logging post.
     *
     * @param sessionDataKey Session data key.
     * @param username       Username.
     * @param password       Password.
     * @return Http Response.
     * @throws Exception If error occurs while sending login post.
     */
    public HttpResponse sendLoginPost(String sessionDataKey, String username, String password) throws Exception {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(SSOConstants.CommonAuthParams.USERNAME, username));
        requestParameters.add(new BasicNameValuePair(SSOConstants.CommonAuthParams.PASSWORD, password));
        requestParameters.add(new BasicNameValuePair(SSOConstants.CommonAuthParams.SESSION_DATA_KEY, sessionDataKey));
        return httpCommonClient.sendPostRequestWithParameters(commonauthEndpoint, requestParameters, null);
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

    public String getLocationHeader(HttpResponse response) {

        if (response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION) != null) {
            return response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION).getValue();
        }
        return null;
    }

    private void setEndpoints(String serverHTTPsUrl) {

        this.commonauthEndpoint = serverHTTPsUrl + "/commonauth";
    }
}
