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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.wso2.identity.scenarios.commons.util.Constants.APPROVE_ALWAYS;
import static org.wso2.identity.scenarios.commons.util.Constants.APPROVE_ONCE;
import static org.wso2.identity.scenarios.commons.util.Constants.CONTENT_TYPE_APPLICATION_FORM;
import static org.wso2.identity.scenarios.commons.util.Constants.COOKIE;
import static org.wso2.identity.scenarios.commons.util.Constants.DENY;
import static org.wso2.identity.scenarios.commons.util.Constants.GRANT_TYPE_AUTHORIZATION_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CLIENT_ID;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CONSENT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_GRANT_TYPE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_MANDATORY_CLAIMS;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_PASSWORD;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_REDIRECT_URI;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_REQUESTED_CLAIMS;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_RESPONSE_TYPE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SCOPE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_USERNAME;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getQueryParams;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getRedirectUrlFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithParameters;

/**
 * Utility class for SSO related functions.
 */
public class SSOUtil {

    /**
     * Sent OAuth authorization request.
     *
     * @param client HttpClient to be used for request sending.
     * @param authzEndpoint Authorization endpoint URL.
     * @param clientId Client ID of the application.
     * @param redirectUri Redirect URI of the application.
     * @param scope Authorization request scopes.
     * @param params Additional request parameters.
     * @return HttpResponse with the authorization response.
     * @throws Exception If error occurs while sending the authorize request.
     */
    public static HttpResponse sendAuthorizeGet(HttpClient client, String authzEndpoint, String clientId, String
            redirectUri, String scope, Map<String, String> params) throws Exception {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(PARAM_RESPONSE_TYPE, PARAM_CODE);
        requestParams.put(PARAM_CLIENT_ID, clientId);
        requestParams.put(PARAM_REDIRECT_URI, redirectUri);
        if (isNotBlank(scope)) {
            requestParams.put(PARAM_SCOPE, redirectUri);
        }
        if (params != null) {
            requestParams.putAll(params);
        }
        return sendGetRequest(client, authzEndpoint, requestParams);
    }

    /**
     * Retrieves SessionDataConsentKey from OAuth consent page.
     *
     * @param response HttpResponse with OAuth consent page.
     * @return SessionDataConsentKey of the corresponding consent request
     * @throws IOException If error occurs while extracting SessionDataConsentKey.
     */
    public static String getSessionDataConsentKeyFromConsentPage(HttpResponse response) throws IOException {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY_CONSENT + "\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(
                response, keyPositionMap);

        if (keyValues.get(0) != null) {
            return keyValues.get(0).getValue();
        } else {
            return null;
        }
    }

    /**
     * Submits user credentials at SSO basic auth login page.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKey Session key related to the authentication request.
     * @param commonauthEndpointUrl IS commonauth endpoint URL.
     * @param username Authenticating username.
     * @param password Authenticating user password.
     * @return @return HttpResponse with the credential submit response.
     * @throws IOException If error occurs while credential submit.
     */
    public static HttpResponse sendLoginPost(HttpClient client, String sessionDataKey, String commonauthEndpointUrl,
                                             String username, String password) throws IOException {

        List<NameValuePair> urlParameters = getBasicLoginParams(sessionDataKey, username, password);
        return sendPostRequestWithParameters(client, urlParameters, commonauthEndpointUrl, null);
    }

    /**
     * Submits user credentials at SSO basic auth login page.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKey Session key related to the authentication request.
     * @param endpointUrl IS login endpoint URL.
     * @param username Authenticating username.
     * @param password Authenticating user password.
     * @return @return HttpResponse with the credential submit response.
     * @throws IOException If error occurs while credential submit.
     */
    public static HttpResponse sendLoginPostWithParamsAndHeaders(HttpClient client, String sessionDataKey, String
            endpointUrl, String username, String password, Map<String,String> params, Header[] headers)
            throws
            IOException {

        List<NameValuePair> urlParameters = getBasicLoginParams(sessionDataKey, username, password);
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return sendPostRequestWithParameters(client, urlParameters, endpointUrl, headers);
    }

    private static List<NameValuePair> getBasicLoginParams(String sessionDataKey, String username, String password) {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(PARAM_USERNAME, username));
        urlParameters.add(new BasicNameValuePair(PARAM_PASSWORD, password));
        urlParameters.add(new BasicNameValuePair(PARAM_SESSION_DATA_KEY, sessionDataKey));
        return urlParameters;
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Once' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws IOException If error occurs while sending the consent submit.
     */
    public static HttpResponse sendOAuthConsentApproveOncePost(HttpClient client, String sessionDataKeyConsent, String
            authzEndpointUrl) throws IOException {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, APPROVE_ONCE);
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Always' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws IOException If error occurs while sending the consent submit.
     */
    public static HttpResponse sendOAuthConsentApproveAlwaysPost(HttpClient client, String sessionDataKeyConsent,
                                                                 String authzEndpointUrl) throws IOException {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, APPROVE_ALWAYS);
    }

    /**
     * Sends OAuth consent submit request. This consent will be a 'Deny' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws IOException If error occurs while sending the consent submit.
     */
    public static HttpResponse sendOAuthConsentDenyPost(HttpClient client, String sessionDataKeyConsent,
                                                                 String authzEndpointUrl) throws IOException {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, DENY);
    }

    /**
     * Sends OAuth consent submit request. This consent will only for OAuth and not for user attribute sharing
     * related consent.
     *
     * @param client HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl Authorization endpoint URL.
     * @param consentInput OAuth consent input. Applicable values: approve, approveAlways, deny.
     * @return @return HttpResponse with the consent submit response.
     * @throws IOException If error occurs while sending the consent submit.
     */
    public static HttpResponse sendOAuthConsentPost(HttpClient client, String sessionDataKeyConsent, String
            authzEndpointUrl, String consentInput) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(PARAM_CONSENT, consentInput));
        urlParameters.add(new BasicNameValuePair(PARAM_SESSION_DATA_KEY_CONSENT, sessionDataKeyConsent));

        return sendPostRequestWithParameters(client, urlParameters, authzEndpointUrl, null);
    }

    /**
     * Sends OAuth token request.
     *
     * @param client HttpClient to be used for request sending.
     * @param authzCode Authorization code.
     * @param tokenEndpoint Token endpoint URL.
     * @param clientId Client ID of the application.
     * @param clientSecret Client secret of the application.
     * @param redirectUri Redirect URI of the application.
     * @param scope Token request scopes.
     * @param params Additional request parameters.
     * @return HttpResponse with the token response.
     * @throws IOException If error occurs while sending the token request.
     */
    public static HttpResponse sendTokenRequest(HttpClient client,String authzCode, String tokenEndpoint, String
            clientId, String clientSecret, String redirectUri, String scope, Map<String, String> params) throws
            IOException {

        Header authzHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, IdentityScenarioUtil.constructBasicAuthzHeader
                (clientId, clientSecret));
        Header contentTypeHeader = new BasicHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_FORM);
        List<NameValuePair> requestParams = new ArrayList<>();
        requestParams.add(new BasicNameValuePair(PARAM_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE));
        requestParams.add(new BasicNameValuePair(PARAM_CODE, authzCode));
        requestParams.add(new BasicNameValuePair(PARAM_REDIRECT_URI, redirectUri));

        if (isNotBlank(scope)) {
            requestParams.add(new BasicNameValuePair(PARAM_SCOPE, scope));
        }
        if (params != null) {
            for (Map.Entry<String, String> entry: params.entrySet()) {
                requestParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        return sendPostRequestWithParameters(client, requestParams, tokenEndpoint, new Header[]{authzHeader,
                contentTypeHeader});
    }

    public static HttpResponse sendRedirectRequest(HttpResponse response, String userAgent, String acsUrl, String
            artifact, HttpClient httpClient) throws IOException {
        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if (HttpHeaders.LOCATION.equals(header.getName())) {
                url = header.getValue();
            }
        }
        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.USER_AGENT, userAgent);
        request.addHeader(HttpHeaders.REFERER, String.format(acsUrl, artifact));
        return httpClient.execute(request);
    }

    public static HttpResponse sendPOSTConsentMessage(HttpResponse response, String commonAuthUrl, String userAgent,
                                                      String referer, HttpClient httpClient, String
                                                              pastreCookie) throws Exception {
        String redirectUrl = getRedirectUrlFromResponse(response);
        Map<String, String> queryParams = getQueryParams(redirectUrl);


        String sessionKey = queryParams.get(PARAM_SESSION_DATA_KEY);
        String mandatoryClaims = queryParams.get(PARAM_MANDATORY_CLAIMS);
        String requestedClaims = queryParams.get(PARAM_REQUESTED_CLAIMS);
        String consentRequiredClaims;

        if (isNotBlank(mandatoryClaims) && isNotBlank(requestedClaims)) {
            StringJoiner joiner = new StringJoiner(",");
            joiner.add(mandatoryClaims);
            joiner.add(requestedClaims);
            consentRequiredClaims = joiner.toString();
        } else if (isNotBlank(mandatoryClaims)) {
            consentRequiredClaims = mandatoryClaims;
        } else {
            consentRequiredClaims = requestedClaims;
        }

        String[] claims;
        if (isNotBlank(consentRequiredClaims)) {
            claims = consentRequiredClaims.split(",");
        } else {
            claims = new String[0];
        }

        HttpPost post = new HttpPost(commonAuthUrl);
        post.setHeader(HttpHeaders.USER_AGENT, userAgent);
        post.addHeader(HttpHeaders.REFERER, referer);
        post.addHeader(COOKIE, pastreCookie);
        List<NameValuePair> urlParameters = new ArrayList<>();

        for (int i = 0; i < claims.length; i++) {

            if (isNotBlank(claims[i])) {
                String[] claimMeta = claims[i].split("_", 2);
                if (claimMeta.length == 2) {
                    urlParameters.add(new BasicNameValuePair("consent_" + claimMeta[0], "on"));
                }
            }
        }
        urlParameters.add(new BasicNameValuePair(PARAM_SESSION_DATA_KEY, sessionKey));
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }
}
