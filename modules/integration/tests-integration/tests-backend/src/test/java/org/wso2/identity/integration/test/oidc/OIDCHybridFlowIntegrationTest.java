/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.identity.integration.test.oidc;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.*;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN;

public class OIDCHybridFlowIntegrationTest extends OAuth2ServiceAbstractIntegrationTest {

    private static final String RESPONSE_TYPE_CODE_ID_TOKEN = "code id_token";
    private static final String HYBRID_RESPONSE_TYPE = "code id_token,code id_token token";
    private static final String RESPONSE_TYPE_CODE_TOKEN = "code token";
    private static final String RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN = "code id_token token";
    private static final String OAUTH_ERROR_CODE = "oauthErrorCode";
    CookieStore cookieStore = new BasicCookieStore();
    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;

    private OpenIDConnectConfiguration opaqueOidcConfig;
    private CloseableHttpClient client;

    private String opaqueAppId;
    private String clientID;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        super.setSystemproperties();
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        this.client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        this.opaqueAppId = super.addApplication(this.getApplicationWithOpaqueTokens());
        assertNotNull(this.opaqueAppId, "OAuth App creation failed");

        this.opaqueOidcConfig = super.restClient.getOIDCInboundDetails(this.opaqueAppId);
        assertNotNull(this.opaqueOidcConfig, "Application creation failed.");

        this.clientID = this.opaqueOidcConfig.getClientId();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        super.deleteApp(this.opaqueAppId);

        this.client.close();
        super.restClient.closeHttpClient();
    }

    private ApplicationModel getApplicationWithOpaqueTokens() {

        final ApplicationModel application = new ApplicationModel();

        HybridFlowConfiguration hybridFlow = new HybridFlowConfiguration();
        hybridFlow.setEnable(true);
        hybridFlow.setResponseType(HYBRID_RESPONSE_TYPE);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setHybridFlow(hybridFlow);
        oidcConfig.setGrantTypes(List.of("authorization_code"));
        oidcConfig.setCallbackURLs(Collections.singletonList(OAuth2Constant.CALLBACK_URL));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName("HybridAuthTestSP");
        application.setDescription(SERVICE_PROVIDER_DESC);
        application.setIsManagementApp(true);
        application.setClaimConfiguration(super.setApplicationClaimConfig());

        return application;
    }

    @Test(groups = "wso2.is",
            description = "Test hybrid flow with configured response type")
    public void testHybridFlowWithCodeIdTokenResponseType() throws Exception {

        final String sessionDataKeyConsent =
                this.sendHybridAuthRequestPost(this.getConfiguredHybridFlowRequestParams(RESPONSE_TYPE_CODE_ID_TOKEN));
        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        final String authorizationCode = DataExtractUtil
                .extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.AUTHORIZATION_CODE_NAME);
        final String id_token = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                OAuth2Constant.ID_TOKEN);
        assertNotNull(authorizationCode, "Authorization code is null");
        assertNotNull(id_token, "ID token is null");
    }

    @Test(groups = "wso2.is",
            description = "Test hybrid flow with non-configured response type")
    public void testHybridFlowWithNonConfiguredResponseType() throws Exception {

        final String sessionDataKeyConsent =
                this.sendHybridAuthRequestPost(this.getNonConfiguredHybridFlowRequestParams());
        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        final String oauthErrorCode = DataExtractUtil
                .extractParamFromURIFragment(locationHeader.getValue(), OAUTH_ERROR_CODE);
        assertNotNull(oauthErrorCode, "OAuth error code is null");
    }

    @Test(groups = "wso2.is",
            description = "Test hybrid flow with code id_token token response type",
            dependsOnMethods = "testHybridFlowWithCodeIdTokenResponseType")
    public void testHybridFlowWithCodeIdTokenTokenResponseType() throws Exception {

        refreshHTTPClient();
        final String sessionDataKeyConsent =
                this.sendHybridAuthRequestPost(this.getConfiguredHybridFlowRequestParams(RESPONSE_TYPE_CODE_ID_TOKEN_TOKEN));
        final HttpResponse response = sendApprovalPost(this.client, sessionDataKeyConsent);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        final String authorizationCode = DataExtractUtil
                .extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.AUTHORIZATION_CODE_NAME);
        final String id_token = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                OAuth2Constant.ID_TOKEN);
        final String token = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(),
                OAuth2Constant.ACCESS_TOKEN);
        assertNotNull(authorizationCode, "Authorization code is null");
        assertNotNull(id_token, "ID token is null");
        assertNotNull(token, "Access token is null");
    }

    /**
     * Initiate the hybrid flow by sending an authorization request to IS and obtain session data key.
     */
    private String sendHybridAuthRequestPost(final List<NameValuePair> urlParameters) throws Exception {

        final HttpResponse response = super.sendPostRequestWithParameters(this.client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        final Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        String sessionDataKey = DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                OAuth2Constant.SESSION_DATA_KEY);
        EntityUtils.consume(response.getEntity());
        if (StringUtils.isBlank(sessionDataKey)) {
            // A user session might already available - try retrieving session data key consent
            return DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                    OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        } else {
            return this.sendAuthCodeGrantAuthenticatePost(sessionDataKey);
        }
    }

    private List<NameValuePair> getConfiguredHybridFlowRequestParams(String responseType) {

        final List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                responseType));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, this.clientID));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        return urlParameters;
    }

    private List<NameValuePair> getNonConfiguredHybridFlowRequestParams() {

        final List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                RESPONSE_TYPE_CODE_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, this.clientID));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        return urlParameters;
    }

    /**
     * Provide user credentials and authenticate to the system.
     */
    private String sendAuthCodeGrantAuthenticatePost(String sessionDataKey) throws Exception {

        HttpResponse response = sendLoginPost(this.client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(this.client, locationHeader.getValue());
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        return DataExtractUtil.getParamFromURIString(locationHeader.getValue(),
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    /**
     * Refresh the cookie store and http client.
     */
    private void refreshHTTPClient() {

        cookieStore.clear();
        this.client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }
}
