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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.identity.scenarios.commons.TestUserMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.TOCOMMONAUTH;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_REQUEST_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_RESPONSE_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.RELAY_STATE_PARAM;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractValueFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithParameters;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPostWithParamsAndHeaders;

public class SAMLSSOUtil {

    public static HttpResponse sendLoginPostMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient) throws Exception {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(HttpHeaders.USER_AGENT, userAgent);
        headers[1] = new BasicHeader(HttpHeaders.REFERER, String.format(acsUrl, artifact));
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(TOCOMMONAUTH, "true");
        return sendLoginPostWithParamsAndHeaders(httpClient, sessionKey, url, userName, password, urlParameters,
                headers);
    }

    public static HttpResponse sendPostMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient) throws Exception {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(HttpHeaders.USER_AGENT, userAgent);
        headers[1] = new BasicHeader(HttpHeaders.REFERER, String.format(acsUrl, artifact));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        return sendPostRequestWithParameters(httpClient, urlParameters, url, headers);
    }

    public static HttpResponse sendSAMLMessage(String url, Map<String, String> parameters, String userAgent,
                                               TestUserMode userMode, String tenantDomainParam, String tenantDomain, HttpClient httpClient)
            throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(tenantDomainParam, tenantDomain));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    public static String extractSAMLRequest(HttpResponse response) throws IOException {
        return extractValueFromResponse(response, "name='" + SAML_REQUEST_PARAM + "'", 5);
    }


    public static String extractSAMLResponse(HttpResponse response) throws IOException {
        return extractValueFromResponse(response, "name='" + SAML_RESPONSE_PARAM + "'", 5);
    }

    public static String extractRelayState(HttpResponse response) throws IOException {
        return extractValueFromResponse(response, "name='" + RELAY_STATE_PARAM + "'", 5);
    }

}
