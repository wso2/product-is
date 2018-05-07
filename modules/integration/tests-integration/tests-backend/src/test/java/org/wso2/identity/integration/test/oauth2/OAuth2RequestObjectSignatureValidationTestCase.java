/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/*
    Integration tests for Signed Request Object validation.
 */
public class OAuth2RequestObjectSignatureValidationTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private RSAPrivateKey sp1PrivateKey;

    private X509Certificate sp1X509PublicCert;

    private RSAPrivateKey sp2PrivateKey;

    private static final String CALLBACK_URL = "https://localhost/callback";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        consumerSecret = null;
        sp1PrivateKey = null;
        sp2PrivateKey = null;
        sp1X509PublicCert = null;
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

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed");
        Assert.assertNotNull(consumerKey);
        Assert.assertNotNull(consumerSecret);
    }

    @Test(groups = "wso2.is", description = "Check Updating public cert of Service Provider",
            dependsOnMethods = "testRegisterApplication")
    public void updateServiceProviderCert() throws Exception {

        ServiceProvider application = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(application);

        application.setCertificateContent(convertToPem(sp1X509PublicCert));
        appMgtclient.updateApplicationData(application);

        ServiceProvider updatedApp = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(updatedApp);
        Assert.assertNotNull(updatedApp.getCertificateContent());
    }

    @Test(groups = "wso2.is", description = "Check Initial OAuth2 Authorize Request",
            dependsOnMethods = "updateServiceProviderCert")
    public void sentAuthorizationGrantRequest() throws Exception {

        HttpClient client = getRedirectDisabledClient();
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL));
        // If the request is valid it will return a 302 to redirect to the login page.
        assertForLoginPage(response);
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Check Initial OAuth2 Authorize Request with unsigned request object",
            dependsOnMethods = "sentAuthorizationGrantRequest")
    public void sendAuthorizationGrantRequestWithPlainJWTRequestObject() throws Exception {

        HttpClient client = getRedirectDisabledClient();
        String unsignedRequestObject = buildPlainJWT(consumerKey);
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, unsignedRequestObject));
        assertForLoginPage(response);
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Check enabling option to enforce request object signature validation",
            dependsOnMethods = "sendAuthorizationGrantRequestWithPlainJWTRequestObject")
    public void testEnforceRequestObjectSignatureValidation() throws Exception {

        OAuthConsumerAppDTO consumerAppDTO = adminClient.getOAuthAppByConsumerKey(consumerKey);
        consumerAppDTO.setRequestObjectSignatureValidationEnabled(true);
        adminClient.updateConsumerApp(consumerAppDTO);
        OAuthConsumerAppDTO updateApp = adminClient.getOAuthAppByConsumerKey(consumerKey);
        Assert.assertTrue(updateApp.getRequestObjectSignatureValidationEnabled());
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation was enforced by sending" +
            " a unsigned request object", dependsOnMethods = "testEnforceRequestObjectSignatureValidation")
    public void sendUnsuccessfulAuthorizationGrantRequestWithPlainJWTRequestObject() throws Exception {

        HttpClient client = getRedirectDisabledClient();
        String unsignedRequestObject = buildPlainJWT(consumerKey);
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, unsignedRequestObject));
        // Since we have enforced request object validation we should be redirected to the error page.
        assertForErrorPage(response);
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation by sending a valid signed" +
            " request object", dependsOnMethods = "sendUnsuccessfulAuthorizationGrantRequestWithPlainJWTRequestObject")
    public void sendSuccessfulAuthorizationGrantRequestWithSignedRequestObject() throws Exception {

        HttpClient client = getRedirectDisabledClient();
        String signedRequestObject = buildSignedJWT(consumerKey, sp1PrivateKey);
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, signedRequestObject));
        assertForLoginPage(response);
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Check request object signature validation by sending an invalid signed" +
            " request object", dependsOnMethods = "sendSuccessfulAuthorizationGrantRequestWithSignedRequestObject")
    public void sendUnSuccessfulAuthorizationGrantRequestWithSignedRequestObjectWithDifferentPrivateKey() throws
            Exception {

        HttpClient client = getRedirectDisabledClient();
        String signedRequestObject = buildSignedJWT(consumerKey, sp2PrivateKey);
        HttpResponse response = sendGetRequest(client, getAuthzRequestUrl(consumerKey, CALLBACK_URL, signedRequestObject));
        assertForErrorPage(response);
        EntityUtils.consume(response.getEntity());
    }

    private void assertForLoginPage(HttpResponse response) {

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains("sessionDataKey="));
        Assert.assertFalse(locationValue.contains("oauthErrorCode=invalid_request"));
    }

    private void assertForErrorPage(HttpResponse response) {

        String locationValue = getLocationHeaderValue(response);
        Assert.assertFalse(locationValue.contains("sessionDataKey="));
        Assert.assertTrue(locationValue.contains("oauthErrorCode=invalid_request"));
    }

    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    private HttpClient getRedirectDisabledClient() {

        HttpClient client = new DefaultHttpClient();
        HttpClientParams.setRedirecting(client.getParams(), false);
        return client;
    }

    private String getAuthzRequestUrl(String clientId, String callbackUrl, String requestObject) {

        return getAuthzRequestUrl(clientId, callbackUrl) + "&request=" + requestObject;
    }

    private String getAuthzRequestUrl(String clientId, String callbackUrl) {

        return OAuth2Constant.AUTHORIZE_ENDPOINT_URL + "?" + "client_id=" + clientId + "&redirect_uri=" + callbackUrl +
                "&response_type=code&scope=openid";
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

        KeyStore keyStore = KeyStore.getInstance("JKS");
        String jksPath = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp1KeyStore.jks";
        String jksPassword = "wso2carbon";

        keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());

        String alias = "wso2carbon";

        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(jksPassword.toCharArray()));
        sp1PrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();

        // Load certificate chain
        Certificate[] chain = keyStore.getCertificateChain(alias);
        sp1X509PublicCert = (X509Certificate) chain[0];

        // Use another keystore to get sp2 private key.
        jksPath = TestConfigurationProvider.getResourceLocation("IS") + File.separator + "sp" +
                File.separator + "keystores" + File.separator + "sp2KeyStore.jks";

        keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());

        pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias,
                new KeyStore.PasswordProtection(jksPassword.toCharArray()));
        sp2PrivateKey = (RSAPrivateKey) pkEntry.getPrivateKey();
    }
}
