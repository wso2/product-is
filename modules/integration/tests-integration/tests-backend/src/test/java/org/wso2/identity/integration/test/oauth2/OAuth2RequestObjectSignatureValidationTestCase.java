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
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH_APPLICATION_NAME;

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

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet();
        jwtClaimsSet.setSubject(consumerKey);
        jwtClaimsSet.setIssuer(consumerKey);
        jwtClaimsSet.setAudience(Collections.singletonList(OAuth2Constant.ACCESS_TOKEN_ENDPOINT));
        jwtClaimsSet.setClaim("client_id", consumerKey);
        jwtClaimsSet.setIssueTime(new Date());
        return jwtClaimsSet;
    }

    private String buildSignedJWT(String consumerKey, RSAPrivateKey privateKey) throws Exception {
        // Create RSA-signer with the private key
        JWSSigner rsaSigner = new RSASSASigner(privateKey);

        // Prepare JWS object with simple string as payload
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.RS256);
        jwsHeader.setKeyID(UUID.randomUUID().toString());

        SignedJWT signedJWT = new SignedJWT(jwsHeader, getJwtClaimsSet(consumerKey));
        signedJWT.sign(rsaSigner);
        return signedJWT.serialize();
    }

    private void initServiceProviderKeys() throws Exception {

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);

        KeyPair kp = keyGenerator.genKeyPair();
        RSAPublicKey sp1RsaPublicKey = (RSAPublicKey) kp.getPublic();
        sp1PrivateKey = (RSAPrivateKey) kp.getPrivate();

        sp1X509PublicCert = getX509PublicCert(sp1RsaPublicKey, sp1PrivateKey);

        KeyPair sp2KeyPair = keyGenerator.genKeyPair();
        sp2PrivateKey = (RSAPrivateKey) sp2KeyPair.getPrivate();
    }
}
