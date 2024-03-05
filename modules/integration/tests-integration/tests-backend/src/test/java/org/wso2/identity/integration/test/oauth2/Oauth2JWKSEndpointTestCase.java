/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Integration tests for Oauth2 JWKS endpoint.
 */
public class Oauth2JWKSEndpointTestCase {

    private static final String JKWS_URI_SUPER_TENANT = "https://localhost:9853/oauth2/jwks";
    private static final String JKWS_URI_TENANT = "https://localhost:9853/t/wso2.com/oauth2/jwks";
    private static final String X_509 = "X.509";
    private static final String KEY_TYPE = "kty";
    private static final String EXPONENT = "e";
    private static final String USAGE = "use";
    private static final String THUMBPRINT = "kid";
    private static final String ALGORITHM = "alg";
    private static final String MODULUS = "n";
    private static final String KEYS = "keys";
    private static final String X5C = "x5c";
    private static final String X5T_SHA256 = "x5t#S256";

    @Test(groups = "wso2.is", description = "This test JWKS endpoints.")
    public void testJWKSEndpoint() throws IOException, JSONException {

        getPublicKeySet(JKWS_URI_SUPER_TENANT);
        getPublicKeySet(JKWS_URI_TENANT);
    }

    private void getPublicKeySet(String jwksEndpoint) throws IOException, JSONException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse httpResponse = sendGetRequest(client, jwksEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");
        JSONObject publicKeySet = new JSONObject(content);

        // Validate keys in the JSON object
        validateKey(publicKeySet, KEY_TYPE, "The public key type");
        validateKey(publicKeySet, EXPONENT, "The exponent value of the public key");
        validateKey(publicKeySet, USAGE, "Usage of the key");
        validateKey(publicKeySet, THUMBPRINT, "The thumbprint of the certificate");
        validateKey(publicKeySet, ALGORITHM, "The algorithm");
        validateKey(publicKeySet, MODULUS, "The modulus value of the public key");

        if ((publicKeySet.getJSONArray(KEYS).getJSONObject(0).has(X5C))
                && (publicKeySet.getJSONArray(KEYS).getJSONObject(0).has(X5T_SHA256))) {

            String x5c = String.valueOf(publicKeySet.getJSONArray(KEYS).getJSONObject(0)
                    .getJSONArray(X5C).getString(0));

            String calculatedThumbPrint = getX509CertSHA256Thumbprint(x5c);

            Assert.assertEquals(String.valueOf(publicKeySet.getJSONArray(KEYS).getJSONObject(0).get(X5T_SHA256)),
                    calculatedThumbPrint,"Incorrect x5t#S256 value");
        }
    }

    private void validateKey(JSONObject jsonObject, String key, String errorMessage) throws JSONException {

        if (!jsonObject.getJSONArray(KEYS).getJSONObject(0).has(key)) {
            Assert.fail(errorMessage + " cannot be null");
        }
    }

    private HttpResponse sendGetRequest(HttpClient client, String jwksEndpoint) throws IOException {

        HttpGet getRequest = new HttpGet(jwksEndpoint);
        return client.execute(getRequest);
    }

    private String getX509CertSHA256Thumbprint(String cert) {

        try {
            X509Certificate certificate = parseCertificate(cert);
            JWK parsedJWK = JWK.parse(certificate);
            return parsedJWK.getX509CertSHA256Thumbprint().toString();
        } catch (CertificateException | JOSEException e) {
            Assert.fail("Failed to compute SHA 256 thumb print.", e);
        }
        return null;
    }

    private X509Certificate parseCertificate(String content) throws CertificateException {

        byte[] decodedContent = java.util.Base64.getDecoder().decode(StringUtils.trim(content));

        return (X509Certificate) CertificateFactory.getInstance(X_509)
                .generateCertificate(new ByteArrayInputStream(decodedContent));
    }
}
