/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OAuth2ResponseModeTestCase extends OAuth2ServiceAbstractIntegrationTest{

    private String applicationId;
    private String consumerKey;
    private String consumerSecret;
    private static final String CALLBACK_URL = OAuth2Constant.CALLBACK_URL;

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);

        consumerKey = null;
        consumerSecret = null;
        applicationId = null;

        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = createApp();
        Assert.assertNotNull(application, "OAuth App creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");
        applicationId = application.getId();
    }

    /**
     * Provide request data to test response modes.
     *
     * @return Object with testAuthCodeGrantSendAuthRequestPost method parameters.
     */
    @DataProvider(name = "responseModeDataProvider")
    private Object[][] responseModeDataProvider() {

        return new Object[][] {
                // Response type, provided response mode, response mode used in the response
                {OAuth2Constant.AUTHORIZATION_CODE_NAME, null, OAuth2Constant.RESPONSE_MODE_QUERY},
                {OAuth2Constant.AUTHORIZATION_CODE_NAME, OAuth2Constant.RESPONSE_MODE_QUERY, OAuth2Constant.RESPONSE_MODE_QUERY},
                {OAuth2Constant.AUTHORIZATION_CODE_NAME, OAuth2Constant.RESPONSE_MODE_JWT, OAuth2Constant.RESPONSE_MODE_QUERY_JWT},
                {OAuth2Constant.AUTHORIZATION_CODE_NAME, OAuth2Constant.RESPONSE_MODE_QUERY_JWT, OAuth2Constant.RESPONSE_MODE_QUERY_JWT},

                // This test data is being commented out temporarily until the hybrid flow response type application configuration is onboarded.
                // {OAuth2Constant.RESPONSE_TYPE_CODE_ID_TOKEN, null, OAuth2Constant.RESPONSE_MODE_FRAGMENT},
                // {OAuth2Constant.RESPONSE_TYPE_CODE_ID_TOKEN, OAuth2Constant.RESPONSE_MODE_FRAGMENT, OAuth2Constant.RESPONSE_MODE_FRAGMENT},
                // {OAuth2Constant.RESPONSE_TYPE_CODE_ID_TOKEN, OAuth2Constant.RESPONSE_MODE_JWT, OAuth2Constant.RESPONSE_MODE_FRAGMENT_JWT},
                // {OAuth2Constant.RESPONSE_TYPE_CODE_ID_TOKEN, OAuth2Constant.RESPONSE_MODE_FRAGMENT_JWT, OAuth2Constant.RESPONSE_MODE_FRAGMENT_JWT},
        };
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with response types and response modes.",
            dependsOnMethods = "testRegisterApplication", dataProvider = "responseModeDataProvider")
    public void testSendAuthRequestPost(String responseType, String responseModeProvided,
                                                     String responseMode) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, responseType));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_MODE, responseModeProvided));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());

        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);

        String sessionDataKey;
        if (sessionDataKeyConsent == null) {
            Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                    "sessionDataKey not found in response.");
            sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
            Assert.assertNotNull(sessionDataKey, "sessionDataKey is null.");

            sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        }

        response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        locationValue = getLocationHeaderValue(response);

        AuthorizationCode authorizationCode = getAuthorizationCode(locationValue, responseMode);
        Assert.assertNotNull(authorizationCode,
                "Authorization code is null or could not be found.");
        EntityUtils.consume(response.getEntity());
    }

    /**
     * Extract authorization code.
     * @param url redirection url
     * @param responseMode response mode which is used to send the request
     * @return AuthorizationCode object
     */
    private AuthorizationCode getAuthorizationCode(String url, String responseMode)
            throws URISyntaxException {

        String code;
        if (responseMode.contains("jwt")) {
            String responseJWT;
            Assert.assertTrue(url.contains("response"), "Response JWT not found in the response.");
            if (responseMode.contains("fragment")) {
                responseJWT =  DataExtractUtil.extractParamFromURIFragment(url, "response");
                Assert.assertNotNull(responseJWT, "Response JWT not found as a fragment parameter.");
            } else {
                responseJWT = DataExtractUtil.getParamFromURIString(url, "response");
                Assert.assertNotNull(responseJWT, "Response JWT not found as a query parameter.");
            }
            Assert.assertNotNull(responseJWT, "Response JWT not found.");
            try {
                // decode the response and get code
                JWTClaimsSet jwtClaimsSet = extractJwt(responseJWT);
                code = jwtClaimsSet.getStringClaim(OAuth2Constant.AUTHORIZATION_CODE_NAME);
            } catch (ParseException e) {
                throw new URISyntaxException(url, "Error while parsing JWT token.");
            }
        } else {
            Assert.assertTrue(url.contains("code"), "code not found in the response.");
            if (responseMode.contains("fragment")) {
                code =  DataExtractUtil.extractParamFromURIFragment(url, OAuth2Constant.AUTHORIZATION_CODE_NAME);
            } else {
                code = DataExtractUtil.getParamFromURIString(url, OAuth2Constant.AUTHORIZATION_CODE_NAME);
            }
        }
        Assert.assertNotNull(code, "Authorization code not found.");
        return new AuthorizationCode(code);
    }

    /**
     * Extract JWT token and assign to a map.
     * @param jwtToken jwt token
     * @return jwt claim set
     */
    private JWTClaimsSet extractJwt(String jwtToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtToken);
        return signedJWT.getJWTClaimsSet();
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException Error
     * @throws URISyntaxException Error
     */
    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        String locationValue = getLocationHeaderValue(response);
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationValue);
        Assert.assertNotNull(response, "GET request response is null.");

        locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        String sessionDataKeyConsent = DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.SESSION_DATA_KEY_CONSENT);
        Assert.assertNotNull(sessionDataKeyConsent, "sessionDataKeyConsent is null.");
        return sessionDataKeyConsent;
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location, "Location header is null.");
        return location.getValue();
    }

    /**
     * Create Application with the given app configurations
     *
     * @return ApplicationResponseModel
     * @throws Exception exception
     */
    private ApplicationResponseModel createApp() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm",
                "urn:ietf:params:oauth:grant-type:device_code");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setPublicClient(true);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);

        String appId = addApplication(application);

        return getApplication(appId);
    }
}
