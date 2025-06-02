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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OAuth2PushedAuthRequestTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String REQUEST_URI = "request_uri";
    private static final String EXPIRY_TIME = "expires_in";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String REQUEST = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwi" +
            "YXVkIjpbImh0dHBzOi8vbG9jYWxob3N0Ojk0NDMvb2F1dGgyL3Rva2VuIl0sImNsYWltcyI6eyJ1c2VyaW5mbyI6eyJnaXZlbl9uYW" +
            "1lIjp7ImVzc2VudGlhbCI6dHJ1ZX0sIm5pY2tuYW1lIjpudWxsLCJlbWFpbCI6eyJlc3NlbnRpYWwiOnRydWV9LCJleHRlcm5hbENs" +
            "YWltMSI6eyJlc3NlbnRpYWwiOnRydWV9LCJwaWN0dXJlIjpudWxsfSwiaWRfdG9rZW4iOnsiZ2VuZGVyIjpudWxsLCJiaXJ0aGRhdGU" +
            "iOnsiZXNzZW50aWFsIjp0cnVlfSwiY3VzdG9tQ2xhaW0xIjp7ImVzc2VudGlhbCI6dHJ1ZX0sImFjciI6eyJ2YWx1ZXMiOlsidXJuOm1" +
            "hY2U6aW5jb21tb246aWFwOnNpbHZlciJdfX19LCJpc3MiOiJLUjFwS0x1Z2RSUTlCbmNsTTV0YUMzVjNHZjBhIiwiaWF0IjoxNTE2Nzg" +
            "zMjc4LCJqdGkiOiIxMDAzIn0=.";
    private String consumerKey;
    private String consumerSecret;
    private String appId;
    private String expiryTime;
    private String requestUri;
    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();

        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(appId);
        consumerKey = null;
        consumerSecret = null;
        appId = null;
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
        appId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Send PAR", dependsOnMethods = "testRegisterApplication")
    public void testSendPar() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(responseString);
        if (jsonResponse == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        requestUri = jsonResponse.get(REQUEST_URI).toString();
        expiryTime = jsonResponse.get(EXPIRY_TIME).toString();
        Assert.assertNotNull(requestUri, "request_uri is null");
        Assert.assertNotNull(expiryTime, "expiry_time is null");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testSendPar")
    public void testSendAuthorize() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(REQUEST_URI, requestUri));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        Assert.assertNotNull(responseString, "Authorized response is null");
    }

    @Test(groups = "wso2.is", description = "Send PAR with openid request object", dependsOnMethods =
            "testRegisterApplication")
    public void testSendParWithRequestObject() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH_OIDC_REQUEST, REQUEST));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));
        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(responseString);
        if (jsonResponse == null) {
            throw new Exception("Error occurred while getting the response.");
        }
        requestUri = jsonResponse.get(REQUEST_URI).toString();
        expiryTime = jsonResponse.get(EXPIRY_TIME).toString();
        Assert.assertNotNull(requestUri, "request_uri is null");
        Assert.assertNotNull(expiryTime, "expiry_time is null");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with invalid client id",
            dependsOnMethods = "testSendPar")
    public void testSendAuthorizeWithInvalidClient() throws Exception {

        testSendPar();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(REQUEST_URI, requestUri));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, "invalid_client_id"));

        HttpResponse response = sendPostRequest(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, urlParameters);
        String locationHeader = response.getFirstHeader("Location") != null ?
                response.getFirstHeader("Location").getValue() : null;

        Assert.assertNotNull(locationHeader, "Location header from the authz response is null");
        Assert.assertTrue(StringUtils.contains(locationHeader, "oauthErrorMsg=par.client.id.not.match"));
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with invalid request uri",
            dependsOnMethods = "testSendAuthorizeWithInvalidClient")
    public void testSendAuthorizeWithInvalidRequestURI() throws Exception {

        testSendPar();
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(REQUEST_URI,
                "urn:ietf:params:oauth:par:request_uri:invalid_request_uri"));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));

        HttpResponse response = sendPostRequest(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, urlParameters);
        String locationHeader = response.getFirstHeader("Location") != null ?
                response.getFirstHeader("Location").getValue() : null;

        Assert.assertNotNull(locationHeader, "Location header from the authz response is null");
        Assert.assertTrue(StringUtils.contains(locationHeader, "oauthErrorMsg=par.invalid.request.uri"));
    }

    @Test(groups = "wso2.is", description = "Send authorize user request with expired request uri",
            dependsOnMethods = "testSendAuthorizeWithInvalidRequestURI")
    public void testSendAuthorizeWithExpiredRequestURI() throws Exception {

        testSendPar();
        // Sleep for 1 min for request uri timeout
        Thread.sleep(60 * 1000);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(REQUEST_URI, requestUri));
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));

        HttpResponse response = sendPostRequest(OAuth2Constant.AUTHORIZE_ENDPOINT_URL, urlParameters);
        String locationHeader = response.getFirstHeader("Location") != null ?
                response.getFirstHeader("Location").getValue() : null;

        Assert.assertNotNull(locationHeader, "Location header from the authz response is null");
        Assert.assertTrue(StringUtils.contains(locationHeader, "oauthErrorMsg=par.request.uri.expired"));
    }

    @Test(groups = "wso2.is", description = "Send PAR with repeated param",
            dependsOnMethods = "testSendAuthorizeWithExpiredRequestURI")
    public void testSendParWithRepeatedParam() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, "repeated_redirect_uri"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid request with repeated parameters.", "invalid_request");
    }

    @Test(groups = "wso2.is", description = "Send PAR with invalid client id",
            dependsOnMethods = "testSendParWithRepeatedParam")
    public void testSendParWithInvalidClient() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, "invalid_consumerKey"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Client credentials are invalid.",
                "invalid_client");
    }

    @Test(groups = "wso2.is", description = "Send PAR without client id",
            dependsOnMethods = "testSendParWithInvalidClient")
    public void testSendParWithoutClient() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.UNAUTHORIZED.getStatusCode(),
                "Client ID not found in the request.", "invalid_client");
    }

    @Test(groups = "wso2.is", description = "Send PAR with invalid response type",
            dependsOnMethods = "testSendParWithoutClient")
    public void testSendParWithInvalidResponseType() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, "invalid_responseType"));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid response_type parameter value", "invalid_request");
    }

    @Test(groups = "wso2.is", description = "Send PAR with invalid redirect uri",
            dependsOnMethods = "testSendParWithInvalidResponseType")
    public void testSendParWithInvalidRedirectURI() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, "invalid_redirect_URI"));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "callback.not.match", "invalid_request");
    }

    @Test(groups = "wso2.is", description = "Send PAR with request uri",
            dependsOnMethods = "testSendParWithInvalidRedirectURI")
    public void testSendParWithRequestURI() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(CLIENT_ID_PARAM, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(REQUEST_URI,
                "urn:ietf:params:oauth:par:request_uri:75fb6713-62fa-4d2f-9f72-0e05eab0d331"));

        HttpResponse response = sendPostRequest(OAuth2Constant.PAR_ENDPOINT, urlParameters);
        assertResponse(response, Response.Status.BAD_REQUEST.getStatusCode(),
                "Request with request_uri not allowed.", "invalid_request");
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

    private HttpResponse sendPostRequest(String endpoint, List<NameValuePair> parameters) throws Exception {

        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setEntity(new UrlEncodedFormEntity(parameters));
        return client.execute(httpPost);
    }

    private void assertResponse(HttpResponse response, int expectedStatusCode,
                                String expectedErrorDescription, String expectedError) throws Exception {

        int responseCode = response.getStatusLine().getStatusCode();
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        EntityUtils.consume(response.getEntity());

        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(responseString);
        if (jsonResponse == null) {
            throw new Exception("Error occurred while getting the response.");
        }

        Assert.assertEquals(responseCode, expectedStatusCode, "Response status code does not match.");
        Assert.assertEquals(jsonResponse.get("error_description").toString(), expectedErrorDescription,
                "Error description is missing or invalid value");
        Assert.assertEquals(jsonResponse.get("error").toString(), expectedError,
                "Error is missing or invalid value");
    }
}
