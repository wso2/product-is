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

package org.wso2.identity.scenarios.commons.util;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import static org.wso2.identity.scenarios.commons.util.Constants.CLIENT_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_GRANT_TYPES;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_REDIRECT_URIS;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.constructBasicAuthzHeader;

/**
 * Utility class for application related functions.
 */
public class ApplicationUtil {

    /**
     * Register application with DCR.
     *
     * @param dcrEndpoint DCR Endpoint URL.
     * @param appName     Application name.
     * @param redirectUri Redirect URL.
     * @param grants      Supported grant types by the application.
     * @param username    Authenticating username.
     * @param password    Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during app creation.
     */
    public static HttpResponse registerDCRApplication(String dcrEndpoint, String appName, String redirectUri, String[]
            grants, String username, String password) throws IOException {

        HttpClient client = HttpClients.createDefault();

        HttpPost request = new HttpPost(dcrEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);

        JSONArray grantTypes = new JSONArray();
        grantTypes.addAll(Arrays.asList(grants));

        JSONArray redirectURI = new JSONArray();
        redirectURI.add(redirectUri);

        JSONObject obj = new JSONObject();
        obj.put(CLIENT_NAME, appName);
        obj.put(PARAM_GRANT_TYPES, grantTypes);
        obj.put(PARAM_REDIRECT_URIS, redirectURI);

        StringEntity entity = new StringEntity(obj.toJSONString());
        request.setEntity(entity);

        return client.execute(request);
    }

    /**
     * Delete application with DCR.
     *
     * @param dcrEndpoint DCR Endpoint URL.
     * @param clientId    Client ID of the application
     * @param username    Authenticating username.
     * @param password    Authenticating user password.
     * @return HttpResponse with the result.
     * @throws IOException If error occurs during app creation.
     */
    public static HttpResponse deleteDCRApplication(String dcrEndpoint, String clientId, String username, String
            password) throws IOException {

        HttpClient client = HttpClients.createDefault();
        String applicationEndpoint = dcrEndpoint + "/" + clientId;

        HttpDelete request = new HttpDelete(applicationEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, constructBasicAuthzHeader(username, password));

        return client.execute(request);
    }
}
