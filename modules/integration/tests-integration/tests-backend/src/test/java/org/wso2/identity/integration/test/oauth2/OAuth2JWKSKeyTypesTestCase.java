/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.File;
import java.io.IOException;

/**
 * Integration tests for Oauth2 JWKS endpoint to test multiple key types (RSA, EdDSA).
 */
public class OAuth2JWKSKeyTypesTestCase extends ISIntegrationTest {

    private static final String JKWS_URI_SUPER_TENANT = "https://localhost:9853/oauth2/jwks";
    private static final String JKWS_URI_TENANT = "https://localhost:9853/t/wso2.com/oauth2/jwks";

    private static final String KEY_TYPE = "kty";
    private static final String CURVE = "crv";
    private static final String X_COORDINATE = "x";
    private static final String ALGORITHM = "alg";
    private static final String THUMBPRINT = "kid";
    private static final String KEYS = "keys";

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        
        // Copy the test keystore with OKP keys over the default one
        long carbonRuntimeDirId = System.currentTimeMillis();
        String keystorePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "keystores" + File.separator + "products" +
                File.separator + "wso2carbon.p12";
        File testKeystore = new File(keystorePath);
        
        serverConfigurationManager.applyConfiguration(testKeystore, 
            new File(org.wso2.carbon.utils.CarbonUtils.getCarbonHome() + File.separator + "repository" + 
            File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.p12"), 
            true, true);
    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() throws Exception {

        if (serverConfigurationManager != null) {
            serverConfigurationManager.restoreToLastConfiguration();
        }
    }

    @Test(groups = "wso2.is", description = "Test JWKS endpoint for OKP keys support.")
    public void testJWKSKeyTypesEndpoint() throws IOException, JSONException {

        validateKeyTypes(JKWS_URI_SUPER_TENANT);
        validateKeyTypes(JKWS_URI_TENANT);
    }

    private void validateKeyTypes(String jwksEndpoint) throws IOException, JSONException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse httpResponse = sendGetRequest(client, jwksEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");
        JSONObject publicKeySet = new JSONObject(content);

        boolean okpKeyFound = false;

        for (int i = 0; i < publicKeySet.getJSONArray(KEYS).length(); i++) {
            JSONObject keyObj = publicKeySet.getJSONArray(KEYS).getJSONObject(i);
            if (!keyObj.has(KEY_TYPE)) {
                continue;
            }
            String kty = keyObj.getString(KEY_TYPE);

            if ("OKP".equals(kty)) {
                okpKeyFound = true;
                validateKey(keyObj, CURVE, "The curve value");
                validateKey(keyObj, X_COORDINATE, "The x coordinate");
                validateKey(keyObj, THUMBPRINT, "The thumbprint");
                validateKey(keyObj, ALGORITHM, "The algorithm");
                Assert.assertEquals(keyObj.getString(CURVE), "Ed25519", "Incorrect curve for OKP key");
                Assert.assertEquals(keyObj.getString(ALGORITHM), "EdDSA", "Incorrect algorithm for OKP key");
            }
        }

        Assert.assertTrue(okpKeyFound, "OKP (EdDSA) key was not found in the JWKS response. Keystore might be missing Ed25519 key.");
    }

    private void validateKey(JSONObject jsonObject, String key, String errorMessage) throws JSONException {

        if (!jsonObject.has(key)) {
            Assert.fail(errorMessage + " cannot be null");
        }
    }

    private HttpResponse sendGetRequest(HttpClient client, String jwksEndpoint) throws IOException {

        HttpGet getRequest = new HttpGet(jwksEndpoint);
        return client.execute(getRequest);
    }
}
