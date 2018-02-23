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
package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Integration test cases for id token encryption.
 */
public class OAuth2IDTokenEncryptionTestCase extends OAuth2ServiceAbstractIntegrationTest {

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
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Check Updating public cert of Service Provider.",
            dependsOnMethods = "testRegisterApplication")
    public void updateServiceProviderCert() throws Exception {

        ServiceProvider application = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(application, "Application: " + SERVICE_PROVIDER_NAME + " retrieval failed.");

        application.setCertificateContent(convertToPem(spX509PublicCert));
        appMgtclient.updateApplicationData(application);

        ServiceProvider updatedApp = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(updatedApp, "Updated application: " + SERVICE_PROVIDER_NAME +
                " retrieval failed.");
        Assert.assertNotNull(updatedApp.getCertificateContent(), "Updating application certificate failed.");
    }

    @Test(groups = "wso2.is", description = "Check enabling option to encrypt ID tokens.",
            dependsOnMethods = "updateServiceProviderCert")
    public void testEnforceIDTokenEncryption() throws Exception {

        OAuthConsumerAppDTO consumerAppDTO = adminClient.getOAuthAppByConsumerKey(consumerKey);
        consumerAppDTO.setIdTokenEncryptionEnabled(true);
        adminClient.updateConsumerApp(consumerAppDTO);
        OAuthConsumerAppDTO updateApp = adminClient.getOAuthAppByConsumerKey(consumerKey);
        Assert.assertTrue(updateApp.getIdTokenEncryptionEnabled(), "Enforcing ID Token encryption failed.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testEnforceIDTokenEncryption")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
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
    }

    @Test(groups = "wso2.is", description = "Decrypted the id token.",
            dependsOnMethods = "testAuthCodeGrantSendGetTokensPost")
    public void testAuthCodeGrantDecryptIDToken() throws Exception {

        Assert.assertTrue(decryptAndCheckIDToken(idToken, consumerKey),
                "Audience value in encrypted ID token does not match.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for implicit grant type.",
            dependsOnMethods = "testAuthCodeGrantDecryptIDToken")
    public void testImplicitGrantSendAuthRequestPost() throws Exception {

        // Remove previous data from variables.
        sessionDataKey = null;
        sessionDataKeyConsent = null;
        idToken = null;

        // Reset client.
        client = HttpClientBuilder.create().disableRedirectHandling().build();

        // Send a direct implicit token request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.ID_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters,
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
            dependsOnMethods = "testImplicitGrantSendAuthRequestPost")
    public void testImplicitGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(client, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testImplicitGrantSendLoginPost")
    public void testImplicitGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(client, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        // Extract authorization code from the location value.
        idToken = DataExtractUtil.extractParamFromURIFragment(locationHeader.getValue(), OAuth2Constant.ID_TOKEN);
        Assert.assertNotNull(idToken, "ID token is null");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Decrypted the id token.",
            dependsOnMethods = "testImplicitGrantSendApprovalPost")
    public void testImplicitGrantDecryptIDToken() throws Exception {

        Assert.assertTrue(decryptAndCheckIDToken(idToken, consumerKey),
                "Audience value in encrypted ID token does not match.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for resource owner grant type.",
            dependsOnMethods = "testImplicitGrantDecryptIDToken")
    public void testResourceOwnerGrantSendAuthRequestPost() throws Exception {

        // Remove previous data from variables.
        sessionDataKey = null;
        sessionDataKeyConsent = null;
        idToken = null;

        // Reset client.
        client = HttpClientBuilder.create().disableRedirectHandling().build();

        String username = "admin";
        Secret password = new Secret("admin");
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(username, password);

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        Scope scope = new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID);

        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, scope);

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
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
    }

    @Test(groups = "wso2.is", description = "Decrypted the id token.",
            dependsOnMethods = "testResourceOwnerGrantSendAuthRequestPost")
    public void testResourceOwnerGrantDecryptIDToken() throws Exception {

        Assert.assertTrue(decryptAndCheckIDToken(idToken, consumerKey),
                "Audience value in encrypted ID token does not match.");
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    /**
     * Initiate service provider keys required for the tests.
     *
     * @throws Exception
     */
    private void initServiceProviderKeys() throws Exception {

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);

        KeyPair kp = keyGenerator.genKeyPair();
        RSAPublicKey sp1RsaPublicKey = (RSAPublicKey) kp.getPublic();
        spPrivateKey = (RSAPrivateKey) kp.getPrivate();

        spX509PublicCert = getX509PublicCert(sp1RsaPublicKey, spPrivateKey);
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException
     * @throws URISyntaxException
     */
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

    /**
     * Decrypt a given id token and compare audience value with consumer key.
     *
     * @param idToken  Encrypted ID token to be decrypted and checked.
     * @param audience Audience value that should be appeared in the token.
     * @return Boolean True if audience matches, False otherwise.
     * @throws ParseException
     * @throws JOSEException
     */
    private boolean decryptAndCheckIDToken(String idToken, String audience) throws ParseException, JOSEException {

        EncryptedJWT jwt = EncryptedJWT.parse(idToken);

        // Create a decrypter with the specified private RSA key.
        RSADecrypter decrypter = new RSADecrypter(spPrivateKey);
        jwt.decrypt(decrypter);

        ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
        Assert.assertNotNull(claims, "ID token claim set is null");

        String aud = claims.getAudience().get(0);
        return StringUtils.equals(aud, audience);
    }
}
