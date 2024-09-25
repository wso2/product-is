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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Integration tests for Signed Request Object validation.
 */
public class OAuth2RequestObjectSignatureValidationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private RSAPrivateKey sp1PrivateKey;
    private X509Certificate sp1X509PublicCert;
    private RSAPrivateKey sp2PrivateKey;
    private static final String CALLBACK_URL = "https://localhost/callback";
    private ApplicationResponseModel application;
    private OpenIDConnectConfiguration oidcInboundConfig;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(application.getId());

        consumerKey = null;
        consumerSecret = null;
        sp1PrivateKey = null;
        sp2PrivateKey = null;
        sp1X509PublicCert = null;
        application = null;
        oidcInboundConfig = null;
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Service Provider key generation")
    public void testGenerateServiceProviderKeys() throws Exception {

        initServiceProviderKeys();
        Assert.assertNotNull(sp1PrivateKey);
        Assert.assertNotNull(sp2PrivateKey);
        Assert.assertNotNull(sp1X509PublicCert);
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration",
            dependsOnMethods = "testGenerateServiceProviderKeys")
    public void testRegisterApplication() throws Exception {

        application = getBasicOAuthApplication(CALLBACK_URL);
        Assert.assertNotNull(application, "OAuth App creation failed.");

        oidcInboundConfig = getOIDCInboundDetailsOfApplication(application.getId());

        consumerKey = oidcInboundConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");

        consumerSecret = oidcInboundConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret, "Application creation failed.");
    }

    @Test(groups = "wso2.is", description = "Check Updating public cert of Service Provider",
            dependsOnMethods = "testRegisterApplication")
    public void updateServiceProviderCert() throws Exception {

        updateApplicationCertificate(application.getId(), sp1X509PublicCert);

        ApplicationResponseModel updatedApplication = getApplication(application.getId());
        Assert.assertNotNull(updatedApplication.getAdvancedConfigurations().getCertificate(),
                "Application Certificate update failed");
    }

    @Test(groups = "wso2.is", description = "Check Initial OAuth2 Authorize Request",
            dependsOnMethods = "updateServiceProviderCert")
    public void sentAuthorizationGrantRequest() throws Exception {

        try (CloseableHttpClient client = getRedirectDisabledClient()) {
            HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL));
            // If the request is valid it will return a 302 to redirect to the login page.
            assertForLoginPage(response);
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(groups = "wso2.is", description = "Check Initial OAuth2 Authorize Request with unsigned request object",
            dependsOnMethods = "sentAuthorizationGrantRequest")
    public void sendAuthorizationGrantRequestWithPlainJWTRequestObject() throws Exception {

        try (CloseableHttpClient client = getRedirectDisabledClient()) {
            String unsignedRequestObject = buildPlainJWT(consumerKey);
            HttpResponse response =
                    sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, unsignedRequestObject));
            assertForLoginPage(response);
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(groups = "wso2.is", description = "Check enabling option to enforce request object signature validation",
            dependsOnMethods = "sendAuthorizationGrantRequestWithPlainJWTRequestObject")
    public void testEnforceRequestObjectSignatureValidation() throws Exception {

        oidcInboundConfig.setValidateRequestObjectSignature(true);
        updateApplicationInboundConfig(application.getId(), oidcInboundConfig, OIDC);

        OpenIDConnectConfiguration updatedOidcInboundConfig = getOIDCInboundDetailsOfApplication(application.getId());
        Assert.assertTrue(updatedOidcInboundConfig.getValidateRequestObjectSignature(),
                "ValidateRequestObjectSignature enable failed");
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation was enforced by sending" +
            " a unsigned request object", dependsOnMethods = "testEnforceRequestObjectSignatureValidation")
    public void sendUnsuccessfulAuthorizationGrantRequestWithPlainJWTRequestObject() throws Exception {

        try (CloseableHttpClient client = getRedirectDisabledClient()) {
            String unsignedRequestObject = buildPlainJWT(consumerKey);
            HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, unsignedRequestObject));
            // Since we have enforced request object validation we should be redirected to the error page.
            assertForErrorPage(response);
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation by sending a valid signed" +
            " request object", dependsOnMethods = "sendUnsuccessfulAuthorizationGrantRequestWithPlainJWTRequestObject")
    public void sendSuccessfulAuthorizationGrantRequestWithSignedRequestObject() throws Exception {

        try (CloseableHttpClient client = getRedirectDisabledClient()) {
            String signedRequestObject = buildSignedJWT(consumerKey, sp1PrivateKey);
            HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, signedRequestObject));
            assertForLoginPage(response);
            EntityUtils.consume(response.getEntity());
        }
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation by sending an invalid signed" +
            " request object", dependsOnMethods = "sendSuccessfulAuthorizationGrantRequestWithSignedRequestObject")
    public void sendUnSuccessfulAuthorizationGrantRequestWithSignedRequestObjectWithDifferentPrivateKey() throws
            Exception {

        try (CloseableHttpClient client = getRedirectDisabledClient()) {
            String signedRequestObject = buildSignedJWT(consumerKey, sp2PrivateKey);
            HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, signedRequestObject));
            assertForErrorPage(response);
            EntityUtils.consume(response.getEntity());
        }
    }

    private void assertForLoginPage(HttpResponse response) {

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("sessionDataKey="));
        Assert.assertFalse(locationValue.contains("error=invalid_request"));
    }

    private void assertForErrorPage(HttpResponse response) {

        String locationValue = getLocationHeaderValue(response);
        Assert.assertFalse(locationValue.contains("sessionDataKey="));
        Assert.assertTrue(locationValue.contains("error=invalid_request"));
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    private CloseableHttpClient getRedirectDisabledClient() {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        return HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .disableRedirectHandling()
                .build();
    }

    private String getAuthzRequestUrl(String clientId, String callbackUrl, String requestObject) {

        return getAuthzRequestUrl(clientId, callbackUrl) + "&request=" + requestObject;
    }

    private String getAuthzRequestUrl(String clientId, String callbackUrl) {

        return OAuth2Constant.AUTHORIZE_ENDPOINT_URL + "?" + "client_id=" + clientId + "&redirect_uri=" + callbackUrl +
                "&response_type=code&scope=openid%20internal_login";
    }

    private String buildPlainJWT(String consumerKey) {

        JWTClaimsSet jwtClaimsSet = getJwtClaimsSet(consumerKey);
        return new PlainJWT(jwtClaimsSet).serialize();
    }

    private JWTClaimsSet getJwtClaimsSet(String consumerKey) {

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.subject(consumerKey);
        jwtClaimsSetBuilder.issuer(consumerKey);
        jwtClaimsSetBuilder.audience(Collections.singletonList(OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        jwtClaimsSetBuilder.claim("client_id", consumerKey);
        jwtClaimsSetBuilder.issueTime(new Date());
        return jwtClaimsSetBuilder.build();
    }

    private String buildSignedJWT(String consumerKey, RSAPrivateKey privateKey) throws Exception {

        // Create RSA-signer with the private key
        JWSSigner rsaSigner = new RSASSASigner(privateKey);

        // Prepare JWS object with simple string as payload
        JWSHeader.Builder jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256);
        jwsHeader.keyID(UUID.randomUUID().toString());

        SignedJWT signedJWT = new SignedJWT(jwsHeader.build(), getJwtClaimsSet(consumerKey));
        signedJWT.sign(rsaSigner);
        return signedJWT.serialize();
    }

    private void initServiceProviderKeys() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        String pkcs12Path = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp1KeyStore.p12";
        String pkcs12Password = "wso2carbon";

        keyStore.load(Files.newInputStream(Paths.get(pkcs12Path)), pkcs12Password.toCharArray());

        String alias = "wso2carbon";

        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(pkcs12Password.toCharArray()));
        sp1PrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();

        // Load certificate chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        sp1X509PublicCert = (X509Certificate) chain[0];

        // Use another keystore to get sp2 private key.
        pkcs12Path = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp2KeyStore.p12";

        keyStore.load(Files.newInputStream(Paths.get(pkcs12Path)), pkcs12Password.toCharArray());

        pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(pkcs12Password.toCharArray()));
        sp2PrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();
    }
}
