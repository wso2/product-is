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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.clients.oauth.OauthAdminClient;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.SSOUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.APPROVE_ALWAYS;
import static org.wso2.identity.scenarios.commons.util.Constants.APPROVE_ONCE;
import static org.wso2.identity.scenarios.commons.util.Constants.DCR_REGISTER_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.DENY;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_AUTHORIZE_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_TOKEN_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CONSENT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithParameters;

public class OAuth2TestBase extends SSOTestBase {

    private String dcrEndpoint;

    private String tokenEndpoint;

    private String authorizeEndpoint;

    private OauthAdminClient oauthAdminClient;

    public void init() throws Exception {

        super.init();
        loginAndObtainSessionCookie();
        oauthAdminClient = new OauthAdminClient(backendServiceURL, sessionCookie);
    }

    public String getDcrEndpoint() {

        if (dcrEndpoint == null) {
            dcrEndpoint = getDeploymentProperty(IS_HTTPS_URL) + DCR_REGISTER_URI_CONTEXT;
        }
        return dcrEndpoint;
    }

    public void setDcrEndpoint(String dcrEndpoint) {

        this.dcrEndpoint = dcrEndpoint;
    }

    public String getTokenEndpoint() {

        if (tokenEndpoint == null) {
            tokenEndpoint = getDeploymentProperty(IS_HTTPS_URL) + OAUTH_TOKEN_URI_CONTEXT;
        }
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {

        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizeEndpoint() {

        if (authorizeEndpoint == null) {
            authorizeEndpoint = getDeploymentProperty(IS_HTTPS_URL) + OAUTH_AUTHORIZE_URI_CONTEXT;
        }
        return authorizeEndpoint;
    }

    public void setAuthorizeEndpoint(String authorizeEndpoint) {

        this.authorizeEndpoint = authorizeEndpoint;
    }

    protected OAuthConsumerAppDTO getOAuthConsumerApp(String serviceProviderName) throws Exception {

        return oauthAdminClient.getOAuthAppByName(serviceProviderName);
    }

    /**
     * Send authorize GET request.
     *
     * @param client              HttpClient to be used for request sending.
     * @param oAuthConsumerAppDTO OAuth Consumer App
     * @param scope               Authorization request scopes.
     * @param params              Additional request parameters.
     * @return sessionDataKey
     * @throws Exception If error occurs while sending the authorize request.
     */
    protected String sendAuthorizeGet(CloseableHttpClient client, OAuthConsumerAppDTO oAuthConsumerAppDTO, String scope,
            Map<String, String> params) throws Exception {

        HttpResponse response = SSOUtil
                .sendAuthorizeGet(client, getAuthorizeEndpoint(), oAuthConsumerAppDTO.getOauthConsumerKey(),
                        oAuthConsumerAppDTO.getCallbackUrl(), scope, params);

        if (response != null) {
            try {
                return getSessionDataKey(response);
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        }
        return null;
    }

    /**
     * Approve consent and get authorization code.
     *
     * @param client HttpClient to be used for request sending.
     * @return HttpResponse with the authorization response.
     * @throws Exception If error occurs while sending the consent post.
     */
    protected String postOAuthConsentAndGetAuthorizeCode(CloseableHttpClient client) throws Exception {

        HttpResponse response = sendOAuthConsentApproveOncePost(client, getSessionDataKeyConsent(),
                getAuthorizeEndpoint());
        if (response != null) {
            try {
                return getAuthorizeCode(response);
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        }
        return null;
    }

    /**
     * Get authorization code
     *
     * @param response HttpResponse with the authorization code.
     * @return Authorization code.
     * @throws Exception If error occurs while sending the consent post.
     */
    protected String getAuthorizeCode(HttpResponse response) throws Exception {

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (locationHeader != null) {
            return DataExtractUtil.getParamFromURIString(locationHeader.getValue(), PARAM_CODE);
        }
        return null;
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Once' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    protected HttpResponse sendOAuthConsentApproveOncePost(HttpClient client, String sessionDataKeyConsent,
            String authzEndpointUrl) throws Exception {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, APPROVE_ONCE);
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Always' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    protected HttpResponse sendOAuthConsentApproveAlwaysPost(HttpClient client, String sessionDataKeyConsent,
            String authzEndpointUrl) throws Exception {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, APPROVE_ALWAYS);
    }

    /**
     * Sends OAuth consent submit request. This consent will be a 'Deny' submit and only for OAuth and not
     * for user attribute sharing related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @return @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    protected HttpResponse sendOAuthConsentDenyPost(HttpClient client, String sessionDataKeyConsent,
            String authzEndpointUrl) throws Exception {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, DENY);
    }

    /**
     * Sends OAuth consent submit request. This consent will only for OAuth and not for user attribute sharing
     * related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @param consentInput          OAuth consent input. Applicable values: approve, approveAlways, deny.
     * @return @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    protected HttpResponse sendOAuthConsentPost(HttpClient client, String sessionDataKeyConsent,
            String authzEndpointUrl, String consentInput) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(PARAM_CONSENT, consentInput));
        urlParameters.add(new BasicNameValuePair(PARAM_SESSION_DATA_KEY_CONSENT, sessionDataKeyConsent));

        return sendPostRequestWithParameters(client, urlParameters, authzEndpointUrl, null);
    }
}
