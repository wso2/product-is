/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration test cases for validating id token claims.
 */
public class OIDCAuthzCodeIdTokenValidationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    public static final String TEST_NONCE = "test_nonce";
    private RSAPrivateKey spPrivateKey;
    private X509Certificate spX509PublicCert;
    private static final String CALLBACK_URL = "https://localhost/callback";
    private CloseableHttpClient client;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String idToken;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        client = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        consumerSecret = null;
        spPrivateKey = null;
        spX509PublicCert = null;

        client.close();
    }

    @Test(groups = "wso2.is", description = "Check Service Provider key generation.")
    public void testGenerateServiceProviderKeys() throws Exception {

        initServiceProviderKeys();
        Assert.assertNotNull(spPrivateKey);
        Assert.assertNotNull(spX509PublicCert);
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration.",
            dependsOnMethods = "testGenerateServiceProviderKeys")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithLocalAndOutboundConfigs(oAuthConsumerAppDTO);
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setUseTenantDomainInLocalSubjectIdentifier(true);
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setUseUserstoreDomainInLocalSubjectIdentifier(true);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testRegisterApplication")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, TEST_NONCE));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters
                        ,
                        OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.",
            dependsOnMethods = "testAuthCodeGrantSendAuthRequestPost")
    public void testAuthCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testAuthCodeGrantSendLoginPost")
    public void testAuthCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send get access token request.",
            dependsOnMethods = "testAuthCodeGrantSendApprovalPost")
    public void testAuthCodeGrantSendGetTokensPost() throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(idToken).getJWTClaimsSet();
        Assert.assertEquals(jwtClaimsSet.getClaim("nonce"), TEST_NONCE, "Invalid nonce received.");
        Assert.assertEquals(jwtClaimsSet.getSubject(), "testuser11@carbon.super", "Invalid subject received.");
        Assert.assertEquals(jwtClaimsSet.getIssuer(), "https://localhost:9853/oauth2/token", "Invalid issuer received.");
    }

    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    private void initServiceProviderKeys() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        String jksPath = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp1KeyStore.jks";
        String jksPassword = "wso2carbon";

        keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());

        String alias = "wso2carbon";
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(jksPassword.toCharArray()));
        spPrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();

        // Load certificate chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        spX509PublicCert = (X509Certificate) chain[0];
    }
}
