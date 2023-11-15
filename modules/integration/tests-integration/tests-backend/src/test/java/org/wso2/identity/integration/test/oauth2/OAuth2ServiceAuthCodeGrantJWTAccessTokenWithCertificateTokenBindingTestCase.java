/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.oauth2.consented.token.OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains test cases for Authorization Grant & Refresh Grant with JWT access token when consented token
 * column and certificate based token binding type is enabled along with token binding validation enabled.
 */
public class OAuth2ServiceAuthCodeGrantJWTAccessTokenWithCertificateTokenBindingTestCase extends
        OAuth2ServiceWithConsentedTokenColumnAbstractIntegrationTest {

    private final List<NameValuePair> consentParameters = new ArrayList<>();
    private String sessionDataKeyConsent;
    private String sessionDataKey;
    private String authorizationCode;

    private static final String CERTIFICATE_HEADER = "x-wso2-mtls-cert";
    private static final String CERTIFICATE = "-----BEGIN CERTIFICATE-----" +
            "MIIF3jCCBMagAwIBAgIEWcaVDDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjIxMDE3MDMyNTA2WhcNMjMxMTE3" +
            "MDM1NTA2WjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBAM5GdwqU9RI6/gX310jqeci//ABudGX7EHyX3rFc55PXR0x3" +
            "3BoP1b/IajT82F4lElBaKKsI/euGrHsdjfmWNXHl9wLtp+UJtENabsh6BMdDH4PV" +
            "r5QvsR2NpR+8K3Tt/4AexNFZ0BDcjCfMC8Oba1g+y04I09s6mMpqUoK2HPwGHGib" +
            "4IB3gTd2KPamNJ4RdkR6i04GHV3HXGHjcWNU2GaANK2ijKaXngL5AV42MKft4tkD" +
            "Vj0aKTlUJ+dclaHjkp08RJLBKlakdKFGiEkrsIjLBeSkhe69kKbhkEeFjDcrLyhX" +
            "FZAhME76vI/FszhQ7cFCtvAKwVSCK7QAe51Y4D0CAwEAAaOCApgwggKUMA4GA1Ud" +
            "DwEB/wQEAwIHgDCBkQYIKwYBBQUHAQMEgYQwgYEwEwYGBACORgEGMAkGBwQAjkYB" +
            "BgMwagYGBACBmCcCMGAwOTARBgcEAIGYJwECDAZQU1BfUEkwEQYHBACBmCcBAwwG" +
            "UFNQX0FJMBEGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0" +
            "aG9yaXR5DAZHQi1GQ0EwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC" +
            "MIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHCMCoGCCsGAQUFBwIBFh5o" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMGCCsGAQUFBwICMIGGDIGD" +
            "VXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBv" +
            "ZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZpY2F0aW9uIFBvbGljaWVz" +
            "IGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1lbnQwbQYIKwYBBQUHAQEE" +
            "YTBfMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA1Bggr" +
            "BgEFBQcwAoYpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5j" +
            "cnQwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3Bw" +
            "X2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy03f0/gASBoFceXluP1Aw" +
            "HQYDVR0OBBYEFH5qLX23ts28fgzvuSy9rhruFHTkMA0GCSqGSIb3DQEBCwUAA4IB" +
            "AQCUIjPcyU209ZbBbFrV7udTvRFyAPv9IYgjsQRmIHbDfWogyfF7BxSSpRJIw3yT" +
            "2WhmtEG5P5ZgAKEknVBbMkd6y2oiAcNc2gW5s9tjeiQXe3ZhlwiCnkC8xtKyFpye" +
            "6DD4d8ERZYFKzcqD5UOUTXTEX99sqhfMZb/ygYLwr6rscvr5bb7aXqrYXtJ+HVct" +
            "w4RzuknjWypNe0bjQjRKgvn0KRmL2xx8u06kvfAYKHM/XCKE4YLHVtrcEQZWpKNC" +
            "i5NGCZsIaJtjRWQMVKxH34+/Qu10s4KvetgCQSC9fuSjedzAthRFo6GOVfaxaPbX" +
            "se7+quaDs/AznFtHiwHP/EGK" +
            "-----END CERTIFICATE-----";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.initConsentedTokenTest();
        createUser();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        removeUser();
        resetVariables();
    }

    @Test(groups = "wso2.is", description = "Create application.")
    public void testRegisterApplication() throws Exception {

        registerApplication();
    }

    @Test(groups = "wso2.is", description = "Send authorize user request.", dependsOnMethods = "testRegisterApplication")
    public void testSendAuthorizedPost() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", consumerKey));
        urlParameters.add(new BasicNameValuePair("callbackurl", OAuth2Constant.CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " +
                OAuth2Constant.OAUTH2_SCOPE_EMAIL));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                OAuth2Constant.AUTHORIZED_USER_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Authorization request failed. Authorized response header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed. Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response, keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null.");

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.", dependsOnMethods = "testSendAuthorizedPost")
    public void testSendLoginPost() throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        EntityUtils.consume(response.getEntity());
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null.");
        response = sendConsentGetRequest(locationHeader.getValue(), consentParameters);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null.");

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send approval post request.", dependsOnMethods = "testSendLoginPost")
    public void testSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPostWithConsent(client, sessionDataKeyConsent, consentParameters);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");
        EntityUtils.consume(response.getEntity());

        response = sendPostRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Get Activation response is invalid.");

        authorizationCode = locationHeader.getValue().split("code=")[1].split("&")[0];
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Obtain access token when TLS certificate is passed in the access token request.",
            dependsOnMethods = "testSendApprovalPost")
    public void testGetAccessTokenWithTlsCertificate() throws Exception {

        TokenResponse tokenResponse = sendAccessTokenRequest(true);
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");
        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        validateUserClaims(oidcTokens);
    }

    @Test(groups = "wso2.is", description = "Validate access token from the introspection endpoint.",
            dependsOnMethods = "testGetAccessTokenWithTlsCertificate")
    public void testIntrospectAccessToken() throws Exception {

        JSONObject responseObject = introspectToken();
        Assert.assertNotNull(responseObject, "Validate access token failed. response is invalid.");
        Assert.assertNotNull(responseObject.get("cnf"), "TLS certificate thumbprint is not included in the response.");
        Assert.assertEquals(responseObject.get("active"), true, "Access token is not active.");
        Assert.assertEquals(((Map<String, String>) responseObject.get("cnf")).get("x5t#S256"),
                "R4Hj_0nNzIzVSPdCcsWlhNKm0aB4Pszp6ZZ4K1iR8o4", "TLS certificate thumbprint does not match.");
    }

    @Test(groups = "wso2.is", description = "Validate the user claim values when TLS certificate is passed in the user info " +
            "endpoint request.", dependsOnMethods = "testIntrospectAccessToken")
    public void testClaimsWithTlsCertificate() throws Exception {

        HttpResponse userInfoEndpointResponse = sendRequestToUserInfoEndpoint(true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(userInfoEndpointResponse.getEntity().getContent()));
        JSONObject responseObject = (JSONObject) JSONValue.parse(reader);
        EntityUtils.consume(userInfoEndpointResponse.getEntity());
        Assert.assertNotNull(responseObject.get("sub").toString(), "Subject claim is not included in the response.");
    }

    @Test(groups = "wso2.is", description = "Validate error scenario when when TLS certificate is not passed in the user info " +
            "endpoint request.", dependsOnMethods = "testClaimsWithTlsCertificate")
    public void testClaimsWithoutTlsCertificate() throws Exception {

        HttpResponse userInfoEndpointResponse = sendRequestToUserInfoEndpoint(false);
        Assert.assertTrue(userInfoEndpointResponse.getStatusLine().getStatusCode() == 400,
                "Client error not received when TLS certificate is not sent.");
    }

    @Test(groups = "wso2.is", description = "Validate error scenario when when TLS certificate is not passed in the " +
            "refresh token request.", dependsOnMethods = "testClaimsWithoutTlsCertificate")
    public void testGetRefreshTokenWithoutTlsCertificate() throws Exception {

        AuthorizationGrant refreshGrant = new RefreshTokenGrant(new RefreshToken(refreshToken));
        TokenResponse tokenResponse = sendRefreshTokenRequest(refreshGrant, false);
        Assert.assertTrue(tokenResponse instanceof TokenErrorResponse, "Client error not received when TLS certificate is not sent.");
        if (tokenResponse instanceof TokenErrorResponse) {
            TokenErrorResponse tokenErrorResponse = tokenResponse.toErrorResponse();
            Assert.assertEquals(tokenErrorResponse.getErrorObject().getHTTPStatusCode(), 400);
            Assert.assertEquals(tokenErrorResponse.getErrorObject().getDescription(),
                    "Invalid token binding value is present in the request.");
        }
    }

    @Test(groups = "wso2.is", description = "Obtain refresh token when TLS certificate is passed in the refresh token request.",
            dependsOnMethods = "testGetRefreshTokenWithoutTlsCertificate")
    public void testGetRefreshTokenWithTlsCertificate() throws Exception {

        AuthorizationGrant refreshGrant = new RefreshTokenGrant(new RefreshToken(refreshToken));
        TokenResponse tokenResponse = sendRefreshTokenRequest(refreshGrant, true);
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");
        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token.");
    }

    @Test(groups = "wso2.is", description = "Validate error scenario when when TLS certificate is not passed in the " +
            "access token request.", dependsOnMethods = "testGetRefreshTokenWithTlsCertificate")
    public void testGetAccessTokenWithoutTlsCertificate() throws Exception {

        resetUserAndGetAuthorizationCode();
        TokenResponse tokenResponse = sendAccessTokenRequest(false);
        Assert.assertTrue(tokenResponse instanceof TokenErrorResponse,
                "Client error not received when TLS certificate is not sent.");
        if (tokenResponse instanceof TokenErrorResponse) {
            TokenErrorResponse tokenErrorResponse = tokenResponse.toErrorResponse();
            Assert.assertEquals(tokenErrorResponse.getErrorObject().getHTTPStatusCode(), 400);
            Assert.assertEquals(tokenErrorResponse.getErrorObject().getDescription(),
                    "TLS certificate not found in the request.");
        }
    }

    protected void registerApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setBindingType("certificate");
        accessTokenConfig.setValidateTokenBinding(true);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);
        oidcConfig.setAccessToken(accessTokenConfig);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);
        application.setClaimConfiguration(setApplicationClaimConfig());

        applicationId = addApplication(application);
        Assert.assertNotNull(applicationId, "Application creation failed.");

        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();
    }

    private HttpResponse sendConsentGetRequest(String locationURL,
            List<NameValuePair> consentRequiredClaimsFromResponse) throws Exception {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultCookieStore(cookieStore)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse response = httpClientWithoutAutoRedirections.execute(getRequest);

        consentRequiredClaimsFromResponse.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        HttpResponse httpResponse = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        return httpResponse;
    }

    private TokenResponse sendAccessTokenRequest(boolean isTlsCertificateSentInRequest) throws URISyntaxException,
            IOException, ParseException {

        URI callbackURI = new URI(OAuth2Constant.CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant =
                new AuthorizationCodeGrant(new AuthorizationCode(authorizationCode), callbackURI);
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationCodeGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HTTPRequest httpRequest = request.toHTTPRequest();
        if (isTlsCertificateSentInRequest) {
            httpRequest.setHeader(CERTIFICATE_HEADER, CERTIFICATE);
        }
        HTTPResponse tokenHTTPResp = httpRequest.send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");

        return tokenResponse;
    }

    private void validateUserClaims(OIDCTokens oidcTokens) throws JsonProcessingException {

        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token.");
        accessToken = oidcTokens.getAccessToken().getValue();
        refreshToken = oidcTokens.getRefreshToken().getValue();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseObject = mapper.readValue(new String(Base64.decodeBase64(
                accessToken.split("\\.")[1])), Map.class);

        Assert.assertNotNull(responseObject.get("cnf"), "TLS certificate thumbprint is not included in the response.");
        Assert.assertEquals(((Map<String, String>) responseObject.get("cnf")).get("x5t#S256"),
                "R4Hj_0nNzIzVSPdCcsWlhNKm0aB4Pszp6ZZ4K1iR8o4", "TLS certificate thumbprint is not matching.");
    }

    private JSONObject introspectToken() throws Exception {

        String introspectionUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.INTRO_SPEC_ENDPOINT : OAuth2Constant.TENANT_INTRO_SPEC_ENDPOINT;
        return introspectTokenWithTenant(client, accessToken, introspectionUrl,
                USERNAME, PASSWORD);
    }

    private HttpResponse sendRequestToUserInfoEndpoint(boolean isTlsCertificateSentInRequest) throws IOException {

        HttpGet request = new HttpGet(OAuth2Constant.USER_INFO_ENDPOINT);
        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        if (isTlsCertificateSentInRequest) {
            request.setHeader(CERTIFICATE_HEADER, CERTIFICATE);
        }
        return client.execute(request);
    }

    private TokenResponse sendRefreshTokenRequest(AuthorizationGrant authorizationGrant, boolean isTlsCertificateSentInRequest)
            throws URISyntaxException, IOException, ParseException {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HTTPRequest httpRequest = request.toHTTPRequest();
        if (isTlsCertificateSentInRequest) {
            httpRequest.setHeader(CERTIFICATE_HEADER, CERTIFICATE);
        }
        HTTPResponse tokenHTTPResp = httpRequest.send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");

        return tokenResponse;
    }

    private void resetUserAndGetAuthorizationCode() throws Exception {

        removeUser();
        createUser();
        testSendAuthorizedPost();
        testSendLoginPost();
        consentParameters.remove(0);
        testSendApprovalPost();
    }
}
