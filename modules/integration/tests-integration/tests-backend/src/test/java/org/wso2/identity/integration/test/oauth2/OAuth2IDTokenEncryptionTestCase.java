/*
 * Copyright (c) 2018, WSO2 LLC. (https://www.wso2.com).
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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
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
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
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
    private static final String ENCRYPTION_ALGORITHM = "RSA-OAEP";
    private static final String ENCRYPTION_METHOD = "A256GCM";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient client;

    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String idToken;
    private String consumerKey;
    private String consumerSecret;
    private ApplicationResponseModel application;
    private OpenIDConnectConfiguration oidcInboundConfig;

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

        deleteApp(application.getId());

        consumerKey = null;
        consumerSecret = null;
        spPrivateKey = null;
        spX509PublicCert = null;
        application = null;
        oidcInboundConfig = null;

        client.close();
        restClient.closeHttpClient();
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

        application = getBasicOAuthApplication(CALLBACK_URL);
        Assert.assertNotNull(application, "OAuth App creation failed.");

        oidcInboundConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcInboundConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        consumerSecret = oidcInboundConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Check Updating public cert of Service Provider.",
            dependsOnMethods = "testRegisterApplication")
    public void updateServiceProviderCert() throws Exception {

        updateApplicationCertificate(application.getId(), spX509PublicCert);

        ApplicationResponseModel updatedApplication = getApplication(application.getId());
        Assert.assertNotNull(updatedApplication, "Application: " + application.getName() + " retrieval failed.");
        Assert.assertNotNull(updatedApplication.getAdvancedConfigurations().getCertificate(),
                "Application Certificate update failed");
    }

    @Test(groups = "wso2.is", description = "Setup encryption algorithm and encryption method.",
            dependsOnMethods = "updateServiceProviderCert")
    public void testConfigureIDTokenEncryptionAlgorithms() throws Exception {

        oidcInboundConfig.getIdToken().getEncryption().setEnabled(true);
        oidcInboundConfig.getIdToken().getEncryption().setAlgorithm(ENCRYPTION_ALGORITHM);
        oidcInboundConfig.getIdToken().getEncryption().setMethod(ENCRYPTION_METHOD);
        updateApplicationInboundConfig(application.getId(), oidcInboundConfig, OIDC);

        OpenIDConnectConfiguration updatedOidcInboundConfig = getOIDCInboundDetailsOfApplication(application.getId());
        Assert.assertTrue(updatedOidcInboundConfig.getIdToken().getEncryption().getEnabled(),
                "Enforcing ID Token encryption failed.");
        Assert.assertEquals(updatedOidcInboundConfig.getIdToken().getEncryption().getAlgorithm(),
                ENCRYPTION_ALGORITHM, "Configuring encryption algorithm failed.");
        Assert.assertEquals(updatedOidcInboundConfig.getIdToken().getEncryption().getMethod(),
                ENCRYPTION_METHOD, "Configuring encryption method failed.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testConfigureIDTokenEncryptionAlgorithms")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

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
    public void testEncryptionAlgorithmAndEncryptionMethod() throws Exception {

        EncryptedJWT jwt = EncryptedJWT.parse(idToken);
        JWEHeader header = jwt.getHeader();
        String algorithm = header.getAlgorithm().getName();
        Assert.assertEquals(algorithm, ENCRYPTION_ALGORITHM, "Encryption algorithm configuration failed.");
        String method = header.getEncryptionMethod().getName();
        Assert.assertEquals(method, ENCRYPTION_METHOD, "Encryption method configuration failed.");
    }

    @Test(groups = "wso2.is", description = "Decrypted the id token.",
            dependsOnMethods = "testEncryptionAlgorithmAndEncryptionMethod")
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
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
        // Send a direct implicit token request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE, OAuth2Constant.ID_TOKEN));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_NONCE, UUID.randomUUID().toString()));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID_WITH_INTERNAL_LOGIN));

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
        client = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();
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
     * @throws Exception If an error occurred while getting certificate.
     */
    private void initServiceProviderKeys() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        String jksPath = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp1KeyStore.jks";
        String jksPassword = "wso2carbon";

        keyStore.load(Files.newInputStream(Paths.get(jksPath)), jksPassword.toCharArray());

        String alias = "wso2carbon";
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(jksPassword.toCharArray()));
        spPrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();

        // Load certificate chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        spX509PublicCert = (X509Certificate) chain[0];
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException        If an error occurred while getting Session Data key Consent.
     * @throws URISyntaxException If an error occurred while extracting Session Data key parameter.
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
     * @throws ParseException If an error occurred while getting jwt.
     * @throws JOSEException  If an error occurred while decrypting jwt.
     */
    private boolean decryptAndCheckIDToken(String idToken, String audience) throws ParseException, JOSEException {

        EncryptedJWT jwt = EncryptedJWT.parse(idToken);

        // Create a decrypter with the specified private RSA key.
        RSADecrypter decrypter = new RSADecrypter(spPrivateKey);
        jwt.decrypt(decrypter);

        JWTClaimsSet claims = jwt.getPayload().toSignedJWT().getJWTClaimsSet();
        Assert.assertNotNull(claims, "ID token claim set is null");

        String aud = claims.getAudience().get(0);
        return StringUtils.equals(aud, audience);
    }
}
