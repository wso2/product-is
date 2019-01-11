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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.clients.oauth.OauthAdminClient;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.SSOUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.scenarios.commons.util.Constants.DCR_REGISTER_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_AUTHORIZE_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.OAUTH_TOKEN_URI_CONTEXT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CODE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_CONSENT;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY_CONSENT;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendPostRequestWithParameters;

public class OAuth2CommonClient extends SSOCommonClient {

    private String dcrEndpoint;

    private String tokenEndpoint;

    private String authorizeEndpoint;

    private OauthAdminClient oauthAdminClient;

    private String consentUrl;

    public OAuth2CommonClient(String sessionCookie, String backendServiceURL, String identityServerHttpsUrl,
            ConfigurationContext configContext) throws Exception {

        super(sessionCookie, backendServiceURL, identityServerHttpsUrl, configContext);
        oauthAdminClient = new OauthAdminClient(backendServiceURL, sessionCookie);
    }

    public String getDcrEndpoint() {

        if (dcrEndpoint == null) {
            dcrEndpoint = getIdentityServerHttpsUrl() + DCR_REGISTER_URI_CONTEXT;
        }
        return dcrEndpoint;
    }

    public void setDcrEndpoint(String dcrEndpoint) {

        this.dcrEndpoint = dcrEndpoint;
    }

    public String getTokenEndpoint() {

        if (tokenEndpoint == null) {
            tokenEndpoint = getIdentityServerHttpsUrl() + OAUTH_TOKEN_URI_CONTEXT;
        }
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {

        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizeEndpoint() {

        if (authorizeEndpoint == null) {
            authorizeEndpoint = getIdentityServerHttpsUrl() + OAUTH_AUTHORIZE_URI_CONTEXT;
        }
        return authorizeEndpoint;
    }

    public void setAuthorizeEndpoint(String authorizeEndpoint) {

        this.authorizeEndpoint = authorizeEndpoint;
    }

    public OAuthConsumerAppDTO getOAuthConsumerApp(String serviceProviderName) throws Exception {

        return oauthAdminClient.getOAuthAppByName(serviceProviderName);
    }

    public String getConsentUrl() {

        return this.consentUrl;
    }

    /**
     * Extract and set session data key.
     *
     * @param response HttpResponse.
     */
    public void setConsentUrl(HttpResponse response) {

        this.consentUrl = SSOUtil.getLocationHeader(response);
    }

    /**
     * Send authorize GET request.
     *
     * @param client              HttpClient to be used for request sending.
     * @param oAuthConsumerAppDTO OAuth Consumer App
     * @param scope               Authorization request scopes.
     * @param params              Additional request parameters.
     * @return Http response.
     * @throws Exception If error occurs while sending the authorize request.
     */
    public HttpResponse sendAuthorizeGet(CloseableHttpClient client, OAuthConsumerAppDTO oAuthConsumerAppDTO,
            String scope, Map<String, String> params) throws Exception {

        return SSOUtil.sendAuthorizeGet(client, getAuthorizeEndpoint(), oAuthConsumerAppDTO.getOauthConsumerKey(),
                oAuthConsumerAppDTO.getCallbackUrl(), scope, params);

    }

    /**
     * Approve consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent SessionDataKeyConsent value.
     * @param approvalType          Approval type: approve or approveAlways or Deny.
     * @return HttpResponse.
     * @throws Exception If error occurs while sending the consent post.
     */
    public HttpResponse postOAuthConsent(CloseableHttpClient client, String sessionDataKeyConsent, String approvalType)
            throws Exception {

        return sendOAuthConsentApproveOncePost(client, sessionDataKeyConsent, getAuthorizeEndpoint(), approvalType);
    }

    /**
     * Get authorization code
     *
     * @param response HttpResponse with the authorization code.
     * @return Authorization code.
     * @throws Exception If error occurs while sending the consent post.
     */
    public String getAuthorizeCode(HttpResponse response) throws Exception {

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (locationHeader != null) {
            return DataExtractUtil.getParamFromURIString(locationHeader.getValue(), PARAM_CODE);
        }
        return null;
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Once' or 'Approve Always' or 'Deny' submit
     * and only for OAuth and not for user attribute sharing related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @param approveType           Approval type: approve or approveAlways or Deny.
     * @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    public HttpResponse sendOAuthConsentApproveOncePost(HttpClient client, String sessionDataKeyConsent,
            String authzEndpointUrl, String approveType) throws Exception {

        return sendOAuthConsentPost(client, sessionDataKeyConsent, authzEndpointUrl, approveType);
    }

    /**
     * Sends OAuth consent submit request. This consent will only for OAuth and not for user attribute sharing
     * related consent.
     *
     * @param client                HttpClient to be used for request sending.
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param authzEndpointUrl      Authorization endpoint URL.
     * @param consentInput          OAuth consent input. Applicable values: approve, approveAlways, deny.
     * @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    public HttpResponse sendOAuthConsentPost(HttpClient client, String sessionDataKeyConsent, String authzEndpointUrl,
            String consentInput) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(PARAM_CONSENT, consentInput));
        urlParameters.add(new BasicNameValuePair(PARAM_SESSION_DATA_KEY_CONSENT, sessionDataKeyConsent));

        return sendPostRequestWithParameters(client, urlParameters, authzEndpointUrl, null);
    }

    /**
     * Send OAuth consent request and get sessionDataConsentKey from consent page.
     *
     * @param client HttpClient to be used for request sending.
     * @return Http Response.
     * @throws Exception If error occurs while consent request.
     */
    public HttpResponse sendOAuthConsentRequest(CloseableHttpClient client) throws Exception {

        return sendGetRequest(client, consentUrl, null);
    }

    /**
     * Clear run time variables.
     */
    @Override
    public void clearRuntimeVariables() {

        super.clearRuntimeVariables();
        this.consentUrl = null;
    }
}
